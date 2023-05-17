package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.StickerSet;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.UploadStickerFile;
import com.pengrad.telegrambot.response.GetFileResponse;

public class ShopBot {
    private final Update update;
    private final Keyboard keyboard;
    private Long chatId;
    private boolean isAdmin;
    private boolean isSticker;
    private boolean isCallBackData;
    private final String message = "я вас не понимаю(";

    public ShopBot(Update update) {
        this.update = update;
        keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDD").callbackData("0451"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData("1540"));
        isAdmin = false;
        isSticker = false;
        isCallBackData = false;
        check();
    }


    private void check() {
        if (update.callbackQuery() != null) {
            chatId = update.callbackQuery().message().chat().id();
            isCallBackData = true;
            if (update.callbackQuery().from().id() == 861513948)
                isAdmin = true;
            return;
        }
        if (update.message().sticker() != null) {
            chatId = update.message().chat().id();
            if (update.message().sticker().setName().contains("MyDeusEx")) {
                isSticker = true;
                if (update.message().from().id() == 861513948)
                    isAdmin = true;
            }
            return;
        }
        chatId = update.message().chat().id();
    }

    public SendMessage answer() {
        if (isSticker)
            if (isAdmin)
                return new SendMessage(chatId, "admin").replyMarkup(keyboard).replyToMessageId(update.message().messageId());
            else
                return new SendMessage(chatId, "user");
        if (isCallBackData)
            if (isAdmin)
                return new SendMessage(chatId, update.callbackQuery().message().text());
            else
                return new SendMessage(chatId, "user");
        return new SendMessage(chatId, message);
    }
}
