package com.example.aihelperapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "topics")
public class Topic {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}
