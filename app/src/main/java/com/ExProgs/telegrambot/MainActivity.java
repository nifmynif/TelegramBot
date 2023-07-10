package com.ExProgs.telegrambot;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHelper dbHelper =new DBHelper(getApplicationContext());
        TextView username = findViewById(R.id.username);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button pfb = findViewById(R.id.pfb);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        pfb.setOnClickListener(view -> {
            ClipData data = clipboardManager.getPrimaryClip();
            ClipData.Item item = data.getItemAt(0);
            String text = item.getText().toString().replaceAll("@", "");
            username.setText(text);
        });
        ShopBot shopBot = new ShopBot(new TelegramBot(getResources().getString(R.string.TBToken)), dbHelper);
        shopBot.getTelegramBot().setUpdatesListener(updates -> {
                    try {
                        shopBot.setUpdate(updates.get(updates.size() - 1), username.getText().toString());
                        shopBot.getTelegramBot().execute(shopBot.answer());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
        );
    }
}