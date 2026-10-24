package com.sylergy.murajaah.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memorized_ranges")
data class MemorizedRangeEntity(
    @PrimaryKey
    val id: String,
    val surah: Int,
    val fromAyah: Int,
    val toAyah: Int,
    val surahName: String? = null,
    val surahArabicName: String? = null,
    val ayahsCount: Int = 0,
    val cardsCount: Int = 0
)
