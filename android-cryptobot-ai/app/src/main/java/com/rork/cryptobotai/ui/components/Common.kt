package com.rork.cryptobotai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.Stroke
import com.rork.cryptobotai.ui.theme.SurfaceCard

/** A premium elevated card with subtle border used across the app. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    borderColor: Color = Stroke,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(contentPadding)
    ) {
        content()
    }
}

/** A small pulsing dot used to indicate the bot is live. */
@Composable
fun PulsingDot(color: Color = NeonGreen, size: Dp = 10.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                drawCircle(color = color.copy(alpha = alpha * 0.3f), radius = this.size.minDimension)
            }
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

/** Vertical labeled value used in stat rows. */
@Composable
fun LabeledValue(
    label: @Composable () -> Unit,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        value()
        label()
    }
}

fun greenGlowBrush(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF00E676), Color(0xFF00B894)),
    start = Offset(0f, 0f),
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

@Composable
fun RowSpacer(width: Dp) = Box(Modifier.size(width, 1.dp))
