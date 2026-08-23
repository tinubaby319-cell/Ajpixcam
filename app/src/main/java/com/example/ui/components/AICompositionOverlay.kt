package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompositionGuidance
import com.example.data.model.HorizonLevel
import com.example.data.model.SubjectDetection
import com.example.ui.theme.LevelGreen
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import kotlin.math.abs

@Composable
fun AICompositionOverlay(
    composition: CompositionGuidance,
    subject: SubjectDetection,
    horizon: HorizonLevel,
    showGrid: Boolean = true,
    showHorizon: Boolean = true,
    showAiGuidance: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aiPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_composition_overlay")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Rule-of-Thirds Grid in soft natural tone
            if (showGrid) {
                val gridColor = Color(0x3D1C1C16)
                val gridStroke = 1.2f

                // Vertical lines
                drawLine(gridColor, Offset(width * 0.333f, 0f), Offset(width * 0.333f, height), gridStroke)
                drawLine(gridColor, Offset(width * 0.666f, 0f), Offset(width * 0.666f, height), gridStroke)

                // Horizontal lines
                drawLine(gridColor, Offset(0f, height * 0.333f), Offset(width, height * 0.333f), gridStroke)
                drawLine(gridColor, Offset(0f, height * 0.666f), Offset(width, height * 0.666f), gridStroke)
            }

            // 2. AI Guidance Target Node & Vector
            if (showAiGuidance) {
                val targetPx = Offset(composition.targetNode.x * width, composition.targetNode.y * height)
                val currentPx = Offset(composition.currentPosition.x * width, composition.currentPosition.y * height)

                // Draw connecting dotted guidance line if not aligned
                if (!composition.isAligned) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    drawLine(
                        color = NaturalOlive.copy(alpha = 0.8f),
                        start = currentPx,
                        end = targetPx,
                        strokeWidth = 2.5f,
                        pathEffect = pathEffect
                    )

                    // Draw subject pointer dot
                    drawCircle(
                        color = NaturalDark.copy(alpha = 0.85f),
                        radius = 5f,
                        center = currentPx
                    )
                }

                // Optimal Composition Target Ring
                val targetColor = if (composition.isAligned) LevelGreen else NaturalOlive
                val ringRadius = 24f * pulseScale

                // Outer halo
                drawCircle(
                    color = targetColor.copy(alpha = 0.22f),
                    radius = ringRadius + 8f,
                    center = targetPx
                )

                // Glowing target ring
                drawCircle(
                    color = targetColor,
                    radius = ringRadius,
                    center = targetPx,
                    style = Stroke(width = 2.5f)
                )

                // Center crosshair dot
                drawCircle(
                    color = targetColor,
                    radius = 4f,
                    center = targetPx
                )

                // Subject Bounding Box
                if (subject.isPerson && !composition.isAligned) {
                    val boxLeft = subject.boundingBox.left * width
                    val boxTop = subject.boundingBox.top * height
                    val boxW = subject.boundingBox.width * width
                    val boxH = subject.boundingBox.height * height

                    drawRoundRect(
                        color = NaturalOlive.copy(alpha = 0.35f),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(boxW, boxH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                    )
                }
            }

            // 3. Horizon Level Indicator Line (Center)
            if (showHorizon && abs(horizon.angleDegrees) > 0.3f) {
                val cx = width / 2f
                val cy = height / 2f
                val barLength = 140f
                val rad = Math.toRadians(-horizon.angleDegrees.toDouble())
                val dx = (Math.cos(rad) * barLength / 2f).toFloat()
                val dy = (Math.sin(rad) * barLength / 2f).toFloat()

                val levelColor = if (horizon.isLevel) LevelGreen else NaturalDark.copy(alpha = 0.7f)

                drawLine(
                    color = levelColor,
                    start = Offset(cx - dx, cy - dy),
                    end = Offset(cx + dx, cy + dy),
                    strokeWidth = if (horizon.isLevel) 3.5f else 2.0f
                )
                // Center pip
                drawCircle(
                    color = levelColor,
                    radius = 3.5f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Top Composition Score & Advice Pill
        if (showAiGuidance) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NaturalSurface)
                    .border(
                        1.dp,
                        if (composition.score >= 88) LevelGreen else NaturalBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Composition",
                        tint = if (composition.score >= 88) LevelGreen else NaturalOlive,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${composition.score}",
                        color = if (composition.score >= 88) LevelGreen else NaturalOlive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • ",
                        color = NaturalTextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = composition.advice,
                        color = NaturalDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
