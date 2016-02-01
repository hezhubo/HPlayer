package com.hezb.hplayer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.hezb.hplayer.entity.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作管理器
 * Created by hezb on 2015/12/12.
 */
public class FileManager {

    /**
     * 保存序列化对象到本地文件
     */
    public static boolean saveSerializable(Serializable serializable, File file) {
        boolean success = false;
        FileOutputStream out = null;
        ObjectOutputStream oos = null;

        try {
            out = new FileOutputStream(file);
            oos = new ObjectOutputStream(out);

            oos.writeObject(serializable);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    /**
     * 读取本地文件返回序列化对象
     */
    public static Serializable readSerializableFile(File file) {
        if (!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);

            return (Serializable) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存
     */
    public static boolean saveData(Context context, Serializable serializable, String fileName) {
        if (serializable == null || TextUtils.isEmpty(fileName)) {
            return false;
        }
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            return saveSerializable(serializable, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取
     */
    public static Serializable getData(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file = new File(context.getFilesDir(), fileName);
        return readSerializableFile(file);
    }

    /**
     * 保存图片, 返回图片路径
     */
    public static String saveBitmap(Context context, Bitmap bitmap, String fileName) {
        File dir = context.getFilesDir();
        return saveBitmap(dir, bitmap, fileName);
    }

    /**
     * 保存图片
     *
     * @param dir      父目录
     * @param bitmap   图片资源
     * @param fileName 图片文件名
     * @return 图片路径
     */
    public static String saveBitmap(File dir, Bitmap bitmap, String fileName) {
        if (bitmap == null || TextUtils.isEmpty(fileName) || dir == null) {
            return null;
        }
        FileOutputStream out = null;
        try {
            File file = new File(dir, fileName + ".jpg");//若存视频截图为png格式会花屏
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            bitmap.recycle();
            System.gc();
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取文件夹大小 （递归耗时）
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 计算文件大小
     */
    public static String sizeAddUnit(long size) {
        char[] unit = new char[]{'B', 'K', 'M', 'G'};
        int index = 0;
        int div = 1;
        for (; index < unit.length; index++) {
            div *= 1024;
            if (size < div)
                break;
        }
        div /= 1024;
        if (size % div == 0)
            return String.valueOf(size / div) + unit[index];
        else {
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(size * 1.0 / div) + unit[index];
        }
    }

    /**
     * 获取路径path下的所有文件信息
     */
    public static List<FileInfo> getFilesList(String path) {
        if (path == null) {
            Log.e("The path is null");
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            Log.e("File does not exist :" + path);
            return null;
        }

        return getFilesList(file);
    }

    /**
     * 获取路径path下的所有文件信息
     */
    public static List<FileInfo> getFilesList(File file) {
        if (file == null) {
            Log.e("File is null");
            return null;
        }
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("have no child file :" + file.getPath());
            return null;
        }
        List<FileInfo> listItem = new ArrayList<>();
        for (File childFile : files) { // 遍历子文件
            if (isDisplay(childFile)) {
                if (childFile.isDirectory()) { // 是目录
                    FileInfo fileInfo = new FileInfo(FileInfo.TYPE_FOLDER, childFile.getPath());
                    fileInfo.setName(childFile.getName());
                    if (childFile.listFiles() == null) {
                        fileInfo.setChildFolderCount(0);
                    } else {
                        fileInfo.setChildFolderCount(getDirectoryCount(childFile));
                    }
                    fileInfo.setLastModified(childFile.lastModified());
                    listItem.add(fileInfo);
                } else if(isVideo(childFile)) { // 是视频
                    FileInfo fileInfo = new FileInfo(FileInfo.TYPE_VIDEO, childFile.getPath());
                    fileInfo.setName(childFile.getName());
                    fileInfo.setLastModified(childFile.lastModified());
                    fileInfo.setLength(childFile.length());
                    listItem.add(fileInfo);
                }
            }
        }
        return listItem;
    }

    public static boolean displayAll = false;

    /**
     * 判断该文件是否需要显示
     */
    public static boolean isDisplay(File file) {
        if (!displayAll) {
            if (file.getName().startsWith(".") || file.isHidden()) {
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * 得到文件夹的子文件数量
     */
    public static int getDirectoryCount(File file) {
        int result = 0;
        if (file == null) {
            return result;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return result;
        }
        for (File childFile : files) {
            if (isDisplay(childFile)) {
                result++;
            }
        }
        return result;
    }

    // 视频
    private static final String _MOV = "mov";
    private static final String _3GP = "3gp";
    private static final String _3g2 = "3g2";
    private static final String _WNV = "wmv";
    private static final String _TS = "ts";
    private static final String _F4V = "f4v";
    private static final String _MPEG = "mpeg";
    private static final String _MP4 = "mp4";
    private static final String _M1V = "m1v";
    private static final String _MOD = "mod";
    private static final String _RM = "rm";
    private static final String _RMVB = "rmvb";
    private static final String _VOB = "vob";
    private static final String _DIVX = "divx";
    private static final String _QT = "qt";
    private static final String _MPG = "mpg";
    private static final String _PFV = "pfv";
    private static final String _FLV = "flv";
    private static final String _MKV = "mkv";
    private static final String _AVI = "avi";
    private static final String _ASF = "asf";
    private static final String _M4V = "m4v";
    private static final String _M3U8 = "m3u8";

    /**
     * @return 文件是否为视频
     */
    public static boolean isVideo(File file) {
        if (file != null) {
            return isVideoPath(file.getName());
        }
        return false;
    }

    /**
     * @return 是否为视频路径
     */
    public static boolean isVideoPath(String path) {
        // 有后缀
        if (path.contains(".")) {
            // 后缀
            String end = path.substring(path.lastIndexOf(".") + 1,
                    path.length()).toLowerCase();
            if (end.equals(_MOV) || end.equals(_3GP) ||
                    end.equals(_3g2) || end.equals(_WNV) ||
                    end.equals(_TS) || end.equals(_F4V) ||
                    end.equals(_MPEG) || end.equals(_MP4) ||
                    end.equals(_M1V) || end.equals(_MOD) ||
                    end.equals(_RM) || end.equals(_RMVB) ||
                    end.equals(_VOB) || end.equals(_DIVX) ||
                    end.equals(_QT) || end.equals(_MPG) ||
                    end.equals(_PFV) || end.equals(_FLV) ||
                    end.equals(_MKV) || end.equals(_AVI) ||
                    end.equals(_ASF) || end.equals(_M4V) ||
                    end.equals(_M3U8)) {
                return true;
            }
        }
        return false;
    }

}
