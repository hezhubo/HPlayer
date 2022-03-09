package com.hezb.clingupnp.model

import android.os.Parcel
import android.os.Parcelable
import com.hezb.clingupnp.util.FormatUtil

/**
 * Project Name: HPlayer
 * File Name:    MediaInfo
 *
 * Description: 媒体信息.
 *
 * @author  hezhubo
 * @date    2022年03月03日 00:50
 */
class MediaInfo() : Parcelable {

    var id: String? = null

    var mimeType: String? = null

    var path: String? = null

    var displayName: String? = null

    var title: String? = null

    var size: Long = 0

    var dateModified: Long = 0

    var duration: Long = 0

    var resolution: String? = null

    var artist: String? = null

    fun getFormatSize(): String {
        return FormatUtil.formatFileSize(size)
    }

    fun getFormatDuration(): String {
        return FormatUtil.formatTime(duration)
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        mimeType = parcel.readString()
        path = parcel.readString()
        displayName = parcel.readString()
        title = parcel.readString()
        size = parcel.readLong()
        dateModified = parcel.readLong()
        duration = parcel.readLong()
        resolution = parcel.readString()
        artist = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(mimeType)
        parcel.writeString(path)
        parcel.writeString(displayName)
        parcel.writeString(title)
        parcel.writeLong(size)
        parcel.writeLong(dateModified)
        parcel.writeLong(duration)
        parcel.writeString(resolution)
        parcel.writeString(artist)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaInfo> {
        override fun createFromParcel(parcel: Parcel): MediaInfo {
            return MediaInfo(parcel)
        }

        override fun newArray(size: Int): Array<MediaInfo?> {
            return arrayOfNulls(size)
        }
    }

}