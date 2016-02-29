package com.hezb.hplayer.ui.activity;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hezb.clingupnp.DlnaSearch;
import com.hezb.clingupnp.dmc.UpnpControlSet;
import com.hezb.hplayer.R;
import com.hezb.hplayer.application.IApplication;
import com.hezb.hplayer.base.BaseActivity;
import com.hezb.hplayer.clingupnp.dmc.DevicesListDialog;
import com.hezb.hplayer.clingupnp.dmc.DlnaFragment;
import com.hezb.hplayer.constant.ConstantKey;
import com.hezb.hplayer.ui.view.BatteryView;
import com.hezb.hplayer.util.FileManager;
import com.hezb.hplayer.util.Log;
import com.hezb.hplayer.util.PlayerGesture;
import com.hezb.hplayer.util.Utility;

import org.fourthline.cling.model.meta.Device;

import java.io.File;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.CenterLayout;
import io.vov.vitamio.widget.VideoView;

/**
 * 播放页 TODO 使用mediacontroller(需要改动)  下载视频到本地(文件下载)
 * Created by hezb on 2016/1/15.
 */
public class PlayerActivity extends BaseActivity implements View.OnClickListener {

    private final int HIDE_INTERVAL = 5000;// 隐藏控制View时间间隔
    private final int UPDATE_INTERVAL = 1000;// 更新进度条时间间隔

    private CenterLayout mCenterLayout;
    private VideoView mVideoView;
    private View mLoadingView;
    private TextView mLoadingInfo;
    private View mVideoCenter;
    private View mVideoBottom;
    private ImageView mVideoPlayPause;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private SeekBar mSeekBar;
    private ImageView mVideoShot;
    private ImageView mOrientationChange;
    private View mVideoTop;
    private View mBack;
    private TextView mName;
    private View mDLNA;
    private DigitalClock mDigitalClock;
    private BatteryView mBatteryView;

    private GestureDetector mDetector;// 手势
    private PlayerGesture mPlayerGesture;

    private String name;// 视频名称
    private Uri playUri;// 播放地址
    private boolean isPlayComplete = false;// 是否播放完成
    private boolean isPlayError = false;// 是否播放出错
    private long currentPosition = 0;// 播放位置
    private int currentVideoLayout = VideoView.VIDEO_LAYOUT_SCALE;

