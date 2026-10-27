package com.sylergy.murajaah.database

import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.scheduler.Card
import com.sylergy.murajaah.scheduler.ReviewerRole

fun CardEntity.toDomain(): Card {
    val domainRating = when (status.lowercase()) {
        "weak" -> Rating.Weak
        "review" -> Rating.Review
        "strong" -> Rating.Strong
        else -> Rating.Review
    }
    val role = when (gradedBy?.lowercase()) {
        "teacher" -> ReviewerRole.Teacher
        "peer" -> ReviewerRole.Peer
        "self" -> ReviewerRole.Self
        else -> null
    }
    return Card(
        id = id,
        surah = surah,
        fromAyah = fromAyah,
        toAyah = toAyah,
        status = domainRating,
        stability = stability,
        difficulty = difficulty,
        scheduledDays = scheduledDays,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt,
        reviewCount = reviewCount,
        gradedBy = role,
        page = page,
        juz = juz
    )
}

fun Card.toEntity(
    juz: Int? = this.juz,
    page: Int? = this.page,
    consensus: Double? = null,
    firstAyahText: String? = null
): CardEntity {
    return CardEntity(
        id = id,
        surah = surah,
        fromAyah = fromAyah,
        toAyah = toAyah,
        juz = juz,
        page = page,
        status = when (status) {
            Rating.Weak -> "weak"
            Rating.Review -> "review"
            Rating.Strong -> "strong"
        },
        stability = stability,
        difficulty = difficulty,
        scheduledDays = scheduledDays,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt,
        reviewCount = reviewCount,
        gradedBy = when (gradedBy) {
            ReviewerRole.Teacher -> "teacher"
            ReviewerRole.Peer -> "peer"
            ReviewerRole.Self -> "self"
            null -> null
        },
        consensus = consensus,
        firstAyahText = firstAyahText
    )
}
