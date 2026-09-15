package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentMeterState
import com.example.model.MeterStatus
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitLcdDark
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MeterPurple
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterRedBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TaxiMeterDisplay(
    meterState: CurrentMeterState,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val isDriving = meterState.status == MeterStatus.DRIVING

    // Infinite animation for running taxi horse / pulse bar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (meterState.currentSpeedKmh > 60) 500 else if (meterState.currentSpeedKmh > 20) 900 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("taxi_meter_display"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Taxi Roof Sign / Status Indicator Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Status Sign: 빈차 / 주행 / 정차 / 지불
            MeterStatusBadge(status = meterState.status)

            // Surcharge Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (meterState.isNightSurcharge) {
                    SurchargeBadge(
                        label = "심야 ${meterState.nightSurchargeRate}%",
                        color = MeterPurple,
                        icon = Icons.Default.Bedtime
                    )
                }
                if (meterState.isOutOfCitySurcharge) {
                    SurchargeBadge(
                        label = "시외 20%",
                        color = MeterCyan,
                        icon = Icons.Default.LocationOn
                    )
                }
                if (meterState.tollFee > 0) {
                    SurchargeBadge(
                        label = "+${ShareReceiptHelper.formatWon(meterState.tollFee)}",
                        color = MeterAmber,
                        icon = null
                    )
                }
            }
        }

        // Digital Cockpit Main Meter Frame
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, CockpitCardBorder, RoundedCornerShape(24.dp)),
            color = CockpitCard,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LCD Fare Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CockpitLcdDark)
                        .border(1.dp, Color(0xFF243347), RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Small LCD Header: FARE / 요금
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FARE / 택시 요금",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = meterState.config.preset.displayName,
                                fontSize = 11.sp,
                                color = MeterAmberBright.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large Digital Fare Display
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "₩",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MeterAmber,
                                modifier = Modifier.padding(bottom = 6.dp, end = 6.dp)
                            )
                            Text(
                                text = numberFormat.format(meterState.totalFare),
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MeterAmberBright,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "원",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Running Pulse Animation Bar (Iconic Korean Taxi meter visual effect)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            if (isDriving) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.35f)
                                        .height(6.dp)
                                        .offset(x = (pulseProgress * 220).dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, MeterAmberBright, Color.Transparent)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3-Metric Sub-readout: Distance / Time / Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Distance
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.DirectionsCar,
                        label = "주행 거리",
                        value = ShareReceiptHelper.formatDistance(meterState.distanceMeters),
                        accentColor = MeterGreenBright
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Duration
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        label = "운행 시간",
                        value = formatTimerDigits(meterState.elapsedSeconds),
                        accentColor = MeterCyanBright
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Speed
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "현재 속도",
                        value = "${meterState.currentSpeedKmh.toInt()} km/h",
                        accentColor = if (meterState.currentSpeedKmh < meterState.config.slowSpeedKmhThreshold && isDriving) {
                            MeterAmber // Slow/idle indicator
                        } else {
                            TextPrimary
                        }
                    )
                }

                // Fare Breakdown preview chips
                Spacer(modifier = Modifier.height(12.dp))
                FareBreakdownRow(meterState = meterState)
            }
        }
    }
}

@Composable
private fun MeterStatusBadge(status: MeterStatus) {
    val (text, color, bgColor) = when (status) {
        MeterStatus.VACANT -> Triple("빈  차", MeterRedBright, Color(0xFF3B1215))
        MeterStatus.DRIVING -> Triple("주  행", MeterGreenBright, Color(0xFF0F3622))
        MeterStatus.PAUSED -> Triple("정차/대기", MeterAmberBright, Color(0xFF382900))
        MeterStatus.PAYMENT -> Triple("지불/정산", MeterCyanBright, Color(0xFF0C2B3E))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SurchargeBadge(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector?
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = CockpitSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FareBreakdownRow(meterState: CurrentMeterState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1722))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "기본 ${ShareReceiptHelper.formatWon(meterState.baseFare)}",
            fontSize = 11.sp,
            color = TextSecondary
        )
        if (meterState.distanceFare > 0) {
            Text(
                text = "+거리 ${ShareReceiptHelper.formatWon(meterState.distanceFare)}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        if (meterState.timeFare > 0) {
            Text(
                text = "+시간 ${ShareReceiptHelper.formatWon(meterState.timeFare)}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        if (meterState.surchargeFare > 0) {
            Text(
                text = "+할증 ${ShareReceiptHelper.formatWon(meterState.surchargeFare)}",
                fontSize = 11.sp,
                color = MeterPurple
            )
        }
        if (meterState.tollFee > 0) {
            Text(
                text = "+통행료 ${ShareReceiptHelper.formatWon(meterState.tollFee)}",
                fontSize = 11.sp,
                color = MeterAmber
            )
        }
    }
}

fun formatTimerDigits(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.KOREA, "%02d:%02d", mins, secs)
}
