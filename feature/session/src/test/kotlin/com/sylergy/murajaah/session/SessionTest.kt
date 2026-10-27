package com.sylergy.murajaah.session

import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.scheduler.Card
import com.sylergy.murajaah.scheduler.MendResult
import com.sylergy.murajaah.scheduler.MendRules
import com.sylergy.murajaah.session.model.RecitationAttempt
import com.sylergy.murajaah.session.model.SessionCard
import com.sylergy.murajaah.session.model.SessionPhase
import com.sylergy.murajaah.session.model.SessionRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTest {

    @Test
    fun testRecallFloorSeconds() {
        // 1 ayah: expected 8s, share 0.75 = 6s -> clamped to min 5s
        assertEquals(6, SessionRules.recallFloorSeconds(1))

        // 10 ayahs: expected 80s, share 0.75 = 60s
        assertEquals(60, SessionRules.recallFloorSeconds(10))

        // 30 ayahs: expected 240s, share 0.75 = 180s -> clamped to max 90s
        assertEquals(90, SessionRules.recallFloorSeconds(30))
    }

    @Test
    fun testSuggestedGrade() {
        // Clean unhelped recall -> Strong
        assertEquals(
            Rating.Strong,
            SessionRules.suggestedGrade(ayahs = 10, slippedCount = 0, hintsUsed = 0)
        )

        // Helped with 1 hint, no slips -> Review
        assertEquals(
            Rating.Review,
            SessionRules.suggestedGrade(ayahs = 10, slippedCount = 0, hintsUsed = 1)
        )

        // 1 slip in 10 ayahs (allowed = max(2, 2) = 2) -> Review
        assertEquals(
            Rating.Review,
            SessionRules.suggestedGrade(ayahs = 10, slippedCount = 1, hintsUsed = 0)
        )

        // 2 slips in 10 ayahs -> Review
        assertEquals(
            Rating.Review,
            SessionRules.suggestedGrade(ayahs = 10, slippedCount = 2, hintsUsed = 0)
        )

        // 3 slips in 10 ayahs (> allowed 2) -> Weak
        assertEquals(
            Rating.Weak,
            SessionRules.suggestedGrade(ayahs = 10, slippedCount = 3, hintsUsed = 0)
        )
    }

    @Test
    fun testSpotRepairCalculations() {
        // Weak should trigger 5 repetitions
        assertEquals(5, MendRules.fixReads(Rating.Weak, weakStreak = 0))

        // Review should trigger 3 repetitions
        assertEquals(3, MendRules.fixReads(Rating.Review, weakStreak = 0))

        // Stubborn weak streak (3+) should trigger 7 repetitions
        assertEquals(7, MendRules.fixReads(Rating.Weak, weakStreak = 3))

        // Slipped ayahs 3 and 4 in range 1..10 should expand to 2..5
        val repair = MendRules.repairAyahs(1, 10, setOf(3, 4), stubborn = false)
        assertEquals(listOf(2, 3, 4, 5), repair)
    }

    @Test
    fun testMendResultsInSummary() {
        val summary = SessionPhase.Summary(
            completedCount = 3,
            totalAyahs = 30,
            totalSeconds = 120,
            strongCount = 1,
            reviewCount = 1,
            weakCount = 1,
            mendedCount = 2,
            retestHeldCount = 2,
            isFreeSession = false,
            history = emptyList()
        )

        assertEquals(2, summary.mendedCount)
        assertEquals(2, summary.retestHeldCount)
        assertFalse(summary.isFreeSession)
    }
}
