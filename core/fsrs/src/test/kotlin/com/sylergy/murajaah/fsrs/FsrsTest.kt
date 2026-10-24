package com.sylergy.murajaah.fsrs

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FsrsTest {

    @Test
    fun mapsWeakReviewAndStrongOntoAgainHardAndGood() {
        assertEquals(FsrsRating.Again, Rating.Weak.toFsrs())
        assertEquals(FsrsRating.Hard, Rating.Review.toFsrs())
        assertEquals(FsrsRating.Good, Rating.Strong.toFsrs())
    }

    @Test
    fun givesAFirstStrongAtLeastAWeekAndAFirstWeakTomorrow() {
        val strongOutcome = MurajaahFsrs.schedule(
            FsrsInput(
                memory = null,
                rating = Rating.Strong,
                elapsedDays = 0.0,
                dueInDays = 0,
                previousInterval = null
            )
        )
        assertEquals(7, strongOutcome.days)

        val weakOutcome = MurajaahFsrs.schedule(
            FsrsInput(
                memory = null,
                rating = Rating.Weak,
                elapsedDays = 0.0,
                dueInDays = 0,
                previousInterval = null
            )
        )
        assertEquals(1, weakOutcome.days)
    }

    @Test
    fun growsAPassageThatHeldUpOverALongGapMoreThanOneHeardYesterday() {
        val memory = MemoryState(stability = 10.0, difficulty = 5.0)
        val afterGap = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 12.0,
                dueInDays = 0,
                previousInterval = 10
            )
        )
        val soon = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 1.0,
                dueInDays = 0,
                previousInterval = 10
            )
        )
        assertTrue(afterGap.stability > soon.stability)
        assertTrue(afterGap.days > 10)
    }

    @Test
    fun neverSpacesACardPastSixtyDays() {
        val memory = MemoryState(stability = 50.0, difficulty = 2.0)
        val outcome = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 50.0,
                dueInDays = 0,
                previousInterval = 50
            )
        )
        assertEquals(60, outcome.days)
    }

    @Test
    fun sendsALapseBackTomorrowRaisingDifficultyAndCuttingStability() {
        val memory = MemoryState(stability = 40.0, difficulty = 5.0)
        val lapse = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Weak,
                elapsedDays = 40.0,
                dueInDays = 0,
                previousInterval = 40
            )
        )
        assertEquals(1, lapse.days)
        assertTrue(lapse.stability < 40.0)
        assertTrue(lapse.difficulty > 5.0)
    }

    @Test
    fun bringsANeedsReviewBackSoonerThanTheCardWasLastSpaced() {
        val memory = MemoryState(stability = 10.0, difficulty = 5.0)
        val hesitant = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Review,
                elapsedDays = 10.0,
                dueInDays = 0,
                previousInterval = 10
            )
        )
        assertTrue(hesitant.stability > 10.0)
        assertEquals(6, hesitant.days)
        assertTrue(hesitant.days >= 2)
    }

    @Test
    fun keepsAStrongGivenAheadFromChangingTheMemoryState() {
        val memory = MemoryState(stability = 40.0, difficulty = 5.0)
        val aheadOutcome = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 5.0,
                dueInDays = 36,
                previousInterval = 40
            )
        )
        assertEquals(40.0, aheadOutcome.stability)
        assertEquals(5.0, aheadOutcome.difficulty)
        assertEquals(36, aheadOutcome.days)

        val dueTomorrowAhead = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 5.0,
                dueInDays = 1,
                previousInterval = 40
            )
        )
        assertEquals(7, dueTomorrowAhead.days)
    }

    @Test
    fun neverSpacesAStrongPastTheCycleWhileFsrsKeepsTheMemoryItEarned() {
        val memory = MemoryState(stability = 40.0, difficulty = 4.0)
        val free = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 40.0,
                dueInDays = 0,
                previousInterval = 40
            )
        )
        val capped = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Strong,
                elapsedDays = 40.0,
                dueInDays = 0,
                previousInterval = 40,
                maxDays = 14
            )
        )
        assertTrue(free.days > 14)
        assertEquals(14, capped.days)
        assertEquals(free.stability, capped.stability)
    }

    @Test
    fun keepsWeakTomorrowAndNeedsReviewWithinDaysWhateverTheCycle() {
        val memory = MemoryState(stability = 10.0, difficulty = 5.0)
        val weak = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Weak,
                elapsedDays = 10.0,
                dueInDays = 0,
                previousInterval = 10,
                maxDays = 7
            )
        )
        assertEquals(1, weak.days)

        val review = MurajaahFsrs.schedule(
            FsrsInput(
                memory = memory,
                rating = Rating.Review,
                elapsedDays = 10.0,
                dueInDays = 0,
                previousInterval = 10,
                maxDays = 7
            )
        ).days
        assertTrue(review in 2..7)
    }
}
