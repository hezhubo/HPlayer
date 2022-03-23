package com.hezb.clingupnp.dms

import android.app.Service
import android.content.Context
import android.media.AudioManager
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser
import org.xml.sax.XMLReader
import javax.xml.parsers.SAXParserFactory

/**
 * Project Name: HPlayer
 * File Name:    AudioRenderingControl
 *
 * Description: 音频控制服务.
 *
 * @author  hezhubo
 * @date    2022年03月08日 00:10
 */
class AudioRenderingControl(context: Context) :
    AbstractAudioRenderingControl(LastChange(object : RenderingControlLastChangeParser() {
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

    private val audioManager =
        context.applicationContext.getSystemService(Service.AUDIO_SERVICE) as AudioManager

    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    private var volume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> {
        return arrayOf(UnsignedIntegerFourBytes(0))
    }

    override fun getMute(instanceId: UnsignedIntegerFourBytes?, channelName: String?): Boolean {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }

    override fun setMute(
        instanceId: UnsignedIntegerFourBytes?,
        channelName: String?,
        desiredMute: Boolean
    ) {
        if (desiredMute) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        }
    }

    override fun getVolume(
        instanceId: UnsignedIntegerFourBytes?,
        channelName: String?
    ): UnsignedIntegerTwoBytes {
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return UnsignedIntegerTwoBytes(volume.toLong())
    }

    override fun setVolume(
        instanceId: UnsignedIntegerFourBytes?,
        channelName: String?,
        desiredVolume: UnsignedIntegerTwoBytes?
    ) {
        volume = desiredVolume?.value?.toInt() ?: 0
        if (volume < 0) {
            volume = 0
        } else if (volume > maxVolume) {
            volume = maxVolume
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    override fun getCurrentChannels(): Array<Channel> {
        return arrayOf(Channel.Master)
    }

}