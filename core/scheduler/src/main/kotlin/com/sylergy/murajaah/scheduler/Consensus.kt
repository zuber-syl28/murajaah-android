package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class GraderGrade(
    val grader: String,
    val graderName: String? = null,
    val rating: Rating,
    val role: ReviewerRole,
    val recordedAt: String, // "YYYY-MM-DD"
    val inPerson: Boolean = false
)

data class EvaluatedGrader(
    val grader: String,
    val graderName: String? = null,
    val rating: Rating,
    val role: ReviewerRole,
    val recordedAt: String,
    val inPerson: Boolean = false,
    val weight: Double,
    val weightPercent: Double
)

data class WeakSpot(
    val tag: String,
    val count: Int,
    val by: List<String>
)

data class Consensus(
    val score: Double,
    val band: Rating,
    val listened: Boolean,
    val graders: List<EvaluatedGrader>
)

object ConsensusRules {
    const val GRADE_HALF_LIFE_DAYS = 30.0
    const val SELF_CHECK_CEILING = 0.84

    val ROLE_WEIGHT = mapOf(
        ReviewerRole.Teacher to 1.0,
        ReviewerRole.Peer to 0.6,
        ReviewerRole.Self to 0.3
    )

    val GRADE_VALUE = mapOf(
        Rating.Strong to 1.0,
        Rating.Review to 0.58,
        Rating.Weak to 0.22
    )

    fun calculate(history: List<GraderGrade>, on: String): Consensus? {
        if (history.isEmpty()) return null

        val latestByGrader = mutableMapOf<String, GraderGrade>()
        for (grade in history) {
            if (!latestByGrader.containsKey(grade.grader)) {
                latestByGrader[grade.grader] = grade
            }
        }
        if (latestByGrader.isEmpty()) return null

        val weightedGraders = latestByGrader.values.map { grade ->
            val weight = weightOf(grade, on)
            grade to weight
        }

        val totalWeight = weightedGraders.sumOf { it.second }.coerceAtLeast(0.0001)

        val evaluated = weightedGraders.map { (grade, weight) ->
            EvaluatedGrader(
                grader = grade.grader,
                graderName = grade.graderName,
                rating = grade.rating,
                role = grade.role,
                recordedAt = grade.recordedAt,
                inPerson = grade.inPerson,
                weight = weight,
                weightPercent = (weight / totalWeight) * 100.0
            )
        }

        val mean = weightedGraders.sumOf { (grade, weight) ->
            (GRADE_VALUE[grade.rating] ?: 0.58) * weight
        } / totalWeight

        val listened = evaluated.any { it.role != ReviewerRole.Self }
        val score = if (listened) mean else min(mean, SELF_CHECK_CEILING)

        return Consensus(
            score = score,
            band = bandOf(score),
            listened = listened,
            graders = evaluated
        )
    }

    private fun weightOf(grade: GraderGrade, on: String): Double {
        val datePart = if (grade.recordedAt.length >= 10) grade.recordedAt.substring(0, 10) else grade.recordedAt
        val age = max(0, DateUtils.daysBetween(datePart, on))
        val baseWeight = ROLE_WEIGHT[grade.role] ?: 0.3
        return baseWeight * (0.5).pow(age / GRADE_HALF_LIFE_DAYS)
    }

    fun bandOf(score: Double): Rating {
        val strongCutoff = ((GRADE_VALUE[Rating.Strong] ?: 1.0) + (GRADE_VALUE[Rating.Review] ?: 0.58)) / 2.0
        val reviewCutoff = ((GRADE_VALUE[Rating.Review] ?: 0.58) + (GRADE_VALUE[Rating.Weak] ?: 0.22)) / 2.0
        return when {
            score >= strongCutoff -> Rating.Strong
            score >= reviewCutoff -> Rating.Review
            else -> Rating.Weak
        }
    }
}
