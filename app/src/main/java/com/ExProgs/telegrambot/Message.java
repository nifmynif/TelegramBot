package com.ExProgs.telegrambot;

import android.graphics.*;

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

    public boolean is0451() {
        return update.callbackQuery().data().contains("0451");
    }

    public int getMessageId() {
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

    public boolean isMySticker() {
        if (isMessage())
            if (update.message().sticker() != null && isMessage())
                return update.message().sticker().setName().contains("MyDeusEx");
        return false;
    }

    public boolean isCallbackQuery() {
        return update.callbackQuery() != null;
    }

    public boolean isMessage() {
        return update.message() != null;
    }

    public SendDocument getPhoto(TelegramBot telegramBot) throws IOException {
        GetFile request = new GetFile(update.message().photo()[update.message().photo().length - 1].fileId());
        GetFileResponse getFileResponse = telegramBot.execute(request);
        URL url = new URL("https://api.telegram.org/file/bot" + telegramBot.getToken() + "/" + getFileResponse.file().filePath());
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
