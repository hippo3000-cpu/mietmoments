package de.mietmoments.verwaltung.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ElegantMagicBackground(
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "magic-bg")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6200, easing = LinearEasing)),
        label = "magic-sparkle"
    )

    val sparkles = remember {
        listOf(
            Triple(.06f, .10f, 1.7f), Triple(.15f, .22f, 1.0f), Triple(.27f, .08f, 1.4f),
            Triple(.39f, .18f, 1.8f), Triple(.52f, .08f, 1.1f), Triple(.64f, .21f, 1.6f),
            Triple(.77f, .12f, 1.2f), Triple(.90f, .25f, 1.7f), Triple(.96f, .08f, .9f),
            Triple(.09f, .42f, 1.0f), Triple(.23f, .55f, 1.6f), Triple(.36f, .40f, .9f),
            Triple(.49f, .58f, 1.3f), Triple(.62f, .45f, 1.0f), Triple(.75f, .59f, 1.5f),
            Triple(.90f, .47f, 1.1f), Triple(.05f, .78f, 1.4f), Triple(.18f, .91f, .9f),
            Triple(.31f, .81f, 1.7f), Triple(.46f, .93f, 1.1f), Triple(.59f, .79f, 1.3f),
            Triple(.72f, .91f, .9f), Triple(.84f, .80f, 1.6f), Triple(.95f, .93f, 1.0f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFEFC),
                        Color(0xFFFFFAF1),
                        Color(0xFFFFF5E8),
                        Color(0xFFFFFCF7)
                    )
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FFF0C9), Color.Transparent),
                    center = Offset(size.width * .17f, size.height * .12f),
                    radius = size.minDimension * .76f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x48F6D9AD), Color.Transparent),
                    center = Offset(size.width * .88f, size.height * .72f),
                    radius = size.minDimension * .66f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x24FFFDF7), Color.Transparent),
                    center = Offset(size.width * .52f, size.height * .42f),
                    radius = size.minDimension * .44f
                )
            )

            sparkles.forEachIndexed { index, sparkle ->
                val pulse = if (animationsEnabled) {
                    ((sin((phase * 2f * PI + index * .67).toDouble()) + 1.0) / 2.0).toFloat()
                } else .42f

                val alpha = .07f + pulse * .28f
                val base = sparkle.third * density
                val center = Offset(size.width * sparkle.first, size.height * sparkle.second)

                drawCircle(Color.White.copy(alpha = alpha * .95f), radius = base * 2.1f, center = center)
                drawCircle(Color(0xFFFFDDA0).copy(alpha = alpha), radius = base * .72f, center = center)

                if (index % 4 == 0) {
                    val arm = base * (2.7f + pulse * .8f)
                    drawLine(
                        color = Color.White.copy(alpha = alpha * .9f),
                        start = Offset(center.x - arm, center.y),
                        end = Offset(center.x + arm, center.y),
                        strokeWidth = .7f * density
                    )
                    drawLine(
                        color = Color.White.copy(alpha = alpha * .9f),
                        start = Offset(center.x, center.y - arm),
                        end = Offset(center.x, center.y + arm),
                        strokeWidth = .7f * density
                    )
                }
            }
        }

        content()
    }
}
