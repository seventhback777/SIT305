package com.example.sportsapp.model;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private int id;
    private String title;
    private String description;
    private String date;
    private int imageResId;
    private String category;
    private boolean isFeatured;

    public NewsItem(int id, String title, String description, String date,
                    int imageResId, String category, boolean isFeatured) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.imageResId = imageResId;
        this.category = category;
        this.isFeatured = isFeatured;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public int getImageResId() { return imageResId; }
    public String getCategory() { return category; }
    public boolean isFeatured() { return isFeatured; }
}
