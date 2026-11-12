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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.example.ui.theme.CharAccentBlue
import com.example.ui.theme.CharAccentCyan
import com.example.ui.theme.CharAccentMint
import com.example.ui.theme.CharAccentSky
import com.example.ui.theme.CharAccentTeal
import com.example.ui.theme.CharActionOrange
import com.example.ui.theme.CharActionPillBg
import com.example.ui.theme.CharActionYellow
import com.example.ui.theme.CharBorderSlate
import com.example.ui.theme.CharCardSurface
import com.example.ui.theme.CharClockBg
import com.example.ui.theme.CharClockRed
import com.example.ui.theme.CharSpeechBubbleBg
import com.example.ui.theme.CharSpeechBubbleText
import com.example.ui.theme.CharStageBase
import com.example.ui.theme.CharStageDeep
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary

enum class AvatarInteractionMode(val labelRes: Int) {
    GREET(R.string.char_action_sapa),
    RF_SCAN(R.string.char_action_pindai),
    SAFETY_CHECK(R.string.char_action_zona4),
    HOLO_POSE(R.string.char_action_fokus)
}

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
    isScanningRf: Boolean,
    activeMode: AvatarInteractionMode,
    interactionCount: Int,
    showActionPanel: Boolean,
    onToggleActionPanel: () -> Unit,
    onAvatarTap: () -> Unit,
    onPrevAvatar: () -> Unit,
    onNextAvatar: () -> Unit,
    onSelectMode: (AvatarInteractionMode) -> Unit,
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
    val tiltAngle by animateFloatAsState(
        targetValue = if (isJumping) (if (interactionCount % 2 == 0) 4f else -4f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tilt_angle"
    )

    val bgItem = CharacterCatalog.getItem(workingState.backgroundId)

    // Pilih varian ekspresi apabila avatar adalah Radiografer MRI
    val resolvedAvatarRes = if (currentAvatar.id == "radiografer_mri") {
        when (activeMode) {
            AvatarInteractionMode.GREET -> when (interactionCount % 4) {
                1 -> R.drawable.avatar_radiografer_senang
                2 -> R.drawable.avatar_radiografer_bangga
                3 -> R.drawable.avatar_radiografer_semangat
                else -> R.drawable.avatar_radiografer_mri
            }
            AvatarInteractionMode.RF_SCAN -> R.drawable.avatar_radiografer_berpikir
            AvatarInteractionMode.SAFETY_CHECK -> R.drawable.avatar_radiografer_bangga
            AvatarInteractionMode.HOLO_POSE -> R.drawable.avatar_radiografer_semangat
        }
    } else {
        currentAvatar.drawableRes
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(CharStageBase)
            .border(1.5.dp, CharBorderSlate, RoundedCornerShape(22.dp))
    ) {
        // 1. Latar Belakang Gambar Ruangan + Panggung Elips
        StageBackgroundCanvas(
            backgroundId = workingState.backgroundId,
            accentPalette = accentPalette,
            glowPulse = glowPulse,
            rfWavePhase = rfWavePhase,
            isScanningRf = isScanningRf,
            isHoloPose = activeMode == AvatarInteractionMode.HOLO_POSE
        )

        // 2. Konten Panggung Utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bagian Atas: Label Ruangan, Jam Digital, & Toggle Aksi + Balon Ucapan di Atas Avatar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bgItem?.let { "${it.name} · ${it.badgeLabel}" }
                            ?: stringResource(R.string.char_stage_default_bg),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharTextSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CharClockBg.copy(alpha = 0.88f))
                                .border(1.dp, CharClockRed.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (workingState.backgroundId == "bg_scanner_suite") {
                                    stringResource(R.string.char_stage_clock_3t)
                                } else {
                                    stringResource(R.string.char_stage_clock_15t)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharClockRed
                            )
                        }

                        // Toggle Pembuka/Penutup Tombol Aksi Kiri
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (showActionPanel) accentPalette.primaryColor else CharCardSurface)
                                .border(1.dp, accentPalette.primaryColor, RoundedCornerShape(8.dp))
                                .clickable { onToggleActionPanel() }
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.char_stage_action_toggle),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showActionPanel) CharStageDeep else CharTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Balon Ucapan di Atas Avatar (Tidak Menimpa Tombol Aksi di Sisi Kiri)
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
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharSpeechBubbleBg)
                                .border(1.dp, accentPalette.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .clickable { onAvatarTap() }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = CharSpeechBubbleText,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    // Segitiga Penunjuk Balon Ucapan ke Arah Kepala Avatar
                    Canvas(modifier = Modifier.size(width = 18.dp, height = 9.dp)) {
                        val triangle = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path = triangle, color = CharSpeechBubbleBg)
                    }
                }
            }

            // Bagian Tengah: Tombol Aksi Vertikal di Sisi Kiri + Avatar 220dp di Tengah + Navigasi Karakter di Kanan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(232.dp)
            ) {
                // Avatar 220dp + Overlay Item Kustomisasi (Berdiri Lebar Tanpa Lingkaran)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = if (showActionPanel) 18.dp else 0.dp)
                        .width(235.dp)
                        .height(225.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAvatarTap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarEquippedVisualOverlay(
                        workingState = workingState,
                        accentPalette = accentPalette,
                        floatOffset = floatOffset + jumpOffsetY,
                        glowPulse = glowPulse
                    )

                    Image(
                        painter = painterResource(id = resolvedAvatarRes),
                        contentDescription = stringResource(R.string.char_avatar_tap_desc, currentAvatar.label),
                        modifier = Modifier
                            .width(230.dp)
                            .height(220.dp)
                            .offset(y = (floatOffset + jumpOffsetY).dp)
                            .graphicsLayer {
                                scaleX = jumpScale
                                scaleY = jumpScale
                                rotationZ = tiltAngle
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                // Panel Aksi Vertikal di Sisi Kiri Panggung dengan AnimatedVisibility (slide + fade)
                // Lebar 64dp, Tinggi 44dp per tombol; menonjolkan Sapa & Pindai RF serta menjaga keempat aksi
                androidx.compose.animation.AnimatedVisibility(
                    visible = showActionPanel,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        AvatarInteractionMode.entries.forEach { mode ->
                            val isSelected = activeMode == mode
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) CharCardSurface
                                        else CharActionPillBg.copy(alpha = 0.90f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentPalette.primaryColor else CharBorderSlate,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectMode(mode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(mode.labelRes),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) accentPalette.glowColor else CharTextPrimary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Tombol Navigasi Cepat Karakter Sebelumnya / Berikutnya di Sisi Kanan Panggung
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(
                        onClick = onPrevAvatar,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharActionPillBg.copy(alpha = 0.88f))
                            .border(1.dp, CharBorderSlate, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.char_prev_avatar_desc),
                            tint = CharTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onNextAvatar,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharActionPillBg.copy(alpha = 0.88f))
                            .border(1.dp, CharBorderSlate, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.char_next_avatar_desc),
                            tint = CharTextPrimary
                        )
                    }
                }
            }

            // Bagian Bawah Panggung: Gelar Spesialis & Sekuens Favorit (Baris Tombol Mode Bawah Dihapus)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.char_fav_sequence_fmt, favoriteSequence),
                    fontSize = 12.sp,
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
fun AvatarEquippedVisualOverlay(
    workingState: CharacterCustomizationState,
    accentPalette: CharacterAccentPalette,
    floatOffset: Float,
    glowPulse: Float
) {
    Canvas(
        modifier = Modifier
            .width(235.dp)
            .height(220.dp)
            .offset(y = floatOffset.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // 1. Aura & Kerah Seragam yang Dipilih
        val uniformColor = when (workingState.equippedUniform) {
            "uniform_scrub_mint" -> CharAccentMint
            "uniform_standard" -> CharTextPrimary
            "uniform_scrub_blue" -> CharAccentBlue
            "uniform_scrub_teal" -> CharAccentTeal
            "uniform_apron_shield" -> CharActionOrange
            else -> CharAccentCyan
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    uniformColor.copy(alpha = 0.25f * glowPulse),
                    accentPalette.primaryColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(cx, cy + 22f),
                radius = size.minDimension * 0.56f
            ),
            radius = size.minDimension * 0.56f,
            center = Offset(cx, cy + 22f)
        )

        // Lencana / Sabuk Seragam di Bagian Bawah Dada Robot
        if (workingState.equippedUniform == "uniform_apron_shield") {
            drawRoundRect(
                color = CharActionOrange.copy(alpha = 0.85f),
                topLeft = Offset(cx - 46f, cy + 58f),
                size = Size(92f, 12f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = CharActionYellow,
                topLeft = Offset(cx - 30f, cy + 60f),
                size = Size(60f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        } else if (workingState.equippedUniform == "uniform_armor") {
            drawCircle(
                color = CharAccentCyan.copy(alpha = 0.8f * glowPulse),
                radius = 18f,
                center = Offset(cx, cy + 44f),
                style = Stroke(width = 3.5f)
            )
        }

        // 2. Visualisasi Kepala & Visor di Kepala Robot
        when (workingState.equippedHair) {
            "hair_visor" -> {
                drawRoundRect(
                    color = CharAccentTeal.copy(alpha = 0.42f),
                    topLeft = Offset(cx - 56f, cy - 36f),
                    size = Size(112f, 24f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = CharAccentCyan,
                    topLeft = Offset(cx - 56f, cy - 36f),
                    size = Size(112f, 24f),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 3f)
                )
            }
            "hair_cyber" -> {
                drawOval(
                    color = CharAccentSky.copy(alpha = 0.9f * glowPulse),
                    topLeft = Offset(cx - 64f, 8f),
                    size = Size(128f, 22f),
                    style = Stroke(width = 4f)
                )
            }
            "hair_cap" -> {
                drawArc(
                    color = CharAccentMint.copy(alpha = 0.65f),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = true,
                    topLeft = Offset(cx - 58f, 18f),
                    size = Size(116f, 48f)
                )
            }
            "hair_goggles" -> {
                drawCircle(
                    color = CharActionOrange,
                    radius = 16f,
                    center = Offset(cx - 22f, cy - 30f),
                    style = Stroke(width = 3.5f)
                )
                drawCircle(
                    color = CharActionOrange,
                    radius = 16f,
                    center = Offset(cx + 22f, cy - 30f),
                    style = Stroke(width = 3.5f)
                )
            }
            "hair_crown_rf" -> {
                drawArc(
                    color = CharTextPrimary.copy(alpha = 0.85f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 68f, 10f),
                    size = Size(136f, 76f),
                    style = Stroke(width = 4f)
                )
            }
        }

        // 3. Gelombang Akustik Headset di Sisi Telinga
        if (workingState.equippedHeadset != "headset_none") {
            val hsColor = when (workingState.equippedHeadset) {
                "headset_neural" -> CharAccentMint
                "headset_pneumatic" -> CharAccentTeal
                "headset_holo" -> CharActionOrange
                else -> CharAccentCyan
            }
            for (i in 1..2) {
                val r = 14f + i * 9f * glowPulse
                drawArc(
                    color = hsColor.copy(alpha = 0.68f / i),
                    startAngle = 135f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - 88f - r, cy - 18f - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = hsColor.copy(alpha = 0.68f / i),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx + 88f - r, cy - 18f - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                )
            }
        }

        // 4. Indikator Alat Klinis Terpasang di Sisi Kanan Bawah Karakter
        val accColor = when (workingState.equippedAccessory) {
            "acc_badge_zona4" -> CharAccentMint
            "acc_phantom" -> CharAccentSky
            "acc_clipboard" -> CharAccentTeal
            "acc_oximeter" -> CharActionOrange
            "acc_coil_surface" -> CharTextPrimary
            else -> CharAccentCyan
        }
        val badgeCenter = Offset(cx + 78f, cy + 48f)
        drawCircle(
            color = CharStageDeep.copy(alpha = 0.85f),
            radius = 18f,
            center = badgeCenter
        )
        drawCircle(
            color = accColor,
            radius = 18f,
            center = badgeCenter,
            style = Stroke(width = 2.5f)
        )
        drawCircle(
            color = accColor.copy(alpha = 0.85f * glowPulse),
            radius = 7f,
            center = badgeCenter
        )
    }
}
