package com.example.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CharacterCatalog
import com.example.data.CharacterCategory
import com.example.data.CharacterCustomizationState
import com.example.data.MriRepository
import com.example.ui.theme.CharRoomBg
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

    val haptic = LocalHapticFeedback.current

    fun triggerAvatarTap(
        customMessage: String? = null,
        hapticType: HapticFeedbackType = HapticFeedbackType.TextHandleMove
    ) {
        haptic.performHapticFeedback(hapticType)
        interactionCount++
        isJumping = true
        if (customMessage != null) {
            customBubbleText = customMessage
        } else {
            customBubbleText = null
            greetingIndex = (greetingIndex + 1) % specialistProfile.greetings.size
        }
    }

    // Fungsi bantu saat berpindah peran: muat atribut bawaan (topi/kacamata/seragam/alat) milik peran tersebut
    // sebagai item slot yang tetap bebas diganti atau dilepas oleh pemain.
    fun applyRoleWithDefaultLoadout(newAvatarId: String) {
        val roleLoadout = AvatarLayerRegistry.defaultLoadoutForRole(
            avatarId = newAvatarId,
            currentBackgroundId = backgroundId,
            currentAccentColor = accentColor
        )
        avatarId = roleLoadout.avatarId
        equippedHair = roleLoadout.equippedHair
        equippedUniform = roleLoadout.equippedUniform
        equippedHeadset = roleLoadout.equippedHeadset
        equippedAccessory = roleLoadout.equippedAccessory
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
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // 1. Panggung Interaktif Ruang Karakter
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
                hasUnsavedChanges = hasUnsavedChanges,
                onReset = {
                    val def = CharacterCustomizationState()
                    avatarId = def.avatarId
                    equippedHair = def.equippedHair
                    equippedUniform = def.equippedUniform
                    equippedHeadset = def.equippedHeadset
                    equippedAccessory = def.equippedAccessory
                    backgroundId = def.backgroundId
                    accentColor = def.accentColor
                    viewModel.resetCharacterCustomization()
                    triggerAvatarTap(resetMsg, HapticFeedbackType.LongPress)
                },
                onSave = {
                    viewModel.updateCharacterCustomization(workingState)
                    triggerAvatarTap("Kustomisasi karakter berhasil disimpan!", HapticFeedbackType.LongPress)
                },
                onAvatarTap = { triggerAvatarTap() },
                onPrevAvatar = {
                    val prevIndex = if (currentAvatarIndex - 1 < 0) avatars.lastIndex else currentAvatarIndex - 1
                    val newAv = avatars[prevIndex]
                    applyRoleWithDefaultLoadout(newAv.id)
                    val prof = CharacterCatalog.getSpecialistProfile(newAv.id)
                    triggerAvatarTap("${newAv.label} siap bertugas: ${prof.zoneSpecialty}!")
                },
                onNextAvatar = {
                    val nextIndex = (currentAvatarIndex + 1) % avatars.size
                    val newAv = avatars[nextIndex]
                    applyRoleWithDefaultLoadout(newAv.id)
                    val prof = CharacterCatalog.getSpecialistProfile(newAv.id)
                    triggerAvatarTap("${newAv.label} siap bertugas: ${prof.zoneSpecialty}!")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Navigasi Tab Segmen (Karakter | Tampilan | Latar & Warna) + Sub-Tab
            CharacterCategoryTabs(
                selectedCategory = selectedCategory,
                selectedAccentId = workingState.accentColor,
                accentPalette = accentPalette,
                onSelectCategory = { category ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedCategory = category
                },
                onSelectAccent = { palette ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    accentColor = palette.id
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Pilihan Item yang Sliding ke Samping (LazyRow Horizontal)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                if (selectedCategory == CharacterCategory.KARAKTER) {
                    SpecialistAvatarGrid(
                        avatars = avatars,
                        selectedAvatarId = workingState.avatarId,
                        accentPalette = accentPalette,
                        onSelectAvatar = { avatar ->
                            applyRoleWithDefaultLoadout(avatar.id)
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
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
    }
}
