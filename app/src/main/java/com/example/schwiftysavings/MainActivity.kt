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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.schwiftysavings.ui.SchwiftyNavHost
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.ui.theme.SchwiftyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        val app = application as SchwiftyApplication
        Log.d("MainActivity", "Main shell created")

        setContent {
            SchwiftyTheme {
                // Wait for the first real session value (in-memory session).
                // Do NOT use collectAsState(initial = null) — that falsely looks "logged out"
                // and causes a Login <-> Home loop after register/login.
                var sessionReady by remember { mutableStateOf(false) }
                var userId by remember { mutableStateOf<Long?>(null) }

                LaunchedEffect(Unit) {
                    app.repository.sessionUserId.collect { id ->
                        userId = id
                        sessionReady = true
                        if (id == null) {
                            startActivity(
                                Intent(this@MainActivity, LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                            finish()
                        }
                    }
                }

                Box(Modifier.fillMaxSize().background(Mint)) {
                    if (sessionReady) {
                        val loggedInId = userId
                        if (loggedInId != null) {
                            SchwiftyNavHost(
                                repository = app.repository,
                                loggedInUserId = loggedInId
                            )
                        }
                    }
                }
            }
        }
    }
}