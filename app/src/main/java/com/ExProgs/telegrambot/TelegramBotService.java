package com.ExProgs.telegrambot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.constants.Constants;
import com.ExProgs.telegrambot.models.PerformanceSession;
import com.ExProgs.telegrambot.models.TheaterBot;
import com.ExProgs.telegrambot.models.TheaterParser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelegramBotService extends Service {
    private static final String TAG = "TelegramBotService";
    private static final int PARSE_PERIOD_MINUTES = 3;
    private static final int CLEANUP_PERIOD_HOURS = 24;
    private static final int NOTIFICATION_ID = 1333;

    private DBHelper dbHelper;
    private TheaterBot theaterBot;
    private TheaterParser theaterParser;
    private ScheduledExecutorService scheduler;
    private PowerManager.WakeLock wakeLock;

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

        startForeground(NOTIFICATION_ID, notification);

        if (Constants.TB_TOKEN.isEmpty()) {
            Log.e(TAG, "TELEGRAM_BOT_TOKEN не настроен");
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            startBot();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка запуска бота, сервис продолжает работать", e);
        }

        return START_STICKY;
    }

    private void startBot() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        acquireWakeLock();

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
        }, e -> Log.e(TAG, "Ошибка получения update от Telegram", e));

        scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                List<PerformanceSession> fresh = theaterParser.parseAllSites(dbHelper);
                theaterBot.notifySubscribers(fresh);
                theaterBot.retryPendingDeliveries();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка парсинга/рассылки", e);
            }
        }, 0, PARSE_PERIOD_MINUTES, TimeUnit.MINUTES);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                dbHelper.deleteOldSessions();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка очистки старых сессий", e);
            }
        }, CLEANUP_PERIOD_HOURS, CLEANUP_PERIOD_HOURS, TimeUnit.HOURS);
    }

    private void acquireWakeLock() {
        PowerManager pm = getSystemService(PowerManager.class);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TelegramBot:ParseLock");
            wakeLock.acquire();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduler != null) scheduler.shutdownNow();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
