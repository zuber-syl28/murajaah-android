package com.sylergy.murajaah.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sylergy.murajaah.reminders.receiver.ReminderActionReceiver

/**
 * Handles notification channels, notification building with actionable buttons, and dispatching.
 */
object NotificationHelper {

    const val CHANNEL_ID = "murajaah_revision_reminders"
    const val NOTIFICATION_ID = 4040

    const val ACTION_START_REVISION = "com.sylergy.murajaah.ACTION_START_REVISION"
    const val ACTION_SNOOZE_15 = "com.sylergy.murajaah.ACTION_SNOOZE_15"
    const val ACTION_SNOOZE_60 = "com.sylergy.murajaah.ACTION_SNOOZE_60"
    const val ACTION_SNOOZE_EVENING = "com.sylergy.murajaah.ACTION_SNOOZE_EVENING"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Revision & Tasmīʿ Reminders"
            val descriptionText = "Supportive daily Qur'an revision reminders and companion listening nudges"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun postNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = NOTIFICATION_ID
    ) {
        ensureChannel(context)

        // Main Tap Intent: launches MainActivity
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ACTION_START_REVISION, true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intent: Start Now
        val startActionIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intent: Snooze 15 min
        val snooze15Intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_15
        }
        val snooze15Pending = PendingIntent.getBroadcast(
            context,
            2,
            snooze15Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intent: Snooze Evening
        val snoozeEveningIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_EVENING
        }
        val snoozeEveningPending = PendingIntent.getBroadcast(
            context,
            3,
            snoozeEveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(0xFF2E6F40.toInt()) // Forest Green
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_media_play, "Start now · ابدأ الآن", startActionIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "15 min · ١٥ دقيقة", snooze15Pending)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "This evening · المساء", snoozeEveningPending)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted yet
        }
    }

    fun cancelNotification(context: Context, notificationId: Int = NOTIFICATION_ID) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
