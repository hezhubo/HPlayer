package com.hezb.hplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseActivity;
import com.hezb.hplayer.entity.MediaInfo;
import com.hezb.hplayer.localvideofilter.MediaQueryTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private MediaQueryTask mediaQueryTask;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaQueryTask = new MediaQueryTask();
        mediaQueryTask.setQueryListener(new MediaQueryTask.QueryListener() {
            @Override
            public void onResult(List<MediaInfo> mediaInfoList) {
                Intent intent = new Intent(mContext, HomePageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("mediaInfoList", (ArrayList) mediaInfoList);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
                finish();
            }
        });
        mediaQueryTask.execute(mContext);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaQueryTask != null) {
            mediaQueryTask.cancel(true);
        }
    }
}
