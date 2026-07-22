package com.example.gruya.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayFormat = SimpleDateFormat("d 'de' MMMM, HH:mm", Locale("es", "ES"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("es", "ES"))

    fun formatIsoToDisplay(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        val date = parseIso(isoString) ?: return isoString
        return displayFormat.format(date)
    }

    fun formatIsoToTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        val date = parseIso(isoString) ?: return isoString
        return timeFormat.format(date)
    }

    fun formatRelative(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        val date = parseIso(isoString) ?: return isoString
        
        return formatRelative(date)
    }

    fun formatRelative(timestamp: Long): String {
        return formatRelative(Date(timestamp))
    }

    private fun formatRelative(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        
        return when {
            diff < 0 -> "Recién"
            diff < 60 * 1000 -> "Hace un momento"
            diff < 60 * 60 * 1000 -> "Hace ${diff / (60 * 1000)} min"
            isSameDay(date, now) -> "Hoy, ${timeFormat.format(date)}"
            isYesterday(date, now) -> "Ayer, ${timeFormat.format(date)}"
            else -> displayFormat.format(date)
        }
    }

    fun formatEtaToArrivalTime(etaMinutes: Double?): String {
        if (etaMinutes == null || etaMinutes <= 0) return ""
        val now = Date()
        val arrivalDate = Date(now.time + (etaMinutes * 60 * 1000).toLong())
        return timeFormat.format(arrivalDate)
    }

    private fun parseIso(isoString: String): Date? {
        // Cleaning the string to handle Z or offset
        val cleanString = isoString.split(".")[0].replace("Z", "")
        return try {
            isoFormat.parse(cleanString)
        } catch (e: Exception) {
            null
        }
    }

    fun isToday(isoString: String?): Boolean {
        if (isoString.isNullOrBlank()) return false
        val date = parseIso(isoString) ?: return false
        return isSameDay(date, Date())
    }

    fun isInCurrentWeek(isoString: String?): Boolean {
        if (isoString.isNullOrBlank()) return false
        val date = parseIso(isoString) ?: return false
        val now = Date()
        val cal1 = java.util.Calendar.getInstance().apply { time = date }
        val cal2 = java.util.Calendar.getInstance().apply { time = now }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.WEEK_OF_YEAR) == cal2.get(java.util.Calendar.WEEK_OF_YEAR)
    }

    fun isInCurrentMonth(isoString: String?): Boolean {
        if (isoString.isNullOrBlank()) return false
        val date = parseIso(isoString) ?: return false
        val now = Date()
        val cal1 = java.util.Calendar.getInstance().apply { time = date }
        val cal2 = java.util.Calendar.getInstance().apply { time = now }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US)
        return fmt.format(date1) == fmt.format(date2)
    }

    private fun isYesterday(date: Date, now: Date): Boolean {
        val yesterday = Date(now.time - 24 * 60 * 60 * 1000)
        return isSameDay(date, yesterday)
    }
}
