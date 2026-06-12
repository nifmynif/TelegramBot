package com.ExProgs.telegrambot.models;

import android.util.Log;

import com.ExProgs.telegrambot.SQL.DBHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;

public class TheaterParser {
    private static final String TAG = "TheaterParser";

    private static final List<String> URLS = Arrays.asList(
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/164-pansion-belvedere",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/264-na-chjornoj-chjornoj-ulitse",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/217-zapiski-yunogo-vracha",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/285-khutar",
            "https://puppet-minsk.by/spektakli/spektakli-dlya-vzroslykh/item/282-mroiva"
    );

    public void parseAllSites(DBHelper dbHelper) {
        for (String url : URLS) {
            try {
                parseSite(url, dbHelper);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing: " + url, e);
            }
        }
    }

    private void parseSite(String url, DBHelper dbHelper) throws Exception {
        Document doc = Jsoup.connect(url).timeout(10000).get();
        String title = doc.select(".performance-title").text();

        for (Element dateItem : doc.select(".date-item")) {
            String dateTime = dateItem.select(".date-time p").text();
            String buyUrl = dateItem.select(".performance-buy-ticket a").attr("href");

            if (!dbHelper.existsByTitleAndDateTime(title, dateTime)) {
                PerformanceSession session = new PerformanceSession();
                session.setTitle(title);
                session.setDateTime(dateTime);
                session.setBuyUrl(buyUrl);
                session.setSent(false);
                dbHelper.saveSession(session);
                Log.i(TAG, "Saved: " + title + " - " + dateTime);
            }
        }
    }
}
