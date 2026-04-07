package com.sans.hydrotrack.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sans.hydrotrack.core.AppContainer
import com.sans.hydrotrack.settings.SettingsStore
import com.sans.hydrotrack.settings.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: SettingsStore,
) : ViewModel() {
    val settings: StateFlow<UserSettings> = settingsStore.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings(
            goalMl = 2000,
            glassSizeMl = 200,
            quickAdds = listOf(200),
            reminderIntervalHours = 2,
            remindersEnabled = true,
            trackerEnabled = true,
            useOunces = false,
        ),
    )

    fun setGoalMl(goalMl: Int) {
        viewModelScope.launch {
            settingsStore.setGoalMl(goalMl)
        }
    }

    fun setGlassSizeMl(glassSizeMl: Int) {
        viewModelScope.launch {
            settingsStore.setGlassSizeMl(glassSizeMl)
        }
    }

    fun setReminderIntervalHours(hours: Int) {
        viewModelScope.launch {
            settingsStore.setReminderIntervalHours(hours)
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setRemindersEnabled(enabled)
        }
    }

    fun setTrackerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setTrackerEnabled(enabled)
        }
    }

    fun setUseOunces(useOunces: Boolean) {
        viewModelScope.launch {
            settingsStore.setUseOunces(useOunces)
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingsViewModel(container.settingsStore)
                }
            }
    }
}
