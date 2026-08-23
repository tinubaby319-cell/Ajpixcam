package com.example.filter

import androidx.compose.ui.graphics.Color
import com.example.data.model.CustomFilmRecipe
import com.example.data.model.FilmFilter
import com.example.data.model.FilmFilterId

object FilmPresets {
    val ORIGINAL = FilmFilter(
        id = FilmFilterId.ORIGINAL,
        code = "RAW",
        name = "Original",
        subtitle = "Unprocessed Master",
        description = "Pure sensor capture without color grading, maximum pixel fidelity",
        badgeColor = Color(0xFF9E9E9E),
        contrast = 1.0f,
        saturation = 1.0f
    )

    val FLAT_LOG = FilmFilter(
        id = FilmFilterId.FLAT_LOG,
        code = "C-LOG",
        name = "Flat Log",
        subtitle = "Master Grading Profile",
        description = "Ultra-wide dynamic range flat logarithmic profile (lifted shadows +35%, roll-off highlights -40%) designed for professional color grading in Lightroom, DaVinci Resolve or AuraCam Studio",
        badgeColor = Color(0xFF00B4D8),
        contrast = 0.76f,
        saturation = 0.70f,
        temperatureOffset = 0.0f,
        tintOffset = 0.0f,
        highlightsRolloff = 0.45f,
        shadowsLift = 0.38f,
        grainStrength = 0.0f,
        vignetteStrength = 0.0f,
        isFlatLog = true
    )

    val F_C200 = FilmFilter(
        id = FilmFilterId.F_C200,
        code = "F C200",
        name = "All-Round",
        subtitle = "Everyday Classic",
        description = "Balanced natural color, gentle highlight warmth and subtle organic grain",
        badgeColor = Color(0xFF00B4D8),
        contrast = 1.08f,
        saturation = 1.06f,
        temperatureOffset = 0.08f,
        tintOffset = -0.02f,
        highlightsRolloff = 0.15f,
        shadowsLift = 0.05f,
        grainStrength = 0.20f,
        vignetteStrength = 0.12f
    )

    val PORTRA_400 = FilmFilter(
        id = FilmFilterId.PORTRA_400,
        code = "Portra 400",
        name = "Versatile",
        subtitle = "Warm Natural",
        description = "Legendary warm highlights, organic skin tones, rich mid-tones and creamy contrast",
        badgeColor = Color(0xFFFFB703),
        contrast = 1.05f,
        saturation = 1.04f,
        temperatureOffset = 0.14f,
        tintOffset = 0.02f,
        highlightsRolloff = 0.25f,
        shadowsLift = 0.08f,
        grainStrength = 0.22f,
        vignetteStrength = 0.15f,
        halation = 0.15f
    )

    val GOLD_200 = FilmFilter(
        id = FilmFilterId.GOLD_200,
        code = "Gold 200",
        name = "Retro Sunny",
        subtitle = "Golden Sunlight",
        description = "Nostalgic golden sunlight hue, amber bias, warm vintage holiday feeling",
        badgeColor = Color(0xFFFB8500),
        contrast = 1.12f,
        saturation = 1.15f,
        temperatureOffset = 0.22f,
        tintOffset = -0.04f,
        highlightsRolloff = 0.18f,
        shadowsLift = 0.06f,
        grainStrength = 0.25f,
        vignetteStrength = 0.20f
    )

    val CINE_TEAL = FilmFilter(
        id = FilmFilterId.CINE_TEAL,
        code = "Cine Teal",
        name = "Teal & Orange",
        subtitle = "Hollywood Look",
        description = "Deep atmospheric teal shadows paired with rich golden amber skin tones",
        badgeColor = Color(0xFF0077B6),
        contrast = 1.22f,
        saturation = 1.12f,
        temperatureOffset = 0.12f,
        tintOffset = -0.10f,
        highlightsRolloff = 0.20f,
        shadowsLift = -0.02f,
        grainStrength = 0.18f,
        vignetteStrength = 0.18f,
        halation = 0.12f
    )

