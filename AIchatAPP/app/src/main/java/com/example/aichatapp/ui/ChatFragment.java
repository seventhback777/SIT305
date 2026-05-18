package com.example.aichatapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aichatapp.R;
import com.example.aichatapp.adapter.ChatAdapter;
import com.example.aichatapp.database.AppDatabase;
import com.example.aichatapp.llm.GeminiClient;
import com.example.aichatapp.model.Message;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.type.Content;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvTitle;

    private ChatAdapter adapter;
    private AppDatabase db;
    private GeminiClient geminiClient;

    private ChatFutures chat;
    private boolean chatInitialized = false;

    private long userId;
    private String username;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Read navigation arguments
        if (getArguments() != null) {
            userId   = getArguments().getLong("userId", -1);
            username = getArguments().getString("username", "Chat");
        }

        // Bind views
        recyclerView = view.findViewById(R.id.recycler_view);
        etMessage    = view.findViewById(R.id.et_message);
        btnSend      = view.findViewById(R.id.btn_send);
        tvTitle      = view.findViewById(R.id.tv_title);

        tvTitle.setText(username);

        // Set up RecyclerView
        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        db           = AppDatabase.getDatabase(requireContext());
        geminiClient = new GeminiClient();

        // Observe messages from Room
        // First emission initialises the Gemini chat with saved history.
        // Subsequent emissions (after inserts) only refresh the list.
        db.messageDao().getMessages(userId).observe(getViewLifecycleOwner(), messages -> {
            adapter.setMessages(messages);
            if (!messages.isEmpty()) {
                recyclerView.scrollToPosition(messages.size() - 1);
            }

            if (!chatInitialized) {
                List<Content> history = geminiClient.messagesToContents(messages);
                chat = geminiClient.startChat(history);
                chatInitialized = true;
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText() != null
                ? etMessage.getText().toString().trim() : "";

        if (text.isEmpty()) return;

        // Ensure chat exists even when there are no prior messages
        if (!chatInitialized) {
            chat = geminiClient.startChat(new ArrayList<>());
            chatInitialized = true;
        }

        etMessage.setText("");
        btnSend.setEnabled(false);

        long timestamp = System.currentTimeMillis();
        Message userMsg = new Message(userId, text, timestamp, true);

        // Persist the user message
        AppDatabase.getExecutor().execute(() ->
                db.messageDao().insertMessage(userMsg));

        // Ask Gemini
        geminiClient.sendMessage(chat, text, new GeminiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded()) return;

                Message botMsg = new Message(
                        userId, response, System.currentTimeMillis(), false);

                AppDatabase.getExecutor().execute(() ->
                        db.messageDao().insertMessage(botMsg));

                btnSend.setEnabled(true);
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        e.getMessage(), Toast.LENGTH_LONG).show();
                btnSend.setEnabled(true);
            }
        });
    }
}
