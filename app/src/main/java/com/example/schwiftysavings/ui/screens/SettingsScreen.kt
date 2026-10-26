package com.example.schwiftysavings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.MonthReward
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.UserEntity
import com.example.schwiftysavings.ui.theme.Danger
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.ForestDark
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.MoneyFormat
import com.example.schwiftysavings.util.ShopCatalog
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onCategories: () -> Unit,
    onGoals: () -> Unit,
    onChart: () -> Unit,
    onBadges: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val user by produceState<UserEntity?>(null, userId) { value = repository.currentUser(userId) }
    val claimed by repository.observeClaimedRewards(userId).collectAsState(initial = emptySet())
    var refresh by remember { mutableIntStateOf(0) }
    val monthReward by produceState<MonthReward?>(null, userId, refresh) {
        value = repository.monthReward(userId)
    }
    var rewardMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val waitingToClaim = ShopCatalog.rewards.filter { it.id !in claimed }
    val readyToUse = ShopCatalog.rewards.filter { it.id in claimed }
    val firstPlaceWaiting = monthReward?.firstPlace == true && monthReward?.voucherUsed == false
    val nothingWaiting = waitingToClaim.isEmpty() && !firstPlaceWaiting

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(ForestDark, Forest))).padding(20.dp)
        ) {
            Text("Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color(0xFFB7E4C7), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                user?.displayName?.take(2)?.uppercase() ?: "SS",
                modifier = Modifier.size(48.dp).background(Forest, CircleShape).padding(12.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(user?.displayName ?: "", fontWeight = FontWeight.Bold, color = Forest)
                Text("Primary Member • @${user?.username}", color = Color.Gray, fontSize = 12.sp)
            }
        }
        Text("ACCOUNT MANAGEMENT", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray, fontSize = 12.sp)
        SettingsRow("Categories", onCategories)
        SettingsRow("Monthly goals (min / max)", onGoals)
        SettingsRow("Spending graph", onChart)
        SettingsRow("Badges", onBadges)
        Text(
            "REWARDS",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp),
            color = Color.Gray,
            fontSize = 12.sp
        )
        Text(
            "Savings balance: ${MoneyFormat.zarUnsignedFromCents(monthReward?.savingsBalanceCents ?: 0L)}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Forest,
            fontWeight = FontWeight.SemiBold
        )
        monthReward?.let { reward ->
            if (reward.firstPlace && reward.voucherUsed) {
                RewardCard(
                    title = "1st place • ${reward.monthLabel}",
                    detail = "R1000 voucher redeemed. It is already in your savings balance."
                )
            } else if (reward.firstPlace) {
                RewardCard(
                    title = "1st place • ${reward.monthLabel}",
                    detail = "R1000 voucher. Redeem adds it to your savings balance.",
                    action = "Redeem",
                    onAction = {
                        scope.launch {
                            val result = repository.useSavingsVoucher(userId)
                            rewardMessage = result.fold(
                                onSuccess = { "R1000 added to your savings balance." },
                                onFailure = { it.message ?: "Could not redeem voucher" }
                            )
                            refresh++
                        }
                    }
                )
            }
        }
        waitingToClaim.forEach { reward ->
            RewardCard(
                title = reward.title,
                detail = "${reward.detail} Costs ${reward.cost} points.",
                action = "Claim",
                onAction = {
                    scope.launch {
                        val result = repository.redeemReward(userId, reward.id)
                        rewardMessage = result.fold(
                            onSuccess = { "${reward.title} is ready to use." },
                            onFailure = { it.message ?: "Could not claim reward" }
                        )
                    }
                }
            )
        }
        readyToUse.forEach { reward ->
            RewardCard(title = reward.title, detail = "Ready to use. ${reward.detail}")
        }
        if (nothingWaiting && readyToUse.isEmpty()) {
            Text(
                "Nothing is waiting to claim or use.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray
            )
        } else if (nothingWaiting) {
            Text(
                "Nothing new is waiting to claim.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray
            )
        }
        rewardMessage?.let {
            Text(it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = Forest)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                scope.launch {
                    repository.logout()
                    onLoggedOut()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0E0), contentColor = Danger),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Log Out Securely") }
        Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RewardCard(
    title: String,
    detail: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(title, color = Forest, fontWeight = FontWeight.Bold)
        Text(detail, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        if (action != null) {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                shape = RoundedCornerShape(12.dp)
            ) { Text(action) }
        }
    }
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color(0xFFB7E4C7), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(title, modifier = Modifier.weight(1f), color = Forest, fontWeight = FontWeight.Medium)
        Text("›", color = Forest)
    }
}