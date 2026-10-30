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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.OkHttpClient;

public class TelegramBotService extends Service {
    private static final String TAG = "TelegramBotService";
    public static final String ACTION_PARSE_NOW = "com.ExProgs.telegrambot.PARSE_NOW";
    public static final String ACTION_ALARM_PARSE = "com.ExProgs.telegrambot.ALARM_PARSE";
    private static final int PARSE_PERIOD_MINUTES = 60;
    private static final int CLEANUP_PERIOD_HOURS = 24;
    private static final int NOTIFICATION_ID = 1333;

    private DBHelper dbHelper;
    private TheaterBot theaterBot;
    private TheaterParser theaterParser;
    private ScheduledExecutorService scheduler;
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private volatile long lastCycleStart;
    private volatile long lastUpdateErrorLog;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
        theaterParser = new TheaterParser();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_ALARM_PARSE.equals(action)) {
            keepAwake();
            parseNow();
            scheduleNextAlarm();
            return START_STICKY;
        }
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

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .dns(hostname -> {
                    List<InetAddress> all = Dns.SYSTEM.lookup(hostname);
                    List<InetAddress> ipv4 = new ArrayList<>();
                    for (InetAddress addr : all) {
                        if (addr instanceof Inet4Address) {
                            ipv4.add(addr);
                        }
                    }
                    return ipv4.isEmpty() ? all : ipv4;
                })
                .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        TelegramBot bot = new TelegramBot.Builder(Constants.TB_TOKEN)
                .okHttpClient(httpClient)
                .updateListenerSleep(10_000L)
                .build();
        theaterBot = new TheaterBot(bot, dbHelper, this::parseNow);

        registerUpdatesListener(bot);

        scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                dbHelper.deleteOldSessions();
            } catch (Exception e) {
                AppLog.e(TAG, "Ошибка очистки старых сессий", e);
            }
        }, CLEANUP_PERIOD_HOURS, CLEANUP_PERIOD_HOURS, TimeUnit.HOURS);

        scheduleNextAlarm();
        parseNow();
    }

    private void scheduleNextAlarm() {
        android.app.AlarmManager am = getSystemService(android.app.AlarmManager.class);
        if (am == null) return;
        Intent alarmIntent = new Intent(this, TelegramBotService.class)
                .setAction(ACTION_ALARM_PARSE);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getService(
                this, 0, alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
                        | android.app.PendingIntent.FLAG_IMMUTABLE);
        am.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + PARSE_PERIOD_MINUTES * 60_000L, pendingIntent);
        AppLog.i(TAG, "Будильник парсинга запланирован через " + PARSE_PERIOD_MINUTES + " мин");
    }

    private void keepAwake() {
        if (wakeLock != null) {
            wakeLock.acquire(10 * 60_000L);
        }
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
            long now = System.currentTimeMillis();
            if (now - lastUpdateErrorLog > 60_000L) {
                lastUpdateErrorLog = now;
                AppLog.e(TAG, "Ошибка получения update от Telegram (логирую не чаще раза в минуту)", e);
            }
        });
    }

    private void parseNow() {
        if (scheduler == null) return;
        scheduler.execute(this::runParseCycle);
    }

    private void runParseCycle() {
        long now = System.currentTimeMillis();
        if (now - lastCycleStart < 10 * 60_000L) {
            AppLog.i(TAG, "Цикл уже выполняется, повторный запуск пропущен");
            return;
        }
        lastCycleStart = now;
        try {
            List<PerformanceSession> fresh = theaterParser.parseAllSites(dbHelper);
            theaterBot.notifySubscribers(fresh);
            theaterBot.retryPendingDeliveries();
            reportCycleResult(true);
            broadcastCycleSummary(fresh.size());
        } catch (Exception e) {
            AppLog.e(TAG, "Ошибка парсинга/рассылки", e);
            reportCycleResult(false);
            broadcastCycleSummary(-1);
        }
    }

    private void broadcastCycleSummary(int freshCount) {
        try {
            if (theaterBot == null) return;
            String time = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            String text;
            if (freshCount < 0) {
                text = "⚠️ Парсинг в " + time + " завершился ошибкой, следующая попытка через "
                        + PARSE_PERIOD_MINUTES + " мин";
            } else {
                int total = 0;
                for (PerformanceSession session : dbHelper.getAllSessions()) {
                    if (session.isOnSale() && session.getFreeSeats() > 0) {
                        total += session.getFreeSeats();
                    }
                }
                text = "🔄 Парсинг завершён в " + time + " — "
                        + (freshCount == 0 ? "ничего нового" : "новых показов: " + freshCount)
                        + (total > 0 ? " (мест в продаже: " + total + ")" : "");
            }
            theaterBot.broadcastText(text);
        } catch (Exception e) {
            AppLog.e(TAG, "Ошибка рассылки сводки парсинга", e);
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
