package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Spacing scale. Every gap in the UI is one of these values so that vertical
 * rhythm stays consistent between the meter, the control cards and the dialogs.
 */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 20.dp
    val xxxl = 28.dp
}

/**
 * Corner radii, ordered by how "near" the surface is to the viewer.
 * Outer containers are rounder than the tiles nested inside them.
 */
object MeterShapes {
    val dialog = RoundedCornerShape(28.dp)
    val hero = RoundedCornerShape(24.dp)
    val card = RoundedCornerShape(20.dp)
    val tile = RoundedCornerShape(14.dp)
    val lcd = RoundedCornerShape(16.dp)
    val chip = RoundedCornerShape(10.dp)
    val button = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(50)
}

/** Fixed heights for touch targets, kept above the 48dp accessibility minimum. */
object MeterSizes {
    val primaryButton = 62.dp
    val secondaryButton = 54.dp
    val tertiaryButton = 48.dp
    val statusDot = 8.dp
    val hairline = 1.dp
}
