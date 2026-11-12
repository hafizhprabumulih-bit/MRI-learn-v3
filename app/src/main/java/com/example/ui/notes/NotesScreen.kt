package com.example.ui.notes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.StudyNote
import com.example.ui.character.AvatarView
import com.example.ui.character.Expression
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import java.io.File
import java.time.LocalDate

@Composable
fun NotesScreen(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories = listOf("SEMUA", "Protokol Klinis", "Fisika Sekuens", "Mitigasi Artefak", "Keselamatan MR", "Catatan Bebas")
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var exportStatusMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val createJsonDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val jsonPayload = viewModel.exportNotesToJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                }
                exportStatusMessage = "Catatan berhasil diunduh ke file JSON pilihan Anda."
            }.onFailure {
                exportStatusMessage = "Cadangan JSON telah disimpan di penyimpanan aplikasi."
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DeepObsidian)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. PINNED TOP HEADER: Judul + Tombol Export Catatan (JSON)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Catatan Belajar MRI",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.notesList.size} catatan tersimpan • Aman & portabel",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        exportStatusMessage = null
                        showExportDialog = true
                    },
                    modifier = Modifier.testTag("export_notes_button"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkNavySlate,
                        contentColor = CyanBright
                    ),
                    border = BorderStroke(1.dp, CyanPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Catatan ke JSON",
                        tint = CyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export Catatan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (exportStatusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_status_banner"),
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldDark,
                    border = BorderStroke(1.dp, EmeraldSuccess)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = exportStatusMessage.orEmpty(),
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. PINNED FILTER KATEGORI: Tetap di atas agar mudah berganti kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = state.selectedNoteCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setNoteCategory(cat) },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // 3. SCROLLABLE NOTES CONTENT: Hanya daftar catatan ini yang bergerak saat di-swipe!
            // B-19: Cocokkan kategori secara akurat (serta dukung alias lama "Fisika" -> "Fisika Sekuens", dsb.)
            val filteredNotes = if (state.selectedNoteCategory == "SEMUA") {
                state.notesList
            } else {
                val targetCat = state.selectedNoteCategory.trim()
                state.notesList.filter { note ->
                    val noteCat = note.category.trim()
                    noteCat.equals(targetCat, ignoreCase = true) ||
                        (targetCat.equals("Fisika Sekuens", ignoreCase = true) && noteCat.equals("Fisika", ignoreCase = true)) ||
                        (targetCat.equals("Protokol Klinis", ignoreCase = true) && noteCat.equals("Protokol", ignoreCase = true)) ||
                        (targetCat.equals("Mitigasi Artefak", ignoreCase = true) && noteCat.equals("Artefak", ignoreCase = true)) ||
                        (targetCat.equals("Keselamatan MR", ignoreCase = true) && noteCat.equals("Keselamatan", ignoreCase = true))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (filteredNotes.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = DarkNavySlate,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarView(
                                role = state.characterCustomization.avatarId,
                                equipped = state.characterCustomization,
                                expression = Expression.BERPIKIR,
                                modifier = Modifier.size(84.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum ada catatan pada kategori ini",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Klik tombol 'Tulis' di kanan bawah untuk membuat catatan baru.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filteredNotes.forEach { note ->
                            NoteCard(
                                note = note,
                                onEdit = { viewModel.openNoteDialog(note) },
                                onDelete = { viewModel.deleteNote(note.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // Tombol Floating "+ Tulis Catatan" di atas footer/bottom bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { viewModel.openNoteDialog(null) }
                .testTag("add_note_fab"),
            shape = RoundedCornerShape(24.dp),
            color = CyanPrimary,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tulis Catatan",
                    tint = DeepObsidian,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Tulis Catatan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepObsidian
                )
            }
        }

        // Dialog Tempat Catatan Kosong (Form Tulis / Edit Catatan)
        if (state.showNoteDialog) {
            NoteEditorDialog(
                initialNote = state.editingNote,
                onDismiss = { viewModel.closeNoteDialog() },
                onSave = { id, title, content, cat ->
                    viewModel.saveNote(id, title, content, cat)
                }
            )
        }

        // Dialog Export Catatan ke Format JSON
        if (showExportDialog) {
            val jsonContent = remember(state.notesList, state.userName) {
                viewModel.exportNotesToJson()
            }
            ExportNotesJsonDialog(
                notesCount = state.notesList.size,
                jsonContent = jsonContent,
                statusMessage = exportStatusMessage,
                onDownloadJsonFile = {
                    val fileName = "catatan_belajar_mri_${LocalDate.now()}.json"
                    runCatching {
                        val exportDir = context.getExternalFilesDir(null) ?: context.filesDir
                        val targetFile = File(exportDir, fileName)
                        targetFile.writeText(jsonContent, Charsets.UTF_8)
                        exportStatusMessage = "File JSON ($fileName) siap diunduh (${state.notesList.size} catatan)."
                    }
                    runCatching {
                        createJsonDocumentLauncher.launch(fileName)
                    }
                },
                onCopyJson = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(ClipData.newPlainText("Catatan Belajar MRI JSON", jsonContent))
                    exportStatusMessage = "Seluruh catatan dalam format JSON berhasil disalin ke clipboard!"
                },
                onShareJson = {
                    runCatching {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "Cadangan Catatan Belajar MRI (JSON)")
                            putExtra(Intent.EXTRA_TEXT, jsonContent)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export / Bagikan Catatan JSON"))
                    }
                },
                onDismiss = { showExportDialog = false }
            )
        }
    }
}


@Composable
fun NoteCard(
    note: StudyNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = note.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = note.dateString,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = "Edit", tint = CyanBright, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = RoseDanger, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.content,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun NoteEditorDialog(
    initialNote: StudyNote?,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, content: String, category: String) -> Unit
) {
    val normalizedInitialCat = when (initialNote?.category?.trim()?.lowercase()) {
        "fisika", "fisika sekuens" -> "Fisika Sekuens"
        "protokol", "klinis", "protokol klinis" -> "Protokol Klinis"
        "artefak", "mitigasi artefak" -> "Mitigasi Artefak"
        "keselamatan", "safety", "keselamatan mr" -> "Keselamatan MR"
        "catatan bebas" -> "Catatan Bebas"
        else -> initialNote?.category ?: "Catatan Bebas"
    }
    var title by rememberSaveable(initialNote?.id) { mutableStateOf(initialNote?.title ?: "") }
    var content by rememberSaveable(initialNote?.id) { mutableStateOf(initialNote?.content ?: "") }
    var category by rememberSaveable(initialNote?.id) { mutableStateOf(normalizedInitialCat) }
    val canSave = title.isNotBlank() || content.isNotBlank()

    val categories = listOf("Catatan Bebas", "Protokol Klinis", "Fisika Sekuens", "Mitigasi Artefak", "Keselamatan MR")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (initialNote != null) "Edit Catatan" else "Tulis Catatan Kosong Baru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tuliskan ringkasan materi, catatan dokter, atau parameter konsol",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Input Judul
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Catatan", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Tips Refokusing Spin Echo 180°", fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeepObsidian,
                        unfocusedContainerColor = DeepObsidian,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = CardSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pilihan Kategori
                Text(text = "Kategori Catatan:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { category = cat },
                            color = if (isSelected) CyanPrimary else DeepObsidian,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanBright else CardSlateBorder)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DeepObsidian else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tempat Catatan Kosong (Multi-line Text Area)
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Tempat Catatan Kosong", fontSize = 11.sp) },
                    placeholder = {
                        Text(
                            text = "Ketik catatan Anda di sini...\nContoh:\n- TR pendek (<600 ms) menghasilkan kontras T1w\n- STIR menggunakan TI ~150ms untuk supresi lemak\n- Pastikan pasien bebas logam sebelum Zona IV",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeepObsidian,
                        unfocusedContainerColor = DeepObsidian,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = CardSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Simpan & Batal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSave(initialNote?.id, title, content, category) },
                        enabled = canSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            disabledContainerColor = CardSlate
                        )
                    ) {
                        Text(
                            text = "Simpan",
                            fontSize = 12.sp,
                            color = if (canSave) DeepObsidian else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportNotesJsonDialog(
    notesCount: Int,
    jsonContent: String,
    statusMessage: String?,
    onDownloadJsonFile: () -> Unit,
    onCopyJson: () -> Unit,
    onShareJson: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_notes_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = BorderStroke(1.dp, CyanPrimary)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Catatan (JSON)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Portabilitas penuh • $notesCount catatan siap diunduh",
                            fontSize = 11.sp,
                            color = CyanBright
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberDark,
                        border = BorderStroke(1.dp, AmberAccent)
                    ) {
                        Text(
                            text = ".JSON",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AmberAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Unduh seluruh catatan belajar MRI Anda dalam format JSON standar agar tetap aman, mudah dicadangkan, dan dapat dipindahkan antar perangkat.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pratinjau Struktur JSON
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .testTag("export_json_preview"),
                    shape = RoundedCornerShape(12.dp),
                    color = DeepObsidian,
                    border = BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = jsonContent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyanBright,
                            lineHeight = 14.sp
                        )
                    }
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldDark,
                        border = BorderStroke(1.dp, EmeraldSuccess)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = statusMessage,
                                fontSize = 10.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Utama: Unduh File JSON
                Button(
                    onClick = onDownloadJsonFile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_json_file_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = DeepObsidian
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unduh File JSON (.json)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Baris Aksi Sekunder: Salin JSON & Bagikan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyJson,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_json_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSlate,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, CardSlateBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Salin JSON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = onShareJson,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_json_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSlate,
                            contentColor = AmberAccent
                        ),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bagikan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("close_export_dialog_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Text("Tutup", fontSize = 12.sp)
                }
            }
        }
    }
}

