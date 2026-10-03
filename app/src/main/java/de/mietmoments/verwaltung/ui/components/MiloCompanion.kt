package de.mietmoments.verwaltung.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
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

    val context = LocalContext.current
    val milo = remember(context) {
        runCatching {
            val encoded = buildString {
                repeat(6) { index ->
                    val file = "milo/milo_pink_3d_" + "%02d".format(index) + ".b64"
                    append(context.assets.open(file).bufferedReader().use { it.readText() })
                }
            }
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }

    if (milo == null) return

    BoxWithConstraints(modifier.fillMaxSize()) {
        var targetX by remember { mutableStateOf(18.dp) }
        var targetY by remember { mutableStateOf(118.dp) }
        var facingRight by remember { mutableStateOf(true) }
        var mood by remember { mutableStateOf(MiloMood.IDLE) }
        var bubble by remember { mutableStateOf<String?>(null) }

        val x by animateDpAsState(
            targetValue = targetX,
            animationSpec = tween(
                durationMillis = if (animationsEnabled) 2500 else 1,
                easing = FastOutSlowInEasing
            ),
            label = "milo-x"
        )
        val y by animateDpAsState(
            targetValue = targetY,
            animationSpec = tween(
                durationMillis = if (animationsEnabled) 2150 else 1,
                easing = FastOutSlowInEasing
            ),
            label = "milo-y"
        )

        val motion = rememberInfiniteTransition(label = "milo-3d-motion")
        val phase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(980, easing = LinearEasing)),
            label = "milo-step"
        )
        val breathePhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2850, easing = LinearEasing)),
            label = "milo-breathe"
        )
        val sparklePhase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
            label = "milo-sparkles"
        )

        val breathing = if (animationsEnabled) {
            1f + sin(breathePhase * 2f * PI).toFloat() * .014f
        } else 1f

        val bounce = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.HOP -> -abs(sin(phase * PI)).toFloat() * 17f
            MiloMood.WALK -> -abs(sin(phase * 2f * PI)).toFloat() * 3.1f
            MiloMood.HAPPY -> -abs(sin(phase * 2f * PI)).toFloat() * 6.0f
            MiloMood.BUSY -> -abs(sin(phase * 3f * PI)).toFloat() * 2.5f
            MiloMood.CURIOUS -> -abs(sin(phase * PI)).toFloat() * 1.4f
            else -> -abs(sin(phase * 2f * PI)).toFloat() * .65f
        }

        val tilt = if (!animationsEnabled) 0f else when (mood) {
            MiloMood.WALK -> sin(phase * 2f * PI).toFloat() * 4.0f
            MiloMood.HOP -> sin(phase * PI).toFloat() * 4.5f
            MiloMood.CURIOUS -> 7.5f + sin(phase * PI).toFloat() * 1.4f
            MiloMood.SAD -> -8f
            MiloMood.SLEEP -> -10f
            MiloMood.HAPPY -> sin(phase * 2f * PI).toFloat() * 2.8f
            MiloMood.BUSY -> sin(phase * 3f * PI).toFloat() * 2.0f
            else -> sin(phase * 2f * PI).toFloat() * .8f
        }

        val scaleY = when (mood) {
            MiloMood.HOP -> (.92f + abs(sin(phase * PI)).toFloat() * .12f) * breathing
            MiloMood.SLEEP -> .93f * breathing
            MiloMood.SAD -> .96f * breathing
            else -> breathing
        }
        val scaleX = when (mood) {
            MiloMood.HOP -> 1.04f - abs(sin(phase * PI)).toFloat() * .05f
            MiloMood.HAPPY -> 1.025f
            else -> 1f
        }

        val opacity = when (mood) {
            MiloMood.SLEEP -> .88f
            MiloMood.SAD -> .93f
            else -> 1f
        }

        fun commentFor(current: MiloMood): String = when {
            syncing -> listOf("Bin dran ✨", "Sekunde …", "Ich sortiere.").random()
            !online -> listOf("Huch?", "Kein Netz …", "Ich warte.").random()
            current == MiloMood.SLEEP -> listOf("Zzz …", "Nur kurz.").random()
            current == MiloMood.HOP -> listOf("Hopp! ✨", "Juhu!").random()
            current == MiloMood.HAPPY -> listOf("Läuft!", "Sehr schön!", "Hihi!").random()
            current == MiloMood.CURIOUS -> listOf("Was ist da?", "Hm? 👀").random()
            eventCount >= 7 -> listOf("Viel los!", "Volle Woche!").random()
            else -> listOf("Na du?", "Alles im Blick.", "Hihi!", "Los geht's ✨").random()
        }

        LaunchedEffect(maxWidth, maxHeight, online, syncing, eventCount, animationsEnabled) {
            delay(900)
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
                    val minX = 6f
                    val maxX = (maxWidth.value - 72f).coerceAtLeast(minX)
                    val minY = 78f
                    val maxY = (maxHeight.value - 150f).coerceAtLeast(minY)

                    when (nextMood) {
                        MiloMood.WALK, MiloMood.BUSY -> {
                            val dx = Random.nextInt(-125, 126).toFloat()
                            val dy = Random.nextInt(-48, 49).toFloat()
                            val newX = (targetX.value + dx).coerceIn(minX, maxX)
                            val newY = (targetY.value + dy).coerceIn(minY, maxY)
                            facingRight = newX >= targetX.value
                            targetX = newX.dp
                            targetY = newY.dp
                        }
                        MiloMood.HOP, MiloMood.HAPPY, MiloMood.CURIOUS -> {
                            val dx = Random.nextInt(-66, 67).toFloat()
                            val dy = Random.nextInt(-25, 26).toFloat()
                            val newX = (targetX.value + dx).coerceIn(minX, maxX)
                            val newY = (targetY.value + dy).coerceIn(minY, maxY)
                            facingRight = newX >= targetX.value
                            targetX = newX.dp
                            targetY = newY.dp
                        }
                        else -> Unit
                    }
                }

                val shouldComment = syncing || !online || Random.nextInt(100) < 10
                if (shouldComment) {
                    bubble = commentFor(nextMood)
                    delay(1500)
                    bubble = null
                }

                delay(
                    when (nextMood) {
                        MiloMood.SLEEP -> 5200L
                        MiloMood.WALK -> 3000L
                        MiloMood.HOP -> 1850L
                        MiloMood.HAPPY -> 2300L
                        else -> Random.nextLong(3000L, 4800L)
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
                enter = fadeIn(tween(120)) + scaleIn(initialScale = .88f),
                exit = fadeOut(tween(180))
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .97f),
                    shadowElevation = 5.dp
                ) {
                    Text(
                        text = bubble.orEmpty(),
                        modifier = Modifier
                            .widthIn(max = 102.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val shadowPulse = when (mood) {
                        MiloMood.HOP -> 1f - abs(sin(phase * PI)).toFloat() * .34f
                        else -> 1f
                    }
                    drawOval(
                        color = Color.Black.copy(alpha = .10f * shadowPulse),
                        topLeft = Offset(size.width * .20f, size.height * .79f),
                        size = androidx.compose.ui.geometry.Size(
                            size.width * .60f * shadowPulse,
                            size.height * .11f
                        )
                    )

                    if (mood == MiloMood.HAPPY || mood == MiloMood.HOP || mood == MiloMood.BUSY) {
                        repeat(5) { index ->
                            val angle = sparklePhase * 2f * PI + index * (2f * PI / 5f)
                            val radius = size.minDimension * (.34f + .04f * sin(angle).toFloat())
                            val sx = size.width / 2f + cos(angle).toFloat() * radius
                            val sy = size.height / 2f + sin(angle).toFloat() * radius
                            val pulse = .45f + .55f * abs(sin(angle * 1.7f)).toFloat()
                            val arm = 2.2f * density * pulse
                            val sparkle = Color.White.copy(alpha = .50f + .45f * pulse)
                            drawLine(sparkle, Offset(sx - arm, sy), Offset(sx + arm, sy), .8f * density)
                            drawLine(sparkle, Offset(sx, sy - arm), Offset(sx, sy + arm), .8f * density)
                            drawCircle(
                                Color(0xFFFFD98C).copy(alpha = .55f * pulse),
                                radius = 1.0f * density * pulse,
                                center = Offset(sx, sy)
                            )
                        }
                    }
                }

                Image(
                    bitmap = milo,
                    contentDescription = "Milo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(62.dp)
                        .rotate(tilt)
                        .scale(
                            scaleX = (if (facingRight) 1f else -1f) * scaleX,
                            scaleY = scaleY
                        )
                        .padding(if (mood == MiloMood.SLEEP) 3.dp else 0.dp),
                    alpha = opacity
                )
            }
        }
    }
}
