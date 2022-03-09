package com.hezb.player.ijk

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.Surface
import com.hezb.player.core.AbstractMediaPlayer
import com.hezb.player.core.MediaModel
import com.hezb.player.core.PlayerLog
import com.hezb.player.core.PlayerOption
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException

/**
 * Project Name: HPlayer
 * File Name:    IjkPlayer
 *
 * Description: Ijk播放器.
 *
 * @author  hezhubo
 * @date    2022年03月09日 10:12
 */
class IjkPlayer : AbstractMediaPlayer() {

    private var mMediaPlayer: IjkMediaPlayer? = null

    override fun getMediaPlayer(): Any? {
        return mMediaPlayer
    }

    override fun setMediaSource(context: Context, mediaModel: MediaModel) {
        super.setMediaSource(context, mediaModel)
        mMediaPlayer.let { player->
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
     * 创建Ijk播放器实例
     */
    private fun createPlayer(): IjkMediaPlayer {
        return IjkMediaPlayer().apply {
            if (PlayerLog.writeLogs) {
                IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
            } else {
                IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT)
            }
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

    private fun setMediaSource(player: IjkMediaPlayer, context: Context, mediaModel: MediaModel) {
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
        mMediaPlayer?.let { player ->
            if (!playerOptions.isNullOrEmpty()) {
                for (option in playerOptions) {
                    if (option.value is String) {
                        player.setOption(option.category, option.name, option.value as String)
                    } else if (option.value is Long) {
                        player.setOption(option.category, option.name, option.value as Long)
                    }
                }
            }
        } ?: PlayerLog.e("set options error, player is not init!")
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
            if (getDuration() in 1001..pos) {
                // 处理 hls seek到视频末尾，找不到对应ts片段，引起一直loading的问题
                // seek到视频末尾统一修改为seek到总时长前1秒
                mMediaPlayer?.seekTo(getDuration() - 1000)
            } else {
                mMediaPlayer?.seekTo(pos)
            }
            mediaModel?.seekPosition = 0
        } else {
            mediaModel?.seekPosition = pos
        }
    }

    override fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    override fun getCurrentPosition(): Long {
        return mMediaPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Long {
        if (isInPlaybackState()){
            return  mMediaPlayer?.duration ?: 0
        }
        return 0
    }

    override fun getNetSpeed(): Long {
        return mMediaPlayer?.tcpSpeed ?: 0
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

    private val mPreparedListener = IMediaPlayer.OnPreparedListener { mp ->
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

    private val mCompletionListener = IMediaPlayer.OnCompletionListener {
        currentState = STATE_PLAYBACK_COMPLETED
        notifyOnCompletion()
    }

    private val mInfoListener = IMediaPlayer.OnInfoListener { _, what, extra ->
        when (what) {
            IjkMediaPlayer.MEDIA_INFO_BUFFERING_START -> { // 开始缓冲
                buffering = true
                notifyOnBufferingUpdate()
            }
            IjkMediaPlayer.MEDIA_INFO_BUFFERING_END, // 缓冲结束
            IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, // 开始渲染视频
            IjkMediaPlayer.MEDIA_INFO_VIDEO_SEEK_RENDERING_START // seek完成开始渲染视频
            -> {
                buffering = false
                notifyOnBufferingUpdate()
            }
        }
        notifyOnInfo(what, extra)
    }

    private val mErrorListener = IMediaPlayer.OnErrorListener { _, framework_err, impl_err ->
        PlayerLog.d("Error: framework_err=%d, impl_err=%d", framework_err, impl_err)
        currentState = STATE_ERROR
        notifyOnError(framework_err, impl_err)
    }

    private val mBufferingUpdateListener = IMediaPlayer.OnBufferingUpdateListener { _, percent ->
        bufferPercentage = percent
        notifyOnBufferingUpdate()
    }

    private val mSeekCompleteListener =
        IMediaPlayer.OnSeekCompleteListener { notifyOnSeekComplete() }

    private val mSizeChangedListener =
        IMediaPlayer.OnVideoSizeChangedListener { _, width, height, sarNum, sarDen ->
            notifyOnVideoSizeChanged(width, height)
        }

}