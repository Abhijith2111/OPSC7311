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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                val userId by app.repository.sessionUserId.collectAsState(initial = null)

                LaunchedEffect(userId) {
                    if (userId == null) {
                        // Logged out → back to LoginActivity via Intent
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Mint) {
                    if (userId != null) {
                        SchwiftyNavHost(
                            repository = app.repository,
                            loggedInUserId = userId
                        )
                    }
                }
            }
        }
    }
}