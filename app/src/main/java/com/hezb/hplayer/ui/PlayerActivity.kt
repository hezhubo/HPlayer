package com.hezb.hplayer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.hezb.clingupnp.HttpServerService
import com.hezb.clingupnp.UpnpControlManager
import com.hezb.clingupnp.UpnpServiceHelper
import com.hezb.hplayer.base.BaseActivity
import com.hezb.hplayer.databinding.ActivityPlayerBinding
import com.hezb.hplayer.ui.widget.DMCPopupWindow
import com.hezb.hplayer.ui.widget.DeviceListDialog
import com.hezb.player.core.AbstractMediaPlayer
import com.hezb.player.core.MediaModel
import com.hezb.player.core.PlayerLog
import com.hezb.player.exo.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service


/**
 * Project Name: HPlayer
 * File Name:    PlayerActivity
 *
 * Description: 播放页.
 *
 * @author  hezhubo
 * @date    2022年03月03日 10:30
 */
class PlayerActivity : BaseActivity() {

    companion object {
        private const val KEY_VIDEO_PATH = "video_path"
        private const val KEY_VIDEO_TITLE = "video_title"
        private const val KEY_VIDEO_ID = "video_id"

        @JvmStatic
        fun startPlayerActivity(context: Context, videoPath: String?, videoTitle: String?, videoId: String? = null) {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(KEY_VIDEO_PATH, videoPath)
            intent.putExtra(KEY_VIDEO_TITLE, videoTitle)
            intent.putExtra(KEY_VIDEO_ID, videoId)
            context.startActivity(intent)
        }
    }

    private val mViewBinding: ActivityPlayerBinding by lazy {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private var mPlayer: AbstractMediaPlayer? = null

    private var mDeviceListDialog: DeviceListDialog? = null
    private var mUpnpServiceHelper: UpnpServiceHelper? = null
    private var mUpnpControlManager: UpnpControlManager? = null

    private var mDMCPopupWindow: DMCPopupWindow? = null

    private var playUri: Uri? = null
    private var videoTitle: String? = null
    private var videoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = uiOptions
        }
        setContentView(mViewBinding.root)

