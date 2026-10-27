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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.MonthReward
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.ForestDark
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.MoneyFormat
import kotlinx.coroutines.launch

private data class RankRow(
    val name: String,
    val percent: Int,
    val isYou: Boolean
)

@Composable
fun LeaderboardScreen(userId: Long, repository: SchwiftyRepository) {
    val entries by repository.observeLeaderboard().collectAsState(initial = emptyList())
    var userName by remember { mutableStateOf("") }
    var myPercent by remember { mutableIntStateOf(0) }
    var mySaved by remember { mutableLongStateOf(0L) }
    var cloudNote by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf<MonthReward?>(null) }
    var amountText by remember { mutableStateOf("") }
    var rewardMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        val user = repository.currentUser(userId)
        userName = user?.username?.ifBlank { "You" } ?: "You"
        myPercent = repository.savingsPercent(userId)
        mySaved = repository.savedAmountCents(userId)
        cloudNote = repository.publishScore(userId)
        repository.useSavingsVoucher(userId)
        reward = repository.monthReward(userId)
    }

    // Merge current user into the list and rank by percent (highest first)
    val ranked = remember(entries, userName, myPercent) {
        val others = entries.map { RankRow(name = it.username, percent = it.savingsPercent, isYou = false) }
        val me = RankRow(name = userName.ifBlank { "You" }, percent = myPercent, isYou = true)
        (others + me).sortedWith(compareByDescending<RankRow> { it.percent }.thenBy { it.name })
    }

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(ForestDark, Forest))).padding(20.dp)
        ) {
            Text("Leaderboard", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("WHAT YOU HAVE SAVED", color = Color(0xFFB7E4C7), fontSize = 12.sp)
            Text(MoneyFormat.zarFromCents(mySaved), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Income this month minus what you spent", color = Color(0xFFD8F3DC), fontSize = 12.sp)
            Text("%$myPercent", color = Color.White, fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))
            Text("YOUR USER NAME", color = Color(0xFFB7E4C7), fontSize = 11.sp)
            Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(cloudNote, color = Color(0xFFD8F3DC), fontSize = 12.sp)
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
            item {
                MonthRewardCard(
                    reward = reward,
                    amountText = amountText,
                    message = rewardMessage,
                    onAmountChange = { amountText = it.filter { ch -> ch.isDigit() }.take(6) },
                    onPlace = {
                        val rands = amountText.toLongOrNull() ?: 0L
                        scope.launch {
                            val result = repository.placeInSavings(userId, rands)
                            rewardMessage = result.fold(
                                onSuccess = { credit ->
                                    "Placed in savings. With 20% interest that is ${MoneyFormat.zarUnsignedFromCents(credit)}."
                                },
                                onFailure = { it.message ?: "Could not place savings" }
                            )
                            reward = repository.monthReward(userId)
                        }
                    },
                    onUseVoucher = {
                        scope.launch {
                            val result = repository.useSavingsVoucher(userId)
                            rewardMessage = result.fold(
                                onSuccess = { "R1000 added to your home balance." },
                                onFailure = { it.message ?: "Could not use voucher" }
                            )
                            reward = repository.monthReward(userId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthRewardCard(
    reward: MonthReward?,
    amountText: String,
    message: String?,
    onAmountChange: (String) -> Unit,
    onPlace: () -> Unit,
    onUseVoucher: () -> Unit
) {
    val details = reward ?: return
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("End of ${details.monthLabel}", fontWeight = FontWeight.Bold, color = Forest, fontSize = 18.sp)
        Text("You finished in place ${details.place}.", color = Color.Gray)
        Text(
            "Savings balance: ${MoneyFormat.zarUnsignedFromCents(details.savingsBalanceCents)}",
            color = Forest,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (!details.eligible) {
            Text(
                "You are not eligible this month. Top ten who saved money can earn 20% interest on up to R2000.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (details.alreadyDeposited) {
            Text("You already placed last month's savings.", color = Forest, modifier = Modifier.padding(top = 8.dp))
        } else {
            Text(
                "Place up to ${MoneyFormat.zarUnsignedFromCents(details.maxDepositCents)} into savings. You earn 20% interest.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
                label = { Text("Amount in rands") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = onPlace,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Place in savings") }
        }
        if (details.firstPlace) {
            Spacer(Modifier.height(12.dp))
            Text("First place prize: R1000 on your home balance", fontWeight = FontWeight.Bold, color = Forest)
            if (details.voucherUsed) {
                Text("It shows on the home page as a Request of R1000, won from the voucher.", color = Color.Gray)
            } else {
                Button(
                    onClick = onUseVoucher,
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add to home balance") }
            }
        }
        message?.let {
            Text(it, color = Forest, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
