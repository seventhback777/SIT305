package com.example.whiskerguide.common.model;

import java.util.Collections;
import java.util.List;

public final class GameState {
    private final int playerHealth;
    private final int playerMaxHealth;
    private final int playerMana;
    private final int playerMaxMana;
    private final int playerAttack;
    private final List<String> inventoryItems;
    private final List<String> availableSkills;
    private final String currentLocation;
    private final boolean inCombat;
    private final String currentEnemyName;
    private final int currentEnemyHealth;
    private final String lastEvent;

    public GameState(int playerHealth, int playerMaxHealth,
                     int playerMana, int playerMaxMana,
                     int playerAttack,
                     List<String> inventoryItems,
                     List<String> availableSkills,
                     String currentLocation,
                     boolean inCombat,
                     String currentEnemyName,
                     int currentEnemyHealth,
                     String lastEvent) {
        this.playerHealth = playerHealth;
        this.playerMaxHealth = playerMaxHealth;
        this.playerMana = playerMana;
        this.playerMaxMana = playerMaxMana;
        this.playerAttack = playerAttack;
        this.inventoryItems = inventoryItems == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(inventoryItems);
        this.availableSkills = availableSkills == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(availableSkills);
        this.currentLocation = currentLocation;
        this.inCombat = inCombat;
        this.currentEnemyName = currentEnemyName;
        this.currentEnemyHealth = currentEnemyHealth;
        this.lastEvent = lastEvent;
    }

    public int getPlayerHealth() { return playerHealth; }
    public int getPlayerMaxHealth() { return playerMaxHealth; }
    public int getPlayerMana() { return playerMana; }
    public int getPlayerMaxMana() { return playerMaxMana; }
    public int getPlayerAttack() { return playerAttack; }
    public List<String> getInventoryItems() { return inventoryItems; }
    public List<String> getAvailableSkills() { return availableSkills; }
    public String getCurrentLocation() { return currentLocation; }
    public boolean isInCombat() { return inCombat; }
    public String getCurrentEnemyName() { return currentEnemyName; }
    public int getCurrentEnemyHealth() { return currentEnemyHealth; }
    public String getLastEvent() { return lastEvent; }
}
