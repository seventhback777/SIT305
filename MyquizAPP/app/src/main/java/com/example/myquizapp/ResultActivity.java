package com.example.myquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private static final String PREFS = "QuizPrefs";
    private String userName;
    private SharedPreferences prefs;
    private boolean isDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        isDark = prefs.getBoolean("isDark", false);
        setTheme(isDark ? R.style.Theme_QuizApp_Dark : R.style.Theme_QuizApp_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        userName = getIntent().getStringExtra("userName");
        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 5);

        TextView tvCongrats = findViewById(R.id.tvCongrats);
        TextView tvScore = findViewById(R.id.tvScore);
        Button btnNewQuiz = findViewById(R.id.btnNewQuiz);
        Button btnFinish = findViewById(R.id.btnFinish);
        Button btnToggleTheme = findViewById(R.id.btnToggleTheme);

        tvCongrats.setText("Congratulations, " + userName + "!");
        tvScore.setText("Your Score: " + score + "/" + total);
        btnToggleTheme.setText(isDark ? "☀️ Light Mode" : "🌙 Dark Mode");

        btnToggleTheme.setOnClickListener(v -> {
            prefs.edit().putBoolean("isDark", !isDark).apply();
            recreate();
        });

        btnNewQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("userName", userName);
            startActivity(intent);
        });

        btnFinish.setOnClickListener(v -> finishAffinity());
    }
}