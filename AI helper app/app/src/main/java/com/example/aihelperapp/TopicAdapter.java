package com.example.aihelperapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private List<Topic> topics = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();
    private Runnable onSelectionChanged;

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
        notifyDataSetChanged();
    }

    public void setOnSelectionChanged(Runnable r) {
        this.onSelectionChanged = r;
    }

    public List<Integer> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topics.get(position);
        holder.tvTopic.setText(topic.name);
        holder.itemView.setSelected(selectedIds.contains(topic.id));
        holder.itemView.setOnClickListener(v -> {
            if (selectedIds.contains(topic.id)) {
                selectedIds.remove(topic.id);
            } else if (selectedIds.size() < 10) {
                selectedIds.add(topic.id);
            }
            notifyItemChanged(position);
            if (onSelectionChanged != null) onSelectionChanged.run();
        });
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic;

        TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvTopicName);
        }
    }
}
