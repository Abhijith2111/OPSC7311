package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Danger
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.GoalZone
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.MonthProgress

@Composable
fun GoalProgressScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onBack: () -> Unit
) {
    val progress by produceState(MonthProgress(0L, null, null, GoalZone.NO_GOAL), userId) {
        value = repository.pastMonthProgress(userId)
    }
    val monthName = progress.monthLabel.ifBlank { "Past month" }
    val heading = when (progress.zone) {
        GoalZone.NO_GOAL -> "Set a minimum and a maximum goal"
        GoalZone.UNDER_MIN -> "Below your minimum"
        GoalZone.IN_RANGE -> "Inside your goals"
        GoalZone.OVER_MAX -> "Over your maximum"
    }
    val cardColor = when (progress.zone) {
        GoalZone.IN_RANGE -> IncomeGreen
        GoalZone.OVER_MAX -> Danger
        GoalZone.UNDER_MIN -> Color(0xFF8A6A00)
        GoalZone.NO_GOAL -> Color.Gray
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Mint)
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Goal progress", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text("$monthName spending", color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(cardColor, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(heading, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "How $monthName spending sits between your minimum and maximum.",
                color = Color(0xFFD8F3DC),
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, RoundedCornerShape(28.dp))
        ) {
            val minCents = progress.minCents ?: 0L
            val maxCents = progress.maxCents ?: 0L
            val scale = maxOf(maxCents, progress.spentCents, 1L).toFloat()
            val minX = (minCents / scale) * size.width
            val maxX = (maxCents / scale) * size.width
            val spentX = ((progress.spentCents / scale) * size.width).coerceIn(18f, size.width - 18f)
            if (progress.zone != GoalZone.NO_GOAL) {
                drawRoundRect(
                    color = IncomeGreen.copy(alpha = 0.45f),
                    topLeft = Offset(minX, 8f),
                    size = Size((maxX - minX).coerceAtLeast(0f), size.height - 16f),
                    cornerRadius = CornerRadius(20f, 20f)
                )
            }
            drawCircle(color = Color.White, radius = 16f, center = Offset(spentX, size.height / 2f))
            drawCircle(color = Forest, radius = 11f, center = Offset(spentX, size.height / 2f))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Green band = between minimum and maximum. Dark dot = what you spent.",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))
        Text("Spent: ${MoneyFormat.zarUnsignedFromCents(progress.spentCents)}", color = Forest, fontWeight = FontWeight.SemiBold)
        Text("Minimum: ${MoneyFormat.zarUnsignedFromCents(progress.minCents ?: 0L)}", color = Forest)
        Text("Maximum: ${MoneyFormat.zarUnsignedFromCents(progress.maxCents ?: 0L)}", color = Forest)
    }
}
