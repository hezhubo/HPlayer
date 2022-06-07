package com.hezb.player.exo

import android.content.Context
import android.view.Surface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.hezb.player.core.AbstractMediaPlayer
import com.hezb.player.core.MediaModel
import com.hezb.player.core.PlayerLog
import com.hezb.player.core.PlayerOption

/**
 * Project Name: HPlayer
 * File Name:    ExoPlayer
 *
 * Description: Exo播放器.
 *
 * @author  hezhubo
 * @date    2022年03月09日 11:23
 */
class ExoPlayer : AbstractMediaPlayer() {

    private var mMediaPlayer: ExoPlayer? = null

    override fun getMediaPlayer(): Any? {
        return mMediaPlayer
    }

    override fun setMediaSource(context: Context, mediaModel: MediaModel) {
        super.setMediaSource(context, mediaModel)
        mMediaPlayer.let { player ->
            if (player == null) {
                mMediaPlayer = createPlayer(context).also {
                    setMediaSource(it, context, mediaModel)
                }
            } else {
                setMediaSource(player, context, mediaModel)
            }
        }
    }

    private fun createPlayer(context: Context): ExoPlayer {
        return SimpleExoPlayer.Builder(context).build().apply {
            addListener(mPlayerEventListener)
        }
    }

    private fun setMediaSource(player: ExoPlayer, context: Context, mediaModel: MediaModel) {
        mSurface?.let {
            player.setVideoSurface(it)
        }
        if (mediaModel.looping) {
            player.repeatMode = Player.REPEAT_MODE_ONE
        } else {
            player.repeatMode = Player.REPEAT_MODE_OFF
        }
        if (mediaModel.isMute) {
            player.volume = 0f
        } else {
            player.volume = 1f
        }
        val mediaItemBuilder = MediaItem.Builder()
        mediaItemBuilder.setUri(mediaModel.uri)
        mediaItemBuilder.setMimeType(Util.getAdaptiveMimeTypeForContentType(Util.inferContentType(mediaModel.uri)))
        val userAgent = mediaModel.headers?.get("user_agent") ?: "HPlayer-ExoPlayer"
        val factory = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
        val source = factory.createMediaSource(mediaItemBuilder.build())
        player.setMediaSource(source)
        currentState = STATE_IDLE
    }

    override fun setOptions(playerOptions: List<PlayerOption>) {
        // TODO mediaOptions 的相应处理
    }

    override fun prepareAsync() {
        super.prepareAsync()
        mMediaPlayer?.let {
            notifyOnPreparing()
            it.prepare()
            currentState = STATE_PREPARING
        }
    }

    override fun start() {
        targetState = STATE_PLAYING
        if (currentState == STATE_PLAYBACK_COMPLETED) {
            mMediaPlayer?.apply {
                seekTo(0)
            }
            currentState = STATE_PLAYING
        } else {
            if (isInPlaybackState()) {
                mMediaPlayer?.play()
                currentState = STATE_PLAYING
            }
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
        seekTo(pos, SeekParameters.EXACT)
    }

    fun quickSeekTo(pos: Long) {
        seekTo(pos, SeekParameters.PREVIOUS_SYNC)
    }

    fun seekTo(pos: Long, mode: SeekParameters) {
        if (isInPlaybackState()) {
            mMediaPlayer?.setSeekParameters(mode)
            mMediaPlayer?.seekTo(pos)
            mediaModel?.seekPosition = 0
        } else {
            mediaModel?.seekPosition = pos
        }
    }

    override fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    override fun getCurrentPosition(): Long {
        mMediaPlayer?.let {
            if (it.currentPosition > 0) {
                return it.currentPosition
            }
        }
        return 0
    }

    override fun getDuration(): Long {
        if (isInPlaybackState()){
            return  mMediaPlayer?.duration ?: 0
        }
        return 0
    }

    override fun getNetSpeed(): Long {
        return 0
    }

    override fun getVideoWidth(): Int {
        return mMediaPlayer?.videoSize?.width ?: 0
    }

    override fun getVideoHeight(): Int {
        return mMediaPlayer?.videoSize?.height ?: 0
    }

    override fun setSurface(surface: Surface?) {
        super.setSurface(surface)
        mMediaPlayer?.setVideoSurface(surface)
    }

    override fun releaseSurface(surface: Surface?) {
        if (surface == null || surface == mSurface) {
            mSurface = null
            mMediaPlayer?.setVideoSurface(null)
        }
    }

    override fun setVolume(volume: Float) {
        mMediaPlayer?.volume = volume
    }

    // -------- listener --------

    private val mPlayerEventListener = object : Player.Listener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    buffering = false
                    notifyOnBufferingUpdate()
                    if (!isInPlaybackState()) {
                        currentState = STATE_PREPARED
                        notifyOnPrepared()
                        // 获取视频跳转位置, 这个跳转位置需要设置完视频地址后马上调用 seekTo() 方法
                        val seekToPosition = mediaModel?.seekPosition ?: 0
                        if (seekToPosition != 0L && seekToPosition < mMediaPlayer?.duration ?: 0) {
                            seekTo(seekToPosition)
                        }
                    }
                    if (targetState == STATE_PLAYING) {
                        start()
                    }
                }

                Player.STATE_ENDED -> {
                    buffering = false
                    notifyOnBufferingUpdate()
                    currentState = STATE_PLAYBACK_COMPLETED
                    notifyOnCompletion()
                }
                Player.STATE_BUFFERING -> {
                    buffering = true
                    notifyOnBufferingUpdate()
                }
                Player.STATE_IDLE -> {
                    currentState = STATE_IDLE
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            PlayerLog.d("Error: type=%d, message=%s", error.type, error.message)
            currentState = STATE_ERROR
            notifyOnError(error.type, 0)
        }

        override fun onPositionDiscontinuity(reason: Int) {
            notifyOnSeekComplete()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                currentState = STATE_PLAYING
            } else {
                if (currentState == STATE_PLAYING) {
                    currentState = STATE_PAUSED
                }
                notifyOnBufferingUpdate()
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            notifyOnVideoSizeChanged(videoSize.width, videoSize.height)
        }
    }

}