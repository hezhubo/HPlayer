package com.hezb.clingupnp.dms

import android.content.Context
import android.content.Intent
import com.hezb.clingupnp.UpnpDMSService
import com.hezb.clingupnp.util.FormatUtil
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.*
import org.xml.sax.XMLReader
import javax.xml.parsers.SAXParserFactory

/**
 * Project Name: HPlayer
 * File Name:    AVTransportService
 *
 * Description: 媒体播放服务.
 *
 * @author  hezhubo
 * @date    2022年03月08日 00:09
 */
class AVTransportService(private val context: Context) :
    AbstractAVTransportService(LastChange(object :
        AVTransportLastChangeParser() {
        override fun create(): XMLReader {
            return try {
                val factory = SAXParserFactory.newInstance()

                // Configure factory to prevent XXE attacks
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                // factory.setXIncludeAware(false);
                // factory.setNamespaceAware(true);
                if (schemaSources != null) {
                    factory.schema = createSchema(schemaSources)
                }
                val xmlReader = factory.newSAXParser().xmlReader
                xmlReader.errorHandler = errorHandler
                xmlReader
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    })) {

    private var currentInstanceId: UnsignedIntegerFourBytes? = null // TODO 通过InstanceId处理指定播放器

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> {
        return arrayOf(currentInstanceId ?: UnsignedIntegerFourBytes(0))
    }

    override fun setAVTransportURI(
        instanceId: UnsignedIntegerFourBytes?,
        currentURI: String?,
        currentURIMetaData: String?
    ) {
        currentInstanceId = instanceId
        // 打开播放页
        context.startActivity(Intent(context, TransportPlayerActivity::class.java).apply {
            putExtra(
                "video_path",
                currentURI
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun setNextAVTransportURI(
        instanceId: UnsignedIntegerFourBytes?,
        nextURI: String?,
        nextURIMetaData: String?
    ) {
        // 播放完成后自动播放下一个
    }

    override fun getMediaInfo(instanceId: UnsignedIntegerFourBytes?): MediaInfo {
        UpnpDMSService.getMediaPlayer()?.let { player ->
            val currentURI = player.getPlayUri()?.toString() ?: ""
            return MediaInfo(currentURI, "")
        }
        return MediaInfo()
    }

    override fun getTransportInfo(instanceId: UnsignedIntegerFourBytes?): TransportInfo {
        return TransportInfo()
    }

    override fun getPositionInfo(instanceId: UnsignedIntegerFourBytes?): PositionInfo {
        val positionInfo = PositionInfo()
        UpnpDMSService.getMediaPlayer()?.let { player ->
            if (player.isInPlaybackState()) {
                positionInfo.trackDuration = FormatUtil.formatTime(player.getDuration())
                positionInfo.relTime = FormatUtil.formatTime(player.getCurrentPosition())
            }
        }
        return positionInfo
    }

    override fun getDeviceCapabilities(instanceId: UnsignedIntegerFourBytes?): DeviceCapabilities {
        return DeviceCapabilities(arrayOf(StorageMedium.NETWORK))
    }

    override fun getTransportSettings(instanceId: UnsignedIntegerFourBytes?): TransportSettings {
        return TransportSettings(PlayMode.NORMAL)
    }

    override fun stop(instanceId: UnsignedIntegerFourBytes?) {
        UpnpDMSService.getPlayerActivity()?.finish()
    }

    override fun play(instanceId: UnsignedIntegerFourBytes?, speed: String?) {
        UpnpDMSService.getMediaPlayer()?.start()
    }

    override fun pause(instanceId: UnsignedIntegerFourBytes?) {
        UpnpDMSService.getMediaPlayer()?.pause()
    }

    override fun record(instanceId: UnsignedIntegerFourBytes?) {
        // 录制
    }

    override fun seek(instanceId: UnsignedIntegerFourBytes?, unit: String?, target: String?) {
        UpnpDMSService.getMediaPlayer()?.let { player ->
            try {
                val seekMode = SeekMode.valueOrExceptionOf(unit) // seek的模式
                if (seekMode == SeekMode.REL_TIME) {
                    val time = FormatUtil.transformTime(target)
                    player.seekTo(time)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun next(instanceId: UnsignedIntegerFourBytes?) {
        // 播放下一个
    }

    override fun previous(instanceId: UnsignedIntegerFourBytes?) {
        // 播放上一个
    }

    override fun setPlayMode(instanceId: UnsignedIntegerFourBytes?, newPlayMode: String?) {
        // 设置播放模式  @see PlayMode
    }

    override fun setRecordQualityMode(
        instanceId: UnsignedIntegerFourBytes?,
        newRecordQualityMode: String?
    ) {
        // 设置录制质量  @see RecordQualityMode
    }

    override fun getCurrentTransportActions(instanceId: UnsignedIntegerFourBytes?): Array<TransportAction> {
        // 获取播放器状态
        return when (TransportInfo().currentTransportState) {
            TransportState.STOPPED -> {
                arrayOf(TransportAction.Play)
            }
            TransportState.PLAYING -> {
                arrayOf(TransportAction.Stop, TransportAction.Pause, TransportAction.Seek)
            }
            TransportState.PAUSED_PLAYBACK -> {
                arrayOf(
                    TransportAction.Stop,
                    TransportAction.Pause,
                    TransportAction.Seek,
                    TransportAction.Play
                )
            }
            else -> {
                arrayOf()
            }
        }
    }

}