package com.ExProgs.telegrambot.models;

import android.util.Log;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

public class TheaterBot {
    private static final String TAG = "TheaterBot";

    private final TelegramBot bot;
    private final DBHelper dbHelper;

    public TheaterBot(TelegramBot bot, DBHelper dbHelper) {
        this.bot = bot;
        this.dbHelper = dbHelper;
    }

    public TelegramBot getBot() {
        return bot;
    }

    public void processUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) return;

        long chatId = update.message().chat().id();
        String text = update.message().text();
        com.pengrad.telegrambot.model.User from = update.message().from();

        String response;
        switch (text) {
            case "/start":
                registerSubscriber(chatId, from);
                response = "✅ Вы подписались на новые спектакли!";
                break;
            case "/stop":
                unsubscribe(chatId);
                response = "❌ Вы отписались от рассылки.";
                break;
            default:
                response = "Я понимаю только команды /start и /stop.";
        }

        bot.execute(new SendMessage(chatId, response));
    }

    public void sendNewAdToSubscribers() {
        List<PerformanceSession> sessions = dbHelper.findAllUnsent();
        if (sessions.isEmpty()) return;

        List<TelegramSubscriber> subscribers = dbHelper.findAllActiveSubscribers();
        List<Long> failed = new java.util.ArrayList<>();

        for (TelegramSubscriber sub : subscribers) {
            try {
                sendNotifications(sub.getChatId(), sessions);
            } catch (Exception e) {
                failed.add(sub.getChatId());
                Log.w(TAG, "Failed to send to chat " + sub.getChatId(), e);
            }
        }

        dbHelper.markAsSent(sessions);

        if (!failed.isEmpty()) {
            Log.w(TAG, "Failed chats: " + failed);
        }
    }

    private void sendNotifications(long chatId, List<PerformanceSession> sessions) throws InterruptedException {
        for (PerformanceSession session : sessions) {
            String text = "🎭 *" + escapeMarkdownV2(session.getTitle()) + "*\n"
                    + "📅 " + escapeMarkdownV2(session.getDateTime()) + "\n"
                    + "🎟️ [Купить билеты](" + session.getBuyUrl() + ")";

            bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.MarkdownV2));
            Thread.sleep(500);
        }
    }

    private void registerSubscriber(long chatId, com.pengrad.telegrambot.model.User from) {
        TelegramSubscriber sub = dbHelper.findSubscriberByChatId(chatId);
        if (sub == null) {
            sub = new TelegramSubscriber();
            sub.setChatId(chatId);
        }
        if (from != null) {
            sub.setUsername(from.username());
            sub.setFirstName(from.firstName());
            sub.setLastName(from.lastName());
        }
        sub.setActive(true);
        dbHelper.saveSubscriber(sub);
    }

    private void unsubscribe(long chatId) {
        TelegramSubscriber sub = dbHelper.findSubscriberByChatId(chatId);
        if (sub != null) {
            sub.setActive(false);
            dbHelper.saveSubscriber(sub);
        }
    }

    private String escapeMarkdownV2(String text) {
        if (text == null) return "";
        return text
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}
