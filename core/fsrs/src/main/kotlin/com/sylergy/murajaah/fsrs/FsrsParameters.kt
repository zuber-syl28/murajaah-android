package com.sylergy.murajaah.fsrs

data class FsrsParameters(
    val requestRetention: Double = 0.9,
    val maximumInterval: Int = 60,
    val w: DoubleArray = DEFAULT_WEIGHTS
) {
    companion object {
        val DEFAULT_WEIGHTS: DoubleArray = doubleArrayOf(
            0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001,
            1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014,
            1.8729, 0.5425, 0.0912, 0.0658, 0.1542
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FsrsParameters
        if (requestRetention != other.requestRetention) return false
        if (maximumInterval != other.maximumInterval) return false
        if (!w.contentEquals(other.w)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = requestRetention.hashCode()
        result = 31 * result + maximumInterval
        result = 31 * result + w.contentHashCode()
        return result
    }
}
