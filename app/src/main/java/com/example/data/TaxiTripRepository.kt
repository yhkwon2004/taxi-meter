package com.example.data

import kotlinx.coroutines.flow.Flow

class TaxiTripRepository(private val dao: TaxiTripDao) {
    val allTrips: Flow<List<TaxiTripEntity>> = dao.getAllTrips()

    suspend fun saveTrip(trip: TaxiTripEntity): Long {
        return dao.insertTrip(trip)
    }

    suspend fun deleteTrip(trip: TaxiTripEntity) {
        dao.deleteTrip(trip)
    }

    suspend fun deleteTripById(id: Long) {
        dao.deleteTripById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllTrips()
    }
}
