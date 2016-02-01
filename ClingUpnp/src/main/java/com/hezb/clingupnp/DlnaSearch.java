package com.hezb.clingupnp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.hezb.clingupnp.dmc.UpnpControlSet;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import de.greenrobot.event.EventBus;

/**
 * 本类从activity中抽离 用于发现DLNA设备
 *
 * @author hezb
 */
public class DlnaSearch {

    private static final String TAG = "DlnaSearch";

    private boolean hadBindService = false;
    private Context mContext;

    private AndroidUpnpService mUpnpService;
    private DefaultRegistryListener mDefaultRegistryListener = new DefaultRegistryListener() {

        @Override
        public void deviceAdded(Registry registry, Device device) {
            DlnaDeviceInfo dlnaDeviceInfo = new DlnaDeviceInfo(device, true);
            EventBus.getDefault().post(dlnaDeviceInfo);
        }

        @Override
        public void deviceRemoved(Registry registry, Device device) {
            DlnaDeviceInfo dlnaDeviceInfo = new DlnaDeviceInfo(device, false);
            EventBus.getDefault().post(dlnaDeviceInfo);
        }
    };


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "DLNA-----DlnaAndRemoteSearch---onServiceConnected");
            mUpnpService = (AndroidUpnpService) service;
            mUpnpService.getControlPoint().getRegistry().removeAllRemoteDevices();// 先清除掉之前的，再搜索
            mUpnpService.getRegistry().addListener(mDefaultRegistryListener);
            mUpnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "DLNA-----DlnaAndRemoteSearch---onServiceDisconnected");
        }
    };

    /**
     * 初始化
     */
    public DlnaSearch(Context context) {
        this.mContext = context;
    }


    /**
     * 开始搜索 设备(提供 AVTransport 及 RenderingControl 服务)
     */
    public void startSearchDMC() {
        hadBindService = mContext.bindService(new Intent(mContext, com.hezb.clingupnp.dmc.UpnpService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 开始搜索 设备(提供 ContentDirectory 服务)
     */
    public void startSearchDMP() {
        hadBindService = mContext.bindService(new Intent(mContext, com.hezb.clingupnp.dmp.UpnpService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 暂停发现设备
     */
    public void pauseSearch() {
        if (mUpnpService != null) {
            mUpnpService.getRegistry().pause();
        }
    }

    /**
     * 恢复发现
     */
    public void resumeSearch() {
        if (mUpnpService != null) {
            mUpnpService.getRegistry().resume();
        }
    }

    /**
     * 解绑服务
     */
    public void unBindService() {
        if (hadBindService) {
            if (mUpnpService != null) {
                mUpnpService.getRegistry().removeListener(mDefaultRegistryListener);
            }
            mContext.unbindService(mServiceConnection);
            mUpnpService = null;
            hadBindService = false;
        }
    }

    /**
     * 根据选择的设备 返回DLNA操作集合
     */
    public UpnpControlSet createUpnpControlSet(Device device) {
        if (!hadBindService || mUpnpService == null) {
            Log.e(TAG, "had no BindService or mUpnpService is null");
            return null;
        }

        UDAServiceType avTransportServiceType =
                new UDAServiceType(UpnpServiceType.AVTRANSPORT);
        Service avTransportService = device.findService(avTransportServiceType);
        if (avTransportService == null) {
            Log.e(TAG, "avTransportService is null");
            return null;
        }

        UDAServiceType renderingControlServiceType =
                new UDAServiceType(UpnpServiceType.RENDERING_CONTROL);
        Service renderingControlService =
                device.findService(renderingControlServiceType);
        if (renderingControlService == null) {
            Log.e(TAG, "renderingControlService is null");
            return null;
        }

        UpnpControlSet upnpControlSet = new UpnpControlSet(mUpnpService,
                avTransportService, renderingControlService);
        return upnpControlSet;
    }

    /**
     * @return upnpIBinder
     */
    public AndroidUpnpService getUpnpService() {
        return mUpnpService;
    }
}
