package com.likeki.alquran.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Emerald = Color(0xFF0B3D2E)
val Ivory = Color(0xFFF5EFE6)
val Gold = Color(0xFFC9A961)
val Midnight = Color(0xFF0A0F1C)
val SoftTeal = Color(0xFF1B4D5C)

private val DarkColors = darkColorScheme(
    primary = Gold, onPrimary = Midnight,
    background = Midnight, onBackground = Ivory,
    surface = Color(0xFF0F1729), onSurface = Ivory,
    secondary = SoftTeal
)
private val LightColors = lightColorScheme(
    primary = Emerald, onPrimary = Ivory,
    background = Ivory, onBackground = Emerald,
    surface = Color(0xFFEFE6D6), onSurface = Emerald,
    secondary = Gold
)

@Composable
fun AlQuranTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
