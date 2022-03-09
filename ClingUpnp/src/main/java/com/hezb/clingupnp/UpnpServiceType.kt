package com.hezb.clingupnp

/**
 * Project Name: HPlayer
 * File Name:    UpnpServiceType
 *
 * Description: Upnp服务类型.
 *
 * @author  hezhubo
 * @date    2022年03月04日 09:36
 */
object UpnpServiceType {

    /** 传输服务：提供媒体文件传输，播放控制等功能 */
    const val AV_TRANSPORT = "AVTransport"

    /** 渲染控制：用于播放时的一些渲染控制，如调节音量、亮度等 */
    const val RENDERING_CONTROL = "RenderingControl"

    /** 内容目录：用于提供媒体文件浏览、检索、获取媒体文件信息等功能 */
    const val CONTENT_DIRECTORY = "ContentDirectory"

}