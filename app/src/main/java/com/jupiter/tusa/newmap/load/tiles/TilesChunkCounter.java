package com.jupiter.tusa.newmap.load.tiles;

import com.jupiter.tusa.newmap.mvt.MvtObjectStyled;

import java.util.ArrayList;
import java.util.List;

public class TilesChunkCounter {
    private List<MvtObjectStyled> styledList = new ArrayList<>();
    private int index = 0;
    private final int amount;
    private final int key;
    private boolean isReady = false;

    public int getKey() {
        return key;
    }

    public List<MvtObjectStyled> getStyledList() {
        return styledList;
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

    public synchronized void increaseLoaded(List<MvtObjectStyled> styledList) {
        index += 1;
        isReady = index >= amount;
        this.styledList.addAll(styledList);
    }
}
