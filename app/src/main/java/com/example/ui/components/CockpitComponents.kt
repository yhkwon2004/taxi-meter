package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitCardBorderSoft
import com.example.ui.theme.CockpitCardTop
import com.example.ui.theme.CockpitLcdBorder
import com.example.ui.theme.CockpitLcdDark
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitTrack
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.MeterType
import com.example.ui.theme.Space
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Reusable cockpit building blocks. Everything in the app is assembled from
 * these four surface levels so that borders, radii and padding never drift:
 *
 *   [CockpitPanel] -> raised dashboard panel (top-lit gradient + hairline edge)
 *   [CockpitTile]  -> tile nested inside a panel
 *   [LcdPanel]     -> inset screen, the only place a glow is allowed
 *   [StatusPill]   -> the roof-sign style badges
 */

@Composable
fun CockpitPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MeterShapes.card,
    borderColor: Color = CockpitCardBorder,
    contentPadding: PaddingValues = PaddingValues(Space.xl),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CockpitCardTop, CockpitCard)))
            .border(MeterSizes.hairline, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun CockpitTile(
    modifier: Modifier = Modifier,
    shape: Shape = MeterShapes.tile,
    containerColor: Color = CockpitSurface,
    borderColor: Color = CockpitCardBorderSoft,
    contentPadding: PaddingValues = PaddingValues(horizontal = Space.lg, vertical = Space.lg),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(MeterSizes.hairline, borderColor, shape)
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/**
 * Inset LCD screen. [glowColor] paints a soft radial bloom behind the digits,
 * the way a backlit meter screen leaks light onto its bezel.
 */
@Composable
fun LcdPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MeterShapes.lcd,
    glowColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = Space.xl, vertical = Space.xl),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(CockpitLcdDark)
            .then(
                if (glowColor == null) {
                    Modifier
                } else {
                    Modifier.drawBehind {
                        drawRect(
                            Brush.radialGradient(
                                colors = listOf(glowColor.copy(alpha = 0.18f), Color.Transparent),
                                center = Offset(size.width / 2f, size.height * 0.62f),
                                radius = size.width * 0.62f
                            )
                        )
                    }
                }
            )
            .border(MeterSizes.hairline, CockpitLcdBorder, shape)
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/** Small engraved caption above a group of controls. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextMuted,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MeterType.caption, color = color)
        trailing?.invoke()
    }
}

/**
 * Roof-sign style status badge. [pulsing] makes the indicator dot breathe, which
 * is what tells the driver at a glance that the meter is actually running.
 */
@Composable
fun StatusPill(
    label: String,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false,
    dense: Boolean = false,
    icon: ImageVector? = null
) {
    val dotAlpha = if (pulsing) rememberPulseAlpha() else 1f

    Row(
        modifier = modifier
            .clip(MeterShapes.pill)
            .background(containerColor)
            .border(MeterSizes.hairline, accentColor.copy(alpha = 0.55f), MeterShapes.pill)
            .padding(
                horizontal = if (dense) Space.md else Space.lg,
                vertical = if (dense) Space.xs else Space.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(if (dense) 11.dp else 13.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (dense) 6.dp else MeterSizes.statusDot)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = dotAlpha))
            )
        }
        Spacer(modifier = Modifier.width(Space.sm))
        Text(
            text = label,
            style = if (dense) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            color = accentColor,
            maxLines = 1
        )
    }
}

/** Slowly breathing alpha, shared by every "live" indicator in the app. */
@Composable
fun rememberPulseAlpha(durationMillis: Int = 900): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    return alpha
}

/**
 * Light band sweeping across a track, the classic Korean meter "running" cue.
 * The band is sized from the measured width at draw time, so it stays centred
 * on any screen instead of running off the end of the track.
 */
@Composable
fun SweepBar(
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    trackColor: Color = CockpitTrack,
    height: androidx.compose.ui.unit.Dp = 6.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MeterShapes.pill)
            .background(trackColor)
            .drawBehind {
                if (!active) return@drawBehind
                val band = size.width * 0.42f
                val startX = -band + (size.width + band) * progress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, accentColor, Color.Transparent),
                        start = Offset(startX, 0f),
                        end = Offset(startX + band, 0f)
                    )
                )
            }
    )
}

