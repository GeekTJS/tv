package com.familytv.app.presentation.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.familytv.app.common.extension.loadImage
import com.familytv.app.data.model.PlayEpisode
import com.familytv.app.databinding.ActivityDetailBinding
import com.familytv.app.presentation.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel
    private lateinit var episodesAdapter: EpisodeAdapter

    private var vodId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vodId = intent.getLongExtra("vodId", 0)

        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        setupUI()
        loadDetail()
        observeData()
    }

    private fun setupUI() {
        episodesAdapter = EpisodeAdapter { episode ->
            val index = episodesAdapter.episodes.indexOf(episode)
            navigateToPlayer(index)
        }

        binding.episodesRecyclerView.layoutManager = GridLayoutManager(this, 6)
        binding.episodesRecyclerView.adapter = episodesAdapter

        binding.playButton.setOnClickListener {
            navigateToPlayer(viewModel.currentEpisodeIndex)
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun loadDetail() {
        viewModel.loadDetail(vodId)
    }

    private fun observeData() {
        viewModel.detail.observe(this) { detail ->
            detail?.let {
                binding.titleTextView.text = it.vodName
                binding.scoreTextView.text = if (it.vodScore > 0) String.format("%.1f分", it.vodScore) else ""
                binding.yearTextView.text = it.vodYear
                binding.areaTextView.text = it.vodArea
                binding.directorTextView.text = getString(com.familytv.app.R.string.director) + ": " + it.vodDirector
                binding.actorTextView.text = getString(com.familytv.app.R.string.actor) + ": " + it.vodActor
                binding.introTextView.text = it.vodContent
                binding.backdropImageView.loadImage(it.vodPic)

                val episodes = viewModel.parseEpisodes(it)
                episodesAdapter.setEpisodes(episodes)

                lifecycleScope.launch {
                    val progress = viewModel.getHistoryProgress(it.vodId)
                    if (progress != null) {
                        viewModel.currentEpisodeIndex = progress.first
                        episodesAdapter.setSelectedIndex(progress.first)
                    }
                }
            }
        }

        viewModel.isFavorite.observe(this) { isFav ->
            binding.favoriteButton.isSelected = isFav
        }

        lifecycleScope.launch {
            viewModel.loadState.collectLatest { state ->
                binding.playButton.visibility = if (state is DetailViewModel.LoadState.Success) View.VISIBLE else View.GONE
            }
        }
    }

    private fun navigateToPlayer(episodeIndex: Int) {
        viewModel.detail.value?.let { detail ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("vodId", detail.vodId)
                putExtra("vodName", detail.vodName)
                putExtra("episodeIndex", episodeIndex)
                putExtra("vodPic", detail.vodPic)
            }
            startActivity(intent)
        }
    }
}
