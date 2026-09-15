package com.example.schwiftysavings.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

object MoneyFormat {
    private val zar: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        currency = Currency.getInstance("ZAR")
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    /** Signed display for list rows, e.g. -R45.00 */
    fun zarFromCents(cents: Long): String {
        val sign = if (cents < 0) "-" else if (cents > 0) "+" else ""
        val formatted = zar.format(abs(cents) / 100.0)
            .replace("ZAR", "R")
            .replace('\u00A0', ' ')
            .trim()
        // NumberFormat may already include R; normalize for Figma-like "R1 234.56"
        val core = formatted.replace("R", "").trim()
        val spaced = core.replace(",", " ")
        return "${sign}R$spaced"
    }

    fun zarUnsignedFromCents(cents: Long): String {
        val core = zar.format(abs(cents) / 100.0)
            .replace("ZAR", "R")
            .replace('\u00A0', ' ')
            .replace("R", "")
            .trim()
            .replace(",", " ")
        return "R$core"
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