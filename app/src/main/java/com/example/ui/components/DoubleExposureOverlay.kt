package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoubleExposureBlendMode
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface

@Composable
fun DoubleExposureOverlay(
    firstFrame: Bitmap?,
    blendMode: DoubleExposureBlendMode,
    overlayOpacity: Float,
    onBlendModeSelected: (DoubleExposureBlendMode) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onClearFirstFrame: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Ghosted 1st frame over the live camera preview
        if (firstFrame != null) {
            Image(
                bitmap = firstFrame.asImageBitmap(),
                contentDescription = "Double Exposure Ghost Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(overlayOpacity)
            )
        }

        // Top Status HUD
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NaturalDark.copy(alpha = 0.85f))
                .border(1.dp, NaturalOlive, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("double_exposure_status_badge")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = NaturalOlive,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (firstFrame == null) "DOUBLE EXP • STEP 1: CAPTURE BASE" else "DOUBLE EXP • STEP 2: CAPTURE OVERLAY",
                    color = NaturalBg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Step 2 Controls: Blend Mode Selector & Opacity slider
        if (firstFrame != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 54.dp, start = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(NaturalSurface.copy(alpha = 0.92f))
                    .border(1.dp, NaturalBorder, RoundedCornerShape(18.dp))
                    .padding(10.dp)
            ) {
                // Blend Modes Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLEND:",
                        color = NaturalDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    DoubleExposureBlendMode.entries.forEach { mode ->
                        val isSelected = mode == blendMode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NaturalOlive else NaturalCard)
                                .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                .clickable { onBlendModeSelected(mode) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("blend_mode_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) NaturalBg else NaturalDark,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Ghost Opacity Slider & Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("GHOST:", color = NaturalDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = overlayOpacity,
                        onValueChange = onOpacityChanged,
                        valueRange = 0.2f..0.9f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = NaturalOlive,
                            activeTrackColor = NaturalOlive,
                            inactiveTrackColor = NaturalBorderLight
                        )
                    )
                    Text(
                        text = "${(overlayOpacity * 100).toInt()}%",
                        color = NaturalDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Clear first shot button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE63946))
                            .clickable(onClick = onClearFirstFrame),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel First Exposure",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            // Option to import existing photo as Base frame
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 54.dp, end = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NaturalSurface.copy(alpha = 0.9f))
                    .border(1.dp, NaturalBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onPickFromGallery)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("double_exp_pick_gallery")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = NaturalOlive,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pick Base Frame", color = NaturalDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
