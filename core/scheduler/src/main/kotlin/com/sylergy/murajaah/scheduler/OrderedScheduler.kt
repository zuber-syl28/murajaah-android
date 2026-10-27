package com.sylergy.murajaah.scheduler

import com.sylergy.murajaah.fsrs.Rating

/**
 * Traditional muraja'a (Ordered Wird):
 * A fixed portion each day, read in Mushaf order, continuing tomorrow
 * where today stopped, and wrapping around to the beginning.
 *
 * Weak passages still come back early (up to MAX_WEAK_INSERTS).
 * Sourced directly from Murajaah core specification (packages/shared/src/ordered.ts).
 */
object OrderedScheduler {
    const val MAX_WEAK_INSERTS = 2
    const val DEFAULT_PORTION_PAGES = 2

    /**
     * Position in the Mushaf (surah * 1000 + fromAyah).
     * Stable across splits and card re-divisions.
     */
    fun orderKey(surah: Int, fromAyah: Int): Int =
        surah * 1000 + fromAyah

    fun orderKey(card: Card): Int =
        orderKey(card.surah, card.fromAyah)

    fun mushafOrder(cards: List<Card>): List<Card> =
        cards.sortedBy { orderKey(it) }

    /**
     * Everything memorized, starting just after [afterKey] and wrapping round once.
     */
    fun fromCursor(cards: List<Card>, afterKey: Int?): List<Card> {
        val ordered = mushafOrder(cards)
        if (afterKey == null || ordered.isEmpty()) return ordered
        val startIndex = ordered.indexOfFirst { orderKey(it) > afterKey }
        return if (startIndex <= 0) {
            ordered
        } else {
            ordered.subList(startIndex, ordered.size) + ordered.subList(0, startIndex)
        }
    }

    /**
     * Today's portion: passages after the cursor until [pages] distinct Mushaf pages
     * are covered. A passage whose page is unknown counts as one page of its own.
     */
    fun portionQueue(cards: List<Card>, cursor: Int?, pages: Int = DEFAULT_PORTION_PAGES): List<Card> {
        val seenPages = mutableSetOf<String>()
        val portion = mutableListOf<Card>()

        for (card in fromCursor(cards, cursor)) {
            val pageKey = if (card.page == null) "card:${card.id}" else "page:${card.page}"
            if (!seenPages.contains(pageKey) && seenPages.size >= pages) {
                break
            }
            seenPages.add(pageKey)
            portion.add(card)
        }
        return portion
    }

    /**
     * Weak passages that are due today, in Mushaf order, capped at [max].
     * Only tested cards (reviewCount > 0) are considered.
     */
    fun weakInserts(
        cards: List<Card>,
        on: String,
        exclude: List<String> = emptyList(),
        max: Int = MAX_WEAK_INSERTS
    ): List<Card> {
        val weakDue = cards.filter { card ->
            card.reviewCount > 0 &&
            card.status == Rating.Weak &&
            DateUtils.daysUntilDue(card.dueAt, on) <= 0 &&
            !exclude.contains(card.id)
        }
        return mushafOrder(weakDue).take(max)
    }

    /**
     * Builds the complete sequential daily portion:
     * Due weak inserts placed first, followed by sequential Mushaf reading.
     */
    fun buildOrderedQueue(
        cards: List<Card>,
        on: String,
        cursor: Int?,
        pages: Int = DEFAULT_PORTION_PAGES,
        maxWeak: Int = MAX_WEAK_INSERTS
    ): List<Card> {
        val weak = weakInserts(cards, on, emptyList(), maxWeak)
        val weakIds = weak.map { it.id }.toSet()
        val portion = portionQueue(cards, cursor, pages).filter { it.id !in weakIds }
        return weak + portion
    }

    /**
     * "A little more" (+2 cards): continues from the last card in order.
     */
    fun continueReading(
        cards: List<Card>,
        afterKey: Int?,
        exclude: List<String> = emptyList(),
        count: Int = 2
    ): List<Card> {
        return fromCursor(cards, afterKey)
            .filter { !exclude.contains(it.id) }
            .take(count)
    }
}
