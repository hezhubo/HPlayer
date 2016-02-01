package com.hezb.clingupnp;

import org.fourthline.cling.model.meta.Device;

import java.io.Serializable;

/**
 * DLNA设备信息
 * Created by hezb on 2016/1/26.
 */
public class DlnaDeviceInfo implements Serializable {

    private boolean isAdd;
    private Device device;

    public DlnaDeviceInfo(Device device, boolean isAdd) {
        this.device = device;
        this.isAdd = isAdd;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
