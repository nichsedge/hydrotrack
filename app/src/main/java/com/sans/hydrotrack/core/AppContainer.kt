package com.sans.hydrotrack.core

import android.content.Context
import androidx.room.Room
import com.sans.hydrotrack.data.HydrationRepository
import com.sans.hydrotrack.data.HydroTrackDatabase
import com.sans.hydrotrack.data.RoomHydrationRepository
import com.sans.hydrotrack.reminders.ReminderScheduler
import com.sans.hydrotrack.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope

class AppContainer(
    context: Context,
    appScope: CoroutineScope,
) {
    private val appContext = context.applicationContext

    private val database: HydroTrackDatabase = Room.databaseBuilder(
        appContext,
        HydroTrackDatabase::class.java,
        "hydrotrack.db",
    ).build()

    val hydrationRepository: HydrationRepository =
        RoomHydrationRepository(database.waterEntryDao())

    val settingsStore: SettingsStore = SettingsStore(appContext)

    val reminderScheduler: ReminderScheduler = ReminderScheduler(
        appContext,
        appScope,
    )
}
