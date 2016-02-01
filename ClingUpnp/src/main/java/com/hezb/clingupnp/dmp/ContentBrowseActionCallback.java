package com.hezb.clingupnp.dmp;


import android.util.Log;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容浏览返回
 */
public class ContentBrowseActionCallback extends Browse {

    private static final String TAG = "Browse";

    private Service service;
    private OnReceiveListener onReceiveListener;

    public ContentBrowseActionCallback(Service service, Container container, OnReceiveListener listener) {
        super(service, container.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0,
                null, new SortCriterion(true, "dc:title"));
        this.service = service;
        this.onReceiveListener = listener;
    }

    public ContentBrowseActionCallback(Service service, String id, OnReceiveListener listener) {
        super(service, id, BrowseFlag.DIRECT_CHILDREN, "*", 0,
                null, new SortCriterion(true, "dc:title"));
        this.service = service;
        this.onReceiveListener = listener;
    }

    @Override
    public void received(ActionInvocation actionInvocation, DIDLContent didlContent) {
        Log.d(TAG, "Received browse action DIDL descriptor, creating tree nodes");
        List<ContentItem> contentItemList = new ArrayList<>();
        try {
            for (Container childContainer : didlContent.getContainers()) {
                ContentItem contentItem = new ContentItem(service, childContainer);
                contentItemList.add(contentItem);
            }
            for (Item childItem : didlContent.getItems()) {
                ContentItem contentItem = new ContentItem(service, childItem);
                contentItemList.add(contentItem);
            }
            onReceiveListener.received(contentItemList);
        } catch (Exception e) {
            e.printStackTrace();
            actionInvocation.setFailure(new ActionException(
                    ErrorCode.ACTION_FAILED,
                    "Can't create list childs: " + e.getMessage(), e));
            failure(actionInvocation, null);
        }
    }

    public void updateStatus(Status status) {
    }

    @Override
    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
        Log.e(TAG, "failure:" + s);
        onReceiveListener.failure(s);
    }

    public interface OnReceiveListener {

        void received(List<ContentItem> contentItemList);

        void failure(String msg);
    }
}
