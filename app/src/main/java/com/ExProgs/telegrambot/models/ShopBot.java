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
    private final DBHelper dbHelper;
    private AbstractUser user;
    private Message message;
    private BaseRequest send;
    private Product product;

    public ShopBot(TelegramBot telegramBot, DBHelper dbHelper) {
        this.telegramBot = telegramBot;
        this.dbHelper = dbHelper;
    }

    public void setUpdate(Update update) throws IOException {
        try {
            this.message = new Message(update);
            user = new User(message.getUserId());
            for (String name : Admin.USERNAMES) {
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

    private EditMessageText editCurrentMessage(String text, InlineKeyboardMarkup keyboard) {
        return new EditMessageText(message.getChatId(), message.getMessageId(), text)
                .replyMarkup(keyboard);
    }

    private Product getCurrentProduct() {
        return dbHelper.getStuffBySticker(message.getStickerUniqueId());
    }

    private void callbackQueryResponse() {
        if (user.isAdmin()) {
            if (message.isInfo()) {
                product = getCurrentProduct();
                if (product == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                send = new EditMessageText(message.getChatId(),
                        message.getMessageId(),
                        "id товара: `" + product.getStickerId() + "`\n"
                                + "id стикера: `" + product.getStickerUniqueId() + "`")
                        .parseMode(ParseMode.Markdown)
                        .replyMarkup(user.getMessageKeyboard(message.getStickerUniqueId()));
            } else if (message.isEdit()) {
                product = getCurrentProduct();
                if (product == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                send = editCurrentMessage(product.toString(), user.getEditKeyboard(message.getStickerUniqueId()));
            } else if (message.isAdd()) {
                String productId = dbHelper.addStuff(message.getStickerId());
                product = new Product(productId);
                product.setStickerUniqueId(message.getStickerUniqueId());
                product.setStickerId(message.getStickerId());
                product.add(dbHelper);
                send = editCurrentMessage("товар добавлен", user.getMessageKeyboard(message.getStickerUniqueId()));
            } else if (message.isDelete()) {
                product = getCurrentProduct();
                if (product == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                send = editCurrentMessage("вы точно хотите удалить данный товар?",
                        user.getYesNoKeyboard(message.getStickerUniqueId()));
            } else if (message.isYes()) {
                product = getCurrentProduct();
                if (product == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                send = editCurrentMessage("товар удалён", new InlineKeyboardMarkup());
                dbHelper.deleteOrdersBySticker(message.getStickerUniqueId());
                dbHelper.deleteStuff(product.getStuffId());
            } else if (message.isNo()) {
                product = getCurrentProduct();
                if (product == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                send = editCurrentMessage(product.toString(), user.getMessageKeyboard(message.getStickerUniqueId()));
            } else if (message.isEditName()) {
                if (getCurrentProduct() == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                dbHelper.addEdit(message.getStickerUniqueId(), "1");
                send = editCurrentMessage("введите имя товара", new InlineKeyboardMarkup());
            } else if (message.isEditPrice()) {
                if (getCurrentProduct() == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                dbHelper.addEdit(message.getStickerUniqueId(), "2");
                send = editCurrentMessage("введите цену товара", new InlineKeyboardMarkup());
            } else if (message.isEditDescription()) {
                if (getCurrentProduct() == null) {
                    send = editCurrentMessage(Constants.NO_STUFF, new InlineKeyboardMarkup());
                    return;
                }
                dbHelper.addEdit(message.getStickerUniqueId(), "3");
                send = editCurrentMessage("введите описание товара", new InlineKeyboardMarkup());
            }
        } else if (message.isBuy()) {
            product = getCurrentProduct();
            if (product == null) {
                send = new SendMessage(message.getChatId(), Constants.NO_STUFF);
                return;
            }
            Order order = dbHelper.getOrderByStickerAndUser(message.getStickerUniqueId(), message.getUserName());
            if (order == null) {
                String orderId = dbHelper.addOrder(message.getStickerUniqueId());
                order = new Order(orderId);
                order.setUser(message.getUserName());
                order.setDate(message.getDate());
                order.setPass(Constants.NO);
                order.setSticker(message.getStickerUniqueId());
                order.add(dbHelper);
            }
            send = new SendMessage(message.getChatId(), "Хороший выбор! скоро с вами свяжутся для согласования оплаты и выбора дотавки");
        }
    }

    private void stickerResponse() {
        if (message.isMySticker())
            if (user.isAdmin()) {
                product = dbHelper.getStuffBySticker(message.getStickerUniqueId());
                if (product == null)
                    send = new SendMessage(message.getChatId(), Constants.NO_STUFF)
                            .replyToMessageId(message.getMessageId())
                            .replyMarkup(user.getAddButton(message.getStickerUniqueId()));
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
                    ArrayList<Product> stuffs = dbHelper.getStuffs();
                    if (stuffs.size() == 0)
                        send = new SendMessage(message.getChatId(), "нет товаров");
                    else {
                        send = null;
                        for (Product product : stuffs) {
                            telegramBot.execute(new SendSticker(message.getChatId(), product.getStickerId()));
                            telegramBot.execute(new SendMessage(message.getChatId(), product.toString()));
                        }
                    }
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
            case "/cancel":
                dbHelper.delEdit();
                send = new SendMessage(message.getChatId(), "последняя операция отменена");
                break;
            case "dropdatabase":
            case "/dropdatabase":
            case "dropDatabase":
            case "/dropDatabase":
            case "DropDatabase":
            case "/DropDatabase":
                send = new SendMessage(message.getChatId(), "хорошая попытка "
                        + message.getUserName() + " >:(");
                break;
            default:
                if (user.isAdmin()) {
                    Cursor cursor = dbHelper.getEdit();
                    while (cursor.moveToNext()) {
                        product = dbHelper.getStuffBySticker(cursor.getString(cursor.getColumnIndex(JDB.KEY_OBJECT_ID)));
                        if (cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID)).equals("1")) {
                            product.setName(text);
                            dbHelper.delEdit();
                        }
                        if (cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID)).equals("2")) {
                            product.setPrice(text);
                            dbHelper.delEdit();
                        }
                        if (cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID)).equals("3")) {
                            product.setDescription(text);
                            dbHelper.delEdit();
                        }
                        product.add(dbHelper);
                        send = new SendMessage(message.getChatId(), "товар обновлён");
                    }
                    cursor.close();
                }
        }
    }

    public BaseRequest answer() {
        return send;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }
}
