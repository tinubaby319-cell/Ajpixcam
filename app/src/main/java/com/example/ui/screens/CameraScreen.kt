package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.camera.CameraManager
import com.example.data.model.AspectRatioMode
import com.example.data.model.CameraLens
import com.example.data.model.CameraMode
import com.example.ui.components.AIAutoFramingOverlay
import com.example.ui.components.AICompositionOverlay
import com.example.ui.components.AIRecommendationCard
import com.example.ui.components.AdvancedGridOverlay
import com.example.ui.components.CompareSlider
import com.example.ui.components.DoubleExposureOverlay
import com.example.ui.components.FilterCarousel
import com.example.ui.components.FocusPeakingOverlay
import com.example.ui.components.HandsFreeCountdownOverlay
import com.example.ui.components.LensSelector
import com.example.ui.components.LiveHistogramOverlay
import com.example.ui.components.ModeSelector
import com.example.ui.components.PoseCoachOverlay
import com.example.ui.components.ProControlsBar
import com.example.ui.components.ShutterButton
import com.example.ui.components.TopControlBar
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecipeStudio: () -> Unit,
    onNavigateToFilmLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraManager = remember {
        CameraManager(context, lifecycleOwner, scope)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }

    // Connect analyzer and histogram to viewModel
    LaunchedEffect(cameraManager.analyzer) {
        launch {
            cameraManager.analyzer.aiState.collect { liveAI ->
                viewModel.updateLiveAiState(liveAI)
            }
        }
        launch {
            cameraManager.analyzer.histogramState.collect { histo ->
                viewModel.updateHistogram(histo)
            }
        }
    }

    val isRecordingVideo by cameraManager.isRecordingVideo.collectAsState()
    val recordingDuration by cameraManager.recordingDurationSeconds.collectAsState()

    // Hands-free state
    val isHandsFreeCountdownActive by viewModel.handsFreeManager.isCountdownActive.collectAsState()
    val handsFreeCountdownSec by viewModel.handsFreeManager.countdownSecRemaining.collectAsState()
    val handsFreeMessage by viewModel.handsFreeManager.triggerStatusMessage.collectAsState()

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    val flashAlpha = remember { Animatable(0f) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Execute capture logic
    fun executeShutterCapture() {
        scope.launch {
            // Play authentic mechanical shutter acoustic & haptic
            viewModel.playShutterSound()

            // Flash visual feedback
            flashAlpha.snapTo(0.85f)
            launch { flashAlpha.animateTo(0f, tween(200)) }

            val userSet = uiState.userSettings
            val customRec = uiState.activeCustomRecipe

            val effectiveGrain = customRec?.let { it.grain > 0.05f } ?: userSet.enableFilmGrain
            val effectiveHalation = customRec?.let { it.halation > 0.05f } ?: userSet.enableHalation
            val effectiveDust = customRec?.let { it.dustScratches > 0.05f } ?: userSet.enableDustScratches
            val effectiveDustStrength = customRec?.dustScratches ?: 0.20f
            val effectiveLens = customRec?.lensStyle ?: userSet.vintageLensStyle
            val effectiveFrame = customRec?.frameStyle ?: userSet.filmFrameStyle
            val effectiveDateStamp = customRec?.dateStamp ?: userSet.dateStampFormat
            val effectiveLeak = customRec?.lightLeak ?: userSet.lightLeakStyle

            if (uiState.selectedMode == CameraMode.DOUBLE_EXPOSURE) {
                if (uiState.doubleExposureFirstBitmap == null) {
                    // Step 1: Capture base frame
                    cameraManager.captureRawBitmap(
                        onSuccess = { rawBmp ->
                            viewModel.setDoubleExposureFirstFrame(rawBmp)
                            scope.launch {
                                snackbarHostState.showSnackbar("📸 Base frame captured! Now frame Step 2.")
                            }
                        },
                        onError = { err -> viewModel.setError(err) }
                    )
                } else {
                    // Step 2: Capture overlay & blend
                    cameraManager.capturePhoto(
                        filter = uiState.selectedFilter,
                        intensity = uiState.filterIntensity,
                        enableGrain = effectiveGrain,
                        enableHalation = effectiveHalation,
                        saveOriginal = userSet.saveOriginalCopy,
                        dateStampFormat = effectiveDateStamp,
                        frameStyle = effectiveFrame,
                        lightLeakStyle = effectiveLeak,
                        applyDustScratches = effectiveDust,
                        dustStrength = effectiveDustStrength,
                        lensStyle = effectiveLens,
                        doubleExposureBaseBitmap = uiState.doubleExposureFirstBitmap,
                        doubleExposureBlendMode = uiState.doubleExposureBlendMode,
                        doubleExposureOpacity = uiState.doubleExposureOpacity,
                        saveRawMaster = userSet.saveRawDngMaster,
                        onSuccess = { photoItem ->
                            viewModel.clearDoubleExposure()
                            viewModel.onPhotoCaptured(photoItem)
                        },
                        onError = { err -> viewModel.setError(err) }
                    )
                }
            } else {
                // Standard capture
                cameraManager.capturePhoto(
                    filter = uiState.selectedFilter,
                    intensity = uiState.filterIntensity,
                    enableGrain = effectiveGrain,
                    enableHalation = effectiveHalation,
                    saveOriginal = userSet.saveOriginalCopy,
                    dateStampFormat = effectiveDateStamp,
                    frameStyle = effectiveFrame,
                    lightLeakStyle = effectiveLeak,
                    applyDustScratches = effectiveDust,
                    dustStrength = effectiveDustStrength,
                    lensStyle = effectiveLens,
                    saveRawMaster = userSet.saveRawDngMaster,
                    onSuccess = { photoItem ->
                        viewModel.onPhotoCaptured(photoItem)
                    },
                    onError = { err -> viewModel.setError(err) }
                )
            }
        }
    }

    // Display error message if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Roll completed notification
    LaunchedEffect(uiState.rollJustCompleted) {
        if (uiState.rollJustCompleted) {
            snackbarHostState.showSnackbar("🎞️ Film Roll Complete (36/36) — Ready to develop in Film Lab!")
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NaturalBg)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera Permission Required", color = NaturalDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "AuraCam needs camera access to capture photographs and provide real-time AI composition guidance.",
                    color = NaturalTextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg),
                    modifier = Modifier.testTag("grant_camera_permission_button")
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // Pinch Zoom gesture
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        val newZoom = (uiState.selectedLens.zoomFactor * zoomChange).coerceIn(0.5f, 5.0f)
        cameraManager.setZoom(newZoom)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBg)
            .testTag("camera_screen_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with shortcuts
            TopControlBar(
                flashMode = uiState.flashMode,
                onFlashChanged = {
                    viewModel.onFlashChanged(it)
                    cameraManager.setFlash(it)
                },
                timerMode = uiState.timerMode,
                onTimerChanged = { viewModel.onTimerChanged(it) },
                aspectRatio = uiState.aspectRatio,
                onAspectRatioChanged = { viewModel.onAspectRatioChanged(it) },
                isAiEnabled = uiState.isAiGuidanceEnabled,
                onAiToggled = { viewModel.toggleAiGuidance() },
                gridType = uiState.userSettings.gridType,
                onGridCycle = { viewModel.cycleGridType() },
                dateStampFormat = uiState.userSettings.dateStampFormat,
                onDateStampCycle = { viewModel.cycleDateStampFormat() },
                showHistogram = uiState.userSettings.showLiveHistogram,
                onHistogramToggle = { viewModel.toggleHistogram() },
                showPoseCoach = uiState.userSettings.showPoseCoach || uiState.selectedMode == CameraMode.POSE_COACH,
                onPoseCoachToggle = { viewModel.togglePoseCoach() },
                shutterSoundProfile = uiState.userSettings.shutterSoundProfile,
                onSoundCycle = { viewModel.cycleShutterSoundProfile() },
                vintageLensStyle = uiState.userSettings.vintageLensStyle,
                onLensStyleCycle = { viewModel.cycleVintageLensStyle() },
                onOpenRecipeStudio = onNavigateToRecipeStudio,
                onOpenFilmLab = onNavigateToFilmLab,
                onOpenSettings = onNavigateToSettings
            )

            // Camera Viewfinder Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NaturalSurface)
                    .border(1.dp, NaturalBorderLight, RoundedCornerShape(24.dp))
                    .transformable(state = transformableState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                val nextLens = when (uiState.selectedLens) {
                                    CameraLens.WIDE -> CameraLens.TELE_2X
                                    CameraLens.TELE_2X -> CameraLens.WIDE
                                    else -> CameraLens.WIDE
                                }
                                viewModel.onLensChanged(nextLens)
                                cameraManager.setLens(nextLens)
                            },
                            onTap = { offset ->
                                cameraManager.focusOnPoint(offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                            }
                        )
                    }
            ) {
                // Live Android PreviewView
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            cameraManager.initialize(this)
                            previewViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 1. Multi-Grid Overlay
                if (uiState.userSettings.showGrid) {
                    AdvancedGridOverlay(gridType = uiState.userSettings.gridType)
                }

                // 2. Focus Peaking & Zebra Striping
                FocusPeakingOverlay(
                    showFocusPeaking = uiState.userSettings.showFocusPeaking || uiState.selectedMode == CameraMode.PRO,
                    showZebraStripes = uiState.userSettings.showZebraStripes
                )

                // 3. AI Composition Overlays
                val showAiHUD = uiState.isAiGuidanceEnabled &&
                        (uiState.selectedMode == CameraMode.AI_COMPOSE || uiState.selectedMode == CameraMode.PHOTO)

                if (showAiHUD) {
                    AICompositionOverlay(
                        composition = uiState.liveAiState.composition,
                        subject = uiState.liveAiState.subject,
                        horizon = uiState.liveAiState.horizon,
                        showGrid = false,
                        showHorizon = uiState.userSettings.showHorizonLevel,
                        showAiGuidance = uiState.selectedMode == CameraMode.AI_COMPOSE || uiState.isAiGuidanceEnabled
                    )
                }

                if (uiState.selectedMode == CameraMode.AUTO_FRAME) {
                    AIAutoFramingOverlay(
                        autoFraming = uiState.liveAiState.autoFraming,
                        onApplyLens = { lens ->
                            viewModel.onLensChanged(lens)
                            cameraManager.setLens(lens)
                        }
                    )
                }

                // 4. AI Pose Coach Silhouette Overlay
                if (uiState.userSettings.showPoseCoach || uiState.selectedMode == CameraMode.POSE_COACH) {
                    PoseCoachOverlay(
                        poseCategory = uiState.userSettings.poseCategory,
                        onCategorySelected = { viewModel.onPoseCategorySelected(it) }
                    )
                }

                // 5. Live Histogram
                if (uiState.userSettings.showLiveHistogram || uiState.selectedMode == CameraMode.PRO || uiState.selectedMode == CameraMode.CINE_LOG) {
                    LiveHistogramOverlay(
                        histogramData = uiState.histogramData,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp)
                    )
                }

                // 6. Double Exposure Viewfinder Overlay (Feature 3)
                if (uiState.selectedMode == CameraMode.DOUBLE_EXPOSURE) {
                    DoubleExposureOverlay(
                        firstFrame = uiState.doubleExposureFirstBitmap,
                        blendMode = uiState.doubleExposureBlendMode,
                        overlayOpacity = uiState.doubleExposureOpacity,
                        onBlendModeSelected = { viewModel.setDoubleExposureBlendMode(it) },
                        onOpacityChanged = { viewModel.setDoubleExposureOpacity(it) },
                        onClearFirstFrame = { viewModel.clearDoubleExposure() },
                        onPickFromGallery = {
                            uiState.latestPhoto?.filePath?.let {
                                viewModel.setDoubleExposureFirstFrameFromPath(it)
                            }
                        }
                    )
                }

                // 7. Active Custom Recipe HUD Tag
                if (uiState.activeCustomRecipe != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 10.dp, start = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaturalOlive.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✨ RECIPE: ${uiState.activeCustomRecipe?.name}",
                            color = NaturalBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (uiState.userSettings.filmRollMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 10.dp, start = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaturalCard.copy(alpha = 0.9f))
                            .border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎞️ EXP ${uiState.userSettings.rollExpCount}/${uiState.userSettings.rollMaxExp}",
                            color = NaturalDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 8. Video Recording Status Banner
                if (isRecordingVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE63946))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val mins = recordingDuration / 60
                            val secs = recordingDuration % 60
                            Text(
                                text = "REC %02d:%02d • CINE-LOG 4K".format(mins, secs),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 9. AI Floating Recommendation Card
                if (uiState.userSettings.aiFilterRecommendationEnabled && !isRecordingVideo && uiState.selectedMode != CameraMode.DOUBLE_EXPOSURE) {
                    AIRecommendationCard(
                        recommendation = uiState.liveAiState.filterRecommendation,
                        currentFilter = uiState.selectedFilter,
                        visible = uiState.showRecommendationCard,
                        onApplyFilter = { recFilter ->
                            viewModel.applyRecommendation(recFilter)
                        },
                        onDismiss = { viewModel.dismissRecommendation() },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // Tap-to-focus indicator ring
                val focusPt = cameraManager.focusPoint.collectAsState().value
                if (focusPt != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.TopStart)
                                .padding(
                                    start = (focusPt.first * 300).dp.coerceAtLeast(0.dp),
                                    top = (focusPt.second * 400).dp.coerceAtLeast(0.dp)
                                )
                                .border(1.5.dp, NaturalOlive, CircleShape)
                        )
                    }
                }

                // Before/After comparison slider view
                if (uiState.isComparing) {
                    CompareSlider(
                        filterName = uiState.selectedFilter.code,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Shutter White Flash animation
                if (flashAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NaturalBg.copy(alpha = flashAlpha.value))
                    )
                }

                // Lens Selector floating inside bottom of preview
                LensSelector(
                    selectedLens = uiState.selectedLens,
                    onLensSelected = { lens ->
                        viewModel.onLensChanged(lens)
                        cameraManager.setLens(lens)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )

                // Hands-free Countdown Overlay (Feature 6)
                HandsFreeCountdownOverlay(
                    isActive = isHandsFreeCountdownActive,
                    secondsRemaining = handsFreeCountdownSec,
                    triggerMessage = handsFreeMessage
                )
            }

            // Pro Manual Controls bar (if in PRO mode)
            if (uiState.selectedMode == CameraMode.PRO) {
                ProControlsBar(
                    proSettings = uiState.proSettings,
                    onSettingsChanged = { proSet ->
                        viewModel.onProSettingsChanged(proSet)
                        cameraManager.setExposureCompensation(proSet.evCompensation)
                    }
                )
            }

            // Film Filter Strip
            FilterCarousel(
                selectedFilter = uiState.selectedFilter,
                filterIntensity = uiState.filterIntensity,
                onFilterSelected = { viewModel.onFilterChanged(it) },
                onIntensityChanged = { viewModel.onIntensityChanged(it) },
                onCompareToggle = { viewModel.toggleCompare() },
                isComparing = uiState.isComparing
            )

            // Mode Selector
            ModeSelector(
                selectedMode = uiState.selectedMode,
                onModeSelected = { viewModel.onModeChanged(it) }
            )

            // Bottom Shutter & Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Gallery Thumbnail
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NaturalSurface)
                        .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToGallery)
                        .testTag("gallery_thumbnail_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.latestPhoto != null && uiState.latestPhoto?.exists == true) {
                        Image(
                            painter = rememberAsyncImagePainter(uiState.latestPhoto?.file),
                            contentDescription = "Gallery Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = NaturalDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Center: Shutter / Video Record Button
                ShutterButton(
                    isCapturing = uiState.isCapturing,
                    isVideoMode = uiState.selectedMode.isVideo,
                    isRecordingVideo = isRecordingVideo,
                    isAiMode = uiState.selectedMode == CameraMode.AI_COMPOSE,
                    onClick = {
                        if (uiState.selectedMode.isVideo) {
                            cameraManager.toggleVideoRecording(
                                onVideoSaved = { videoItem ->
                                    viewModel.onPhotoCaptured(videoItem)
                                },
                                onError = { err -> viewModel.setError(err) }
                            )
                        } else if (uiState.selectedMode == CameraMode.BURST) {
                            cameraManager.captureBurst(
                                filter = uiState.selectedFilter,
                                intensity = uiState.filterIntensity,
                                enableGrain = uiState.userSettings.enableFilmGrain,
                                enableHalation = uiState.userSettings.enableHalation,
                                dateStampFormat = uiState.userSettings.dateStampFormat,
                                frameStyle = uiState.userSettings.filmFrameStyle,
                                lightLeakStyle = uiState.userSettings.lightLeakStyle,
                                onFrameCaptured = { viewModel.onPhotoCaptured(it) },
                                onBurstComplete = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("⚡ Burst 4 frames saved!")
                                    }
                                },
                                onError = { err -> viewModel.setError(err) }
                            )
                        } else if (uiState.selectedMode == CameraMode.HANDS_FREE) {
                            viewModel.handsFreeManager.startHandsFreeCountdown(
                                triggerReason = "✋ Palm / Voice Triggered",
                                onComplete = { executeShutterCapture() }
                            )
                        } else {
                            if (uiState.timerMode.seconds > 0) {
                                scope.launch {
                                    delay(uiState.timerMode.seconds * 1000L)
                                    executeShutterCapture()
                                }
                            } else {
                                executeShutterCapture()
                            }
                        }
                    }
                )

                // Right: Camera Switch Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NaturalSurface)
                        .border(1.dp, NaturalBorder, CircleShape)
                        .clickable {
                            previewViewRef?.let { cameraManager.toggleCamera(it) }
                        }
                        .testTag("switch_camera_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = NaturalDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
