package com.hezb.hplayer.clingupnp.dmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hezb.hplayer.R;

import org.fourthline.cling.model.meta.Device;

import java.util.List;


/**
 * 设备列表适配器
 * Created by hezb on 2016/1/26.
 */
public class DevicesListAdapter extends BaseAdapter {

    private List<Device> deviceList;
    private LayoutInflater mInflater;

    public DevicesListAdapter(Context context, List<Device> deviceList) {
        mInflater = LayoutInflater.from(context);
        this.deviceList = deviceList;
    }


    @Override
    public int getCount() {
        return deviceList == null ? 0 : deviceList.size();
    }

    @Override
    public Device getItem(int position) {
        return deviceList == null ? null : deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.devices_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.device_name);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Device device = getItem(position);
        holder.name.setText(device.getDetails().getFriendlyName());

        return convertView;
    }

    class ViewHolder {
        TextView name;
    }
}
