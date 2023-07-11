package com.ExProgs.telegrambot.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper {
    public static int staffId;
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

    public void addParam(String attrId, String value, String dateValue) {
        database = jdb.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(JDB.KEY_OBJECT_ID, staffId);
        contentValues.put(JDB.KEY_ATTR_ID, attrId);
        contentValues.put(JDB.KEY_VALUE, value);
        contentValues.put(JDB.KEY_DATE_VALUE, dateValue);
        database.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
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
}
