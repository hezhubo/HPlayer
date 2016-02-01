package com.hezb.hplayer.clingupnp.dmp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hezb.clingupnp.dmp.Constants;
import com.hezb.clingupnp.dmp.ContentItem;
import com.hezb.hplayer.R;
import com.hezb.hplayer.util.Utility;

import java.util.List;

/**
 * 内容列表适配器
 * Created by hezb on 2016/1/27.
 */
public class ContentListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<ContentItem> contentItemList;

    public ContentListAdapter(Context context, List<ContentItem> contentItemList) {
        mInflater = LayoutInflater.from(context);
        this.contentItemList = contentItemList;
    }

    @Override
    public int getCount() {
        return contentItemList == null ? 0 : contentItemList.size();
    }

    @Override
    public ContentItem getItem(int position) {
        return contentItemList == null ? null : contentItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.content_list_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.content_icon);
            holder.title = (TextView) convertView.findViewById(R.id.content_title);
            holder.childCount = (TextView) convertView.findViewById(R.id.content_child_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ContentItem contentItem = getItem(position);
        holder.title.setText(contentItem.getTitle());
        if (contentItem.isContainer()) {
            holder.childCount.setText("" + contentItem.getContainer().getChildCount().intValue());
            holder.childCount.setVisibility(View.VISIBLE);
            holder.icon.setImageResource(R.drawable.folder_icon);
        } else {
            holder.childCount.setVisibility(View.GONE);
            String format = contentItem.getFormat();
            if (format != null) {
                if (format.contains(Constants.FORMAT_IMG)) {
                    Utility.displayImage(contentItem.getUrl(),
                            holder.icon, null, R.drawable.image_default_bg);
                } else if(format.contains(Constants.FORMAT_VIDEO)) {
                    holder.icon.setImageResource(R.drawable.video_icon);
                } else if(format.contains(Constants.FORMAT_AUDIO)) {
                    holder.icon.setImageResource(R.drawable.music_icon);
                } else {
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView childCount;
        ImageView icon;
    }

}
