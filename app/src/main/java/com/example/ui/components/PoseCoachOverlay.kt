package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PoseCategory
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary

@Composable
fun PoseCoachOverlay(
    poseCategory: PoseCategory,
    onCategorySelected: (PoseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Silhouette Blueprint Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawPoseSilhouette(w, h, poseCategory)
        }

        // Top guidance advice banner
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NaturalCard.copy(alpha = 0.92f))
                .border(1.dp, NaturalBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NaturalOlive)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (poseCategory) {
                        PoseCategory.PORTRAIT -> "Align eyes with upper third • Tilt chin 10°"
                        PoseCategory.STANDING -> "Shift weight to back leg • Relax shoulders"
                        PoseCategory.SITTING -> "Lean forward slightly • Rest arm naturally"
                        PoseCategory.COUPLE -> "Natural head tilt together • Gentle hand hold"
                        PoseCategory.FASHION -> "Stride diagonally across frame • Loose arms"
                    },
                    color = NaturalDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bottom pose category selector bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            PoseCategory.entries.forEach { cat ->
                val isSelected = cat == poseCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) NaturalOlive else NaturalCard.copy(alpha = 0.9f))
                        .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(18.dp))
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("pose_${cat.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (cat) {
                                PoseCategory.PORTRAIT -> Icons.Default.Person
                                PoseCategory.STANDING -> Icons.Default.Accessibility
                                PoseCategory.SITTING -> Icons.Default.Chair
                                PoseCategory.COUPLE -> Icons.Default.Group
                                PoseCategory.FASHION -> Icons.Default.DirectionsWalk
                            },
                            contentDescription = cat.label,
                            tint = if (isSelected) NaturalBg else NaturalTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.label,
                            color = if (isSelected) NaturalBg else NaturalDark,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPoseSilhouette(w: Float, h: Float, category: PoseCategory) {
    val outlineColor = NaturalOlive.copy(alpha = 0.75f)
    val guideDashes = Stroke(
        width = 2.0f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )

    when (category) {
        PoseCategory.PORTRAIT -> {
            // Head and shoulders close-up
            val headCenter = Offset(w * 0.5f, h * 0.36f)
            val headRadius = w * 0.18f

            // Head oval
            drawCircle(outlineColor, radius = headRadius, center = headCenter, style = guideDashes)

            // Eye line guide
            drawLine(
                outlineColor.copy(alpha = 0.4f),
                Offset(headCenter.x - headRadius, headCenter.y - headRadius * 0.1f),
                Offset(headCenter.x + headRadius, headCenter.y - headRadius * 0.1f),
                strokeWidth = 1.2f
            )

            // Neck & Shoulders curve
            val shoulderPath = Path().apply {
                moveTo(w * 0.12f, h * 0.88f)
                cubicTo(w * 0.28f, h * 0.62f, w * 0.38f, h * 0.52f, w * 0.43f, h * 0.52f)
                lineTo(w * 0.57f, h * 0.52f)
                cubicTo(w * 0.62f, h * 0.52f, w * 0.72f, h * 0.62f, w * 0.88f, h * 0.88f)
            }
            drawPath(shoulderPath, outlineColor, style = guideDashes)
        }

        PoseCategory.STANDING -> {
            // 3/4 Standing silhouette
            val headCenter = Offset(w * 0.50f, h * 0.22f)
            drawCircle(outlineColor, radius = w * 0.08f, center = headCenter, style = guideDashes)

            val torsoPath = Path().apply {
                moveTo(w * 0.40f, h * 0.30f)
                lineTo(w * 0.60f, h * 0.30f)
                lineTo(w * 0.56f, h * 0.58f)
                lineTo(w * 0.44f, h * 0.58f)
                close()
            }
            drawPath(torsoPath, outlineColor, style = guideDashes)

            // Legs
            drawLine(outlineColor, Offset(w * 0.46f, h * 0.58f), Offset(w * 0.44f, h * 0.90f), strokeWidth = 2f)
            drawLine(outlineColor, Offset(w * 0.54f, h * 0.58f), Offset(w * 0.57f, h * 0.90f), strokeWidth = 2f)
        }

        PoseCategory.SITTING -> {
            // Seated posture
            val headCenter = Offset(w * 0.50f, h * 0.28f)
            drawCircle(outlineColor, radius = w * 0.09f, center = headCenter, style = guideDashes)

            val sitPath = Path().apply {
                moveTo(w * 0.38f, h * 0.37f)
                lineTo(w * 0.62f, h * 0.37f)
                lineTo(w * 0.58f, h * 0.62f)
                lineTo(w * 0.70f, h * 0.68f)
                lineTo(w * 0.68f, h * 0.88f)
                moveTo(w * 0.42f, h * 0.62f)
                lineTo(w * 0.36f, h * 0.88f)
            }
            drawPath(sitPath, outlineColor, style = guideDashes)
        }

        PoseCategory.COUPLE -> {
            // Person 1 (Left)
            drawCircle(outlineColor, radius = w * 0.09f, center = Offset(w * 0.38f, h * 0.32f), style = guideDashes)
            // Person 2 (Right, slight head tilt towards center)
            drawCircle(outlineColor, radius = w * 0.09f, center = Offset(w * 0.60f, h * 0.36f), style = guideDashes)

            // Shared torso embrace outline
            val couplePath = Path().apply {
                moveTo(w * 0.22f, h * 0.78f)
                cubicTo(w * 0.28f, h * 0.45f, w * 0.48f, h * 0.44f, w * 0.50f, h * 0.48f)
                cubicTo(w * 0.52f, h * 0.44f, w * 0.72f, h * 0.45f, w * 0.78f, h * 0.78f)
            }
            drawPath(couplePath, outlineColor, style = guideDashes)
        }

        PoseCategory.FASHION -> {
            // Stride posture
            val headCenter = Offset(w * 0.52f, h * 0.20f)
            drawCircle(outlineColor, radius = w * 0.08f, center = headCenter, style = guideDashes)

            val walkPath = Path().apply {
                moveTo(w * 0.44f, h * 0.28f)
                lineTo(w * 0.60f, h * 0.28f)
                lineTo(w * 0.54f, h * 0.52f)
                // Front leg
                lineTo(w * 0.68f, h * 0.86f)
                moveTo(w * 0.54f, h * 0.52f)
                // Back leg
                lineTo(w * 0.36f, h * 0.86f)
            }
            drawPath(walkPath, outlineColor, style = guideDashes)
        }
    }
}
