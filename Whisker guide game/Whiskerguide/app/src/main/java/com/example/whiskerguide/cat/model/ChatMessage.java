package com.example.whiskerguide.cat.model;

public class ChatMessage {
    private String content;
    private boolean isFromCat;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String content, boolean isFromCat) {
        this.content = content;
        this.isFromCat = isFromCat;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFromCat() { return isFromCat; }
    public void setFromCat(boolean fromCat) { isFromCat = fromCat; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
