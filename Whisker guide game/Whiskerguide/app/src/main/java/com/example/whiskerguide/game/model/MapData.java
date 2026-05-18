package com.example.whiskerguide.game.model;

public class MapData {
    private int[][] grid;
    private int width;
    private int height;

    public MapData() {}

    public MapData(int[][] grid) {
        this.grid = grid;
        this.height = grid.length;
        this.width = grid.length > 0 ? grid[0].length : 0;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return TileType.WALL;
        return TileType.values()[grid[y][x]];
    }

    public boolean isWalkable(int x, int y) {
        TileType t = getTile(x, y);
        return t == TileType.FLOOR || t == TileType.EXIT;
    }

    public int[][] getGrid() { return grid; }
    public void setGrid(int[][] grid) {
        this.grid = grid;
        this.height = grid.length;
        this.width = grid.length > 0 ? grid[0].length : 0;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
