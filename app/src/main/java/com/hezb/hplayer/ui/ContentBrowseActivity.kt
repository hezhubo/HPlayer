package com.hezb.hplayer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hezb.clingupnp.UpnpControlManager
import com.hezb.clingupnp.UpnpServiceHelper
import com.hezb.clingupnp.model.MediaInfo
import com.hezb.clingupnp.util.FormatUtil
import com.hezb.hplayer.databinding.ActivityContentBrowseBinding
import com.hezb.hplayer.model.ContentInfo
import com.hezb.hplayer.ui.adapter.ContentListAdapter
import com.hezb.hplayer.ui.widget.DeviceListDialog
import com.hezb.hplayer.util.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.DIDLObject

/**
 * Project Name: HPlayer
 * File Name:    ContentBrowseActivity
 *
 * Description: 浏览共享文件页面.
 *
 * @author  hezhubo
 * @date    2022年03月06日 15:03
 */
class ContentBrowseActivity : AppCompatActivity() {

    private val mViewBinding: ActivityContentBrowseBinding by lazy {
        ActivityContentBrowseBinding.inflate(layoutInflater)
    }

    private var mDeviceListDialog: DeviceListDialog? = null
    private var mUpnpServiceHelper: UpnpServiceHelper? = null
    private var mUpnpControlManager: UpnpControlManager? = null

    private lateinit var mContentListAdapter: ContentListAdapter

    private val directoryIdList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initContentListView()

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
        mUpnpServiceHelper?.pauseUpnpSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
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
        mUpnpControlManager?.let {
            if (directoryIdList.isNotEmpty()) {
                it.browse(directoryIdList[directoryIdList.size - 1], BrowseFlag.DIRECT_CHILDREN)
                directoryIdList.removeAt(directoryIdList.size - 1)
                return
            }
        }
        super.onBackPressed()
    }

    private fun initContentListView() {
        mViewBinding.rvContentList.layoutManager = LinearLayoutManager(this)
        mContentListAdapter = ContentListAdapter(object : ContentListAdapter.OnItemClickListener {
            override fun onItemClick(contentInfo: ContentInfo) {
                if (contentInfo.type == ContentInfo.TYPE_DIRECTORY) {
                    directoryIdList.add(contentInfo.parentId ?: "0")
                    mUpnpControlManager?.browse(contentInfo.id, BrowseFlag.DIRECT_CHILDREN)
                } else if (contentInfo.type == ContentInfo.TYPE_VIDEO || contentInfo.type == ContentInfo.TYPE_AUDIO) {
                    PlayerActivity.startPlayerActivity(
                        this@ContentBrowseActivity,
                        contentInfo.mediaInfo?.path,
                        contentInfo.title
                    )
                } else if (contentInfo.type == ContentInfo.TYPE_IMAGE) {
                    // TODO 查看大图
                }
            }
        })
        mViewBinding.rvContentList.adapter = mContentListAdapter
    }

    private fun initDeviceSearchBtn() {
        mViewBinding.ivSearchRemoteDevices.setOnClickListener {
            mDeviceListDialog.let {
                if (it == null) {
                    DeviceListDialog(this).apply {
                        setOnDeviceSelectedCallback(object : DeviceListDialog.OnItemClickListener {
                            override fun onItemClick(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
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

    private fun initUpnpControlManager(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
        mUpnpServiceHelper?.getUpnpService()?.let {
            mViewBinding.tvRemoteDevice.text = device.details.friendlyName
            mUpnpControlManager = UpnpControlManager(it.controlPoint, device).apply {
                upnpActionCallback = object : UpnpControlManager.UpnpActionCallback() {
                    override fun onFailure(type: Int, error: String?) {
                        GlobalScope.launch(Dispatchers.Main) {
                            ToastUtil.show(
                                this@ContentBrowseActivity,
                                "upnp action failure! type=$type, error=$error"
                            )
                        }
                    }

                    override fun browseReceived(didlContent: DIDLContent) {
                        val contentList = ArrayList<ContentInfo>()
                        // 遍历文件夹
                        didlContent.containers?.let { containers ->
                            for (container in containers) {
                                ContentInfo().apply {
                                    type = ContentInfo.TYPE_DIRECTORY
                                    id = container.id
                                    parentId = container.parentID
                                    title = container.title
                                    childCount = container.childCount
                                    contentList.add(this)
                                }
                            }
                        }
                        // 遍历文件
                        didlContent.items?.let { items ->
                            for (item in items) {
                                ContentInfo().let { contentInfo ->
                                    contentInfo.id = item.id
                                    contentInfo.parentId = item.parentID
                                    contentInfo.title = item.title
                                    item.firstResource?.let { res ->
                                        val mediaInfo = MediaInfo()
                                        mediaInfo.path = res.value
                                        mediaInfo.size = res.size
                                        mediaInfo.duration = FormatUtil.transformTime(res.duration)
                                        res.protocolInfo?.contentFormat?.let { mimeType ->
                                            when {
                                                mimeType.contains("video") -> {
                                                    contentInfo.type = ContentInfo.TYPE_VIDEO
                                                }
                                                mimeType.contains("audio") -> {
                                                    contentInfo.type = ContentInfo.TYPE_AUDIO
                                                }
                                                mimeType.contains("image") -> {
                                                    contentInfo.type = ContentInfo.TYPE_IMAGE
                                                }
                                            }
                                        }
                                        mediaInfo.resolution = res.resolution
                                        mediaInfo.artist =
                                            item.getFirstProperty(DIDLObject.Property.UPNP.ACTOR::class.java)?.value?.name
                                        contentInfo.mediaInfo = mediaInfo
                                    }
                                    contentList.add(contentInfo)
                                }
                            }
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            mContentListAdapter.replaceAll(contentList)
                        }
                    }

                    override fun browseUpdateStatus(status: Browse.Status) {
                        // TODO 此处可以做Loading提示
                        when (status) {
                            Browse.Status.LOADING -> {

                            }
                            Browse.Status.OK -> {

                            }
                            Browse.Status.NO_CONTENT -> {

                            }
                        }
                    }
                }
                browse("0", BrowseFlag.DIRECT_CHILDREN)
            }
        }

    }

}