package com.example.videoapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.videoapp.model.VideoEntry;

import java.util.List;

@Dao
public interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(VideoEntry entry);

    @Delete
    void delete(VideoEntry entry);

    @Query("SELECT * FROM video_entries WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<VideoEntry>> getVideosForUser(int userId);
}
