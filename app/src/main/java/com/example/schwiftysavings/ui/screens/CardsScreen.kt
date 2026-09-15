package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.UserEntity
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.ForestDark
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.PeriodFilter
import kotlin.math.abs

@Composable
fun CardsScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onOpenGoals: () -> Unit,
    onOpenPayments: () -> Unit,
    onAddExpense: () -> Unit
) {
    val user by produceState<UserEntity?>(null, userId) { value = repository.currentUser(userId) }
    val goal by repository.observeGoal(userId).collectAsState(initial = null)
    val spent by produceState(0L, userId, goal) {
        val (start, end) = DateUtils.periodRange(PeriodFilter.THIS_MONTH)
        value = abs(repository.categoryTotals(userId, start, end).sumOf { it.totalCents })
    }
    val max = goal?.maxGoalCents ?: 1L
    val progress = (spent.toFloat() / max.toFloat()).coerceIn(0f, 1f)

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(ForestDark, Forest))).padding(16.dp)
        ) {
            Text("My Cards", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.padding(20.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF52B788), Color(0xFFD8F3DC))), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text("Schwifty Supreme", fontWeight = FontWeight.Bold, color = Forest)
                Text("SS", color = Forest)
                Spacer(Modifier.height(24.dp))
                Text("•••• •••• •••• 0137", color = Forest, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(user?.displayName ?: "Cardholder", color = Forest)
                    Text("12 / 29", color = Forest)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("This month spending vs max goal", fontWeight = FontWeight.SemiBold, color = Forest)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = Forest,
                trackColor = Color.White
            )
            Text(
                "${MoneyFormat.zarUnsignedFromCents(spent)} spent of ${MoneyFormat.zarUnsignedFromCents(max)} max",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOpenPayments,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Payments") }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onOpenGoals,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF95D5B2)),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Limits (min / max goals)") }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Add expense") }
        }
    }
}