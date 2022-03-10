package com.hezb.hplayer.ui.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.hezb.hplayer.R
import com.hezb.player.widget.PlayerControllerView

/**
 * Project Name: HPlayer
 * File Name:    AppPlayerControllerView
 *
 * Description: 播放器控制层.
 *
 * @author  hezhubo
 * @date    2022年03月08日 15:17
 */
class AppPlayerControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PlayerControllerView(context, attrs, defStyleAttr) {

    private val mTopBar: View by lazy { mRootView.findViewById(R.id.player_top_bar) }
    private val mBottomBar: View by lazy { mRootView.findViewById(R.id.player_bottom_layout) }
    val mBackBtn: ImageView by lazy { mRootView.findViewById(R.id.player_back) }
    val mTitle: TextView by lazy { mRootView.findViewById(R.id.player_title) }
    val mDlnaPushMediaBtn: ImageView by lazy { mRootView.findViewById(R.id.iv_dlna_push_media) }

    private val animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
    private val animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)

    override fun getRootLayoutId(): Int {
        return R.layout.widget_app_player_controller_view
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (mTopBar.visibility== View.VISIBLE) {
            hideFloatView()
        } else {
            showFloatView()
        }
        return true
    }

    fun hideFloatView() {
        mTopBar.startAnimation(animOut)
        mBottomBar.startAnimation(animOut)
        mTopBar.visibility = View.GONE
        mBottomBar.visibility = View.GONE
        (context as Activity).window.let { window ->
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = uiOptions
        }
    }

    fun showFloatView() {
        mTopBar.startAnimation(animIn)
        mBottomBar.startAnimation(animIn)
        mTopBar.visibility = View.VISIBLE
        mBottomBar.visibility = View.VISIBLE
        (context as Activity).window.let { window ->
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

}