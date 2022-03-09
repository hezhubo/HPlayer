package com.hezb.hplayer.application

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.multidex.MultiDex
import com.hezb.clingupnp.HttpServerService
import com.hezb.hplayer.util.ActivityStateUtil
import kotlin.properties.Delegates

/**
 * Project Name: HPlayer
 * File Name:    IApplication
 *
 * Description: Application.
 *
 * @author  hezhubo
 * @date    2022年03月02日 21:49
 */
class IApplication : Application() {

    companion object {
        @JvmStatic
        var instance: IApplication by Delegates.notNull()
    }

    private var mCurrentActivity: Activity? = null
    private var runningActivityCount: Int = 0

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    /**
     * 应用是否在前台
     *
     * @return
     */
    fun isForeground(): Boolean {
        return runningActivityCount > 0
    }

    /**
     * 当前应用最上层页面
     *
     * @return
     */
    fun getCurrentActivity(): Activity? {
        if (ActivityStateUtil.isDestroy(mCurrentActivity)) {
            return null
        }
        return mCurrentActivity
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                runningActivityCount++
            }

            override fun onActivityResumed(activity: Activity) {
                mCurrentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                runningActivityCount--
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                if (mCurrentActivity === activity) { // 比较地址
                    mCurrentActivity = null
                }
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HttpServerService.NOTIFICATION_CHANNEL_ID,
                "DLNA",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}