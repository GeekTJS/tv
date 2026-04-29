package com.familytv.app.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.familytv.app.R
import com.familytv.app.data.model.PlayEpisode
import com.familytv.app.databinding.ItemEpisodeBinding

class EpisodeAdapter(
    private val onItemClick: (PlayEpisode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    val episodes = mutableListOf<PlayEpisode>()
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position], position == selectedIndex)
    }

    override fun getItemCount() = episodes.size

    fun setEpisodes(list: List<PlayEpisode>) {
        episodes.clear()
        episodes.addAll(list)
        notifyDataSetChanged()
    }

    fun setSelectedIndex(index: Int) {
        val oldIndex = selectedIndex
        selectedIndex = index
        if (oldIndex < episodes.size) notifyItemChanged(oldIndex)
        if (index < episodes.size) notifyItemChanged(index)
    }

    inner class EpisodeViewHolder(
        private val binding: ItemEpisodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedIndex(position)
                    onItemClick(episodes[position])
                }
            }
        }

        fun bind(episode: PlayEpisode, isSelected: Boolean) {
            binding.episodeTitleTextView.text = episode.title
            val context = binding.root.context
            binding.root.background = if (isSelected) {
                ContextCompat.getDrawable(context, R.drawable.episode_selected)
            } else {
                ContextCompat.getDrawable(context, R.drawable.episode_background)
            }
        }
    }
}
