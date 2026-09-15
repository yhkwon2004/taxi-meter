package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography =
  Typography(
    titleLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
      ),
    titleSmall =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
      ),
    bodySmall =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
      ),
    labelMedium =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.4.sp,
      ),
  )

/**
 * Meter-specific styles that sit outside the Material scale: the LCD readouts
 * and the small capitalised captions engraved on the cockpit panels.
 */
object MeterType {
  /** The big fare number on the main LCD. */
  val lcdHero =
    TextStyle(
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Black,
      fontSize = 54.sp,
      letterSpacing = (-2).sp,
    )

  /** Secondary LCD number: per-person split, receipt total. */
  val lcdLarge =
    TextStyle(
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Black,
      fontSize = 24.sp,
      letterSpacing = (-0.8).sp,
    )

  /** Metric readouts (distance / time / speed). */
  val lcdSmall =
    TextStyle(
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      letterSpacing = (-0.5).sp,
    )

  /** Monospace amounts inside receipt rows, so decimal columns line up. */
  val amount =
    TextStyle(
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Medium,
      fontSize = 13.sp,
    )

  /** Engraved panel caption, e.g. "FARE / 택시 요금". */
  val caption =
    TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      lineHeight = 13.sp,
      letterSpacing = 1.4.sp,
    )
}
