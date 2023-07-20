package com.ExProgs.telegrambot.models.user;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;

public abstract class AbstractUser {
    private final long id;
    private boolean isAdmin;

    public AbstractUser(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public InlineKeyboardMarkup getYesNoKeyboard(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("ДА\uD83E\uDD18️").callbackData(id + ".yes"),
                new InlineKeyboardButton("НЕТ\uD83D\uDD95").callbackData(id + ".no"));
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public abstract InlineKeyboardMarkup getMessageKeyboard(String id);

    public abstract Keyboard getUserKeyboard();
}
