package com.sans.hydrotrack.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WaterEntry::class],
    version = 1,
    exportSchema = false,
)
abstract class HydroTrackDatabase : RoomDatabase() {
    abstract fun waterEntryDao(): WaterEntryDao
}
