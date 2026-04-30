package com.familytv.app.presentation.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.familytv.app.data.local.AppDatabase
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player: ExoPlayer
    private var episodes = mutableListOf<PlayEpisode>()
    private var currentEpisodeIndex = 0
    private var vodId: Long = 0
    private var vodName: String = ""
    private val controlBarHandler = Handler(Looper.getMainLooper())
    private val controlBarRunnable = Runnable { hideControlBar() }
    private var isControlBarVisible = false

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
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        player.addListener(object : Player.Listener {
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
                    val duration = player.duration
                    val seekPosition = (duration * progress / 100).toLong()
                    player.seekTo(seekPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun loadVideoDetail() {
        lifecycleScope.launch {
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
                }
            } else {
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

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()

        showControlBar()
    }

    private fun setupControlBar() {
        binding.playPauseButton.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
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
        if (player.currentPosition > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
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
                    player.currentPosition
                )
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
                player.seekTo(player.currentPosition - 10000)
                showControlBar()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                player.seekTo(player.currentPosition + 10000)
                showControlBar()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (player.isPlaying) player.pause() else player.play()
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
        player.release()
        controlBarHandler.removeCallbacksAndMessages(null)
    }
}
