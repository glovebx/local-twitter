package xyz.mirage.app.presentation.core.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object DateUtils {

//    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val dateFormatter = SimpleDateFormat("dd MMM yy", Locale.ENGLISH)
    private val currentYearFormatter = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    private val joinedFormatter = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

    fun relativeTime(string: String): String {

        val current = Date()
        val date = stringToDate(string)

        val diffInMillies: Long = abs(current.time - date.time)
        val diffMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS)

        val cal1 = Calendar.getInstance()
        cal1.time = current
        val cal2 = Calendar.getInstance()
        cal2.time = date

        val sameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)

        return when {
            diffMinutes < 60 -> {
                "${diffMinutes}m"
            }
            diffMinutes < 1440 -> {
                "${diffMinutes / 60}h"
            }
            sameYear -> {
                currentYearFormatter.format(date)
            }
            else -> {
                timeFormatter.format(date)
            }
        }
    }

    fun formatDate(string: String): String {
        val date = stringToDate(string)
        return "${timeFormatter.format(date)} · ${dateFormatter.format(date)}"
    }

    private fun stringToDate(string: String): Date {
        return sdf.parse(string)
            ?: throw NullPointerException("Could not convert date string to Date object.")
    }

    fun createTimestamp(): Long {
        return Date().time / 1000
    }

    fun getJoinTime(time: String): String {
        val date = stringToDate(time)
        return joinedFormatter.format(date)
    }
}