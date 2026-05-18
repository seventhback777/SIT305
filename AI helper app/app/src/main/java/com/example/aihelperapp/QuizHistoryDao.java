package com.example.aihelperapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuizHistoryDao {
    @Insert
    void insert(QuizHistory quizHistory);

    @Query("SELECT * FROM quiz_history WHERE userId = :userId ORDER BY timestamp DESC")
    List<QuizHistory> getHistoryForUser(int userId);

    @Query("SELECT * FROM quiz_history WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<QuizHistory>> getHistoryLive(int userId);

    @Query("SELECT COUNT(*) FROM quiz_history WHERE userId = :userId")
    int getTotalCount(int userId);

    @Query("SELECT COUNT(*) FROM quiz_history WHERE userId = :userId AND isCorrect = 1")
    int getCorrectCount(int userId);

    @Query("SELECT * FROM quiz_history WHERE userId = :userId AND isCorrect = 0 ORDER BY timestamp DESC")
    List<QuizHistory> getWrongAnswers(int userId);
}
