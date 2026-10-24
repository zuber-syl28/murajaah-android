package com.sylergy.murajaah.progress.calculator

import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.progress.model.JuzCell
import com.sylergy.murajaah.progress.model.MasteryLevel
import com.sylergy.murajaah.progress.model.MasterySummary
import com.sylergy.murajaah.progress.model.SurahSegment
import com.sylergy.murajaah.scheduler.DateUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object MasteryCalculator {

    const val MAX_DRIFT_DAYS = 30.0
    const val MIN_DRIFT_FACTOR = 0.55
    const val SELF_CHECK_CEILING = 0.84

    val GRADE_VALUE = mapOf(
        "strong" to 1.0,
        "review" to 0.58,
        "weak" to 0.22
    )

    fun ayahCount(card: CardEntity): Int {
        return max(1, card.toAyah - card.fromAyah + 1)
    }

    fun isTested(card: CardEntity): Boolean {
        return card.reviewCount > 0
    }

    fun driftFrom(card: CardEntity): String {
        val lastReviewed = card.lastReviewedAt ?: return card.dueAt
        val scheduledDays = card.scheduledDays ?: card.stability.roundToInt()
        val earned = DateUtils.addDays(lastReviewed, scheduledDays)
        return if (earned < card.dueAt) earned else card.dueAt
    }

    fun cardMastery(card: CardEntity, today: String): Double {
        val driftOrigin = driftFrom(card)
        val dueInDays = DateUtils.daysBetween(today, driftOrigin)
        val drift = if (dueInDays < 0) {
            max(MIN_DRIFT_FACTOR, 1.0 + dueInDays / MAX_DRIFT_DAYS)
        } else {
            1.0
        }

        val consensus = card.consensus
        val base: Double = if (consensus != null) {
            consensus
        } else {
            val statusVal = GRADE_VALUE[card.status.lowercase()] ?: 0.58
            if (card.gradedBy == "self") {
                min(statusVal, SELF_CHECK_CEILING)
            } else {
                statusVal
            }
        }

        return base * drift
    }

    fun overallMastery(cards: List<CardEntity>, today: String): Double {
        if (cards.isEmpty()) return 0.0
        var weightedSum = 0.0
        var totalAyahs = 0
        for (card in cards) {
            val count = ayahCount(card)
            totalAyahs += count
            if (isTested(card)) {
                weightedSum += cardMastery(card, today) * count
            }
        }
        return if (totalAyahs > 0) weightedSum / totalAyahs else 0.0
    }

    fun testedMastery(cards: List<CardEntity>, today: String): Double? {
        val tested = cards.filter { isTested(it) }
        if (tested.isEmpty()) return null
        var weightedSum = 0.0
        var totalAyahs = 0
        for (card in tested) {
            val count = ayahCount(card)
            totalAyahs += count
            weightedSum += cardMastery(card, today) * count
        }
        return if (totalAyahs > 0) weightedSum / totalAyahs else null
    }

    fun getMasteryLevel(fraction: Double): MasteryLevel {
        val pct = fraction * 100.0
        return when {
            pct >= 85.0 -> MasteryLevel.MASTERED
            pct >= 70.0 -> MasteryLevel.SOLID
            pct >= 40.0 -> MasteryLevel.HOLDING
            else -> MasteryLevel.REBUILD
        }
    }

    fun computeMasterySummary(cards: List<CardEntity>, today: String): MasterySummary {
        if (cards.isEmpty()) {
            return MasterySummary(
                value = null,
                level = null,
                testedCount = 0,
                untestedCount = 0,
                totalAyahs = 0,
                heardAyahs = 0,
                heardFraction = 0.0,
                strength = null
            )
        }

        val tested = cards.filter { isTested(it) }
        val totalAyahs = cards.sumOf { ayahCount(it) }
        val heardAyahs = tested.sumOf { ayahCount(it) }
        val overall = overallMastery(cards, today)
        val strength = testedMastery(cards, today)

        return MasterySummary(
            value = overall,
            level = getMasteryLevel(overall),
            testedCount = tested.size,
            untestedCount = cards.size - tested.size,
            totalAyahs = totalAyahs,
            heardAyahs = heardAyahs,
            heardFraction = if (totalAyahs > 0) heardAyahs.toDouble() / totalAyahs else 0.0,
            strength = strength
        )
    }

    fun computeSurahSegments(cards: List<CardEntity>, totalAyahsInSurah: Int): List<SurahSegment> {
        val total = max(1, totalAyahsInSurah)
        val sorted = cards.sortedBy { it.fromAyah }
        data class RawRun(var from: Int, var to: Int, val state: String)
        val runs = mutableListOf<RawRun>()

        for (card in sorted) {
            val from = max(1, card.fromAyah)
            val to = min(total, card.toAyah)
            if (to < from) continue
            val state = if (!isTested(card)) "untested" else card.status.lowercase()
            val last = runs.lastOrNull()
            if (last != null && last.state == state && from <= last.to + 1) {
                last.to = max(last.to, to)
            } else {
                runs.add(RawRun(from, to, state))
            }
        }

        return runs.map { run ->
            SurahSegment(
                fromAyah = run.from,
                toAyah = run.to,
                status = run.state,
                startFraction = (run.from - 1).toFloat() / total.toFloat(),
                widthFraction = (run.to - run.from + 1).toFloat() / total.toFloat()
            )
        }
    }

    fun computeJuzGrid(cards: List<CardEntity>): List<JuzCell> {
        return (1..30).map { juzNum ->
            val juzCards = cards.filter { it.juz == juzNum }
            if (juzCards.isEmpty()) {
                JuzCell(juz = juzNum, status = null, cardCount = 0, ayahCount = 0)
            } else {
                val hasWeak = juzCards.any { isTested(it) && it.status.lowercase() == "weak" }
                val hasReview = juzCards.any { isTested(it) && it.status.lowercase() == "review" }
                val allUntested = juzCards.none { isTested(it) }

                val status = when {
                    allUntested -> "untested"
                    hasWeak -> "weak"
                    hasReview -> "review"
                    else -> "strong"
                }

                JuzCell(
                    juz = juzNum,
                    status = status,
                    cardCount = juzCards.size,
                    ayahCount = juzCards.sumOf { ayahCount(it) }
                )
            }
        }
    }
}
