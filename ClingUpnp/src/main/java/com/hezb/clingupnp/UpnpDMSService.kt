package com.hezb.clingupnp

import com.hezb.clingupnp.dms.ContentDirectoryService
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.LocalService
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
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

    override fun onCreate() {
        super.onCreate()

        addLocalDevice()

    }

    /**
     * 添加本地媒体服务器设备
     */
    private fun addLocalDevice() {
        // 媒体内容目录服务
        val contentDirectoryService: LocalService<AbstractContentDirectoryService> =
            AnnotationLocalServiceBinder().read(AbstractContentDirectoryService::class.java) as LocalService<AbstractContentDirectoryService>
        val contentDirectoryManger = object :
            DefaultServiceManager<AbstractContentDirectoryService>(contentDirectoryService) {
            override fun createServiceInstance(): AbstractContentDirectoryService {
                return ContentDirectoryService(this@UpnpDMSService)
            }
        }
        contentDirectoryService.manager = contentDirectoryManger

        // TODO 添加  AVTransportService  AudioRenderingControl

        val deviceIdentity = DeviceIdentity(UDN(UUID.randomUUID()))
        val deviceType = UDADeviceType("HPlayer-MediaServer")
        val deviceDetails = DeviceDetails("HPlayer-MediaServer(DMS)-${android.os.Build.MODEL}")
        val localDevice = LocalDevice(
            deviceIdentity,
            deviceType,
            deviceDetails,
            arrayOf<LocalService<*>>(contentDirectoryService)
        )
        upnpService.registry.addDevice(localDevice)
    }

}