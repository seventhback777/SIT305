package com.example.aihelperapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class QuizViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final LLMRepository llmRepo;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<List<LLMRepository.QuizQuestion>> questions = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    public final MutableLiveData<List<String>> explanations = new MutableLiveData<>();

    private final List<String> selectedAnswers = new ArrayList<>();
    private long sessionStart;

    public QuizViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        llmRepo = new LLMRepository();
    }

    public void loadQuizForTask(String taskTitle, String taskDescription) {
        isLoading.postValue(true);
        selectedAnswers.clear();
        currentQuestionIndex.postValue(0);
        sessionStart = System.currentTimeMillis();

        llmRepo.generateQuiz(taskTitle, taskDescription, new LLMRepository.QuizCallback() {
            @Override
            public void onSuccess(List<LLMRepository.QuizQuestion> result) {
                questions.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void submitAnswer(String answer) {
        selectedAnswers.add(answer);
    }

    public void goToNextQuestion() {
        Integer current = currentQuestionIndex.getValue();
        if (current != null) currentQuestionIndex.postValue(current + 1);
    }

    public boolean isLastQuestion() {
        List<LLMRepository.QuizQuestion> qs = questions.getValue();
        Integer idx = currentQuestionIndex.getValue();
        if (qs == null || idx == null) return true;
        return idx >= qs.size() - 1;
    }

    public void saveResultsAndExplain(int userId) {
        List<LLMRepository.QuizQuestion> qs = questions.getValue();
        if (qs == null || selectedAnswers.size() != qs.size()) return;

        isLoading.postValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<QuizHistory> histories = new ArrayList<>();
            for (int i = 0; i < qs.size(); i++) {
                LLMRepository.QuizQuestion q = qs.get(i);
                String selected = selectedAnswers.get(i);
                QuizHistory h = new QuizHistory();
                h.userId = userId;
                h.questionText = q.question;
                h.optionA = q.optionA;
                h.optionB = q.optionB;
                h.optionC = q.optionC;
                h.optionD = q.optionD;
                h.correctAnswer = q.correctAnswer;
                h.selectedAnswer = selected;
                h.isCorrect = selected.equals(q.correctAnswer);
                h.timestamp = sessionStart + i;
                db.quizHistoryDao().insert(h);
                histories.add(h);
            }

            llmRepo.explainAnswers(histories, new LLMRepository.ExplanationCallback() {
                @Override
                public void onSuccess(List<String> result) {
                    explanations.postValue(result);
                    isLoading.postValue(false);
                }

                @Override
                public void onError(String error) {
                    List<String> fallback = new ArrayList<>();
                    for (int i = 0; i < histories.size(); i++) fallback.add("");
                    explanations.postValue(fallback);
                    isLoading.postValue(false);
                }
            });
        });
    }

    public List<QuizHistory> buildHistorySnapshot() {
        List<LLMRepository.QuizQuestion> qs = questions.getValue();
        if (qs == null) return new ArrayList<>();
        List<QuizHistory> list = new ArrayList<>();
        for (int i = 0; i < qs.size() && i < selectedAnswers.size(); i++) {
            LLMRepository.QuizQuestion q = qs.get(i);
            QuizHistory h = new QuizHistory();
            h.questionText = q.question;
            h.optionA = q.optionA;
            h.optionB = q.optionB;
            h.optionC = q.optionC;
            h.optionD = q.optionD;
            h.correctAnswer = q.correctAnswer;
            h.selectedAnswer = selectedAnswers.get(i);
            h.isCorrect = selectedAnswers.get(i).equals(q.correctAnswer);
            list.add(h);
        }
        return list;
    }
}
