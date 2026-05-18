package com.example.whiskerguide.common.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.whiskerguide.common.model.GameState;

public class GameStateHolder {

    private static volatile GameStateHolder instance;

    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();

    private GameStateHolder() {}

    public static GameStateHolder getInstance() {
        if (instance == null) {
            synchronized (GameStateHolder.class) {
                if (instance == null) {
                    instance = new GameStateHolder();
                }
            }
        }
        return instance;
    }

    public LiveData<GameState> getGameState() {
        return gameState;
    }

    public GameState getCurrentSnapshot() {
        return gameState.getValue();
    }

    public void update(GameState newState) {
        gameState.postValue(newState);
    }
}
