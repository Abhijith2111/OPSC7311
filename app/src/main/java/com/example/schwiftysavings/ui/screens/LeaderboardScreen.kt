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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.MoneyFormat

private data class RankRow(
    val name: String,
    val percent: Int,
    val isYou: Boolean
)

@Composable
fun LeaderboardScreen(userId: Long, repository: SchwiftyRepository) {
    val entries by repository.observeLeaderboard().collectAsState(initial = emptyList())
    var displayName by remember { mutableStateOf("") }
    var myPercent by remember { mutableIntStateOf(0) }
    var mySaved by remember { mutableLongStateOf(0L) }

    LaunchedEffect(userId) {
        val user = repository.currentUser(userId)
        displayName = user?.displayName?.ifBlank { user.username } ?: "You"
        myPercent = repository.savingsPercent(userId)
        mySaved = repository.savedAmountCents(userId)
    }

    // Merge current user into the list and rank by percent (highest first)
    val ranked = remember(entries, displayName, myPercent) {
        val others = entries.map { RankRow(name = it.username, percent = it.savingsPercent, isYou = false) }
        val me = RankRow(name = displayName.ifBlank { "You" }, percent = myPercent, isYou = true)
        (others + me).sortedWith(compareByDescending<RankRow> { it.percent }.thenBy { it.name })
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
            Text("YOUR DISPLAY NAME", color = Color(0xFFB7E4C7), fontSize = 11.sp)
            Text(displayName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        LazyColumn {
            itemsIndexed(ranked, key = { index, row -> "${row.isYou}-${row.name}-$index" }) { index, row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .then(
                            if (row.isYou) Modifier.background(Color(0xFFB7E4C7)) else Modifier
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}. ${if (row.isYou) "YOU (${row.name})" else row.name}",
                        fontWeight = if (row.isYou) FontWeight.Bold else FontWeight.Medium,
                        color = Forest
                    )
                    Text(
                        "%${row.percent}",
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
