package com.sylergy.murajaah.scheduler

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

object DateUtils {
    private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(FORMATTER)

    fun addDays(date: String, days: Int): String {
        val parsed = LocalDate.parse(date, FORMATTER)
        return parsed.plusDays(days.toLong()).format(FORMATTER)
    }

    fun daysBetween(from: String, to: String): Int {
        val d1 = LocalDate.parse(from, FORMATTER)
        val d2 = LocalDate.parse(to, FORMATTER)
        return ChronoUnit.DAYS.between(d1, d2).toInt()
    }

    fun daysUntilDue(dueAt: String, on: String): Int {
        return daysBetween(on, dueAt)
    }

    fun daysSinceReview(lastReviewedAt: String?, on: String, fallback: Int = 30): Int {
        if (lastReviewedAt == null) return fallback
        return max(0, daysBetween(lastReviewedAt, on))
    }
}
