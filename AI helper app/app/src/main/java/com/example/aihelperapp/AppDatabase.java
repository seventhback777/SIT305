package com.example.aihelperapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Topic.class, UserTopic.class, Task.class, QuizHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract TopicDao topicDao();
    public abstract TaskDao taskDao();
    public abstract QuizHistoryDao quizHistoryDao();

    private static volatile AppDatabase INSTANCE;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "learning_assistant_db")
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        TopicDao dao = INSTANCE.topicDao();
                                        String[] topics = {
                                                "Algorithms", "Data Structures", "Web Development",
                                                "Mobile Development", "Machine Learning", "Database Systems",
                                                "Operating Systems", "Computer Networks", "Software Testing",
                                                "Cybersecurity", "Cloud Computing", "Object-Oriented Programming"
                                        };
                                        for (String name : topics) {
                                            Topic t = new Topic();
                                            t.name = name;
                                            dao.insert(t);
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
