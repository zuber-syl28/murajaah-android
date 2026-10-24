package com.sylergy.murajaah.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.voice.audio.AudioEngine
import com.sylergy.murajaah.voice.crypto.VoiceCipher
import com.sylergy.murajaah.voice.model.EndReason
import com.sylergy.murajaah.voice.model.VoicePassage
import com.sylergy.murajaah.voice.model.VoiceStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel managing peer-to-peer encrypted voice sessions in Murajaah.
 */
class VoiceSessionViewModel : ViewModel() {

    private val _status = MutableStateFlow<VoiceStatus>(VoiceStatus.Idle)
    val status: StateFlow<VoiceStatus> = _status.asStateFlow()

    private var audioEngine: AudioEngine? = null
    private var cipher: VoiceCipher? = null
    private var timerJob: Job? = null
    private var audioCollectJob: Job? = null

    /**
     * Start reciting to a companion who will listen and grade.
     */
    fun reciteTo(friendId: String, friendName: String) {
        startCall(friendId = friendId, friendName = friendName, isReciter = true)
    }

    /**
     * Hear a companion recite their due cards.
     */
    fun hearFrom(friendId: String, friendName: String) {
        startCall(friendId = friendId, friendName = friendName, isReciter = false)
    }

    private fun startCall(friendId: String, friendName: String, isReciter: Boolean) {
        cleanup()
        val callId = UUID.randomUUID().toString()
        _status.value = VoiceStatus.Starting(friendId, friendName, isReciter)

        viewModelScope.launch {
            delay(400) // Brief setup
            _status.value = VoiceStatus.Waiting(
                callId = callId,
                friendId = friendId,
                name = friendName,
                isReciter = isReciter,
                secondsLeft = 120
            )

            // Start countdown for waiting
            startWaitingCountdown(callId, friendId, friendName, isReciter)
        }
    }

    private fun startWaitingCountdown(
        callId: String,
        friendId: String,
        friendName: String,
        isReciter: Boolean
    ) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = 120
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                val cur = _status.value
                if (cur is VoiceStatus.Waiting && cur.callId == callId) {
                    _status.value = cur.copy(secondsLeft = remaining)
                } else {
                    break
                }
            }
            if (remaining <= 0 && _status.value is VoiceStatus.Waiting) {
                _status.value = VoiceStatus.Ended(friendName, EndReason.NO_ANSWER)
            }
        }
    }

    /**
     * Simulates companion receiving the call or peer connection established.
     */
    fun simulateFriendAnswers() {
        val current = _status.value
        if (current is VoiceStatus.Waiting) {
            connectCall(
                callId = current.callId,
                friendId = current.friendId,
                friendName = current.name,
                isReciter = current.isReciter
            )
        }
    }

    /**
     * Triggers an incoming call alert (e.g. from push or circle sync).
     */
    fun notifyIncomingCall(callId: String, friendId: String, friendName: String, toHear: Boolean) {
        if (_status.value is VoiceStatus.Connected || _status.value is VoiceStatus.Connecting) {
            return // Busy
        }
        cleanup()
        _status.value = VoiceStatus.Incoming(
            callId = callId,
            friendId = friendId,
            name = friendName,
            toHear = toHear
        )
    }

    /**
     * Answers an incoming call.
     */
    fun joinCall(callId: String) {
        val current = _status.value
        if (current is VoiceStatus.Incoming && current.callId == callId) {
            connectCall(
                callId = callId,
                friendId = current.friendId,
                friendName = current.name,
                isReciter = !current.toHear
            )
        }
    }

    private fun connectCall(
        callId: String,
        friendId: String,
        friendName: String,
        isReciter: Boolean
    ) {
        timerJob?.cancel()
        _status.value = VoiceStatus.Connecting(callId, friendId, friendName, isReciter)

        viewModelScope.launch {
            delay(600) // Simulated WebRTC ICE/DTLS handshake
            val sessionCipher = VoiceCipher(sharedSecret = "murajaah-call-$callId")
            cipher = sessionCipher

            val engine = AudioEngine(cipher = sessionCipher)
            audioEngine = engine

            engine.startPlayback()
            engine.startRecording(viewModelScope)

            // Loopback transport for simulated P2P stream
            engine.onEncryptedFrameReady = { encryptedFrame ->
                engine.receiveAndPlayFrame(encryptedFrame)
            }

            _status.value = VoiceStatus.Connected(
                callId = callId,
                friendId = friendId,
                name = friendName,
                isReciter = isReciter,
                durationSeconds = 0,
                audioLevel = 0f,
                isMuted = false,
                e2eVerified = true,
                fingerprint = sessionCipher.securityFingerprint
            )

            // Start call duration timer & audio level observer
            startConnectedSession(callId, friendId, friendName, isReciter, sessionCipher.securityFingerprint)
        }
    }

    private fun startConnectedSession(
        callId: String,
        friendId: String,
        friendName: String,
        isReciter: Boolean,
        fingerprint: String
    ) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1000)
                seconds++
                val cur = _status.value
                if (cur is VoiceStatus.Connected && cur.callId == callId) {
                    _status.value = cur.copy(durationSeconds = seconds)
                } else {
                    break
                }
            }
        }

        audioCollectJob?.cancel()
        audioEngine?.let { engine ->
            audioCollectJob = viewModelScope.launch {
                engine.audioLevel.collect { level ->
                    val cur = _status.value
                    if (cur is VoiceStatus.Connected && cur.callId == callId) {
                        _status.value = cur.copy(audioLevel = level)
                    }
                }
            }
        }
    }

    /**
     * Toggles microphone mute.
     */
    fun toggleMute() {
        val engine = audioEngine ?: return
        val muted = engine.toggleMute()
        val cur = _status.value
        if (cur is VoiceStatus.Connected) {
            _status.value = cur.copy(isMuted = muted)
        }
    }

    /**
     * Shares the current recitation passage between reciter and listener.
     */
    fun sharePassage(surahName: String, surahArabicName: String, fromAyah: Int, toAyah: Int) {
        val cur = _status.value
        if (cur is VoiceStatus.Connected) {
            _status.value = cur.copy(
                currentPassage = VoicePassage(
                    surahName = surahName,
                    surahArabicName = surahArabicName,
                    fromAyah = fromAyah,
                    toAyah = toAyah
                )
            )
        }
    }

    /**
     * Ends the voice call.
     */
    fun endCall() {
        val cur = _status.value
        val name = when (cur) {
            is VoiceStatus.Starting -> cur.name
            is VoiceStatus.Waiting -> cur.name
            is VoiceStatus.Connecting -> cur.name
            is VoiceStatus.Connected -> cur.name
            is VoiceStatus.Incoming -> cur.name
            is VoiceStatus.Ended -> cur.name
            VoiceStatus.Idle -> "Companion"
        }
        cleanup()
        _status.value = VoiceStatus.Ended(name, EndReason.HUNG_UP)
    }

    /**
     * Dismisses ended status back to Idle.
     */
    fun dismiss() {
        cleanup()
        _status.value = VoiceStatus.Idle
    }

    private fun cleanup() {
        timerJob?.cancel()
        timerJob = null
        audioCollectJob?.cancel()
        audioCollectJob = null
        audioEngine?.release()
        audioEngine = null
        cipher = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
