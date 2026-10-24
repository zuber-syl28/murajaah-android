package com.sylergy.murajaah.network.sync

import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.GradeOutboxEntity
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import com.sylergy.murajaah.network.api.MurajaahApiService
import com.sylergy.murajaah.network.model.CardDto
import com.sylergy.murajaah.network.model.GradeItemDto
import com.sylergy.murajaah.network.model.GradeSyncRequestDto
import com.sylergy.murajaah.network.model.SpanDto
import java.util.UUID

sealed class SyncResult {
    data class Success(val applied: Int, val skipped: Int) : SyncResult()
    data class Error(val message: String, val throwable: Throwable? = null) : SyncResult()
}

class SyncRepository(
    private val apiService: MurajaahApiService,
    private val database: MurajaahDatabase
) {
    suspend fun fetchAndStoreSnapshot(subject: String = "me"): SyncResult {
        return try {
            val response = apiService.getOfflineSnapshot(subject)
            if (!response.isSuccessful) {
                return SyncResult.Error("Failed to fetch snapshot: ${response.code()} ${response.message()}")
            }
            val snapshot = response.body() ?: return SyncResult.Error("Empty snapshot response")

            // Ingest cards
            val cardEntities = snapshot.cards.map { it.toEntity() }
            if (cardEntities.isNotEmpty()) {
                database.cardDao().insertAll(cardEntities)
                database.cardDao().deleteCardsNotIn(cardEntities.map { it.id })
            }

            // Ingest ranges
            val rangeEntities = snapshot.ranges.map { range ->
                MemorizedRangeEntity(
                    id = range.id,
                    surah = range.surah,
                    fromAyah = range.fromAyah,
                    toAyah = range.toAyah,
                    surahName = range.surahName,
                    surahArabicName = range.surahArabicName,
                    ayahsCount = range.ayahs ?: (range.toAyah - range.fromAyah + 1),
                    cardsCount = range.cards ?: 0
                )
            }
            if (rangeEntities.isNotEmpty()) {
                database.memorizedRangeDao().insertAll(rangeEntities)
                database.memorizedRangeDao().deleteRangesNotIn(rangeEntities.map { it.id })
            }

            // Ingest plans
            val planEntities = snapshot.plans.plans.map { plan ->
                RevisionPlanEntity(
                    id = plan.id,
                    name = plan.name,
                    surahs = plan.surahs,
                    isActive = plan.id == snapshot.plans.activeId
                )
            }
            if (planEntities.isNotEmpty()) {
                database.revisionPlanDao().insertAll(planEntities)
            }

            SyncResult.Success(applied = cardEntities.size, skipped = 0)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Snapshot sync failed", e)
        }
    }

    suspend fun syncPendingGrades(subject: String = "me"): SyncResult {
        return try {
            val pending = database.gradeOutboxDao().getPendingGrades()
            if (pending.isEmpty()) {
                return SyncResult.Success(applied = 0, skipped = 0)
            }

            val items = pending.map { outbox ->
                GradeItemDto(
                    clientId = outbox.clientId,
                    cardId = outbox.cardId,
                    rating = outbox.rating,
                    recordedAt = outbox.recordedAt,
                    recallSeconds = outbox.recallSeconds,
                    hintsCount = outbox.hintsCount,
                    slippedAyahs = outbox.slippedAyahs.ifEmpty { null },
                    tags = outbox.tags.ifEmpty { null },
                    note = outbox.note,
                    span = run {
                        val s = outbox.surah
                        val f = outbox.fromAyah
                        val t = outbox.toAyah
                        if (s != null && f != null && t != null) SpanDto(s, f, t) else null
                    }
                )
            }

            val response = apiService.syncGrades(subject, GradeSyncRequestDto(grades = items))
            if (!response.isSuccessful) {
                return SyncResult.Error("Sync failed: ${response.code()} ${response.message()}")
            }

            val syncResponse = response.body() ?: return SyncResult.Error("Empty sync response")

            // Remove synced items from outbox
            database.gradeOutboxDao().deleteByClientIds(pending.map { it.clientId })

            // Update cards from response mirror
            if (syncResponse.cards.isNotEmpty()) {
                val updatedEntities = syncResponse.cards.map { it.toEntity() }
                database.cardDao().insertAll(updatedEntities)
            }

            SyncResult.Success(applied = syncResponse.applied, skipped = syncResponse.skipped)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Grade sync failed", e)
        }
    }

    suspend fun recordOfflineGrade(
        cardId: String,
        rating: String,
        recordedAt: String,
        recallSeconds: Int? = null,
        hintsCount: Int? = null,
        slippedAyahs: List<Int> = emptyList(),
        tags: List<String> = emptyList(),
        note: String? = null,
        surah: Int? = null,
        fromAyah: Int? = null,
        toAyah: Int? = null
    ): String {
        val clientId = UUID.randomUUID().toString()
        val outboxItem = GradeOutboxEntity(
            clientId = clientId,
            cardId = cardId,
            rating = rating,
            recordedAt = recordedAt,
            recallSeconds = recallSeconds,
            hintsCount = hintsCount,
            slippedAyahs = slippedAyahs,
            tags = tags,
            note = note,
            surah = surah,
            fromAyah = fromAyah,
            toAyah = toAyah,
            createdAt = System.currentTimeMillis(),
            synced = false
        )
        database.gradeOutboxDao().insert(outboxItem)
        return clientId
    }

    private fun CardDto.toEntity(): CardEntity {
        return CardEntity(
            id = id,
            surah = surah,
            fromAyah = fromAyah,
            toAyah = toAyah,
            juz = juz,
            page = page,
            status = status,
            stability = stability,
            difficulty = difficulty,
            scheduledDays = scheduledDays,
            dueAt = dueAt,
            lastReviewedAt = lastReviewedAt,
            reviewCount = reviewCount,
            gradedBy = gradedBy,
            consensus = consensus,
            firstAyahText = firstAyahText
        )
    }
}
