package com.hezb.hplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hezb.clingupnp.HttpServerService
import com.hezb.clingupnp.UpnpDMSService
import com.hezb.clingupnp.model.MediaInfo
import com.hezb.clingupnp.util.ContentResolverUtil
import com.hezb.hplayer.base.BaseActivity
import com.hezb.hplayer.databinding.ActivityMainBinding
import com.hezb.hplayer.ui.adapter.VideoListAdapter
import com.hezb.hplayer.util.ToastUtil
import kotlinx.coroutines.*

/**
 * Project Name: HPlayer
 * File Name:    MainActivity
 *
 * Description: 应用主页.
 *
 * @author  hezhubo
 * @date    2022年03月02日 22:13
 */
class MainActivity : BaseActivity() {

    private val mViewBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var deferred: Deferred<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initAllMember()
    }

    private fun initAllMember() {
        mViewBinding.dlnaRemoteDevices.setOnClickListener {
            startActivity(Intent(this, ContentBrowseActivity::class.java))
        }
        mViewBinding.ivDmsService.setOnClickListener {
            startService(Intent(this, UpnpDMSService::class.java))
            ToastUtil.show(this, "DMS服务已启动")
        }

        if (checkPermission()) {
            loadLocalVideo()
        }

        startHttpServerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        deferred?.let {
            if (!it.isCancelled) {
                it.cancel()
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (result != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 999)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 999) {
            if (permissions.isNotEmpty() && permissions.size == grantResults.size) {
                for (i in permissions.indices) {
                    if (permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            loadLocalVideo()
                            return
                        }
                    }
                }
                Toast.makeText(this, "授权失败！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 加载本地视频
     */
    private fun loadLocalVideo() {
        GlobalScope.launch(Dispatchers.Main) {
            val deferred = GlobalScope.async(Dispatchers.IO) {
                return@async ContentResolverUtil.queryVideos(this@MainActivity)
            }.also {
                deferred = it
            }
            val videoList = deferred.await()
            if (videoList.isEmpty()) {
                // 无视频

            } else {
                mViewBinding.rvVideoList.layoutManager = LinearLayoutManager(this@MainActivity)
                val videoListAdapter = VideoListAdapter(videoList)
                videoListAdapter.onItemClickListener =
                    object : VideoListAdapter.OnItemClickListener {
                        override fun onItemClick(video: MediaInfo) {
                            PlayerActivity.startPlayerActivity(
                                this@MainActivity,
                                video.path,
                                video.displayName,
                                video.id
                            )
                        }
                    }
                mViewBinding.rvVideoList.adapter = videoListAdapter
            }
            deferred.cancel()
        }
    }

    /**
     * 启动http服务器服务，用于分享本地文件
     * 此处为了方便应用启动时同时启动服务器，可在需要投屏本地文件时开启
     */
    private fun startHttpServerService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, HttpServerService::class.java))
        } else {
            startService(Intent(this, HttpServerService::class.java))
        }
    }

}