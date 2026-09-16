package com.example.schwiftysavings

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.schwiftysavings.ui.screens.AuthScreens
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.ui.theme.SchwiftyTheme

/**
 * First Activity. On successful login/register we fire an Intent to MainActivity.
 * Demonstrates Activity + Intent (OPSC/PROG learning outcomes).
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        val app = application as SchwiftyApplication

        setContent {
            SchwiftyTheme {
                val userId by app.repository.sessionUserId.collectAsState(initial = null)

                // If already logged in (session restored), jump to main
                LaunchedEffect(userId) {
                    if (userId != null) {
                        goToMain()
                    }
                }

                Box(Modifier.fillMaxSize().background(Mint)) {
                    // Simple toggle between login and register (no NavHost needed here)
                    var showRegister by remember { mutableStateOf(false) }
                    if (showRegister) {
                        AuthScreens.Register(
                            repository = app.repository,
                            onRegistered = { goToMain() },
                            onBack = { showRegister = false }
                        )
                    } else {
                        AuthScreens.Login(
                            repository = app.repository,
                            onLoggedIn = { goToMain() },
                            onGoRegister = { showRegister = true }
                        )
                    }
                }
            }
        }
    }

    private fun goToMain() {
        Log.i("LoginActivity", "Starting MainActivity via Intent")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}