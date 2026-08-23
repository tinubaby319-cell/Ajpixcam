package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FilmRollAlbum
import com.example.data.model.FilmFilterId
import com.example.ui.theme.NaturalBg
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalBorderLight
import com.example.ui.theme.NaturalCard
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalTextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmLabScreen(
    viewModel: CameraViewModel,
    onOpenContactSheetMaker: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filmRolls by viewModel.filmRolls.collectAsState()
    val scope = rememberCoroutineScope()

    var isDevelopingActive by remember { mutableStateOf(false) }
    var devStep by remember { mutableIntStateOf(0) }
    var devProgress by remember { mutableStateOf(0.0f) }

    val darkroomSteps = listOf(
        "Step 1/4: Developer Chemical Bath (Agitation)",
        "Step 2/4: Acetic Acid Stop Bath",
        "Step 3/4: Rapid Fixer Solution (Silver Halide)",
        "Step 4/4: Wash Bath & Film Drying Rack"
    )

    fun startDarkroomDevelopment() {
        isDevelopingActive = true
        devStep = 0
        devProgress = 0f

        scope.launch {
            for (step in 0..3) {
                devStep = step
                for (p in 1..25) {
                    delay(80)
                    devProgress = (step * 25 + p) / 100f
                }
            }
            delay(500)
            isDevelopingActive = false

            // Add newly developed roll to archive
            viewModel.completeAndArchiveCurrentRoll()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Vintage Film Lab & Darkroom", color = NaturalDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Chemical Development & Roll Archives", color = NaturalTextMuted, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("film_lab_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NaturalDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NaturalBg)
            )
        },
        containerColor = NaturalBg,
        modifier = modifier.testTag("film_lab_screen")
    ) { paddingValues ->
        if (isDevelopingActive) {
            // Interactive Red Safelight Darkroom Animation
            DarkroomDevelopmentView(
                currentStepName = darkroomSteps[devStep],
                stepIndex = devStep,
                progress = devProgress
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Roll In Camera
                item {
                    ActiveRollCard(
                        rollName = uiState.userSettings.activeRollName,
                        currentShots = uiState.userSettings.rollExpCount,
                        maxShots = uiState.userSettings.rollMaxExp,
                        onDevelopClick = { startDarkroomDevelopment() }
                    )
                }

                // Proof Sheet Maker Shortcut Card
                item {
                    ProofSheetBannerCard(onClick = onOpenContactSheetMaker)
                }

                // Archived Developed Rolls Header
                item {
                    Text(
                        text = "DEVELOPED ROLL ARCHIVES (${filmRolls.size})",
                        color = NaturalOlive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }

                // Rolls list
                items(filmRolls, key = { it.id }) { roll ->
                    DevelopedRollItem(roll = roll, onMakeContactSheet = onOpenContactSheetMaker)
                }
            }
        }
    }
}

@Composable
private fun ActiveRollCard(
    rollName: String,
    currentShots: Int,
    maxShots: Int,
    onDevelopClick: () -> Unit
) {
    val progress = (currentShots.toFloat() / maxShots).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NaturalOlive),
        modifier = Modifier.fillMaxWidth().testTag("active_roll_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NaturalOlive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = NaturalBg, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("LOADED ROLL IN CAMERA", color = NaturalOlive, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(rollName, color = NaturalDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NaturalCard)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("$currentShots / $maxShots EXP", color = NaturalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NaturalOlive,
                trackColor = NaturalBorderLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDevelopClick,
                colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive, contentColor = NaturalBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("develop_roll_button")
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Develop Roll in Darkroom", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ProofSheetBannerCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NaturalCard)
            .border(1.dp, NaturalBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("banner_make_contact_sheet")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NaturalDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = NaturalBg, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("35mm Contact Sheet Maker", color = NaturalDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Collage multiple roll exposures with real film sprockets and metadata imprint for print/story.", color = NaturalTextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DevelopedRollItem(roll: FilmRollAlbum, onMakeContactSheet: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(roll.completedTimestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorderLight, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NaturalOlive, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(roll.title, color = NaturalDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Film: ${roll.filmName} • $dateStr • ${roll.totalShots} Shots", color = NaturalTextMuted, fontSize = 11.sp)
            }

            Button(
                onClick = onMakeContactSheet,
                colors = ButtonDefaults.buttonColors(containerColor = NaturalCard, contentColor = NaturalDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Contact Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DarkroomDevelopmentView(
    currentStepName: String,
    stepIndex: Int,
    progress: Float
) {
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotationAnim.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF140000)), // Deep darkroom red ambience
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Safelight Red Glow Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B0000).copy(alpha = 0.5f))
                    .border(2.dp, Color(0xFFFF2222), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = Color(0xFFFF4444),
                    modifier = Modifier
                        .size(54.dp)
                        .rotate(rotationAnim.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "DARKROOM CHEMICAL LAB",
                color = Color(0xFFFF3333),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentStepName,
                color = Color(0xFFFFE0E0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFFFF2222),
                trackColor = Color(0xFF330000)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${(progress * 100).toInt()}% COMPLETED",
                color = Color(0xFFFF8888),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
