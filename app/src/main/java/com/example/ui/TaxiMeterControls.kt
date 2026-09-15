package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentMeterState
import com.example.model.MeterStatus
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterPurple
import com.example.ui.theme.MeterRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper

@Composable
fun TaxiMeterControls(
    meterState: CurrentMeterState,
    onStartRide: () -> Unit,
    onPauseRide: () -> Unit,
    onResumeRide: () -> Unit,
    onEndRide: () -> Unit,
    onResetToVacant: () -> Unit,
    onToggleNightSurcharge: () -> Unit,
    onToggleOutOfCity: () -> Unit,
    onAddTollFee: (Int) -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onSetSimulationSpeed: (Int) -> Unit,
    onOpenReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTollDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("taxi_meter_controls"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Surcharge & Toll Options Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CockpitCard,
            border = BorderStroke(1.dp, CockpitCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "할증 및 부가요금 설정",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Night surcharge toggle
                    val nightText = when {
                        !meterState.isNightSurcharge -> "심야 OFF"
                        meterState.nightSurchargeRate == 20 -> "심야 20%"
                        else -> "심야 40%"
                    }
                    val isNightActive = meterState.isNightSurcharge
                    FilterChip(
                        selected = isNightActive,
                        onClick = onToggleNightSurcharge,
                        label = { Text(nightText, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeterPurple.copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFD8B4FE),
                            selectedLeadingIconColor = Color(0xFFD8B4FE),
                            containerColor = CockpitSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isNightActive,
                            borderColor = CockpitCardBorder,
                            selectedBorderColor = MeterPurple
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_night_surcharge")
                    )

                    // Out of city surcharge toggle
                    val isCityOutActive = meterState.isOutOfCitySurcharge
                    FilterChip(
                        selected = isCityOutActive,
                        onClick = onToggleOutOfCity,
                        label = {
                            Text(
                                if (isCityOutActive) "시외 20%" else "시외 OFF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeterCyan.copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF7DD3FC),
                            selectedLeadingIconColor = Color(0xFF7DD3FC),
                            containerColor = CockpitSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isCityOutActive,
                            borderColor = CockpitCardBorder,
                            selectedBorderColor = MeterCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_out_of_city")
                    )

                    // Toll fee button
                    FilterChip(
                        selected = meterState.tollFee > 0,
                        onClick = { showTollDialog = true },
                        label = {
                            Text(
                                if (meterState.tollFee > 0) "+${ShareReceiptHelper.formatWon(meterState.tollFee)}" else "+통행료",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeterAmber.copy(alpha = 0.25f),
                            selectedLabelColor = MeterAmberBright,
                            selectedLeadingIconColor = MeterAmberBright,
                            containerColor = CockpitSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = meterState.tollFee > 0,
                            borderColor = CockpitCardBorder,
                            selectedBorderColor = MeterAmber
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_toll_fee")
                    )
                }
            }
        }

        // Simulation Mode Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CockpitCard,
            border = BorderStroke(1.dp, CockpitCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "모의 주행 (실내/체험 모드)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (meterState.isSimulationMode) "가상 속도로 미터기 실시간 작동 중" else "실제 기기 GPS 이동 거리 측정 중",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = meterState.isSimulationMode,
                        onCheckedChange = onToggleSimulation,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MeterGreen,
                            checkedTrackColor = MeterGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CockpitSurface
                        ),
                        modifier = Modifier.testTag("toggle_simulation_mode")
                    )
                }

                AnimatedVisibility(visible = meterState.isSimulationMode) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "시뮬레이션 주행 속도 선택:",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                0 to "정차 (0km/h)",
                                30 to "서행 (30km/h)",
                                60 to "시내 (60km/h)",
                                90 to "고속 (90km/h)"
                            ).forEach { (speed, label) ->
                                val isSelected = meterState.simulationSpeedOption == speed
                                OutlinedButton(
                                    onClick = { onSetSimulationSpeed(speed) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MeterGreen.copy(alpha = 0.2f) else Color.Transparent,
                                        contentColor = if (isSelected) MeterGreen else TextSecondary
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MeterGreen else CockpitCardBorder)
                                ) {
                                    Text(
                                        text = label.substringBefore(" "),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tactile Cockpit Driving Action Buttons
        when (meterState.status) {
            MeterStatus.VACANT -> {
                // Giant Start Ride Button
                Button(
                    onClick = onStartRide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("btn_start_ride"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeterGreen,
                        contentColor = Color(0xFF003822)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "주행 시작 (미터기 작동)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            MeterStatus.DRIVING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pause / Slow
                    OutlinedButton(
                        onClick = onPauseRide,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .testTag("btn_pause_ride"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CockpitSurface,
                            contentColor = MeterAmberBright
                        ),
                        border = BorderStroke(1.5.dp, MeterAmber)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "정차 / 일시정지", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // End Ride
                    Button(
                        onClick = onEndRide,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .testTag("btn_end_ride"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeterRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "주행 종료 (계산)", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            MeterStatus.PAUSED -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Resume
                    Button(
                        onClick = onResumeRide,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .testTag("btn_resume_ride"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeterGreen,
                            contentColor = Color(0xFF003822)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "주행 재개", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }

                    // End Ride
                    Button(
                        onClick = onEndRide,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .testTag("btn_end_ride_from_pause"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeterRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "주행 종료 (계산)", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            MeterStatus.PAYMENT -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Open / View Receipt Button
                    Button(
                        onClick = onOpenReceipt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("btn_view_receipt"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeterCyan,
                            contentColor = Color(0xFF00293B)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "영수증 확인 & SNS 정산 공유", fontSize = 17.sp, fontWeight = FontWeight.Black)
                    }

                    // Reset to vacant
                    OutlinedButton(
                        onClick = onResetToVacant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_reset_vacant"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CockpitSurface,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, CockpitCardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MeterRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "다음 승객 탑승 (빈차 초기화)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Toll Fee Dialog
    if (showTollDialog) {
        var tollInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTollDialog = false },
            title = {
                Text(
                    text = "통행료 / 추가금 추가",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "현재 추가금: ${ShareReceiptHelper.formatWon(meterState.tollFee)}",
                        fontSize = 13.sp,
                        color = MeterAmberBright
                    )

                    // Quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1000, 2000, 3000, 5000).forEach { amount ->
                            OutlinedButton(
                                onClick = {
                                    onAddTollFee(amount)
                                    showTollDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CockpitCardBorder)
                            ) {
                                Text("+${amount / 1000}천", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }

                    // Direct input
                    OutlinedTextField(
                        value = tollInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) tollInput = it },
                        label = { Text("직접 입력 (원)") },
                        placeholder = { Text("예: 2500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeterAmber,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (meterState.tollFee > 0) {
                        TextButton(
                            onClick = {
                                onAddTollFee(-meterState.tollFee) // reset toll fee
                                showTollDialog = false
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("통행료 0원으로 초기화", color = MeterRed)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = tollInput.toIntOrNull() ?: 0
                        if (amt > 0) onAddTollFee(amt)
                        showTollDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeterAmber)
                ) {
                    Text("확인", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTollDialog = false }) {
                    Text("취소", color = TextSecondary)
                }
            },
            containerColor = CockpitCard
        )
    }
}
