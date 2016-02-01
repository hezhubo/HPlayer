package com.hezb.clingupnp.dms;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * 媒体服务
 * 鸡肋。。。
 * Created by hezb on 2016/1/28.
 */
public class MediaServer {

    private static final String TAG = "MediaServer";

    private final static String deviceType = "MediaServer";
    private final static int version = 1;
    private final static int port = 8765;

    private UDN udn;
    private LocalDevice localDevice;

    private static InetAddress localAddress;
    private NanoHttpServer nanoHttpServer;
    private GenerateContentTask generateContentTask;

    public MediaServer(final Context context) throws ValidationException, UnknownHostException {

        localAddress = getWIFIIpAddress(context);

        generateContentTask = new GenerateContentTask();
        generateContentTask.execute(context);

        udn = new UDN(UUID.randomUUID());
        DeviceType type = new UDADeviceType(deviceType, version);

        DeviceDetails details = new DeviceDetails(android.os.Build.MODEL,
                new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails("HPlayer", "HPlayer MediaServer for Android", "v1"));

        LocalServiceBinder binder = new AnnotationLocalServiceBinder();

        // 文件共享服务
        LocalService<ContentDirectoryService> contentDirectoryService
                = binder.read(ContentDirectoryService.class);
        ServiceManager<ContentDirectoryService> contentDirectoryManger
                = new DefaultServiceManager<ContentDirectoryService>(contentDirectoryService) {
            @Override
            protected ContentDirectoryService createServiceInstance() throws Exception {
                return new ContentDirectoryService();
            }
        };
        contentDirectoryService.setManager(contentDirectoryManger);

        // 连接管理服务
        LocalService<ConnectionManagerService> connectionManagerService
                = binder.read(ConnectionManagerService.class);
        connectionManagerService.setManager(new DefaultServiceManager<>(
                connectionManagerService, ConnectionManagerService.class));

        // TODO 添加 AVTransportService
        // TODO 添加 AudioRenderingControl

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details,
                new LocalService[]{contentDirectoryService, connectionManagerService});

        // 启动服务器
        try {
            nanoHttpServer = new NanoHttpServer(port);
            nanoHttpServer.start();
            Log.d(TAG, "Started Http Server on port " + port);
        } catch (IOException e) {
            Log.d(TAG, "Started Http Server on error:" + e.getMessage());
            Toast.makeText(context, "启动服务器失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public LocalDevice getDevice() {
        return localDevice;
    }

    public void release() {
        if (nanoHttpServer != null) {
            nanoHttpServer.stop();
        }
        localAddress = null;
        if (generateContentTask != null) {
            generateContentTask.cancel(true);
        }
        ContentTree.release();
    }

    public static String getAddress() {
        if (localAddress == null) {
            return null;
        }
        return localAddress.getHostAddress() + ":" + port;
    }

    /**
     * 获取 WIFI IP
     */
    private InetAddress getWIFIIpAddress(Context context) throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return InetAddress.getByName(String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
    }

}
