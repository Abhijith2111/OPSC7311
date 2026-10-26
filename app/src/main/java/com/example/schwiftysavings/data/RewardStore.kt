package com.example.schwiftysavings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.rewardClaims by preferencesDataStore(name = "schwifty_reward_claims")

/** Saved reward ids the user has already redeemed, kept per user. */
class RewardStore(private val context: Context) {
    private fun key(userId: Long) = stringSetPreferencesKey("claimed_$userId")

    fun claimed(userId: Long): Flow<Set<String>> {
        return context.rewardClaims.data.map { prefs -> prefs[key(userId)] ?: emptySet() }
    }

    suspend fun claimedNow(userId: Long): Set<String> = claimed(userId).first()

    suspend fun claim(userId: Long, rewardId: String) {
        context.rewardClaims.edit { prefs ->
            val current = prefs[key(userId)] ?: emptySet()
            prefs[key(userId)] = current + rewardId
        }
    }
}
