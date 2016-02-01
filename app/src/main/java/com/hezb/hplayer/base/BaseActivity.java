package com.hezb.hplayer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * activity父类
 * Created by hezb on 2016/1/14.
 */
public abstract class BaseActivity extends FragmentActivity {

    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(getContentViewId());
    }

    protected abstract int getContentViewId();
}
