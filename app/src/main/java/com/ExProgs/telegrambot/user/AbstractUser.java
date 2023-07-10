package com.ExProgs.telegrambot.user;

import com.pengrad.telegrambot.model.request.Keyboard;

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
    public abstract Keyboard getMessageKeyboard(String id);
    public abstract Keyboard getUserKeyboard();
}
