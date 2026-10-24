package com.sylergy.murajaah.achievements

import com.sylergy.murajaah.achievements.evaluator.AchievementEvaluator
import com.sylergy.murajaah.achievements.model.AchievementFacts
import com.sylergy.murajaah.achievements.model.ReviewFacts
import com.sylergy.murajaah.database.entity.CardEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AchievementEvaluatorTest {

    @Test
    fun `zero facts leaves all achievements open`() {
        val facts = AchievementFacts()
        val results = AchievementEvaluator.evaluate(facts, emptyMap(), "2026-10-24")

        assertEquals(18, results.size)
        assertTrue(results.none { it.earned })
    }

    @Test
    fun `first revision earns first_revision achievement`() {
        val facts = AchievementFacts(revisedReviews = 1)
        val results = AchievementEvaluator.evaluate(facts, emptyMap(), "2026-10-24")

        val firstRev = results.first { it.key == "first_revision" }
        assertTrue(firstRev.earned)
        assertEquals(1, firstRev.current)
        assertEquals("2026-10-24", firstRev.earnedAt)

        val rev50 = results.first { it.key == "revised_50" }
        assertFalse(rev50.earned)
        assertEquals(1, rev50.current)
        assertEquals(50, rev50.target)
    }

    @Test
    fun `50 recitations earns revised_50`() {
        val facts = AchievementFacts(revisedReviews = 50)
        val results = AchievementEvaluator.evaluate(facts, emptyMap(), "2026-10-24")

        val rev50 = results.first { it.key == "revised_50" }
        assertTrue(rev50.earned)
        assertEquals(50, rev50.current)
    }

    @Test
    fun `non-lossy retention preserves previously earned achievement even when current facts drop`() {
        // Suppose user previously earned revised_50 on 2026-09-01, but cleared cards
        val storedEarned = mapOf("revised_50" to "2026-09-01", "first_revision" to "2026-08-15")
        val emptyFacts = AchievementFacts(revisedReviews = 0)

        val results = AchievementEvaluator.evaluate(emptyFacts, storedEarned, "2026-10-24")

        val rev50 = results.first { it.key == "revised_50" }
        assertTrue(rev50.earned)
        assertEquals(50, rev50.current)
        assertEquals("2026-09-01", rev50.earnedAt)

        val firstRev = results.first { it.key == "first_revision" }
        assertTrue(firstRev.earned)
        assertEquals("2026-08-15", firstRev.earnedAt)
    }

    @Test
    fun `distinct weeks counts unique calendar weeks`() {
        val dates = listOf("2026-10-01", "2026-10-02", "2026-10-08", "2026-10-15")
        val weeks = AchievementEvaluator.distinctWeeks(dates)
        assertTrue(weeks >= 3)
    }

    @Test
    fun `surahsCovered detects completely covered surah`() {
        // Surah 114 (An-Nas) has 6 ayahs
        val card1 = CardEntity(
            id = "c1",
            surah = 114,
            fromAyah = 1,
            toAyah = 3,
            status = "strong",
            stability = 5.0,
            dueAt = "2026-10-24",
            reviewCount = 1
        )
        val card2 = CardEntity(
            id = "c2",
            surah = 114,
            fromAyah = 4,
            toAyah = 6,
            status = "strong",
            stability = 5.0,
            dueAt = "2026-10-24",
            reviewCount = 1
        )

        val covered = AchievementEvaluator.surahsCovered(listOf(card1, card2))
        assertEquals(1, covered)
    }
}
