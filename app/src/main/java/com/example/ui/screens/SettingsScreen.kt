package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.Vignette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoubleExposureBlendMode
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFrameStyle
import com.example.data.model.GridType
import com.example.data.model.LightLeakStyle
import com.example.data.model.PoseCategory
import com.example.data.model.ShutterSoundProfile
import com.example.data.model.VintageLensStyle
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.userSettings

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera & Film Settings", color = NaturalDark, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NaturalDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NaturalBg)
            )
        },
        containerColor = NaturalBg,
        modifier = modifier.testTag("settings_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Privacy & On-Device Processing Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NaturalCard)
                    .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NaturalOlive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NaturalBg, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("100% On-Device Photography", color = NaturalDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NaturalOlive.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PRIVATE", color = NaturalOlive, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Color grading, double exposure, mechanical acoustics, and AI composition run locally with zero cloud upload.",
                            color = NaturalTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Mechanical Shutter & Acoustics
            SettingsSectionHeader("MECHANICAL SHUTTER ACOUSTICS & HAPTICS", Icons.Default.VolumeUp)

            SettingsOptionSelector(
                title = "Camera Shutter Sound Profile",
                subtitle = "Synthesized vintage mechanical audio feedback on capture",
                currentValue = settings.shutterSoundProfile.label,
                options = ShutterSoundProfile.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = ShutterSoundProfile.entries.find { it.label == label } ?: ShutterSoundProfile.LEICA_M
                    viewModel.updateSettings { it.copy(shutterSoundProfile = selected) }
                    viewModel.soundManager.playShutter(selected, isSoundEnabled = true, isHapticsEnabled = true)
                }
            )

            SettingsSwitchRow(
                title = "Enable Shutter Audio",
                subtitle = "Play acoustic sound on shutter click",
                checked = settings.enableShutterSound,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(enableShutterSound = it) } },
                testTag = "setting_enable_shutter_sound"
            )

            SettingsSwitchRow(
                title = "Shutter & Film Winding Haptics",
                subtitle = "Tactile mechanical vibration on capture and winding",
                checked = settings.hapticFeedback,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(hapticFeedback = it) } },
                testTag = "setting_haptics"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Vintage Analog & Film Emulation Section
            SettingsSectionHeader("VINTAGE ANALOG & OPTICAL EFFECTS", Icons.Default.CameraAlt)

            SettingsOptionSelector(
                title = "Vintage Lens Character",
                subtitle = "Optical distortion, fisheye barrel, anamorphic flares, and dreamy soft-focus",
                currentValue = settings.vintageLensStyle.label,
                options = VintageLensStyle.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = VintageLensStyle.entries.find { it.label == label } ?: VintageLensStyle.STANDARD
                    viewModel.updateSettings { it.copy(vintageLensStyle = selected) }
                }
            )

            SettingsSwitchRow(
                title = "Vintage Film Dust & Scratches",
                subtitle = "Overlays organic hairline scratches and microscopic film emulsion specs",
                checked = settings.enableDustScratches,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(enableDustScratches = it) } },
                testTag = "setting_dust_scratches"
            )

            SettingsSwitchRow(
                title = "Silver Halide Film Grain",
                subtitle = "Organic per-pixel film noise texture",
                checked = settings.enableFilmGrain,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(enableFilmGrain = it) } },
                testTag = "setting_film_grain"
            )

            SettingsSwitchRow(
                title = "Warm Halation Glow",
                subtitle = "Soft analog bloom in bright highlights and candle light",
                checked = settings.enableHalation,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(enableHalation = it) } },
                testTag = "setting_film_halation"
            )

            // Date Stamp Selector
            SettingsOptionSelector(
                title = "Retro Date Stamp Imprint",
                subtitle = "Classic analog camera LCD orange/white date imprint on photo",
                currentValue = settings.dateStampFormat.label,
                options = FilmDateStampFormat.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = FilmDateStampFormat.entries.find { it.label == label } ?: FilmDateStampFormat.OFF
                    viewModel.updateSettings { it.copy(dateStampFormat = selected) }
                }
            )

            // Film Borders & Frames
            SettingsOptionSelector(
                title = "Analog Frames & Borders",
                subtitle = "Polaroid white borders, 35mm film sprockets, or Hasselblad 6x6",
                currentValue = settings.filmFrameStyle.label,
                options = FilmFrameStyle.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = FilmFrameStyle.entries.find { it.label == label } ?: FilmFrameStyle.NONE
                    viewModel.updateSettings { it.copy(filmFrameStyle = selected) }
                }
            )

            // Light Leaks
            SettingsOptionSelector(
                title = "Analog Light Leak Flare",
                subtitle = "Warm amber sun flares and sunset edge light leaks",
                currentValue = settings.lightLeakStyle.label,
                options = LightLeakStyle.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = LightLeakStyle.entries.find { it.label == label } ?: LightLeakStyle.NONE
                    viewModel.updateSettings { it.copy(lightLeakStyle = selected) }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Double Exposure & Multi-Exposure
            SettingsSectionHeader("DOUBLE EXPOSURE BLEND MODES", Icons.Default.Layers)

            SettingsOptionSelector(
                title = "Default Blend Mode",
                subtitle = "How double exposure frames combine in camera",
                currentValue = settings.doubleExposureBlendMode.label,
                options = DoubleExposureBlendMode.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = DoubleExposureBlendMode.entries.find { it.label == label } ?: DoubleExposureBlendMode.SCREEN
                    viewModel.updateSettings { it.copy(doubleExposureBlendMode = selected) }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Hands-Free Gesture & Voice Controls
            SettingsSectionHeader("HANDS-FREE GESTURE & VOICE SHUTTER", Icons.Default.Mic)

            SettingsSwitchRow(
                title = "Palm Open / Wave Shutter Trigger",
                subtitle = "Auto triggers 3-second self-timer when waving palm in front of camera",
                checked = settings.handsFreePalmGestureEnabled,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(handsFreePalmGestureEnabled = it) } },
                testTag = "setting_palm_shutter"
            )

            SettingsSwitchRow(
                title = "Voice Command Shutter Trigger",
                subtitle = "Triggers capture on voice audio cues (e.g. 'Cheese', 'Shoot', 'Snap')",
                checked = settings.handsFreeVoiceEnabled,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(handsFreeVoiceEnabled = it) } },
                testTag = "setting_voice_shutter"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. RAW Master & Post Color Grading Section
            SettingsSectionHeader("RAW & COLOR GRADING (CINE-LOG)", Icons.Default.VideoCameraBack)

            SettingsSwitchRow(
                title = "Save RAW Master Copy",
                subtitle = "Saves flat sensor capture alongside graded photo for post-color grading in Lightroom / Studio",
                checked = settings.saveRawDngMaster,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(saveRawDngMaster = it) } },
                testTag = "setting_save_raw_master"
            )

            SettingsSwitchRow(
                title = "Save Original Unprocessed Copy",
                subtitle = "Keep original JPEG alongside film simulation",
                checked = settings.saveOriginalCopy,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(saveOriginalCopy = it) } },
                testTag = "setting_save_original"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Pro Composition & Telemetry Tools
            SettingsSectionHeader("COMPOSITION & PRO TELEMETRY", Icons.Default.GridOn)

            SettingsOptionSelector(
                title = "Composition Grid Type",
                subtitle = "Rule of Thirds, Fibonacci Spiral, Triangles, or Center Crosshair",
                currentValue = settings.gridType.label,
                options = GridType.entries.map { it.label },
                onOptionSelected = { label ->
                    val selected = GridType.entries.find { it.label == label } ?: GridType.RULE_OF_THIRDS
                    viewModel.updateSettings { it.copy(gridType = selected) }
                }
            )

            SettingsSwitchRow(
                title = "Live RGB & Luma Histogram",
                subtitle = "Real-time 4-channel exposure and highlight clipping monitor",
                checked = settings.showLiveHistogram,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(showLiveHistogram = it) } },
                testTag = "setting_live_histogram"
            )

            SettingsSwitchRow(
                title = "Focus Peaking Highlights",
                subtitle = "Colored outline on in-focus sharp contrast edges",
                checked = settings.showFocusPeaking,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(showFocusPeaking = it) } },
                testTag = "setting_focus_peaking"
            )

            SettingsSwitchRow(
                title = "Zebra Striping Overexposure Alert",
                subtitle = "Diagonal warning stripes on blown-out highlight zones",
                checked = settings.showZebraStripes,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(showZebraStripes = it) } },
                testTag = "setting_zebra_stripes"
            )

            SettingsSwitchRow(
                title = "Sub-Degree Sensor Horizon Level",
                subtitle = "Real-time gravity accelerometer tilt indicator",
                checked = settings.showHorizonLevel,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(showHorizonLevel = it) } },
                testTag = "setting_horizon_level"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 7. AI Guidance & Pose Assistant Section
            SettingsSectionHeader("AI PHOTOGRAPHY & POSE COACH", Icons.Default.AutoAwesome)

            SettingsSwitchRow(
                title = "AI Pose Coach Overlay",
                subtitle = "Translucent silhouette guides for portrait headshots, standing, café & couple poses",
                checked = settings.showPoseCoach,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(showPoseCoach = it) } },
                testTag = "setting_pose_coach"
            )

            SettingsSwitchRow(
                title = "AI Composition Guidance",
                subtitle = "AR target rings & golden node alignment vectors",
                checked = settings.aiCompositionEnabled,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(aiCompositionEnabled = it) } },
                testTag = "setting_ai_composition"
            )

            SettingsSwitchRow(
                title = "AI Auto-Framing",
                subtitle = "Intelligent lens and crop recommendations",
                checked = settings.aiAutoFramingEnabled,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(aiAutoFramingEnabled = it) } },
                testTag = "setting_ai_auto_framing"
            )

            SettingsSwitchRow(
                title = "AI Scene & Film Filter Suggestions",
                subtitle = "Automatic scene classification with 1-tap film presets",
                checked = settings.aiFilterRecommendationEnabled,
                onCheckedChange = { viewModel.updateSettings { s -> s.copy(aiFilterRecommendationEnabled = it) } },
                testTag = "setting_ai_filter_rec"
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = NaturalOlive, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = NaturalOlive,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorderLight, RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(title, color = NaturalDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = NaturalTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NaturalBg,
                    checkedTrackColor = NaturalOlive,
                    uncheckedThumbColor = NaturalTextTertiary,
                    uncheckedTrackColor = NaturalCard
                )
            )
        }
    }
}

@Composable
fun SettingsOptionSelector(
    title: String,
    subtitle: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorderLight, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(title, color = NaturalDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = NaturalTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { opt ->
                    val isSelected = opt == currentValue
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NaturalOlive else NaturalCard)
                            .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                            .clickable { onOptionSelected(opt) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = opt,
                            color = if (isSelected) NaturalBg else NaturalDark,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
