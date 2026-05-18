package com.example.aichatapp.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "messages",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "uid",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("userId")}
)
public class Message {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    @NonNull
    public String content;

    public long timestamp;


    public boolean isUser;

    public Message(long userId, @NonNull String content, long timestamp, boolean isUser) {
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.isUser = isUser;
    }
}
