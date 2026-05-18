package com.example.aihelperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.List;

public class QuizFragment extends Fragment {

    private QuizViewModel quizViewModel;
    private TaskViewModel taskViewModel;
    private UserViewModel userViewModel;
    private boolean quizLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        quizViewModel = new ViewModelProvider(requireActivity()).get(QuizViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        LinearLayout layoutQuiz = view.findViewById(R.id.layoutQuiz);
        TextView tvProgress = view.findViewById(R.id.tvProgress);
        TextView tvQuestion = view.findViewById(R.id.tvQuestion);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        RadioButton rbA = view.findViewById(R.id.rbOptionA);
        RadioButton rbB = view.findViewById(R.id.rbOptionB);
        RadioButton rbC = view.findViewById(R.id.rbOptionC);
        RadioButton rbD = view.findViewById(R.id.rbOptionD);
        Button btnNext = view.findViewById(R.id.btnNext);

        int taskId = getArguments() != null ? getArguments().getInt("taskId") : -1;

        taskViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null && !quizLoaded) {
                quizLoaded = true;
                quizViewModel.loadQuizForTask(task.title, task.description);
            }
        });

        quizViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            layoutQuiz.setVisibility(Boolean.TRUE.equals(loading) ? View.GONE : View.VISIBLE);
        });

        quizViewModel.questions.observe(getViewLifecycleOwner(), questions -> {
            if (questions != null && !questions.isEmpty()) {
                Integer idx = quizViewModel.currentQuestionIndex.getValue();
                showQuestion(questions, idx, tvProgress, tvQuestion, rbA, rbB, rbC, rbD, radioGroup);
                btnNext.setText((idx != null && idx >= questions.size() - 1) ? "Finish" : "Next");
            }
        });

        quizViewModel.currentQuestionIndex.observe(getViewLifecycleOwner(), index -> {
            List<LLMRepository.QuizQuestion> questions = quizViewModel.questions.getValue();
            if (questions != null && index != null && index < questions.size()) {
                showQuestion(questions, index, tvProgress, tvQuestion, rbA, rbB, rbC, rbD, radioGroup);
                btnNext.setText(index >= questions.size() - 1 ? "Finish" : "Next");
            }
        });

        quizViewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            String answer;
            if (selectedId == R.id.rbOptionA) answer = "A";
            else if (selectedId == R.id.rbOptionB) answer = "B";
            else if (selectedId == R.id.rbOptionC) answer = "C";
            else answer = "D";

            quizViewModel.submitAnswer(answer);

            if (quizViewModel.isLastQuestion()) {
                int userId = userViewModel.getLoggedInUserId();
                quizViewModel.saveResultsAndExplain(userId);
                taskViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
                    if (task != null) taskViewModel.markTaskCompleted(task);
                });
                Navigation.findNavController(view).navigate(R.id.action_quiz_to_result);
            } else {
                quizViewModel.goToNextQuestion();
                radioGroup.clearCheck();
            }
        });
    }

    private void showQuestion(List<LLMRepository.QuizQuestion> questions, Integer index,
                               TextView tvProgress, TextView tvQuestion,
                               RadioButton rbA, RadioButton rbB, RadioButton rbC, RadioButton rbD,
                               RadioGroup radioGroup) {
        if (index == null || index >= questions.size()) return;
        LLMRepository.QuizQuestion q = questions.get(index);
        tvProgress.setText("Question " + (index + 1) + " of " + questions.size());
        tvQuestion.setText(q.question);
        rbA.setText("A. " + q.optionA);
        rbB.setText("B. " + q.optionB);
        rbC.setText("C. " + q.optionC);
        rbD.setText("D. " + q.optionD);
        radioGroup.clearCheck();
    }
}
