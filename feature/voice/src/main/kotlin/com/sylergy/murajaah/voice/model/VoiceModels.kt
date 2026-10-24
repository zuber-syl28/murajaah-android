package com.sylergy.murajaah.voice.model

import kotlinx.serialization.Serializable

/**
 * Reason why a peer-to-peer voice recitation session ended.
 */
enum class EndReason {
    HUNG_UP,
    NO_ANSWER,
    MICROPHONE,
    FAILED
}

/**
 * Representation of a passage recited or viewed during live tasmīʿ.
 */
@Serializable
data class VoicePassage(
    val surahName: String,
    val surahArabicName: String,
    val fromAyah: Int,
    val toAyah: Int
)

/**
 * State of peer-to-peer voice recitation session.
 */
sealed interface VoiceStatus {
    object Idle : VoiceStatus

    data class Starting(
        val friendId: String,
        val name: String,
        val isReciter: Boolean
    ) : VoiceStatus

    data class Waiting(
        val callId: String,
        val friendId: String,
        val name: String,
        val isReciter: Boolean,
        val secondsLeft: Int = 120
    ) : VoiceStatus

    data class Connecting(
        val callId: String,
        val friendId: String,
        val name: String,
        val isReciter: Boolean
    ) : VoiceStatus

    data class Connected(
        val callId: String,
        val friendId: String,
        val name: String,
        val isReciter: Boolean,
        val durationSeconds: Int = 0,
        val audioLevel: Float = 0f,
        val isMuted: Boolean = false,
        val currentPassage: VoicePassage? = null,
        val e2eVerified: Boolean = true,
        val fingerprint: String = ""
    ) : VoiceStatus

    data class Incoming(
        val callId: String,
        val friendId: String,
        val name: String,
        val toHear: Boolean
    ) : VoiceStatus

    data class Ended(
        val name: String,
        val reason: EndReason
    ) : VoiceStatus
}

/**
 * An encrypted audio frame sent peer-to-peer with AES-GCM 256-bit authentication.
 */
class EncryptedVoiceFrame(
    val sequenceNumber: Long,
    val iv: ByteArray,
    val cipherText: ByteArray,
    val timestampMs: Long = System.currentTimeMillis()
)
