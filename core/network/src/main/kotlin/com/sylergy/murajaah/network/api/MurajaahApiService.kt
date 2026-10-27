package com.sylergy.murajaah.network.api

import com.sylergy.murajaah.network.model.AcceptInviteResponseDto
import com.sylergy.murajaah.network.model.AnswerCallRequestDto
import com.sylergy.murajaah.network.model.CallDetailResponseDto
import com.sylergy.murajaah.network.model.CallResponseDto
import com.sylergy.murajaah.network.model.CreateInviteResponseDto
import com.sylergy.murajaah.network.model.CreatePlanRequestDto
import com.sylergy.murajaah.network.model.FriendDetailResponseDto
import com.sylergy.murajaah.network.model.FriendsResponseDto
import com.sylergy.murajaah.network.model.GradeSyncRequestDto
import com.sylergy.murajaah.network.model.GradeSyncResponseDto
import com.sylergy.murajaah.network.model.IceServersResponseDto
import com.sylergy.murajaah.network.model.InvitePreviewResponseDto
import com.sylergy.murajaah.network.model.MendItemRequestDto
import com.sylergy.murajaah.network.model.OfflineSnapshotDto
import com.sylergy.murajaah.network.model.PlansResponseDto
import com.sylergy.murajaah.network.model.SkipItemRequestDto
import com.sylergy.murajaah.network.model.StartCallRequestDto
import com.sylergy.murajaah.network.model.UpdatePlanRequestDto
import com.sylergy.murajaah.network.model.UpdateRangesRequestDto
import com.sylergy.murajaah.network.model.UpdateRangesResponseDto
import com.sylergy.murajaah.network.model.UpdateRemindRequestDto
import com.sylergy.murajaah.network.model.UpdateRoleRequestDto
import com.sylergy.murajaah.network.model.WaitingCallsResponseDto
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

    // Revision Plans endpoints
    @GET("api/u/{subject}/plans")
    suspend fun getPlans(
        @Path("subject") subject: String = "me"
    ): Response<PlansResponseDto>

    @POST("api/u/{subject}/plans")
    suspend fun createPlan(
        @Path("subject") subject: String = "me",
        @Body request: CreatePlanRequestDto
    ): Response<PlansResponseDto>

    @PATCH("api/u/{subject}/plans/{planId}")
    suspend fun updatePlan(
        @Path("subject") subject: String = "me",
        @Path("planId") planId: String,
        @Body request: UpdatePlanRequestDto
    ): Response<PlansResponseDto>

    @DELETE("api/u/{subject}/plans/{planId}")
    suspend fun deletePlan(
        @Path("subject") subject: String = "me",
        @Path("planId") planId: String
    ): Response<PlansResponseDto>

    @POST("api/u/{subject}/plans/{planId}/activate")
    suspend fun activatePlan(
        @Path("subject") subject: String = "me",
        @Path("planId") planId: String
    ): Response<PlansResponseDto>

    // Session Live sync endpoints
    @POST("api/u/{subject}/sessions/{sessionId}/skip")
    suspend fun skipSessionItem(
        @Path("subject") subject: String = "me",
        @Path("sessionId") sessionId: String,
        @Body request: SkipItemRequestDto
    ): Response<ResponseBody>

    @POST("api/u/{subject}/sessions/{sessionId}/mend")
    suspend fun mendSessionItem(
        @Path("subject") subject: String = "me",
        @Path("sessionId") sessionId: String,
        @Body request: MendItemRequestDto
    ): Response<ResponseBody>

    @POST("api/u/{subject}/sessions/{sessionId}/extend")
    suspend fun extendSession(
        @Path("subject") subject: String = "me",
        @Path("sessionId") sessionId: String
    ): Response<ResponseBody>

    // Voice Signaling & ICE endpoints
    @GET("api/voice/ice")
    suspend fun getIceServers(): Response<IceServersResponseDto>

    @POST("api/voice")
    suspend fun startVoiceCall(
        @Body request: StartCallRequestDto
    ): Response<CallResponseDto>

    @GET("api/voice/waiting")
    suspend fun getWaitingCalls(): Response<WaitingCallsResponseDto>

    @GET("api/voice/{id}")
    suspend fun getCallDetail(
        @Path("id") id: String
    ): Response<CallDetailResponseDto>

    @POST("api/voice/{id}/answer")
    suspend fun answerVoiceCall(
        @Path("id") id: String,
        @Body request: AnswerCallRequestDto
    ): Response<CallResponseDto>

    @POST("api/voice/{id}/end")
    suspend fun endVoiceCall(
        @Path("id") id: String
    ): Response<CallResponseDto>

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
