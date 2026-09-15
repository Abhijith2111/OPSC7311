package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.CategoryTotalRow
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.PeriodFilter
import kotlin.math.abs

@Composable
fun CategoryTotalsScreen(userId: Long, repository: SchwiftyRepository, onBack: () -> Unit) {
    var period by remember { mutableStateOf(PeriodFilter.THIS_MONTH) }
    val totals by produceState<List<CategoryTotalRow>>(emptyList(), userId, period) {
        val (start, end) = DateUtils.periodRange(period)
        value = repository.categoryTotals(userId, start, end)
    }

    Column(Modifier.fillMaxSize().background(Mint).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Category totals", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text("Total money spent per category for a selected period.", color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Row {
            PeriodFilter.entries.forEach { p ->
                FilterChip(
                    selected = period == p,
                    onClick = { period = p },
                    label = { Text(p.label) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(totals, key = { it.categoryId }) { row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Text(row.categoryName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, color = Forest)
                    Text(MoneyFormat.zarUnsignedFromCents(abs(row.totalCents)), fontWeight = FontWeight.Bold, color = Forest)
                }
            }
            if (totals.isEmpty()) {
                item { Text("No expenses in this period.", color = Color.Gray) }
            }
        }
    }
}