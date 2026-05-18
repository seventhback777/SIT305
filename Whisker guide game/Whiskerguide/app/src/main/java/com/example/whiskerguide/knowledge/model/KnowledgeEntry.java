package com.example.whiskerguide.knowledge.model;

import java.util.List;

public class KnowledgeEntry {
    private String id;
    private String type;
    private List<String> keywords;
    private String content;

    public KnowledgeEntry() {}

    public KnowledgeEntry(String id, String type, List<String> keywords, String content) {
        this.id = id;
        this.type = type;
        this.keywords = keywords;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
