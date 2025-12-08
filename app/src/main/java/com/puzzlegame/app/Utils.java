package com.puzzlegame.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import java.util.Locale;

public class Utils {

    public static Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        return Bitmap.createBitmap(bitmap, x, y, size, size);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int targetSize) {
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
    }

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config,
            context.getResources().getDisplayMetrics());
    }

    public static void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}
