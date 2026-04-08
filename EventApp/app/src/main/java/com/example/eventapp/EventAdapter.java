package com.example.eventapp;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public void setListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public Event getEventAt(int position) {
        return events.get(position);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDateTime, tvLocation;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tv_title);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
        }

        void bind(Event event) {
            tvTitle.setText(event.getTitle());
            tvCategory.setText(event.getCategory());
            tvLocation.setText(event.getLocation() != null ? event.getLocation() : "");
            SimpleDateFormat sdf =
                    new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault());
            tvDateTime.setText(sdf.format(new Date(event.getDateTime())));
        }
    }
}