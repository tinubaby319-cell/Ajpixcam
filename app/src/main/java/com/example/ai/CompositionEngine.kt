package com.example.ai

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.data.model.CompositionGuidance
import com.example.data.model.SubjectDetection
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class CompositionEngine {

    // 4 Rule-of-Thirds Golden Nodes (normalized coordinates)
    private val goldenNodes = listOf(
        Offset(0.333f, 0.333f), // Top-Left
        Offset(0.666f, 0.333f), // Top-Right
        Offset(0.333f, 0.666f), // Bottom-Left
        Offset(0.666f, 0.666f)  // Bottom-Right
    )

    private val centerNode = Offset(0.500f, 0.500f)

    /**
     * Analyzes image frame for salient subject and golden composition balance.
     */
    fun analyzeComposition(
        bitmap: Bitmap,
        isPortraitMode: Boolean = false,
        horizonAngle: Float = 0.0f
    ): Pair<SubjectDetection, CompositionGuidance> {
        val width = bitmap.width
        val height = bitmap.height
        val step = max(1, width / 32)

        var totalWeight = 0.0
        var weightedX = 0.0
        var weightedY = 0.0
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var skinPixels = 0

        // Salience calculation based on local contrast & center-bias
        for (y in step until height - step step step) {
            for (x in step until width - step step step) {
                val centerPixel = bitmap.getPixel(x, y)
                val rightPixel = bitmap.getPixel(x + step, y)
                val bottomPixel = bitmap.getPixel(x, y + step)

                val cLum = getLuminance(centerPixel)
                val rLum = getLuminance(rightPixel)
                val bLum = getLuminance(bottomPixel)

                // Edge gradient strength
                val gradient = abs(cLum - rLum) + abs(cLum - bLum)

                // Check skin
                val cr = (centerPixel shr 16) and 0xFF
                val cg = (centerPixel shr 8) and 0xFF
                val cb = centerPixel and 0xFF
                val isSkin = (cr > 95 && cg > 40 && cb > 20 && cr > cg && cr > cb)
                if (isSkin) skinPixels++

                val skinMultiplier = if (isSkin) 2.2 else 1.0
                val weight = gradient * skinMultiplier

                if (weight > 0.15) {
                    totalWeight += weight
                    weightedX += x * weight
                    weightedY += y * weight

                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        val subjectCenter: Offset
        val subjectBox: Rect

        if (totalWeight > 0.5 && maxX > minX && maxY > minY) {
            val cx = (weightedX / totalWeight).toFloat() / width
            val cy = (weightedY / totalWeight).toFloat() / height
            subjectCenter = Offset(cx.coerceIn(0.1f, 0.9f), cy.coerceIn(0.1f, 0.9f))

            val normMinX = (minX.toFloat() / width).coerceIn(0.05f, 0.85f)
            val normMaxX = (maxX.toFloat() / width).coerceIn(normMinX + 0.1f, 0.95f)
            val normMinY = (minY.toFloat() / height).coerceIn(0.05f, 0.85f)
            val normMaxY = (maxY.toFloat() / height).coerceIn(normMinY + 0.1f, 0.95f)

            subjectBox = Rect(normMinX, normMinY, normMaxX, normMaxY)
        } else {
            subjectCenter = Offset(0.5f, 0.45f)
            subjectBox = Rect(0.25f, 0.20f, 0.75f, 0.75f)
        }

        val subject = SubjectDetection(
            boundingBox = subjectBox,
            center = subjectCenter,
            isPerson = skinPixels > 25,
            faceDetected = skinPixels > 15,
            confidence = (0.80f + (skinPixels.toFloat() / 200f)).coerceIn(0.75f, 0.96f)
        )

        // Find closest optimal target node (Rule of thirds golden points or Golden Center)
        var closestNode = goldenNodes[0]
        var minDistance = Float.MAX_VALUE

        for (node in goldenNodes) {
            val dist = distance(subjectCenter, node)
            if (dist < minDistance) {
                minDistance = dist
                closestNode = node
            }
        }

        // For tight portraits or centered symmetry, evaluate center node as well
        val centerDist = distance(subjectCenter, centerNode)
        val targetNode = if (centerDist < 0.10f && !isPortraitMode) {
            centerNode
        } else {
            closestNode
        }

        val effectiveDist = distance(subjectCenter, targetNode)
        val isAligned = effectiveDist < 0.065f

        // Calculate scores
        val ruleOfThirdsScore = ((1.0f - (effectiveDist / 0.45f).coerceIn(0f, 1f)) * 100).toInt()
        val horizonPenalty = (abs(horizonAngle) * 4f).toInt().coerceIn(0, 30)
        val negativeSpaceScore = if (subjectCenter.y < 0.70f) 88 else 60
        val symmetryScore = ((1.0f - abs(subjectCenter.x - 0.5f) * 2f).coerceIn(0f, 1f) * 100).toInt()
        val lightingScore = 90

        val rawScore = (ruleOfThirdsScore * 0.50f + negativeSpaceScore * 0.30f + lightingScore * 0.20f).toInt()
        val finalScore = (rawScore - horizonPenalty).coerceIn(35, 98)

        // Generate Smart AR Guidance Text
        val (advice, secondary) = generateGuidance(subjectCenter, targetNode, isAligned, finalScore, horizonAngle)

        val guidance = CompositionGuidance(
            score = finalScore,
            advice = advice,
            secondaryAdvice = secondary,
            targetNode = targetNode,
            currentPosition = subjectCenter,
            vectorToTarget = Offset(targetNode.x - subjectCenter.x, targetNode.y - subjectCenter.y),
            isAligned = isAligned,
            ruleOfThirdsScore = ruleOfThirdsScore,
            negativeSpaceScore = negativeSpaceScore,
            symmetryScore = symmetryScore,
            lightingScore = lightingScore
        )

        return Pair(subject, guidance)
    }

    private fun generateGuidance(
        current: Offset,
        target: Offset,
        isAligned: Boolean,
        score: Int,
        horizonAngle: Float
    ): Pair<String, String> {
        if (abs(horizonAngle) > 2.5f) {
            val dir = if (horizonAngle > 0) "Tilt phone left" else "Tilt phone right"
            return Pair("Straighten horizon", dir)
        }

        if (isAligned || score >= 88) {
            return Pair("Great composition", "Subject placed on golden intersection")
        }

        val dx = target.x - current.x
        val dy = target.y - current.y

        val primaryAdvice = when {
            dx > 0.08f -> "Move slightly right"
            dx < -0.08f -> "Move slightly left"
            dy > 0.08f -> "Lower camera slightly"
            dy < -0.08f -> "Raise camera slightly"
            current.y > 0.75f -> "Leave more headroom"
            else -> "Aim at the glowing ring"
        }

        val secondaryAdvice = "Align subject with the composition target"

        return Pair(primaryAdvice, secondaryAdvice)
    }

    private fun getLuminance(pixel: Int): Double {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    }

    private fun distance(p1: Offset, p2: Offset): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }
}
