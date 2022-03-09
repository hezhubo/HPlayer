package com.hezb.player.core

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.Surface

/**
 * Project Name: HPlayer
 * File Name:    AbstractMediaPlayer
 *
 * Description: 播放器基类.
 *
 * @author  hezhubo
 * @date    2022年03月02日 15:37
 */
abstract class AbstractMediaPlayer : IMediaPlayer {

    /** 所有有可能的播放状态 */
    companion object {
        /** 未知状态 */
        const val STATE_UNKNOWN = -2
        /** 错误 */
        const val STATE_ERROR = -1
        /** 初始化 */
        const val STATE_IDLE = 0
        /** 预处理中 */
        const val STATE_PREPARING = 1
        /** 预处理完成 */
        const val STATE_PREPARED = 2
        /** 播放中 */
        const val STATE_PLAYING = 3
        /** 暂停 */
        const val STATE_PAUSED = 4
        /** 播放完成 */
        const val STATE_PLAYBACK_COMPLETED = 5
        /** 播放停止 */
        const val STATE_STOP = 6
        /** 播放已释放 */
        const val STATE_RELEASE = 7
    }

    /** 当前播放器状态 */
    var currentState = STATE_UNKNOWN
        protected set
    /** 目标状态 */
    var targetState = STATE_UNKNOWN
        protected set

    protected var mediaModel: MediaModel? = null

    protected var mAudioManager: AudioManager? = null
    protected var mSurface: Surface? = null
    /** 缓冲标记 */
    protected var buffering: Boolean = false
    /** 当前缓冲进度 0-100 */
    protected var bufferPercentage = 0

    private var mPlayerCallback: IMediaPlayer.PlayerCallback? = null

    override fun setMediaSource(context: Context, mediaModel: MediaModel) {
        this.mediaModel = mediaModel
        if (mAudioManager == null) {
            mAudioManager =
                context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }

    override fun prepareAsync() {
        mAudioManager?.requestAudioFocus(
            null,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun stop() {
        mAudioManager?.abandonAudioFocus(null)
    }

    override fun release() {
        mAudioManager?.abandonAudioFocus(null)
        mAudioManager = null
    }

    override fun getPlayUri(): Uri? {
        return mediaModel?.uri
    }

    override fun setSurface(surface: Surface?) {
        mSurface = surface
    }

    override fun releaseSurface(surface: Surface?) {
        if (surface == null || surface == mSurface) {
            mSurface = null
        }
    }

    /**
     * @return 播放器内核实体
     */
    abstract fun getMediaPlayer(): Any?

    /**
     * @return 是否在播放状态
     */
    open fun isInPlaybackState(): Boolean {
        return getMediaPlayer() != null && currentState >= STATE_PREPARED && currentState <= STATE_PLAYBACK_COMPLETED
    }

    fun setPlayerCallback(callback: IMediaPlayer.PlayerCallback) {
        mPlayerCallback = callback
    }

    fun removePlayerCallback() {
        mPlayerCallback = null
    }

    override fun isBuffering(): Boolean {
        if (getMediaPlayer() != null) {
            return buffering
        }
        return false
    }

    override fun getBufferedPercentage(): Int {
        if (getMediaPlayer() != null) {
            return bufferPercentage
        }
        return 0
    }

    protected fun notifyOnPreparing() {
        mPlayerCallback?.onPreparing(this)
    }

    protected fun notifyOnPrepared() {
        mPlayerCallback?.onPrepared(this)
    }

    protected fun notifyOnCompletion() {
        mPlayerCallback?.onCompletion(this)
    }

    protected fun notifyOnBufferingUpdate() {
        mPlayerCallback?.onBufferingUpdate(this)
    }

    protected fun notifyOnSeekComplete() {
        mPlayerCallback?.onSeekComplete(this)
    }

    protected fun notifyOnVideoSizeChanged(width: Int, height: Int) {
        mPlayerCallback?.onVideoSizeChanged(this, width, height)
    }

    protected fun notifyOnError(what: Int, extra: Int): Boolean {
        return mPlayerCallback?.onError(this, what, extra) ?: false
    }

    protected fun notifyOnInfo(what: Int, extra: Int): Boolean {
        return mPlayerCallback?.onInfo(this, what, extra) ?: false
    }
}