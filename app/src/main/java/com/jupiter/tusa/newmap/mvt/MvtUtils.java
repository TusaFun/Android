package com.jupiter.tusa.newmap.mvt;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.jupiter.tusa.utils.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import earcut4j.Earcut;
import vector_tile.VectorTile;

public class MvtUtils {
    public static VectorTile.Tile parse(byte[] bytesTile) throws InvalidProtocolBufferException {
        return VectorTile.Tile.parseFrom(bytesTile);
    }

    public static VectorTile.Tile parse(Context context, int resourceId) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // Буфер для чтения байтов
        int bytesRead;

        // Чтение и запись байтов в буфер
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] allBytes = outputStream.toByteArray(); // Получить все байты из потока
        return VectorTile.Tile.parseFrom(allBytes);
    }

    private static int getParameter(int geometry) {
        return  ((geometry >> 1) ^ (-(geometry & 1)));
    }

    public static float[] insertZOrderTo2D(float[] vertices, float z) {
        int pointsAmount = vertices.length / 2;
        float[] newArray = new float[pointsAmount * 3];
        for(int i = 0; i < pointsAmount; i++) {
            int rightIndex = i * 2;
            int leftIndex = i * 3;
            newArray[leftIndex] = vertices[rightIndex];
            newArray[leftIndex + 1] = vertices[rightIndex + 1];
            newArray[leftIndex + 2] = z;
        }
        return newArray;
    }

    public static List<MvtPolygon> readPolygons(VectorTile.Tile.Feature feature, VectorTile.Tile.Layer layer) {
        int dimension = 2;
        List<MvtPolygon> mvtPolygons = new ArrayList<>();
        MvtGeometryRead mvtGeometryRead = MvtUtils.readFeatureGeometry(feature);

        for(int i = 0; i < mvtGeometryRead.vertices.size(); ) {
            Vertices vertices = mvtGeometryRead.vertices.get(i);
            List<List<Float>> nextInteriorRings = new ArrayList<>();

            i += 1;
            while (i < mvtGeometryRead.vertices.size()) {
                Vertices next = mvtGeometryRead.vertices.get(i);
                if(!next.getClockwise())
                    break;
                nextInteriorRings.add(next.vertices);
                i += 1;
            }

            List<Integer> holesIndices = new ArrayList<>();
            List<Float> verticesWithHoleVertices = new ArrayList<>(vertices.vertices);
            for(int interior = 0; interior < nextInteriorRings.size(); interior++) {
                List<Float> interiorRing = nextInteriorRings.get(interior);
                holesIndices.add(verticesWithHoleVertices.size() / dimension);
                verticesWithHoleVertices.addAll(interiorRing);
            }

            double[] points = ArrayUtils.floatToDouble(verticesWithHoleVertices);
            int[] holesInt = ArrayUtils.ToArrayInt(holesIndices);
            if(holesInt.length == 0) {
                holesInt = null;
            }
            List<Integer> triangles = Earcut.earcut(points, holesInt, dimension);
            float[] verticesArray = ArrayUtils.ToArray(verticesWithHoleVertices);
            mvtPolygons.add(new MvtPolygon(
                            verticesArray,
                            ArrayUtils.ToArrayInt(triangles),
                            layer.getName(),
                            readTags(layer, feature)
                    )
            );
        }
        return mvtPolygons;
    }

    public static List<MvtLines> readLines(VectorTile.Tile.Feature feature, VectorTile.Tile.Layer layer) {
        List<MvtLines> linesInputs = new ArrayList<>();
        MvtGeometryRead mvtGeometryRead = readFeatureGeometry(feature);
        for(Vertices vertices : mvtGeometryRead.vertices) {
            float[] pointsFloat = ArrayUtils.ToArray(vertices.vertices);
            linesInputs.add(new MvtLines(
                    pointsFloat,
                    layer.getName(),
                    readTags(layer, feature)
            ));
        }
        return linesInputs;
    }

    public static MvtGeometryRead readFeatureGeometry(VectorTile.Tile.Feature feature) {
        VectorTile.Tile.GeomType geomType = feature.getType();
        Iterator<Integer> geometryIterator = feature.getGeometryList().iterator();

        int needParameters = -1;
        int currentCommand = -1;
        int currentCount = -1;
        Integer parameterX = null;
        Integer parameterY = null;
        int cursorX = 0;
        int cursorY = 0;
        Integer firstGeometryPointX = null;
        Integer firstGeometryPointY = null;
        List<Float> vertices = new ArrayList<>();
        ArrayList<Vertices> verticesArray = new ArrayList<>();
        int currentElement = 0;
        while(geometryIterator.hasNext()) {
            int geometry = geometryIterator.next();
            if(currentCommand == -1) {
                currentCommand = geometry & 0x7;
                currentCount = geometry >> 3;
                if(currentCommand == 1 || currentCommand == 2) {
                    needParameters = 2;

                    if(geomType == VectorTile.Tile.GeomType.LINESTRING && currentCommand == 1 && vertices.size() > 0) {
                        List<Float> verticesCopy = new ArrayList<>(vertices);
                        verticesArray.add(new Vertices(verticesCopy));
                        vertices.clear();
                    }
                } else if(currentCommand == 7) {
                    currentElement++;
                    currentCommand = -1;
                    firstGeometryPointX = null;
                    firstGeometryPointY = null;

//                    if(currentElement != 2) {
//                        vertices.clear();
//                        continue;
//                    }
//
//                    if(vertices.size() < 2000) {
//                        vertices.clear();
//                        continue;
//                    }
                    List<Float> verticesCopy = new ArrayList<>(vertices);
                    verticesArray.add(new Vertices(verticesCopy));
                    vertices.clear();
                }
                continue;
            }

            needParameters--;
            int parameter = ((geometry >> 1) ^ (-(geometry & 1)));
            if(needParameters == 1) {
                parameterX = parameter;
            } else if(needParameters == 0) {
                parameterY = parameter;
            }

            if(parameterX != null && parameterY != null) {
                cursorX += parameterX;
                cursorY -= parameterY;

                if(firstGeometryPointX == null) {
                    firstGeometryPointX = cursorX;
                }
                if(firstGeometryPointY == null) {
                    firstGeometryPointY = cursorY;
                }

                if(currentCommand == 1) {
                    vertices.add((float)cursorX);
                    vertices.add((float)cursorY);
                } else if(currentCommand == 2) {
                    vertices.add((float)cursorX);
                    vertices.add((float)cursorY);
                }

            }

            if(needParameters == 0) {
                currentCount--;
                needParameters = 2;
                parameterX = null;
                parameterY = null;
            }

            if(currentCount <= 0) {
                currentCount = -1;
                needParameters = -1;
                currentCommand = -1;
                parameterX = null;
                parameterY = null;
            }
        }

        if(
            (geomType == VectorTile.Tile.GeomType.POINT || geomType == VectorTile.Tile.GeomType.LINESTRING) &&
            vertices.size() > 0
        ) {
            List<Float> verticesCopy = new ArrayList<>(vertices);
            verticesArray.add(new Vertices(verticesCopy));
        }

        return new MvtGeometryRead(verticesArray, geomType);
    }


    public static Map<String, VectorTile.Tile.Value> readTags(VectorTile.Tile.Layer layer, VectorTile.Tile.Feature feature) {
        List<Integer> features = feature.getTagsList();
        Map<String, VectorTile.Tile.Value> map = new HashMap<>();
        for(int i = 0; i < features.size() / 2; i++) {
            int keyIndex = features.get(i * 2);
            int valueIndex = features.get(i * 2 + 1);
            String key = layer.getKeys(keyIndex);
            VectorTile.Tile.Value value = layer.getValues(valueIndex);
            map.put(key, value);
        }
        return map;
    }

    public static boolean isClockwise(List<Float> coordinates) {
        int n = coordinates.size() / 2;
        int sum = 0;

        for (int i = 0; i < n; i++) {
            float x1 = coordinates.get(2 * i);
            float y1 = coordinates.get(2 * i + 1);
            float x2 = coordinates.get((2 * i + 2) % (2 * n)); // Обработка последней точки, которая соединяется с первой
            float y2 = coordinates.get((2 * i + 3) % (2 * n));
            sum += (x2 - x1) * (y2 + y1);
        }

        return sum < 0;
    }

    public static int[] makeSequentSegments(int coordinatesArrayLength) {
        int[] segments = new int[coordinatesArrayLength];
        segments[0] = 0;
        for(int i = 1; i < coordinatesArrayLength / 2; i++) {
            int shift = i + (i - 1);
            segments[shift] = i;
            segments[shift + 1] = i;
        }
        segments[coordinatesArrayLength - 1] = 0;
        return segments;
    }
}
