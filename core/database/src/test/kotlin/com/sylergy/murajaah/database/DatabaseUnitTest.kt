package com.sylergy.murajaah.database

import com.sylergy.murajaah.database.converter.Converters
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.scheduler.ReviewerRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatabaseUnitTest {
    private val converters = Converters()

    @Test
    fun testIntListConverter() {
        val list = listOf(1, 2, 3, 67, 114)
        val json = converters.fromIntList(list)
        val decoded = converters.toIntList(json)
        assertEquals(list, decoded)

        assertEquals(emptyList<Int>(), converters.toIntList(null))
        assertEquals(emptyList<Int>(), converters.toIntList(""))
    }

    @Test
    fun testStringListConverter() {
        val list = listOf("tajweed", "hesitation", "slip")
        val json = converters.fromStringList(list)
        val decoded = converters.toStringList(json)
        assertEquals(list, decoded)

        assertEquals(emptyList<String>(), converters.toStringList(null))
        assertEquals(emptyList<String>(), converters.toStringList(""))
    }

    @Test
    fun testCardMappers() {
        val entity = CardEntity(
            id = "card-1",
            surah = 67,
            fromAyah = 1,
            toAyah = 10,
            status = "strong",
            stability = 14.5,
            difficulty = 3.2,
            scheduledDays = 14,
            dueAt = "2026-10-30",
            lastReviewedAt = "2026-10-16",
            reviewCount = 5,
            gradedBy = "teacher"
        )

        val domain = entity.toDomain()
        assertEquals("card-1", domain.id)
        assertEquals(67, domain.surah)
        assertEquals(1, domain.fromAyah)
        assertEquals(10, domain.toAyah)
        assertEquals(Rating.Strong, domain.status)
        assertEquals(14.5, domain.stability, 0.001)
        assertEquals(ReviewerRole.Teacher, domain.gradedBy)

        val backToEntity = domain.toEntity(juz = 29, page = 562)
        assertEquals(entity.id, backToEntity.id)
        assertEquals(entity.status, backToEntity.status)
        assertEquals(entity.stability, backToEntity.stability, 0.001)
        assertEquals(29, backToEntity.juz)
        assertEquals(562, backToEntity.page)
    }
}
