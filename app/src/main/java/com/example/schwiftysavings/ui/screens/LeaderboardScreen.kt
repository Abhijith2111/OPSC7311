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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.MoneyFormat
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(userId: Long, repository: SchwiftyRepository) {
    val entries by repository.observeLeaderboard().collectAsState(initial = emptyList())
    var username by remember { mutableStateOf("") }
    var myPercent by remember { mutableIntStateOf(0) }
    var mySaved by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        val user = repository.currentUser(userId)
        username = user?.leaderboardUsername ?: ""
        myPercent = repository.savingsPercent(userId)
        mySaved = repository.savedAmountCents(userId)
    }

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(ForestDark, Forest))).padding(20.dp)
        ) {
            Text("Leaderboard", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("WHAT YOU HAVE SAVED", color = Color(0xFFB7E4C7), fontSize = 12.sp)
            Text("ZAR ${MoneyFormat.zarUnsignedFromCents(mySaved)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("%$myPercent", color = Color.White, fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))
            Text("ENTER A USERNAME", color = Color(0xFFB7E4C7), fontSize = 11.sp)
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    scope.launch { repository.updateLeaderboardUsername(userId, it) }
                },
                modifier = Modifier.fillMaxWidth().background(Color(0xFF2D6A4F), RoundedCornerShape(24.dp)),
                singleLine = true
            )
            Text("WARNING : DO NOT ENTER PERSONAL DETAILS", color = Color(0xFFFFCDD2), fontSize = 11.sp)
        }
        LazyColumn {
            item {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("YOU ($username)", fontWeight = FontWeight.Bold, color = Forest)
                    Text("%$myPercent", color = IncomeGreen, fontWeight = FontWeight.Bold)
                }
            }
            items(entries, key = { it.id }) { entry ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.username, fontWeight = FontWeight.Medium, color = Forest)
                    Text("%${entry.savingsPercent}", color = IncomeGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}