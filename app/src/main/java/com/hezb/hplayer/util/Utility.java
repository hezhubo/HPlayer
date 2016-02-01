package com.hezb.hplayer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hezb.hplayer.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import io.vov.vitamio.utils.ScreenResolution;

/**
 * 工具类
 * Created by hezb on 2016/1/15.
 */
public class Utility {


    /**
     * 屏幕分辨率
     */
    public static Pair<Integer, Integer> screenPair;

    /**
     * 按 屏幕比例 缩放图片
     */
    public static void resizeImageViewOnScreenSize(Context context, View view, int numColumns,
                                                   int horizontalSpacing, int zoomX, int zoomY) {
        if (view == null) {
            return;
        }
        if (screenPair == null) {
            screenPair = ScreenResolution.getResolution(context);
        }
        android.view.ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = screenPair.first / numColumns - horizontalSpacing * (numColumns - 1);
        layoutParams.height = layoutParams.width * zoomY / zoomX;
    }

    /**
     * 位移动画
     *
     * @param view
     * @param xFrom
     * @param xTo
     * @param yFrom
     * @param yTo
     * @param duration
     */
    public static void translateAnimation(View view, float xFrom, float xTo,
                                          float yFrom, float yTo, long duration) {

        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, xFrom, Animation.RELATIVE_TO_SELF, xTo,
                Animation.RELATIVE_TO_SELF, yFrom, Animation.RELATIVE_TO_SELF, yTo);
        translateAnimation.setFillAfter(false);
        translateAnimation.setDuration(duration);
        view.startAnimation(translateAnimation);
        translateAnimation.startNow();
    }

    private static Toast toast;

    /**
     * 全局系统toast
     */
    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            View v = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
            ((TextView) v.findViewById(R.id.toast_msg)).setText(content);
            toast.setView(v);
        } else {
            ((TextView) toast.getView().findViewById(R.id.toast_msg)).setText(content);
        }
        toast.show();
    }

    public static void showToast(Context context, int content) {
        if (toast == null) {
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            View v = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
            ((TextView) v.findViewById(R.id.toast_msg)).setText(content);
            toast.setView(v);
        } else {
            ((TextView) toast.getView().findViewById(R.id.toast_msg)).setText(content);
        }
        toast.show();
    }

    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    /**
     * 获取手机SD卡本应用缓存大小
     *
     * @return Long size
     */
    public static long getAppCacheSize(Context context) {
        long size = 0;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 外部存储
            File externalCacheDir = context.getExternalCacheDir();
            File externalFilesDir = context.getExternalFilesDir(null);
            size = FileManager.getFolderSize(externalCacheDir);
            size = size + FileManager.getFolderSize(externalFilesDir);
        }
        // 内部存储
        File cacheDir = context.getCacheDir();
        File filesDir = context.getFilesDir();
        size = size + FileManager.getFolderSize(cacheDir);
        size = size + FileManager.getFolderSize(filesDir);
        return size;
    }

    /**
     * 清除app缓存
     */
    public static void delAppCache(Context context) {
        DataCleanManager.cleanInternalCache(context);
        DataCleanManager.cleanFiles(context);
        DataCleanManager.cleanExternalCache(context);
        DataCleanManager.cleanExternalFilesDir(context);
    }

    /**
     * md5加密算法
     *
     * @return 密文
     */
    public static String md5(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

    /**
     * 图片加载器
     */
    private static ImageLoader mImageLoader = ImageLoader.getInstance();
    private static DisplayImageOptions mOptions;
    @SuppressLint("UseSparseArrays")
    private static Map<Integer, DisplayImageOptions> mOptionsMap = new HashMap<Integer, DisplayImageOptions>();

    private static DisplayImageOptions getDisplayImageOptionsFactory(int resId) {
        if (mOptionsMap.containsKey(resId)) {
            return mOptionsMap.get(resId);
        }
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
                .cacheInMemory(true).bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(300));

        if (resId != 0) {
            builder.showImageOnFail(resId).showImageForEmptyUri(resId)
                    .showImageOnLoading(resId);
        }
        mOptions = builder.build();
        mOptionsMap.put(resId, mOptions);
        return mOptions;
    }

    /**
     * 异步加载网络图片
     */
    public static void displayImage(String url, ImageView view,
                                    ImageLoadingListener listener, int defaultImgRes) {
        if (view == null) {
            return;
        }
        mOptions = getDisplayImageOptionsFactory(defaultImgRes);
        mImageLoader.displayImage(url, view, mOptions, listener);
    }
}
