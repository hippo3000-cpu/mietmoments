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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MomoMascot(
    message: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    compact: Boolean = false
) {
    val infinite = rememberInfiniteTransition(label = "momo")
    val bob by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) -5f else 0f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "momo-bob"
    )
    val tilt by infinite.animateFloat(
        initialValue = if (animated) -1.5f else 0f,
        targetValue = if (animated) 1.5f else 0f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "momo-tilt"
    )

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp))
            .padding(if (compact) 12.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MomoFace(
            modifier = Modifier
                .size(if (compact) 56.dp else 76.dp)
                .offset(y = bob.dp)
                .rotate(tilt)
        )
        Column(Modifier.weight(1f)) {
            Text("Momo meint", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            AnimatedContent(
                targetState = message,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "momo-text"
            ) { text ->
                Text(text, style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium)
            }
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
        drawRoundRect(green, cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .26f, w * .26f))
        drawCircle(gold, radius = w * .075f, center = Offset(w * .33f, h * .40f))
        drawCircle(gold, radius = w * .075f, center = Offset(w * .67f, h * .40f))
        drawArc(
            color = cream,
            startAngle = 18f,
            sweepAngle = 144f,
            useCenter = false,
            topLeft = Offset(w * .31f, h * .42f),
            size = Size(w * .38f, h * .30f),
            style = Stroke(width = w * .055f)
        )
        drawCircle(gold, radius = w * .055f, center = Offset(w * .50f, h * .18f))
    }
}
