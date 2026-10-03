package de.mietmoments.verwaltung.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E6B5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F4EC),
    onPrimaryContainer = Color(0xFF083B33),
    secondary = Color(0xFFE0A83A),
    onSecondary = Color(0xFF362600),
    secondaryContainer = Color(0xFFFFE6AC),
    onSecondaryContainer = Color(0xFF4B3600),
    tertiary = Color(0xFF5E6FCE),
    tertiaryContainer = Color(0xFFE3E6FF),
    background = Color(0xFFF6F7F4),
    surface = Color(0xFFFFFBF6),
    surfaceVariant = Color(0xFFE9EDE8),
    onSurface = Color(0xFF17201D),
    onSurfaceVariant = Color(0xFF5C6864),
    outline = Color(0xFF7A8682),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AD8C4),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF0B5145),
    onPrimaryContainer = Color(0xFFB5F2E1),
    secondary = Color(0xFFF0C66B),
    onSecondary = Color(0xFF3C2F00),
    secondaryContainer = Color(0xFF574500),
    onSecondaryContainer = Color(0xFFFFE6A3),
    tertiary = Color(0xFFBBC3FF),
    tertiaryContainer = Color(0xFF404A9A),
    background = Color(0xFF101714),
    surface = Color(0xFF151E1A),
    surfaceVariant = Color(0xFF27302D),
    onSurface = Color(0xFFE3EAE6),
    onSurfaceVariant = Color(0xFFBEC8C3),
    outline = Color(0xFF89948F),
    error = Color(0xFFFFB4AB)
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Black),
    headlineLarge = TextStyle(fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black),
    headlineMedium = TextStyle(fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun MietMomentsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
