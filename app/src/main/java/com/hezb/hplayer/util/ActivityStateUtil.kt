package com.hezb.hplayer.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Project Name: HPlayer
 * File Name:    ActivityStateUtil
 *
 * Description: Activity状态工具类.
 *
 * @author  hezhubo
 * @date    2022年03月02日 21:56
 */
object ActivityStateUtil {

    /**
     * 判断对象是否已Destroy
     *
     * @param context
     * @return true 已销毁
     */
    @JvmStatic
    fun isDestroy(context: Context?): Boolean {
        if (context == null) {
            return true
        }
        if (context is Activity) {
            return isDestroy(context as Activity?)
        } else if (context is ContextWrapper) {
            if (context.baseContext is Activity) {
                return isDestroy(context.baseContext as Activity?)
            }
        }
        return false
    }

    /**
     * 判断对象是否已Destroy
     *
     * @param activity
     * @return true 已销毁
     */
    @JvmStatic
    fun isDestroy(activity: Activity?): Boolean {
        if (activity == null) {
            return true
        }
        return activity.isFinishing || activity.isDestroyed
    }

}