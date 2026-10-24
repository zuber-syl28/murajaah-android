package com.sylergy.murajaah.network.model

import kotlinx.serialization.Serializable

@Serializable
data class OfflineSnapshotDto(
    val today: String,
    val subject: SubjectDto,
    val settings: SettingsDto,
    val plans: PlansResponseDto,
    val cardSize: String? = null,
    val cards: List<CardDto> = emptyList(),
    val ranges: List<RangeDto> = emptyList()
)

@Serializable
data class SubjectDto(
    val id: String,
    val name: String,
    val narration: String = "hafs",
    val timezone: String = "UTC"
)

@Serializable
data class SettingsDto(
    val sessionMode: String? = null,
    val revisionStyle: String? = null,
    val dailyPages: Double? = null,
    val dailyMinutes: Int? = null
)

@Serializable
data class PlansResponseDto(
    val plans: List<PlanDto> = emptyList(),
    val activeId: String? = null
)

@Serializable
data class PlanDto(
    val id: String,
    val name: String,
    val surahs: List<Int> = emptyList()
)

@Serializable
data class CardDto(
    val id: String,
    val surah: Int,
    val surahName: String? = null,
    val surahArabicName: String? = null,
    val fromAyah: Int,
    val toAyah: Int,
    val ayahs: Int = 0,
    val narration: String = "hafs",
    val firstAyahText: String? = null,
    val juz: Int? = null,
    val page: Int? = null,
    val status: String,
    val stability: Double,
    val difficulty: Double? = null,
    val scheduledDays: Int? = null,
    val dueAt: String,
    val dueInDays: Int? = null,
    val lastReviewedAt: String? = null,
    val daysSinceReview: Int? = null,
    val reviewCount: Int = 0,
    val gradedBy: String? = null,
    val consensus: Double? = null
)

@Serializable
data class RangeDto(
    val id: String,
    val surah: Int,
    val fromAyah: Int,
    val toAyah: Int,
    val surahName: String? = null,
    val surahArabicName: String? = null,
    val ayahs: Int? = null,
    val cards: Int? = null,
    val juz: List<Int> = emptyList()
)
