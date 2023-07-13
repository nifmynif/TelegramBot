package com.ExProgs.telegrambot.SQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ExProgs.telegrambot.models.Product;

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
        database.insert(JDB.TABLE_OBJECTS, null, contentValues);
        database.close();
    }

    @SuppressLint("Range")
    public Product getStuff(String id) {
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
                continue;
            }
            if (attrId.equals("5"))
                product.setNum(value);
        }
        cursor.close();
        return product;
    }

    public Cursor getStuffs() {
        return jdb.getReadableDatabase()
                .query(JDB.TABLE_OBJECTS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    public void deleteStuff(String id) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        database.delete(JDB.TABLE_OBJECTS, JDB.KEY_ID + " = " + id, null);
        deleteParam(id);
        database.close();
    }

    public void addParam(String id, String attrId, String value, String dateValue) {
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
        contentValues.put(JDB.KEY_DATE_VALUE, dateValue);
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
