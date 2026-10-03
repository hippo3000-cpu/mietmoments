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
import kotlin.math.cos
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
        animationSpec = infiniteRepeatable(tween(7600, easing = LinearEasing)),
        label = "magic-sparkle"
    )
    val shimmer by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(9800, easing = LinearEasing)),
        label = "magic-shimmer"
    )

    val sparkles = remember {
        listOf(
            Triple(.04f,.07f,1.8f), Triple(.10f,.17f,1.1f), Triple(.18f,.09f,1.3f),
            Triple(.27f,.22f,1.9f), Triple(.34f,.06f,1.0f), Triple(.42f,.16f,1.5f),
            Triple(.51f,.08f,1.2f), Triple(.59f,.24f,1.8f), Triple(.68f,.11f,1.0f),
            Triple(.77f,.20f,1.6f), Triple(.86f,.07f,1.2f), Triple(.95f,.18f,1.9f),
            Triple(.06f,.36f,1.1f), Triple(.16f,.47f,1.7f), Triple(.25f,.34f,1.0f),
            Triple(.37f,.50f,1.4f), Triple(.47f,.38f,1.2f), Triple(.56f,.55f,1.8f),
            Triple(.66f,.40f,1.0f), Triple(.74f,.51f,1.5f), Triple(.85f,.37f,1.2f),
            Triple(.94f,.49f,1.7f), Triple(.04f,.67f,1.5f), Triple(.13f,.79f,1.0f),
            Triple(.22f,.63f,1.8f), Triple(.32f,.73f,1.1f), Triple(.43f,.65f,1.4f),
            Triple(.54f,.81f,1.9f), Triple(.63f,.69f,1.0f), Triple(.72f,.78f,1.5f),
            Triple(.82f,.64f,1.2f), Triple(.92f,.75f,1.8f), Triple(.07f,.91f,1.1f),
            Triple(.20f,.96f,1.6f), Triple(.36f,.88f,1.0f), Triple(.48f,.96f,1.7f),
            Triple(.65f,.91f,1.2f), Triple(.79f,.96f,1.5f), Triple(.94f,.90f,1.1f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFEFD),
                        Color(0xFFFFFBF4),
                        Color(0xFFFFF4E4),
                        Color(0xFFFFFAF2),
                        Color(0xFFFFFDFC)
                    )
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x7AFFF0C2), Color.Transparent),
                    center = Offset(size.width * .12f, size.height * .10f),
                    radius = size.minDimension * .82f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55F6D8C7), Color.Transparent),
                    center = Offset(size.width * .91f, size.height * .30f),
                    radius = size.minDimension * .62f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4CFFE8AE), Color.Transparent),
                    center = Offset(size.width * .82f, size.height * .80f),
                    radius = size.minDimension * .72f
                )
            )

            if (animationsEnabled) {
                val centerX = size.width * shimmer
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = .03f),
                            Color(0xFFFFE4A6).copy(alpha = .10f),
                            Color.White.copy(alpha = .22f),
                            Color(0xFFFFE4A6).copy(alpha = .08f),
                            Color.Transparent
                        ),
                        start = Offset(centerX - size.width * .24f, 0f),
                        end = Offset(centerX + size.width * .24f, size.height)
                    )
                )
            }

            sparkles.forEachIndexed { index, sparkle ->
                val wave = if (animationsEnabled) {
                    ((sin((phase * 2f * PI + index * .71).toDouble()) + 1.0) / 2.0).toFloat()
                } else .55f
                val driftX = if (animationsEnabled) cos((phase * 2f * PI + index).toDouble()).toFloat() * 2.2f * density else 0f
                val driftY = if (animationsEnabled) sin((phase * 2f * PI + index * .6).toDouble()).toFloat() * 1.8f * density else 0f

                val alpha = .11f + wave * .50f
                val base = sparkle.third * density
                val center = Offset(
                    size.width * sparkle.first + driftX,
                    size.height * sparkle.second + driftY
                )

                drawCircle(
                    color = Color.White.copy(alpha = alpha * .72f),
                    radius = base * (2.3f + wave * .9f),
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFD98B).copy(alpha = alpha * .92f),
                    radius = base * (.62f + wave * .28f),
                    center = center
                )

                if (index % 3 == 0) {
                    val arm = base * (3.2f + wave * 1.5f)
                    val stroke = (.62f + wave * .42f) * density
                    val ray = Color.White.copy(alpha = alpha)

                    drawLine(ray, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), stroke)
                    drawLine(ray, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), stroke)
                    drawLine(
                        ray.copy(alpha = alpha * .62f),
                        Offset(center.x - arm * .55f, center.y - arm * .55f),
                        Offset(center.x + arm * .55f, center.y + arm * .55f),
                        stroke * .72f
                    )
                    drawLine(
                        ray.copy(alpha = alpha * .62f),
                        Offset(center.x + arm * .55f, center.y - arm * .55f),
                        Offset(center.x - arm * .55f, center.y + arm * .55f),
                        stroke * .72f
                    )
                }
            }
        }

        content()
    }
}
