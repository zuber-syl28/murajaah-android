package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsensusTest {

    private val on = "2026-09-19"

    private fun grade(
        grader: String,
        rating: Rating,
        role: ReviewerRole,
        recordedAt: String = on,
        graderName: String? = null
    ) = GraderGrade(
        grader = grader,
        graderName = graderName ?: grader.replaceFirstChar { it.uppercase() },
        rating = rating,
        role = role,
        recordedAt = recordedAt
    )

    @Test
    fun isNothingForCardNobodyHasGraded() {
        assertNull(ConsensusRules.calculate(emptyList(), on))
    }

    @Test
    fun isTheGradeItselfWhenOneListenerHasGraded() {
        val result = ConsensusRules.calculate(listOf(grade("omar", Rating.Review, ReviewerRole.Peer)), on)
        assertNotNull(result)
        assertEquals(0.58, result!!.score, 0.001)
        assertEquals(Rating.Review, result.band)
        assertTrue(result.listened)
    }

    @Test
    fun weighsTeacherAbovePeerAndPeerAboveSelfCheck() {
        val history = listOf(
            grade("you", Rating.Strong, ReviewerRole.Self),
            grade("omar", Rating.Strong, ReviewerRole.Peer),
            grade("sheikh", Rating.Review, ReviewerRole.Teacher)
        )
        val result = ConsensusRules.calculate(history, on)
        assertNotNull(result)
        val expected = (1.0 * 0.3 + 1.0 * 0.6 + 0.58 * 1.0) / (0.3 + 0.6 + 1.0)
        assertEquals(expected, result!!.score, 0.0001)
        assertEquals(Rating.Review, result.band)

        val weights = result.graders.associate { it.grader to it.weight }
        assertEquals(0.3, weights["you"]!!, 0.0001)
        assertEquals(0.6, weights["omar"]!!, 0.0001)
        assertEquals(1.0, weights["sheikh"]!!, 0.0001)
    }

    @Test
    fun countsEachPersonOnceTheirLatestGrade() {
        val history = listOf(
            grade("omar", Rating.Strong, ReviewerRole.Peer, "2026-09-18"),
            grade("omar", Rating.Weak, ReviewerRole.Peer, "2026-09-01"),
            grade("omar", Rating.Weak, ReviewerRole.Peer, "2026-08-20"),
            grade("sheikh", Rating.Weak, ReviewerRole.Teacher, "2026-09-18")
        )
        val result = ConsensusRules.calculate(history, on)
        assertNotNull(result)
        assertEquals(2, result!!.graders.size)
        val omar = result.graders.first { it.grader == "omar" }
        assertEquals(Rating.Strong, omar.rating)
    }

    @Test
    fun letsOldGradeFadeHalfWeightEveryThirtyDays() {
        val history = listOf(
            grade("omar", Rating.Strong, ReviewerRole.Peer, on),
            grade("sheikh", Rating.Weak, ReviewerRole.Teacher, "2026-08-20") // 30 days prior
        )
        val result = ConsensusRules.calculate(history, on)
        assertNotNull(result)
        val sheikh = result!!.graders.first { it.grader == "sheikh" }
        assertEquals(0.5, sheikh.weight, 0.001)
        val expected = (1.0 * 0.6 + 0.22 * 0.5) / 1.1
        assertEquals(expected, result.score, 0.001)
    }

    @Test
    fun neverMarksCardSolidOnSelfChecksAlone() {
        val result = ConsensusRules.calculate(listOf(grade("you", Rating.Strong, ReviewerRole.Self)), on)
        assertNotNull(result)
        assertTrue(result!!.score <= ConsensusRules.SELF_CHECK_CEILING)
        assertFalse(result.listened)
    }
}
