package com.sylergy.murajaah.mushaf.audio

/**
 * Millisecond timestamp boundaries for a single ayah in the reciter's audio stream.
 */
data class AyahTiming(
    val ayahNumber: Int,
    val startMs: Int,
    val endMs: Int
)

/**
 * A recitation clip for a specific passage in the account's narration.
 * Sourced from Murajaah audio specification (apps/web/src/quran/audio.ts).
 */
data class AudioClip(
    val url: String,
    val startMs: Int,
    val endMs: Int,
    val ayahs: List<AyahTiming>
)
