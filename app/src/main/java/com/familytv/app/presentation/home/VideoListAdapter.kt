package com.familytv.app.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familytv.app.common.extension.loadImage
import com.familytv.app.data.model.VodItem
import com.familytv.app.databinding.ItemVideoCardBinding

class VideoListAdapter(
    private val onItemClick: (VodItem) -> Unit
) : RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    private val videos = mutableListOf<VodItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount() = videos.size

    fun submitList(list: List<VodItem>) {
        videos.clear()
        videos.addAll(list)
        notifyDataSetChanged()
    }

    inner class VideoViewHolder(
        private val binding: ItemVideoCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(videos[position])
                }
            }
        }

        fun bind(video: VodItem) {
            binding.titleTextView.text = video.vodName
            binding.coverImageView.loadImage(video.vodPic)
        }
    }
}
