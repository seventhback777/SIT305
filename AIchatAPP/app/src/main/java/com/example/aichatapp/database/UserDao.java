package com.example.aichatapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.aichatapp.model.User;

@Dao
public interface UserDao {

    /** Insert a new user; returns the generated uid. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUser(User user);

    /** Returns null if username does not exist. */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);
}
