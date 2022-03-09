package com.hezb.player.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.hezb.player.controller.AbstractMediaController
import com.hezb.player.core.AbstractMediaPlayer
import com.hezb.player.core.IMediaPlayer
import com.hezb.player.core.R
import com.hezb.player.gesture.PlayerGesture

/**
 * Project Name: HPlayer
 * File Name:    PlayerControllerView
 *
 * Description: 播放器控制层.
 *
 * @author  hezhubo
 * @date    2022年03月02日 19:02
 */
open class PlayerControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractMediaController(context, attrs, defStyleAttr),
    PlayerGesture.OnScrollListener,
    PlayerGesture.OnSingleTapUpListener {

    protected val mRootView: View by lazy {
        LayoutInflater.from(context).inflate(getRootLayoutId(), this)
    }
    protected val mLoadingView: View? by lazy { mRootView.findViewById(getLoadingViewId()) }
    protected val mBrightnessVolumeView: BrightnessVolumeView? by lazy {
        mRootView.findViewById(getBrightnessVolumeViewId())
    }
    protected val mPlayPause: ImageView? by lazy { mRootView.findViewById(getPlayPauseId()) }
    protected val mCenterPlayPause: ImageView? by lazy { mRootView.findViewById(getCenterPlayPauseId()) }
    protected val mCurrentTime: TextView? by lazy { mRootView.findViewById(getCurrentTimeId()) }
    protected val mTotalTime: TextView? by lazy { mRootView.findViewById(getTotalTimeId()) }
    protected val mPlayerSeekBar: SeekBar? by lazy { mRootView.findViewById(getPlayerSeekBarId()) }

    protected val mPlayerGesture: PlayerGesture by lazy {
        PlayerGesture(context).apply {
            needLeftRightSeek(enableSeekGesture)
            setOnScrollListener(this@PlayerControllerView)
            setOnSingleTapUpListener(this@PlayerControllerView)
        }
    }

    /** 是否在拖动进度条 */
    protected var isDragging = false

    /** 默认开启手势 */
    protected var enableGesture = true

    /** 默认开启seek手势 */
    protected var enableSeekGesture = true

    /** 是否需要执行手势seek */
    protected var toGestureSeek = false

    /** 执行手势seek位置 */
    protected var gestureSeekPosition: Long = 0

    open fun getRootLayoutId(): Int {
        return R.layout.player_widget_player_controller_view
    }

    open fun getRenderViewId(): Int {
        return R.id.player_render_view
    }

    open fun getLoadingViewId(): Int {
        return R.id.player_loading_view
    }

    open fun getBrightnessVolumeViewId(): Int {
        return R.id.player_brightness_volume_view
    }

    open fun getPlayPauseId(): Int {
        return R.id.player_play_pause
    }

    open fun getCenterPlayPauseId(): Int {
        return R.id.player_center_play_pause
    }

    open fun getCurrentTimeId(): Int {
        return R.id.player_current_time
    }

    open fun getTotalTimeId(): Int {
        return R.id.player_total_time
    }

    open fun getPlayerSeekBarId(): Int {
        return R.id.player_seek_bar
    }

    init {
        initView()
    }

    private fun initView() {
        initRendView(mRootView.findViewById(getRenderViewId()))

        initPlayPause()

        initSeekBar()
    }

    protected open fun initPlayPause() {
        mPlayPause?.let { playPause ->
            playPause.tag = false
            playPause.setOnClickListener {
                if (it.tag as Boolean) {
                    pause()
                    mCenterPlayPause?.visibility = View.VISIBLE
                } else {
                    play()
                }
            }
        }
        mCenterPlayPause?.setOnClickListener {
            play()
            it.visibility = View.GONE
        }
    }

    protected open fun initSeekBar() {
        mPlayerSeekBar?.let { playerSeekBar ->
            playerSeekBar.max = 1000
            playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                var newPosition: Long = 0
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (!fromUser) { // 不是用户主动拉动进度条，则不需要seek
                        return
                    }
                    mMediaPlayer?.let { mp ->
                        newPosition = mp.getDuration() * progress / seekBar.max
                        mCurrentTime?.text = formatTime(newPosition)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isDragging = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    isDragging = false
                    mMediaPlayer?.seekTo(newPosition)
                }
            })
        }
    }

    protected fun setSeekbarProgress(currentPosition: Long, duration: Long) {
        mPlayerSeekBar?.let { playerSeekBar ->
            val progress = if (duration > 0) {
                val pos: Long = playerSeekBar.max * currentPosition / duration
                pos.toInt()
            } else {
                0
            }
            playerSeekBar.progress = progress
        }
    }

    override fun onCompletion(mp: IMediaPlayer) {
        super.onCompletion(mp)
        if (!isDragging) {
            mPlayerSeekBar?.apply {
                progress = max
            }
        }
    }

    override fun autoUpdateInfo(mp: AbstractMediaPlayer) {
        if (!toGestureSeek && !isDragging) { // 非手势操作时，才执行自动更新进度
            super.autoUpdateInfo(mp)
            setSeekbarProgress(mp.getCurrentPosition(), mp.getDuration())
        }
    }

    override fun onBufferingUpdate(mp: IMediaPlayer) {
        super.onBufferingUpdate(mp)
        mLoadingView?.let {
            if (mp.isBuffering() && !mp.isPlaying()) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    protected open fun getPlayIconResId(): Int {
        return R.drawable.player_xml_vector_play_icon
    }

    protected open fun getPauseIconResId(): Int {
        return R.drawable.player_xml_vector_pause_icon
    }

    override fun updatePlayerState(isPlaying: Boolean) {
        super.updatePlayerState(isPlaying)
        mPlayPause?.let {
            it.tag = isPlaying
            if (isPlaying) {
                it.setImageResource(getPauseIconResId())
            } else {
                it.setImageResource(getPlayIconResId())
            }
        }
        mCenterPlayPause?.let {
            if (isPlaying) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }
    }

    override fun updateCurrentPosition(position: Long) {
        super.updateCurrentPosition(position)
        mCurrentTime?.text = formatTime(position)
    }

    override fun updateDuration(duration: Long) {
        super.updateDuration(duration)
        mTotalTime?.text = formatTime(duration)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (enableGesture) {
            mPlayerGesture.onTouchEvent(event, width)
        } else super.onTouchEvent(event)
    }

    /**
     * 手势总开关
     *
     * @param enable
     */
    fun setPlayerGesture(enable: Boolean) {
        enableGesture = enable
    }

    /**
     * seek手势开关
     *
     * @param enable
     */
    fun setPlayerSeekGesture(enable: Boolean) {
        enableSeekGesture = enable
        mPlayerGesture.needLeftRightSeek(enableSeekGesture)
    }

    override fun onLeftSideUpDown(isUp: Boolean) {
        mBrightnessVolumeView?.showScreenBrightness(isUp)
    }

    override fun onRightSideUpDown(isUp: Boolean) {
        mBrightnessVolumeView?.showVolume(isUp)
    }

    override fun onFingerUp() {
        if (toGestureSeek) {
            seekTo(gestureSeekPosition)
            toGestureSeek = false
        }
        mBrightnessVolumeView?.hide()
    }

    override fun onScrollLeftRight(width: Int, distance: Float) {
        mMediaPlayer?.let {
            if (it.isInPlaybackState()) {
                if (!toGestureSeek) {
                    gestureSeekPosition = it.getCurrentPosition()
                    toGestureSeek = true
                }
                val percent = it.getDuration() / width.toFloat()
                val seekChange = (distance * percent).toLong()
                gestureSeekPosition -= seekChange
                if (gestureSeekPosition > it.getDuration()) {
                    gestureSeekPosition = it.getDuration()
                } else if (gestureSeekPosition < 0) {
                    gestureSeekPosition = 0
                }

                mCurrentTime?.text = formatTime(gestureSeekPosition)
                setSeekbarProgress(gestureSeekPosition, it.getDuration())
            }
        }
    }

    override fun onFingerDown() {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    /**
     * 格式化时间
     *
     * @param timeMs
     * @return
     */
    protected fun formatTime(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}