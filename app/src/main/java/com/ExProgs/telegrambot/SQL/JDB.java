package com.ExProgs.telegrambot.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class JDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "shopDB";
    public static final String TABLE_OBJECTS = "objects";
    public static final String TABLE_ATTRIBUTES = "attributes";
    public static final String TABLE_ATTRIBUTE_GROUPS = "attribute_groups";
    public static final String TABLE_PARAMS = "params";
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ATTR_GROUP = "attr_group_id";
    public static final String KEY_OBJECT_ID = "object_id";
    public static final String KEY_ATTR_ID = "attr_id";
    public static final String KEY_VALUE = "value";
    public static final String KEY_DATE_VALUE = "date_value";


    public JDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableObjects(db);
        createTableAttributes(db);
        createTableParams(db);
        createTableAttributeGroups(db);
        setDefault(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + TABLE_OBJECTS);
        db.execSQL("drop table if exists " + TABLE_ATTRIBUTES);
        db.execSQL("drop table if exists " + TABLE_PARAMS);
        db.execSQL("drop table if exists " + TABLE_ATTRIBUTE_GROUPS);
        onCreate(db);
    }

    private void createTableObjects(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_OBJECTS + "("
                + KEY_ID + " integer primary key,"
                + KEY_NAME + " text"
                + ")");
    }

    private void createTableAttributes(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_ATTRIBUTES + "("
                + KEY_ID + " integer primary key AUTOINCREMENT,"
                + KEY_NAME + " text,"
                + KEY_ATTR_GROUP + " integer,"
                + "FOREIGN KEY (" + KEY_ATTR_GROUP + ") REFERENCES " + TABLE_ATTRIBUTE_GROUPS + "(" + KEY_ATTR_GROUP + ")"
                + ")");
    }

    private void createTableAttributeGroups(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_ATTRIBUTE_GROUPS + "("
                + KEY_ID + " integer primary key AUTOINCREMENT,"
                + KEY_NAME + " text"
                + ")");
    }

    private void createTableParams(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_PARAMS + "("
                + KEY_ID + " integer primary key AUTOINCREMENT,"
                + KEY_OBJECT_ID + " integer,"
                + KEY_ATTR_ID + " integer,"
                + KEY_VALUE + " text,"
                + KEY_DATE_VALUE + " data,"
                + "FOREIGN KEY (" + KEY_ATTR_ID + ") REFERENCES " + TABLE_ATTRIBUTES + "(" + KEY_ATTR_ID + "),"
                + "FOREIGN KEY (" + KEY_OBJECT_ID + ") REFERENCES " + TABLE_OBJECTS + "(" + KEY_OBJECT_ID + ")"
                + ")");
    }
    private void setDefault(SQLiteDatabase db){
        ContentValues contentValues = new ContentValues();
        contentValues.put(JDB.KEY_NAME, "Stuff");
        db.insert(JDB.TABLE_ATTRIBUTE_GROUPS, null, contentValues);
        contentValues.clear();
        contentValues.put(JDB.KEY_NAME, "Query");
        db.insert(JDB.TABLE_ATTRIBUTE_GROUPS, null, contentValues);
        contentValues.clear();
        contentValues.put(KEY_NAME, "name");
        contentValues.put(KEY_ATTR_GROUP, "1");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.clear();
        contentValues.put(KEY_NAME, "price");
        contentValues.put(KEY_ATTR_GROUP, "1");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.clear();
        contentValues.put(KEY_NAME, "description");
        contentValues.put(KEY_ATTR_GROUP, "1");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.put(KEY_NAME, "sticker");
        contentValues.put(KEY_ATTR_GROUP, "1");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.put(KEY_NAME, "num");
        contentValues.put(KEY_ATTR_GROUP, "1");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.put(KEY_NAME, "id_stuff");
        contentValues.put(KEY_ATTR_GROUP, "2");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.put(KEY_NAME, "num");
        contentValues.put(KEY_ATTR_GROUP, "2");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.put(KEY_NAME, "user");
        contentValues.put(KEY_ATTR_GROUP, "2");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
        contentValues.put(KEY_NAME, "pass");
        contentValues.put(KEY_ATTR_GROUP, "2");
        db.insert(JDB.TABLE_ATTRIBUTES, null, contentValues);
    }
}
