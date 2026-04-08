package com.sans.hydrotrack.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.sans.hydrotrack.HydroTrackApp
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as HydroTrackApp
        val settings = app.container.settingsStore.settingsFlow.first()
        val currentTotal = app.container.hydrationRepository.dayTotal(LocalDate.now()).first()
        
        if (currentTotal >= settings.goalMl) {
            return Result.success()
        }
        
        NotificationHelper.showHydrationReminder(
            applicationContext,
            settings.glassSizeMl,
            settings.useOunces
        )
        return Result.success()
    }
}
