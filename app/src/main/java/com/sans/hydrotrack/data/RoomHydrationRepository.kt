package com.sans.hydrotrack.data

import com.sans.hydrotrack.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class RoomHydrationRepository(
    private val dao: WaterEntryDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : HydrationRepository {
    override suspend fun addEntry(amountMl: Int, source: String?) {
        val now = System.currentTimeMillis()
        dao.insert(
            WaterEntry(
                timestamp = now,
                amountMl = amountMl,
                source = source,
            )
        )
    }

    override suspend fun deleteEntry(entry: WaterEntry) {
        dao.delete(entry)
    }

    override fun dayEntries(date: LocalDate): Flow<List<WaterEntry>> {
        val bounds = DateTimeUtils.dayBounds(date, zoneId)
        return dao.entriesForDay(bounds.startMillis, bounds.endMillis)
    }

    override fun dayTotal(date: LocalDate): Flow<Int> {
        val bounds = DateTimeUtils.dayBounds(date, zoneId)
        return dao.totalForDay(bounds.startMillis, bounds.endMillis)
            .map { it ?: 0 }
    }

    override fun history(start: LocalDate, end: LocalDate): Flow<List<WaterEntry>> {
        val bounds = DateTimeUtils.rangeBounds(start, end, zoneId)
        return dao.entriesForRange(bounds.startMillis, bounds.endMillis)
    }
}
