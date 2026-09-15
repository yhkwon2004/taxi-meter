package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxiTripDao {
    @Query("SELECT * FROM taxi_trips ORDER BY startTimeMillis DESC")
    fun getAllTrips(): Flow<List<TaxiTripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TaxiTripEntity): Long

    @Delete
    suspend fun deleteTrip(trip: TaxiTripEntity)

    @Query("DELETE FROM taxi_trips WHERE id = :id")
    suspend fun deleteTripById(id: Long)

    @Query("DELETE FROM taxi_trips")
    suspend fun clearAllTrips()
}
