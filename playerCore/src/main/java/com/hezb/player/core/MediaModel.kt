package com.hezb.player.core

import android.net.Uri

/**
 * Project Name: HPlayer
 * File Name:    MediaModel
 *
 * Description: 媒体播放数据.
 *
 * @author  hezhubo
 * @date    2022年03月02日 15:25
 */
class MediaModel @JvmOverloads constructor(val uri: Uri, var headers: Map<String, String>? = null) {

    /** 是否循环播放 */
    var looping = false

    /** 预处理完跳转位置 */
    var seekPosition: Long = 0

    /** 是否静音 */
    var isMute = false

}