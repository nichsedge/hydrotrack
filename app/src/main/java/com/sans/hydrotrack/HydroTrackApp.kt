package com.sans.hydrotrack

import android.app.Application
import com.sans.hydrotrack.core.AppContainer
import com.sans.hydrotrack.reminders.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HydroTrackApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, appScope)
        NotificationHelper.createChannel(this)
        appScope.launch {
            container.settingsStore.settingsFlow.collect { settings ->
                container.reminderScheduler.applySettings(settings)
            }
        }
    }
}
