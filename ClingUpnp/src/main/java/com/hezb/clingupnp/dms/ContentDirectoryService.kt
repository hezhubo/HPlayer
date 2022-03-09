package com.hezb.clingupnp.dms

import android.content.Context
import com.hezb.clingupnp.AndroidDIDLParser
import com.hezb.clingupnp.HttpServerService
import com.hezb.clingupnp.util.ContentResolverUtil
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.MusicTrack
import org.fourthline.cling.support.model.item.VideoItem
import org.seamless.util.MimeType

/**
 * Project Name: HPlayer
 * File Name:    ContentDirectoryService
 *
 * Description: 媒体内容目录服务.
 *
 * @author  hezhubo
 * @date    2022年03月06日 23:07
 */
class ContentDirectoryService(context: Context) : AbstractContentDirectoryService() {

    companion object {
        const val CONTAINER_ROOT_ID = "0"
        const val CONTAINER_VIDEO_ID = "1"
        const val CONTAINER_AUDIO_ID = "2"
        const val CONTAINER_IMAGE_ID = "3"
    }

    private val rootContent = DIDLContent().also {
        val videoContainer = Container()
        videoContainer.clazz = DIDLObject.Class("object.container.storageFolder")
        videoContainer.id = CONTAINER_VIDEO_ID
        videoContainer.parentID = CONTAINER_ROOT_ID
        videoContainer.title = "Video"
        videoContainer.writeStatus = WriteStatus.NOT_WRITABLE
        videoContainer.childCount = 0
        it.addContainer(videoContainer)

        val audioContainer = Container()
        audioContainer.clazz = DIDLObject.Class("object.container.storageFolder")
        audioContainer.id = CONTAINER_AUDIO_ID
        audioContainer.parentID = CONTAINER_ROOT_ID
        audioContainer.title = "Audio"
        audioContainer.childCount = 0
        it.addContainer(audioContainer)

        val imageContainer = Container()
        imageContainer.clazz = DIDLObject.Class("object.container.storageFolder")
        imageContainer.id = CONTAINER_IMAGE_ID
        imageContainer.parentID = CONTAINER_ROOT_ID
        imageContainer.title = "Image"
        imageContainer.childCount = 0
        it.addContainer(imageContainer)
    }

    private var videoContent: DIDLContent? = null
    private var audioContent: DIDLContent? = null
    private var imageContent: DIDLContent? = null

    init {
        // 此处无脑的查系统媒体数据库，正常实现应考虑查询时机、数据更新等问题
        val ip = HttpServerService.getLocalIpByWifi(context)
        if (!ip.isNullOrEmpty()) {
            // 视频
            val videoList = ContentResolverUtil.queryVideos(context)
            if (videoList.isNotEmpty()) {
                videoContent = DIDLContent().apply {
                    for (video in videoList) {
                        val videoItem = VideoItem()
                        videoItem.parentID = CONTAINER_VIDEO_ID
                        videoItem.id = video.id
                        videoItem.title = video.title
                        videoItem.actors = arrayOf(PersonWithRole(video.artist))
                        val mimeType = video.mimeType?.let {
                            val mimeTypeSplit = it.split("/")
                            return@let MimeType(mimeTypeSplit[0], mimeTypeSplit[1])
                        } ?: MimeType()
                        val url = HttpServerService.getVideoUrl(ip, video.id)
                        val res = Res(mimeType, video.size, url)
                        res.duration = video.getFormatDuration()
                        res.resolution = video.resolution
                        videoItem.addResource(res)
                        addItem(videoItem)
                    }
                    rootContent.containers[0].childCount = items.size
                }
            }

            // 音频
            val audioList = ContentResolverUtil.queryAudios(context)
            if (audioList.isNotEmpty()) {
                audioContent = DIDLContent().apply {
                    for (audio in audioList) {
                        val musicTrack = MusicTrack()
                        musicTrack.parentID = CONTAINER_AUDIO_ID
                        musicTrack.id = audio.id
                        musicTrack.title = audio.title
                        musicTrack.artists = arrayOf(PersonWithRole(audio.artist))
                        val mimeType = audio.mimeType?.let {
                            val mimeTypeSplit = it.split("/")
                            return@let MimeType(mimeTypeSplit[0], mimeTypeSplit[1])
                        } ?: MimeType()
                        val url = HttpServerService.getAudioUrl(ip, audio.id)
                        val res = Res(mimeType, audio.size, url)
                        res.duration = audio.getFormatDuration()
                        musicTrack.addResource(res)
                        addItem(musicTrack)
                    }
                    rootContent.containers[1].childCount = items.size
                }
            }

            // 图片
            val imageList = ContentResolverUtil.queryImages(context)
            if (imageList.isNotEmpty()) {
                imageContent = DIDLContent().apply {
                    for (image in imageList) {
                        val imageItem = ImageItem()
                        imageItem.parentID = CONTAINER_IMAGE_ID
                        imageItem.id = image.id
                        imageItem.title = image.title
                        val mimeType = image.mimeType?.let {
                            val mimeTypeSplit = it.split("/")
                            return@let MimeType(mimeTypeSplit[0], mimeTypeSplit[1])
                        } ?: MimeType()
                        val url = HttpServerService.getImageUrl(ip, image.id)
                        val res = Res(mimeType, image.size, url)
                        imageItem.addResource(res)
                        addItem(imageItem)
                    }
                    rootContent.containers[2].childCount = items.size
                }
            }
        }
    }

    override fun browse(
        objectID: String?,
        browseFlag: BrowseFlag?,
        filter: String?,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<out SortCriterion>?
    ): BrowseResult {
        when (objectID) {
            CONTAINER_VIDEO_ID -> {
                videoContent?.let {
                    val count = it.items.size.toLong()
                    return BrowseResult(AndroidDIDLParser().generate(it), count, count)
                }
            }
            CONTAINER_AUDIO_ID -> {
                audioContent?.let {
                    val count = it.items.size.toLong()
                    return BrowseResult(AndroidDIDLParser().generate(it), count, count)
                }
            }
            CONTAINER_IMAGE_ID -> {
                imageContent?.let {
                    val count = it.items.size.toLong()
                    return BrowseResult(AndroidDIDLParser().generate(it), count, count)
                }
            }
            else -> {
                return BrowseResult(AndroidDIDLParser().generate(rootContent), 3, 3)
            }
        }
        return BrowseResult(AndroidDIDLParser().generate(DIDLContent()), 0, 0)
    }
}