package com.jupiter.tusa.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.jupiter.tusa.MainActivity;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class MyGlSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private MainActivity mainActivity;

    public MyGlSurfaceView(Context context, AttributeSet attributeSet) throws ExecutionException, InterruptedException, TimeoutException {
        super(context, attributeSet);
        mainActivity = (MainActivity) context;
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context, this);
        setRenderer(renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // world location
        float wx = -0.5f;
        float wy = 0.5f;

        float ndcCoordinates[] = new float[4];
        float modelViewMatrix[] = renderer.getModelViewMatrix();
        Matrix.multiplyMV(ndcCoordinates, 0, modelViewMatrix, 0, new float[] { wx, wy, 1f, 1 }, 0);

        //Log.d("GL_ARTEM", "NDC x(" + ndcCoordinates[0] / ndcCoordinates[3] + ") y(" + ndcCoordinates[1] / ndcCoordinates[3] + ") z(" + ndcCoordinates[2] / ndcCoordinates[3] + ") w(" + ndcCoordinates[3] + ")");

        float ndcX = ndcCoordinates[0] / ndcCoordinates[3];
        float ndcY = ndcCoordinates[1] / ndcCoordinates[3];
        float ndcZ = ndcCoordinates[2] / ndcCoordinates[3];

        float[] invertedModelViewMatrix = new float[16];
        Matrix.invertM(invertedModelViewMatrix, 0, modelViewMatrix, 0);


        // normalize viewport coordinates
        float nx = x * 2 / renderer.getWidth() - 1;
        float ny = 1 - (y * 2 / renderer.getHeight());
        float nz = -1f;

        float[] worldCoordinates = new float[4];
        Matrix.multiplyMV(worldCoordinates, 0, invertedModelViewMatrix, 0, new float[] {nx, ny, nz, 1}, 0);

        float w = worldCoordinates[3];
        worldCoordinates[0] /= w;
        worldCoordinates[1] /= w;
        worldCoordinates[2] /= w;

        Log.d("GL_ARTEM", "World( x(" + worldCoordinates[0] +") y(" + worldCoordinates[1] + ") z(" + worldCoordinates[2] + "))");

        // Локальные координаты вершин
        float[] localVertices = new float[] {
                -1f, 1f,
                -1f, -1f,
                1f, -1f,
                1f, 1f
        };

        float[] projectionMatrix = new float[16];
        Matrix.perspectiveM(projectionMatrix, 0, 45, 0.507f, 1, 7);

        //Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, 1, 7);

        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 4, 0, 0f, 0f, 0f, 1.0f, 0.0f);

        Matrix.multiplyMM(modelViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        for (int i = 0; i < localVertices.length; i += 2) {
            float x1 = localVertices[i];
            float y1 = localVertices[i + 1];
            float z1 = 0f; // Здесь предполагаем, что треугольник находится в плоскости z = 0

            // Преобразование из локальных координат в NDC координаты
            Matrix.multiplyMV(ndcCoordinates, 0, modelViewMatrix, 0, new float[] { x1, y1, z1, 1 }, 0);

            // Преобразование из NDC координат в мировые координаты
            Matrix.invertM(invertedModelViewMatrix, 0, modelViewMatrix, 0); // Обратная матрица модели-вида
            Matrix.multiplyMV(worldCoordinates, 0, invertedModelViewMatrix, 0, ndcCoordinates, 0);

            //Log.d("Vertex", "Local: (" + x + ", " + y + ", " + z + "), World: (" + worldCoordinates[0] + ", " + worldCoordinates[1] + ", " + worldCoordinates[2] + ") NDC: (" + ndcCoordinates[0] + ", " + ndcCoordinates[1] + ", " + ndcCoordinates[2] + ")");
        }

        requestRender();
        return true;
    }


    private float xToLongitude(float worldX) {
        float longitudeRange = 180.0f;
        return worldX * longitudeRange;
    }

    private float yToLatitude(float worldY) {
        float longitudeRange = 90.0f;
        return worldY * longitudeRange;
    }
}
