package com.ExProgs.telegrambot.models;

import android.annotation.SuppressLint;

import com.ExProgs.telegrambot.SQL.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Order {
    private final String orderId;
    private String user;
    private String sticker;
    private String date;
    private String pass;

    public Order(String orderId) {

        this.orderId = orderId;
    }

    public void setSticker(String sticker) {
        this.sticker = sticker;
    }

    public void setDate(int date) {
        Date dateTime = new Date(date * 1000L);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.date = dateFormat.format(dateTime);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSticker(DBHelper dbHelper) {
        return dbHelper.getStuffBySticker(sticker).getStickerId();
    }

    public void add(DBHelper dbHelper) {
        dbHelper.addParam(orderId,
                "7",
                user);
        dbHelper.addParam(orderId,
                "6",
                sticker);
        dbHelper.addParam(orderId,
                "9",
                date);
        dbHelper.addParam(orderId,
                "8",
                pass);
    }

    public String getOrderId() {
        return orderId;
    }

    public String toStringForUser() {
        return "дата заказа: " + date + "\n"
                + "статус заказа: на рассмотрении";
    }

    public String toStringForAdmin() {
        return "Пользователь: https://t.me/" + user + "\n"
                + "дата заказа: " + date + "\n";
    }
}
