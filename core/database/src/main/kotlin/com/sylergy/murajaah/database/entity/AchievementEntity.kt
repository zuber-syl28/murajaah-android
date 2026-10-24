package com.sylergy.murajaah.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val key: String,
    val role: String, // "self", "student", "reviewer"
    val earnedAt: String, // ISO date "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)
