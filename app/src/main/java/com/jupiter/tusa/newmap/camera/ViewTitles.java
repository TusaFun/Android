package com.jupiter.tusa.newmap.camera;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ViewTitles {
    public int z;
    public int startX;
    public int endX;
    public int startY;
    public int endY;
    public int amount;
    public void markAsLoaded(int x, int y) {
        String key = "" + x + y;
        if(loaded.containsKey(key)) {
            Log.w("GL_ARTEM", "tile already loaded. ViewTiles class.");
            return;
        }
        loaded.put(key, true);
    }

    public boolean coverTile(int x, int y, int z) {
        return  false;
    }

    public boolean isAllLoaded() {
        return loaded.size() == amount;
    }

    private Map<String, Boolean> loaded = new HashMap<>();

    public ViewTitles(int startX, int endX, int startY, int endY, int z) {
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.z = z;
        amount = (endX - startX + 1) * (endY - startY + 1);
    }
}
