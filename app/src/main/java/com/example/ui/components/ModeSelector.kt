package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CameraMode
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalTextTertiary

@Composable
fun ModeSelector(
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = CameraMode.entries
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            val isSelected = selectedMode == mode
            val isAiSpecial = mode == CameraMode.AI_COMPOSE || mode == CameraMode.AUTO_FRAME

            val textColor by animateColorAsState(
                targetValue = when {
                    isSelected && isAiSpecial -> NaturalOlive
                    isSelected -> NaturalDark
                    else -> NaturalTextTertiary
                },
                label = "modeTextColor"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .testTag("mode_${mode.name.lowercase()}")
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Text(
                    text = mode.label,
                    color = textColor,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(width = 4.dp, height = 4.dp),
                        shape = CircleShape,
                        color = NaturalOlive
                    ) {}
                }
            }
        }
    }
}
