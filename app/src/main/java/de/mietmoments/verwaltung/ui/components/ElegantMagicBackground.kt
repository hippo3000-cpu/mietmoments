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
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "magic-sparkle"
    )
    val sparkles = remember {
        listOf(
            Triple(.08f, .12f, 2.2f), Triple(.20f, .26f, 1.5f), Triple(.34f, .09f, 1.8f),
            Triple(.48f, .20f, 2.4f), Triple(.66f, .11f, 1.5f), Triple(.82f, .25f, 2.0f),
            Triple(.92f, .08f, 1.2f), Triple(.12f, .48f, 1.4f), Triple(.28f, .62f, 2.1f),
            Triple(.55f, .51f, 1.3f), Triple(.73f, .66f, 1.8f), Triple(.90f, .53f, 2.3f),
            Triple(.06f, .84f, 1.8f), Triple(.22f, .92f, 1.2f), Triple(.43f, .80f, 2.2f),
            Triple(.61f, .91f, 1.5f), Triple(.78f, .83f, 2.0f), Triple(.95f, .94f, 1.3f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFEFB),
                        Color(0xFFFFF9ED),
                        Color(0xFFFDF4E6),
                        Color(0xFFFFFCF6)
                    )
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55FFF0C7), Color.Transparent),
                    center = Offset(size.width * .18f, size.height * .13f),
                    radius = size.minDimension * .72f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x44F7DDB0), Color.Transparent),
                    center = Offset(size.width * .87f, size.height * .72f),
                    radius = size.minDimension * .62f
                )
            )

            sparkles.forEachIndexed { index, sparkle ->
                val pulse = if (animationsEnabled) {
                    ((sin((phase * 2f * PI + index * .83).toDouble()) + 1.0) / 2.0).toFloat()
                } else .45f
                val alpha = .10f + pulse * .34f
                val radius = sparkle.third * density * (.72f + pulse * .35f)
                val center = Offset(size.width * sparkle.first, size.height * sparkle.second)
                drawCircle(Color.White.copy(alpha = alpha), radius = radius * 2.4f, center = center)
                drawCircle(Color(0xFFFFD98D).copy(alpha = alpha * .85f), radius = radius, center = center)
            }
        }

        content()
    }
}
