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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(getApplicationContext());

        if (!isServiceRunning(this, TelegramBotService.class)) {
            startForegroundService(new Intent(this, TelegramBotService.class));
        }

        Button btnParse = findViewById(R.id.btn_parse);
        btnParse.setOnClickListener(v -> {
            btnParse.setEnabled(false);
            btnParse.setText("Парсинг...");
            Intent intent = new Intent(this, TelegramBotService.class);
            intent.setAction(TelegramBotService.ACTION_PARSE_NOW);
            startService(intent);
            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                fillTable();
                btnParse.setEnabled(true);
                btnParse.setText("Парсить сейчас");
            }, 30000);
        });

        Button btnShareLog = findViewById(R.id.btn_share_log);
        btnShareLog.setOnClickListener(v -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, AppLog.dump());
            startActivity(Intent.createChooser(send, "Логи"));
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

        TextView logText = findViewById(R.id.log_text);
        logText.setText(AppLog.dump().isEmpty() ? "Логов пока нет" : AppLog.dump());
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
