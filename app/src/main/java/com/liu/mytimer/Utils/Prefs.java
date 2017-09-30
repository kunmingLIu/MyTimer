package com.liu.mytimer.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class Prefs {
    public static String getStartDate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("start_date", "2017/09/20");
    }
    public static void saveStartDate(Context context, String startDate){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("start_date", startDate)
                .commit();
    }
    public static String getStartTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("start_time", "00:00:00");
    }
    public static void saveStartTime(Context context, String startTime){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("start_time", startTime)
                .commit();
    }

    public static String getEndTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("end_time", "00:00:00");
    }
    public static void saveEndTime(Context context, String stopTime){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("end_time", stopTime)
                .commit();
    }
    public static void saveFirstTime(Context context , boolean isFirst){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("first_time", isFirst)
                .commit();
    }
    public static boolean isFirstTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("first_time", true);
    }
}
