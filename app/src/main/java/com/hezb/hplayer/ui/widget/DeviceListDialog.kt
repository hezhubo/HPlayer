package com.hezb.hplayer.ui.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hezb.hplayer.R
import com.hezb.hplayer.databinding.ViewItemDeviceListBinding
import com.hezb.hplayer.databinding.WidgetDialogDeviceListBinding
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service

/**
 * Project Name: HPlayer
 * File Name:    DeviceListDialog
 *
 * Description: DLNA设备列表弹窗.
 *
 * @author  hezhubo
 * @date    2022年03月04日 23:05
 */
class DeviceListDialog(context: Context) : Dialog(context, R.style.Theme_Dialog_Common) {

    private val mDeviceRecyclerView: RecyclerView by lazy {
        findViewById(R.id.rv_device_list)
    }

    private val mDeviceListAdapter: DeviceListAdapter

    init {
        setContentView(R.layout.widget_dialog_device_list)

        mDeviceRecyclerView.layoutManager = LinearLayoutManager(context)
        mDeviceRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        mDeviceListAdapter = DeviceListAdapter()
        mDeviceRecyclerView.adapter = mDeviceListAdapter
    }

    fun setOnDeviceSelectedCallback(callback: OnItemClickListener?) {
        mDeviceListAdapter.onItemClickListener = callback
    }

    fun addDevice(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
        mDeviceListAdapter.add(device)
    }

    fun removeDevice(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
        mDeviceListAdapter.remove(device)
    }

    class DeviceListAdapter : RecyclerView.Adapter<DeviceViewHolder>() {
        private val deviceList: MutableList<Device<*, out Device<*, *, *>, out Service<*, *>>> =
            ArrayList()

        var onItemClickListener: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            return DeviceViewHolder(ViewItemDeviceListBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val device = deviceList[position]
            holder.binding.deviceName.text = device.details.friendlyName
            holder.binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(device)
            }
        }

        override fun getItemCount(): Int {
            return deviceList.size
        }

        fun add(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
            deviceList.add(0, device)
            notifyItemInserted(0)
        }

        fun remove(device: Device<*, out Device<*, *, *>, out Service<*, *>>) {
            val index = deviceList.lastIndexOf(device)
            if (index != -1) {
                deviceList.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    class DeviceViewHolder(val binding: ViewItemDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    interface OnItemClickListener {
        fun onItemClick(device: Device<*, out Device<*, *, *>, out Service<*, *>>)
    }

}