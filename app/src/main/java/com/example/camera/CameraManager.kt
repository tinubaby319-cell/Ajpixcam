package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.data.model.CameraLens
import com.example.data.model.DoubleExposureBlendMode
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFilter
import com.example.data.model.FilmFrameStyle
import com.example.data.model.FlashMode
import com.example.data.model.LightLeakStyle
import com.example.data.model.PhotoItem
import com.example.data.model.PhotoMetadata
import com.example.data.model.VintageLensStyle
import com.example.filter.FilmPresets
import com.example.filter.FilmSimulationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val scope: CoroutineScope
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null

    private var previewUseCase: Preview? = null
    private var imageCaptureUseCase: ImageCapture? = null
    private var imageAnalysisUseCase: ImageAnalysis? = null

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    val analyzer = CameraAnalyzer(scope)

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _currentZoom = MutableStateFlow(1.0f)
    val currentZoom: StateFlow<Float> = _currentZoom.asStateFlow()

    private val _flashMode = MutableStateFlow(FlashMode.OFF)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _isRecordingVideo = MutableStateFlow(false)
    val isRecordingVideo: StateFlow<Boolean> = _isRecordingVideo.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _focusPoint = MutableStateFlow<Pair<Float, Float>?>(null)
    val focusPoint: StateFlow<Pair<Float, Float>?> = _focusPoint.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var videoTimerRunnable: Runnable? = null

    fun initialize(previewView: PreviewView, onReady: () -> Unit = {}) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(previewView)
                onReady()
            } catch (e: Exception) {
                Log.e("CameraManager", "Failed to bind camera use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(previewView: PreviewView) {
        val provider = cameraProvider ?: return
        val cameraSelector = if (_isFrontCamera.value) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val rotation = previewView.display?.rotation ?: Surface.ROTATION_0

        previewUseCase = Preview.Builder()
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        imageCaptureUseCase = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(rotation)
            .build()

        imageAnalysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                imageCaptureUseCase,
                imageAnalysisUseCase
            )
            cameraControl = camera?.cameraControl
            cameraInfo = camera?.cameraInfo

            // Apply current flash / zoom
            setZoom(_currentZoom.value)
            applyFlashMode(_flashMode.value)
        } catch (e: Exception) {
            Log.e("CameraManager", "Use case binding failed", e)
        }
    }

    fun toggleCamera(previewView: PreviewView) {
        _isFrontCamera.value = !_isFrontCamera.value
        bindCameraUseCases(previewView)
    }

    fun setZoom(zoomFactor: Float) {
        _currentZoom.value = zoomFactor
        cameraControl?.setZoomRatio(zoomFactor)
    }

    fun setLens(lens: CameraLens) {
        analyzer.currentLens = lens
        setZoom(lens.zoomFactor)
    }

    fun setFlash(mode: FlashMode) {
        _flashMode.value = mode
        applyFlashMode(mode)
    }

    private fun applyFlashMode(mode: FlashMode) {
        val capture = imageCaptureUseCase ?: return
        when (mode) {
            FlashMode.OFF -> {
                capture.flashMode = ImageCapture.FLASH_MODE_OFF
                cameraControl?.enableTorch(false)
            }
            FlashMode.AUTO -> {
                capture.flashMode = ImageCapture.FLASH_MODE_AUTO
                cameraControl?.enableTorch(false)
            }
            FlashMode.ON -> {
                capture.flashMode = ImageCapture.FLASH_MODE_ON
                cameraControl?.enableTorch(false)
            }
            FlashMode.TORCH -> {
                capture.flashMode = ImageCapture.FLASH_MODE_OFF
                cameraControl?.enableTorch(true)
            }
        }
    }

    fun setExposureCompensation(ev: Float) {
        val info = cameraInfo ?: return
        val control = cameraControl ?: return
        val range = info.exposureState.exposureCompensationRange
        val step = info.exposureState.exposureCompensationStep.toFloat()

        if (step > 0 && range.lower < range.upper) {
            val targetIndex = (ev / step).toInt().coerceIn(range.lower, range.upper)
            control.setExposureCompensationIndex(targetIndex)
        }
    }

    fun focusOnPoint(x: Float, y: Float, previewViewWidth: Float, previewViewHeight: Float) {
        if (previewViewWidth <= 0 || previewViewHeight <= 0) return
        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(previewViewWidth, previewViewHeight)
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        cameraControl?.startFocusAndMetering(action)

        _focusPoint.value = Pair(x / previewViewWidth, y / previewViewHeight)
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            _focusPoint.value = null
        }, 2500)
    }

    /**
     * Captures a single raw bitmap without applying grading filters (for 1st frame in Double Exposure)
     */
    fun captureRawBitmap(
        onSuccess: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCaptureUseCase ?: run {
            onError("Camera not ready")
            return
        }

        _isCapturing.value = true
        capture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val rawBitmap = BitmapUtils.imageProxyToBitmap(image)
                    image.close()
                    _isCapturing.value = false
                    if (rawBitmap != null) {
                        scope.launch(Dispatchers.Main) { onSuccess(rawBitmap) }
                    } else {
                        scope.launch(Dispatchers.Main) { onError("Failed to capture image") }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                    scope.launch(Dispatchers.Main) { onError("Capture failed: ${exception.message}") }
                }
            }
        )
    }

    /**
     * Start/Stop Flat Cine-Log / Film Video Recording Simulation
     */
    fun toggleVideoRecording(
        onVideoSaved: (PhotoItem) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isRecordingVideo.value) {
            // Stop recording
            _isRecordingVideo.value = false
            videoTimerRunnable?.let { mainHandler.removeCallbacks(it) }
            val dur = _recordingDurationSeconds.value
            _recordingDurationSeconds.value = 0

            // Capture final keyframe with telemetry
            capturePhoto(
                filter = FilmPresets.FLAT_LOG,
                intensity = 100,
                enableGrain = false,
                enableHalation = false,
                saveOriginal = true,
                dateStampFormat = FilmDateStampFormat.OFF,
                frameStyle = FilmFrameStyle.NONE,
                lightLeakStyle = LightLeakStyle.NONE,
                saveRawMaster = true,
                onSuccess = { item ->
                    val videoItem = item.copy(
                        metadata = item.metadata.copy(
                            sceneType = "Cine-Log 4K Clip (${dur}s)",
                            fileSizeFormatted = "${dur * 8}.4 MB"
                        )
                    )
                    onVideoSaved(videoItem)
                },
                onError = onError
            )
        } else {
            // Start recording
            _isRecordingVideo.value = true
            _recordingDurationSeconds.value = 0
            videoTimerRunnable = object : Runnable {
                override fun run() {
                    if (_isRecordingVideo.value) {
                        _recordingDurationSeconds.value += 1
                        mainHandler.postDelayed(this, 1000)
                    }
                }
            }
            mainHandler.post(videoTimerRunnable!!)
        }
    }

    /**
     * Captures photo, executes film simulation profile & retro imprint on bitmap, saves to storage, returns PhotoItem.
     */
    fun capturePhoto(
        filter: FilmFilter,
        intensity: Int,
        enableGrain: Boolean,
        enableHalation: Boolean,
        saveOriginal: Boolean,
        dateStampFormat: FilmDateStampFormat = FilmDateStampFormat.OFF,
        frameStyle: FilmFrameStyle = FilmFrameStyle.NONE,
        lightLeakStyle: LightLeakStyle = LightLeakStyle.NONE,
        applyDustScratches: Boolean = false,
        dustStrength: Float = 0.20f,
        lensStyle: VintageLensStyle = VintageLensStyle.STANDARD,
        doubleExposureBaseBitmap: Bitmap? = null,
        doubleExposureBlendMode: DoubleExposureBlendMode = DoubleExposureBlendMode.SCREEN,
        doubleExposureOpacity: Float = 0.6f,
        saveRawMaster: Boolean = false,
        onSuccess: (PhotoItem) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCaptureUseCase ?: run {
            onError("Camera capture not available")
            return
        }

        _isCapturing.value = true

        capture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val rawBitmap = BitmapUtils.imageProxyToBitmap(image)
                    image.close()

                    if (rawBitmap == null) {
                        _isCapturing.value = false
                        scope.launch(Dispatchers.Main) { onError("Failed to process captured image") }
                        return
                    }

                    scope.launch(Dispatchers.Default) {
                        try {
                            val timestamp = System.currentTimeMillis()
                            var originalFilePath: String? = null

                            // Save original master / flat log if requested
                            if (saveOriginal || saveRawMaster || filter.isFlatLog) {
                                val origFile = BitmapUtils.saveBitmapToInternalStorage(
                                    context,
                                    rawBitmap,
                                    "RAW_MASTER_${timestamp}.jpg"
                                )
                                originalFilePath = origFile.absolutePath
                            }

                            // If Double Exposure Mode with base bitmap, blend first
                            val processedSource = if (doubleExposureBaseBitmap != null) {
                                FilmSimulationEngine.blendDoubleExposure(
                                    base = doubleExposureBaseBitmap,
                                    overlay = rawBitmap,
                                    blendMode = doubleExposureBlendMode,
                                    overlayAlpha = doubleExposureOpacity
                                )
                            } else {
                                rawBitmap
                            }

                            // Apply Film Profile, Date Stamp, Light Leaks & Borders
                            val filteredBitmap = FilmSimulationEngine.applyFilmProfile(
                                source = processedSource,
                                filter = filter,
                                intensityPercent = intensity,
                                applyGrain = enableGrain,
                                applyHalation = enableHalation,
                                applyDustScratches = applyDustScratches,
                                dustStrength = dustStrength,
                                lensStyle = lensStyle,
                                dateStampFormat = dateStampFormat,
                                frameStyle = frameStyle,
                                lightLeakStyle = lightLeakStyle,
                                dateTimestamp = timestamp
                            )

                            // Save processed image
                            val processedFile = BitmapUtils.saveBitmapToInternalStorage(
                                context,
                                filteredBitmap,
                                "AURA_${filter.code.replace(" ", "_")}_${timestamp}.jpg"
                            )

                            // Also save to MediaStore DCIM
                            BitmapUtils.saveBitmapToMediaStore(context, filteredBitmap, "AuraCam_${filter.code}")

                            val aiState = analyzer.aiState.value

                            val photoItem = PhotoItem(
                                filePath = processedFile.absolutePath,
                                originalFilePath = originalFilePath,
                                timestamp = timestamp,
                                filterId = filter.id,
                                filterIntensity = intensity,
                                metadata = PhotoMetadata(
                                    iso = 100,
                                    shutterSpeed = "1/250s",
                                    ev = 0.0f,
                                    kelvin = aiState.estimatedKelvin,
                                    lens = "${_currentZoom.value}x",
                                    filterName = if (doubleExposureBaseBitmap != null) "Double Exp • ${filter.code}" else filter.code,
                                    filterIntensity = intensity,
                                    aiCompositionScore = aiState.composition.score,
                                    sceneType = if (filter.isFlatLog) "RAW / Flat Log Master" else aiState.sceneCategory.label,
                                    width = filteredBitmap.width,
                                    height = filteredBitmap.height,
                                    fileSizeFormatted = "${String.format("%.1f", processedFile.length() / (1024f * 1024f))} MB"
                                )
                            )

                            withContext(Dispatchers.Main) {
                                _isCapturing.value = false
                                onSuccess(photoItem)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                _isCapturing.value = false
                                onError("Processing error: ${e.localizedMessage}")
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                    scope.launch(Dispatchers.Main) {
                        onError("Capture failed: ${exception.message}")
                    }
                }
            }
        )
    }

    /**
     * Burst multi-frame capture (takes 4 frames sequentially)
     */
    fun captureBurst(
        filter: FilmFilter,
        intensity: Int,
        enableGrain: Boolean,
        enableHalation: Boolean,
        dateStampFormat: FilmDateStampFormat,
        frameStyle: FilmFrameStyle,
        lightLeakStyle: LightLeakStyle,
        onFrameCaptured: (PhotoItem) -> Unit,
        onBurstComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            _isCapturing.value = true
            for (i in 1..4) {
                capturePhoto(
                    filter = filter,
                    intensity = intensity,
                    enableGrain = enableGrain,
                    enableHalation = enableHalation,
                    saveOriginal = false,
                    dateStampFormat = dateStampFormat,
                    frameStyle = frameStyle,
                    lightLeakStyle = lightLeakStyle,
                    onSuccess = { onFrameCaptured(it) },
                    onError = { /* continue burst */ }
                )
                kotlinx.coroutines.delay(280)
            }
            _isCapturing.value = false
            onBurstComplete()
        }
    }

    fun shutdown() {
        videoTimerRunnable?.let { mainHandler.removeCallbacks(it) }
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e("CameraManager", "Error unbinding camera provider", e)
        }
        try {
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e("CameraManager", "Error shutting down camera executor", e)
        }
    }
}
