package com.example.eventapp;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface EventDao {

    @Query("SELECT * FROM events ORDER BY dateTime ASC")
    LiveData<List<Event>> getAllEvents();

    @Query("SELECT * FROM events WHERE id = :id")
    Event getEventById(int id);

    @Insert
    void insertEvent(Event event);

    @Update
    void updateEvent(Event event);

    @Delete
    void deleteEvent(Event event);
}
