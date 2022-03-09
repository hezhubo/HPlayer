package com.hezb.player.widget

import android.content.Context
import android.view.View
import com.hezb.player.controller.AbstractFloatWindow
import com.hezb.player.core.IMediaPlayer
import com.hezb.player.core.R

/**
 * Project Name: HPlayer
 * File Name:    PlayerFloatWindow
 *
 * Description: 悬浮窗播放组件.
 *
 * @author  hezhubo
 * @date    2022年03月02日 18:25
 */
open class PlayerFloatWindow(context: Context): AbstractFloatWindow(context) {

    private lateinit var mCloseBtn: View
    private lateinit var mLoadingView: View

    override fun getLayoutId(): Int {
        return R.layout.player_widget_player_float_window
    }

    override fun getRenderViewId(): Int {
        return R.id.player_render_view
    }

    override fun initAllMember() {
        mCloseBtn = findViewById(R.id.btn_close)
        mLoadingView = findViewById(R.id.player_loading_view)
    }

    override fun onBufferingUpdate(mp: IMediaPlayer) {
        super.onBufferingUpdate(mp)
        if (mp.isBuffering() && !mp.isPlaying()) {
            mLoadingView.visibility = View.VISIBLE
        } else {
            mLoadingView.visibility = View.GONE
        }
    }

}