package com.example.myquizapp;

public class Option {
    private final int id;
    private final String text;
    private final boolean isCorrect;

    public Option(int id, String text, boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public boolean isCorrect() { return isCorrect; }
}