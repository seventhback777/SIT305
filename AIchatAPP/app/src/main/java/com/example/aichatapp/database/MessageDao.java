package com.example.aichatapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.aichatapp.model.Message;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    long insertMessage(Message message);


    @Query("SELECT * FROM messages WHERE userId = :userId ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessages(long userId);


    @Query("SELECT * FROM messages WHERE userId = :userId ORDER BY timestamp ASC")
    List<Message> getMessagesSync(long userId);
}
