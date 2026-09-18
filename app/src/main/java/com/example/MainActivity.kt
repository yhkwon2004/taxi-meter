package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CurrentMeterState
import com.example.model.MeterStatus
import com.example.ui.FareSettingsDialog
import com.example.ui.ReceiptShareDialog
import com.example.ui.TaxiMeterActionBar
import com.example.ui.TaxiMeterControls
import com.example.ui.TaxiMeterDisplay
import com.example.ui.TripHistoryDialog
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitBackgroundTop
import com.example.ui.theme.CockpitCardBorderSoft
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterCyanBright
import com.example.ui.theme.MeterShapes
import com.example.ui.theme.MeterSizes
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Space
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.TaxiMeterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaxiMeterApp()
            }
        }
    }
}

@Composable
fun TaxiMeterApp(viewModel: TaxiMeterViewModel = viewModel()) {
    val context = LocalContext.current
    val meterState by viewModel.meterState.collectAsStateWithLifecycle()
    val tripHistory by viewModel.tripHistory.collectAsStateWithLifecycle()
    val passengerCount by viewModel.passengerCount.collectAsStateWithLifecycle()
    val savedAccountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val routeMemo by viewModel.routeMemo.collectAsStateWithLifecycle()

    var showReceiptDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Location Permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startGpsTracking()
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.startGpsTracking()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGpsTracking()
        }
    }

    // Auto-popup receipt dialog when ride ends
    LaunchedEffect(meterState.status) {
        if (meterState.status == MeterStatus.PAYMENT) {
            showReceiptDialog = true
        }
    }

    // The cockpit sits on a vertical gradient: slightly lit at the windscreen
    // end, falling away to near-black at the bottom of the panel.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to CockpitBackgroundTop,
                    0.45f to CockpitBackground,
                    1f to CockpitBackground
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                MeterTopBar(
                    meterState = meterState,
                    tripCount = tripHistory.size,
                    onOpenHistory = { showHistoryDialog = true },
                    onOpenSettings = { showSettingsDialog = true }
                )
            },
            bottomBar = {
                // Driving actions stay pinned within thumb reach instead of
                // drifting with the scroll position.
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MeterSizes.hairline)
                            .background(CockpitCardBorderSoft)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CockpitBackground)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = Space.xl, vertical = Space.lg)
                    ) {
                        TaxiMeterActionBar(
                            meterState = meterState,
                            onStartRide = { viewModel.startRide() },
                            onPauseRide = { viewModel.pauseRide() },
                            onResumeRide = { viewModel.resumeRide() },
                            onEndRide = { viewModel.endRide() },
                            onResetToVacant = { viewModel.resetToVacant() },
                            onOpenReceipt = { showReceiptDialog = true }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.xl, vertical = Space.xl),
                verticalArrangement = Arrangement.spacedBy(Space.lg)
            ) {
                TaxiMeterDisplay(meterState = meterState)

                TaxiMeterControls(
                    meterState = meterState,
                    onToggleNightSurcharge = { viewModel.toggleNightSurcharge() },
                    onToggleOutOfCity = { viewModel.toggleOutOfCitySurcharge() },
                    onAddTollFee = { amount -> viewModel.addTollFee(amount) },
                    onToggleSimulation = { enabled -> viewModel.setSimulationMode(enabled) },
                    onSetSimulationSpeed = { speed -> viewModel.setSimulationSpeed(speed) }
                )
            }
        }
    }

    // Receipt & SNS Sharing Dialog
    if (showReceiptDialog) {
        ReceiptShareDialog(
            meterState = meterState,
            passengerCount = passengerCount,
            savedAccountInfo = savedAccountInfo,
            routeMemo = routeMemo,
            onPassengerCountChange = { viewModel.setPassengerCount(it) },
            onAccountInfoChange = { viewModel.setAccountInfo(it) },
            onRouteMemoChange = { viewModel.setRouteMemo(it) },
            onDismiss = { showReceiptDialog = false },
            onNewRide = {
                viewModel.resetToVacant()
                showReceiptDialog = false
            }
        )
    }

    // Trip History Dialog
    if (showHistoryDialog) {
        TripHistoryDialog(
            trips = tripHistory,
            savedAccountInfo = savedAccountInfo,
            onDeleteTrip = { viewModel.deleteTrip(it) },
            onClearAllTrips = { viewModel.clearAllTrips() },
            onDismiss = { showHistoryDialog = false }
        )
    }

    // Fare Rates Settings Dialog
    if (showSettingsDialog) {
        FareSettingsDialog(
            currentConfig = meterState.config,
            onSelectPreset = { viewModel.setPreset(it) },
            onSaveCustomConfig = { viewModel.updateCustomConfig(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

/**
 * Header: identity on the left, and directly under it the one thing a driver
 * needs to trust the reading — where the distance is coming from.
 */
@Composable
private fun MeterTopBar(
    meterState: CurrentMeterState,
    tripCount: Int,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Space.xl, end = Space.md, top = Space.md, bottom = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(MeterShapes.chip)
                    .background(MeterAmber.copy(alpha = 0.14f))
                    .border(MeterSizes.hairline, MeterAmber.copy(alpha = 0.35f), MeterShapes.chip),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚕", fontSize = 17.sp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Space.lg)
            ) {
                Text(
                    text = "택시 미터기",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (meterState.isSimulationMode) {
                        "모의 주행 모드"
                    } else {
                        meterState.gpsProviderStatus
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (meterState.isSimulationMode) MeterCyanBright else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("top_bar_history")
            ) {
                BadgedBox(
                    badge = {
                        if (tripCount > 0) {
                            Badge(
                                containerColor = MeterAmber,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = tripCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "운행 기록",
                        tint = TextPrimary
                    )
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("top_bar_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "요금 설정",
                    tint = TextPrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MeterSizes.hairline)
                .background(CockpitCardBorderSoft)
        )
    }
}
