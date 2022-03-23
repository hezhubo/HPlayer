package com.hezb.clingupnp

import android.app.Activity
import com.hezb.clingupnp.dms.AVTransportService
import com.hezb.clingupnp.dms.AudioRenderingControl
import com.hezb.clingupnp.dms.ContentDirectoryService
import com.hezb.player.core.AbstractMediaPlayer
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.LocalService
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
import java.lang.ref.WeakReference
import java.util.*


/**
 * Project Name: HPlayer
 * File Name:    UpnpDMSService
 *
 * Description: DLNA数字媒体服务器(DMS).
 *
 * @author  hezhubo
 * @date    2022年03月06日 02:13
 */
class UpnpDMSService : UpnpService() {

    companion object {
        private var sMediaPlayer: AbstractMediaPlayer? = null

        fun setMediaPlayer(mp: AbstractMediaPlayer?) {
            sMediaPlayer?.let {
                if (it != mp) {
                    if (it.isPlaying()) {
                        it.stop()
                    }
                    it.release()
                    it.removePlayerCallback()
                }
            }
            sMediaPlayer = mp
        }

        fun getMediaPlayer(): AbstractMediaPlayer? {
            return sMediaPlayer
        }

        private var activityReference: WeakReference<Activity>? = null

        fun register(activity: Activity) {
            unregister()
            activityReference = WeakReference(activity)
        }

        fun unregister() {
            activityReference?.clear()
            activityReference = null
        }

        fun getPlayerActivity(): Activity? {
            return activityReference?.get()
        }

    }

    override fun onCreate() {
        super.onCreate()

        addLocalDevice()

    }

    /**
     * 添加本地媒体服务器设备
     */
    private fun addLocalDevice() {
        val binder = AnnotationLocalServiceBinder()
        // 媒体内容目录服务
        val contentDirectoryService: LocalService<AbstractContentDirectoryService> =
            binder.read(AbstractContentDirectoryService::class.java) as LocalService<AbstractContentDirectoryService>
        val contentDirectoryManager = object :
            DefaultServiceManager<AbstractContentDirectoryService>(contentDirectoryService) {
            override fun createServiceInstance(): AbstractContentDirectoryService {
                return ContentDirectoryService(this@UpnpDMSService)
            }
        }
        contentDirectoryService.manager = contentDirectoryManager

        // 播放服务
        val avTransportService: LocalService<AbstractAVTransportService> =
            binder.read(AbstractAVTransportService::class.java) as LocalService<AbstractAVTransportService>
        val avTransportManager = object : DefaultServiceManager<AbstractAVTransportService>(avTransportService) {
            override fun createServiceInstance(): AbstractAVTransportService {
                return AVTransportService(this@UpnpDMSService)
            }
        }
        avTransportService.manager = avTransportManager

        // 音频控制服务
        val audioRenderingControl: LocalService<AbstractAudioRenderingControl> =
            binder.read(AbstractAudioRenderingControl::class.java) as LocalService<AbstractAudioRenderingControl>
        val audioRenderingControlManager = object : DefaultServiceManager<AbstractAudioRenderingControl>(audioRenderingControl) {
            override fun createServiceInstance(): AbstractAudioRenderingControl {
                return AudioRenderingControl(this@UpnpDMSService)
            }
        }
        audioRenderingControl.manager = audioRenderingControlManager

        val deviceIdentity = DeviceIdentity(UDN(UUID.randomUUID()))
        val deviceType = UDADeviceType("HPlayer-MediaServer")
        val deviceDetails = DeviceDetails("HPlayer-MediaServer(DMS)-${android.os.Build.MODEL}")
        val localDevice = LocalDevice(
            deviceIdentity,
            deviceType,
            deviceDetails,
            arrayOf<LocalService<*>>(contentDirectoryService, avTransportService, audioRenderingControl)
        )
        upnpService.registry.addDevice(localDevice)
    }

}