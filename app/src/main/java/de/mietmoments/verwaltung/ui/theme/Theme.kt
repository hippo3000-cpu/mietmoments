package de.mietmoments.verwaltung.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ElegantMagicColors = lightColorScheme(
    primary = Color(0xFFB57A27),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE9BB),
    onPrimaryContainer = Color(0xFF4B3511),
    secondary = Color(0xFFC58B82),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE5DF),
    onSecondaryContainer = Color(0xFF53312C),
    tertiary = Color(0xFFD6B06A),
    onTertiary = Color(0xFF35270C),
    tertiaryContainer = Color(0xFFFFEDC7),
    onTertiaryContainer = Color(0xFF4A3712),
    background = Color(0xFFFFFBF3),
    surface = Color(0xFFFFFEFB),
    surfaceVariant = Color(0xFFF7EFE3),
    onSurface = Color(0xFF2D2823),
    onSurfaceVariant = Color(0xFF6D6257),
    outline = Color(0xFFB9AA98),
    error = Color(0xFFB3261E)
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
        colorScheme = ElegantMagicColors,
        typography = AppTypography,
        content = content
    )
}
