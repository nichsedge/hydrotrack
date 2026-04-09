package com.sans.hydrotrack.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sans.hydrotrack.data.WaterEntry
import com.sans.hydrotrack.ui.components.WaterProgressIndicator
import com.sans.hydrotrack.util.UnitUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onQuickAdd: (Int) -> Unit,
    onRemoveQuickAdd: (Int) -> Unit,
    onCustomAdd: (Int) -> Unit,
    onDelete: (WaterEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var customInput by remember { mutableStateOf("") }
    var quickAddToDelete by remember { mutableStateOf<Int?>(null) }
    val unitLabel = if (state.useOunces) "oz" else "ml"

    if (quickAddToDelete != null) {
        val amount = quickAddToDelete!!
        val amountDisplay = if (state.useOunces) UnitUtils.mlToOunces(amount).toInt() else amount
        AlertDialog(
            onDismissRequest = { quickAddToDelete = null },
            title = { Text("Delete Quick Add") },
            text = { Text("Are you sure you want to remove $amountDisplay $unitLabel from your quick adds?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveQuickAdd(amount)
                        quickAddToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { quickAddToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Stay Hydrated",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            ProgressCard(
                totalMl = state.totalMl,
                goalMl = state.goalMl,
                progress = state.progress,
                useOunces = state.useOunces,
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Quick add",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.quickAdds.forEach { amountMl ->
                    val amountDisplay =
                        if (state.useOunces) UnitUtils.mlToOunces(amountMl).toInt() else amountMl
                    QuickAddButton(
                        amount = amountDisplay,
                        unit = unitLabel,
                        onClick = { onQuickAdd(amountMl) },
                        onLongClick = {
                            if (state.quickAdds.size > 1) {
                                quickAddToDelete = amountMl
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Custom amount",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it.filter(Char::isDigit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(text = unitLabel) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val amount = customInput.toIntOrNull() ?: 0
                        val mlAmount =
                            if (state.useOunces) UnitUtils.ouncesToMl(amount.toFloat()) else amount
                        if (mlAmount > 0) {
                            onCustomAdd(mlAmount)
                            customInput = ""
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Add")
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Today's Log",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (state.entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No entries yet. Start drinking!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(state.entries, key = { it.id }) { entry ->
                LogEntryItem(
                    entry = entry,
                    useOunces = state.useOunces,
                    onDelete = onDelete
                )
            }
        }
    }
}

// Helper for log entries to keep HomeScreen clean
@Composable
private fun LogEntryItem(
    entry: WaterEntry,
    useOunces: Boolean,
    onDelete: (WaterEntry) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val zoneId = remember { ZoneId.systemDefault() }
    val unitLabel = if (useOunces) "oz" else "ml"
    val time = Instant.ofEpochMilli(entry.timestamp).atZone(zoneId).toLocalTime()
    val amount = if (useOunces) UnitUtils.mlToOunces(entry.amountMl).toInt() else entry.amountMl

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💧", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "$amount $unitLabel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatter.format(time),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = { onDelete(entry) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
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
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Today's goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalDisplay",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = " / $goalDisplay $unitLabel",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            WaterProgressIndicator(
                progress = progress,
                size = 100.dp,
                strokeWidth = 10.dp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickAddButton(
    amount: Int,
    unit: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$amount",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntryList(
    entries: List<WaterEntry>,
    useOunces: Boolean,
    onDelete: (WaterEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }
    val zoneId = remember { ZoneId.systemDefault() }
    val unitLabel = if (useOunces) "oz" else "ml"

    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No entries yet. Start drinking!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            val time = Instant.ofEpochMilli(entry.timestamp).atZone(zoneId).toLocalTime()
            val amount =
                if (useOunces) UnitUtils.mlToOunces(entry.amountMl).toInt() else entry.amountMl

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💧",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "$amount $unitLabel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatter.format(time),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDelete(entry) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}
