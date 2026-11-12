package com.example.ui.character

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
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
    isWaveActive: Boolean,
    activeMode: AvatarInteractionMode,
    interactionCount: Int,
    hasUnsavedChanges: Boolean,
    onToggleWave: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onAvatarTap: () -> Unit,
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
    val tiltAngle by animateFloatAsState(
        targetValue = if (isJumping) (if (interactionCount % 2 == 0) 4f else -4f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tilt_angle"
    )

    val bgItem = CharacterCatalog.getItem(workingState.backgroundId)

    // Pilih ekspresi visor Nano (berlaku untuk SEMUA 9 peran) berdasarkan kejadian & konteks klinis
    val isSafetyAlertContext = bubbleText.contains("Zona IV", ignoreCase = true) ||
        bubbleText.contains("proyektil", ignoreCase = true) ||
        bubbleText.contains("Quench", ignoreCase = true) ||
        bubbleText.contains("luka bakar", ignoreCase = true) ||
        bubbleText.contains("SAR", ignoreCase = true) ||
        workingState.backgroundId == "bg_scanner_suite"

    val currentExpression = when {
        bubbleText.contains("berhasil disimpan", ignoreCase = true) -> Expression.BANGGA
        isSafetyAlertContext && interactionCount % 3 == 0 -> Expression.WASPADA
        isJumping -> Expression.SENANG
        else -> when (interactionCount % 6) {
            0 -> if (isSafetyAlertContext) Expression.WASPADA else Expression.NETRAL
            1 -> Expression.SENANG
            2 -> Expression.BERPIKIR
            3 -> Expression.WASPADA
            4 -> Expression.BANGGA
            else -> Expression.SEDIH
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(376.dp)
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
            isScanningRf = isScanningRf || isWaveActive,
            isHoloPose = activeMode == AvatarInteractionMode.HOLO_POSE
        )

        // 2. Konten Panggung Utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bagian Atas: Judul "Ruang Karakter", Subjudul Latar, serta Simbol Reset & Simbol Simpan (Disket) di Dalam Card
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.char_room_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharTextPrimary
                        )
                        Text(
                            text = bgItem?.let { "${it.name} · ${it.badgeLabel}" }
                                ?: stringResource(R.string.char_stage_default_bg),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharTextSecondary
                        )
                    }

                    // Tombol Ikon Gelombang RF, Reset & Toggle Ikon Simpan (Disket) Tanpa Tulisan di Dalam Kartu Ruang Karakter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Toggle Kecil Ikon Gelombang di Dalam Kartu Ruang Karakter
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isWaveActive) accentPalette.primaryColor.copy(alpha = 0.28f)
                                    else CharCardSurface.copy(alpha = 0.88f)
                                )
                                .border(
                                    width = if (isWaveActive) 1.5.dp else 1.dp,
                                    color = if (isWaveActive) accentPalette.primaryColor else CharBorderSlate,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onToggleWave() },
                            contentAlignment = Alignment.Center
                        ) {
                            val waveIconDesc = stringResource(R.string.char_wave_toggle_desc)
                            Canvas(
                                modifier = Modifier.size(20.dp)
                            ) {
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
                                        width = 2.4.dp.toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharCardSurface.copy(alpha = 0.88f))
                                .border(1.dp, CharBorderSlate, RoundedCornerShape(12.dp))
                                .clickable { onReset() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.char_room_reset_desc),
                                tint = CharTextPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (hasUnsavedChanges) CharActionOrange else CharAccentMint)
                                .border(
                                    width = 1.dp,
                                    color = if (hasUnsavedChanges) CharActionYellow else CharAccentCyan,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSave() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(R.string.char_room_save_floating),
                                tint = CharActionDarkText,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Awan Chat (Balon Ucapan) Agak Transparan di Atas Avatar
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
                                .background(CharSpeechBubbleBg.copy(alpha = 0.74f))
                                .border(
                                    width = 1.dp,
                                    color = accentPalette.primaryColor.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onAvatarTap() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = CharSpeechBubbleText,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    // Segitiga Penunjuk Awan Chat Transparan
                    Canvas(modifier = Modifier.size(width = 18.dp, height = 8.dp)) {
                        val triangle = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path = triangle, color = CharSpeechBubbleBg.copy(alpha = 0.74f))
                    }
                }
            }

            // Bagian Tengah: Panah < di Kiri (Tanpa Kotak Toggle), Avatar Berlapis Nano 220dp di Tengah, & Panah > di Kanan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
            ) {
                // Panah Kiri `<` Tanpa Latar/Kotak Toggle
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

                // Avatar Berlapis "Nano" (220dp) di Tengah Panggung
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(216.dp)
                        .offset(y = (floatOffset + jumpOffsetY).dp)
                        .graphicsLayer {
                            scaleX = jumpScale
                            scaleY = jumpScale
                            rotationZ = tiltAngle
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAvatarTap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        role = currentAvatar.id,
                        equipped = workingState,
                        expression = currentExpression,
                        modifier = Modifier.size(216.dp)
                    )
                }

                // Panah Kanan `>` Tanpa Latar/Kotak Toggle
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

            // Bagian Bawah Panggung: Gelar Spesialis & Sekuens Favorit (Hanya Muncul di Ruang Karakter)
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
                    fontSize = 15.sp,
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
