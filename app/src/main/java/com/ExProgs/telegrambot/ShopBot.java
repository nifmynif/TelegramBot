package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.IOException;

public class ShopBot {
    private final TelegramBot telegramBot;
    private AbstractUser user;
    private Message message;
    private BaseRequest send;

    public ShopBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void setUpdate(Update update) throws IOException {
        this.message = new Message(update);
        if (this.message.getUserName().equals(Admin.userName))
            this.user = new Admin(message.getUserId());
        else
            user = new User(message.getUserId());
        send = null;
        check();
    }


    private void check() throws IOException {
        if (message.isCallbackQuery()) {
            if (user.isAdmin() && message.is0451()) {
                send = new SendMessage(message.getChatId(), "PUKI KAKIf");
            }
        }
        if (message.isMessage()) {
            if (message.isMySticker()) {
                if (user.isAdmin())
                    send = new SendMessage(message.getChatId(), "admin")
                            .replyToMessageId(message.getMessageId())
                            .replyMarkup(user.getMessageKeyboard());
            } else if (message.isPhoto() && user.isAdmin()) {
                send = message.getPhoto(telegramBot);
            } else
                send = new SendMessage(message.getChatId(), "я вас не понимаю(");
        }
        if (send == null)
            send = new SendMessage(message.getChatId(), "я вас не понимаю(");
    }

    public BaseRequest answer() {
        return send;
    }

    public TelegramBot getTelegramBot(){
        return telegramBot;
    }
}
