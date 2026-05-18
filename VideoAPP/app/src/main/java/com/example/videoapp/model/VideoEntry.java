package com.example.videoapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "video_entries")
public class VideoEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String videoUrl;
    public int userId;

    public VideoEntry(String videoUrl, int userId) {
        this.videoUrl = videoUrl;
        this.userId = userId;
    }
}
