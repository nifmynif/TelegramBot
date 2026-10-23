package com.ExProgs.telegrambot.models;

import android.util.Log;

import com.ExProgs.telegrambot.SQL.DBHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TheaterParser {
    private static final String TAG = "TheaterParser";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13) TheaterBot/1.0";

    private static final List<String> URLS = Arrays.asList(
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/164-pansion-belvedere",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/264-na-chjornoj-chjornoj-ulitse",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/217-zapiski-yunogo-vracha",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/285-khutar",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/282-mroiva"
    );

    public List<PerformanceSession> parseAllSites(DBHelper dbHelper) {
        List<PerformanceSession> fresh = new ArrayList<>();
        for (String url : URLS) {
            try {
                parseSite(url, dbHelper, fresh);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing: " + url, e);
            }
        }
        return fresh;
    }

    private void parseSite(String url, DBHelper dbHelper, List<PerformanceSession> fresh) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get();
        String title = doc.select(".performance-title").text();

        for (Element dateItem : doc.select(".date-item")) {
            String dateTime = dateItem.select(".date-time p").text();
            String buyUrl = dateItem.select(".performance-buy-ticket a").attr("abs:href").trim();
            if (dateTime.isEmpty()) continue;

            PerformanceSession existing = dbHelper.findSession(title, dateTime);
            if (existing == null) {
                saveNewSession(dbHelper, fresh, title, dateTime, buyUrl);
            } else if (!buyUrl.isEmpty() && !existing.isOnSale()) {
                markAvailableAgain(dbHelper, fresh, existing, buyUrl);
            } else if (!buyUrl.isEmpty() && !buyUrl.equals(existing.getBuyUrl())) {
                existing.setBuyUrl(buyUrl);
                dbHelper.updateSession(existing);
            } else if (buyUrl.isEmpty() && existing.isOnSale()) {
                existing.setStatus(PerformanceSession.STATUS_UNAVAILABLE);
                dbHelper.updateSession(existing);
            }
        }
    }

    private void saveNewSession(DBHelper dbHelper, List<PerformanceSession> fresh,
                                String title, String dateTime, String buyUrl) {
        PerformanceSession session = new PerformanceSession();
        session.setTitle(title);
        session.setDateTime(dateTime);
        session.setBuyUrl(buyUrl);
        session.setSent(false);
        session.setStatus(buyUrl.isEmpty()
                ? PerformanceSession.STATUS_UNAVAILABLE
                : PerformanceSession.STATUS_ON_SALE);
        dbHelper.saveSession(session);
        Log.i(TAG, "Saved: " + title + " - " + dateTime);
        if (session.isOnSale()) {
            fresh.add(session);
        }
    }

    private void markAvailableAgain(DBHelper dbHelper, List<PerformanceSession> fresh,
                                    PerformanceSession existing, String buyUrl) {
        existing.setBuyUrl(buyUrl);
        existing.setStatus(PerformanceSession.STATUS_ON_SALE);
        existing.setSent(false);
        existing.setAvailableAgain(true);
        dbHelper.updateSession(existing);
        dbHelper.clearDeliveries(existing.getId());
        Log.i(TAG, "Available again: " + existing.getTitle() + " - " + existing.getDateTime());
        fresh.add(existing);
    }
}
