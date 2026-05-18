package com.example.aihelperapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<Task>> getTasksForUser(int userId);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task findById(int taskId);

    @Query("DELETE FROM tasks WHERE userId = :userId")
    void clearTasksForUser(int userId);
}
