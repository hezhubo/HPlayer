package com.hezb.hplayer.localvideofilter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.hezb.hplayer.entity.MediaInfo;
import com.hezb.hplayer.util.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.ThumbnailUtils;

/**
 * 视频查找任务
 * Created by hezb on 2016/1/15.
 */
public class MediaQueryTask extends AsyncTask<Context, Integer, List<MediaInfo>> {

    private ArrayList<MediaInfo> mediaInfoList;
    private QueryListener listener;

    public MediaQueryTask() {
        mediaInfoList = new ArrayList<>();
    }

    public void setQueryListener(QueryListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<MediaInfo> doInBackground(Context... params) {
        Context context = params[0];
//        Cursor inCursor = context.getContentResolver()
//                .query(MediaStore.Video.Media.INTERNAL_CONTENT_URI,
//                        null, null, null, null);
//        getMediaInfo(context, inCursor);

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Cursor exCursor = context.getContentResolver()
                    .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            null, null, null, null);
            getMediaInfo(context, exCursor);
        }

        return mediaInfoList;
    }

    @Override
    protected void onPostExecute(List<MediaInfo> mediaInfoList) {
        if (listener != null) {
            listener.onResult(mediaInfoList);
        }
    }

    private void getMediaInfo(Context context, Cursor cursor) {
        while (cursor.moveToNext()) {
            String path = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            File file = new File(path);
            if (!file.exists()) {// 文件不存在
                continue;
            }
            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setPath(path);
            mediaInfo.setName(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
            mediaInfo.setSize(Integer.parseInt(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.SIZE))));
            mediaInfo.setTitle(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
            mediaInfo.setTime(Integer.parseInt(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))));

            mediaInfo.setMediaID(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media._ID)));
            mediaInfo.setMimeType(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
            mediaInfo.setDuration(cursor.getLong(
                    cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
            mediaInfo.setResolution(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION)));
            mediaInfo.setArtist(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)));

            // 缓存缩略图
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(context, mediaInfo.getPath(),
                    io.vov.vitamio.provider.MediaStore.Video.Thumbnails.MICRO_KIND);
            if (bitmap == null) {// vitamio取不到，用原生方法取
                bitmap = android.media.ThumbnailUtils.createVideoThumbnail(
                    mediaInfo.getPath(), MediaStore.Images.Thumbnails.MICRO_KIND);
            }
            String thumbnailPath = FileManager.saveBitmap(context, bitmap, mediaInfo.getMediaID());
            mediaInfo.setThumbnailPath(thumbnailPath);

            mediaInfoList.add(mediaInfo);

        }

        cursor.close();

    }

    public interface QueryListener {
        void onResult(List<MediaInfo> mediaInfoList);
    }
}
