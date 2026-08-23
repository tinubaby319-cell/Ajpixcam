package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.camera.BitmapUtils
import com.example.data.model.ContactSheetLayout
import com.example.data.model.PhotoItem
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSheetMakerScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoEntities by viewModel.allPhotosFlow.collectAsState()
    val allPhotos = remember(photoEntities) { photoEntities.map { it.toDomainModel() } }

    val selectedPhotoIds = remember { mutableStateListOf<Long>() }
    var selectedLayout by remember { mutableStateOf(ContactSheetLayout.GRID_2X2) }
    var isRenderingExport by remember { mutableStateOf(false) }
    var previewSheetBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Pre-select photos if empty
    LaunchedEffect(allPhotos) {
        if (selectedPhotoIds.isEmpty() && allPhotos.isNotEmpty()) {
            val initial = allPhotos.take(selectedLayout.maxPhotos).map { it.id }
            selectedPhotoIds.addAll(initial)
        }
    }

    // Function to generate the contact sheet bitmap
    fun generateContactSheet(photos: List<PhotoItem>, layout: ContactSheetLayout, isHighRes: Boolean): Bitmap {
        val outW = if (isHighRes) 2000 else 800
        val outH = (outW / layout.aspectRatio).toInt()

        val resultBitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // Darkroom black board background
        canvas.drawColor(AndroidColor.rgb(16, 16, 16))

        // Header Title
        val headerH = (outH * 0.08f)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 180, 40)
            textSize = headerH * 0.42f
            typeface = Typeface.MONOSPACE
            isFakeBoldText = true
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date())
        val headerText = "AURACAM PROOF SHEET • ${layout.label.uppercase()} • $dateStr"
        canvas.drawText(headerText, outW * 0.04f, headerH * 0.65f, titlePaint)

        // Grid parameters
        val gridTop = headerH
        val gridBottom = outH * 0.94f
        val gridLeft = outW * 0.04f
        val gridRight = outW * 0.96f

        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop

        val gap = outW * 0.025f
        val cellW = (gridW - (layout.cols - 1) * gap) / layout.cols
        val cellH = (gridH - (layout.rows - 1) * gap) / layout.rows

        val cellBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(40, 40, 40)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(180, 180, 170)
            textSize = cellH * 0.07f
            typeface = Typeface.MONOSPACE
        }

        var photoIdx = 0
        for (r in 0 until layout.rows) {
            for (c in 0 until layout.cols) {
                val left = gridLeft + c * (cellW + gap)
                val top = gridTop + r * (cellH + gap)
                val right = left + cellW
                val bottom = top + cellH

                if (photoIdx < photos.size) {
                    val p = photos[photoIdx]
                    val srcBmp = BitmapFactory.decodeFile(p.filePath)
                    if (srcBmp != null) {
                        val imgBottom = bottom - (cellH * 0.12f)
                        val dstRect = Rect(left.toInt(), top.toInt(), right.toInt(), imgBottom.toInt())
                        canvas.drawBitmap(srcBmp, null, dstRect, null)
                        srcBmp.recycle()

                        // Film frame marker
                        val frameNumber = "▶ %02dA • %s".format(photoIdx + 1, p.metadata.filterName)
                        canvas.drawText(frameNumber, left + 4f, bottom - 4f, metaPaint)
                    }
                } else {
                    canvas.drawRect(left, top, right, bottom, cellBorderPaint)
                }
                photoIdx++
            }
        }

        // Bottom Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(120, 120, 120)
            textSize = outH * 0.022f
            typeface = Typeface.MONOSPACE
        }
        val footerText = "EMULSION CHEMISTRY • ARCHIVAL 35MM PROOF • SHOT ON AURACAM"
        canvas.drawText(footerText, outW * 0.04f, outH * 0.98f, footerPaint)

        return resultBitmap
    }

    // Refresh preview when selection or layout changes
    LaunchedEffect(selectedPhotoIds.toList(), selectedLayout) {
        val selectedPhotos = allPhotos.filter { selectedPhotoIds.contains(it.id) }
        if (selectedPhotos.isNotEmpty()) {
            val preview = withContext(Dispatchers.Default) {
                generateContactSheet(selectedPhotos, selectedLayout, isHighRes = false)
            }
            previewSheetBitmap = preview
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("35mm Contact Sheet Maker", color = NaturalDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Proof Sheet Collages for Print & Story", color = NaturalTextMuted, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("contact_sheet_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NaturalDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NaturalBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NaturalBg,
        modifier = modifier.testTag("contact_sheet_maker_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Live Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(selectedLayout.aspectRatio)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111111))
                    .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (previewSheetBitmap != null) {
                    Image(
                        bitmap = previewSheetBitmap!!.asImageBitmap(),
                        contentDescription = "Contact Sheet Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = NaturalTextTertiary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select photos below to preview sheet", color = NaturalTextMuted, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Layout Format Selector
            Text("LAYOUT FORMAT:", color = NaturalDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactSheetLayout.entries.forEach { layout ->
                    val isSel = layout == selectedLayout
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) NaturalOlive else NaturalSurface)
                            .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(12.dp))
                            .clickable {
                                selectedLayout = layout
                                if (selectedPhotoIds.size > layout.maxPhotos) {
                                    val trimmed = selectedPhotoIds.take(layout.maxPhotos)
                                    selectedPhotoIds.clear()
                                    selectedPhotoIds.addAll(trimmed)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = layout.label,
                            color = if (isSel) NaturalBg else NaturalDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Select Photos Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SELECT PHOTOS:", color = NaturalDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${selectedPhotoIds.size} / ${selectedLayout.maxPhotos} selected",
                    color = NaturalOlive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allPhotos, key = { it.id }) { photo ->
                    val isSelected = selectedPhotoIds.contains(photo.id)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                2.dp,
                                if (isSelected) NaturalOlive else NaturalBorderLight,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                if (isSelected) {
                                    selectedPhotoIds.remove(photo.id)
                                } else if (selectedPhotoIds.size < selectedLayout.maxPhotos) {
                                    selectedPhotoIds.add(photo.id)
                                }
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(photo.file),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(NaturalOlive),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NaturalBg, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Export High-Res Proof Sheet Action
            Button(
                onClick = {
                    val selectedPhotos = allPhotos.filter { selectedPhotoIds.contains(it.id) }
                    if (selectedPhotos.isEmpty()) return@Button

                    isRenderingExport = true
                    scope.launch(Dispatchers.Default) {
                        try {
                            val hiRes = generateContactSheet(selectedPhotos, selectedLayout, isHighRes = true)
                            val filename = "AURA_PROOF_SHEET_${System.currentTimeMillis()}.jpg"
                            BitmapUtils.saveBitmapToInternalStorage(context, hiRes, filename)
                            BitmapUtils.saveBitmapToMediaStore(context, hiRes, "AuraCam_ContactSheets")

                            withContext(Dispatchers.Main) {
                                isRenderingExport = false
                                snackbarHostState.showSnackbar("35mm Contact Sheet saved to Gallery!")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isRenderingExport = false
                                snackbarHostState.showSnackbar("Export error: ${e.localizedMessage}")
                            }
                        }
                    }
                },
                enabled = selectedPhotoIds.isNotEmpty() && !isRenderingExport,
                colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("export_contact_sheet_button")
            ) {
                if (isRenderingExport) {
                    CircularProgressIndicator(color = NaturalBg, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rendering Master Contact Sheet...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Archival Contact Sheet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
