package com.hezb.hplayer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hezb.hplayer.R
import com.hezb.hplayer.databinding.ViewItemContentListBinding
import com.hezb.hplayer.model.ContentInfo

/**
 * Project Name: HPlayer
 * File Name:    ContentListAdapter
 *
 * Description: 文件列表适配器.
 *
 * @author  hezhubo
 * @date    2022年03月06日 19:32
 */
class ContentListAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<ContentListAdapter.ContentViewHolder>() {

    private val contentList: MutableList<ContentInfo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_content_list, parent, false)
        return ContentViewHolder(ViewItemContentListBinding.bind(itemView))
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val contentInfo = contentList[position]
        holder.bindModel(contentInfo)
        when (contentInfo.type) {
            ContentInfo.TYPE_DIRECTORY -> {
                holder.binding.ivIcon.setImageResource(R.drawable.xml_vector_file_directory_icon)
                holder.binding.tvSubInfo.text = contentInfo.childCount.toString()
            }
            ContentInfo.TYPE_VIDEO -> {
                holder.binding.ivIcon.setImageResource(R.drawable.xml_vector_file_video_icon)
                holder.binding.tvSubInfo.text = "${contentInfo.mediaInfo?.getFormatDuration()}  ${contentInfo.mediaInfo?.getFormatSize()}"
            }
            ContentInfo.TYPE_AUDIO -> {
                holder.binding.ivIcon.setImageResource(R.drawable.xml_vector_file_audio_icon)
                holder.binding.tvSubInfo.text = "${contentInfo.mediaInfo?.getFormatDuration()}  ${contentInfo.mediaInfo?.getFormatSize()}"
            }
            ContentInfo.TYPE_IMAGE -> {
                Glide.with(holder.binding.ivIcon)
                    .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.xml_vector_file_image_icon))
                    .asBitmap()
                    .load(contentInfo.mediaInfo?.path)
                    .into(holder.binding.ivIcon)
                holder.binding.tvSubInfo.text = contentInfo.mediaInfo?.getFormatSize()
            }
            else -> {
                holder.binding.ivIcon.setImageResource(R.drawable.xml_vector_file_unknown_icon)
                holder.binding.tvSubInfo.text = "unknown"
            }
        }
        holder.binding.root.setOnClickListener {
            onItemClickListener.onItemClick(contentInfo)
        }
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    fun replaceAll(contentList: MutableList<ContentInfo>) {
        this.contentList.clear()
        this.contentList.addAll(contentList)
        notifyDataSetChanged()
    }

    class ContentViewHolder(val binding: ViewItemContentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindModel(model: ContentInfo) {
            binding.apply {
                contentInfo = model
                executePendingBindings()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(contentInfo: ContentInfo)
    }

}