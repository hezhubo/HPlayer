package com.hezb.clingupnp.dms;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLDecoder;

import fi.iki.elonen.NanoHTTPD;

/**
 * HTTP服务器
 * 太卡！无法快进快退
 * Created by hezb on 2016/1/29.
 */
public class NanoHttpServer extends NanoHTTPD {

    private static final String TAG = "NanoHttpServer";

    public NanoHttpServer(int port) {
        super(port);
    }

    public NanoHttpServer(String hostname, int port) {
        super(hostname, port);
        setTempFileManagerFactory(new TempFileManagerFactory() {
            @Override
            public TempFileManager create() {
                return null;
            }
        });

    }
    // 每次请求都要构建 Response 太浪费资源，且图片在列表中看不到（还未构建） TODO 先构建
    @Override
    public Response serve(IHTTPSession session) {
        String itemId = session.getUri().replaceFirst("/", "");
        itemId = URLDecoder.decode(itemId);
        if (itemId != null && ContentTree.hasNode(itemId)) {
            ContentNode node = ContentTree.getNode(itemId);
            if (node.isItem()) {
                String localPath = node.getFullPath();
                Long fileSize = node.getItem().getFirstResource().getSize();
                String mimeType = node.getItem().getFirstResource()
                        .getProtocolInfo().getContentFormatMimeType().toString();
                return responseFile(mimeType, localPath, fileSize);
            }
        }
        Log.d(TAG, " response error!");
        String msg = "<html><body><h1>Error</h1>\n" + "</body></html>\n";
        return newFixedLengthResponse(msg);

    }


    public Response responseFile(String mimeType, String path, long size) {
        Log.d(TAG, "=====responseFile=====mimeType:" + mimeType
                + "=======path:" + path + "=======size:" + size);
        try {
            FileInputStream fis = new FileInputStream(path);
            return newFixedLengthResponse(Response.Status.OK, mimeType, fis, size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return newFixedLengthResponse("Error");
        }
    }

}
