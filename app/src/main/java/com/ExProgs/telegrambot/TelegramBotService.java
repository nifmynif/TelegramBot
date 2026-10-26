package com.ExProgs.telegrambot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.constants.Constants;
import com.ExProgs.telegrambot.models.PerformanceSession;
import com.ExProgs.telegrambot.models.TceClient;
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
    public static final String ACTION_PARSE_NOW = "com.ExProgs.telegrambot.PARSE_NOW";
    private static final int PARSE_PERIOD_MINUTES = 60;
    private static final int CLEANUP_PERIOD_HOURS = 24;
    private static final int NOTIFICATION_ID = 1333;

    private DBHelper dbHelper;
    private TheaterBot theaterBot;
    private TheaterParser theaterParser;
    private ScheduledExecutorService scheduler;
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private final java.util.concurrent.atomic.AtomicBoolean cycleRunning =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
        theaterParser = new TheaterParser();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_PARSE_NOW.equals(action) && theaterBot != null) {
            parseNow();
            return START_STICKY;
        }

        NotificationChannel channel = new NotificationChannel(
                Constants.CHANNEL_ID, "Театральный бот", NotificationManager.IMPORTANCE_LOW);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);
        notificationManager = nm;

        Notification notification = new Notification.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("Театральный бот работает")
                .setContentText(Constants.TB_TOKEN.isEmpty()
                        ? "не настроен TELEGRAM_BOT_TOKEN"
                        : "парсинг и рассылка активны")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        if (Constants.TB_TOKEN.isEmpty()) {
            AppLog.e(TAG, "TELEGRAM_BOT_TOKEN не настроен");
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            startBot();
        } catch (Exception e) {
            AppLog.e(TAG, "Ошибка запуска бота, сервис продолжает работать", e);
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
        theaterBot = new TheaterBot(bot, dbHelper, this::parseNow);

        registerUpdatesListener(bot);

        scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleWithFixedDelay(this::runParseCycle,
                0, PARSE_PERIOD_MINUTES, TimeUnit.MINUTES);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                dbHelper.deleteOldSessions();
            } catch (Exception e) {
                AppLog.e(TAG, "Ошибка очистки старых сессий", e);
            }
        }, CLEANUP_PERIOD_HOURS, CLEANUP_PERIOD_HOURS, TimeUnit.HOURS);
    }

    private void registerUpdatesListener(TelegramBot bot) {
        bot.setUpdatesListener(updates -> {
            for (com.pengrad.telegrambot.model.Update update : updates) {
                try {
                    theaterBot.processUpdate(update);
                } catch (Exception e) {
                    AppLog.e(TAG, "Ошибка обработки update", e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            AppLog.e(TAG, "Ошибка получения update от Telegram", e);
            if (scheduler != null) {
                scheduler.schedule(() -> {
                    try {
                        registerUpdatesListener(bot);
                        AppLog.i(TAG, "Listener Telegram перерегистрирован");
                    } catch (Exception ex) {
                        AppLog.e(TAG, "Не удалось перерегистрировать listener", ex);
                    }
                }, 30, TimeUnit.SECONDS);
            }
        });
    }

    private void parseNow() {
        if (scheduler == null) return;
        scheduler.execute(this::runParseCycle);
    }

    private void runParseCycle() {
        if (!cycleRunning.compareAndSet(false, true)) {
            AppLog.i(TAG, "Цикл уже выполняется, повторный запуск пропущен");
            return;
        }
        try {
            List<PerformanceSession> fresh = theaterParser.parseAllSites(dbHelper);
            theaterBot.notifySubscribers(fresh);
            theaterBot.retryPendingDeliveries();
            reportCycleResult(true);
        } catch (Exception e) {
            AppLog.e(TAG, "Ошибка парсинга/рассылки", e);
            reportCycleResult(false);
        } finally {
            cycleRunning.set(false);
        }
    }

    private void reportCycleResult(boolean ok) {
        try {
            String time = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            String text;
            if (ok) {
                int total = 0;
                int unknown = 0;
                for (PerformanceSession session : dbHelper.getAllSessions()) {
                    if (session.isOnSale()) {
                        if (session.getFreeSeats() > 0) {
                            total += session.getFreeSeats();
                        } else if (session.getFreeSeats() == TceClient.UNKNOWN) {
                            unknown++;
                        }
                    }
                }
                text = "Проверка: " + time + " · мест в продаже: " + total;
                if (unknown > 0) {
                    text += " · нет данных tce: " + unknown;
                }
            } else {
                text = "Проверка: " + time + " · ошибка, следующая попытка через "
                        + PARSE_PERIOD_MINUTES + " мин";
            }
            updateStatusNotification(text);
            AppLog.i(TAG, "Цикл завершён: " + text);
        } catch (Exception e) {
            AppLog.e(TAG, "Ошибка обновления уведомления", e);
        }
    }

    private void updateStatusNotification(String text) {
        if (notificationManager == null) return;
        Notification notification = new Notification.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("Театральный бот работает")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
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
