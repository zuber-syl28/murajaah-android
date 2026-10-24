package com.sylergy.murajaah.network.api

import com.sylergy.murajaah.network.model.AcceptInviteResponseDto
import com.sylergy.murajaah.network.model.CreateInviteResponseDto
import com.sylergy.murajaah.network.model.FriendDetailResponseDto
import com.sylergy.murajaah.network.model.FriendsResponseDto
import com.sylergy.murajaah.network.model.GradeSyncRequestDto
import com.sylergy.murajaah.network.model.GradeSyncResponseDto
import com.sylergy.murajaah.network.model.InvitePreviewResponseDto
import com.sylergy.murajaah.network.model.OfflineSnapshotDto
import com.sylergy.murajaah.network.model.UpdateRangesRequestDto
import com.sylergy.murajaah.network.model.UpdateRangesResponseDto
import com.sylergy.murajaah.network.model.UpdateRemindRequestDto
import com.sylergy.murajaah.network.model.UpdateRoleRequestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MurajaahApiService {
    @GET("api/u/{subject}/offline")
    suspend fun getOfflineSnapshot(
        @Path("subject") subject: String = "me"
    ): Response<OfflineSnapshotDto>

    @POST("api/u/{subject}/grades/sync")
    suspend fun syncGrades(
        @Path("subject") subject: String = "me",
        @Body request: GradeSyncRequestDto
    ): Response<GradeSyncResponseDto>

    @PUT("api/u/{subject}/ranges")
    suspend fun updateRanges(
        @Path("subject") subject: String = "me",
        @Body request: UpdateRangesRequestDto
    ): Response<UpdateRangesResponseDto>

    // Friends & Circle endpoints
    @GET("api/friends")
    suspend fun getFriends(): Response<FriendsResponseDto>

    @GET("api/friends/{userId}")
    suspend fun getFriendDetail(
        @Path("userId") userId: String
    ): Response<FriendDetailResponseDto>

    @POST("api/invites")
    suspend fun createInvite(): Response<CreateInviteResponseDto>

    @GET("api/invites/{token}")
    suspend fun getInvitePreview(
        @Path("token") token: String
    ): Response<InvitePreviewResponseDto>

    @POST("api/invites/{token}/accept")
    suspend fun acceptInvite(
        @Path("token") token: String
    ): Response<AcceptInviteResponseDto>

    @POST("api/partners/{partnershipId}/approve")
    suspend fun approvePartnership(
        @Path("partnershipId") partnershipId: String
    ): Response<ResponseBody>

    @DELETE("api/partners/{partnershipId}")
    suspend fun deletePartnership(
        @Path("partnershipId") partnershipId: String
    ): Response<ResponseBody>

    @PATCH("api/partners/{partnershipId}/role")
    suspend fun updatePartnershipRole(
        @Path("partnershipId") partnershipId: String,
        @Body request: UpdateRoleRequestDto
    ): Response<ResponseBody>

    @PATCH("api/partners/{partnershipId}/remind")
    suspend fun updatePartnershipRemind(
        @Path("partnershipId") partnershipId: String,
        @Body request: UpdateRemindRequestDto
    ): Response<ResponseBody>
}
