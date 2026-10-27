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
import androidx.compose.runtime.LaunchedEffect
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
    // Keep one MutableState for the whole screen so SeekBar listeners stay valid.
    // Do NOT use remember(goal) — that recreates state while AndroidView keeps the old listener.
    var minRands by remember { mutableIntStateOf(500) }
    var maxRands by remember { mutableIntStateOf(1_200) }
    var seededFromDb by remember { mutableStateOf(false) }
    var draggingMin by remember { mutableStateOf(false) }
    var draggingMax by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("en", "ZA")) }

    // Load saved goals once (or when DB values change after Save), never while dragging
    LaunchedEffect(goal?.minGoalCents, goal?.maxGoalCents) {
        if (draggingMin || draggingMax) return@LaunchedEffect
        val g = goal
        if (g != null) {
            minRands = snapGoalRands((g.minGoalCents / 100).toInt())
            maxRands = snapGoalRands((g.maxGoalCents / 100).toInt())
            seededFromDb = true
        } else if (!seededFromDb) {
            minRands = 500
            maxRands = 1_200
        }
    }

    Column(
        Modifier.fillMaxSize().background(Mint).padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Monthly spending goals", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text("Drag the bars in steps of R50 to set the minimum and maximum.", color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        Text("Minimum: R${numberFormat.format(minRands)}", color = Forest, fontWeight = FontWeight.SemiBold)
        Text("(${MoneyFormat.zarUnsignedFromCents(minRands * 100L)})", color = Color.Gray, fontSize = 12.sp)
        AndroidView(
            factory = { context ->
                SeekBar(context).apply {
                    max = GOAL_STEPS
                    progress = minRands / GOAL_STEP_RANDS
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) minRands = progress * GOAL_STEP_RANDS
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            draggingMin = true
                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            draggingMin = false
                        }
                    })
                }
            },
            update = { seekBar ->
                val step = minRands / GOAL_STEP_RANDS
                if (!draggingMin && seekBar.progress != step) {
                    seekBar.progress = step
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Maximum: R${numberFormat.format(maxRands)}", color = Forest, fontWeight = FontWeight.SemiBold)
        Text("(${MoneyFormat.zarUnsignedFromCents(maxRands * 100L)})", color = Color.Gray, fontSize = 12.sp)
        AndroidView(
            factory = { context ->
                SeekBar(context).apply {
                    max = GOAL_STEPS
                    progress = maxRands / GOAL_STEP_RANDS
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) maxRands = progress * GOAL_STEP_RANDS
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            draggingMax = true
                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            draggingMax = false
                        }
                    })
                }
            },
            update = { seekBar ->
                val step = maxRands / GOAL_STEP_RANDS
                if (!draggingMax && seekBar.progress != step) {
                    seekBar.progress = step
                }
            },
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

private const val GOAL_STEP_RANDS = 50
private const val GOAL_MAX_RANDS = 50_000
private const val GOAL_STEPS = GOAL_MAX_RANDS / GOAL_STEP_RANDS

private fun snapGoalRands(rands: Int): Int {
    val snapped = ((rands + GOAL_STEP_RANDS / 2) / GOAL_STEP_RANDS) * GOAL_STEP_RANDS
    return snapped.coerceIn(0, GOAL_MAX_RANDS)
}