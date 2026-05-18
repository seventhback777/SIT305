package com.example.whiskerguide;

import android.app.Application;

import com.example.whiskerguide.cat.engine.ContextBuilder;
import com.example.whiskerguide.cat.engine.InitCallback;
import com.example.whiskerguide.cat.engine.LlmEngine;
import com.example.whiskerguide.cat.engine.MediaPipeLlmEngine;
import com.example.whiskerguide.cat.engine.MockLlmEngine;
import com.example.whiskerguide.cat.engine.ResponseFilter;
import com.example.whiskerguide.common.repository.GameStateHolder;
import com.example.whiskerguide.knowledge.repository.JsonKnowledgeRepository;
import com.example.whiskerguide.knowledge.repository.KnowledgeRepository;

public class WhiskerGuideApp extends Application {

    private static WhiskerGuideApp instance;

    private KnowledgeRepository knowledgeRepository;
    private ContextBuilder contextBuilder;
    private ResponseFilter responseFilter;

    private MockLlmEngine mockEngine;
    private MediaPipeLlmEngine mediaPipeEngine;
    private volatile boolean mediaPipeReady = false;
    // 标记初始化是否已尝试完成(无论成功或失败)。用于 UI 区分"还在加载"和"已失败"。
    private volatile boolean mediaPipeInitFinished = false;
    private volatile String mediaPipeInitError = null;

    // 默认使用 MediaPipe(英文场景下 Gemma 1B 质量足够),
    // ResponseFilter 做兜底校验。可通过 setPreferMock(true) 强制 Mock。
    private volatile boolean preferMock = false;

    public static WhiskerGuideApp get() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        knowledgeRepository = new JsonKnowledgeRepository(this);
        knowledgeRepository.load();

        contextBuilder = new ContextBuilder(
                GameStateHolder.getInstance(), knowledgeRepository);
        responseFilter = new ResponseFilter();

        mockEngine = new MockLlmEngine();
        mediaPipeEngine = new MediaPipeLlmEngine();
    }

    public void tryInitMediaPipe(InitCallback callback) {
        mediaPipeEngine.initialize(this, new InitCallback() {
            @Override
            public void onSuccess() {
                mediaPipeReady = true;
                mediaPipeInitFinished = true;
                mediaPipeInitError = null;
                if (callback != null) callback.onSuccess();
            }
            @Override
            public void onError(String error) {
                mediaPipeReady = false;
                mediaPipeInitFinished = true;
                mediaPipeInitError = error;
                // 加载失败时强制 Mock,防止 getLlmEngine 持续返回 Mock 但 UI 仍说 "AI"
                preferMock = true;
                if (callback != null) callback.onError(error);
            }
        });
    }

    public LlmEngine getLlmEngine() {
        if (preferMock || !mediaPipeReady) return mockEngine;
        return mediaPipeEngine;
    }

    public boolean isUsingMediaPipe() { return mediaPipeReady && !preferMock; }

    public boolean isMediaPipeReady() { return mediaPipeReady; }

    public boolean isMediaPipeInitFinished() { return mediaPipeInitFinished; }

    public String getMediaPipeInitError() { return mediaPipeInitError; }

    public void setPreferMock(boolean prefer) { this.preferMock = prefer; }

    public boolean isPreferMock() { return preferMock; }

    public KnowledgeRepository getKnowledgeRepository() { return knowledgeRepository; }
    public ContextBuilder getContextBuilder() { return contextBuilder; }
    public ResponseFilter getResponseFilter() { return responseFilter; }

    @Override
    public void onTerminate() {
        if (mockEngine != null) mockEngine.close();
        if (mediaPipeEngine != null) mediaPipeEngine.close();
        super.onTerminate();
    }
}
