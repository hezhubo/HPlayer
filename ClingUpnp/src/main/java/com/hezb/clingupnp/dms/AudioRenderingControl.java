package com.hezb.clingupnp.dms;

import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

/**
 * TODO 实现相应功能
 * Created by hezb on 2016/1/28.
 */
public class AudioRenderingControl extends AbstractAudioRenderingControl {

    @Override
    public boolean getMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String s) throws RenderingControlException {
        return false;
    }

    @Override
    public void setMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String s, @UpnpInputArgument(name = "DesiredMute", stateVariable = "Mute") boolean b) throws RenderingControlException {

    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String s) throws RenderingControlException {
        return null;
    }

    @Override
    public void setVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String s, @UpnpInputArgument(name = "DesiredVolume", stateVariable = "Volume") UnsignedIntegerTwoBytes unsignedIntegerTwoBytes) throws RenderingControlException {

    }

    @Override
    protected Channel[] getCurrentChannels() {
        return new Channel[0];
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[0];
    }
}
