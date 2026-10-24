package com.sylergy.murajaah.network

import com.sylergy.murajaah.network.model.GradeItemDto
import com.sylergy.murajaah.network.model.GradeSyncRequestDto
import com.sylergy.murajaah.network.model.GradeSyncResponseDto
import com.sylergy.murajaah.network.model.OfflineSnapshotDto
import com.sylergy.murajaah.network.model.SpanDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NetworkSerializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testOfflineSnapshotDeserialization() {
        val payload = """
        {
          "today": "2026-10-24",
          "subject": {
            "id": "usr-123",
            "name": "Aisha",
            "narration": "hafs",
            "timezone": "UTC"
          },
          "settings": {
            "sessionMode": "full",
            "revisionStyle": "smart",
            "dailyPages": 5.0,
            "dailyMinutes": 20
          },
          "plans": {
            "plans": [
              { "id": "plan-1", "name": "Juz 30", "surahs": [78, 79, 80] }
            ],
            "activeId": "plan-1"
          },
          "cardSize": "page",
          "cards": [
            {
              "id": "card-1",
              "surah": 67,
              "surahName": "Al-Mulk",
              "surahArabicName": "الملك",
              "fromAyah": 1,
              "toAyah": 10,
              "ayahs": 10,
              "narration": "hafs",
              "firstAyahText": "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ",
              "juz": 29,
              "page": 562,
              "status": "strong",
              "stability": 14.2,
              "difficulty": 3.1,
              "scheduledDays": 14,
              "dueAt": "2026-10-30",
              "dueInDays": 6,
              "lastReviewedAt": "2026-10-16",
              "daysSinceReview": 8,
              "reviewCount": 3,
              "gradedBy": "teacher",
              "consensus": 0.95
            }
          ],
          "ranges": [
            {
              "id": "range-1",
              "surah": 67,
              "fromAyah": 1,
              "toAyah": 30,
              "surahName": "Al-Mulk",
              "surahArabicName": "الملك",
              "ayahs": 30,
              "cards": 3,
              "juz": [29]
            }
          ]
        }
        """.trimIndent()

        val snapshot = json.decodeFromString<OfflineSnapshotDto>(payload)
        assertEquals("2026-10-24", snapshot.today)
        assertEquals("usr-123", snapshot.subject.id)
        assertEquals("hafs", snapshot.subject.narration)
        assertEquals("smart", snapshot.settings.revisionStyle)
        assertEquals(1, snapshot.plans.plans.size)
        assertEquals("plan-1", snapshot.plans.activeId)
        assertEquals(1, snapshot.cards.size)

        val card = snapshot.cards.first()
        assertEquals("card-1", card.id)
        assertEquals(67, card.surah)
        assertEquals("strong", card.status)
        assertEquals(14.2, card.stability, 0.001)
        assertEquals("teacher", card.gradedBy)

        assertEquals(1, snapshot.ranges.size)
        val range = snapshot.ranges.first()
        assertEquals(67, range.surah)
        assertEquals(1, range.fromAyah)
        assertEquals(30, range.toAyah)
    }

    @Test
    fun testGradeSyncRequestAndResponse() {
        val request = GradeSyncRequestDto(
            grades = listOf(
                GradeItemDto(
                    clientId = "offline-1",
                    cardId = "card-1",
                    rating = "review",
                    recordedAt = "2026-10-24",
                    recallSeconds = 40,
                    hintsCount = 1,
                    slippedAyahs = listOf(3),
                    tags = listOf("slip", "hesitation"),
                    span = SpanDto(surah = 67, fromAyah = 1, toAyah = 10)
                )
            )
        )

        val jsonStr = json.encodeToString(request)
        val decoded = json.decodeFromString<GradeSyncRequestDto>(jsonStr)
        assertEquals(1, decoded.grades.size)
        assertEquals("offline-1", decoded.grades[0].clientId)
        assertEquals(listOf(3), decoded.grades[0].slippedAyahs)
        assertNotNull(decoded.grades[0].span)
        assertEquals(67, decoded.grades[0].span?.surah)

        val responsePayload = """
        {
          "applied": 1,
          "skipped": 0,
          "today": "2026-10-24",
          "cards": []
        }
        """.trimIndent()

        val response = json.decodeFromString<GradeSyncResponseDto>(responsePayload)
        assertEquals(1, response.applied)
        assertEquals(0, response.skipped)
        assertEquals("2026-10-24", response.today)
    }
}
