package com.example.whiskerguide.cat.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaPipeLlmEngine implements LlmEngine {

    private static final String TAG = "MediaPipeLlm";
    private static final String MODEL_FILENAME = "gemma.task";
    // Phi-4-mini-instruct ekv4096 支持 4096 上下文。
    // 留出 prompt + 输出空间,这里设 2048(实际 prompt ~500 tokens,输出 ~200 tokens)。
    private static final int MAX_TOKENS = 2048;
    private static final int TOP_K = 40;
    private static final float TEMPERATURE = 0.7f;
    private static final float TOP_P = 0.9f;

    private LlmInference inference;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean ready = false;
    // 用对象锁串行化 native 调用,避免 generate 与 close 并发触发 native crash。
    private final Object inferenceLock = new Object();

    public boolean isReady() { return ready; }

    @Override
    public void initialize(Context context, InitCallback callback) {
        Context appCtx = context.getApplicationContext();
        executor.execute(() -> {
            try {
                File modelFile = resolveModelFile(appCtx);
                if (modelFile == null) {
                    fail(callback, "Model file " + MODEL_FILENAME +
                            " not found. Place it in /data/local/tmp/llm/ or the app's external files dir.");
                    return;
                }
                Log.i(TAG, "Loading model from " + modelFile.getAbsolutePath());

                LlmInferenceOptions options = LlmInferenceOptions.builder()
                        .setModelPath(modelFile.getAbsolutePath())
                        .setMaxTokens(MAX_TOKENS)
                        .setMaxTopK(TOP_K)
                        .build();

                inference = LlmInference.createFromOptions(appCtx, options);
                ready = true;
                Log.i(TAG, "Model loaded successfully");
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess();
                });
            } catch (Throwable t) {
                Log.e(TAG, "Init failed", t);
                fail(callback, "Model load failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void generate(String prompt, LlmCallback callback) {
        if (callback == null) return;
        if (!ready || inference == null) {
            mainHandler.post(() -> callback.onError("Model not ready"));
            return;
        }
        executor.execute(() -> {
            LlmInferenceSession session = null;
            try {
                synchronized (inferenceLock) {
                    if (!ready || inference == null) {
                        mainHandler.post(() -> callback.onError("Model not ready"));
                        return;
                    }
                    LlmInferenceSessionOptions sessionOptions =
                            LlmInferenceSessionOptions.builder()
                                    .setTopK(TOP_K)
                                    .setTopP(TOP_P)
                                    .setTemperature(TEMPERATURE)
                                    .setRandomSeed((int) (System.nanoTime() & 0x7fffffff))
                                    .build();
                    session = LlmInferenceSession.createFromOptions(inference, sessionOptions);
                    session.addQueryChunk(prompt);
                    String result = session.generateResponse();
                    final String reply = result == null ? "" : result;
                    mainHandler.post(() -> callback.onResult(reply));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Generate failed", t);
                final String err = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
                mainHandler.post(() -> callback.onError(err));
            } finally {
                if (session != null) {
                    try { session.close(); } catch (Throwable ignored) {}
                }
            }
        });
    }

    @Override
    public void close() {
        ready = false;
        executor.execute(() -> {
            synchronized (inferenceLock) {
                if (inference != null) {
                    try { inference.close(); } catch (Throwable ignored) {}
                    inference = null;
                }
            }
        });
        executor.shutdown();
    }

    private File resolveModelFile(Context ctx) {
        File extRoot = ctx.getExternalFilesDir(null);
        if (extRoot != null) {
            File a = new File(extRoot, MODEL_FILENAME);
            if (a.exists() && a.canRead()) return a;
            File b = new File(extRoot, "llm/" + MODEL_FILENAME);
            if (b.exists() && b.canRead()) return b;
        }
        File c = new File("/data/local/tmp/llm/" + MODEL_FILENAME);
        if (c.exists() && c.canRead()) return c;
        return null;
    }

    private void fail(InitCallback callback, String msg) {
        mainHandler.post(() -> {
            if (callback != null) callback.onError(msg);
        });
    }
}
