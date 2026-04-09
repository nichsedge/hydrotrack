package com.sans.hydrotrack

import android.app.Application
import com.sans.hydrotrack.core.AppContainer
import com.sans.hydrotrack.reminders.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class HydroTrackApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, appScope)
        NotificationHelper.createChannels(this)

        // Handle settings changes (Reminders)
        appScope.launch {
            container.settingsStore.settingsFlow.collect { settings ->
                container.reminderScheduler.applySettings(settings)
            }
        }

        // Handle tracker updates
        appScope.launch {
            val dateFlow = flow {
                while (true) {
                    emit(LocalDate.now())
                    delay(TimeUnit.MINUTES.toMillis(1))
                }
            }

            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            dateFlow.flatMapLatest { date ->
                combine(
                    container.settingsStore.settingsFlow,
                    container.hydrationRepository.dayTotal(date)
                ) { settings, currentTotal ->
                    if (settings.trackerEnabled) {
                        NotificationHelper.updateTrackerNotification(
                            context = this@HydroTrackApp,
                            currentMl = currentTotal,
                            goalMl = settings.goalMl,
                            glassSizeMl = settings.glassSizeMl,
                            useOunces = settings.useOunces
                        )
                    } else {
                        NotificationHelper.cancelTrackerNotification(this@HydroTrackApp)
                    }
                }
            }.collect {}
        }
    }
}
