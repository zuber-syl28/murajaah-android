package com.sylergy.murajaah.reminders

import android.content.Context
import android.content.SharedPreferences
import com.sylergy.murajaah.reminders.model.ReminderPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneId

/**
 * Manages local persistence for reminder schedules, snooze states, and firing records.
 */
class ReminderPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("murajaah_reminders", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _planFlow = MutableStateFlow(loadPlan())
    val planFlow: StateFlow<ReminderPlan> = _planFlow.asStateFlow()

    fun loadPlan(): ReminderPlan {
        val raw = prefs.getString(KEY_PLAN, null) ?: return ReminderPlan(
            enabled = true,
            times = listOf("08:00", "19:00"),
            timezone = ZoneId.systemDefault().id
        )
        return try {
            json.decodeFromString<ReminderPlan>(raw)
        } catch (e: Exception) {
            ReminderPlan()
        }
    }

    fun savePlan(plan: ReminderPlan) {
        val raw = json.encodeToString(plan)
        prefs.edit().putString(KEY_PLAN, raw).apply()
        _planFlow.value = plan
    }

    fun updatePlan(transform: (ReminderPlan) -> ReminderPlan): ReminderPlan {
        val current = loadPlan()
        val next = transform(current)
        savePlan(next)
        return next
    }

    companion object {
        private const val KEY_PLAN = "reminder_plan_v1"
    }
}
