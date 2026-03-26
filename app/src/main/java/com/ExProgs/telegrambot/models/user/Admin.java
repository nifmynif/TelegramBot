package com.ExProgs.telegrambot.models.user;

import com.ExProgs.telegrambot.BuildConfig;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Admin extends AbstractUser {
    public static final List<String> USERNAMES = parseUsernames(BuildConfig.ADMIN_USERNAMES);

    public Admin(long id) {
        super(id);
        setAdmin(true);
    }

    private static List<String> parseUsernames(String rawUsernames) {
        if (rawUsernames == null || rawUsernames.trim().isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<String> usernames = new ArrayList<>();
        for (String username : rawUsernames.split(",")) {
            String trimmedUsername = username.trim();
            if (!trimmedUsername.isEmpty()) {
                usernames.add(trimmedUsername);
            }
        }
        return Collections.unmodifiableList(usernames);
    }

    public InlineKeyboardMarkup getMessageKeyboard(String id) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("\uD83D\uDCBD️").callbackData(id + ".info"),
                new InlineKeyboardButton("⚒ ️").callbackData(id + ".edit"),
                new InlineKeyboardButton("\uD83D\uDDD1").callbackData(id + ".delete"));
    }

    public ReplyKeyboardMarkup getUserKeyboard() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton("/stuffs"),
                new KeyboardButton("/orders"),
                new KeyboardButton("/cancel"))
                .resizeKeyboard(true);
    }
}
