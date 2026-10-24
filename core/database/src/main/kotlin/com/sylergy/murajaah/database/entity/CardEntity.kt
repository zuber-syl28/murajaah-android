package com.sylergy.murajaah.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    indices = [
        Index(value = ["surah", "fromAyah", "toAyah"]),
        Index(value = ["dueAt"]),
        Index(value = ["status"])
    ]
)
data class CardEntity(
    @PrimaryKey
    val id: String,
    val surah: Int,
    val fromAyah: Int,
    val toAyah: Int,
    val juz: Int? = null,
    val page: Int? = null,
    val status: String,
    val stability: Double,
    val difficulty: Double? = null,
    val scheduledDays: Int? = null,
    val dueAt: String,
    val lastReviewedAt: String? = null,
    val reviewCount: Int = 0,
    val gradedBy: String? = null,
    val consensus: Double? = null,
    val firstAyahText: String? = null
)
