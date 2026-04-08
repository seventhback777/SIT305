package com.example.myquizapp;

import java.util.List;

public class Question {
    private final String questionText;
    private final List<Option> options;

    public Question(String questionText, List<Option> options) {
        this.questionText = questionText;
        this.options = options;
    }

    public String getQuestionText() { return questionText; }
    public List<Option> getOptions() { return options; }
}
