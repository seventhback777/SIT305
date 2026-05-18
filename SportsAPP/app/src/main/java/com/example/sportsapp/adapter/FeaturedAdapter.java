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

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    private List<NewsItem> featuredList = new ArrayList<>();
    private OnItemClickListener listener;

    public FeaturedAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NewsItem> items) {
        this.featuredList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        holder.bind(featuredList.get(position));
    }

    @Override
    public int getItemCount() {
        return featuredList.size();
    }

    class FeaturedViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgFeatured;
        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvDate;

        FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFeatured = itemView.findViewById(R.id.img_featured);
            tvTitle = itemView.findViewById(R.id.tv_featured_title);
            tvCategory = itemView.findViewById(R.id.tv_featured_category);
            tvDate = itemView.findViewById(R.id.tv_featured_date);
        }

        void bind(NewsItem item) {
            imgFeatured.setImageResource(item.getImageResId());
            tvTitle.setText(item.getTitle());
            tvCategory.setText(item.getCategory());
            tvDate.setText(item.getDate());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}
