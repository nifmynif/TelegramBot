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
import com.ExProgs.telegrambot.models.ShopBot;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class TelegramBotService extends Service {
    private static final String TAG = "TelegramBotService";
    private DBHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel channel = new NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        Notification.Builder notification = new Notification.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("магазин работает!")
                .setContentText(Constants.TB_TOKEN.isEmpty()
                        ? "не настроен TELEGRAM_BOT_TOKEN"
                        : "и делает какуюто фигню...")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(1333, notification.build());

        if (Constants.TB_TOKEN.isEmpty()) {
            Log.e(TAG, "TELEGRAM_BOT_TOKEN is not configured. Set it via Gradle property or environment variable.");
            stopSelf();
            return START_NOT_STICKY;
        }

        ShopBot shopBot = new ShopBot(new TelegramBot(Constants.TB_TOKEN), dbHelper);
        shopBot.getTelegramBot().setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    shopBot.setUpdate(update);
                    shopBot.getTelegramBot().execute(shopBot.answer());
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to process Telegram update", e);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
