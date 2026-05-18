package com.example.aihelperapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TopicDao {
    @Insert
    void insert(Topic topic);

    @Query("SELECT * FROM topics")
    List<Topic> getAllTopics();

    @Insert
    void insertUserTopic(UserTopic userTopic);

    @Query("DELETE FROM user_topics WHERE userId = :userId")
    void clearUserTopics(int userId);

    @Query("SELECT t.* FROM topics t INNER JOIN user_topics ut ON t.id = ut.topicId WHERE ut.userId = :userId")
    List<Topic> getTopicsForUser(int userId);

    @Query("SELECT COUNT(*) FROM user_topics WHERE userId = :userId")
    int getUserTopicCount(int userId);
}
