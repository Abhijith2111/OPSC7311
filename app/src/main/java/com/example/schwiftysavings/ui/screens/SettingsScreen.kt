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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.local.UserEntity
import com.example.schwiftysavings.ui.theme.Danger
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.ForestDark
import com.example.schwiftysavings.ui.theme.Mint
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

@Composable
fun SettingsScreen(
    userId: Long,
    repository: SchwiftyRepository,
    onCategories: () -> Unit,
    onGoals: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val user by produceState<UserEntity?>(null, userId) { value = repository.currentUser(userId) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(Mint)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(ForestDark, Forest))).padding(20.dp)
        ) {
            Text("Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
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