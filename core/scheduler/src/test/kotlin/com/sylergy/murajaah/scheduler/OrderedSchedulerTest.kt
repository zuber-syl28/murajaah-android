package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderedSchedulerTest {

    private fun testCard(
        id: String,
        surah: Int,
        fromAyah: Int,
        page: Int?,
        status: Rating = Rating.Strong,
        reviewCount: Int = 2,
        dueAt: String = "2026-03-20"
    ): Card {
        return Card(
            id = id,
            surah = surah,
            fromAyah = fromAyah,
            toAyah = fromAyah + 9,
            juz = 29,
            page = page,
            status = status,
            stability = 7.0,
            difficulty = 5.0,
            dueAt = dueAt,
            lastReviewedAt = "2026-03-03",
            reviewCount = reviewCount
        )
    }

    private val sampleCards = listOf(
        testCard("q1", 68, 1, 564),
        testCard("m3", 67, 21, 564),
        testCard("m1", 67, 1, 562),
        testCard("q2", 68, 11, 565),
        testCard("m2", 67, 11, 563),
        testCard("q3", 68, 21, 566)
    )

    @Test
    fun orderKey_sortsBySurahThenAyah() {
        assertEquals(67021, OrderedScheduler.orderKey(67, 21))
        val sorted = OrderedScheduler.mushafOrder(sampleCards)
        assertEquals(listOf("m1", "m2", "m3", "q1", "q2", "q3"), sorted.map { it.id })
    }

    @Test
    fun portionQueue_startsAtBeginningWhenNoCursor() {
        val portion = OrderedScheduler.portionQueue(sampleCards, cursor = null, pages = 2)
        assertEquals(listOf("m1", "m2"), portion.map { it.id })
    }

    @Test
    fun portionQueue_continuesAfterCursorAndCountsPagesNotCards() {
        // After m2 (page 563): m3 and q1 share page 564, q2 is page 565 -> 2 pages = [m3, q1, q2]
        val cursor = OrderedScheduler.orderKey(67, 11)
        val portion = OrderedScheduler.portionQueue(sampleCards, cursor = cursor, pages = 2)
        assertEquals(listOf("m3", "q1", "q2"), portion.map { it.id })
    }

    @Test
    fun portionQueue_wrapsAroundToBeginningAfterLastPassage() {
        val cursor = OrderedScheduler.orderKey(68, 11) // q2
        val portion = OrderedScheduler.portionQueue(sampleCards, cursor = cursor, pages = 2)
        // after q2 -> q3 (page 566), wraps to m1 (page 562)
        assertEquals(listOf("q3", "m1"), portion.map { it.id })
    }

    @Test
    fun weakInserts_prioritizesDueWeakCardsCapped() {
        val today = "2026-03-10"
        val cards = listOf(
            testCard("w2", 68, 1, 564, status = Rating.Weak, dueAt = today),
            testCard("w1", 67, 1, 562, status = Rating.Weak, dueAt = "2026-03-09"),
            testCard("w3", 78, 1, 582, status = Rating.Weak, dueAt = today),
            testCard("notdue", 2, 1, 2, status = Rating.Weak, dueAt = "2026-04-01"),
            testCard("strong", 3, 1, 50, status = Rating.Strong, dueAt = today)
        )
        val weak = OrderedScheduler.weakInserts(cards, today, exclude = listOf("w1"), max = 2)
        assertEquals(listOf("w2", "w3"), weak.map { it.id })
    }

    @Test
    fun buildOrderedQueue_combinesWeakInsertsAndPortion() {
        val today = "2026-03-10"
        val cards = listOf(
            testCard("m1", 67, 1, 562, status = Rating.Weak, dueAt = today),
            testCard("m2", 67, 11, 563),
            testCard("m3", 67, 21, 564)
        )
        val queue = OrderedScheduler.buildOrderedQueue(cards, on = today, cursor = null, pages = 2)
        // m1 is due weak -> first, portion covers m1 and m2 -> m1 deduped, queue has [m1, m2]
        assertEquals(listOf("m1", "m2"), queue.map { it.id })
    }
}
