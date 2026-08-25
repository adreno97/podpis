package ru.trucker.sign;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

/** Палитра приложения «Подпись PDF» (светлая тема). */
public class Ui {

    private Ui() {}

    public static GradientDrawable round(Context c, int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(Util.dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable gradient(Context c, int start, int end, int radiusDp) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        g.setCornerRadius(Util.dp(c, radiusDp));
        return g;
    }

    // Фоны
    public static int bg() { return 0xFFF3F6FB; }
    public static int card() { return 0xFFFFFFFF; }
    public static int field() { return 0xFFFFFFFF; }
    public static int divider() { return 0xFFE8EDF5; }

    // Акценты
    public static int accent() { return 0xFF3D5AFE; }
    public static int accentDark() { return 0xFF3949AB; }
    public static int headerStart() { return 0xFF1A237E; }
    public static int headerEnd() { return 0xFF3D5AFE; }
    public static int success() { return 0xFF2E7D32; }
    public static int danger() { return 0xFFC62828; }
    public static int neutral() { return 0xFF546E7A; }

    // Текст
    public static int title() { return 0xFF1A237E; }
    public static int primary() { return 0xFF263238; }
    public static int sub() { return 0xFF78909C; }
    public static int label() { return 0xFF607D8B; }

    public static int buttonText() { return 0xFFFFFFFF; }
}
