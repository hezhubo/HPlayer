package com.hezb.hplayer.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseFragment;
import com.hezb.hplayer.constant.ConstantKey;
import com.hezb.hplayer.entity.MediaInfo;
import com.hezb.hplayer.ui.activity.PlayerActivity;
import com.hezb.hplayer.ui.adapter.VideoListAdapter;
import com.hezb.hplayer.ui.listviewanimation.ScaleInAnimationAdapter;
import com.hezb.hplayer.util.DensityUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 本地视频列表
 * Created by hezb on 2016/1/14.
 */
public class VideoListFragment extends BaseFragment {

    private SwipeMenuListView mVideoList;
    private VideoListAdapter mVideoListAdapter;

    private ArrayList<MediaInfo> mediaInfoList;

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_video_list;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        if (args != null) {
            mediaInfoList = (ArrayList<MediaInfo>) args.getSerializable("mediaInfoList");
        } else {
            mediaInfoList = new ArrayList<>();
        }
    }

    @Override
    protected void initAllMember() {
        mVideoList = (SwipeMenuListView) findViewById(R.id.video_list);

        initSwipeMenuList();

    }

    private void initSwipeMenuList() {
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(mContext);
                openItem.setBackground(R.color.color_bdbdbd);
                openItem.setWidth(DensityUtil.dip2px(mContext, 90));
                openItem.setTitle(R.string.open);
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
                deleteItem.setBackground(R.color.color_e84e40);
                deleteItem.setWidth(DensityUtil.dip2px(mContext, 90));
                deleteItem.setTitle(R.string.delete);
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mVideoList.setMenuCreator(creator);

        // step 2. listener item click event
        mVideoList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                MediaInfo mediaInfo = mVideoListAdapter.getItem(position);
                switch (index) {
                    case 0: // open
                        goPlayer(mediaInfo.getPath(), mediaInfo.getTitle());
                        break;
                    case 1: // delete
                        deleteFile(mediaInfo.getPath());
                        mediaInfoList.remove(position);
                        mVideoListAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        mVideoListAdapter = new VideoListAdapter(mContext, mediaInfoList);
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(mVideoListAdapter);
        scaleInAnimationAdapter.setAbsListView(mVideoList);
        mVideoList.setAdapter(scaleInAnimationAdapter);
        mVideoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo mediaInfo = mVideoListAdapter.getItem(position);
                goPlayer(mediaInfo.getPath(), mediaInfo.getTitle());
            }
        });
    }

    /**
     * 去播放
     */
    private void goPlayer(String playUrl, String name) {
        Intent intent = new Intent(mContext, PlayerActivity.class);
        intent.putExtra(ConstantKey.PLAY_URL, playUrl);
        intent.putExtra(ConstantKey.NAME, name);
        startActivity(intent);
    }

    /**
     * 删除文件
     */
    private void deleteFile(String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
