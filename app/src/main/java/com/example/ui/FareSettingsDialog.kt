package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RegionPreset
import com.example.model.TaxiFareConfig
import com.example.ui.components.CockpitTile
import com.example.ui.components.MeterDialogShell
import com.example.ui.components.MeterFilledButton
import com.example.ui.components.MeterOutlinedButton
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AmberButtonGradient
import com.example.ui.theme.CockpitCardBorderSoft
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterType
import com.example.ui.theme.OnAmberButton
import com.example.ui.theme.Space
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper
import java.util.Locale

/** Built-in fare tables, in the order they are offered. */
private val FARE_PRESETS = listOf(
    TaxiFareConfig.SEOUL,
    TaxiFareConfig.REGIONAL,
    TaxiFareConfig.DELUXE
)

/**
 * Fare table picker. The preset rows are rendered straight from
 * [TaxiFareConfig], so the numbers shown here can never drift away from the
 * ones the meter actually charges.
 */
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
        MeterDialogShell(
            title = "요금 체계 설정",
            subtitle = "지역 프리셋 또는 직접 입력",
            icon = Icons.Default.Tune,
            accentColor = MeterAmber,
            onDismiss = onDismiss,
            closeButtonModifier = Modifier.testTag("btn_close_settings"),
            modifier = Modifier
                .padding(vertical = Space.xl)
                .testTag("fare_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.xxl, vertical = Space.xl),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                SectionLabel(text = "지역 프리셋")

                FARE_PRESETS.forEach { preset ->
                    PresetRow(
                        title = preset.preset.displayName,
                        summary = "기본 ${ShareReceiptHelper.formatWon(preset.baseFare)} · " +
                            "${String.format(Locale.KOREA, "%.1f", preset.baseDistanceMeters / 1000.0)} km",
                        detail = "${preset.distanceUnitMeters}m / ${preset.distanceUnitFare}원\n" +
                            "${preset.timeUnitSeconds}초 / ${preset.timeUnitFare}원",
                        selected = !isCustomMode && currentConfig.preset == preset.preset,
                        onClick = {
                            isCustomMode = false
                            onSelectPreset(preset.preset)
                        }
                    )
                }

                PresetRow(
                    title = "사용자 지정",
                    summary = "기본 · 거리 · 시간 요금을 직접 입력",
                    detail = null,
                    selected = isCustomMode,
                    onClick = { isCustomMode = true }
                )

                AnimatedVisibility(visible = isCustomMode) {
                    Column(
                        modifier = Modifier.padding(top = Space.md),
                        verticalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        SectionLabel(text = "커스텀 요금", color = MeterAmberBright)

                        FareInputRow(
                            firstValue = baseFareStr,
                            onFirstChange = { baseFareStr = it },
                            firstLabel = "기본 요금(원)",
                            secondValue = baseDistanceStr,
                            onSecondChange = { baseDistanceStr = it },
                            secondLabel = "기본 거리(m)"
                        )
                        FareInputRow(
                            firstValue = distUnitMetersStr,
                            onFirstChange = { distUnitMetersStr = it },
                            firstLabel = "거리 단위(m)",
                            secondValue = distUnitFareStr,
                            onSecondChange = { distUnitFareStr = it },
                            secondLabel = "단위 요금(원)"
                        )
                        FareInputRow(
                            firstValue = timeUnitSecStr,
                            onFirstChange = { timeUnitSecStr = it },
                            firstLabel = "시간 단위(초)",
                            secondValue = timeUnitFareStr,
                            onSecondChange = { timeUnitFareStr = it },
                            secondLabel = "시간 요금(원)"
                        )

                        MeterFilledButton(
                            label = "커스텀 요금 저장",
                            icon = Icons.Default.Check,
                            gradient = AmberButtonGradient,
                            contentColor = OnAmberButton,
                            onClick = {
                                onSaveCustomConfig(
                                    TaxiFareConfig(
                                        preset = RegionPreset.CUSTOM,
                                        baseFare = baseFareStr.toIntOrNull() ?: 4800,
                                        baseDistanceMeters = baseDistanceStr.toIntOrNull() ?: 1600,
                                        distanceUnitMeters = distUnitMetersStr.toIntOrNull() ?: 131,
                                        distanceUnitFare = distUnitFareStr.toIntOrNull() ?: 100,
                                        timeUnitSeconds = timeUnitSecStr.toIntOrNull() ?: 30,
                                        timeUnitFare = timeUnitFareStr.toIntOrNull() ?: 100,
                                        slowSpeedKmhThreshold = currentConfig.slowSpeedKmhThreshold,
                                        nightSurchargePercent = currentConfig.nightSurchargePercent,
                                        outOfCitySurchargePercent =
                                            currentConfig.outOfCitySurchargePercent
                                    )
                                )
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.xs))

                MeterOutlinedButton(
                    label = "확인",
                    icon = null,
                    onClick = onDismiss,
                    contentColor = MeterAmberBright,
                    borderColor = MeterAmber.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun PresetRow(
    title: String,
    summary: String,
    detail: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    CockpitTile(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        containerColor = if (selected) MeterAmber.copy(alpha = 0.14f) else CockpitSurface,
        borderColor = if (selected) MeterAmber.copy(alpha = 0.7f) else CockpitCardBorderSoft,
        contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MeterAmber,
                    unselectedColor = TextMuted
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Space.md)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MeterAmberBright else TextPrimary
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            if (detail != null) {
                Text(
                    text = detail,
                    style = MeterType.amount,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun FareInputRow(
    firstValue: String,
    onFirstChange: (String) -> Unit,
    firstLabel: String,
    secondValue: String,
    onSecondChange: (String) -> Unit,
    secondLabel: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
        FareNumberField(
            value = firstValue,
            onValueChange = onFirstChange,
            label = firstLabel,
            modifier = Modifier.weight(1f)
        )
        FareNumberField(
            value = secondValue,
            onValueChange = onSecondChange,
            label = secondLabel,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FareNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.all { it.isDigit() }) onValueChange(input) },
        label = {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        },
        singleLine = true,
        shape = MeterShapes.tile,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = meterTextFieldColors(MeterAmber),
        modifier = modifier
    )
}
