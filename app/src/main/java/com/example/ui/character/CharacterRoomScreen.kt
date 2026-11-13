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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val sanitizedSavedHair = if (saved.equippedHair == "hair_cap") "hair_default" else saved.equippedHair
    var avatarId by rememberSaveable(saved.avatarId) { mutableStateOf(saved.avatarId) }
    var equippedHair by rememberSaveable(sanitizedSavedHair) { mutableStateOf(sanitizedSavedHair) }
    var equippedUniform by rememberSaveable(saved.equippedUniform) { mutableStateOf(saved.equippedUniform) }
    var equippedHeadset by rememberSaveable(saved.equippedHeadset) { mutableStateOf(saved.equippedHeadset) }
    var equippedAccessory by rememberSaveable(saved.equippedAccessory) { mutableStateOf(saved.equippedAccessory) }
    var backgroundId by rememberSaveable(saved.backgroundId) { mutableStateOf(saved.backgroundId) }

    val workingState = remember(
        avatarId,
        equippedHair,
        equippedUniform,
        equippedHeadset,
        equippedAccessory,
        backgroundId
    ) {
        CharacterCustomizationState(
            avatarId = avatarId,
            equippedHair = equippedHair,
            equippedUniform = equippedUniform,
            equippedHeadset = equippedHeadset,
            equippedAccessory = equippedAccessory,
            backgroundId = backgroundId
        )
    }

    val hasUnsavedChanges = workingState != state.characterCustomization

    var selectedCategory by rememberSaveable { mutableStateOf(CharacterCategory.RAMBUT) }
    var interactionCount by rememberSaveable { mutableIntStateOf(0) }
    var greetingIndex by rememberSaveable { mutableIntStateOf(0) }
    var customBubbleText by rememberSaveable { mutableStateOf<String?>(null) }
    var isJumping by remember { mutableStateOf(false) }
    var isWaveActive by rememberSaveable { mutableStateOf(false) }

    // State Interaksi (Body Zones, Rub, Double-Tap Larmor Spin, Shift Card, Studio)
    var forcedExpression by remember { mutableStateOf<Expression?>(null) }
    var larmorSpinTrigger by remember { mutableIntStateOf(0) }
    var heartBurstTrigger by remember { mutableIntStateOf(0) }
    var shiftMood by rememberSaveable { mutableIntStateOf(3) }
    var showShiftDialog by rememberSaveable { mutableStateOf(false) }
    var showSnapshotStudio by rememberSaveable { mutableStateOf(false) }

    val avatars = MriRepository.avatars
    val currentAvatarIndex = avatars.indexOfFirst { it.id == workingState.avatarId }.coerceAtLeast(0)
    val currentAvatar = avatars[currentAvatarIndex]
    val specialistProfile = CharacterCatalog.getSpecialistProfile(workingState.avatarId)
    val accentPalette = CharacterCatalog.getAccentPalette("cyan")

    LaunchedEffect(isJumping) {
        if (isJumping) {
            delay(360)
            isJumping = false
        }
    }

    LaunchedEffect(forcedExpression, interactionCount) {
        if (forcedExpression != null) {
            delay(2200)
            forcedExpression = null
        }
    }

    val haptic = LocalHapticFeedback.current

    fun triggerAvatarTap(
        customMessage: String? = null,
        hapticType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
        expression: Expression? = null
    ) {
        haptic.performHapticFeedback(hapticType)
        interactionCount++
        isJumping = true
        forcedExpression = expression
        if (customMessage != null) {
            customBubbleText = customMessage
        } else {
            customBubbleText = null
            greetingIndex = (greetingIndex + 1) % specialistProfile.greetings.size
        }
    }

    fun applyRoleWithDefaultLoadout(newAvatarId: String) {
        val roleLoadout = AvatarLayerRegistry.defaultLoadoutForRole(
            avatarId = newAvatarId,
            currentBackgroundId = backgroundId
        )
        avatarId = roleLoadout.avatarId
        equippedHair = roleLoadout.equippedHair
        equippedUniform = roleLoadout.equippedUniform
        equippedHeadset = roleLoadout.equippedHeadset
        equippedAccessory = roleLoadout.equippedAccessory
    }

    val displayedBubbleText = customBubbleText
        ?: if (interactionCount == 0 && state.studyStreakDays == 0) {
            "Streak belajar belum aktif hari ini! Yuk selesaikan 1 simpul agar koherensi belajar terjaga."
        } else {
            specialistProfile.greetings.getOrElse(greetingIndex % specialistProfile.greetings.size) {
                specialistProfile.greetings.first()
            }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp)
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
                isWaveActive = isWaveActive,
                interactionCount = interactionCount,
                hasUnsavedChanges = hasUnsavedChanges,
                studyStreakDays = state.studyStreakDays,
                shiftMood = shiftMood,
                forcedExpression = forcedExpression,
                larmorSpinTrigger = larmorSpinTrigger,
                heartBurstTrigger = heartBurstTrigger,
                onOpenSnapshotStudio = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showSnapshotStudio = true
                },
                onToggleWave = {
                    isWaveActive = !isWaveActive
                    val waveComment = when (workingState.backgroundId) {
                        "bg_led_stage" -> if (isWaveActive) "Gelombang RF menyala! Lintasan spiral & radial Ruang-k sedang dipindai!" else null
                        "bg_lecture_hall" -> if (isWaveActive) "Demonstrasi gelombang pulsa 90° di papan kuliah siap diamati!" else null
                        "bg_simulation_center" -> if (isWaveActive) "Simulasi rekonstruksi sinyal RF & gradien pusat riset aktif!" else null
                        else -> if (isWaveActive) "Pulsa RF 90° aktif — vektor Mxy beresonansi pada frekuensi Larmor!" else null
                    }
                    if (waveComment != null) {
                        triggerAvatarTap(waveComment, expression = Expression.BERPIKIR)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                onReset = {
                    val def = CharacterCustomizationState()
                    avatarId = def.avatarId
                    equippedHair = def.equippedHair
                    equippedUniform = def.equippedUniform
                    equippedHeadset = def.equippedHeadset
                    equippedAccessory = def.equippedAccessory
                    backgroundId = def.backgroundId
                    viewModel.resetCharacterCustomization()
                    triggerAvatarTap(resetMsg, HapticFeedbackType.LongPress, Expression.SEDIH)
                },
                onSave = {
                    viewModel.updateCharacterCustomization(workingState)
                    triggerAvatarTap(
                        "Kustomisasi karakter berhasil disimpan!",
                        HapticFeedbackType.LongPress,
                        Expression.BANGGA
                    )
                },
                onAvatarTap = { triggerAvatarTap() },
                onBodyZoneTap = { zone ->
                    when (zone) {
                        NanoBodyZone.HEAD -> {
                            val headItem = CharacterCatalog.getItem(workingState.equippedHair)
                            triggerAvatarTap(
                                customMessage = headItem?.let { "${it.name}: ${it.clinicalBenefit}." }
                                    ?: "Visor K-Space Nano dalam kondisi optimal!",
                                expression = if (interactionCount % 2 == 0) Expression.SENANG else Expression.BERPIKIR
                            )
                        }
                        NanoBodyZone.TOOL -> {
                            val acc = CharacterCatalog.getItem(workingState.equippedAccessory)
                            val toolMsg = when (workingState.equippedAccessory) {
                                "acc_magnet_detector" -> "Detektor Feromagnetik aktif: pembacaan medan tepi < 5 Gauss aman!"
                                "acc_clipboard" -> "Data konsol cepat: T1w (TR 500ms, TE 15ms) | T2w (TR 3500ms, TE 100ms)."
                                "acc_phantom" -> "Kalibrasi SNR fantom ACR selesai: homogenitas medan B0 terjaga!"
                                "acc_laser_pointer" -> "Pointer presentasi siap menyorot kurva relaksasi & spektrum K-Space!"
                                "acc_handbook_mri" -> "Buku Saku MRI: γ(¹H) = 42,58 MHz/T · Aturan ACR Zona I–IV selalu siap."
                                else -> acc?.let { "${it.name}: ${it.clinicalBenefit}." } ?: "Alat klinis siap digunakan!"
                            }
                            triggerAvatarTap(toolMsg, HapticFeedbackType.LongPress, Expression.BANGGA)
                        }
                        NanoBodyZone.TORSO -> {
                            val uni = CharacterCatalog.getItem(workingState.equippedUniform)
                            val zoneSuitability = when {
                                workingState.backgroundId == "bg_chiller_room" && workingState.equippedUniform != "uniform_armor" ->
                                    "Peringatan M05/M07B: Di Ruang Chiller 4,2K butuh Suit Kryogenik untuk bahaya helium cair!"
                                workingState.backgroundId == "bg_scanner_suite" && workingState.equippedHeadset == "headset_none" ->
                                    "Peringatan M07C: Di Ruang 3T Zona IV wajib memakai pelindung pendengaran terhadap bising gradien!"
                                else -> uni?.let { "${it.name}: ${it.clinicalBenefit}." } ?: "Seragam bebas komponen feromagnetik!"
                            }
                            triggerAvatarTap(
                                customMessage = zoneSuitability,
                                expression = if (zoneSuitability.startsWith("Peringatan")) Expression.WASPADA else Expression.SENANG
                            )
                        }
                    }
                },
                onDoubleTapLarmorSpin = {
                    larmorSpinTrigger++
                    triggerAvatarTap(
                        customMessage = "Presesi Larmor 63,87 MHz aktif! Spin fase 360° selaras!",
                        hapticType = HapticFeedbackType.LongPress,
                        expression = Expression.BANGGA
                    )
                },
                onLongPressShiftCard = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showShiftDialog = true
                },
                onRubHead = {
                    heartBurstTrigger++
                    shiftMood++
                    triggerAvatarTap(
                        customMessage = "Nyaman sekali! Semangat kerja shift naik ♥ (Mood Shift: $shiftMood)",
                        expression = Expression.SENANG
                    )
                },
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

            Spacer(modifier = Modifier.height(7.dp))

            // 2. Navigasi Tab Segmen (Karakter | Tampilan | Latar) + Sub-Tab
            CharacterCategoryTabs(
                selectedCategory = selectedCategory,
                accentPalette = accentPalette,
                onSelectCategory = { category ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedCategory = category
                }
            )

            Spacer(modifier = Modifier.height(7.dp))

            // 3. Pilihan Item yang Sliding ke Samping (LazyRow Horizontal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 138.dp),
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
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
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
                            val req = item.unlockRequirement
                            val unlockProgress = if (!isUnlocked && req.requiredModuleId != null && req.requiredNodesCount > 0) {
                                val matchedCompleted = state.modulesList
                                    .filter { mod ->
                                        mod.id.equals(req.requiredModuleId, ignoreCase = true) ||
                                            mod.code.equals(req.requiredModuleId, ignoreCase = true) ||
                                            mod.id.startsWith(req.requiredModuleId, ignoreCase = true)
                                    }
                                    .flatMap { it.nodes }
                                    .count { it.code in state.completedNodeCodes }
                                (matchedCompleted.toFloat() / req.requiredNodesCount.toFloat()).coerceIn(0f, 1f)
                            } else {
                                1f
                            }

                            CharacterItemCard(
                                item = item,
                                isSelected = isSelected,
                                isUnlocked = isUnlocked,
                                accentPalette = accentPalette,
                                unlockProgress = unlockProgress,
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

    if (showShiftDialog) {
        NanoShiftStatusDialog(
            avatarLabel = currentAvatar.label,
            specialistRole = specialistProfile.roleTitle,
            userName = state.userName,
            completedCount = state.completedCount,
            totalNodesCount = state.totalNodesCount,
            studyStreakDays = state.studyStreakDays,
            shiftMood = shiftMood,
            favoriteSequence = specialistProfile.favoriteSequence,
            activeExpression = forcedExpression ?: Expression.NETRAL,
            onSelectExpression = { exp ->
                forcedExpression = exp
                showShiftDialog = false
                triggerAvatarTap(
                    customMessage = "Ekspresi Nano diatur ke ${exp.name.lowercase().replaceFirstChar { it.uppercase() }}!",
                    expression = exp
                )
            },
            onDismiss = { showShiftDialog = false }
        )
    }

    if (showSnapshotStudio) {
        NanoSnapshotStudioDialog(
            workingState = workingState,
            currentAvatar = currentAvatar,
            specialistRole = specialistProfile.roleTitle,
            favoriteSequence = specialistProfile.favoriteSequence,
            userName = state.userName,
            accentPalette = accentPalette,
            completedCount = state.completedCount,
            totalNodesCount = state.totalNodesCount,
            onDismiss = { showSnapshotStudio = false },
            onExported = { msg ->
                triggerAvatarTap(msg, HapticFeedbackType.LongPress, Expression.BANGGA)
            }
        )
    }
}
