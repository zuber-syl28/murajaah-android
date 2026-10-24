package com.sylergy.murajaah.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.reminders.model.CompanionDueFact
import com.sylergy.murajaah.reminders.model.ReminderFacts
import com.sylergy.murajaah.reminders.model.ReminderKind
import com.sylergy.murajaah.reminders.model.ReminderPlan
import com.sylergy.murajaah.reminders.model.ReminderRules
import com.sylergy.murajaah.reminders.worker.ReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

/**
 * ViewModel managing notification reminder times, master toggle, and test nudges.
 */
class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ReminderPreferences(application)

    val plan: StateFlow<ReminderPlan> = prefs.planFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = prefs.loadPlan()
    )

    init {
        val current = prefs.loadPlan()
        if (current.enabled) {
            ReminderWorker.schedule(application)
        }
    }

    fun toggleEnabled(): Boolean {
        val updated = prefs.updatePlan { it.copy(enabled = !it.enabled) }
        if (updated.enabled) {
            ReminderWorker.schedule(getApplication())
        } else {
            ReminderWorker.cancel(getApplication())
        }
        return updated.enabled
    }

    fun addTime(time: String): Boolean {
        if (!ReminderRules.isValidTime(time)) return false
        val currentTimes = plan.value.times
        if (currentTimes.contains(time) || currentTimes.size >= plan.value.maxTimes) return false

        val newTimes = (currentTimes + time).sorted()
        prefs.updatePlan { it.copy(times = newTimes) }
        return true
    }

    fun removeTime(time: String) {
        val currentTimes = plan.value.times
        val newTimes = currentTimes.filter { it != time }
        prefs.updatePlan { it.copy(times = newTimes) }
    }

    fun triggerTestNotification() {
        val isArabic = Locale.getDefault().language == "ar"
        val (title, body) = ReminderRules.generateCopy(
            kind = ReminderKind.TEST,
            facts = ReminderFacts(cards = 3, minutes = 4, daysAway = 0),
            isArabic = isArabic
        )
        NotificationHelper.postNotification(
            context = getApplication(),
            title = title,
            body = body
        )
    }

    fun snooze(minutes: Int?, evening: Boolean) {
        val now = System.currentTimeMillis()
        val until = ReminderRules.calculateSnooze(now, plan.value.timezone, minutes, evening)
        prefs.updatePlan { it.copy(snoozeUntil = until) }
    }
}
