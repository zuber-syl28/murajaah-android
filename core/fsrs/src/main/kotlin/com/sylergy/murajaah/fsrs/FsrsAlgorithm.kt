package com.sylergy.murajaah.fsrs

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class FsrsAlgorithm(
    val param: FsrsParameters = FsrsParameters()
) {
    private val decay: Double = -param.w[20]
    private val factor: Double = roundToDigits(exp(decay.pow(-1.0) * ln(0.9)) - 1.0, 8)
    val intervalModifier: Double = calculateIntervalModifier(param.requestRetention)

    fun calculateIntervalModifier(requestRetention: Double): Double {
        require(requestRetention in 0.0..1.0) { "Requested retention rate should be in (0, 1]" }
        return roundToDigits((requestRetention.pow(1.0 / decay) - 1.0) / factor, 8)
    }

    fun forgettingCurve(elapsed: Double, stability: Double): Double {
        val s = max(0.001, stability)
        val t = max(0.0, elapsed)
        return roundToDigits((1.0 + factor * t / s).pow(decay), 8)
    }

    fun initStability(rating: FsrsRating): Double {
        return max(param.w[rating.value - 1], 0.1)
    }

    fun initDifficulty(rating: FsrsRating): Double {
        val w = param.w
        val r = w[4] - exp((rating.value - 1) * w[5]) + 1.0
        return roundToDigits(r, 8)
    }

    private fun linearDamping(delta: Double, difficulty: Double): Double {
        return roundToDigits(delta * (10.0 - difficulty) / 9.0, 8)
    }

    private fun meanReversion(initEasy: Double, current: Double): Double {
        val w = param.w
        return roundToDigits(w[7] * initEasy + (1.0 - w[7]) * current, 8)
    }

    fun nextDifficulty(difficulty: Double, rating: FsrsRating): Double {
        val w = param.w
        val delta = -w[6] * (rating.value - 3)
        val l = difficulty + linearDamping(delta, difficulty)
        val initEasy = initDifficulty(FsrsRating.Easy)
        return clamp(meanReversion(initEasy, l), 1.0, 10.0)
    }

    fun nextRecallStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double,
        rating: FsrsRating
    ): Double {
        val w = param.w
        val hardFactor = if (rating == FsrsRating.Hard) w[15] else 1.0
        val easyFactor = if (rating == FsrsRating.Easy) w[16] else 1.0
        val nextS = stability * (1.0 + exp(w[8]) * (11.0 - difficulty) *
                stability.pow(-w[9]) *
                (exp((1.0 - retrievability) * w[10]) - 1.0) *
                hardFactor * easyFactor)
        return roundToDigits(clamp(nextS, 0.001, 36500.0), 8)
    }

    fun nextForgetStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double
    ): Double {
        val w = param.w
        val nextS = w[11] * difficulty.pow(-w[12]) *
                ((stability + 1.0).pow(w[13]) - 1.0) *
                exp((1.0 - retrievability) * w[14])
        return roundToDigits(clamp(nextS, 0.001, 36500.0), 8)
    }

    fun nextState(
        memory: MemoryState?,
        elapsed: Double,
        rating: FsrsRating
    ): MemoryState {
        if (memory == null || (memory.stability == 0.0 && memory.difficulty == 0.0)) {
            return MemoryState(
                stability = initStability(rating),
                difficulty = clamp(initDifficulty(rating), 1.0, 10.0)
            )
        }
        val r = forgettingCurve(elapsed, memory.stability)
        val nextS = if (rating == FsrsRating.Again) {
            nextForgetStability(memory.difficulty, memory.stability, r)
        } else {
            nextRecallStability(memory.difficulty, memory.stability, r, rating)
        }
        val nextD = nextDifficulty(memory.difficulty, rating)
        return MemoryState(
            stability = nextS,
            difficulty = nextD
        )
    }

    fun nextInterval(stability: Double): Int {
        val ivl = round(stability * intervalModifier).toInt()
        return clamp(ivl, 1, param.maximumInterval)
    }

    companion object {
        fun clamp(v: Double, minV: Double, maxV: Double): Double = max(minV, min(maxV, v))
        fun clamp(v: Int, minV: Int, maxV: Int): Int = max(minV, min(maxV, v))
        fun roundToDigits(value: Double, digits: Int): Double {
            val factor = 10.0.pow(digits)
            return round(value * factor) / factor
        }
    }
}
