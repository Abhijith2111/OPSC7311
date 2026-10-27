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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.theme.Danger
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.IncomeGreen
import com.example.schwiftysavings.ui.theme.Mint
import com.example.schwiftysavings.ui.theme.TextMuted
import com.example.schwiftysavings.util.PasswordRules
import kotlinx.coroutines.launch

object AuthScreens {
    @Composable
    fun Login(
        repository: SchwiftyRepository,
        onLoggedIn: () -> Unit,
        onGoRegister: () -> Unit
    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Mint)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Schwifty Savings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Forest)
            Text("Sign in with your user name and password", color = Forest)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("User name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = Color.Red)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    scope.launch {
                        repository.login(username, password)
                            .onSuccess { onLoggedIn() }
                            .onFailure { error = it.message }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Log in") }
            TextButton(onClick = onGoRegister) { Text("Create an account") }
        }
    }

    @Composable
    fun Register(
        repository: SchwiftyRepository,
        onRegistered: () -> Unit,
        onBack: () -> Unit
    ) {
        var userName by remember { mutableStateOf("") }
        var personalName by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        val strength = PasswordRules.strength(password)
        val strengthColor = when (strength) {
            PasswordRules.Strength.STRONG -> IncomeGreen
            PasswordRules.Strength.MEDIUM -> Color(0xFFC47B00)
            else -> Danger
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Mint)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Forest)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("User name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Do not use your real name as your user name. Only one person can use each user name.",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = personalName,
                onValueChange = { personalName = it },
                label = { Text("Personal name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "At least 8 characters. Use letters and a special character, such as ! @ #.",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { PasswordRules.fraction(password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = strengthColor,
                trackColor = Color.White
            )
            if (password.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        PasswordRules.label(password),
                        color = strengthColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = Color.Red)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    scope.launch {
                        repository.register(userName, password, personalName)
                            .onSuccess { onRegistered() }
                            .onFailure { error = it.message }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Register") }
            TextButton(onClick = onBack) { Text("Back to login") }
        }
    }
}