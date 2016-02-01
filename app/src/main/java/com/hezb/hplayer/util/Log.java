package com.hezb.hplayer.util;

/**
 * Log工具
 */
public class Log {

    /** 是否打印Log */
    private static final boolean DEBUG = true;

    private static final String TAG = "hezb";

    public static void d(String info) {
        if (DEBUG) {
            android.util.Log.d(TAG, info);
        }
    }

    public static void e(String info) {
        if (DEBUG) {
            android.util.Log.e(TAG, info);
        }
    }

    public static void v(String TAG, String info) {
        if (DEBUG) {
            android.util.Log.v(TAG, info);
        }
    }
    public static void d(String TAG, String info) {
        if (DEBUG) {
            android.util.Log.d(TAG, info);
        }
    }
    public static void i(String TAG, String info) {
        if (DEBUG) {
            android.util.Log.i(TAG, info);
        }
    }
    public static void w(String TAG, String info) {
        if (DEBUG) {
            android.util.Log.w(TAG, info);
        }
    }
    public static void e(String TAG, String info) {
        if (DEBUG) {
            android.util.Log.e(TAG, info);
        }
    }
    
}
