package com.sans.hydrotrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {
    @Insert
    suspend fun insert(entry: WaterEntry)

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query(
        "SELECT * FROM water_entries " +
            "WHERE timestamp BETWEEN :start AND :end " +
            "ORDER BY timestamp ASC"
    )
    fun entriesForDay(start: Long, end: Long): Flow<List<WaterEntry>>

    @Query(
        "SELECT SUM(amountMl) FROM water_entries " +
            "WHERE timestamp BETWEEN :start AND :end"
    )
    fun totalForDay(start: Long, end: Long): Flow<Int?>

    @Query(
        "SELECT * FROM water_entries " +
            "WHERE timestamp BETWEEN :start AND :end " +
            "ORDER BY timestamp DESC"
    )
    fun entriesForRange(start: Long, end: Long): Flow<List<WaterEntry>>
}
