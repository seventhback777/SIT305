package com.example.aihelperapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final LLMRepository llmRepo;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TaskViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        llmRepo = new LLMRepository();
    }

    public LiveData<List<Task>> getTasksForUser(int userId) {
        return db.taskDao().getTasksForUser(userId);
    }

    public void generateAndSaveTasks(int userId) {
        isLoading.postValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Topic> topics = db.topicDao().getTopicsForUser(userId);
            if (topics.isEmpty()) {
                isLoading.postValue(false);
                errorMessage.postValue("No interests selected");
                return;
            }
            List<String> topicNames = new ArrayList<>();
            for (Topic t : topics) topicNames.add(t.name);

            llmRepo.generateTasks(topicNames, userId, new LLMRepository.TasksCallback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.taskDao().clearTasksForUser(userId);
                        for (Task task : tasks) db.taskDao().insert(task);
                        isLoading.postValue(false);
                    });
                }

                @Override
                public void onError(String error) {
                    isLoading.postValue(false);
                    errorMessage.postValue(error);
                }
            });
        });
    }

    public void markTaskCompleted(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            task.isCompleted = true;
            db.taskDao().update(task);
        });
    }

    public MutableLiveData<Task> getTaskById(int taskId) {
        MutableLiveData<Task> liveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() ->
                liveData.postValue(db.taskDao().findById(taskId)));
        return liveData;
    }
}
