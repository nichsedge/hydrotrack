package com.sans.hydrotrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sans.hydrotrack.ui.history.HistoryScreen
import com.sans.hydrotrack.ui.history.HistoryViewModel
import com.sans.hydrotrack.ui.home.HomeScreen
import com.sans.hydrotrack.ui.home.HomeViewModel
import com.sans.hydrotrack.ui.settings.SettingsScreen
import com.sans.hydrotrack.ui.settings.SettingsUiState
import com.sans.hydrotrack.ui.settings.SettingsViewModel
import com.sans.hydrotrack.ui.theme.HydroTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as HydroTrackApp).container
        setContent {
            HydroTrackTheme {
                var selectedTab by remember { mutableStateOf(AppTab.HOME) }
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(container),
                )
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.provideFactory(container),
                )
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(container),
                )

                RequestNotificationPermission()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = tab == selectedTab,
                                    onClick = { selectedTab = tab },
                                    label = { Text(text = tab.title) },
                                    icon = {
                                        Icon(
                                            imageVector = when (tab) {
                                                AppTab.HOME -> Icons.Default.Home
                                                AppTab.HISTORY -> Icons.AutoMirrored.Filled.List
                                                AppTab.SETTINGS -> Icons.Default.Settings
                                            },
                                            contentDescription = tab.title
                                        )
                                    },
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        AppTab.HOME -> {
                            val state by homeViewModel.uiState.collectAsStateWithLifecycle()
                            HomeScreen(
                                state = state,
                                onQuickAdd = homeViewModel::addQuick,
                                onRemoveQuickAdd = homeViewModel::removeQuickAdd,
                                onCustomAdd = homeViewModel::addCustom,
                                onDelete = homeViewModel::delete,
                                modifier = Modifier.padding(innerPadding),
                            )
                        }

                        AppTab.HISTORY -> {
                            val state by historyViewModel.uiState.collectAsStateWithLifecycle()
                            HistoryScreen(
                                state = state,
                                modifier = Modifier.padding(innerPadding),
                            )
                        }

                        AppTab.SETTINGS -> {
                            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
                            SettingsScreen(
                                state = SettingsUiState(
                                    goalMl = settings.goalMl,
                                    glassSizeMl = settings.glassSizeMl,
                                    reminderIntervalHours = settings.reminderIntervalHours,
                                    remindersEnabled = settings.remindersEnabled,
                                    trackerEnabled = settings.trackerEnabled,
                                    useOunces = settings.useOunces,
                                ),
                                onGoalChange = settingsViewModel::setGoalMl,
                                onGlassSizeChange = settingsViewModel::setGlassSizeMl,
                                onIntervalChange = settingsViewModel::setReminderIntervalHours,
                                onRemindersEnabled = settingsViewModel::setRemindersEnabled,
                                onTrackerEnabled = settingsViewModel::setTrackerEnabled,
                                onUseOunces = settingsViewModel::setUseOunces,
                                modifier = Modifier.padding(innerPadding),
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class AppTab(val title: String) {
    HOME("Home"),
    HISTORY("History"),
    SETTINGS("Settings"),
}

@Composable
private fun RequestNotificationPermission() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )

    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(granted) {
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
