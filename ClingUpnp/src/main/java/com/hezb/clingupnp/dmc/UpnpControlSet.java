package com.hezb.clingupnp.dmc;

import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.item.Movie;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Upnp命令集合
 */
public class UpnpControlSet {

    private static final String TAG = "UpnpControlSet";

    public static final int SET_MUTE = 0;// 静音
    public static final int GET_VOLUME = 1;// 获取音量
    public static final int SET_VOLUME = 2;// 设置音量
    public static final int SET_AVTRANSPORT = 3;// 设置传输媒体
    public static final int GET_POSITIONINFO = 4;// 获取时长
    public static final int SEEK = 5;// 跳转
    public static final int STOP = 6;// 停止
    public static final int PLAY = 7;// 播放
    public static final int PAUSE = 8;// 暂停

    private AndroidUpnpService mUpnpService;
    private Service avTransportService, renderingControlService;
    private UpnpActionCallBack mUpnpActionCallBack;

    private int mDuration = 0;
    private int mCurrentPosition = 0;

    private ActionCallback getVolume;// 音量回调
    private ActionCallback getPositionInfo;// 进度回调
    private ActionCallback stop;// 停止回调
    private ActionCallback play;// 播放回调
    private ActionCallback pause;// 暂停回调
    private ActionCallback getTransportInfo;// 传输回调

    private boolean alreadyPlay = true;
    private int currentPosition;
    private Handler mHandler;

    /**
     * @param upnpService             upnp服务
     * @param avTransportService      远程视频传输服务
     * @param renderingControlService 远程渲染器控制服务
     */
    public UpnpControlSet(AndroidUpnpService upnpService,
                          Service avTransportService,
                          Service renderingControlService) {
        this.mUpnpService = upnpService;
        this.avTransportService = avTransportService;
        this.renderingControlService = renderingControlService;
        if (mUpnpService == null || avTransportService == null
                || renderingControlService == null) {
            Log.e(TAG, "UpnpControlSet parameters is null");
        }
        mHandler = new Handler();
        initNoParameterActionCallback();
    }

    /**
     * 设置回调监听
     */
    public void setUPnPActionCallBack(UpnpActionCallBack upnpActionCallBack) {
        this.mUpnpActionCallBack = upnpActionCallBack;
    }

    private void onFailureCallBack(int type, String error) {
        if (mUpnpActionCallBack != null) {
            mUpnpActionCallBack.onFailure(type, error);
        }
    }

    private void onSuccessCallBack(int type) {
        if (mUpnpActionCallBack != null) {
            mUpnpActionCallBack.onSuccess(type);
        }
    }

    /**
     * 初始化无参回调事件
     */
    private void initNoParameterActionCallback() {

        getVolume = new GetVolume(renderingControlService) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(GET_VOLUME, arg2);
            }

