package com.hezb.hplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hezb.hplayer.R;
import com.hezb.hplayer.entity.MediaInfo;
import com.hezb.hplayer.util.FileManager;
import com.hezb.hplayer.util.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.vov.vitamio.utils.StringUtils;

/**
 * 视频列表适配器
 * Created by hezb on 2016/1/14.
 */
public class VideoListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<MediaInfo> mediaInfoList;

    public VideoListAdapter(Context context, ArrayList<MediaInfo> mediaInfoList) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mediaInfoList = mediaInfoList;
    }

    @Override
    public int getCount() {
        return mediaInfoList == null ? 0 : mediaInfoList.size();
    }

    @Override
    public MediaInfo getItem(int position) {
        return mediaInfoList == null ? null : mediaInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.video_list_item, null);
            holder = new ViewHolder();
            holder.infoLayout = convertView.findViewById(R.id.info_layout);
            holder.name = (TextView) convertView.findViewById(R.id.media_name);
            holder.size = (TextView) convertView.findViewById(R.id.media_size);
            holder.path = (TextView) convertView.findViewById(R.id.media_path);
            holder.duration = (TextView) convertView.findViewById(R.id.media_duration);
            holder.image = (ImageView) convertView.findViewById(R.id.media_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Utility.resizeImageViewOnScreenSize(mContext, holder.image, 3, 10, 4, 3);
        ViewGroup.LayoutParams lp = holder.infoLayout.getLayoutParams();
        lp.height = holder.image.getLayoutParams().height;

        MediaInfo mediaInfo = getItem(position);
        holder.name.setText(mediaInfo.getName());
        holder.size.setText(FileManager.sizeAddUnit(mediaInfo.getSize()));
        holder.duration.setText(StringUtils.generateTime(mediaInfo.getDuration()));
        holder.path.setText(mediaInfo.getPath());
        if (mediaInfo.getThumbnailPath() != null) {
            Picasso.with(mContext).load("file://" + mediaInfo.getThumbnailPath())
                    .error(R.drawable.image_default_bg).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.image_default_bg);
        }

        return convertView;
    }

    class ViewHolder {
        View infoLayout;
        TextView name;
        TextView size;
        TextView path;
        TextView duration;
        ImageView image;
    }
}
