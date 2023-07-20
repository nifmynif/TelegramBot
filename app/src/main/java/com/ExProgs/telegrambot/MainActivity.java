package com.ExProgs.telegrambot;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ExProgs.telegrambot.SQL.DBHelper;
import com.ExProgs.telegrambot.SQL.JDB;
import com.ExProgs.telegrambot.models.Product;

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(getApplicationContext());
        createAddStuffBtn();
        fillTable();
        if (!isServiceRunning(this, TelegramBotService.class)) {
            Intent serviceIntent = new Intent(this, TelegramBotService.class);
            startForegroundService(serviceIntent);
        }
    }

    public boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void createAddStuffBtn() {
        Button addStuff = findViewById(R.id.add_stuff);
        addStuff.setOnClickListener(view -> dialogAdd());
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
            buttonEdit.setOnClickListener(v -> dialogEdit(textId.getText().toString()));
            Button buttonDelete = new Button(this);
            buttonDelete.setText("\uD83D\uDDD1");
            buttonDelete.setId(Integer.parseInt(id));
            buttonDelete.setOnClickListener(v -> dialogDel(textId.getText().toString()));

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

    private void dialogDel(String id) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_del);
        Button noBtn = dialog.findViewById(R.id.no);
        Button yesBtn = dialog.findViewById(R.id.yes);
        noBtn.setOnClickListener(v -> dialog.dismiss());
        yesBtn.setOnClickListener(view -> {
            dbHelper.deleteStuff(id);
            fillTable();
            dialog.dismiss();
        });
        dialog.show();
    }

    @SuppressLint("Range")
    private void dialogEdit(String id) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit);
        Button closeBtn = dialog.findViewById(R.id.close);
        Button saveBtn = dialog.findViewById(R.id.save);
        Cursor cursor = dbHelper.getParams(id);
        EditText stuff_name = dialog.findViewById(R.id.stuff_name);
        EditText stuff_price = dialog.findViewById(R.id.stuff_price);
        EditText stuff_description = dialog.findViewById(R.id.stuff_description);
        EditText stuff_sticker = dialog.findViewById(R.id.stuff_sticker);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String attrId = cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID));
            @SuppressLint("Range") String value = cursor.getString(cursor.getColumnIndex(JDB.KEY_VALUE));
            if (attrId.equals("1")) {
                stuff_name.setText(value);
                continue;
            }
            if (attrId.equals("2")) {
                stuff_price.setText(value);
                continue;
            }
            if (attrId.equals("3")) {
                stuff_description.setText(value);
                continue;
            }
            if (attrId.equals("4")) {
                stuff_sticker.setText(value);
            }
        }
        cursor.close();
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        saveBtn.setOnClickListener(view -> {
            Product product = new Product(id);
            product.setName(stuff_name.getText().toString());
            product.setPrice(stuff_price.getText().toString());
            product.setDescription(stuff_description.getText().toString());
            product.setSticker(stuff_sticker.getText().toString());
            product.add(dbHelper);
            fillTable();
            dialog.dismiss();
        });
        dialog.show();
    }

    @SuppressLint("Range")
    private void dialogAdd() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add);
        Button closeBtn = dialog.findViewById(R.id.close);
        Button saveBtn = dialog.findViewById(R.id.save);
        EditText stuff_name = dialog.findViewById(R.id.stuff_name);
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        saveBtn.setOnClickListener(view -> {
            dbHelper.addStuff(stuff_name.getText().toString());
            fillTable();
            dialog.dismiss();
        });
        dialog.show();
    }
}