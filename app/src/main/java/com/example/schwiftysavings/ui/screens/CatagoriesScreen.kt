package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(userId: Long, repository: SchwiftyRepository, onBack: () -> Unit) {
    val categories by repository.observeCategories(userId).collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(Mint).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Categories", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("New category name") }, modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error!!, color = Color.Red)
        Button(
            onClick = {
                scope.launch {
                    repository.addCategory(userId, name)
                        .onSuccess { name = ""; error = null }
                        .onFailure { error = it.message }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create category") }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(categories, key = { it.id }) { cat ->
                Text(cat.name, modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Medium)
            }
        }
    }
}