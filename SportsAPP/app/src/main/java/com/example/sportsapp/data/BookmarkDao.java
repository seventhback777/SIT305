package com.example.sportsapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Bookmark bookmark);

    @Delete
    void delete(Bookmark bookmark);

    @Query("SELECT * FROM bookmarks")
    LiveData<List<Bookmark>> getAllBookmarks();

    // Returns 1 if bookmarked, 0 if not
    @Query("SELECT COUNT(*) FROM bookmarks WHERE newsId = :newsId")
    LiveData<Integer> isBookmarked(int newsId);
}
