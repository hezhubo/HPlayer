package com.hezb.clingupnp.dms

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl

/**
 * Project Name: HPlayer
 * File Name:    AudioRenderingControl
 *
 * Description: TODO.
 *
 * @author  hezhubo
 * @date    2022年03月08日 00:10
 */
class AudioRenderingControl : AbstractAudioRenderingControl() {

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> {
        return arrayOf()
    }

    override fun getMute(instanceId: UnsignedIntegerFourBytes?, channelName: String?): Boolean {
        return false
    }

    override fun setMute(instanceId: UnsignedIntegerFourBytes?, channelName: String?, desiredMute: Boolean) {
    }

    override fun getVolume(instanceId: UnsignedIntegerFourBytes?, channelName: String?): UnsignedIntegerTwoBytes {
        return UnsignedIntegerTwoBytes(0)
    }

    override fun setVolume(
        instanceId: UnsignedIntegerFourBytes?,
        channelName: String?,
        desiredVolume: UnsignedIntegerTwoBytes?
    ) {
    }

    override fun getCurrentChannels(): Array<Channel> {
        return arrayOf()
    }

}