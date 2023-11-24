package com.jupiter.tusa.utils;

import android.content.Context;
import android.content.res.Resources;

import java.io.InputStream;
import java.util.Scanner;

public class ReadTextFromResource {
    public static String readText(Context context, int resourceId) {
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(resourceId);
        Scanner scanner = new Scanner(inputStream);
        StringBuilder stringBuilder = new StringBuilder();

        try {
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
        } finally {
            scanner.close();
        }

        return stringBuilder.toString();
    }
}
