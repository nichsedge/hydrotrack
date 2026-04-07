package com.sans.hydrotrack.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sans.hydrotrack.util.UnitUtils
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val unitLabel = if (state.useOunces) "oz" else "ml"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Last 7 days")
        Spacer(Modifier.height(12.dp))
        if (state.summaries.isEmpty()) {
            Text(text = "No history yet.")
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.summaries, key = { it.date.toString() }) { summary ->
                val total = if (state.useOunces) UnitUtils.mlToOunces(summary.totalMl).toInt() else summary.totalMl
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = dateFormatter.format(summary.date))
                        Text(text = "$total $unitLabel")
                        if (summary.entries.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Divider()
                            Spacer(Modifier.height(8.dp))
                            summary.entries.forEach { entry ->
                                val amount = if (state.useOunces) UnitUtils.mlToOunces(entry.amountMl).toInt() else entry.amountMl
                                Text(text = "- $amount $unitLabel")
                            }
                        }
                    }
                }
            }
        }
    }
}
