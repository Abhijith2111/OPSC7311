package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.PeriodFilter
import kotlin.math.abs

@Composable
fun PaymentsScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onBack: () -> Unit,
    onAddExpense: () -> Unit
) {
    val goal by repository.observeGoal(userId).collectAsState(initial = null)
    val spent by produceState(0L, userId, goal) {
        val (start, end) = DateUtils.periodRange(PeriodFilter.THIS_MONTH)
        value = abs(repository.categoryTotals(userId, start, end).sumOf { it.totalCents })
    }
    val max = goal?.maxGoalCents ?: 120_000L
    val progress = (spent.toFloat() / max.toFloat()).coerceIn(0f, 1f)

    Column(Modifier.fillMaxSize().background(Mint).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Payments", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Forest)
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text("Upcoming Bills", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("Linked to goals", color = Color.Gray)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Forest)
            Text(
                "${MoneyFormat.zarUnsignedFromCents(spent)} spent of ${MoneyFormat.zarUnsignedFromCents(max)} max this month",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("UPCOMING BILLS", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        listOf("Figma Professional" to 33000L, "Spotify Premium" to 45000L).forEach { (title, cents) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, color = Forest)
                    Text("Demo bill row", color = Color.Gray, fontSize = 12.sp)
                }
                Text(MoneyFormat.zarUnsignedFromCents(cents), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF95D5B2), contentColor = Forest),
            shape = RoundedCornerShape(24.dp)
        ) { Text("Schedule New Payment +") }
    }
}