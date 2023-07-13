package com.ExProgs.telegrambot.models;

import androidx.annotation.NonNull;

import com.ExProgs.telegrambot.SQL.DBHelper;

public class Product {
    private String name;
    private String price;
    private String description;
    private String sticker;
    private String num;
    private final String stuffId;

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSticker(String sticker) {
        this.sticker = sticker;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public Product(String id) {
        this.stuffId = id;
    }

    public void add(DBHelper dbHelper) {
        dbHelper.addParam(stuffId,
                "1",
                name,
                null);
        dbHelper.addParam(stuffId,
                "2",
                price,
                null);
        dbHelper.addParam(stuffId,
                "3",
                description,
                null);
        dbHelper.addParam(stuffId,
                "4",
                sticker,
                null);
        dbHelper.addParam(stuffId,
                "5",
                num,
                null);
    }

    @NonNull
    @Override
    public String toString() {
        return "Название: " + name + "\n"
                + "Цена: " + price + "\n"
                + "Описание: " + description + "\n"
                + "Количество: " + num;
    }
}
