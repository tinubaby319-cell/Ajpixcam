package com.example.data.model

import java.io.File

data class PhotoMetadata(
    val iso: Int = 100,
    val shutterSpeed: String = "1/250s",
    val ev: Float = 0.0f,
    val kelvin: Int = 5400,
    val lens: String = "1x (26mm eq)",
    val filterName: String = "F C200",
    val filterIntensity: Int = 70,
    val aiCompositionScore: Int = 88,
    val sceneType: String = "Portrait & People",
    val width: Int = 3000,
    val height: Int = 4000,
    val fileSizeFormatted: String = "4.2 MB"
)

data class PhotoItem(
    val id: Long = 0,
    val filePath: String,
    val originalFilePath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val filterId: FilmFilterId = FilmFilterId.F_C200,
    val filterIntensity: Int = 70,
    val metadata: PhotoMetadata = PhotoMetadata(),
    val isFavorite: Boolean = false
) {
    val file: File get() = File(filePath)
    val exists: Boolean get() = file.exists()
}
