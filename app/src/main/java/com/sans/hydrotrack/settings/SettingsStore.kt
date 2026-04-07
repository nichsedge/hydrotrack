package com.sans.hydrotrack.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserSettings(
    val goalMl: Int,
    val reminderIntervalHours: Int,
    val remindersEnabled: Boolean,
    val useOunces: Boolean,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hydrotrack_settings",
)

class SettingsStore(context: Context) {
    private val dataStore = context.dataStore

    val settingsFlow: Flow<UserSettings> = dataStore.data.map { prefs ->
        val goalMl = prefs[Keys.GOAL_ML] ?: Defaults.GOAL_ML
        val interval = prefs[Keys.REMINDER_INTERVAL_HOURS] ?: Defaults.REMINDER_INTERVAL_HOURS
        val enabled = prefs[Keys.REMINDERS_ENABLED] ?: Defaults.REMINDERS_ENABLED
        val useOunces = prefs[Keys.USE_OUNCES] ?: Defaults.USE_OUNCES
        UserSettings(
            goalMl = goalMl,
            reminderIntervalHours = interval,
            remindersEnabled = enabled,
            useOunces = useOunces,
        )
    }

    suspend fun setGoalMl(goalMl: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.GOAL_ML] = goalMl
        }
    }

    suspend fun setReminderIntervalHours(hours: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDER_INTERVAL_HOURS] = hours
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setUseOunces(useOunces: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.USE_OUNCES] = useOunces
        }
    }

    private object Keys {
        val GOAL_ML = intPreferencesKey("goal_ml")
        val REMINDER_INTERVAL_HOURS = intPreferencesKey("reminder_interval_hours")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val USE_OUNCES = booleanPreferencesKey("use_ounces")
    }

    private object Defaults {
        const val GOAL_ML = 2000
        const val REMINDER_INTERVAL_HOURS = 2
        const val REMINDERS_ENABLED = true
        const val USE_OUNCES = false
    }
}
