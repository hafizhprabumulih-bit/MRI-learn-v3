package com.example.ui.character

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CharacterAccentPalette
import com.example.data.CharacterCatalog
import com.example.data.CharacterCustomizationState
import com.example.data.MedicalAvatar
import com.example.ui.theme.CharAccentCyan
import com.example.ui.theme.CharAccentMint
import com.example.ui.theme.CharActionDarkText
import com.example.ui.theme.CharActionOrange
import com.example.ui.theme.CharActionYellow
import com.example.ui.theme.CharBorderSlate
import com.example.ui.theme.CharCardSurface
import com.example.ui.theme.CharSpeechBubbleBg
import com.example.ui.theme.CharSpeechBubbleText
import com.example.ui.theme.CharStageBase
import com.example.ui.theme.CharStageDeep
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

enum class NanoBodyZone { HEAD, TORSO, TOOL }

data class QuickFlashChallenge(
    val question: String,
    val optionA: String,
    val optionB: String,
    val correctIndex: Int,
    val moduleRef: String
)

@Composable
fun InteractiveCharacterStage(
    workingState: CharacterCustomizationState,
    currentAvatar: MedicalAvatar,
    specialistRole: String,
    favoriteSequence: String,
    userName: String,
    accentPalette: CharacterAccentPalette,
    bubbleText: String,
    isJumping: Boolean,
    isWaveActive: Boolean,
    interactionCount: Int,
    hasUnsavedChanges: Boolean,
    studyStreakDays: Int,
    shiftMood: Int,
    forcedExpression: Expression?,
    larmorSpinTrigger: Int,
    heartBurstTrigger: Int,
    isCoherenceGameActive: Boolean,
    gamePulseProgress: Float,
    gameHitFlash: Float,
    coherenceScore: Int,
    coherenceCombo: Int,
    activeFlashChallenge: QuickFlashChallenge?,
    onToggleCoherenceGame: () -> Unit,
    onOpenSnapshotStudio: () -> Unit,
    onToggleWave: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onAvatarTap: () -> Unit,
    onBodyZoneTap: (NanoBodyZone) -> Unit,
    onDoubleTapLarmorSpin: () -> Unit,
    onLongPressShiftCard: () -> Unit,
    onRubHead: () -> Unit,
    onCoherenceTap: (Float) -> Unit,
    onAskQuizTip: () -> Unit,
    onAskZoneCheck: () -> Unit,
    onAskFlashChallenge: () -> Unit,
    onAnswerFlashChallenge: (Int) -> Unit,
    onPrevAvatar: () -> Unit,
    onNextAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stage_idle")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_float"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val rfWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rf_wave"
    )

    val jumpScale by animateFloatAsState(
        targetValue = if (isJumping) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "jump_scale"
    )
    val jumpOffsetY by animateFloatAsState(
        targetValue = if (isJumping) -12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "jump_y"
    )
    val larmorSpinAngle by animateFloatAsState(
        targetValue = larmorSpinTrigger * 360f,
        animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing),
        label = "larmor_spin_y"
    )
    val tiltAngle by animateFloatAsState(
        targetValue = if (isJumping) (if (interactionCount % 2 == 0) 4f else -4f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tilt_angle"
    )

    var showHeartBadge by remember { mutableStateOf(false) }
    LaunchedEffect(heartBurstTrigger) {
        if (heartBurstTrigger > 0) {
            showHeartBadge = true
            delay(1100)
            showHeartBadge = false
        }
    }

    val bgItem = CharacterCatalog.getItem(workingState.backgroundId)

    // Deteksi bahaya keselamatan MRI secara deterministik (bukan random modulo)
    val isNoiseHazard = workingState.backgroundId == "bg_scanner_suite" &&
        workingState.equippedHeadset == "headset_none"
    val isCryoHazard = workingState.backgroundId == "bg_chiller_room" &&
        workingState.equippedUniform != "uniform_armor"
    val isSafetyAlertContext = isNoiseHazard || isCryoHazard ||
        bubbleText.contains("bahaya", ignoreCase = true) ||
        bubbleText.contains("proyektil", ignoreCase = true) ||
        bubbleText.contains("kriogen", ignoreCase = true) ||
        bubbleText.contains("kebisingan", ignoreCase = true)
    val isSadContext = bubbleText.contains("terputus", ignoreCase = true) ||
        bubbleText.contains("meluruh", ignoreCase = true) ||
        bubbleText.contains("kurang tepat", ignoreCase = true) ||
        bubbleText.contains("reset", ignoreCase = true)

    val currentExpression = when {
        forcedExpression != null -> forcedExpression
        bubbleText.contains("berhasil disimpan", ignoreCase = true) -> Expression.BANGGA
        isSafetyAlertContext -> Expression.WASPADA
        isSadContext || (studyStreakDays == 0 && interactionCount == 0) -> Expression.SEDIH
        isJumping -> Expression.SENANG
        isWaveActive -> Expression.BERPIKIR
        else -> when (interactionCount % 4) {
            0 -> Expression.NETRAL
            1 -> Expression.SENANG
            2 -> Expression.BERPIKIR
            else -> Expression.BANGGA
        }
    }

    val rfSwayDeg = if (isWaveActive) sin(rfWavePhase * 2f * PI.toFloat()) * 2.6f else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(454.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(CharStageBase)
            .border(1.5.dp, CharBorderSlate, RoundedCornerShape(22.dp))
    ) {
        // 1. Latar Belakang Gambar Ruangan + Panggung Elips + Cincin Mini-Game
        StageBackgroundCanvas(
            backgroundId = workingState.backgroundId,
            accentPalette = accentPalette,
            glowPulse = glowPulse,
            rfWavePhase = rfWavePhase,
            isWaveActive = isWaveActive || isCoherenceGameActive,
            isCoherenceGameActive = isCoherenceGameActive,
            gamePulseProgress = gamePulseProgress,
            gameHitFlash = gameHitFlash
        )

        // 2. Konten Panggung Utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bagian Atas: Judul + 5 Tombol Aksi + Balon Chat & Chip Tanya Nano
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.char_room_title),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharTextPrimary
                        )
                        Text(
                            text = bgItem?.let { "${it.name} · ${it.badgeLabel}" }
                                ?: stringResource(R.string.char_stage_default_bg),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini-Game Koherensi RF
                        StageActionSquareButton(
                            isActive = isCoherenceGameActive,
                            activeColor = CharAccentMint.copy(alpha = 0.28f),
                            activeBorder = CharAccentMint,
                            onClick = onToggleCoherenceGame
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Mini-Game Tangkap Sinyal",
                                tint = if (isCoherenceGameActive) CharAccentMint else CharTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Snapshot Studio Foto Kartu
                        StageActionSquareButton(
                            isActive = false,
                            activeColor = CharCardSurface,
                            activeBorder = CharBorderSlate,
                            onClick = onOpenSnapshotStudio
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Mode Foto Kartu Profil",
                                tint = CharTextPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Toggle Gelombang RF
                        val waveIconDesc = stringResource(R.string.char_wave_toggle_desc)
                        StageActionSquareButton(
                            isActive = isWaveActive,
                            activeColor = accentPalette.primaryColor.copy(alpha = 0.28f),
                            activeBorder = accentPalette.primaryColor,
                            onClick = onToggleWave,
                            modifier = Modifier.semantics { contentDescription = waveIconDesc }
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val w = size.width
                                val h = size.height
                                val cy = h / 2f
                                val wavePath = Path().apply {
                                    moveTo(0f, cy)
                                    cubicTo(w * 0.16f, cy - h * 0.40f, w * 0.34f, cy - h * 0.40f, w * 0.50f, cy)
                                    cubicTo(w * 0.66f, cy + h * 0.40f, w * 0.84f, cy + h * 0.40f, w, cy)
                                }
                                drawPath(
                                    path = wavePath,
                                    color = if (isWaveActive) accentPalette.glowColor else CharTextPrimary,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 2.2.dp.toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                            }
                        }

                        // Reset
                        StageActionSquareButton(
                            isActive = false,
                            activeColor = CharCardSurface,
                            activeBorder = CharBorderSlate,
                            onClick = onReset
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.char_room_reset_desc),
                                tint = CharTextPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Simpan
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (hasUnsavedChanges) CharActionOrange else CharAccentMint)
                                .border(
                                    width = 1.dp,
                                    color = if (hasUnsavedChanges) CharActionYellow else CharAccentCyan,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSave() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(R.string.char_room_save_floating),
                                tint = CharActionDarkText,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Awan Chat (Balon Ucapan)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = bubbleText,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                        label = "speech_bubble"
                    ) { text ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharSpeechBubbleBg.copy(alpha = 0.78f))
                                .border(
                                    width = 1.dp,
                                    color = accentPalette.primaryColor.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onAvatarTap() }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = CharSpeechBubbleText,
                                lineHeight = 15.sp,
                                maxLines = if (activeFlashChallenge != null) 3 else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Canvas(modifier = Modifier.size(width = 16.dp, height = 7.dp)) {
                        val triangle = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path = triangle, color = CharSpeechBubbleBg.copy(alpha = 0.78f))
                    }

                    // Chip Cepat: Status Mini-Game / Tes Kilat / Tanya Nano
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isCoherenceGameActive) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = CharStageDeep.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, CharAccentMint.copy(alpha = 0.75f))
                        ) {
                            Text(
                                text = "⚡ Koherensi: $coherenceScore · Kombo x$coherenceCombo · Ketuk Nano saat cincin menyatu!",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharAccentMint,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (activeFlashChallenge != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickBubbleActionChip(
                                text = "A: ${activeFlashChallenge.optionA}",
                                accentColor = accentPalette.primaryColor,
                                onClick = { onAnswerFlashChallenge(0) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickBubbleActionChip(
                                text = "B: ${activeFlashChallenge.optionB}",
                                accentColor = accentPalette.primaryColor,
                                onClick = { onAnswerFlashChallenge(1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickBubbleActionChip(
                                text = "💡 Tips Kuis",
                                accentColor = accentPalette.primaryColor,
                                onClick = onAskQuizTip
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            QuickBubbleActionChip(
                                text = "🛡️ Cek Zona IV",
                                accentColor = accentPalette.primaryColor,
                                onClick = onAskZoneCheck
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            QuickBubbleActionChip(
                                text = "⚡ Tes Kilat",
                                accentColor = CharAccentMint,
                                onClick = onAskFlashChallenge
                            )
                        }
                    }
                }
            }

            // Bagian Tengah: Panah < , Avatar Nano Diperbesar (242dp) dengan Multi-Gestur, Panah >
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(242.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPrevAvatar
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.char_prev_avatar_desc),
                        tint = CharTextPrimary.copy(alpha = 0.90f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Partikel Mengelus Kepala ("Elus Nano")
                AnimatedVisibility(
                    visible = showHeartBadge,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(400)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 74.dp, y = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = CharStageDeep.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, CharAccentMint)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = CharAccentMint,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "+1 Mood ($shiftMood)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharTextPrimary
                            )
                        }
                    }
                }

                // Avatar Berlapis "Nano" (242dp) + Multi-Gestur (Tap Zona, Double-Tap Larmor Spin, Long-Press, Usap Kepala)
                var rubDragAccum by remember { mutableFloatStateOf(0f) }
                var dragStartedTop by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(242.dp)
                        .offset(y = (floatOffset + jumpOffsetY).dp)
                        .graphicsLayer {
                            scaleX = jumpScale
                            scaleY = jumpScale
                            rotationZ = tiltAngle + rfSwayDeg
                            rotationY = larmorSpinAngle
                            cameraDistance = 12f * density
                        }
                        .pointerInput(isCoherenceGameActive, gamePulseProgress, currentAvatar.id) {
                            detectTapGestures(
                                onDoubleTap = { onDoubleTapLarmorSpin() },
                                onLongPress = { onLongPressShiftCard() },
                                onTap = { offset ->
                                    if (isCoherenceGameActive) {
                                        onCoherenceTap(gamePulseProgress)
                                    } else {
                                        val fracY = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                        val fracX = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                        val zone = when {
                                            fracY < 0.44f -> NanoBodyZone.HEAD
                                            fracX >= 0.62f -> NanoBodyZone.TOOL
                                            else -> NanoBodyZone.TORSO
                                        }
                                        onBodyZoneTap(zone)
                                    }
                                }
                            )
                        }
                        .pointerInput(currentAvatar.id) {
                            detectHorizontalDragGestures(
                                onDragStart = { startOffset ->
                                    dragStartedTop = startOffset.y < size.height * 0.48f
                                    rubDragAccum = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    if (dragStartedTop) {
                                        rubDragAccum += abs(dragAmount)
                                        if (rubDragAccum >= 48f) {
                                            rubDragAccum = 0f
                                            onRubHead()
                                        }
                                    }
                                },
                                onDragEnd = {
                                    dragStartedTop = false
                                    rubDragAccum = 0f
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        role = currentAvatar.id,
                        equipped = workingState,
                        expression = currentExpression,
                        modifier = Modifier.size(242.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNextAvatar
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.char_next_avatar_desc),
                        tint = CharTextPrimary.copy(alpha = 0.90f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bagian Bawah Panggung: Gelar Spesialis & Sekuens Favorit
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (userName.isNotBlank() && userName != currentAvatar.label) {
                        "$userName · $specialistRole"
                    } else {
                        specialistRole
                    },
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.char_fav_sequence_fmt, favoriteSequence),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StageActionSquareButton(
    isActive: Boolean,
    activeColor: androidx.compose.ui.graphics.Color,
    activeBorder: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) activeColor else CharCardSurface.copy(alpha = 0.88f))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) activeBorder else CharBorderSlate,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun QuickBubbleActionChip(
    text: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(99.dp),
        color = CharStageDeep.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.60f))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}