    private final int SAVE_BITMAP = 1;// 保存截图
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SAVE_BITMAP:
                    if (msg.obj != null) {
                        Utility.showToast(mContext, getString(R.string.save_video_shot_tips) + msg.obj);
                    } else {
                        Utility.showToast(mContext, R.string.save_video_shot_error);
                    }
                    mVideoShot.setEnabled(true);
                    break;
                default:
                    break;
            }

            return false;
        }
    });

    private DlnaSearch mDlnaSearch;
    private DevicesListDialog mDevicesListDialog;
    private DlnaFragment mDlnaFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_player;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPlayerUrl();

        mFindViewById();

        initVideoView();

        initSeekBar();

        initOther();

        initDLNA();
    }

    @Override
    protected void onResume() {
        mDlnaSearch.resumeSearch();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mVideoView.isPlaying()) {
            currentPosition = mVideoView.getCurrentPosition();
            onVideoPause();
        }
        mDlnaSearch.pauseSearch();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(hiddenViewThread);
        mHandler.removeCallbacks(updateSeekBarThread);
        mVideoView.stopPlayback();
        // TODO 可做更多处理 保存播放记录到本地等

        mDlnaSearch.unBindService();
        super.onDestroy();
    }

    /**
     * 初始化播放地址
     */
    private void initPlayerUrl() {
        Intent intent = getIntent();
        if (intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = intent.getData();
            if (uri != null) {
                if (uri.getScheme().contains("content")) {
                    String[] projection = {MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media.TITLE};
                    Cursor videoCursor = new CursorLoader(
                            mContext, uri, projection, null, null, null).loadInBackground();
                    int dataColumnIndex = videoCursor.getColumnIndexOrThrow(
                            MediaStore.Video.Media.DATA);
                    int titleColumnIndex = videoCursor.getColumnIndexOrThrow(
                            MediaStore.Video.Media.TITLE);
                    videoCursor.moveToFirst();
                    String videoPath = videoCursor.getString(dataColumnIndex);
                    File videoFile = new File(videoPath);
                    playUri = Uri.fromFile(videoFile);
                    name = videoCursor.getString(titleColumnIndex);

                } else {
                    int index = uri.getPath().lastIndexOf("/");
                    if (index != -1) {
                        index++;
                    }
                    String fullName = uri.getPath().substring(index);
                    if (fullName.lastIndexOf(".") != -1) {// 若带后缀，去掉后缀
                        name = fullName.substring(0, fullName.lastIndexOf("."));
                    } else {
                        name = fullName;
                    }

                    playUri = uri;

                }
            }

        } else {
            name = intent.getStringExtra(ConstantKey.NAME);
            String url = intent.getStringExtra(ConstantKey.PLAY_URL);
            if (url == null) {
                Log.e("play url is null");
            } else {
                playUri = Uri.parse(url);
            }
        }
    }

    private void mFindViewById() {
        mCenterLayout = (CenterLayout) findViewById(R.id.vitamio_centerLayout);
        mVideoView = (VideoView) findViewById(R.id.vitamio_videoview);
        mLoadingView = findViewById(R.id.player_loading_layout);
        mLoadingInfo = (TextView) findViewById(R.id.loading_text);
        mVideoCenter = findViewById(R.id.player_center_iv);
        mVideoBottom = findViewById(R.id.player_bottom_layout);
        mVideoPlayPause = (ImageView) findViewById(R.id.player_play_iv);
        mCurrentTime = (TextView) findViewById(R.id.player_current_time);
        mTotalTime = (TextView) findViewById(R.id.player_total_time);
        mSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
        mVideoShot = (ImageView) findViewById(R.id.video_shot);
        mOrientationChange = (ImageView) findViewById(R.id.orientation_change);
        mVideoTop = findViewById(R.id.player_top_bar);
        mBack = findViewById(R.id.player_back);
        mName = (TextView) findViewById(R.id.player_name);
        mDLNA = findViewById(R.id.dlna_devices);
        mDigitalClock = (DigitalClock) findViewById(R.id.clock);
        mBatteryView = (BatteryView) findViewById(R.id.battery_view);
    }

    /**
     * 初始化VideoView
     */
    private void initVideoView() {
        Vitamio.isInitialized(mContext);
        mVideoView.setMediaBufferingIndicator(mLoadingView);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoShot.setEnabled(true);
                mLoadingView.setVisibility(View.GONE);
                mHandler.postDelayed(hiddenViewThread, HIDE_INTERVAL);
                onVideoPlay();
                if (currentPosition != 0
                        && currentPosition < mVideoView.getDuration()) {
                    // 若不调用 VideoView.getDuration() 就进行 seekTo() 那么 seekTo() 无效！
                    // vitamio5.0 的问题？
                    mVideoView.seekTo(currentPosition);
                }
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlayComplete = true;
                onPlaySRCChange(false);
                mLoadingView.setVisibility(View.GONE);
                mVideoCenter.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(updateSeekBarThread);
                mCurrentTime.setText(StringUtils.generateTime(mVideoView.getDuration()));
                mSeekBar.setProgress(mSeekBar.getMax());
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Utility.showToast(mContext, R.string.play_error);
                isPlayError = true;
                Log.e("VideoView on error! what:" + what + " extra:" + extra);
                return true;
            }
        });
        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (percent > 0 && percent < 100) {
                    mLoadingInfo.setText(percent + "%");
                } else {
                    mLoadingInfo.setText("");
                }
            }
        });
        mVideoView.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                onVideoPlay();
            }
        });

    }

    /**
     * 初始化进度条
     */
    private void initSeekBar() {
        mSeekBar.setMax(1000);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                float percent = seekbar.getProgress() / (float) seekbar.getMax();
                mVideoView.seekTo((int) (mVideoView.getDuration() * percent));
                mHandler.postDelayed(hiddenViewThread, HIDE_INTERVAL);
                onVideoPlay();
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                mHandler.removeCallbacks(updateSeekBarThread);
                mHandler.removeCallbacks(hiddenViewThread);
            }

            @Override
            public void onProgressChanged(SeekBar seekbar, int position, boolean arg2) {
                float percent = position / (float) seekbar.getMax();
                mCurrentTime.setText(StringUtils.generateTime(
                        (int) (mVideoView.getDuration() * percent)));
            }
        });
    }

    /**
     * 初始化其他组件
     */
    private void initOther() {
        mVideoBottom.setOnClickListener(this);
        mVideoCenter.setOnClickListener(this);
        mVideoPlayPause.setOnClickListener(this);
        mVideoShot.setOnClickListener(this);
        mOrientationChange.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mDLNA.setOnClickListener(this);

        mVideoShot.setEnabled(false);
        if (name != null) {
            mName.setText(name);
        }

        // 添加手势监听
        mPlayerGesture = new PlayerGesture(this, mVideoView,
                (RelativeLayout) findViewById(R.id.player_root));
        mDetector = new GestureDetector(this, mPlayerGesture);
        mDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mVideoBottom.getVisibility() == View.VISIBLE) {
                    mHandler.post(hiddenViewThread);
                } else {
                    showControllerBar();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleVideoLayout();
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });


        if (playUri == null) {
            Utility.showToast(mContext, R.string.play_url_error);
        } else {
            mVideoView.setVideoURI(playUri);
            mLoadingView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 初始化DLNA
     */
    private void initDLNA() {
        mDevicesListDialog = new DevicesListDialog(this);
        mDevicesListDialog.setSelectDeviceListener(new DevicesListDialog.SelectDeviceListener() {
            @Override
            public void selected(Device device) {
                UpnpControlSet upnpControlSet = mDlnaSearch.createUpnpControlSet(device);
                if (upnpControlSet != null) {
                    getSupportFragmentManager().beginTransaction().show(mDlnaFragment).commit();
                    mDlnaFragment.setUPnPControlSet(upnpControlSet, device.getDetails().getFriendlyName());
                    mDlnaFragment.openDLNA((int) mVideoView.getCurrentPosition());
                    mVideoView.stopPlayback();// 停止当前视频
                    mHandler.removeCallbacks(updateSeekBarThread);
                } else {
                    Utility.showToast(mContext, "远程设备不支持推屏");
                }
            }
        });
        mDlnaSearch = new DlnaSearch(this);
        mDlnaSearch.startSearchDMC();
        mDlnaFragment = new DlnaFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ConstantKey.NAME, name);
        bundle.putString(ConstantKey.PLAY_URL, playUri.toString());
        mDlnaFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.front_layout, mDlnaFragment)
                .hide(mDlnaFragment).commit();
    }

    /**
     * 隐藏底部控制视图
     */
    private Runnable hiddenViewThread = new Runnable() {

        @Override
        public void run() {
            Utility.translateAnimation(mVideoBottom, 0f, 0f, 0f, 1.0f, 400);
            mVideoBottom.setVisibility(View.GONE);
            Utility.translateAnimation(mVideoTop, 0f, 0f, 0f, -1.0f, 400);
            mVideoTop.setVisibility(View.GONE);
        }
    };

    /**
     * 更新进度条
     */
    private Runnable updateSeekBarThread = new Runnable() {

        @Override
        public void run() {
            mHandler.postDelayed(updateSeekBarThread, UPDATE_INTERVAL);
            if (mVideoView.getDuration() != 0) {
                mSeekBar.setProgress((int) (mVideoView.getCurrentPosition() /
                        (float) mVideoView.getDuration() * 1000));
                mCurrentTime.setText(StringUtils.generateTime(
                        mVideoView.getCurrentPosition()));
                mTotalTime.setText(StringUtils.generateTime(
                        mVideoView.getDuration()));
            }
        }
    };

    /**
     * 显示底部控制视图
     */
    private void showControllerBar() {
        mHandler.removeCallbacks(hiddenViewThread);
        mVideoBottom.setVisibility(View.VISIBLE);
        mVideoTop.setVisibility(View.VISIBLE);
        if (mVideoView.isPlaying()) {
            onPlaySRCChange(true);
        } else {
            onPlaySRCChange(false);
        }
        Utility.translateAnimation(mVideoBottom,
                0f, 0f, 1.0f, 0f, 300);
        Utility.translateAnimation(mVideoTop,
                0f, 0f, -1.0f, 0f, 300);
        mHandler.postDelayed(hiddenViewThread, HIDE_INTERVAL);

    }

    /**
     * 重新倒计时 隐藏底部控制视图
     */
    private void removeHideControllerBar() {
        mHandler.removeCallbacks(hiddenViewThread);
        mHandler.postDelayed(hiddenViewThread, HIDE_INTERVAL);
    }

    /**
     * 根据播放状态改变 切换播放图标
     */
    private void onPlaySRCChange(boolean isPlaying) {
        if (isPlaying) {
            mVideoPlayPause.setImageResource(R.drawable.play_pause_icon);
        } else {
            mVideoPlayPause.setImageResource(R.drawable.play_start_icon);
        }
    }

    /**
     * 暂停
     */
    private void onVideoPause() {
        mVideoView.pause();
        onPlaySRCChange(false);
        mVideoCenter.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(updateSeekBarThread);
    }

    /**
     * 播放
     */
    private void onVideoPlay() {
        mVideoView.start();
        onPlaySRCChange(true);
        mVideoCenter.setVisibility(View.GONE);
        mHandler.post(updateSeekBarThread);
    }

    /**
     * 点击播放按钮
     */
    private void onClickPlayButton() {
        removeHideControllerBar();
        if (isPlayComplete) {
            currentPosition = 0;
            mVideoView.setVideoURI(playUri);
            isPlayComplete = false;
        } else {
            if (mVideoView.isValid()) {
                if (mVideoView.isPlaying()) {
                    onVideoPause();
                } else {
                    onVideoPlay();
                }
            }
        }
    }

    /**
     * 切换视频布局
     */
    private void toggleVideoLayout() {
        currentVideoLayout++;
        if (currentVideoLayout > VideoView.VIDEO_LAYOUT_ZOOM) {
            currentVideoLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
        }
        mVideoView.setVideoLayout(currentVideoLayout, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_bottom_layout:
                removeHideControllerBar();
                break;
            case R.id.player_center_iv:
            case R.id.player_play_iv:
                onClickPlayButton();
                break;
            case R.id.video_shot:
                new Thread(saveVideoShot).start();
                mVideoShot.setEnabled(false);
                break;
            case R.id.orientation_change:
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case R.id.player_back:
                finish();
                break;
            case R.id.dlna_devices:
                mDevicesListDialog.show();
                break;
            default:
                break;
        }
    }

    private Runnable saveVideoShot = new Runnable() {
        @Override
        public void run() {
            File dir = new File(IApplication.videoShotDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String saveName = name + "_" + System.currentTimeMillis();
            String savePath = FileManager.saveBitmap(dir, mVideoView.getCurrentFrame(), saveName);
            Message message = new Message();
            message.what = SAVE_BITMAP;
            if (savePath != null) {
                message.obj = savePath;
            }
            mHandler.sendMessage(message);
        }
    };

    public void onStopDLNA(int remotePosition) {
        getSupportFragmentManager().beginTransaction().hide(mDlnaFragment).commit();
        currentPosition = remotePosition;
        mVideoView.setVideoURI(playUri);
        mLoadingView.setVisibility(View.VISIBLE);
    }


    // 平面两点间距离公式：|AB|=√[(x2－x1)^2+(y2－y1)^2]
    private double lastDistance = 0;// 上一次的两点间距离
    private float lastX = 0;
    private float lastY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int action = event.getAction();

        if (pointerCount == 2) {
            if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                lastX = event.getX(0) - event.getX(1);
                lastY = event.getY(0) - event.getY(1);
                lastDistance = Math.sqrt(lastX * lastX + lastY * lastY);

            } else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP) {
                // 抬手，重置
                lastX = 0;
                lastY = 0;
                lastDistance = 0;

            } else if (action == MotionEvent.ACTION_MOVE) {
                lastX = event.getX(0) - event.getX(1);
                lastY = event.getY(0) - event.getY(1);
                double moveDistance = Math.sqrt(lastX * lastX + lastY * lastY);

                int difference = (int) (moveDistance - lastDistance);
                float changeScale = difference / 100f;
                mVideoView.scaleVideo(changeScale);
                lastDistance = moveDistance;

            }

            return super.onTouchEvent(event);

        } else {
            if (mDlnaFragment != null && mDlnaFragment.isVisible()) {
                return super.onTouchEvent(event);
            }
            if (action == MotionEvent.ACTION_DOWN) {
                mPlayerGesture.onFingerDown();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mPlayerGesture.onFingerUp();
            }
            return mDetector.onTouchEvent(event);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(params);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mDigitalClock.setVisibility(View.VISIBLE);
            mBatteryView.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(params);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mDigitalClock.setVisibility(View.GONE);
            mBatteryView.setVisibility(View.GONE);
        }
        // 每次切换屏幕方向完成，需要重新计算VideoView宽高，故重新设置VideoLayout
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        mPlayerGesture.setScreenWidth(this);
    }
}
