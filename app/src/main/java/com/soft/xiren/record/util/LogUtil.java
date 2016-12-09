package com.soft.xiren.record.util;

import android.util.Log;

/**
 * @author yanggf
 */
public class LogUtil {
    public static boolean sIsDebug = true;
    /**
     * 日志输出时的TAG
     */
    private static String TAG = LogUtil.class.getSimpleName();

    /**
     * 设置是否打印log日志
     * @param isDebug
     * */
    public static void enableDebug(boolean isDebug) {
        sIsDebug = isDebug;
    }

    public static void v(String msg) {
        if (sIsDebug)
            Log.v(TAG, msg);
    }

    public static void v(String msg, Throwable t) {
        if (sIsDebug)
            Log.v(TAG, msg, t);
    }

    public static void d(String msg) {
        if (sIsDebug)
            Log.d(TAG, msg);
    }

    public static void d(String msg, Throwable t) {
        if (sIsDebug)
            Log.d(TAG, msg, t);
    }

    public static void i(String msg) {
        if (sIsDebug)
            Log.i(TAG, msg);
    }

    public static void i(String msg, Throwable t) {
        if (sIsDebug)
            Log.i(TAG, msg, t);
    }

    public static void w(String msg) {
        if (sIsDebug)
            Log.w(TAG, msg);
    }

    public static void w(String msg, Throwable t) {
        if (sIsDebug)
            Log.w(TAG, msg, t);
    }

    public static void e(String msg) {
        if (sIsDebug)
            Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable t) {
        if (sIsDebug)
            Log.e(TAG, msg, t);
    }

    public static void e(Throwable t) {
        if (sIsDebug)
            Log.e(TAG, "", t);
    }


    public static void v(String tag, String msg) {
        if (sIsDebug)
            Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable t) {
        if (sIsDebug)
            Log.v(tag, msg, t);
    }

    public static void d(String tag, String msg) {
        if (sIsDebug)
            Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable t) {
        if (sIsDebug)
            Log.d(tag, msg, t);
    }

    public static void i(String tag, String msg) {
        if (sIsDebug)
            Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable t) {
        if (sIsDebug)
            Log.i(tag, msg, t);
    }

    public static void w(String tag, String msg) {
        if (sIsDebug)
            Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable t) {
        if (sIsDebug)
            Log.w(tag, msg, t);
    }

    public static void e(String tag, String msg) {
        if (sIsDebug)
            Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        if (sIsDebug)
            Log.e(tag, msg, t);
    }


}
