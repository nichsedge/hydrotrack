package com.sans.hydrotrack.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HydrationRepository {
    suspend fun addEntry(amountMl: Int, source: String? = null)
    suspend fun deleteEntry(entry: WaterEntry)
    fun dayEntries(date: LocalDate): Flow<List<WaterEntry>>
    fun dayTotal(date: LocalDate): Flow<Int>
    fun history(start: LocalDate, end: LocalDate): Flow<List<WaterEntry>>
}
