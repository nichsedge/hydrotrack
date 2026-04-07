package com.sans.hydrotrack.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sans.hydrotrack.util.UnitUtils

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onGoalChange: (Int) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onRemindersEnabled: (Boolean) -> Unit,
    onUseOunces: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val goalDisplay = if (state.useOunces) {
        UnitUtils.mlToOunces(state.goalMl).toInt()
    } else {
        state.goalMl
    }
    var goalInput by remember(goalDisplay) { mutableStateOf(goalDisplay.toString()) }
    var intervalInput by remember(state.reminderIntervalHours) {
        mutableStateOf(state.reminderIntervalHours.toString())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Settings")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Use ounces")
            Switch(
                checked = state.useOunces,
                onCheckedChange = onUseOunces,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Reminders enabled")
            Switch(
                checked = state.remindersEnabled,
                onCheckedChange = onRemindersEnabled,
            )
        }

        OutlinedTextField(
            value = goalInput,
            onValueChange = {
                goalInput = it.filter(Char::isDigit)
                goalInput.toIntOrNull()?.let { value ->
                    if (value > 0) {
                        val mlValue = if (state.useOunces) {
                            UnitUtils.ouncesToMl(value.toFloat())
                        } else {
                            value
                        }
                        onGoalChange(mlValue)
                    }
                }
            },
            label = { Text(text = if (state.useOunces) "Daily goal (oz)" else "Daily goal (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = intervalInput,
            onValueChange = {
                intervalInput = it.filter(Char::isDigit)
                intervalInput.toIntOrNull()?.let { value ->
                    if (value > 0) onIntervalChange(value)
                }
            },
            label = { Text(text = "Reminder interval (hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = state.remindersEnabled,
        )

        Spacer(Modifier.height(8.dp))
        Text(text = "Changes save automatically.")
    }
}

data class SettingsUiState(
    val goalMl: Int,
    val reminderIntervalHours: Int,
    val remindersEnabled: Boolean,
    val useOunces: Boolean,
)
