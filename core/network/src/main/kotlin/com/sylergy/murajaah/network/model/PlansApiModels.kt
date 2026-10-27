package com.sylergy.murajaah.network.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlanRequestDto(
    val id: String? = null,
    val name: String,
    val surahs: List<Int>
)

@Serializable
data class UpdatePlanRequestDto(
    val name: String,
    val surahs: List<Int>
)
