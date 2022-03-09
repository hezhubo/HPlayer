package com.hezb.player.core

import android.util.Log

/**
 * Project Name: HPlayer
 * File Name:    PlayerLog
 *
 * Description: 日志打印.
 *
 * @author  hezhubo
 * @date    2022年03月02日 15:32
 */
object PlayerLog {

    private const val TAG = "[HPlayer]"

    var writeLogs = BuildConfig.DEBUG

    @JvmStatic
    fun i(msg: String, vararg args: Any?) {
        if (writeLogs) {
            Log.i(TAG, String.format(msg, *args))
        }
    }

    @JvmStatic
    fun d(msg: String, vararg args: Any?) {
        if (writeLogs) {
            Log.d(TAG, String.format(msg, *args))
        }
    }

    @JvmStatic
    fun e(msg: String, vararg args: Any?) {
        Log.e(TAG, String.format(msg, *args))
    }

    @JvmStatic
    fun e(msg: String, t: Throwable?) {
        Log.e(TAG, msg, t)
    }

}