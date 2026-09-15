package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The meter is designed to be read from the driver's seat, so it ships a single
 * always-dark "cockpit" scheme instead of following the system light/dark
 * setting: a bright panel washes out the amber LCD at night.
 */
private val TaxiMeterDarkColorScheme = darkColorScheme(
    primary = MeterAmber,
    onPrimary = Color(0xFF1E1600),
    primaryContainer = MeterAmberDeep,
    onPrimaryContainer = MeterAmberBright,
    secondary = MeterGreen,
    onSecondary = Color(0xFF003822),
    secondaryContainer = MeterGreenDeep,
    onSecondaryContainer = MeterGreenBright,
    tertiary = MeterCyan,
    onTertiary = Color(0xFF00293B),
    tertiaryContainer = MeterCyanDeep,
    onTertiaryContainer = MeterCyanBright,
    background = CockpitBackground,
    onBackground = TextPrimary,
    surface = CockpitCard,
    onSurface = TextPrimary,
    surfaceVariant = CockpitSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = CockpitSurface,
    outline = CockpitCardBorder,
    outlineVariant = CockpitCardBorderSoft,
    error = MeterRed,
    onError = Color.White,
    errorContainer = MeterRedDeep,
    onErrorContainer = MeterRedBright,
    scrim = Color(0xCC03060A)
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TaxiMeterDarkColorScheme,
        typography = Typography,
        content = content
    )
}
