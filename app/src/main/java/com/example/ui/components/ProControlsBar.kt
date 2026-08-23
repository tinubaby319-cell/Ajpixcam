package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProCameraSettings
import com.example.data.model.WhiteBalancePreset
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary

enum class ProControlTab {
    EV, ISO, SHUTTER, WB
}

@Composable
fun ProControlsBar(
    proSettings: ProCameraSettings,
    onSettingsChanged: (ProCameraSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ProControlTab.EV) }

    val isoOptions = listOf(0, 100, 200, 400, 800, 1600, 3200)
    val shutterOptions = listOf("Auto", "1/1000", "1/500", "1/250", "1/125", "1/60", "1/30")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorderLight, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(vertical = 10.dp, horizontal = 14.dp)
            .testTag("pro_controls_bar")
    ) {
        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProTabButton("EV", activeTab == ProControlTab.EV, "${String.format("%+.1f", proSettings.evCompensation)}") {
                activeTab = ProControlTab.EV
            }
            ProTabButton("ISO", activeTab == ProControlTab.ISO, if (proSettings.isoValue == 0) "Auto" else "${proSettings.isoValue}") {
                activeTab = ProControlTab.ISO
            }
            ProTabButton("SEC", activeTab == ProControlTab.SHUTTER, shutterOptions[proSettings.shutterSpeedIndex]) {
                activeTab = ProControlTab.SHUTTER
            }
            ProTabButton("WB", activeTab == ProControlTab.WB, proSettings.whiteBalance.label) {
                activeTab = ProControlTab.WB
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active control adjustment
        when (activeTab) {
            ProControlTab.EV -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("-3.0", color = NaturalTextTertiary, fontSize = 11.sp)
                    Slider(
                        value = proSettings.evCompensation,
                        onValueChange = { onSettingsChanged(proSettings.copy(evCompensation = Math.round(it * 10) / 10f)) },
                        valueRange = -3.0f..3.0f,
                        steps = 19,
                        modifier = Modifier.weight(1f).testTag("pro_ev_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = NaturalOlive,
                            activeTrackColor = NaturalOlive,
                            inactiveTrackColor = NaturalBorderLight
                        )
                    )
                    Text("+3.0", color = NaturalTextTertiary, fontSize = 11.sp)
                }
            }

            ProControlTab.ISO -> {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    isoOptions.forEach { iso ->
                        val isSelected = proSettings.isoValue == iso
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NaturalCard else NaturalBg)
                                .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(12.dp))
                                .clickable { onSettingsChanged(proSettings.copy(isoValue = iso)) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("pro_iso_$iso")
                        ) {
                            Text(
                                text = if (iso == 0) "Auto" else "$iso",
                                color = if (isSelected) NaturalDark else NaturalTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            ProControlTab.SHUTTER -> {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shutterOptions.forEachIndexed { index, speed ->
                        val isSelected = proSettings.shutterSpeedIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NaturalCard else NaturalBg)
                                .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(12.dp))
                                .clickable { onSettingsChanged(proSettings.copy(shutterSpeedIndex = index)) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("pro_shutter_$index")
                        ) {
                            Text(
                                text = speed,
                                color = if (isSelected) NaturalDark else NaturalTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            ProControlTab.WB -> {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WhiteBalancePreset.entries.forEach { wb ->
                        val isSelected = proSettings.whiteBalance == wb
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NaturalCard else NaturalBg)
                                .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(12.dp))
                                .clickable { onSettingsChanged(proSettings.copy(whiteBalance = wb)) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("pro_wb_${wb.name.lowercase()}")
                        ) {
                            Text(
                                text = wb.label,
                                color = if (isSelected) NaturalDark else NaturalTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProTabButton(
    title: String,
    isSelected: Boolean,
    value: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NaturalCard else Color.Transparent)
            .then(if (isSelected) Modifier.border(1.dp, NaturalBorder, RoundedCornerShape(12.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) NaturalOlive else NaturalTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = if (isSelected) NaturalDark else NaturalTextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
