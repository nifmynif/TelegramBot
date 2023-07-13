package com.ExProgs.telegrambot.models.user;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public class User extends AbstractUser{
    public User(long id) {
        super(id);
        setAdmin(false);
    }
    public InlineKeyboardMarkup getMessageKeyboard(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDD").callbackData(id + ".edit"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData(id + ".delete"));
    }
    public ReplyKeyboardMarkup getUserKeyboard(){
        return null;
    }
}
