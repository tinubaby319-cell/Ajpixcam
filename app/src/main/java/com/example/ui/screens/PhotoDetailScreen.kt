package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.PhotoItem
import com.example.ui.components.CompareSlider
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photoId: Long,
    viewModel: CameraViewModel,
    onNavigateToEditor: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allPhotos by viewModel.allPhotosFlow.collectAsState()
    val photoEntity = allPhotos.find { it.id == photoId }
    val photo = photoEntity?.toDomainModel()

    var showInfoSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isComparing by remember { mutableStateOf(false) }

    if (photo == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(NaturalBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Photo not found", color = NaturalDark)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = photo.metadata.filterName,
                        color = NaturalDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NaturalDark
                        )
                    }
                },
                actions = {
                    // Compare Toggle if original is saved
                    if (photo.originalFilePath != null) {
                        IconButton(
                            onClick = { isComparing = !isComparing },
                            modifier = Modifier.testTag("detail_compare_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compare,
                                contentDescription = "Compare Before/After",
                                tint = if (isComparing) NaturalOlive else NaturalDark
                            )
                        }
                    }

                    // Info Sheet Button
                    IconButton(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier.testTag("detail_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Photo Info",
                            tint = NaturalDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NaturalBg)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NaturalSurface)
                    .border(1.dp, NaturalBorderLight, RoundedCornerShape(0.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share
                IconButton(
                    onClick = {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                photo.file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Photograph"))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.testTag("share_photo_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = NaturalDark)
                }

                // Favorite
                IconButton(
                    onClick = { viewModel.toggleFavorite(photo) },
                    modifier = Modifier.testTag("favorite_photo_button")
                ) {
                    Icon(
                        imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (photo.isFavorite) NaturalOlive else NaturalDark
                    )
                }

                // Edit in Studio
                IconButton(
                    onClick = { onNavigateToEditor(photo.id) },
                    modifier = Modifier.testTag("edit_photo_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit in Studio", tint = NaturalOlive)
                }

                // Delete
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.testTag("delete_photo_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFA64438))
                }
            }
        },
        containerColor = NaturalBg,
        modifier = modifier.testTag("photo_detail_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(photo.file),
                contentDescription = "Photograph",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            if (isComparing) {
                CompareSlider(
                    filterName = photo.metadata.filterName,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Photograph", color = NaturalDark, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete this photo?", color = NaturalTextMuted) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deletePhoto(photo) { onBack() }
                        }
                    ) {
                        Text("Delete", color = Color(0xFFA64438), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = NaturalDark)
                    }
                },
                containerColor = NaturalCard
            )
        }

        // Info Metadata Bottom Sheet
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                containerColor = NaturalSurface,
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "EXIF & AI Telemetry",
                        color = NaturalDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val dateStr = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault())
                        .format(Date(photo.timestamp))

                    MetadataRow("Date", dateStr)
                    MetadataRow("Film Profile", "${photo.metadata.filterName} (${photo.metadata.filterIntensity}%)")
                    MetadataRow("AI Scene", photo.metadata.sceneType)
                    MetadataRow("Composition Score", "${photo.metadata.aiCompositionScore} / 100")
                    MetadataRow("Camera Lens", photo.metadata.lens)
                    MetadataRow("ISO / Exposure", "ISO ${photo.metadata.iso} • EV ${String.format("%+.1f", photo.metadata.ev)}")
                    MetadataRow("Shutter Speed", photo.metadata.shutterSpeed)
                    MetadataRow("Color Temp", "${photo.metadata.kelvin} K")
                    MetadataRow("Dimensions", "${photo.metadata.width} × ${photo.metadata.height}")
                    MetadataRow("File Size", photo.metadata.fileSizeFormatted)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = NaturalTextMuted, fontSize = 13.sp)
        Text(text = value, color = NaturalDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
