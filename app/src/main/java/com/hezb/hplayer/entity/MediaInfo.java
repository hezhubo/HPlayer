package com.hezb.hplayer.entity;

import java.io.Serializable;

/**
 * 视频信息
 * Created by hezb on 2016/1/15.
 */
public class MediaInfo implements Serializable {

    private String name;// Media.DISPLAY_NAME 带后缀的名字
    private String path;// DATA 路径
    private long size;// SIZE
    private String title;// TITLE 不带后缀的名字
    private long time;// DATE_MODIFIED 最后修改时间

    //DLNA用到的
    private String mediaID;
    private String mimeType;// 类型
    private long duration;// 时长
    private String resolution;// 分辨率
    private String artist;

    private String thumbnailPath;// 视频缩略图路径

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}
