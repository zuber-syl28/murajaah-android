package com.sylergy.murajaah.session

import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.session.model.SessionRules
import org.junit.Assert.assertEquals
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
}
