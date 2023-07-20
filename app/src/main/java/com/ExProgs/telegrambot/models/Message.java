package com.ExProgs.telegrambot.models;

import android.graphics.*;

import com.ExProgs.telegrambot.constants.Constants;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.GetFileResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Message {
    private final Update update;

    public Message(Update update) {
        this.update = update;
    }

    public long getUserId() {
        if (isCallbackQuery())
            return update.callbackQuery().from().id();
        else
            return update.message().from().id();
    }

    public String getUserName() {
        if (isCallbackQuery())
            return update.callbackQuery().from().username();
        else
            return update.message().from().username();
    }

    public boolean isInfo() {
        return update.callbackQuery().data()
                .substring(update.callbackQuery().data().indexOf('.'))
                .contains("info");
    }

    public boolean isDelete() {
        return update.callbackQuery().data()
                .substring(update.callbackQuery().data().indexOf('.'))
                .contains("delete");
    }
    public boolean isYes() {
        return update.callbackQuery().data()
                .substring(update.callbackQuery().data().indexOf('.'))
                .contains("yes");
    }
    public boolean isNo() {
        return update.callbackQuery().data()
                .substring(update.callbackQuery().data().indexOf('.'))
                .contains("no");
    }

    public boolean isBuy() {
        return update.callbackQuery().data()
                .substring(update.callbackQuery().data().indexOf('.'))
                .contains("buy");
    }

    public int getMessageId() {
        if (isCallbackQuery())
            return update.callbackQuery().message().messageId();
        else
            return update.message().messageId();
    }

    public boolean isPhoto() {
        return update.message().photo() != null;
    }

    public long getChatId() {
        if (isCallbackQuery())
            return update.callbackQuery().message().chat().id();
        else
            return update.message().chat().id();
    }

    public boolean isSticker() {
        if (isMessage())
            return (update.message().sticker() != null);
        else return false;
    }

    public boolean isMySticker() {
        return update.message().sticker().setName().contains("MyDeusEx");
    }

    public String getStickerId() {
        if (isMessage()) {
            if (update.message().sticker() != null && isMessage())
                return update.message().sticker().fileUniqueId();
        } else
            return update.callbackQuery().data().substring(0, update.callbackQuery().data().indexOf("."));
        return "";
    }

    public boolean isCallbackQuery() {
        return update.callbackQuery() != null;
    }

    public boolean isMessage() {
        return update.message() != null;
    }

    public boolean hasText() {
        return update.message().text() != null;
    }

    public String getText() {
        return update.message().text();
    }

    public SendDocument getResizePhoto(TelegramBot telegramBot) throws IOException {
        GetFile request = new GetFile(update.message().photo()[update.message().photo().length - 1].fileId());
        GetFileResponse getFileResponse = telegramBot.execute(request);
        URL url = new URL(Constants.FILE_API + telegramBot.getToken() + "/" + getFileResponse.file().filePath());
        InputStream in = new BufferedInputStream(url.openStream());
        Bitmap originalBitmap = BitmapFactory.decodeStream(in);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 512, 512, false);
        File tempFile = File.createTempFile("photo", ".png");
        FileOutputStream fos = new FileOutputStream(tempFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        fos.write(bitmapData);
        fos.close();
        return new SendDocument(getChatId(), tempFile);
    }
}
