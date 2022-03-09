package com.hezb.hplayer.model

import com.hezb.clingupnp.model.MediaInfo

/**
 * Project Name: HPlayer
 * File Name:    ContentInfo
 *
 * Description: 文件信息.
 *
 * @author  hezhubo
 * @date    2022年03月06日 19:34
 */
class ContentInfo {

    companion object {
        const val TYPE_UNKNOWN = -1
        const val TYPE_DIRECTORY = 0
        const val TYPE_VIDEO = 1
        const val TYPE_AUDIO = 2
        const val TYPE_IMAGE = 3
    }

    var type: Int = TYPE_UNKNOWN

    var id: String? = null

    var parentId: String? = null

    var title: String? = null

    var childCount: Int = 0

    var mediaInfo: MediaInfo? = null

}