package com.ExProgs.telegrambot;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.models.PerformanceSession;
import com.ExProgs.telegrambot.models.TheaterParser;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TheaterParser theaterParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(getApplicationContext());
        theaterParser = new TheaterParser();

        if (!isServiceRunning(this, TelegramBotService.class)) {
            startForegroundService(new Intent(this, TelegramBotService.class));
        }

        Button btnParse = findViewById(R.id.btn_parse);
        btnParse.setOnClickListener(v -> {
            btnParse.setEnabled(false);
            btnParse.setText("Парсинг...");
            new Thread(() -> {
                try {
                    theaterParser.parseAllSites(dbHelper);
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Ошибка ручного парсинга", e);
                }
                runOnUiThread(() -> {
                    fillTable();
                    btnParse.setEnabled(true);
                    btnParse.setText("Парсить сейчас");
                });
            }).start();
        });

        fillTable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillTable();
    }

    private void fillTable() {
        List<PerformanceSession> sessions = dbHelper.getAllSessions();
        int activeSubscribers = dbHelper.getActiveSubscriberCount();

        TextView statsText = findViewById(R.id.stats_text);
        statsText.setText("Спектаклей: " + sessions.size()
                + "  |  Подписчиков: " + activeSubscribers);

        TableLayout table = findViewById(R.id.table);
        table.removeAllViews();

        TableRow header = new TableRow(this);
        header.addView(makeCell("Спектакль", true));
        header.addView(makeCell("Дата", true));
        header.addView(makeCell("✓", true));
        table.addView(header);

        for (PerformanceSession session : sessions) {
            TableRow row = new TableRow(this);
            row.addView(makeCell(session.getTitle(), false));
            row.addView(makeCell(session.getDateTime(), false));
            row.addView(makeCell(session.isSent() ? "✓" : "◌", false));
            table.addView(row);
        }
    }

    private TextView makeCell(String text, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setPadding(8, 4, 8, 4);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
