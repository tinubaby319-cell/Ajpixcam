package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CameraLens
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted

@Composable
fun LensSelector(
    selectedLens: CameraLens,
    onLensSelected: (CameraLens) -> Unit,
    modifier: Modifier = Modifier
) {
    val lenses = listOf(CameraLens.ULTRA_WIDE, CameraLens.WIDE, CameraLens.TELE_2X, CameraLens.TELE_3X)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorderLight, RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            lenses.forEach { lens ->
                val isSelected = selectedLens == lens
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) NaturalCard else Color.Transparent,
                    label = "lensBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) NaturalDark else NaturalTextMuted,
                    label = "lensText"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .then(
                            if (isSelected) Modifier.border(1.dp, NaturalBorder, CircleShape) else Modifier
                        )
                        .testTag("lens_button_${lens.label}")
                        .clickable { onLensSelected(lens) }
                ) {
                    Text(
                        text = lens.label,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
