package com.example.aihelperapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private int userId;

    private TextView tvUsername, tvEmail, tvTotal, tvCorrect, tvIncorrect, tvSummary;
    private CardView cardSummary;
    private ProgressBar progressSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userId = userViewModel.getLoggedInUserId();

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        Button btnSummarise = view.findViewById(R.id.btnSummarise);
        Button btnShare = view.findViewById(R.id.btnShare);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvCorrect = view.findViewById(R.id.tvCorrect);
        tvIncorrect = view.findViewById(R.id.tvIncorrect);
        tvSummary = view.findViewById(R.id.tvSummary);
        cardSummary = view.findViewById(R.id.cardSummary);
        progressSummary = view.findViewById(R.id.progressSummary);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        userViewModel.getUserLive(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUsername.setText(user.username);
                tvEmail.setText(user.email);
            }
        });

        // Show subscription tier in notification banner
        TextView tvNotification = view.findViewById(R.id.tvNotification);
        SharedPreferences prefs = requireContext().getSharedPreferences("learning_prefs", 0);
        String tier = prefs.getString("subscription_tier", null);
        if (tier != null) {
            tvNotification.setText("⭐ " + tier + " subscriber — enjoy your benefits!");
        }

        userViewModel.getQuizStats(userId).observe(getViewLifecycleOwner(), stats -> {
            tvTotal.setText(String.valueOf(stats[0]));
            tvCorrect.setText(String.valueOf(stats[1]));
            tvIncorrect.setText(String.valueOf(stats[2]));
        });

        btnSummarise.setOnClickListener(v -> {
            cardSummary.setVisibility(View.VISIBLE);
            progressSummary.setVisibility(View.VISIBLE);
            tvSummary.setText("");
            btnSummarise.setEnabled(false);

            userViewModel.summarizeWrongAnswers(userId, new LLMRepository.LLMCallback() {
                @Override
                public void onSuccess(String result) {
                    requireActivity().runOnUiThread(() -> {
                        progressSummary.setVisibility(View.GONE);
                        tvSummary.setText(result);
                        btnSummarise.setEnabled(true);
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        progressSummary.setVisibility(View.GONE);
                        tvSummary.setText("Could not load summary. Please try again.");
                        btnSummarise.setEnabled(true);
                    });
                }
            });
        });

        btnShare.setOnClickListener(v -> shareProfile());
    }

    private void shareProfile() {
        String username = tvUsername.getText().toString();
        String total = tvTotal.getText().toString();
        String correct = tvCorrect.getText().toString();
        String incorrect = tvIncorrect.getText().toString();

        String shareText = "Check out my AI Learning Assistant progress!\n\n"
                + "👤 " + username + "\n"
                + "📚 Total Questions: " + total + "\n"
                + "✅ Correct Answers: " + correct + "\n"
                + "❌ Incorrect Answers: " + incorrect + "\n\n"
                + "Download the AI Learning Assistant app to start your learning journey!";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share Profile"));
    }
}
