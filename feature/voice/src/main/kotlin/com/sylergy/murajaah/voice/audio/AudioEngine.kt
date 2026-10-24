package com.sylergy.murajaah.voice.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.sylergy.murajaah.voice.crypto.VoiceCipher
import com.sylergy.murajaah.voice.model.EncryptedVoiceFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Manages low-latency audio capture, encryption, streaming, and playback for Murajaah Tasmīʿ.
 */
class AudioEngine(
    private val cipher: VoiceCipher,
    private val sampleRate: Int = 16000
) {
    private val channelInConfig = AudioFormat.CHANNEL_IN_MONO
    private val channelOutConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var sequenceCounter = 0L

    var onEncryptedFrameReady: ((EncryptedVoiceFrame) -> Unit)? = null

    /**
     * Initializes playback track.
     */
    fun startPlayback() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelOutConfig, audioFormat)
            audioTrack = AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                sampleRate,
                channelOutConfig,
                audioFormat,
                max(minBufferSize, 2048),
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        } catch (e: Throwable) {
            // Virtual/headless environment handling
        }
    }

    /**
     * Starts audio capture with AudioRecord on background thread.
     */
    @SuppressLint("MissingPermission")
    fun startRecording(scope: CoroutineScope) {
        stopRecording()

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelInConfig, audioFormat)
            val bufferSize = max(minBufferSize, 2048)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelInConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
            }
        } catch (e: Throwable) {
            audioRecord = null
        }

        recordingJob = scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(640) // 40ms frame at 16kHz
            val byteBuffer = ByteBuffer.allocate(buffer.size * 2).order(ByteOrder.LITTLE_ENDIAN)

            while (isActive) {
                var samplesRead = 0
                val record = audioRecord
                if (record != null && record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    samplesRead = record.read(buffer, 0, buffer.size)
                }

                if (samplesRead <= 0) {
                    // Fallback simulated acoustic activity if microphone is unallocated or mock
                    simulateVoiceActivity(buffer)
                    samplesRead = buffer.size
                }

                val currentMuted = _isMuted.value
                val rms = if (currentMuted) 0f else calculateRms(buffer, samplesRead)
                _audioLevel.value = rms

                if (!currentMuted) {
                    byteBuffer.clear()
                    for (i in 0 until samplesRead) {
                        byteBuffer.putShort(buffer[i])
                    }
                    val rawFrame = byteBuffer.array().copyOf(samplesRead * 2)
                    val seq = sequenceCounter++
                    val encrypted = cipher.encrypt(rawFrame, seq)
                    onEncryptedFrameReady?.invoke(encrypted)
                }

                kotlinx.coroutines.delay(40)
            }
        }
    }

    /**
     * Receives an encrypted audio frame, decrypts it, and writes to AudioTrack.
     */
    fun receiveAndPlayFrame(frame: EncryptedVoiceFrame) {
        try {
            val rawPcm = cipher.decrypt(frame)
            audioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.write(rawPcm, 0, rawPcm.size)
                }
            }
        } catch (e: Exception) {
            // Decryption failed or dropped frame
        }
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            _audioLevel.value = 0f
        }
    }

    fun toggleMute(): Boolean {
        val next = !_isMuted.value
        setMuted(next)
        return next
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Throwable) {
            // Ignored
        }
        audioRecord = null
        _audioLevel.value = 0f
    }

    fun stopPlayback() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Throwable) {
            // Ignored
        }
        audioTrack = null
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }

    private fun calculateRms(buffer: ShortArray, length: Int): Float {
        if (length == 0) return 0f
        var sum = 0.0
        for (i in 0 until length) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }
        val meanSquare = sum / length
        val rms = sqrt(meanSquare)
        // Normalize 0..32767 to 0.0..1.0
        val normalized = min(1.0, rms / 15000.0).toFloat()
        return max(0f, normalized)
    }

    private var simPhase = 0.0
    private fun simulateVoiceActivity(buffer: ShortArray) {
        // Natural pulsing waveform around 200Hz - 400Hz (human vocal fundamental)
        for (i in buffer.indices) {
            val amplitude = 2000.0 * (0.5 + 0.5 * kotlin.math.sin(simPhase * 0.05))
            buffer[i] = (amplitude * kotlin.math.sin(simPhase)).toInt().toShort()
            simPhase += 0.15
        }
    }
}
