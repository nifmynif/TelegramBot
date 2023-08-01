package com.ExProgs.telegrambot.models;

import androidx.annotation.NonNull;

import com.ExProgs.telegrambot.SQL.DBHelper;

public class Product {
    private final String stuffId;
    private String name;
    private String price;
    private String description;
    private String stickerId;
    private String stickerUniqueId;

    public Product(String id) {
        this.stuffId = id;
    }

    public void add(DBHelper dbHelper) {
        if (name != null)
            dbHelper.addParam(stuffId,
                    "1",
                    name);
        if (price != null)
            dbHelper.addParam(stuffId,
                    "2",
                    price);
        if (description != null)
            dbHelper.addParam(stuffId,
                    "3",
                    description);
        if (stickerId != null)
            dbHelper.addParam(stuffId,
                    "4",
                    stickerId);
        if (stickerUniqueId != null)
            dbHelper.addParam(stuffId,
                    "5",
                    stickerUniqueId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public String getStickerUniqueId() {
        return stickerUniqueId;
    }

    public void setStickerUniqueId(String stickerUniqueId) {
        this.stickerUniqueId = stickerUniqueId;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
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