        initAllMember()
    }

    private fun initAllMember() {
        initData()

        initPlayer()

        initDeviceSearchBtn()
    }

    override fun onResume() {
        super.onResume()
        if (mDeviceListDialog?.isShowing == true) {
            mUpnpServiceHelper?.resumeUpnpSearch()
        }
    }

    override fun onPause() {
        super.onPause()
        mViewBinding.playerControllerView.pause()

        mUpnpServiceHelper?.pauseUpnpSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewBinding.playerControllerView.release()

        mDMCPopupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        mDeviceListDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        mUpnpServiceHelper?.let {
            it.unBindUpnpService()
            it.release()
        }
    }

    override fun onBackPressed() {
        if (mDMCPopupWindow?.isShowing == true) {
            return
        }
        super.onBackPressed()
    }

    private fun initData() {
        val uri = intent.data
        if (uri == null) {
            val path = intent.getStringExtra(KEY_VIDEO_PATH)

            playUri = Uri.parse(path)
            videoTitle = intent.getStringExtra(KEY_VIDEO_TITLE)
            videoId = intent.getStringExtra(KEY_VIDEO_ID)

        } else {
            playUri = uri
            uri.path?.let {
                val index = it.lastIndexOf("/")
                title = if (index != -1) {
                    it.substring(index + 1)
                } else {
                    it
                }
            }
        }
        PlayerLog.d("play uri = $playUri")
    }

    private fun initPlayer() {
        mViewBinding.playerControllerView.mBackBtn.setOnClickListener {
            onBackPressed()
        }
        mViewBinding.playerControllerView.mTitle.text = videoTitle
        playUri?.let {
            val mediaModel = MediaModel(it)

            // TODO 切换内核
//            val player = AndroidPlayer()
//            val player = IjkPlayer()
            val player = ExoPlayer()
            player.setMediaSource(this, mediaModel)
            mViewBinding.playerControllerView.bindMediaPlayer(player)
            mViewBinding.playerControllerView.play()
            mPlayer = player
        }
    }

    private fun initDeviceSearchBtn() {
        mViewBinding.playerControllerView.mDlnaPushMediaBtn.setOnClickListener {
            mDeviceListDialog.let {
                if (it == null) {
                    DeviceListDialog(this).apply {
                        setOnDeviceSelectedCallback(object : DeviceListDialog.OnItemClickListener {
                            override fun onItemClick(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
                                mViewBinding.playerControllerView.stop()

                                showDMCPopupWindow()

                                initUpnpControlManager(device)

                                dismiss()
                            }
                        })
                        setOnDismissListener {
                            mUpnpServiceHelper?.pauseUpnpSearch()
                        }
                        show()
                        mDeviceListDialog = this
                    }
                } else {
                    if (!it.isShowing) {
                        it.show()
                        return@let
                    }
                }
            }

            openUpnp()
        }

    }

    private fun openUpnp() {
        if (mUpnpServiceHelper == null) {
            mUpnpServiceHelper =
                UpnpServiceHelper(this, object : UpnpServiceHelper.UpnpServiceCallback {
                    override fun deviceAdded(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
                        GlobalScope.launch(Dispatchers.Main) {
                            mDeviceListDialog?.addDevice(device)
                        }
                    }

                    override fun deviceRemoved(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
                        GlobalScope.launch(Dispatchers.Main) {
                            mDeviceListDialog?.removeDevice(device)
                        }
                    }
                }).apply {
                    bindUpnpService()
                }
        } else {
            mUpnpServiceHelper!!.resumeUpnpSearch()
        }
    }

    private fun showDMCPopupWindow() {
        mDMCPopupWindow.let {
            if (it == null) {
                DMCPopupWindow(this).apply {
                    controlCallback = object : DMCPopupWindow.ControlCallback {
                        override fun close() {
                            mUpnpControlManager?.stop()
                        }

                        override fun play() {
                            mUpnpControlManager?.play()
                        }

                        override fun pause() {
                            mUpnpControlManager?.pause()
                        }

                        override fun seekTo(position: Long) {
                            mUpnpControlManager?.seek(position)
                        }

                        override fun updateTime() {
                            mUpnpControlManager?.getPositionInfo()
                        }
                    }
                    showAtLocation(mViewBinding.root, Gravity.NO_GRAVITY, 0, 0)
                    mDMCPopupWindow = this
                }
            } else {
                if (!it.isShowing) {
                    it.showAtLocation(mViewBinding.root, Gravity.NO_GRAVITY, 0, 0)
                    return@let
                }
            }
        }
    }

    private fun initUpnpControlManager(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
        mUpnpServiceHelper?.getUpnpService()?.let {
            mDMCPopupWindow?.setDeviceName(device.details.friendlyName)
            mUpnpControlManager = UpnpControlManager(it.controlPoint, device).apply {
                upnpActionCallback = object : UpnpControlManager.UpnpActionCallback() {
                    override fun onSuccess(type: Int) {
                        when (type) {
                            UpnpControlManager.ACTION_TYPE_SET_MUTE -> {}
                            UpnpControlManager.ACTION_TYPE_GET_VOLUME -> {}
                            UpnpControlManager.ACTION_TYPE_SET_VOLUME -> {}
                            UpnpControlManager.ACTION_TYPE_SET_AV_TRANSPORT -> {
                                mDMCPopupWindow?.setAVTransportURISuccess()
                                play()
                            }
                            UpnpControlManager.ACTION_TYPE_GET_POSITION -> {}
                            UpnpControlManager.ACTION_TYPE_GET_TRANSPORT -> {}
                            UpnpControlManager.ACTION_TYPE_PLAY -> {
                                GlobalScope.launch(Dispatchers.Main) {
                                    mDMCPopupWindow?.setPlayState(true)
                                }
                            }
                            UpnpControlManager.ACTION_TYPE_PAUSE -> {
                                GlobalScope.launch(Dispatchers.Main) {
                                    mDMCPopupWindow?.setPlayState(false)
                                }
                            }
                            UpnpControlManager.ACTION_TYPE_SEEK -> {}
                            UpnpControlManager.ACTION_TYPE_STOP -> {}
                        }
                    }

                    override fun onFailure(type: Int, error: String?) {
                        // TODO 失败状态处理
                        //  查看判断远端关闭播放器？
                        PlayerLog.i("upnp action callback onFailure type=$type error=$error")
                    }

                    override fun getVolumeReceived(volume: Int) {
                    }

                    override fun setMuteSuccess(isMute: Boolean) {
                    }

                    override fun getTransportReceived(isPlaying: Boolean) {
                        GlobalScope.launch(Dispatchers.Main) {
                            mDMCPopupWindow?.setPlayState(isPlaying)
                        }
                    }

                    override fun getPositionReceived(currentPosition: Long, duration: Long) {
                        GlobalScope.launch(Dispatchers.Main) {
                            mDMCPopupWindow?.updateTime(currentPosition, duration)
                        }
                    }
                }

                if (!videoId.isNullOrEmpty()) {
                    setAVTransportURI(HttpServerService.getVideoUrl(this@PlayerActivity, videoId))
                } else {
                    setAVTransportURI(playUri.toString())
                }
            }
        }

    }

}