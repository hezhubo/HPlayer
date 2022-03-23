package com.hezb.hplayer.ui.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.SeekBar
import com.hezb.clingupnp.util.FormatUtil
import com.hezb.hplayer.R
import com.hezb.hplayer.databinding.WidgetDmcPopupWindowBinding

/**
 * Project Name: HPlayer
 * File Name:    DMCPopupWindow
 *
 * Description: DMC操作弹窗.
 *
 * @author  hezhubo
 * @date    2022年03月05日 01:07
 */
class DMCPopupWindow(context: Context) : PopupWindow(context) {

    private val viewBinding = WidgetDmcPopupWindowBinding.inflate(LayoutInflater.from(context))

    var controlCallback: ControlCallback? = null

    private var isSuccess = false
    private var isPlaying = false
    private var isDragging = false
    private var duration: Long = 0

    private val MSG_UPDATE_TIME = 66
    private val UPDATE_TIME_INTERVAL = 900L
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            controlCallback?.updateTime()
            startUpdateTime()
        }
    }

    init {
        contentView = viewBinding.root

        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        isOutsideTouchable = false
        isTouchable = true
        isClippingEnabled = false

        setOnDismissListener {
            stopUpdateTime()
        }

        initView()
    }

    private fun initView() {
        viewBinding.btnClose.setOnClickListener {
            controlCallback?.close()
            reset()
            dismiss()
        }
        viewBinding.ivPlayPause.setOnClickListener {
            if (isPlaying) {
                controlCallback?.pause()
            } else {
                controlCallback?.play()
            }
        }
        viewBinding.seekbar.max = 1000
        viewBinding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var newPosition: Long = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) { // 不是用户主动拉动进度条，则不需要seek
                    return
                }
                if (isSuccess) {
                    newPosition = duration * progress / seekBar.max
                    viewBinding.tvCurrentTime.text = FormatUtil.formatTime(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isDragging = true
                stopUpdateTime()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSuccess) {
                    controlCallback?.seekTo(newPosition)
                }
                isDragging = false
                startUpdateTime()
            }
        })

        viewBinding.btnAddVolume.setOnClickListener {
            controlCallback?.addVolume()
        }
        viewBinding.btnLessVolume.setOnClickListener {
            controlCallback?.lessVolume()
        }
    }

    private fun reset() {
        isSuccess = false
        isPlaying = false
        duration = 0
        viewBinding.ivPlayPause.setImageResource(R.drawable.player_xml_vector_play_icon)
        viewBinding.tvDeviceName.text = ""
        stopUpdateTime()
    }

    private fun startUpdateTime() {
        mHandler.removeMessages(MSG_UPDATE_TIME)
        if (isSuccess && !isDragging) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_TIME_INTERVAL)
        }
    }

    private fun stopUpdateTime() {
        mHandler.removeMessages(MSG_UPDATE_TIME)
    }

    fun setDeviceName(name: String) {
        viewBinding.tvDeviceName.text = name
    }

    fun setAVTransportURISuccess() {
        isSuccess = true
        startUpdateTime()
    }

    fun setPlayState(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        if (isPlaying) {
            viewBinding.ivPlayPause.setImageResource(R.drawable.player_xml_vector_pause_icon)
            startUpdateTime()
        } else {
            viewBinding.ivPlayPause.setImageResource(R.drawable.player_xml_vector_play_icon)
            stopUpdateTime()
        }
    }

    fun updateTime(currentPosition: Long, duration: Long) {
        this.duration = duration
        if (!isDragging) {
            if (duration > 0) {
                viewBinding.seekbar.progress = (currentPosition * viewBinding.seekbar.max / duration).toInt()
            }
            viewBinding.tvCurrentTime.text = FormatUtil.formatTime(currentPosition)
        }
        viewBinding.tvTotalTime.text = FormatUtil.formatTime(duration)
    }

    interface ControlCallback {

        fun close()

        fun play()

        fun pause()

        fun seekTo(position: Long)

        fun updateTime()

        fun addVolume()

        fun lessVolume()
    }

}