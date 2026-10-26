package com.ExProgs.telegrambot.models;

import com.ExProgs.telegrambot.AppLog;


import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TceClient {
    private static final String TAG = "TceClient";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36";
    private static final String NO_RESULTS = "Операция не принесла результат";
    private static final Pattern CHALLENGE_PATTERN = Pattern.compile(
            "<script id=\"anubis_challenge\" type=\"application/json\">(.*?)</script>",
            Pattern.DOTALL);

    public static final int UNKNOWN = -1;

    private final Map<String, String> cookies = new HashMap<>();

    public int fetchFreeSeats(String buyUrl) {
        String base = extractParam(buyUrl, "base");
        String data = extractParam(buyUrl, "data");
        if (base == null || data == null) {
            AppLog.w(TAG, "нет base/data в ссылке: " + buyUrl);
            return UNKNOWN;
        }
        String apiUrl = "https://tce.by/index.php?view=shows&action=ticket&kind=json"
                + "&base=" + base + "&data=" + data;
        String body = null;
        try {
            body = getWithAnubis(apiUrl);
            return parseSeats(body, data);
        } catch (Exception e) {
            String snippet = body != null
                    ? body.substring(0, Math.min(200, body.length()))
                    : "no body";
            AppLog.w(TAG, "fetchFreeSeats failed for " + data + ", body: " + snippet, e);
            return UNKNOWN;
        }
    }

    int parseSeats(String body, String what) throws Exception {
        JSONObject obj = new JSONObject(body);
        boolean success = obj.optBoolean("success");
        Object payload = obj.opt("data");
        if (!success) {
            if (payload instanceof String && ((String) payload).startsWith(NO_RESULTS)) {
                return 0;
            }
            AppLog.w(TAG, "tce " + what + ": success=false, ответ: " + payload);
            return UNKNOWN;
        }
        if ("encoded".equals(obj.optString("attr")) && payload instanceof String) {
            payload = new JSONArray((String) payload);
        }
        if (payload instanceof JSONArray) {
            return ((JSONArray) payload).length();
        }
        if (payload instanceof JSONObject) {
            return ((JSONObject) payload).length();
        }
        AppLog.w(TAG, "tce " + what + ": неожиданный формат ответа: " + payload);
        return UNKNOWN;
    }

    private String getWithAnubis(String url) throws Exception {
        Connection.Response response = get(url);
        if (!response.body().contains("anubis_challenge")) {
            return response.body();
        }
        solveAnubis(url, response.body());
        Connection.Response retry = get(url);
        if (retry.body().contains("anubis_challenge")) {
            AppLog.w(TAG, "Anubis challenge not passed, giving up this round");
        }
        return retry.body();
    }

    private Connection.Response get(String url) throws Exception {
        String current = url;
        for (int i = 0; i < 5; i++) {
            Connection.Response response = Jsoup.connect(current)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .cookies(cookies)
                    .execute();
            cookies.putAll(response.cookies());
            int status = response.statusCode();
            if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                String location = response.header("Location");
                if (location == null || location.isEmpty()) {
                    return response;
                }
                current = new java.net.URL(new java.net.URL(current), location).toString();
                continue;
            }
            return response;
        }
        throw new java.io.IOException("Too many redirects: " + url);
    }

    private void solveAnubis(String challengedUrl, String challengePage) throws Exception {
        Matcher matcher = CHALLENGE_PATTERN.matcher(challengePage);
        if (!matcher.find()) return;
        JSONObject challenge = new JSONObject(matcher.group(1).trim());
        JSONObject rules = challenge.getJSONObject("rules");
        int difficulty = rules.getInt("difficulty");
        String randomData = challenge.getJSONObject("challenge").getString("randomData");
        String id = challenge.getJSONObject("challenge").getString("id");

        long startedAt = System.currentTimeMillis();
        String prefix = new String(new char[difficulty]).replace('\0', '0');
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int nonce = 0;
        String hash = "";
        while (true) {
            digest.reset();
            byte[] bytes = digest.digest((randomData + nonce).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hex.append(String.format("%02x", b));
            hash = hex.toString();
            if (hash.startsWith(prefix)) break;
            nonce++;
        }
        Thread.sleep(1500);
        long elapsed = System.currentTimeMillis() - startedAt;

        String passUrl = "https://tce.by/.within.website/x/cmd/anubis/api/pass-challenge"
                + "?id=" + URLEncoder.encode(id, "UTF-8")
                + "&response=" + hash
                + "&nonce=" + nonce
                + "&redir=" + URLEncoder.encode(challengedUrl, "UTF-8")
                + "&elapsedTime=" + elapsed;
        Connection.Response pass = get(passUrl);
        AppLog.i(TAG, "Anubis passed, nonce=" + nonce);
    }

    private String extractParam(String url, String name) {
        Pattern pattern = Pattern.compile("[?&]" + Pattern.quote(name) + "=([^&#]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
}
