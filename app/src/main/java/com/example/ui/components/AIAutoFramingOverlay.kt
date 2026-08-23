package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoFramingRecommendation
import com.example.data.model.CameraLens
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMoss
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted

@Composable
fun AIAutoFramingOverlay(
    autoFraming: AutoFramingRecommendation,
    onApplyLens: (CameraLens) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_auto_framing_overlay")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val cropRect = autoFraming.cropRect
            val left = cropRect.left * width
            val top = cropRect.top * height
            val right = cropRect.right * width
            val bottom = cropRect.bottom * height
            val rectWidth = right - left
            val rectHeight = bottom - top

            // Darken outside excluded area with soft warm shading
            val path = Path().apply {
                // Outer full bounds
                addRect(androidx.compose.ui.geometry.Rect(0f, 0f, width, height))
                // Cutout inner window
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom,
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                )
                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
            }

            drawPath(path = path, color = NaturalDark.copy(alpha = 0.35f))

            // Soft organic gradient border around framing window
            val gradientBrush = Brush.linearGradient(
                colors = listOf(NaturalOlive, NaturalMoss, NaturalOlive),
                start = Offset(left, top),
                end = Offset(right, bottom)
            )

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 2.5f)
            )

            // Corner accent brackets
            val bracketLen = 28f
            val bracketColor = NaturalDark
            val bracketStroke = 3.5f

            // Top-left
            drawLine(bracketColor, Offset(left, top + bracketLen), Offset(left, top + 8f), bracketStroke)
            drawLine(bracketColor, Offset(left + 8f, top), Offset(left + bracketLen, top), bracketStroke)

            // Top-right
            drawLine(bracketColor, Offset(right, top + bracketLen), Offset(right, top + 8f), bracketStroke)
            drawLine(bracketColor, Offset(right - 8f, top), Offset(right - bracketLen, top), bracketStroke)

            // Bottom-left
            drawLine(bracketColor, Offset(left, bottom - bracketLen), Offset(left, bottom - 8f), bracketStroke)
            drawLine(bracketColor, Offset(left + 8f, bottom), Offset(left + bracketLen, bottom), bracketStroke)

            // Bottom-right
            drawLine(bracketColor, Offset(right, bottom - bracketLen), Offset(right, bottom - 8f), bracketStroke)
            drawLine(bracketColor, Offset(right - 8f, bottom), Offset(right - bracketLen, bottom), bracketStroke)
        }

        // Floating Auto-Framing Recommendation Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NaturalSurface)
                .border(1.dp, NaturalBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = NaturalOlive,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = autoFraming.framingAdvice,
                            color = NaturalDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = autoFraming.reason,
                        color = NaturalTextMuted,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NaturalCard)
                        .border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                        .clickable { onApplyLens(autoFraming.recommendedLens) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("apply_lens_recommendation")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = NaturalDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = autoFraming.recommendedLensLabel,
                            color = NaturalDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
