package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetFileResponse;

public class ShopBot {
    private final TelegramBot telegramBot;
    private final Update update;
    private Keyboard keyboard, keyboard1;

    private final long admin = 2016298598;
    private BaseRequest send;

    public ShopBot(Update update, TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        this.update = update;

        keyboard1 = new ReplyKeyboardMarkup(new KeyboardButton("text")).resizeKeyboard(true);
        keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDD").callbackData("0451"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData("1540"));
        send = null;
        check();
    }


    private void check() {
        if (update.callbackQuery() != null) {
            if (update.callbackQuery().from().id() == admin && update.callbackQuery().data().contains("0451")) {
                send = new SendMessage(update.callbackQuery().message().chat().id(), "PUKI KAKI");
            }
        }
        if (update.message() != null) {
            if (update.message().sticker().setName().contains("MyDeusEx") && update.message().from().id() == admin) {
                send = new SendMessage(update.message().chat().id(), "admin")
                        .replyToMessageId(update.message().messageId())
                        .replyMarkup(keyboard)
                        .replyMarkup(keyboard1);
            } else if (update.message().photo() != null && update.message().from().id() == admin) {
                GetFile request = new GetFile(update.message().photo()[4].fileId());
                GetFileResponse getFileResponse = telegramBot.execute(request);
                File file = getFileResponse.file();
                send = new SendPhoto(update.message().chat().id(), file.fileId());
            } else
                send = new SendMessage(update.message().chat().id(), "я вас не понимаю(");
        }
        if (send == null)
            if (update.message() != null)
                send = new SendMessage(update.message().chat().id(), "я вас не понимаю(");
            else
                send = new SendMessage(update.callbackQuery().message().chat().id(), "я вас не понимаю(");
    }

    public BaseRequest answer() {
        return send;
    }
}
