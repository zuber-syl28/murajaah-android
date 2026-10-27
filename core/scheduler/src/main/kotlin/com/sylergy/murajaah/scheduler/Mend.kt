package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import kotlin.math.max
import kotlin.math.min

/**
 * Mending a recitation that slipped ("Make it firm" / Tathbeet).
 * Teachers of the Qur'an have a slipped place read again several times,
 * joined to the ayah before and after, and heard once more before moving on.
 *
 * Sourced from Murajaah core engine specification (packages/shared/src/mend.ts).
 */
enum class MendResult {
    Held,
    Slipped,
    Skipped
}

object MendRules {
    /** Weak this many times in a row: the whole passage is taken up again. */
    const val STUBBORN_STREAK = 3

    /**
     * Number of times slipped ayahs are read, looking at the Mushaf,
     * before reciting them again.
     */
    fun fixReads(rating: Rating?, weakStreak: Int = 0): Int {
        if (weakStreak >= STUBBORN_STREAK) return 7
        return if (rating == Rating.Weak) 5 else 3
    }

    /**
     * Determines which ayahs should be read during Spot Repair.
     * If stubborn streak or no ayahs explicitly marked, takes whole passage.
     * Otherwise, expands the slipped ayahs by 1 ayah on either side, clamped to card boundaries.
     */
    fun repairAyahs(
        fromAyah: Int,
        toAyah: Int,
        slippedAyahs: Set<Int>,
        stubborn: Boolean = false
    ): List<Int> {
        if (stubborn || slippedAyahs.isEmpty()) {
            return (fromAyah..toAyah).toList()
        }
        val minAyah = max(fromAyah, (slippedAyahs.minOrNull() ?: fromAyah) - 1)
        val maxAyah = min(toAyah, (slippedAyahs.maxOrNull() ?: toAyah) + 1)
        return (minAyah..maxAyah).toList()
    }

    /**
     * True if the rating warrants a spot repair intervention.
     */
    fun shouldMend(rating: Rating): Boolean =
        rating == Rating.Weak || rating == Rating.Review
}
