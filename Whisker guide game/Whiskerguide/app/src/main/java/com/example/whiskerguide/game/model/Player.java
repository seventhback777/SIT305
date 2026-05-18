package com.example.whiskerguide.game.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int health;
    private int maxHealth;
    private int mana;
    private int maxMana;
    private int attackDamage;
    private List<Item> inventory;
    private List<Skill> skills;
    private int x;
    private int y;

    public Player() {
        this.inventory = new ArrayList<>();
        this.skills = new ArrayList<>();
    }

    public Player(int maxHealth, int maxMana, int attackDamage, int x, int y) {
        this();
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.maxMana = maxMana;
        this.mana = maxMana;
        this.attackDamage = attackDamage;
        this.x = x;
        this.y = y;
    }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }

    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }

    public int getAttackDamage() { return attackDamage; }
    public void setAttackDamage(int attackDamage) { this.attackDamage = attackDamage; }

    public List<Item> getInventory() { return inventory; }
    public void setInventory(List<Item> inventory) { this.inventory = inventory; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
}
