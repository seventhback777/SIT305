package com.example.eventapp;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class EventViewModel extends AndroidViewModel {

    private final EventDao dao;
    private final LiveData<List<Event>> allEvents;

    public EventViewModel(@NonNull Application application) {
        super(application);
        EventDatabase db = EventDatabase.getInstance(application);
        dao       = db.eventDao();
        allEvents = dao.getAllEvents();
    }

    public LiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    public LiveData<Event> getEventById(int id) {
        MutableLiveData<Event> result = new MutableLiveData<>();
        new Thread(() -> result.postValue(dao.getEventById(id))).start();
        return result;
    }

    public void insertEvent(Event event) {
        new Thread(() -> dao.insertEvent(event)).start();
    }

    public void updateEvent(Event event) {
        new Thread(() -> dao.updateEvent(event)).start();
    }

    public void deleteEvent(Event event) {
        new Thread(() -> dao.deleteEvent(event)).start();
    }
}
