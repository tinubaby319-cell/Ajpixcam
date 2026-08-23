package com.example.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.ai.AutoFramingEngine
import com.example.ai.CompositionEngine
import com.example.ai.FilterRecommendationEngine
import com.example.ai.SceneClassifier
import com.example.data.model.CameraLens
import com.example.data.model.HistogramData
import com.example.data.model.LiveAIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class CameraAnalyzer(
    private val scope: CoroutineScope
) : ImageAnalysis.Analyzer {

    private val sceneClassifier = SceneClassifier()
    private val compositionEngine = CompositionEngine()
    private val autoFramingEngine = AutoFramingEngine()
    private val filterRecommendationEngine = FilterRecommendationEngine()

    private val _aiState = MutableStateFlow(LiveAIState())
    val aiState: StateFlow<LiveAIState> = _aiState.asStateFlow()

    private val _histogramState = MutableStateFlow(HistogramData())
    val histogramState: StateFlow<HistogramData> = _histogramState.asStateFlow()

    private val isAnalyzing = AtomicBoolean(false)
    private var lastAnalysisTimestamp = 0L
    private val minAnalysisIntervalMs = 120L // ~8-10 FPS for ML inference (preserves battery & UI 60fps)

    var currentLens: CameraLens = CameraLens.WIDE
    var isPortraitMode: Boolean = false
    var currentHorizonAngle: Float = 0.0f
    var devSimulationMode: Boolean = false

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastAnalysisTimestamp < minAnalysisIntervalMs || !isAnalyzing.compareAndSet(false, true)) {
            image.close()
            return
        }

        lastAnalysisTimestamp = now

        // Convert ImageProxy to downscaled bitmap
        val bitmap = try {
            BitmapUtils.imageProxyToBitmap(image)
        } catch (_: Exception) {
            null
        } finally {
            image.close()
        }

        if (bitmap == null) {
            isAnalyzing.set(false)
            return
        }

        scope.launch(Dispatchers.Default) {
            try {
                // Downscale for ultra-fast CV processing
                val scaled = if (bitmap.width > 200) {
                    val scale = 200f / bitmap.width
                    Bitmap.createScaledBitmap(bitmap, 200, (bitmap.height * scale).toInt(), false)
                } else {
                    bitmap
                }

                // Compute real-time RGB & Luma Histogram
                val histo = computeHistogram(scaled)
                _histogramState.value = histo

                val sceneAnalysis = sceneClassifier.classifyFrame(scaled)
                val (subject, composition) = compositionEngine.analyzeComposition(
                    scaled,
                    isPortraitMode = isPortraitMode,
                    horizonAngle = currentHorizonAngle
                )
                val autoFraming = autoFramingEngine.recommendFraming(subject, currentLens)
                val filterRec = filterRecommendationEngine.recommend(sceneAnalysis)

                _aiState.value = _aiState.value.copy(
                    sceneCategory = sceneAnalysis.category,
                    sceneConfidence = sceneAnalysis.confidence,
                    estimatedKelvin = sceneAnalysis.estimatedKelvin,
                    brightnessLevel = sceneAnalysis.averageLuminance,
                    subject = subject,
                    composition = composition,
                    autoFraming = autoFraming,
                    filterRecommendation = filterRec,
                    isProcessing = false
                )
            } catch (_: Exception) {
                // Keep robust
            } finally {
                isAnalyzing.set(false)
            }
        }
    }

    private fun computeHistogram(bitmap: Bitmap): HistogramData {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val binCount = 32
        val lumCounts = IntArray(binCount)
        val rCounts = IntArray(binCount)
        val gCounts = IntArray(binCount)
        val bCounts = IntArray(binCount)

        var totalPixels = 0
        var clippedHigh = 0
        var crushedLow = 0

        val step = 2 // Sample every 2nd pixel for instant calculation
        for (i in 0 until pixels.size step step) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()

            if (lum > 245) clippedHigh++
            if (lum < 15) crushedLow++

            val lumBin = (lum * binCount / 256).coerceIn(0, binCount - 1)
            val rBin = (r * binCount / 256).coerceIn(0, binCount - 1)
            val gBin = (g * binCount / 256).coerceIn(0, binCount - 1)
            val bBin = (b * binCount / 256).coerceIn(0, binCount - 1)

            lumCounts[lumBin]++
            rCounts[rBin]++
            gCounts[gBin]++
            bCounts[bBin]++
            totalPixels++
        }

        var maxCount = 1
        for (i in 0 until binCount) {
            if (lumCounts[i] > maxCount) maxCount = lumCounts[i]
            if (rCounts[i] > maxCount) maxCount = rCounts[i]
            if (gCounts[i] > maxCount) maxCount = gCounts[i]
            if (bCounts[i] > maxCount) maxCount = bCounts[i]
        }

        val lumFloat = FloatArray(binCount) { lumCounts[it].toFloat() / maxCount }
        val rFloat = FloatArray(binCount) { rCounts[it].toFloat() / maxCount }
        val gFloat = FloatArray(binCount) { gCounts[it].toFloat() / maxCount }
        val bFloat = FloatArray(binCount) { bCounts[it].toFloat() / maxCount }

        val isClipped = totalPixels > 0 && (clippedHigh.toFloat() / totalPixels > 0.08f)
        val isCrushed = totalPixels > 0 && (crushedLow.toFloat() / totalPixels > 0.12f)

        return HistogramData(
            lum = lumFloat,
            r = rFloat,
            g = gFloat,
            b = bFloat,
            hasClippingHighlights = isClipped,
            hasCrushedShadows = isCrushed
        )
    }

    fun updateHorizon(angle: Float, isLevel: Boolean, guidance: String) {
        currentHorizonAngle = angle
        _aiState.value = _aiState.value.copy(
            horizon = com.example.data.model.HorizonLevel(
                angleDegrees = angle,
                isLevel = isLevel,
                guidanceText = guidance
            )
        )
    }
}
