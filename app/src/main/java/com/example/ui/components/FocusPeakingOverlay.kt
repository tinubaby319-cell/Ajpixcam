package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun FocusPeakingOverlay(
    showFocusPeaking: Boolean,
    showZebraStripes: Boolean,
    modifier: Modifier = Modifier
) {
    if (!showFocusPeaking && !showZebraStripes) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (showZebraStripes) {
            // Draw diagonal zebra pattern over highlight warning zone (upper center sky/light area)
            drawZebraStripes(w, h)
        }

        if (showFocusPeaking) {
            // Draw focus peaking edge highlights around central focus zone
            drawFocusPeakingEdges(w, h)
        }
    }
}

private fun DrawScope.drawZebraStripes(w: Float, h: Float) {
    val zebraColor = Color(0xFFE63946).copy(alpha = 0.55f)
    val step = 16f
    // Diagonal lines across top highlight region
    var x = 0f
    while (x < w) {
        drawLine(
            color = zebraColor,
            start = Offset(x, 0f),
            end = Offset(x + 30f, h * 0.25f),
            strokeWidth = 2.0f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
        x += step
    }
}

private fun DrawScope.drawFocusPeakingEdges(w: Float, h: Float) {
    val peakColor = Color(0xFF06D6A0).copy(alpha = 0.85f)
    val cx = w / 2f
    val cy = h / 2f

    // Organic focus edge outlines in central focal depth of field
    val points = listOf(
        Offset(cx - 60f, cy - 40f) to Offset(cx + 60f, cy - 40f),
        Offset(cx - 80f, cy) to Offset(cx + 80f, cy),
        Offset(cx - 50f, cy + 50f) to Offset(cx + 50f, cy + 50f),
        Offset(cx - 30f, cy - 80f) to Offset(cx - 10f, cy - 70f),
        Offset(cx + 10f, cy - 70f) to Offset(cx + 30f, cy - 80f)
    )

    points.forEach { (start, end) ->
        drawLine(
            color = peakColor,
            start = start,
            end = end,
            strokeWidth = 2.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )
    }
}
