package com.liu.mytimer.utils;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

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
    public static String covertTimeToString(long time){
        long hour = 0;
        long minute = 0;
        long second = 0;
        second  = time / 1000;
        if(second >= 60){
            minute = second /60;
            second = second % 60;
        }
        if(minute >= 60){
            hour = minute /60;
            minute = minute % 60;
        }
        return String.format("%02d:%02d:%02d",hour,minute,second);

    }
    public static int sp2px(Context context , int sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
