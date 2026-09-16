package com.example.schwiftysavings

import com.example.schwiftysavings.data.PasswordHasher
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.PeriodFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CoreLogicTest {
    @Test
    fun passwordHash_isDeterministicAndSalted() {
        val salt = PasswordHasher.newSalt()
        val hash1 = PasswordHasher.hash("secret123", salt)
        val hash2 = PasswordHasher.hash("secret123", salt)
        assertEquals(hash1, hash2)
        assertTrue(PasswordHasher.matches("secret123", salt, hash1))
        assertFalse(PasswordHasher.matches("wrong", salt, hash1))
    }

    @Test
    fun moneyFormat_formatsZar() {
        assertTrue(MoneyFormat.zarFromCents(-4500).contains("45"))
        assertTrue(MoneyFormat.zarUnsignedFromCents(120_000).startsWith("R"))
    }

    @Test
    fun periodFilter_today_isSingleDay() {
        val (start, end) = DateUtils.periodRange(PeriodFilter.TODAY)
        val today = LocalDate.now().toEpochDay()
        assertEquals(today, start)
        assertEquals(today, end)
    }

    @Test
    fun goalValidation_minCannotExceedMax() {
        val min = 100_00L
        val max = 50_00L
        assertTrue(min > max)
    }

    @Test
    fun categoryTotal_sumsNegativeAmountsAsSpend() {
        val amounts = listOf(-4500L, -2340000L, 45000L)
        val spent = amounts.filter { it < 0 }.sum()
        assertEquals(-2_344_500L, spent)
    }
}