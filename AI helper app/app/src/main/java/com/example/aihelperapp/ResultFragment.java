package com.example.aihelperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResultFragment extends Fragment {

    private QuizViewModel quizViewModel;
    private ResultAdapter resultAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        quizViewModel = new ViewModelProvider(requireActivity()).get(QuizViewModel.class);

        RecyclerView rvResults = view.findViewById(R.id.rvResults);
        Button btnBackHome = view.findViewById(R.id.btnBackHome);
        TextView tvScore = view.findViewById(R.id.tvScore);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvLoading = view.findViewById(R.id.tvLoadingExplanations);

        resultAdapter = new ResultAdapter();
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(resultAdapter);

        List<QuizHistory> histories = quizViewModel.buildHistorySnapshot();

        int correct = 0;
        for (QuizHistory h : histories) if (h.isCorrect) correct++;
        tvScore.setText(correct + " / " + histories.size() + " correct");

        resultAdapter.setData(histories, new ArrayList<>());

        progressBar.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);

        quizViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (!Boolean.TRUE.equals(loading)) {
                progressBar.setVisibility(View.GONE);
                tvLoading.setVisibility(View.GONE);
            }
        });

        quizViewModel.explanations.observe(getViewLifecycleOwner(), explanations -> {
            if (explanations != null) {
                progressBar.setVisibility(View.GONE);
                tvLoading.setVisibility(View.GONE);
                resultAdapter.setData(histories, explanations);
            }
        });

        btnBackHome.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_result_to_home));
    }
}
