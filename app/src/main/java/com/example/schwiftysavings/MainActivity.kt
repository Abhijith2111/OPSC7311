package com.example.schwiftysavings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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
        enableEdgeToEdge()
        val app = application as SchwiftyApplication
        Log.d("MainActivity", "Main shell created")

        setContent {
            SchwiftyTheme {
                // Wait for first real session value (fixes Login <-> Home loop)
                var sessionReady by remember { mutableStateOf(false) }
                var userId by remember { mutableStateOf<Long?>(null) }

                LaunchedEffect(Unit) {
                    app.repository.sessionUserId.collect { id ->
                        userId = id
                        sessionReady = true
                        if (id == null) {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Mint) {
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