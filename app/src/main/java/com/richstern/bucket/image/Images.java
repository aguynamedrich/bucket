package com.richstern.bucket.image;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Images {

    public static int[][] to2dArray(int[] pixels, int width, int height) {
        int[][] grid = new int[width][height];
        int x, y;
        for (int i = 0; i < pixels.length; i++) {
            x = i % width;
            y = i / width;
            grid[x][y] = pixels[i];
        }
        return grid;
    }

    public static int[] flatten(int[][] grid) {
        int width = grid.length;
        int[] flat = new int[width * grid[0].length];
        int x, y;
        for (int i = 0; i < flat.length; i++) {
            x = i % width;
            y = i / width;
            flat[i] = grid[x][y];
        }
        return flat;
    }

    public static void floodFill(int[][] imageData, int x, int y, int fillColor, int threshold) {
        int originalColor = imageData[x][y];

        List<Point> origins = new ArrayList<>();
        List<Point> newOrigins = new ArrayList<>();
        List<Point> used = new ArrayList<>();

        origins.add(new Point(x, y));

        while (!origins.isEmpty()) {
            for (Point origin : origins) {
                int originX = origin.x;
                int originY = origin.y;

                int minX = Math.max(0, originX - 1);
                int minY = Math.max(0, originY - 1);
                int maxX = Math.min(imageData.length - 1, originX + 1);
                int maxY = Math.min(imageData[0].length - 1, originY + 1);

                for (int i = minX; i <= maxX; i++) {
                    for (int j = minY; j <= maxY; j++) {
                        int colorAtPixel = imageData[i][j];
                        if (withinThreshold(originalColor, colorAtPixel, threshold)) {
                            // Fill current pixel
                            imageData[i][j] = fillColor;

                            // Add new origin if not existing origin
                            if (i != originX && j != originY) {
                                Point newOrigin = new Point(i, j);
                                // Avoid repeats, since it's possible they pass threshold test
                                if (!used.contains(newOrigin)) {
                                    used.add(newOrigin);
                                    newOrigins.add(newOrigin);
                                }
                            }
                        }
                    }
                }
            }

            origins.clear();
            origins.addAll(newOrigins);
            newOrigins.clear();
        }
    }

    private static boolean withinThreshold(int originalColor, int colorAtPixel, int threshold) {
        int originalRed = originalColor & 0x00FF0000 >> 16;
        int originalGreen = originalColor & 0x0000FF00 >> 8;
        int originalBlue = originalColor & 0x000000FF;
        int colorAtPixelRed = colorAtPixel & 0x00FF0000 >> 16;
        int colorAtPixelGreen = colorAtPixel & 0x0000FF00 >> 8;
        int colorAtPixelBlue = colorAtPixel & 0x000000FF;
        int diffRed = Math.abs(originalRed - colorAtPixelRed);
        int diffGreen = Math.abs(originalGreen - colorAtPixelGreen);
        int diffBlue = Math.abs(originalBlue - colorAtPixelBlue);
        float avgDiff = (diffRed + diffGreen + diffBlue) / 3.0f;
        float percentDiff = avgDiff / 255 * 100;
        return  percentDiff < threshold;
    }

    public static int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, copy[i], 0, src[i].length);
        }
        return copy;
    }
}
