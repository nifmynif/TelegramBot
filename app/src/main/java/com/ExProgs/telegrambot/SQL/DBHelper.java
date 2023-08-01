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
    private final JDB jdb;
    private SQLiteDatabase database;
    private ContentValues contentValues;

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

    @SuppressLint({"Range", "Recycle"})
    public Product getStuffBySticker(String id) {
        Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_PARAMS,
                        null,
                        JDB.KEY_VALUE + " = \"" + id + "\" AND " + JDB.KEY_ATTR_ID + " = 5",
                        null,
                        null,
                        null,
                        null);
        Product product = null;
        while (cursor.moveToNext())
            product = getStuffById(cursor.getString(cursor.getColumnIndex(JDB.KEY_OBJECT_ID)));
        cursor.close();
        return product;
    }

    @SuppressLint({"Range", "Recycle"})
    public Product getStuffByName(String name) {
        Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_OBJECTS,
                        null,
                        JDB.KEY_NAME + " = \"" + name + "\" AND " + JDB.KEY_OBJECT_TYPE + " = 1",
                        null,
                        null,
                        null,
                        null);
        Product product = null;
        while (cursor.moveToNext())
            product = getStuffById(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)));
        cursor.close();
        return product;
    }

    @SuppressLint("Range")
    public Product getStuffById(String id) {
        Cursor cursor = getParams(id);
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
                product.setStickerId(value);
                continue;
            }
            if (attrId.equals("5"))
                product.setStickerUniqueId(value);
        }
        cursor.close();
        return product;
    }

    @SuppressLint("Range")
    private Order getOrderById(String id) {
        Cursor cursor = getParams(id);
        Order order = new Order(id);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String attrId = cursor.getString(cursor.getColumnIndex(JDB.KEY_ATTR_ID));
                String value = cursor.getString(cursor.getColumnIndex(JDB.KEY_VALUE));
                if (attrId.equals("7")) {
                    order.setUser(value);
                    continue;
                }
                if (attrId.equals("9")) {
                    order.setDate(value);
                    continue;
                }
                if (attrId.equals("8")) {
                    order.setPass(value);
                    continue;
                }
                if (attrId.equals("6")) {
                    order.setSticker(value);
                }
            }
        }
        cursor.close();
        return order;
    }

    @SuppressLint({"Range", "Recycle"})
    public Order getOrderBySticker(String id) {
        Order order = null;
        if (id != null) {
            Cursor cursor = jdb.getReadableDatabase()
                    .query(JDB.TABLE_OBJECTS,
                            null,
                            JDB.KEY_NAME + " = \"" + id + "\" AND " + JDB.KEY_OBJECT_TYPE + " = 2",
                            null,
                            null,
                            null,
                            null);
            while (cursor.moveToNext())
                order = getOrderById(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID)));
            cursor.close();
        }
        return order;
    }

    @SuppressLint({"Range", "Recycle"})
    public String getParam(String object_id, String attr_id) {
        Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_PARAMS,
                        null,
                        JDB.KEY_OBJECT_ID + " = " + object_id
                                + " AND " + JDB.KEY_ATTR_ID + " = " + attr_id,
                        null,
                        null,
                        null,
                        null);
        String s = null;
        while (cursor.moveToNext())
            s = cursor.getString(cursor.getColumnIndex(JDB.KEY_VALUE));
        cursor.close();
        return s;
    }

    @SuppressLint({"Range", "Recycle"})
    public ArrayList<Product> getStuffs() {
        Cursor cursor = jdb.getReadableDatabase()
                .query(JDB.TABLE_OBJECTS,
                        null,
                        JDB.KEY_OBJECT_TYPE + " = 1",
                        null,
                        null,
                        null,
                        null);
        ArrayList<Product> stuffs = new ArrayList<>();
        while (cursor.moveToNext())
            stuffs.add(getStuffById(cursor.getString(cursor.getColumnIndex(JDB.KEY_ID))));
        cursor.close();
        return stuffs;
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
            if (getParam(orderId, "8")
                    .equals(Constants.NO)) {
                orders.add(getOrderById(orderId));
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
        contentValues = new ContentValues();
        contentValues.put(JDB.KEY_ATTR_ID, attrId);
        contentValues.put(JDB.KEY_VALUE, value);
        contentValues.put(JDB.KEY_OBJECT_ID, id);
        if (getParam(id, attrId) == null)
            database.insert(JDB.TABLE_PARAMS, null, contentValues);
        else
            database.update(JDB.TABLE_PARAMS, contentValues,
                    JDB.KEY_OBJECT_ID + " = " + id + " AND " + JDB.KEY_ATTR_ID + " = " + attrId,
                    null);
        database.close();
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

    public void addEdit(String objectId, String attrId) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(JDB.KEY_OBJECT_ID, objectId);
        contentValues.put(JDB.KEY_ATTR_ID, attrId);
        database.insert(JDB.TABLE_EDIT, null, contentValues);
        database.close();
    }

    public void delEdit() {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        database.delete(JDB.TABLE_EDIT,
                null,
                null);
        database.close();
    }

    @SuppressLint({"Range", "Recycle"})
    public Cursor getEdit() {
        return jdb.getReadableDatabase()
                .query(JDB.TABLE_EDIT,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }
}