/**
 * Segmented bar gauge. Reads like the rev-counter LEDs on a dashboard: the
 * first [filled] of [segments] bars light up in [accentColor].
 */
@Composable
fun SegmentedGauge(
    filled: Int,
    segments: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(segments) { index ->
            val isOn = index < filled
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isOn) {
                            accentColor.copy(alpha = 0.45f + 0.55f * (index + 1f) / segments)
                        } else {
                            CockpitTrack
                        }
                    )
            )
        }
    }
}

/**
 * Selectable cockpit switch tile: stacked icon + label so that Korean labels
 * never get clipped the way they do in a horizontal chip squeezed to a third
 * of the screen.
 */
@Composable
fun ToggleTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedLabelColor: Color = accentColor
) {
    val border = if (selected) accentColor.copy(alpha = 0.75f) else CockpitCardBorderSoft
    val container = if (selected) accentColor.copy(alpha = 0.16f) else CockpitSurface
    val contentColor = if (selected) selectedLabelColor else TextSecondary

    Column(
        modifier = modifier
            .clip(MeterShapes.tile)
            .background(container)
            .border(MeterSizes.hairline, border, MeterShapes.tile)
            .selectable(
                selected = selected,
                role = Role.Switch,
                onClick = onClick
            )
            .padding(horizontal = Space.md, vertical = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/** Compact selectable text button used for speed / passenger presets. */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = if (selected) accentColor.copy(alpha = 0.75f) else CockpitCardBorderSoft
    val container = if (selected) accentColor.copy(alpha = 0.18f) else CockpitSurface
    val contentColor = if (selected) accentColor else TextSecondary

    Box(
        modifier = modifier
            .clip(MeterShapes.chip)
            .background(container)
            .border(MeterSizes.hairline, border, MeterShapes.chip)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/** Perforated tear line for the receipt ticket. */
@Composable
fun PerforatedDivider(
    modifier: Modifier = Modifier,
    color: Color = CockpitCardBorder,
    dashWidth: androidx.compose.ui.unit.Dp = 5.dp,
    gapWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                val dash = dashWidth.toPx()
                val gap = gapWidth.toPx()
                var x = 0f
                while (x < size.width) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x, 0f),
                        size = androidx.compose.ui.geometry.Size(
                            width = minOf(dash, size.width - x),
                            height = size.height
                        )
                    )
                    x += dash + gap
                }
            }
    )
}

/**
 * Shared shell for the three full-screen dialogs, so their headers, widths and
 * corner radii match instead of each dialog inventing its own.
 */
@Composable
fun MeterDialogShell(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    closeContentDescription: String = "닫기",
    closeButtonModifier: Modifier = Modifier,
    headerActions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    // The dialog window spans the full screen width, so the card has to be
    // centred explicitly — otherwise a 94%-wide card sits flush against the
    // left edge.
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(MeterShapes.dialog)
                .background(Brush.verticalGradient(listOf(CockpitCardTop, CockpitCard)))
                .border(MeterSizes.hairline, CockpitCardBorder, MeterShapes.dialog)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Space.xxl, top = Space.xl, end = Space.lg, bottom = Space.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MeterShapes.chip)
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(
                            MeterSizes.hairline,
                            accentColor.copy(alpha = 0.35f),
                            MeterShapes.chip
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Space.lg)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                headerActions()

                IconButton(onClick = onDismiss, modifier = closeButtonModifier) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeContentDescription,
                        tint = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MeterSizes.hairline)
                    .background(CockpitCardBorderSoft)
            )

            content()
        }
    }
}

/** Empty-state block used when a list has nothing to show. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = Space.xxl)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CockpitSurface)
                    .border(MeterSizes.hairline, CockpitCardBorderSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(Space.xl))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Space.sm))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
