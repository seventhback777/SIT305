package com.example.whiskerguide.game.model;

public class Skill {
    private String id;
    private String name;
    private int manaCost;
    private int value;
    private SkillType type;
    private int cooldownTurns;
    private int currentCooldown;
    private String description;

    public Skill() {}

    public Skill(String id, String name, int manaCost, int value,
                 SkillType type, int cooldownTurns, String description) {
        this.id = id;
        this.name = name;
        this.manaCost = manaCost;
        this.value = value;
        this.type = type;
        this.cooldownTurns = cooldownTurns;
        this.currentCooldown = 0;
        this.description = description;
    }

    public boolean isReady() {
        return currentCooldown <= 0;
    }

    public void triggerCooldown() {
        currentCooldown = cooldownTurns;
    }

    public void tickCooldown() {
        if (currentCooldown > 0) currentCooldown--;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getManaCost() { return manaCost; }
    public void setManaCost(int manaCost) { this.manaCost = manaCost; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public SkillType getType() { return type; }
    public void setType(SkillType type) { this.type = type; }

    public int getCooldownTurns() { return cooldownTurns; }
    public void setCooldownTurns(int cooldownTurns) { this.cooldownTurns = cooldownTurns; }

    public int getCurrentCooldown() { return currentCooldown; }
    public void setCurrentCooldown(int currentCooldown) { this.currentCooldown = currentCooldown; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
