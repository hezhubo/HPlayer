package com.hezb.hplayer.entity;

import com.hezb.hplayer.R;

import java.io.Serializable;

/**
 * 文件信息
 * Created by hezb on 2016/1/22.
 */
public class FileInfo implements Serializable {

    public static final int TYPE_FOLDER = 0;// 文件夹
    public static final int TYPE_VIDEO = 1;// 视频

    private int type;// 类型
    private String path;// 路径
    private String name;// 展示名称
    private int icon;// 图标资源
    private int childFolderCount;// 子文件夹数目
    private long length;// 文件大小
    private long lastModified;// 最后修改时间

    public FileInfo(int type, String path) {
        this.type = type;
        this.path = path;
        switch (type) {
            case TYPE_FOLDER:
                icon = R.drawable.folder_icon;
                break;
            case TYPE_VIDEO:
                icon = R.drawable.video_icon;
                break;
            default:
                break;
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getChildFolderCount() {
        return childFolderCount;
    }

    public void setChildFolderCount(int childFolderCount) {
        this.childFolderCount = childFolderCount;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
