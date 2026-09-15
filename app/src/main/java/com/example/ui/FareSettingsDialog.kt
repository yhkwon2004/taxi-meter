package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RegionPreset
import com.example.model.TaxiFareConfig
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper

@Composable
fun FareSettingsDialog(
    currentConfig: TaxiFareConfig,
    onSelectPreset: (RegionPreset) -> Unit,
    onSaveCustomConfig: (TaxiFareConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var isCustomMode by remember { mutableStateOf(currentConfig.preset == RegionPreset.CUSTOM) }
    var baseFareStr by remember { mutableStateOf(currentConfig.baseFare.toString()) }
    var baseDistanceStr by remember { mutableStateOf(currentConfig.baseDistanceMeters.toString()) }
    var distUnitMetersStr by remember { mutableStateOf(currentConfig.distanceUnitMeters.toString()) }
    var distUnitFareStr by remember { mutableStateOf(currentConfig.distanceUnitFare.toString()) }
    var timeUnitSecStr by remember { mutableStateOf(currentConfig.timeUnitSeconds.toString()) }
    var timeUnitFareStr by remember { mutableStateOf(currentConfig.timeUnitFare.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CockpitCardBorder, RoundedCornerShape(24.dp))
                .testTag("fare_settings_dialog"),
            color = CockpitCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MeterAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "택시 요금 체계 설정",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_settings")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "닫기", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "지역별 기본 요금 체계",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets list
                val presets = listOf(
                    RegionPreset.SEOUL to "기본 4,800원 / 1.6km (131m당 100원, 30초당 100원)",
                    RegionPreset.REGIONAL to "기본 4,000원 / 2.0km (132m당 100원, 31초당 100원)",
                    RegionPreset.DELUXE to "기본 7,000원 / 3.0km (151m당 200원, 36초당 200원)"
                )

                presets.forEach { (preset, desc) ->
                    val isSelected = !isCustomMode && currentConfig.preset == preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                isCustomMode = false
                                onSelectPreset(preset)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MeterAmber.copy(alpha = 0.15f) else CockpitSurface,
                        border = BorderStroke(1.dp, if (isSelected) MeterAmber else CockpitCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    isCustomMode = false
                                    onSelectPreset(preset)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MeterAmber)
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = preset.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MeterAmberBright else TextPrimary
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // Custom option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { isCustomMode = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCustomMode) MeterAmber.copy(alpha = 0.15f) else CockpitSurface,
                    border = BorderStroke(1.dp, if (isCustomMode) MeterAmber else CockpitCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCustomMode,
                            onClick = { isCustomMode = true },
                            colors = RadioButtonDefaults.colors(selectedColor = MeterAmber)
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = "사용자 직접 입력 (커스텀)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCustomMode) MeterAmberBright else TextPrimary
                            )
                            Text(
                                text = "기본요금, 거리요금, 지체시간요금을 직접 변경",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Custom editable inputs
                if (isCustomMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "커스텀 요금 상세 설정",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MeterAmberBright
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = baseFareStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) baseFareStr = it },
                            label = { Text("기본 요금(원)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeterAmber,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = baseDistanceStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) baseDistanceStr = it },
                            label = { Text("기본 거리(m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeterAmber,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = distUnitMetersStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) distUnitMetersStr = it },
                            label = { Text("거리 단위(m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeterAmber,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = distUnitFareStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) distUnitFareStr = it },
                            label = { Text("단위 요금(원)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeterAmber,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = timeUnitSecStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) timeUnitSecStr = it },
                            label = { Text("시간 단위(초)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeterAmber,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeUnitFareStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) timeUnitFareStr = it },
                            label = { Text("시간 요금(원)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeterAmber,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newConfig = TaxiFareConfig(
                                preset = RegionPreset.CUSTOM,
                                baseFare = baseFareStr.toIntOrNull() ?: 4800,
                                baseDistanceMeters = baseDistanceStr.toIntOrNull() ?: 1600,
                                distanceUnitMeters = distUnitMetersStr.toIntOrNull() ?: 131,
                                distanceUnitFare = distUnitFareStr.toIntOrNull() ?: 100,
                                timeUnitSeconds = timeUnitSecStr.toIntOrNull() ?: 30,
                                timeUnitFare = timeUnitFareStr.toIntOrNull() ?: 100,
                                slowSpeedKmhThreshold = 15.0,
                                nightSurchargePercent = currentConfig.nightSurchargePercent,
                                outOfCitySurchargePercent = currentConfig.outOfCitySurchargePercent
                            )
                            onSaveCustomConfig(newConfig)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MeterAmber)
                    ) {
                        Text("사용자 정의 요금 저장", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("확인 및 닫기", color = MeterAmber)
                }
            }
        }
    }
}
