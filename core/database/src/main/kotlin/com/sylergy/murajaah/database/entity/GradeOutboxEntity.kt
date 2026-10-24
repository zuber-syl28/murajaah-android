package com.sylergy.murajaah.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grade_outbox",
    indices = [
        Index(value = ["synced"]),
        Index(value = ["recordedAt"])
    ]
)
data class GradeOutboxEntity(
    @PrimaryKey
    val clientId: String,
    val cardId: String,
    val rating: String,
    val recordedAt: String,
    val recallSeconds: Int? = null,
    val hintsCount: Int? = null,
    val slippedAyahs: List<Int> = emptyList(),
    val tags: List<String> = emptyList(),
    val note: String? = null,
    val surah: Int? = null,
    val fromAyah: Int? = null,
    val toAyah: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
