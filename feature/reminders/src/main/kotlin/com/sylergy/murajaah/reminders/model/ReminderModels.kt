package com.sylergy.murajaah.reminders.model

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Concrete companion fact for listening reminder.
 */
@Serializable
data class CompanionDueFact(
    val name: String,
    val cards: Int
)

/**
 * Concrete reminder payload facts.
 */
@Serializable
data class ReminderFacts(
    val cards: Int,
    val minutes: Int,
    val daysAway: Int,
    val listening: List<CompanionDueFact> = emptyList()
)

/**
 * Reminder scheduling and persistence configuration.
 */
@Serializable
data class ReminderPlan(
    val enabled: Boolean = true,
    val times: List<String> = listOf("08:00", "19:00"),
    val timezone: String = ZoneId.systemDefault().id,
    val snoozeUntil: Long? = null,
    val lastSentKey: String? = null,
    val lastSentAt: Long? = null,
    val maxTimes: Int = 5
)

sealed interface ReminderTrigger {
    data class Due(val key: String) : ReminderTrigger
    data class Snoozed(val key: String) : ReminderTrigger
}

enum class ReminderKind {
    DUE,
    GENTLE,
    SNOOZED,
    LISTEN,
    TEST
}

object ReminderRules {
    const val SLOT_GRACE_MINUTES = 45
    const val EVENING_HOUR = 19
    const val GENTLE_AFTER_DAYS = 4

    fun isValidTime(value: String): Boolean {
        return Regex("^([01]\\d|2[0-3]):([0-5]\\d)$").matches(value)
    }

    fun minutesOfDay(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    /**
     * Determines whether a reminder is due to fire at the current instant.
     */
    fun dueReminder(plan: ReminderPlan, nowMillis: Long = System.currentTimeMillis()): ReminderTrigger? {
        if (!plan.enabled) return null

        val zoneId = try {
            ZoneId.of(plan.timezone)
        } catch (e: Exception) {
            ZoneId.of("UTC")
        }

        // Snooze checking
        if (plan.snoozeUntil != null) {
            if (nowMillis < plan.snoozeUntil) {
                return null // Still snoozed
            }
            val key = "snooze:${plan.snoozeUntil}"
            if (plan.lastSentKey != key && (nowMillis - plan.snoozeUntil) < SLOT_GRACE_MINUTES * 60_000L) {
                return ReminderTrigger.Snoozed(key)
            }
        }

        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zoneId)
        val currentMinutesOfDay = zonedDateTime.hour * 60 + zonedDateTime.minute
        val currentDateStr = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)

        var latestSlotTime: String? = null
        var minSince: Int = Int.MAX_VALUE

        for (timeStr in plan.times) {
            val slot = minutesOfDay(timeStr) ?: continue
            val since = currentMinutesOfDay - slot
            if (since in 0..SLOT_GRACE_MINUTES) {
                if (since < minSince) {
                    minSince = since
                    latestSlotTime = timeStr
                }
            }
        }

        if (latestSlotTime == null) return null

        val slotKey = "${currentDateStr}T$latestSlotTime"
        if (plan.lastSentKey == slotKey) return null

        // Check if recently sent within slot
        val lastSent = plan.lastSentAt
        val slotStartMillis = nowMillis - (minSince * 60_000L) - (zonedDateTime.second * 1000L)
        if (lastSent != null && lastSent >= slotStartMillis) return null

        return ReminderTrigger.Due(slotKey)
    }

    /**
     * Calculates the timestamp when a snooze should expire.
     */
    fun calculateSnooze(nowMillis: Long, timezone: String, minutes: Int?, evening: Boolean): Long {
        if (!evening && minutes != null) {
            return nowMillis + (minutes * 60_000L)
        }

        val zoneId = try {
            ZoneId.of(timezone)
        } catch (e: Exception) {
            ZoneId.of("UTC")
        }

        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zoneId)
        var targetEvening = zdt.with(LocalTime.of(EVENING_HOUR, 0, 0))

        if (targetEvening.toInstant().toEpochMilli() <= nowMillis) {
            // Tomorrow evening
            targetEvening = targetEvening.plusDays(1)
        }
        return targetEvening.toInstant().toEpochMilli()
    }

    /**
     * Produces supportive, non-shaming notification copy in English and Arabic.
     */
    fun generateCopy(kind: ReminderKind, facts: ReminderFacts, isArabic: Boolean): Pair<String, String> {
        val cardsStr = if (isArabic) "${facts.cards} مقاطع" else "${facts.cards} passages"
        val minStr = if (isArabic) "${facts.minutes} دقائق" else "${facts.minutes} min"

        if (kind == ReminderKind.TEST) {
            return if (isArabic) {
                "التذكيرات مفعلة" to "هكذا يبدو تذكير مراجعة وتثبيت القرآن."
            } else {
                "Reminders are active" to "This is what a supportive revision nudge looks like."
            }
        }

        val listeningLine = if (facts.listening.isNotEmpty()) {
            val names = facts.listening.joinToString("، ") { "${it.name} (${it.cards})" }
            if (isArabic) "لديك تسميع مستحق لرفقائك: $names." else "Due listening for: $names."
        } else ""

        if (kind == ReminderKind.LISTEN) {
            val title = if (isArabic) "وقت الاستماع" else "Time to listen"
            val body = if (isArabic) "$listeningLine هل ترغب بالاستماع الآن؟" else "$listeningLine Ready to listen now?"
            return title to body
        }

        val gentle = kind == ReminderKind.GENTLE || facts.daysAway >= GENTLE_AFTER_DAYS

        val (title, bodyTemplate) = when {
            kind == ReminderKind.SNOOZED -> {
                if (isArabic) "ما زلنا هنا حين تكون جاهزاً" to "بانتظارك $cardsStr ($minStr)."
                else "Still here when you are" to "$cardsStr waiting ($minStr)."
            }
            gentle -> {
                if (isArabic) "لنبدأ من جديد بهدوء" to "المستحق اليوم: $cardsStr، حوالي $minStr. لا داعي للتعويض."
                else "Let’s restart gently" to "Today: $cardsStr, about $minStr. No need to catch up."
            }
            else -> {
                if (isArabic) "وقت المراجعة" to "المستحق اليوم: $cardsStr. $minStr. تبدأ الآن؟"
                else "Time to revise" to "$cardsStr due. $minStr. Start now?"
            }
        }

        val fullBody = if (listeningLine.isNotBlank()) "$bodyTemplate $listeningLine" else bodyTemplate
        return title to fullBody
    }
}
