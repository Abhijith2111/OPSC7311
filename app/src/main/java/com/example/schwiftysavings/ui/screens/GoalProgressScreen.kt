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
    val heading = when (progress.zone) {
        GoalZone.NO_GOAL -> "Set a minimum and a maximum goal"
        GoalZone.UNDER_MIN -> "Below your minimum"
        GoalZone.IN_RANGE -> "Inside your goals"
        GoalZone.OVER_MAX -> "Over your maximum"
    }
    val headingColor = when (progress.zone) {
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
        Text("Last 30 days of spending, compared with your minimum and maximum goals.", color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        Text(heading, color = headingColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color.White, RoundedCornerShape(18.dp))
        ) {
            val minCents = progress.minCents ?: 0L
            val maxCents = progress.maxCents ?: 0L
            val scale = maxOf(maxCents, progress.spentCents, 1L).toFloat()
            val minX = (minCents / scale) * size.width
            val maxX = (maxCents / scale) * size.width
            val spentX = (progress.spentCents / scale) * size.width
            if (progress.zone != GoalZone.NO_GOAL) {
                drawRect(
                    color = IncomeGreen.copy(alpha = 0.45f),
                    topLeft = Offset(minX, 0f),
                    size = Size((maxX - minX).coerceAtLeast(0f), size.height)
                )
            }
            drawLine(
                color = Forest,
                start = Offset(spentX, 0f),
                end = Offset(spentX, size.height),
                strokeWidth = 8f
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("The dark line is what you spent. The green band is the space between min and max.", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Text("Spent: ${MoneyFormat.zarUnsignedFromCents(progress.spentCents)}", color = Forest, fontWeight = FontWeight.SemiBold)
        Text("Minimum: ${MoneyFormat.zarUnsignedFromCents(progress.minCents ?: 0L)}", color = Forest)
        Text("Maximum: ${MoneyFormat.zarUnsignedFromCents(progress.maxCents ?: 0L)}", color = Forest)
    }
}