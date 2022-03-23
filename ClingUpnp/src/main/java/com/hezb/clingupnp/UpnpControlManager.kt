package com.hezb.clingupnp

import com.hezb.clingupnp.util.FormatUtil
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.avtransport.callback.*
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume
import org.fourthline.cling.support.renderingcontrol.callback.SetMute
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume

/**
 * Project Name: HPlayer
 * File Name:    UpnpDeviceActionManager
 *
 * Description: 统一管理设备行为事件.
 *
 * @author  hezhubo
 * @date    2022年03月04日 17:18
 */
class UpnpControlManager(
    private val controlPoint: ControlPoint,
    device: Device<*, out Device<*, *, *>, out Service<*, *>>
) {

    var upnpActionCallback: UpnpActionCallback? = null

    private val mAVTransportService: Service<*, *>? =
        device.findService(UDAServiceType(UpnpServiceType.AV_TRANSPORT))

    private val mRenderingControlService: Service<*, *>? =
        device.findService(UDAServiceType(UpnpServiceType.RENDERING_CONTROL))

    private val mContentDirectoryService: Service<*, *>? =
        device.findService(UDAServiceType(UpnpServiceType.CONTENT_DIRECTORY))

    private val getVolume: GetVolume? by lazy {
        return@lazy if (mRenderingControlService == null) {
            null
        } else {
            object : GetVolume(mRenderingControlService) {
                override fun received(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    currentVolume: Int
                ) {
                    upnpActionCallback?.getVolumeReceived(currentVolume)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_GET_VOLUME, defaultMsg)
                }
            }
        }
    }

    private val getTransportInfo: GetTransportInfo? by lazy {
        return@lazy if (mAVTransportService == null) {
            null
        } else {
            object : GetTransportInfo(mAVTransportService) {
                override fun received(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    transportInfo: TransportInfo
                ) {
                    val isPlaying = transportInfo.currentTransportState == TransportState.PLAYING
                    upnpActionCallback?.getTransportReceived(isPlaying)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_GET_TRANSPORT, defaultMsg)
                }
            }
        }
    }

    private val getPositionInfo: GetPositionInfo? by lazy {
        return@lazy if (mAVTransportService == null) {
            null
        } else {
            object : GetPositionInfo(mAVTransportService) {
                override fun received(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    positionInfo: PositionInfo
                ) {
                    val currentPosition = FormatUtil.transformTime(positionInfo.relTime)
                    val duration = FormatUtil.transformTime(positionInfo.trackDuration)
                    upnpActionCallback?.getPositionReceived(currentPosition, duration)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_GET_POSITION, defaultMsg)
                }
            }
        }
    }

    private val play: Play? by lazy {
        return@lazy if (mAVTransportService == null) {
            null
        } else {
            object : Play(mAVTransportService) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.onSuccess(ACTION_TYPE_PLAY)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_PLAY, defaultMsg)
                }
            }
        }
    }

    private val pause: Pause? by lazy {
        return@lazy if (mAVTransportService == null) {
            null
        } else {
            object : Pause(mAVTransportService) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.onSuccess(ACTION_TYPE_PAUSE)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_PAUSE, defaultMsg)
                }
            }
        }
    }

    private val stop: Stop? by lazy {
        return@lazy if (mAVTransportService == null) {
            null
        } else {
            object : Stop(mAVTransportService) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.onSuccess(ACTION_TYPE_STOP)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_STOP, defaultMsg)
                }
            }
        }
    }

    /**
     * 设置音量
     *
     * @param volume
     */
    fun setVolume(volume: Int) {
        mRenderingControlService?.let {
            controlPoint.execute(object : SetVolume(it, volume.toLong()) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.onSuccess(ACTION_TYPE_SET_VOLUME)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_SET_VOLUME, defaultMsg)
                }
            })
        }
    }

    /**
     * 设置静音
     *
     * @param isMute
     */
    fun setMute(isMute: Boolean) {
        mRenderingControlService?.let {
            controlPoint.execute(object : SetMute(it, isMute) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.setMuteSuccess(isMute)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_SET_MUTE, defaultMsg)
                }
            })
        }
    }

    /**
     * 获取音量
     */
    fun getVolume() {
        getVolume?.let {
            controlPoint.execute(it)
        }
    }

    /**
     * 投屏
     *
     * @param uri
     */
    fun setAVTransportURI(uri: String) {
        mAVTransportService?.let {
            controlPoint.execute(object : SetAVTransportURI(it, uri) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.onSuccess(ACTION_TYPE_SET_AV_TRANSPORT)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_SET_AV_TRANSPORT, defaultMsg)
                }
            })
        }
    }

    /**
     * 获取远程渲染器状态
     */
    fun getTransportInfo() {
        getTransportInfo?.let {
            controlPoint.execute(it)
        }
    }

    /**
     * 获取播放位置信息，时长
     */
    fun getPositionInfo() {
        getPositionInfo?.let {
            controlPoint.execute(it)
        }
    }

    /**
     * 播放
     */
    fun play() {
        play?.let {
            controlPoint.execute(it)
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        pause?.let {
            controlPoint.execute(it)
        }
    }

    /**
     * 跳转
     *
     * @param position
     */
    fun seek(position: Long) {
        mAVTransportService?.let {
            val relativeTimeTarget = FormatUtil.formatTime(position, true)
            controlPoint.execute(object : Seek(it, relativeTimeTarget) {
                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    upnpActionCallback?.onSuccess(ACTION_TYPE_SEEK)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_SEEK, defaultMsg)
                }
            })
        }
    }

    /**
     * 停止
     */
    fun stop() {
        stop?.let {
            controlPoint.execute(it)
        }
    }

    // --------------------------------------------

    /**
     * 获取共享目录文件
     *
     * @param id
     * @param flag
     */
    fun browse(id: String?, flag: BrowseFlag) {
        mContentDirectoryService?.let {
            controlPoint.execute(object : AndroidBrowse(it, id, flag) {
                override fun received(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    didlContent: DIDLContent
                ) {
                    upnpActionCallback?.browseReceived(didlContent)
                }

                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    upnpActionCallback?.onFailure(ACTION_TYPE_BROWSE, defaultMsg)
                }

                override fun updateStatus(status: Status) {
                    upnpActionCallback?.browseUpdateStatus(status)
                }
            })
        }
    }

    // --------------------------------------------

    companion object {
        /** 静音 */
        const val ACTION_TYPE_SET_MUTE = 0

        /** 获取音量 */
        const val ACTION_TYPE_GET_VOLUME = 1

        /** 设置音量 */
        const val ACTION_TYPE_SET_VOLUME = 2

        /** 设置传输媒体 */
        const val ACTION_TYPE_SET_AV_TRANSPORT = 3

        /** 获取时长 */
        const val ACTION_TYPE_GET_POSITION = 4

        /** 获取播放状态 */
        const val ACTION_TYPE_GET_TRANSPORT = 5

        /** 播放 */
        const val ACTION_TYPE_PLAY = 6

        /** 暂停 */
        const val ACTION_TYPE_PAUSE = 7

        /** 跳转 */
        const val ACTION_TYPE_SEEK = 8

        /** 停止 */
        const val ACTION_TYPE_STOP = 9

        /** 浏览共享文件 */
        const val ACTION_TYPE_BROWSE = 10
    }

    abstract class UpnpActionCallback {
        /**
         * 成功
         *
         * @param type
         */
        open fun onSuccess(type: Int) {}

        /**
         * 出错
         *
         * @param type
         * @param error
         */
        open fun onFailure(type: Int, error: String?) {}

        /**
         * 获取音量成功
         *
         * @param volume 音量值
         */
        open fun getVolumeReceived(volume: Int) {}

        /**
         * 设置静音/非静音成功
         *
         * @param isMute 是否静音
         */
        open fun setMuteSuccess(isMute: Boolean) {}

        /**
         * 获取当前媒体状态成功
         *
         * @param isPlaying 是否播放中
         */
        open fun getTransportReceived(isPlaying: Boolean) {}

        /**
         * 获取当前媒体信息成功
         *
         * @param currentPosition 当前进度
         * @param duration 总时长
         */
        open fun getPositionReceived(currentPosition: Long, duration: Long) {}

        /**
         * 获取共享目录文件成功
         *
         * @param didlContent
         */
        open fun browseReceived(didlContent: DIDLContent) {}

        /**
         * browse action 状态更新
         *
         * @param status
         */
        open fun browseUpdateStatus(status: Browse.Status) {}
    }

}