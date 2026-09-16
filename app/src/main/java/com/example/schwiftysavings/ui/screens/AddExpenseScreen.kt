package com.example.schwiftysavings.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AddExpenseScreen(
    userId: Long,
    repository: SchwiftyRepository,
    entry: String = "expense",
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val entryKey = entry.lowercase()
    val (screenTitle, submitLabel) = when (entryKey) {
        "request" -> "Request money" to "Submit request"
        "pay" -> "Pay" to "Pay now"
        "send" -> "Send money" to "Send now"
        else -> "Create expense" to "Save expense"
    }
    val categories by repository.observeCategories(userId).collectAsState(initial = emptyList())
    val now = remember { LocalTime.now() }
    var description by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember(entryKey) { mutableStateOf(entryKey == "request") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var hourText by remember { mutableStateOf(now.hour.toString()) }
    var minuteText by remember { mutableStateOf(now.minute.toString()) }
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
        Text(screenTitle, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
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
        Text("Time the transaction took place", color = Forest, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = hourText,
            onValueChange = { hourText = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Hour (0-23) *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = minuteText,
            onValueChange = { minuteText = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Minute (0-59) *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Select category *"
        Text("Category *", color = Forest, fontWeight = FontWeight.SemiBold)
        if (categories.isEmpty()) {
            Text(
                "No categories yet. Go to Settings → Categories and add at least one, then come back.",
                color = Color.Red,
                fontSize = 13.sp
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedName,
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
                // Full-size overlay — OutlinedTextField alone often blocks clickable{}
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId = cat.id
                                expanded = false
                            }
                        )
                    }
                }
            }
            TextButton(onClick = { expanded = true }) {
                Text("Choose category", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { photoPicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Forest)
        ) {
            Text(
                text = if (photoUri == null) "Add photo" else "Photo added",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    val h = hourText.toIntOrNull()
                    val m = minuteText.toIntOrNull()
                    when {
                        h == null || m == null -> {
                            error = "Enter valid hour and minute"
                            return@launch
                        }
                        h !in 0..23 -> {
                            error = "Hour must be 0–23"
                            return@launch
                        }
                        m !in 0..59 -> {
                            error = "Minute must be 0–59"
                            return@launch
                        }
                    }
                    val timeMinute = h * 60 + m
                    // Store same value in start/end so Room schema stays unchanged
                    repository.addExpense(
                        userId, catId, description, merchant, date.toEpochDay(),
                        timeMinute, timeMinute, cents, photoUri
                    ).onSuccess { onDone() }.onFailure { error = it.message }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = submitLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            "Required: date, time, description, category.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}