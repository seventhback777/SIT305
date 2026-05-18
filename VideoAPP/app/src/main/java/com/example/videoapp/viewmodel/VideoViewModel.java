package com.example.videoapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.videoapp.data.AppDatabase;
import com.example.videoapp.data.VideoDao;
import com.example.videoapp.model.VideoEntry;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoViewModel extends AndroidViewModel {

    private final VideoDao videoDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Shared between PlaylistFragment and HomeFragment for URL hand-off
    public final MutableLiveData<String> selectedUrl = new MutableLiveData<>();

    public VideoViewModel(@NonNull Application application) {
        super(application);
        videoDao = AppDatabase.getInstance(application).videoDao();
    }

    public LiveData<List<VideoEntry>> getVideosForUser(int userId) {
        return videoDao.getVideosForUser(userId);
    }

    public void addVideo(String url, int userId) {
        executor.execute(() -> videoDao.insert(new VideoEntry(url, userId)));
    }

    public void deleteVideo(VideoEntry entry) {
        executor.execute(() -> videoDao.delete(entry));
    }
}
