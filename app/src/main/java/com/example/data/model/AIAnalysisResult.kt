package com.example.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.filter.FilmPresets

enum class SceneCategory(val label: String, val iconName: String) {
    GENERAL("General Scene", "camera"),
    PORTRAIT("Portrait & People", "person"),
    LANDSCAPE("Nature & Landscape", "landscape"),
    SUNSET("Golden Hour / Sunset", "wb_sunny"),
    CITY("City & Architecture", "apartment"),
    BEACH("Beach & Water", "water"),
    NIGHT("Low Light / Night", "nightlight"),
    FOOD("Food & Macro", "restaurant"),
    INDOOR("Moody Indoor", "home")
}

data class SubjectDetection(
    val boundingBox: Rect = Rect(0.25f, 0.25f, 0.75f, 0.75f), // Normalized 0..1
    val center: Offset = Offset(0.5f, 0.5f),
    val isPerson: Boolean = true,
    val faceDetected: Boolean = true,
    val confidence: Float = 0.88f
)

data class CompositionGuidance(
    val score: Int = 85, // 0..100
    val advice: String = "Great composition",
    val secondaryAdvice: String = "Subject balanced in upper-right golden node",
    val targetNode: Offset = Offset(0.66f, 0.33f), // Optimal rule-of-thirds point
    val currentPosition: Offset = Offset(0.58f, 0.38f),
    val vectorToTarget: Offset = Offset(0.08f, -0.05f),
    val isAligned: Boolean = false,
    val ruleOfThirdsScore: Int = 88,
    val negativeSpaceScore: Int = 82,
    val symmetryScore: Int = 74,
    val lightingScore: Int = 90
)

data class HorizonLevel(
    val angleDegrees: Float = 0.0f, // -45..+45
    val isLevel: Boolean = true,    // abs(angle) < 0.8 deg
    val guidanceText: String = "Level"
)

data class AutoFramingRecommendation(
    val recommendedLens: CameraLens = CameraLens.WIDE,
    val recommendedLensLabel: String = "1x",
    val cropRect: Rect = Rect(0.08f, 0.06f, 0.92f, 0.94f),
    val framingAdvice: String = "Ideal full composition",
    val reason: String = "Environmental portrait balanced"
)

data class FilterRecommendation(
    val recommendedFilter: FilmFilter = FilmPresets.F_C200,
    val reason: String = "Natural lighting — all-round balanced film simulation",
    val sceneLabel: String = "Daylight Scene",
    val confidence: Float = 0.92f
)

data class LiveAIState(
    val sceneCategory: SceneCategory = SceneCategory.GENERAL,
    val sceneConfidence: Float = 0.91f,
    val estimatedKelvin: Int = 5400,
    val brightnessLevel: Float = 0.65f, // 0..1
    val subject: SubjectDetection = SubjectDetection(),
    val composition: CompositionGuidance = CompositionGuidance(),
    val horizon: HorizonLevel = HorizonLevel(),
    val autoFraming: AutoFramingRecommendation = AutoFramingRecommendation(),
    val filterRecommendation: FilterRecommendation = FilterRecommendation(),
    val isProcessing: Boolean = false
)
