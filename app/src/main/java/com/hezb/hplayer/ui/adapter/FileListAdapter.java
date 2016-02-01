package com.hezb.hplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hezb.hplayer.R;
import com.hezb.hplayer.entity.FileInfo;
import com.hezb.hplayer.util.FileManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 文件列表适配器
 * Created by hezb on 2016/1/22.
 */
public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<FileInfo> fileInfoList;

    private SimpleDateFormat format;

    public FileListAdapter(Context context, List<FileInfo> fileInfoList) {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.fileInfoList = fileInfoList;
    }

    @Override
    public int getCount() {
        return fileInfoList == null ? 0 : fileInfoList.size();
    }

    @Override
    public FileInfo getItem(int position) {
        return fileInfoList == null ? null : fileInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_list_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
            holder.name = (TextView) convertView.findViewById(R.id.file_name);
            holder.childFileCount = (TextView) convertView.findViewById(R.id.child_file_count);
            holder.lastModified = (TextView) convertView.findViewById(R.id.last_modified_time);
            holder.length = (TextView) convertView.findViewById(R.id.file_length);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FileInfo fileInfo = getItem(position);
        holder.icon.setImageResource(fileInfo.getIcon());
        holder.name.setText(fileInfo.getName());
        holder.lastModified.setText(format.format(new Date(fileInfo.getLastModified())));
        if (fileInfo.getType() == FileInfo.TYPE_FOLDER) {
            holder.childFileCount.setText(mContext.getString(
                    R.string.child_file_count, fileInfo.getChildFolderCount()));
            holder.childFileCount.setVisibility(View.VISIBLE);
            holder.length.setVisibility(View.GONE);

        } else if(fileInfo.getType() == FileInfo.TYPE_VIDEO) {
            holder.length.setText(FileManager.sizeAddUnit(fileInfo.getLength()));
            holder.length.setVisibility(View.VISIBLE);
            holder.childFileCount.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        TextView childFileCount;
        TextView lastModified;
        TextView length;
    }


}
