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
import androidx.compose.foundation.layout.Box
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class MiloMood {
    IDLE, WALK, TROT, HOP, HAPPY, SAD, SLEEP, CURIOUS, SNIFF, BUSY
}

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
                durationMillis = if (animationsEnabled) {
                    when (mood) {
                        MiloMood.TROT -> 1350
                        MiloMood.WALK, MiloMood.BUSY -> 2100
                        else -> 1500
                    }
                } else 1,
                easing = FastOutSlowInEasing
            ),
            label = "milo-x"
        )
        val y by animateDpAsState(
            targetValue = targetY,
            animationSpec = tween(
                durationMillis = if (animationsEnabled) 1750 else 1,
                easing = FastOutSlowInEasing
            ),
            label = "milo-y"
        )

        val motion = rememberInfiniteTransition(label = "milo-life")
        val stepPhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(760, easing = LinearEasing)),
            label = "milo-step"
        )
        val slowPhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
            label = "milo-breathe"
        )
        val blinkPhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
            label = "milo-blink"
        )
        val sparklePhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
            label = "milo-sparkle"
        )

        val hop = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.HOP -> -abs(sin(stepPhase * PI)).toFloat() * 16f
            MiloMood.HAPPY -> -abs(sin(stepPhase * 2f * PI)).toFloat() * 5f
            MiloMood.TROT -> -abs(sin(stepPhase * 2f * PI)).toFloat() * 2.4f
            MiloMood.WALK, MiloMood.BUSY -> -abs(sin(stepPhase * 2f * PI)).toFloat() * 1.6f
            else -> -abs(sin(slowPhase * 2f * PI)).toFloat() * .45f
        }

        val bodyTilt = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.CURIOUS -> 5.5f
            MiloMood.SNIFF -> -4.5f
            MiloMood.SAD -> -5.5f
            MiloMood.SLEEP -> -7f
            MiloMood.WALK -> sin(stepPhase * 2f * PI).toFloat() * 1.8f
            MiloMood.TROT -> sin(stepPhase * 2f * PI).toFloat() * 2.8f
            else -> sin(slowPhase * 2f * PI).toFloat() * .7f
        }

        fun commentFor(current: MiloMood): String = when {
            syncing -> listOf("Bin dran ✨", "Ich flitze!", "Daten holen …").random()
            !online -> listOf("Huch?", "Kein Netz …", "Ich warte.").random()
            current == MiloMood.SLEEP -> listOf("Zzz …", "Nur kurz.").random()
            current == MiloMood.SNIFF -> listOf("Schnüffel …", "Was ist hier?").random()
            current == MiloMood.HOP -> listOf("Hopp! ✨", "Juhu!").random()
            current == MiloMood.HAPPY -> listOf("Läuft!", "Hihi!", "Sehr schön!").random()
            eventCount >= 7 -> listOf("Viel los!", "Volle Woche!").random()
            else -> listOf("Na du?", "Alles im Blick.", "Hihi!").random()
        }

        LaunchedEffect(maxWidth, maxHeight, online, syncing, eventCount, animationsEnabled) {
            delay(850)
            while (isActive) {
                val next = when {
                    syncing -> MiloMood.BUSY
                    !online -> MiloMood.SAD
                    eventCount >= 8 && Random.nextInt(100) < 35 -> MiloMood.TROT
                    else -> listOf(
                        MiloMood.IDLE,
                        MiloMood.IDLE,
                        MiloMood.WALK,
                        MiloMood.WALK,
                        MiloMood.TROT,
                        MiloMood.HOP,
                        MiloMood.HAPPY,
                        MiloMood.CURIOUS,
                        MiloMood.SNIFF,
                        MiloMood.SLEEP
                    ).random()
                }
                mood = next

                if (animationsEnabled) {
                    val minX = 8f
                    val maxX = (maxWidth.value - 78f).coerceAtLeast(minX)
                    val minY = 78f
                    val maxY = (maxHeight.value - 152f).coerceAtLeast(minY)

                    when (next) {
                        MiloMood.WALK, MiloMood.TROT, MiloMood.BUSY -> {
                            val range = if (next == MiloMood.TROT) 150 else 110
                            val dx = Random.nextInt(-range, range + 1).toFloat()
                            val dy = Random.nextInt(-36, 37).toFloat()
                            val nx = (targetX.value + dx).coerceIn(minX, maxX)
                            val ny = (targetY.value + dy).coerceIn(minY, maxY)
                            facingRight = nx >= targetX.value
                            targetX = nx.dp
                            targetY = ny.dp
                        }
                        MiloMood.HOP, MiloMood.HAPPY, MiloMood.CURIOUS, MiloMood.SNIFF -> {
                            val dx = Random.nextInt(-48, 49).toFloat()
                            val nx = (targetX.value + dx).coerceIn(minX, maxX)
                            facingRight = nx >= targetX.value
                            targetX = nx.dp
                        }
                        else -> Unit
                    }
                }

                val shouldComment = syncing || !online || Random.nextInt(100) < 8
                if (shouldComment) {
                    bubble = commentFor(next)
                    delay(1450)
                    bubble = null
                }

                delay(
                    when (next) {
                        MiloMood.SLEEP -> 5200L
                        MiloMood.WALK -> 3000L
                        MiloMood.TROT -> 2350L
                        MiloMood.HOP -> 1750L
                        MiloMood.HAPPY -> 2100L
                        MiloMood.SNIFF -> 2500L
                        else -> Random.nextLong(2600L, 4400L)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .zIndex(50f)
                .offset(x = x, y = y + hop.dp)
                .widthIn(max = 112.dp)
                .clickable {
                    mood = MiloMood.HAPPY
                    bubble = "Hihi! ♥"
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = bubble != null,
                enter = fadeIn(tween(110)) + scaleIn(initialScale = .9f),
                exit = fadeOut(tween(160))
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .97f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        bubble.orEmpty(),
                        modifier = Modifier
                            .widthIn(max = 100.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                MiloFourPaws(
                    mood = mood,
                    stepPhase = stepPhase,
                    slowPhase = slowPhase,
                    blinkPhase = blinkPhase,
                    facingRight = facingRight,
                    animated = animationsEnabled,
                    modifier = Modifier
                        .size(70.dp)
                        .rotate(bodyTilt)
                )

                if (mood == MiloMood.HAPPY || mood == MiloMood.HOP || mood == MiloMood.BUSY) {
                    Canvas(Modifier.fillMaxSize()) {
                        repeat(5) { index ->
                            val angle = sparklePhase * 2f * PI + index * (2f * PI / 5f)
                            val radius = size.minDimension * .40f
                            val sx = size.width / 2f + cos(angle).toFloat() * radius
                            val sy = size.height / 2f + sin(angle).toFloat() * radius
                            val pulse = .45f + abs(sin(angle * 1.7f)).toFloat() * .55f
                            val arm = 2.2f * density * pulse
                            val c = Color.White.copy(alpha = .48f + .45f * pulse)
                            drawLine(c, Offset(sx - arm, sy), Offset(sx + arm, sy), .8f * density)
                            drawLine(c, Offset(sx, sy - arm), Offset(sx, sy + arm), .8f * density)
                            drawCircle(
                                Color(0xFFFFD68A).copy(alpha = .62f * pulse),
                                radius = .9f * density * pulse,
                                center = Offset(sx, sy)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiloFourPaws(
    mood: MiloMood,
    stepPhase: Float,
    slowPhase: Float,
    blinkPhase: Float,
    facingRight: Boolean,
    animated: Boolean,
    modifier: Modifier = Modifier
) {
    val direction = if (facingRight) 1f else -1f

    Canvas(modifier.scale(scaleX = direction, scaleY = 1f)) {
        val w = size.width
        val h = size.height
        val step = if (animated) sin(stepPhase * 2f * PI).toFloat() else 0f
        val slow = if (animated) sin(slowPhase * 2f * PI).toFloat() else 0f

        val bodyTop = when (mood) {
            MiloMood.SLEEP -> h * .48f
            MiloMood.SNIFF -> h * .38f
            else -> h * .32f
        }
        val bodyHeight = when (mood) {
            MiloMood.SLEEP -> h * .29f
            else -> h * .40f
        }

        val walkAmount = when (mood) {
            MiloMood.WALK, MiloMood.BUSY -> .075f
            MiloMood.TROT -> .11f
            else -> 0f
        }
        val frontLift = step * h * walkAmount
        val rearLift = -step * h * walkAmount

        val bodyLight = Color(0xFFFFA7C7)
        val bodyMid = Color(0xFFF56B9A)
        val bodyDark = Color(0xFFC93E70)
        val shadowPink = Color(0xFFD85282)
        val muzzleLight = Color(0xFFFFCAD9)
        val muzzleMid = Color(0xFFFF9FBC)
        val earInner = Color(0xFFF67EA4)
        val eyeDark = Color(0xFF24141D)
        val iris = Color(0xFF7B244A)
        val mouth = Color(0xFF7A1939)
        val tongue = Color(0xFFFF789B)
        val nail = Color(0xFFFFE7DA)

        val shadowScale = when (mood) {
            MiloMood.HOP -> .62f + abs(sin(stepPhase * PI)).toFloat() * .25f
            else -> 1f
        }
        drawOval(
            color = Color.Black.copy(alpha = .10f),
            topLeft = Offset(w * (.18f + (1f - shadowScale) * .18f), h * .82f),
            size = Size(w * .65f * shadowScale, h * .075f)
        )

        val tailWag = when (mood) {
            MiloMood.HAPPY, MiloMood.WALK, MiloMood.TROT, MiloMood.BUSY ->
                if (animated) sin(stepPhase * 4f * PI).toFloat() * h * .05f else 0f
            else -> 0f
        }
        val tail = Path().apply {
            moveTo(w * .20f, bodyTop + bodyHeight * .45f)
            quadraticBezierTo(
                w * .06f,
                bodyTop + bodyHeight * .34f + tailWag,
                w * .10f,
                bodyTop + bodyHeight * .16f + tailWag
            )
        }
        drawPath(tail, bodyDark, style = Stroke(width = w * .032f))

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(bodyLight, bodyMid, bodyDark),
                center = Offset(w * .50f, bodyTop + bodyHeight * .34f),
                radius = w * .63f
            ),
            topLeft = Offset(w * .13f, bodyTop),
            size = Size(w * .66f, bodyHeight)
        )

        val legBaseY = if (mood == MiloMood.SLEEP) h * .68f else h * .64f
        val legH = if (mood == MiloMood.SLEEP) h * .12f else h * .24f
        val legW = w * .15f

        fun leg(x: Float, lift: Float, front: Boolean) {
            val compress = if (mood == MiloMood.HOP) .82f else 1f
            val y = legBaseY + lift
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(bodyMid, bodyDark)),
                topLeft = Offset(w * x, y),
                size = Size(legW, legH * compress),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(legW * .45f)
            )
            val toeY = y + legH * compress * .76f
            repeat(2) { idx ->
                drawOval(
                    color = nail,
                    topLeft = Offset(w * x + legW * (.18f + idx * .36f), toeY),
                    size = Size(legW * .24f, legH * .16f)
                )
            }
        }

        val rearA = if (mood == MiloMood.HOP) -h * .07f else rearLift
        val rearB = if (mood == MiloMood.HOP) -h * .055f else -rearLift * .72f
        val frontA = if (mood == MiloMood.HOP) -h * .09f else frontLift
        val frontB = if (mood == MiloMood.HOP) -h * .075f else -frontLift * .72f

        leg(.20f, rearA, false)
        leg(.34f, rearB, false)
        leg(.57f, frontB, true)
        leg(.69f, frontA, true)

        val sniffDrop = if (mood == MiloMood.SNIFF) h * .13f else 0f
        val sleepDrop = if (mood == MiloMood.SLEEP) h * .10f else 0f
        val curiousTilt = if (mood == MiloMood.CURIOUS) -h * .018f else 0f
        val headCx = w * .67f
        val headCy = h * .34f + sniffDrop + sleepDrop + curiousTilt
        val headRx = w * .28f
        val headRy = h * .29f

        val earWiggle = when (mood) {
            MiloMood.HAPPY, MiloMood.CURIOUS, MiloMood.BUSY ->
                if (animated) sin(stepPhase * 4f * PI).toFloat() * h * .015f else 0f
            else -> 0f
        }

        drawOval(
            brush = Brush.radialGradient(listOf(bodyLight, bodyMid, bodyDark)),
            topLeft = Offset(headCx - headRx * .83f, headCy - headRy * 1.07f - earWiggle),
            size = Size(headRx * .62f, headRy * .55f)
        )
        drawOval(
            brush = Brush.radialGradient(listOf(bodyLight, bodyMid, bodyDark)),
            topLeft = Offset(headCx + headRx * .28f, headCy - headRy * 1.03f + earWiggle),
            size = Size(headRx * .62f, headRy * .55f)
        )
        drawOval(
            color = earInner,
            topLeft = Offset(headCx - headRx * .69f, headCy - headRy * .91f - earWiggle),
            size = Size(headRx * .34f, headRy * .30f)
        )
        drawOval(
            color = earInner,
            topLeft = Offset(headCx + headRx * .40f, headCy - headRy * .87f + earWiggle),
            size = Size(headRx * .34f, headRy * .30f)
        )

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFBCD1), bodyMid, bodyDark),
                center = Offset(headCx - w * .05f, headCy - h * .08f),
                radius = headRx * 1.2f
            ),
            topLeft = Offset(headCx - headRx, headCy - headRy),
            size = Size(headRx * 2f, headRy * 2f)
        )

        drawOval(
            brush = Brush.radialGradient(
                listOf(muzzleLight, muzzleMid, Color(0xFFE96691)),
                center = Offset(headCx + w * .035f, headCy + h * .05f),
                radius = headRx * .82f
            ),
            topLeft = Offset(headCx - headRx * .82f, headCy + headRy * .02f),
            size = Size(headRx * 1.55f, headRy * .82f)
        )

        drawCircle(
            Color.White.copy(alpha = .25f),
            radius = w * .055f,
            center = Offset(headCx - w * .10f, headCy - h * .11f)
        )

        val blink = mood == MiloMood.SLEEP || blinkPhase > .935f
        val sad = mood == MiloMood.SAD
        val eyeY = headCy - h * .065f
        val eyeLX = headCx - w * .075f
        val eyeRX = headCx + w * .095f

        fun eye(cx: Float) {
            if (blink) {
                drawArc(
                    color = eyeDark,
                    startAngle = 10f,
                    sweepAngle = 155f,
                    useCenter = false,
                    topLeft = Offset(cx - w * .055f, eyeY - h * .015f),
                    size = Size(w * .11f, h * .055f),
                    style = Stroke(width = w * .018f)
                )
            } else {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(cx - w * .055f, eyeY - h * .065f),
                    size = Size(w * .11f, h * .14f)
                )
                val look = when (mood) {
                    MiloMood.CURIOUS -> w * .010f
                    MiloMood.SNIFF -> -w * .005f
                    else -> 0f
                }
                val pupilY = eyeY + if (sad) h * .018f else 0f
                drawCircle(iris, radius = w * .035f, center = Offset(cx + look, pupilY))
                drawCircle(eyeDark, radius = w * .024f, center = Offset(cx + look, pupilY))
                drawCircle(Color.White, radius = w * .009f, center = Offset(cx + look - w * .009f, pupilY - h * .018f))
            }
        }
        eye(eyeLX)
        eye(eyeRX)

        drawOval(
            color = shadowPink,
            topLeft = Offset(headCx - w * .065f, headCy + h * .095f),
            size = Size(w * .035f, h * .025f)
        )
        drawOval(
            color = shadowPink,
            topLeft = Offset(headCx + w * .075f, headCy + h * .095f),
            size = Size(w * .035f, h * .025f)
        )

        when (mood) {
            MiloMood.HAPPY, MiloMood.HOP, MiloMood.BUSY, MiloMood.TROT -> {
                drawArc(
                    color = mouth,
                    startAngle = 12f,
                    sweepAngle = 155f,
                    useCenter = false,
                    topLeft = Offset(headCx - w * .09f, headCy + h * .11f),
                    size = Size(w * .18f, h * .10f),
                    style = Stroke(width = w * .021f)
                )
                drawOval(
                    color = tongue,
                    topLeft = Offset(headCx - w * .027f, headCy + h * .175f),
                    size = Size(w * .07f, h * .035f)
                )
            }
            MiloMood.SAD -> {
                drawArc(
                    color = mouth,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(headCx - w * .075f, headCy + h * .155f),
                    size = Size(w * .15f, h * .075f),
                    style = Stroke(width = w * .018f)
                )
            }
            MiloMood.SLEEP -> {
                drawLine(
                    color = mouth,
                    start = Offset(headCx - w * .035f, headCy + h * .18f),
                    end = Offset(headCx + w * .035f, headCy + h * .18f),
                    strokeWidth = w * .014f
                )
            }
            else -> {
                drawArc(
                    color = mouth,
                    startAngle = 18f,
                    sweepAngle = 145f,
                    useCenter = false,
                    topLeft = Offset(headCx - w * .07f, headCy + h * .125f),
                    size = Size(w * .14f, h * .075f),
                    style = Stroke(width = w * .018f)
                )
            }
        }

        if (sad) {
            drawLine(
                eyeDark,
                Offset(eyeLX - w * .045f, eyeY - h * .085f),
                Offset(eyeLX + w * .028f, eyeY - h * .065f),
                strokeWidth = w * .013f
            )
            drawLine(
                eyeDark,
                Offset(eyeRX - w * .028f, eyeY - h * .065f),
                Offset(eyeRX + w * .045f, eyeY - h * .085f),
                strokeWidth = w * .013f
            )
        }

        if (mood == MiloMood.SNIFF) {
            val puff = .5f + slow * .5f
            repeat(2) { i ->
                drawCircle(
                    color = Color.White.copy(alpha = .25f + .18f * puff),
                    radius = w * (.018f + i * .007f),
                    center = Offset(headCx + w * (.20f + i * .055f), headCy + h * (.12f - i * .025f))
                )
            }
        }

        if (mood == MiloMood.SLEEP) {
            val z = .5f + .5f * slow
            drawLine(
                Color(0xFFD95A89).copy(alpha = .55f + .3f * z),
                Offset(headCx + w * .19f, headCy - h * .16f),
                Offset(headCx + w * .25f, headCy - h * .20f),
                strokeWidth = w * .012f
            )
        }
    }
}
