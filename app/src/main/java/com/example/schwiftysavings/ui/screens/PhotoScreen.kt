package com.example.schwiftysavings.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.ExpenseEntity
import com.example.schwiftysavings.ui.theme.Mint
import java.io.File

@Composable
fun PhotoScreen(expenseId: Long, repository: SchwiftyRepository, onBack: () -> Unit) {
    val expense by produceState<ExpenseEntity?>(null, expenseId) { value = repository.getExpense(expenseId) }
    Column(Modifier.fillMaxSize().background(Mint).padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        val path = expense?.photoPath
        if (path == null) {
            Text("No photo for this expense", color = Color.Gray)
        } else {
            val imageBitmap = remember(path) {
                BitmapFactory.decodeFile(File(path).absolutePath)?.asImageBitmap()
            }
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Expense photo",
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("Could not load photo", color = Color.Red)
            }
        }
    }
}