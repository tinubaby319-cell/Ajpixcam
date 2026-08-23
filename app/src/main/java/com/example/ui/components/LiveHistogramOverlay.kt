package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistogramData
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalTextMuted

@Composable
fun LiveHistogramOverlay(
    histogramData: HistogramData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NaturalCard.copy(alpha = 0.88f))
            .border(1.dp, NaturalBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("live_histogram_hud")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.width(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RGB HISTO",
                    color = NaturalDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                if (histogramData.hasClippingHighlights) {
                    Text("CLIP ▲", color = Color(0xFFE63946), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                } else if (histogramData.hasCrushedShadows) {
                    Text("CRUSH ▼", color = Color(0xFF457B9D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("BALANCED", color = NaturalOlive, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Canvas(modifier = Modifier.size(width = 110.dp, height = 36.dp)) {
                val w = size.width
                val h = size.height

                // Draw background grid lines
                drawLine(NaturalBorder.copy(alpha = 0.4f), Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth = 0.8f)
                drawLine(NaturalBorder.copy(alpha = 0.4f), Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), strokeWidth = 0.8f)

                // Draw Red, Green, Blue, Luma Curves
                drawHistoChannel(histogramData.r, Color(0xFFE63946).copy(alpha = 0.6f), w, h)
                drawHistoChannel(histogramData.g, Color(0xFF2A9D8F).copy(alpha = 0.6f), w, h)
                drawHistoChannel(histogramData.b, Color(0xFF457B9D).copy(alpha = 0.6f), w, h)
                drawHistoChannel(histogramData.lum, NaturalOlive.copy(alpha = 0.9f), w, h, isMaster = true)
            }
        }
    }
}

private fun DrawScope.drawHistoChannel(
    bins: FloatArray,
    color: Color,
    w: Float,
    h: Float,
    isMaster: Boolean = false
) {
    if (bins.isEmpty()) return
    val path = Path()
    val count = bins.size
    val step = w / (count - 1).toFloat()

    path.moveTo(0f, h - (bins[0].coerceIn(0f, 1f) * h * 0.95f))

    for (i in 1 until count) {
        val x = i * step
        val y = h - (bins[i].coerceIn(0f, 1f) * h * 0.95f)
        path.lineTo(x, y)
    }

    if (isMaster) {
        drawPath(path, color, style = Stroke(width = 1.6f))
    } else {
        drawPath(path, color, style = Stroke(width = 1.0f))
    }
}
