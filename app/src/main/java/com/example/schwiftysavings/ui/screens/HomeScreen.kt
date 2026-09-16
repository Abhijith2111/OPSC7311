package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.ExpenseEntity
import com.example.schwiftysavings.data.local.UserEntity
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.ForestDark
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.PeriodFilter

@Composable
fun HomeScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onAddExpense: (String) -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenExpense: (Long) -> Unit
) {
    val balance by repository.observeNetBalance(userId).collectAsState(initial = 0L)
    val user by produceState<UserEntity?>(null, userId) {
        value = repository.currentUser(userId)
    }
    val (start, end) = DateUtils.periodRange(PeriodFilter.LAST_30_DAYS)
    val expenses by repository.observeExpenses(userId, start, end).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Mint)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(ForestDark, Forest)))
                    .padding(20.dp)
            ) {
                Text("SS  Schwifty Savings", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(24.dp))
                Text("NET TREASURY BALANCE", color = Color(0xFFB7E4C7), fontSize = 12.sp)
                Text(
                    "ZAR ${MoneyFormat.zarUnsignedFromCents(balance)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Signed in as ${user?.displayName ?: "..."}", color = Color(0xFFD8F3DC), fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Pay", "Send", "Request").forEach { label ->
                        Button(
                            onClick = { onAddExpense(label.lowercase()) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Forest)
                TextButton(onClick = onOpenTransactions) { Text("History →") }
            }
        }
        items(expenses.take(8), key = { it.id }) { expense ->
            ExpenseRow(expense, onClick = { onOpenExpense(expense.id) })
        }
    }
}

@Composable
fun ExpenseRow(expense: ExpenseEntity, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(expense.merchantName, fontWeight = FontWeight.SemiBold, color = Forest)
            Text(
                "${expense.description} • ${DateUtils.formatEpochDay(expense.dateEpochDay)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            if (expense.photoPath != null) {
                Text("Photo attached", color = IncomeGreen, fontSize = 11.sp)
            }
        }
        Text(
            MoneyFormat.zarFromCents(expense.amountCents),
            fontWeight = FontWeight.Bold,
            color = if (expense.amountCents >= 0) IncomeGreen else Forest
        )
        trailing?.invoke()
    }
}