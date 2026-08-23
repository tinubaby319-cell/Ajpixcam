package com.example.data.model

enum class CameraMode(val label: String, val description: String, val isVideo: Boolean = false) {
    PHOTO("PHOTO", "Standard computational capture"),
    DOUBLE_EXPOSURE("DOUBLE EXP", "2-shot multi-exposure analog overlay"),
    CINE_LOG("CINE-LOG", "Flat log video recording for post color grading", isVideo = true),
    FILM_VIDEO("FILM VIDEO", "Live analog film graded video recording", isVideo = true),
    AI_COMPOSE("AI COMPOSE", "Real-time intelligent framing & composition guide"),
    AUTO_FRAME("AUTO FRAME", "Smart crop & focal length recommendation"),
    POSE_COACH("AI POSE", "Silhouette pose assistant for portraits & couples"),
    BURST("BURST", "High-speed multi-frame capture"),
    HANDS_FREE("HANDS FREE", "Voice & palm gesture automated shutter"),
    PORTRAIT("PORTRAIT", "Depth perception & soft bokeh rendering"),
    NIGHT("NIGHT", "Enhanced low-light dynamic range stack"),
    PRO("PRO", "Full manual exposure, ISO & white balance")
}

enum class CameraLens(val label: String, val zoomFactor: Float) {
    ULTRA_WIDE("0.5x", 0.5f),
    WIDE("1x", 1.0f),
    TELE_2X("2x", 2.0f),
    TELE_3X("3x", 3.0f)
}

enum class AspectRatioMode(val label: String, val ratio: Float, val w: Int, val h: Int) {
    RATIO_4_3("3:4", 3f / 4f, 3, 4),
    RATIO_16_9("9:16", 9f / 16f, 9, 16),
    RATIO_1_1("1:1", 1f, 1, 1),
    RATIO_FULL("FULL", 0f, 0, 0)
}

enum class FlashMode(val label: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class TimerMode(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_10(10, "10s")
}

enum class WhiteBalancePreset(val label: String, val kelvin: Int) {
    AUTO("Auto", 0),
    DAYLIGHT("Daylight", 5500),
    SHADE("Shade", 6500),
    CLOUDY("Cloudy", 6000),
    INCANDESCENT("Tungsten", 3200),
    FLUORESCENT("Fluorescent", 4000)
}

enum class GridType(val label: String, val description: String) {
    RULE_OF_THIRDS("3x3 Grid", "Rule of thirds golden intersections"),
    GOLDEN_RATIO("Golden Spiral", "Fibonacci spiral composition"),
    TRIANGLES("Triangles", "Dynamic diagonal triangle division"),
    CENTER_SYMMETRY("Center Cross", "Symmetric central focus & crosshair"),
    NONE("Off", "Clean viewfinder without guidelines")
}

enum class FilmDateStampFormat(val label: String, val previewText: String) {
    OFF("Off", "No date stamp"),
    ORANGE_LCD("Amber '26 08 23", "'26 08 23"),
    CLASSIC_WHITE("White 2026.08.23", "2026.08.23"),
    RETRO_RED("Red '26 08 23", "'26 08 23")
}

enum class FilmFrameStyle(val label: String, val description: String) {
    NONE("None", "Edge-to-edge photo"),
    POLAROID("Polaroid", "Classic white polaroid border with bottom margin"),
    FILM_35MM("35mm Sprocket", "Analog roll borders with sprocket holes & frame numbers"),
    HASSELBLAD("Hasselblad 6x6", "Medium format black borders with film rebate notch"),
    EXIF_IMPRINT("Minimal EXIF", "Subtle technical metadata strip at bottom")
}

enum class LightLeakStyle(val label: String) {
    NONE("Off"),
    AMBER_LEFT("Amber Flare Left"),
    SUNSET_RIGHT("Sunset Red Right"),
    CORNER_BURN("Vintage Corner Burn")
}

enum class DoubleExposureBlendMode(val label: String, val description: String) {
    SCREEN("Screen", "Classic analog additive exposure"),
    LIGHTEN("Lighten", "Takes brighter pixels from each frame"),
    OVERLAY("Overlay", "Preserves base contrast while adding highlights"),
    SOFT_LIGHT("Soft Light", "Subtle dreamlike double exposure blend"),
    MULTIPLY("Multiply", "Darkens overlap for silhouette cutout art")
}

enum class ShutterSoundProfile(val label: String, val description: String) {
    LEICA_M("Leica M3 Mechanical", "Crisp rangefinder dual-curtain click"),
    VINTAGE_SLR("35mm SLR & Crank", "Heavy mirror slap & film winding crank"),
    POLAROID_MOTOR("Polaroid Motor", "Motorized gear whirr & photo ejection"),
    SILENT("Mute (Silent)", "Haptics only, no audio")
}

enum class VintageLensStyle(val label: String) {
    STANDARD("Standard Prime"),
    FISHEYE_8MM("Fisheye 8mm"),
    ANAMORPHIC_BLUE("Anamorphic Flare"),
    DREAMY_DIFFUSION("Pro-Mist Bloom")
}

enum class ContactSheetLayout(val label: String, val cols: Int, val rows: Int, val maxPhotos: Int, val aspectRatio: Float) {
    GRID_2X2("2x2 Square (1:1)", 2, 2, 4, 1.0f),
    GRID_3X2("3x2 Classic (3:4)", 2, 3, 6, 3f / 4f),
    GRID_3X3("3x3 Contact (1:1)", 3, 3, 9, 1.0f),
    STORY_4X2("4x2 Story (9:16)", 2, 4, 8, 9f / 16f)
}

enum class PoseCategory(val label: String, val description: String, val iconName: String) {
    PORTRAIT("Portrait Headshot", "Natural head-and-shoulders crop & angle", "person"),
    STANDING("Casual Standing", "Relaxed 3/4 standing posture with weight shift", "accessibility"),
    SITTING("Café & Chair", "Casual seated pose leaning forward", "chair"),
    COUPLE("Couple / Duo", "Two person framing with natural closeness", "group"),
    FASHION("Model Walk", "Dynamic street style stride with movement", "directions_walk")
}

data class HistogramData(
    val lum: FloatArray = FloatArray(32),
    val r: FloatArray = FloatArray(32),
    val g: FloatArray = FloatArray(32),
    val b: FloatArray = FloatArray(32),
    val hasClippingHighlights: Boolean = false,
    val hasCrushedShadows: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HistogramData
        return lum.contentEquals(other.lum) &&
                r.contentEquals(other.r) &&
                g.contentEquals(other.g) &&
                b.contentEquals(other.b)
    }

    override fun hashCode(): Int {
        var result = lum.contentHashCode()
        result = 31 * result + r.contentHashCode()
        result = 31 * result + g.contentHashCode()
        result = 31 * result + b.contentHashCode()
        return result
    }
}

