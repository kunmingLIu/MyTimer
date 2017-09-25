package com.liu.mytimer.Utils;

import android.util.Log;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class Util {
    public static void log(String message, Object... formatArgs) {
        if (formatArgs != null && formatArgs.length > 0) {
            Log.e("MyTimer", String.format(message, formatArgs));
        } else {
            Log.e("MyTimer", message);
        }
    }
}
