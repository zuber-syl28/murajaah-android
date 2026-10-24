package com.sylergy.murajaah.fsrs

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

const val MAX_STABILITY_DAYS = 60
const val MIN_REVIEW_INTERVAL_DAYS = 2
const val MIN_STRONG_INTERVAL_DAYS = 7
const val HESITANT_CEILING = 0.6
const val REQUEST_RETENTION = 0.9

data class FsrsInput(
    val memory: MemoryState?,
    val rating: Rating,
    val elapsedDays: Double,
    val dueInDays: Int,
    val previousInterval: Int?,
    val growthWeight: Double = 1.0,
    val maxDays: Int = MAX_STABILITY_DAYS
)

data class FsrsOutcome(
    val stability: Double,
    val difficulty: Double,
    val days: Int
)

object MurajaahFsrs {
    private val algorithm = FsrsAlgorithm(
        FsrsParameters(
            requestRetention = REQUEST_RETENTION,
            maximumInterval = MAX_STABILITY_DAYS
        )
    )

    fun schedule(input: FsrsInput): FsrsOutcome {
        val longest = max(MIN_STRONG_INTERVAL_DAYS, min(MAX_STABILITY_DAYS, input.maxDays))

        // Strong given before card was due (revising ahead): does not change memory state
        if (input.memory != null && input.rating == Rating.Strong && input.dueInDays > 0) {
            val days = clamp(input.dueInDays, MIN_STRONG_INTERVAL_DAYS, longest)
            return FsrsOutcome(
                stability = input.memory.stability,
                difficulty = input.memory.difficulty,
                days = days
            )
        }

        val elapsed = if (input.memory != null) max(0.0, input.elapsedDays) else 0.0
        val next = algorithm.nextState(input.memory, elapsed, input.rating.toFsrs())
        val stability = if (input.memory != null && next.stability > input.memory.stability) {
            input.memory.stability + (next.stability - input.memory.stability) * input.growthWeight
        } else {
            next.stability
        }

        val fsrsDays = algorithm.nextInterval(stability)

        val days: Int = when (input.rating) {
            Rating.Weak -> 1
            Rating.Review -> {
                val ceiling = if (input.previousInterval == null) {
                    fsrsDays
                } else {
                    round(input.previousInterval * HESITANT_CEILING).toInt()
                }
                clamp(fsrsDays, MIN_REVIEW_INTERVAL_DAYS, max(MIN_REVIEW_INTERVAL_DAYS, min(ceiling, longest)))
            }
            Rating.Strong -> clamp(fsrsDays, MIN_STRONG_INTERVAL_DAYS, longest)
        }

        return FsrsOutcome(
            stability = roundToFourDecimals(stability),
            difficulty = roundToFourDecimals(next.difficulty),
            days = days
        )
    }

    fun retrievability(stability: Double, elapsedDays: Double): Double {
        if (stability <= 0.0) return 0.5
        return algorithm.forgettingCurve(max(0.0, elapsedDays), stability)
    }

    fun initialDifficulty(rating: Rating): Double {
        return roundToFourDecimals(algorithm.initDifficulty(rating.toFsrs()))
    }

    private fun clamp(v: Int, minV: Int, maxV: Int): Int = max(minV, min(maxV, v))

    private fun roundToFourDecimals(value: Double): Double =
        round(value * 10_000.0) / 10_000.0
}
