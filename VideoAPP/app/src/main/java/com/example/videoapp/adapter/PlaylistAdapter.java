package com.example.videoapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.R;
import com.example.videoapp.model.VideoEntry;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(VideoEntry entry);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(VideoEntry entry);
    }

    private List<VideoEntry> entries = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnDeleteClickListener deleteListener;

    public PlaylistAdapter(OnItemClickListener clickListener, OnDeleteClickListener deleteListener) {
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    public void submitList(List<VideoEntry> list) {
        this.entries = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUrl;
        private final ImageButton btnDelete;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrl = itemView.findViewById(R.id.tv_video_url);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(VideoEntry entry) {
            tvUrl.setText(entry.videoUrl);
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(entry);
            });
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDeleteClick(entry);
            });
        }
    }
}
