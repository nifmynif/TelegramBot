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

    private final TceClient tceClient = new TceClient();

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
                handleNewSession(dbHelper, fresh, title, dateTime, buyUrl);
            } else {
                handleExistingSession(dbHelper, fresh, existing, buyUrl);
            }
        }
    }

    private void handleNewSession(DBHelper dbHelper, List<PerformanceSession> fresh,
                                  String title, String dateTime, String buyUrl) {
        PerformanceSession session = new PerformanceSession();
        session.setTitle(title);
        session.setDateTime(dateTime);
        session.setBuyUrl(buyUrl);
        session.setSent(false);
        session.setStatus(buyUrl.isEmpty()
                ? PerformanceSession.STATUS_UNAVAILABLE
                : PerformanceSession.STATUS_ON_SALE);
        if (!buyUrl.isEmpty()) {
            int seats = tceClient.fetchFreeSeats(buyUrl);
            session.setFreeSeats(seats);
            if (seats == 0) {
                session.setStatus(PerformanceSession.STATUS_UNAVAILABLE);
            }
        }
        dbHelper.saveSession(session);
        Log.i(TAG, "Saved: " + title + " - " + dateTime
                + (session.getFreeSeats() >= 0 ? " (мест: " + session.getFreeSeats() + ")" : ""));
        if (session.isOnSale()) {
            fresh.add(session);
        }
    }

    private void handleExistingSession(DBHelper dbHelper, List<PerformanceSession> fresh,
                                       PerformanceSession existing, String buyUrl) {
        boolean urlChanged = !buyUrl.equals(existing.getBuyUrl());
        if (urlChanged) {
            existing.setBuyUrl(buyUrl);
        }
        if (buyUrl.isEmpty()) {
            if (existing.isOnSale()) {
                existing.setStatus(PerformanceSession.STATUS_UNAVAILABLE);
                dbHelper.updateSession(existing);
            } else if (urlChanged) {
                dbHelper.updateSession(existing);
            }
            return;
        }

        int seats = tceClient.fetchFreeSeats(buyUrl);
        if (seats == TceClient.UNKNOWN) {
            if (urlChanged) {
                dbHelper.updateSession(existing);
            }
            return;
        }
        existing.setFreeSeats(seats);

        if (seats > 0 && !existing.isOnSale()) {
            existing.setStatus(PerformanceSession.STATUS_ON_SALE);
            existing.setSent(false);
            existing.setAvailableAgain(true);
            dbHelper.updateSession(existing);
            dbHelper.clearDeliveries(existing.getId());
            Log.i(TAG, "Available again: " + existing.getTitle() + " - " + existing.getDateTime());
            fresh.add(existing);
        } else if (seats == 0 && existing.isOnSale()) {
            existing.setStatus(PerformanceSession.STATUS_UNAVAILABLE);
            dbHelper.updateSession(existing);
        } else {
            dbHelper.updateSession(existing);
        }
    }
}
