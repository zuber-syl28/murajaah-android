package com.sylergy.murajaah.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SkipItemRequestDto(
    val cardId: String
)

@Serializable
data class MendItemRequestDto(
    val cardId: String,
    val result: String // "held", "slipped", "skipped"
)
