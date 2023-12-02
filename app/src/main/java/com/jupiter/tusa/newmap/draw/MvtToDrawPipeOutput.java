package com.jupiter.tusa.newmap.draw;

import com.jupiter.tusa.newmap.gl.TileDataForPrograms;
import com.jupiter.tusa.newmap.load.tiles.TilesChunkCounter;

public class MvtToDrawPipeOutput {
    private TilesChunkCounter tilesChunkCounter;
    private TileDataForPrograms tileDataForPrograms;
    public TilesChunkCounter getTilesChunkCounter() {
        return tilesChunkCounter;
    }
    public TileDataForPrograms getTileDataForPrograms() {
        return tileDataForPrograms;
    }
    public MvtToDrawPipeOutput(TilesChunkCounter tilesChunkCounter, TileDataForPrograms tileDataForPrograms) {
        this.tileDataForPrograms = tileDataForPrograms;
        this.tilesChunkCounter = tilesChunkCounter;
    }
}
