package com.example.aichatapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.aichatapp.model.Message;
import com.example.aichatapp.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Message.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4);

    public abstract UserDao userDao();
    public abstract MessageDao messageDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "aichat_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

    /** Shared executor for all background write operations. */
    public static ExecutorService getExecutor() {
        return DB_EXECUTOR;
    }
}
