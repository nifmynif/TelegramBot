package com.ExProgs.telegrambot.models;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.SQL.JDB;
import com.ExProgs.telegrambot.constants.Constants;
import com.ExProgs.telegrambot.models.user.AbstractUser;
import com.ExProgs.telegrambot.models.user.Admin;
import com.ExProgs.telegrambot.models.user.User;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;

import java.io.IOException;
import java.util.ArrayList;

public class ShopBot {
    private final TelegramBot telegramBot;
    private AbstractUser user;
    private Message message;
    private BaseRequest send;
    private final DBHelper dbHelper;
    private Product product;

    public ShopBot(TelegramBot telegramBot, DBHelper dbHelper) {
        this.telegramBot = telegramBot;
        this.dbHelper = dbHelper;
    }

    public void setUpdate(Update update) throws IOException {
        try {
            this.message = new Message(update);
            user = new User(message.getUserId());
            for (String name : Admin.userName) {
                if (this.message.getUserName() != null)
                    if (this.message.getUserName().equals(name)) {
                        this.user = new Admin(message.getUserId());
                        break;
                    }
            }
            send = new SendMessage(message.getChatId(), Constants.DEFAULT);
            check();
        } catch (IllegalArgumentException e) {
            send = new SendMessage(message.getChatId(), e.getMessage());
        }
    }

    private void check() throws IOException {
        if (message.isMessage()) {
            if (message.isSticker())
                stickerResponse();
            else if (message.hasText())
                textResponse(message.getText());
            else if (message.isPhoto() && user.isAdmin())
                send = message.getResizePhoto(telegramBot);
        } else if (message.isCallbackQuery()) {
            callbackQueryResponse();
        }
    }

    private void callbackQueryResponse() {
        if (user.isAdmin()) {
            if (message.isInfo()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                send = new EditMessageText(message.getChatId(),
                        message.getMessageId(),
                        product.toString())
                        .replyMarkup(user.getMessageKeyboard(message.getStickerUniqueId()));
            }
            if (message.isDelete()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                send = new EditMessageText(message.getChatId(),
                        message.getMessageId(),
                        "вы точно хотите удалить данный товар?")
                        .replyMarkup(user.getYesNoKeyboard(message.getStickerUniqueId()));
            }
            if (message.isYes()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                send = new EditMessageText(message.getChatId(),
                        message.getMessageId(),
                        "товар удалён")
                        .replyMarkup(new InlineKeyboardMarkup());
                Order order = dbHelper.getOrderBySticker(message.getStickerUniqueId());
                if (order != null)
                    dbHelper.deleteOrder(order.getOrderId());
                dbHelper.deleteStuff(product.getStuffId());
            }
            if (message.isNo()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                send = new EditMessageText(message.getChatId(),
                        message.getMessageId(),
                        product.toString())
                        .replyMarkup(user.getMessageKeyboard(message.getStickerUniqueId()));
            }
        } else {
            if (message.isBuy()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                dbHelper.addOrder(message.getStickerUniqueId());
                Order order = dbHelper.getOrderBySticker(message.getStickerUniqueId());
                order.setUser(message.getUserName());
                order.setDate(message.getDate());
                order.setPass(Constants.NO);
                order.setSticker(message.getStickerUniqueId());
                order.add(dbHelper);
                send = new SendMessage(message.getChatId(), "Хороший выбор! скоро с вами свяжутся для согласования оплаты и выбора дотавки");
            }
        }
    }

    private void stickerResponse() {
        if (message.isMySticker())
            if (user.isAdmin()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                if (product == null)
                    send = new SendMessage(message.getChatId(),
                            "id товара: `" + message.getStickerUniqueId() + "`\n"
                                    + "id стикера: `" + message.getStickerId() + "`")
                            .replyToMessageId(message.getMessageId())
                            .parseMode(ParseMode.Markdown);
                else
                    send = new SendMessage(message.getChatId(), product.toString())
                            .replyToMessageId(message.getMessageId())
                            .replyMarkup(user.getMessageKeyboard(message.getStickerUniqueId()));
            } else {
                try {
                    product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                    send = new SendMessage(message.getChatId(), product.toString())
                            .replyToMessageId(message.getMessageId())
                            .replyMarkup(user.getMessageKeyboard(message.getStickerUniqueId()));
                } catch (Exception e) {
                    send = new SendMessage(message.getChatId(), Constants.NO_STUFF);
                }
            }
        else
            send = new SendMessage(message.getChatId(), Constants.DEFAULT);
    }

    @SuppressLint("Range")
    private void textResponse(String text) {
        switch (text) {
            case "/start":
                send = new SendMessage(message.getChatId(), Constants.START)
                        .replyMarkup(user.getUserKeyboard());
                break;
            case "/stuffs":
                if (user.isAdmin()) {
                    Cursor cursor = dbHelper.getStuffs();
                    StringBuilder result = new StringBuilder();
                    if (cursor.getCount() == 0)
                        send = new SendMessage(message.getChatId(), "нет товаров");
                    else {
                        while (cursor.moveToNext())
                            result.append("название: ")
                                    .append(cursor.getString(cursor.getColumnIndex(JDB.KEY_NAME)))
                                    .append(" | id: ")
                                    .append(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)))
                                    .append("\n");
                        send = new SendMessage(message.getChatId(), result.toString());
                    }
                    cursor.close();
                }
                break;
            case "/orders":
                ArrayList<Order> orders;
                StringBuilder result;
                if (user.isAdmin())
                    orders = dbHelper.getOrders();
                else
                    orders = dbHelper.getUserOrders(message.getUserName());
                for (Order order : orders) {
                    result = new StringBuilder();
                    if (user.isAdmin())
                        result.append(order.toStringForAdmin());
                    else
                        result.append(order.toStringForUser());
                    telegramBot.execute(new SendSticker(message.getChatId(), order.getSticker(dbHelper)));
                    telegramBot.execute(new SendMessage(message.getChatId(), result.toString()));
                }
                if (orders.size() == 0)
                    send = new SendMessage(message.getChatId(), "нет заказов(");
                else
                    send = null;
                break;
            case "dropdatabase":
            case "/dropdatabase":
            case "dropDatabase":
            case "/dropDatabase":
            case "DropDatabase":
            case "/DropDatabase":
                send = new SendMessage(message.getChatId(), "хорошая попытка "
                        + message.getUserName() + " >:(");
        }
    }

    public BaseRequest answer() {
        return send;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }
}
