package com.example.schwiftysavings.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val categories by repository.observeCategories(userId).collectAsState(initial = emptyList())
    var description by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    Column(
        Modifier
            .fillMaxSize()
            .background(Mint)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Create expense", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount in Rands (e.g. 45.00) *") }, modifier = Modifier.fillMaxWidth())
        TextButton(onClick = { isIncome = !isIncome }) {
            Text(if (isIncome) "Type: Income (+)" else "Type: Expense (−)")
        }
        OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Date (YYYY-MM-DD) *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = "%02d".format(startHour), onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let { v -> startHour = v } }, label = { Text("Start hour (0-23) *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = "%02d".format(startMinute), onValueChange = { it.toIntOrNull()?.coerceIn(0, 59)?.let { v -> startMinute = v } }, label = { Text("Start minute *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = "%02d".format(endHour), onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let { v -> endHour = v } }, label = { Text("End hour (0-23) *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = "%02d".format(endMinute), onValueChange = { it.toIntOrNull()?.coerceIn(0, 59)?.let { v -> endMinute = v } }, label = { Text("End minute *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Select category *"
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            androidx.compose.material3.ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { photoPicker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
            Text(if (photoUri == null) "Add photo (optional)" else "Photo selected ✓")
        }
        if (error != null) Text(error!!, color = Color.Red)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    val catId = selectedCategoryId
                    if (catId == null) { error = "Choose a category"; return@launch }
                    val amountRand = amountText.toDoubleOrNull()
                    if (amountRand == null) { error = "Enter a valid amount"; return@launch }
                    val date = runCatching { LocalDate.parse(dateText) }.getOrNull()
                    if (date == null) { error = "Date must be YYYY-MM-DD"; return@launch }
                    var cents = (amountRand * 100).toLong()
                    if (!isIncome) cents = -cents
                    repository.addExpense(
                        userId, catId, description, merchant, date.toEpochDay(),
                        startHour * 60 + startMinute, endHour * 60 + endMinute, cents, photoUri
                    ).onSuccess { onDone() }.onFailure { error = it.message }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Save expense") }
        Text("Required: date, start/end time, description, category. Photo optional.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}