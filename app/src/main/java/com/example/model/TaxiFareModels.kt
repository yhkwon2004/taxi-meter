package com.example.model

enum class MeterStatus {
    VACANT,    // 빈차 (탑승 대기)
    DRIVING,   // 주행 (미터기 작동 중)
    PAUSED,    // 정차/대기 (일시정지)
    PAYMENT    // 지불/정산 완료 (영수증 및 공유 화면)
}

enum class RegionPreset(val displayName: String) {
    SEOUL("서울/수도권 일반"),
    REGIONAL("지방/광역시 일반"),
    DELUXE("모범/대형 택시"),
    CUSTOM("사용자 지정")
}

data class TaxiFareConfig(
    val preset: RegionPreset = RegionPreset.SEOUL,
    val baseFare: Int = 4800,
    val baseDistanceMeters: Int = 1600,
    val distanceUnitMeters: Int = 131,
    val distanceUnitFare: Int = 100,
    val timeUnitSeconds: Int = 30,
    val timeUnitFare: Int = 100,
    val slowSpeedKmhThreshold: Double = 15.0,
    val nightSurchargePercent: Int = 20,
    val outOfCitySurchargePercent: Int = 20
) {
    companion object {
        val SEOUL = TaxiFareConfig(
            preset = RegionPreset.SEOUL,
            baseFare = 4800,
            baseDistanceMeters = 1600,
            distanceUnitMeters = 131,
            distanceUnitFare = 100,
            timeUnitSeconds = 30,
            timeUnitFare = 100,
            slowSpeedKmhThreshold = 15.0,
            nightSurchargePercent = 20,
            outOfCitySurchargePercent = 20
        )

        val REGIONAL = TaxiFareConfig(
            preset = RegionPreset.REGIONAL,
            baseFare = 4000,
            baseDistanceMeters = 2000,
            distanceUnitMeters = 132,
            distanceUnitFare = 100,
            timeUnitSeconds = 31,
            timeUnitFare = 100,
            slowSpeedKmhThreshold = 15.0,
            nightSurchargePercent = 20,
            outOfCitySurchargePercent = 20
        )

        val DELUXE = TaxiFareConfig(
            preset = RegionPreset.DELUXE,
            baseFare = 7000,
            baseDistanceMeters = 3000,
            distanceUnitMeters = 151,
            distanceUnitFare = 200,
            timeUnitSeconds = 36,
            timeUnitFare = 200,
            slowSpeedKmhThreshold = 15.0,
            nightSurchargePercent = 20,
            outOfCitySurchargePercent = 20
        )
    }
}

data class CurrentMeterState(
    val status: MeterStatus = MeterStatus.VACANT,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val baseFare: Int = 4800,
    val distanceFare: Int = 0,
    val timeFare: Int = 0,
    val surchargeFare: Int = 0,
    val tollFee: Int = 0,
    val totalFare: Int = 4800,
    val isNightSurcharge: Boolean = false,
    val nightSurchargeRate: Int = 20, // 20% or 40% (peak 23~02시)
    val isOutOfCitySurcharge: Boolean = false,
    val isSimulationMode: Boolean = false,
    val simulationSpeedOption: Int = 50, // 30, 50, 80 km/h
    val gpsEnabled: Boolean = false,
    val gpsProviderStatus: String = "GPS 준비 완료",
    val config: TaxiFareConfig = TaxiFareConfig.SEOUL
) {
    val totalSurchargePercent: Int
        get() {
            var percent = 0
            if (isNightSurcharge) percent += nightSurchargeRate
            if (isOutOfCitySurcharge) percent += config.outOfCitySurchargePercent
            return percent
        }

    val surchargeDescription: String
        get() {
            val list = mutableListOf<String>()
            if (isNightSurcharge) list.add("심야(${nightSurchargeRate}%)")
            if (isOutOfCitySurcharge) list.add("시계외(${config.outOfCitySurchargePercent}%)")
            return if (list.isEmpty()) "일반" else list.joinToString(" + ")
        }
}
