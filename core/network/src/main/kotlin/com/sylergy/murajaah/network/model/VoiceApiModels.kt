package com.sylergy.murajaah.network.model

import kotlinx.serialization.Serializable

@Serializable
data class IceServerDto(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null
)

@Serializable
data class IceServersResponseDto(
    val iceServers: List<IceServerDto>
)

@Serializable
data class VoiceCallDto(
    val id: String,
    val reciterId: String,
    val listenerId: String,
    val startedBy: String,
    val createdAt: String,
    val answeredAt: String? = null,
    val endedAt: String? = null
)

@Serializable
data class CallResponseDto(
    val call: VoiceCallDto
)

@Serializable
data class CallDetailResponseDto(
    val call: VoiceCallDto,
    val offer: String? = null,
    val answer: String? = null
)

@Serializable
data class WaitingCallsResponseDto(
    val calls: List<VoiceCallDto>
)

@Serializable
data class StartCallRequestDto(
    val reciterId: String? = null,
    val listenerId: String? = null,
    val offer: String
)

@Serializable
data class AnswerCallRequestDto(
    val answer: String
)
