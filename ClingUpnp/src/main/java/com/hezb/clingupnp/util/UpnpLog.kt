package com.hezb.clingupnp.util

import android.util.Log
import com.hezb.clingupnp.BuildConfig

/**
 * Project Name: HPlayer
 * File Name:    UpnpLog
 *
 * Description: 日志打印.
 *
 * @author  hezhubo
 * @date    2022年03月04日 10:07
 */
object UpnpLog {

    private const val TAG = "[ClingUpnp]"

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