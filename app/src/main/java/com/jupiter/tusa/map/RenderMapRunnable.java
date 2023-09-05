package com.jupiter.tusa.map;


public class RenderMapRunnable implements Runnable {
    private MyGlSurfaceView myGlSurfaceView;
    private RenderTileInitiator initiator;

    public RenderMapRunnable(MyGlSurfaceView myGlSurfaceView,
                             RenderTileInitiator initiator
    ) {
        this.myGlSurfaceView = myGlSurfaceView;
        this.initiator = initiator;
    }

    @Override
    public void run() {
        int[][][] viewTiles = myGlSurfaceView.calcViewTiles();
        if(initiator == RenderTileInitiator.MOVE) {
            myGlSurfaceView.optimizeTilesForStaticZ(viewTiles);
        }
        myGlSurfaceView.renderMap(viewTiles, initiator);
    }
}
