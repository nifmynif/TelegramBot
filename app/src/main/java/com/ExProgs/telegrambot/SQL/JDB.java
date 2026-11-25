package com.ExProgs.telegrambot.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ExProgs.telegrambot.AppLog;
import com.ExProgs.telegrambot.models.SessionNormalizer;

public class JDB extends SQLiteOpenHelper {
    private static final String TAG = "JDB";

    public static final int DATABASE_VERSION = 26;
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
    public static final String COL_TITLE_NORM = "title_norm";
    public static final String COL_DATE_NORM = "date_norm";

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
                + COL_FREE_SEATS + " INTEGER DEFAULT -1,"
                + COL_TITLE_NORM + " TEXT DEFAULT '',"
                + COL_DATE_NORM + " TEXT DEFAULT ''"
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
        createIdentityIndex(db);
    }

    private void createIdentityIndex(SQLiteDatabase db) {
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_sessions_identity ON "
                + TABLE_SESSIONS + " (" + COL_TITLE_NORM + ", " + COL_DATE_NORM + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            upgradeInternal(db, oldVersion);
            AppLog.i(TAG, "Миграция БД " + oldVersion + " -> " + newVersion + " завершена");
        } catch (Exception e) {
            // никогда не оставляем БД в непригодном состоянии:
            // при любом сбое миграции пересоздаём таблицы заново
            AppLog.e(TAG, "Миграция БД " + oldVersion + " -> " + newVersion
                    + " не удалась, пересоздаю таблицы", e);
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBSCRIBERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELIVERIES);
            } catch (Exception dropError) {
                AppLog.e(TAG, "Не удалось удалить таблицы", dropError);
            }
            onCreate(db);
        }
    }

    private void upgradeInternal(SQLiteDatabase db, int oldVersion) {
        if (oldVersion >= 22) {
            if (oldVersion <= 24) {
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
            }
            if (oldVersion <= 25) {
                AppLog.i(TAG, "Добавляю нормализованные колонки идентичности показов");
                db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN "
                        + COL_TITLE_NORM + " TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN "
                        + COL_DATE_NORM + " TEXT DEFAULT ''");
                backfillNorms(db);
                dedupeSessions(db);
            }
        } else {
            // старые версии: таблицы пересоздаются с нуля через onCreate
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBSCRIBERS);
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELIVERIES);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // самовосстановление: если колонки идентичности по какой-то причине
        // отсутствуют (прерванная миграция, повреждённая схема) — чиним на месте
        try {
            ensureNormColumns(db);
        } catch (Exception e) {
            AppLog.e(TAG, "Не удалось проверить/починить колонки идентичности", e);
        }
    }

    private void ensureNormColumns(SQLiteDatabase db) {
        boolean hasTitleNorm = false;
        boolean hasDateNorm = false;
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_SESSIONS + ")", null);
        try {
            int nameIdx = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIdx);
                if (COL_TITLE_NORM.equals(name)) hasTitleNorm = true;
                if (COL_DATE_NORM.equals(name)) hasDateNorm = true;
            }
        } finally {
            cursor.close();
        }
        if (hasTitleNorm && hasDateNorm) return;
        AppLog.w(TAG, "Колонки идентичности отсутствуют, добавляю (title_norm="
                + hasTitleNorm + ", date_norm=" + hasDateNorm + ")");
        if (!hasTitleNorm) {
            db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN "
                    + COL_TITLE_NORM + " TEXT DEFAULT ''");
        }
        if (!hasDateNorm) {
            db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN "
                    + COL_DATE_NORM + " TEXT DEFAULT ''");
        }
        backfillNorms(db);
        dedupeSessions(db);
        createIdentityIndex(db);
    }

    private void backfillNorms(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_SESSIONS,
                new String[]{COL_ID, COL_TITLE, COL_DATE_TIME},
                null, null, null, null, null);
        try {
            int idIdx = cursor.getColumnIndex(COL_ID);
            int titleIdx = cursor.getColumnIndex(COL_TITLE);
            int dateIdx = cursor.getColumnIndex(COL_DATE_TIME);
            while (cursor.moveToNext()) {
                ContentValues cv = new ContentValues();
                cv.put(COL_TITLE_NORM,
                        SessionNormalizer.normalizeTitle(cursor.getString(titleIdx)));
                cv.put(COL_DATE_NORM,
                        SessionNormalizer.normalizeDateTime(cursor.getString(dateIdx)));
                db.update(TABLE_SESSIONS, cv, COL_ID + " = ?",
                        new String[]{String.valueOf(cursor.getLong(idIdx))});
            }
        } finally {
            cursor.close();
        }
    }

    private void dedupeSessions(SQLiteDatabase db) {
        // после нормализации разные сырые строки могли схлопнуться в одну
        // идентичность — оставляем старейшую запись, дубликаты удаляем
        db.execSQL("DELETE FROM " + TABLE_DELIVERIES + " WHERE " + COL_SESSION_ID
                + " NOT IN (SELECT " + COL_ID + " FROM (SELECT MIN(" + COL_ID + ") AS "
                + COL_ID + " FROM " + TABLE_SESSIONS + " GROUP BY " + COL_TITLE_NORM
                + ", " + COL_DATE_NORM + "))");
        db.execSQL("DELETE FROM " + TABLE_SESSIONS + " WHERE " + COL_ID
                + " NOT IN (SELECT " + COL_ID + " FROM (SELECT MIN(" + COL_ID + ") AS "
                + COL_ID + " FROM " + TABLE_SESSIONS + " GROUP BY " + COL_TITLE_NORM
                + ", " + COL_DATE_NORM + "))");
    }
}
