package com.example.ai

import androidx.compose.ui.geometry.Rect
import com.example.data.model.AutoFramingRecommendation
import com.example.data.model.CameraLens
import com.example.data.model.SubjectDetection

class AutoFramingEngine {

    fun recommendFraming(
        subject: SubjectDetection,
        currentLens: CameraLens
    ): AutoFramingRecommendation {
        val box = subject.boundingBox
        val boxWidth = box.width
        val boxHeight = box.height
        val area = boxWidth * boxHeight

        return when {
            // Subject is very small in the frame (e.g. distant person or close portrait opportunity)
            area < 0.12f && subject.isPerson -> {
                val padX = (0.28f).coerceAtMost((1f - boxWidth) / 2f)
                val padY = (0.25f).coerceAtMost((1f - boxHeight) / 2f)
                val rect = Rect(
                    (box.left - padX).coerceIn(0.05f, 0.40f),
                    (box.top - padY).coerceIn(0.05f, 0.40f),
                    (box.right + padX).coerceIn(0.60f, 0.95f),
                    (box.bottom + padY).coerceIn(0.60f, 0.95f)
                )
                AutoFramingRecommendation(
                    recommendedLens = CameraLens.TELE_2X,
                    recommendedLensLabel = "2x",
                    cropRect = rect,
                    framingAdvice = "Try 2x for tighter portrait framing",
                    reason = "Isolates subject with natural compression"
                )
            }

            // Subject is wide / close (group, architecture or close-up)
            area > 0.45f -> {
                AutoFramingRecommendation(
                    recommendedLens = CameraLens.WIDE,
                    recommendedLensLabel = "1x",
                    cropRect = Rect(0.04f, 0.04f, 0.96f, 0.96f),
                    framingAdvice = "Subject fills frame nicely",
                    reason = "Environmental context preserved"
                )
            }

            // Normal environmental portrait / street scene
            else -> {
                val cx = subject.center.x
                val cy = subject.center.y
                val halfW = 0.42f
                val halfH = 0.45f
                val rect = Rect(
                    (cx - halfW).coerceIn(0.04f, 0.12f),
                    (cy - halfH).coerceIn(0.04f, 0.10f),
                    (cx + halfW).coerceIn(0.88f, 0.96f),
                    (cy + halfH).coerceIn(0.90f, 0.96f)
                )
                AutoFramingRecommendation(
                    recommendedLens = CameraLens.WIDE,
                    recommendedLensLabel = "1x",
                    cropRect = rect,
                    framingAdvice = "Balanced framing window",
                    reason = "Optimal headroom & side margins"
                )
            }
        }
    }
}
