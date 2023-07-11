package com.ExProgs.telegrambot;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.SQL.JDB;
import com.ExProgs.telegrambot.user.AbstractUser;
import com.ExProgs.telegrambot.user.Admin;
import com.ExProgs.telegrambot.user.User;
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
    private final DBHelper dbHelper;

    public ShopBot(TelegramBot telegramBot, DBHelper dbHelper) {
        this.telegramBot = telegramBot;
        this.dbHelper = dbHelper;
    }

    public void setUpdate(Update update, String username) throws IOException {
        this.message = new Message(update);
        Admin.userName.add(username);
        for (String name : Admin.userName) {
            if (this.message.getUserName() != null)
                if (this.message.getUserName().equals(name)) {
                    this.user = new Admin(message.getUserId());
                    break;
                } else
                    user = new User(message.getUserId());
        }
        send = new SendMessage(message.getChatId(), Constants.DEFAULT);
        check();
    }

    @SuppressLint("Range")
    private void check() throws IOException {
        if (message.isMessage()) {
            if (message.isMySticker()) {
                if (user.isAdmin())
                    send = new SendMessage(message.getChatId(), "admin")
                            .replyToMessageId(message.getMessageId())
                            .replyMarkup(user.getMessageKeyboard("123456"));
                else
                    send = new SendMessage(message.getChatId(), "user")
                            .replyToMessageId(message.getMessageId())
                            .replyMarkup(user.getMessageKeyboard("123456"));
            } else if (message.isPhoto() && user.isAdmin())
                send = message.getPhoto(telegramBot);
            else if (message.hasText()) {
                switch (message.getText()) {
                    case "/start":
                        send = new SendMessage(message.getChatId(), Constants.START)
                                .replyMarkup(user.getUserKeyboard());
                        break;
                    case "/add":
                        if (user.isAdmin())
                            dbHelper.addStuff("трусы трусы трусы трусы трусы трусы");
                        break;
                    case "/check":
                        Cursor cursor = dbHelper.getStuffs();
                        if (cursor.moveToFirst())
                            send = new SendMessage(message.getChatId(), cursor.getString(cursor.getColumnIndex(JDB.KEY_NAME)));
                        cursor.close();
                }
            }
        }
        if (message.isCallbackQuery()) {
            if (user.isAdmin() && message.isEdit()) {
                send = new SendMessage(message.getChatId(), "PUKI KAKI");
            }
        }
    }

    public BaseRequest answer() {
        return send;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }
}
