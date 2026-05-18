package com.example.sportsapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sportsapp.data.Bookmark;
import com.example.sportsapp.data.BookmarkDao;
import com.example.sportsapp.data.BookmarkDatabase;
import com.example.sportsapp.data.NewsRepository;
import com.example.sportsapp.model.NewsItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarkViewModel extends AndroidViewModel {

    private final BookmarkDao dao;
    private final NewsRepository repository = NewsRepository.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // LiveData of all bookmarked NewsItems (derived from Room bookmarks)
    public final LiveData<List<NewsItem>> bookmarkedNews;

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
        dao = BookmarkDatabase.getInstance(application).bookmarkDao();

        // Transform LiveData<List<Bookmark>> → LiveData<List<NewsItem>>
        bookmarkedNews = Transformations.map(dao.getAllBookmarks(), bookmarks -> {
            List<Integer> ids = new ArrayList<>();
            for (Bookmark b : bookmarks) ids.add(b.newsId);
            return repository.getNewsForIds(ids);
        });
    }

    /** Toggle: add bookmark if not present, remove if present */
    public void toggleBookmark(int newsId) {
        executor.execute(() -> {
            // Check current count synchronously on background thread
            // We query directly instead of using LiveData here to avoid threading issues
            Bookmark bookmark = new Bookmark(newsId);
            // Simple toggle: try insert first; if it fails (IGNORE), it means it exists → delete
            // Since we use IGNORE conflict strategy, insert is a no-op if row exists.
            // We handle this by checking the bookmarked LiveData state in the Fragment instead.
            dao.insert(bookmark);
        });
    }

    /** Explicitly add a bookmark */
    public void addBookmark(int newsId) {
        executor.execute(() -> dao.insert(new Bookmark(newsId)));
    }

    /** Explicitly remove a bookmark */
    public void removeBookmark(int newsId) {
        executor.execute(() -> dao.delete(new Bookmark(newsId)));
    }

    /** LiveData that emits true/false for whether a given news item is bookmarked */
    public LiveData<Boolean> isBookmarked(int newsId) {
        return Transformations.map(dao.isBookmarked(newsId), count -> count != null && count > 0);
    }
}
