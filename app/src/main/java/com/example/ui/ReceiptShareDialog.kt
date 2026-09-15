package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CurrentMeterState
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val dateStr = dateFormat.format(Date(if (meterState.startTimeMillis > 0) meterState.startTimeMillis else System.currentTimeMillis()))

    val perPersonFare = if (passengerCount > 0) (meterState.totalFare + passengerCount - 1) / passengerCount else meterState.totalFare

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
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CockpitCardBorder, RoundedCornerShape(24.dp))
                .testTag("receipt_share_dialog"),
            color = CockpitCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🚕",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "택시 이용 영수증",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = dateStr,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_receipt")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Ticket Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CockpitSurface,
                    border = BorderStroke(1.dp, CockpitCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Summary bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "주행 거리", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = ShareReceiptHelper.formatDistance(meterState.distanceMeters),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MeterGreenBright
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "운행 시간", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = ShareReceiptHelper.formatDuration(meterState.elapsedSeconds),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MeterCyanBright
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = CockpitCardBorder
                        )

                        // Fare Breakdown List
                        ReceiptLine(label = "기본 요금", amount = meterState.baseFare)
                        if (meterState.distanceFare > 0) {
                            ReceiptLine(label = "거리 요금", amount = meterState.distanceFare)
                        }
                        if (meterState.timeFare > 0) {
                            ReceiptLine(label = "시간/지체 요금", amount = meterState.timeFare)
                        }
                        if (meterState.surchargeFare > 0) {
                            ReceiptLine(
                                label = "할증 (${meterState.surchargeDescription})",
                                amount = meterState.surchargeFare,
                                labelColor = MeterPurple
                            )
                        }
                        if (meterState.tollFee > 0) {
                            ReceiptLine(
                                label = "통행료 / 추가금",
                                amount = meterState.tollFee,
                                labelColor = MeterAmber
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.5.dp,
                            color = MeterAmber.copy(alpha = 0.5f)
                        )

                        // Total Fare
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "총 결제 금액",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${numberFormat.format(meterState.totalFare)}원",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MeterAmberBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1/N Dutch Pay Calculator Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F1A28),
                    border = BorderStroke(1.dp, MeterCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MeterCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1/N 더치페이 정산 계산기",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeterCyanBright
                            )
                        }

                        // Passenger selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "탑승 인원",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (passengerCount > 1) onPassengerCountChange(passengerCount - 1) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CockpitSurface, CircleShape)
                                        .border(1.dp, CockpitCardBorder, CircleShape)
                                        .testTag("btn_minus_passenger")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "인원 감소",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "$passengerCount 명",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = { if (passengerCount < 10) onPassengerCountChange(passengerCount + 1) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CockpitSurface, CircleShape)
                                        .border(1.dp, CockpitCardBorder, CircleShape)
                                        .testTag("btn_plus_passenger")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "인원 증가",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Quick buttons: 2명, 3명, 4명
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                val isSelected = passengerCount == count
                                OutlinedButton(
                                    onClick = { onPassengerCountChange(count) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MeterCyan.copy(alpha = 0.25f) else CockpitSurface,
                                        contentColor = if (isSelected) MeterCyanBright else TextSecondary
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MeterCyan else CockpitCardBorder)
                                ) {
                                    Text(text = "${count}인 정산", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Per Person Split Amount Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitLcdDark)
                                .border(1.dp, Color(0xFF1B3248), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1인당 분담금",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${numberFormat.format(perPersonFare)}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = MeterCyanBright
                                    )
                                    Text(
                                        text = "원",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account & Route Note Inputs
                OutlinedTextField(
                    value = savedAccountInfo,
                    onValueChange = onAccountInfoChange,
                    label = { Text("입금 계좌 / 카카오페이 / 토스 안내 (선택)") },
                    placeholder = { Text("예: 카카오뱅크 3333-12-3456789 홍길동") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = TextMuted)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MeterCyan,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_info")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = routeMemo,
                    onValueChange = onRouteMemoChange,
                    label = { Text("운행 구간 / 메모 (선택)") },
                    placeholder = { Text("예: 강남역 11번 출구 -> 홍대입구") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = TextMuted)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MeterAmber,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_route_memo")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Share & Action Buttons
                Button(
                    onClick = {
                        ShareReceiptHelper.shareViaSns(
                            context = context,
                            text = shareText,
                            title = "택시 요금 및 정산 내역 공유"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_sns_share"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeterAmber,
                        contentColor = Color(0xFF1E1600)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SNS로 정산 영수증 공유하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy to clipboard
                    OutlinedButton(
                        onClick = {
                            ShareReceiptHelper.copyToClipboard(context, shareText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_copy_clipboard"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CockpitSurface,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, CockpitCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MeterCyanBright
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "정산 문구 복사", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Start new ride
                    OutlinedButton(
                        onClick = {
                            onNewRide()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_new_ride_from_dialog"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CockpitSurface,
                            contentColor = MeterGreenBright
                        ),
                        border = BorderStroke(1.dp, MeterGreen.copy(alpha = 0.5f))
                    ) {
                        Text(text = "새로운 주행 (빈차)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
        Text(text = label, fontSize = 13.sp, color = labelColor)
        Text(
            text = "${numberFormat.format(amount)}원",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary
        )
    }
}
