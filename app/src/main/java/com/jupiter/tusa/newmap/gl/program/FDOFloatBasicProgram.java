package com.jupiter.tusa.newmap.gl.program;

import android.opengl.GLES20;

import com.google.j2objc.annotations.ObjectiveCName;
import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import com.jupiter.tusa.newmap.mvt.MvtObjectStyled;
import com.jupiter.tusa.utils.ArrayUtils;

import org.checkerframework.checker.units.qual.A;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FDOFloatBasicProgram implements DrawOpenGlProgram {
    private final Object lockObject = new Object();
    private final Object renderLock = new Object();
    private final List<MvtObjectStyled> input;
    private List<FDOFloatBasicInput> fdoFloatBasicInputs = new ArrayList<>();
    private final int mProgram;

    public FDOFloatBasicProgram(
            List<MvtObjectStyled> initInput,
            int fragmentShaderPointer,
            int vertexShaderPointer
    ) {
        this.input = initInput;

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderPointer);
        GLES20.glAttachShader(mProgram, fragmentShaderPointer);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);
    }

    public void addDataForDrawing(List<MvtObjectStyled> mvtObjectStyledList, int freshKey) {
        List<FDOFloatBasicInput> fdoFloatBasicInputs = null;
        synchronized (lockObject) {
            if(freshKey >= 0) {
                Iterator<MvtObjectStyled> iterator = input.iterator();
                while (iterator.hasNext()) {
                    MvtObjectStyled data = iterator.next();
                    if(data.getKey() < freshKey) {
                        iterator.remove();
                    }
                }
            }

            input.addAll(mvtObjectStyledList);
            fdoFloatBasicInputs = update();
        }

        synchronized (renderLock) {
            this.fdoFloatBasicInputs = fdoFloatBasicInputs;
        }
    }

    private List<FDOFloatBasicInput> update() {
        List<FDOFloatBasicInput> fdoFloatBasicInputs = new ArrayList<>();
        Map<String, List<MvtObjectStyled>> groupedByKeys = groupByKeys(input);
        for(List<MvtObjectStyled> styledArray : groupedByKeys.values()) {
            MvtObjectStyled mvtObjectStyled = styledArray.get(0);
            for(int i = 1; i < styledArray.size(); i++) {
                MvtObjectStyled to = styledArray.get(i);
                mvtObjectStyled.addAdditional(to.getVertices(), to.getDrawOrder());
            }
            fdoFloatBasicInputs.add(new FDOFloatBasicInput(
                    mvtObjectStyled.getKey(),
                    mvtObjectStyled.getStyleKey(),
                    ArrayUtils.ToArray(mvtObjectStyled.getVertices()),
                    ArrayUtils.ToArrayInt(mvtObjectStyled.getDrawOrder()),
                    mvtObjectStyled.getCoordinatesPerVertex(),
                    mvtObjectStyled.getSizeOfOneCoordinate(),
                    FDOFloatBasicInput.getModeFromMvtShape(mvtObjectStyled.getShape()),
                    mvtObjectStyled.getParameters().getColor(),
                    mvtObjectStyled.getParameters().getLineWidth()
            ));
        }
        return fdoFloatBasicInputs;
    }

    @Override
    public void draw(float[] mvpMatrix) {
        int handle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(handle, 1, false, mvpMatrix, 0);
        synchronized (renderLock)
        {
            for(FDOFloatBasicInput input : fdoFloatBasicInputs) {
                int coordinatesPerVertex = input.getCoordinatesPerVertex();
                float[] color = input.getColor();

                int vertexAttributePointerHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
                GLES20.glEnableVertexAttribArray(vertexAttributePointerHandle);
                GLES20.glVertexAttribPointer(
                        vertexAttributePointerHandle,
                        coordinatesPerVertex,
                        GLES20.GL_FLOAT,
                        false,
                        input.getVertexStride(),
                        input.getVertexBuffer()
                );

                int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
                GLES20.glUniform4fv(colorHandle, 1, color, 0);
                GLES20.glLineWidth(input.getLineWidth());
                GLES20.glDrawElements(input.getDrawMode(), input.getDrawOrderLength(), GLES20.GL_UNSIGNED_INT, input.getDrawListBuffer());
                GLES20.glDisableVertexAttribArray(vertexAttributePointerHandle);
            }
        }
    }

    private Map<String, List<MvtObjectStyled>> groupByKeys(List<MvtObjectStyled> mvtObjectStyledList) {
        Map<String, List<MvtObjectStyled>> groupedByKeys = new HashMap<>();
        for (MvtObjectStyled mvtObject : mvtObjectStyledList) {
            String key = mvtObject.getStyleKey();
            if (!groupedByKeys.containsKey(key)) {
                groupedByKeys.put(key, new ArrayList<>());
            }
            Objects.requireNonNull(groupedByKeys.get(key)).add(mvtObject);
        }
        return groupedByKeys;
    }
}