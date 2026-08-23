package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.GridType
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AdvancedGridOverlay(
    gridType: GridType,
    modifier: Modifier = Modifier
) {
    if (gridType == GridType.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (gridType) {
            GridType.RULE_OF_THIRDS -> {
                drawRuleOfThirds(w, h)
            }
            GridType.GOLDEN_RATIO -> {
                drawGoldenRatioSpiral(w, h)
            }
            GridType.TRIANGLES -> {
                drawDynamicTriangles(w, h)
            }
            GridType.CENTER_SYMMETRY -> {
                drawCenterSymmetry(w, h)
            }
            GridType.NONE -> {}
        }
    }
}

private fun DrawScope.drawRuleOfThirds(w: Float, h: Float) {
    val linePaint = NaturalBorder.copy(alpha = 0.45f)
    val nodeColor = NaturalOlive.copy(alpha = 0.7f)
    val stroke = Stroke(width = 1.2f)

    // Vertical lines
    drawLine(linePaint, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.2f)
    drawLine(linePaint, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 1.2f)

    // Horizontal lines
    drawLine(linePaint, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.2f)
    drawLine(linePaint, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 1.2f)

    // Golden intersection nodes
    val nodes = listOf(
        Offset(w / 3f, h / 3f),
        Offset(w * 2f / 3f, h / 3f),
        Offset(w / 3f, h * 2f / 3f),
        Offset(w * 2f / 3f, h * 2f / 3f)
    )
    nodes.forEach { node ->
        drawCircle(nodeColor, radius = 5f, center = node)
        drawCircle(NaturalDark.copy(alpha = 0.5f), radius = 2f, center = node)
    }
}

private fun DrawScope.drawGoldenRatioSpiral(w: Float, h: Float) {
    val spiralColor = NaturalOlive.copy(alpha = 0.75f)
    val stroke = Stroke(
        width = 1.8f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
    )

    // Draw Golden Spiral Path
    val path = Path()
    var currentRect = Rect(0f, 0f, w, h)
    path.moveTo(currentRect.left, currentRect.bottom)

    // Progressive Golden logarithmic spiral approximation
    val phi = 0.6180339887f
    val cx = w * 0.618f
    val cy = h * 0.618f

    path.reset()
    var theta = 0.0
    val maxTheta = 4.0 * PI
    val step = 0.05
    val a = 4f
    val b = 0.30635 // ln(phi)/(pi/2)

    var first = true
    while (theta <= maxTheta) {
        val r = (a * kotlin.math.exp(b * theta)).toFloat() * (w / 140f)
        val px = cx + (r * cos(theta)).toFloat()
        val py = cy + (r * sin(theta)).toFloat()
        if (first) {
            path.moveTo(px, py)
            first = false
        } else {
            path.lineTo(px, py)
        }
        theta += step
    }

    drawPath(path, spiralColor, style = stroke)

    // Golden section dividing lines
    val sectionLine = NaturalBorder.copy(alpha = 0.35f)
    drawLine(sectionLine, Offset(w * phi, 0f), Offset(w * phi, h), strokeWidth = 1.2f)
    drawLine(sectionLine, Offset(0f, h * phi), Offset(w, h * phi), strokeWidth = 1.2f)
}

private fun DrawScope.drawDynamicTriangles(w: Float, h: Float) {
    val color = NaturalOlive.copy(alpha = 0.5f)
    val stroke = Stroke(width = 1.4f)

    // Main diagonal
    drawLine(color, Offset(0f, 0f), Offset(w, h), strokeWidth = 1.5f)

    // Perpendiculars forming golden triangles
    drawLine(color, Offset(w, 0f), Offset(w * 0.382f, h * 0.382f), strokeWidth = 1.2f)
    drawLine(color, Offset(0f, h), Offset(w * 0.618f, h * 0.618f), strokeWidth = 1.2f)
}

private fun DrawScope.drawCenterSymmetry(w: Float, h: Float) {
    val color = NaturalOlive.copy(alpha = 0.6f)
    val cx = w / 2f
    val cy = h / 2f

    // Center Crosshair
    drawLine(color, Offset(cx - 30f, cy), Offset(cx + 30f, cy), strokeWidth = 1.5f)
    drawLine(color, Offset(cx, cy - 30f), Offset(cx, cy + 30f), strokeWidth = 1.5f)

    // Concentric subtle target rings
    drawCircle(
        color = NaturalBorder.copy(alpha = 0.4f),
        radius = 48f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
    )
    drawCircle(
        color = NaturalOlive.copy(alpha = 0.25f),
        radius = 120f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f)
    )
}
