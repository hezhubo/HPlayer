package com.hezb.clingupnp.dms;

import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

/**
 * TODO 实现相应功能
 * Created by hezb on 2016/1/28.
 */
public class AVTransportService extends AbstractAVTransportService {


    @Override
    public void setAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "CurrentURI", stateVariable = "AVTransportURI") String s, @UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData") String s1) throws AVTransportException {

    }

    @Override
    public void setNextAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "NextURI", stateVariable = "AVTransportURI") String s, @UpnpInputArgument(name = "NextURIMetaData", stateVariable = "AVTransportURIMetaData") String s1) throws AVTransportException {

    }

    @Override
    public MediaInfo getMediaInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public TransportInfo getTransportInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public PositionInfo getPositionInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public TransportSettings getTransportSettings(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public void stop(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void play(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String s) throws AVTransportException {

    }

    @Override
    public void pause(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void record(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void seek(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String s, @UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String s1) throws AVTransportException {

    }

    @Override
    public void next(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void previous(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void setPlayMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String s) throws AVTransportException {

    }

    @Override
    public void setRecordQualityMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String s) throws AVTransportException {

    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws Exception {
        return new TransportAction[0];
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[0];
    }
}
