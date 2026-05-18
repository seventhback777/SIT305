package com.example.lostandfoundapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfoundapp.R;
import com.example.lostandfoundapp.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(int postId);
    }

    private List<Post> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public PostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostType, tvCategory, tvName, tvDate, tvLocation;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostType = itemView.findViewById(R.id.tv_post_type);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLocation = itemView.findViewById(R.id.tv_location);
        }

        void bind(Post post, OnPostClickListener listener) {
            tvPostType.setText(post.getPostType());
            tvCategory.setText(post.getCategory());
            tvName.setText(post.getName());
            tvDate.setText(post.getDate());
            tvLocation.setText(post.getLocation());

            if ("Lost".equals(post.getPostType())) {
                tvPostType.setBackgroundResource(R.drawable.badge_lost);
            } else {
                tvPostType.setBackgroundResource(R.drawable.badge_found);
            }

            itemView.setOnClickListener(v -> listener.onPostClick(post.getId()));
        }
    }
}
