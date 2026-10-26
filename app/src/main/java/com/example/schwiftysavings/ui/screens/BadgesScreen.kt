package com.example.schwiftysavings.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.util.Badge

@Composable
fun BadgesScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onBack: () -> Unit
) {
    var badges by remember { mutableStateOf<List<Badge>>(emptyList()) }

    LaunchedEffect(userId) {
        badges = repository.currentBadges(userId)
        repository.publishScore(userId)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Mint)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Badges", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text(
            "Badges unlock when you log expenses, keep a streak, save goals, or stay between your min and max.",
            color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
        badges.forEach { badge ->
            val background = if (badge.unlocked) Forest else Color.White
            val titleColor = if (badge.unlocked) Color.White else Forest
            val detailColor = if (badge.unlocked) Color(0xFFD8F3DC) else Color.Gray
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(background, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(badge.title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(badge.detail, color = detailColor, fontSize = 13.sp)
                Text(
                    if (badge.unlocked) "Unlocked" else "Locked",
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}