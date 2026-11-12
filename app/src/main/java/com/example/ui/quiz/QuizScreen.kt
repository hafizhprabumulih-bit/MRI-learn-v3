package com.example.ui.quiz

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharacterCatalog
import com.example.ui.character.AvatarView
import com.example.ui.character.Expression
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. PINNED TOP HEADER: Tetap di atas, tidak bergerak saat di-swipe!
        Text(
            text = "Kuis Evaluasi Mandiri",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // 2. PINNED FILTER MODUL KUIS: Tetap di atas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val moduleFilters = listOf(
                "SEMUA" to "Semua (${allQuizzes.size})",
                "M01" to "Modul 1: Fisika Dasar (${allQuizzes.count { it.moduleId == "M01" || it.nodeCode.startsWith("M01") }})",
                "M02" to "Modul 2: Relaksasi T1/T2 (${allQuizzes.count { it.moduleId == "M02" || it.nodeCode.startsWith("M02") }})",
                "M03" to "Modul 3: Sekuens Pulsa (${allQuizzes.count { it.moduleId.startsWith("M03") || it.nodeCode.startsWith("M03") }})",
                "M03A" to "Modul 3A: SE, IR & TSE (${allQuizzes.count { it.moduleId == "M03A" || it.nodeCode.startsWith("M03A") }})",
                "M03B" to "Modul 3B: GRE & Keadaan Tunak (${allQuizzes.count { it.moduleId == "M03B" || it.nodeCode.startsWith("M03B") }})",
                "M03C" to "Modul 3C: DWI, BOLD & Kontras (${allQuizzes.count { it.moduleId == "M03C" || it.nodeCode.startsWith("M03C") }})",
                "M04" to "Modul 4: Pengodean & Ruang-k (${allQuizzes.count { it.moduleId.startsWith("M04") || it.nodeCode.startsWith("M04") }})",
                "M04A" to "Modul 4A: Pengodean Spasial (${allQuizzes.count { it.moduleId == "M04A" || it.nodeCode.startsWith("M04A") }})",
                "M04B" to "Modul 4B: Ruang-k & Fourier (${allQuizzes.count { it.moduleId == "M04B" || it.nodeCode.startsWith("M04B") }})",
                "M04C" to "Modul 4C: Ruang-k Lanjut (${allQuizzes.count { it.moduleId == "M04C" || it.nodeCode.startsWith("M04C") }})",
                "M05" to "Modul 5: Pesawat & Magnet (${allQuizzes.count { it.moduleId == "M05" || it.nodeCode.startsWith("M05") }})",
                "M06" to "Modul 6: Gradien & RF (${allQuizzes.count { it.moduleId.startsWith("M06") || it.nodeCode.startsWith("M06") }})",
                "M06A" to "Modul 6A: Sistem Gradien (${allQuizzes.count { it.moduleId == "M06A" || it.nodeCode.startsWith("M06A") }})",
                "M06B" to "Modul 6B: Sistem RF & Koil (${allQuizzes.count { it.moduleId == "M06B" || it.nodeCode.startsWith("M06B") }})",
                "M07" to "Modul 7: Keselamatan MR (${allQuizzes.count { it.moduleId.startsWith("M07") || it.nodeCode.startsWith("M07") }})",
                "M07A" to "Modul 7A: Zona & Skrining (${allQuizzes.count { it.moduleId == "M07A" || it.nodeCode.startsWith("M07A") }})",
                "M07B" to "Modul 7B: B0 & Kriogen (${allQuizzes.count { it.moduleId == "M07B" || it.nodeCode.startsWith("M07B") }})",
                "M07C" to "Modul 7C: SAR & Bising (${allQuizzes.count { it.moduleId == "M07C" || it.nodeCode.startsWith("M07C") }})",
                "M08" to "Modul 8: Artefak & QA (${allQuizzes.count { it.moduleId.startsWith("M08") || it.nodeCode.startsWith("M08") }})",
                "M08A" to "Modul 8A: Artefak Pengodean (${allQuizzes.count { it.moduleId == "M08A" || it.nodeCode.startsWith("M08A") }})",
                "M08B" to "Modul 8B: Aliran & Kompensasi (${allQuizzes.count { it.moduleId == "M08B" || it.nodeCode.startsWith("M08B") }})",
                "M08C" to "Modul 8C: Hardware & QA (${allQuizzes.count { it.moduleId == "M08C" || it.nodeCode.startsWith("M08C") }})",
                "M09" to "Modul 9: Optimasi & Konsol (${allQuizzes.count { it.moduleId == "M09" || it.nodeCode.startsWith("M09") }})"
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
        }

        // 3. PINNED FILTER TINGKAT KESULITAN: Tetap di atas
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
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val levelFilters = listOf(
                0 to "Semua Tingkat (${moduleScopedQuizzes.size})",
                1 to "Tingkat 1 · Dasar (${moduleScopedQuizzes.count { it.level == 1 }})",
                2 to "Tingkat 2 · Menengah (${moduleScopedQuizzes.count { it.level == 2 }})",
                3 to "Tingkat 3 · Lanjut (${moduleScopedQuizzes.count { it.level == 3 }})"
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
        }

        // 4. SCROLLABLE QUIZ CONTENT: Hanya kartu soal / statistik yang bergerak saat di-swipe!
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

        // Ringkasan Riwayat & Statistik Kuis - Di-scope ke filter modul aktif & status terbaru per soal (K-10)
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
        if (totalAttempts > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Statistik & Riwayat Kuis (Room DB)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanBright)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Total Percobaan: $totalAttempts jawaban | Benar: $correctAttempts", fontSize = 11.sp, color = TextSecondary)
                    if (stillWrongAttempts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Soal yang masih salah (${stillWrongAttempts.size}):", fontSize = 11.sp, color = RoseDanger, fontWeight = FontWeight.SemiBold)
                        stillWrongAttempts.take(3).forEach { att ->
                            val q = allQuizzes.find { it.id == att.quizId }
                            if (q != null) {
                                Text(text = "• [${q.moduleId}] ${q.question}", fontSize = 10.sp, color = TextMuted, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        if (quizzes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
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
            // Screen Hasil Kuis
            QuizFinishedView(
                score = state.quizScore,
                total = quizzes.size.coerceAtLeast(1),
                onRestart = { viewModel.restartQuiz() }
            )
        } else {
            val currentQuiz = quizzes.getOrNull(state.currentQuizIndex)
            if (currentQuiz != null) {
                // Progress Bar Soal
                val progress = (state.currentQuizIndex + 1).toFloat() / quizzes.size.coerceAtLeast(1).toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanBright,
                    trackColor = DarkNavySlate
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Soal ${state.currentQuizIndex + 1} / ${quizzes.size}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (currentQuiz.level == 1) EmeraldDark else AmberDark,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (currentQuiz.level == 1) EmeraldSuccess else AmberWarning
                                    )
                                ) {
                                    Text(
                                        text = "T${currentQuiz.level} · ${currentQuiz.levelBadge}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentQuiz.level == 1) EmeraldSuccess else AmberWarning,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (state.quizModuleFilter != "SEMUA") {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.setQuizModuleFilter("SEMUA") }
                                            .testTag("quiz_reset_module_package"),
                                        shape = RoundedCornerShape(6.dp),
                                        color = CardSlate,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                                    ) {
                                        Text(
                                            text = "Ganti Paket Modul",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyanBright,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                                ) {
                                    Text(
                                        text = "${currentQuiz.moduleId} · ${currentQuiz.nodeCode}",
                                        fontSize = 10.sp,
                                        color = CyanBright,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = currentQuiz.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Opsi Jawaban
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
                                isSubmitted && (isCorrectOption || (isSelected && isCorrectOption)) -> TextPrimary
                                isSubmitted && isSelected && !isCorrectOption -> TextPrimary
                                isSelected -> CyanBright
                                else -> TextSecondary
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !isSubmitted) {
                                        viewModel.selectQuizOption(index)
                                    }
                                    .testTag("quiz_option_$index"),
                                shape = RoundedCornerShape(12.dp),
                                color = containerColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) CyanPrimary else DeepObsidian)
                                                .border(1.dp, borderColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ('A' + index).toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) DeepObsidian else TextMuted
                                            )
                                        }
                                        Text(
                                            text = option,
                                            fontSize = 13.sp,
                                            color = textColor,
                                            lineHeight = 18.sp
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

                        // Reaksi Maskot Nano:
                        // - Sebelum dijawab: BERPIKIR + Tip dari peran spesialis aktif
                        // - Sesudah dijawab: BANGGA/SENANG (Benar) atau WASPADA/SEDIH (Salah)
                        val specialistProfile = CharacterCatalog.getSpecialistProfile(state.characterCustomization.avatarId)
                        if (!state.isAnswerSubmitted) {
                            val tipText = specialistProfile.greetings.getOrElse(
                                state.currentQuizIndex % specialistProfile.greetings.size
                            ) { specialistProfile.greetings.first() }
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AvatarView(
                                        role = state.characterCustomization.avatarId,
                                        equipped = state.characterCustomization,
                                        expression = Expression.BERPIKIR,
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Nano (${specialistProfile.roleTitle}) sedang berpikir…",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanBright
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = tipText,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            val isAnswerCorrect = state.selectedOptionIndex == currentQuiz.correctIndex
                            val quizExpression = when {
                                isAnswerCorrect && state.currentQuizIndex % 2 == 0 -> Expression.BANGGA
                                isAnswerCorrect -> Expression.SENANG
                                currentQuiz.moduleId.equals("M09", ignoreCase = true) -> Expression.WASPADA
                                else -> Expression.SEDIH
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isAnswerCorrect) EmeraldSuccess.copy(alpha = 0.5f) else RoseDanger.copy(alpha = 0.5f)
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
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAnswerCorrect) "✓ Jawaban Tepat! 🎉" else "✗ Kurang Tepat ❌",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAnswerCorrect) EmeraldSuccess else RoseDanger
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentQuiz.explanation,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Tombol Aksi Kuis
                        if (!state.isAnswerSubmitted) {
                            Button(
                                onClick = { viewModel.submitQuizAnswer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_quiz_button"),
                                enabled = state.selectedOptionIndex != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                            ) {
                                Text("Jawab", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.nextQuizQuestion() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("next_quiz_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                            ) {
                                Text("Soal Berikutnya →", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Baris Navigasi Soal (← Sebelumnya | ↻ Ulangi Kuis)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.prevQuizQuestion() },
                                enabled = state.currentQuizIndex > 0,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                            ) {
                                Text("← Sebelumnya", fontSize = 11.sp, color = TextSecondary)
                            }
                            OutlinedButton(
                                onClick = { viewModel.restartQuiz() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                            ) {
                                Text("↻ Ulangi Kuis", fontSize = 11.sp, color = CyanBright)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
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
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
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

            val percentage = (score.toFloat() / total.toFloat()) * 100f
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
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
