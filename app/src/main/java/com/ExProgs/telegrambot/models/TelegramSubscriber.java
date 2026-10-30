package com.ExProgs.telegrambot.models;

public class TelegramSubscriber {
    private long id;
    private long chatId;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private boolean wantsDigest;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getChatId() { return chatId; }
    public void setChatId(long chatId) { this.chatId = chatId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isWantsDigest() { return wantsDigest; }
    public void setWantsDigest(boolean wantsDigest) { this.wantsDigest = wantsDigest; }
}
