package com.sans.hydrotrack.reminders

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sans.hydrotrack.settings.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    context: Context,
    private val appScope: CoroutineScope,
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun applySettings(settings: UserSettings) {
        appScope.launch {
            if (!settings.remindersEnabled) {
                workManager.cancelUniqueWork(WORK_NAME)
                return@launch
            }

            val intervalHours = settings.reminderIntervalHours.coerceAtLeast(1)
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                intervalHours.toLong(),
                TimeUnit.HOURS,
                FLEX_MINUTES,
                TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }

    private companion object {
        private const val WORK_NAME = "hydration_reminder"
        private const val FLEX_MINUTES = 15L
    }
}
