package de.mietmoments.verwaltung.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

private enum class MiloMood { IDLE, WALK, HOP, HAPPY, SAD, SLEEP, CURIOUS, BUSY }

@Composable
fun MiloCompanion(
    enabled: Boolean,
    animationsEnabled: Boolean,
    online: Boolean,
    syncing: Boolean,
    eventCount: Int,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    BoxWithConstraints(modifier.fillMaxSize()) {
        var targetX by remember { mutableStateOf(14.dp) }
        var targetY by remember { mutableStateOf(112.dp) }
        var facingRight by remember { mutableStateOf(true) }
        var mood by remember { mutableStateOf(MiloMood.IDLE) }
        var bubble by remember { mutableStateOf<String?>(null) }

        val x by animateDpAsState(
            targetValue = targetX,
            animationSpec = tween(if (animationsEnabled) 1850 else 1, easing = LinearEasing),
            label = "milo-x"
        )
        val y by animateDpAsState(
            targetValue = targetY,
            animationSpec = tween(if (animationsEnabled) 1450 else 1),
            label = "milo-y"
        )

        val transition = rememberInfiniteTransition(label = "milo-body")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(860, easing = LinearEasing)),
            label = "milo-phase"
        )

        val bounce = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.HOP -> -abs(sin(phase * PI)).toFloat() * 11f
            MiloMood.WALK -> -abs(sin(phase * 2f * PI)).toFloat() * 2.2f
            MiloMood.HAPPY -> -abs(sin(phase * 2f * PI)).toFloat() * 4.2f
            MiloMood.BUSY -> -abs(sin(phase * 3f * PI)).toFloat() * 1.8f
            else -> -abs(sin(phase * 2f * PI)).toFloat() * .65f
        }
        val tilt = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.WALK -> sin(phase * 2f * PI).toFloat() * 3.8f
            MiloMood.CURIOUS -> 7f
            MiloMood.SAD -> -4f
            else -> sin(phase * 2f * PI).toFloat() * 1.2f
        }
        val squashY = if (!animationsEnabled) 1f else when (mood) {
            MiloMood.HOP -> .94f + abs(sin(phase * PI)).toFloat() * .08f
            MiloMood.SLEEP -> .94f
            else -> 1f
        }

        fun commentFor(current: MiloMood): String = when {
            syncing -> listOf("Bin dran ✨", "Sekunde …", "Daten holen!").random()
            !online -> listOf("Huch?", "Kein Netz …", "Ich warte.").random()
            current == MiloMood.SLEEP -> listOf("Zzz …", "Kurz Pause.").random()
            current == MiloMood.HOP -> listOf("Juhu!", "Hopp! ✨").random()
            current == MiloMood.HAPPY -> listOf("Läuft!", "Sehr schön!", "Hihi!").random()
            eventCount >= 7 -> listOf("Viel los!", "Volle Woche!").random()
            else -> listOf("Na du?", "Alles im Blick.", "Los geht's ✨", "Hihi!").random()
        }

        LaunchedEffect(maxWidth, maxHeight, online, syncing, eventCount, animationsEnabled) {
            delay(900)
            while (isActive) {
                val nextMood = when {
                    syncing -> MiloMood.BUSY
                    !online -> MiloMood.SAD
                    else -> listOf(
                        MiloMood.IDLE,
                        MiloMood.WALK,
                        MiloMood.WALK,
                        MiloMood.HOP,
                        MiloMood.HAPPY,
                        MiloMood.CURIOUS,
                        MiloMood.SLEEP
                    ).random()
                }
                mood = nextMood

                if (animationsEnabled && nextMood != MiloMood.SLEEP) {
                    val minX = 8f
                    val maxX = (maxWidth.value - 62f).coerceAtLeast(minX)
                    val minY = 76f
                    val maxY = (maxHeight.value - 132f).coerceAtLeast(minY)
                    val newX = if (maxX > minX) Random.nextDouble(minX.toDouble(), maxX.toDouble()).toFloat() else minX
                    val newY = if (maxY > minY) Random.nextDouble(minY.toDouble(), maxY.toDouble()).toFloat() else minY
                    facingRight = newX >= targetX.value
                    targetX = newX.dp
                    targetY = newY.dp
                }

                val shouldComment = syncing || !online || Random.nextInt(100) < 15
                if (shouldComment) {
                    bubble = commentFor(nextMood)
                    delay(1550)
                    bubble = null
                }

                delay(
                    when (nextMood) {
                        MiloMood.SLEEP -> 5200L
                        MiloMood.WALK -> 2600L
                        MiloMood.HOP -> 1800L
                        else -> Random.nextLong(2600L, 4300L)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .zIndex(50f)
                .offset(x = x, y = y + bounce.dp)
                .widthIn(max = 112.dp)
                .clickable {
                    mood = MiloMood.HAPPY
                    bubble = "Hihi! ♥"
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = bubble != null,
                enter = fadeIn(tween(120)) + scaleIn(initialScale = .86f),
                exit = fadeOut(tween(160))
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = bubble.orEmpty(),
                        modifier = Modifier
                            .widthIn(max = 104.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            MiloFigure(
                mood = mood,
                modifier = Modifier
                    .size(52.dp)
                    .rotate(tilt)
                    .scale(
                        scaleX = if (facingRight) 1f else -1f,
                        scaleY = squashY
                    )
            )
        }
    }
}

@Composable
private fun MiloFigure(mood: MiloMood, modifier: Modifier = Modifier) {
    val body = Color(0xFFB8A8B7)
    val bodyDark = Color(0xFF8E7C8C)
    val snout = Color(0xFFE3C2BA)
    val innerEar = Color(0xFFF0D3CC)
    val eye = Color(0xFF30292D)
    val white = Color(0xFFFFFCF8)
    val cheek = Color(0x66F2AAA1)

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        drawOval(
            color = bodyDark.copy(alpha = .16f),
            topLeft = Offset(w * .13f, h * .81f),
            size = Size(w * .72f, h * .11f)
        )

        drawOval(
            color = body,
            topLeft = Offset(w * .08f, h * .43f),
            size = Size(w * .72f, h * .43f)
        )

        val legLift = if (mood == MiloMood.WALK || mood == MiloMood.HOP) .03f else 0f
        drawOval(bodyDark, Offset(w * .17f, h * (.73f - legLift)), Size(w * .18f, h * .20f))
        drawOval(bodyDark, Offset(w * .57f, h * (.73f + legLift)), Size(w * .18f, h * .20f))

        drawCircle(body, radius = w * .12f, center = Offset(w * .35f, h * .20f))
        drawCircle(body, radius = w * .12f, center = Offset(w * .74f, h * .20f))
        drawCircle(innerEar, radius = w * .062f, center = Offset(w * .35f, h * .20f))
        drawCircle(innerEar, radius = w * .062f, center = Offset(w * .74f, h * .20f))

        drawOval(
            color = body,
            topLeft = Offset(w * .24f, h * .08f),
            size = Size(w * .62f, h * .61f)
        )

        drawOval(
            color = snout,
            topLeft = Offset(w * .35f, h * .40f),
            size = Size(w * .47f, h * .28f)
        )

        drawCircle(body.copy(alpha = .25f), radius = w * .055f, center = Offset(w * .31f, h * .33f))
        drawCircle(cheek, radius = w * .055f, center = Offset(w * .71f, h * .48f))

        if (mood == MiloMood.SLEEP) {
            drawLine(eye, Offset(w * .42f, h * .31f), Offset(w * .51f, h * .33f), strokeWidth = w * .035f)
            drawLine(eye, Offset(w * .64f, h * .33f), Offset(w * .73f, h * .31f), strokeWidth = w * .035f)
        } else {
            drawCircle(white, radius = w * .074f, center = Offset(w * .47f, h * .31f))
            drawCircle(white, radius = w * .074f, center = Offset(w * .68f, h * .31f))
            val pupilY = if (mood == MiloMood.SAD) h * .34f else h * .315f
            drawCircle(eye, radius = w * .036f, center = Offset(w * .48f, pupilY))
            drawCircle(eye, radius = w * .036f, center = Offset(w * .67f, pupilY))
            drawCircle(Color.White, radius = w * .012f, center = Offset(w * .492f, pupilY - h * .012f))
            drawCircle(Color.White, radius = w * .012f, center = Offset(w * .682f, pupilY - h * .012f))
        }

        drawCircle(bodyDark, radius = w * .018f, center = Offset(w * .49f, h * .51f))
        drawCircle(bodyDark, radius = w * .018f, center = Offset(w * .67f, h * .51f))

        when (mood) {
            MiloMood.SAD -> drawArc(
                color = eye,
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(w * .51f, h * .57f),
                size = Size(w * .16f, h * .11f),
                style = Stroke(width = w * .025f)
            )
            MiloMood.CURIOUS, MiloMood.BUSY -> drawCircle(
                color = eye,
                radius = w * .024f,
                center = Offset(w * .59f, h * .61f),
                style = Stroke(width = w * .018f)
            )
            MiloMood.SLEEP -> drawLine(
                color = eye,
                start = Offset(w * .55f, h * .62f),
                end = Offset(w * .64f, h * .62f),
                strokeWidth = w * .018f
            )
            else -> drawArc(
                color = eye,
                startAngle = 18f,
                sweepAngle = 145f,
                useCenter = false,
                topLeft = Offset(w * .49f, h * .53f),
                size = Size(w * .20f, h * .14f),
                style = Stroke(width = w * .026f)
            )
        }

        if (mood == MiloMood.SAD) {
            drawLine(eye, Offset(w * .40f, h * .24f), Offset(w * .50f, h * .27f), strokeWidth = w * .018f)
            drawLine(eye, Offset(w * .65f, h * .27f), Offset(w * .75f, h * .24f), strokeWidth = w * .018f)
        }
    }
}
