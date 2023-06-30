package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public abstract class AbstractUser {
    private long id;
    private boolean isAdmin;

    public AbstractUser(long id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public abstract InlineKeyboardMarkup getMessageKeyboard();
    public abstract ReplyKeyboardMarkup getUserKeyboard();
}
