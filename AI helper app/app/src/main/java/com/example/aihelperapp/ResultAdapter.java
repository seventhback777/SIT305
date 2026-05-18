package com.example.aihelperapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {

    private List<QuizHistory> histories = new ArrayList<>();
    private List<String> explanations = new ArrayList<>();

    public void setData(List<QuizHistory> histories, List<String> explanations) {
        this.histories = histories;
        this.explanations = explanations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        QuizHistory h = histories.get(position);
        holder.tvQuestion.setText((position + 1) + ". " + h.questionText);
        holder.tvCorrectAnswer.setText("Correct: " + h.correctAnswer);
        holder.tvYourAnswer.setText("Your answer: " + h.selectedAnswer);

        if (h.isCorrect) {
            holder.tvResult.setText("Correct");
            holder.tvResult.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvResult.setText("Incorrect");
            holder.tvResult.setTextColor(Color.parseColor("#F44336"));
        }

        if (position < explanations.size() && !explanations.get(position).isEmpty()) {
            holder.tvExplanation.setText(explanations.get(position));
            holder.tvExplanation.setVisibility(View.VISIBLE);
        } else {
            holder.tvExplanation.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvResult, tvCorrectAnswer, tvYourAnswer, tvExplanation;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvResultQuestion);
            tvResult = itemView.findViewById(R.id.tvResultStatus);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvYourAnswer = itemView.findViewById(R.id.tvYourAnswer);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
        }
    }
}
