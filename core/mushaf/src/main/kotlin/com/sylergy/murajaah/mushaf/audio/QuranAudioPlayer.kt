package com.sylergy.murajaah.mushaf.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Live audio streaming player for Qur'anic recitations with ayah-synchronized tracking.
 */
class QuranAudioPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentClip: AudioClip? = null
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeAyah = MutableStateFlow<Int?>(null)
    val activeAyah: StateFlow<Int?> = _activeAyah.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun play(clip: AudioClip) {
        stop()
        currentClip = clip
        _isLoading.value = true

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
        mediaPlayer = player

        try {
            player.setDataSource(clip.url)
            player.setOnPreparedListener { mp ->
                _isLoading.value = false
                applySpeed(mp, _playbackSpeed.value)
                mp.seekTo(clip.startMs)
                mp.start()
                _isPlaying.value = true
                startProgressTracker(clip)
            }
            player.setOnCompletionListener {
                stop()
            }
            player.setOnErrorListener { _, _, _ ->
                stop()
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            stop()
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        } else {
            player.start()
            _isPlaying.value = true
            currentClip?.let { startProgressTracker(it) }
        }
    }

    fun cycleSpeed() {
        val nextSpeed = when (_playbackSpeed.value) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            else -> 1.0f
        }
        _playbackSpeed.value = nextSpeed
        mediaPlayer?.let { applySpeed(it, nextSpeed) }
    }

    private fun applySpeed(player: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = speed
                player.playbackParams = params
            } catch (ignored: Exception) {}
        }
    }

    private fun startProgressTracker(clip: AudioClip) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition

                    // Check clip upper bound
                    if (pos >= clip.endMs) {
                        stop()
                        break
                    }

                    // Identify which ayah is currently playing
                    val ayah = clip.ayahs.find { pos in it.startMs..it.endMs }?.ayahNumber
                    _activeAyah.value = ayah
                }
                delay(100)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
        currentClip = null
        _isPlaying.value = false
        _isLoading.value = false
        _activeAyah.value = null
    }

    fun release() {
        stop()
    }
}
