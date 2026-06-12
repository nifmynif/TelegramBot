package com.ExProgs.telegrambot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.constants.Constants;
import com.ExProgs.telegrambot.models.TheaterBot;
import com.ExProgs.telegrambot.models.TheaterParser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelegramBotService extends Service {
    private static final String TAG = "TelegramBotService";

    private DBHelper dbHelper;
    private TheaterBot theaterBot;
    private TheaterParser theaterParser;
    private ScheduledExecutorService scheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
        theaterParser = new TheaterParser();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel channel = new NotificationChannel(
                Constants.CHANNEL_ID, "Театральный бот", NotificationManager.IMPORTANCE_LOW);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("Театральный бот работает")
                .setContentText(Constants.TB_TOKEN.isEmpty()
                        ? "не настроен TELEGRAM_BOT_TOKEN"
                        : "парсинг и рассылка активны")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(1333, notification);

        if (Constants.TB_TOKEN.isEmpty()) {
            Log.e(TAG, "TELEGRAM_BOT_TOKEN не настроен");
            stopSelf();
            return START_NOT_STICKY;
        }
        Log.e(TAG, "TELEGRAM_BOT_TOKEN " + Constants.TB_TOKEN);


        TelegramBot bot = new TelegramBot(Constants.TB_TOKEN);
        theaterBot = new TheaterBot(bot, dbHelper);

        bot.setUpdatesListener(updates -> {
            for (com.pengrad.telegrambot.model.Update update : updates) {
                try {
                    theaterBot.processUpdate(update);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка обработки update", e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                theaterParser.parseAllSites(dbHelper);
                theaterBot.sendNewAdToSubscribers();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка парсинга/рассылки", e);
            }
        }, 0, 60, TimeUnit.MINUTES);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                dbHelper.deleteOldSessions();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка очистки старых сессий", e);
            }
        }, 24, 24, TimeUnit.HOURS);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduler != null) scheduler.shutdownNow();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
