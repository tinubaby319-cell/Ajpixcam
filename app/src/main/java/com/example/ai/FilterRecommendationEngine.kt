package com.example.ai

import com.example.data.model.FilterRecommendation
import com.example.data.model.SceneCategory
import com.example.filter.FilmPresets

class FilterRecommendationEngine {

    fun recommend(analysis: SceneAnalysisOutput): FilterRecommendation {
        return when (analysis.category) {
            SceneCategory.PORTRAIT -> {
                if (analysis.warmLightingRatio > 0.4f) {
                    FilterRecommendation(
                        recommendedFilter = FilmPresets.PORTRA_400,
                        reason = "Warm ambient portrait — Portra 400 for natural glowing skin",
                        sceneLabel = "Warm Portrait",
                        confidence = 0.94f
                    )
                } else {
                    FilterRecommendation(
                        recommendedFilter = FilmPresets.F_PRONEG,
                        reason = "Soft daylight portrait — F ProNeg for gentle pastel tones",
                        sceneLabel = "Natural Portrait",
                        confidence = 0.92f
                    )
                }
            }

            SceneCategory.SUNSET -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.GOLD_200,
                    reason = "Golden hour detected — Gold 200 enhances warm amber sunlight",
                    sceneLabel = "Golden Hour",
                    confidence = 0.96f
                )
            }

            SceneCategory.LANDSCAPE -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.F_160C,
                    reason = "Vibrant outdoor scenery — F 160C for rich greens and deep skies",
                    sceneLabel = "Nature & Landscape",
                    confidence = 0.90f
                )
            }

            SceneCategory.BEACH -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.F_400H,
                    reason = "Airy beach scene — F 400H for crisp pastel highlights & cool cyan water",
                    sceneLabel = "Beach & Ocean",
                    confidence = 0.91f
                )
            }

            SceneCategory.CITY -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.F_CCHROME,
                    reason = "Urban lines & architecture — F C-Chrome for raw documentary tone",
                    sceneLabel = "City & Street",
                    confidence = 0.88f
                )
            }

            SceneCategory.NIGHT -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.VISTA_800,
                    reason = "Low light environment — Vista 800 for high-contrast punchy color",
                    sceneLabel = "Night & Low Light",
                    confidence = 0.89f
                )
            }

            SceneCategory.INDOOR -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.F_CNEG2,
                    reason = "Moody indoor ambiance — F CNeg-2 for nostalgic soft shadow fade",
                    sceneLabel = "Moody Indoor",
                    confidence = 0.87f
                )
            }

            SceneCategory.FOOD -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.PORTRA_160,
                    reason = "Rich details — Portra 160 for true color fidelity & fine grain",
                    sceneLabel = "Food & Detail",
                    confidence = 0.85f
                )
            }

            SceneCategory.GENERAL -> {
                FilterRecommendation(
                    recommendedFilter = FilmPresets.F_C200,
                    reason = "Balanced daylight — F C200 for timeless everyday analog look",
                    sceneLabel = "Everyday Scene",
                    confidence = 0.90f
                )
            }
        }
    }
}
