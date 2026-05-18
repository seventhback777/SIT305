package com.example.aichatapp.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "users",
    indices = {@Index(value = "username", unique = true)}
)
public class User {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @NonNull
    public String username;

    public User(@NonNull String username) {
        this.username = username;
    }
}
