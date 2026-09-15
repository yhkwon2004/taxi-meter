package com.example.ui.theme

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

private val TaxiMeterDarkColorScheme = darkColorScheme(
    primary = MeterAmber,
    onPrimary = Color(0xFF1E1600),
    primaryContainer = Color(0xFF382900),
    onPrimaryContainer = MeterAmberBright,
    secondary = MeterGreen,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = MeterGreenBright,
    tertiary = MeterCyan,
    background = CockpitBackground,
    onBackground = TextPrimary,
    surface = CockpitCard,
    onSurface = TextPrimary,
    surfaceVariant = CockpitSurface,
    onSurfaceVariant = TextSecondary,
    outline = CockpitCardBorder,
    error = MeterRed,
    onError = Color.White
)

private val TaxiMeterLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = Color(0xFF0284C7),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to cockpit dark theme for authentic taxi meter feel
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) TaxiMeterDarkColorScheme else TaxiMeterLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

