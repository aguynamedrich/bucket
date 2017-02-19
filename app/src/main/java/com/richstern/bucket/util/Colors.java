package com.richstern.bucket.util;

public class Colors {

    public static int toRGB(int red, int green, int blue) {
        return 0xFF000000 |
            ((red << 16) & 0x00FF0000) |
            ((green << 8) & 0x0000FF00) |
            (blue & 0x000000FF);
    }

    public static int red(int color) {
        return color & 0x00FF0000 >> 16;
    }

    public static int green(int color) {
        return color & 0x0000FF00 >> 8;
    }

    public static int blue(int color) {
        return color & 0x000000FF;
    }
}
