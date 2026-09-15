package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TaxiTripEntity
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MeterRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareReceiptHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripHistoryDialog(
    trips: List<TaxiTripEntity>,
    savedAccountInfo: String,
    onDeleteTrip: (TaxiTripEntity) -> Unit,
    onClearAllTrips: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    var tripToShare by remember { mutableStateOf<TaxiTripEntity?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val totalSpent = trips.sumOf { it.totalFare }
    val totalDistance = trips.sumOf { it.distanceMeters }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CockpitCardBorder, RoundedCornerShape(24.dp))
                .testTag("trip_history_dialog"),
            color = CockpitCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MeterAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "운행 기록 보관함",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CockpitSurface
                        ) {
                            Text(
                                text = "${trips.size}건",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row {
                        if (trips.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.testTag("btn_clear_all_trips")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "전체 삭제",
                                    tint = TextMuted
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // Summary Stats Bar
                if (trips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = CockpitSurface,
                        border = BorderStroke(1.dp, CockpitCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "총 이용 금액", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = "${numberFormat.format(totalSpent)}원",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MeterAmberBright
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "총 주행 거리", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = ShareReceiptHelper.formatDistance(totalDistance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MeterGreenBright
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trip Items List or Empty State
                if (trips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "아직 완료된 운행 기록이 없습니다",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "주행을 시작하고 종료하면 여기에 영수증이 자동 저장됩니다.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(trips, key = { it.id }) { trip ->
                            TripHistoryItemCard(
                                trip = trip,
                                onShare = {
                                    val text = ShareReceiptHelper.buildShareMessage(
                                        startTimeMillis = trip.startTimeMillis,
                                        endTimeMillis = trip.endTimeMillis,
                                        distanceMeters = trip.distanceMeters,
                                        durationSeconds = trip.durationSeconds,
                                        baseFare = trip.baseFare,
                                        distanceFare = trip.distanceFare,
                                        timeFare = trip.timeFare,
                                        surchargeFare = trip.surchargeFare,
                                        tollFee = trip.tollFee,
                                        totalFare = trip.totalFare,
                                        surchargeDesc = trip.surchargeDescription,
                                        passengerCount = trip.passengerCount,
                                        accountInfo = savedAccountInfo,
                                        memo = trip.memo
                                    )
                                    ShareReceiptHelper.shareViaSns(
                                        context = context,
                                        text = text,
                                        title = "과거 택시 영수증 재공유"
                                    )
                                },
                                onDelete = { onDeleteTrip(trip) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("기록 전체 삭제", color = TextPrimary) },
            text = { Text("저장된 모든 택시 운행 기록을 삭제하시겠습니까?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllTrips()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeterRed)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("취소", color = TextSecondary)
                }
            },
            containerColor = CockpitCard
        )
    }
}

@Composable
private fun TripHistoryItemCard(
    trip: TaxiTripEntity,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val dateFormat = SimpleDateFormat("MM.dd (E) HH:mm", Locale.KOREA)
    val dateStr = dateFormat.format(Date(trip.startTimeMillis))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CockpitSurface,
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
                        text = dateStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (trip.memo.isNotBlank()) {
                        Text(
                            text = trip.memo,
                            fontSize = 12.sp,
                            color = MeterCyanBright,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = "${numberFormat.format(trip.totalFare)}원",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MeterAmberBright
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📏 ${ShareReceiptHelper.formatDistance(trip.distanceMeters)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "⏱️ ${ShareReceiptHelper.formatDuration(trip.durationSeconds)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (trip.passengerCount > 1) {
                        Text(
                            text = "👥 ${trip.passengerCount}인 더치페이",
                            fontSize = 12.sp,
                            color = MeterCyan
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "영수증 공유",
                            tint = MeterAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
