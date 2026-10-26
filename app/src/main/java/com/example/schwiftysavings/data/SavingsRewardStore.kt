package com.example.schwiftysavings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.savingsRewards by preferencesDataStore(name = "schwifty_savings_rewards")

data class MonthReward(
    val monthLabel: String,
    val yearMonth: String,
    val place: Int,
    val savedCents: Long,
    val maxDepositCents: Long,
    val eligible: Boolean,
    val firstPlace: Boolean,
    val alreadyDeposited: Boolean,
    val savingsBalanceCents: Long,
    val voucherUsed: Boolean
)

/** One deposit and one voucher per user per finished month. */
class SavingsRewardStore(private val context: Context) {
    private fun balanceKey(userId: Long) = longPreferencesKey("balance_$userId")
    private fun depositedKey(userId: Long) = stringSetPreferencesKey("deposited_$userId")
    private fun voucherUsedKey(userId: Long) = stringSetPreferencesKey("voucher_used_$userId")

    suspend fun balance(userId: Long): Long {
        val prefs = context.savingsRewards.data.first()
        return prefs[balanceKey(userId)] ?: 0L
    }

    suspend fun depositedMonths(userId: Long): Set<String> {
        val prefs = context.savingsRewards.data.first()
        return prefs[depositedKey(userId)] ?: emptySet()
    }

    suspend fun usedVoucherMonths(userId: Long): Set<String> {
        val prefs = context.savingsRewards.data.first()
        return prefs[voucherUsedKey(userId)] ?: emptySet()
    }

    suspend fun deposit(userId: Long, yearMonth: String, creditCents: Long) {
        context.savingsRewards.edit { prefs ->
            val months = prefs[depositedKey(userId)] ?: emptySet()
            prefs[depositedKey(userId)] = months + yearMonth
            val balance = prefs[balanceKey(userId)] ?: 0L
            prefs[balanceKey(userId)] = balance + creditCents
        }
    }

    suspend fun useVoucher(userId: Long, yearMonth: String) {
        context.savingsRewards.edit { prefs ->
            val months = prefs[voucherUsedKey(userId)] ?: emptySet()
            prefs[voucherUsedKey(userId)] = months + yearMonth
        }
    }
}
