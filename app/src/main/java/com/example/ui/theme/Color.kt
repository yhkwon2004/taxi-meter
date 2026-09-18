package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Accent palette. Each hue carries one meaning across the whole app:
//   amber  = 요금(돈)      green = 주행/거리      cyan = 시간/정보
//   red    = 정지/빈차     purple = 할증
// "Bright" is the on-dark readable tint, the base tone is for fills & borders.
// ---------------------------------------------------------------------------
val MeterAmber = Color(0xFFF59E0B)
val MeterAmberBright = Color(0xFFFCD34D)
val MeterAmberDeep = Color(0xFF3A2A06)
val MeterGreen = Color(0xFF10B981)
val MeterGreenBright = Color(0xFF4ADE9E)
val MeterGreenDeep = Color(0xFF07301F)
val MeterRed = Color(0xFFEF4444)
val MeterRedBright = Color(0xFFFB7A7A)
val MeterRedDeep = Color(0xFF341013)
val MeterCyan = Color(0xFF06B6D4)
val MeterCyanBright = Color(0xFF5CCFEA)
val MeterCyanDeep = Color(0xFF082A38)
val MeterPurple = Color(0xFF8B5CF6)
val MeterPurpleBright = Color(0xFFC4B5FD)
val MeterPurpleDeep = Color(0xFF221540)

// ---------------------------------------------------------------------------
// Automotive dark cockpit surfaces, ordered from furthest to nearest:
//   Background -> Card -> Surface (inner tile) -> Lcd (inset screen)
// ---------------------------------------------------------------------------
val CockpitBackground = Color(0xFF080B11)
val CockpitBackgroundTop = Color(0xFF101823)
val CockpitCard = Color(0xFF141B27)
val CockpitCardTop = Color(0xFF18212F)
val CockpitCardBorder = Color(0xFF223046)
val CockpitCardBorderSoft = Color(0xFF1A2434)
val CockpitSurface = Color(0xFF1A2434)
val CockpitLcdDark = Color(0xFF05080C)
val CockpitLcdBorder = Color(0xFF1D2A3C)
val CockpitLcdGlow = Color(0xFF0E1A16)
val CockpitTrack = Color(0xFF16202E)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF9BA9BC)
val TextMuted = Color(0xFF6B7C93)
val TextFaint = Color(0xFF44526A)

// ---------------------------------------------------------------------------
// Vertical gradients for the primary action buttons. A top-lit fill reads as a
// physical, pressable key on the dashboard rather than a flat coloured slab.
// ---------------------------------------------------------------------------
val GreenButtonGradient = listOf(Color(0xFF34D399), Color(0xFF0E9F71))
val RedButtonGradient = listOf(Color(0xFFF87171), Color(0xFFDC2626))
val CyanButtonGradient = listOf(Color(0xFF38BDF8), Color(0xFF0891B2))
val AmberButtonGradient = listOf(Color(0xFFFCD34D), Color(0xFFE08C08))

val OnGreenButton = Color(0xFF032417)
val OnRedButton = Color(0xFFFFFFFF)
val OnCyanButton = Color(0xFF02222F)
val OnAmberButton = Color(0xFF231703)
