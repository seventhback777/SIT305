package com.example.myquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "QuizPrefs";
    private EditText etName;
    private SharedPreferences prefs;
    private boolean isDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        isDark = prefs.getBoolean("isDark", false);
        setTheme(isDark ? R.style.Theme_QuizApp_Dark : R.style.Theme_QuizApp_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnToggleTheme = findViewById(R.id.btnToggleTheme);

        btnToggleTheme.setText(isDark ? "☀️ Light Mode" : "🌙 Dark Mode");
        handleIncomingName(getIntent());

        btnStart.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .remove("currentIndex")
                    .remove("score")
                    .remove("selectedOptionId")
                    .remove("questionOrder")
                    .remove("isSubmitted")
                    .remove("welcomeDismissed")
                    .remove("optionOrder")
                    .remove("optionOrderIndex")
                    .putString("userName", name)
                    .apply();

            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("userName", name);
            startActivity(intent);
        });

        btnToggleTheme.setOnClickListener(v -> {
            prefs.edit().putBoolean("isDark", !isDark).apply();
            recreate();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingName(intent);
    }

    private void handleIncomingName(Intent intent) {
        if (intent == null) return;
        String name = intent.getStringExtra("userName");
        if (name != null && etName != null) {
            etName.setText(name);
        }
    }
}