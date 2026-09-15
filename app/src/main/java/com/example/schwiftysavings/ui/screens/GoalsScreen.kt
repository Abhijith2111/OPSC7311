package com.example.schwiftysavings.ui.screens

import android.widget.SeekBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.MoneyFormat
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Min/max monthly goals using classic SeekBar (learning unit) via AndroidView.
 * Money labels use NumberFormat.
 */
@Composable
fun GoalsScreen(userId: Long, repository: SchwiftyRepository, onBack: () -> Unit) {
    val goal by repository.observeGoal(userId).collectAsState(initial = null)
    // SeekBar range in whole Rands (0..50000)
    var minRands by remember(goal) { mutableIntStateOf(((goal?.minGoalCents ?: 50_000) / 100).toInt().coerceIn(0, 50_000)) }
    var maxRands by remember(goal) { mutableIntStateOf(((goal?.maxGoalCents ?: 120_000) / 100).toInt().coerceIn(0, 50_000)) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("en", "ZA")) }

    Column(
        Modifier.fillMaxSize().background(Mint).padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Monthly spending goals", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text("Drag SeekBars to set minimum and maximum goals.", color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        Text("Minimum: R${numberFormat.format(minRands)}", color = Forest, fontWeight = FontWeight.SemiBold)
        Text("(${MoneyFormat.zarUnsignedFromCents(minRands * 100L)})", color = Color.Gray, fontSize = 12.sp)
        AndroidView(
            factory = { context ->
                SeekBar(context).apply {
                    max = 50_000
                    progress = minRands
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) minRands = progress
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                }
            },
            update = { it.progress = minRands },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Maximum: R${numberFormat.format(maxRands)}", color = Forest, fontWeight = FontWeight.SemiBold)
        Text("(${MoneyFormat.zarUnsignedFromCents(maxRands * 100L)})", color = Color.Gray, fontSize = 12.sp)
        AndroidView(
            factory = { context ->
                SeekBar(context).apply {
                    max = 50_000
                    progress = maxRands
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) maxRands = progress
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                }
            },
            update = { it.progress = maxRands },
            modifier = Modifier.fillMaxWidth()
        )

        if (message != null) {
            Text(message!!, color = if (message!!.startsWith("Saved")) Forest else Color.Red)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    repository.setGoals(userId, minRands * 100L, maxRands * 100L)
                        .onSuccess { message = "Saved" }
                        .onFailure { message = it.message }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Save goals") }
    }
}