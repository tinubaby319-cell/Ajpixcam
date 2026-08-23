package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class FilmFilterId {
    ORIGINAL,
    FLAT_LOG,    // Ultra-flat log profile for manual color grading (DaVinci/Lightroom/Studio)
    F_C200,      // All-Round
    F_PRONEG,    // Pure Soft
    DOKA_X400,   // B&W High Contrast
    PORTRA_400,  // Versatile Warmth
    GOLD_200,    // Retro Sunny
    VISTA_800,   // High Contrast Vivid
    CINE_TEAL,   // Cinematic Teal & Orange
    KODAK_EKTAR, // Ultra-fine High Saturation
    F_CCHROME,   // Urban Raw
    F_CNEG2,     // Retro Vibe
    F_S400,      // Moody Daily
    F_160C,      // High Saturation
    F_400H,      // J-Fresh Pastel
    PORTRA_160,  // Port & Land
    CUSTOM_RECIPE // User custom formula
}

data class FilmFilter(
    val id: FilmFilterId,
    val code: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val badgeColor: Color = Color(0xFFFFB703),
    val exposureBias: Float = 0.0f,
    val contrast: Float = 1.0f,      // 1.0 = normal, >1.0 = higher
    val saturation: Float = 1.0f,    // 1.0 = normal, 0.0 = b&w
    val temperatureOffset: Float = 0f, // >0 = warm, <0 = cool
    val tintOffset: Float = 0f,      // >0 = magenta, <0 = green
    val highlightsRolloff: Float = 0.0f, // positive compresses highlights
    val shadowsLift: Float = 0.0f,   // positive lifts shadows (faded)
    val grainStrength: Float = 0.0f, // 0.0 to 1.0
    val vignetteStrength: Float = 0.0f, // 0.0 to 1.0
    val halation: Float = 0.0f,      // soft bloom in warm highlights
    val isMonochrome: Boolean = false,
    val isFlatLog: Boolean = false
)
