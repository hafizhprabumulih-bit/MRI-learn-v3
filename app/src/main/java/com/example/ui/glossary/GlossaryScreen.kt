package com.example.ui.glossary

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GlossaryItem
import com.example.data.MriRepository
import com.example.ui.components.AnimatedBookmarkPill
import com.example.ui.components.FlashcardDeckSection
import com.example.ui.components.StudyModeToggleBar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel

@Composable
fun GlossaryScreen(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTermForDetail by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItemForDetail = remember(selectedTermForDetail) {
        selectedTermForDetail?.let { term -> MriRepository.glossary.find { it.term == term } }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. PINNED TOP HEADER & SEARCH: Tetap di atas, tidak bergerak saat di-swipe!
        Text(
            text = "Glosarium Medis & Fisika MRI",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Study Mode Toggle Bar: Daftar Kamus vs Flashcards Hafalan
        StudyModeToggleBar(
            primaryLabel = "Daftar Kamus (${MriRepository.glossary.size})",
            flashcardLabel = "Flashcards Hafalan",
            isFlashcardMode = state.isGlossaryFlashcardMode,
            onSelectMode = { viewModel.setGlossaryFlashcardMode(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = state.glossarySearchQuery,
            onValueChange = { viewModel.setGlossarySearch(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("glossary_search_input"),
            placeholder = { Text("Cari terminologi klinis (Larmor, TR, TE, FID, SAR, DWI, ADC, FLAIR)...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (state.glossarySearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setGlossarySearch("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkNavySlate,
                unfocusedContainerColor = DarkNavySlate,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = CardSlateBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CyanPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. SCROLLABLE CONTENT: Hanya daftar kartu kamus / flashcards yang bergerak saat di-swipe!
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

        // Bookmark + Module Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = state.onlyBookmarkedGlossary,
                onClick = { viewModel.setOnlyBookmarkedGlossary(!state.onlyBookmarkedGlossary) },
                modifier = Modifier.testTag("glossary_bookmark_filter_chip"),
                label = {
                    Text(
                        text = "★ Tersimpan (${state.bookmarkedGlossaryTerms.size})",
                        fontSize = 11.sp,
                        fontWeight = if (state.onlyBookmarkedGlossary) FontWeight.Bold else FontWeight.Normal
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
                    selected = state.onlyBookmarkedGlossary,
                    borderColor = if (state.onlyBookmarkedGlossary) AmberAccent else CardSlateBorder
                )
            )

            val filters = listOf(
                "SEMUA", "M01", "M02", "M03", "M03A", "M03B", "M03C",
                "M04", "M04A", "M04B", "M04C", "M05", "M06", "M06A", "M06B",
                "M07", "M07A", "M07B", "M07C",
                "M08", "M08A", "M08B", "M08C", "M09"
            )
            filters.forEach { mod ->
                val isSel = state.glossaryModuleFilter == mod
                FilterChip(
                    selected = isSel,
                    onClick = { viewModel.setGlossaryModuleFilter(mod) },
                    modifier = Modifier.testTag("glossary_filter_chip_$mod"),
                    label = { Text(if (mod == "SEMUA") "Semua" else mod, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSel,
                        borderColor = if (isSel) CyanBright else CardSlateBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val query = state.glossarySearchQuery.trim().lowercase()

        if (state.isGlossaryFlashcardMode) {
            val filteredCards = MriRepository.flashcards.filter { card ->
                val matchesQuery = query.isEmpty() ||
                    card.frontTitle.lowercase().contains(query) ||
                    card.frontSubtitle.lowercase().contains(query) ||
                    card.backContent.lowercase().contains(query)
                val filter = state.glossaryModuleFilter
                val matchesMod = filter == "SEMUA" ||
                    card.moduleId == filter ||
                    (filter.length <= 3 && card.moduleId.startsWith(filter)) ||
                    (filter.length <= 3 && card.nodeCode.startsWith(filter)) ||
                    card.nodeCode.startsWith(filter)
                val isBookmarked = state.bookmarkedGlossaryTerms.contains(card.frontTitle) ||
                    state.bookmarkedNodeCodes.contains(card.nodeCode)
                val matchesBookmark = !state.onlyBookmarkedGlossary || isBookmarked
                matchesQuery && matchesMod && matchesBookmark
            }

            FlashcardDeckSection(
                cards = filteredCards,
                currentIndex = state.glossaryFlashcardIndex,
                isFlipped = state.isGlossaryFlashcardFlipped,
                shuffleSeed = state.glossaryFlashcardShuffleSeed,
                masteredCardIds = state.masteredFlashcardIds,
                isCardBookmarked = { card ->
                    state.bookmarkedGlossaryTerms.contains(card.frontTitle) ||
                        state.bookmarkedNodeCodes.contains(card.nodeCode)
                },
                onToggleBookmark = { card ->
                    if (card.id.startsWith("fc_gloss_")) {
                        viewModel.toggleGlossaryBookmark(card.frontTitle)
                    } else {
                        viewModel.toggleNodeBookmark(card.nodeCode)
                    }
                },
                onFlip = { viewModel.flipFlashcard() },
                onPrev = { size -> viewModel.prevFlashcard(size) },
                onNext = { size -> viewModel.nextFlashcard(size) },
                onShuffle = { viewModel.shuffleFlashcards() },
                onToggleMastered = { id -> viewModel.toggleFlashcardMastered(id) },
                emptyMessage = if (state.onlyBookmarkedGlossary) {
                    "Belum ada kartu atau istilah tersimpan (Bookmark) untuk filter ini."
                } else {
                    "Tidak ada kartu hafalan yang cocok dengan kata kunci '$query'."
                }
            )
        } else {
            val filteredList = MriRepository.glossary.filter { item ->
                val matchesQuery = query.isEmpty() ||
                    item.term.lowercase().contains(query) ||
                    item.symbolOrAbbr.lowercase().contains(query) ||
                    item.definition.lowercase().contains(query)
                val filter = state.glossaryModuleFilter
                val matchesMod = filter == "SEMUA" ||
                    item.moduleId == filter ||
                    (filter.length <= 3 && item.moduleId.startsWith(filter)) ||
                    (filter.length <= 3 && item.nodeCode.startsWith(filter)) ||
                    item.nodeCode.startsWith(filter)
                val matchesBookmark = !state.onlyBookmarkedGlossary || state.bookmarkedGlossaryTerms.contains(item.term)
                matchesQuery && matchesMod && matchesBookmark
            }

            if (filteredList.isEmpty()) {
                Text(
                    text = if (state.onlyBookmarkedGlossary) {
                        "Belum ada istilah glosarium yang disimpan (Bookmark)."
                    } else {
                        "Istilah tidak ditemukan untuk kata kunci '$query'"
                    },
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredList.forEach { item ->
                        val isBookmarked = state.bookmarkedGlossaryTerms.contains(item.term)
                        GlossaryCard(
                            item = item,
                            isBookmarked = isBookmarked,
                            onToggleBookmark = { viewModel.toggleGlossaryBookmark(item.term) },
                            onClick = { selectedTermForDetail = item.term }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(84.dp))
        }
        }

        // Dialog Rincian Glosarium
        selectedItemForDetail?.let { item ->
            val isBookmarked = state.bookmarkedGlossaryTerms.contains(item.term)
            GlossaryDetailDialog(
                item = item,
                isBookmarked = isBookmarked,
                onToggleBookmark = { viewModel.toggleGlossaryBookmark(item.term) },
                onDismiss = { selectedTermForDetail = null },
                onNavigate = {
                    val modulesSource = state.modulesList.ifEmpty { MriRepository.modules }
                    val mod = modulesSource.find { it.id == item.moduleId }
                        ?: modulesSource.find { it.id == item.nodeCode.substringBefore(".") }
                        ?: modulesSource.find { it.id.startsWith(item.moduleId) }
                        ?: modulesSource.find { item.moduleId.startsWith(it.id) }
                    if (mod != null) {
                        selectedTermForDetail = null
                        viewModel.openModuleDetail(mod)
                    }
                }
            )
        }
    }
}

@Composable
fun GlossaryCard(
    item: GlossaryItem,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onClick: () -> Unit
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (isBookmarked) AmberAccent.copy(alpha = 0.75f) else CardSlateBorder,
        animationSpec = tween(durationMillis = 240),
        label = "glossary_card_border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.term,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.symbolOrAbbr.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DeepObsidian,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                        ) {
                            Text(
                                text = item.symbolOrAbbr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                    ) {
                        Text(
                            text = item.nodeCode,
                            fontSize = 10.sp,
                            color = CyanBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    AnimatedBookmarkPill(
                        isBookmarked = isBookmarked,
                        onToggleBookmark = onToggleBookmark,
                        testTag = "bookmark_glossary_${item.term}",
                        savedContentDescription = "Hapus Bookmark Istilah",
                        unsavedContentDescription = "Simpan Bookmark Istilah"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.definition,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun GlossaryDetailDialog(
    item: GlossaryItem,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                        Text(text = "Rincian Istilah Medis", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onToggleBookmark, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Hapus Bookmark" else "Simpan Bookmark",
                                tint = if (isBookmarked) AmberAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = item.term, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyanBright)
                Text(text = "Simbol: ${item.symbolOrAbbr} • Modul Rujukan: ${item.nodeCode}", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Text(
                        text = item.definition,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text(text = "Buka Modul ${item.moduleId} (${item.nodeCode})", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
