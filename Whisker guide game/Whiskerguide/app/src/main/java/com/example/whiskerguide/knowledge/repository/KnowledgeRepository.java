package com.example.whiskerguide.knowledge.repository;

import com.example.whiskerguide.knowledge.model.KnowledgeEntry;

import java.util.List;

public interface KnowledgeRepository {
    void load();
    List<KnowledgeEntry> getAll();
    List<KnowledgeEntry> query(List<String> keywords);
    List<KnowledgeEntry> queryByType(String type);
}
