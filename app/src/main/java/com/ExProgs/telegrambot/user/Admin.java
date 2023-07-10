package com.ExProgs.telegrambot.user;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

import java.util.ArrayList;

public class Admin extends AbstractUser {
    public static ArrayList<String> userName = new ArrayList<>();

    static {
        userName.add("nifmynif");
        userName.add("Zl_Arsuxason");
    }

    public Admin(long id) {
        super(id);
        setAdmin(true);
    }

    public InlineKeyboardMarkup getMessageKeyboard(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCDD").callbackData(id + ".edit"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData(id + ".delete"));
    }

    public ReplyKeyboardMarkup getUserKeyboard() {
        return new ReplyKeyboardMarkup(new KeyboardButton("/add"),
                new KeyboardButton("/del"),
                new KeyboardButton("/check"))
                .resizeKeyboard(true);
    }
}
