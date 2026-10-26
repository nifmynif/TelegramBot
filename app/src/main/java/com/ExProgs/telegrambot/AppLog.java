package com.ExProgs.telegrambot;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;

public class AppLog {
    private static final int CAPACITY = 300;
    private static final Deque<String> LINES = new ArrayDeque<>(CAPACITY);
    private static final SimpleDateFormat TIME =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void i(String tag, String msg) {
        add(Log.INFO, tag, msg, null);
    }

    public static void w(String tag, String msg) {
        add(Log.WARN, tag, msg, null);
    }

    public static void w(String tag, String msg, Throwable t) {
        add(Log.WARN, tag, msg, t);
    }

    public static void e(String tag, String msg) {
        add(Log.ERROR, tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable t) {
        add(Log.ERROR, tag, msg, t);
    }

    private static synchronized void add(int priority, String tag, String msg, Throwable t) {
        String line = TIME.format(new Date()) + " " + tag + ": " + msg;
        LINES.addLast(line);
        while (LINES.size() > CAPACITY) {
            LINES.removeFirst();
        }
        if (t != null) {
            String[] trace = Log.getStackTraceString(t).split("\n");
            for (String s : trace) {
                if (s.isEmpty()) continue;
                LINES.addLast(s);
                while (LINES.size() > CAPACITY) {
                    LINES.removeFirst();
                }
            }
            Log.println(priority, tag, msg + "\n" + Log.getStackTraceString(t));
        } else {
            Log.println(priority, tag, msg);
        }
    }

    public static synchronized String dump() {
        return String.join("\n", LINES);
    }
}
