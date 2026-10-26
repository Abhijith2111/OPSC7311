package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.Badge
import com.example.schwiftysavings.util.ShopCatalog
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(userId: Long, repository: SchwiftyRepository) {
    val badges by produceState<List<Badge>>(emptyList(), userId) {
        value = repository.currentBadges(userId)
    }
    val claimed by repository.observeClaimedRewards(userId).collectAsState(initial = emptySet())
    val unlocked = badges.filter { it.unlocked }
    val earned = unlocked.sumOf { it.points }
    val spent = ShopCatalog.rewards.filter { it.id in claimed }.sumOf { it.cost }
    val balance = (earned - spent).coerceAtLeast(0)
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(ForestDark, Forest)))
                .padding(20.dp)
        ) {
            Text("Points shop", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            if (unlocked.isEmpty()) {
                Text(
                    "Unlock a badge to open the shop.",
                    color = Color(0xFFD8F3DC),
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text("YOUR POINTS", color = Color(0xFFB7E4C7), fontSize = 12.sp)
                Text("$balance", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(
                    unlocked.joinToString { "${it.title} ${it.points}" },
                    color = Color(0xFFD8F3DC),
                    fontSize = 12.sp
                )
            }
        }
        if (unlocked.isNotEmpty()) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                message?.let {
                    Text(it, color = Forest, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                }
                ShopCatalog.rewards.forEach { reward ->
                    val owned = reward.id in claimed
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(reward.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Forest)
                            Text("${reward.cost} pts", color = Forest, fontWeight = FontWeight.SemiBold)
                        }
                        Text(reward.detail, color = Color.Gray, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val result = repository.redeemReward(userId, reward.id)
                                    message = result.fold(
                                        onSuccess = { "Redeemed ${reward.title}" },
                                        onFailure = { it.message ?: "Could not redeem" }
                                    )
                                }
                            },
                            enabled = !owned && balance >= reward.cost,
                            colors = ButtonDefaults.buttonColors(containerColor = Forest),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (owned) "Claimed" else "Redeem")
                        }
                    }
                }
            }
        }
    }
}
