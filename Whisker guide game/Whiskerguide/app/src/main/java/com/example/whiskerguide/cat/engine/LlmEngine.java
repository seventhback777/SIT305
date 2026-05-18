package com.example.whiskerguide.cat.engine;

import android.content.Context;

public interface LlmEngine {
    void initialize(Context context, InitCallback callback);
    void generate(String prompt, LlmCallback callback);
    void close();
}
