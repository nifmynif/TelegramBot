package com.ExProgs.telegrambot.models;

import android.util.Log;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.List;

public class TheaterBot {
    private static final String TAG = "TheaterBot";
    private static final long SEND_PAUSE_MS = 500;

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

    public void notifySubscribers(List<PerformanceSession> sessions) {
        if (sessions.isEmpty()) return;
        List<TelegramSubscriber> subscribers = dbHelper.findAllActiveSubscribers();
        for (PerformanceSession session : sessions) {
            deliverToSubscribers(session, subscribers);
        }
    }

    public void retryPendingDeliveries() {
        List<PerformanceSession> pending = dbHelper.findAllUnsent();
        if (pending.isEmpty()) return;
        List<TelegramSubscriber> subscribers = dbHelper.findAllActiveSubscribers();
        for (PerformanceSession session : pending) {
            deliverToSubscribers(session, subscribers);
        }
    }

    private void deliverToSubscribers(PerformanceSession session, List<TelegramSubscriber> subscribers) {
        if (subscribers.isEmpty()) return;
        int delivered = 0;
        for (TelegramSubscriber sub : subscribers) {
            if (dbHelper.deliveryExists(session.getId(), sub.getChatId())) {
                delivered++;
                continue;
            }
            try {
                sendWithRetry(sub, session);
                dbHelper.addDelivery(session.getId(), sub.getChatId());
                delivered++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                Log.w(TAG, "Failed to send to chat " + sub.getChatId(), e);
            }
        }
        if (delivered >= subscribers.size()) {
            dbHelper.markAsSent(session);
        }
    }

    private void sendWithRetry(TelegramSubscriber sub, PerformanceSession session) throws Exception {
        SendMessage request = new SendMessage(sub.getChatId(), buildMessage(session))
                .parseMode(ParseMode.MarkdownV2);
        SendResponse response = bot.execute(request);
        if (isRateLimited(response)) {
            Integer retryAfter = response.parameters().retryAfter();
            Thread.sleep((retryAfter + 1) * 1000L);
            response = bot.execute(request);
        }
        if (!response.isOk()) {
            if (response.errorCode() == 403) {
                sub.setActive(false);
                dbHelper.saveSubscriber(sub);
            }
            throw new IllegalStateException("Send failed, code=" + response.errorCode()
                    + ", chat=" + sub.getChatId());
        }
        Thread.sleep(SEND_PAUSE_MS);
    }

    private boolean isRateLimited(SendResponse response) {
        return !response.isOk()
                && response.errorCode() == 429
                && response.parameters() != null
                && response.parameters().retryAfter() != null;
    }

    private String buildMessage(PerformanceSession session) {
        String header = session.isAvailableAgain() ? "🔔 Снова в продаже" : "🎭 Новый показ";
        String seats = session.getFreeSeats() >= 0
                ? "\n🪑 Свободно мест: " + session.getFreeSeats()
                : "";
        return header + "\n*" + escapeMarkdownV2(session.getTitle()) + "*\n"
                + "📅 " + escapeMarkdownV2(session.getDateTime()) + seats + "\n"
                + "🎟️ [Купить билеты](" + session.getBuyUrl() + ")";
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
