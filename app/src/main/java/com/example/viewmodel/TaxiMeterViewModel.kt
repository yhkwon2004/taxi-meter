package com.example.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TaxiDatabase
import com.example.data.TaxiTripEntity
import com.example.data.TaxiTripRepository
import com.example.model.CurrentMeterState
import com.example.model.MeterStatus
import com.example.model.RegionPreset
import com.example.model.TaxiFareConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.roundToInt

class TaxiMeterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaxiTripRepository
    val tripHistory: StateFlow<List<TaxiTripEntity>>

    private val _meterState = MutableStateFlow(CurrentMeterState())
    val meterState: StateFlow<CurrentMeterState> = _meterState.asStateFlow()

    private val _passengerCount = MutableStateFlow(2)
    val passengerCount: StateFlow<Int> = _passengerCount.asStateFlow()

    private val _accountInfo = MutableStateFlow("")
    val accountInfo: StateFlow<String> = _accountInfo.asStateFlow()

    private val _routeMemo = MutableStateFlow("")
    val routeMemo: StateFlow<String> = _routeMemo.asStateFlow()

    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private var lastLocation: Location? = null
    private var slowDurationSeconds: Long = 0L

    private var tickerJob: Job? = null

    private val prefs = application.getSharedPreferences("taxi_meter_prefs", Context.MODE_PRIVATE)

    init {
        val database = TaxiDatabase.getInstance(application)
        repository = TaxiTripRepository(database.tripDao())
        tripHistory = repository.allTrips.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        _accountInfo.value = prefs.getString("saved_account_info", "") ?: ""

        // Check current hour for auto night surcharge suggestion
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 22 || currentHour < 4) {
            val rate = if (currentHour >= 23 || currentHour < 2) 40 else 20
            _meterState.value = _meterState.value.copy(
                isNightSurcharge = true,
                nightSurchargeRate = rate
            )
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (_meterState.value.status != MeterStatus.DRIVING || _meterState.value.isSimulationMode) {
                return
            }
            if (location.hasAccuracy() && location.accuracy > 40f) {
                // Jitter filtering
                return
            }

            val prev = lastLocation
            lastLocation = location

            if (prev != null) {
                val deltaMeters = prev.distanceTo(location).toDouble()
                if (deltaMeters in 0.5..150.0) { // realistic delta within 1-2 sec
                    var speedKmh = if (location.hasSpeed()) location.speed * 3.6 else (deltaMeters / 1.0) * 3.6
                    if (speedKmh < 0) speedKmh = 0.0

                    updateDrivingProgress(deltaDistanceMeters = deltaMeters, currentSpeed = speedKmh)
                }
            } else {
                val speedKmh = if (location.hasSpeed()) location.speed * 3.6 else 0.0
                _meterState.value = _meterState.value.copy(
                    currentSpeedKmh = speedKmh,
                    gpsProviderStatus = "GPS 수신 중"
                )
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            _meterState.value = _meterState.value.copy(gpsEnabled = true, gpsProviderStatus = "GPS 활성화")
        }
        override fun onProviderDisabled(provider: String) {
            _meterState.value = _meterState.value.copy(gpsEnabled = false, gpsProviderStatus = "GPS 꺼짐")
        }
    }

    fun startRide() {
        val now = System.currentTimeMillis()
        slowDurationSeconds = 0L
        lastLocation = null

        _meterState.value = _meterState.value.copy(
            status = MeterStatus.DRIVING,
            startTimeMillis = now,
            endTimeMillis = 0L,
            elapsedSeconds = 0L,
            distanceMeters = 0.0,
            currentSpeedKmh = if (_meterState.value.isSimulationMode) _meterState.value.simulationSpeedOption.toDouble() else 0.0,
            distanceFare = 0,
            timeFare = 0,
            surchargeFare = 0,
            tollFee = 0
        )
        recalculateFare(distance = 0.0, slowSeconds = 0L)
        startTicker()
        vibrateFeedback(50)
    }

    fun pauseRide() {
        if (_meterState.value.status == MeterStatus.DRIVING) {
            _meterState.value = _meterState.value.copy(
                status = MeterStatus.PAUSED,
                currentSpeedKmh = 0.0
            )
            vibrateFeedback(30)
        }
    }

    fun resumeRide() {
        if (_meterState.value.status == MeterStatus.PAUSED) {
            _meterState.value = _meterState.value.copy(
                status = MeterStatus.DRIVING,
                currentSpeedKmh = if (_meterState.value.isSimulationMode) _meterState.value.simulationSpeedOption.toDouble() else 0.0
            )
            vibrateFeedback(30)
        }
    }

    fun endRide() {
        val now = System.currentTimeMillis()
        tickerJob?.cancel()
        tickerJob = null

        val currentState = _meterState.value
        val finalState = currentState.copy(
            status = MeterStatus.PAYMENT,
            endTimeMillis = now,
            currentSpeedKmh = 0.0
        )
        _meterState.value = finalState

        vibrateFeedback(100)

        // Save trip to Room Database
        viewModelScope.launch {
            val count = _passengerCount.value
            val perPerson = if (count > 0) (finalState.totalFare + count - 1) / count else finalState.totalFare
            val record = TaxiTripEntity(
                startTimeMillis = finalState.startTimeMillis,
                endTimeMillis = now,
                distanceMeters = finalState.distanceMeters,
                durationSeconds = finalState.elapsedSeconds,
                baseFare = finalState.baseFare,
                distanceFare = finalState.distanceFare,
                timeFare = finalState.timeFare,
                surchargeFare = finalState.surchargeFare,
                tollFee = finalState.tollFee,
                totalFare = finalState.totalFare,
                surchargeDescription = finalState.surchargeDescription,
                passengerCount = count,
                splitFarePerPerson = perPerson,
                memo = _routeMemo.value
            )
            repository.saveTrip(record)
        }
    }

    fun resetToVacant() {
        tickerJob?.cancel()
        tickerJob = null
        slowDurationSeconds = 0L
        lastLocation = null

        val config = _meterState.value.config
        _meterState.value = _meterState.value.copy(
            status = MeterStatus.VACANT,
            startTimeMillis = 0L,
            endTimeMillis = 0L,
            elapsedSeconds = 0L,
            distanceMeters = 0.0,
            currentSpeedKmh = 0.0,
            baseFare = config.baseFare,
            distanceFare = 0,
            timeFare = 0,
            surchargeFare = 0,
            tollFee = 0,
            totalFare = config.baseFare
        )
        _routeMemo.value = ""
        vibrateFeedback(40)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (_meterState.value.status == MeterStatus.DRIVING) {
                    val current = _meterState.value
                    val newElapsed = current.elapsedSeconds + 1

                    if (current.isSimulationMode) {
                        // In simulation mode, advance simulated distance every second
                        val speedKmh = current.simulationSpeedOption.toDouble()
                        val deltaMeters = (speedKmh * 1000.0) / 3600.0
                        val newDistance = current.distanceMeters + deltaMeters
                        val isSlow = speedKmh < current.config.slowSpeedKmhThreshold
                        if (isSlow) slowDurationSeconds++

                        _meterState.value = current.copy(
                            elapsedSeconds = newElapsed,
                            distanceMeters = newDistance,
                            currentSpeedKmh = speedKmh
                        )
                        recalculateFare(newDistance, slowDurationSeconds)
                    } else {
                        // Real GPS / timer ticker: check if current speed is slow
                        val isSlow = current.currentSpeedKmh < current.config.slowSpeedKmhThreshold
                        if (isSlow) slowDurationSeconds++

                        _meterState.value = current.copy(elapsedSeconds = newElapsed)
                        recalculateFare(current.distanceMeters, slowDurationSeconds)
                    }
                }
            }
        }
    }

    private fun updateDrivingProgress(deltaDistanceMeters: Double, currentSpeed: Double) {
        val current = _meterState.value
        val newDistance = current.distanceMeters + deltaDistanceMeters
        _meterState.value = current.copy(
            distanceMeters = newDistance,
            currentSpeedKmh = currentSpeed
        )
        val isSlow = currentSpeed < current.config.slowSpeedKmhThreshold
        if (isSlow) slowDurationSeconds++

        recalculateFare(newDistance, slowDurationSeconds)
    }

    private fun recalculateFare(distance: Double, slowSeconds: Long) {
        val state = _meterState.value
        val config = state.config

        val baseFare = config.baseFare

        // Distance Fare
        var distFare = 0
        if (distance > config.baseDistanceMeters) {
            val extraMeters = distance - config.baseDistanceMeters
            val steps = ceil(extraMeters / config.distanceUnitMeters).toInt()
            distFare = steps * config.distanceUnitFare
        }

        // Time Fare (slow speed / waiting)
        var timeFare = 0
        if (slowSeconds > 0) {
            val timeSteps = (slowSeconds / config.timeUnitSeconds).toInt()
            timeFare = timeSteps * config.timeUnitFare
        }

        // Surcharge
        val subtotal = baseFare + distFare + timeFare
        val surchargePercent = state.totalSurchargePercent
        val surchargeRaw = (subtotal * (surchargePercent / 100.0)).roundToInt()
        // Round surcharge to nearest 10 KRW
        val surchargeFare = (surchargeRaw / 10) * 10

        val totalFare = subtotal + surchargeFare + state.tollFee

        val oldTotal = state.totalFare
        if (totalFare > oldTotal && state.status == MeterStatus.DRIVING) {
            // Slight tick vibration on fare increase!
            vibrateFeedback(15)
        }

        _meterState.value = state.copy(
            baseFare = baseFare,
            distanceFare = distFare,
            timeFare = timeFare,
            surchargeFare = surchargeFare,
            totalFare = totalFare
        )
    }

    fun toggleNightSurcharge() {
        val current = _meterState.value
        val (nextActive, nextRate) = when {
            !current.isNightSurcharge -> Pair(true, 20)
            current.nightSurchargeRate == 20 -> Pair(true, 40)
            else -> Pair(false, 20)
        }
        _meterState.value = current.copy(
            isNightSurcharge = nextActive,
            nightSurchargeRate = nextRate
        )
        recalculateFare(_meterState.value.distanceMeters, slowDurationSeconds)
        vibrateFeedback(25)
    }

    fun toggleOutOfCitySurcharge() {
        val current = _meterState.value
        _meterState.value = current.copy(isOutOfCitySurcharge = !current.isOutOfCitySurcharge)
        recalculateFare(_meterState.value.distanceMeters, slowDurationSeconds)
        vibrateFeedback(25)
    }

    fun addTollFee(amount: Int) {
        val current = _meterState.value
        val newToll = (current.tollFee + amount).coerceAtLeast(0)
        _meterState.value = current.copy(tollFee = newToll)
        recalculateFare(current.distanceMeters, slowDurationSeconds)
        vibrateFeedback(25)
    }

    fun setSimulationMode(enabled: Boolean) {
        val current = _meterState.value
        _meterState.value = current.copy(
            isSimulationMode = enabled,
            currentSpeedKmh = if (enabled && current.status == MeterStatus.DRIVING) current.simulationSpeedOption.toDouble() else 0.0
        )
    }

    fun setSimulationSpeed(speedKmh: Int) {
        _meterState.value = _meterState.value.copy(
            simulationSpeedOption = speedKmh,
            currentSpeedKmh = if (_meterState.value.status == MeterStatus.DRIVING) speedKmh.toDouble() else 0.0
        )
    }

    fun setPassengerCount(count: Int) {
        _passengerCount.value = count.coerceIn(1, 10)
    }

    fun setAccountInfo(info: String) {
        _accountInfo.value = info
        prefs.edit().putString("saved_account_info", info).apply()
    }

    fun setRouteMemo(memo: String) {
        _routeMemo.value = memo
    }

    fun setPreset(preset: RegionPreset) {
        val newConfig = when (preset) {
            RegionPreset.SEOUL -> TaxiFareConfig.SEOUL
            RegionPreset.REGIONAL -> TaxiFareConfig.REGIONAL
            RegionPreset.DELUXE -> TaxiFareConfig.DELUXE
            RegionPreset.CUSTOM -> _meterState.value.config.copy(preset = RegionPreset.CUSTOM)
        }
        _meterState.value = _meterState.value.copy(config = newConfig, baseFare = newConfig.baseFare)
        recalculateFare(_meterState.value.distanceMeters, slowDurationSeconds)
    }

    fun updateCustomConfig(config: TaxiFareConfig) {
        _meterState.value = _meterState.value.copy(config = config, baseFare = config.baseFare)
        recalculateFare(_meterState.value.distanceMeters, slowDurationSeconds)
    }

    @SuppressLint("MissingPermission")
    fun startGpsTracking() {
        val lm = locationManager ?: return
        try {
            val isGpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetOn = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            _meterState.value = _meterState.value.copy(gpsEnabled = isGpsOn || isNetOn)

            if (isGpsOn) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 2f, locationListener)
            } else if (isNetOn) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 2f, locationListener)
            }
        } catch (_: SecurityException) {
            _meterState.value = _meterState.value.copy(gpsProviderStatus = "위치 권한 필요")
        }
    }

    fun stopGpsTracking() {
        locationManager?.removeUpdates(locationListener)
    }

    fun deleteTrip(trip: TaxiTripEntity) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    fun clearAllTrips() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun vibrateFeedback(millis: Long) {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(millis)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        stopGpsTracking()
        tickerJob?.cancel()
    }
}
