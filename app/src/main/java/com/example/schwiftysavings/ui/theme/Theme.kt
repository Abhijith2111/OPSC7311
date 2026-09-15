package com.example.schwiftysavings.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Forest = Color(0xFF1B4332)
val ForestDark = Color(0xFF081C15)
val Mint = Color(0xFFD8F3DC)
val MintSoft = Color(0xFFE9F5EC)
val AccentGreen = Color(0xFF2D6A4F)
val IncomeGreen = Color(0xFF40916C)
val Danger = Color(0xFFB00020)
val CardWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF6B7280)

private val ColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = AccentGreen,
    background = Mint,
    onBackground = ForestDark,
    surface = CardWhite,
    onSurface = ForestDark,
    error = Danger
)

@Composable
fun SchwiftyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}