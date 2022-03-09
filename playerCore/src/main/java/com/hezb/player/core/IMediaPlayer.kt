package com.hezb.player.core

import android.content.Context
import android.net.Uri
import android.view.Surface

/**
 * Project Name: HPlayer
 * File Name:    IMediaPlayer
 *
 * Description: 播放器基础接口.
 *
 * @author  hezhubo
 * @date    2022年03月02日 15:23
 */
interface IMediaPlayer {

    /**
     * 设置数据源
     *
     * @param context
     * @param mediaModel
     */
    fun setMediaSource(context: Context, mediaModel: MediaModel)

    /**
     * 设置播放器配置，设置完数据源后才会生效
     *
     * @param playerOptions
     */
    fun setOptions(playerOptions: List<PlayerOption>)

    /**
     * 异步加载
     */
    fun prepareAsync()

    /**
     * 播放
     */
    fun start()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()

    /**
     * 释放内核
     */
    fun release()

    /**
     * 跳转
     *
     * @param pos
     */
    fun seekTo(pos: Long)

    /**
     * 播放地址
     */
    fun getPlayUri(): Uri?

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean

    /**
     * 是否正在缓冲
     */
    fun isBuffering(): Boolean

    /**
     * 获取当前播放时间
     */
    fun getCurrentPosition(): Long

    /**
     * 获取时长
     */
    fun getDuration(): Long

    /**
     * 缓存进度百分比
     */
    fun getBufferedPercentage(): Int

    /**
     * 网络速度
     */
    fun getNetSpeed(): Long

    /**
     * 获取视频宽
     */
    fun getVideoWidth(): Int

    /**
     * 获取视频高
     */
    fun getVideoHeight(): Int

    /**
     * 设置视频输出
     *
     * @param surface
     */
    fun setSurface(surface: Surface?)

    /**
     * 释放渲染
     *
     * @param surface
     */
    fun releaseSurface(surface: Surface?)

    /**
     * 设置音量
     *
     * @param volume
     */
    fun setVolume(volume: Float)

    // -------- 播放器回调 --------
    interface PlayerCallback {
        fun onPreparing(mp: IMediaPlayer)
        fun onPrepared(mp: IMediaPlayer)
        fun onCompletion(mp: IMediaPlayer)
        fun onBufferingUpdate(mp: IMediaPlayer)
        fun onSeekComplete(mp: IMediaPlayer)
        fun onVideoSizeChanged(mp: IMediaPlayer, width: Int, height: Int)
        fun onError(mp: IMediaPlayer, what: Int, extra: Int): Boolean
        fun onInfo(mp: IMediaPlayer, what: Int, extra: Int): Boolean
    }

}