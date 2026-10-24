package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

enum class SessionMode {
    Focus,
    Normal
}

data class CycleProgress(
    val days: Int,
    val heard: Int,
    val total: Int
)

data class StrengthenLoad(
    val due: Int,
    val crowded: Boolean
)

data class NextDue(
    val date: String,
    val cards: Int,
    val inDays: Int
)

object MurajaahScheduler {
    const val MAX_STABILITY_DAYS = 60
    const val NEW_PER_DAY = 3
    const val AYAHS_PER_JUZ = 6236.0 / 30.0 // ~207.866

    private val STATUS_WEIGHT = mapOf(
        Rating.Weak to 100.0,
        Rating.Review to 60.0,
        Rating.Strong to 20.0
    )
    private const val OVERDUE_WEIGHT = 8.0
    private const val STALENESS_WEIGHT = 1.5
    private const val DUE_BONUS = 1000.0

    private val STRENGTHEN_SHARE_MIN = 1.0 / 3.0
    private val STRENGTHEN_SHARE_MAX = 2.0 / 3.0

    /**
     * Traditional Islamic Khatmah cycle days based on total memorized ayahs.
     */
    fun revisionCycleDays(cards: List<Card>): Int {
        val totalAyahs = cards.sumOf { it.toAyah - it.fromAyah + 1 }
        val ajza = totalAyahs / AYAHS_PER_JUZ
        return when {
            ajza <= 5.0 -> 7
            ajza <= 15.0 -> 14
            ajza <= 25.0 -> 21
            else -> 30
        }
    }

    fun priority(card: Card, on: String): Double {
        val due = DateUtils.daysUntilDue(card.dueAt, on)
        val overdue = max(0, -due)
        val stale = DateUtils.daysSinceReview(card.lastReviewedAt, on)
        val bonus = if (due <= 0) DUE_BONUS else 0.0
        val weight = STATUS_WEIGHT[card.status] ?: 20.0
        return bonus + weight + (overdue * OVERDUE_WEIGHT) + (stale * STALENESS_WEIGHT)
    }

    fun sessionCap(mode: SessionMode, daysAway: Int): Int {
        if (mode == SessionMode.Focus) return 3
        return when {
            daysAway <= 1 -> 8
            daysAway <= 3 -> 6
            daysAway <= 7 -> 5
            else -> 3
        }
    }

    fun isReturningAfterGap(daysAway: Int): Boolean = daysAway >= 4

    fun isStrengthening(card: Card): Boolean =
        card.reviewCount > 0 && card.status != Rating.Strong

    fun dueCards(cards: List<Card>, on: String): List<Card> =
        cards.filter { DateUtils.daysUntilDue(it.dueAt, on) <= 0 }

    fun dueTodayCount(cards: List<Card>, on: String): Int =
        dueCards(cards, on).size

    fun byPriority(cards: List<Card>, on: String, random: Random = Random.Default): List<Card> {
        val shuffled = cards.shuffled(random)
        return shuffled.sortedByDescending { priority(it, on) }
    }

    fun buildQueue(
        cards: List<Card>,
        on: String,
        cap: Int,
        exclude: List<String> = emptyList(),
        random: Random = Random.Default
    ): List<Card> {
        val due = byPriority(
            dueCards(cards, on).filter { !exclude.contains(it.id) },
            on,
            random
        )
        val weak = due.filter { isStrengthening(it) }
        val rotation = due.filter { !isStrengthening(it) }

        val total = weak.size + rotation.size
        val slots = if (total == 0) 0 else {
            val share = min(STRENGTHEN_SHARE_MAX, max(STRENGTHEN_SHARE_MIN, weak.size.toDouble() / total))
            (cap * share).roundToInt()
        }

        val fromWeak = min(weak.size, max(slots, cap - rotation.size))
        val fromRotation = min(rotation.size, cap - fromWeak)

        return weak.take(fromWeak) + rotation.take(fromRotation)
    }

    fun extendQueue(
        cards: List<Card>,
        queue: List<String>,
        on: String,
        extra: Int = 2,
        random: Random = Random.Default
    ): List<Card> {
        return byPriority(
            dueCards(cards, on).filter { !queue.contains(it.id) },
            on,
            random
        ).take(extra)
    }

    fun aheadQueue(
        cards: List<Card>,
        on: String,
        count: Int,
        exclude: List<String> = emptyList(),
        random: Random = Random.Default
    ): List<Card> {
        return byPriority(
            cards.filter { DateUtils.daysUntilDue(it.dueAt, on) > 0 && !exclude.contains(it.id) },
            on,
            random
        ).sortedBy { it.dueAt }.take(count)
    }

    fun nextDue(cards: List<Card>, on: String): NextDue? {
        val later = cards
            .filter { DateUtils.daysUntilDue(it.dueAt, on) > 0 }
            .map { it.dueAt }
            .sorted()
        val firstDate = later.firstOrNull() ?: return null
        return NextDue(
            date = firstDate,
            cards = later.count { it == firstDate },
            inDays = DateUtils.daysUntilDue(firstDate, on)
        )
    }

    fun strengthenLoad(cards: List<Card>, on: String, cap: Int): StrengthenLoad {
        val due = dueCards(cards, on).count { isStrengthening(it) }
        val threshold = 2 * (cap * STRENGTHEN_SHARE_MAX).roundToInt()
        return StrengthenLoad(due = due, crowded = due > threshold)
    }

    fun cycleProgress(cards: List<Card>, on: String): CycleProgress {
        val days = revisionCycleDays(cards)
        val since = DateUtils.addDays(on, -(days - 1))
        val heard = cards.count { it.lastReviewedAt != null && it.lastReviewedAt >= since }
        return CycleProgress(days = days, heard = heard, total = cards.size)
    }
}
