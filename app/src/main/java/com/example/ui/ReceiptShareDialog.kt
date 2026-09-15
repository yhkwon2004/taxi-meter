package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CurrentMeterState
import com.example.ui.components.CockpitPanel
import com.example.ui.components.CockpitTile
import com.example.ui.components.LcdPanel
import com.example.ui.components.MeterFilledButton
import com.example.ui.components.MeterOutlinedButton
import com.example.ui.components.PerforatedDivider
import com.example.ui.components.MeterDialogShell
import com.example.ui.components.SectionLabel
import com.example.ui.components.SelectableChip
import com.example.ui.theme.AmberButtonGradient
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MeterPurpleBright
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.MeterType
import com.example.ui.theme.OnAmberButton
import com.example.ui.theme.Space
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * End-of-ride receipt: the printed ticket on top, the 1/N split calculator
 * under it, then the share actions. Reads top to bottom in the order a
 * passenger actually needs it — what it cost, what I owe, how I send it.
 */
@Composable
fun ReceiptShareDialog(
    meterState: CurrentMeterState,
    passengerCount: Int,
    savedAccountInfo: String,
    routeMemo: String,
    onPassengerCountChange: (Int) -> Unit,
    onAccountInfoChange: (String) -> Unit,
    onRouteMemoChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onNewRide: () -> Unit
) {
    val context = LocalContext.current
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    val dateStr = dateFormat.format(
        Date(
            if (meterState.startTimeMillis > 0) {
                meterState.startTimeMillis
            } else {
                System.currentTimeMillis()
            }
        )
    )

    val perPersonFare = if (passengerCount > 0) {
        (meterState.totalFare + passengerCount - 1) / passengerCount
    } else {
        meterState.totalFare
    }

    val shareText = ShareReceiptHelper.buildShareMessage(
        startTimeMillis = meterState.startTimeMillis,
        endTimeMillis = meterState.endTimeMillis,
        distanceMeters = meterState.distanceMeters,
        durationSeconds = meterState.elapsedSeconds,
        baseFare = meterState.baseFare,
        distanceFare = meterState.distanceFare,
        timeFare = meterState.timeFare,
        surchargeFare = meterState.surchargeFare,
        tollFee = meterState.tollFee,
        totalFare = meterState.totalFare,
        surchargeDesc = meterState.surchargeDescription,
        passengerCount = passengerCount,
        accountInfo = savedAccountInfo,
        memo = routeMemo
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MeterDialogShell(
            title = "택시 이용 영수증",
            subtitle = dateStr,
            icon = Icons.Default.ReceiptLong,
            accentColor = MeterAmber,
            onDismiss = onDismiss,
            closeButtonModifier = Modifier.testTag("btn_close_receipt"),
            modifier = Modifier
                .padding(vertical = Space.xl)
                .testTag("receipt_share_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.xxl, vertical = Space.xl),
                verticalArrangement = Arrangement.spacedBy(Space.lg)
            ) {
                // --- The ticket
                CockpitTile(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(Space.xl)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TicketSummaryItem(
                            icon = Icons.Default.DirectionsCar,
                            label = "주행 거리",
                            value = ShareReceiptHelper.formatDistance(meterState.distanceMeters),
                            accentColor = MeterGreenBright
                        )
                        TicketSummaryItem(
                            icon = Icons.Default.Timer,
                            label = "운행 시간",
                            value = ShareReceiptHelper.formatDuration(meterState.elapsedSeconds),
                            accentColor = MeterCyanBright,
                            alignEnd = true
                        )
                    }

                    PerforatedDivider(modifier = Modifier.padding(vertical = Space.lg))

                    ReceiptLine(label = "기본 요금", amount = meterState.baseFare)
                    if (meterState.distanceFare > 0) {
                        ReceiptLine(label = "거리 요금", amount = meterState.distanceFare)
                    }
                    if (meterState.timeFare > 0) {
                        ReceiptLine(label = "시간 · 지체 요금", amount = meterState.timeFare)
                    }
                    if (meterState.surchargeFare > 0) {
                        ReceiptLine(
                            label = "할증 (${meterState.surchargeDescription})",
                            amount = meterState.surchargeFare,
                            labelColor = MeterPurpleBright
                        )
                    }
                    if (meterState.tollFee > 0) {
                        ReceiptLine(
                            label = "통행료 · 추가금",
                            amount = meterState.tollFee,
                            labelColor = MeterAmberBright
                        )
                    }

                    PerforatedDivider(
                        modifier = Modifier.padding(vertical = Space.lg),
                        color = MeterAmber.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "총 결제 금액",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "${numberFormat.format(meterState.totalFare)}원",
                            style = MeterType.lcdLarge,
                            color = MeterAmberBright
                        )
                    }
                }

                // --- 1/N split
                CockpitPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MeterShapes.tile,
                    borderColor = MeterCyan.copy(alpha = 0.35f),
                    contentPadding = PaddingValues(Space.xl)
                ) {
                    SectionLabel(text = "1/N 더치페이 정산", color = MeterCyanBright)

                    Spacer(modifier = Modifier.height(Space.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "탑승 인원",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            StepperButton(
                                icon = Icons.Default.Remove,
                                contentDescription = "인원 감소",
                                enabled = passengerCount > 1,
                                onClick = { onPassengerCountChange(passengerCount - 1) },
                                modifier = Modifier.testTag("btn_minus_passenger")
                            )
                            Text(
                                text = "$passengerCount 명",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            StepperButton(
                                icon = Icons.Default.Add,
                                contentDescription = "인원 증가",
                                enabled = passengerCount < 10,
                                onClick = { onPassengerCountChange(passengerCount + 1) },
                                modifier = Modifier.testTag("btn_plus_passenger")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            SelectableChip(
                                label = "${count}인",
                                selected = passengerCount == count,
                                accentColor = MeterCyanBright,
                                onClick = { onPassengerCountChange(count) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.lg))

                    LcdPanel(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MeterShapes.tile,
                        glowColor = MeterCyanBright,
                        contentPadding = PaddingValues(horizontal = Space.xl, vertical = Space.lg)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "1인당 분담금",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = numberFormat.format(perPersonFare),
                                    style = MeterType.lcdLarge,
                                    fontSize = 22.sp,
                                    color = MeterCyanBright
                                )
                                Text(
                                    text = "원",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(start = Space.xxs, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

                // --- Optional details carried into the shared message
                OutlinedTextField(
                    value = savedAccountInfo,
                    onValueChange = onAccountInfoChange,
                    label = { Text("입금 계좌 · 간편결제 안내 (선택)") },
                    placeholder = { Text("예: 카카오뱅크 3333-12-3456789 홍길동") },
                    singleLine = true,
                    shape = MeterShapes.tile,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null
                        )
                    },
                    colors = meterTextFieldColors(MeterCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_info")
                )

                OutlinedTextField(
                    value = routeMemo,
                    onValueChange = onRouteMemoChange,
                    label = { Text("운행 구간 · 메모 (선택)") },
                    placeholder = { Text("예: 강남역 11번 출구 → 홍대입구") },
                    singleLine = true,
                    shape = MeterShapes.tile,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Route, contentDescription = null)
                    },
                    colors = meterTextFieldColors(MeterAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_route_memo")
                )

                // --- Actions
                MeterFilledButton(
                    label = "SNS로 정산 공유",
                    icon = Icons.Default.Share,
                    gradient = AmberButtonGradient,
                    contentColor = OnAmberButton,
                    onClick = {
                        ShareReceiptHelper.shareViaSns(
                            context = context,
                            text = shareText,
                            title = "택시 요금 및 정산 내역 공유"
                        )
                    },
                    height = MeterSizes.secondaryButton,
                    modifier = Modifier.testTag("btn_sns_share")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    MeterOutlinedButton(
                        label = "문구 복사",
                        icon = Icons.Default.ContentCopy,
                        onClick = { ShareReceiptHelper.copyToClipboard(context, shareText) },
                        contentColor = TextPrimary,
                        iconTint = MeterCyanBright,
                        labelStyle = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_clipboard")
                    )
                    MeterOutlinedButton(
                        label = "새 주행",
                        icon = Icons.Default.DirectionsCar,
                        onClick = {
                            onNewRide()
                            onDismiss()
                        },
                        contentColor = MeterGreenBright,
                        borderColor = MeterGreen.copy(alpha = 0.5f),
                        labelStyle = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_new_ride_from_dialog")
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketSummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
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
        Spacer(modifier = Modifier.height(Space.xs))
        Text(text = value, style = MeterType.lcdSmall, color = accentColor)
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CockpitSurface)
            .border(MeterSizes.hairline, CockpitCardBorder, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) TextPrimary else TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ReceiptLine(
    label: String,
    amount: Int,
    labelColor: Color = TextSecondary
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
        Text(
            text = "${numberFormat.format(amount)}원",
            style = MeterType.amount,
            color = TextPrimary
        )
    }
}
