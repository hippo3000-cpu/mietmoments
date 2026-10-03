package de.mietmoments.verwaltung.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6B5A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F1E7),
    onPrimaryContainer = Color(0xFF0B332A),
    secondary = Color(0xFFB8872E),
    secondaryContainer = Color(0xFFFFE6B0),
    background = Color(0xFFF8F5F0),
    surface = Color(0xFFFFFCF8),
    surfaceVariant = Color(0xFFE9E2DA),
    onSurface = Color(0xFF241F1B),
    outline = Color(0xFF81756D),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BD6C5),
    onPrimary = Color(0xFF07372D),
    primaryContainer = Color(0xFF155041),
    onPrimaryContainer = Color(0xFFC9F5E8),
    secondary = Color(0xFFF1C66E),
    secondaryContainer = Color(0xFF5A430E),
    background = Color(0xFF151917),
    surface = Color(0xFF1B211E),
    surfaceVariant = Color(0xFF3A403D),
    onSurface = Color(0xFFE7E4DE),
    outline = Color(0xFF9C948D),
    error = Color(0xFFFFB4AB)
)

@Composable
fun MietMomentsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
