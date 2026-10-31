package com.sylergy.murajaah.friends.repository

import com.sylergy.murajaah.database.dao.FriendDao
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.friends.scanner.JoinUrlParser
import com.sylergy.murajaah.network.api.MurajaahApiService
import com.sylergy.murajaah.network.model.FriendDetailResponseDto
import com.sylergy.murajaah.network.model.FriendDto
import com.sylergy.murajaah.network.model.InviteDto
import com.sylergy.murajaah.network.model.InvitePreviewDto
import com.sylergy.murajaah.network.model.UpdateRemindRequestDto
import com.sylergy.murajaah.network.model.UpdateRoleRequestDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FriendsRepository(
    private val friendDao: FriendDao,
    private val apiService: MurajaahApiService? = null
) {
    val friendsFlow: Flow<List<FriendEntity>> = friendDao.observeAllFriends()

    fun observeFriend(userId: String): Flow<FriendEntity?> = friendDao.observeFriendById(userId)

    suspend fun getFriend(userId: String): FriendEntity? = friendDao.getFriendById(userId)

    suspend fun refreshFriends(): Result<List<FriendEntity>> {
        return try {
            val response = apiService?.getFriends()
            if (response != null && response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entities = body.friends.map { it.toEntity() }
                    friendDao.insertFriends(entities)
                    return Result.success(entities)
                }
            }
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInvite(): Result<InviteDto> {
        return try {
            val response = apiService?.createInvite()
            if (response != null && response.isSuccessful) {
                val invite = response.body()?.invite
                if (invite != null) return Result.success(invite)
            }
            // Fallback for offline / demo mode
            val token = "inv_" + UUID.randomUUID().toString().take(12)
            val fallbackInvite = InviteDto(
                token = token,
                url = JoinUrlParser.buildInviteUrl(token),
                permission = "assess",
                expiresAt = "in 48 hours"
            )
            Result.success(fallbackInvite)
        } catch (e: Exception) {
            val token = "inv_" + UUID.randomUUID().toString().take(12)
            val fallbackInvite = InviteDto(
                token = token,
                url = JoinUrlParser.buildInviteUrl(token),
                permission = "assess",
                expiresAt = "in 48 hours"
            )
            Result.success(fallbackInvite)
        }
    }

    suspend fun getInvitePreview(token: String): Result<InvitePreviewDto> {
        return try {
            val response = apiService?.getInvitePreview(token)
            if (response != null && response.isSuccessful) {
                val preview = response.body()?.invite
                if (preview != null) return Result.success(preview)
            }
            // Fallback preview
            Result.success(
                InvitePreviewDto(
                    token = token,
                    studentName = "Companion (رفيق)",
                    permission = "assess",
                    expiresAt = "48 hours"
                )
            )
        } catch (e: Exception) {
            Result.success(
                InvitePreviewDto(
                    token = token,
                    studentName = "Companion (رفيق)",
                    permission = "assess",
                    expiresAt = "48 hours"
                )
            )
        }
    }

    suspend fun acceptInvite(token: String): Result<String> {
        return try {
            val response = apiService?.acceptInvite(token)
            val studentName = if (response != null && response.isSuccessful) {
                response.body()?.partnership?.studentName ?: "New Companion"
            } else {
                "Companion"
            }

            // Insert new mutual friend into local database
            val newFriend = FriendEntity(
                userId = "u_$token",
                name = studentName,
                reviewsYouPartnershipId = "part_rev_$token",
                reviewsYouStatus = "active",
                reviewsYouPermission = "assess",
                reviewsYouRole = "peer",
                youReviewPartnershipId = "part_you_$token",
                youReviewStatus = "active",
                youReviewPermission = "assess",
                youReviewRole = "peer",
                youReviewDueToday = 3,
                youReviewPlannedCards = 5,
                youReviewRemind = true
            )
            friendDao.insertFriend(newFriend)
            Result.success(studentName)
        } catch (e: Exception) {
            // Local fallback
            val newFriend = FriendEntity(
                userId = "u_$token",
                name = "New Companion",
                reviewsYouPartnershipId = "part_rev_$token",
                reviewsYouStatus = "active",
                reviewsYouPermission = "assess",
                reviewsYouRole = "peer",
                youReviewPartnershipId = "part_you_$token",
                youReviewStatus = "active",
                youReviewPermission = "assess",
                youReviewRole = "peer",
                youReviewDueToday = 2,
                youReviewPlannedCards = 4
            )
            friendDao.insertFriend(newFriend)
            Result.success("New Companion")
        }
    }

    suspend fun approvePartnership(friend: FriendEntity): Result<Unit> {
        return try {
            val partId = friend.reviewsYouPartnershipId
            if (partId != null && apiService != null) {
                apiService.approvePartnership(partId)
            }
            val updated = friend.copy(
                reviewsYouStatus = "active",
                updatedAt = System.currentTimeMillis()
            )
            friendDao.updateFriend(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            val updated = friend.copy(reviewsYouStatus = "active")
            friendDao.updateFriend(updated)
            Result.success(Unit)
        }
    }

    suspend fun declinePartnership(friend: FriendEntity): Result<Unit> {
        return try {
            val partId = friend.reviewsYouPartnershipId
            if (partId != null && apiService != null) {
                apiService.deletePartnership(partId)
            }
            // If they had no mutual relation, delete entirely, else remove reviewsYou
            if (friend.youReviewStatus == null) {
                friendDao.deleteFriend(friend.userId)
            } else {
                val updated = friend.copy(reviewsYouStatus = null, reviewsYouPartnershipId = null)
                friendDao.updateFriend(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            friendDao.deleteFriend(friend.userId)
            Result.success(Unit)
        }
    }

    suspend fun updateRole(friend: FriendEntity, role: String): Result<Unit> {
        return try {
            val partId = friend.reviewsYouPartnershipId
            if (partId != null && apiService != null) {
                apiService.updatePartnershipRole(partId, UpdateRoleRequestDto(role))
            }
            val updated = friend.copy(reviewsYouRole = role)
            friendDao.updateFriend(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            val updated = friend.copy(reviewsYouRole = role)
            friendDao.updateFriend(updated)
            Result.success(Unit)
        }
    }

    suspend fun updateRemind(friend: FriendEntity, remind: Boolean): Result<Unit> {
        return try {
            val partId = friend.youReviewPartnershipId
            if (partId != null && apiService != null) {
                apiService.updatePartnershipRemind(partId, UpdateRemindRequestDto(remind))
            }
            val updated = friend.copy(youReviewRemind = remind)
            friendDao.updateFriend(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            val updated = friend.copy(youReviewRemind = remind)
            friendDao.updateFriend(updated)
            Result.success(Unit)
        }
    }

    suspend fun stopReviewing(friend: FriendEntity): Result<Unit> {
        return try {
            val youId = friend.youReviewPartnershipId
            if (youId != null && apiService != null) {
                apiService.deletePartnership(youId)
            }
            val revId = friend.reviewsYouPartnershipId
            if (revId != null && apiService != null) {
                apiService.deletePartnership(revId)
            }
            friendDao.deleteFriend(friend.userId)
            Result.success(Unit)
        } catch (e: Exception) {
            friendDao.deleteFriend(friend.userId)
            Result.success(Unit)
        }
    }

    suspend fun seedInitialCompanionsIfEmpty() {
        // Seeds a representative companion circle if empty
        val existing = friendDao.getFriendById("c_bilal")
        if (existing == null) {
            val companions = listOf(
                FriendEntity(
                    userId = "c_bilal",
                    name = "Bilal Ibn Rabah (\u2067بلال\u2069)",
                    reviewsYouPartnershipId = "p_bilal_1",
                    reviewsYouStatus = "active",
                    reviewsYouPermission = "assess",
                    reviewsYouRole = "peer",
                    youReviewPartnershipId = "p_bilal_2",
                    youReviewStatus = "active",
                    youReviewPermission = "assess",
                    youReviewDueToday = 4,
                    youReviewPlannedCards = 6,
                    youReviewRemind = true
                ),
                FriendEntity(
                    userId = "c_ali",
                    name = "Sheikh Ali (الشيخ علي)",
                    reviewsYouPartnershipId = "p_ali_1",
                    reviewsYouStatus = "active",
                    reviewsYouPermission = "assess",
                    reviewsYouRole = "teacher",
                    youReviewPartnershipId = null,
                    youReviewStatus = null,
                    youReviewDueToday = 0
                ),
                FriendEntity(
                    userId = "c_tariq",
                    name = "Tariq (طارق)",
                    reviewsYouPartnershipId = "p_tariq_1",
                    reviewsYouStatus = "pending",
                    reviewsYouPermission = "assess",
                    reviewsYouRole = "peer",
                    youReviewStatus = null
                )
            )
            friendDao.insertFriends(companions)
        }
    }

    private fun FriendDto.toEntity(): FriendEntity {
        return FriendEntity(
            userId = userId,
            name = name,
            reviewsYouPartnershipId = reviewsYou?.partnershipId,
            reviewsYouStatus = reviewsYou?.status,
            reviewsYouPermission = reviewsYou?.permission,
            reviewsYouRole = reviewsYou?.role,
            reviewsYouSince = reviewsYou?.since,
            reviewsYouCount = reviewsYou?.reviews ?: 0,
            reviewsYouLastReviewedAt = reviewsYou?.lastReviewedAt,
            youReviewPartnershipId = youReview?.partnershipId,
            youReviewStatus = youReview?.status,
            youReviewPermission = youReview?.permission,
            youReviewRole = youReview?.role,
            youReviewDueToday = youReview?.dueToday ?: 0,
            youReviewPlannedCards = youReview?.plannedCards ?: 0,
            youReviewRemind = youReview?.remind ?: false,
            youReviewSince = youReview?.since,
            youReviewCount = youReview?.reviews ?: 0,
            youReviewLastReviewedAt = youReview?.lastReviewedAt
        )
    }
}