    val F_PRONEG = FilmFilter(
        id = FilmFilterId.F_PRONEG,
        code = "F ProNeg",
        name = "Pure Soft",
        subtitle = "Portrait Soft",
        description = "Soft portrait rendering, delicate creamy skin tones with gentle shadow rolloff",
        badgeColor = Color(0xFFFF85A1),
        contrast = 0.94f,
        saturation = 0.96f,
        temperatureOffset = 0.05f,
        tintOffset = 0.04f,
        highlightsRolloff = 0.30f,
        shadowsLift = 0.12f,
        grainStrength = 0.15f,
        vignetteStrength = 0.08f,
        halation = 0.18f
    )

    val DOKA_X400 = FilmFilter(
        id = FilmFilterId.DOKA_X400,
        code = "Doka X400",
        name = "B&W",
        subtitle = "Monochrome",
        description = "Rich deep blacks, authentic silver halide grain, classic street documentary aesthetic",
        badgeColor = Color(0xFF495057),
        contrast = 1.28f,
        saturation = 0.0f,
        temperatureOffset = 0.0f,
        tintOffset = 0.0f,
        highlightsRolloff = 0.10f,
        shadowsLift = -0.05f,
        grainStrength = 0.40f,
        vignetteStrength = 0.22f,
        isMonochrome = true
    )

    val VISTA_800 = FilmFilter(
        id = FilmFilterId.VISTA_800,
        code = "Vista 800",
        name = "High Contrast",
        subtitle = "Vibrant Night",
        description = "Punchy saturated colors, vivid reds and deep shadows for urban night photography",
        badgeColor = Color(0xFFE63946),
        contrast = 1.30f,
        saturation = 1.25f,
        temperatureOffset = 0.06f,
        tintOffset = 0.06f,
        highlightsRolloff = 0.08f,
        shadowsLift = -0.08f,
        grainStrength = 0.35f,
        vignetteStrength = 0.25f
    )

    val KODAK_EKTAR = FilmFilter(
        id = FilmFilterId.KODAK_EKTAR,
        code = "Ektar 100",
        name = "Fine Vivid",
        subtitle = "Ultra Sharp",
        description = "Ultra-fine grain with high saturation and brilliant clarity for architecture and nature",
        badgeColor = Color(0xFFD90429),
        contrast = 1.24f,
        saturation = 1.32f,
        temperatureOffset = 0.04f,
        tintOffset = -0.02f,
        highlightsRolloff = 0.12f,
        shadowsLift = 0.00f,
        grainStrength = 0.10f,
        vignetteStrength = 0.14f
    )

    val F_CCHROME = FilmFilter(
        id = FilmFilterId.F_CCHROME,
        code = "F C-Chrome",
        name = "Urban Raw",
        subtitle = "Documentary",
        description = "Muted saturation with deep tonal contrast and slightly cool architectural shadows",
        badgeColor = Color(0xFF2A9D8F),
        contrast = 1.18f,
        saturation = 0.85f,
        temperatureOffset = -0.08f,
        tintOffset = -0.02f,
        highlightsRolloff = 0.20f,
        shadowsLift = -0.04f,
        grainStrength = 0.22f,
        vignetteStrength = 0.18f
    )

    val F_CNEG2 = FilmFilter(
        id = FilmFilterId.F_CNEG2,
        code = "F CNeg-2",
        name = "Retro Vibe",
        subtitle = "Nostalgic Soft",
        description = "Faded pastel shadows, muted highlights, charming 1990s analog photo look",
        badgeColor = Color(0xFFB5838D),
        contrast = 0.90f,
        saturation = 0.88f,
        temperatureOffset = 0.10f,
        tintOffset = 0.08f,
        highlightsRolloff = 0.35f,
        shadowsLift = 0.22f,
        grainStrength = 0.28f,
        vignetteStrength = 0.14f
    )

