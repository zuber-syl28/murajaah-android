package com.sylergy.murajaah.network.api

import com.sylergy.murajaah.network.model.GradeSyncRequestDto
import com.sylergy.murajaah.network.model.GradeSyncResponseDto
import com.sylergy.murajaah.network.model.OfflineSnapshotDto
import com.sylergy.murajaah.network.model.UpdateRangesRequestDto
import com.sylergy.murajaah.network.model.UpdateRangesResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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
}
