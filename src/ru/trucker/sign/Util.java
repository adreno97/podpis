package ru.trucker.sign;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;

public class Util {

    public static int dp(Context ctx, int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }

    public static String date(long millis) {
        return DateFormat.format("dd.MM.yyyy HH:mm", millis).toString();
    }
}
