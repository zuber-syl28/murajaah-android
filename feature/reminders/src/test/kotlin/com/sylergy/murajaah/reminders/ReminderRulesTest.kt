package com.sylergy.murajaah.reminders

import com.sylergy.murajaah.reminders.model.CompanionDueFact
import com.sylergy.murajaah.reminders.model.ReminderFacts
import com.sylergy.murajaah.reminders.model.ReminderKind
import com.sylergy.murajaah.reminders.model.ReminderPlan
import com.sylergy.murajaah.reminders.model.ReminderRules
import com.sylergy.murajaah.reminders.model.ReminderTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderRulesTest {

    private val testZone = "UTC"

    @Test
    fun testTimeValidation() {
        assertTrue(ReminderRules.isValidTime("00:00"))
        assertTrue(ReminderRules.isValidTime("08:30"))
        assertTrue(ReminderRules.isValidTime("19:00"))
        assertTrue(ReminderRules.isValidTime("23:59"))

        assertFalse(ReminderRules.isValidTime("24:00"))
        assertFalse(ReminderRules.isValidTime("8:30"))
        assertFalse(ReminderRules.isValidTime("12:60"))
        assertFalse(ReminderRules.isValidTime("invalid"))
    }

    @Test
    fun testDueReminderWithinGraceWindow() {
        val plan = ReminderPlan(
            enabled = true,
            times = listOf("08:00"),
            timezone = testZone
        )

        val date = LocalDate.of(2026, 10, 24)

        // 07:55 - Before slot: should NOT fire
        val before = ZonedDateTime.of(date, LocalTime.of(7, 55), ZoneId.of(testZone)).toInstant().toEpochMilli()
        assertNull(ReminderRules.dueReminder(plan, before))

        // 08:15 - Inside 45-minute grace window: should fire
        val insideGrace = ZonedDateTime.of(date, LocalTime.of(8, 15), ZoneId.of(testZone)).toInstant().toEpochMilli()
        val trigger = ReminderRules.dueReminder(plan, insideGrace)
        assertNotNull(trigger)
        assertTrue(trigger is ReminderTrigger.Due)
        assertEquals("2026-10-24T08:00", (trigger as ReminderTrigger.Due).key)

        // 08:50 - Past 45-minute grace window: should stay quiet
        val afterGrace = ZonedDateTime.of(date, LocalTime.of(8, 50), ZoneId.of(testZone)).toInstant().toEpochMilli()
        assertNull(ReminderRules.dueReminder(plan, afterGrace))
    }

    @Test
    fun testDeduplicationForAlreadySentSlot() {
        val plan = ReminderPlan(
            enabled = true,
            times = listOf("08:00"),
            timezone = testZone,
            lastSentKey = "2026-10-24T08:00"
        )

        val date = LocalDate.of(2026, 10, 24)
        val insideGrace = ZonedDateTime.of(date, LocalTime.of(8, 20), ZoneId.of(testZone)).toInstant().toEpochMilli()

        // Already sent for this slot today: should not fire again
        assertNull(ReminderRules.dueReminder(plan, insideGrace))
    }

    @Test
    fun testSnoozeMechanism() {
        val date = LocalDate.of(2026, 10, 24)
        val snoozeTarget = ZonedDateTime.of(date, LocalTime.of(9, 0), ZoneId.of(testZone)).toInstant().toEpochMilli()

        val plan = ReminderPlan(
            enabled = true,
            times = listOf("08:00"),
            timezone = testZone,
            snoozeUntil = snoozeTarget
        )

        // 08:30 - Snooze is active: must remain quiet even though 08:00 grace window is open
        val duringSnooze = ZonedDateTime.of(date, LocalTime.of(8, 30), ZoneId.of(testZone)).toInstant().toEpochMilli()
        assertNull(ReminderRules.dueReminder(plan, duringSnooze))

        // 09:05 - Snooze has expired within grace window: should fire Snoozed trigger
        val afterSnooze = ZonedDateTime.of(date, LocalTime.of(9, 5), ZoneId.of(testZone)).toInstant().toEpochMilli()
        val trigger = ReminderRules.dueReminder(plan, afterSnooze)
        assertNotNull(trigger)
        assertTrue(trigger is ReminderTrigger.Snoozed)
    }

    @Test
    fun testReminderCopyNonShamingPrinciples() {
        // Standard due copy
        val facts = ReminderFacts(cards = 4, minutes = 5, daysAway = 0)
        val (enTitle, enBody) = ReminderRules.generateCopy(ReminderKind.DUE, facts, isArabic = false)
        assertEquals("Time to revise", enTitle)
        assertTrue(enBody.contains("4 passages due. 5 min. Start now?"))

        val (arTitle, arBody) = ReminderRules.generateCopy(ReminderKind.DUE, facts, isArabic = true)
        assertEquals("وقت المراجعة", arTitle)
        assertTrue(arBody.contains("المستحق"))

        // Gentle re-entry when days away >= 4
        val gentleFacts = ReminderFacts(cards = 4, minutes = 5, daysAway = 5)
        val (gentleTitle, gentleBody) = ReminderRules.generateCopy(ReminderKind.DUE, gentleFacts, isArabic = false)
        assertEquals("Let’s restart gently", gentleTitle)
        assertTrue("Must reassure no catch up pressure", gentleBody.contains("No need to catch up"))

        // Companion listening nudge
        val listenFacts = ReminderFacts(
            cards = 0,
            minutes = 0,
            daysAway = 0,
            listening = listOf(CompanionDueFact("Bilal", 4))
        )
        val (listenTitle, listenBody) = ReminderRules.generateCopy(ReminderKind.LISTEN, listenFacts, isArabic = false)
        assertEquals("Time to listen", listenTitle)
        assertTrue(listenBody.contains("Bilal (4)"))
    }
}
