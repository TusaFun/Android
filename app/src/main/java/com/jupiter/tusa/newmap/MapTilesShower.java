package com.jupiter.tusa.newmap;

import android.util.Log;

import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.camera.MapWorldCamera;
import com.jupiter.tusa.newmap.camera.ViewTitles;
import com.jupiter.tusa.newmap.draw.MapStyle;
import com.jupiter.tusa.newmap.draw.MvtToDrawObjectPipeRunnable;
import com.jupiter.tusa.newmap.draw.MvtToDrawPipeOutput;
import com.jupiter.tusa.newmap.gl.TileDataForPrograms;
import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicProgram;
import com.jupiter.tusa.newmap.load.tiles.TilesChunkCounter;
import com.jupiter.tusa.newmap.load.tiles.MvtApiResource;
import com.jupiter.tusa.newmap.event.MapSignatureEvent;
import com.jupiter.tusa.newmap.event.TilePipelineReadyEvent;
import com.jupiter.tusa.newmap.mvt.MvtObjectStyled;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vector_tile.VectorTile;

public class MapTilesShower {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    List<Future> futuresList = new ArrayList<>();
    CacheBytes cacheBytes;
    private final Object lockObject = new Object();
    private final MapStyle mapStyle;
    private final MapSurfaceView mapSurfaceView;
    private final boolean showTestTile = false;
    private final MapSignatureEvent<MvtToDrawPipeOutput> onTilePipelineReady;
    private int state = 0;
    private int theMostFreshState = state;

    public MapTilesShower(
            MapSurfaceView mapSurfaceView
    ) {
        this.mapSurfaceView = mapSurfaceView;
        this.cacheBytes = new CacheBytes(mapSurfaceView.getCacheStorage());

        mapStyle = mapSurfaceView.getMapStyle();
        onTilePipelineReady = new TilePipelineReadyEvent(mapSurfaceView);
    }

    public void showTile(MvtToDrawPipeOutput pipeOutput) {
        synchronized (lockObject) {
            TileDataForPrograms tileDataForPrograms = pipeOutput.getTileDataForPrograms();
            TilesChunkCounter chunkCounter = pipeOutput.getTilesChunkCounter();
            FDOFloatBasicProgram fdoFloatBasicProgram = mapSurfaceView
                    .getMapRenderer()
                    .getDrawPrograms()
                    .getFdoFloatBasicProgram();

            if(theMostFreshState > chunkCounter.getKey()) {
                return;
            }

            List<MvtObjectStyled> styled = tileDataForPrograms.getMvtObjectStyled();
            fdoFloatBasicProgram.addDataForDrawing(styled, -1);
            if(chunkCounter.getIsReady()) {
                theMostFreshState = chunkCounter.getKey();
                fdoFloatBasicProgram.addDataForDrawing(styled, chunkCounter.getKey());
                //Log.d("GL_ARTEM", "Clear map state lesser than " + chunkCounter.getKey());
            }
        }
    }

    public void nextMapState() {
        state++;
        Iterator<Future> futureIterator = futuresList.listIterator();
        while(futureIterator.hasNext()) {
            if(futureIterator.next().isDone()) {
                futureIterator.remove();
            }
        }

        if(futuresList.size() > 4) {
            futureIterator = futuresList.listIterator();
            while(futureIterator.hasNext()) {
                if(futureIterator.next().cancel(true)) {
                    futureIterator.remove();
                }
            }
        }


        Log.d("GL_ARTEM", "Next map state " + state);
        int tileZ = mapSurfaceView.getTileWorldCoordinates().getCurrentZ();
        MapWorldCamera camera = mapSurfaceView.getMapRenderer().getMapWorldCamera();
        ViewTitles viewTitles = camera.getViewTiles();
        //Log.d("GL_ARTEM", "AMOUNT = " + viewTitles.amount);
        TilesChunkCounter loadCounter = new TilesChunkCounter(viewTitles.amount, state);
        if(showTestTile) {
            nextTile(2, 1, 1, loadCounter);
            nextTile(2, 0, 1, loadCounter);
            return;
        }

        int startXTile = viewTitles.startX;
        int endXTile = viewTitles.endX;
        int startYTile = viewTitles.startY;
        int endYTile = viewTitles.endY;

        for(int x = startXTile; x <= endXTile; x++) {
            for(int y = startYTile; y <= endYTile; y++) {
                nextTile(tileZ, x, y, loadCounter);
            }
        }
    }

    private void nextTile(int zoom, int x, int y, TilesChunkCounter loadCounter) {
        MvtApiResource mvtApiResource = new MvtApiResource(zoom, x, y);
        Future<?> future = executor.submit(new MvtToDrawObjectPipeRunnable(
                cacheBytes,
                mvtApiResource,
                mapStyle,
                loadCounter,
                onTilePipelineReady
        ));
        futuresList.add(future);
    }
}
