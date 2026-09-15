package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.MeterStatus
import com.example.ui.FareSettingsDialog
import com.example.ui.ReceiptShareDialog
import com.example.ui.TaxiMeterControls
import com.example.ui.TaxiMeterDisplay
import com.example.ui.TripHistoryDialog
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterGreenBright
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CockpitBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🚕 택시 미터기",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )

                        // GPS / Simulation Pill Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (meterState.isSimulationMode) MeterCyan.copy(alpha = 0.2f) else MeterGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (meterState.isSimulationMode) MeterCyan else MeterGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (meterState.isSimulationMode) MeterCyan else MeterGreenBright)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (meterState.isSimulationMode) "모의주행" else "GPS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (meterState.isSimulationMode) MeterCyan else MeterGreenBright
                                )
                            }
                        }
                    }
                },
                actions = {
                    // History button with count badge
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("top_bar_history")
                    ) {
                        BadgedBox(
                            badge = {
                                if (tripHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = MeterAmber,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = tripHistory.size.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
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

                    // Rate Settings button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("top_bar_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "요금 설정",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CockpitBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Meter Cockpit Display
            TaxiMeterDisplay(meterState = meterState)

            // Tactile Controls & Surcharges
            TaxiMeterControls(
                meterState = meterState,
                onStartRide = { viewModel.startRide() },
                onPauseRide = { viewModel.pauseRide() },
                onResumeRide = { viewModel.resumeRide() },
                onEndRide = { viewModel.endRide() },
                onResetToVacant = { viewModel.resetToVacant() },
                onToggleNightSurcharge = { viewModel.toggleNightSurcharge() },
                onToggleOutOfCity = { viewModel.toggleOutOfCitySurcharge() },
                onAddTollFee = { amount -> viewModel.addTollFee(amount) },
                onToggleSimulation = { enabled -> viewModel.setSimulationMode(enabled) },
                onSetSimulationSpeed = { speed -> viewModel.setSimulationSpeed(speed) },
                onOpenReceipt = { showReceiptDialog = true }
            )
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
