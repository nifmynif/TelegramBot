package com.ExProgs.telegrambot;

import android.graphics.*;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetFileResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public SendPhoto getPhoto(TelegramBot telegramBot) throws IOException {
        GetFile request = new GetFile(update.message().photo()[update.message().photo().length - 1].fileId());
        GetFileResponse getFileResponse = telegramBot.execute(request);
        URL url = new URL("https://api.telegram.org/file/bot" + telegramBot.getToken() + "/" + getFileResponse.file().filePath());
        Bitmap originalBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 512, 512, false);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return new SendPhoto(getChatId(), os.toByteArray());
    }
}
