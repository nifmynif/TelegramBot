package com.ExProgs.telegrambot.models;

public class PerformanceSession {
    public static final int STATUS_UNAVAILABLE = 0;
    public static final int STATUS_ON_SALE = 1;

    private long id;
    private String title;
    private String dateTime;
    private String buyUrl;
    private boolean sent;
    private long createdAt;
    private int status = STATUS_ON_SALE;
    private boolean availableAgain;

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

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isOnSale() { return status == STATUS_ON_SALE; }

    public boolean isAvailableAgain() { return availableAgain; }
    public void setAvailableAgain(boolean availableAgain) { this.availableAgain = availableAgain; }
}
