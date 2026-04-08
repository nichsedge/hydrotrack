package com.sans.hydrotrack.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sans.hydrotrack.core.AppContainer
import com.sans.hydrotrack.data.HydrationRepository
import com.sans.hydrotrack.data.WaterEntry
import com.sans.hydrotrack.settings.SettingsStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

data class DaySummary(
    val date: LocalDate,
    val totalMl: Int,
    val entries: List<WaterEntry>,
)

data class HistoryUiState(
    val summaries: List<DaySummary> = emptyList(),
    val useOunces: Boolean = false,
)

class HistoryViewModel(
    repository: HydrationRepository,
    settingsStore: SettingsStore,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val dateFlow = flow {
        while (true) {
            emit(LocalDate.now())
            delay(TimeUnit.MINUTES.toMillis(1))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocalDate.now())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = dateFlow.flatMapLatest { endDate ->
        val startDate = endDate.minusDays(6)
        combine(
            repository.history(startDate, endDate),
            settingsStore.settingsFlow,
        ) { entries, settings ->
            val grouped = entries.groupBy { entry ->
                Instant.ofEpochMilli(entry.timestamp).atZone(zoneId).toLocalDate()
            }

            val summaries = (0L..6L).map { offset ->
                val date = endDate.minusDays(offset)
                val dayEntries = grouped[date].orEmpty()
                DaySummary(
                    date = date,
                    totalMl = dayEntries.sumOf { it.amountMl },
                    entries = dayEntries,
                )
            }

            HistoryUiState(
                summaries = summaries,
                useOunces = settings.useOunces,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HistoryUiState(),
    )

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    HistoryViewModel(
                        repository = container.hydrationRepository,
                        settingsStore = container.settingsStore,
                    )
                }
            }
    }
}
