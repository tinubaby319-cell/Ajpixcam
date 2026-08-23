package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive

@Composable
fun ShutterButton(
    modifier: Modifier = Modifier,
    isCapturing: Boolean = false,
    isVideoMode: Boolean = false,
    isRecordingVideo: Boolean = false,
    isAiMode: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "shutterScale"
    )

    val innerCornerRadius by animateDpAsState(
        targetValue = if (isRecordingVideo) 8.dp else 30.dp,
        label = "shutterCorner"
    )

    val innerSize by animateDpAsState(
        targetValue = if (isRecordingVideo) 32.dp else 54.dp,
        label = "shutterInnerSize"
    )

    val innerColor by animateColorAsState(
        targetValue = when {
            isVideoMode || isRecordingVideo -> Color(0xFFE63946)
            isAiMode -> NaturalOlive
            else -> NaturalDark
        },
        label = "shutterColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(86.dp)
            .scale(scale)
            .testTag("shutter_button")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isCapturing,
                onClick = onClick
            )
    ) {
        // Outer thin ring
        Canvas(modifier = Modifier.size(86.dp)) {
            val ringColor = when {
                isRecordingVideo -> Color(0xFFE63946)
                isAiMode -> NaturalOlive
                else -> NaturalBorder
            }
            drawCircle(
                color = ringColor,
                radius = size.minDimension / 2f - 2f,
                style = Stroke(width = 3.5f)
            )
        }

        // Inner solid shutter disc
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(if (isAiMode) NaturalCard else NaturalBg),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(innerSize)
                    .clip(RoundedCornerShape(innerCornerRadius))
                    .background(innerColor)
            )
        }

        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                color = NaturalOlive,
                strokeWidth = 3.dp
            )
        }
    }
}
