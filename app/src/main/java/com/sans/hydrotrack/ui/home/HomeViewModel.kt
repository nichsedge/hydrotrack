package com.sans.hydrotrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sans.hydrotrack.core.AppContainer
import com.sans.hydrotrack.data.HydrationRepository
import com.sans.hydrotrack.data.WaterEntry
import com.sans.hydrotrack.settings.SettingsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val totalMl: Int = 0,
    val goalMl: Int = 2000,
    val quickAdds: List<Int> = listOf(200),
    val progress: Float = 0f,
    val entries: List<WaterEntry> = emptyList(),
    val useOunces: Boolean = false,
)

class HomeViewModel(
    private val repository: HydrationRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {
    private val dateFlow = flow {
        while (true) {
            emit(LocalDate.now())
            delay(TimeUnit.MINUTES.toMillis(1)) // Check every minute
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocalDate.now())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = dateFlow.flatMapLatest { date ->
        combine(
            repository.dayEntries(date),
            repository.dayTotal(date),
            settingsStore.settingsFlow,
        ) { entries, total, settings ->
            val goal = settings.goalMl
            val progress = if (goal > 0) total.toFloat() / goal.toFloat() else 0f
            HomeUiState(
                totalMl = total,
                goalMl = goal,
                quickAdds = settings.quickAdds,
                progress = progress.coerceIn(0f, 1f),
                entries = entries,
                useOunces = settings.useOunces,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(),
    )

    fun addQuick(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            repository.addEntry(amountMl, source = "quick_button")
        }
    }

    fun addCustom(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            repository.addEntry(amountMl, source = "custom")
            settingsStore.addQuickAddAmount(amountMl)
        }
    }

    fun removeQuickAdd(amountMl: Int) {
        viewModelScope.launch {
            settingsStore.removeQuickAddAmount(amountMl)
        }
    }

    fun delete(entry: WaterEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    HomeViewModel(
                        repository = container.hydrationRepository,
                        settingsStore = container.settingsStore,
                    )
                }
            }
    }
}
