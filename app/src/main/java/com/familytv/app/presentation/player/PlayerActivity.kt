package com.familytv.app.presentation.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.familytv.app.data.model.PlayEpisode
import com.familytv.app.data.model.VodItem
import com.familytv.app.data.repository.VodRepository
import com.familytv.app.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var episodes = mutableListOf<PlayEpisode>()
    private var currentEpisodeIndex = 0
    private var vodId: Long = 0
    private var vodName: String = ""
    private val controlBarHandler = Handler(Looper.getMainLooper())
    private val controlBarRunnable = Runnable { hideControlBar() }
    private var isControlBarVisible = false
    private var isPlayerInitialized = false

    @Inject
    lateinit var repository: VodRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vodId = intent.getLongExtra("vodId", 0)
        vodName = intent.getStringExtra("vodName") ?: ""
        currentEpisodeIndex = intent.getIntExtra("episodeIndex", 0)

        initializePlayer()
        setupControlBar()
        loadVideoDetail()
    }

    private fun initializePlayer() {
        try {
            val playerBuilder = ExoPlayer.Builder(this)
            player = playerBuilder.build()
            isPlayerInitialized = true
            binding.playerView.player = player

            player?.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    showError()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> showLoading()
                        Player.STATE_READY -> hideLoading()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon(isPlaying)
                }
            })

            binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val duration = player?.duration ?: 0
                        if (duration > 0) {
                            val seekPosition = (duration * progress / 100).toLong()
                            player?.seekTo(seekPosition)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        } catch (e: Exception) {
            showError()
            return
        }
    }

    private fun loadVideoDetail() {
        lifecycleScope.launch {
            try {
                val result = repository.getVideoDetail(vodId)
                if (result.isSuccess) {
                    val video = result.getOrNull()
                    if (video != null) {
                        episodes.clear()
                        episodes.addAll(repository.parseEpisodes(video))

                        if (episodes.isNotEmpty() && currentEpisodeIndex < episodes.size) {
                            playEpisode(currentEpisodeIndex)
                        } else {
                            showError()
                        }
                    } else {
                        showError()
                    }
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showError()
            }
        }
    }

    private fun playEpisode(index: Int) {
        if (index < 0 || index >= episodes.size) return

        currentEpisodeIndex = index
        val episode = episodes[index]

        binding.episodeTitleTextView.text = "${vodName} - ${episode.title}"

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("FamilyTV/1.0")

        val mediaItem = MediaItem.fromUri(episode.url)
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.play()

        showControlBar()
    }

    private fun setupControlBar() {
        binding.playPauseButton.setOnClickListener {
            val p = player ?: return@setOnClickListener
            if (p.isPlaying) {
                p.pause()
            } else {
                p.play()
            }
            showControlBar()
        }

        binding.prevEpisodeButton.setOnClickListener {
            if (currentEpisodeIndex > 0) {
                playEpisode(currentEpisodeIndex - 1)
            }
        }

        binding.nextEpisodeButton.setOnClickListener {
            if (currentEpisodeIndex < episodes.size - 1) {
                playEpisode(currentEpisodeIndex + 1)
            }
        }
    }

    private fun showControlBar() {
        binding.controlBar.visibility = View.VISIBLE
        isControlBarVisible = true
        controlBarHandler.removeCallbacks(controlBarRunnable)
        controlBarHandler.postDelayed(controlBarRunnable, 5000)
    }

    private fun hideControlBar() {
        binding.controlBar.visibility = View.GONE
        isControlBarVisible = false
    }

    private fun showLoading() {
        binding.loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgressBar.visibility = View.GONE
    }

    private fun showError() {
        binding.errorTextView.visibility = View.VISIBLE
        binding.loadingProgressBar.visibility = View.GONE
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        binding.playPauseButton.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun saveProgress() {
        val p = player ?: return
        if (!isPlayerInitialized) return
        val currentPosition = try { p.currentPosition } catch (e: Exception) { return }
        if (currentPosition > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repository.addHistory(
                        VodItem(
                            vodId = vodId,
                            vodName = vodName,
                            vodPic = intent.getStringExtra("vodPic") ?: "",
                            vodYear = "",
                            vodArea = "",
                            vodScore = "0.0",
                            typeName = "",
                            vodRemarks = "",
                            vodActor = "",
                            vodDirector = "",
                            vodContent = ""
                        ),
                        currentEpisodeIndex,
                        currentPosition
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                showControlBar()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                val p = player ?: return true
                val newPosition = (p.currentPosition - 10000).coerceAtLeast(0)
                p.seekTo(newPosition)
                showControlBar()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                val p = player ?: return true
                val duration = p.duration
                val newPosition = if (duration > 0) {
                    (p.currentPosition + 10000).coerceAtMost(duration)
                } else {
                    p.currentPosition + 10000
                }
                p.seekTo(newPosition)
                showControlBar()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                val p = player ?: return true
                if (p.isPlaying) p.pause() else p.play()
                showControlBar()
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                saveProgress()
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onPause() {
        super.onPause()
        saveProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        controlBarHandler.removeCallbacksAndMessages(null)
    }
}
