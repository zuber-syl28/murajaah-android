package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating

enum class ReviewerRole {
    Teacher,
    Peer,
    Self
}

data class Card(
    val id: String,
    val surah: Int,
    val fromAyah: Int,
    val toAyah: Int,
    val status: Rating,
    val stability: Double,
    val difficulty: Double?,
    val scheduledDays: Int?,
    val dueAt: String, // "YYYY-MM-DD"
    val lastReviewedAt: String?, // "YYYY-MM-DD"
    val reviewCount: Int,
    val gradedBy: ReviewerRole? = null
) {
    val ayahCount: Int get() = toAyah - fromAyah + 1
}

data class GradeRecord(
    val rating: Rating,
    val role: ReviewerRole,
    val recordedAt: String
)
