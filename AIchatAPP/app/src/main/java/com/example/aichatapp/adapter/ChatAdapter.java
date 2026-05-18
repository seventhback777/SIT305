package com.example.aichatapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aichatapp.R;
import com.example.aichatapp.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT  = 2;

    private List<Message> messages = new ArrayList<>();

    // ── ViewHolders ────────────────────────────────────────────────────────────

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessage, tvTime;
        UserViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime    = v.findViewById(R.id.tv_time);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessage, tvTime;
        BotViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime    = v.findViewById(R.id.tv_time);
        }
    }

    // ── Adapter overrides ─────────────────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View v = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_bot, parent, false);
            return new BotViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.timestamp));

        if (holder instanceof UserViewHolder) {
            UserViewHolder vh = (UserViewHolder) holder;
            vh.tvMessage.setText(msg.content);
            vh.tvTime.setText(time);
        } else {
            BotViewHolder vh = (BotViewHolder) holder;
            vh.tvMessage.setText(msg.content);
            vh.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Full replace – used when LiveData delivers the DB snapshot. */
    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        notifyDataSetChanged();
    }
}
