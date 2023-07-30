package com.ExProgs.telegrambot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.constants.Constants;
import com.ExProgs.telegrambot.models.ShopBot;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class TelegramBotService extends Service {
    private final DBHelper dbHelper;

    public TelegramBotService() {
        this.dbHelper = new DBHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel channel = new NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("магазин работает!")
                .setContentText("и делает какуюто фигню...")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(1333, notification.build());

        ShopBot shopBot = new ShopBot(new TelegramBot(getResources().getString(R.string.TBToken)), dbHelper);
        shopBot.getTelegramBot().setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    shopBot.setUpdate(update);
                    shopBot.getTelegramBot().execute(shopBot.answer());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}