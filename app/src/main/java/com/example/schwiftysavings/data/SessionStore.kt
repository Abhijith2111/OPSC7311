package com.example.schwiftysavings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private val userIdKey = longPreferencesKey("logged_in_user_id")

    val userIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[userIdKey]
    }

    suspend fun setUserId(userId: Long?) {
        context.dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(userIdKey)
            } else {
                prefs[userIdKey] = userId
            }
        }
    }
}