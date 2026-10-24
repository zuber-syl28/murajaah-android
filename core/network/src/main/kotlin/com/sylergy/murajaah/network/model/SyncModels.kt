package com.sylergy.murajaah.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SpanDto(
    val surah: Int,
    val fromAyah: Int,
    val toAyah: Int
)

@Serializable
data class GradeItemDto(
    val clientId: String,
    val cardId: String,
    val rating: String,
    val recordedAt: String,
    val recallSeconds: Int? = null,
    val hintsCount: Int? = null,
    val slippedAyahs: List<Int>? = null,
    val tags: List<String>? = null,
    val note: String? = null,
    val span: SpanDto? = null
)

@Serializable
data class GradeSyncRequestDto(
    val grades: List<GradeItemDto>
)

@Serializable
data class GradeSyncResponseDto(
    val applied: Int,
    val skipped: Int,
    val today: String,
    val cards: List<CardDto> = emptyList()
)

@Serializable
data class UpdateRangesRequestDto(
    val ranges: List<RangeDto>
)

@Serializable
data class UpdateRangesResponseDto(
    val ranges: List<RangeDto> = emptyList()
)
