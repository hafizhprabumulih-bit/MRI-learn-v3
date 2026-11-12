package com.example.ui.saved

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MriModule
import com.example.data.MriNode
import com.example.data.MriRepository
import com.example.ui.glossary.GlossaryCard
import com.example.ui.glossary.GlossaryDetailDialog
import com.example.ui.module.NodeCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainTab
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import com.example.viewmodel.SimType

@Composable
fun SavedScreen(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    // 0 = Semua, 1 = Materi Kurikulum, 2 = Istilah Glosarium
    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTermForDetail by rememberSaveable { mutableStateOf<String?>(null) }

    val modulesSource = state.modulesList.ifEmpty { MriRepository.modules }

    val bookmarkedNodesWithModule: List<Pair<MriModule, MriNode>> = remember(
        modulesSource,
        state.bookmarkedNodeCodes
    ) {
        buildList {
            modulesSource.forEach { mod ->
                mod.nodes.forEach { node ->
                    if (state.bookmarkedNodeCodes.contains(node.code)) {
                        add(mod to node)
                    }
                }
            }
        }
    }

    val bookmarkedGlossaryItems = remember(state.bookmarkedGlossaryTerms) {
        MriRepository.glossary.filter { state.bookmarkedGlossaryTerms.contains(it.term) }
    }

    val query = searchQuery.trim().lowercase()

    val filteredNodes = remember(bookmarkedNodesWithModule, query) {
        if (query.isEmpty()) {
            bookmarkedNodesWithModule
        } else {
            bookmarkedNodesWithModule.filter { (mod, node) ->
                node.code.lowercase().contains(query) ||
                    node.title.lowercase().contains(query) ||
                    node.summaryShort.lowercase().contains(query) ||
                    node.detailFull.lowercase().contains(query) ||
                    node.analogi.lowercase().contains(query) ||
                    mod.title.lowercase().contains(query)
            }
        }
    }

    val filteredGlossary = remember(bookmarkedGlossaryItems, query) {
        if (query.isEmpty()) {
            bookmarkedGlossaryItems
        } else {
            bookmarkedGlossaryItems.filter { item ->
                item.term.lowercase().contains(query) ||
                    item.symbolOrAbbr.lowercase().contains(query) ||
                    item.definition.lowercase().contains(query) ||
                    item.nodeCode.lowercase().contains(query)
            }
        }
    }

    val selectedItemForDetail = remember(selectedTermForDetail) {
        selectedTermForDetail?.let { term -> MriRepository.glossary.find { it.term == term } }
    }

    val totalSavedCount = bookmarkedNodesWithModule.size + bookmarkedGlossaryItems.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp)
            .testTag("saved_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Materi & Glosarium Tersimpan",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${bookmarkedNodesWithModule.size} Kartu Materi • ${bookmarkedGlossaryItems.size} Istilah Glosarium",
                    fontSize = 11.sp,
                    color = AmberAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (totalSavedCount > 0) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.clearAllBookmarks() }
                            .testTag("saved_clear_all_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = RoseDark,
                        border = BorderStroke(1.dp, RoseDanger)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus Semua Bookmark",
                                tint = RoseDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Hapus Semua",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseDanger
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberDark,
                    border = BorderStroke(1.dp, AmberAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$totalSavedCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("saved_search_input"),
            placeholder = {
                Text(
                    text = "Cari materi atau istilah glosarium yang disimpan...",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus Pencarian",
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

        // Filter Chips: Semua vs Materi vs Glosarium
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(0, "Semua ($totalSavedCount)", "saved_filter_all"),
                Triple(1, "Kartu Materi (${bookmarkedNodesWithModule.size})", "saved_filter_nodes"),
                Triple(2, "Glosarium (${bookmarkedGlossaryItems.size})", "saved_filter_glossary")
            ).forEach { (idx, label, tag) ->
                val isSelected = selectedCategory == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = idx },
                    modifier = Modifier.testTag(tag),
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberAccent,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AmberAccent else CardSlateBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            val showNodes = selectedCategory == 0 || selectedCategory == 1
            val showGlossary = selectedCategory == 0 || selectedCategory == 2

            val visibleNodeCount = if (showNodes) filteredNodes.size else 0
            val visibleGlossaryCount = if (showGlossary) filteredGlossary.size else 0

            if (visibleNodeCount == 0 && visibleGlossaryCount == 0) {
                SavedEmptyStateCard(
                    hasQuery = query.isNotEmpty(),
                    query = searchQuery,
                    onExploreModules = { viewModel.selectTab(MainTab.BERANDA) },
                    onExploreGlossary = { viewModel.selectTab(MainTab.GLOSARIUM) }
                )
            } else {
                if (showNodes && filteredNodes.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kartu Materi Tersimpan (${filteredNodes.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        filteredNodes.forEach { (mod, node) ->
                            val isCompleted = state.completedNodeCodes.contains(node.code)
                            val isBookmarked = state.bookmarkedNodeCodes.contains(node.code)
                            val activeMode = state.nodeModeMap[node.code] ?: state.globalNodeMode

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Baris Info Modul Induk, Tombol Hapus Bookmark, & Pintasan Buka Modul
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${mod.code} • ${mod.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.removeNodeBookmark(node.code) }
                                                .testTag("saved_remove_node_${node.code}"),
                                            shape = RoundedCornerShape(8.dp),
                                            color = RoseDark,
                                            border = BorderStroke(1.dp, RoseDanger)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Hapus dari Tersimpan",
                                                    tint = RoseDanger,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Hapus",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoseDanger
                                                )
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.openModuleDetail(mod) }
                                                .testTag("saved_open_module_${node.code}"),
                                            shape = RoundedCornerShape(8.dp),
                                            color = CyanDark,
                                            border = BorderStroke(1.dp, CyanPrimary)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Buka ${mod.code}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyanBright,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = CyanBright,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                NodeCard(
                                    node = node,
                                    isCompleted = isCompleted,
                                    isBookmarked = isBookmarked,
                                    activeMode = activeMode,
                                    onModeSelected = { mode -> viewModel.setNodeMode(node.code, mode) },
                                    onToggleCompleted = { viewModel.toggleNodeCompletion(node.code) },
                                    onMarkNodeCompleted = { code -> viewModel.markNodeCompleted(code.ifBlank { node.code }) },
                                    onToggleBookmark = { viewModel.removeNodeBookmark(node.code) },
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

                    Spacer(modifier = Modifier.height(18.dp))
                }

                if (showGlossary && filteredGlossary.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Istilah Glosarium Tersimpan (${filteredGlossary.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filteredGlossary.forEach { item ->
                            val isBookmarked = state.bookmarkedGlossaryTerms.contains(item.term)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.removeGlossaryBookmark(item.term) }
                                            .testTag("saved_remove_glossary_${item.term}"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = RoseDark,
                                        border = BorderStroke(1.dp, RoseDanger)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Hapus Istilah dari Tersimpan",
                                                tint = RoseDanger,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Hapus dari Tersimpan",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoseDanger
                                            )
                                        }
                                    }
                                }

                                GlossaryCard(
                                    item = item,
                                    isBookmarked = isBookmarked,
                                    onToggleBookmark = { viewModel.removeGlossaryBookmark(item.term) },
                                    onClick = { selectedTermForDetail = item.term }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
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
private fun SavedEmptyStateCard(
    hasQuery: Boolean,
    query: String,
    onExploreModules: () -> Unit,
    onExploreGlossary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .testTag("saved_empty_state_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AmberDark)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (hasQuery) {
                    "Tidak Ditemukan Item Tersimpan"
                } else {
                    "Belum Ada Materi atau Glosarium Tersimpan"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasQuery) {
                    "Tidak ada kartu materi atau istilah glosarium tersimpan yang cocok dengan kata kunci '$query'."
                } else {
                    "Ketuk tombol Simpan (Bookmark) pada kartu materi di dalam modul atau pada entri glosarium agar mudah diakses kembali dari tab ini."
                },
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            if (!hasQuery) {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExploreModules,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text(
                            text = "Jelajahi Modul",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepObsidian
                        )
                    }

                    OutlinedButton(
                        onClick = onExploreGlossary,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyanPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSlate,
                            contentColor = CyanBright
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Glosarium",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
