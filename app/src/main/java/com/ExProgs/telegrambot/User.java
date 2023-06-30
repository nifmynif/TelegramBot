package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public class User extends AbstractUser{
    public User(long id) {
        super(id);
        setAdmin(false);
    }
    public InlineKeyboardMarkup getMessageKeyboard(){
        return null;
    }
    public ReplyKeyboardMarkup getUserKeyboard(){
        return null;
    }
}
