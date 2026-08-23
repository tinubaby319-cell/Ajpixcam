package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface

@Composable
fun CompareSlider(
    modifier: Modifier = Modifier,
    initialSplit: Float = 0.5f,
    filterName: String = "Film Filter"
) {
    var splitFraction by remember { mutableFloatStateOf(initialSplit) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newFraction = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    splitFraction = newFraction
                }
            }
            .testTag("compare_slider_view")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val splitX = width * splitFraction

            // Vertical divider line
            drawLine(
                color = NaturalOlive,
                start = Offset(splitX, 0f),
                end = Offset(splitX, height),
                strokeWidth = 3f
            )

            // Center handle knob
            val cy = height / 2f
            drawCircle(
                color = NaturalOlive,
                radius = 16f,
                center = Offset(splitX, cy)
            )
            drawCircle(
                color = NaturalBg,
                radius = 7f,
                center = Offset(splitX, cy)
            )
        }

        // Left Label: ORIGINAL
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NaturalSurface)
                .border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "ORIGINAL",
                color = NaturalDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right Label: FILTERED
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NaturalCard)
                .border(1.dp, NaturalOlive, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = filterName.uppercase(),
                color = NaturalOlive,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
