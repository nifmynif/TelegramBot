package com.ExProgs.telegrambot;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.SQL.JDB;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;


public class MainActivity extends AppCompatActivity {
    private TextView username;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(getApplicationContext());
        username = findViewById(R.id.username);
        createPfbButton();
        createBot();
        fillTable();
    }

    private void createPfbButton() {
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button pfb = findViewById(R.id.pfb);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        pfb.setOnClickListener(view -> {
            ClipData data = clipboardManager.getPrimaryClip();
            ClipData.Item item = data.getItemAt(0);
            String text = item.getText().toString().replaceAll("@", "");
            username.setText(text);
        });
    }

    private void createBot() {
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

    private void fillTable() {
        TableLayout tableLayout = findViewById(R.id.table);
        tableLayout.removeAllViews();
        Cursor cursor = dbHelper.getStuffs();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(JDB.KEY_NAME));
            @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(JDB.KEY_ID));
            TableRow tableRow = new TableRow(this);
            TextView textName = new TextView(this);
            textName.setText(name);
            textName.setTextSize(20);
            textName.setMaxWidth(150);
            TextView textId = new TextView(this);
            textId.setText(id);
            textId.setTextSize(20);
            Button buttonEdit = new Button(this);
            buttonEdit.setText("\uD83D\uDCDD");
            buttonEdit.setId(Integer.parseInt(id));
            buttonEdit.setOnClickListener(v -> {
                System.out.println("edit");
            });
            Button buttonDelete = new Button(this);
            buttonDelete.setText("\uD83D\uDDD1");
            buttonDelete.setId(Integer.parseInt(id));
            buttonDelete.setOnClickListener(v -> {
                dbHelper.deleteStuff(textId.getText().toString());
                fillTable();
            });

            tableRow.addView(textName);
            tableRow.addView(getDivider());

            tableRow.addView(textId);
            tableRow.addView(getDivider());

            tableRow.addView(buttonEdit);
            tableRow.addView(getDivider());

            tableRow.addView(buttonDelete);

            tableLayout.addView(tableRow);
        }
        cursor.close();
    }

    private View getDivider() {
        View divider = new View(this);
        divider.setLayoutParams(new TableRow.LayoutParams(50, TableRow.LayoutParams.MATCH_PARENT));
        return divider;
    }
}