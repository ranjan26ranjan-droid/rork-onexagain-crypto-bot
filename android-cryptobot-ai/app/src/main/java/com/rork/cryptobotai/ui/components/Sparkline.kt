package com.rork.cryptobotai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.rork.cryptobotai.ui.theme.BearRed
import com.rork.cryptobotai.ui.theme.BullGreen

/**
 * Lightweight line chart that renders a price series with a soft area fill.
 * Colour reflects whether the series ended up or down.
 */
@Composable
fun Sparkline(
    points: List<Double>,
    modifier: Modifier = Modifier,
    color: Color? = null,
    showFill: Boolean = true,
    strokeWidth: Float = 4f
) {
    if (points.size < 2) return
    val up = points.last() >= points.first()
    val lineColor = color ?: if (up) BullGreen else BearRed

    Canvas(modifier = modifier) {
        val min = points.min()
        val max = points.max()
        val range = (max - min).takeIf { it != 0.0 } ?: 1.0
        val stepX = size.width / (points.size - 1)

        fun yFor(value: Double): Float =
            (size.height - ((value - min) / range * size.height)).toFloat()

        val linePath = Path()
        points.forEachIndexed { index, value ->
            val x = stepX * index
            val y = yFor(value)
            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }

        if (showFill) {
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.28f), Color.Transparent)
                )
            )
        }

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Glowing end dot
        val lastX = size.width
        val lastY = yFor(points.last())
        drawCircle(color = lineColor.copy(alpha = 0.25f), radius = strokeWidth * 2.5f, center = Offset(lastX, lastY))
        drawCircle(color = lineColor, radius = strokeWidth, center = Offset(lastX, lastY))
    }
}
