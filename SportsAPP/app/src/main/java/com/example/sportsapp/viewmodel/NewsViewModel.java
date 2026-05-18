package com.example.sportsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sportsapp.data.NewsRepository;
import com.example.sportsapp.model.NewsItem;

import java.util.List;

public class NewsViewModel extends ViewModel {

    private final NewsRepository repository = NewsRepository.getInstance();

    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");
    private final MutableLiveData<List<NewsItem>> filteredNews = new MutableLiveData<>();

    public NewsViewModel() {
        // Load initial "All" list
        filteredNews.setValue(repository.getNewsByCategory("All"));
    }

    public LiveData<List<NewsItem>> getFilteredNews() {
        return filteredNews;
    }

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public List<NewsItem> getFeaturedNews() {
        return repository.getFeaturedNews();
    }

    /** Called when user taps a category chip */
    public void setCategory(String category) {
        selectedCategory.setValue(category);
        filteredNews.setValue(repository.getNewsByCategory(category));
    }

    /** Get related news for the detail screen */
    public List<NewsItem> getRelatedNews(int currentId, String category) {
        return repository.getRelatedNews(currentId, category);
    }

    /** Fetch a single item by id for the detail screen */
    public NewsItem getNewsById(int id) {
        return repository.getNewsById(id);
    }
}
