package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
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
import com.example.data.model.FilmFilter
import com.example.data.model.FilmFilterId
import com.example.filter.FilmPresets
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary

@Composable
fun FilterCarousel(
    selectedFilter: FilmFilter,
    filterIntensity: Int,
    onFilterSelected: (FilmFilter) -> Unit,
    onIntensityChanged: (Int) -> Unit,
    onCompareToggle: () -> Unit = {},
    isComparing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val filters = FilmPresets.ALL_PRESETS
    var showIntensitySlider by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Intensity slider bar if expanded
        AnimatedVisibility(
            visible = showIntensitySlider && selectedFilter.id != FilmFilterId.ORIGINAL,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NaturalSurface)
                    .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${selectedFilter.code} Intensity",
                    color = NaturalTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = filterIntensity.toFloat(),
                    onValueChange = { onIntensityChanged(it.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f).testTag("filter_intensity_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NaturalOlive,
                        activeTrackColor = NaturalOlive,
                        inactiveTrackColor = NaturalBorderLight
                    )
                )
                Text(
                    text = "$filterIntensity%",
                    color = NaturalDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        // Horizontal Film Strips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Intensity Toggle
            if (selectedFilter.id != FilmFilterId.ORIGINAL) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (showIntensitySlider) NaturalCard else NaturalSurface)
                        .border(1.dp, if (showIntensitySlider) NaturalOlive else NaturalBorderLight, CircleShape)
                        .clickable { showIntensitySlider = !showIntensitySlider }
                        .testTag("toggle_intensity_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Intensity",
                        tint = if (showIntensitySlider) NaturalOlive else NaturalDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            filters.forEach { filter ->
                val isSelected = selectedFilter.id == filter.id
                FilterCard(
                    filter = filter,
                    isSelected = isSelected,
                    onClick = {
                        onFilterSelected(filter)
                        if (isSelected) {
                            showIntensitySlider = !showIntensitySlider
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterCard(
    filter: FilmFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) NaturalCard else NaturalSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NaturalOlive else NaturalBorderLight,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("filter_card_${filter.code.replace(" ", "_")}")
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Film Frame Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(filter.badgeColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = filter.code,
                color = if (isSelected) NaturalOlive else NaturalDark,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = filter.name,
            color = if (isSelected) NaturalDark else NaturalTextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
