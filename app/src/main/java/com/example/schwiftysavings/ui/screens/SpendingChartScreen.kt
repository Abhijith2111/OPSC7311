package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.CategoryTotalRow
import com.example.schwiftysavings.ui.theme.Danger
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.PeriodFilter
import kotlin.math.abs

@Composable
fun SpendingChartScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onBack: () -> Unit
) {
    var period by remember { mutableStateOf(PeriodFilter.THIS_MONTH) }
    val goal by repository.observeGoal(userId).collectAsState(initial = null)
    val totals by produceState<List<CategoryTotalRow>>(emptyList(), userId, period) {
        val (start, end) = DateUtils.periodRange(period)
        value = repository.categoryTotals(userId, start, end)
    }
    val minGoal = goal?.minGoalCents ?: 0L
    val maxGoal = goal?.maxGoalCents ?: 0L

    Column(
        Modifier
            .fillMaxSize()
            .background(Mint)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Spending graph", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text(
            "Bars show the amount spent in each category. The lines are this month's minimum and maximum goals.",
            color = Color.Gray
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            PeriodFilter.entries.forEach { item ->
                FilterChip(
                    selected = period == item,
                    onClick = { period = item },
                    label = { Text(item.label) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.White)
        ) {
            val highestBar = totals.maxOfOrNull { abs(it.totalCents) } ?: 0L
            val scale = maxOf(highestBar, minGoal, maxGoal, 1L).toFloat()
            val count = totals.size.coerceAtLeast(1)
            val slot = size.width / count
            val barWidth = slot * 0.55f
            totals.forEachIndexed { index, row ->
                val barHeight = (abs(row.totalCents) / scale) * size.height
                val left = slot * index + (slot - barWidth) / 2f
                drawRect(
                    color = Forest,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
            if (goal != null) {
                val minY = size.height - (minGoal / scale) * size.height
                val maxY = size.height - (maxGoal / scale) * size.height
                drawLine(IncomeGreen, Offset(0f, minY), Offset(size.width, minY), strokeWidth = 4f)
                drawLine(Danger, Offset(0f, maxY), Offset(size.width, maxY), strokeWidth = 4f)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Green line = minimum goal", color = IncomeGreen, fontSize = 13.sp)
        Text("Red line = maximum goal", color = Danger, fontSize = 13.sp)
        if (goal == null) {
            Text("Set a minimum and maximum goal so the lines can be drawn.", color = Color.Gray)
        }
        Text(
            "Min ${MoneyFormat.zarUnsignedFromCents(minGoal)}    Max ${MoneyFormat.zarUnsignedFromCents(maxGoal)}",
            color = Forest,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(12.dp))
        if (totals.isEmpty()) {
            Text("No category spending in this period yet.", color = Color.Gray)
        }
        totals.forEach { row ->
            Text(
                "${row.categoryName}: ${MoneyFormat.zarUnsignedFromCents(abs(row.totalCents))}",
                color = Forest,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}