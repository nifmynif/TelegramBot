package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetFileResponse;

public class ShopBot {
    private final TelegramBot telegramBot;
    private final Update update;
    private final Keyboard keyboard;
    private final long admin = 861513947;
    private final String message = "я вас не понимаю(";
    private BaseRequest send;

    public ShopBot(Update update, TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        this.update = update;
        keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDD").callbackData("0451"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData("1540"));
        check();
    }


    private void check() {
        send = new SendMessage(update.message().chat().id(), "я вас не понимаю(");
        if (update.callbackQuery() != null) {
            if (update.callbackQuery().from().id() == admin)
                send = new SendMessage(update.callbackQuery().message().chat().id(), update.callbackQuery().message().text());
        }
        if (update.message().sticker() != null) {
            if (update.message().sticker().setName().contains("MyDeusEx")) {
                if (update.message().from().id() == admin)
                    send = new SendMessage(update.message().chat().id(), "admin")
                            .replyToMessageId(update.message().messageId())
                            .replyMarkup(keyboard);
                else
                    send = new SendMessage(update.message().chat().id(), "user");
            } else
                send = new SendMessage(update.message().chat().id(), "я вас не понимаю(");
        }
        if (update.message().photo() != null && update.message().from().id() == admin) {
            GetFile request = new GetFile(update.message().photo()[4].fileId());
            GetFileResponse getFileResponse = telegramBot.execute(request);
            File file = getFileResponse.file();
            send = new SendPhoto(update.message().chat().id(), file.fileId());
        }
    }

    public BaseRequest answer() {
        return send;
    }
}
