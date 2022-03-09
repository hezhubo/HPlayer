package com.hezb.clingupnp.dms

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.model.*

/**
 * Project Name: HPlayer
 * File Name:    AVTransportService
 *
 * Description: TODO.
 *
 * @author  hezhubo
 * @date    2022年03月08日 00:09
 */
class AVTransportService : AbstractAVTransportService() {

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> {
        return arrayOf()
    }

    override fun setAVTransportURI(instanceId: UnsignedIntegerFourBytes?, currentURI: String?, currentURIMetaData: String?) {
    }

    override fun setNextAVTransportURI(instanceId: UnsignedIntegerFourBytes?, nextURI: String?, nextURIMetaData: String?) {
    }

    override fun getMediaInfo(instanceId: UnsignedIntegerFourBytes?): MediaInfo {
        return MediaInfo()
    }

    override fun getTransportInfo(instanceId: UnsignedIntegerFourBytes?): TransportInfo {
        return TransportInfo()
    }

    override fun getPositionInfo(instanceId: UnsignedIntegerFourBytes?): PositionInfo {
        return PositionInfo()
    }

    override fun getDeviceCapabilities(instanceId: UnsignedIntegerFourBytes?): DeviceCapabilities {
        return DeviceCapabilities(arrayOf())
    }

    override fun getTransportSettings(instanceId: UnsignedIntegerFourBytes?): TransportSettings {
        return TransportSettings()
    }

    override fun stop(instanceId: UnsignedIntegerFourBytes?) {
    }

    override fun play(instanceId: UnsignedIntegerFourBytes?, speed: String?) {
    }

    override fun pause(instanceId: UnsignedIntegerFourBytes?) {
    }

    override fun record(instanceId: UnsignedIntegerFourBytes?) {
    }

    override fun seek(instanceId: UnsignedIntegerFourBytes?, unit: String?, target: String?) {
    }

    override fun next(instanceId: UnsignedIntegerFourBytes?) {
    }

    override fun previous(instanceId: UnsignedIntegerFourBytes?) {
    }

    override fun setPlayMode(instanceId: UnsignedIntegerFourBytes?, newPlayMode: String?) {
    }

    override fun setRecordQualityMode(instanceId: UnsignedIntegerFourBytes?, newRecordQualityMode: String?) {
    }

    override fun getCurrentTransportActions(instanceId: UnsignedIntegerFourBytes?): Array<TransportAction> {
        return arrayOf()
    }

}