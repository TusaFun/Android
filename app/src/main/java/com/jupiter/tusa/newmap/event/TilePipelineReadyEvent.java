package com.jupiter.tusa.newmap.event;

import com.jupiter.tusa.newmap.MapSurfaceView;
import com.jupiter.tusa.newmap.MapTilesShower;
import com.jupiter.tusa.newmap.draw.MvtToDrawPipeOutput;
import com.jupiter.tusa.newmap.gl.TileDataForPrograms;
import com.jupiter.tusa.newmap.load.tiles.TilesChunkCounter;

public class TilePipelineReadyEvent implements MapSignatureEvent<MvtToDrawPipeOutput> {
    private final MapSurfaceView mapSurfaceView;
    public TilePipelineReadyEvent(MapSurfaceView mapSurfaceView) {
        this.mapSurfaceView = mapSurfaceView;
    }

    @Override
    public void handle(MvtToDrawPipeOutput input) {
        MapTilesShower shower = mapSurfaceView.getMapTilesShower();
        shower.showTile(input);

        mapSurfaceView.requestRender();
    }
}
