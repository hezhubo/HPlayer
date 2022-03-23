package com.hezb.clingupnp.dms

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hezb.clingupnp.R
import com.hezb.clingupnp.UpnpDMSService
import com.hezb.player.android.AndroidPlayer
import com.hezb.player.core.MediaModel
import com.hezb.player.widget.PlayerControllerView

/**
 * Project Name: HPlayer
 * File Name:    TransportPlayerActivity
 *
 * Description: DMS播放页.
 *
 * @author  hezhubo
 * @date    2022年03月22日 16:00
 */
class TransportPlayerActivity : Activity() {

    private val playerControllerView: PlayerControllerView by lazy {
        findViewById(R.id.player_controller_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upnp_activity_transport_player)

        initPlayer(intent)

        UpnpDMSService.register(this)
    }

    override fun onPause() {
        super.onPause()
        playerControllerView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerControllerView.release()
        UpnpDMSService.setMediaPlayer(null)

        UpnpDMSService.unregister()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initPlayer(intent)
    }

    private fun initPlayer(intent: Intent) {
        val videoPath = intent.getStringExtra("video_path")
        val playUri = Uri.parse(videoPath)
        val mediaModel = MediaModel(playUri)
        val player = AndroidPlayer()
        player.setMediaSource(this, mediaModel)
        UpnpDMSService.setMediaPlayer(player)
        playerControllerView.bindMediaPlayer(player)
        playerControllerView.play()
    }

}