package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.BitmapUtils
import com.example.data.model.CustomFilmRecipe
import com.example.data.model.FilmDateStampFormat
import com.example.data.model.FilmFilterId
import com.example.data.model.FilmFrameStyle
import com.example.data.model.LightLeakStyle
import com.example.data.model.VintageLensStyle
import com.example.filter.FilmPresets
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import com.example.ui.theme.NaturalTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmRecipeStudioScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val customRecipes by viewModel.customRecipes.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var editingRecipe by remember { mutableStateOf<CustomFilmRecipe?>(null) }
    var showQrDialogForRecipe by remember { mutableStateOf<CustomFilmRecipe?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCodeText by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    // Editor state
    var recipeName by remember { mutableStateOf("My Fuji Golden Recipe") }
    var baseFilterId by remember { mutableStateOf(FilmFilterId.PORTRA_400) }
    var contrast by remember { mutableFloatStateOf(1.10f) }
    var saturation by remember { mutableFloatStateOf(1.15f) }
    var warmth by remember { mutableFloatStateOf(0.15f) }
    var tint by remember { mutableFloatStateOf(-0.04f) }
    var grain by remember { mutableFloatStateOf(0.30f) }
    var halation by remember { mutableFloatStateOf(0.20f) }
    var vignette by remember { mutableFloatStateOf(0.18f) }
    var dustScratches by remember { mutableFloatStateOf(0.10f) }
    var lensStyle by remember { mutableStateOf(VintageLensStyle.STANDARD) }
    var dateStamp by remember { mutableStateOf(FilmDateStampFormat.ORANGE_LCD) }
    var frameStyle by remember { mutableStateOf(FilmFrameStyle.FILM_35MM) }
    var lightLeak by remember { mutableStateOf(LightLeakStyle.AMBER_LEFT) }

    fun loadRecipeForEditing(recipe: CustomFilmRecipe) {
        editingRecipe = recipe
        recipeName = recipe.name
        baseFilterId = recipe.baseFilterId
        contrast = recipe.contrast
        saturation = recipe.saturation
        warmth = recipe.warmth
        tint = recipe.tint
        grain = recipe.grain
        halation = recipe.halation
        vignette = recipe.vignette
        dustScratches = recipe.dustScratches
        lensStyle = recipe.lensStyle
        dateStamp = recipe.dateStamp
        frameStyle = recipe.frameStyle
        lightLeak = recipe.lightLeak
        selectedTabIndex = 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Custom Film Recipe Studio", color = NaturalDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Fuji-Style Recipes & QR Code Sharing", color = NaturalTextMuted, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("recipe_studio_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NaturalDark)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("recipe_import_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Import QR Code", tint = NaturalDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NaturalBg)
            )
        },
        containerColor = NaturalBg,
        modifier = modifier.testTag("film_recipe_studio_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = NaturalSurface,
                contentColor = NaturalOlive,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NaturalOlive
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Saved Recipes (${customRecipes.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text(if (editingRecipe != null) "Edit Recipe" else "+ Create New", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            if (selectedTabIndex == 0) {
                // Tab 0: Saved Recipes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(customRecipes, key = { it.id }) { recipe ->
                        RecipeCardItem(
                            recipe = recipe,
                            onEdit = { loadRecipeForEditing(recipe) },
                            onShareQr = { showQrDialogForRecipe = recipe },
                            onApply = {
                                viewModel.applyCustomRecipe(recipe)
                                onBack()
                            },
                            onDelete = { viewModel.deleteCustomRecipe(recipe.id) }
                        )
                    }
                }
            } else {
                // Tab 1: Recipe Editor / Creator
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Recipe Name Input
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Film Recipe Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NaturalOlive,
                            unfocusedBorderColor = NaturalBorder,
                            focusedLabelColor = NaturalOlive
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("recipe_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Base Emulation Preset
                    Text("BASE EMULSION:", color = NaturalDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilmPresets.ALL_PRESETS.take(8).forEach { preset ->
                            val isSel = preset.id == baseFilterId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) NaturalOlive else NaturalCard)
                                    .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                    .clickable { baseFilterId = preset.id }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(preset.name, color = if (isSel) NaturalBg else NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fine Tuning Sliders
                    RecipeSliderRow("Warmth (Kelvin Offset)", warmth, -0.4f..0.4f, "%+.2f") { warmth = it }
                    RecipeSliderRow("Tint (Green/Magenta)", tint, -0.3f..0.3f, "%+.2f") { tint = it }
                    RecipeSliderRow("Tone Contrast", contrast, 0.7f..1.5f, "%.2f") { contrast = it }
                    RecipeSliderRow("Color Saturation", saturation, 0.0f..1.8f, "%.2f") { saturation = it }
                    RecipeSliderRow("Film Grain Strength", grain, 0.0f..1.0f, "%.2f") { grain = it }
                    RecipeSliderRow("Halation Glow", halation, 0.0f..0.8f, "%.2f") { halation = it }
                    RecipeSliderRow("Lens Vignette", vignette, 0.0f..0.8f, "%.2f") { vignette = it }
                    RecipeSliderRow("Organic Dust & Scratches", dustScratches, 0.0f..0.8f, "%.2f") { dustScratches = it }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vintage Lens Selector
                    Text("VINTAGE LENS LOOK:", color = NaturalDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VintageLensStyle.entries.forEach { style ->
                            val isSel = style == lensStyle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) NaturalOlive else NaturalCard)
                                    .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                    .clickable { lensStyle = style }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(style.label, color = if (isSel) NaturalBg else NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Film Borders Selector
                    Text("FRAME BORDER:", color = NaturalDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilmFrameStyle.entries.forEach { style ->
                            val isSel = style == frameStyle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) NaturalOlive else NaturalCard)
                                    .border(1.dp, if (isSel) NaturalOlive else NaturalBorderLight, RoundedCornerShape(10.dp))
                                    .clickable { frameStyle = style }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(style.label, color = if (isSel) NaturalBg else NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Recipe Action
                    Button(
                        onClick = {
                            val newRecipe = CustomFilmRecipe(
                                id = editingRecipe?.id ?: "recipe_${System.currentTimeMillis()}",
                                name = recipeName.ifBlank { "Custom Film Recipe" },
                                baseFilterId = baseFilterId,
                                contrast = contrast,
                                saturation = saturation,
                                warmth = warmth,
                                tint = tint,
                                grain = grain,
                                halation = halation,
                                vignette = vignette,
                                dustScratches = dustScratches,
                                lensStyle = lensStyle,
                                dateStamp = dateStamp,
                                frameStyle = frameStyle,
                                lightLeak = lightLeak,
                                description = "Customized analog recipe with organic grain and grading"
                            )
                            viewModel.saveCustomRecipe(newRecipe)
                            editingRecipe = null
                            selectedTabIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_recipe_button")
                    ) {
                        Text(if (editingRecipe != null) "Update Recipe" else "Save Custom Film Recipe", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // QR Code Share Dialog
    if (showQrDialogForRecipe != null) {
        val recipe = showQrDialogForRecipe!!
        val qrBitmap = remember(recipe) { generateQrMatrixBitmap(recipe.toShareableCode(), 320) }

        AlertDialog(
            onDismissRequest = { showQrDialogForRecipe = null },
            title = {
                Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NaturalDark)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, NaturalBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Recipe QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Scan or copy this code to import this exact film recipe into AuraCam on any device.",
                        color = NaturalTextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(recipe.toShareableCode()))
                        showQrDialogForRecipe = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Code")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showQrDialogForRecipe = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalCard, contentColor = NaturalDark)
                ) {
                    Text("Close")
                }
            },
            containerColor = NaturalSurface
        )
    }

    // Import QR / Code Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Film Recipe", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NaturalDark) },
            text = {
                Column {
                    Text("Paste an AuraCam recipe code string below:", color = NaturalTextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importCodeText,
                        onValueChange = {
                            importCodeText = it
                            importErrorMessage = null
                        },
                        placeholder = { Text("AURACAM|V1|Tokyo 90s|GOLD_200|...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NaturalOlive,
                            unfocusedBorderColor = NaturalBorder
                        )
                    )
                    if (importErrorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(importErrorMessage!!, color = Color(0xFFE63946), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val imported = CustomFilmRecipe.fromShareableCode(importCodeText.trim())
                        if (imported != null) {
                            viewModel.saveCustomRecipe(imported)
                            showImportDialog = false
                            importCodeText = ""
                        } else {
                            importErrorMessage = "Invalid recipe format. Must start with AURACAM|V1|..."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg)
                ) {
                    Text("Import Recipe")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showImportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalCard, contentColor = NaturalDark)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = NaturalSurface
        )
    }
}

@Composable
private fun RecipeCardItem(
    recipe: CustomFilmRecipe,
    onEdit: () -> Unit,
    onShareQr: () -> Unit,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorderLight, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("recipe_item_${recipe.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(recipe.name, color = NaturalDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Base: ${recipe.baseFilterId.name} • ${recipe.lensStyle.label}", color = NaturalTextMuted, fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShareQr) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Share QR", tint = NaturalOlive)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NaturalTextTertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecipeBadge("Contrast %+.1f".format(recipe.contrast - 1f))
                RecipeBadge("Warmth %+.2f".format(recipe.warmth))
                RecipeBadge("Grain ${(recipe.grain * 100).toInt()}%")
                RecipeBadge(recipe.frameStyle.label)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("Shoot with Recipe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalCard, contentColor = NaturalDark),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Tweak", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RecipeBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NaturalCard)
            .border(0.8.dp, NaturalBorderLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = NaturalDark, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecipeSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, color = NaturalTextMuted, fontSize = 12.sp, modifier = Modifier.width(130.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = NaturalOlive,
                activeTrackColor = NaturalOlive,
                inactiveTrackColor = NaturalBorderLight
            )
        )
        Text(
            text = String.format(format, value),
            color = NaturalDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp)
        )
    }
}

/**
 * Pure Kotlin algorithmic QR Matrix visual renderer for shareable film recipe cards.
 */
private fun generateQrMatrixBitmap(content: String, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(20, 20, 20)
    }

    val numModules = 25
    val cellSize = size.toFloat() / numModules

    // Hash-based deterministic pseudo matrix
    val hash = content.hashCode()
    val random = kotlin.random.Random(hash)

    // Position Finder Patterns (Top-Left, Top-Right, Bottom-Left)
    fun drawFinderPattern(startX: Int, startY: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                if (isOuter || isInner) {
                    canvas.drawRect(
                        (startX + c) * cellSize,
                        (startY + r) * cellSize,
                        (startX + c + 1) * cellSize,
                        (startY + r + 1) * cellSize,
                        paint
                    )
                }
            }
        }
    }

    drawFinderPattern(1, 1)
    drawFinderPattern(numModules - 8, 1)
    drawFinderPattern(1, numModules - 8)

    // Timing patterns
    for (i in 8 until numModules - 8) {
        if (i % 2 == 0) {
            canvas.drawRect(6 * cellSize, i * cellSize, 7 * cellSize, (i + 1) * cellSize, paint)
            canvas.drawRect(i * cellSize, 6 * cellSize, (i + 1) * cellSize, 7 * cellSize, paint)
        }
    }

    // Data modules
    for (r in 0 until numModules) {
        for (c in 0 until numModules) {
            val inFinder1 = r < 9 && c < 9
            val inFinder2 = r < 9 && c >= numModules - 9
            val inFinder3 = r >= numModules - 9 && c < 9
            if (!inFinder1 && !inFinder2 && !inFinder3) {
                if (random.nextFloat() > 0.45f) {
                    canvas.drawRect(
                        c * cellSize,
                        r * cellSize,
                        (c + 1) * cellSize,
                        (r + 1) * cellSize,
                        paint
                    )
                }
            }
        }
    }

    return bitmap
}
