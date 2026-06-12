package com.ExProgs.telegrambot.SQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ExProgs.telegrambot.models.PerformanceSession;
import com.ExProgs.telegrambot.models.TelegramSubscriber;

import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private final JDB jdb;

    public DBHelper(Context context) {
        this.jdb = new JDB(context);
    }

    public boolean existsByTitleAndDateTime(String title, String dateTime) {
        SQLiteDatabase db = jdb.getReadableDatabase();
        Cursor cursor = db.query(JDB.TABLE_SESSIONS, new String[]{JDB.COL_ID},
                JDB.COL_TITLE + " = ? AND " + JDB.COL_DATE_TIME + " = ?",
                new String[]{title, dateTime}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void saveSession(PerformanceSession session) {
        SQLiteDatabase db = jdb.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(JDB.COL_TITLE, session.getTitle());
        cv.put(JDB.COL_DATE_TIME, session.getDateTime());
        cv.put(JDB.COL_BUY_URL, session.getBuyUrl());
        cv.put(JDB.COL_SENT, session.isSent() ? 1 : 0);
        cv.put(JDB.COL_CREATED_AT, System.currentTimeMillis());
        db.insert(JDB.TABLE_SESSIONS, null, cv);
    }

    @SuppressLint("Range")
    public List<PerformanceSession> findAllUnsent() {
        SQLiteDatabase db = jdb.getReadableDatabase();
        Cursor cursor = db.query(JDB.TABLE_SESSIONS, null,
                JDB.COL_SENT + " = 0", null, null, null, null);
        List<PerformanceSession> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursorToSession(cursor));
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<PerformanceSession> getAllSessions() {
        SQLiteDatabase db = jdb.getReadableDatabase();
        Cursor cursor = db.query(JDB.TABLE_SESSIONS, null, null, null,
                null, null, JDB.COL_ID + " DESC");
        List<PerformanceSession> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursorToSession(cursor));
        }
        cursor.close();
        return list;
    }

    public void markAsSent(List<PerformanceSession> sessions) {
        SQLiteDatabase db = jdb.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(JDB.COL_SENT, 1);
        for (PerformanceSession session : sessions) {
            db.update(JDB.TABLE_SESSIONS, cv,
                    JDB.COL_ID + " = ?", new String[]{String.valueOf(session.getId())});
        }
    }

    public void deleteOldSessions() {
        long threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        SQLiteDatabase db = jdb.getWritableDatabase();
        db.delete(JDB.TABLE_SESSIONS, JDB.COL_CREATED_AT + " < ?",
                new String[]{String.valueOf(threshold)});
    }

    @SuppressLint("Range")
    public TelegramSubscriber findSubscriberByChatId(long chatId) {
        SQLiteDatabase db = jdb.getReadableDatabase();
        Cursor cursor = db.query(JDB.TABLE_SUBSCRIBERS, null,
                JDB.COL_CHAT_ID + " = ?", new String[]{String.valueOf(chatId)},
                null, null, null);
        TelegramSubscriber sub = null;
        if (cursor.moveToFirst()) {
            sub = cursorToSubscriber(cursor);
        }
        cursor.close();
        return sub;
    }

    public void saveSubscriber(TelegramSubscriber subscriber) {
        SQLiteDatabase db = jdb.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(JDB.COL_CHAT_ID, subscriber.getChatId());
        cv.put(JDB.COL_USERNAME, subscriber.getUsername());
        cv.put(JDB.COL_FIRST_NAME, subscriber.getFirstName());
        cv.put(JDB.COL_LAST_NAME, subscriber.getLastName());
        cv.put(JDB.COL_IS_ACTIVE, subscriber.isActive() ? 1 : 0);
        int updated = db.update(JDB.TABLE_SUBSCRIBERS, cv,
                JDB.COL_CHAT_ID + " = ?", new String[]{String.valueOf(subscriber.getChatId())});
        if (updated == 0) {
            db.insert(JDB.TABLE_SUBSCRIBERS, null, cv);
        }
    }

    @SuppressLint("Range")
    public List<TelegramSubscriber> findAllActiveSubscribers() {
        SQLiteDatabase db = jdb.getReadableDatabase();
        Cursor cursor = db.query(JDB.TABLE_SUBSCRIBERS, null,
                JDB.COL_IS_ACTIVE + " = 1", null, null, null, null);
        List<TelegramSubscriber> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursorToSubscriber(cursor));
        }
        cursor.close();
        return list;
    }

    public int getActiveSubscriberCount() {
        SQLiteDatabase db = jdb.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + JDB.TABLE_SUBSCRIBERS + " WHERE " + JDB.COL_IS_ACTIVE + " = 1",
                null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    @SuppressLint("Range")
    private PerformanceSession cursorToSession(Cursor cursor) {
        PerformanceSession session = new PerformanceSession();
        session.setId(cursor.getLong(cursor.getColumnIndex(JDB.COL_ID)));
        session.setTitle(cursor.getString(cursor.getColumnIndex(JDB.COL_TITLE)));
        session.setDateTime(cursor.getString(cursor.getColumnIndex(JDB.COL_DATE_TIME)));
        session.setBuyUrl(cursor.getString(cursor.getColumnIndex(JDB.COL_BUY_URL)));
        session.setSent(cursor.getInt(cursor.getColumnIndex(JDB.COL_SENT)) == 1);
        session.setCreatedAt(cursor.getLong(cursor.getColumnIndex(JDB.COL_CREATED_AT)));
        return session;
    }

    @SuppressLint("Range")
    private TelegramSubscriber cursorToSubscriber(Cursor cursor) {
        TelegramSubscriber sub = new TelegramSubscriber();
        sub.setId(cursor.getLong(cursor.getColumnIndex(JDB.COL_ID)));
        sub.setChatId(cursor.getLong(cursor.getColumnIndex(JDB.COL_CHAT_ID)));
        sub.setUsername(cursor.getString(cursor.getColumnIndex(JDB.COL_USERNAME)));
        sub.setFirstName(cursor.getString(cursor.getColumnIndex(JDB.COL_FIRST_NAME)));
        sub.setLastName(cursor.getString(cursor.getColumnIndex(JDB.COL_LAST_NAME)));
        sub.setActive(cursor.getInt(cursor.getColumnIndex(JDB.COL_IS_ACTIVE)) == 1);
        return sub;
    }
}
