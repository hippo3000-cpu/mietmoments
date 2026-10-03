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
    primary = Color(0xFF9E681D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE8B2),
    onPrimaryContainer = Color(0xFF49310B),
    secondary = Color(0xFFB87772),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE2DD),
    onSecondaryContainer = Color(0xFF512E2B),
    tertiary = Color(0xFFC99A45),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFEDC7),
    onTertiaryContainer = Color(0xFF49350D),
    background = Color(0xFFFFFBF5),
    surface = Color(0xF5FFFEFB),
    surfaceVariant = Color(0xE8F8EFE3),
    onSurface = Color(0xFF28231F),
    onSurfaceVariant = Color(0xFF6B5E53),
    outline = Color(0xFFB7A58E),
    outlineVariant = Color(0x66CBB99F),
    error = Color(0xFFB3261E)
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Black),
    headlineLarge = TextStyle(fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black),
    headlineMedium = TextStyle(fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun MietMomentsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ElegantMagicColors,
        typography = AppTypography,
        content = content
    )
}
