package com.ExProgs.telegrambot;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TelegramBot telegramBot = new TelegramBot("6287266463:AAHFzUt2K_kiMZwvaeESt1fyq1rXkDYD3JM");
        telegramBot.setUpdatesListener(updates -> {
                    ShopBot shopBot = new ShopBot(updates.get(updates.size() - 1), telegramBot);
                    telegramBot.execute(shopBot.answer());
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
        );
    }
}