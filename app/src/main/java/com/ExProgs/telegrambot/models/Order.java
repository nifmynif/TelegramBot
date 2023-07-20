package com.ExProgs.telegrambot.models;

import androidx.annotation.NonNull;

public class Order {
    private String user;
    private String sticker;
    private String date;
    private String pass;

    @NonNull
    @Override
    public String toString() {
        return "Пользователь: " + user + "\n"
                + "товар: " + sticker + "\n"
                + "дата заказа: " + date + "\n"
                + "выполнен: " + pass + "\n";
    }
}
