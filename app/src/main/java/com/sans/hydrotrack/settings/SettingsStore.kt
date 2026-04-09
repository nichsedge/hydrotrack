package com.sans.hydrotrack.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserSettings(
    val goalMl: Int,
    val glassSizeMl: Int,
    val quickAdds: List<Int>,
    val reminderIntervalHours: Int,
    val remindersEnabled: Boolean,
    val trackerEnabled: Boolean,
    val useOunces: Boolean,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hydrotrack_settings",
)

class SettingsStore(context: Context) {
    private val dataStore = context.dataStore

    val settingsFlow: Flow<UserSettings> = dataStore.data.map { prefs ->
        val goalMl = prefs[Keys.GOAL_ML] ?: Defaults.GOAL_ML
        val glassSizeMl = prefs[Keys.GLASS_SIZE_ML] ?: Defaults.GLASS_SIZE_ML
        val quickAddsString = prefs[Keys.QUICK_ADDS] ?: Defaults.QUICK_ADDS
        val quickAdds = quickAddsString.split(",").filter { it.isNotEmpty() }.map { it.toInt() }

        val interval = prefs[Keys.REMINDER_INTERVAL_HOURS] ?: Defaults.REMINDER_INTERVAL_HOURS
        val enabled = prefs[Keys.REMINDERS_ENABLED] ?: Defaults.REMINDERS_ENABLED
        val trackerEnabled = prefs[Keys.TRACKER_ENABLED] ?: Defaults.TRACKER_ENABLED
        val useOunces = prefs[Keys.USE_OUNCES] ?: Defaults.USE_OUNCES
        UserSettings(
            goalMl = goalMl,
            glassSizeMl = glassSizeMl,
            quickAdds = quickAdds,
            reminderIntervalHours = interval,
            remindersEnabled = enabled,
            trackerEnabled = trackerEnabled,
            useOunces = useOunces,
        )
    }

    suspend fun setGoalMl(goalMl: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.GOAL_ML] = goalMl
        }
    }

    suspend fun setGlassSizeMl(glassSizeMl: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.GLASS_SIZE_ML] = glassSizeMl
        }
    }

    suspend fun addQuickAddAmount(amountMl: Int) {
        dataStore.edit { prefs ->
            val current = (prefs[Keys.QUICK_ADDS] ?: Defaults.QUICK_ADDS)
                .split(",")
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .toMutableList()

            if (!current.contains(amountMl)) {
                current.add(amountMl)
                val updated = current.distinct().sorted()
                prefs[Keys.QUICK_ADDS] = updated.joinToString(",")
            }
        }
    }

    suspend fun removeQuickAddAmount(amountMl: Int) {
        dataStore.edit { prefs ->
            val current = (prefs[Keys.QUICK_ADDS] ?: Defaults.QUICK_ADDS)
                .split(",")
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .toMutableList()

            if (current.size > 1) {
                current.removeIf { it == amountMl }
                val updated = current.distinct().sorted()
                prefs[Keys.QUICK_ADDS] = updated.joinToString(",")
            }
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

    suspend fun setTrackerEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.TRACKER_ENABLED] = enabled
        }
    }

    suspend fun setUseOunces(useOunces: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.USE_OUNCES] = useOunces
        }
    }

    private object Keys {
        val GOAL_ML = intPreferencesKey("goal_ml")
        val GLASS_SIZE_ML = intPreferencesKey("glass_size_ml")
        val QUICK_ADDS = stringPreferencesKey("quick_adds")
        val REMINDER_INTERVAL_HOURS = intPreferencesKey("reminder_interval_hours")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val TRACKER_ENABLED = booleanPreferencesKey("tracker_enabled")
        val USE_OUNCES = booleanPreferencesKey("use_ounces")
    }

    private object Defaults {
        const val GOAL_ML = 2000
        const val GLASS_SIZE_ML = 200
        const val QUICK_ADDS = "200"
        const val REMINDER_INTERVAL_HOURS = 2
        const val REMINDERS_ENABLED = true
        const val TRACKER_ENABLED = true
        const val USE_OUNCES = false
    }
}
