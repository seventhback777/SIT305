package com.example.whiskerguide.game.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.whiskerguide.game.model.MapData;
import com.example.whiskerguide.game.model.TileType;

public class GameView extends View {

    private MapData mapData;
    private int playerX = -1;
    private int playerY = -1;
    private boolean enemyVisible = false;

    private final Paint floorPaint = new Paint();
    private final Paint wallPaint = new Paint();
    private final Paint exitPaint = new Paint();
    private final Paint playerPaint = new Paint();
    private final Paint enemyPaint = new Paint();
    private final Paint gridPaint = new Paint();

    public GameView(Context context) { super(context); init(); }
    public GameView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }

    private void init() {
        floorPaint.setColor(Color.parseColor("#5C6F4A"));
        wallPaint.setColor(Color.parseColor("#2A2218"));
        exitPaint.setColor(Color.parseColor("#F2C34A"));
        playerPaint.setColor(Color.parseColor("#4FB3F2"));
        playerPaint.setAntiAlias(true);
        enemyPaint.setColor(Color.parseColor("#E04848"));
        enemyPaint.setAntiAlias(true);
        gridPaint.setColor(Color.parseColor("#1A1A1A"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);
    }

    public void setMap(MapData mapData) {
        this.mapData = mapData;
        invalidate();
    }

    public void setPlayerPosition(int x, int y) {
        this.playerX = x;
        this.playerY = y;
        invalidate();
    }

    public void setEnemyVisible(boolean visible) {
        this.enemyVisible = visible;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mapData == null) return;

        int cols = mapData.getWidth();
        int rows = mapData.getHeight();
        if (cols == 0 || rows == 0) return;

        int viewW = getWidth();
        int viewH = getHeight();
        float cellSize = Math.min(viewW / (float) cols, viewH / (float) rows);
        float boardW = cellSize * cols;
        float boardH = cellSize * rows;
        float offsetX = (viewW - boardW) / 2f;
        float offsetY = (viewH - boardH) / 2f;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                TileType t = mapData.getTile(x, y);
                Paint p;
                switch (t) {
                    case WALL: p = wallPaint; break;
                    case EXIT: p = exitPaint; break;
                    case FLOOR:
                    default:   p = floorPaint; break;
                }
                float left = offsetX + x * cellSize;
                float top = offsetY + y * cellSize;
                canvas.drawRect(left, top, left + cellSize, top + cellSize, p);
                canvas.drawRect(left, top, left + cellSize, top + cellSize, gridPaint);
            }
        }

        if (playerX >= 0 && playerY >= 0) {
            float cx = offsetX + playerX * cellSize + cellSize / 2f;
            float cy = offsetY + playerY * cellSize + cellSize / 2f;
            canvas.drawCircle(cx, cy, cellSize * 0.35f, playerPaint);

            if (enemyVisible) {
                float ex = cx + cellSize * 0.4f;
                float ey = cy - cellSize * 0.4f;
                canvas.drawCircle(ex, ey, cellSize * 0.25f, enemyPaint);
            }
        }
    }
}
