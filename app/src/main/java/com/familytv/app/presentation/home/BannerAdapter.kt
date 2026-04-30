package com.familytv.app.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.familytv.app.R
import com.familytv.app.common.extension.loadImage
import com.familytv.app.data.model.VodItem

class BannerAdapter(
    private val banners: List<VodItem>,
    private val onItemClick: (VodItem) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return BannerViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        if (banners.isEmpty()) return
        holder.bind(banners[position % banners.size])
    }

    override fun getItemCount() = if (banners.isEmpty()) 0 else Int.MAX_VALUE

    inner class BannerViewHolder(
        private val imageView: ImageView
    ) : RecyclerView.ViewHolder(imageView) {

        init {
            imageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val actualPosition = position % banners.size
                    onItemClick(banners[actualPosition])
                }
            }
        }

        fun bind(video: VodItem) {
            imageView.loadImage(video.vodPic)
        }
    }
}