data class ProCameraSettings(
    val isManualMode: Boolean = false,
    val evCompensation: Float = 0.0f, // -3.0 to +3.0
    val isoValue: Int = 0, // 0 = Auto, or 100, 200, 400, 800, 1600, 3200
    val shutterSpeedIndex: Int = 0, // 0 = Auto, or index
    val whiteBalance: WhiteBalancePreset = WhiteBalancePreset.AUTO,
    val manualFocusDistance: Float = 0.0f // 0.0 = Auto/Infinity
)

data class CustomFilmRecipe(
    val id: String = "recipe_${System.currentTimeMillis()}",
    val name: String = "Tokyo 90s Golden",
    val author: String = "AuraCam Artist",
    val baseFilterId: FilmFilterId = FilmFilterId.PORTRA_400,
    val contrast: Float = 1.10f,
    val saturation: Float = 1.15f,
    val warmth: Float = 0.15f,
    val tint: Float = -0.05f,
    val grain: Float = 0.30f,
    val halation: Float = 0.20f,
    val vignette: Float = 0.18f,
    val dustScratches: Float = 0.15f,
    val lensStyle: VintageLensStyle = VintageLensStyle.STANDARD,
    val dateStamp: FilmDateStampFormat = FilmDateStampFormat.ORANGE_LCD,
    val frameStyle: FilmFrameStyle = FilmFrameStyle.FILM_35MM,
    val lightLeak: LightLeakStyle = LightLeakStyle.AMBER_LEFT,
    val description: String = "Warm nostalgic amber highlights with authentic film sprockets and organic silver grain"
) {
    fun toShareableCode(): String {
        return "AURACAM|V1|$name|$baseFilterId|%.2f|%.2f|%.2f|%.2f|%.2f|%.2f|%.2f|%.2f|$lensStyle|$dateStamp|$frameStyle|$lightLeak".format(
            contrast, saturation, warmth, tint, grain, halation, vignette, dustScratches
        )
    }

    companion object {
        fun fromShareableCode(code: String): CustomFilmRecipe? {
            return try {
                val parts = code.split("|")
                if (parts.size >= 14 && parts[0] == "AURACAM") {
                    val name = parts[2]
                    val baseId = FilmFilterId.valueOf(parts[3])
                    val contrast = parts[4].toFloat()
                    val saturation = parts[5].toFloat()
                    val warmth = parts[6].toFloat()
                    val tint = parts[7].toFloat()
                    val grain = parts[8].toFloat()
                    val halation = parts[9].toFloat()
                    val vignette = parts[10].toFloat()
                    val dust = parts[11].toFloat()
                    val lens = VintageLensStyle.valueOf(parts[12])
                    val dateStamp = FilmDateStampFormat.valueOf(parts[13])
                    val frame = if (parts.size > 14) FilmFrameStyle.valueOf(parts[14]) else FilmFrameStyle.NONE
                    val leak = if (parts.size > 15) LightLeakStyle.valueOf(parts[15]) else LightLeakStyle.NONE

                    CustomFilmRecipe(
                        id = "recipe_imported_${System.currentTimeMillis()}",
                        name = name,
                        baseFilterId = baseId,
                        contrast = contrast,
                        saturation = saturation,
                        warmth = warmth,
                        tint = tint,
                        grain = grain,
                        halation = halation,
                        vignette = vignette,
                        dustScratches = dust,
                        lensStyle = lens,
                        dateStamp = dateStamp,
                        frameStyle = frame,
                        lightLeak = leak,
                        description = "Imported custom recipe via QR / Code"
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
