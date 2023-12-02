package com.jupiter.tusa.newmap.load.tiles;

public class TilesChunkCounter {
    private int index = 0;
    private final int amount;
    private final int key;
    private boolean isReady = false;

    public int getKey() {
        return key;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public int getIndex() {
        return index;
    }

    public TilesChunkCounter(int waitForTilesAmount, int key) {
        this.amount = waitForTilesAmount;
        this.key = key;
    }

    public synchronized void increaseLoaded() {
        index += 1;
        isReady = index >= amount;
    }
}
