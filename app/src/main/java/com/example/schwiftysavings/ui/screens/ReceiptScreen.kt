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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.ExpenseEntity
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.MoneyFormat

@Composable
fun ReceiptScreen(
    expenseId: Long,
    repository: SchwiftyRepository,
    onBack: () -> Unit,
    onOpenPhoto: () -> Unit
) {
    val expense by produceState<ExpenseEntity?>(null, expenseId) { value = repository.getExpense(expenseId) }
    val currentExpense = expense
    val categoryName by produceState("", currentExpense) {
        value = currentExpense?.categoryId?.let { repository.getCategory(it)?.name } ?: ""
    }

    Column(Modifier.fillMaxSize().background(Mint).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Receipt", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Forest)
        Spacer(Modifier.height(16.dp))
        val e = currentExpense
        if (e == null) {
            Text("Loading…")
        } else {
            Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(20.dp)) {
                Text(e.merchantName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Forest)
                Text(categoryName.uppercase(), color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Text(MoneyFormat.zarFromCents(e.amountCents), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(e.description, color = Color.Gray)
                Text(
                    "${DateUtils.formatEpochDay(e.dateEpochDay)} • ${DateUtils.formatMinute(e.startMinute)}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(20.dp)) {
                Meta("Status", e.status)
                Meta("Reference No.", e.referenceNo.ifBlank { "—" })
                Meta("Photo", if (e.photoPath != null) "Attached" else "None")
            }
            if (e.photoPath != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onOpenPhoto,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("View photo") }
            }
        }
    }
}

@Composable
private fun Meta(label: String, value: String) {
    Row(Modifier.padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold, color = Forest)
    }
}