package com.example.whiskerguide.cat.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whiskerguide.R;
import com.example.whiskerguide.cat.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CAT = 0;
    private static final int TYPE_PLAYER = 1;

    private final List<ChatMessage> messages = new ArrayList<>();

    public void submit(List<ChatMessage> newList) {
        messages.clear();
        if (newList != null) messages.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isFromCat() ? TYPE_CAT : TYPE_PLAYER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CAT) {
            View v = inflater.inflate(R.layout.item_chat_cat, parent, false);
            return new MessageVH(v);
        }
        View v = inflater.inflate(R.layout.item_chat_player, parent, false);
        return new MessageVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MessageVH) holder).bind(messages.get(position));
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class MessageVH extends RecyclerView.ViewHolder {
        final TextView text;
        MessageVH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_message);
        }
        void bind(ChatMessage msg) {
            text.setText(msg.getContent());
        }
    }
}
