package com.example.aichatapp.llm;

import com.example.aichatapp.BuildConfig;
import android.os.Handler;
import android.os.Looper;

import com.example.aichatapp.model.Message;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiClient {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String MODEL_NAME = "gemini-2.5-flash";

    public interface Callback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }

    private final GenerativeModelFutures model;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public GeminiClient() {
        GenerativeModel gm = new GenerativeModel(MODEL_NAME, API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public ChatFutures startChat(List<Content> history) {
        if (history == null || history.isEmpty()) {
            return model.startChat();
        }
        return model.startChat(history);
    }

    public void sendMessage(ChatFutures chat, String text, Callback callback) {
        Content.Builder builder = new Content.Builder();
        builder.setRole("user");
        builder.addText(text);
        Content userContent = builder.build();

        ListenableFuture<GenerateContentResponse> future = chat.sendMessage(userContent);

        executor.submit(() -> {
            try {
                GenerateContentResponse response = future.get();
                String responseText = response.getText();
                mainHandler.post(() ->
                        callback.onSuccess(responseText != null ? responseText : ""));
            } catch (Exception e) {
                mainHandler.post(() ->
                        callback.onFailure(new Exception("Gemini error: " + e.getMessage(), e)));
            }
        });
    }

    public List<Content> messagesToContents(List<Message> messages) {
        List<Content> contents = new ArrayList<>();
        for (Message msg : messages) {
            Content.Builder builder = new Content.Builder();
            builder.setRole(msg.isUser ? "user" : "model");
            builder.addText(msg.content);
            contents.add(builder.build());
        }
        return contents;
    }
}