            @Override
            public void received(ActionInvocation arg0, int arg1) {
                if (mUpnpActionCallBack != null) {
                    mUpnpActionCallBack.getVolumeReceived(arg1);
                }
            }
        };

        getPositionInfo = new GetPositionInfo(avTransportService) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(GET_POSITIONINFO, arg2);
            }

            @Override
            public void received(ActionInvocation arg0, PositionInfo arg1) {
                Log.d(TAG, "executeGetPositionInfo, received, " + arg0);
                Log.d(TAG, "MediaInfo, getMediaDuration = " + arg1.getAbsTime() + " " + arg1.getRelTime() + " " + arg1.getTrackDuration());
                mCurrentPosition = generateTime(arg1.getRelTime());
                mDuration = generateTime(arg1.getTrackDuration());
                if (mUpnpActionCallBack != null) {
                    mUpnpActionCallBack.getPositionInfoReceived(mCurrentPosition, mDuration);
                }
            }
        };

        getTransportInfo = new GetTransportInfo(avTransportService) {

            @Override
            public void received(ActionInvocation arg0, TransportInfo arg1) {
                Log.d(TAG, "GetTransportInfo, received, " + arg1.getCurrentTransportState().getValue());
                if (!alreadyPlay) {
                    if (arg1.getCurrentTransportState()
                            .getValue().equals("PLAYING")) { // TODO 固定是 PLAYING ？
                        onVideoSeek(currentPosition);
                        alreadyPlay = true;
                        mHandler.removeCallbacks(getTransportInfoRunnable);
                        mUpnpActionCallBack.getTransportReceived(true);
                    } else {
                        mUpnpActionCallBack.getTransportReceived(false);
                        mHandler.postDelayed(getTransportInfoRunnable, 500);
                    }
                }
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Log.e(TAG, "get transport info failure:" + arg2);
            }
        };

        stop = new Stop(avTransportService) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(STOP, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onSuccessCallBack(STOP);
            }

        };

        play = new Play(avTransportService) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(PLAY, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onSuccessCallBack(PLAY);
            }
        };

        pause = new Pause(avTransportService) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(PAUSE, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onSuccessCallBack(PAUSE);
            }
        };

    }

    /**
     * 获取音量
     */
    public void getDeviceVolume() {
        mUpnpService.getControlPoint().execute(getVolume);
    }

    /**
     * 视频停止
     */
    public void onVideoStop() {
        mUpnpService.getControlPoint().execute(stop);
    }

    /**
     * 视频播放
     */
    public void onVideoPlay() {
        mUpnpService.getControlPoint().execute(play);
    }

    /**
     * 视频暂停
     */
    public void onVideoPause() {
        mUpnpService.getControlPoint().execute(pause);
    }

    /**
     * 得到视频播放位置信息，时长
     */
    public void getVideoPositionInfo() {
        if (alreadyPlay) {
            mUpnpService.getControlPoint().execute(getPositionInfo);
        }
    }

    /**
     * 获取远程渲染器状态
     */
    private void getDMRTransportInfo() {
        mUpnpService.getControlPoint().execute(getTransportInfo);
    }

    /**
     * 为防止 返回参数不是 PLAYING 一直请求，因此需要供外部调用移除队列
     */
    public void stopGetDMRTransportInfo() {
        mHandler.removeCallbacks(getTransportInfoRunnable);
    }

    /**
     * 设置音量
     */
    public void setDeviceVolume(int volume) {
        ActionCallback setVolume = new SetVolume(renderingControlService, volume) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(SET_VOLUME, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onSuccessCallBack(SET_VOLUME);
            }
        };
        mUpnpService.getControlPoint().execute(setVolume);
    }

    /**
     * 设置静音
     */
    public void setDeviceMute(boolean mute) {
        ActionCallback setMute = new SetMute(renderingControlService, mute) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(SET_MUTE, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onSuccessCallBack(SET_MUTE);
            }
        };
        mUpnpService.getControlPoint().execute(setMute);
    }

    /**
     * 推屏，设置URL
     */
    public void setAVTransportURI(String url, int schedule, String mediaName) {
        String id = md5(url);
        setAVTransportURI(url, schedule, mediaName, id, "creator", "parentID");
    }

    public void setAVTransportURI(String url, int schedule, String title, String id, String creator, String parentID) {
        alreadyPlay = false;
        currentPosition = schedule;
        //TODO 此处有问题 究竟如何才是正确的DLNA推屏数据？
        DIDLParser didlParser = new DIDLParser();
        DIDLContent content = new DIDLContent();
        Res res = new Res();
        Movie movie = new Movie(id, parentID, title, creator, res);
        content.addItem(movie);
        String didlString = "";
        try {
            didlString = didlParser.generate(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ActionCallback setAVTransport = new SetAVTransportURI(
                avTransportService, url, didlString) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(SET_AVTRANSPORT, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onVideoPlay();

                // TODO 究竟如何将当前进度一起推送过去，让播放器播放时自动跳转？
                // TODO DLNA 是否支持这个尚不清楚
                getDMRTransportInfo();// 远程渲染器播放准备完成不会主动告诉终端，需获取状态来做进度推送

                onSuccessCallBack(SET_AVTRANSPORT);
            }
        };
        mUpnpService.getControlPoint().execute(setAVTransport);
    }

    /**
     * 视频跳转
     */
    public void onVideoSeek(int schedule) {
        String seekToTime = generateTime(schedule);
        ActionCallback seek = new Seek(avTransportService, seekToTime) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                onFailureCallBack(SEEK, arg2);
            }

            @Override
            public void success(ActionInvocation invocation) {
                onSuccessCallBack(SEEK);
            }
        };
        mUpnpService.getControlPoint().execute(seek);
    }

    /**
     * 获取DMR状态
     */
    private Runnable getTransportInfoRunnable = new Runnable() {

        @Override
        public void run() {
            mUpnpService.getControlPoint().execute(getTransportInfo);
        }
    };

    private String generateTime(int time) {
        int totalSeconds = time / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private int generateTime(String time) {
        try {
            String[] totalTime = time.split(":");
            int hours = Integer.parseInt(totalTime[0]) * 3600;
            int minutes = Integer.parseInt(totalTime[1]) * 60;
            int seconds = (int) Float.parseFloat(totalTime[2]); // 存在异类的秒，还带小数点的
            int allTime = (hours + minutes + seconds) * 1000;
            return allTime;
        } catch (Exception e) {
            Log.e(TAG, "upnp control set generateTime exception:" + e.getMessage());
        }

        return 0;
    }

    /**
     * md5加密算法
     *
     * @return 密文
     */
    private String md5(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

}
