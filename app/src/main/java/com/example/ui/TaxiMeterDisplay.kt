package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentMeterState
import com.example.model.MeterStatus
import com.example.ui.components.CockpitPanel
import com.example.ui.components.CockpitTile
import com.example.ui.components.LcdPanel
import com.example.ui.components.SegmentedGauge
import com.example.ui.components.StatusPill
import com.example.ui.components.SweepBar
import com.example.ui.theme.CockpitCardBorderSoft
import com.example.ui.theme.CockpitLcdDark
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterAmberDeep
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterCyanDeep
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MeterGreenDeep
import com.example.ui.theme.MeterPurple
import com.example.ui.theme.MeterPurpleBright
import com.example.ui.theme.MeterPurpleDeep
import com.example.ui.theme.MeterRedBright
import com.example.ui.theme.MeterRedDeep
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.MeterType
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Space
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper
import java.text.NumberFormat
import java.util.Locale

/** Number of LEDs in the speed gauge; each one stands for 10 km/h. */
private const val SPEED_GAUGE_SEGMENTS = 12
private const val SPEED_PER_SEGMENT = 10

/**
 * The main cockpit read-out: status sign, the amber fare LCD, the trip counters
 * and a live breakdown of what the passenger is being charged for.
 */
@Composable
fun TaxiMeterDisplay(
    meterState: CurrentMeterState,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val isDriving = meterState.status == MeterStatus.DRIVING

    // The sweep speeds up with the car. Bucketed on purpose: a duration derived
    // straight from the GPS speed would restart the animation on every fix.
    val sweepDuration = when {
        meterState.currentSpeedKmh > 60 -> 600
        meterState.currentSpeedKmh > 20 -> 1000
        else -> 1800
    }
    val infiniteTransition = rememberInfiniteTransition(label = "meter_sweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = sweepDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meter_sweep_progress"
    )

    // Roll the fare up instead of snapping, so a jump of several hundred won is
    // visible to the passenger rather than happening between blinks.
    val animatedFare by animateIntAsState(
        targetValue = meterState.totalFare,
        animationSpec = tween(durationMillis = 420),
        label = "fare_roll"
    )

    CockpitPanel(
        modifier = modifier
            .fillMaxWidth()
            .testTag("taxi_meter_display"),
        shape = MeterShapes.hero,
        contentPadding = PaddingValues(Space.lg)
    ) {
        // --- Roof sign row: current state on the left, active surcharges right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Space.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MeterStatusBadge(status = meterState.status)
            SurchargeBadges(meterState = meterState)
        }

        // --- Fare LCD
        LcdPanel(
            modifier = Modifier.fillMaxWidth(),
            glowColor = MeterAmberBright,
            contentPadding = PaddingValues(horizontal = Space.xl, vertical = Space.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "FARE · 총 요금", style = MeterType.caption, color = TextMuted)
                Text(
                    text = meterState.config.preset.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MeterAmberBright.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(Space.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₩",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = MeterAmber,
                    modifier = Modifier.padding(bottom = 7.dp, end = Space.xs)
                )
                Text(
                    text = numberFormat.format(animatedFare),
                    style = MeterType.lcdHero,
                    color = MeterAmberBright
                )
                Text(
                    text = "원",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp, start = Space.xs)
                )
            }

            Spacer(modifier = Modifier.height(Space.md))

            SweepBar(
                progress = sweepProgress,
                accentColor = MeterAmberBright,
                active = isDriving
            )
        }

        // --- Trip counters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Space.lg),
            horizontalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Route,
                label = "주행 거리",
                value = ShareReceiptHelper.formatDistance(meterState.distanceMeters),
                accentColor = MeterGreenBright
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                label = "운행 시간",
                value = formatTimerDigits(meterState.elapsedSeconds),
                accentColor = MeterCyanBright
            )
        }

        // --- Speed: continuous, so it gets a gauge rather than a counter tile
        SpeedGaugeTile(
            modifier = Modifier.padding(top = Space.md),
            speedKmh = meterState.currentSpeedKmh,
            isSlowFareApplied = isDriving &&
                meterState.currentSpeedKmh < meterState.config.slowSpeedKmhThreshold
        )

        FareBreakdown(
            meterState = meterState,
            modifier = Modifier.padding(top = Space.lg)
        )
    }
}

@Composable
private fun MeterStatusBadge(status: MeterStatus) {
    val (label, accent, container) = when (status) {
        MeterStatus.VACANT -> Triple("빈 차", MeterRedBright, MeterRedDeep)
        MeterStatus.DRIVING -> Triple("주 행", MeterGreenBright, MeterGreenDeep)
        MeterStatus.PAUSED -> Triple("정차 / 대기", MeterAmberBright, MeterAmberDeep)
        MeterStatus.PAYMENT -> Triple("지불 / 정산", MeterCyanBright, MeterCyanDeep)
    }

    StatusPill(
        label = label,
        accentColor = accent,
        containerColor = container,
        pulsing = status == MeterStatus.DRIVING
    )
}

/**
 * Active surcharges. Always renders something — a muted "일반 요금" pill when
 * nothing is applied — so the row keeps its balance instead of collapsing.
 */
