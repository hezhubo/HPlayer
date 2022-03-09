package com.hezb.clingupnp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.hezb.clingupnp.util.UpnpLog
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry

/**
 * Project Name: HPlayer
 * File Name:    UpnpServiceHelper
 *
 * Description: Upnp服务辅助工具.
 *
 * @author  hezhubo
 * @date    2022年03月04日 09:56
 */
class UpnpServiceHelper(context: Context, callback: UpnpServiceCallback) {

    private var mContext: Context? = context
    private var mUpnpServiceCallback: UpnpServiceCallback? = callback

    private var hadBindService = false
    private var mAndroidUpnpService: AndroidUpnpService? = null

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            UpnpLog.d("upnp service connected!")
            (service as? AndroidUpnpService)?.apply {
                registry.addListener(mRegistryListener)
                for (device in registry.devices) {
                    mRegistryListener.deviceAdded(registry, device)
                }
                controlPoint.search() // 执行设备搜索
                mAndroidUpnpService = this
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            UpnpLog.d("upnp service disconnected!")
            mAndroidUpnpService?.apply {
                registry.removeListener(mRegistryListener)
                registry.shutdown()
            }
            mAndroidUpnpService = null
        }

    }

    private val mRegistryListener = object : DefaultRegistryListener() {

        override fun deviceAdded(
            registry: Registry,
            device: Device<*, out Device<*, *, *>, out Service<*, *>>
        ) {
            mUpnpServiceCallback?.deviceAdded(device)
        }

        override fun deviceRemoved(
            registry: Registry,
            device: Device<*, out Device<*, *, *>, out Service<*, *>>
        ) {
            mUpnpServiceCallback?.deviceRemoved(device)
        }

    }

    fun getUpnpService(): AndroidUpnpService? {
        return mAndroidUpnpService
    }

    /**
     * 启动并绑定服务
     */
    fun bindUpnpService() {
        mContext?.let { context ->
            hadBindService = context.bindService(
                Intent(context, UpnpService::class.java),
                mServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    /**
     * 解绑服务
     */
    fun unBindUpnpService() {
        if (hadBindService) {
            mContext?.unbindService(mServiceConnection)
            hadBindService = false
        }
    }

    /**
     * 暂停设备发现
     */
    fun pauseUpnpSearch() {
        mAndroidUpnpService?.registry?.pause()
    }

    /**
     * 恢复设备发现
     */
    fun resumeUpnpSearch() {
        mAndroidUpnpService?.registry?.resume()
    }

    /**
     * 释放引用
     */
    fun release() {
        mContext = null
        mUpnpServiceCallback = null
    }

    interface UpnpServiceCallback {

        /**
         * 设备添加
         * 非UI线程
         */
        fun deviceAdded(device: Device<*, out Device<*, *, *>, out Service<*, *>>)

        /**
         * 设备移除
         * 非UI线程
         */
        fun deviceRemoved(device: Device<*, out Device<*, *, *>, out Service<*, *>>)

    }

}