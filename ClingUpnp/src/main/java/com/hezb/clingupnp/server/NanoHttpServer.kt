package com.hezb.clingupnp.server

import android.content.Context
import android.provider.MediaStore
import com.hezb.clingupnp.model.MediaInfo
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.util.regex.Pattern

/**
 * Project Name: HPlayer
 * File Name:    NanoHttpServer
 *
 * Description: http服务器.
 *
 * @author  hezhubo
 * @date    2022年03月05日 18:16
 */
class NanoHttpServer(context: Context, port: Int) : NanoHTTPD(port) {

    companion object {
        const val SESSION_URI_VIDEO = "/video"
        const val SESSION_URI_AUDIO = "/audio"
        const val SESSION_URI_IMAGE = "/image"
    }

    private var mContext: Context? = context.applicationContext

    /**
     * 释放context引用
     */
    fun release() {
        mContext = null
    }

    override fun serve(session: IHTTPSession): Response {
        try {
            val idParams = session.parameters?.get("id")
            val id = if (!idParams.isNullOrEmpty()) {
                idParams[0]
            } else {
                "0"
            }
            val mediaInfo = queryMedia(session.uri, id) ?: return super.serve(session)
            val mediaPath = mediaInfo.path ?: return super.serve(session)

            val inputStream = FileInputStream(File(mediaPath))
            val responseMimeType = getMimeTypeForFile(mediaPath)
            val fileLength = inputStream.available()

            val requestRange = session.headers["range"]
            if (requestRange == null) {
                return newFixedLengthResponse(Response.Status.OK, responseMimeType, inputStream, fileLength.toLong())
            } else {
                // 处理seek
                val matcher = Pattern.compile("bytes=(\\d+)-(\\d*)").matcher(requestRange)
                matcher.find()
                val start: Long = matcher.group(1)?.toLong() ?: 0
                inputStream.skip(start)

                val restLength: Long = fileLength - start
                val response = newFixedLengthResponse(
                    Response.Status.PARTIAL_CONTENT,
                    responseMimeType,
                    inputStream,
                    restLength
                )
                val contentRange = "bytes ${start}-${fileLength - 1}/${fileLength}"
                response.addHeader("Content-Range", contentRange)
                return response
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return super.serve(session)
    }

    /**
     * 查媒体数据库
     *
     * @param sessionUri
     * @param id
     */
    private fun queryMedia(sessionUri: String?, id: String): MediaInfo? {
        val queryUri = when (sessionUri) {
            SESSION_URI_VIDEO -> {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            SESSION_URI_AUDIO -> {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            SESSION_URI_IMAGE -> {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            else -> {
                MediaStore.Files.getContentUri("external")
            }
        }
        val cursor = mContext?.contentResolver?.query(
            queryUri,
            null,
            "(_id = $id)",
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val mediaInfo = MediaInfo()
                mediaInfo.id = id
                mediaInfo.mimeType = cursor.getString(cursor.getColumnIndex("mime_type"))
                mediaInfo.path = cursor.getString(cursor.getColumnIndex("_data"))
                mediaInfo.displayName = cursor.getString(cursor.getColumnIndex("_display_name"))
                mediaInfo.title = cursor.getString(cursor.getColumnIndex("title"))
                mediaInfo.size = cursor.getLong(cursor.getColumnIndex("_size"))
                mediaInfo.dateModified = cursor.getLong(cursor.getColumnIndex("date_modified"))
                if (mediaInfo.mimeType?.contains("audio") == true
                    || mediaInfo.mimeType?.contains("video") == true
                ) {
                    mediaInfo.duration = cursor.getLong(cursor.getColumnIndex("duration"))
                    mediaInfo.artist = cursor.getString(cursor.getColumnIndex("artist"))
                    if (mediaInfo.mimeType?.contains("video") == true) {
                        mediaInfo.resolution = cursor.getString(cursor.getColumnIndex("resolution"))
                    }
                }
                cursor.close()
                return mediaInfo
            }
            cursor.close()
        }
        return null
    }

}