package de.mietmoments.verwaltung.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val momoReactions = listOf(
    "Ich habe nichts angefasst. Also fast nichts.",
    "Kabelbinder sagen mehr als tausend Worte.",
    "Wenn alles dringend ist, brauche ich erst mal einen Kaffee.",
    "Ich passe auf die Termine auf. Du auf den Rest. Deal?",
    "Synchronisiert ist auch nur ein schickes Wort für: Jetzt weiß ich's auch."
)

@Composable
fun MomoMascot(
    message: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    compact: Boolean = false
) {
    var reaction by remember { mutableIntStateOf(-1) }
    val shown = if (reaction >= 0) momoReactions[reaction % momoReactions.size] else message
    val infinite = rememberInfiniteTransition(label = "momo")
    val bob by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) -6f else 0f,
        animationSpec = infiniteRepeatable(tween(1250), RepeatMode.Reverse),
        label = "momo-bob"
    )
    val tilt by infinite.animateFloat(
        initialValue = if (animated) -2f else 0f,
        targetValue = if (animated) 2f else 0f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "momo-tilt"
    )

    Row(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .72f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
            .clickable { reaction = (reaction + 1) % momoReactions.size }
            .padding(if (compact) 12.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MomoFace(
            modifier = Modifier
                .size(if (compact) 58.dp else 82.dp)
                .offset(y = bob.dp)
                .rotate(tilt)
        )
        Column(Modifier.weight(1f)) {
            Text("Momo meint", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(3.dp))
            AnimatedContent(
                targetState = shown,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "momo-text"
            ) { text ->
                Text(text, style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium)
            }
            Text("Antippen für einen Kommentar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MomoFace(modifier: Modifier = Modifier) {
    val green = MaterialTheme.colorScheme.primary
    val gold = MaterialTheme.colorScheme.secondary
    val cream = MaterialTheme.colorScheme.surface
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(green, cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .30f, w * .30f))
        drawCircle(gold, radius = w * .078f, center = Offset(w * .33f, h * .40f))
        drawCircle(gold, radius = w * .078f, center = Offset(w * .67f, h * .40f))
        drawArc(
            color = cream,
            startAngle = 18f,
            sweepAngle = 144f,
            useCenter = false,
            topLeft = Offset(w * .30f, h * .42f),
            size = Size(w * .40f, h * .31f),
            style = Stroke(width = w * .055f)
        )
        drawCircle(gold, radius = w * .055f, center = Offset(w * .50f, h * .17f))
        drawCircle(cream.copy(alpha = .55f), radius = w * .028f, center = Offset(w * .23f, h * .26f))
        drawCircle(cream.copy(alpha = .55f), radius = w * .020f, center = Offset(w * .78f, h * .25f))
    }
}
