package com.example.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CharacterCatalog
import com.example.ui.character.AvatarView
import com.example.ui.character.Expression
import com.example.ui.components.rememberRestorableScrollState
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

@Composable
fun QuizScreen(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    val quizzes = state.filteredQuizzes
    val allQuizzes = state.quizzesList
    var showStatsDialog by rememberSaveable { mutableStateOf(false) }
    var isFilterExpanded by rememberSaveable { mutableStateOf(false) }
    var showPreSubmitHint by rememberSaveable(state.currentQuizIndex, state.quizModuleFilter) {
        mutableStateOf(false)
    }

    val scopedAttempts = state.quizAttempts.filter { attempt ->
        val filter = state.quizModuleFilter
        filter == "SEMUA" ||
            attempt.moduleId == filter ||
            (filter.length <= 3 && attempt.moduleId.startsWith(filter)) ||
            attempt.nodeCode.startsWith(filter)
    }
    val totalAttempts = scopedAttempts.size
    val correctAttempts = scopedAttempts.count { it.isCorrect }
    val latestAttemptByQuizId = scopedAttempts
        .groupBy { it.quizId }
        .mapValues { (_, list) -> list.maxByOrNull { it.answeredAt } }
    val stillWrongAttempts = latestAttemptByQuizId.values
        .filterNotNull()
        .filter { !it.isCorrect }
    val accuracyPct = if (totalAttempts > 0) (correctAttempts * 100) / totalAttempts else 0

    val currentQuiz = quizzes.getOrNull(state.currentQuizIndex)
    val quizScrollState = rememberRestorableScrollState("tab_kuis", viewModel)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // 1. HEADER BERSIH & ERGONOMIS: Judul + Tombol Filter Paket + Tombol Statistik
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kuis Evaluasi Mandiri",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Latihan terstruktur M01–M09 · ${quizzes.size} soal aktif",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Tombol Statistik Ringkas (tidak lagi menutupi tombol bawah!)
                Surface(
                    onClick = { showStatsDialog = !showStatsDialog },
                    shape = RoundedCornerShape(12.dp),
                    color = if (showStatsDialog) CyanPrimary else DarkNavySlate,
                    border = BorderStroke(
                        1.dp,
                        if (showStatsDialog) CyanBright else CyanPrimary.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val barColor = if (showStatsDialog) DeepObsidian else CyanBright
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val barW = size.width * 0.22f
                            val gap = size.width * 0.11f
                            val startX = (size.width - (3 * barW + 2 * gap)) / 2f
                            val bottomY = size.height * 0.88f
                            listOf(0.35f, 0.62f, 0.88f).forEachIndexed { idx, frac ->
                                val h = size.height * frac
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(startX + idx * (barW + gap), bottomY - h),
                                    size = Size(barW, h),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                        Text(
                            text = if (totalAttempts > 0) "$accuracyPct%" else "Stats",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showStatsDialog) DeepObsidian else CyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. RINGKASAN FILTER RINGKAS + TOGGLE PANEL FILTER LENGKAP
            val levelTitle = when (state.quizLevelFilter) {
                1 -> "T1 Dasar"
                2 -> "T2 Menengah"
                3 -> "T3 Lanjut"
                else -> "Semua Tingkat"
            }
            val modTitle = if (state.quizModuleFilter == "SEMUA") "Semua Modul" else state.quizModuleFilter

            Surface(
                onClick = { isFilterExpanded = !isFilterExpanded },
                shape = RoundedCornerShape(14.dp),
                color = DarkNavySlate,
                border = BorderStroke(
                    1.dp,
                    if (isFilterExpanded) CyanPrimary else CardSlateBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Paket: $modTitle · $levelTitle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isFilterExpanded) "Tutup" else "Ubah",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                        Icon(
                            imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isFilterExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val moduleFilters = listOf(
                            "SEMUA" to "Semua (${allQuizzes.size})",
                            "M01" to "M01: Fisika (${allQuizzes.count { it.moduleId == "M01" || it.nodeCode.startsWith("M01") }})",
                            "M02" to "M02: T1/T2 (${allQuizzes.count { it.moduleId == "M02" || it.nodeCode.startsWith("M02") }})",
                            "M03" to "M03: Sekuens (${allQuizzes.count { it.moduleId.startsWith("M03") || it.nodeCode.startsWith("M03") }})",
                            "M03A" to "M03A (${allQuizzes.count { it.moduleId == "M03A" || it.nodeCode.startsWith("M03A") }})",
                            "M03B" to "M03B (${allQuizzes.count { it.moduleId == "M03B" || it.nodeCode.startsWith("M03B") }})",
                            "M03C" to "M03C (${allQuizzes.count { it.moduleId == "M03C" || it.nodeCode.startsWith("M03C") }})",
                            "M04" to "M04: k-Space (${allQuizzes.count { it.moduleId.startsWith("M04") || it.nodeCode.startsWith("M04") }})",
                            "M04A" to "M04A (${allQuizzes.count { it.moduleId == "M04A" || it.nodeCode.startsWith("M04A") }})",
                            "M04B" to "M04B (${allQuizzes.count { it.moduleId == "M04B" || it.nodeCode.startsWith("M04B") }})",
                            "M04C" to "M04C (${allQuizzes.count { it.moduleId == "M04C" || it.nodeCode.startsWith("M04C") }})",
                            "M05" to "M05: Magnet (${allQuizzes.count { it.moduleId == "M05" || it.nodeCode.startsWith("M05") }})",
                            "M06" to "M06: Gradien/RF (${allQuizzes.count { it.moduleId.startsWith("M06") || it.nodeCode.startsWith("M06") }})",
                            "M06A" to "M06A (${allQuizzes.count { it.moduleId == "M06A" || it.nodeCode.startsWith("M06A") }})",
                            "M06B" to "M06B (${allQuizzes.count { it.moduleId == "M06B" || it.nodeCode.startsWith("M06B") }})",
                            "M07" to "M07: Safety (${allQuizzes.count { it.moduleId.startsWith("M07") || it.nodeCode.startsWith("M07") }})",
                            "M07A" to "M07A (${allQuizzes.count { it.moduleId == "M07A" || it.nodeCode.startsWith("M07A") }})",
                            "M07B" to "M07B (${allQuizzes.count { it.moduleId == "M07B" || it.nodeCode.startsWith("M07B") }})",
                            "M07C" to "M07C (${allQuizzes.count { it.moduleId == "M07C" || it.nodeCode.startsWith("M07C") }})",
                            "M08" to "M08: Artefak/QA (${allQuizzes.count { it.moduleId.startsWith("M08") || it.nodeCode.startsWith("M08") }})",
                            "M08A" to "M08A (${allQuizzes.count { it.moduleId == "M08A" || it.nodeCode.startsWith("M08A") }})",
                            "M08B" to "M08B (${allQuizzes.count { it.moduleId == "M08B" || it.nodeCode.startsWith("M08B") }})",
                            "M08C" to "M08C (${allQuizzes.count { it.moduleId == "M08C" || it.nodeCode.startsWith("M08C") }})",
                            "M09" to "M09: Konsol (${allQuizzes.count { it.moduleId == "M09" || it.nodeCode.startsWith("M09") }})"
                        )
                        moduleFilters.forEach { (modId, label) ->
                            val isSelected = state.quizModuleFilter == modId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setQuizModuleFilter(modId) },
                                modifier = Modifier.testTag("quiz_filter_$modId"),
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = DeepObsidian,
                                    containerColor = CardSlate,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyanBright else CardSlateBorder
                                )
                            )
                        }
                    }

                    val moduleScopedQuizzes = if (state.quizModuleFilter == "SEMUA") {
                        allQuizzes
                    } else {
                        val filter = state.quizModuleFilter
                        allQuizzes.filter { q ->
                            q.moduleId == filter ||
                                (filter.length <= 3 && q.moduleId.startsWith(filter)) ||
                                (filter.length <= 3 && q.nodeCode.startsWith(filter)) ||
                                q.nodeCode.startsWith(filter)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val levelFilters = listOf(
                            0 to "Semua Tingkat (${moduleScopedQuizzes.size})",
                            1 to "T1 · Dasar (${moduleScopedQuizzes.count { it.level == 1 }})",
                            2 to "T2 · Menengah (${moduleScopedQuizzes.count { it.level == 2 }})",
                            3 to "T3 · Lanjut (${moduleScopedQuizzes.count { it.level == 3 }})"
                        )
                        levelFilters.forEach { (lvl, label) ->
                            val isSelected = state.quizLevelFilter == lvl
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setQuizLevelFilter(lvl) },
                                modifier = Modifier.testTag("quiz_level_filter_$lvl"),
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (lvl) {
                                        1 -> EmeraldSuccess
                                        2 -> CyanPrimary
                                        3 -> AmberWarning
                                        else -> CyanPrimary
                                    },
                                    selectedLabelColor = DeepObsidian,
                                    containerColor = CardSlate,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyanBright else CardSlateBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. AREA SOAL NYAMAN DIGULIR (Bebas halangan)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(quizScrollState)
                .padding(horizontal = 16.dp)
        ) {
            if (quizzes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                    border = BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tidak ada soal pada kombinasi modul dan tingkat ini.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { viewModel.setQuizLevelFilter(0) }) {
                            Text("Tampilkan Semua Tingkat", fontSize = 11.sp, color = CyanBright)
                        }
                    }
                }
            } else if (state.isQuizFinished) {
                QuizFinishedView(
                    score = state.quizScore,
                    total = quizzes.size.coerceAtLeast(1),
                    onRestart = { viewModel.restartQuiz() }
                )
            } else if (currentQuiz != null) {
                // Progress Bar Soal
                val progress = (state.currentQuizIndex + 1).toFloat() / quizzes.size.coerceAtLeast(1).toFloat()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Soal ${state.currentQuizIndex + 1} dari ${quizzes.size}",
                        fontSize = 12.sp,
                        color = CyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Skor Sesi: ${state.quizScore}",
                        fontSize = 11.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanBright,
                    trackColor = DarkNavySlate
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                    border = BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Metadata Baris Atas Soal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanDark,
                                    border = BorderStroke(1.dp, CyanPrimary)
                                ) {
                                    Text(
                                        text = "${currentQuiz.moduleId} · ${currentQuiz.nodeCode}",
                                        fontSize = 10.sp,
                                        color = CyanBright,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (currentQuiz.level == 1) EmeraldDark else AmberDark,
                                    border = BorderStroke(
                                        1.dp,
                                        if (currentQuiz.level == 1) EmeraldSuccess else AmberWarning
                                    )
                                ) {
                                    Text(
                                        text = "T${currentQuiz.level} · ${currentQuiz.levelBadge}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentQuiz.level == 1) EmeraldSuccess else AmberWarning,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (state.quizModuleFilter != "SEMUA") {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.setQuizModuleFilter("SEMUA")
                                            isFilterExpanded = true
                                        }
                                        .testTag("quiz_reset_module_package"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = CardSlate,
                                    border = BorderStroke(1.dp, CardSlateBorder)
                                ) {
                                    Text(
                                        text = "Ganti Paket",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyanBright,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQuiz.question,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 23.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Opsi Jawaban Luas & Nyaman Disentuh
                        currentQuiz.options.forEachIndexed { index, option ->
                            val isSelected = state.selectedOptionIndex == index
                            val isSubmitted = state.isAnswerSubmitted
                            val isCorrectOption = index == currentQuiz.correctIndex

                            val containerColor = when {
                                isSubmitted && isCorrectOption -> EmeraldDark
                                isSubmitted && isSelected && !isCorrectOption -> RoseDark
                                isSelected -> CyanDark
                                else -> CardSlate
                            }

                            val borderColor = when {
                                isSubmitted && isCorrectOption -> EmeraldSuccess
                                isSubmitted && isSelected && !isCorrectOption -> RoseDanger
                                isSelected -> CyanPrimary
                                else -> CardSlateBorder
                            }

                            val textColor = when {
                                isSubmitted && (isCorrectOption || isSelected) -> TextPrimary
                                isSelected -> CyanBright
                                else -> TextSecondary
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable(enabled = !isSubmitted) {
                                        viewModel.selectQuizOption(index)
                                    }
                                    .testTag("quiz_option_$index"),
                                shape = RoundedCornerShape(14.dp),
                                color = containerColor,
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) CyanPrimary else DeepObsidian)
                                                .border(1.dp, borderColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ('A' + index).toString(),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) DeepObsidian else TextMuted
                                            )
                                        }
                                        Text(
                                            text = option,
                                            fontSize = 13.5.sp,
                                            color = textColor,
                                            lineHeight = 19.sp
                                        )
                                    }

                                    if (isSubmitted) {
                                        if (isCorrectOption) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Benar",
                                                tint = EmeraldSuccess,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Salah",
                                                tint = RoseDanger,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Petunjuk Ringkas Pra-Jawab (Opsional agar layar tetap bersih) atau Pembahasan Pasca-Jawab
                        val specialistProfile = CharacterCatalog.getSpecialistProfile(state.characterCustomization.avatarId)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (!state.isAnswerSubmitted) {
                            Surface(
                                onClick = { showPreSubmitHint = !showPreSubmitHint },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = DeepObsidian.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, CardSlateBorder)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = AmberWarning,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (showPreSubmitHint) "Sembunyikan Petunjuk Nano" else "Butuh petunjuk Nano? Ketuk di sini",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyanBright
                                        )
                                    }
                                    AnimatedVisibility(visible = showPreSubmitHint) {
                                        val tipText = specialistProfile.greetings.getOrElse(
                                            state.currentQuizIndex % specialistProfile.greetings.size
                                        ) { specialistProfile.greetings.first() }
                                        Text(
                                            text = tipText,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            val isAnswerCorrect = state.selectedOptionIndex == currentQuiz.correctIndex
                            val quizExpression = when {
                                isAnswerCorrect && state.currentQuizIndex % 2 == 0 -> Expression.BANGGA
                                isAnswerCorrect -> Expression.SENANG
                                currentQuiz.moduleId.startsWith("M07", ignoreCase = true) -> Expression.WASPADA
                                else -> Expression.SEDIH
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = DeepObsidian,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isAnswerCorrect) EmeraldSuccess.copy(alpha = 0.55f) else RoseDanger.copy(alpha = 0.55f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AvatarView(
                                        role = state.characterCustomization.avatarId,
                                        equipped = state.characterCustomization,
                                        expression = quizExpression,
                                        modifier = Modifier.size(58.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAnswerCorrect) "✓ Jawaban Tepat!" else "✗ Kurang Tepat",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAnswerCorrect) EmeraldSuccess else RoseDanger
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentQuiz.explanation,
                                            fontSize = 11.5.sp,
                                            color = TextSecondary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. DOCK TOMBOL AKSI BAWAH PINNED: Selalu mudah dijangkau ibu jari!
        if (!state.isQuizFinished && currentQuiz != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkNavySlate,
                border = BorderStroke(1.dp, CardSlateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.prevQuizQuestion() },
                        enabled = state.currentQuizIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardSlateBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Soal Sebelumnya",
                            tint = if (state.currentQuizIndex > 0) TextPrimary else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!state.isAnswerSubmitted) {
                        Button(
                            onClick = { viewModel.submitQuizAnswer() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("submit_quiz_button"),
                            enabled = state.selectedOptionIndex != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Periksa Jawaban", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("next_quiz_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Soal Berikutnya", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = DeepObsidian,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.restartQuiz() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardSlateBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ulangi Kuis",
                            tint = CyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStatsDialog) {
        Dialog(onDismissRequest = { showStatsDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Statistik & Riwayat Kuis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                        IconButton(
                            onClick = { showStatsDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup statistik",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (totalAttempts == 0) {
                        Text(
                            text = "Belum ada riwayat jawaban untuk filter modul ini. Jawab minimal 1 soal untuk melihat statistik.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    } else {
                        Text(
                            text = "Total Percobaan: $totalAttempts jawaban | Benar: $correctAttempts ($accuracyPct%)",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (stillWrongAttempts.isNotEmpty()) {
                            Text(
                                text = "Soal yang masih salah (${stillWrongAttempts.size}):",
                                fontSize = 11.sp,
                                color = RoseDanger,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            stillWrongAttempts.take(5).forEach { att ->
                                val q = allQuizzes.find { it.id == att.quizId }
                                if (q != null) {
                                    Text(
                                        text = "• [${q.moduleId}] ${q.question}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        } else {
                            Text(
                                text = "Semua soal yang pernah dicoba pada cakupan ini sudah terjawab benar!",
                                fontSize = 11.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizFinishedView(
    score: Int,
    total: Int,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CyanDark)
                    .border(2.dp, CyanPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Evaluasi Selesai!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Skor Anda: $score dari $total Benar",
                fontSize = 14.sp,
                color = CyanBright,
                fontWeight = FontWeight.SemiBold
            )

            val percentage = if (total > 0) (score.toFloat() / total.toFloat()) * 100f else 0f
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DeepObsidian,
                border = BorderStroke(1.dp, CardSlateBorder)
            ) {
                Text(
                    text = "Akurasi: ${percentage.toInt()}%",
                    fontSize = 13.sp,
                    color = if (percentage >= 75f) EmeraldSuccess else AmberWarning,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = DeepObsidian,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ulangi Latihan Kuis",
                    color = DeepObsidian,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
