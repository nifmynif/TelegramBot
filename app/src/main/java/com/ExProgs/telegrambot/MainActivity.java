package com.ExProgs.telegrambot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetFileResponse;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TelegramBot telegramBot = new TelegramBot("6287266463:AAHFzUt2K_kiMZwvaeESt1fyq1rXkDYD3JM");
        telegramBot.setUpdatesListener(updates -> {
                    //ShopBot shopBot = new ShopBot(updates.get(updates.size() - 1));
                    GetFile request = new GetFile(updates.get(updates.size() - 1).message().photo()[1].fileId());
                    GetFileResponse getFileResponse = telegramBot.execute(request);
                    File file = getFileResponse.file();
            System.out.println(telegramBot.getFullFilePath(file).replaceFirst("/", "//"));
                    SendPhoto sendPhoto = new SendPhoto(updates.get(updates.size() - 1).message().chat().id(), new java.io.File(telegramBot.getFullFilePath(file).replaceFirst("/", "//")));
                    telegramBot.execute(sendPhoto);
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
        );
    }
}