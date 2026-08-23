package com.example.ai

import android.graphics.Bitmap
import com.example.data.model.SceneCategory
import kotlin.math.max
import kotlin.math.min

data class SceneAnalysisOutput(
    val category: SceneCategory,
    val confidence: Float,
    val estimatedKelvin: Int,
    val averageLuminance: Float, // 0..1
    val isLowLight: Boolean,
    val warmLightingRatio: Float
)

class SceneClassifier {

    /**
     * Real-time lightweight Computer Vision classification on sampled frame bitmap.
     */
    fun classifyFrame(bitmap: Bitmap): SceneAnalysisOutput {
        val width = bitmap.width
        val height = bitmap.height
        val step = max(1, width / 24) // fast subsampling

        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var totalLum = 0.0
        var skinPixels = 0
        var skyWaterPixels = 0
        var warmGoldenPixels = 0
        var foliagePixels = 0
        var darkPixels = 0
        var brightPixels = 0
        var sampleCount = 0

        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                totalR += r
                totalG += g
                totalB += b

                val lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                totalLum += lum
                sampleCount++

                if (lum < 0.2) darkPixels++
                if (lum > 0.8) brightPixels++

                // Skin tone heuristic in RGB space
                if (r > 95 && g > 40 && b > 20 && (max(r, max(g, b)) - min(r, min(g, b)) > 15) &&
                    Math.abs(r - g) > 15 && r > g && r > b) {
                    skinPixels++
                }

                // Foliage / Nature
                if (g > r * 1.15 && g > b * 1.15 && g > 50) {
                    foliagePixels++
                }

                // Sky / Ocean Blue
                if (b > r * 1.2 && b > g * 1.05 && b > 70) {
                    skyWaterPixels++
                }

                // Warm sunset / Golden hour
                if (r > 160 && g > 100 && b < 100 && r > g * 1.1) {
                    warmGoldenPixels++
                }
            }
        }

        if (sampleCount == 0) {
            return SceneAnalysisOutput(
                category = SceneCategory.GENERAL,
                confidence = 0.85f,
                estimatedKelvin = 5500,
                averageLuminance = 0.5f,
                isLowLight = false,
                warmLightingRatio = 0.3f
            )
        }

        val avgR = totalR.toDouble() / sampleCount
        val avgG = totalG.toDouble() / sampleCount
        val avgB = totalB.toDouble() / sampleCount
        val avgLum = (totalLum / sampleCount).toFloat()

        // Kelvin estimation from R/B ratio
        val rbRatio = if (avgB > 0) avgR / avgB else 1.0
        val estimatedKelvin = (5500.0 / (rbRatio.coerceIn(0.5, 2.5))).toInt().coerceIn(2800, 8500)

        val skinRatio = skinPixels.toFloat() / sampleCount
        val foliageRatio = foliagePixels.toFloat() / sampleCount
        val skyWaterRatio = skyWaterPixels.toFloat() / sampleCount
        val goldenRatio = warmGoldenPixels.toFloat() / sampleCount
        val darkRatio = darkPixels.toFloat() / sampleCount

        val isLowLight = avgLum < 0.28f || darkRatio > 0.55f

        val (category, confidence) = when {
            isLowLight -> SceneCategory.NIGHT to (0.85f + (darkRatio * 0.12f).coerceAtMost(0.12f))
            goldenRatio > 0.18f -> SceneCategory.SUNSET to (0.88f + goldenRatio.coerceAtMost(0.1f))
            skinRatio > 0.16f -> SceneCategory.PORTRAIT to (0.86f + skinRatio.coerceAtMost(0.12f))
            foliageRatio > 0.25f -> SceneCategory.LANDSCAPE to (0.87f + foliageRatio.coerceAtMost(0.1f))
            skyWaterRatio > 0.30f -> SceneCategory.BEACH to (0.85f + skyWaterRatio.coerceAtMost(0.1f))
            avgLum > 0.65f && skyWaterRatio > 0.15f -> SceneCategory.LANDSCAPE to 0.84f
            darkRatio > 0.35f && avgLum > 0.35f -> SceneCategory.CITY to 0.83f
            skinRatio > 0.08f -> SceneCategory.PORTRAIT to 0.80f
            else -> SceneCategory.GENERAL to 0.82f
        }

        return SceneAnalysisOutput(
            category = category,
            confidence = confidence.coerceIn(0.70f, 0.98f),
            estimatedKelvin = estimatedKelvin,
            averageLuminance = avgLum,
            isLowLight = isLowLight,
            warmLightingRatio = (goldenRatio * 2f).coerceIn(0f, 1f)
        )
    }
}
