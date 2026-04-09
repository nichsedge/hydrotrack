package com.sans.hydrotrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WaterBlueDark,
    secondary = WaterTealDark,
    tertiary = WaterAccent,
    primaryContainer = WaterBlueDark.copy(alpha = 0.2f),
    onPrimaryContainer = WaterOnSurfaceDark,
    secondaryContainer = WaterTealDark.copy(alpha = 0.2f),
    onSecondaryContainer = WaterOnSurfaceDark,
    background = WaterSurfaceDark,
    surface = WaterSurfaceDark,
    onPrimary = WaterOnSurfaceDark,
    onSecondary = WaterOnSurfaceDark,
    onTertiary = WaterOnSurfaceDark,
    onBackground = WaterOnSurfaceDark,
    onSurface = WaterOnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = WaterBlueLight,
    secondary = WaterTealLight,
    tertiary = WaterAccent,
    primaryContainer = WaterSecondary,
    onPrimaryContainer = WaterOnSurfaceLight,
    secondaryContainer = WaterSurfaceLight,
    onSecondaryContainer = WaterOnSurfaceLight,
    background = WaterSurfaceLight,
    surface = WaterSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = WaterOnSurfaceLight,
    onBackground = WaterOnSurfaceLight,
    onSurface = WaterOnSurfaceLight,
)

@Composable
fun HydroTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}