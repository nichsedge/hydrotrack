package com.sans.hydrotrack.util

object UnitUtils {
    private const val ML_PER_OUNCE = 29.5735f

    fun mlToOunces(ml: Int): Float = ml / ML_PER_OUNCE

    fun ouncesToMl(oz: Float): Int = (oz * ML_PER_OUNCE).toInt()
}
