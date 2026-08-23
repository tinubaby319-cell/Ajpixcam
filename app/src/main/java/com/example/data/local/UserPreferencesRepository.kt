package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AspectRatioMode
import com.example.data.model.CameraLens
import com.example.data.model.CustomFilmRecipe
import com.example.data.model.DoubleExposureBlendMode
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFilterId
import com.example.data.model.FilmFrameStyle
import com.example.data.model.GridType
import com.example.data.model.LightLeakStyle
import com.example.data.model.PoseCategory
import com.example.data.model.ShutterSoundProfile
import com.example.data.model.VintageLensStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class FilmRollAlbum(
    val id: String = "roll_${System.currentTimeMillis()}",
    val title: String = "Roll #01 - Kodak Gold 200",
    val filmName: String = "Gold 200",
    val filterId: FilmFilterId = FilmFilterId.GOLD_200,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val totalShots: Int = 36,
    val isDeveloped: Boolean = true,
    val coverPhotoPath: String? = null
)

data class UserSettings(
    val showGrid: Boolean = true,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val showHorizonLevel: Boolean = true,
    val showLiveHistogram: Boolean = false,
    val showFocusPeaking: Boolean = false,
    val showZebraStripes: Boolean = false,
    val showPoseCoach: Boolean = false,
    val poseCategory: PoseCategory = PoseCategory.PORTRAIT,
    val aiCompositionEnabled: Boolean = true,
    val aiAutoFramingEnabled: Boolean = true,
    val aiFilterRecommendationEnabled: Boolean = true,
    val compositionSensitivity: String = "Balanced",
    val saveOriginalCopy: Boolean = true,
    val saveRawDngMaster: Boolean = false,
    val defaultFilterId: FilmFilterId = FilmFilterId.F_C200,
    val defaultFilterIntensity: Int = 70,
    val enableFilmGrain: Boolean = true,
    val enableHalation: Boolean = true,
    val enableDustScratches: Boolean = false,
    val dateStampFormat: FilmDateStampFormat = FilmDateStampFormat.OFF,
    val filmFrameStyle: FilmFrameStyle = FilmFrameStyle.NONE,
    val lightLeakStyle: LightLeakStyle = LightLeakStyle.NONE,
    val vintageLensStyle: VintageLensStyle = VintageLensStyle.STANDARD,
    val doubleExposureBlendMode: DoubleExposureBlendMode = DoubleExposureBlendMode.SCREEN,
    val shutterSoundProfile: ShutterSoundProfile = ShutterSoundProfile.LEICA_M,
    val enableShutterSound: Boolean = true,
    val handsFreeVoiceEnabled: Boolean = false,
    val handsFreePalmGestureEnabled: Boolean = false,
    val filmRollMode: Boolean = false,
    val rollExpCount: Int = 1,
    val rollMaxExp: Int = 36,
    val activeRollName: String = "Roll #01 - Classic Color",
    val hapticFeedback: Boolean = true,
    val defaultAspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val defaultLens: CameraLens = CameraLens.WIDE,
    val devSimulationMode: Boolean = false
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auracam_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _customRecipes = MutableStateFlow(loadCustomRecipes())
    val customRecipes: StateFlow<List<CustomFilmRecipe>> = _customRecipes.asStateFlow()

    private val _filmRolls = MutableStateFlow(loadFilmRolls())
    val filmRolls: StateFlow<List<FilmRollAlbum>> = _filmRolls.asStateFlow()

    private fun loadSettings(): UserSettings {
        val filterIdStr = prefs.getString("default_filter", FilmFilterId.F_C200.name) ?: FilmFilterId.F_C200.name
        val filterId = try { FilmFilterId.valueOf(filterIdStr) } catch (_: Exception) { FilmFilterId.F_C200 }

        val gridStr = prefs.getString("grid_type", GridType.RULE_OF_THIRDS.name) ?: GridType.RULE_OF_THIRDS.name
        val gridType = try { GridType.valueOf(gridStr) } catch (_: Exception) { GridType.RULE_OF_THIRDS }

        val dateStampStr = prefs.getString("date_stamp", FilmDateStampFormat.OFF.name) ?: FilmDateStampFormat.OFF.name
        val dateStampFormat = try { FilmDateStampFormat.valueOf(dateStampStr) } catch (_: Exception) { FilmDateStampFormat.OFF }

        val frameStyleStr = prefs.getString("frame_style", FilmFrameStyle.NONE.name) ?: FilmFrameStyle.NONE.name
        val frameStyle = try { FilmFrameStyle.valueOf(frameStyleStr) } catch (_: Exception) { FilmFrameStyle.NONE }

        val lightLeakStr = prefs.getString("light_leak", LightLeakStyle.NONE.name) ?: LightLeakStyle.NONE.name
        val lightLeak = try { LightLeakStyle.valueOf(lightLeakStr) } catch (_: Exception) { LightLeakStyle.NONE }

        val lensStr = prefs.getString("lens_style", VintageLensStyle.STANDARD.name) ?: VintageLensStyle.STANDARD.name
        val lensStyle = try { VintageLensStyle.valueOf(lensStr) } catch (_: Exception) { VintageLensStyle.STANDARD }

        val blendStr = prefs.getString("blend_mode", DoubleExposureBlendMode.SCREEN.name) ?: DoubleExposureBlendMode.SCREEN.name
        val blendMode = try { DoubleExposureBlendMode.valueOf(blendStr) } catch (_: Exception) { DoubleExposureBlendMode.SCREEN }

        val soundStr = prefs.getString("sound_profile", ShutterSoundProfile.LEICA_M.name) ?: ShutterSoundProfile.LEICA_M.name
        val soundProfile = try { ShutterSoundProfile.valueOf(soundStr) } catch (_: Exception) { ShutterSoundProfile.LEICA_M }

        val poseCatStr = prefs.getString("pose_cat", PoseCategory.PORTRAIT.name) ?: PoseCategory.PORTRAIT.name
        val poseCat = try { PoseCategory.valueOf(poseCatStr) } catch (_: Exception) { PoseCategory.PORTRAIT }

        return UserSettings(
            showGrid = prefs.getBoolean("show_grid", true),
            gridType = gridType,
            showHorizonLevel = prefs.getBoolean("show_horizon", true),
            showLiveHistogram = prefs.getBoolean("show_histogram", false),
            showFocusPeaking = prefs.getBoolean("show_focus_peaking", false),
            showZebraStripes = prefs.getBoolean("show_zebra_stripes", false),
            showPoseCoach = prefs.getBoolean("show_pose_coach", false),
            poseCategory = poseCat,
            aiCompositionEnabled = prefs.getBoolean("ai_composition", true),
            aiAutoFramingEnabled = prefs.getBoolean("ai_framing", true),
            aiFilterRecommendationEnabled = prefs.getBoolean("ai_filter_rec", true),
            compositionSensitivity = prefs.getString("composition_sensitivity", "Balanced") ?: "Balanced",
            saveOriginalCopy = prefs.getBoolean("save_original", true),
            saveRawDngMaster = prefs.getBoolean("save_raw_master", false),
            defaultFilterId = filterId,
            defaultFilterIntensity = prefs.getInt("default_intensity", 70),
            enableFilmGrain = prefs.getBoolean("film_grain", true),
            enableHalation = prefs.getBoolean("film_halation", true),
            enableDustScratches = prefs.getBoolean("film_dust_scratches", false),
            dateStampFormat = dateStampFormat,
            filmFrameStyle = frameStyle,
            lightLeakStyle = lightLeak,
            vintageLensStyle = lensStyle,
            doubleExposureBlendMode = blendMode,
            shutterSoundProfile = soundProfile,
            enableShutterSound = prefs.getBoolean("enable_sound", true),
            handsFreeVoiceEnabled = prefs.getBoolean("hands_free_voice", false),
            handsFreePalmGestureEnabled = prefs.getBoolean("hands_free_palm", false),
            filmRollMode = prefs.getBoolean("film_roll_mode", false),
            rollExpCount = prefs.getInt("roll_exp_count", 1),
            rollMaxExp = prefs.getInt("roll_max_exp", 36),
            activeRollName = prefs.getString("active_roll_name", "Roll #01 - Classic Color") ?: "Roll #01 - Classic Color",
            hapticFeedback = prefs.getBoolean("haptics", true),
            devSimulationMode = prefs.getBoolean("dev_simulation", false)
        )
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated

        prefs.edit().apply {
            putBoolean("show_grid", updated.showGrid)
            putString("grid_type", updated.gridType.name)
            putBoolean("show_horizon", updated.showHorizonLevel)
            putBoolean("show_histogram", updated.showLiveHistogram)
            putBoolean("show_focus_peaking", updated.showFocusPeaking)
            putBoolean("show_zebra_stripes", updated.showZebraStripes)
            putBoolean("show_pose_coach", updated.showPoseCoach)
            putString("pose_cat", updated.poseCategory.name)
            putBoolean("ai_composition", updated.aiCompositionEnabled)
            putBoolean("ai_framing", updated.aiAutoFramingEnabled)
            putBoolean("ai_filter_rec", updated.aiFilterRecommendationEnabled)
            putString("composition_sensitivity", updated.compositionSensitivity)
            putBoolean("save_original", updated.saveOriginalCopy)
            putBoolean("save_raw_master", updated.saveRawDngMaster)
            putString("default_filter", updated.defaultFilterId.name)
            putInt("default_intensity", updated.defaultFilterIntensity)
            putBoolean("film_grain", updated.enableFilmGrain)
            putBoolean("film_halation", updated.enableHalation)
            putBoolean("film_dust_scratches", updated.enableDustScratches)
            putString("date_stamp", updated.dateStampFormat.name)
            putString("frame_style", updated.filmFrameStyle.name)
            putString("light_leak", updated.lightLeakStyle.name)
            putString("lens_style", updated.vintageLensStyle.name)
            putString("blend_mode", updated.doubleExposureBlendMode.name)
            putString("sound_profile", updated.shutterSoundProfile.name)
            putBoolean("enable_sound", updated.enableShutterSound)
            putBoolean("hands_free_voice", updated.handsFreeVoiceEnabled)
            putBoolean("hands_free_palm", updated.handsFreePalmGestureEnabled)
            putBoolean("film_roll_mode", updated.filmRollMode)
            putInt("roll_exp_count", updated.rollExpCount)
            putInt("roll_max_exp", updated.rollMaxExp)
            putString("active_roll_name", updated.activeRollName)
            putBoolean("haptics", updated.hapticFeedback)
            putBoolean("dev_simulation", updated.devSimulationMode)
            apply()
        }
    }

    // Custom Recipe Storage
    private fun loadCustomRecipes(): List<CustomFilmRecipe> {
        val jsonStr = prefs.getString("custom_recipes_json", null)
        val defaultList = listOf(
            CustomFilmRecipe(
                id = "preset_tokyo_90s",
                name = "Tokyo 90s Amber",
                author = "AuraCam Lab",
                baseFilterId = FilmFilterId.GOLD_200,
                contrast = 1.15f,
                saturation = 1.12f,
                warmth = 0.20f,
                tint = -0.04f,
                grain = 0.35f,
                halation = 0.25f,
                vignette = 0.20f,
                dustScratches = 0.15f,
                lensStyle = VintageLensStyle.STANDARD,
                dateStamp = FilmDateStampFormat.ORANGE_LCD,
                frameStyle = FilmFrameStyle.FILM_35MM,
                lightLeak = LightLeakStyle.AMBER_LEFT,
                description = "Warm nostalgic Japanese neon & golden hour feeling"
            ),
            CustomFilmRecipe(
                id = "preset_pacific_blues",
                name = "Pacific Coast 94",
                author = "Fuji Emulsion",
                baseFilterId = FilmFilterId.F_400H,
                contrast = 0.95f,
                saturation = 0.95f,
                warmth = -0.10f,
                tint = -0.06f,
                grain = 0.18f,
                halation = 0.20f,
                vignette = 0.12f,
                dustScratches = 0.08f,
                lensStyle = VintageLensStyle.DREAMY_DIFFUSION,
                dateStamp = FilmDateStampFormat.CLASSIC_WHITE,
                frameStyle = FilmFrameStyle.POLAROID,
                lightLeak = LightLeakStyle.NONE,
                description = "Pastel airy ocean blues, soft mint shadows, and dreamy highlight glow"
            ),
            CustomFilmRecipe(
                id = "preset_cinematic_noir",
                name = "Silver Noir 1950",
                author = "Documentary Master",
                baseFilterId = FilmFilterId.DOKA_X400,
                contrast = 1.35f,
                saturation = 0.0f,
                warmth = 0.0f,
                tint = 0.0f,
                grain = 0.45f,
                halation = 0.05f,
                vignette = 0.28f,
                dustScratches = 0.20f,
                lensStyle = VintageLensStyle.FISHEYE_8MM,
                dateStamp = FilmDateStampFormat.OFF,
                frameStyle = FilmFrameStyle.HASSELBLAD,
                lightLeak = LightLeakStyle.NONE,
                description = "Dramatic high contrast silver halide street monochrome"
            )
        )

        if (jsonStr == null) return defaultList

        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CustomFilmRecipe>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomFilmRecipe(
                        id = obj.optString("id", "recipe_$i"),
                        name = obj.optString("name", "Custom Recipe"),
                        author = obj.optString("author", "User"),
                        baseFilterId = FilmFilterId.valueOf(obj.optString("baseFilterId", FilmFilterId.PORTRA_400.name)),
                        contrast = obj.optDouble("contrast", 1.0).toFloat(),
                        saturation = obj.optDouble("saturation", 1.0).toFloat(),
                        warmth = obj.optDouble("warmth", 0.0).toFloat(),
                        tint = obj.optDouble("tint", 0.0).toFloat(),
                        grain = obj.optDouble("grain", 0.2).toFloat(),
                        halation = obj.optDouble("halation", 0.15).toFloat(),
                        vignette = obj.optDouble("vignette", 0.15).toFloat(),
                        dustScratches = obj.optDouble("dustScratches", 0.0).toFloat(),
                        lensStyle = VintageLensStyle.valueOf(obj.optString("lensStyle", VintageLensStyle.STANDARD.name)),
                        dateStamp = FilmDateStampFormat.valueOf(obj.optString("dateStamp", FilmDateStampFormat.OFF.name)),
                        frameStyle = FilmFrameStyle.valueOf(obj.optString("frameStyle", FilmFrameStyle.NONE.name)),
                        lightLeak = LightLeakStyle.valueOf(obj.optString("lightLeak", LightLeakStyle.NONE.name)),
                        description = obj.optString("description", "")
                    )
                )
            }
            if (list.isEmpty()) defaultList else list
        } catch (_: Exception) {
            defaultList
        }
    }

    fun saveCustomRecipe(recipe: CustomFilmRecipe) {
        val current = _customRecipes.value.toMutableList()
        val index = current.indexOfFirst { it.id == recipe.id }
        if (index >= 0) {
            current[index] = recipe
        } else {
            current.add(0, recipe)
        }
        _customRecipes.value = current
        saveCustomRecipesToDisk(current)
    }

    fun deleteCustomRecipe(recipeId: String) {
        val current = _customRecipes.value.filterNot { it.id == recipeId }
        _customRecipes.value = current
        saveCustomRecipesToDisk(current)
    }

    private fun saveCustomRecipesToDisk(recipes: List<CustomFilmRecipe>) {
        val array = JSONArray()
        for (r in recipes) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("author", r.author)
                put("baseFilterId", r.baseFilterId.name)
                put("contrast", r.contrast.toDouble())
                put("saturation", r.saturation.toDouble())
                put("warmth", r.warmth.toDouble())
                put("tint", r.tint.toDouble())
                put("grain", r.grain.toDouble())
                put("halation", r.halation.toDouble())
                put("vignette", r.vignette.toDouble())
                put("dustScratches", r.dustScratches.toDouble())
                put("lensStyle", r.lensStyle.name)
                put("dateStamp", r.dateStamp.name)
                put("frameStyle", r.frameStyle.name)
                put("lightLeak", r.lightLeak.name)
                put("description", r.description)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_recipes_json", array.toString()).apply()
    }

    // Film Rolls Storage
    private fun loadFilmRolls(): List<FilmRollAlbum> {
        val jsonStr = prefs.getString("film_rolls_json", null)
        val defaultRolls = listOf(
            FilmRollAlbum(
                id = "roll_01_archived",
                title = "Manali Expedition • Roll #01",
                filmName = "Kodak Gold 200",
                filterId = FilmFilterId.GOLD_200,
                completedTimestamp = System.currentTimeMillis() - 86400000L * 3,
                totalShots = 36,
                isDeveloped = true
            ),
            FilmRollAlbum(
                id = "roll_02_archived",
                title = "Street Documentary • Roll #02",
                filmName = "Doka X400 Silver",
                filterId = FilmFilterId.DOKA_X400,
                completedTimestamp = System.currentTimeMillis() - 86400000L * 7,
                totalShots = 36,
                isDeveloped = true
            )
        )
        if (jsonStr == null) return defaultRolls

        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FilmRollAlbum>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FilmRollAlbum(
                        id = obj.optString("id", "roll_$i"),
                        title = obj.optString("title", "Roll #$i"),
                        filmName = obj.optString("filmName", "Portra 400"),
                        filterId = FilmFilterId.valueOf(obj.optString("filterId", FilmFilterId.PORTRA_400.name)),
                        completedTimestamp = obj.optLong("completedTimestamp", System.currentTimeMillis()),
                        totalShots = obj.optInt("totalShots", 36),
                        isDeveloped = obj.optBoolean("isDeveloped", true),
                        coverPhotoPath = obj.optString("coverPhotoPath", null)
                    )
                )
            }
            if (list.isEmpty()) defaultRolls else list
        } catch (_: Exception) {
            defaultRolls
        }
    }

    fun addDevelopedRoll(roll: FilmRollAlbum) {
        val current = _filmRolls.value.toMutableList()
        current.add(0, roll)
        _filmRolls.value = current
        saveFilmRollsToDisk(current)
    }

    private fun saveFilmRollsToDisk(rolls: List<FilmRollAlbum>) {
        val array = JSONArray()
        for (r in rolls) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("filmName", r.filmName)
                put("filterId", r.filterId.name)
                put("completedTimestamp", r.completedTimestamp)
                put("totalShots", r.totalShots)
                put("isDeveloped", r.isDeveloped)
                put("coverPhotoPath", r.coverPhotoPath ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString("film_rolls_json", array.toString()).apply()
    }
}
