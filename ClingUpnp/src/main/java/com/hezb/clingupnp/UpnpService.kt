package com.hezb.clingupnp

import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDAServiceType

/**
 * Project Name: HPlayer
 * File Name:    UpnpService
 *
 * Description: Upnp服务.
 *
 * @author  hezhubo
 * @date    2022年03月04日 09:45
 */
open class UpnpService : AndroidUpnpServiceImpl() {

    override fun createConfiguration(): UpnpServiceConfiguration {
        return MyConfiguration()
    }

    /**
     * Upnp服务配置
     */
    class MyConfiguration : AndroidUpnpServiceConfiguration() {

        override fun getRegistryMaintenanceIntervalMillis(): Int {
            return 7000
        }

        /**
         * 配置搜索的upnp服务器类型
         * 一般客户端app(视频类)来说，只要搜索支持 AVTransport 和 RenderingControl 的即可
         * 若要做浏览服务器共享文件的app，只需查找支持 ContentDirectory 的服务器
         */
        override fun getExclusiveServiceTypes(): Array<ServiceType> {
            return arrayOf(
                UDAServiceType(UpnpServiceType.AV_TRANSPORT),
                UDAServiceType(UpnpServiceType.RENDERING_CONTROL),
                UDAServiceType(UpnpServiceType.CONTENT_DIRECTORY)
            )
        }

        override fun createServiceDescriptorBinderUDA10(): ServiceDescriptorBinder {
            // issues https://github.com/4thline/cling/issues/249
            return UDA10ServiceDescriptorBinderImpl()
        }

    }

}