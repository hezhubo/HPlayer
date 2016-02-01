package com.hezb.hplayer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hezb.hplayer.entity.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 文件操作
 */
public class FileOperate {

    public static final int TYPE_SORT_NAME = 1;
    public static final int TYPE_SORT_SIZE = 2;
    public static final int TYPE_SORT_TIME = 3;
    public static final int TYPE_SORT_TYPE = 4;

    private Context mContext;

    private List<FileInfo> mFileInfoList;

    public FileOperate(Context context, List<FileInfo> fileInfoList) {
        mContext = context;
        mFileInfoList = fileInfoList;
    }

    public void send(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        mContext.startActivity(intent);
    }

    public void send(ArrayList<FileInfo> selectedItem) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (FileInfo fileInfo : selectedItem) {
            File file = new File(fileInfo.getPath());
            Uri u = Uri.fromFile(file);
            uris.add(u);
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        mContext.startActivity(intent);
    }

    /**
     * 排序
     */
    public void sortListItem(int type) {
        if (mFileInfoList == null) {
            return;
        }
        Collections.sort(mFileInfoList, getComparator(type));
    }

    Comparator<FileInfo> comparatorByName = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            File file0 = new File(lhs.getPath());
            File file1 = new File(rhs.getPath());
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                String title0 = lhs.getName();
                String title1 = rhs.getName();
                return title0.compareToIgnoreCase(title1);
            }
        }

    };
    Comparator<FileInfo> comparatorBySize = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            File file0 = new File(lhs.getPath());
            File file1 = new File(rhs.getPath());
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                if (file0.isDirectory() && file1.isDirectory()) {
                    String title0 = lhs.getName();
                    String title1 = rhs.getName();
                    return title0.compareToIgnoreCase(title1);
                } else {
                    return (int) (lhs.getLength() - rhs.getLength());
                }
            }
        }

    };
    Comparator<FileInfo> comparatorByTime = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            File file0 = new File(lhs.getPath());
            File file1 = new File(rhs.getPath());
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                return (int) (lhs.getLastModified() - rhs.getLastModified());
            }
        }

    };
    Comparator<FileInfo> comparatorByType = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            File file0 = new File(lhs.getPath());
            File file1 = new File(rhs.getPath());
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                String title0 = lhs.getName();
                String title1 = rhs.getName();
                String end0 = title0.substring(title0.lastIndexOf(".") + 1,
                        title0.length()).toLowerCase();
                String end1 = title1.substring(title1.lastIndexOf(".") + 1,
                        title1.length()).toLowerCase();
                if (end0.equals(end1)) {
                    return title0.compareToIgnoreCase(title1);
                } else {
                    return end0.compareToIgnoreCase(end1);
                }
            }
        }

    };

    // 按名称排序
    // 按文件名以字母顺序排列。
    //
    // 按大小排序
    // 按文件大小(文件占用的磁盘空间)排序。默认情况下会从最小到最大排列。
    //
    // 按类型排序
    // 按文件类型以字母顺序排列。会将同类文件归并到一起，然后按名称排序。
    //
    // 按修改日期排序
    // 按上次更改文件的日期和时间排序。默认情况下会从最旧到最新排列。
    public Comparator<FileInfo> getComparator(int type) {
        Comparator comparator = null;
        switch (type) {
            case TYPE_SORT_NAME:
                comparator = comparatorByName;
                break;
            case TYPE_SORT_SIZE:
                comparator = comparatorBySize;
                break;
            case TYPE_SORT_TIME:
                comparator = comparatorByTime;
                break;
            case TYPE_SORT_TYPE:
                comparator = comparatorByType;
                break;
        }
        return comparator;
    }

}
