package com.sylergy.murajaah.network.model

import kotlinx.serialization.Serializable

@Serializable
data class FriendLinkDto(
    val partnershipId: String,
    val status: String, // "pending" | "active"
    val permission: String, // "assess" | "view"
    val role: String = "peer", // "teacher" | "peer"
    val since: String = "",
    val lastActiveAt: String? = null,
    val reviews: Int = 0,
    val lastReviewedAt: String? = null,
    val dueToday: Int = 0,
    val plannedCards: Int = 0,
    val remind: Boolean = false
)

@Serializable
data class FriendDto(
    val userId: String,
    val name: String,
    val reviewsYou: FriendLinkDto? = null,
    val youReview: FriendLinkDto? = null
)

@Serializable
data class InviteDto(
    val token: String,
    val url: String,
    val permission: String = "assess",
    val expiresAt: String
)

@Serializable
data class InvitePreviewDto(
    val token: String,
    val studentName: String,
    val permission: String,
    val expiresAt: String
)

@Serializable
data class InvitePreviewResponseDto(
    val invite: InvitePreviewDto
)

@Serializable
data class FriendsResponseDto(
    val today: String = "",
    val friends: List<FriendDto> = emptyList(),
    val invite: InviteDto? = null
)

@Serializable
data class ExchangedGradeDto(
    val id: String,
    val direction: String, // "given" | "received"
    val card: String,
    val rating: String,
    val recordedAt: String,
    val daysAgo: Int = 0,
    val reviewerRole: String = "peer",
    val inPerson: Boolean = false
)

@Serializable
data class TogetherStatsDto(
    val heardYou30: Int = 0,
    val youHeard30: Int = 0,
    val earned: Int = 0
)

@Serializable
data class FriendDetailResponseDto(
    val today: String = "",
    val friend: FriendDto,
    val recent: List<ExchangedGradeDto> = emptyList(),
    val together: TogetherStatsDto = TogetherStatsDto()
)

@Serializable
data class CreateInviteResponseDto(
    val invite: InviteDto
)

@Serializable
data class AcceptInvitePartnership(
    val id: String = "",
    val studentName: String = "",
    val status: String = "active"
)

@Serializable
data class AcceptInviteResponseDto(
    val partnership: AcceptInvitePartnership
)

@Serializable
data class UpdateRoleRequestDto(
    val role: String // "teacher" | "peer"
)

@Serializable
data class UpdateRemindRequestDto(
    val enabled: Boolean
)
