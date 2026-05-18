package com.example.whiskerguide.game.model;

public class Item {
    private String id;
    private String name;
    private ItemType type;
    private int value;

    public Item() {}

    public Item(String id, String name, ItemType type, int value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
