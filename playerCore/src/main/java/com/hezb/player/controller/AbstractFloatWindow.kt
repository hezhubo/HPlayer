package com.hezb.player.controller

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.LinearInterpolator
import com.hezb.player.core.AbstractMediaPlayer
import com.hezb.player.core.PlayerLog
import com.hezb.player.render.IRenderView
import kotlin.math.abs

/**
 * Project Name: HPlayer
 * File Name:    AbstractFloatWindow
 *
 * Description: 悬浮窗播放基类.
 *  使用请添加悬浮窗权限 :
 * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
 * <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
 *
 * @author  hezhubo
 * @date    2022年03月02日 18:13
 */
abstract class AbstractFloatWindow(context: Context) :
    AbstractMediaController(context.applicationContext) {

    /** 窗体管理者：管理悬浮窗 */
    protected lateinit var mWindowManager: WindowManager

    /** 悬浮窗布局参数 */
    protected lateinit var mWindowLayoutParams: WindowManager.LayoutParams

    /** 动画时长 */
    protected val DURATION_ANIMATION = 200L

    protected var showAnimatorSet: AnimatorSet? = null
    protected var hideAnimatorSet: AnimatorSet? = null

    protected var touchSlop = 0
    protected var screenWidth = 0
    protected var screenHeight: Int = 0
    protected var realScreenWidth: Int = 0

    /** 判断该悬浮窗是否可见 */
    var isShowing = false
        protected set

    /** 是否执行动画隐藏中 */
    var isHiding = false
        protected set

    init {
        initWindowManager(context.applicationContext)
        initView(context.applicationContext)
        initAnim()
    }

    abstract fun getLayoutId(): Int

    abstract fun getRenderViewId(): Int

    abstract fun initAllMember()

    /**
     * 初始化窗体管理者
     */
    private fun initWindowManager(context: Context) {
        // 取得系统窗体
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // 窗体的布局样式
        mWindowLayoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else { // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            // 设置窗体焦点及触摸：
            // FLAG_NOT_FOCUSABLE(不能获得按键输入焦点)
            flags = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            // 设置显示的模式
            format = PixelFormat.RGBA_8888
            // 设置对齐的方法
            gravity = Gravity.TOP or Gravity.LEFT
            // 设置窗体宽度和高度
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    /**
     * 初始化UI组件
     */
    private fun initView(context: Context) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        LayoutInflater.from(context).inflate(getLayoutId(), this)
        findViewById<View>(getRenderViewId()).apply {
            if (this is IRenderView) {
                initRendView(this as IRenderView)
            }
        }
        initTouch()
        initScreenSize()
        initLocation()

        initAllMember()
    }

    /**
     * 设置手势操作
     */
    private fun initTouch() {
        setOnTouchListener(object : OnTouchListener {
            var startRawX = 0f
            var startRawY = 0f
            var moveRawX = 0f
            var moveRawY = 0f
            var startX = 0f
            var startY = 0f
            var isMoving = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isMoving = false
                        startX = event.x
                        startY = event.y
                        startRawX = event.rawX
                        startRawY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - startX
                        val dy = event.y - startY
                        if (!isMoving && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            isMoving = true
                        }
                        if (isMoving) {
                            moveRawX = event.rawX
                            moveRawY = event.rawY
                            mWindowLayoutParams.let {
                                it.x += (moveRawX - startRawX).toInt()
                                it.y += (moveRawY - startRawY).toInt()
                            }
                            updateViewLayout()
                            startRawX = moveRawX
                            startRawY = moveRawY
                        }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        // 解决onTouch和OnClick冲突
                        if (!isMoving) {
                            v.performClick()
                        }
                        return true
                    }
                }
                return true
            }
        })
    }

    /**
     * 初始化屏幕尺寸
     */
    private fun initScreenSize() {
        val displayMetrics = DisplayMetrics()
        mWindowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        val realDisplayMetrics = DisplayMetrics()
        mWindowManager.defaultDisplay.getRealMetrics(realDisplayMetrics)
        realScreenWidth = realDisplayMetrics.widthPixels
    }

    /**
     * 初始化窗体出现位置
     */
    protected open fun initLocation() {
        mWindowLayoutParams.let {
            val scale = context.resources.displayMetrics.density
            it.x = realScreenWidth - (scale * 234 + 0.5f).toInt()
            it.y = screenHeight - (scale * 186 + 0.5f).toInt()
        }
    }

    /**
     * 更新窗体位置
     */
    protected fun updateViewLayout() {
        mWindowLayoutParams.let {
            val width = width
            val height = height
            it.x = if (it.x > realScreenWidth - width) realScreenWidth - width else it.x
            it.x = if (it.x < 0) 0 else it.x
            it.y = if (it.y > screenHeight - height) screenHeight - height else it.y
            it.y = if (it.y < 0) 0 else it.y
            try { // 部分手机在某些情况下会出现 非法参数异常
                mWindowManager.updateViewLayout(this, it) // 刷新显示
            } catch (e: IllegalArgumentException) {
                PlayerLog.e("float window update error!", e)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initScreenSize()
        updateViewLayout()
    }

    /**
     * 初始化动画
     */
    private fun initAnim() {
        showAnimatorSet = getShowAnimator()
        hideAnimatorSet = getHideAnimator()

        hideAnimatorSet?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && this@AbstractFloatWindow.isAttachedToWindow)
                    || this@AbstractFloatWindow.parent != null
                ) {
                    try {
                        mWindowManager.removeViewImmediate(this@AbstractFloatWindow)
                    } catch (e: Exception) {
                    }
                }
                isHiding = false
                isShowing = false
            }
        })
    }

    /**
     * 执行显示动画
     */
    private fun startShowAnim() {
        hideAnimatorSet?.cancel()
        showAnimatorSet?.let {
            it.cancel()
            it.start()
        }
    }

    /**
     * 执行隐藏动画
     *
     * @return 是否成功执行
     */
    private fun startHideAnim(): Boolean {
        showAnimatorSet?.cancel()
        hideAnimatorSet?.let {
            isHiding = true
            it.cancel()
            it.start()
            return true
        }
        return false
    }

    /**
     * 显示动画
     */
    protected open fun getShowAnimator(): AnimatorSet {
        val alpha: ObjectAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
        val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, 1f)
        val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.duration = DURATION_ANIMATION
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.playTogether(alpha, scaleX, scaleY)
        animatorSet.setTarget(this)
        return animatorSet
    }

    /**
     * 退出动画
     */
    protected open fun getHideAnimator(): AnimatorSet {
        val alpha: ObjectAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)
        val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 0f)
        val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 0f)
        val animatorSet = AnimatorSet()
        animatorSet.duration = DURATION_ANIMATION
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.playTogether(alpha, scaleX, scaleY)
        animatorSet.setTarget(this)
        return animatorSet
    }

    /**
     * 显示悬浮窗
     */
    open fun show(mediaPlayer: AbstractMediaPlayer) {
        if (!isShowing) {
            initScreenSize()
            try {
                mWindowManager.addView(this, mWindowLayoutParams)
                updateViewLayout()
                startShowAnim()
                isHiding = false
                isShowing = true
            } catch (e: Exception) {
                PlayerLog.e("show player float window error!", e)
                try {
                    mWindowManager.removeView(this)
                } catch (e: Exception) {
                }
                mediaPlayer.release() // 显示悬浮窗失败，则关闭播放器
                return
            }

        }

        bindMediaPlayer(mediaPlayer)
        play() // 直接播放
    }

    /**
     * 隐藏悬浮窗
     * 不释放播放器
     */
    open fun hide() {
        if (isShowing) {
            if (!startHideAnim()) {
                mWindowManager.removeView(this)
                isHiding = false
                isShowing = false
            }
        }
        unBindMediaPlayer()
    }

    /**
     * 关闭悬浮窗
     * 释放播放器
     */
    open fun close() {
        release()
        hide()
    }
}