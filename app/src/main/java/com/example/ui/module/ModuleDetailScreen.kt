package com.example.ui.module

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MriModule
import com.example.data.MriNode
import com.example.data.MriRepository
import com.example.ui.components.AnimatedBookmarkPill
import com.example.ui.components.FlashcardDeckSection
import com.example.ui.components.StudyModeToggleBar
import com.example.ui.simulators.*
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import com.example.viewmodel.SimType
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun ModuleDetailScreen(
    module: MriModule,
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val moduleBookmarkedCount = module.nodes.count { state.bookmarkedNodeCodes.contains(it.code) }
    val moduleCompletedCount = module.nodes.count { state.completedNodeCodes.contains(it.code) }
    val totalModuleNodes = module.nodes.size
    val isModuleComplete = moduleCompletedCount == totalModuleNodes && totalModuleNodes > 0
    val moduleProgress = if (totalModuleNodes > 0) {
        (moduleCompletedCount.toFloat() / totalModuleNodes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Modul dengan Indikator Lingkaran Progres Belajar di Dalam Kartu Penjelasan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isModuleComplete) EmeraldSuccess.copy(alpha = 0.8f) else CyanPrimary.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                        ) {
                            Text(
                                text = module.code,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = module.levelBadge,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = module.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Lingkaran Progres Belajar di Dalam Kartu Penjelasan Modul
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(58.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { moduleProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (isModuleComplete) EmeraldSuccess else CyanBright,
                            trackColor = CardSlate,
                            strokeWidth = 5.dp
                        )
                        Text(
                            text = "$moduleCompletedCount/$totalModuleNodes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isModuleComplete) EmeraldSuccess else TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selesai",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isModuleComplete) EmeraldSuccess else CyanBright
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Toggle Bar: Materi Modul vs Flashcards Hafalan
        StudyModeToggleBar(
            primaryLabel = "Materi Modul (${module.nodes.size})",
            flashcardLabel = "Flashcards Hafalan",
            isFlashcardMode = state.isModuleFlashcardMode,
            onSelectMode = { viewModel.setModuleFlashcardMode(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = state.onlyBookmarkedNodes,
                onClick = { viewModel.setOnlyBookmarkedNodes(!state.onlyBookmarkedNodes) },
                modifier = Modifier.testTag("module_bookmark_filter_chip"),
                label = {
                    Text(
                        text = "★ Tersimpan ($moduleBookmarkedCount)",
                        fontSize = 11.sp,
                        fontWeight = if (state.onlyBookmarkedNodes) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberAccent,
                    selectedLabelColor = DeepObsidian,
                    containerColor = DarkNavySlate,
                    labelColor = AmberAccent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = state.onlyBookmarkedNodes,
                    borderColor = if (state.onlyBookmarkedNodes) AmberAccent else CardSlateBorder
                )
            )
            val availableLevels = remember(module.nodes) {
                module.nodes.map { if (it.level == 0) 1 else it.level }.toSet()
            }
            val levels = buildList {
                add(0 to "Semua Tingkat")
                if (1 in availableLevels) add(1 to "T1 (Fundamental)")
                if (2 in availableLevels) add(2 to "T2 (Klinis Menengah)")
                if (3 in availableLevels) add(3 to "T3 (Fisika Lanjut)")
            }
            levels.forEach { (lvl, label) ->
                FilterChipLevel(label = label, isSelected = state.nodeLevelFilter == lvl) {
                    viewModel.setNodeLevelFilter(lvl)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (state.isModuleFlashcardMode) {
            val moduleCards = MriRepository.flashcards.filter { card ->
                val matchesModule = card.moduleId == module.id
                val rawNodeLevel = module.nodes.find { it.code == card.nodeCode }?.level ?: 1
                val nodeLevel = if (rawNodeLevel == 0) 1 else rawNodeLevel
                val matchesLevel = state.nodeLevelFilter == 0 || nodeLevel == state.nodeLevelFilter
                val matchesBookmark = !state.onlyBookmarkedNodes || state.isFlashcardBookmarked(card)
                matchesModule && matchesLevel && matchesBookmark
            }

            FlashcardDeckSection(
                cards = moduleCards,
                currentIndex = state.moduleFlashcardIndex,
                isFlipped = state.isModuleFlashcardFlipped,
                shuffleSeed = state.moduleFlashcardShuffleSeed,
                masteredCardIds = state.masteredFlashcardIds,
                isCardBookmarked = { card -> state.isFlashcardBookmarked(card) },
                onToggleBookmark = { card -> viewModel.toggleFlashcardBookmark(card) },
                onFlip = { viewModel.flipFlashcard() },
                onPrev = { size -> viewModel.prevFlashcard(size) },
                onNext = { size -> viewModel.nextFlashcard(size) },
                onShuffle = { viewModel.shuffleFlashcards() },
                onToggleMastered = { id -> viewModel.toggleFlashcardMastered(id) },
                emptyMessage = if (state.onlyBookmarkedNodes) {
                    "Belum ada simpul materi yang disimpan (Bookmark) pada filter ini."
                } else {
                    "Tidak ada kartu hafalan pada tingkatan ini."
                }
            )
        } else {
            val filteredNodes = module.nodes.filter { node ->
                val effectiveLevel = if (node.level == 0) 1 else node.level
                val matchesLevel = state.nodeLevelFilter == 0 || effectiveLevel == state.nodeLevelFilter
                val matchesBookmark = !state.onlyBookmarkedNodes || state.bookmarkedNodeCodes.contains(node.code)
                matchesLevel && matchesBookmark
            }

            if (filteredNodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.onlyBookmarkedNodes) {
                            "Belum ada simpul materi yang disimpan (Bookmark) pada filter ini."
                        } else {
                            "Tidak ada simpul materi pada tingkatan ini."
                        },
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    filteredNodes.forEach { node ->
                        val isCompleted = state.completedNodeCodes.contains(node.code)
                        val isBookmarked = state.bookmarkedNodeCodes.contains(node.code)
                        val activeMode = state.nodeModeMap[node.code] ?: state.globalNodeMode
                        NodeCard(
                            node = node,
                            isCompleted = isCompleted,
                            isBookmarked = isBookmarked,
                            activeMode = activeMode,
                            onModeSelected = { mode -> viewModel.setNodeMode(node.code, mode) },
                            onToggleCompleted = { viewModel.toggleNodeCompletion(node.code) },
                            onMarkNodeCompleted = { code -> viewModel.markNodeCompleted(code.ifBlank { node.code }) },
                            onToggleBookmark = { viewModel.toggleNodeBookmark(node.code) },
                            onLaunchLab = { lab ->
                                when {
                                    lab == "ta_snr" || lab.startsWith("m9_") -> viewModel.selectSimType(SimType.PROTOKOL_TA_SNR)
                                    lab == "console_tabs" -> viewModel.selectSimType(SimType.KONSOL_VENDOR)
                                    lab.startsWith("m3_") || lab.startsWith("m3a_") || lab.startsWith("m3b_") || lab.startsWith("m3c_") -> viewModel.selectSimType(SimType.SEKUENS_KONTRAS_M03)
                                    lab.startsWith("m4_") || lab.startsWith("m4a_") || lab.startsWith("m4b_") || lab.startsWith("m4c_") -> viewModel.selectSimType(SimType.RUANG_K)
                                    lab.startsWith("m5_") -> viewModel.selectSimType(SimType.PESAWAT_MAGNET_M05)
                                    lab.startsWith("m6_") || lab.startsWith("m6a_") || lab.startsWith("m6b_") -> viewModel.selectSimType(SimType.GRADIEN_RF_M06)
                                    lab == "sar" || lab.startsWith("m7_") || lab.startsWith("m7a_") || lab.startsWith("m7b_") || lab.startsWith("m7c_") -> viewModel.selectSimType(SimType.SAR_SAFETY)
                                    lab.startsWith("m8_") || lab.startsWith("m8a_") || lab.startsWith("m8b_") || lab.startsWith("m8c_") -> viewModel.selectSimType(SimType.ARTEFAK_QA_M08)
                                    lab == "contrast" || lab.startsWith("m2_") -> viewModel.selectSimType(SimType.KONTRAS_TR_TE)
                                    lab.startsWith("m1_") || lab in listOf("presesi", "larmor", "atom_h", "cek_inti", "populasi_spin", "spektrum_em", "nmv_3d", "sudut_flip", "fase_fid", "bola_bloch", "tr_berulang") -> viewModel.selectSimType(SimType.SPIN_LARMOR)
                                    else -> viewModel.selectSimType(SimType.RUANG_K)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Card Pintasan Evaluasi Kuis Modul
        val moduleQuizzes = state.quizzesList.filter { it.moduleId == module.id }
        val moduleQuizCount = moduleQuizzes.size
        if (moduleQuizCount > 0) {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Evaluasi Pemahaman ${module.code}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tersedia $moduleQuizCount soal kuis interaktif (3 tingkatan) untuk menguji penguasaan materi ${module.title}.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val t1Count = moduleQuizzes.count { it.level == 1 }
                    val t2Count = moduleQuizzes.count { it.level == 2 }
                    val t3Count = moduleQuizzes.count { it.level == 3 }
                    if (t1Count > 0 || t2Count > 0 || t3Count > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (t1Count > 0) {
                                OutlinedButton(
                                    onClick = { viewModel.openModuleQuiz(module.id, 1) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f))
                                ) {
                                    Text("T1 ($t1Count)", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (t2Count > 0) {
                                OutlinedButton(
                                    onClick = { viewModel.openModuleQuiz(module.id, 2) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.6f))
                                ) {
                                    Text("T2 ($t2Count)", fontSize = 10.sp, color = CyanBright, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (t3Count > 0) {
                                OutlinedButton(
                                    onClick = { viewModel.openModuleQuiz(module.id, 3) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.6f))
                                ) {
                                    Text("T3 ($t3Count)", fontSize = 10.sp, color = AmberWarning, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.openModuleQuiz(module.id, 0) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_module_quiz_${module.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = DeepObsidian,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mulai Kuis ${module.code} ($moduleQuizCount Soal)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepObsidian
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FilterChipLevel(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanPrimary,
            selectedLabelColor = DeepObsidian,
            containerColor = DarkNavySlate,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) CyanBright else CardSlateBorder
        )
    )
}

@Composable
fun NodeCard(
    node: MriNode,
    isCompleted: Boolean,
    isBookmarked: Boolean,
    activeMode: String,
    onModeSelected: (String) -> Unit,
    onToggleCompleted: () -> Unit,
    onToggleBookmark: () -> Unit,
    onLaunchLab: (String) -> Unit,
    onMarkNodeCompleted: (String) -> Unit = { if (!isCompleted) onToggleCompleted() }
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (isBookmarked) AmberAccent.copy(alpha = 0.75f) else CardSlateBorder,
        animationSpec = tween(durationMillis = 240),
        label = "node_card_border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_card_${node.code}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Kode Simpul, Tag Level, Bookmark, dan Tombol Selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node.code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = DeepObsidian
                    ) {
                        Text(
                            text = if (node.level == 0) "Pendahuluan" else "Tingkat ${node.level}",
                            fontSize = 9.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedBookmarkPill(
                        isBookmarked = isBookmarked,
                        onToggleBookmark = onToggleBookmark,
                        testTag = "bookmark_node_${node.code}",
                        savedContentDescription = "Hapus Bookmark Materi",
                        unsavedContentDescription = "Simpan Bookmark Materi"
                    )

                    var isCompleteAnimTriggered by remember { mutableStateOf(false) }
                    LaunchedEffect(isCompleteAnimTriggered) {
                        if (isCompleteAnimTriggered) {
                            delay(340)
                            isCompleteAnimTriggered = false
                        }
                    }

                    val completeScale by animateFloatAsState(
                        targetValue = when {
                            isCompleteAnimTriggered -> 1.24f
                            isCompleted -> 1.04f
                            else -> 1f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "complete_pill_scale"
                    )
                    val completeRotation by animateFloatAsState(
                        targetValue = if (isCompleteAnimTriggered) -8f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "complete_pill_rotation"
                    )
                    val completeBgColor by animateColorAsState(
                        targetValue = if (isCompleted) EmeraldDark else CardSlate,
                        animationSpec = tween(durationMillis = 240),
                        label = "complete_pill_bg"
                    )
                    val completeBorderColor by animateColorAsState(
                        targetValue = if (isCompleted) EmeraldSuccess else CardSlateBorder,
                        animationSpec = tween(durationMillis = 240),
                        label = "complete_pill_border"
                    )

                    Surface(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = completeScale
                                scaleY = completeScale
                                rotationZ = completeRotation
                            }
                            .clip(CircleShape)
                            .clickable {
                                isCompleteAnimTriggered = true
                                onToggleCompleted()
                            }
                            .testTag("complete_node_${node.code}"),
                        shape = CircleShape,
                        color = completeBgColor,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            completeBorderColor
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (isCompleted) EmeraldSuccess else TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isCompleted) "Selesai" else "Tandai",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) EmeraldSuccess else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = node.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (!node.prerequisite.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚑ Prasyarat: ${node.prerequisite}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            if (!node.learningGoal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tujuan belajar: ${node.learningGoal}",
                    fontSize = 11.sp,
                    color = CyanBright,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Mode Segmented Control: [Singkat | Lengkap | Analogi]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepObsidian)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeButton(
                    label = "Ringkasan",
                    isSelected = activeMode == "s",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_singkat_${node.code}")
                ) {
                    onModeSelected("s")
                }
                ModeButton(
                    label = "Materi Lengkap",
                    isSelected = activeMode == "l",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_lengkap_${node.code}")
                ) {
                    onModeSelected("l")
                }
                ModeButton(
                    label = "Analogi",
                    isSelected = activeMode == "a",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_analogi_${node.code}")
                ) {
                    onModeSelected("a")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Mode Content
            when (activeMode) {
                "s" -> {
                    Text(
                        text = node.summaryShort,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp
                    )
                }
                "l" -> {
                    val paragraphs = node.detailFull.split("\n\n")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        paragraphs.forEach { block ->
                            if (block.trim().startsWith("Rumus:")) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = CardSlate,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                                ) {
                                    Text(
                                        text = block.trim(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyanBright,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = block,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
                "a" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CyanDark.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = node.analogi,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = CyanBright,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (!node.crossLinks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = node.crossLinks,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Inline Modul 1 Interactive Activity: Cek Inti Aktif MR (t1-4b-inti-aktif-mr & t3-4-pengecualian)
            if (node.labType == "cek_inti") {
                Spacer(modifier = Modifier.height(12.dp))
                InlineMrActiveNucleusChecker(nodeCode = node.code)
            }

            // Inline Modul 1 Interactive Activity: Spektrum Elektromagnetik (t1-7-spektrum-em)
            if (node.labType == "spektrum_em") {
                Spacer(modifier = Modifier.height(12.dp))
                InlineEmSpectrumExplorer()
            }

            // Inline Modul 5 Native Jetpack Compose Interactive Activities (A1 - A7)
            when (node.labType) {
                "m5_a1_penampang" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A1PenampangPesawatSimulator()
                }
                "m5_a2_ppm_hz" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A2PpmKeHzCalculator()
                }
                "m5_a3_superkonduktif" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A3SuperkonduktifSimulator()
                }
                "m5_a4_perisai_5g" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A4Perisai5GSimulator()
                }
                "m5_a5_b0_snr_sar" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A5B0SnrSarSimulator()
                }
                "m5_a6_pilih_magnet" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A6PilihMagnetActivity()
                }
                "m5_a7_quench" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    A7UrutanQuenchActivity()
                }
                "m6a_peta_frekuensi", "m6a_sumbu_gradien" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientTrapezoidAndLobesSimulator(initialTab = 2)
                }
                "m6a_trapesium_gradien", "m6a_koil_maxwell_golay" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientTrapezoidAndLobesSimulator(initialTab = 0)
                }
                "m6a_gradien_seimbang" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientTrapezoidAndLobesSimulator(initialTab = 1)
                }
                "m6b_a1_rantai_sinyal" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    RfSignalChainSimulator()
                }
                "m6b_a2_a3_a4_koil" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    CoilSelectorAndDepthSimulator()
                }
                "m6b_a5_faraday" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    FaradayShieldArtifactSimulator()
                }
                "m6b_a8_birdcage" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    BirdcageAndB1ParallelTransmitSimulator(initialSection = 0)
                }
                "m6b_a6_chemical_shift" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    BirdcageAndB1ParallelTransmitSimulator(initialSection = 1)
                }
                "m6b_a7_b1_ptx" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    BirdcageAndB1ParallelTransmitSimulator(initialSection = 2)
                }
                // Modul 7 (M07A, M07B, M07C) Native Jetpack Compose Interactive Activities
                "m7a_zona_akses" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07AZoneAndAccessSimulator()
                }
                "m7a_skrining" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07AScreeningScenarioSimulator(onComplete = { onMarkNodeCompleted(node.code) })
                }
                "m7a_label_alur" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07ADeviceLabelAndFlowSimulator()
                }
                "m7a_lima_bahaya" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07AFiveHazardsScannerSimulator()
                }
                "m7a_kecemasan" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07APatientAnxietyJourneySimulator()
                }
                "m7a_tips" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07ASafetyTipsTrueFalseSimulator(onComplete = { onMarkNodeCompleted(node.code) })
                }
                "m7a_kontras" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07AContrastScreeningSimulator()
                }
                "m7b_medan_tepi" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07BFringeFieldAndForceSimulator()
                }
                "m7b_torsi" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07BImplantTorqueSimulator()
                }
                "m7b_skala_efek" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07BFieldScaleEffectsSimulator()
                }
                "m7b_oksigen_helium" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07BOxygenAndHeliumExpansionSimulator()
                }
                "m7b_lenz" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07BLenzEffectDropSimulator()
                }
                "m7b_darurat" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07BEmergencyActionSimulator(onComplete = { onMarkNodeCompleted(node.code) })
                }
                "m7c_sar" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07CSarCalculatorSimulator()
                }
                "m7c_antena_loop" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07CAntennaAndLoopBurnSimulator()
                }
                "m7c_stimulasi" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07CPnsThresholdSimulator()
                }
                "m7c_bising" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07CAcousticNoiseCalculatorSimulator()
                }
                "m7c_b1_jaringan" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    M07CRfWavelengthInTissueSimulator()
                }
                // Modul 8 (M08A, M08B, M08C) Native Jetpack Compose Interactive Activities
                "m8a_luar_fase" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    EncodingAndTissueArtifactsSimulator(
                        initialTab = 0,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8a_magic_angle" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    EncodingAndTissueArtifactsSimulator(
                        initialTab = 1,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8a_ghosting", "m8a_resp_gating" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    EncodingAndTissueArtifactsSimulator(
                        initialTab = 2,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8a_aliasing" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    EncodingAndTissueArtifactsSimulator(
                        initialTab = 3,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8a_chemical_shift", "m8a_gibbs" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    EncodingAndTissueArtifactsSimulator(
                        initialTab = 4,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8b_aliran" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowAndCompensationSimulator(
                        initialTab = 0,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8b_tof", "m8b_reynolds" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowAndCompensationSimulator(
                        initialTab = 1,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8b_gmn" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowAndCompensationSimulator(
                        initialTab = 2,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8b_presat" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowAndCompensationSimulator(
                        initialTab = 3,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8c_qa_fantom" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    HardwareQaAndCapstoneSimulator(
                        initialTab = 0,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8c_celah_irisan" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    HardwareQaAndCapstoneSimulator(
                        initialTab = 1,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8c_spike_moire", "m8c_logam_udara" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    HardwareQaAndCapstoneSimulator(
                        initialTab = 2,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                "m8c_capstone_tabel88" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    HardwareQaAndCapstoneSimulator(
                        initialTab = 3,
                        onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) }
                    )
                }
                // Modul 3 (M03A, M03B, M03C) Native Jetpack Compose Interactive Activities
                "m3a_spin_echo" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpinEchoIrTseSimulator(initialTab = 0, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3a_ir_null" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpinEchoIrTseSimulator(initialTab = 1, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3a_tse" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpinEchoIrTseSimulator(initialTab = 2, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3a_drive" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpinEchoIrTseSimulator(initialTab = 3, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3b_ernst" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientEchoSteadyStateSimulator(initialTab = 0, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3b_gre_kompor", "m3b_steady_state" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientEchoSteadyStateSimulator(initialTab = 1, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3b_bipolar_rewinder" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientEchoSteadyStateSimulator(initialTab = 2, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3b_tiga_tunak" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientEchoSteadyStateSimulator(initialTab = 3, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3b_t2star_servikal", "m3b_epi" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientEchoSteadyStateSimulator(initialTab = 4, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3c_dwi" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpecialContrastDwiAgentSimulator(initialTab = 0, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3c_kontras" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpecialContrastDwiAgentSimulator(initialTab = 1, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
                "m3c_bold_mtc" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    SpecialContrastDwiAgentSimulator(initialTab = 2, onNodeComplete = { targetCode -> onMarkNodeCompleted(targetCode.ifBlank { node.code }) })
                }
            }


            // Integrated Activity Badge (🧪)
            if (node.labType != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { onLaunchLab(node.labType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_lab_${node.code}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepObsidian,
                        contentColor = CyanBright
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (node.labType) {
                            "ta_snr" -> "🧪 S1: Kalkulator TA, Voksel & SNR"
                            "console_tabs" -> "🧪 S2: Peta Tab Konsol & Tabel Vendor"
                            "m5_a1_penampang" -> "🧪 Buka Lab M05: A1 Penampang Pesawat"
                            "m5_a2_ppm_hz" -> "🧪 Buka Lab M05: A2 Kalkulator ppm ke Hz"
                            "m5_a3_superkonduktif" -> "🧪 Buka Lab M05: A3 Magnet Superkonduktif"
                            "m5_a4_perisai_5g" -> "🧪 Buka Lab M05: A4 Perisai Medan Tepi 5G"
                            "m5_a5_b0_snr_sar" -> "🧪 Buka Lab M05: A5 B0 vs SNR & SAR"
                            "m5_a6_pilih_magnet" -> "🧪 Buka Lab M05: A6 Pilih Magnet"
                            "m5_a7_quench" -> "🧪 Buka Lab M05: A7 Urutan Quench"
                            "m6a_peta_frekuensi", "m6a_sumbu_gradien" -> "🧪 Buka Lab M06A: Peta Frekuensi & Sumbu Gradien"
                            "m6a_trapesium_gradien", "m6a_koil_maxwell_golay" -> "🧪 Buka Lab M06A: Trapesium Slew Rate & Maxwell/Golay"
                            "m6a_gradien_seimbang" -> "🧪 Buka Lab M06A: Gradien Seimbang & Stejskal–Tanner"
                            "m6b_a1_rantai_sinyal" -> "🧪 Buka Lab M06B: A1 Rantai Sinyal Penerima"
                            "m6b_a2_a3_a4_koil" -> "🧪 Buka Lab M06B: A2–A4 Pemilih Koil & Array Fasa"
                            "m6b_a5_faraday" -> "🧪 Buka Lab M06B: A5 Kandang Faraday & Artefak RF"
                            "m6b_a6_chemical_shift" -> "🧪 Buka Lab M06B: A6 Spektrum Pergeseran Kimia"
                            "m6b_a7_b1_ptx" -> "🧪 Buka Lab M06B: A7 Peta B1 & Transmit Paralel"
                            "m6b_a8_birdcage" -> "🧪 Buka Lab M06B: A8 Birdcage Kuadratur & Faktor Pengisi"
                            "m7a_zona_akses" -> "🧪 Buka Lab M07A: A1 Denah Zona I–IV & Akses Personel"
                            "m7a_skrining" -> "🧪 Buka Lab M07A: A2 Simulasi Keputusan Skrining"
                            "m7a_label_alur" -> "🧪 Buka Lab M07A: A3 Standar Label ASTM & Alur Implan"
                            "m7a_lima_bahaya" -> "🧪 Buka Lab M07A: A4 Peta 5 Bahaya Utama Pemindai"
                            "m7a_kecemasan" -> "🧪 Buka Lab M07A: A5 Perjalanan Pasien & Kecemasan"
                            "m7a_tips" -> "🧪 Buka Lab M07A: A6 Pernyataan Benar/Salah Keselamatan"
                            "m7a_kontras" -> "🧪 Buka Lab M07A: A7 Skrining Agen Kontras & eGFR"
                            "m7b_medan_tepi" -> "🧪 Buka Lab M07B: A1 Kurva Medan Tepi 5G & Gaya Tarik"
                            "m7b_torsi" -> "🧪 Buka Lab M07B: A2 Torsi Implan Memanjang"
                            "m7b_skala_efek" -> "🧪 Buka Lab M07B: A3 Skala Efek Medan Statis B0"
                            "m7b_oksigen_helium" -> "🧪 Buka Lab M07B: A4 Oksigen Ruang & Ekspansi Helium"
                            "m7b_lenz" -> "🧪 Buka Lab M07B: A5 Efek Lenz (Arus Pusar)"
                            "m7b_darurat" -> "🧪 Buka Lab M07B: A6 Tindakan Darurat di Ruang MR"
                            "m7c_sar" -> "🧪 Buka Lab M07C: A1 Kalkulator SAR & Tabel 10.1 IEC"
                            "m7c_antena_loop" -> "🧪 Buka Lab M07C: A2 Efek Antena & Loop Konduktif"
                            "m7c_stimulasi" -> "🧪 Buka Lab M07C: A3 Ambang Stimulasi Saraf Tepi (PNS)"
                            "m7c_bising" -> "🧪 Buka Lab M07C: A4 Kalkulator Bising Desibel & Pelindung"
                            "m7c_b1_jaringan" -> "🧪 Buka Lab M07C: A5 Panjang Gelombang RF & Ketidakseragaman B1"
                            "m8a_luar_fase" -> "🧪 Buka Lab M08A: A1 Pembatalan Luar-Fase & Dixon"
                            "m8a_magic_angle" -> "🧪 Buka Lab M08A: A2 Sudut Ajaib Tendon (54,7°)"
                            "m8a_ghosting", "m8a_resp_gating" -> "🧪 Buka Lab M08A: A3–A4 Ghosting Gerak & Gating Napas"
                            "m8a_aliasing" -> "🧪 Buka Lab M08A: A5–A6 Aliasing Fase & Nyquist"
                            "m8a_chemical_shift", "m8a_gibbs" -> "🧪 Buka Lab M08A: A7–A8 Pergeseran Kimia & Gibbs Ringing"
                            "m8b_aliran" -> "🧪 Buka Lab M08B: A1 Fenomena Aliran & Entry-Slice (ESP)"
                            "m8b_tof", "m8b_reynolds" -> "🧪 Buka Lab M08B: A2–A3 TOF Spin-Echo & Bilangan Reynolds"
                            "m8b_gmn" -> "🧪 Buka Lab M08B: A4 Dephasing Intravoksel & GMN (+1, −2, +1)"
                            "m8b_presat" -> "🧪 Buka Lab M08B: A5–A6 Presaturasi Spasial & Skenario Aliran"
                            "m8c_qa_fantom" -> "🧪 Buka Lab M08C: A1 Simulasi QA Fantom Harian/Mingguan"
                            "m8c_celah_irisan" -> "🧪 Buka Lab M08C: A2 Celah Irisan & Cross-Excitation"
                            "m8c_spike_moire", "m8c_logam_udara" -> "🧪 Buka Lab M08C: A3 & A6 Spike Ruang-k & Suseptibilitas Logam"
                            "m8c_capstone_tabel88" -> "🧪 Buka Lab M08C: A4–A5 Tabel 8.8 Remediasi & Tebak Artefak"
                            else -> "🧪 Praktik Simulasi Terintegrasi"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class IsotopePreset(
    val label: String,
    val symbol: String,
    val z: Int,
    val n: Int
)

private val ISOTOPE_PRESETS = listOf(
    IsotopePreset("¹H (Hidrogen)", "H", 1, 0),
    IsotopePreset("²H (Deuterium)", "²H", 1, 1),
    IsotopePreset("¹⁴N (Nitrogen-14)", "N", 7, 7),
    IsotopePreset("¹³C (Karbon-13)", "C", 6, 7),
    IsotopePreset("¹⁹F (Fluor-19)", "F", 9, 10),
    IsotopePreset("²³Na (Natrium-23)", "Na", 11, 12),
    IsotopePreset("³¹P (Fosfor-31)", "P", 15, 16),
    IsotopePreset("¹²C (Karbon-12)", "C", 6, 6),
    IsotopePreset("¹⁶O (Oksigen-16)", "O", 8, 8)
)

@Composable
private fun InlineMrActiveNucleusChecker(nodeCode: String = "") {
    val isExceptionLesson = nodeCode == "M01.T3.4"
    var z by rememberSaveable(nodeCode) { mutableIntStateOf(if (isExceptionLesson) 7 else 1) }
    var n by rememberSaveable(nodeCode) { mutableIntStateOf(if (isExceptionLesson) 7 else 0) }
    val a = z + n
    val bothEven = (z % 2 == 0) && (n % 2 == 0)
    val bothOdd = (z % 2 == 1) && (n % 2 == 1)
    val isActive = !bothEven

    val matchedPreset = ISOTOPE_PRESETS.firstOrNull { it.z == z && it.n == n }
    val elementSymbol = matchedPreset?.symbol ?: when (z) {
        1 -> if (n == 1) "²H" else "H"
        2 -> "He"
        3 -> "Li"
        4 -> "Be"
        5 -> "B"
        6 -> "C"
        7 -> "N"
        8 -> "O"
        9 -> "F"
        10 -> "Ne"
        11 -> "Na"
        12 -> "Mg"
        13 -> "Al"
        14 -> "Si"
        15 -> "P"
        16 -> "S"
        17 -> "Cl"
        18 -> "Ar"
        19 -> "K"
        20 -> "Ca"
        else -> "X"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inline_mr_active_checker"),
        shape = RoundedCornerShape(14.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isExceptionLesson) "Uji Kasus Pengecualian: Apakah Inti Aktif MR?" else "Coba Interaktif: Apakah Inti Ini Aktif MR?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBright
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExceptionLesson) {
                    "Fokus Pengecualian ²H (Z=1, N=1) & ¹⁴N (Z=7, N=7): nomor massa A genap tetapi arah putar nukleon tidak seimbang sehingga tetap memiliki spin neto bulat (I ≥ 1)!"
                } else {
                    "Pilih contoh inti atau atur Proton (Z) & Neutron (N) untuk menguji spin magnetik neto atom:"
                },
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Isotop (Horizontal Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ISOTOPE_PRESETS.forEach { preset ->
                    val isSelected = z == preset.z && n == preset.n
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                z = preset.z
                                n = preset.n
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) CyanPrimary else CardSlate,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanBright else CardSlateBorder
                        )
                    ) {
                        Text(
                            text = preset.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DeepObsidian else TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Ringkasan Notasi Nuklida
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CardSlate,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Notasi Nuklida Simbol Kimia: A di atas, Z di bawah, X besar di kanan
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(DarkNavySlate, RoundedCornerShape(10.dp))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$a",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanBright
                            )
                            Text(
                                text = "$z",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = elementSymbol,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Keterangan A, Z, N
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nomor Massa (A):", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "$a (${if (a % 2 != 0) "Ganjil" else "Genap"})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (a % 2 != 0) EmeraldSuccess else AmberWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Proton (Z):", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "$z (${if (z % 2 != 0) "Ganjil" else "Genap"})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanBright
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Neutron (N):", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "$n (${if (n % 2 != 0) "Ganjil" else "Genap"})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kontrol Stepper Proton (Z) & Neutron (N)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stepper Proton (Z)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkNavySlate,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Proton (Z)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Tombol Minus Z
                            Surface(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = z > 1) { z = (z - 1).coerceAtLeast(1) },
                                shape = CircleShape,
                                color = if (z > 1) CardSlate else DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (z > 1) CyanPrimary.copy(alpha = 0.5f) else CardSlateBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Kurang Proton",
                                        tint = if (z > 1) TextPrimary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$z",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )

                            // Tombol Plus Z
                            Surface(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = z < 30) { z = (z + 1).coerceAtMost(30) },
                                shape = CircleShape,
                                color = if (z < 30) CardSlate else DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (z < 30) CyanPrimary.copy(alpha = 0.5f) else CardSlateBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah Proton",
                                        tint = if (z < 30) TextPrimary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Stepper Neutron (N)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkNavySlate,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Neutron (N)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Tombol Minus N
                            Surface(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = n > 0) { n = (n - 1).coerceAtLeast(0) },
                                shape = CircleShape,
                                color = if (n > 0) CardSlate else DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (n > 0) AmberAccent.copy(alpha = 0.5f) else CardSlateBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Kurang Neutron",
                                        tint = if (n > 0) TextPrimary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$n",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )

                            // Tombol Plus N
                            Surface(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = n < 40) { n = (n + 1).coerceAtMost(40) },
                                shape = CircleShape,
                                color = if (n < 40) CardSlate else DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (n < 40) AmberAccent.copy(alpha = 0.5f) else CardSlateBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah Neutron",
                                        tint = if (n < 40) TextPrimary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Status Hasil Evaluasi Fisika MRI
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = when {
                    bothOdd -> AmberDark.copy(alpha = 0.35f)
                    isActive -> EmeraldDark.copy(alpha = 0.35f)
                    else -> RoseDark.copy(alpha = 0.35f)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        bothOdd -> AmberWarning
                        isActive -> EmeraldSuccess
                        else -> RoseDanger
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = when {
                            bothOdd -> Icons.Default.Warning
                            isActive -> Icons.Default.CheckCircle
                            else -> Icons.Default.Close
                        },
                        contentDescription = null,
                        tint = when {
                            bothOdd -> AmberWarning
                            isActive -> EmeraldSuccess
                            else -> RoseDanger
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when {
                                bothOdd -> "✓ Aktif MR (Pengecualian: Z & N Ganjil, A=$a Genap)"
                                isActive -> "✓ Aktif MR — Nomor Massa A = $a Ganjil (Spin Neto ≠ 0)"
                                else -> "✗ Tidak Aktif MR — Z & N Keduanya Genap (Spin = 0)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                bothOdd -> AmberWarning
                                isActive -> EmeraldSuccess
                                else -> RoseDanger
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                z == 1 && n == 1 -> "Pengecualian Deuterium (²H): Z = 1 (ganjil) dan N = 1 (ganjil). Nomor massanya genap (A = 2), namun karena proton dan neutron tidak saling meniadakan secara lengkap, inti tetap memiliki spin neto bulat (I = 1). Aktif MR!"
                                z == 7 && n == 7 -> "Pengecualian Nitrogen-14 (¹⁴N): Z = 7 (ganjil) dan N = 7 (ganjil). Nomor massanya genap (A = 14), namun nukleon tidak berpasangan sempurna sehingga menghasilkan spin neto bulat (I = 1). Aktif MR!"
                                bothOdd -> "Pengecualian nomor massa genap: Z ($z) ganjil dan N ($n) ganjil. Meskipun nomor massa A = $a genap, nukleon tidak berpasangan sempurna sehingga menghasilkan spin bilangan bulat (I = 1, 2, ...). Inti ini aktif MR!"
                                z == 1 && n == 0 -> "Inti utama MRI klinis: Hidrogen (¹H) memiliki kelimpahan tertinggi dalam air & lemak tubuh serta momen magnetik terkuat (spin ½)."
                                isActive -> "Nomor massa A = $a bernilai GANJIL. Inti memiliki nukleon tak berpasangan sehingga menghasilkan momentum sudut dan momen magnetik neto (spin ½, 3/2, 5/2, ...). Inti ini aktif beresonansi magnetik!"
                                else -> "Nomor proton Z ($z) genap dan neutron N ($n) genap. Semua nukleon berpasangan anti-sejajar secara lengkap sehingga spin neto saling meniadakan (I = 0). Inti ini tidak menghasilkan sinyal MRI."
                            },
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatFrequency(freqHz: Double): String {
    return when {
        freqHz >= 1e18 -> String.format(Locale.US, "%.1f EHz", freqHz / 1e18)
        freqHz >= 1e15 -> String.format(Locale.US, "%.1f PHz", freqHz / 1e15)
        freqHz >= 1e12 -> String.format(Locale.US, "%.1f THz", freqHz / 1e12)
        freqHz >= 1e9 -> String.format(Locale.US, "%.1f GHz", freqHz / 1e9)
        freqHz >= 1e6 -> String.format(Locale.US, "%.2f MHz", freqHz / 1e6)
        freqHz >= 1e3 -> String.format(Locale.US, "%.1f kHz", freqHz / 1e3)
        else -> String.format(Locale.US, "%.0f Hz", freqHz)
    }
}

@Composable
private fun InlineEmSpectrumExplorer() {
    var b0 by rememberSaveable { mutableFloatStateOf(1.5f) }
    val mriFreqHz = 42.58f * b0 * 1e6f
    val mriLogHz = log10(mriFreqHz.toDouble()).toFloat()
    var logHz by rememberSaveable { mutableFloatStateOf(mriLogHz) }

    val bandName = when {
        logHz < 9f -> "Gelombang Radio / RF (Pita MRI — Non-Pengion)"
        logHz < 11.5f -> "Gelombang Mikro (Non-Pengion)"
        logHz < 14.1f -> "Inframerah (Non-Pengion)"
        logHz < 14.9f -> "Cahaya Tampak (Non-Pengion)"
        logHz < 16.5f -> "Ultraviolet (Mulai Pengion)"
        logHz < 19f -> "Sinar-X (Radiasi Pengion)"
        else -> "Sinar Gamma (Radiasi Pengion)"
    }
    val isIonizing = logHz >= 15.5f
    val freqHz = 10.0.pow(logHz.toDouble())
    val photonEv = 4.1357e-15 * freqHz

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inline_em_spectrum_explorer"),
        shape = RoundedCornerShape(14.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Judul dan Tombol Reset Pintas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Peta Spektrum EM & MRI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyanDark.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable {
                        logHz = log10((42.58f * b0 * 1e6f).toDouble()).toFloat()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pita RF MRI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Pita Spektrum Cepat
            Text(
                text = "Pilih Pita Spektrum:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    Triple("Radio (MRI)", 7.81f, false),
                    Triple("Gel. Mikro", 10.0f, false),
                    Triple("Inframerah", 13.5f, false),
                    Triple("Cahaya Tampak", 14.5f, false),
                    Triple("Ultraviolet", 15.8f, true),
                    Triple("Sinar-X", 17.5f, true),
                    Triple("Sinar Gamma", 19.5f, true)
                )
                presets.forEach { (label, presetLogHz, presetIonizing) ->
                    val isSelected = when (label) {
                        "Radio (MRI)" -> logHz < 9.0f
                        "Gel. Mikro" -> logHz in 9.0f..11.5f
                        "Inframerah" -> logHz in 11.5f..14.1f
                        "Cahaya Tampak" -> logHz in 14.1f..14.9f
                        "Ultraviolet" -> logHz in 14.9f..16.5f
                        "Sinar-X" -> logHz in 16.5f..19.0f
                        else -> logHz >= 19.0f
                    }
                    val chipBorder = if (isSelected) {
                        if (presetIonizing) RoseDanger else CyanBright
                    } else {
                        CardSlateBorder
                    }
                    val chipBg = if (isSelected) {
                        if (presetIonizing) RoseDark.copy(alpha = 0.5f) else CyanDark.copy(alpha = 0.5f)
                    } else {
                        CardSlate.copy(alpha = 0.6f)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = chipBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                        modifier = Modifier.clickable { logHz = presetLogHz }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) (if (presetIonizing) RoseDanger else CyanBright) else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kartu Status: Pengion vs Non-Pengion
            val statusBg = if (isIonizing) RoseDark.copy(alpha = 0.25f) else EmeraldDark.copy(alpha = 0.25f)
            val statusBorder = if (isIonizing) RoseDanger.copy(alpha = 0.6f) else EmeraldSuccess.copy(alpha = 0.6f)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = statusBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (isIonizing) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isIonizing) RoseDanger else EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isIonizing) "⚠️ $bandName — RADIASI PENGION" else "✓ $bandName — NON-PENGION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIonizing) RoseDanger else EmeraldSuccess
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isIonizing) {
                                "Energi foton (≥ 10 eV) cukup kuat untuk melepaskan elektron dan merusak DNA seluler. (Contoh: Rontgen/CT-Scan, BUKAN MRI!)"
                            } else {
                                "Energi foton (< 10 eV) sangat rendah, hanya menyebabkan transisi spin magnetik nuklir & efek termal ringan tanpa merusak DNA."
                            },
                            fontSize = 10.sp,
                            color = TextPrimary.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Parameter Frekuensi & Energi
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DarkNavySlate.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "log₁₀(f) = ${String.format(Locale.US, "%.2f", logHz)}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "f ≈ ${formatFrequency(freqHz)}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isIonizing) RoseDanger else CyanBright
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Energi: ${String.format(Locale.US, "%.2e eV", photonEv)}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "Larmor ¹H: ${String.format(Locale.US, "%.2f MHz", 42.58f * b0)} (@ ${String.format(Locale.US, "%.1f T", b0)})",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Frekuensi Spektrum EM
            Text(
                text = "Geser Frekuensi Spektrum EM (10⁶ – 10²⁰ Hz):",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Slider(
                value = logHz,
                onValueChange = { logHz = it },
                valueRange = 6f..20f,
                colors = SliderDefaults.colors(
                    thumbColor = if (isIonizing) RoseDanger else CyanBright,
                    activeTrackColor = if (isIonizing) RoseDanger else CyanPrimary,
                    inactiveTrackColor = CardSlate
                )
            )

            // Slider 2: Kuat Medan Magnet B0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kuat Medan Magnet B₀: ${String.format(Locale.US, "%.1f T", b0)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.5f, 1.5f, 3.0f, 7.0f).forEach { presetB0 ->
                        val isSelectedB0 = kotlin.math.abs(b0 - presetB0) < 0.05f
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelectedB0) EmeraldDark else CardSlate,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelectedB0) EmeraldSuccess else CardSlateBorder
                            ),
                            modifier = Modifier.clickable {
                                b0 = presetB0
                                logHz = log10((42.58f * presetB0 * 1e6f).toDouble()).toFloat()
                            }
                        ) {
                            Text(
                                text = "${presetB0}T",
                                fontSize = 9.sp,
                                fontWeight = if (isSelectedB0) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelectedB0) EmeraldSuccess else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Slider(
                value = b0,
                onValueChange = {
                    b0 = it
                    logHz = log10((42.58f * it * 1e6f).toDouble()).toFloat()
                },
                valueRange = 0.2f..7.0f,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldSuccess,
                    activeTrackColor = EmeraldSuccess,
                    inactiveTrackColor = CardSlate
                )
            )
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CardSlate else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CyanBright else TextSecondary
        )
    }
}



