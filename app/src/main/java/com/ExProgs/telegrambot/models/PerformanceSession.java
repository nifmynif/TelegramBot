package com.ExProgs.telegrambot.models;

public class PerformanceSession {
    private long id;
    private String title;
    private String dateTime;
    private String buyUrl;
    private boolean sent;
    private long createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getBuyUrl() { return buyUrl; }
    public void setBuyUrl(String buyUrl) { this.buyUrl = buyUrl; }

    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
