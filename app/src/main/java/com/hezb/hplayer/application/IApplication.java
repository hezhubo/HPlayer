package com.hezb.hplayer.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import com.hezb.hplayer.constant.ConstantKey;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;

/**
 * 应用配置
 * Created by hezb on 2016/1/19.
 */
public class IApplication extends Application {

    private SharedPreferences mSharedPreferences;

    public static String videoShotDirPath;
    public static String videoCacheDirPath;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getApplicationContext()
                .getSharedPreferences(ConstantKey.H_PLAYER, Context.MODE_PRIVATE);

        // 设置截图保存路径
        videoShotDirPath = mSharedPreferences.getString(ConstantKey.VIDEO_SHOT_DIR, "");
        if (TextUtils.isEmpty(videoShotDirPath)) {
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File defaultShotDir = new File(dcim, "/HPlayer/VideoShots/");
            if (!defaultShotDir.exists()) {
                defaultShotDir.mkdirs();
            }
            videoShotDirPath = defaultShotDir.getPath();
            mSharedPreferences.edit().putString(ConstantKey.VIDEO_SHOT_DIR, videoShotDirPath).apply();
        }

        // 设置视频缓存路径
        videoCacheDirPath = mSharedPreferences.getString(ConstantKey.VIDEO_CACHE_DIR, "");
        if (TextUtils.isEmpty(videoCacheDirPath)) {
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File defaultCacheDir = new File(dcim, "/HPlayer/VideoCache/");
            if (!defaultCacheDir.exists()) {
                defaultCacheDir.mkdirs();
            }
            videoCacheDirPath = defaultCacheDir.getPath();
            mSharedPreferences.edit().putString(ConstantKey.VIDEO_CACHE_DIR, videoCacheDirPath).apply();
        }

        initImageLoader(this);
    }

    /**
     * 初始化ImageLoader
     */
    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheSize(50 * 1024 * 1024);
//                .writeDebugLogs(); // Remove for release app
        ImageLoader.getInstance().init(config.build());

    }
}
