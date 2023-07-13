package com.ExProgs.telegrambot.models.user;

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

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public abstract Keyboard getMessageKeyboard(String id);
    public abstract Keyboard getUserKeyboard();
}
