package com.example.aihelperapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onStartQuiz(Task task);
    }

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener listener;

    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.title);
        holder.tvDescription.setText(task.description);
        holder.tvTopic.setText(task.topic);
        holder.btnStartQuiz.setEnabled(!task.isCompleted);
        holder.btnStartQuiz.setText(task.isCompleted ? "Completed" : "Start Quiz");
        holder.btnStartQuiz.setOnClickListener(v -> listener.onStartQuiz(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTopic;
        Button btnStartQuiz;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTopic = itemView.findViewById(R.id.tvTaskTopic);
            btnStartQuiz = itemView.findViewById(R.id.btnStartQuiz);
        }
    }
}
