package com.sylergy.murajaah.reminders.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sylergy.murajaah.reminders.NotificationHelper
import com.sylergy.murajaah.reminders.ReminderPreferences
import com.sylergy.murajaah.reminders.model.ReminderRules

/**
 * BroadcastReceiver triggered by notification actions for zero-friction snoozing.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val prefs = ReminderPreferences(context)
        val plan = prefs.loadPlan()
        val now = System.currentTimeMillis()

        when (action) {
            NotificationHelper.ACTION_SNOOZE_15 -> {
                val until = ReminderRules.calculateSnooze(now, plan.timezone, minutes = 15, evening = false)
                prefs.updatePlan { it.copy(snoozeUntil = until) }
                NotificationHelper.cancelNotification(context)
            }
            NotificationHelper.ACTION_SNOOZE_60 -> {
                val until = ReminderRules.calculateSnooze(now, plan.timezone, minutes = 60, evening = false)
                prefs.updatePlan { it.copy(snoozeUntil = until) }
                NotificationHelper.cancelNotification(context)
            }
            NotificationHelper.ACTION_SNOOZE_EVENING -> {
                val until = ReminderRules.calculateSnooze(now, plan.timezone, minutes = null, evening = true)
                prefs.updatePlan { it.copy(snoozeUntil = until) }
                NotificationHelper.cancelNotification(context)
            }
        }
    }
}
