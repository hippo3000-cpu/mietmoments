package de.mietmoments.verwaltung.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
        var targetX by remember { mutableStateOf(18.dp) }
        var targetY by remember { mutableStateOf(118.dp) }
        var facingRight by remember { mutableStateOf(true) }
        var mood by remember { mutableStateOf(MiloMood.IDLE) }
        var bubble by remember { mutableStateOf<String?>(null) }

        val x by animateDpAsState(
            targetValue = targetX,
            animationSpec = tween(
                durationMillis = if (animationsEnabled) 2350 else 1,
                easing = FastOutSlowInEasing
            ),
            label = "milo-x"
        )
        val y by animateDpAsState(
            targetValue = targetY,
            animationSpec = tween(
                durationMillis = if (animationsEnabled) 1950 else 1,
                easing = FastOutSlowInEasing
            ),
            label = "milo-y"
        )

        val motion = rememberInfiniteTransition(label = "milo-motion")
        val phase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1080, easing = LinearEasing)),
            label = "milo-step"
        )
        val blinkPhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4300, easing = LinearEasing)),
            label = "milo-blink"
        )
        val breathePhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
            label = "milo-breathe"
        )

        val bounce = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.HOP -> -abs(sin(phase * PI)).toFloat() * 13f
            MiloMood.WALK -> -abs(sin(phase * 2f * PI)).toFloat() * 2.4f
            MiloMood.HAPPY -> -abs(sin(phase * 2f * PI)).toFloat() * 5.2f
            MiloMood.BUSY -> -abs(sin(phase * 3f * PI)).toFloat() * 2.0f
            MiloMood.CURIOUS -> -abs(sin(phase * PI)).toFloat() * 1.0f
            else -> -abs(sin(phase * 2f * PI)).toFloat() * .55f
        }

        val tilt = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.WALK -> sin(phase * 2f * PI).toFloat() * 3.4f
            MiloMood.CURIOUS -> 7f + sin(phase * PI).toFloat() * 1.5f
            MiloMood.SAD -> -4f
            MiloMood.HAPPY -> sin(phase * 2f * PI).toFloat() * 2.1f
            else -> sin(phase * 2f * PI).toFloat() * .9f
        }

        val breathing = if (!animationsEnabled) 1f else {
            1f + sin(breathePhase * 2f * PI).toFloat() * .012f
        }
        val squashY = when (mood) {
            MiloMood.HOP -> (.94f + abs(sin(phase * PI)).toFloat() * .08f) * breathing
            MiloMood.SLEEP -> .95f * breathing
            else -> breathing
        }

        fun commentFor(current: MiloMood): String = when {
            syncing -> listOf("Bin dran ✨", "Sekunde …", "Ich sortiere.").random()
            !online -> listOf("Huch?", "Kein Netz …", "Ich warte.").random()
            current == MiloMood.SLEEP -> listOf("Zzz …", "Nur kurz.").random()
            current == MiloMood.HOP -> listOf("Hopp! ✨", "Juhu!").random()
            current == MiloMood.HAPPY -> listOf("Läuft!", "Sehr schön!", "Hihi!").random()
            eventCount >= 7 -> listOf("Viel los!", "Volle Woche!").random()
            else -> listOf("Na du?", "Alles im Blick.", "Hihi!", "Los geht's ✨").random()
        }

        LaunchedEffect(maxWidth, maxHeight, online, syncing, eventCount, animationsEnabled) {
            delay(1000)
            while (isActive) {
                val nextMood = when {
                    syncing -> MiloMood.BUSY
                    !online -> MiloMood.SAD
                    else -> listOf(
                        MiloMood.IDLE,
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

                if (animationsEnabled) {
                    val minX = 8f
                    val maxX = (maxWidth.value - 62f).coerceAtLeast(minX)
                    val minY = 78f
                    val maxY = (maxHeight.value - 138f).coerceAtLeast(minY)

                    when (nextMood) {
                        MiloMood.WALK, MiloMood.BUSY -> {
                            val dx = Random.nextInt(-115, 116).toFloat()
                            val dy = Random.nextInt(-55, 56).toFloat()
                            val newX = (targetX.value + dx).coerceIn(minX, maxX)
                            val newY = (targetY.value + dy).coerceIn(minY, maxY)
                            facingRight = newX >= targetX.value
                            targetX = newX.dp
                            targetY = newY.dp
                        }

                        MiloMood.HOP, MiloMood.HAPPY, MiloMood.CURIOUS -> {
                            val dx = Random.nextInt(-58, 59).toFloat()
                            val dy = Random.nextInt(-28, 29).toFloat()
                            val newX = (targetX.value + dx).coerceIn(minX, maxX)
                            val newY = (targetY.value + dy).coerceIn(minY, maxY)
                            facingRight = newX >= targetX.value
                            targetX = newX.dp
                            targetY = newY.dp
                        }

                        else -> Unit
                    }
                }

                val shouldComment = syncing || !online || Random.nextInt(100) < 12
                if (shouldComment) {
                    bubble = commentFor(nextMood)
                    delay(1450)
                    bubble = null
                }

                delay(
                    when (nextMood) {
                        MiloMood.SLEEP -> 5600L
                        MiloMood.WALK -> 3000L
                        MiloMood.HOP -> 1900L
                        MiloMood.HAPPY -> 2200L
                        else -> Random.nextLong(2800L, 4700L)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .zIndex(50f)
                .offset(x = x, y = y + bounce.dp)
                .widthIn(max = 106.dp)
                .clickable {
                    mood = MiloMood.HAPPY
                    bubble = "Hihi! ♥"
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = bubble != null,
                enter = fadeIn(tween(120)) + scaleIn(initialScale = .88f),
                exit = fadeOut(tween(180))
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .97f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = bubble.orEmpty(),
                        modifier = Modifier
                            .widthIn(max = 98.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            MiloFigure(
                mood = mood,
                blinkPhase = blinkPhase,
                phase = phase,
                modifier = Modifier
                    .size(56.dp)
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
private fun MiloFigure(
    mood: MiloMood,
    blinkPhase: Float,
    phase: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        val bodyLight = Color(0xFFD1C3CD)
        val bodyMid = Color(0xFFB7A5B3)
        val bodyDark = Color(0xFF796C78)
        val muzzleLight = Color(0xFFEBCFC7)
        val muzzleDark = Color(0xFFCFA79F)
        val earPink = Color(0xFFEBC4C3)
        val eye = Color(0xFF241F22)
        val eyeBrown = Color(0xFF5A4037)
        val white = Color(0xFFFFFDFC)
        val blush = Color(0x55F19B9F)

        drawOval(
            color = Color.Black.copy(alpha = .10f),
            topLeft = Offset(w * .12f, h * .83f),
            size = Size(w * .73f, h * .095f)
        )

        val tailSwing = if (mood == MiloMood.HAPPY || mood == MiloMood.WALK) {
            sin(phase * 2f * PI).toFloat() * h * .035f
        } else 0f
        val tail = Path().apply {
            moveTo(w * .16f, h * .61f)
            quadraticBezierTo(w * .03f, h * .57f + tailSwing, w * .08f, h * .48f + tailSwing)
        }
        drawPath(tail, color = bodyDark, style = Stroke(width = w * .035f))

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(bodyLight, bodyMid, bodyDark),
                center = Offset(w * .45f, h * .53f),
                radius = w * .72f
            ),
            topLeft = Offset(w * .08f, h * .42f),
            size = Size(w * .74f, h * .45f)
        )

        val step = sin(phase * 2f * PI).toFloat()
        val leftLift = if (mood == MiloMood.WALK) (-step).coerceAtLeast(0f) * h * .035f else 0f
        val rightLift = if (mood == MiloMood.WALK) step.coerceAtLeast(0f) * h * .035f else 0f

        drawOval(
            brush = Brush.verticalGradient(listOf(bodyMid, bodyDark)),
            topLeft = Offset(w * .18f, h * .73f - leftLift),
            size = Size(w * .18f, h * .20f)
        )
        drawOval(
            brush = Brush.verticalGradient(listOf(bodyMid, bodyDark)),
            topLeft = Offset(w * .58f, h * .73f - rightLift),
            size = Size(w * .18f, h * .20f)
        )

        drawLine(
            color = Color(0xFF675B66),
            start = Offset(w * .215f, h * .875f - leftLift),
            end = Offset(w * .285f, h * .875f - leftLift),
            strokeWidth = w * .012f
        )
        drawLine(
            color = Color(0xFF675B66),
            start = Offset(w * .615f, h * .875f - rightLift),
            end = Offset(w * .685f, h * .875f - rightLift),
            strokeWidth = w * .012f
        )

        drawCircle(
            brush = Brush.radialGradient(listOf(bodyLight, bodyMid, bodyDark)),
            radius = w * .125f,
            center = Offset(w * .34f, h * .20f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(bodyLight, bodyMid, bodyDark)),
            radius = w * .125f,
            center = Offset(w * .74f, h * .20f)
        )
        drawCircle(earPink, radius = w * .066f, center = Offset(w * .34f, h * .20f))
        drawCircle(earPink, radius = w * .066f, center = Offset(w * .74f, h * .20f))

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFD9CCD5), bodyMid, bodyDark),
                center = Offset(w * .48f, h * .28f),
                radius = w * .58f
            ),
            topLeft = Offset(w * .22f, h * .075f),
            size = Size(w * .66f, h * .62f)
        )

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(muzzleLight, Color(0xFFE0B9B1), muzzleDark),
                center = Offset(w * .52f, h * .48f),
                radius = w * .38f
            ),
            topLeft = Offset(w * .34f, h * .40f),
            size = Size(w * .49f, h * .30f)
        )

        drawCircle(Color.White.copy(alpha = .18f), radius = w * .08f, center = Offset(w * .39f, h * .19f))
        drawCircle(blush, radius = w * .052f, center = Offset(w * .72f, h * .49f))

        val blink = blinkPhase > .935f || mood == MiloMood.SLEEP
        val curiousShift = if (mood == MiloMood.CURIOUS) w * .016f else 0f

        if (blink) {
            drawArc(
                color = eye,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * .395f, h * .27f),
                size = Size(w * .12f, h * .07f),
                style = Stroke(width = w * .022f)
            )
            drawArc(
                color = eye,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * .615f, h * .27f),
                size = Size(w * .12f, h * .07f),
                style = Stroke(width = w * .022f)
            )
        } else {
            drawOval(white, Offset(w * .405f, h * .255f), Size(w * .115f, h * .14f))
            drawOval(white, Offset(w * .615f, h * .255f), Size(w * .115f, h * .14f))

            val pupilY = if (mood == MiloMood.SAD) h * .335f else h * .318f
            drawCircle(eyeBrown, radius = w * .037f, center = Offset(w * .466f + curiousShift, pupilY))
            drawCircle(eyeBrown, radius = w * .037f, center = Offset(w * .675f + curiousShift, pupilY))
            drawCircle(eye, radius = w * .025f, center = Offset(w * .466f + curiousShift, pupilY))
            drawCircle(eye, radius = w * .025f, center = Offset(w * .675f + curiousShift, pupilY))
            drawCircle(white, radius = w * .010f, center = Offset(w * .455f + curiousShift, pupilY - h * .016f))
            drawCircle(white, radius = w * .010f, center = Offset(w * .664f + curiousShift, pupilY - h * .016f))
        }

        drawOval(bodyDark, Offset(w * .475f, h * .505f), Size(w * .040f, h * .026f))
        drawOval(bodyDark, Offset(w * .665f, h * .505f), Size(w * .040f, h * .026f))

        when (mood) {
            MiloMood.SAD -> {
                drawArc(
                    color = eye,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(w * .515f, h * .58f),
                    size = Size(w * .17f, h * .11f),
                    style = Stroke(width = w * .024f)
                )
                drawLine(eye, Offset(w * .40f, h * .24f), Offset(w * .50f, h * .275f), strokeWidth = w * .017f)
                drawLine(eye, Offset(w * .64f, h * .275f), Offset(w * .74f, h * .24f), strokeWidth = w * .017f)
            }

            MiloMood.CURIOUS, MiloMood.BUSY -> {
                drawOval(
                    color = eye,
                    topLeft = Offset(w * .555f, h * .59f),
                    size = Size(w * .07f, h * .055f),
                    style = Stroke(width = w * .016f)
                )
            }

            MiloMood.SLEEP -> {
                drawLine(
                    color = eye,
                    start = Offset(w * .55f, h * .625f),
                    end = Offset(w * .64f, h * .625f),
                    strokeWidth = w * .016f
                )
            }

            MiloMood.HAPPY, MiloMood.HOP -> {
                drawArc(
                    color = eye,
                    startAngle = 16f,
                    sweepAngle = 148f,
                    useCenter = false,
                    topLeft = Offset(w * .49f, h * .535f),
                    size = Size(w * .22f, h * .15f),
                    style = Stroke(width = w * .027f)
                )
                drawCircle(Color(0xFFF49DA3).copy(alpha = .55f), radius = w * .042f, center = Offset(w * .42f, h * .50f))
                drawCircle(Color(0xFFF49DA3).copy(alpha = .55f), radius = w * .042f, center = Offset(w * .77f, h * .50f))
            }

            else -> {
                drawArc(
                    color = eye,
                    startAngle = 22f,
                    sweepAngle = 138f,
                    useCenter = false,
                    topLeft = Offset(w * .50f, h * .55f),
                    size = Size(w * .20f, h * .13f),
                    style = Stroke(width = w * .023f)
                )
            }
        }
    }
}
