package com.sans.hydrotrack.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sans.hydrotrack.MainActivity
import com.sans.hydrotrack.R
import com.sans.hydrotrack.reminders.NotificationActionReceiver.Companion.EXTRA_AMOUNT_ML
import com.sans.hydrotrack.reminders.NotificationActionReceiver.Companion.EXTRA_NOTIFICATION_ID
import com.sans.hydrotrack.util.UnitUtils

object NotificationHelper {
    private const val REMINDER_CHANNEL_ID = "hydration_reminder"
    private const val REMINDER_CHANNEL_NAME = "Hydration Reminders"
    private const val REMINDER_NOTIFICATION_ID = 1001

    private const val TRACKER_CHANNEL_ID = "hydration_tracker"
    private const val TRACKER_CHANNEL_NAME = "Hydration Tracker"
    private const val TRACKER_NOTIFICATION_ID = 1002

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        
        // Reminder Channel (Default importance)
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            REMINDER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminders to drink water"
        }
        manager.createNotificationChannel(reminderChannel)

        // Tracker Channel (Low importance for persistent notification)
        val trackerChannel = NotificationChannel(
            TRACKER_CHANNEL_ID,
            TRACKER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Always-on hydration progress"
            setShowBadge(false)
        }
        manager.createNotificationChannel(trackerChannel)
    }

    fun showHydrationReminder(context: Context, glassSizeMl: Int, useOunces: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to hydrate")
            .setContentText("Drink water to stay on track.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Add ${formatAmount(glassSizeMl, useOunces)}",
                createActionPendingIntent(context, glassSizeMl, REMINDER_NOTIFICATION_ID)
            )

        val notification = builder.build()
        NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
    }

    fun updateTrackerNotification(
        context: Context,
        currentMl: Int,
        goalMl: Int,
        glassSizeMl: Int,
        useOunces: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val currentDisplay = formatAmount(currentMl, useOunces)
        val goalDisplay = formatAmount(goalMl, useOunces)
        val progress = if (goalMl > 0) (currentMl * 100 / goalMl).coerceAtMost(100) else 0

        val builder = NotificationCompat.Builder(context, TRACKER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hydration: $currentDisplay / $goalDisplay ($progress%)")
            .setContentText("Stay hydrated! Tap to open or use quick actions.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Add ${formatAmount(glassSizeMl, useOunces)}",
                createActionPendingIntent(context, glassSizeMl, TRACKER_NOTIFICATION_ID)
            )

        NotificationManagerCompat.from(context).notify(TRACKER_NOTIFICATION_ID, builder.build())
    }

    fun cancelTrackerNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(TRACKER_NOTIFICATION_ID)
    }

    private fun formatAmount(amountMl: Int, useOunces: Boolean): String {
        return if (useOunces) {
            "${UnitUtils.mlToOunces(amountMl).toInt()}oz"
        } else {
            "${amountMl}ml"
        }
    }

    private fun createActionPendingIntent(context: Context, amountMl: Int, notificationId: Int): PendingIntent {
        val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(EXTRA_AMOUNT_ML, amountMl)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            amountMl + notificationId, // More unique request code
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
