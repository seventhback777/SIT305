package com.example.aihelperapp;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_history",
        indices = {@androidx.room.Index("userId")},
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE))
public class QuizHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String questionText;
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
    public String correctAnswer;
    public String selectedAnswer;
    public boolean isCorrect;
    public long timestamp;
}
