package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.BitmapUtils
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFrameStyle
import com.example.data.model.LightLeakStyle
import com.example.data.model.PhotoItem
import com.example.filter.FilmPresets
import com.example.filter.FilmSimulationEngine
import com.example.ui.components.FilterCarousel
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class EditTool(val label: String) {
    PRESETS("Film Preset"),
    EXPOSURE("Exposure"),
    CONTRAST("Contrast"),
    TEMPERATURE("Warmth"),
    SATURATION("Saturation"),
    GRAIN("Film Grain"),
    VIGNETTE("Vignette"),
    DATE_STAMP("Date Stamp"),
    FRAME("Frame"),
    LIGHT_LEAK("Light Leak")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    photoId: Long,
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allPhotos by viewModel.allPhotosFlow.collectAsState()
    val photo = allPhotos.find { it.id == photoId }?.toDomainModel()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(true) }
    var isHoldingCompare by remember { mutableStateOf(false) }

    // Editor adjustments
    var selectedFilter by remember { mutableStateOf(FilmPresets.F_C200) }
    var filterIntensity by remember { mutableIntStateOf(70) }
    var exposureAdjustment by remember { mutableFloatStateOf(0.0f) }
    var contrastAdjustment by remember { mutableFloatStateOf(0.0f) }
    var tempAdjustment by remember { mutableFloatStateOf(0.0f) }
    var satAdjustment by remember { mutableFloatStateOf(0.0f) }
    var grainAdjustment by remember { mutableFloatStateOf(0.2f) }
    var vignetteAdjustment by remember { mutableFloatStateOf(0.1f) }
    var dateStampFormat by remember { mutableStateOf(FilmDateStampFormat.OFF) }
    var frameStyle by remember { mutableStateOf(FilmFrameStyle.NONE) }
    var lightLeakStyle by remember { mutableStateOf(LightLeakStyle.NONE) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }

    var activeTool by remember { mutableStateOf(EditTool.PRESETS) }

    // Load initial bitmap
    LaunchedEffect(photo) {
        if (photo != null) {
            val sourcePath = photo.originalFilePath ?: photo.filePath
            withContext(Dispatchers.IO) {
                val loaded = BitmapFactory.decodeFile(sourcePath)
                originalBitmap = loaded
                selectedFilter = FilmPresets.getById(photo.filterId)
                filterIntensity = photo.filterIntensity
                isProcessing = false
            }
        }
    }

    // Recompute preview bitmap whenever adjustment changes
    fun renderPreview() {
        val base = originalBitmap ?: return
        scope.launch(Dispatchers.Default) {
            var working = base
            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                working = Bitmap.createBitmap(base, 0, 0, base.width, base.height, matrix, true)
            }

            val customFilter = selectedFilter.copy(
                exposureBias = selectedFilter.exposureBias + exposureAdjustment,
                contrast = selectedFilter.contrast + contrastAdjustment,
                temperatureOffset = selectedFilter.temperatureOffset + tempAdjustment,
                saturation = selectedFilter.saturation + satAdjustment,
                grainStrength = (selectedFilter.grainStrength + grainAdjustment).coerceIn(0f, 1f),
                vignetteStrength = (selectedFilter.vignetteStrength + vignetteAdjustment).coerceIn(0f, 1f)
            )

            val rendered = FilmSimulationEngine.applyFilmProfile(
                source = working,
                filter = customFilter,
                intensityPercent = filterIntensity,
                applyGrain = true,
                applyHalation = true,
                dateStampFormat = dateStampFormat,
                frameStyle = frameStyle,
                lightLeakStyle = lightLeakStyle,
                dateTimestamp = photo?.timestamp ?: System.currentTimeMillis()
            )

            withContext(Dispatchers.Main) {
                previewBitmap = rendered
            }
        }
    }

    LaunchedEffect(
        selectedFilter,
        filterIntensity,
        exposureAdjustment,
        contrastAdjustment,
        tempAdjustment,
        satAdjustment,
        grainAdjustment,
        vignetteAdjustment,
        dateStampFormat,
        frameStyle,
        lightLeakStyle,
        rotationDegrees,
        originalBitmap
    ) {
        renderPreview()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color Grading & Film Studio", color = NaturalDark, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NaturalDark)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            rotationDegrees = (rotationDegrees + 90f) % 360f
                        },
                        modifier = Modifier.testTag("editor_rotate_button")
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = NaturalDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NaturalBg)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NaturalSurface)
                    .border(1.dp, NaturalBorderLight, RoundedCornerShape(0.dp))
            ) {
                // Tool values slider or selector
                when (activeTool) {
                    EditTool.PRESETS -> {
                        FilterCarousel(
                            selectedFilter = selectedFilter,
                            filterIntensity = filterIntensity,
                            onFilterSelected = { selectedFilter = it },
                            onIntensityChanged = { filterIntensity = it }
                        )
                    }


                    EditTool.EXPOSURE -> {
                        EditorSliderRow("Exposure", exposureAdjustment, -1.0f..1.0f, "%+.2f") {
                            exposureAdjustment = it
                        }
                    }

                    EditTool.CONTRAST -> {
                        EditorSliderRow("Contrast", contrastAdjustment, -0.5f..0.5f, "%+.2f") {
                            contrastAdjustment = it
                        }
                    }

                    EditTool.TEMPERATURE -> {
                        EditorSliderRow("Warmth", tempAdjustment, -0.4f..0.4f, "%+.2f") {
                            tempAdjustment = it
                        }
                    }

                    EditTool.SATURATION -> {
                        EditorSliderRow("Saturation", satAdjustment, -0.6f..0.6f, "%+.2f") {
                            satAdjustment = it
                        }
                    }

                    EditTool.GRAIN -> {
                        EditorSliderRow("Film Grain", grainAdjustment, 0.0f..1.0f, "%.2f") {
                            grainAdjustment = it
                        }
                    }

                    EditTool.VIGNETTE -> {
                        EditorSliderRow("Vignette", vignetteAdjustment, 0.0f..1.0f, "%.2f") {
                            vignetteAdjustment = it
                        }
                    }

                    EditTool.DATE_STAMP -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilmDateStampFormat.entries.forEach { format ->
                                val isSel = dateStampFormat == format
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) NaturalOlive else NaturalCard)
                                        .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                        .clickable { dateStampFormat = format }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(format.label, color = if (isSel) NaturalBg else NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    EditTool.FRAME -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilmFrameStyle.entries.forEach { style ->
                                val isSel = frameStyle == style
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) NaturalOlive else NaturalCard)
                                        .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                        .clickable { frameStyle = style }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(style.label, color = if (isSel) NaturalBg else NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    EditTool.LIGHT_LEAK -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LightLeakStyle.entries.forEach { style ->
                                val isSel = lightLeakStyle == style
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) NaturalOlive else NaturalCard)
                                        .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                        .clickable { lightLeakStyle = style }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(style.label, color = if (isSel) NaturalBg else NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Bottom Tool Categories Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditTool.entries.forEach { tool ->
                        val isSelected = activeTool == tool
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) NaturalCard else NaturalBg)
                                .border(1.dp, if (isSelected) NaturalOlive else NaturalBorderLight, RoundedCornerShape(14.dp))
                                .clickable { activeTool = tool }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("tool_${tool.name.lowercase()}")
                        ) {
                            Text(
                                text = tool.label,
                                color = if (isSelected) NaturalOlive else NaturalDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Save Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(NaturalOlive)
                            .clickable {
                                val finalBmp = previewBitmap ?: return@clickable
                                scope.launch(Dispatchers.IO) {
                                    val timestamp = System.currentTimeMillis()
                                    val file = BitmapUtils.saveBitmapToInternalStorage(
                                        context,
                                        finalBmp,
                                        "EDITED_${selectedFilter.code.replace(" ", "_")}_${timestamp}.jpg"
                                    )
                                    BitmapUtils.saveBitmapToMediaStore(context, finalBmp, "AuraCam_Edited")

                                    val newPhoto = PhotoItem(
                                        filePath = file.absolutePath,
                                        originalFilePath = photo?.originalFilePath ?: photo?.filePath,
                                        timestamp = timestamp,
                                        filterId = selectedFilter.id,
                                        filterIntensity = filterIntensity,
                                        metadata = photo?.metadata?.copy(
                                            filterName = selectedFilter.code,
                                            filterIntensity = filterIntensity
                                        ) ?: com.example.data.model.PhotoMetadata()
                                    )
                                    viewModel.onPhotoCaptured(newPhoto)
                                    withContext(Dispatchers.Main) { onBack() }
                                }
                            }
                            .testTag("editor_save_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save Graded Photograph", color = NaturalBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = NaturalBg,
        modifier = modifier.testTag("photo_editor_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NaturalSurface)
                    .border(1.dp, NaturalBorderLight, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = NaturalOlive)
                } else {
                    val displayBitmap = if (isHoldingCompare) originalBitmap else (previewBitmap ?: originalBitmap)
                    displayBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Editor Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Hold to compare original button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NaturalSurface)
                        .border(1.dp, NaturalBorder, RoundedCornerShape(14.dp))
                        .clickable { isHoldingCompare = !isHoldingCompare }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("editor_hold_compare")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Compare, contentDescription = null, tint = if (isHoldingCompare) NaturalOlive else NaturalDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHoldingCompare) "ORIGINAL RAW" else "COMPARE", color = NaturalDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = title, color = NaturalTextMuted, fontSize = 12.sp, modifier = Modifier.width(74.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f).testTag("editor_slider"),
            colors = SliderDefaults.colors(
                thumbColor = NaturalOlive,
                activeTrackColor = NaturalOlive,
                inactiveTrackColor = NaturalBorderLight
            )
        )
        Text(
            text = String.format(format, value),
            color = NaturalDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp)
        )
    }
}
