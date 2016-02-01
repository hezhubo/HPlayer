package com.hezb.hplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseActivity;
import com.hezb.hplayer.clingupnp.dmp.DevicesActivity;
import com.hezb.hplayer.clingupnp.dms.DMSActivity;
import com.hezb.hplayer.ui.fragment.FileListFragment;
import com.hezb.hplayer.ui.fragment.VideoListFragment;
import com.hezb.hplayer.ui.fragment.WebViewFragment;

/**
 * 主页
 * Created by hezb on 2016/1/14.
 */
public class HomePageActivity extends BaseActivity {

    private RadioGroup mRadioGroup;
    private VideoListFragment mVideoListFragment;
    private WebViewFragment mWebViewFragment;
    private FileListFragment mFileListFragment;
    private Fragment mCurrentFragment;

    private View mDLNARemoteDevices;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFindViewById();

        initOperation();

    }

    private void mFindViewById() {
        mRadioGroup = (RadioGroup) findViewById(R.id.bottom_bar);
        mDLNARemoteDevices = findViewById(R.id.dlna_remote_devices);
    }

    private void initOperation() {

        mVideoListFragment = new VideoListFragment();
        mVideoListFragment.setArguments(getIntent().getBundleExtra("bundle"));
        mWebViewFragment = new WebViewFragment();
        mFileListFragment = new FileListFragment();

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.local_video:
                        switchFragment(mVideoListFragment);
                        break;
                    case R.id.browser:
                        switchFragment(mWebViewFragment);
                        break;
                    case R.id.file_browser:
                        switchFragment(mFileListFragment);
                        break;
                }
            }
        });
        mRadioGroup.check(R.id.local_video);

        mDLNARemoteDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DevicesActivity.class);
                startActivity(intent);
            }
        });
        mDLNARemoteDevices.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO 优化服务器
                Intent intent = new Intent(mContext, DMSActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment != null) {
            if (!fragment.isAdded()) {
                fragmentTransaction.add(R.id.fragment_layout, fragment);
            }
            if (mCurrentFragment != null) {
                fragmentTransaction.hide(mCurrentFragment).show(fragment).commit();
            }
            mCurrentFragment = fragment;
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebViewFragment.isEditVisible()) {
            return;
        }
        exit();
    }

    private boolean waitExit = true;
    private Toast toast;
    private Handler mHandler = new Handler();
    private Runnable cancelExit = new Runnable() {
        @Override
        public void run() {
            waitExit = true;
        }
    };

    private void exit() {
        if (waitExit) {
            waitExit = false;
            toast = Toast.makeText(mContext, getString(R.string.press_to_exit), Toast.LENGTH_SHORT);
            toast.show();
            mHandler.postDelayed(cancelExit, 2000);
        } else {
            toast.cancel();
            finish();
            System.exit(0);
        }
    }
}
