package com.hezb.hplayer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * fragment父类
 * Created by hezb on 2016/1/14.
 */
public abstract class BaseFragment extends Fragment {

    protected Context mContext;
    protected View mRootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = LayoutInflater.from(getContext()).inflate(getContentViewId(), container, false);

        initAllMember();

        return mRootView;
    }

    protected View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    protected abstract int getContentViewId();

    protected abstract void initAllMember();

}
