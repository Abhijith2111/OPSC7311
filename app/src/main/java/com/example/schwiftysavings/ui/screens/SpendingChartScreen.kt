package com.example.schwiftysavings.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.MonthSpend
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Danger
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.MoneyFormat

@Composable
fun SpendingChartScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onBack: () -> Unit
) {
    val goal by repository.observeGoal(userId).collectAsState(initial = null)
    val categories by repository.observeCategories(userId).collectAsState(initial = emptyList())
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    LaunchedEffect(categories) {
        if (selectedCategoryId != null && categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = null
        }
    }
    val months by produceState<List<MonthSpend>>(emptyList(), userId, selectedCategoryId) {
        value = repository.monthlySpending(userId, selectedCategoryId)
    }
    val minGoal = goal?.minGoalCents ?: 0L
    val maxGoal = goal?.maxGoalCents ?: 0L
    val showingAll = selectedCategory == null

    Column(
        Modifier
            .fillMaxSize()
            .background(Mint)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Spending graph", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text("Amount spent", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Forest)
        Text(
            if (showingAll) {
                "Each bar is one month. The lines are your minimum and maximum spending goals."
            } else {
                "Each bar is what you spent on ${selectedCategory?.name} that month."
            },
            color = Color.Gray
        )
        Spacer(Modifier.height(12.dp))
        Text("Category", color = Forest, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "All categories",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Tap to choose") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Forest,
                    disabledBorderColor = Forest,
                    disabledLabelColor = Forest,
                    disabledContainerColor = Color.White
                )
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { menuOpen = true }
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownMenuItem(
                    text = { Text("All categories") },
                    onClick = {
                        selectedCategoryId = null
                        menuOpen = false
                    }
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategoryId = category.id
                            menuOpen = false
                        }
                    )
                }
            }
        }
        if (categories.isEmpty()) {
            Text(
                "Add a category in Settings to filter this graph.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.White)
        ) {
            val left = 72.dp.toPx()
            val bottom = 48.dp.toPx()
            val top = 16.dp.toPx()
            val plotLeft = left
            val plotRight = size.width - 12.dp.toPx()
            val plotTop = top
            val plotBottom = size.height - bottom
            val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)
            val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)

            val highest = months.maxOfOrNull { it.spentCents } ?: 0L
            val topCents = chartTopCents(
                if (showingAll) maxOf(highest, minGoal, maxGoal, 100L) else maxOf(highest, 100L)
            )
            val steps = 4

            val axisPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1B4332")
                textSize = 11.sp.toPx()
                isAntiAlias = true
            }
            val gridPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#D0D5DD")
                strokeWidth = 2f
                isAntiAlias = true
            }

            repeat(steps + 1) { step ->
                val fraction = step / steps.toFloat()
                val y = plotBottom - fraction * plotHeight
                drawContext.canvas.nativeCanvas.drawLine(plotLeft, y, plotRight, y, gridPaint)
                val cents = (topCents * fraction).toLong()
                val label = (cents / 100L).toString()
                axisPaint.textAlign = Paint.Align.RIGHT
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    plotLeft - 8.dp.toPx(),
                    y + 4.dp.toPx(),
                    axisPaint
                )
            }

            val count = months.size.coerceAtLeast(1)
            val slot = plotWidth / count
            val barWidth = slot * 0.45f
            months.forEachIndexed { index, month ->
                val barHeight = (month.spentCents / topCents.toFloat()) * plotHeight
                val leftEdge = plotLeft + slot * index + (slot - barWidth) / 2f
                drawRect(
                    color = Forest,
                    topLeft = Offset(leftEdge, plotBottom - barHeight),
                    size = Size(barWidth, barHeight)
                )
                axisPaint.textAlign = Paint.Align.CENTER
                drawContext.canvas.nativeCanvas.drawText(
                    month.label.take(3),
                    leftEdge + barWidth / 2f,
                    plotBottom + 18.dp.toPx(),
                    axisPaint
                )
            }

            axisPaint.textAlign = Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.drawText(
                "Month",
                plotLeft + plotWidth / 2f,
                size.height - 6.dp.toPx(),
                axisPaint
            )

            if (showingAll && goal != null) {
                val minY = plotBottom - (minGoal / topCents.toFloat()) * plotHeight
                val maxY = plotBottom - (maxGoal / topCents.toFloat()) * plotHeight
                drawLine(IncomeGreen, Offset(plotLeft, minY), Offset(plotRight, minY), strokeWidth = 4f)
                drawLine(Danger, Offset(plotLeft, maxY), Offset(plotRight, maxY), strokeWidth = 4f)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (showingAll) {
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
        }
        Spacer(Modifier.height(12.dp))
        months.forEach { month ->
            Text(
                "${month.label}: ${MoneyFormat.zarUnsignedFromCents(month.spentCents)}",
                color = Forest,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

private fun chartTopCents(highestCents: Long): Long {
    val rands = (highestCents / 100L).coerceAtLeast(4L)
    val step = when {
        rands <= 20L -> 5L
        rands <= 100L -> 25L
        rands <= 500L -> 100L
        rands <= 2_000L -> 500L
        else -> 1_000L
    }
    val topRands = ((rands + step - 1) / step) * step
    return topRands * 100L
}
