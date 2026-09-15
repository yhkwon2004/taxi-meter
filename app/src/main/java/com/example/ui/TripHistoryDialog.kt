package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TaxiTripEntity
import com.example.ui.components.CockpitTile
import com.example.ui.components.EmptyState
import com.example.ui.components.MeterDialogShell
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.MeterType
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
 * Saved receipts, newest first, with the running totals on top — the two
 * numbers someone opening this screen is actually looking for.
 */
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
    var showClearConfirm by remember { mutableStateOf(false) }

    val totalSpent = trips.sumOf { it.totalFare }
    val totalDistance = trips.sumOf { it.distanceMeters }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MeterDialogShell(
            title = "운행 기록",
            subtitle = if (trips.isEmpty()) "저장된 영수증 없음" else "저장된 영수증 ${trips.size}건",
            icon = Icons.Default.History,
            accentColor = MeterAmber,
            onDismiss = onDismiss,
            closeButtonModifier = Modifier.testTag("btn_close_history"),
            headerActions = {
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
            },
            modifier = Modifier
                .fillMaxHeight(0.86f)
                .testTag("trip_history_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.xxl)
            ) {
                if (trips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Space.xl))
                    CockpitTile(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(Space.xl)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TotalsColumn(
                                modifier = Modifier.weight(1f),
                                label = "총 이용 금액",
                                value = "${numberFormat.format(totalSpent)}원",
                                accentColor = MeterAmberBright
                            )
                            Box(
                                modifier = Modifier
                                    .width(MeterSizes.hairline)
                                    .height(36.dp)
                                    .background(CockpitCardBorder)
                            )
                            TotalsColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = Space.xl),
                                label = "총 주행 거리",
                                value = ShareReceiptHelper.formatDistance(totalDistance),
                                accentColor = MeterGreenBright
                            )
                        }
                    }
                }

                if (trips.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.DirectionsCar,
                        title = "아직 완료된 운행 기록이 없습니다",
                        description = "주행을 시작하고 종료하면 영수증이 여기에 자동으로 저장됩니다.",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(top = Space.lg, bottom = Space.xxl),
                        verticalArrangement = Arrangement.spacedBy(Space.md)
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
            shape = MeterShapes.hero,
            containerColor = CockpitCard,
            title = {
                Text(
                    text = "기록 전체 삭제",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "저장된 모든 택시 운행 기록을 삭제하시겠습니까? 되돌릴 수 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllTrips()
                        showClearConfirm = false
                    },
                    shape = MeterShapes.chip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeterRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "삭제", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(
                        text = "취소",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun TotalsColumn(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MeterType.caption, color = TextMuted)
        Spacer(modifier = Modifier.height(Space.xs))
        Text(
            text = value,
            style = MeterType.lcdSmall,
            fontSize = 17.sp,
            color = accentColor,
            maxLines = 1
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

    // Distance, duration and split collapsed into one line: three separate
    // badges wrapped onto two rows on narrow phones.
    val metaLine = buildString {
        append(ShareReceiptHelper.formatDistance(trip.distanceMeters))
        append(" · ")
        append(ShareReceiptHelper.formatDuration(trip.durationSeconds))
    }

    CockpitTile(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Space.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                if (trip.memo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Space.xxs))
                    Text(
                        text = trip.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MeterCyanBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(Space.md))

            Text(
                text = "${numberFormat.format(trip.totalFare)}원",
                style = MeterType.lcdSmall,
                fontSize = 18.sp,
                color = MeterAmberBright
            )
        }

        Spacer(modifier = Modifier.height(Space.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = metaLine,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trip.passengerCount > 1) {
                Text(
                    text = " · ${trip.passengerCount}인 정산",
                    style = MaterialTheme.typography.bodySmall,
                    color = MeterCyanBright,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "영수증 공유",
                    tint = MeterAmber,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
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
