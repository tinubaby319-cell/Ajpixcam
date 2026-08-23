package com.example.ui.screens

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.OrientationSensorHelper
import com.example.camera.CameraSoundManager
import com.example.camera.HandsFreeShutterManager
import com.example.data.local.AppDatabase
import com.example.data.local.FilmRollAlbum
import com.example.data.local.PhotoEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.UserSettings
import com.example.data.model.AspectRatioMode
import com.example.data.model.CameraLens
import com.example.data.model.CameraMode
import com.example.data.model.CustomFilmRecipe
import com.example.data.model.DoubleExposureBlendMode
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFilter
import com.example.data.model.FilmFilterId
import com.example.data.model.FilmFrameStyle
import com.example.data.model.FlashMode
import com.example.data.model.GridType
import com.example.data.model.HistogramData
import com.example.data.model.LightLeakStyle
import com.example.data.model.LiveAIState
import com.example.data.model.PhotoItem
import com.example.data.model.PoseCategory
import com.example.data.model.ProCameraSettings
import com.example.data.model.ShutterSoundProfile
import com.example.data.model.TimerMode
import com.example.data.model.VintageLensStyle
import com.example.filter.FilmPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CameraUiState(
    val selectedMode: CameraMode = CameraMode.PHOTO,
    val selectedLens: CameraLens = CameraLens.WIDE,
    val selectedFilter: FilmFilter = FilmPresets.F_C200,
    val filterIntensity: Int = 70,
    val flashMode: FlashMode = FlashMode.OFF,
    val timerMode: TimerMode = TimerMode.OFF,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val proSettings: ProCameraSettings = ProCameraSettings(),
    val liveAiState: LiveAIState = LiveAIState(),
    val histogramData: HistogramData = HistogramData(),
    val isAiGuidanceEnabled: Boolean = true,
    val isComparing: Boolean = false,
    val isCapturing: Boolean = false,
    val isRecordingVideo: Boolean = false,
    val recordingDurationSec: Int = 0,
    val showRecommendationCard: Boolean = true,
    val latestPhoto: PhotoItem? = null,
    val userSettings: UserSettings = UserSettings(),
    val errorMessage: String? = null,
    val rollJustCompleted: Boolean = false,
    val activeCustomRecipe: CustomFilmRecipe? = null,
    val doubleExposureFirstBitmap: Bitmap? = null,
    val doubleExposureBlendMode: DoubleExposureBlendMode = DoubleExposureBlendMode.SCREEN,
    val doubleExposureOpacity: Float = 0.6f
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val photoDao = db.photoDao()
    private val preferencesRepository = UserPreferencesRepository(application)
    private val orientationHelper = OrientationSensorHelper(application)

    val soundManager = CameraSoundManager(application, viewModelScope)
    val handsFreeManager = HandsFreeShutterManager(application, viewModelScope)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val customRecipes: StateFlow<List<CustomFilmRecipe>> = preferencesRepository.customRecipes
    val filmRolls: StateFlow<List<FilmRollAlbum>> = preferencesRepository.filmRolls

    val allPhotosFlow = photoDao.getAllPhotosFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        orientationHelper.startListening()

        // Observe settings
        viewModelScope.launch {
            preferencesRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    userSettings = settings,
                    isAiGuidanceEnabled = settings.aiCompositionEnabled,
                    selectedFilter = FilmPresets.getById(settings.defaultFilterId),
                    filterIntensity = settings.defaultFilterIntensity,
                    doubleExposureBlendMode = settings.doubleExposureBlendMode
                )
            }
        }

        // Observe latest photo for gallery thumbnail
        viewModelScope.launch {
            photoDao.getLatestPhotoFlow().collect { entity ->
                _uiState.value = _uiState.value.copy(
                    latestPhoto = entity?.toDomainModel()
                )
            }
        }

        // Observe horizon level
        viewModelScope.launch {
            orientationHelper.horizonLevel.collect { horizon ->
                _uiState.value = _uiState.value.copy(
                    liveAiState = _uiState.value.liveAiState.copy(horizon = horizon)
                )
            }
        }
    }

    fun playShutterSound() {
        soundManager.playShutter(
            profile = _uiState.value.userSettings.shutterSoundProfile,
            isSoundEnabled = _uiState.value.userSettings.enableShutterSound,
            isHapticsEnabled = _uiState.value.userSettings.hapticFeedback
        )
    }

    fun setDoubleExposureFirstFrame(bitmap: Bitmap?) {
        _uiState.value = _uiState.value.copy(doubleExposureFirstBitmap = bitmap)
    }

    fun setDoubleExposureFirstFrameFromPath(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = BitmapFactory.decodeFile(path)
            if (bmp != null) {
                _uiState.value = _uiState.value.copy(doubleExposureFirstBitmap = bmp)
            }
        }
    }

    fun setDoubleExposureBlendMode(mode: DoubleExposureBlendMode) {
        _uiState.value = _uiState.value.copy(doubleExposureBlendMode = mode)
        preferencesRepository.updateSettings { it.copy(doubleExposureBlendMode = mode) }
    }

    fun setDoubleExposureOpacity(opacity: Float) {
        _uiState.value = _uiState.value.copy(doubleExposureOpacity = opacity)
    }

    fun clearDoubleExposure() {
        _uiState.value = _uiState.value.copy(doubleExposureFirstBitmap = null)
    }

    fun applyCustomRecipe(recipe: CustomFilmRecipe) {
        val baseFilter = FilmPresets.getById(recipe.baseFilterId)
        _uiState.value = _uiState.value.copy(
            selectedFilter = baseFilter,
            activeCustomRecipe = recipe
        )
        preferencesRepository.updateSettings {
            it.copy(
                defaultFilterId = recipe.baseFilterId,
                enableFilmGrain = recipe.grain > 0.05f,
                enableHalation = recipe.halation > 0.05f,
                enableDustScratches = recipe.dustScratches > 0.05f,
                vintageLensStyle = recipe.lensStyle,
                dateStampFormat = recipe.dateStamp,
                filmFrameStyle = recipe.frameStyle,
                lightLeakStyle = recipe.lightLeak
            )
        }
    }

    fun saveCustomRecipe(recipe: CustomFilmRecipe) {
        preferencesRepository.saveCustomRecipe(recipe)
    }

    fun deleteCustomRecipe(id: String) {
        preferencesRepository.deleteCustomRecipe(id)
    }

    fun completeAndArchiveCurrentRoll() {
        val currentSettings = _uiState.value.userSettings
        val roll = FilmRollAlbum(
            id = "roll_${System.currentTimeMillis()}",
            title = currentSettings.activeRollName,
            filmName = _uiState.value.selectedFilter.name,
            filterId = _uiState.value.selectedFilter.id,
            completedTimestamp = System.currentTimeMillis(),
            totalShots = currentSettings.rollExpCount.coerceAtLeast(1),
            isDeveloped = true,
            coverPhotoPath = _uiState.value.latestPhoto?.filePath
        )
        preferencesRepository.addDevelopedRoll(roll)
        preferencesRepository.updateSettings {
            it.copy(
                rollExpCount = 1,
                activeRollName = "Roll #%02d - %s".format((preferencesRepository.filmRolls.value.size + 1), _uiState.value.selectedFilter.name)
            )
        }
    }

    fun updateLiveAiState(aiState: LiveAIState) {
        _uiState.value = _uiState.value.copy(
            liveAiState = aiState.copy(horizon = _uiState.value.liveAiState.horizon)
        )
    }

    fun updateHistogram(data: HistogramData) {
        _uiState.value = _uiState.value.copy(histogramData = data)
    }

    fun onModeChanged(mode: CameraMode) {
        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            proSettings = _uiState.value.proSettings.copy(isManualMode = mode == CameraMode.PRO),
            selectedFilter = if (mode == CameraMode.CINE_LOG) FilmPresets.FLAT_LOG else _uiState.value.selectedFilter
        )
    }

    fun onLensChanged(lens: CameraLens) {
        _uiState.value = _uiState.value.copy(selectedLens = lens)
    }

    fun onFilterChanged(filter: FilmFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            activeCustomRecipe = null,
            showRecommendationCard = false
        )
    }

    fun onIntensityChanged(intensity: Int) {
        _uiState.value = _uiState.value.copy(filterIntensity = intensity)
    }

    fun onFlashChanged(flash: FlashMode) {
        _uiState.value = _uiState.value.copy(flashMode = flash)
    }

    fun onTimerChanged(timer: TimerMode) {
        _uiState.value = _uiState.value.copy(timerMode = timer)
    }

    fun onAspectRatioChanged(aspectRatio: AspectRatioMode) {
        _uiState.value = _uiState.value.copy(aspectRatio = aspectRatio)
    }

    fun onProSettingsChanged(settings: ProCameraSettings) {
        _uiState.value = _uiState.value.copy(proSettings = settings)
    }

    fun toggleAiGuidance() {
        val newEnabled = !_uiState.value.isAiGuidanceEnabled
        _uiState.value = _uiState.value.copy(isAiGuidanceEnabled = newEnabled)
        preferencesRepository.updateSettings { it.copy(aiCompositionEnabled = newEnabled) }
    }

    fun toggleHistogram() {
        val newShow = !_uiState.value.userSettings.showLiveHistogram
        preferencesRepository.updateSettings { it.copy(showLiveHistogram = newShow) }
    }

    fun toggleFocusPeaking() {
        val newShow = !_uiState.value.userSettings.showFocusPeaking
        preferencesRepository.updateSettings { it.copy(showFocusPeaking = newShow) }
    }

    fun toggleZebraStripes() {
        val newShow = !_uiState.value.userSettings.showZebraStripes
        preferencesRepository.updateSettings { it.copy(showZebraStripes = newShow) }
    }

    fun togglePoseCoach() {
        val newShow = !_uiState.value.userSettings.showPoseCoach
        preferencesRepository.updateSettings { it.copy(showPoseCoach = newShow) }
    }

    fun onPoseCategorySelected(category: PoseCategory) {
        preferencesRepository.updateSettings { it.copy(poseCategory = category) }
    }

    fun cycleGridType() {
        val all = GridType.entries
        val nextIndex = (all.indexOf(_uiState.value.userSettings.gridType) + 1) % all.size
        preferencesRepository.updateSettings { it.copy(gridType = all[nextIndex]) }
    }

    fun cycleDateStampFormat() {
        val all = FilmDateStampFormat.entries
        val nextIndex = (all.indexOf(_uiState.value.userSettings.dateStampFormat) + 1) % all.size
        preferencesRepository.updateSettings { it.copy(dateStampFormat = all[nextIndex]) }
    }

    fun cycleFrameStyle() {
        val all = FilmFrameStyle.entries
        val nextIndex = (all.indexOf(_uiState.value.userSettings.filmFrameStyle) + 1) % all.size
        preferencesRepository.updateSettings { it.copy(filmFrameStyle = all[nextIndex]) }
    }

    fun cycleLightLeakStyle() {
        val all = LightLeakStyle.entries
        val nextIndex = (all.indexOf(_uiState.value.userSettings.lightLeakStyle) + 1) % all.size
        preferencesRepository.updateSettings { it.copy(lightLeakStyle = all[nextIndex]) }
    }

    fun cycleVintageLensStyle() {
        val all = VintageLensStyle.entries
        val nextIndex = (all.indexOf(_uiState.value.userSettings.vintageLensStyle) + 1) % all.size
        preferencesRepository.updateSettings { it.copy(vintageLensStyle = all[nextIndex]) }
    }

    fun toggleDustScratches() {
        val newDust = !_uiState.value.userSettings.enableDustScratches
        preferencesRepository.updateSettings { it.copy(enableDustScratches = newDust) }
    }

    fun cycleShutterSoundProfile() {
        val all = ShutterSoundProfile.entries
        val nextIndex = (all.indexOf(_uiState.value.userSettings.shutterSoundProfile) + 1) % all.size
        val nextProfile = all[nextIndex]
        preferencesRepository.updateSettings { it.copy(shutterSoundProfile = nextProfile) }
        soundManager.playShutter(nextProfile, isSoundEnabled = true, isHapticsEnabled = true)
    }

    fun toggleCompare() {
        _uiState.value = _uiState.value.copy(isComparing = !_uiState.value.isComparing)
    }

    fun dismissRecommendation() {
        _uiState.value = _uiState.value.copy(showRecommendationCard = false)
    }

    fun applyRecommendation(filter: FilmFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            activeCustomRecipe = null,
            showRecommendationCard = false
        )
    }

    fun onPhotoCaptured(photoItem: PhotoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = PhotoEntity.fromDomainModel(photoItem)
            val newId = photoDao.insertPhoto(entity)

            // Film roll advancement
            val currentSettings = _uiState.value.userSettings
            var rollCompleted = false
            if (currentSettings.filmRollMode) {
                val nextExp = currentSettings.rollExpCount + 1
                if (nextExp > currentSettings.rollMaxExp) {
                    rollCompleted = true
                    completeAndArchiveCurrentRoll()
                } else {
                    preferencesRepository.updateSettings { it.copy(rollExpCount = nextExp) }
                }
            }

            _uiState.value = _uiState.value.copy(
                latestPhoto = photoItem.copy(id = newId),
                isCapturing = false,
                rollJustCompleted = rollCompleted
            )
        }
    }

    fun resetFilmRoll() {
        preferencesRepository.updateSettings { it.copy(rollExpCount = 1) }
        _uiState.value = _uiState.value.copy(rollJustCompleted = false)
    }

    fun deletePhoto(photo: PhotoItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            photoDao.deleteById(photo.id)
            try {
                photo.file.delete()
                photo.originalFilePath?.let { java.io.File(it).delete() }
            } catch (_: Exception) {}
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun toggleFavorite(photo: PhotoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val newFav = !photo.isFavorite
            photoDao.setFavorite(photo.id, newFav)
        }
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        preferencesRepository.updateSettings(transform)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isCapturing = false)
    }

    override fun onCleared() {
        super.onCleared()
        orientationHelper.stopListening()
        handsFreeManager.release()
    }
}
