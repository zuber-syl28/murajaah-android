package com.sylergy.murajaah.reminders.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.reminders.NotificationHelper
import com.sylergy.murajaah.reminders.ReminderPreferences
import com.sylergy.murajaah.reminders.model.CompanionDueFact
import com.sylergy.murajaah.reminders.model.ReminderFacts
import com.sylergy.murajaah.reminders.model.ReminderKind
import com.sylergy.murajaah.reminders.model.ReminderRules
import com.sylergy.murajaah.reminders.model.ReminderTrigger
import com.sylergy.murajaah.scheduler.DateUtils
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * WorkManager worker that periodically checks if a revision reminder is due.
 */
class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = ReminderPreferences(applicationContext)
        val plan = prefs.loadPlan()
        val now = System.currentTimeMillis()

        val trigger = ReminderRules.dueReminder(plan, now) ?: return Result.success()

        val db = MurajaahDatabase.getInstance(applicationContext)
        val today = DateUtils.today()
        val dueCards = db.cardDao().getDueCards(today)
        val friends = db.friendDao().getAllFriends()
        val dueCompanions = friends
            .filter { it.youReviewStatus == "active" && it.youReviewDueToday > 0 }
            .map { CompanionDueFact(it.name, it.youReviewDueToday) }

        if (dueCards.isEmpty() && dueCompanions.isEmpty()) {
            // Nothing to revise or hear: keep quiet
            return Result.success()
        }

        val kind = when (trigger) {
            is ReminderTrigger.Snoozed -> ReminderKind.SNOOZED
            is ReminderTrigger.Due -> {
                if (dueCards.isEmpty() && dueCompanions.isNotEmpty()) {
                    ReminderKind.LISTEN
                } else {
                    ReminderKind.DUE
                }
            }
        }

        val estimatedMinutes = max(2, (dueCards.size * 0.75).toInt())
        val facts = ReminderFacts(
            cards = dueCards.size,
            minutes = estimatedMinutes,
            daysAway = 0,
            listening = dueCompanions
        )

        val isArabic = Locale.getDefault().language == "ar"
        val (title, body) = ReminderRules.generateCopy(kind, facts, isArabic)

        NotificationHelper.postNotification(
            context = applicationContext,
            title = title,
            body = body
        )

        val key = when (trigger) {
            is ReminderTrigger.Due -> trigger.key
            is ReminderTrigger.Snoozed -> trigger.key
        }

        prefs.updatePlan {
            it.copy(
                lastSentKey = key,
                lastSentAt = now,
                snoozeUntil = null // Clear expired snooze
            )
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "murajaah_reminder_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
