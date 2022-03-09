package com.hezb.clingupnp.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.hezb.clingupnp.model.MediaInfo

/**
 * Project Name: HPlayer
 * File Name:    ContentResolverUtil
 *
 * Description: ContentResolver工具类.
 *
 * @author  hezhubo
 * @date    2022年03月06日 23:18
 */
object ContentResolverUtil {

    fun queryVideos(context: Context): MutableList<MediaInfo> {
        val videoList = ArrayList<MediaInfo>()
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val mediaInfo = MediaInfo()
                mediaInfo.id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                mediaInfo.mimeType =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
                mediaInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                mediaInfo.displayName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                mediaInfo.title =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                mediaInfo.size =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                mediaInfo.dateModified =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))
                var duration =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                if (duration <= 0) {
                    duration = getVideoDurationFromMediaMetadata(mediaInfo.path)
                }
                mediaInfo.duration = duration
                mediaInfo.resolution =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION))
                mediaInfo.artist =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST))
                videoList.add(mediaInfo)
            }

            cursor.close()
        }
        return videoList
    }

    fun queryAudios(context: Context): MutableList<MediaInfo> {
        val audioList = ArrayList<MediaInfo>()
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val mediaInfo = MediaInfo()
                mediaInfo.id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                mediaInfo.mimeType =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE))
                mediaInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                mediaInfo.displayName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                mediaInfo.title =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                mediaInfo.size =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                mediaInfo.dateModified =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED))
                var duration =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                if (duration <= 0) {
                    duration = getVideoDurationFromMediaMetadata(mediaInfo.path)
                }
                mediaInfo.duration = duration
                mediaInfo.artist =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                audioList.add(mediaInfo)
            }

            cursor.close()
        }
        return audioList
    }

    fun queryImages(context: Context): MutableList<MediaInfo> {
        val imageList = ArrayList<MediaInfo>()
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val mediaInfo = MediaInfo()
                mediaInfo.id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                mediaInfo.mimeType =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                mediaInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                mediaInfo.displayName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                mediaInfo.title =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE))
                mediaInfo.size =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                mediaInfo.dateModified =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
                imageList.add(mediaInfo)
            }

            cursor.close()
        }
        return imageList
    }

    /**
     * 通过MediaMetadataRetriever获取音视频时长
     *
     * @param path
     */
    private fun getVideoDurationFromMediaMetadata(path: String?): Long {
        if (path.isNullOrEmpty()) {
            return 0
        }
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(path)
            return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

}