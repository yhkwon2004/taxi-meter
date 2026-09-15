package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.CurrentMeterState
import com.example.model.MeterStatus
import com.example.ui.components.CockpitPanel
import com.example.ui.components.MeterFilledButton
import com.example.ui.components.MeterOutlinedButton
import com.example.ui.components.MeterPrimaryLabelStyle
import com.example.ui.components.SectionLabel
import com.example.ui.components.SelectableChip
import com.example.ui.components.ToggleTile
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitCardBorderSoft
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CyanButtonGradient
import com.example.ui.theme.GreenButtonGradient
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MeterPurple
import com.example.ui.theme.MeterPurpleBright
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.OnAmberButton
import com.example.ui.theme.OnCyanButton
import com.example.ui.theme.OnGreenButton
import com.example.ui.theme.OnRedButton
import com.example.ui.theme.RedButtonGradient
import com.example.ui.theme.Space
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper

/** Simulation speeds offered in 모의 주행, as (km/h to short label). */
private val SIMULATION_SPEEDS = listOf(
    0 to "정차",
    30 to "서행",
    60 to "시내",
    90 to "고속"
)

/**
 * The settings half of the cockpit: surcharge switches and the indoor demo
 * mode. The driving actions live in [TaxiMeterActionBar], pinned to the bottom
 * of the screen where a thumb can reach them.
 */
