package com.example.whiskerguide.cat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.whiskerguide.WhiskerGuideApp;
import com.example.whiskerguide.cat.engine.ContextBuilder;
import com.example.whiskerguide.cat.engine.LlmCallback;
import com.example.whiskerguide.cat.engine.LlmEngine;
import com.example.whiskerguide.cat.engine.ResponseFilter;
import com.example.whiskerguide.cat.model.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CatViewModel extends AndroidViewModel {

    private final WhiskerGuideApp app;
    private final ContextBuilder contextBuilder;
    private final ResponseFilter responseFilter;

    private final List<ChatMessage> history = new ArrayList<>();
    private final MutableLiveData<List<ChatMessage>> messagesLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> thinkingLive = new MutableLiveData<>(false);

    // 不安全请求前置黑名单。命中任一即不走 LLM,直接给出 refusal。
    // 覆盖:暴力/伤害、自我伤害、武器/违法、个人信息索取、典型越狱模式、不当内容。
    // 避免误伤游戏词:不阻挡单独的 "kill" / "weapon"(玩家可能问 "kill the goblin")。
    private static final List<String> UNSAFE_PATTERNS = Arrays.asList(
            // self-harm / harm to people
            "kill myself", "kill yourself", "kill a person", "kill people",
            "kill someone", "suicide", "self-harm", "self harm", "hurt myself",
            "hurt someone", "end my life", "commit suicide",
            // weapons / explosives
            "make a bomb", "build a bomb", "make bomb", "build bomb",
            "homemade weapon", "build a weapon", "buy a gun", "build a gun",
            // illegal
            "hack into", "illegal drug", "make drugs", "make meth",
            "buy drugs", "sell drugs", "ransomware",
            // personal info exfiltration
            "my password", "my credit card", "credit card number",
            "my address", "my phone number", "social security",
            // prompt injection / jailbreak
            "ignore previous", "ignore your instructions", "ignore the system",
            "forget your instructions", "pretend you are", "you are now",
            "jailbreak", "dan mode", "act as dan",
            "system prompt", "reveal your prompt",
            // inappropriate content
            "nsfw", "sexual", "porn", "racist", "nazi"
    );

    private static final String SAFETY_REFUSAL =
            "Meow~ I can't help with that. I'm only your in-game guide "
                    + "(Goblins, Fireball, items, the exit). "
                    + "For sensitive topics please reach out to a qualified human. "
                    + "Let's get back to the adventure!";

    public CatViewModel(@NonNull Application application) {
        super(application);
        this.app = (WhiskerGuideApp) application;
        this.contextBuilder = app.getContextBuilder();
        this.responseFilter = app.getResponseFilter();

        history.add(new ChatMessage(
                "Meow~ I'm WhiskerGuide, ask me anything about the game!\n"
                        + "🔒 Privacy: I run entirely on your device. Nothing you type ever leaves your phone.",
                true));
        messagesLive.setValue(new ArrayList<>(history));
    }

    public LiveData<List<ChatMessage>> getMessages() { return messagesLive; }
    public LiveData<Boolean> getThinking() { return thinkingLive; }

    private boolean isUnsafeRequest(String question) {
        if (question == null) return false;
        String lower = question.toLowerCase(Locale.ROOT);
        for (String pattern : UNSAFE_PATTERNS) {
            if (lower.contains(pattern)) return true;
        }
        return false;
    }

    public void ask(String question) {
        if (question == null) return;
        final String q = question.trim();
        if (q.isEmpty()) return;
        if (Boolean.TRUE.equals(thinkingLive.getValue())) return;

        history.add(new ChatMessage(q, false));
        messagesLive.setValue(new ArrayList<>(history));

        // 安全前置拦截:不安全请求不送到 LLM,直接返回 refusal。
        if (isUnsafeRequest(q)) {
            history.add(new ChatMessage(SAFETY_REFUSAL, true));
            messagesLive.setValue(new ArrayList<>(history));
            return;
        }

        thinkingLive.setValue(true);

        String prompt = contextBuilder.build(q);
        LlmEngine engine = app.getLlmEngine();

        engine.generate(prompt, new LlmCallback() {
            @Override
            public void onResult(String result) {
                String filtered = responseFilter.filter(result);
                history.add(new ChatMessage(filtered, true));
                messagesLive.postValue(new ArrayList<>(history));
                thinkingLive.postValue(false);
            }

            @Override
            public void onError(String error) {
                history.add(new ChatMessage("(error: " + error + ")", true));
                messagesLive.postValue(new ArrayList<>(history));
                thinkingLive.postValue(false);
            }
        });
    }
}
