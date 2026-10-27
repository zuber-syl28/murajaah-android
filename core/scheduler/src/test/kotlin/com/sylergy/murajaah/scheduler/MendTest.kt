package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MendTest {

    @Test
    fun fixReads_requiresMoreReadsForWeakerRecitation() {
        assertEquals(3, MendRules.fixReads(Rating.Review))
        assertEquals(5, MendRules.fixReads(Rating.Weak))
    }

    @Test
    fun fixReads_demandsSevenReadsForStubbornStreak() {
        assertEquals(7, MendRules.fixReads(Rating.Weak, weakStreak = 3))
        assertEquals(7, MendRules.fixReads(Rating.Review, weakStreak = 4))
        assertEquals(5, MendRules.fixReads(Rating.Weak, weakStreak = 2))
    }

    @Test
    fun fixReads_fallsBackToLightestDoseWhenRatingNull() {
        assertEquals(3, MendRules.fixReads(null, weakStreak = 0))
    }

    @Test
    fun repairAyahs_expandsSlippedAyahsByOneEitherSide() {
        val repair = MendRules.repairAyahs(
            fromAyah = 1,
            toAyah = 10,
            slippedAyahs = setOf(4, 5),
            stubborn = false
        )
        // 4-1 = 3, 5+1 = 6 -> [3, 4, 5, 6]
        assertEquals(listOf(3, 4, 5, 6), repair)
    }

    @Test
    fun repairAyahs_clampsToCardBoundaries() {
        val repair = MendRules.repairAyahs(
            fromAyah = 1,
            toAyah = 10,
            slippedAyahs = setOf(1, 10),
            stubborn = false
        )
        assertEquals((1..10).toList(), repair)
    }

    @Test
    fun repairAyahs_takesWholePassageWhenNothingMarkedOrStubborn() {
        val repairEmpty = MendRules.repairAyahs(
            fromAyah = 1,
            toAyah = 10,
            slippedAyahs = emptySet(),
            stubborn = false
        )
        assertEquals((1..10).toList(), repairEmpty)

        val repairStubborn = MendRules.repairAyahs(
            fromAyah = 1,
            toAyah = 10,
            slippedAyahs = setOf(5),
            stubborn = true
        )
        assertEquals((1..10).toList(), repairStubborn)
    }

    @Test
    fun shouldMend_onlyForWeakAndReview() {
        assertTrue(MendRules.shouldMend(Rating.Weak))
        assertTrue(MendRules.shouldMend(Rating.Review))
        assertFalse(MendRules.shouldMend(Rating.Strong))
    }
}