@Composable
fun TaxiMeterControls(
    meterState: CurrentMeterState,
    onToggleNightSurcharge: () -> Unit,
    onToggleOutOfCity: () -> Unit,
    onAddTollFee: (Int) -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onSetSimulationSpeed: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTollDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("taxi_meter_controls"),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        // --- Surcharges & extras
        CockpitPanel(modifier = Modifier.fillMaxWidth()) {
            SectionLabel(text = "할증 · 부가요금")
            Spacer(modifier = Modifier.height(Space.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                ToggleTile(
                    label = if (meterState.isNightSurcharge) {
                        "심야 ${meterState.nightSurchargeRate}%"
                    } else {
                        "심야 OFF"
                    },
                    icon = Icons.Default.Bedtime,
                    selected = meterState.isNightSurcharge,
                    accentColor = MeterPurple,
                    selectedLabelColor = MeterPurpleBright,
                    onClick = onToggleNightSurcharge,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_night_surcharge")
                )

                ToggleTile(
                    label = if (meterState.isOutOfCitySurcharge) {
                        "시외 ${meterState.config.outOfCitySurchargePercent}%"
                    } else {
                        "시외 OFF"
                    },
                    icon = Icons.Default.LocationOn,
                    selected = meterState.isOutOfCitySurcharge,
                    accentColor = MeterCyan,
                    selectedLabelColor = MeterCyanBright,
                    onClick = onToggleOutOfCity,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_out_of_city")
                )

                ToggleTile(
                    label = if (meterState.tollFee > 0) {
                        ShareReceiptHelper.formatWon(meterState.tollFee)
                    } else {
                        "통행료"
                    },
                    icon = Icons.Default.Payments,
                    selected = meterState.tollFee > 0,
                    accentColor = MeterAmber,
                    selectedLabelColor = MeterAmberBright,
                    onClick = { showTollDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_toll_fee")
                )
            }
        }

        // --- Simulation mode
        CockpitPanel(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "모의 주행",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(Space.xxs))
                    Text(
                        text = if (meterState.isSimulationMode) {
                            "가상 속도로 미터기 작동 중"
                        } else {
                            "실제 GPS 이동 거리로 측정 중"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = meterState.isSimulationMode,
                    onCheckedChange = onToggleSimulation,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MeterGreenBright,
                        checkedTrackColor = MeterGreen.copy(alpha = 0.28f),
                        checkedBorderColor = MeterGreen.copy(alpha = 0.6f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CockpitSurface,
                        uncheckedBorderColor = CockpitCardBorderSoft
                    ),
                    modifier = Modifier.testTag("toggle_simulation_mode")
                )
            }

            AnimatedVisibility(visible = meterState.isSimulationMode) {
                Column(modifier = Modifier.padding(top = Space.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        SIMULATION_SPEEDS.forEach { (speed, label) ->
                            SelectableChip(
                                label = label,
                                selected = meterState.simulationSpeedOption == speed,
                                accentColor = MeterGreenBright,
                                onClick = { onSetSimulationSpeed(speed) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTollDialog) {
        TollFeeDialog(
            currentTollFee = meterState.tollFee,
            onAddTollFee = onAddTollFee,
            onDismiss = { showTollDialog = false }
        )
    }
}

/**
 * The one action row for the meter's current state, meant to be hosted in the
 * Scaffold's bottom bar. Exactly one filled button per state.
 */
@Composable
fun TaxiMeterActionBar(
    meterState: CurrentMeterState,
    onStartRide: () -> Unit,
    onPauseRide: () -> Unit,
    onResumeRide: () -> Unit,
    onEndRide: () -> Unit,
    onResetToVacant: () -> Unit,
    onOpenReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (meterState.status) {
            MeterStatus.VACANT -> {
                MeterFilledButton(
                    label = "주행 시작",
                    icon = Icons.Default.DirectionsCar,
                    gradient = GreenButtonGradient,
                    contentColor = OnGreenButton,
                    onClick = onStartRide,
                    height = MeterSizes.primaryButton,
                    labelStyle = MeterPrimaryLabelStyle,
                    iconSize = 26.dp,
                    modifier = Modifier.testTag("btn_start_ride")
                )
            }

            MeterStatus.DRIVING -> {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    MeterOutlinedButton(
                        label = "정차",
                        icon = Icons.Default.Pause,
                        onClick = onPauseRide,
                        contentColor = MeterAmberBright,
                        borderColor = MeterAmber.copy(alpha = 0.6f),
                        height = MeterSizes.secondaryButton,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_pause_ride")
                    )
                    MeterFilledButton(
                        label = "주행 종료",
                        icon = Icons.Default.Stop,
                        gradient = RedButtonGradient,
                        contentColor = OnRedButton,
                        onClick = onEndRide,
                        height = MeterSizes.secondaryButton,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_end_ride")
                    )
                }
            }

            MeterStatus.PAUSED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    MeterFilledButton(
                        label = "주행 재개",
                        icon = Icons.Default.PlayArrow,
                        gradient = GreenButtonGradient,
                        contentColor = OnGreenButton,
                        onClick = onResumeRide,
                        height = MeterSizes.secondaryButton,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_resume_ride")
                    )
                    MeterOutlinedButton(
                        label = "주행 종료",
                        icon = Icons.Default.Stop,
                        onClick = onEndRide,
                        contentColor = MeterRed,
                        borderColor = MeterRed.copy(alpha = 0.6f),
                        height = MeterSizes.secondaryButton,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_end_ride_from_pause")
                    )
                }
            }

            MeterStatus.PAYMENT -> {
                MeterFilledButton(
                    label = "영수증 · 정산 공유",
                    icon = Icons.Default.Receipt,
                    gradient = CyanButtonGradient,
                    contentColor = OnCyanButton,
                    onClick = onOpenReceipt,
                    height = MeterSizes.secondaryButton,
                    modifier = Modifier.testTag("btn_view_receipt")
                )
                Spacer(modifier = Modifier.height(Space.md))
                MeterOutlinedButton(
                    label = "다음 승객 (빈차 초기화)",
                    icon = Icons.Default.Refresh,
                    onClick = onResetToVacant,
                    contentColor = TextSecondary,
                    iconTint = MeterRed,
                    modifier = Modifier.testTag("btn_reset_vacant")
                )
            }
        }
    }
}

@Composable
private fun TollFeeDialog(
    currentTollFee: Int,
    onAddTollFee: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tollInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeterShapes.hero,
        containerColor = CockpitCard,
        title = {
            Text(
                text = "통행료 · 추가금",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
                Text(
                    text = "현재 추가금 ${ShareReceiptHelper.formatWon(currentTollFee)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MeterAmberBright
                )

                SectionLabel(text = "빠른 추가")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    listOf(1000, 2000, 3000, 5000).forEach { amount ->
                        SelectableChip(
                            label = "+${amount / 1000}천",
                            selected = false,
                            accentColor = MeterAmberBright,
                            onClick = {
                                onAddTollFee(amount)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = tollInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) tollInput = input
                    },
                    label = { Text("직접 입력 (원)") },
                    placeholder = { Text("예: 2500") },
                    singleLine = true,
                    shape = MeterShapes.tile,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = meterTextFieldColors(MeterAmber),
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentTollFee > 0) {
                    TextButton(
                        onClick = {
                            onAddTollFee(-currentTollFee)
                            onDismiss()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "추가금 초기화",
                            style = MaterialTheme.typography.labelMedium,
                            color = MeterRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = tollInput.toIntOrNull() ?: 0
                    if (amount > 0) onAddTollFee(amount)
                    onDismiss()
                },
                shape = MeterShapes.chip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeterAmber,
                    contentColor = OnAmberButton
                )
            ) {
                Text(text = "확인", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    )
}

/** Shared text field styling so every input in the app matches. */
@Composable
internal fun meterTextFieldColors(accentColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accentColor,
    unfocusedBorderColor = CockpitCardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = accentColor,
    unfocusedLabelColor = TextMuted,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
    focusedLeadingIconColor = accentColor,
    unfocusedLeadingIconColor = TextMuted,
    cursorColor = accentColor,
    focusedContainerColor = CockpitSurface,
    unfocusedContainerColor = CockpitSurface
)