    val F_S400 = FilmFilter(
        id = FilmFilterId.F_S400,
        code = "F S400",
        name = "Moody Daily",
        subtitle = "Cinematic Tone",
        description = "Slightly desaturated everyday tones with cinematic shadow roll-off and moody vibe",
        badgeColor = Color(0xFF6D6875),
        contrast = 1.14f,
        saturation = 0.90f,
        temperatureOffset = -0.04f,
        tintOffset = 0.02f,
        highlightsRolloff = 0.22f,
        shadowsLift = 0.04f,
        grainStrength = 0.24f,
        vignetteStrength = 0.16f
    )

    val F_160C = FilmFilter(
        id = FilmFilterId.F_160C,
        code = "F 160C",
        name = "High Sat",
        subtitle = "Vivid Landscape",
        description = "Vibrant saturated primaries, intense blues and greens with clean brilliant highlights",
        badgeColor = Color(0xFF06D6A0),
        contrast = 1.16f,
        saturation = 1.30f,
        temperatureOffset = 0.02f,
        tintOffset = -0.03f,
        highlightsRolloff = 0.12f,
        shadowsLift = 0.02f,
        grainStrength = 0.12f,
        vignetteStrength = 0.10f
    )

    val F_400H = FilmFilter(
        id = FilmFilterId.F_400H,
        code = "F 400H",
        name = "J-Fresh",
        subtitle = "Pastel Airy",
        description = "Clean, fresh, slightly cool highlights, soft mint shadows and gentle glowing skin",
        badgeColor = Color(0xFF48CAE4),
        contrast = 0.96f,
        saturation = 0.92f,
        temperatureOffset = -0.06f,
        tintOffset = -0.06f,
        highlightsRolloff = 0.32f,
        shadowsLift = 0.15f,
        grainStrength = 0.16f,
        vignetteStrength = 0.08f,
        halation = 0.20f
    )

    val PORTRA_160 = FilmFilter(
        id = FilmFilterId.PORTRA_160,
        code = "Portra 160",
        name = "Port & Land",
        subtitle = "Fine Grain",
        description = "Ultra-smooth fine grain, pastel highlight rendering and organic natural skin tone",
        badgeColor = Color(0xFFE7C6FF),
        contrast = 1.02f,
        saturation = 0.98f,
        temperatureOffset = 0.06f,
        tintOffset = 0.01f,
        highlightsRolloff = 0.28f,
        shadowsLift = 0.10f,
        grainStrength = 0.10f,
        vignetteStrength = 0.08f
    )

    val ALL_PRESETS = listOf(
        ORIGINAL,
        FLAT_LOG,
        F_C200,
        PORTRA_400,
        GOLD_200,
        CINE_TEAL,
        F_PRONEG,
        DOKA_X400,
        VISTA_800,
        KODAK_EKTAR,
        F_CCHROME,
        F_CNEG2,
        F_S400,
        F_160C,
        F_400H,
        PORTRA_160
    )

    fun getById(id: FilmFilterId): FilmFilter {
        return ALL_PRESETS.find { it.id == id } ?: F_C200
    }

    fun fromRecipe(recipe: CustomFilmRecipe): FilmFilter {
        val base = getById(recipe.baseFilterId)
        return FilmFilter(
            id = FilmFilterId.CUSTOM_RECIPE,
            code = recipe.name.take(8).uppercase(),
            name = recipe.name,
            subtitle = "Custom Recipe",
            description = "Customized analog film recipe with user color grading",
            badgeColor = Color(0xFFE07A5F),
            contrast = (base.contrast + (recipe.contrast - 1f)).coerceIn(0.6f, 1.6f),
            saturation = (base.saturation + (recipe.saturation - 1f)).coerceIn(0f, 2.0f),
            temperatureOffset = (base.temperatureOffset + recipe.warmth).coerceIn(-0.5f, 0.5f),
            tintOffset = (base.tintOffset + recipe.tint).coerceIn(-0.5f, 0.5f),
            grainStrength = recipe.grain.coerceIn(0f, 1f),
            halation = recipe.halation.coerceIn(0f, 1f),
            vignetteStrength = recipe.vignette.coerceIn(0f, 1f)
        )
    }
}
