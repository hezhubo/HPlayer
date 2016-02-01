package com.hezb.hplayer.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hezb.hplayer.R;
import com.hezb.hplayer.ui.view.LightnessVolumeView;

import io.vov.vitamio.utils.ScreenResolution;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.VideoView;

/**
 * 播放器手势操作
 * TODO 需优化 问题不少！
 */
public class PlayerGesture implements OnGestureListener {

    private VideoView mVideoView;
    private LightnessVolumeView mLV;// 音量，亮度
    private TextView mTime;// 快进/快退 时间
    private OnSingleTapUpListener singleTapUpListener;
    private Handler mHandler;

    private int left; // VideoView 左边坐标
    private int right; // VideoView 右边坐标
    private int width;// VideoView 宽
    private int currentSeek = -1;// 当前需要跳转的进度
    private boolean intoSeek = false;// 进入 快进/快退 状态

    public PlayerGesture(Activity activity, VideoView videoView, RelativeLayout rootView) {
        mVideoView = videoView;

        Pair<Integer, Integer> screenPair = ScreenResolution.getResolution(activity);
        width = screenPair.first;
        left = 0;
        right = width;

        // 添加音量，亮度设置展示View，VideoView 的父布局必须是 RelativeLayout
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                DensityUtil.dip2px(activity, 60), DensityUtil.dip2px(activity, 60));
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLV = new LightnessVolumeView(activity);
        mLV.setLayoutParams(lp);
        mLV.setActivity(activity);
        mLV.setBackgroundResource(R.drawable.loading_bg);
        mLV.setGravity(Gravity.CENTER);
        mLV.setVisibility(View.GONE);
        rootView.addView(mLV);

        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTime = new TextView(activity);
        mTime.setLayoutParams(lp2);
        mTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTime.setTextColor(Color.parseColor("#ffffff"));
        mTime.setGravity(Gravity.CENTER);
        mTime.setBackgroundResource(R.drawable.loading_bg);
        mTime.setVisibility(View.GONE);
        rootView.addView(mTime);
        mTime.setPadding(20, 10, 20, 10);

        mHandler = new Handler();
    }

    public void setOnSingleTapUpListener(OnSingleTapUpListener listener) {
        singleTapUpListener = listener;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (singleTapUpListener != null) {
            return singleTapUpListener.onSingleTapUp(e);
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        float x1 = e1.getX();
        float y1 = e1.getY();
        float x2 = e2.getX();
        float y2 = e2.getY();
        float absX = Math.abs(x1 - x2);
        float absY = Math.abs(y1 - y2);
        float absDistanceX = Math.abs(distanceX);// distanceX < 0 从左到右
        float absDistanceY = Math.abs(distanceY);// distanceY < 0 从上到下
        if (absDistanceX < absDistanceY && !intoSeek) { // Y方向的速率比X方向的大，即 上下 滑动
            if (distanceY > 0) {
                if (x1 >= (right - width * 0.35)) {
                    //在右边，调节声音
                    mLV.changeVolume(LightnessVolumeView.ADD_FLAG);
                } else if (x1 <= (left + width * 0.35)) {
                    //在左边，调节亮度
                    mLV.changeLightness(LightnessVolumeView.ADD_FLAG);
                }

                return true;
            } else {
                if (x1 >= (right - width * 0.35)) {
                    //在右边，调节声音
                    mLV.changeVolume(LightnessVolumeView.SUB_FLAG);
                } else if (x1 <= (left + width * 0.35)) {
                    //在左边，调节亮度
                    mLV.changeLightness(LightnessVolumeView.SUB_FLAG);
                }

                return true;
            }

        } else { // 左右
            if (absX > absY) {
                intoSeek = true;
                float progress = absX / width * 1.2f;
                if (x1 > x2) {
                    onRewind(progress);
                } else {
                    onForward(progress);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }

    private void onForward(float progress) {
        int seek = (int) (mVideoView.getCurrentPosition()
                + mVideoView.getDuration() * progress);
        if (seek < mVideoView.getDuration()) {
            showTime(seek);
        }
    }

    private void onRewind(float progress) {
        int seek = (int) (mVideoView.getCurrentPosition()
                - mVideoView.getDuration() * progress);
        if (seek >= 0) {
            showTime(seek);
        }
    }

    private void showTime(int seek) {
        currentSeek = seek;
        String showTime = StringUtils.generateTime(seek) + "/" + StringUtils.generateTime(mVideoView.getDuration());
        mTime.setText(showTime);
        if (mTime.getVisibility() != View.VISIBLE) {
            mTime.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 手指抬起
     */
    public void onFingerUp() {
        if (currentSeek != -1) {
            mVideoView.seekTo(currentSeek);
            mHandler.postDelayed(hideTimeView, 500);
        }
        currentSeek = -1;
        intoSeek = false;
    }

    private Runnable hideTimeView = new Runnable() {
        @Override
        public void run() {
            mTime.setVisibility(View.GONE);
        }
    };


    public interface OnSingleTapUpListener {
        boolean onSingleTapUp(MotionEvent e);
    }
}
