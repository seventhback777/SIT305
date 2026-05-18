package com.example.whiskerguide.knowledge.repository;

import android.content.Context;
import android.util.Log;

import com.example.whiskerguide.knowledge.model.KnowledgeEntry;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JsonKnowledgeRepository implements KnowledgeRepository {

    private static final String TAG = "JsonKnowledgeRepo";
    private static final String[] FILES = {
            "knowledge/enemies.json",
            "knowledge/items.json",
            "knowledge/skills.json",
            "knowledge/quests.json",
            "knowledge/mechanics.json"
    };

    private final Context appContext;
    private final List<KnowledgeEntry> allEntries = new ArrayList<>();
    private boolean loaded = false;

    public JsonKnowledgeRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void load() {
        if (loaded) return;
        Gson gson = new Gson();
        for (String path : FILES) {
            try (InputStream is = appContext.getAssets().open(path);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                EntriesWrapper wrapper = gson.fromJson(reader, EntriesWrapper.class);
                if (wrapper != null && wrapper.entries != null) {
                    allEntries.addAll(wrapper.entries);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to load " + path, e);
            }
        }
        loaded = true;
        Log.i(TAG, "Loaded " + allEntries.size() + " knowledge entries");
    }

    @Override
    public List<KnowledgeEntry> getAll() {
        return new ArrayList<>(allEntries);
    }

    @Override
    public List<KnowledgeEntry> query(List<String> keywords) {
        List<KnowledgeEntry> result = new ArrayList<>();
        if (keywords == null || keywords.isEmpty()) return result;

        List<String> lowered = new ArrayList<>();
        for (String k : keywords) {
            if (k != null && !k.isEmpty()) lowered.add(k.toLowerCase(Locale.ROOT));
        }

        for (KnowledgeEntry entry : allEntries) {
            if (matches(entry, lowered)) result.add(entry);
        }
        return result;
    }

    @Override
    public List<KnowledgeEntry> queryByType(String type) {
        List<KnowledgeEntry> result = new ArrayList<>();
        if (type == null) return result;
        for (KnowledgeEntry entry : allEntries) {
            if (type.equalsIgnoreCase(entry.getType())) result.add(entry);
        }
        return result;
    }

    private boolean matches(KnowledgeEntry entry, List<String> lowerKeywords) {
        if (entry.getKeywords() == null) return false;
        for (String entryKw : entry.getKeywords()) {
            String ek = entryKw.toLowerCase(Locale.ROOT);
            for (String userKw : lowerKeywords) {
                if (ek.contains(userKw) || userKw.contains(ek)) return true;
            }
        }
        return false;
    }

    private static class EntriesWrapper {
        List<KnowledgeEntry> entries;
    }
}
