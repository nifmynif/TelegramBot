package com.ExProgs.telegrambot.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class JDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 25;
    public static final String DATABASE_NAME = "theaterDB";

    public static final String TABLE_SESSIONS = "performance_sessions";
    public static final String TABLE_SUBSCRIBERS = "telegram_subscribers";
    public static final String TABLE_DELIVERIES = "session_deliveries";

    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DATE_TIME = "date_time";
    public static final String COL_BUY_URL = "buy_url";
    public static final String COL_SENT = "sent";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_STATUS = "status";
    public static final String COL_FREE_SEATS = "free_seats";

    public static final String COL_CHAT_ID = "chat_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_IS_ACTIVE = "is_active";
    public static final String COL_WANTS_DIGEST = "wants_digest";

    public static final String COL_SESSION_ID = "session_id";
    public static final String COL_DELIVERED_AT = "delivered_at";

    public JDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SESSIONS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TITLE + " TEXT,"
                + COL_DATE_TIME + " TEXT,"
                + COL_BUY_URL + " TEXT,"
                + COL_SENT + " INTEGER DEFAULT 0,"
                + COL_CREATED_AT + " INTEGER,"
                + COL_STATUS + " INTEGER DEFAULT 1,"
                + COL_FREE_SEATS + " INTEGER DEFAULT -1"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUBSCRIBERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_CHAT_ID + " INTEGER UNIQUE,"
                + COL_USERNAME + " TEXT,"
                + COL_FIRST_NAME + " TEXT,"
                + COL_LAST_NAME + " TEXT,"
                + COL_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + COL_WANTS_DIGEST + " INTEGER DEFAULT 0"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DELIVERIES + " ("
                + COL_SESSION_ID + " INTEGER,"
                + COL_CHAT_ID + " INTEGER,"
                + COL_DELIVERED_AT + " INTEGER,"
                + "PRIMARY KEY (" + COL_SESSION_ID + ", " + COL_CHAT_ID + ")"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= 22 && oldVersion <= 24) {
            if (oldVersion == 22) {
                db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN "
                        + COL_STATUS + " INTEGER DEFAULT 1");
            }
            if (oldVersion <= 23) {
                db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN "
                        + COL_FREE_SEATS + " INTEGER DEFAULT -1");
            }
            db.execSQL("ALTER TABLE " + TABLE_SUBSCRIBERS + " ADD COLUMN "
                    + COL_WANTS_DIGEST + " INTEGER DEFAULT 0");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBSCRIBERS);
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELIVERIES);
        onCreate(db);
    }
}
