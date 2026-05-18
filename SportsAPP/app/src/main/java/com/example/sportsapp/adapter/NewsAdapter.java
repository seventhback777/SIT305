package com.example.sportsapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsapp.R;
import com.example.sportsapp.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    private List<NewsItem> newsList = new ArrayList<>();
    private OnItemClickListener listener;

    public NewsAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NewsItem> items) {
        this.newsList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        holder.bind(newsList.get(position));
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgThumbnail;
        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvDate;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvCategory = itemView.findViewById(R.id.tv_news_category);
            tvDate = itemView.findViewById(R.id.tv_news_date);
        }

        void bind(NewsItem item) {
            imgThumbnail.setImageResource(item.getImageResId());
            tvTitle.setText(item.getTitle());
            tvCategory.setText(item.getCategory());
            tvDate.setText(item.getDate());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}