@Composable
private fun SurchargeBadges(meterState: CurrentMeterState) {
    val hasAny = meterState.isNightSurcharge ||
        meterState.isOutOfCitySurcharge ||
        meterState.tollFee > 0

    if (!hasAny) {
        StatusPill(
            label = "일반 요금",
            accentColor = TextMuted,
            containerColor = Color.Transparent,
            dense = true
        )
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        if (meterState.isNightSurcharge) {
            StatusPill(
                label = "심야 ${meterState.nightSurchargeRate}%",
                accentColor = MeterPurpleBright,
                containerColor = MeterPurpleDeep,
                dense = true,
                icon = Icons.Default.Bedtime
            )
        }
        if (meterState.isOutOfCitySurcharge) {
            StatusPill(
                label = "시외 ${meterState.config.outOfCitySurchargePercent}%",
                accentColor = MeterCyanBright,
                containerColor = MeterCyanDeep,
                dense = true,
                icon = Icons.Default.LocationOn
            )
        }
        if (meterState.tollFee > 0) {
            StatusPill(
                label = "+${ShareReceiptHelper.formatWon(meterState.tollFee)}",
                accentColor = MeterAmberBright,
                containerColor = MeterAmberDeep,
                dense = true,
                icon = Icons.Default.Payments
            )
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    CockpitTile(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(Space.xs))
            Text(text = label, style = MeterType.caption, color = TextMuted)
        }
        Spacer(modifier = Modifier.height(Space.sm))
        Text(
            text = value,
            style = MeterType.lcdSmall,
            fontSize = 19.sp,
            color = accentColor,
            maxLines = 1
        )
    }
}

@Composable
private fun SpeedGaugeTile(
    speedKmh: Double,
    isSlowFareApplied: Boolean,
    modifier: Modifier = Modifier
) {
    val filled = (speedKmh / SPEED_PER_SEGMENT).toInt().coerceIn(0, SPEED_GAUGE_SEGMENTS)
    val accent = if (isSlowFareApplied) MeterAmberBright else MeterGreenBright

    CockpitTile(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(Space.xs))
                Text(text = "현재 속도", style = MeterType.caption, color = TextMuted)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSlowFareApplied) {
                    // Below the slow-speed threshold the meter switches to
                    // charging by time; say so rather than leaving the
                    // passenger to wonder why the fare still climbs.
                    StatusPill(
                        label = "지체요금 적용",
                        accentColor = MeterAmberBright,
                        containerColor = MeterAmberDeep,
                        dense = true,
                        icon = Icons.Default.Timer
                    )
                    Spacer(modifier = Modifier.width(Space.md))
                }
                Text(
                    text = speedKmh.toInt().toString(),
                    style = MeterType.lcdSmall,
                    color = if (isSlowFareApplied) MeterAmberBright else TextPrimary
                )
                Text(
                    text = " km/h",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(Space.md))

        SegmentedGauge(
            filled = filled,
            segments = SPEED_GAUGE_SEGMENTS,
            accentColor = accent
        )
    }
}

/**
 * Live "where your money went" strip. Wraps onto a second line instead of
 * squeezing five amounts into one row the way a SpaceBetween row would.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FareBreakdown(
    meterState: CurrentMeterState,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        BreakdownChip(label = "기본", amount = meterState.baseFare)
        if (meterState.distanceFare > 0) {
            BreakdownChip(label = "+거리", amount = meterState.distanceFare)
        }
        if (meterState.timeFare > 0) {
            BreakdownChip(label = "+시간", amount = meterState.timeFare)
        }
        if (meterState.surchargeFare > 0) {
            BreakdownChip(
                label = "+할증",
                amount = meterState.surchargeFare,
                accentColor = MeterPurpleBright,
                borderColor = MeterPurple.copy(alpha = 0.4f)
            )
        }
        if (meterState.tollFee > 0) {
            BreakdownChip(
                label = "+통행료",
                amount = meterState.tollFee,
                accentColor = MeterAmberBright,
                borderColor = MeterAmber.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun BreakdownChip(
    label: String,
    amount: Int,
    accentColor: Color = TextSecondary,
    borderColor: Color = CockpitCardBorderSoft
) {
    Row(
        modifier = Modifier
            .clip(MeterShapes.chip)
            .background(CockpitLcdDark.copy(alpha = 0.75f))
            .border(MeterSizes.hairline, borderColor, MeterShapes.chip)
            .padding(horizontal = Space.md, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = accentColor
        )
        Spacer(modifier = Modifier.width(Space.xs))
        Text(
            text = ShareReceiptHelper.formatWon(amount),
            style = MaterialTheme.typography.labelMedium,
            color = accentColor
        )
    }
}

fun formatTimerDigits(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.KOREA, "%02d:%02d", mins, secs)
}

@Preview(showBackground = true, backgroundColor = 0xFF080B11, widthDp = 400)
@Composable
private fun TaxiMeterDisplayVacantPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(Space.xl)) {
            TaxiMeterDisplay(meterState = CurrentMeterState())
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080B11, widthDp = 400)
@Composable
private fun TaxiMeterDisplayDrivingPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(Space.xl)) {
            TaxiMeterDisplay(
                meterState = CurrentMeterState(
                    status = MeterStatus.DRIVING,
                    elapsedSeconds = 847,
                    distanceMeters = 9421.0,
                    currentSpeedKmh = 54.0,
                    baseFare = 4800,
                    distanceFare = 5900,
                    timeFare = 600,
                    surchargeFare = 6780,
                    tollFee = 3000,
                    totalFare = 21080,
                    isNightSurcharge = true,
                    nightSurchargeRate = 40,
                    isOutOfCitySurcharge = true
                )
            )
        }
    }
}
