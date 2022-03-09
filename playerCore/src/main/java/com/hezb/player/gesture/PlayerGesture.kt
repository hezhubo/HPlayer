package com.hezb.player.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

/**
 * Project Name: HPlayer
 * File Name:    PlayerGesture
 *
 * Description: 手势.
 *
 * @author  hezhubo
 * @date    2022年03月02日 17:12
 */
class PlayerGesture(context: Context) : GestureDetector.OnGestureListener {

    /** 左右两边的上下滑动范围监听比例，最大0.5f */
    private val LEFT_RIGHT_LIMIT = 0.4f
    /** 手势检测器 */
    private val mGestureDetector = GestureDetector(context.applicationContext, this)
    /** 单击监听器 */
    private var mSingleTapUpListener: OnSingleTapUpListener? = null
    /** 手势滑动监听器 */
    private var mOnScrollListener: OnScrollListener? = null
    /** 触摸控件的宽度 */
    private var width = 0
    /** 是否为seek状态 */
    private var isSeekState = false
    /** 是否是否需要左右滑动进行seek */
    private var needSeek = true

    /**
     * 设置是否需要快进快退手势
     *
     * @param needSeek
     */
    fun needLeftRightSeek(needSeek: Boolean) {
        this.needSeek = needSeek
    }

    /**
     * 设置单击监听器
     * 必须设置手势滑动监听器此监听器才生效
     *
     * @param l
     */
    fun setOnSingleTapUpListener(l: OnSingleTapUpListener) {
        mSingleTapUpListener = l
    }

    /**
     * 设置手势滑动监听器
     *
     * @param l
     */
    fun setOnScrollListener(l: OnScrollListener) {
        mOnScrollListener = l
    }

    /**
     * 触摸事件
     *
     * @param event
     * @param viewWidth
     * @return
     */
    fun onTouchEvent(event: MotionEvent, viewWidth: Int): Boolean {
        width = viewWidth
        if (mOnScrollListener != null) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                onFingerDown()
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                onFingerUp()
            }
            mGestureDetector.onTouchEvent(event)
            return true
        }
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return mSingleTapUpListener?.onSingleTapUp(e) ?: false
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        mOnScrollListener?.let {
            val x1 = e1.x
            val y1 = e1.y
            val x2 = e2.x
            val y2 = e2.y
            val absX = abs(x1 - x2)
            val absY = abs(y1 - y2)
            val absDistanceX = abs(distanceX) // distanceX < 0 从左到右
            val absDistanceY = abs(distanceY) // distanceY < 0 从上到下
            if (absDistanceX < absDistanceY && !isSeekState) { // Y方向的速率比X方向的大，即 上下 滑动
                return if (distanceY > 0) { // 增加
                    if (x1 >= width * (1 - LEFT_RIGHT_LIMIT)) { // 在右边
                        it.onRightSideUpDown(true)
                    } else if (x1 <= width * LEFT_RIGHT_LIMIT) { //在左边
                        it.onLeftSideUpDown(true)
                    }
                    true
                } else {
                    if (x1 >= width * (1 - LEFT_RIGHT_LIMIT)) { //在右边
                        it.onRightSideUpDown(false)
                    } else if (x1 <= width * LEFT_RIGHT_LIMIT) { //在左边
                        it.onLeftSideUpDown(false)
                    }
                    true
                }
            } else if (needSeek) { // 左右
                if (absX > absY) {
                    isSeekState = true
                    it.onScrollLeftRight(width, distanceX)
                    return true
                }
            }
        }
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    /**
     * 单个手指按下
     */
    private fun onFingerDown() {
        mOnScrollListener?.onFingerDown()
    }

    /**
     * 单个手指抬起
     */
    fun onFingerUp() {
        mOnScrollListener?.onFingerUp()
        isSeekState = false
    }

    /**
     * 单击事件监听器
     */
    interface OnSingleTapUpListener {
        fun onSingleTapUp(e: MotionEvent): Boolean
    }

    /**
     * 滑动监听器
     */
    interface OnScrollListener {
        /**
         * 在左边上下滑动
         *
         * @param isUp 是否向上
         */
        fun onLeftSideUpDown(isUp: Boolean)

        /**
         * 在右边
         *
         * @param isUp 是否向上
         */
        fun onRightSideUpDown(isUp: Boolean)

        /**
         * 在左右滑动滑动
         * 需要在手指抬起时叠加计算，再进行seek
         * 若直接调用seek，则对播放器操作十分频繁
         *
         * @param width    触摸控件的宽度
         * @param distance 滑动距离  向左>0  向右<0
         */
        fun onScrollLeftRight(width: Int, distance: Float)

        /**
         * 手指按下
         */
        fun onFingerDown()

        /**
         * 手指抬起
         */
        fun onFingerUp()
    }
}