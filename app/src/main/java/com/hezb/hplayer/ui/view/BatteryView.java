package com.hezb.hplayer.ui.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.hezb.hplayer.R;

/**
 * 电池组件
 * 运用广播更新  TODO 低电量变色，画的更好看点。。。
 *
 * @author hezb
 */
public class BatteryView extends View {

    private BroadcastReceiver batteryChangeReceiver;

    private int batteryColor = 0xffffffff; // 电池颜色，默认白色
    private int textColor = 0xff009688; // 电量文字颜色，默认深绿色(茶色)
    private final int padding = 2; // 内边距
    private final int batteryHeadWidth = 3; // 电池头的宽度
    private final int batteryInsideMargin = 3; // 电量外边距

    private int currentPower = 100;// 当前电量
    private int showPower = currentPower;// 展示电量

    private static final int NOT_CHARGING_INVALIDATE = 0;
    private static final int CHARGING_INVALIDATE = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            invalidate();
            switch (msg.what) {
                case CHARGING_INVALIDATE:
                    if (showPower == 100) {
                        showPower = currentPower;
                    } else {
                        showPower = showPower + 5;
                        if (showPower > 100) {
                            showPower = 100;
                        }
                    }
                    if (currentPower != 100) {
                        mHandler.sendEmptyMessageDelayed(CHARGING_INVALIDATE, 1000);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    public BatteryView(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeMessages(CHARGING_INVALIDATE);
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(batteryChangeReceiver);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BatteryViewAttrs, defStyle, 0);
            batteryColor = typedArray.getColor(R.styleable.BatteryViewAttrs_batteryColor, batteryColor);
            textColor = typedArray.getColor(R.styleable.BatteryViewAttrs_textColor, textColor);
            typedArray.recycle();
        }
        initBroadcastReceiver(context);
    }

    public void setBatteryColor(int color) {
        batteryColor = getResources().getColor(color);
    }

    public void setTextColor(int color) {
        textColor = getResources().getColor(color);
    }

    private void initBroadcastReceiver(Context context) {
        IntentFilter batteryChangeIntentFilter = new IntentFilter();
        batteryChangeIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryChangeIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        batteryChangeIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        batteryChangeIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        batteryChangeReceiver = new BatteryChangeReceiver();
        context.registerReceiver(batteryChangeReceiver, batteryChangeIntentFilter);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
//        if (width < 22) {// 设置最小宽高，会用到么？
//            setMeasuredDimension(22, getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
//        } else {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (width < 22) {
            return;
        }
        // 计算电池宽高
        int batteryWidth = getWidth() - padding * 2 - batteryHeadWidth;
        int tempHeight = batteryWidth / 2;
        int batteryHeight = tempHeight > height ? height : tempHeight;

        int batteryHeadHeight = batteryHeight / 3;

        // 先画外框
        Paint paint = new Paint();
        paint.setColor(batteryColor);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        Rect rect = new Rect(padding, padding, padding + batteryWidth, padding + batteryHeight);
        canvas.drawRect(rect, paint);

        // 画电量
        float powerPercent = showPower / 100.0f;
        Paint paint2 = new Paint(paint);
        paint2.setStyle(Paint.Style.FILL);
        if (powerPercent != 0) {
            int pWidth = (int) ((batteryWidth - 2 * batteryInsideMargin) * powerPercent);
            int pHeight = batteryHeight - 2 * batteryInsideMargin;
            int pLeft = padding + batteryInsideMargin;
            int pTop = padding + batteryInsideMargin;
            int pRight = pLeft + pWidth;
            int pBottom = pTop + pHeight;
            Rect rect2 = new Rect(pLeft, pTop, pRight, pBottom);
            canvas.drawRect(rect2, paint2);
        }

        // 画电池头
        int hLeft = padding + batteryWidth;
        int hTop = padding + batteryHeight / 3;
        int hRight = hLeft + batteryHeadWidth;
        int hBottom = hTop + batteryHeadHeight;
        Rect rect3 = new Rect(hLeft, hTop, hRight, hBottom);
        canvas.drawRect(rect3, paint2);

        // 画数值
        String showText = currentPower + "%";
        float textSize = batteryHeight / 5f * 3;
        Paint paint3 = new Paint();
        paint3.setColor(textColor);
        paint3.setAntiAlias(true);
        paint3.setTextSize(textSize);
        Rect rect4 = new Rect();
        paint3.getTextBounds(showText, 0, showText.length(), rect4);
        int baseline = (batteryHeight + padding) / 2 + rect4.height() / 2;
        int textX = width / 2 - rect4.width() / 2;
        canvas.drawText(showText, textX, baseline, paint3);
    }

    class BatteryChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                // 已接通电源,正在充电!
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                // 已断开电源!
            } else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
                // 电池电量过低,请接通电源!
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // 获取当前电量
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                // 获取当前电池状态 充电：BATTERY_STATUS_CHARGING
                int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_DISCHARGING);

                currentPower = level;

                if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                    if (!mHandler.hasMessages(CHARGING_INVALIDATE)) {
                        showPower = currentPower;
                        mHandler.sendEmptyMessage(CHARGING_INVALIDATE);
                    }
                } else {
                    mHandler.removeMessages(CHARGING_INVALIDATE);
                    showPower = currentPower;
                    mHandler.sendEmptyMessage(NOT_CHARGING_INVALIDATE);
                }

            }

        }
    }
}