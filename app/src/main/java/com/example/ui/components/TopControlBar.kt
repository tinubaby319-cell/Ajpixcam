package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioMode
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FlashMode
import com.example.data.model.GridType
import com.example.data.model.ShutterSoundProfile
import com.example.data.model.TimerMode
import com.example.data.model.VintageLensStyle
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary

@Composable
fun TopControlBar(
    flashMode: FlashMode,
    onFlashChanged: (FlashMode) -> Unit,
    timerMode: TimerMode,
    onTimerChanged: (TimerMode) -> Unit,
    aspectRatio: AspectRatioMode,
    onAspectRatioChanged: (AspectRatioMode) -> Unit,
    isAiEnabled: Boolean,
    onAiToggled: () -> Unit,
    gridType: GridType,
    onGridCycle: () -> Unit,
    dateStampFormat: FilmDateStampFormat,
    onDateStampCycle: () -> Unit,
    showHistogram: Boolean,
    onHistogramToggle: () -> Unit,
    showPoseCoach: Boolean,
    onPoseCoachToggle: () -> Unit,
    shutterSoundProfile: ShutterSoundProfile,
    onSoundCycle: () -> Unit,
    vintageLensStyle: VintageLensStyle,
    onLensStyleCycle: () -> Unit,
    onOpenRecipeStudio: () -> Unit,
    onOpenFilmLab: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash Button
        TopBarIconButton(
            onClick = {
                val next = when (flashMode) {
                    FlashMode.OFF -> FlashMode.AUTO
                    FlashMode.AUTO -> FlashMode.ON
                    FlashMode.ON -> FlashMode.TORCH
                    FlashMode.TORCH -> FlashMode.OFF
                }
                onFlashChanged(next)
            },
            testTag = "flash_toggle",
            isActive = flashMode != FlashMode.OFF
        ) {
            Icon(
                imageVector = when (flashMode) {
                    FlashMode.OFF -> Icons.Default.FlashOff
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.TORCH -> Icons.Default.Highlight
                },
                contentDescription = "Flash ${flashMode.label}",
                tint = if (flashMode != FlashMode.OFF) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // Aspect Ratio
        TopBarIconButton(
            onClick = {
                val next = when (aspectRatio) {
                    AspectRatioMode.RATIO_4_3 -> AspectRatioMode.RATIO_16_9
                    AspectRatioMode.RATIO_16_9 -> AspectRatioMode.RATIO_1_1
                    AspectRatioMode.RATIO_1_1 -> AspectRatioMode.RATIO_4_3
                    else -> AspectRatioMode.RATIO_4_3
                }
                onAspectRatioChanged(next)
            },
            testTag = "aspect_ratio_toggle"
        ) {
            Text(
                text = aspectRatio.label,
                color = NaturalDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Custom Film Recipe Studio Shortcut
        TopBarIconButton(
            onClick = onOpenRecipeStudio,
            testTag = "recipe_studio_shortcut",
            isActive = true
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Film Recipe Studio",
                tint = NaturalOlive,
                modifier = Modifier.size(17.dp)
            )
        }

        // Film Lab / Darkroom Development Shortcut
        TopBarIconButton(
            onClick = onOpenFilmLab,
            testTag = "film_lab_shortcut",
            isActive = true
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "Darkroom Film Lab",
                tint = NaturalOlive,
                modifier = Modifier.size(17.dp)
            )
        }

        // Shutter Sound Profile Switcher
        TopBarIconButton(
            onClick = onSoundCycle,
            testTag = "shutter_sound_toggle",
            isActive = shutterSoundProfile != ShutterSoundProfile.SILENT
        ) {
            Icon(
                imageVector = if (shutterSoundProfile == ShutterSoundProfile.SILENT) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = "Sound: ${shutterSoundProfile.label}",
                tint = if (shutterSoundProfile != ShutterSoundProfile.SILENT) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // Vintage Lens Style Switcher
        TopBarIconButton(
            onClick = onLensStyleCycle,
            testTag = "lens_style_cycle",
            isActive = vintageLensStyle != VintageLensStyle.STANDARD
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Lens: ${vintageLensStyle.label}",
                tint = if (vintageLensStyle != VintageLensStyle.STANDARD) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // Grid Style Cycle
        TopBarIconButton(
            onClick = onGridCycle,
            testTag = "grid_cycle_button",
            isActive = gridType != GridType.NONE
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Grid ${gridType.label}",
                tint = if (gridType != GridType.NONE) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // AI Pose Coach Toggle
        TopBarIconButton(
            onClick = onPoseCoachToggle,
            testTag = "pose_coach_toggle",
            isActive = showPoseCoach
        ) {
            Icon(
                imageVector = Icons.Default.Accessibility,
                contentDescription = "AI Pose Coach",
                tint = if (showPoseCoach) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // Live Histogram Toggle
        TopBarIconButton(
            onClick = onHistogramToggle,
            testTag = "histogram_toggle",
            isActive = showHistogram
        ) {
            Icon(
                imageVector = Icons.Default.QueryStats,
                contentDescription = "Live Histogram",
                tint = if (showHistogram) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // Date Stamp Imprint Cycle
        TopBarIconButton(
            onClick = onDateStampCycle,
            testTag = "datestamp_cycle_button",
            isActive = dateStampFormat != FilmDateStampFormat.OFF
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Date Stamp ${dateStampFormat.label}",
                tint = if (dateStampFormat != FilmDateStampFormat.OFF) NaturalOlive else NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }

        // Timer
        TopBarIconButton(
            onClick = {
                val next = when (timerMode) {
                    TimerMode.OFF -> TimerMode.SEC_3
                    TimerMode.SEC_3 -> TimerMode.SEC_10
                    TimerMode.SEC_10 -> TimerMode.OFF
                }
                onTimerChanged(next)
            },
            testTag = "timer_toggle",
            isActive = timerMode != TimerMode.OFF
        ) {
            when (timerMode) {
                TimerMode.OFF -> Icon(Icons.Default.Timer, "Timer Off", tint = NaturalTextMuted, modifier = Modifier.size(17.dp))
                TimerMode.SEC_3 -> Icon(Icons.Default.Timer3, "Timer 3s", tint = NaturalOlive, modifier = Modifier.size(17.dp))
                TimerMode.SEC_10 -> Icon(Icons.Default.Timer10, "Timer 10s", tint = NaturalOlive, modifier = Modifier.size(17.dp))
            }
        }

        // AI Guidance Master Toggle
        TopBarIconButton(
            onClick = onAiToggled,
            testTag = "ai_guidance_toggle",
            isActive = isAiEnabled
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Guidance",
                tint = if (isAiEnabled) NaturalOlive else NaturalTextTertiary,
                modifier = Modifier.size(17.dp)
            )
        }

        // Settings Button
        TopBarIconButton(
            onClick = onOpenSettings,
            testTag = "settings_button"
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = NaturalTextMuted,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
fun TopBarIconButton(
    onClick: () -> Unit,
    testTag: String,
    isActive: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isActive) NaturalCard else NaturalSurface)
            .border(
                1.dp,
                if (isActive) NaturalOlive else NaturalBorderLight,
                CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
