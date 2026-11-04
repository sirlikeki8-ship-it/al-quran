package com.likeki.alquran.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.likeki.alquran.AlQuranApp

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
fun AlQuranTheme(
    themeModeOverride: Int? = null,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as AlQuranApp
    val systemDark = isSystemInDarkTheme()

    val themeModeState = app.settings.themeMode.collectAsState(initial = 0)
    val textSizeState = app.settings.textSize.collectAsState(initial = 1)

    val mode = themeModeOverride ?: themeModeState.value
    val textSize = textSizeState.value

    val useDark = when (mode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

    val fontScale = when (textSize) {
        0 -> 0.85f
        2 -> 1.20f
        else -> 1.0f
    }

    val density = LocalDensity.current
    val newDensity = remember(density, fontScale) {
        Density(density.density, density.fontScale * fontScale)
    }

    CompositionLocalProvider(LocalDensity provides newDensity) {
        MaterialTheme(
            colorScheme = if (useDark) DarkColors else LightColors,
            content = content
        )
    }
}
