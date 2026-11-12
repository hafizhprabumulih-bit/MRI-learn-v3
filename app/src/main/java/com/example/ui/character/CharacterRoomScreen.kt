package com.example.ui.character

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CharacterCatalog
import com.example.data.CharacterCategory
import com.example.data.CharacterCustomizationState
import com.example.data.MriRepository
import com.example.ui.theme.CharAccentMint
import com.example.ui.theme.CharActionDarkText
import com.example.ui.theme.CharActionOrange
import com.example.ui.theme.CharBorderSlate
import com.example.ui.theme.CharCardSurface
import com.example.ui.theme.CharRoomBg
import com.example.ui.theme.CharStageDeep
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import kotlinx.coroutines.delay

@Composable
fun CharacterRoomScreen(
    state: MriUiState,
    viewModel: MriViewModel
) {
    // Gunakan rememberSaveable per field primitif agar tahan rotasi layar tanpa membebani Bundle
    val saved = state.characterCustomization
    var avatarId by rememberSaveable(saved.avatarId) { mutableStateOf(saved.avatarId) }
    var equippedHair by rememberSaveable(saved.equippedHair) { mutableStateOf(saved.equippedHair) }
    var equippedUniform by rememberSaveable(saved.equippedUniform) { mutableStateOf(saved.equippedUniform) }
    var equippedHeadset by rememberSaveable(saved.equippedHeadset) { mutableStateOf(saved.equippedHeadset) }
    var equippedAccessory by rememberSaveable(saved.equippedAccessory) { mutableStateOf(saved.equippedAccessory) }
    var backgroundId by rememberSaveable(saved.backgroundId) { mutableStateOf(saved.backgroundId) }
    var accentColor by rememberSaveable(saved.accentColor) { mutableStateOf(saved.accentColor) }

    val workingState = remember(
        avatarId,
        equippedHair,
        equippedUniform,
        equippedHeadset,
        equippedAccessory,
        backgroundId,
        accentColor
    ) {
        CharacterCustomizationState(
            avatarId = avatarId,
            equippedHair = equippedHair,
            equippedUniform = equippedUniform,
            equippedHeadset = equippedHeadset,
            equippedAccessory = equippedAccessory,
            backgroundId = backgroundId,
            accentColor = accentColor
        )
    }

    val hasUnsavedChanges = workingState != state.characterCustomization

    var selectedCategory by rememberSaveable { mutableStateOf(CharacterCategory.RAMBUT) }
    var showActionPanel by rememberSaveable { mutableStateOf(true) }
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

    val resetMsg = stringResource(R.string.char_room_reset_msg)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharRoomBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // 1. Header Ruang Karakter + Tombol Reset (Sesuai Gambar PNG)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.char_room_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharTextPrimary
                )

                OutlinedButton(
                    onClick = {
                        val def = CharacterCustomizationState()
                        avatarId = def.avatarId
                        equippedHair = def.equippedHair
                        equippedUniform = def.equippedUniform
                        equippedHeadset = def.equippedHeadset
                        equippedAccessory = def.equippedAccessory
                        backgroundId = def.backgroundId
                        accentColor = def.accentColor
                        viewModel.resetCharacterCustomization()
                        triggerAvatarTap(resetMsg)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CharBorderSlate),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CharCardSurface,
                        contentColor = CharTextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.char_room_reset),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Panggung Interaktif 420dp & Avatar 220dp
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
                showActionPanel = showActionPanel,
                onToggleActionPanel = { showActionPanel = !showActionPanel },
                onAvatarTap = { triggerAvatarTap() },
                onPrevAvatar = {
                    val prevIndex = if (currentAvatarIndex - 1 < 0) avatars.lastIndex else currentAvatarIndex - 1
                    val newAv = avatars[prevIndex]
                    avatarId = newAv.id
                    val prof = CharacterCatalog.getSpecialistProfile(newAv.id)
                    triggerAvatarTap("${newAv.label} siap bertugas: ${prof.zoneSpecialty}!")
                },
                onNextAvatar = {
                    val nextIndex = (currentAvatarIndex + 1) % avatars.size
                    val newAv = avatars[nextIndex]
                    avatarId = newAv.id
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
                            val b0Text = if (workingState.backgroundId == "bg_scanner_suite") {
                                "3,0T (f₀ = 127,74 MHz)"
                            } else {
                                "1,5T (f₀ = 63,87 MHz)"
                            }
                            triggerAvatarTap("Pulsa RF 90° dipancarkan! Resonansi spin ¹H terdeteksi pada $b0Text.")
                        }
                        AvatarInteractionMode.SAFETY_CHECK -> {
                            val hairName = CharacterCatalog.getItem(workingState.equippedHair)?.name ?: "Standar"
                            val uniformName = CharacterCatalog.getItem(workingState.equippedUniform)?.name ?: "Scrub"
                            val accName = CharacterCatalog.getItem(workingState.equippedAccessory)?.name ?: "Detektor"
                            triggerAvatarTap("Skrining Zona IV Lulus ✅ [$hairName · $uniformName · $accName] 100% MR-Safe!")
                        }
                        AvatarInteractionMode.HOLO_POSE -> {
                            triggerAvatarTap("Mode Fokus Aktif ✨ Menampilkan telemetri sekuens unggulan: ${specialistProfile.favoriteSequence}.")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Navigasi Tab Segmen (Karakter | Tampilan | Latar & Warna) + Sub-Tab
            CharacterCategoryTabs(
                selectedCategory = selectedCategory,
                selectedAccentId = workingState.accentColor,
                accentPalette = accentPalette,
                onSelectCategory = { category -> selectedCategory = category },
                onSelectAccent = { palette ->
                    accentColor = palette.id
                    triggerAvatarTap("Warna aksen panggung diubah ke ${palette.label}!")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Grid Item Kustomisasi
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
                            avatarId = avatar.id
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 72.dp),
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
                                    when (selectedCategory) {
                                        CharacterCategory.KARAKTER -> Unit
                                        CharacterCategory.RAMBUT -> equippedHair = item.id
                                        CharacterCategory.SERAGAM -> equippedUniform = item.id
                                        CharacterCategory.HEADSET -> equippedHeadset = item.id
                                        CharacterCategory.AKSESORIS -> equippedAccessory = item.id
                                        CharacterCategory.LATAR -> backgroundId = item.id
                                    }
                                    triggerAvatarTap("${item.name} dipasang! ${item.clinicalBenefit}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Bar & Toggle Simpan Melayang dengan Simbol Disket Penyimpanan (Icons.Default.Save)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = RoundedCornerShape(18.dp),
            color = CharStageDeep.copy(alpha = 0.94f),
            border = BorderStroke(
                width = 1.dp,
                color = if (hasUnsavedChanges) CharActionOrange.copy(alpha = 0.65f) else CharBorderSlate
            ),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (hasUnsavedChanges) CharActionOrange else CharAccentMint)
                    )
                    Text(
                        text = if (hasUnsavedChanges) {
                            stringResource(R.string.char_room_unsaved_hint)
                        } else {
                            stringResource(R.string.char_room_saved_hint)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CharTextSecondary
                    )
                }

                // Toggle Melayang Simpan dengan Simbol Disket
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (hasUnsavedChanges) CharActionOrange else CharAccentMint)
                        .clickable {
                            viewModel.updateCharacterCustomization(workingState)
                            triggerAvatarTap("Kustomisasi karakter berhasil disimpan ke DataStore!")
                        }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.char_room_save_floating),
                            tint = CharActionDarkText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasUnsavedChanges) {
                                stringResource(R.string.char_room_save_floating)
                            } else {
                                stringResource(R.string.char_room_saved_floating)
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharActionDarkText
                        )
                    }
                }
            }
        }
    }
}
