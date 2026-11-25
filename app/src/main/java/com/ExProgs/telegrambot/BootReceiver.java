package com.ExProgs.telegrambot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Перезапускает бота после загрузки телефона.
 *
 * Без этого приёмника бот молчал до первого ручного открытия
 * приложения: Android не запускает приложения сам после reboot.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }
        AppLog.i("BootReceiver", "Загрузка телефона, запускаю сервис бота");
        Intent serviceIntent = new Intent(context, TelegramBotService.class);
        context.startForegroundService(serviceIntent);
    }
}
