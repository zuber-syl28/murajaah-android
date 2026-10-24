package com.sylergy.murajaah.progress

import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.progress.calculator.MasteryCalculator
import com.sylergy.murajaah.progress.model.MasteryLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MasteryCalculatorTest {

    private fun createCard(
        id: String,
        surah: Int = 1,
        fromAyah: Int = 1,
        toAyah: Int = 10,
        juz: Int? = 30,
        status: String = "strong",
        stability: Double = 10.0,
        scheduledDays: Int? = null,
        dueAt: String = "2026-10-24",
        lastReviewedAt: String? = "2026-10-14",
        reviewCount: Int = 1,
        gradedBy: String? = "teacher",
        consensus: Double? = null
    ): CardEntity {
        return CardEntity(
            id = id,
            surah = surah,
            fromAyah = fromAyah,
            toAyah = toAyah,
            juz = juz,
            status = status,
            stability = stability,
            scheduledDays = scheduledDays,
            dueAt = dueAt,
            lastReviewedAt = lastReviewedAt,
            reviewCount = reviewCount,
            gradedBy = gradedBy,
            consensus = consensus
        )
    }

    @Test
    fun `empty cards list returns empty summary`() {
        val summary = MasteryCalculator.computeMasterySummary(emptyList(), "2026-10-24")
        assertNull(summary.value)
        assertNull(summary.level)
        assertEquals(0, summary.totalAyahs)
        assertEquals(0, summary.heardAyahs)
        assertNull(summary.strength)
    }

    @Test
    fun `single strong card on-time yields 100 percent mastery`() {
        val card = createCard("c1", status = "strong", dueAt = "2026-10-24", gradedBy = "teacher")
        val score = MasteryCalculator.cardMastery(card, "2026-10-24")
        assertEquals(1.0, score, 0.001)

        val summary = MasteryCalculator.computeMasterySummary(listOf(card), "2026-10-24")
        assertEquals(1.0, summary.value!!, 0.001)
        assertEquals(MasteryLevel.MASTERED, summary.level)
        assertEquals(10, summary.totalAyahs)
        assertEquals(10, summary.heardAyahs)
    }

    @Test
    fun `self-checked strong card is capped at self check ceiling`() {
        val card = createCard("c1", status = "strong", dueAt = "2026-10-24", gradedBy = "self")
        val score = MasteryCalculator.cardMastery(card, "2026-10-24")
        assertEquals(MasteryCalculator.SELF_CHECK_CEILING, score, 0.001)
        assertEquals(MasteryLevel.SOLID, MasteryCalculator.getMasteryLevel(score))
    }

    @Test
    fun `untested cards score zero in overall mastery but are excluded from tested mastery`() {
        val card1 = createCard("c1", fromAyah = 1, toAyah = 10, status = "strong", reviewCount = 1, gradedBy = "teacher")
        val card2 = createCard("c2", fromAyah = 11, toAyah = 20, status = "strong", reviewCount = 0) // untested

        val overall = MasteryCalculator.overallMastery(listOf(card1, card2), "2026-10-24")
        // 10 ayahs * 1.0 + 10 ayahs * 0.0 = 10.0 / 20 = 0.5 (50%)
        assertEquals(0.5, overall, 0.001)

        val tested = MasteryCalculator.testedMastery(listOf(card1, card2), "2026-10-24")
        // Only tested cards: 10 ayahs * 1.0 / 10 = 1.0 (100%)
        assertNotNull(tested)
        assertEquals(1.0, tested, 0.001)
    }

    @Test
    fun `drift discount applies to overdue cards and caps at minimum drift factor`() {
        // Due 15 days ago (half of 30 days max drift)
        val cardHalfDrift = createCard(
            "c1",
            status = "strong",
            dueAt = "2026-10-09",
            lastReviewedAt = "2026-10-01",
            scheduledDays = 8,
            gradedBy = "teacher"
        )
        val scoreHalf = MasteryCalculator.cardMastery(cardHalfDrift, "2026-10-24")
        // 1.0 + (-15 / 30.0) = 0.5 -> but min drift factor is 0.55!
        assertEquals(0.55, scoreHalf, 0.001)

        // Due 60 days ago (past max drift)
        val cardMaxDrift = createCard(
            "c2",
            status = "strong",
            dueAt = "2026-08-25",
            lastReviewedAt = "2026-08-15",
            scheduledDays = 10,
            gradedBy = "teacher"
        )
        val scoreMax = MasteryCalculator.cardMastery(cardMaxDrift, "2026-10-24")
        assertEquals(MasteryCalculator.MIN_DRIFT_FACTOR, scoreMax, 0.001)
    }

    @Test
    fun `surah segments merge contiguous cards with matching state`() {
        val c1 = createCard("c1", surah = 67, fromAyah = 1, toAyah = 10, status = "strong", reviewCount = 1)
        val c2 = createCard("c2", surah = 67, fromAyah = 11, toAyah = 20, status = "strong", reviewCount = 1)
        val c3 = createCard("c3", surah = 67, fromAyah = 21, toAyah = 30, status = "weak", reviewCount = 1)

        val segments = MasteryCalculator.computeSurahSegments(listOf(c1, c2, c3), 30)
        assertEquals(2, segments.size)

        // First segment merges c1 and c2: ayahs 1..20
        assertEquals(1, segments[0].fromAyah)
        assertEquals(20, segments[0].toAyah)
        assertEquals("strong", segments[0].status)

        // Second segment: ayahs 21..30
        assertEquals(21, segments[1].fromAyah)
        assertEquals(30, segments[1].toAyah)
        assertEquals("weak", segments[1].status)
    }

    @Test
    fun `juz grid correctly classifies juz statuses`() {
        val c1 = createCard("c1", juz = 30, status = "strong", reviewCount = 1)
        val c2 = createCard("c2", juz = 29, status = "weak", reviewCount = 1)
        val c3 = createCard("c3", juz = 28, status = "review", reviewCount = 0) // untested

        val grid = MasteryCalculator.computeJuzGrid(listOf(c1, c2, c3))
        assertEquals(30, grid.size)

        val juz30 = grid.first { it.juz == 30 }
        assertEquals("strong", juz30.status)

        val juz29 = grid.first { it.juz == 29 }
        assertEquals("weak", juz29.status)

        val juz28 = grid.first { it.juz == 28 }
        assertEquals("untested", juz28.status)

        val juz1 = grid.first { it.juz == 1 }
        assertNull(juz1.status) // Not memorized
    }
}
