package com.example.schwiftysavings.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session is kept in memory only (not saved on disk).
 * When the app is closed, the user must log in again.
 */
class SessionStore(@Suppress("UNUSED_PARAMETER") context: Context) {
    private val _userId = MutableStateFlow<Long?>(null)
    val userIdFlow: Flow<Long?> = _userId.asStateFlow()

    suspend fun setUserId(userId: Long?) {
        _userId.value = userId
    }
}