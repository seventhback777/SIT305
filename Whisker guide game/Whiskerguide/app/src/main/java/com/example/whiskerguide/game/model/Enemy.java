package com.example.whiskerguide.game.model;

public class Enemy {
    private String id;
    private String name;
    private int health;
    private int maxHealth;
    private int attackDamage;

    public Enemy() {}

    public Enemy(String id, String name, int maxHealth, int attackDamage) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attackDamage = attackDamage;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getAttackDamage() { return attackDamage; }
    public void setAttackDamage(int attackDamage) { this.attackDamage = attackDamage; }
}
