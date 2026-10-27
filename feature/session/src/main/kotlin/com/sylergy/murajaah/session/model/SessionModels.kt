package com.sylergy.murajaah.session.model

import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.scheduler.Card
import com.sylergy.murajaah.scheduler.MendResult

data class SessionCard(
    val card: Card,
    val surahName: String,
    val surahArabicName: String,
    val firstAyahText: String? = null,
    val juz: Int? = null,
    val page: Int? = null
) {
    val ayahsCount: Int get() = card.ayahCount
}

data class RecitationAttempt(
    val cardId: String,
    val startedAtMs: Long = System.currentTimeMillis(),
    val elapsedSeconds: Int = 0,
    val hintsUsed: Int = 0,
    val slippedAyahs: Set<Int> = emptySet(),
    val selectedGrade: Rating? = null,
    val mendResult: MendResult? = null
)

sealed interface SessionPhase {
    data class Prompt(val card: SessionCard, val hintsUsed: Int = 0) : SessionPhase

    data class InspectMushaf(val card: SessionCard, val attempt: RecitationAttempt) : SessionPhase

    /**
     * Spot Repair (Make it firm / Tathbeet).
     * Immediate intervention after a Shaky (Review) or Weak recitation.
     */
    data class SpotRepair(
        val card: SessionCard,
        val attempt: RecitationAttempt,
        val repairAyahs: List<Int>,
        val totalReadsNeeded: Int,
        val currentRead: Int = 1
    ) : SessionPhase

    /**
     * Before closing the sitting, retest what slipped during today's session.
     */
    data class Retest(
        val card: SessionCard,
        val attempt: RecitationAttempt,
        val isTextRevealed: Boolean = false,
        val remainingInRetest: Int = 1
    ) : SessionPhase

    data class Summary(
        val completedCount: Int,
        val totalAyahs: Int,
        val totalSeconds: Int,
        val strongCount: Int,
        val reviewCount: Int,
        val weakCount: Int,
        val mendedCount: Int = 0,
        val retestHeldCount: Int = 0,
        val isFreeSession: Boolean = false,
        val history: List<Pair<SessionCard, RecitationAttempt>>
    ) : SessionPhase
}

object SessionRules {
    const val DEFAULT_SECONDS_PER_AYAH = 8
    const val RECALL_FLOOR_SHARE = 0.75
    const val RECALL_FLOOR_MIN = 5 // User-friendly floor for mobile
    const val RECALL_FLOOR_MAX = 90
    const val MAX_HINTS = 2

    fun recallFloorSeconds(ayahs: Int): Int {
        val expectedSeconds = ayahs * DEFAULT_SECONDS_PER_AYAH
        val floor = Math.round(expectedSeconds * RECALL_FLOOR_SHARE).toInt()
        return floor.coerceIn(RECALL_FLOOR_MIN, RECALL_FLOOR_MAX)
    }

    fun suggestedGrade(ayahs: Int, slippedCount: Int, hintsUsed: Int): Rating {
        val allowedSlips = Math.max(2, ayahs / 5)
        return when {
            slippedCount > allowedSlips -> Rating.Weak
            slippedCount > 0 || hintsUsed > 0 -> Rating.Review
            else -> Rating.Strong
        }
    }
}
