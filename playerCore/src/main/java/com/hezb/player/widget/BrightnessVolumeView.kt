package com.hezb.player.widget

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.hezb.player.core.R

/**
 * Project Name: HPlayer
 * File Name:    BrightnessVolumeView
 *
 * Description: 亮度大小、声音大小调节展示组件.
 *
 * @author  hezhubo
 * @date    2022年03月02日 18:52
 */
class BrightnessVolumeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mIcon: ImageView
    private var mProgressBar: ProgressBar

    private var mAudioManager: AudioManager? = null

    /** 音量设置步长 */
    private val VOLUME_STEP = 3
    /** 亮度设置步长 */
    private val BRIGHTNESS_STEP = 2

    private var maxVolume = 0
    private var currentVolumePercentage = -1
    private var currentBrightnessPercentage = -1

    init {
        LayoutInflater.from(context).inflate(R.layout.player_widget_brightness_volume_view, this)
        mIcon = findViewById(R.id.iv_icon)
        mProgressBar = findViewById(R.id.progress_bar)

        hide()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mAudioManager = context.applicationContext.getSystemService(Service.AUDIO_SERVICE) as AudioManager?
        maxVolume = mAudioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAudioManager = null
    }

    /**
     * 隐藏
     */
    fun hide() {
        visibility = View.GONE
    }

    /**
     * 展示音量模式
     *
     * @param isAdd 是否加音量
     */
    fun showVolume(isAdd: Boolean) {
        if (maxVolume == 0) {
            return
        }
        if (currentVolumePercentage == -1) {
            currentVolumePercentage = getVolume() * 100 / maxVolume
        }
        currentVolumePercentage = if (isAdd) {
            currentVolumePercentage + VOLUME_STEP
        } else {
            currentVolumePercentage - VOLUME_STEP
        }
        var volume = currentVolumePercentage * maxVolume / 100
        if (volume > maxVolume) {
            volume = maxVolume
            currentVolumePercentage = 100
        } else if (volume < 0) {
            volume = 0
            currentVolumePercentage = 0
        }
        if (volume != getVolume()) { // 数值不等时才设置音量
            setVolume(volume)
        }
        when {
            volume == 0 -> {
                mIcon.setImageResource(R.drawable.player_xml_vector_gesture_volume_mute_icon)
            }
            volume < maxVolume / 2 -> {
                mIcon.setImageResource(R.drawable.player_xml_vector_gesture_volume_small_icon)
            }
            else -> {
                mIcon.setImageResource(R.drawable.player_xml_vector_gesture_volume_notice_icon)
            }
        }
        mProgressBar.progress = currentVolumePercentage
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
    }

    /**
     * 展示亮度模式
     *
     * @param isAdd 是否提高亮度
     */
    fun showScreenBrightness(isAdd: Boolean) {
        val activity = if (context is Activity) {
            context as Activity
        } else if (context is ContextWrapper && (context as ContextWrapper).baseContext is Activity) {
            (context as ContextWrapper).baseContext as Activity
        } else {
            null
        }
        activity?.let {
            if (currentBrightnessPercentage == -1) {
                currentBrightnessPercentage =
                    getScreenBrightness(it) * 100 / 255
            }
            currentBrightnessPercentage = if (isAdd) {
                currentBrightnessPercentage + BRIGHTNESS_STEP
            } else {
                currentBrightnessPercentage - BRIGHTNESS_STEP
            }
            var brightness = currentBrightnessPercentage * 255 / 100
            if (brightness > 255) {
                brightness = 255
                currentBrightnessPercentage = 100
            } else if (brightness < 0) {
                brightness = 0
                currentBrightnessPercentage = 0
            }
            if (brightness != getScreenBrightness(context as Activity)) { // 数值不等时才设置亮度
                setScreenBrightness(it, brightness)
            }
            mIcon.setImageResource(R.drawable.player_xml_vector_gesture_brightness_icon)
            mProgressBar.progress = currentBrightnessPercentage
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
    }

    /**
     * 设置音量
     *
     * @param volume 音量值
     */
    private fun setVolume(volume: Int) {
        mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    /**
     * 获取当前音量
     *
     * @return
     */
    private fun getVolume(): Int {
        return mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    /**
     * 设置亮度
     *
     * @param brightness 取值0-255
     */
    private fun setScreenBrightness(activity: Activity, brightness: Int) {
        val window: Window = activity.window
        val localLayoutParams: WindowManager.LayoutParams = window.attributes
        localLayoutParams.screenBrightness = brightness / 255.0f
        window.attributes = localLayoutParams
    }

    /**
     * 获取当前亮度(取值0-255)
     */
    private fun getScreenBrightness(activity: Activity): Int {
        return (activity.window.attributes.screenBrightness * 255).toInt()
    }

}