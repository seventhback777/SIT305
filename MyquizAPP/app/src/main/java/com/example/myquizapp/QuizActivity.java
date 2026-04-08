package com.example.myquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private static final String PREFS = "QuizPrefs";

    private TextView tvWelcome, tvProgress, tvQuestion;
    private ProgressBar progressBar;
    private Button[] optionBtns;
    private Button btnSubmit, btnNext, btnToggleTheme;

    private List<Question> allQuestions;
    private List<Question> questionList;
    private int currentIndex;
    private int score;
    private Option selectedOption;
    private boolean isSubmitted;
    private boolean isWelcomeDismissed;
    private boolean isDark;
    private String userName;
    private SharedPreferences prefs;

    private static final int COLOR_CORRECT = Color.parseColor("#4CAF50");
    private static final int COLOR_WRONG = Color.parseColor("#F44336");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        isDark = prefs.getBoolean("isDark", false);
        setTheme(isDark ? R.style.Theme_QuizApp_Dark : R.style.Theme_QuizApp_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        userName = getIntent().getStringExtra("userName");
        if (userName == null) userName = prefs.getString("userName", "");

        tvWelcome = findViewById(R.id.tvWelcome);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        progressBar = findViewById(R.id.progressBar);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNext = findViewById(R.id.btnNext);
        btnToggleTheme = findViewById(R.id.btnToggleTheme);
        optionBtns = new Button[]{
                findViewById(R.id.btnOption1),
                findViewById(R.id.btnOption2),
                findViewById(R.id.btnOption3)
        };

        btnToggleTheme.setText(isDark ? "☀️" : "🌙");

        buildQuestions();
        initQuestionOrder();

        currentIndex = prefs.getInt("currentIndex", 0);
        score = prefs.getInt("score", 0);
        isSubmitted = prefs.getBoolean("isSubmitted", false);
        isWelcomeDismissed = prefs.getBoolean("welcomeDismissed", false);

        tvWelcome.setText("Welcome, " + userName + "!");
        if (isWelcomeDismissed || currentIndex > 0) {
            tvWelcome.setVisibility(View.GONE);
        } else {
            tvWelcome.setVisibility(View.VISIBLE);
            startWelcomeFadeOut();
        }

        loadQuestion();

        btnToggleTheme.setOnClickListener(v -> {
            prefs.edit()
                    .putInt("currentIndex", currentIndex)
                    .putInt("score", score)
                    .putBoolean("isSubmitted", isSubmitted)
                    .putString("userName", userName)
                    .putInt("selectedOptionId", selectedOption != null ? selectedOption.getId() : -1)
                    .putBoolean("welcomeDismissed",
                            isWelcomeDismissed || currentIndex > 0 || tvWelcome.getVisibility() == View.GONE)
                    .putBoolean("isDark", !isDark)
                    .apply();
            recreate();
        });

        btnSubmit.setOnClickListener(v -> {
            if (selectedOption == null) {
                Toast.makeText(this, "Please select an option first!", Toast.LENGTH_SHORT).show();
                return;
            }
            submitAnswer();
        });

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            prefs.edit()
                    .putInt("currentIndex", currentIndex)
                    .putBoolean("isSubmitted", false)
                    .putInt("selectedOptionId", -1)
                    .remove("optionOrder")
                    .remove("optionOrderIndex")
                    .apply();

            if (currentIndex >= questionList.size()) {
                prefs.edit()
                        .remove("currentIndex")
                        .remove("score")
                        .remove("selectedOptionId")
                        .remove("questionOrder")
                        .remove("isSubmitted")
                        .remove("welcomeDismissed")
                        .remove("optionOrder")
                        .remove("optionOrderIndex")
                        .apply();

                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("score", score);
                intent.putExtra("total", questionList.size());
                startActivity(intent);
            } else {
                selectedOption = null;
                isSubmitted = false;
                loadQuestion();
            }
        });
    }

    private void buildQuestions() {
        allQuestions = new ArrayList<>();

        allQuestions.add(new Question(
                "What is the capital of Australia?",
                Arrays.asList(
                        new Option(1, "Sydney", false),
                        new Option(2, "Canberra", true),
                        new Option(3, "Melbourne", false)
                )
        ));

        allQuestions.add(new Question(
                "Which planet is known as the Red Planet?",
                Arrays.asList(
                        new Option(4, "Venus", false),
                        new Option(5, "Mars", true),
                        new Option(6, "Jupiter", false)
                )
        ));

        allQuestions.add(new Question(
                "What is the result of 2 + 2 × 2?",
                Arrays.asList(
                        new Option(7, "8", false),
                        new Option(8, "6", true),
                        new Option(9, "4", false)
                )
        ));

        allQuestions.add(new Question(
                "Which language is primarily used for Android development?",
                Arrays.asList(
                        new Option(10, "Swift", false),
                        new Option(11, "Java", true),
                        new Option(12, "Python", false)
                )
        ));

        allQuestions.add(new Question(
                "Who wrote 'Romeo and Juliet'?",
                Arrays.asList(
                        new Option(13, "Charles Dickens", false),
                        new Option(14, "William Shakespeare", true),
                        new Option(15, "Jane Austen", false)
                )
        ));
    }

    private void initQuestionOrder() {
        String savedOrder = prefs.getString("questionOrder", null);

        if (savedOrder != null) {
            questionList = new ArrayList<>();
            for (String part : savedOrder.split(",")) {
                questionList.add(allQuestions.get(Integer.parseInt(part.trim())));
            }
        } else {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < allQuestions.size(); i++) indices.add(i);
            Collections.shuffle(indices);

            questionList = new ArrayList<>();
            StringBuilder order = new StringBuilder();
            for (int i = 0; i < indices.size(); i++) {
                questionList.add(allQuestions.get(indices.get(i)));
                if (i > 0) order.append(",");
                order.append(indices.get(i));
            }
            prefs.edit().putString("questionOrder", order.toString()).apply();
        }
    }

    private void loadQuestion() {
        Question current = questionList.get(currentIndex);
        tvQuestion.setText(current.getQuestionText());

        int total = questionList.size();
        int percentage = (currentIndex + 1) * 100 / total;
        progressBar.setProgress(percentage);
        tvProgress.setText("Question " + (currentIndex + 1) + "/" + total + " (" + percentage + "%)");

        List<Option> options = getOrderedOptions(current);

        int savedSelectedId = prefs.getInt("selectedOptionId", -1);
        selectedOption = null;

        for (int i = 0; i < optionBtns.length; i++) {
            Option opt = options.get(i);
            optionBtns[i].setText(opt.getText());
            optionBtns[i].setTag(opt);
            optionBtns[i].setEnabled(true);
            optionBtns[i].setClickable(true);
            setButtonDefault(optionBtns[i]);

            if (opt.getId() == savedSelectedId) selectedOption = opt;

            final int idx = i;
            optionBtns[i].setOnClickListener(v ->
                    selectedOption = (Option) optionBtns[idx].getTag()
            );
        }

        if (isSubmitted) {
            applyFeedbackColors(savedSelectedId);
            for (Button btn : optionBtns) {
                btn.setEnabled(false);
                btn.setAlpha(1.0f);
            }
            btnSubmit.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
        } else {
            btnSubmit.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
        }
    }

    private List<Option> getOrderedOptions(Question question) {
        List<Option> options = new ArrayList<>(question.getOptions());
        int savedIdx = prefs.getInt("optionOrderIndex", -1);
        String savedOrder = prefs.getString("optionOrder", null);

        if (savedOrder != null && savedIdx == currentIndex) {
            List<Option> ordered = new ArrayList<>();
            for (String part : savedOrder.split(",")) {
                int optId = Integer.parseInt(part.trim());
                for (Option opt : options) {
                    if (opt.getId() == optId) {
                        ordered.add(opt);
                        break;
                    }
                }
            }
            return ordered;
        } else {
            Collections.shuffle(options);
            StringBuilder order = new StringBuilder();
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) order.append(",");
                order.append(options.get(i).getId());
            }
            prefs.edit()
                    .putString("optionOrder", order.toString())
                    .putInt("optionOrderIndex", currentIndex)
                    .apply();
            return options;
        }
    }

    private void submitAnswer() {
        isSubmitted = true;
        if (selectedOption.isCorrect()) score++;

        prefs.edit()
                .putInt("score", score)
                .putBoolean("isSubmitted", true)
                .putInt("selectedOptionId", selectedOption.getId())
                .apply();

        applyFeedbackColors(selectedOption.getId());

        for (Button btn : optionBtns) {
            btn.setEnabled(false);
            btn.setAlpha(1.0f);
        }

        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void applyFeedbackColors(int selectedId) {
        for (Button btn : optionBtns) {
            Option opt = (Option) btn.getTag();
            if (opt == null) continue;
            if (opt.isCorrect()) {
                setButtonColor(btn, COLOR_CORRECT, Color.WHITE);
            } else if (opt.getId() == selectedId) {
                setButtonColor(btn, COLOR_WRONG, Color.WHITE);
            }
        }
    }

    private void setButtonDefault(Button btn) {
        int bg = isDark ? Color.parseColor("#3E3E3E") : Color.parseColor("#E0E0E0");
        int text = isDark ? Color.WHITE : Color.parseColor("#212121");
        setButtonColor(btn, bg, text);
    }

    private void setButtonColor(Button btn, int bgColor, int textColor) {
        btn.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        btn.setTextColor(textColor);
        btn.setAlpha(1.0f);
    }

    private void startWelcomeFadeOut() {
        tvWelcome.postDelayed(() -> {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(1000);
            fadeOut.setFillAfter(true);
            tvWelcome.startAnimation(fadeOut);
            tvWelcome.postDelayed(() -> {
                tvWelcome.setVisibility(View.GONE);
                isWelcomeDismissed = true;
                prefs.edit().putBoolean("welcomeDismissed", true).apply();
            }, 1000);
        }, 2000);
    }
}