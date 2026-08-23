package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FilmFilterId
import com.example.data.model.PhotoItem
import com.example.data.model.PhotoMetadata

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val originalFilePath: String?,
    val timestamp: Long,
    val filterId: String,
    val filterIntensity: Int,
    val iso: Int,
    val shutterSpeed: String,
    val ev: Float,
    val kelvin: Int,
    val lens: String,
    val filterName: String,
    val aiCompositionScore: Int,
    val sceneType: String,
    val width: Int,
    val height: Int,
    val fileSizeFormatted: String,
    val isFavorite: Boolean = false
) {
    fun toDomainModel(): PhotoItem {
        val fId = try {
            FilmFilterId.valueOf(filterId)
        } catch (_: Exception) {
            FilmFilterId.F_C200
        }
        return PhotoItem(
            id = id,
            filePath = filePath,
            originalFilePath = originalFilePath,
            timestamp = timestamp,
            filterId = fId,
            filterIntensity = filterIntensity,
            metadata = PhotoMetadata(
                iso = iso,
                shutterSpeed = shutterSpeed,
                ev = ev,
                kelvin = kelvin,
                lens = lens,
                filterName = filterName,
                filterIntensity = filterIntensity,
                aiCompositionScore = aiCompositionScore,
                sceneType = sceneType,
                width = width,
                height = height,
                fileSizeFormatted = fileSizeFormatted
            ),
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromDomainModel(item: PhotoItem): PhotoEntity {
            return PhotoEntity(
                id = item.id,
                filePath = item.filePath,
                originalFilePath = item.originalFilePath,
                timestamp = item.timestamp,
                filterId = item.filterId.name,
                filterIntensity = item.filterIntensity,
                iso = item.metadata.iso,
                shutterSpeed = item.metadata.shutterSpeed,
                ev = item.metadata.ev,
                kelvin = item.metadata.kelvin,
                lens = item.metadata.lens,
                filterName = item.metadata.filterName,
                aiCompositionScore = item.metadata.aiCompositionScore,
                sceneType = item.metadata.sceneType,
                width = item.metadata.width,
                height = item.metadata.height,
                fileSizeFormatted = item.metadata.fileSizeFormatted,
                isFavorite = item.isFavorite
            )
        }
    }
}
