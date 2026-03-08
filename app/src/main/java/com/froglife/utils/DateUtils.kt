package com.froglife.utils

import java.util.*

object DateUtils {
    fun getStartOfDay(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDay(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun getStartOfWeek(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return getStartOfDay(calendar.time)
    }

    fun getEndOfWeek(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        return getEndOfDay(calendar.time)
    }

    fun getStartOfMonth(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(calendar.time)
    }

    fun getEndOfMonth(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return getEndOfDay(calendar.time)
    }

    fun getStartOfYear(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        return getStartOfDay(calendar.time)
    }

    fun getEndOfYear(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        return getEndOfDay(calendar.time)
    }
}
