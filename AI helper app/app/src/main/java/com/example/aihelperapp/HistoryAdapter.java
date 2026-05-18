package com.example.aihelperapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<QuizHistory> items = new ArrayList<>();
    private final Set<Integer> expandedPositions = new HashSet<>();

    public void setItems(List<QuizHistory> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizHistory h = items.get(position);
        boolean expanded = expandedPositions.contains(position);

        holder.tvQuestionNumber.setText((position + 1) + ".");
        holder.tvQuestion.setText(h.questionText);
        holder.layoutDetail.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.ivExpand.setRotation(expanded ? 180f : 0f);

        if (expanded) {
            bindOption(holder.tvOptionA, "A", h.optionA, h.correctAnswer, h.selectedAnswer);
            bindOption(holder.tvOptionB, "B", h.optionB, h.correctAnswer, h.selectedAnswer);
            bindOption(holder.tvOptionC, "C", h.optionC, h.correctAnswer, h.selectedAnswer);
            bindOption(holder.tvOptionD, "D", h.optionD, h.correctAnswer, h.selectedAnswer);

            if (h.isCorrect) {
                holder.tvYourAnswer.setText("Your answer: " + h.selectedAnswer + " ✓ Correct!");
                holder.tvYourAnswer.setTextColor(Color.parseColor("#69F0AE"));
            } else {
                holder.tvYourAnswer.setText("Your answer: " + h.selectedAnswer + " ✗  Correct: " + h.correctAnswer);
                holder.tvYourAnswer.setTextColor(Color.parseColor("#FF5252"));
            }
        }

        holder.layoutHeader.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (expandedPositions.contains(pos)) {
                expandedPositions.remove(pos);
            } else {
                expandedPositions.add(pos);
            }
            notifyItemChanged(pos);
        });
    }

    private void bindOption(TextView tv, String letter, String text,
                            String correctAnswer, String selectedAnswer) {
        tv.setText(letter + ". " + text);
        if (letter.equals(correctAnswer)) {
            tv.setTextColor(Color.parseColor("#69F0AE"));
        } else if (letter.equals(selectedAnswer)) {
            tv.setTextColor(Color.parseColor("#FF5252"));
        } else {
            tv.setTextColor(Color.parseColor("#B3E5FC"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutHeader, layoutDetail;
        TextView tvQuestionNumber, tvQuestion;
        TextView tvOptionA, tvOptionB, tvOptionC, tvOptionD, tvYourAnswer;
        ImageView ivExpand;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layoutHeader);
            layoutDetail = itemView.findViewById(R.id.layoutDetail);
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvOptionA = itemView.findViewById(R.id.tvOptionA);
            tvOptionB = itemView.findViewById(R.id.tvOptionB);
            tvOptionC = itemView.findViewById(R.id.tvOptionC);
            tvOptionD = itemView.findViewById(R.id.tvOptionD);
            tvYourAnswer = itemView.findViewById(R.id.tvYourAnswer);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }
    }
}
