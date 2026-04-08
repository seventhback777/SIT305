package com.example.eventapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class Event {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String category;
    private String location;
    private long dateTime;       // stored as milliseconds since epoch
    private String description;  // optional

    public Event(String title, String category, String location,
                 long dateTime, String description) {
        this.title       = title;
        this.category    = category;
        this.location    = location;
        this.dateTime    = dateTime;
        this.description = description;
    }

    // Getters
    public int    getId()          { return id; }
    public String getTitle()       { return title; }
    public String getCategory()    { return category; }
    public String getLocation()    { return location; }
    public long   getDateTime()    { return dateTime; }
    public String getDescription() { return description; }

    // Setters
    public void setId(int id)                   { this.id = id; }
    public void setTitle(String title)           { this.title = title; }
    public void setCategory(String category)     { this.category = category; }
    public void setLocation(String location)     { this.location = location; }
    public void setDateTime(long dateTime)       { this.dateTime = dateTime; }
    public void setDescription(String desc)      { this.description = desc; }
}
