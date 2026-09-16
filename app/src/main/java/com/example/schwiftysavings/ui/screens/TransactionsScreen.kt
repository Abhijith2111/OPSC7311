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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.ForestDark
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.PeriodFilter

@Composable
fun TransactionsScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onOpenExpense: (Long) -> Unit,
    onOpenPhoto: (Long) -> Unit,
    onOpenTotals: () -> Unit,
    onAddExpense: () -> Unit
) {
    var period by remember { mutableStateOf(PeriodFilter.ALL) }
    var typeFilter by remember { mutableStateOf("All") }
    val (start, end) = DateUtils.periodRange(period)
    val expenses by repository.observeExpenses(userId, start, end).collectAsState(initial = emptyList())
    val filtered = expenses.filter {
        when (typeFilter) {
            "Expenses" -> it.amountCents < 0
            "Income" -> it.amountCents > 0
            "Pending" -> it.status == "PENDING"
            else -> true
        }
    }

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(ForestDark, Forest)))
                .padding(16.dp)
        ) {
            Text("Transactions", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Choose a period", color = Color(0xFFB7E4C7), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PeriodFilter.entries.forEach { p ->
                    FilterChip(selected = period == p, onClick = { period = p }, label = { Text(p.label) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("All", "Expenses", "Income", "Pending").forEach { label ->
                    FilterChip(
                        selected = typeFilter == label,
                        onClick = { typeFilter = label },
                        label = { Text(label) }
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onOpenTotals) { Text("Category totals") }
            Button(
                onClick = onAddExpense,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Add expense") }
        }
        LazyColumn {
            items(filtered, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    onClick = { onOpenExpense(expense.id) },
                    trailing = {
                        if (expense.photoPath != null) {
                            TextButton(onClick = { onOpenPhoto(expense.id) }) { Text("Photo") }
                        }
                    }
                )
            }
        }
    }
}