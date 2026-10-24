package com.sylergy.murajaah.fsrs

enum class FsrsRating(val value: Int) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4);

    companion object {
        fun fromValue(value: Int): FsrsRating = entries.first { it.value == value }
    }
}

/**
 * Murajaah's 3-word rating vocabulary used by reciters and examiners.
 */
enum class Rating {
    Weak,
    Review,
    Strong;

    fun toFsrs(): FsrsRating = when (this) {
        Weak -> FsrsRating.Again
        Review -> FsrsRating.Hard
        Strong -> FsrsRating.Good
    }
}

data class MemoryState(
    val stability: Double,
    val difficulty: Double
)
