package com.ExProgs.telegrambot.models.user;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public class User extends AbstractUser {
    public User(long id) {
        super(id);
        setAdmin(false);
    }

    public InlineKeyboardMarkup getMessageKeyboard(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("БЕРУ\uD83E\uDD18").callbackData(id + ".buy"));
    }

    public ReplyKeyboardMarkup getUserKeyboard() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton("/orders"));
    }
}
