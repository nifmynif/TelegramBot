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

    public InlineKeyboardMarkup getEditKeyboard(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDB️").callbackData(id + ".editName"),
                new InlineKeyboardButton("\uD83D\uDCB8").callbackData(id + ".editPrice"),
                new InlineKeyboardButton("\uD83D\uDCC3").callbackData(id + ".editDescription"));
    }

    public InlineKeyboardMarkup getAddButton(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83C\uDFAF️").callbackData(id + ".add"));
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public abstract InlineKeyboardMarkup getMessageKeyboard(String id);

    public abstract Keyboard getUserKeyboard();
}
