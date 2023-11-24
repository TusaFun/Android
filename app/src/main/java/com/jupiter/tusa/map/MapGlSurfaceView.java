package com.jupiter.tusa.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.cache.OnImageReady;
import com.jupiter.tusa.cache.PrepareImageRunnable;
import com.jupiter.tusa.map.figures.TuserMarker;
import com.jupiter.tusa.map.figures.Sprite;
import com.jupiter.tusa.newmap.MapScaleListener;
import com.jupiter.tusa.utils.MathUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class MapGlSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private MainActivity mainActivity;
    private int executorPoolsAmount = 2;
    private ExecutorService executorService = Executors.newFixedThreadPool(executorPoolsAmount);

    // Маштабирование карты
    private GestureDetector gestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;
    private int mapZ = 0;
    private int maxMapZ = 18;

    // Отрисовка карты
    final int tilesAmount = 80;
    private final float maxBottomRightZoneWorldLength = (float) Math.pow(2, maxMapZ);
    private float useTileSize = determineTileSize();

    // Маркера
    TuserMarker testMarker;

    // Перемещение карты
    private float previousX = 0f;
    private float previousY = 0f;
    private int currentTileX = 0;
    private int currentTileY = 0;
    private int previousCurrentTileX = currentTileX;
    private int previousCurrentTileY = currentTileY;
    private float currentMapX = 0;
    private float currentMapY = 0;
    private float moveStrong = 100f;
    private float maxScaleFactor = useTileSize;
    private Future<?> renderMapFuture;

    // регрессии
    private double mapZRegressionParamOne = 18.86230239;
    private double mapZRegressionParamTwo = 1.44971501;

    private final Tile[] renderedTiles = new Tile[tilesAmount];

    // animations
    private final List<Tile> fadeOutTiles = new ArrayList<Tile>();
    private final ReentrantLock fadeOutTilesLock = new ReentrantLock();
    private final List<TuserMarker> animatedMarkersRadius = new ArrayList<TuserMarker>();
    private final ReentrantLock animatedMarkersRadiusLock = new ReentrantLock();
    private final List<TuserMarker> animatedMarkersMove = new ArrayList<TuserMarker>();
    private final ReentrantLock animatedMarkersMoveLock = new ReentrantLock();
    public float getCurrentMapX() {
        return currentMapX;
    }

    public float getCurrentMapY() {
        return currentMapY;
    }

    public float getScaleFactor() {
        return 1;
    }

    // Применение тайлов после загрузки
    private OnPrepareSprite onSpriteReady = new OnPrepareSprite() {
        @Override
        public void ready(Bitmap bitmap, float[] vertexLocations, int useIndex, RenderTileInitiator initiator, Tile tile) {
            if(tile.isCancelled())
                return;

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if(initiator == RenderTileInitiator.ZOOM_DOWN) {
                        renderTile(tile);
                    }
                    if(initiator == RenderTileInitiator.CHANGE_SURFACE) {
                        renderTile(tile);
                    } else if(initiator == RenderTileInitiator.MOVE) {
                        renderTile(tile);
                    } else if(initiator == RenderTileInitiator.ZOOM_UP) {
                        renderTile(tile);
                    }
                }
            });
            requestRender();
        }
    };

    private void clearTile(int index) {
        Tile tile = renderedTiles[index];
        if(tile != null) {
            tile.cancelLoadTile();
            renderedTiles[index] = null;
            renderer.removeSprite(tile.getSprite());
        }
    }

    private void renderTile(Tile tile) {
        Sprite sprite = new Sprite(
                mainActivity,
                tile.getBitmap(),
                tile.getVertexLocations()
        );
        tile.setSprite(sprite);
        fadeOutTilesLock.lock();
        fadeOutTiles.add(tile);
        fadeOutTilesLock.unlock();
        renderer.renderSprite(sprite);
    }

    public MapGlSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mainActivity = (MainActivity) context;
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context, this);
        setRenderer(renderer);

        //mapScaleListener = new MapScaleListener(maxScaleFactor, 1, maxScaleFactor);
        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                return true;
            }
        });

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        initCameraZ(1);
        initCameraLatLng(55.7558f, 37.6173f);

        // Анимации
        Handler animationHandler = new Handler();
        long delayMillis = 20;
        animationHandler.post(new Runnable() {
            @Override
            public void run() {

                // Sprites fade out animation
                fadeOutTilesLock.lock();
                Iterator<Tile> iterator = fadeOutTiles.iterator();
                while(iterator.hasNext()) {
                    Tile fadeOutTile = iterator.next();
                    Sprite sprite = fadeOutTile.getSprite();
                    if(sprite == null)
                        continue;
                    float currentAlpha = sprite.getAlpha();
                    if(currentAlpha >= 1) {
                        iterator.remove();
                        continue;
                    }
                    float deltaAlpha = (float) delayMillis / 1000;
                    sprite.setAlpha(currentAlpha + deltaAlpha);
                }
                fadeOutTilesLock.unlock();

                animatedMarkersRadiusLock.lock();
                Iterator<TuserMarker> iteratorMarkers = animatedMarkersRadius.iterator();
                float scaleRadiusMarkerSpeed = (float) getRadiusOfMarkerForCurrentMap() / 20;
                while(iteratorMarkers.hasNext()) {
                    TuserMarker tuserMarker = iteratorMarkers.next();
                    float currentRadius = tuserMarker.getRadius();
                    float animatedRadius = tuserMarker.getAnimateToRadius();
                    if(currentRadius > animatedRadius) {
                        currentRadius -= scaleRadiusMarkerSpeed;
                    } else {
                        currentRadius += scaleRadiusMarkerSpeed;
                    }

                    //Log.d("ANIMATE_MARKERS", String.format("Current radius %f to %f", currentRadius, animatedRadius));

                    tuserMarker.changeGeometry(tuserMarker.getSegments(), tuserMarker.getCenterX(), tuserMarker.getCenterY(), currentRadius);

                    if(Math.abs(currentRadius - animatedRadius) <= scaleRadiusMarkerSpeed) {
                        iteratorMarkers.remove();
                    }
                }
                animatedMarkersRadiusLock.unlock();

                animatedMarkersMoveLock.lock();
                Iterator<TuserMarker> iteratorMarkersMove = animatedMarkersMove.iterator();
                float moveMarkerSpeed = useTileSize / 100;
                while(iteratorMarkersMove.hasNext()) {
                    TuserMarker marker = iteratorMarkersMove.next();
                    float currentX = marker.getCenterX();
                    float currentY = marker.getCenterY();
                    float animateToX = marker.getAnimateToCenterX();
                    float absX = Math.abs(currentX - animateToX);
                    if(absX > moveMarkerSpeed) {
                        if(currentX > marker.getAnimateToCenterX()) {
                            currentX -= moveMarkerSpeed;
                        } else {
                            currentX += moveMarkerSpeed;
                        }
                    }

                    if(Math.abs(currentY - marker.getAnimateToCenterY()) > moveMarkerSpeed) {
                        if(currentY > marker.getAnimateToCenterY()) {
                            currentY -= moveMarkerSpeed;
                        } else {
                            currentY += moveMarkerSpeed;
                        }
                    }
                    marker.changeGeometry(marker.getSegments(), currentX, currentY, marker.getRadius());

                    if(Math.abs(currentX - marker.getAnimateToCenterX()) <= moveMarkerSpeed && Math.abs(currentY - marker.getAnimateToCenterY()) <= moveMarkerSpeed) {
                        iteratorMarkersMove.remove();
                    }
                }
                animatedMarkersMoveLock.unlock();

                // Если что-то обновилось то заставляем рендер перерендрить картинку
                boolean anythingIsAnimated = !fadeOutTiles.isEmpty() ||
                        !animatedMarkersRadius.isEmpty() || !animatedMarkersMove.isEmpty();
                if(anythingIsAnimated) {
                    requestRender();
                }

                animationHandler.postDelayed(this, delayMillis);
            }
        });
    }

    public void surfaceCreated() {
        addMarker(55.7558f, 37.6173f);
    }

    public void addMarker(float lat, float lon) {
        float[] xAndY = latLngToXAndY(lat, lon);
        PrepareImageRunnable prepareImageRunnable = new PrepareImageRunnable(
                mainActivity.getCacheStorage(),
                new OnImageReady() {
                    @Override
                    public void received(Bitmap image) {
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                testMarker = new TuserMarker(mainActivity, image, 60, xAndY[0], xAndY[1], maxBottomRightZoneWorldLength / 50);
                                renderer.renderCircle(testMarker);
                            }
                        });
                        requestRender();
                    }
                },
                "testmarker1",
                "https://sun9-80.userapi.com/impg/pW2fP0QmjMGNLcOybHo1YrLDbYTVUktiEglcGA/jz9bPy-yo7U.jpg?size=1024x1080&quality=95&sign=19ac7ac8920eb60dd627eb485905e967&type=album"
        );
        executorService.submit(prepareImageRunnable);
    }

    public void renderMap(int[][][] viewTiles, RenderTileInitiator initiator) {
        Log.d("GL_ARTEM", "Render map");

        // Отрендрить тайлы которые должны быть видимы
        int rows = viewTiles.length;
        int cols = viewTiles[0].length;

        int centerRow = rows / 2;
        int centerCol = cols / 2;

        // рендрим от центра к краям. Чтобы пользователь начинал видеть с центра
        for (int distance = 0; distance <= Math.max(rows, cols) / 2; distance++) {
            for (int row = centerRow - distance; row <= centerRow + distance; row++) {
                for (int col = centerCol - distance; col <= centerCol + distance; col++) {

                    // Проверяем, что индексы находятся в пределах массива
                    if (row >= 0 && row < rows && col >= 0 && col < cols) {
                        int freeIndex = -1;
                        int[] tileCoordinates = viewTiles[row][col];
                        int tileX = tileCoordinates[0];
                        int tileY = tileCoordinates[1];
                        int tileZ = tileCoordinates[2];

                        // ищем этот тайл в отрендренных
                        boolean exist = false;
                        for(int i = 0; i < renderedTiles.length; i++) {
                            Tile tile = renderedTiles[i];
                            if(tile == null) {
                                freeIndex = i;
                                continue;
                            }

                            if(tile.getX() == tileX && tile.getY() == tileY && tile.getZ() == tileZ) {
                                exist = true;
                                break;
                            }
                        }

                        // тайл уже на карте и уже существует
                        // переходим к следующему видимому тайлу
                        if(exist)
                            continue;

                        if(freeIndex == -1) {
                            Log.w("GL_ARTEM", "Map was optimized in render map! It is bad :/");
                            continue;
                        }

                        Tile tile = new Tile(mainActivity, onSpriteReady, executorService, renderer, tileX, tileY,
                                mapZ, useTileSize, freeIndex, initiator, renderedTiles, viewTiles);
                        tile.render();
                    }
                }
            }
        }
    }

    public float determineTileSize() {
        return (float) Math.pow(2, maxMapZ - mapZ);
    }

    public void optimizeTilesForStaticZ(int[][][] viewTiles) {
        // Выгрузить тайлы которые вне зоны видимости
        for(int i = 0; i < renderedTiles.length; i++) {
            Tile renderedTile = renderedTiles[i];
            if(renderedTile == null)
                continue;
            int[] leftTop = viewTiles[0][0];
            int[] rightBottom = viewTiles[viewTiles.length - 1][viewTiles[0].length - 1];
            boolean xIsNotInBounds = renderedTile.getX() > rightBottom[0] || renderedTile.getX() < leftTop[0];
            boolean yIsNotInBounds = renderedTile.getY() > rightBottom[1] || renderedTile.getY() < leftTop[1];
            boolean zIsNotInBounds = renderedTile.getZ() != mapZ;
            if(xIsNotInBounds || yIsNotInBounds || zIsNotInBounds) {
                clearTile(i);
            }
        }
    }

    // тут определяется видимая область
    // отдается массив где первый элемент(первая размерность трехмерного массива) x(ширина) второй y(высота),
    // а в третьем храняться координаты тайла x y
    // используются координаты тайлан а третьем элементе
    public int[][][] calcViewTiles() {
        // Текущая видимая область в мировых координатах
        float[] leftTopWorld = renderer.screenCoordinatesToWorldLocation(0f, 0f);
        float[] bottomRightWorld = renderer.screenCoordinatesToWorldLocation(renderer.getWidth(), renderer.getHeight());

        int maxTilesCountXOrY = (int) Math.pow(2, mapZ);
        // -1 так как без этого получается максимальное количество тайлов
        // нам же нужна максимальная координата тайла Координаты тайлов начинаются с нуля
        int maxTileXOrY = maxTilesCountXOrY - 1;

        // Технически видим область больше чтобы она успевала прогружаться
        // когда скролим вверх или влево не отрабатвыет
        // так будет меньше видимых загрузок тайлов, артефактов.
        float leftX = leftTopWorld[0];
        float topY = leftTopWorld[1];
        if(leftX < 0) {
            // рендрим только от нуля по x и y
            leftX = 0;
        }
        if(topY > 0) {
            topY = 0;
        } else {
            // перевернуть ось чтобы она соответствовала оси y тайлов
            topY = -1 * topY;
        }

        float rightX = bottomRightWorld[0];
        float bottomY = bottomRightWorld[1];

        if(rightX < 0) {
            // рендрим только от нуля по x и y
            rightX = 0;
        }

        if(bottomY > 0) {
            bottomY = 0;
        } else {
            // перевернуть ось чтобы она соответствовала оси y тайлов
            bottomY = -1 * bottomY;
        }

        // ограничиваем зону видимости размером карты
        if(bottomY >= maxBottomRightZoneWorldLength) {
            // -1 так как если не вычитать тогда будет считаться что нужно рендрить следующий тайл
            // которого нету
            bottomY = maxBottomRightZoneWorldLength - 1;
        }

        if(rightX >= maxBottomRightZoneWorldLength) {
            // -1 так как если не вычитать тогда будет считаться что нужно рендрить следующий тайл
            // которого нету
            rightX = maxBottomRightZoneWorldLength - 1;
        }

        // получаем видимость области в тайловых координатах
        float leftXViewTile = leftX / useTileSize;
        float rightXViewTile = rightX / useTileSize;

        int startX = (int) Math.floor(Math.min(leftXViewTile, rightXViewTile));
        int endX = (int) Math.floor(Math.max(leftXViewTile, rightXViewTile));

        // Увеличивыем видимую область чтобы прогружалось заранее
        if(startX > 0) {
            startX -= 1;
        }
        if(endX < maxTileXOrY) {
            endX += 1;
        }

        int[] xArray = new int[endX - startX + 1];
        for(int i = startX; i <= endX; i++) {
            xArray[i - startX] = i;
        }

        // получаем видимость области в тайловых координатах
        double bottomYViewTile = bottomY / useTileSize;
        double topYViewTile = topY / useTileSize;

        int startY = (int) Math.floor(Math.min(bottomYViewTile, topYViewTile));
        int endY = (int) Math.floor(Math.max(bottomYViewTile, topYViewTile));

        // Увеличивыем видимую область чтобы прогружалось заранее
        if(startY > 0) {
            startY -= 1;
        }
        if(endY < maxTileXOrY) {
            endY += 1;
        }

        int[] yArray = new int[endY - startY + 1];
        for(int i = startY; i <= endY; i++) {
            yArray[i - startY] = i;
        }

        //Log.d("GL_ARTEM", String.format("x %d - %d, y %d - %d", startX, endX, startY, endY));
        int[][][] viewTiles = new int[xArray.length][yArray.length][2];
        for(int xIndex = 0; xIndex < xArray.length; xIndex++) {
           for(int yIndex = 0; yIndex < yArray.length; yIndex++) {
               viewTiles[xIndex][yIndex] = new int[] {xArray[xIndex], yArray[yIndex], mapZ};
           }
        }
        return viewTiles;
    }

    public void renderMap(RenderTileInitiator renderTileInitiator) {
        if(renderMapFuture != null) {
            renderMapFuture.cancel(false);
        }
        RenderMapRunnable renderMapRunnable = new RenderMapRunnable(this, renderTileInitiator);
        renderMapFuture = executorService.submit(renderMapRunnable);
    }

    public void initCameraZ(int z) {
        float scaleFactor = calcScaleFactorByZ(z);
        mapScaleListener.setScaleFactor(scaleFactor);
        mapZ = calcMapZByScaleFactor(scaleFactor);
        moveStrong = calcMoveStrongByScaleFactor(scaleFactor);
        useTileSize = determineTileSize();
    }

    public void initCameraLatLng(float latitude, float longitude) {
        float[] xAndY = latLngToXAndY(latitude, longitude);
        currentMapX = xAndY[0];
        currentMapY = xAndY[1];
    }

    public void setCameraLatLng(float latitude, float longitude) {
        float[] xAndY = latLngToXAndY(latitude, longitude);
        currentMapX = xAndY[0];
        currentMapY = xAndY[1];

        actionsAfterMove();
    }

    public void setCameraZ(int z) {
        float scaleFactor = calcScaleFactorByZ(z);
        mapScaleListener.setScaleFactor(scaleFactor);
        renderer.setSeeMultiply(scaleFactor);
        updateMapZ(scaleFactor);
        calcCurrentTilesXAndY();
        previousCurrentTileX = currentTileX;
        previousCurrentTileY = currentTileY;
    }

    private void actionsAfterMove() {
        // Для оптимизации рендринга
        calcCurrentTilesXAndY();
        if(currentTileY != previousCurrentTileY || currentTileX != previousCurrentTileX) {
            renderMap(RenderTileInitiator.MOVE);
        }

        renderer.moveCameraHorizontally(currentMapX, currentMapY);
    }

    public float[] latLngToXAndY(float latitude, float longitude) {
        float latR = (float) (latitude * Math.PI / 180);
        float lngR = (float) (longitude * Math.PI / 180);
        return latRLongRToXAndY(latR, lngR);
    }

    public float[] latRLongRToXAndY(float latitudeR, float longitudeR) {
        float[] xAndYFactors = latRLngRToXAndYFactors(latitudeR, longitudeR);
        float xf = xAndYFactors[0];
        float yf = xAndYFactors[1];
        float x = (xf + 1) / 2;
        float y = (1 - yf) / 2;
        xAndYFactors[0] = x * maxBottomRightZoneWorldLength;
        xAndYFactors[1] = y * -maxBottomRightZoneWorldLength;
        return xAndYFactors;
    }

    public float[] latRLngRToXAndYFactors(float latitudeR, float longitudeR) {
        double radius = 1 / Math.PI;
        //float x = (maxBottomRightZoneWorldLength * longitudeR + maxBottomRightZoneWorldLength) / 2;
        double x = radius * longitudeR;
        double y = radius * Math.log( Math.tan(Math.PI / 4 + latitudeR / 2) );
        return new float[] { (float)x, (float)y};
    }

    public float[] xAndYToLatLng(float x, float y) {
        float longitudeFactor = x / maxBottomRightZoneWorldLength;

        float longitude = (float) (longitudeFactor * Math.PI);
        float latitude  = (float) ((2 * Math.atan(Math.exp(y / maxBottomRightZoneWorldLength * Math.PI)) - Math.PI / 2));
        return new float[] { latitude, longitude };
    }

    public float[] calcCurrentLatLongInRadians() {
        float y = currentMapY * 2 + maxBottomRightZoneWorldLength;
        float x = currentMapX * 2 - maxBottomRightZoneWorldLength;
        return xAndYToLatLng(x, y);
    }

    private void clearAllTiles() {
        for(int i = 0; i < renderedTiles.length; i++) {
            Tile tile = renderedTiles[i];
            if(tile != null) {
                clearTile(i);
            }
        }
    }

    public void determineMoveStrong(float scaleFactor) {
        float determineMoveStrong = 60;
        determineMoveStrong = calcMoveStrongByScaleFactor(scaleFactor);
        if(determineMoveStrong != moveStrong) {
            moveStrong = determineMoveStrong;
        }
    }

    public void updateMapZ(float scaleFactor) {
        int determinedMapZ = 0;
        determineMoveStrong(scaleFactor);

        // логарифмическая регрессия
        determinedMapZ = calcMapZByScaleFactor(scaleFactor);

        if(determinedMapZ != mapZ) {
            boolean up = determinedMapZ > mapZ;
            RenderTileInitiator initiator = up ? RenderTileInitiator.ZOOM_UP : RenderTileInitiator.ZOOM_DOWN;
            // убираем все тайлы
            clearAllTiles();

            mapZ = determinedMapZ;
            useTileSize = determineTileSize();
            Log.d("GL_ARTEM", "New zoom " + mapZ);

            // Обновляем размер маркеров
            animatedMarkersRadiusLock.lock();
            animatedMarkersRadius.clear();
            List<TuserMarker> markers = renderer.animateMarkersRadius(getRadiusOfMarkerForCurrentMap());
            animatedMarkersRadius.addAll(markers);
            animatedMarkersRadiusLock.unlock();

            // чтобы calcViewTiles правильно отработал нужно чтобы матрицы изменились
            // а матрица modelViewMatrix считается в момент рендринга...
            requestRender();

            renderMap(initiator);
        }
    }

    private float getRadiusOfMarkerForCurrentMap() {
        return (float) (useTileSize / 10.);
    }

    private int calcMapZByScaleFactor(double scaleFactor) {
        return (int)(mapZRegressionParamOne - mapZRegressionParamTwo * Math.log(scaleFactor));
    }

    private float calcMoveStrongByScaleFactor(double scaleFactor) {
        return (float)(0.00089951 * Math.pow(scaleFactor, 0.94762810));
    }

    private float calcScaleFactorByZ(int z) {
        return (float) Math.exp((mapZRegressionParamOne - z) / mapZRegressionParamTwo);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

//        float[] worldLocation = renderer.screenCoordinatesToWorldLocation(x, y);
//        float[] latLng = xAndYToLatLng(worldLocation[0], worldLocation[1]);
//        animatedMarkersMoveLock.lock();
//        Iterator<TuserMarker> markers = renderer.getMarkers().iterator();
//        while (markers.hasNext()) {
//            TuserMarker moveMarker = markers.next();
//            moveMarker.setAnimateToCenterXY(worldLocation[0], worldLocation[1]);
//            animatedMarkersMove.clear();
//            animatedMarkersMove.add(moveMarker);
//        }
//        animatedMarkersMoveLock.unlock();

        if(event.getPointerCount() == 2) {
            // Маштабирование карты
            float scaleFactor = 1;
            renderer.setSeeMultiply(scaleFactor);
            updateMapZ(scaleFactor);
            // подсчет нужен для оптимизации рендринга
            calcCurrentTilesXAndY();
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            // Перемещение по карте
            float dx = previousX - x;
            float dy = previousY - y;

            dx *= moveStrong;
            dy *= moveStrong;

            double realMove = MathUtils.calculateVectorLength(dx, dy);
            double realMoveLimit = moveStrong * 300;
            //Log.d("GL_ARTEM", String.format("Real move = %.3f moveStrong = %.3f limit = %.3f", realMove, moveStrong, realMoveLimit));
            if(realMove < realMoveLimit) {
                currentMapX += dx;
                currentMapY -= dy;
                actionsAfterMove();
            }
        }

        previousCurrentTileX = currentTileX;
        previousCurrentTileY = currentTileY;

        previousX = x;
        previousY = y;

        requestRender();
        return true;
    }

    private void calcCurrentTilesXAndY() {
        currentTileX = (int)(currentMapX / useTileSize);
        currentTileY = (int)Math.abs(currentMapY / useTileSize);
    }
}
