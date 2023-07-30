package com.ExProgs.telegrambot.models;

import androidx.annotation.NonNull;

import com.ExProgs.telegrambot.SQL.DBHelper;

public class Product {
    private String name;
    private String price;
    private String description;
    private String stickerId;

    public String getName() {
        return name;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerUniqueId(String stickerUniqueId) {
        this.stickerUniqueId = stickerUniqueId;
    }

    private String stickerUniqueId;
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

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public Product(String id) {
        this.stuffId = id;
    }

    public void add(DBHelper dbHelper) {
        dbHelper.addParam(stuffId,
                "1",
                name);
        dbHelper.addParam(stuffId,
                "2",
                price);
        dbHelper.addParam(stuffId,
                "3",
                description);
        dbHelper.addParam(stuffId,
                "4",
                stickerId);
        dbHelper.addParam(stuffId,
                "5",
                stickerUniqueId);
    }

    public String getStuffId() {
        return stuffId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Название: " + name + "\n"
                + "Цена: " + price + "\n"
                + "Описание: " + description + "\n";
    }
}
