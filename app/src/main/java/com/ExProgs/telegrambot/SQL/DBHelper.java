package com.ExProgs.telegrambot.SQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ExProgs.telegrambot.constants.Constants;
import com.ExProgs.telegrambot.models.Order;
import com.ExProgs.telegrambot.models.Product;

import java.util.ArrayList;

public class DBHelper {
    private SQLiteDatabase database;
    private ContentValues contentValues;
    private final JDB jdb;

    public DBHelper(Context context) {
        this.jdb = new JDB(context);
    }

    public void addStuff(String s) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(JDB.KEY_NAME, s);
        contentValues.put(JDB.KEY_OBJECT_TYPE, 1);
        database.insert(JDB.TABLE_OBJECTS, null, contentValues);
        database.close();
    }

    public void addOrder(String s) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(JDB.KEY_NAME, s);
        contentValues.put(JDB.KEY_OBJECT_TYPE, 2);
        database.insert(JDB.TABLE_OBJECTS, null, contentValues);
        database.close();
    }

    @SuppressLint("Range")
    public Product getStuffBySticker(String id) {
        @SuppressLint("Recycle") Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_PARAMS,
                        null,
                        JDB.KEY_VALUE + " = \"" + id + "\" AND " + JDB.KEY_ATTR_ID + " = 4",
                        null,
                        null,
                        null,
                        null);
        cursor.moveToFirst();
        id = cursor.getString(cursor.getColumnIndex(JDB.KEY_OBJECT_ID));
        cursor = getParams(id);
        Product product = new Product(id);
        while (cursor.moveToNext()) {
            String attrId = cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID));
            String value = cursor.getString(cursor.getColumnIndex(JDB.KEY_VALUE));
            if (attrId.equals("1")) {
                product.setName(value);
                continue;
            }
            if (attrId.equals("2")) {
                product.setPrice(value);
                continue;
            }
            if (attrId.equals("3")) {
                product.setDescription(value);
                continue;
            }
            if (attrId.equals("4")) {
                product.setSticker(value);
            }
        }
        cursor.close();
        return product;
    }

    @SuppressLint("Range")
    public Order getOrderBySticker(String id) {
        @SuppressLint("Recycle") Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_OBJECTS,
                        null,
                        JDB.KEY_NAME + " = \"" + id + "\" AND " + JDB.KEY_OBJECT_TYPE + " = 2",
                        null,
                        null,
                        null,
                        null);
        cursor.moveToFirst();
        id = cursor.getString(cursor.getColumnIndex(JDB.KEY_ID));
        cursor = getParams(id);
        Order order = new Order(id);
        while (cursor.moveToNext()) {
            String attrId = cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID));
            String value = cursor.getString(cursor.getColumnIndex(JDB.KEY_VALUE));
            if (attrId.equals("6")) {
                order.setUser(value);
                continue;
            }
            if (attrId.equals("8")) {
                order.setDate(value);
                continue;
            }
            if (attrId.equals("7")) {
                order.setPass(value);
                continue;
            }
            if (attrId.equals("5")) {
                order.setSticker(value);
            }
        }
        cursor.close();
        return order;
    }

    public Cursor getStuffs() {
        return jdb.getReadableDatabase()
                .query(JDB.TABLE_OBJECTS,
                        null,
                        JDB.KEY_OBJECT_TYPE + " = 1",
                        null,
                        null,
                        null,
                        null);
    }

    @SuppressLint("Range")
    public String getParam(String object_id, String attr_id) {
        @SuppressLint("Recycle") Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_PARAMS,
                        null,
                        JDB.KEY_OBJECT_ID + " = " + object_id
                                + " AND " + JDB.KEY_ATTR_ID + " = " + attr_id,
                        null,
                        null,
                        null,
                        null);
        String s = "";
        while (cursor.moveToNext())
            s = cursor.getString(cursor.getColumnIndex(JDB.KEY_VALUE));
        cursor.close();
        return s;
    }

    @SuppressLint({"Range", "Recycle"})
    public ArrayList<Order> getOrders() {
        Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_OBJECTS,
                        null,
                        JDB.KEY_OBJECT_TYPE + " = 2",
                        null,
                        null,
                        null,
                        null);
        ArrayList<Order> orders = new ArrayList<>();
        String orderId;
        while (cursor.moveToNext()) {
            orderId = cursor.getString(cursor.getColumnIndex(JDB.KEY_ID));
            if (getParam(orderId, "7")
                    .equals(Constants.NO)) {
                Order order = new Order(orderId);
                order.setPass(getParam(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)), "7"));
                order.setDate(getParam(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)), "8"));
                order.setUser(getParam(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)), "6"));
                order.setSticker(getParam(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)), "5"));
                orders.add(order);
            }
        }
        cursor.close();
        return orders;
    }

    @SuppressLint({"Range", "Recycle"})
    public ArrayList<Order> getUserOrders(String user) {
        ArrayList<Order> orders = getOrders();
        orders.removeIf(order -> !order.getUser().contains(user));
        return orders;
    }

    public void deleteStuff(String id) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        database.delete(JDB.TABLE_OBJECTS, JDB.KEY_ID + " = " + id, null);
        deleteParam(id);
        database.close();
    }

    public void deleteOrder(String id) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        database.delete(JDB.TABLE_OBJECTS, JDB.KEY_ID + " = " + id, null);
        deleteParam(id);
        database.close();
    }

    public void addParam(String id, String attrId, String value) {
        database = jdb.getWritableDatabase();
        Cursor cursor = database.query(JDB.TABLE_PARAMS,
                null,
                JDB.KEY_OBJECT_ID + " = " + id
                        + " AND " + JDB.KEY_ATTR_ID + " = " + attrId,
                null,
                null,
                null,
                null);
        contentValues = new ContentValues();
        contentValues.put(JDB.KEY_ATTR_ID, attrId);
        contentValues.put(JDB.KEY_VALUE, value);
        contentValues.put(JDB.KEY_OBJECT_ID, id);
        if (cursor.getCount() == 0)
            database.insert(JDB.TABLE_PARAMS, null, contentValues);
        else
            database.update(JDB.TABLE_PARAMS, contentValues,
                    JDB.KEY_OBJECT_ID + " = " + id, null);
        database.close();
        cursor.close();
    }

    public Cursor getParams(String id) {
        return jdb.getReadableDatabase()
                .query(JDB.TABLE_PARAMS,
                        null,
                        JDB.KEY_OBJECT_ID + " = " + id,
                        null,
                        null,
                        null,
                        null);
    }

    private void deleteParam(String id) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        database.delete(JDB.TABLE_PARAMS, JDB.KEY_OBJECT_ID + " = " + id, null);
        database.close();
    }
}
