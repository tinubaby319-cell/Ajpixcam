package com.example.filter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.model.DoubleExposureBlendMode
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFilter
import com.example.data.model.FilmFrameStyle
import com.example.data.model.LightLeakStyle
import com.example.data.model.VintageLensStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object FilmSimulationEngine {

    /**
     * Applies a FilmFilter profile, vintage analog effects, dust & scratches, lens profiles, and borders to a Bitmap.
     */
    fun applyFilmProfile(
        source: Bitmap,
        filter: FilmFilter,
        intensityPercent: Int = 70,
        applyGrain: Boolean = true,
        applyHalation: Boolean = true,
        applyDustScratches: Boolean = false,
        dustStrength: Float = 0.20f,
        lensStyle: VintageLensStyle = VintageLensStyle.STANDARD,
        dateStampFormat: FilmDateStampFormat = FilmDateStampFormat.OFF,
        frameStyle: FilmFrameStyle = FilmFrameStyle.NONE,
        lightLeakStyle: LightLeakStyle = LightLeakStyle.NONE,
        dateTimestamp: Long = System.currentTimeMillis()
    ): Bitmap {
        val factor = (intensityPercent / 100f).coerceIn(0f, 1f)
        val srcW = source.width
        val srcH = source.height

        // 1. Process Color Grading & Tone Matrix on Core Image
        val coreBitmap = Bitmap.createBitmap(srcW, srcH, Bitmap.Config.ARGB_8888)
        val coreCanvas = Canvas(coreBitmap)

        if (filter.id == com.example.data.model.FilmFilterId.ORIGINAL && intensityPercent <= 0) {
            coreCanvas.drawBitmap(source, 0f, 0f, null)
        } else {
            val cm = buildFilmColorMatrix(filter, factor)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(cm)
            }
            coreCanvas.drawBitmap(source, 0f, 0f, paint)

            // Vintage Lens Effects
            when (lensStyle) {
                VintageLensStyle.DREAMY_DIFFUSION -> {
                    applyDreamyGlow(coreCanvas, srcW, srcH, 0.35f * factor)
                }
                VintageLensStyle.ANAMORPHIC_BLUE -> {
                    applyAnamorphicStreak(coreCanvas, srcW, srcH, 0.45f * factor)
                }
                VintageLensStyle.FISHEYE_8MM -> {
                    applyFisheyeVignette(coreCanvas, srcW, srcH)
                }
                VintageLensStyle.STANDARD -> {}
            }

            // Halation
            if (applyHalation && filter.halation > 0.05f && factor > 0.2f) {
                applyHalationEffect(coreCanvas, srcW, srcH, filter.halation * factor)
            }

            // Light Leak
            if (lightLeakStyle != LightLeakStyle.NONE) {
                applyLightLeak(coreCanvas, srcW, srcH, lightLeakStyle)
            }

            // Vignette
            if (filter.vignetteStrength > 0.05f && factor > 0.2f) {
                applyVignetteEffect(coreCanvas, srcW, srcH, filter.vignetteStrength * factor)
            }

            // Dust & Scratches
            if (applyDustScratches || dustStrength > 0.05f) {
                applyDustAndScratches(coreCanvas, srcW, srcH, dustStrength)
            }

            // Authentic Film Grain
            if (applyGrain && filter.grainStrength > 0.05f && factor > 0.2f) {
                applyGrainOverlay(coreCanvas, srcW, srcH, filter.grainStrength * factor)
            }
        }

        // 2. Apply Date Stamp
        if (dateStampFormat != FilmDateStampFormat.OFF) {
            applyDateStamp(coreCanvas, srcW, srcH, dateStampFormat, dateTimestamp)
        }

        // 3. Apply Frame Style if selected
        return if (frameStyle != FilmFrameStyle.NONE) {
            applyFrameBorder(coreBitmap, frameStyle, filter)
        } else {
            coreBitmap
        }
    }

    /**
     * Blends two photos together in double exposure mode with authentic analog blending formulas.
     */
    fun blendDoubleExposure(
        base: Bitmap,
        overlay: Bitmap,
        blendMode: DoubleExposureBlendMode = DoubleExposureBlendMode.SCREEN,
        overlayAlpha: Float = 0.65f
    ): Bitmap {
        val width = base.width
        val height = base.height

        // Scaled overlay to match base dimensions
        val scaledOverlay = if (overlay.width != width || overlay.height != height) {
            Bitmap.createScaledBitmap(overlay, width, height, true)
        } else {
            overlay
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // Draw base image
        canvas.drawBitmap(base, 0f, 0f, null)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (overlayAlpha * 255).toInt().coerceIn(0, 255)
            xfermode = when (blendMode) {
                DoubleExposureBlendMode.SCREEN -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                DoubleExposureBlendMode.LIGHTEN -> PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
                DoubleExposureBlendMode.OVERLAY -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
                DoubleExposureBlendMode.SOFT_LIGHT -> PorterDuffXfermode(PorterDuff.Mode.ADD)
                DoubleExposureBlendMode.MULTIPLY -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            }
        }

        canvas.drawBitmap(scaledOverlay, 0f, 0f, paint)
        return resultBitmap
    }

    private fun buildFilmColorMatrix(filter: FilmFilter, factor: Float): ColorMatrix {
        val finalMatrix = ColorMatrix()

        // Saturation adjustment
        val targetSat = 1.0f + (filter.saturation - 1.0f) * factor
        val satMatrix = ColorMatrix().apply { setSaturation(max(0f, targetSat)) }

        // Contrast & Exposure
        val contrast = 1.0f + (filter.contrast - 1.0f) * factor
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f + (filter.exposureBias * 30f * factor)

        // Temperature (Red/Blue bias) & Tint (Green/Magenta bias)
        val tempR = 1.0f + (filter.temperatureOffset * 0.4f * factor)
        val tempB = 1.0f - (filter.temperatureOffset * 0.4f * factor)
        val tintG = 1.0f - (filter.tintOffset * 0.3f * factor)
        val tintR = 1.0f + (filter.tintOffset * 0.15f * factor)
        val tintB = 1.0f + (filter.tintOffset * 0.15f * factor)

        // Highlights rolloff and shadows lift
        val shadowShift = filter.shadowsLift * 30f * factor
        val rTranslate = translate + shadowShift
        val gTranslate = translate + shadowShift
        val bTranslate = translate + shadowShift

        val toneMatrix = ColorMatrix(floatArrayOf(
            scale * tempR * tintR, 0f, 0f, 0f, rTranslate,
            0f, scale * tintG, 0f, 0f, gTranslate,
            0f, 0f, scale * tempB * tintB, 0f, bTranslate,
            0f, 0f, 0f, 1f, 0f
        ))

        finalMatrix.postConcat(satMatrix)
        finalMatrix.postConcat(toneMatrix)

        if (filter.isMonochrome) {
            val monoMatrix = ColorMatrix(floatArrayOf(
                0.299f, 0.587f, 0.114f, 0f, rTranslate,
                0.299f, 0.587f, 0.114f, 0f, gTranslate,
                0.299f, 0.587f, 0.114f, 0f, bTranslate,
                0f, 0f, 0f, 1f, 0f
            ))
            finalMatrix.postConcat(monoMatrix)
        }

        return finalMatrix
    }

    private fun applyDreamyGlow(canvas: Canvas, width: Int, height: Int, strength: Float) {
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb((strength * 70).toInt().coerceIn(0, 100), 255, 245, 235)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
    }

    private fun applyAnamorphicStreak(canvas: Canvas, width: Int, height: Int, strength: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }
        val streakY = height * 0.45f
        val streakH = height * 0.08f

        val gradient = LinearGradient(
            0f, streakY, width.toFloat(), streakY,
            intArrayOf(
                0x00000000,
                AndroidColor.argb((strength * 180).toInt().coerceIn(0, 200), 0, 180, 255),
                AndroidColor.argb((strength * 230).toInt().coerceIn(0, 255), 180, 230, 255),
                AndroidColor.argb((strength * 180).toInt().coerceIn(0, 200), 0, 180, 255),
                0x00000000
            ),
            floatArrayOf(0f, 0.35f, 0.50f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, streakY - streakH, width.toFloat(), streakY + streakH, paint)
    }

    private fun applyFisheyeVignette(canvas: Canvas, width: Int, height: Int) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * 0.62f

        val colors = intArrayOf(0x00000000, 0x00000000, 0x88000000.toInt(), 0xFF000000.toInt())
        val stops = floatArrayOf(0.0f, 0.70f, 0.90f, 1.0f)

        val gradient = RadialGradient(cx, cy, radius * 1.15f, colors, stops, Shader.TileMode.CLAMP)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun applyDustAndScratches(canvas: Canvas, width: Int, height: Int, strength: Float) {
        val dustPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb((strength * 120).toInt().coerceIn(20, 140), 245, 245, 240)
            strokeWidth = max(1f, width * 0.002f)
        }
        val scratchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb((strength * 90).toInt().coerceIn(15, 110), 255, 255, 255)
            strokeWidth = max(1f, width * 0.0015f)
            style = Paint.Style.STROKE
        }

        val random = Random(1337)
        // Vertical hair / emulsion scratches
        val numScratches = (4 * strength).toInt().coerceIn(1, 6)
        for (i in 0 until numScratches) {
            val startX = random.nextFloat() * width
            val startY = random.nextFloat() * (height * 0.4f)
            val endY = startY + random.nextFloat() * (height * 0.5f) + height * 0.1f
            val curveX = startX + (random.nextFloat() - 0.5f) * 20f

            canvas.drawLine(startX, startY, curveX, endY, scratchPaint)
        }

        // Dust specks
        val numDust = (80 * strength).toInt().coerceIn(10, 150)
        for (i in 0 until numDust) {
            val dx = random.nextFloat() * width
            val dy = random.nextFloat() * height
            val radius = random.nextFloat() * (width * 0.003f) + 0.8f
            canvas.drawCircle(dx, dy, radius, dustPaint)
        }
    }

    private fun applyVignetteEffect(canvas: Canvas, width: Int, height: Int, strength: Float) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = max(width, height) * 0.75f

        val alpha = (strength * 160f).toInt().coerceIn(0, 220)
        val colors = intArrayOf(0x00000000, 0x00000000, (alpha shl 24))
        val stops = floatArrayOf(0.0f, 0.55f, 1.0f)

        val gradient = RadialGradient(cx, cy, radius, colors, stops, Shader.TileMode.CLAMP)
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
    }

    private fun applyHalationEffect(canvas: Canvas, width: Int, height: Int, strength: Float) {
        val halationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb((strength * 35).toInt().coerceIn(0, 75), 255, 110, 60)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), halationPaint)
    }

    private fun applyLightLeak(canvas: Canvas, width: Int, height: Int, style: LightLeakStyle) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (style) {
            LightLeakStyle.AMBER_LEFT -> {
                val gradient = LinearGradient(
                    0f, 0f, width * 0.45f, height * 0.35f,
                    intArrayOf(
                        AndroidColor.argb(140, 255, 120, 30),
                        AndroidColor.argb(80, 255, 180, 70),
                        AndroidColor.argb(0, 255, 200, 100)
                    ),
                    floatArrayOf(0f, 0.4f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            LightLeakStyle.SUNSET_RIGHT -> {
                val gradient = LinearGradient(
                    width.toFloat(), height.toFloat(), width * 0.55f, height * 0.5f,
                    intArrayOf(
                        AndroidColor.argb(150, 230, 50, 40),
                        AndroidColor.argb(90, 255, 130, 40),
                        AndroidColor.argb(0, 255, 190, 80)
                    ),
                    floatArrayOf(0f, 0.45f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            LightLeakStyle.CORNER_BURN -> {
                val gradient = RadialGradient(
                    width * 0.95f, 0f, max(width, height) * 0.55f,
                    intArrayOf(
                        AndroidColor.argb(160, 255, 90, 30),
                        AndroidColor.argb(70, 255, 160, 60),
                        0x00000000
                    ),
                    floatArrayOf(0f, 0.5f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            LightLeakStyle.NONE -> {}
        }
    }

    private fun applyGrainOverlay(canvas: Canvas, width: Int, height: Int, strength: Float) {
        val grainPaint = Paint().apply {
            color = AndroidColor.WHITE
            strokeWidth = 1f
        }
        val numDots = (width * height * 0.0005f * strength).toInt().coerceIn(100, 3500)
        val random = Random(42)

        for (i in 0 until numDots) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val alpha = (random.nextFloat() * 55f * strength).toInt().coerceIn(10, 110)
            grainPaint.alpha = alpha
            canvas.drawPoint(x, y, grainPaint)
        }
    }

    private fun applyDateStamp(
        canvas: Canvas,
        width: Int,
        height: Int,
        format: FilmDateStampFormat,
        timestamp: Long
    ) {
        val date = Date(timestamp)
        val dateText = when (format) {
            FilmDateStampFormat.ORANGE_LCD -> SimpleDateFormat("''yy MM dd", Locale.US).format(date)
            FilmDateStampFormat.CLASSIC_WHITE -> SimpleDateFormat("yyyy.MM.dd", Locale.US).format(date)
            FilmDateStampFormat.RETRO_RED -> SimpleDateFormat("''yy  M  d", Locale.US).format(date)
            FilmDateStampFormat.OFF -> return
        }

        val fontSize = max(18f, width * 0.038f)
        val margin = width * 0.05f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = fontSize
            typeface = Typeface.MONOSPACE
            isFakeBoldText = true
            when (format) {
                FilmDateStampFormat.ORANGE_LCD -> {
                    color = AndroidColor.argb(230, 255, 140, 0)
                    setShadowLayer(fontSize * 0.25f, 0f, 0f, AndroidColor.argb(180, 255, 90, 0))
                }
                FilmDateStampFormat.RETRO_RED -> {
                    color = AndroidColor.argb(230, 240, 50, 40)
                    setShadowLayer(fontSize * 0.25f, 0f, 0f, AndroidColor.argb(180, 180, 20, 20))
                }
                FilmDateStampFormat.CLASSIC_WHITE -> {
                    color = AndroidColor.argb(220, 255, 255, 255)
                    setShadowLayer(fontSize * 0.2f, 0f, 0f, AndroidColor.argb(180, 0, 0, 0))
                }
                FilmDateStampFormat.OFF -> {}
            }
        }

        val textWidth = paint.measureText(dateText)
        val x = width - textWidth - margin
        val y = height - margin

        canvas.drawText(dateText, x, y, paint)
    }

    private fun applyFrameBorder(
        source: Bitmap,
        frameStyle: FilmFrameStyle,
        filter: FilmFilter
    ): Bitmap {
        val srcW = source.width
        val srcH = source.height

        return when (frameStyle) {
            FilmFrameStyle.POLAROID -> {
                val sideMargin = (srcW * 0.06f).toInt()
                val topMargin = (srcW * 0.06f).toInt()
                val bottomMargin = (srcH * 0.20f).toInt()

                val framedW = srcW + sideMargin * 2
                val framedH = srcH + topMargin + bottomMargin

                val framedBitmap = Bitmap.createBitmap(framedW, framedH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(framedBitmap)

                // White paper background with slight warm tint
                canvas.drawColor(AndroidColor.rgb(250, 249, 245))

                // Inner photo
                canvas.drawBitmap(source, sideMargin.toFloat(), topMargin.toFloat(), null)

                // Subtle inner shadow border around photo
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(30, 0, 0, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRect(
                    sideMargin.toFloat(),
                    topMargin.toFloat(),
                    (sideMargin + srcW).toFloat(),
                    (topMargin + srcH).toFloat(),
                    borderPaint
                )

                // Handwritten-style caption at bottom
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(80, 80, 80)
                    textSize = framedW * 0.038f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                }
                val caption = "AuraCam • ${filter.name} film"
                val textX = framedW * 0.08f
                val textY = topMargin + srcH + bottomMargin * 0.55f
                canvas.drawText(caption, textX, textY, textPaint)

                framedBitmap
            }

            FilmFrameStyle.FILM_35MM -> {
                val borderX = (srcW * 0.08f).toInt()
                val borderY = (srcH * 0.08f).toInt()
                val framedW = srcW + borderX * 2
                val framedH = srcH + borderY * 2

                val framedBitmap = Bitmap.createBitmap(framedW, framedH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(framedBitmap)

                // Black film base
                canvas.drawColor(AndroidColor.rgb(18, 18, 18))
                canvas.drawBitmap(source, borderX.toFloat(), borderY.toFloat(), null)

                // Film sprocket markings text
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(255, 180, 30)
                    textSize = borderY * 0.38f
                    typeface = Typeface.MONOSPACE
                    isFakeBoldText = true
                }
                val frameNum = "▶ 36A  KODAK ${filter.code.uppercase()}  SAFETY FILM"
                canvas.drawText(frameNum, borderX * 0.8f, borderY * 0.65f, textPaint)
                canvas.drawText(frameNum, borderX * 0.8f, (framedH - borderY * 0.35f), textPaint)

                framedBitmap
            }

            FilmFrameStyle.HASSELBLAD -> {
                val border = (srcW * 0.07f).toInt()
                val framedW = srcW + border * 2
                val framedH = srcH + border * 2

                val framedBitmap = Bitmap.createBitmap(framedW, framedH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(framedBitmap)

                // Pure matte black
                canvas.drawColor(AndroidColor.rgb(12, 12, 12))
                canvas.drawBitmap(source, border.toFloat(), border.toFloat(), null)

                // Medium format film rebate notches (left edge)
                val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(250, 250, 250)
                }
                val notchW = border * 0.35f
                val notchH = border * 0.18f
                val notchY = framedH * 0.48f
                canvas.drawRect(border.toFloat(), notchY, border + notchW, notchY + notchH, notchPaint)

                framedBitmap
            }

            FilmFrameStyle.EXIF_IMPRINT -> {
                val bannerH = (srcH * 0.065f).toInt()
                val framedW = srcW
                val framedH = srcH + bannerH

                val framedBitmap = Bitmap.createBitmap(framedW, framedH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(framedBitmap)

                canvas.drawBitmap(source, 0f, 0f, null)

                // Dark linen banner
                val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(24, 24, 20)
                }
                canvas.drawRect(0f, srcH.toFloat(), framedW.toFloat(), framedH.toFloat(), bannerPaint)

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(220, 220, 205)
                    textSize = bannerH * 0.42f
                    typeface = Typeface.MONOSPACE
                }
                val exifInfo = "AURACAM • ${filter.code} • 26MM F/1.8 • ISO 100 • 1/250S"
                canvas.drawText(exifInfo, framedW * 0.04f, srcH + bannerH * 0.65f, textPaint)

                framedBitmap
            }

            FilmFrameStyle.NONE -> source
        }
    }
}
