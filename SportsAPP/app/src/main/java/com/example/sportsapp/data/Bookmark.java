package com.example.sportsapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class Bookmark {
    @PrimaryKey
    public int newsId;

    public Bookmark(int newsId) {
        this.newsId = newsId;
    }
}
