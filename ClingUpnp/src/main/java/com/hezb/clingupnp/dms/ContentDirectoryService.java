package com.hezb.clingupnp.dms;

import android.util.Log;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

/**
 * 内容目录服务
 * Created by hezb on 2016/1/28.
 */
public class ContentDirectoryService extends AbstractContentDirectoryService {

    private static final String TAG = "LocalDirectory";

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String s1, long l, long l1,
                               SortCriterion[] sortCriterions) throws ContentDirectoryException {
        try {

            DIDLContent didl = new DIDLContent();

            ContentNode contentNode = ContentTree.getNode(objectID);

            Log.d(TAG, "someone's browsing id: " + objectID);

            if (contentNode == null) { // 没有共享资源
                return new BrowseResult("", 0, 0);
            }

            if (contentNode.isItem()) { // 是文件
                didl.addItem(contentNode.getItem());

                Log.v(TAG, "returing item: " + contentNode.getItem().getTitle());

                return new BrowseResult(new DIDLParser().generate(didl), 1, 1);

            } else { // 是文件夹
                if (browseFlag == BrowseFlag.METADATA) {
                    didl.addContainer(contentNode.getContainer());

                    Log.v(TAG, "returning metadata of container: " + contentNode.getContainer().getTitle());

                    return new BrowseResult(new DIDLParser().generate(didl), 1, 1);

                } else {
                    for (Container container : contentNode.getContainer().getContainers()) {
                        didl.addContainer(container);

                        Log.v(TAG, "getting child container: " + container.getTitle());
                    }
                    for (Item item : contentNode.getContainer().getItems()) {
                        didl.addItem(item);

                        Log.v(TAG, "getting child item: " + item.getTitle());
                    }
                    return new BrowseResult(new DIDLParser().generate(didl),
                            contentNode.getContainer().getChildCount(),
                            contentNode.getContainer().getChildCount());
                }

            }

        } catch (Exception e) {
            throw new ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString());
        }
    }


}
