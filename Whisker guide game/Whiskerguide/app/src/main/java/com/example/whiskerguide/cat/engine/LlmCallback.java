package com.example.whiskerguide.cat.engine;

public interface LlmCallback {
    void onResult(String result);
    void onError(String error);
}
