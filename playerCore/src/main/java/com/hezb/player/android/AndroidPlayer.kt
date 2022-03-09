package com.hezb.player.android

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.Surface
import com.hezb.player.core.*
import java.io.IOException

/**
 * Project Name: HPlayer
 * File Name:    AndroidPlayer
 *
 * Description: Android 播放器.
 *
 * @author  hezhubo
 * @date    2022年03月02日 15:49
 */
open class AndroidPlayer : AbstractMediaPlayer() {

    private var mMediaPlayer: MediaPlayer? = null

    override fun getMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    override fun setMediaSource(context: Context, mediaModel: MediaModel) {
        super.setMediaSource(context, mediaModel)
        mMediaPlayer.let { player ->
            if (player == null) {
                mMediaPlayer = createPlayer().also {
                    setMediaSource(it, context, mediaModel)
                }
            } else {
                player.reset()
                setMediaSource(player, context, mediaModel)
            }
        }
    }

    /**
     * 创建Android播放器实例
     */
    private fun createPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC) // 设置音频流类型
            // 设置各监听器
            setOnPreparedListener(mPreparedListener)
            setOnVideoSizeChangedListener(mSizeChangedListener)
            setOnCompletionListener(mCompletionListener)
            setOnErrorListener(mErrorListener)
            setOnInfoListener(mInfoListener)
            setOnBufferingUpdateListener(mBufferingUpdateListener)
            setOnSeekCompleteListener(mSeekCompleteListener)
        }
    }

    private fun setMediaSource(player: MediaPlayer, context: Context, mediaModel: MediaModel) {
        mSurface?.let {
            player.setSurface(it)
        }
        player.isLooping = mediaModel.looping
        if (mediaModel.isMute) {
            player.setVolume(0f, 0f)
        } else {
            player.setVolume(1f, 1f)
        }
        try {
            player.setDataSource(context, mediaModel.uri, mediaModel.headers) // 设置播放地址等参数
            currentState = STATE_IDLE
        } catch (e: IOException) {
            PlayerLog.e("Unable to open content: ${mediaModel.uri}", e)
            currentState = STATE_ERROR
            targetState = STATE_ERROR
            notifyOnError(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
    }

    override fun setOptions(playerOptions: List<PlayerOption>) {
        // TODO mediaOptions 的相应处理
    }

    override fun prepareAsync() {
        super.prepareAsync()
        mMediaPlayer?.let {
            notifyOnPreparing()
            it.prepareAsync() // 异步预处理
            currentState = STATE_PREPARING
        }
    }

    override fun start() {
        targetState = STATE_PLAYING
        if (isInPlaybackState()) {
            mMediaPlayer?.start()
            currentState = STATE_PLAYING
        }
    }

    override fun pause() {
        targetState = STATE_PAUSED
        if (isInPlaybackState()) {
            mMediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    currentState = STATE_PAUSED
                }
            }
        }
    }

    override fun stop() {
        super.stop()
        mMediaPlayer?.stop()
        currentState = STATE_STOP
        targetState = STATE_STOP
    }

    override fun release() {
        super.release()
        mMediaPlayer?.let {
            it.release()
            currentState = STATE_RELEASE
        }
        targetState = STATE_RELEASE
        mMediaPlayer = null
        bufferPercentage = 0
        buffering = false
        notifyOnBufferingUpdate()
    }

    override fun seekTo(pos: Long) {
        if (isInPlaybackState()) {
            mMediaPlayer?.seekTo(pos.toInt())
            mediaModel?.seekPosition = 0
        } else {
            mediaModel?.seekPosition = pos
        }
    }

    override fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    override fun getCurrentPosition(): Long {
        return mMediaPlayer?.currentPosition?.toLong() ?: 0
    }

    override fun getDuration(): Long {
        if (isInPlaybackState()){
            return  mMediaPlayer?.duration?.toLong() ?: 0
        }
        return 0
    }

    override fun getNetSpeed(): Long {
        return 0
    }

    override fun getVideoWidth(): Int {
        return mMediaPlayer?.videoWidth ?: 0
    }

    override fun getVideoHeight(): Int {
        return mMediaPlayer?.videoHeight ?: 0
    }

    override fun setSurface(surface: Surface?) {
        super.setSurface(surface)
        mMediaPlayer?.setSurface(surface)
    }

    override fun releaseSurface(surface: Surface?) {
        if (surface == null || surface == mSurface) {
            mSurface = null
            mMediaPlayer?.setSurface(null)
        }
    }

    override fun setVolume(volume: Float) {
        mMediaPlayer?.setVolume(volume, volume)
    }

    // -------- listener --------

    private val mPreparedListener = MediaPlayer.OnPreparedListener { mp ->
        currentState = STATE_PREPARED
        notifyOnPrepared()
        // 获取视频跳转位置, 这个跳转位置需要设置完视频地址后马上调用 seekTo() 方法
        val seekToPosition = mediaModel?.seekPosition ?: 0
        if (seekToPosition != 0L && seekToPosition < mp.duration) {
            seekTo(seekToPosition)
        }
        if (targetState == STATE_PLAYING) {
            start()
        }
    }

    private val mCompletionListener = MediaPlayer.OnCompletionListener {
        currentState = STATE_PLAYBACK_COMPLETED
        notifyOnCompletion()
    }

    private val mInfoListener = MediaPlayer.OnInfoListener { _, what, extra ->
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> { // 开始缓冲
                buffering = true
                notifyOnBufferingUpdate()
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END, // 缓冲结束
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START // 开始渲染视频
            -> {
                buffering = false
                notifyOnBufferingUpdate()
            }
        }
        notifyOnInfo(what, extra)
    }

    private val mErrorListener = MediaPlayer.OnErrorListener { _, what, extra ->
        PlayerLog.d("Error: what=%d, extra=%d", what, extra)
        currentState = STATE_ERROR
        notifyOnError(what, extra)
    }

    private val mBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { _, percent ->
        bufferPercentage = percent
        notifyOnBufferingUpdate()
    }

    private val mSeekCompleteListener = MediaPlayer.OnSeekCompleteListener { notifyOnSeekComplete() }

    private val mSizeChangedListener = MediaPlayer.OnVideoSizeChangedListener { _, width, height ->
        notifyOnVideoSizeChanged(width, height)
    }
}