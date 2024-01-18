package com.jupiter.tusa.newmap.mvt;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jupiter.tusa.newmap.earcut.EarCutDeer;
import com.jupiter.tusa.utils.ArrayUtils;

import org.checkerframework.checker.units.qual.A;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public static List<Float> insertZOrderTo2D(List<Float> vertices, float z) {
        int pointsAmount = vertices.size() / 2;
        Float[] newArray = new Float[pointsAmount * 3];
        for(int i = 0; i < pointsAmount; i++) {
            int rightIndex = i * 2;
            int leftIndex = i * 3;
            newArray[leftIndex] = vertices.get(rightIndex);
            newArray[leftIndex + 1] = vertices.get(rightIndex + 1);
            newArray[leftIndex + 2] = z;
        }
        return new ArrayList(Arrays.asList(newArray));
    }

    public static MvtObject readPolygons(VectorTile.Tile.Feature feature, VectorTile.Tile.Layer layer) {
        int dimension = 2;
        MvtGeometryRead mvtGeometryRead = MvtUtils.readFeatureGeometry(feature);

        List<Float> resultVertices = new ArrayList<>();
        List<Integer> resultDrawOrder = new ArrayList<>();

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

            float[] points = ArrayUtils.ToArray(verticesWithHoleVertices);
            int[] holesInt = ArrayUtils.ToArrayInt(holesIndices);
            if(holesInt.length == 0) {
                holesInt = null;
            }
            List<Integer> triangles = EarCutDeer.earcut(points, holesInt, dimension);

            int startFromDrawOrder = (int) (resultVertices.size() / dimension);
            for(int m = 0; m < triangles.size(); m++) {
                resultDrawOrder.add(triangles.get(m) + startFromDrawOrder);
            }
            resultVertices.addAll(verticesWithHoleVertices);
        }
        return new MvtObject(
                resultVertices,
                resultDrawOrder,
                layer.getName(),
                readTags(layer, feature),
                MvtShapes.POLYGON
        );
    }

    public static MvtObject readLines(VectorTile.Tile.Feature feature, VectorTile.Tile.Layer layer) {
        MvtGeometryRead mvtGeometryRead = readFeatureGeometry(feature);
        int dimension = 2;
        List<Float> resultPoints = new ArrayList<>();
        List<Integer> resultDrawOrder = new ArrayList<>();
        for(Vertices vertices : mvtGeometryRead.vertices) {
            int startFromDrawOrder = resultPoints.size() / dimension;
            int maxIndexBorder = vertices.vertices.size() / dimension - 1;
            for(int i = 0; i < maxIndexBorder; i++) {
                resultDrawOrder.add(i + startFromDrawOrder);
                resultDrawOrder.add(i + startFromDrawOrder + 1);
            }

            resultPoints.addAll(vertices.vertices);
        }
        return new MvtObject(
                resultPoints,
                resultDrawOrder,
                layer.getName(),
                readTags(layer, feature),
                MvtShapes.LINE
        );
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
        List<Integer> tags = feature.getTagsList();
        Map<String, VectorTile.Tile.Value> map = new HashMap<>();
        for(int i = 0; i < tags.size() / 2; i++) {
            int keyIndex = tags.get(i * 2);
            int valueIndex = tags.get(i * 2 + 1);
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

    public static String mapToString(Map<String, VectorTile.Tile.Value> tags) {
        StringBuilder stringBuilder = new StringBuilder("{");

        for (Map.Entry<String, VectorTile.Tile.Value> entry : tags.entrySet()) {
            String key = entry.getKey();
            VectorTile.Tile.Value value = entry.getValue();

            // Convert the Value to a string representation
            String valueString = value.getStringValue();

            // Append the key-value pair to the result string
            stringBuilder.append("\"").append(key).append("\":").append(valueString).append(", ");
        }

        // Remove the trailing comma and space if there are entries in the map
        if (!tags.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
