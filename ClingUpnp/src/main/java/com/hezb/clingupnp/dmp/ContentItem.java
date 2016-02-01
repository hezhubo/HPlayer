package com.hezb.clingupnp.dmp;

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.io.Serializable;

/**
 * 内容项，包含文件夹和文件
 * Created by hezb on 2016/1/27.
 */
public class ContentItem implements Serializable {

    private Service service;
    private DIDLObject didlObject;
    private String id;
    private boolean isContainer;// 是否为文件夹
    private String title;
    private String format;
    private String url;

    public ContentItem(Service service, Container container) {
        this.service = service;
        didlObject = container;
        id = container.getId();
        title = container.getTitle();
        isContainer = true;
    }

    public ContentItem(Service service, Item item) {
        this.service = service;
        didlObject = item;
        id = item.getId();
        title = item.getTitle();
        isContainer = false;
        try {
            format = item.getFirstResource().getProtocolInfo().getContentFormat();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            url = item.getFirstResource().getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Container getContainer() {
        if (isContainer) {
            return (Container) didlObject;
        } else {
            return null;
        }
    }

    public Item getItem() {
        if (!isContainer) {
            return (Item) didlObject;
        } else {
            return null;
        }
    }

    public Service getService() {
        return service;
    }

    public DIDLObject getDidlObject() {
        return didlObject;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public String getFormat() {
        return format;
    }

    public String getUrl() {
        return url;
    }
}
