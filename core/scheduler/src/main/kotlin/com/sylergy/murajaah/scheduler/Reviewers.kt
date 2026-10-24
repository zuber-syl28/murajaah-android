package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.FsrsInput
import com.sylergy.murajaah.fsrs.MemoryState
import com.sylergy.murajaah.fsrs.MurajaahFsrs
import com.sylergy.murajaah.fsrs.Rating
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GradeRule {
    Full,
    PeerWeighted,
    TeacherHold
}

data class WeightedGrade(
    val card: Card,
    val days: Int,
    val stability: Double,
    val status: Rating,
    val rule: GradeRule
)

object Reviewers {
    const val TEACHER_HOLD_DAYS = 30
    const val PEER_STRONG_WEIGHT = 0.625
    const val SELF_CHECK_CEILING = 0.84

    fun standingTeacherLapse(history: List<GradeRecord>, on: String): GradeRecord? {
        val latest = history.firstOrNull { it.role == ReviewerRole.Teacher } ?: return null
        if (latest.rating == Rating.Strong) return null
        val days = DateUtils.daysBetween(latest.recordedAt, on)
        return if (days <= TEACHER_HOLD_DAYS) latest else null
    }

    fun applyWeightedRating(
        card: Card,
        rating: Rating,
        role: ReviewerRole,
        history: List<GradeRecord>,
        on: String,
        cycleDays: Int = MurajaahScheduler.MAX_STABILITY_DAYS
    ): WeightedGrade {
        val memory = memoryOf(card)
        val previousInterval = if (card.reviewCount > 0) {
            card.scheduledDays ?: card.stability.roundToInt()
        } else null

        // Teacher hold safety brake
        if (role != ReviewerRole.Teacher && rating == Rating.Strong && standingTeacherLapse(history, on) != null) {
            val kept = memory ?: MemoryState(
                stability = card.stability,
                difficulty = MurajaahFsrs.initialDifficulty(Rating.Review)
            )
            val days = max(
                2,
                (min(min(kept.stability, MurajaahScheduler.MAX_STABILITY_DAYS.toDouble()), cycleDays.toDouble()) * 0.6).roundToInt()
            )
            val updatedCard = card.copy(
                status = Rating.Review,
                stability = kept.stability,
                difficulty = kept.difficulty,
                scheduledDays = days,
                dueAt = DateUtils.addDays(on, days),
                lastReviewedAt = on,
                reviewCount = card.reviewCount + 1,
                gradedBy = role
            )
            return WeightedGrade(
                card = updatedCard,
                days = days,
                stability = kept.stability,
                status = Rating.Review,
                rule = GradeRule.TeacherHold
            )
        }

        val peerWeighted = role == ReviewerRole.Peer &&
                rating == Rating.Strong &&
                history.any { it.role == ReviewerRole.Teacher }

        val scheduled = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = rating,
                elapsedDays = if (card.lastReviewedAt != null) {
                    DateUtils.daysBetween(card.lastReviewedAt, on).toDouble()
                } else 0.0,
                dueInDays = DateUtils.daysUntilDue(card.dueAt, on),
                previousInterval = previousInterval,
                growthWeight = if (peerWeighted) PEER_STRONG_WEIGHT else 1.0,
                maxDays = cycleDays
            )
        )

        val updatedCard = card.copy(
            status = rating,
            stability = scheduled.stability,
            difficulty = scheduled.difficulty,
            scheduledDays = scheduled.days,
            dueAt = DateUtils.addDays(on, scheduled.days),
            lastReviewedAt = on,
            reviewCount = card.reviewCount + 1,
            gradedBy = role
        )

        return WeightedGrade(
            card = updatedCard,
            days = scheduled.days,
            stability = scheduled.stability,
            status = rating,
            rule = if (peerWeighted) GradeRule.PeerWeighted else GradeRule.Full
        )
    }

    fun memoryOf(card: Card): MemoryState? {
        if (card.difficulty != null) {
            return MemoryState(card.stability, card.difficulty)
        }
        if (card.reviewCount == 0) return null
        return MemoryState(card.stability, MurajaahFsrs.initialDifficulty(card.status))
    }
}
