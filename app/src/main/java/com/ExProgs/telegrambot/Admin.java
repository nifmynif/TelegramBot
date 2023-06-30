package com.ExProgs.telegrambot;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public class Admin extends AbstractUser {
    public static String userName = "nifmynf";

    public Admin(long id) {
        super(id);
        setAdmin(true);
    }
    public InlineKeyboardMarkup getMessageKeyboard(){
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDD").callbackData("0451"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData("1540"));
    }
    public ReplyKeyboardMarkup getUserKeyboard(){
        return new ReplyKeyboardMarkup(new KeyboardButton("text")).resizeKeyboard(true);
    }
}
