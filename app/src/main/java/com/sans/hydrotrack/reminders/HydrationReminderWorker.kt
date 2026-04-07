package com.sans.hydrotrack.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.showHydrationReminder(applicationContext)
        return Result.success()
    }
}
