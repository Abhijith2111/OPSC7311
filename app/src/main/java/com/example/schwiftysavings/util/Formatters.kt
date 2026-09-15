package com.example.schwiftysavings.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object MoneyFormat {
    /** Groups thousands with a space, e.g. 1234 -> "1 234" */
    private val grouping: NumberFormat = NumberFormat.getIntegerInstance(Locale.US).apply {
        isGroupingUsed = true
    }

    private fun formatRandAmount(cents: Long): String {
        val absCents = abs(cents)
        val whole = absCents / 100
        val frac = absCents % 100
        val wholeText = grouping.format(whole).replace(',', ' ')
        return "$wholeText.${frac.toString().padStart(2, '0')}"
    }

    /** Signed display for list rows, e.g. -R45.00 */
    fun zarFromCents(cents: Long): String {
        val sign = if (cents < 0) "-" else if (cents > 0) "+" else ""
        return "${sign}R${formatRandAmount(cents)}"
    }

    /** Unsigned display, e.g. R850.00 */
    fun zarUnsignedFromCents(cents: Long): String {
        return "R${formatRandAmount(cents)}"
    }
}

object DateUtils {
    private val displayDate: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH)

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun currentYearMonth(): String = YearMonth.now().toString()

    fun formatEpochDay(day: Long): String = LocalDate.ofEpochDay(day).format(displayDate)

    fun formatMinute(minute: Int): String {
        val h = minute / 60
        val m = minute % 60
        val amPm = if (h < 12) "AM" else "PM"
        val hour12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return "%d:%02d %s".format(hour12, m, amPm)
    }

    fun periodRange(period: PeriodFilter): Pair<Long, Long> {
        val today = LocalDate.now()
        return when (period) {
            PeriodFilter.TODAY -> today.toEpochDay() to today.toEpochDay()
            PeriodFilter.THIS_WEEK -> {
                val start = today.minusDays((today.dayOfWeek.value % 7).toLong())
                start.toEpochDay() to today.toEpochDay()
            }
            PeriodFilter.THIS_MONTH -> {
                val start = today.withDayOfMonth(1)
                start.toEpochDay() to today.toEpochDay()
            }
            PeriodFilter.LAST_30_DAYS -> today.minusDays(29).toEpochDay() to today.toEpochDay()
        }
    }
}

enum class PeriodFilter(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_30_DAYS("Last 30 days")
}