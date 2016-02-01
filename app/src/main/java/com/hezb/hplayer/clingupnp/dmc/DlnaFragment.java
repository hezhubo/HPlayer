package com.hezb.hplayer.clingupnp.dmc;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hezb.clingupnp.dmc.UpnpActionCallBack;
import com.hezb.clingupnp.dmc.UpnpControlSet;
import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseFragment;
import com.hezb.hplayer.constant.ConstantKey;
import com.hezb.hplayer.ui.activity.PlayerActivity;
import com.hezb.hplayer.util.Log;
import com.hezb.hplayer.util.Utility;

import io.vov.vitamio.utils.StringUtils;


/**
 * @author hezb
 * @Description: Dlna层
 * @date 2015年7月7日 上午11:34:12
 * TODO 垂直seekbar设置音量
 */
public class DlnaFragment extends BaseFragment implements UpnpActionCallBack, OnClickListener {

    private TextView mDeviceName;
    private View mClose;
    private ImageView mVideoPlayPause;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private SeekBar mSeekBar;

    private Handler mHandler = new Handler();

    private String deviceName;
    private String videoName;
    private String playUrl;

    private UpnpControlSet mUPnPControlSet;
    private boolean success = false;// 成功推屏
    private boolean isPlaying = false;
    private int duration;
    private int currentPosition;

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_dlna;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        videoName = (String) args.get(ConstantKey.NAME);
        playUrl = (String) args.get(ConstantKey.PLAY_URL);
    }

    @Override
    protected void initAllMember() {
        mFindViewById();

        initOperation();

        initSeekBar();
    }

    private void mFindViewById() {
        mDeviceName = (TextView) findViewById(R.id.device_name);
        mClose = findViewById(R.id.dlna_close);
        mVideoPlayPause = (ImageView) findViewById(R.id.play_or_pause);
        mCurrentTime = (TextView) findViewById(R.id.current_time);
        mTotalTime = (TextView) findViewById(R.id.total_time);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
    }

    private void initOperation() {
        mVideoPlayPause.setOnClickListener(this);
        mClose.setOnClickListener(this);
        if (deviceName != null) {
            mDeviceName.setText(deviceName);
        }
    }

    private void initSeekBar() {
        mSeekBar.setMax(1000);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                if (mUPnPControlSet != null && success) {
                    float percent = seekbar.getProgress() / (float) seekbar.getMax();
                    mUPnPControlSet.onVideoSeek((int) (duration * percent));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar seekbar, int position, boolean arg2) {
                float percent = position / (float) seekbar.getMax();
                mCurrentTime.setText(StringUtils.generateTime((int) (duration * percent)));
            }
        });
    }

    /**
     * 设置DLNA控制对象
     */
    public void setUPnPControlSet(UpnpControlSet uPnPControlSet, String deviceName) {
        mUPnPControlSet = uPnPControlSet;
        mUPnPControlSet.setUPnPActionCallBack(this);
        this.deviceName = deviceName;
    }

    /**
     * 打开DLNA
     * currentPosition 当前播放位置
     */
    public void openDLNA(int currentPosition) {
        if (mUPnPControlSet != null && !TextUtils.isEmpty(playUrl)) {
            mUPnPControlSet.setAVTransportURI(playUrl, currentPosition, videoName);
        }
    }

    @Override
    public void onSuccess(int type) {
        switch (type) {
            case UpnpControlSet.SET_AVTRANSPORT:
                success = true;
                break;

            default:
                break;
        }
        Log.d("dlna onSuccess, type:" + type);
    }

    @Override
    public void onFailure(int type, String info) {
        switch (type) {
            case UpnpControlSet.SET_AVTRANSPORT:
                success = false;
                Utility.showToast(mContext, "推屏失败！");
                break;
            case UpnpControlSet.PLAY:
                Utility.showToast(mContext, "远程设备播放失败！");
                break;
            default:
                break;
        }
        Log.e("dlna onFailure, type:" + type + "  info:" + info);
    }

    @Override
    public void getVolumeReceived(int volume) {
    }

    @Override
    public void getTransportReceived(boolean isPlaying) {
        if (isPlaying) {
            this.isPlaying = isPlaying;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayPause.setImageResource(R.drawable.play_pause_icon);
                }
            });
            mHandler.postDelayed(updateSeekBarThread, 0);
        }
    }

    @Override
    public void getPositionInfoReceived(int currentPosition, int duration) {
        if (duration != 0) {
            this.duration = duration;
            this.currentPosition = currentPosition;
            getActivity().runOnUiThread(onUIUpdateThread);
        }
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.play_or_pause:
                if (mUPnPControlSet != null) {
                    isPlaying = !isPlaying;
                    if (isPlaying) {
                        mVideoPlayPause.setImageResource(R.drawable.play_pause_icon);
                        mUPnPControlSet.onVideoPlay();
                    } else {
                        mVideoPlayPause.setImageResource(R.drawable.play_start_icon);
                        mUPnPControlSet.onVideoPause();
                    }
                }
                break;
            case R.id.dlna_close:
                mHandler.removeCallbacks(updateSeekBarThread);
                if (mUPnPControlSet != null) {
                    mUPnPControlSet.onVideoStop();
                }
                ((PlayerActivity)getActivity()).onStopDLNA(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUPnPControlSet != null) {
            mUPnPControlSet.stopGetDMRTransportInfo();
        }
        mHandler.removeCallbacks(updateSeekBarThread);
    }

    private Runnable updateSeekBarThread = new Runnable() {

        @Override
        public void run() {
            if (mUPnPControlSet != null && success) {
                mUPnPControlSet.getVideoPositionInfo();
                mHandler.postDelayed(updateSeekBarThread, 1000);
            }
        }
    };

    private Runnable onUIUpdateThread = new Runnable() {

        @Override
        public void run() {
            mSeekBar.setProgress((int) (currentPosition / (float) duration * 1000));
            mCurrentTime.setText(StringUtils.generateTime(currentPosition));
            mTotalTime.setText(StringUtils.generateTime(duration));
        }
    };
}
