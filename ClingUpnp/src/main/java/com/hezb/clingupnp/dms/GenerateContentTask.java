package com.hezb.clingupnp.dms;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

/**
 * 生成共享内容任务
 * Created by hezb on 2016/1/28.
 */
public class GenerateContentTask extends AsyncTask<Context, Integer, Boolean> {

    private static final String TAG = "GenerateContentTask";

    private String address;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        address = MediaServer.getAddress();
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        if (address == null) {
            return false;
        }

        Context context = params[0];

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            ContentNode rootNode = ContentTree.getRootNode();

            addVideoContent(context, rootNode);

            addAudioContent(context, rootNode);

            addImageContent(context, rootNode);

            return true;
        }

        return false;
    }

    /**
     * 添加视频
     */
    private void addVideoContent(Context context, ContentNode rootNode) {

        Container videoContainer = new Container();
        videoContainer.setClazz(new DIDLObject.Class("object.container"));
        videoContainer.setId(ContentTree.VIDEO_ID);
        videoContainer.setParentID(ContentTree.ROOT_ID);
        videoContainer.setTitle("Videos");
        videoContainer.setRestricted(true);
        videoContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
        videoContainer.setChildCount(0);

        rootNode.getContainer().addContainer(videoContainer);
        rootNode.getContainer().setChildCount(
                rootNode.getContainer().getChildCount() + 1);
        ContentTree.addNode(ContentTree.VIDEO_ID, new ContentNode(
                ContentTree.VIDEO_ID, videoContainer));

        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            String id = ContentTree.VIDEO_PREFIX
                    + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
            String title = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
            String creator = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
            String filePath = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            String mimeType = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
            long size = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            long duration = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            String resolution = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));
            Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                    mimeType.substring(mimeType.indexOf('/') + 1)), size,
                    "http://" + address + "/" + id);
            res.setDuration(duration / (1000 * 60 * 60) + ":"
                    + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                    + (duration % (1000 * 60)) / 1000);
            res.setResolution(resolution);

            VideoItem videoItem = new VideoItem(id, ContentTree.VIDEO_ID, title, creator, res);
            videoContainer.addItem(videoItem);
            videoContainer.setChildCount(videoContainer.getChildCount() + 1);
            ContentTree.addNode(id, new ContentNode(id, videoItem, filePath));

//            Log.d(TAG, "added video item " + title + "from " + filePath);
        }

        cursor.close();
    }

    /**
     * 添加音频
     */
    private void addAudioContent(Context context, ContentNode rootNode) {

        Container audioContainer = new Container(ContentTree.AUDIO_ID,
                ContentTree.ROOT_ID, "Audios", "HPlayer MediaServer",
                new DIDLObject.Class("object.container"), 0);
        audioContainer.setRestricted(true);
        audioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

        rootNode.getContainer().addContainer(audioContainer);
        rootNode.getContainer().setChildCount(
                rootNode.getContainer().getChildCount() + 1);
        ContentTree.addNode(ContentTree.AUDIO_ID, new ContentNode(
                ContentTree.AUDIO_ID, audioContainer));

        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
        if (cursor == null) {
            return;
        }

        while (cursor.moveToNext()) {
            String id = ContentTree.AUDIO_PREFIX
                    + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String creator = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String filePath = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            String mimeType = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
            long size = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            long duration = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            String album = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                    mimeType.substring(mimeType.indexOf('/') + 1)), size,
                    "http://" + address + "/" + id);
            res.setDuration(duration / (1000 * 60 * 60) + ":"
                    + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                    + (duration % (1000 * 60)) / 1000);

            // Music Track must have `artist' with role field, or
            // DIDLParser().generate(didl) will throw nullpointException
            MusicTrack musicTrack = new MusicTrack(id,
                    ContentTree.AUDIO_ID, title, creator, album,
                    new PersonWithRole(creator, "Performer"), res);
            audioContainer.addItem(musicTrack);
            audioContainer.setChildCount(audioContainer.getChildCount() + 1);
            ContentTree.addNode(id, new ContentNode(id, musicTrack, filePath));

//            Log.d(TAG, "added audio item " + title + "from " + filePath);
        }

        cursor.close();
    }

    /**
     * 添加图片
     */
    private void addImageContent(Context context, ContentNode rootNode) {

        Container imageContainer = new Container(ContentTree.IMAGE_ID,
                ContentTree.ROOT_ID, "Images", "HPlayer MediaServer",
                new DIDLObject.Class("object.container"), 0);
        imageContainer.setRestricted(true);
        imageContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

        rootNode.getContainer().addContainer(imageContainer);
        rootNode.getContainer().setChildCount(
                rootNode.getContainer().getChildCount() + 1);
        ContentTree.addNode(ContentTree.IMAGE_ID, new ContentNode(
                ContentTree.IMAGE_ID, imageContainer));

        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
        if (cursor == null) {
            return;
        }

        while (cursor.moveToNext()) {
            String id = ContentTree.IMAGE_PREFIX
                    + cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            String title = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
            String creator = "unkown";
            String filePath = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            String mimeType = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
            long size = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

            Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                    mimeType.substring(mimeType.indexOf('/') + 1)), size,
                    "http://" + address + "/" + id);

            ImageItem imageItem = new ImageItem(id, ContentTree.IMAGE_ID, title, creator, res);
            imageContainer.addItem(imageItem);
            imageContainer.setChildCount(imageContainer.getChildCount() + 1);
            ContentTree.addNode(id, new ContentNode(id, imageItem, filePath));

//            Log.d(TAG, "added image item " + title + "from " + filePath);
        }

        cursor.close();
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}
