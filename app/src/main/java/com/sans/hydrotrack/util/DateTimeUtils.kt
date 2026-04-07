package com.sans.hydrotrack.util

import java.time.LocalDate
import java.time.ZoneId

object DateTimeUtils {
    data class Bounds(val startMillis: Long, val endMillis: Long)

    fun dayBounds(date: LocalDate, zoneId: ZoneId): Bounds {
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return Bounds(start, end)
    }

    fun rangeBounds(startDate: LocalDate, endDate: LocalDate, zoneId: ZoneId): Bounds {
        val start = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return Bounds(start, end)
    }
}
