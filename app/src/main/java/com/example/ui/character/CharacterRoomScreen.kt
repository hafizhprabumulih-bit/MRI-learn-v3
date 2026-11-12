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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharacterAccentPalette
import com.example.data.CharacterCatalog
import com.example.data.CharacterCategory
import com.example.data.CharacterCustomizationState
import com.example.data.CharacterItem
import com.example.data.MedicalAvatar
import com.example.data.MriRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainTab
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private enum class AvatarInteractionMode(val label: String) {
    GREET("💬 Tips MRI"),
    RF_SCAN("📡 Pindai RF"),
    SAFETY_CHECK("🛡️ Cek Zona IV"),
    HOLO_POSE("✨ Mode Fokus")
}

@Composable
fun CharacterRoomScreen(
    state: MriUiState,
    viewModel: MriViewModel
) {
    var workingState by remember(state.characterCustomization) {
        mutableStateOf(state.characterCustomization)
    }
    var selectedCategory by rememberSaveable { mutableStateOf(CharacterCategory.KARAKTER) }

    // State Interaktivitas Avatar
    var interactionCount by rememberSaveable { mutableIntStateOf(0) }
    var greetingIndex by rememberSaveable { mutableIntStateOf(0) }
    var activeMode by rememberSaveable { mutableStateOf(AvatarInteractionMode.GREET) }
    var customBubbleText by rememberSaveable { mutableStateOf<String?>(null) }
    var isJumping by remember { mutableStateOf(false) }
    var isScanningRf by remember { mutableStateOf(false) }

    val avatars = MriRepository.avatars
    val currentAvatarIndex = avatars.indexOfFirst { it.id == workingState.avatarId }.coerceAtLeast(0)
    val currentAvatar = avatars[currentAvatarIndex]
    val specialistProfile = CharacterCatalog.getSpecialistProfile(workingState.avatarId)
    val accentPalette = CharacterCatalog.getAccentPalette(workingState.accentColor)

    LaunchedEffect(isJumping) {
        if (isJumping) {
            delay(360)
            isJumping = false
        }
    }

    LaunchedEffect(isScanningRf) {
        if (isScanningRf) {
            delay(1800)
            isScanningRf = false
        }
    }

    fun triggerAvatarTap(customMessage: String? = null) {
        interactionCount++
        isJumping = true
        if (customMessage != null) {
            customBubbleText = customMessage
        } else {
            customBubbleText = null
            greetingIndex = (greetingIndex + 1) % specialistProfile.greetings.size
        }
    }

    val displayedBubbleText = customBubbleText
        ?: specialistProfile.greetings.getOrElse(greetingIndex % specialistProfile.greetings.size) {
            specialistProfile.greetings.first()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071A2B))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // 1. Header Ruang Karakter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RUANG KARAKTER • ZONA PERSIAPAN MRI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentPalette.primaryColor,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "Kustomisasi Karakter & Perlengkapan Klinis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6F2FB)
                )
            }
            OutlinedButton(
                onClick = {
                    val defaultState = CharacterCustomizationState()
                    workingState = defaultState
                    viewModel.resetCharacterCustomization()
                    triggerAvatarTap("Perlengkapan dikembalikan ke setelan standar Ruang Kontrol MRI!")
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1D4468)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF0F2A45),
                    contentColor = Color(0xFFBFD9EC)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Kustomisasi",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Panggung Interaktif Lebar (Tanpa Lingkaran di Sekitar Avatar)
        InteractiveCharacterStage(
            workingState = workingState,
            currentAvatar = currentAvatar,
            specialistRole = specialistProfile.roleTitle,
            favoriteSequence = specialistProfile.favoriteSequence,
            userName = state.userName.ifBlank { currentAvatar.label },
            accentPalette = accentPalette,
            bubbleText = displayedBubbleText,
            isJumping = isJumping,
            isScanningRf = isScanningRf,
            activeMode = activeMode,
            interactionCount = interactionCount,
            onAvatarTap = {
                triggerAvatarTap()
            },
            onPrevAvatar = {
                val prevIndex = if (currentAvatarIndex - 1 < 0) avatars.lastIndex else currentAvatarIndex - 1
                val newAv = avatars[prevIndex]
                workingState = workingState.copy(avatarId = newAv.id)
                val prof = CharacterCatalog.getSpecialistProfile(newAv.id)
                triggerAvatarTap("${newAv.label} siap bertugas: ${prof.zoneSpecialty}!")
            },
            onNextAvatar = {
                val nextIndex = (currentAvatarIndex + 1) % avatars.size
                val newAv = avatars[nextIndex]
                workingState = workingState.copy(avatarId = newAv.id)
                val prof = CharacterCatalog.getSpecialistProfile(newAv.id)
                triggerAvatarTap("${newAv.label} siap bertugas: ${prof.zoneSpecialty}!")
            },
            onSelectMode = { mode ->
                activeMode = mode
                when (mode) {
                    AvatarInteractionMode.GREET -> {
                        triggerAvatarTap()
                    }
                    AvatarInteractionMode.RF_SCAN -> {
                        isScanningRf = true
                        val b0Text = if (workingState.backgroundId == "bg_scanner_suite") "3,0T (f₀ = 127,74 MHz)" else "1,5T (f₀ = 63,87 MHz)"
                        triggerAvatarTap("Pulsa RF 90° dipancarkan! Resonansi spin ¹H terdeteksi pada $b0Text.")
                    }
                    AvatarInteractionMode.SAFETY_CHECK -> {
                        val hairName = CharacterCatalog.getItem(workingState.equippedHair)?.name ?: "Standar"
                        val uniformName = CharacterCatalog.getItem(workingState.equippedUniform)?.name ?: "Scrub"
                        val accName = CharacterCatalog.getItem(workingState.equippedAccessory)?.name ?: "Detektor"
                        triggerAvatarTap("Skrining Zona IV Lulus ✅ [$hairName • $uniformName • $accName] 100% Bebas Feromagnetik!")
                    }
                    AvatarInteractionMode.HOLO_POSE -> {
                        triggerAvatarTap("Mode Fokus Aktif ✨ Menampilkan telemetri sekuens unggulan: ${specialistProfile.favoriteSequence}.")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Bar Pilihan Warna Aksen & Panggung
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F2A45))
                .border(1.dp, Color(0xFF1D4468), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WARNA AKSEN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFBFD9EC),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = accentPalette.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentPalette.primaryColor
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterCatalog.accentPalettes.forEach { palette ->
                    val isSelected = workingState.accentColor == palette.id
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(palette.primaryColor)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFE6F2FB) else Color(0xFF071A2B),
                                shape = CircleShape
                            )
                            .clickable {
                                workingState = workingState.copy(accentColor = palette.id)
                                triggerAvatarTap("Warna panggung & indikator diubah ke ${palette.label}!")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = palette.label,
                                tint = Color(0xFF071A2B),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Tab Kategori Lengkap (Karakter, Kepala & Visor, Seragam, Headset, Alat Klinis, Latar Ruang)
        ScrollableTabRow(
            selectedTabIndex = CharacterCategory.entries.indexOf(selectedCategory),
            containerColor = Color(0xFF0F2A45),
            contentColor = accentPalette.primaryColor,
            edgePadding = 4.dp,
             modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF1D4468), RoundedCornerShape(14.dp)),
            indicator = {},
            divider = {}
        ) {
            CharacterCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    modifier = Modifier
                        .padding(horizontal = 3.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) accentPalette.primaryColor else Color.Transparent),
                    text = {
                        Text(
                            text = category.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF04121F) else Color(0xFFBFD9EC)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Grid Kustomisasi Terisi Penuh (Tanpa Item Kosong)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedCategory == CharacterCategory.KARAKTER) {
                SpecialistAvatarGrid(
                    avatars = avatars,
                    selectedAvatarId = workingState.avatarId,
                    accentPalette = accentPalette,
                    onSelectAvatar = { avatar ->
                        workingState = workingState.copy(avatarId = avatar.id)
                        val prof = CharacterCatalog.getSpecialistProfile(avatar.id)
                        triggerAvatarTap("Halo! Saya ${prof.roleTitle}. Sekuens favorit: ${prof.favoriteSequence}.")
                    }
                )
            } else {
                val currentItems = when (selectedCategory) {
                    CharacterCategory.KARAKTER -> emptyList()
                    CharacterCategory.RAMBUT -> CharacterCatalog.hairItems
                    CharacterCategory.SERAGAM -> CharacterCatalog.uniformItems
                    CharacterCategory.HEADSET -> CharacterCatalog.headsetItems
                    CharacterCategory.AKSESORIS -> CharacterCatalog.accessoryItems
                    CharacterCategory.LATAR -> CharacterCatalog.backgroundItems
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentItems, key = { it.id }) { item ->
                        val isSelected = when (selectedCategory) {
                            CharacterCategory.KARAKTER -> false
                            CharacterCategory.RAMBUT -> workingState.equippedHair == item.id
                            CharacterCategory.SERAGAM -> workingState.equippedUniform == item.id
                            CharacterCategory.HEADSET -> workingState.equippedHeadset == item.id
                            CharacterCategory.AKSESORIS -> workingState.equippedAccessory == item.id
                            CharacterCategory.LATAR -> workingState.backgroundId == item.id
                        }
                        val isUnlocked = CharacterCatalog.isItemUnlocked(
                            item = item,
                            completedNodeCodes = state.completedNodeCodes,
                            modules = state.modulesList
                        )

                        CharacterItemCard(
                            item = item,
                            isSelected = isSelected,
                            isUnlocked = isUnlocked,
                            accentPalette = accentPalette,
                            onClick = {
                                workingState = when (selectedCategory) {
                                    CharacterCategory.KARAKTER -> workingState
                                    CharacterCategory.RAMBUT -> workingState.copy(equippedHair = item.id)
                                    CharacterCategory.SERAGAM -> workingState.copy(equippedUniform = item.id)
                                    CharacterCategory.HEADSET -> workingState.copy(equippedHeadset = item.id)
                                    CharacterCategory.AKSESORIS -> workingState.copy(equippedAccessory = item.id)
                                    CharacterCategory.LATAR -> workingState.copy(backgroundId = item.id)
                                }
                                triggerAvatarTap("${item.name} dipasang! Manfaat klinis: ${item.clinicalBenefit}.")
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6. Tombol Aksi Utama (Oranye #FF8A1F sesuai aturan desain)
        Button(
            onClick = {
                viewModel.updateCharacterCustomization(workingState)
                viewModel.selectTab(MainTab.BERANDA)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF8A1F),
                contentColor = Color(0xFF1A0E00)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Simpan dan Kembali",
                tint = Color(0xFF1A0E00),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simpan dan Kembali ke Beranda",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A0E00)
            )
        }
    }
}

@Composable
private fun InteractiveCharacterStage(
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
    onAvatarTap: () -> Unit,
    onPrevAvatar: () -> Unit,
    onNextAvatar: () -> Unit,
    onSelectMode: (AvatarInteractionMode) -> Unit
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
        targetValue = if (isJumping) 1.09f else 1.0f,
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
        targetValue = if (isJumping) (if (interactionCount % 2 == 0) 5f else -5f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tilt_angle"
    )

    val bgItem = CharacterCatalog.getItem(workingState.backgroundId)
    val hairItem = CharacterCatalog.getItem(workingState.equippedHair)
    val uniformItem = CharacterCatalog.getItem(workingState.equippedUniform)
    val headsetItem = CharacterCatalog.getItem(workingState.equippedHeadset)
    val accessoryItem = CharacterCatalog.getItem(workingState.equippedAccessory)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(276.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B2740))
            .border(1.5.dp, accentPalette.primaryColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
    ) {
        // 1. Latar Belakang Visual Sesuai Scene Ruangan yang Dipilih
        StageBackgroundCanvas(
            backgroundId = workingState.backgroundId,
            accentPalette = accentPalette,
            glowPulse = glowPulse,
            rfWavePhase = rfWavePhase,
            isScanningRf = isScanningRf,
            isHoloPose = activeMode == AvatarInteractionMode.HOLO_POSE
        )

        // 2. Konten Interaktif di Atas Panggung
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bar Atas: Info Latar & Balon Dialog Interaktif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06121F).copy(alpha = 0.88f))
                        .border(1.dp, accentPalette.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${bgItem?.name ?: "Ruang Kontrol MRI"} • ${bgItem?.badgeLabel ?: "Zona III"}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentPalette.glowColor
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF120808).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (workingState.backgroundId == "bg_scanner_suite") "07:23 • B₀ 3,0T" else "07:23 • B₀ 1,5T",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF6B6B)
                    )
                }
            }

            // Balon Dialog Klinis Interaktif (Muncul & Berganti saat Avatar Diketuk)
            AnimatedContent(
                targetState = bubbleText,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                label = "speech_bubble"
            ) { text ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF071A2B).copy(alpha = 0.92f))
                        .border(1.dp, accentPalette.primaryColor.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                        .clickable { onAvatarTap() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = text,
                        fontSize = 10.sp,
                        color = Color(0xFFE6F2FB),
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ketuk ▶",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentPalette.primaryColor
                    )
                }
            }

            // Area Tengah: Panah Ganti Karakter, Avatar Lebar Tanpa Lingkaran, & Badge Item Terpasang
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Kolom Kiri: Tombol Panah Kiri + Tag Item Kiri
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.width(86.dp)
                ) {
                    EquippedOverlayPill(
                        categoryLabel = "KEPALA",
                        itemName = hairItem?.name ?: "Antena",
                        pillColor = Color(hairItem?.previewColorHex ?: 0xFF1FB6E8)
                    )
                    EquippedOverlayPill(
                        categoryLabel = "SERAGAM",
                        itemName = uniformItem?.name ?: "Scrub",
                        pillColor = Color(uniformItem?.previewColorHex ?: 0xFF3CC4A3)
                    )
                    IconButton(
                        onClick = onPrevAvatar,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F2A45).copy(alpha = 0.9f))
                            .border(1.dp, Color(0xFF1D4468), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Karakter Sebelumnya",
                            tint = Color(0xFFE6F2FB)
                        )
                    }
                }

                // TENGAH: AVATAR LEBAR TANPA LINGKARAN DI SEKITARNYA + OVERLAY INTERAKTIF
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAvatarTap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Overlay Visual Aksesori & Visor di sekitar Avatar
                    AvatarEquippedVisualOverlay(
                        workingState = workingState,
                        accentPalette = accentPalette,
                        floatOffset = floatOffset + jumpOffsetY,
                        glowPulse = glowPulse
                    )

                    // Gambar Karakter Robot Transparan Lebar & Utuh (ContentScale.Fit, TANPA CircleShape)
                    Image(
                        painter = painterResource(id = currentAvatar.drawableRes),
                        contentDescription = "Karakter ${currentAvatar.label} - Ketuk untuk berinteraksi",
                        modifier = Modifier
                            .width(185.dp)
                            .height(145.dp)
                            .offset(y = (floatOffset + jumpOffsetY).dp)
                            .graphicsLayer {
                                scaleX = jumpScale
                                scaleY = jumpScale
                                rotationZ = tiltAngle
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                // Kolom Kanan: Tag Item Kanan + Tombol Panah Kanan
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.width(86.dp)
                ) {
                    EquippedOverlayPill(
                        categoryLabel = "AUDIO",
                        itemName = headsetItem?.name ?: "Acoustic",
                        pillColor = Color(headsetItem?.previewColorHex ?: 0xFF7FDCD6)
                    )
                    EquippedOverlayPill(
                        categoryLabel = "ALAT",
                        itemName = accessoryItem?.name ?: "Detektor",
                        pillColor = Color(accessoryItem?.previewColorHex ?: 0xFFFF8A1F)
                    )
                    IconButton(
                        onClick = onNextAvatar,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F2A45).copy(alpha = 0.9f))
                            .border(1.dp, Color(0xFF1D4468), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Karakter Berikutnya",
                            tint = Color(0xFFE6F2FB)
                        )
                    }
                }
            }

            // Bar Bawah Panggung: Nama Spesialis + Tombol Interaksi Cepat
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$userName • $specialistRole",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE6F2FB)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentPalette.primaryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = favoriteSequence,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentPalette.glowColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4 Tombol Aksi Interaktif Karakter di Panggung
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    AvatarInteractionMode.entries.forEach { mode ->
                        val isActive = activeMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isActive) accentPalette.primaryColor.copy(alpha = 0.25f)
                                    else Color(0xFF071A2B).copy(alpha = 0.85f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) accentPalette.primaryColor else Color(0xFF1D4468),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectMode(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 9.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isActive) accentPalette.glowColor else Color(0xFFBFD9EC)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EquippedOverlayPill(
    categoryLabel: String,
    itemName: String,
    pillColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF071A2B).copy(alpha = 0.86f))
            .border(1.dp, pillColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = categoryLabel,
            fontSize = 7.sp,
            fontWeight = FontWeight.ExtraBold,
            color = pillColor,
            letterSpacing = 0.5.sp
        )
        Text(
            text = itemName,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6F2FB),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AvatarEquippedVisualOverlay(
    workingState: CharacterCustomizationState,
    accentPalette: CharacterAccentPalette,
    floatOffset: Float,
    glowPulse: Float
) {
    Canvas(
        modifier = Modifier
            .width(190.dp)
            .height(145.dp)
            .offset(y = floatOffset.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Aura Seragam di sekitar tubuh bawah
        val uniformColor = Color(
            CharacterCatalog.getItem(workingState.equippedUniform)?.previewColorHex ?: 0xFF3CC4A3
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    uniformColor.copy(alpha = 0.28f * glowPulse),
                     accentPalette.primaryColor.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(cx, cy + 12f),
                radius = size.minDimension * 0.62f
            ),
            radius = size.minDimension * 0.62f,
            center = Offset(cx, cy + 12f)
        )

        // Overlay Kepala / Visor / Halo di bagian atas kepala robot
        when (workingState.equippedHair) {
            "hair_visor" -> {
                drawRoundRect(
                    color = Color(0xFF7FDCD6).copy(alpha = 0.45f),
                    topLeft = Offset(cx - 38f, cy - 34f),
                    size = Size(76f, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color(0xFF1FB6E8),
                    topLeft = Offset(cx - 38f, cy - 34f),
                    size = Size(76f, 16f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2.5f)
                )
            }
            "hair_cyber" -> {
                drawOval(
                    color = Color(0xFF55BBDC).copy(alpha = 0.85f * glowPulse),
                    topLeft = Offset(cx - 48f, 4f),
                    size = Size(96f, 18f),
                    style = Stroke(width = 3.5f)
                )
            }
            "hair_cap" -> {
                drawArc(
                    color = Color(0xFF3CC4A3).copy(alpha = 0.55f),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = true,
                    topLeft = Offset(cx - 42f, 10f),
                    size = Size(84f, 38f)
                )
            }
            "hair_goggles" -> {
                drawCircle(
                    color = Color(0xFFFF8A1F),
                    radius = 12f,
                    center = Offset(cx - 16f, cy - 24f),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color(0xFFFF8A1F),
                    radius = 12f,
                    center = Offset(cx + 16f, cy - 24f),
                    style = Stroke(width = 3f)
                )
            }
            "hair_crown_rf" -> {
                drawArc(
                    color = Color(0xFFE6F2FB).copy(alpha = 0.8f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 50f, 6f),
                    size = Size(100f, 60f),
                    style = Stroke(width = 3.5f)
                )
            }
        }

        // Overlay Gelombang Akustik Headset di kiri & kanan telinga
        if (workingState.equippedHeadset != "headset_none") {
            val hsColor = Color(
                CharacterCatalog.getItem(workingState.equippedHeadset)?.previewColorHex ?: 0xFF1FB6E8
            )
            for (i in 1..2) {
                val r = 10f + i * 7f * glowPulse
                drawArc(
                    color = hsColor.copy(alpha = 0.65f / i),
                    startAngle = 135f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - 64f - r, cy - 16f - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = hsColor.copy(alpha = 0.65f / i),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx + 64f - r, cy - 16f - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }
        }

        // Indikator Alat Klinis di sisi kanan bawah karakter
        val accColor = Color(
            CharacterCatalog.getItem(workingState.equippedAccessory)?.previewColorHex ?: 0xFFFF8A1F
        )
        drawCircle(
            color = accColor.copy(alpha = 0.28f * glowPulse),
            radius = 16f,
            center = Offset(cx + 58f, cy + 32f)
        )
        drawCircle(
            color = accColor,
            radius = 6f,
            center = Offset(cx + 58f, cy + 32f)
        )
    }
}

@Composable
private fun StageBackgroundCanvas(
    backgroundId: String,
    accentPalette: CharacterAccentPalette,
    glowPulse: Float,
    rfWavePhase: Float,
    isScanningRf: Boolean,
    isHoloPose: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Gradien Dasar Ruangan
        val bgColors = when (backgroundId) {
            "bg_control_room" -> listOf(Color(0xFF081F36), Color(0xFF0E2C4B), Color(0xFF071A2B))
            "bg_changing_room" -> listOf(Color(0xFF0B2740), Color(0xFF0F2A45), Color(0xFF071A2B))
            "bg_scanner_suite" -> listOf(Color(0xFF0B1F4A), Color(0xFF14357A), Color(0xFF071A2B))
            "bg_reading_room" -> listOf(Color(0xFF061320), Color(0xFF0D233A), Color(0xFF050E18))
            "bg_physics_lab" -> listOf(Color(0xFF07222C), Color(0xFF0E3A47), Color(0xFF071A2B))
            else -> listOf(Color(0xFF051628), Color(0xFF0B2E4E), Color(0xFF04101E))
        }
        drawRect(brush = Brush.verticalGradient(bgColors))

        // 2. Detail Arsitektur Ruangan Sesuai Pilihan Latar
        when (backgroundId) {
            "bg_control_room" -> {
                // Jendela Kaca RF Ruang Kontrol melihat ke Gantry MRI
                val winW = w * 0.56f
                val winH = h * 0.42f
                val winX = (w - winW) / 2f
                val winY = h * 0.14f
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0E5F86).copy(alpha = 0.65f), Color(0xFF1FB6E8).copy(alpha = 0.35f))
                    ),
                    topLeft = Offset(winX, winY),
                    size = Size(winW, winH),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = Color(0xFF1D4468),
                    topLeft = Offset(winX, winY),
                    size = Size(winW, winH),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 3f)
                )
                // Siluet Gantry MRI di balik jendela
                val gantryCenter = Offset(w / 2f, winY + winH * 0.56f)
                drawCircle(
                    color = Color(0xFFE3F0FB).copy(alpha = 0.28f),
                    radius = winH * 0.38f,
                    center = gantryCenter
                )
                drawCircle(
                    color = Color(0xFF0F2A6B).copy(alpha = 0.65f),
                    radius = winH * 0.22f,
                    center = gantryCenter
                )
                // Dua Monitor Citra Otak di kiri & kanan meja konsol
                val monW = w * 0.16f
                val monH = h * 0.18f
                drawRoundRect(
                    color = Color(0xFF06121F),
                    topLeft = Offset(w * 0.04f, h * 0.36f),
                    size = Size(monW, monH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawOval(
                    color = Color(0xFFFF8A1F).copy(alpha = 0.65f),
                    topLeft = Offset(w * 0.07f, h * 0.39f),
                    size = Size(monW * 0.35f, monH * 0.65f)
                )
                drawOval(
                    color = Color(0xFF1FB6E8).copy(alpha = 0.65f),
                    topLeft = Offset(w * 0.12f, h * 0.39f),
                    size = Size(monW * 0.35f, monH * 0.65f)
                )

                drawRoundRect(
                    color = Color(0xFF06121F),
                    topLeft = Offset(w * 0.80f, h * 0.36f),
                    size = Size(monW, monH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawOval(
                    color = Color(0xFF3CC4A3).copy(alpha = 0.65f),
                    topLeft = Offset(w * 0.83f, h * 0.39f),
                    size = Size(monW * 0.35f, monH * 0.65f)
                )
                drawOval(
                    color = Color(0xFF55BBDC).copy(alpha = 0.65f),
                    topLeft = Offset(w * 0.88f, h * 0.39f),
                    size = Size(monW * 0.35f, monH * 0.65f)
                )
            }

            "bg_changing_room" -> {
                // Loker Ruang Ganti Radiografer di sisi kiri dan kanan (sesuai mockup)
                val lockerW = w * 0.15f
                val lockerH = h * 0.48f
                drawRoundRect(
                    color = Color(0xFF0F2A45),
                    topLeft = Offset(w * 0.04f, h * 0.15f),
                    size = Size(lockerW, lockerH),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color(0xFF1D4468),
                    topLeft = Offset(w * 0.04f, h * 0.15f),
                    size = Size(lockerW, lockerH),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2.5f)
                )
                drawLine(
                    color = Color(0xFF1D4468),
                    start = Offset(w * 0.115f, h * 0.15f),
                    end = Offset(w * 0.115f, h * 0.63f),
                    strokeWidth = 2f
                )

                drawRoundRect(
                    color = Color(0xFF0F2A45),
                    topLeft = Offset(w * 0.81f, h * 0.15f),
                    size = Size(lockerW, lockerH),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color(0xFF1D4468),
                    topLeft = Offset(w * 0.81f, h * 0.15f),
                    size = Size(lockerW, lockerH),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2.5f)
                )
                drawLine(
                    color = Color(0xFF1D4468),
                    start = Offset(w * 0.885f, h * 0.15f),
                    end = Offset(w * 0.885f, h * 0.63f),
                    strokeWidth = 2f
                )
            }

            "bg_scanner_suite" -> {
                // Gantry Bore Magnet 3T Besar di Belakang Karakter
                val boreCenter = Offset(w / 2f, h * 0.45f)
                drawCircle(
                    color = Color(0xFFE6F2FB).copy(alpha = 0.18f),
                    radius = h * 0.36f,
                    center = boreCenter
                )
                drawCircle(
                    color = Color(0xFF55BBDC).copy(alpha = 0.45f),
                    radius = h * 0.36f,
                    center = boreCenter,
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = Color(0xFF071A2B).copy(alpha = 0.75f),
                    radius = h * 0.21f,
                    center = boreCenter
                )
            }

            "bg_reading_room" -> {
                // 3 Layar Workstation PACS Diagnostik
                for (i in 0..2) {
                    val mw = w * 0.22f
                    val mh = h * 0.26f
                    val mx = w * 0.14f + i * (mw + w * 0.03f)
                    val my = h * 0.18f
                    drawRoundRect(
                        color = Color(0xFF040B12),
                        topLeft = Offset(mx, my),
                        size = Size(mw, mh),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = accentPalette.primaryColor.copy(alpha = 0.4f),
                        topLeft = Offset(mx, my),
                        size = Size(mw, mh),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            "bg_physics_lab" -> {
                // Gelombang Sinyal FID / Kurva Relaksasi T1 & T2 di Latar
                val path = Path()
                val startX = w * 0.08f
                val endX = w * 0.92f
                val midY = h * 0.38f
                path.moveTo(startX, midY)
                val steps = 60
                for (s in 1..steps) {
                    val t = s / steps.toFloat()
                    val x = startX + t * (endX - startX)
                    val env = (1f - t * 0.75f)
                    val y = midY - sin((t * 8f + rfWavePhase * 2f) * PI).toFloat() * (h * 0.12f) * env
                    path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF7FDCD6).copy(alpha = 0.45f),
                    style = Stroke(width = 2.5f)
                )
            }

            else -> {
                // Kisi Hologram K-Space (kx, ky)
                for (i in 1..6) {
                    val yLine = h * (0.15f + i * 0.08f)
                    drawLine(
                        color = accentPalette.primaryColor.copy(alpha = 0.16f),
                        start = Offset(w * 0.1f, yLine),
                        end = Offset(w * 0.9f, yLine),
                        strokeWidth = 1.2f
                    )
                }
            }
        }

        // 3. Panggung Elips Bercahaya Tempat Karakter Berdiri (Sesuai Mockup)
        val platformY = h * 0.72f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentPalette.primaryColor.copy(alpha = 0.55f * glowPulse),
                    accentPalette.glowColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(w / 2f, platformY),
                radius = w * 0.38f
            ),
            topLeft = Offset(w * 0.16f, platformY - 22f),
            size = Size(w * 0.68f, 44f)
        )
        drawOval(
            color = Color(0xFF0F2A6B),
            topLeft = Offset(w * 0.27f, platformY - 13f),
            size = Size(w * 0.46f, 26f)
        )
        drawOval(
            color = accentPalette.primaryColor,
            topLeft = Offset(w * 0.27f, platformY - 13f),
            size = Size(w * 0.46f, 26f),
            style = Stroke(width = 2.5f)
        )

        // 4. Efek Gelombang Pemindaian Pulsa RF 90° atau Mode Fokus Holografik
        if (isScanningRf || isHoloPose) {
            val scanRadius = (w * 0.12f) + rfWavePhase * (w * 0.34f)
            drawCircle(
                color = accentPalette.glowColor.copy(alpha = (1f - rfWavePhase) * 0.7f),
                radius = scanRadius,
                center = Offset(w / 2f, h * 0.48f),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
private fun SpecialistAvatarGrid(
    avatars: List<MedicalAvatar>,
    selectedAvatarId: String,
    accentPalette: CharacterAccentPalette,
    onSelectAvatar: (MedicalAvatar) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(avatars, key = { it.id }) { av ->
            val isSelected = selectedAvatarId == av.id
            val profile = CharacterCatalog.getSpecialistProfile(av.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAvatar(av) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF13385C) else Color(0xFF0F2A45)
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accentPalette.primaryColor else Color(0xFF1D4468)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gambar Karakter Utuh & Lebar (TANPA CircleShape)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF071A2B).copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = av.drawableRes),
                            contentDescription = av.label,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(accentPalette.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Aktif",
                                    tint = Color(0xFF04121F),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = av.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) accentPalette.glowColor else Color(0xFFE6F2FB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = profile.favoriteSequence,
                        fontSize = 8.sp,
                        color = Color(0xFFBFD9EC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterItemCard(
    item: CharacterItem,
    isSelected: Boolean,
    isUnlocked: Boolean,
    accentPalette: CharacterAccentPalette,
    onClick: () -> Unit
) {
    val itemPrimary = Color(item.previewColorHex)
    val itemSecondary = Color(item.secondaryColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF13385C) else Color(0xFF0F2A45)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentPalette.primaryColor else Color(0xFF1D4468)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Baris Atas: Ilustrasi Visual Item (Tidak Kosong) + Status Dipakai/Tersedia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Ilustrasi Grafis Kustom untuk Setiap Item
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF071A2B))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) accentPalette.primaryColor else itemPrimary.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CharacterItemVisualIcon(
                        item = item,
                        primary = itemPrimary,
                        secondary = itemSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) Color(0xFF3CC4A3).copy(alpha = 0.22f)
                                else itemPrimary.copy(alpha = 0.16f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSelected) "✓ Dipakai" else item.badgeLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF3CC4A3) else itemSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.unlockRequirement.requiredModuleId?.uppercase()?.let { "Modul $it" } ?: "Standar",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8FB3CC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = item.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE6F2FB),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.description,
                fontSize = 9.5.sp,
                color = Color(0xFFBFD9EC),
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis
            )

            if (item.clinicalBenefit.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✦ ${item.clinicalBenefit}",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentPalette.glowColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CharacterItemVisualIcon(
    item: CharacterItem,
    primary: Color,
    secondary: Color
) {
    Canvas(modifier = Modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (item.category) {
            CharacterCategory.RAMBUT -> {
                // Ilustrasi Visor / Antena / Helm Koil
                drawArc(
                    color = primary,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.15f, h * 0.25f),
                    size = Size(w * 0.7f, h * 0.55f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                drawRoundRect(
                    color = secondary.copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.22f, h * 0.45f),
                    size = Size(w * 0.56f, h * 0.22f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawCircle(color = primary, radius = 3.5f, center = Offset(cx, h * 0.16f))
            }

            CharacterCategory.SERAGAM -> {
                // Ilustrasi Seragam / Scrub / Jas Lab Medis
                val path = Path().apply {
                    moveTo(w * 0.28f, h * 0.18f)
                    lineTo(w * 0.10f, h * 0.38f)
                    lineTo(w * 0.24f, h * 0.48f)
                    lineTo(w * 0.24f, h * 0.85f)
                    lineTo(w * 0.76f, h * 0.85f)
                    lineTo(w * 0.76f, h * 0.48f)
                    lineTo(w * 0.90f, h * 0.38f)
                    lineTo(w * 0.72f, h * 0.18f)
                    lineTo(cx, h * 0.34f)
                    close()
                }
                drawPath(path = path, color = primary.copy(alpha = 0.35f))
                drawPath(path = path, color = primary, style = Stroke(width = 2.4f))
                drawCircle(color = secondary, radius = 3f, center = Offset(w * 0.62f, h * 0.52f))
            }

            CharacterCategory.HEADSET -> {
                // Ilustrasi Headset Peredam Akustik MRI
                drawArc(
                    color = primary,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.18f, h * 0.15f),
                    size = Size(w * 0.64f, h * 0.60f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                drawRoundRect(
                    color = secondary,
                    topLeft = Offset(w * 0.10f, h * 0.45f),
                    size = Size(w * 0.20f, h * 0.34f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawRoundRect(
                    color = secondary,
                    topLeft = Offset(w * 0.70f, h * 0.45f),
                    size = Size(w * 0.20f, h * 0.34f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
            }

            CharacterCategory.AKSESORIS -> {
                // Ilustrasi Alat Klinis / Sensor / Koil / Phantom
                drawRoundRect(
                    color = primary.copy(alpha = 0.28f),
                    topLeft = Offset(w * 0.20f, h * 0.16f),
                    size = Size(w * 0.60f, h * 0.68f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(w * 0.20f, h * 0.16f),
                    size = Size(w * 0.60f, h * 0.68f),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 2.4f)
                )
                drawCircle(color = secondary, radius = 4.5f, center = Offset(cx, cy))
            }

            CharacterCategory.LATAR -> {
                // Ilustrasi Mini Scene Ruangan & Gantry MRI
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(primary, secondary)),
                    topLeft = Offset(w * 0.08f, h * 0.16f),
                    size = Size(w * 0.84f, h * 0.68f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawCircle(
                    color = Color(0xFF071A2B),
                    radius = w * 0.18f,
                    center = Offset(cx, cy)
                )
            }

            CharacterCategory.KARAKTER -> {
                drawCircle(color = primary, radius = w * 0.35f, center = Offset(cx, cy))
            }
        }
    }
}
