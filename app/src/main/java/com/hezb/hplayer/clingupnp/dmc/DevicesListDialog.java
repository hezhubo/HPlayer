package com.hezb.hplayer.clingupnp.dmc;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hezb.clingupnp.DlnaDeviceInfo;
import com.hezb.hplayer.R;
import com.hezb.hplayer.util.Utility;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * DLNA设备列表弹窗
 * Created by hezb on 2016/1/26.
 */
public class DevicesListDialog extends Dialog {

    private Context mContext;
    private ListView mDevicesList;
    private DevicesListAdapter mDevicesListAdapter;
    private List<Device> deviceList;

    private SelectDeviceListener mListener;

    public DevicesListDialog(Context context) {
        super(context, android.R.style.Theme_Holo_Dialog_NoActionBar);
        setContentView(R.layout.dialog_devices_list);
        mContext = context;

        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        onWindowAttributesChanged(wl);

        mDevicesList = (ListView) findViewById(R.id.devices_list);
        mDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mListener != null) {
                    Device device = (Device) parent.getItemAtPosition(position);
                    if (device == null) {
                        Utility.showToast(mContext, "DLNA连接异常！");
                        return;
                    }
                    mListener.selected(device);
                }
                dismiss();

            }
        });

        deviceList = new ArrayList<>();
        mDevicesListAdapter = new DevicesListAdapter(mContext, deviceList);
        mDevicesList.setAdapter(mDevicesListAdapter);

        EventBus.getDefault().register(this);
    }

    public void setSelectDeviceListener(SelectDeviceListener listener) {
        mListener = listener;
    }

    @Override
    public void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    public void onEventMainThread(DlnaDeviceInfo dlnaDeviceInfo) {
        if (dlnaDeviceInfo.isAdd()) {
            deviceList.add(dlnaDeviceInfo.getDevice());
        } else {
            String removeIdentifier = dlnaDeviceInfo.getDevice().getIdentity()
                    .getUdn().getIdentifierString();
            String identifier = null;
            for (int i = 0; i < deviceList.size(); i++) {
                identifier = deviceList.get(i).getIdentity().getUdn().getIdentifierString();
                if (identifier.equals(removeIdentifier)) {
                    deviceList.remove(i);
                    break;
                }
            }
        }
        mDevicesListAdapter.notifyDataSetChanged();
    }

    public interface SelectDeviceListener {
        void selected(Device device);
    }

}
