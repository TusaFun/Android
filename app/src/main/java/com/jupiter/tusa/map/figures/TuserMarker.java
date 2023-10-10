package com.jupiter.tusa.map.figures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.jupiter.tusa.R;
import com.jupiter.tusa.map.MyGLRenderer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TuserMarker {
    private final Context mActivityContext;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private final int mTextureCoordinateDataSize = 2;
    private int mTextureDataHandle;
    private FloatBuffer vertexBuffer;
    private FloatBuffer mCubeTextureCoordinates;
    private ShortBuffer drawListBuffer;
    private int shaderProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float alpha = 1f;
    private short[] drawOrder;
    private Bitmap bitmap;
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    final int COORDINATE_SIZE = 4;
    final int COORDS_PER_VERTEX = 2;
    private final int vertexStride = COORDS_PER_VERTEX * COORDINATE_SIZE; //Bytes per vertex

    private final String vertexShaderCode =
            "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate;" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition + vec4(0, 0, 0, 0);" +
            "  v_TexCoordinate = a_TexCoordinate;" +
            "}";

    private final String fragmentShaderCode =
            "uniform vec4 vColor;" +
            "uniform float uAlpha;" +
            "uniform float uTime;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "gl_FragColor = vec4(texture2D(u_Texture, v_TexCoordinate).rgb, uAlpha);" +
            "}";

    public int getSegments() {return _segments;}
    public float getRadius() {return _radius;}
    public float getCenterX() {return _centerX;}
    public float getCenterY() {return _centerY;}
    public float getAnimateToRadius() {return _animateRadiusTo;}
    public float getAnimateToCenterX() {return _animateToCenterX;}
    public float getAnimateToCenterY() {return _animateToCenterY;}
    public void setAnimateRadiusTo(float radius) {
        _animateRadiusTo = radius;
    }
    public void setAnimateToCenterXY(float x, float y) {
        _animateToCenterX = x;
        _animateToCenterY = y;
    }

    private Float _animateRadiusTo;
    private float _animateToCenterX;
    private float _animateToCenterY;
    private int _segments;
    private float _radius;
    private float _centerX;
    private float _centerY;

    public TuserMarker(Context context, Bitmap bitmap, int segments, float centerX, float centerY, float radius) {
        _centerX = centerX;
        _centerY = centerY;
        _radius = radius;
        _segments = segments;
        this.bitmap = bitmap;
        changeGeometry(segments, centerX, centerY, radius);
        mActivityContext = context;

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);

        //Texture Code
        GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");
        GLES20.glLinkProgram(shaderProgram);
        mTextureDataHandle = loadTexture(mActivityContext, bitmap);
    }


    public void changeGeometry(int segments, float centerX, float centerY, float radius) {
        _radius = radius;
        _segments = segments;
        _centerY = centerY;
        _centerX = centerX;
        float[] vertexLocations = new float[segments * COORDS_PER_VERTEX];
        drawOrder = new short[segments];

        float[] cubeTextureCoordinateData = new float[segments * COORDS_PER_VERTEX];

        for (int i = 0; i < segments; i++) {
            double angle = Math.toRadians(i * (360.0 / segments));

            float x = (float) (radius * Math.cos(angle));
            float y = (float) (radius * Math.sin(angle));

            vertexLocations[i * 2] = centerX + x;
            vertexLocations[i * 2 + 1] = centerY + y;

            cubeTextureCoordinateData[i * 2] = (float) (0.5 + 0.5 * Math.cos(angle));
            cubeTextureCoordinateData[i * 2 + 1] = (float) (0.5 - 0.5 * Math.sin(angle));

            drawOrder[i] = (short) i;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexLocations.length * COORDINATE_SIZE);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexLocations);
        vertexBuffer.position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

        //Initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    public void draw(float[] mvpMatrix)
    {
        //Add program to OpenGL ES Environment
        GLES20.glUseProgram(shaderProgram);

        int alphaParameter = GLES20.glGetUniformLocation(shaderProgram, "uAlpha");
        GLES20.glUniform1f(alphaParameter, alpha);

        //Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

        //Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        //Get Handle to Fragment Shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");

        //Set the Color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        //Set Texture Handles and bind Texture
        mTextureUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 1);

        //Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public int loadTexture(final Context context, Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            if(bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bricks, options);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
