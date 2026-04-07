package com.sans.hydrotrack.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HydrationRepository {
    suspend fun addEntry(amountMl: Int, source: String? = null)
    suspend fun deleteEntry(entry: WaterEntry)
    fun dayEntries(date: LocalDate): Flow<List<WaterEntry>>
    fun dayTotal(date: LocalDate): Flow<Int>
    fun history(start: LocalDate, end: LocalDate): Flow<List<WaterEntry>>
}
