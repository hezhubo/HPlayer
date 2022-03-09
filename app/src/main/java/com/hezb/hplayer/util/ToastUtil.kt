package com.hezb.hplayer.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * Project Name: HPlayer
 * File Name:    ToastUtil
 *
 * Description: Toast工具类.
 *
 * @author  hezhubo
 * @date    2022年03月03日 20:41
 */
object ToastUtil {

    private var sToast: Toast? = null

    @JvmStatic
    fun show(context: Context?, stringId: Int) {
        context?.let {
            show(context, it.getString(stringId))
        }
    }

    @JvmStatic
    @JvmOverloads
    fun show(
        context: Context?,
        msg: CharSequence,
        duration: Int = Toast.LENGTH_SHORT,
        gravity: Int = Gravity.NO_GRAVITY,
        x: Int = 0,
        y: Int = 0
    ) {
        context?.let {
            sToast?.cancel()
            sToast = Toast.makeText(it.applicationContext, msg, duration)
            sToast?.let { toast ->
                if (gravity != Gravity.NO_GRAVITY) {
                    toast.setGravity(gravity, x, y)
                }
                toast.show()
            }
        }
    }

    @JvmStatic
    fun cancel() {
        sToast?.cancel()
    }
}