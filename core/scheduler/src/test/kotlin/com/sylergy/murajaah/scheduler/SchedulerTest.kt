package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchedulerTest {

    private val TODAY = "2026-10-24"

    private fun sampleCard(
        id: String = "c1",
        surah: Int = 2,
        fromAyah: Int = 1,
        toAyah: Int = 5,
        status: Rating = Rating.Strong,
        stability: Double = 10.0,
        difficulty: Double? = 5.0,
        scheduledDays: Int? = 10,
        dueAt: String = TODAY,
        lastReviewedAt: String? = "2026-10-14",
        reviewCount: Int = 1
    ) = Card(
        id = id,
        surah = surah,
        fromAyah = fromAyah,
        toAyah = toAyah,
        status = status,
        stability = stability,
        difficulty = difficulty,
        scheduledDays = scheduledDays,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt,
        reviewCount = reviewCount
    )

    @Test
    fun testRevisionCycleDays() {
        val juzCard = { n: Int ->
            listOf(sampleCard(fromAyah = 1, toAyah = (n * 6236.0 / 30.0).toInt()))
        }
        assertEquals(7, MurajaahScheduler.revisionCycleDays(emptyList()))
        assertEquals(7, MurajaahScheduler.revisionCycleDays(juzCard(1)))
        assertEquals(7, MurajaahScheduler.revisionCycleDays(juzCard(5)))
        assertEquals(14, MurajaahScheduler.revisionCycleDays(juzCard(10)))
        assertEquals(21, MurajaahScheduler.revisionCycleDays(juzCard(20)))
        assertEquals(30, MurajaahScheduler.revisionCycleDays(juzCard(30)))
    }

    @Test
    fun testPriorityScoring() {
        val dueCard = sampleCard(dueAt = TODAY, status = Rating.Weak)
        val notDueCard = sampleCard(dueAt = "2026-10-26", status = Rating.Weak)

        val pDue = MurajaahScheduler.priority(dueCard, TODAY)
        val pNotDue = MurajaahScheduler.priority(notDueCard, TODAY)

        // Due cards receive +1000 due bonus
        assertTrue(pDue > 1000.0)
        assertTrue(pNotDue < 1000.0)
    }

    @Test
    fun testSessionCap() {
        assertEquals(3, MurajaahScheduler.sessionCap(SessionMode.Focus, 0))
        assertEquals(8, MurajaahScheduler.sessionCap(SessionMode.Normal, 0))
        assertEquals(8, MurajaahScheduler.sessionCap(SessionMode.Normal, 1))
        assertEquals(6, MurajaahScheduler.sessionCap(SessionMode.Normal, 2))
        assertEquals(5, MurajaahScheduler.sessionCap(SessionMode.Normal, 5))
        assertEquals(3, MurajaahScheduler.sessionCap(SessionMode.Normal, 10))
    }

    @Test
    fun testBuildQueueBalancing() {
        val weakCards = (1..10).map {
            sampleCard(id = "w$it", status = Rating.Weak, reviewCount = 2, dueAt = TODAY)
        }
        val strongCards = (1..10).map {
            sampleCard(id = "s$it", status = Rating.Strong, reviewCount = 2, dueAt = TODAY)
        }

        val queue = MurajaahScheduler.buildQueue(weakCards + strongCards, TODAY, cap = 8)
        assertEquals(8, queue.size)

        val weakInQueue = queue.count { MurajaahScheduler.isStrengthening(it) }
        val strongInQueue = queue.count { !MurajaahScheduler.isStrengthening(it) }

        // Must balance between 1/3 and 2/3 ratio
        assertTrue(weakInQueue in 3..5)
        assertTrue(strongInQueue in 3..5)
    }

    @Test
    fun testTeacherHoldSafetyBrake() {
        val card = sampleCard(stability = 30.0, status = Rating.Review, reviewCount = 3)
        val teacherHistory = listOf(
            GradeRecord(rating = Rating.Weak, role = ReviewerRole.Teacher, recordedAt = "2026-10-20")
        )

        // Peer or self gives Strong, but teacher lapse is within 30 days -> Triggers TeacherHold
        val result = Reviewers.applyWeightedRating(
            card = card,
            rating = Rating.Strong,
            role = ReviewerRole.Self,
            history = teacherHistory,
            on = TODAY
        )

        assertEquals(GradeRule.TeacherHold, result.rule)
        assertEquals(Rating.Review, result.status)
        assertEquals(30.0, result.stability) // Stability kept unchanged
        assertEquals(18, result.days) // max(2, round(30 * 0.6)) = 18 days
    }

    @Test
    fun testPeerWeightedGrowth() {
        val card = sampleCard(stability = 10.0, status = Rating.Strong, reviewCount = 2)
        val teacherHistory = listOf(
            GradeRecord(rating = Rating.Strong, role = ReviewerRole.Teacher, recordedAt = "2026-10-10")
        )

        val peerResult = Reviewers.applyWeightedRating(
            card = card,
            rating = Rating.Strong,
            role = ReviewerRole.Peer,
            history = teacherHistory,
            on = TODAY
        )

        assertEquals(GradeRule.PeerWeighted, peerResult.rule)
        assertEquals(Rating.Strong, peerResult.status)
        assertTrue(peerResult.stability > 10.0)
    }
}
