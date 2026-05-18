package com.example.aihelperapp;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_topics",
        indices = {@androidx.room.Index("userId"), @androidx.room.Index("topicId")},
        foreignKeys = {
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Topic.class, parentColumns = "id", childColumns = "topicId", onDelete = ForeignKey.CASCADE)
        })
public class UserTopic {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public int topicId;
}
