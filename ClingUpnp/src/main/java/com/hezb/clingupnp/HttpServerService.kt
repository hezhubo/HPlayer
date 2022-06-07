package com.hezb.clingupnp

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import com.hezb.clingupnp.server.NanoHttpServer

/**
 * Project Name: HPlayer
 * File Name:    HttpServerService
 *
 * Description: http服务器本地服务.
 *
 * @author  hezhubo
 * @date    2022年03月05日 18:24
 */
class HttpServerService : Service() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "http_server"

        const val SERVER_PORT = 8086

        fun getLocalIpByWifi(context: Context?): String? {
            context?.let {
                (it.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.let { wifiManager ->
                    wifiManager.connectionInfo?.ipAddress?.let { ipAddress ->
                        return String.format(
                            "%d.%d.%d.%d",
                            ipAddress and 0xff,
                            ipAddress shr 8 and 0xff,
                            ipAddress shr 16 and 0xff,
                            ipAddress shr 24 and 0xff
                        )
                    }
                }
            }
            return null
        }

        /**
         * 此处简单的使用静态方法返回本地视频共享地址
         */
        fun getVideoUrl(context: Context, videoId: String?): String {
            return getVideoUrl(getLocalIpByWifi(context), videoId)
        }

        fun getVideoUrl(ip: String?, videoId: String?): String {
            return "http://${ip}:${SERVER_PORT}${NanoHttpServer.SESSION_URI_VIDEO}?id=${videoId}"
        }

        fun getAudioUrl(ip: String?, audioId: String?): String {
            return "http://${ip}:${SERVER_PORT}${NanoHttpServer.SESSION_URI_AUDIO}?id=${audioId}"
        }

        fun getImageUrl(ip: String?, imageId: String?): String {
            return "http://${ip}:${SERVER_PORT}${NanoHttpServer.SESSION_URI_IMAGE}?id=${imageId}"
        }
    }

    private var mNanoHttpServer: NanoHttpServer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null // 通过绑定服务以获取媒体文件共享url
    }

    override fun onCreate() {
        super.onCreate()
        showForeground()

        mNanoHttpServer = NanoHttpServer(this, SERVER_PORT).also {
            it.start()
        }
    }

    private fun showForeground() {
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        // 点击回到应用的intent
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?.setPackage(null)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
        }
        val notification = builder
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle("HTTP SERVER")
            .setContentText("投屏服务")
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .notification
        startForeground(6666, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)

        mNanoHttpServer?.let {
            it.stop()
            it.release()
        }
    }

}