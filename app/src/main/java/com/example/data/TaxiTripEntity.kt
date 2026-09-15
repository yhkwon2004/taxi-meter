package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxi_trips")
data class TaxiTripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val baseFare: Int,
    val distanceFare: Int,
    val timeFare: Int,
    val surchargeFare: Int,
    val tollFee: Int,
    val totalFare: Int,
    val surchargeDescription: String,
    val passengerCount: Int = 1,
    val splitFarePerPerson: Int = totalFare,
    val memo: String = ""
)
