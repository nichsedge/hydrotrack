package com.sans.hydrotrack.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
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
import com.sans.hydrotrack.data.WaterEntry
import com.sans.hydrotrack.util.UnitUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    state: HomeUiState,
    onQuickAdd: (Int) -> Unit,
    onCustomAdd: (Int) -> Unit,
    onDelete: (WaterEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var customInput by remember { mutableStateOf("") }
    val unitLabel = if (state.useOunces) "oz" else "ml"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        ProgressCard(
            totalMl = state.totalMl,
            goalMl = state.goalMl,
            progress = state.progress,
            useOunces = state.useOunces,
        )

        Spacer(Modifier.height(16.dp))

        Text(text = "Quick add")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(100, 250, 500).forEach { amount ->
                Button(onClick = { onQuickAdd(amount) }) {
                    Text(text = "$amount ml")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(text = "Custom amount")
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it.filter(Char::isDigit) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(text = unitLabel) },
                modifier = Modifier.weight(1f),
            )
            Button(onClick = {
                val amount = customInput.toIntOrNull() ?: 0
                val mlAmount = if (state.useOunces) UnitUtils.ouncesToMl(amount.toFloat()) else amount
                if (mlAmount > 0) {
                    onCustomAdd(mlAmount)
                    customInput = ""
                }
            }) {
                Text(text = "Add")
            }
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
        Text(text = "Today")
        Spacer(Modifier.height(8.dp))
        EntryList(
            entries = state.entries,
            useOunces = state.useOunces,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun ProgressCard(
    totalMl: Int,
    goalMl: Int,
    progress: Float,
    useOunces: Boolean,
) {
    val totalDisplay = if (useOunces) UnitUtils.mlToOunces(totalMl).toInt() else totalMl
    val goalDisplay = if (useOunces) UnitUtils.mlToOunces(goalMl).toInt() else goalMl
    val unitLabel = if (useOunces) "oz" else "ml"

    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = "Today's goal")
                Spacer(Modifier.height(4.dp))
                Text(text = "$totalDisplay / $goalDisplay $unitLabel")
            }
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
            )
        }
    }
}

@Composable
private fun EntryList(
    entries: List<WaterEntry>,
    useOunces: Boolean,
    onDelete: (WaterEntry) -> Unit,
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }
    val zoneId = remember { ZoneId.systemDefault() }
    val unitLabel = if (useOunces) "oz" else "ml"

    if (entries.isEmpty()) {
        Text(text = "No entries yet.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.id }) { entry ->
            val time = Instant.ofEpochMilli(entry.timestamp).atZone(zoneId).toLocalTime()
            val amount = if (useOunces) UnitUtils.mlToOunces(entry.amountMl).toInt() else entry.amountMl
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(text = formatter.format(time))
                        Text(text = "$amount $unitLabel")
                    }
                    TextButton(onClick = { onDelete(entry) }) {
                        Text(text = "Delete")
                    }
                }
            }
        }
    }
}
