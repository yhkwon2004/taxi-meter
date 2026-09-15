package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaxiTripEntity::class], version = 1, exportSchema = false)
abstract class TaxiDatabase : RoomDatabase() {
    abstract fun tripDao(): TaxiTripDao

    companion object {
        @Volatile
        private var INSTANCE: TaxiDatabase? = null

        fun getInstance(context: Context): TaxiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxiDatabase::class.java,
                    "taxi_meter.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
