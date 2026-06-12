package com.ExProgs.telegrambot.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class JDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 22;
    public static final String DATABASE_NAME = "theaterDB";

    public static final String TABLE_SESSIONS = "performance_sessions";
    public static final String TABLE_SUBSCRIBERS = "telegram_subscribers";

    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DATE_TIME = "date_time";
    public static final String COL_BUY_URL = "buy_url";
    public static final String COL_SENT = "sent";
    public static final String COL_CREATED_AT = "created_at";

    public static final String COL_CHAT_ID = "chat_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_IS_ACTIVE = "is_active";

    public JDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SESSIONS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TITLE + " TEXT,"
                + COL_DATE_TIME + " TEXT,"
                + COL_BUY_URL + " TEXT,"
                + COL_SENT + " INTEGER DEFAULT 0,"
                + COL_CREATED_AT + " INTEGER"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_SUBSCRIBERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_CHAT_ID + " INTEGER UNIQUE,"
                + COL_USERNAME + " TEXT,"
                + COL_FIRST_NAME + " TEXT,"
                + COL_LAST_NAME + " TEXT,"
                + COL_IS_ACTIVE + " INTEGER DEFAULT 1"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBSCRIBERS);
        onCreate(db);
    }
}
