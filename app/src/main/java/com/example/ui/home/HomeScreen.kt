package com.example.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MriModule
import com.example.data.MriRepository
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainTab
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import com.example.viewmodel.SimType

@Composable
fun HomeScreen(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Top Header: Pinned at top, does NOT scroll away when swiping up!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sisi Kiri: Avatar Pengguna & Judul Aplikasi
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gambar Avatar Interaktif
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyanBright, CircleShape)
                        .clickable { viewModel.setAvatarDialogVisible(true) },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarRes = MriRepository.getAvatarDrawableRes(state.userAvatarId)
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Avatar Pengguna",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = state.userName.ifBlank { "Pengguna" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "MRI EDU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Belajar MRI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }

            // Sisi Kanan: Gambar Api (Streak Hari Belajar 🔥) + Gambar Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GAMBAR API (STREAK HARI BELAJAR 🔥)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.setStreakDialogVisible(true) },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF451A03), // Dark Amber / Flame Background
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔥", fontSize = 13.sp)
                        Text(
                            text = "${state.studyStreakDays} Hari",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }

                // GAMBAR INFO
                IconButton(
                    onClick = { viewModel.setInfoDialogVisible(true) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DarkNavySlate)
                        .border(1.dp, CardSlateBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Aplikasi",
                        tint = CyanBright,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable Content Below Pinned Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Card matching user's photo with MRI scanner photo background
            HeroProgressCard(
                completedCount = state.completedCount,
                totalCount = state.totalNodesCount,
                lastVisitedTitle = if (state.isResumeRestored) state.lastVisitedNode.title else "Memuat posisi terakhir…",
                onContinueClicked = { viewModel.resumeStudy() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Title: "Simulasi interaktif"
            Text(
                text = "Simulasi interaktif",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Horizontal Row of Simulation & Feature Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SimInteractiveCard(
                    title = "Animasi Belajar",
                    subtitle = "Spin Echo & Ruang-k",
                    icon = Icons.Default.PlayCircle,
                    iconColor = CyanBright,
                    onClick = { viewModel.selectTab(MainTab.ANIMASI) }
                )
                SimInteractiveCard(
                    title = "Tersimpan",
                    subtitle = "Bookmark Materi & Kamus",
                    icon = Icons.Default.Bookmark,
                    iconColor = AmberWarning,
                    onClick = { viewModel.selectTab(MainTab.TERSIMPAN) }
                )
                SimInteractiveCard(
                    title = "Catatan Saya",
                    subtitle = "Logbook & Protokol Klinis",
                    icon = Icons.Default.EditNote,
                    iconColor = EmeraldSuccess,
                    onClick = { viewModel.selectTab(MainTab.CATATAN) }
                )
                SimInteractiveCard(
                    title = "Spin Proton & B0",
                    subtitle = "Presesi Larmor & B0",
                    icon = Icons.Default.MotionPhotosOn,
                    iconColor = CyanBright,
                    onClick = { viewModel.selectSimType(SimType.SPIN_LARMOR) }
                )
                SimInteractiveCard(
                    title = "Kontras TR & TE",
                    subtitle = "Pembobotan T1w, T2w & PDw",
                    icon = Icons.Default.Tune,
                    iconColor = EmeraldSuccess,
                    onClick = { viewModel.selectSimType(SimType.KONTRAS_TR_TE) }
                )
                SimInteractiveCard(
                    title = "Ruang-k & FFT",
                    subtitle = "Frekuensi Spasial & FFT",
                    icon = Icons.Default.Layers,
                    iconColor = AmberWarning,
                    onClick = { viewModel.selectSimType(SimType.RUANG_K) }
                )
                SimInteractiveCard(
                    title = "Pesawat & Magnet",
                    subtitle = "5 Lapisan, 4,2K & 5G",
                    icon = Icons.Default.Radio,
                    iconColor = CyanBright,
                    onClick = { viewModel.selectSimType(SimType.PESAWAT_MAGNET_M05) }
                )
                SimInteractiveCard(
                    title = "Gradien & Sistem RF",
                    subtitle = "Slew Rate, Faraday & Koil",
                    icon = Icons.Default.Tune,
                    iconColor = AmberWarning,
                    onClick = { viewModel.selectSimType(SimType.GRADIEN_RF_M06) }
                )
                SimInteractiveCard(
                    title = "Sekuens & Kontras",
                    subtitle = "SE, TSE, GRE, Ernst & DWI",
                    icon = Icons.Default.Bolt,
                    iconColor = CyanBright,
                    onClick = { viewModel.selectSimType(SimType.SEKUENS_KONTRAS_M03) }
                )
                SimInteractiveCard(
                    title = "Keselamatan & SAR",
                    subtitle = "Zonasi ACR & SAR Termal RF",
                    icon = Icons.Default.Security,
                    iconColor = CyanBright,
                    onClick = { viewModel.selectSimType(SimType.SAR_SAFETY) }
                )
                SimInteractiveCard(
                    title = "Artefak, Aliran & QA",
                    subtitle = "M08A–C, GMN & Tabel 8.8",
                    icon = Icons.Default.Warning,
                    iconColor = AmberWarning,
                    onClick = { viewModel.selectSimType(SimType.ARTEFAK_QA_M08) }
                )
                SimInteractiveCard(
                    title = "Glosarium Medis",
                    subtitle = "Kamus Terminologi Radiologi",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconColor = CyanBright,
                    onClick = { viewModel.selectTab(MainTab.GLOSARIUM) }
                )
                SimInteractiveCard(
                    title = "Kuis Evaluasi",
                    subtitle = "Uji Kompetensi Radiografer",
                    icon = Icons.Default.Quiz,
                    iconColor = AmberWarning,
                    onClick = { viewModel.selectTab(MainTab.KUIS) }
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Section Title: "Modul Pembelajaran (M01 - M09)"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kurikulum Radiologi Medis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${state.modulesList.size} Modul",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanBright
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Module Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.modulesList.forEach { module ->
                    val completedInModule = module.nodes.count { state.completedNodeCodes.contains(it.code) }
                    val bookmarkedInModule = module.nodes.count { state.bookmarkedNodeCodes.contains(it.code) }
                    ModuleCard(
                        module = module,
                        completedCount = completedInModule,
                        bookmarkedCount = bookmarkedInModule,
                        onClick = { viewModel.openModuleDetail(module) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun HeroProgressCard(
    completedCount: Int,
    totalCount: Int,
    lastVisitedTitle: String,
    onContinueClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image MRI Scanner Hero
            Image(
                painter = painterResource(id = R.drawable.mri_scanner_hero),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC090D16),
                                Color(0xF0090D16)
                            )
                        )
                    )
            )

            // Content inside hero
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PROGRES KURIKULUM KLINIS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberWarning,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount dari $totalCount simpul kompetensi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanBright,
                        trackColor = CardSlate
                    )
                }

                // "Lanjutkan" Pill Button matching photo!
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onContinueClicked),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF2F8FAFC)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Lanjutkan",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = lastVisitedTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D9488)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Lanjut",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimInteractiveCard(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(138.dp)
            .height(114.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, iconColor.copy(alpha = 0.78f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidian)
                    .border(1.dp, iconColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ModuleCard(
    module: MriModule,
    completedCount: Int,
    bookmarkedCount: Int = 0,
    onClick: () -> Unit
) {
    val totalNodes = module.nodes.size
    val isComplete = completedCount == totalNodes && totalNodes > 0
    val moduleProgress = if (totalNodes > 0) {
        (completedCount.toFloat() / totalNodes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val moduleAccentColor = when {
        isComplete -> EmeraldSuccess
        module.code.endsWith("1") || module.code.endsWith("5") || module.code.endsWith("9") -> CyanBright
        module.code.endsWith("2") || module.code.endsWith("6") -> Color(0xFF2DD4BF)
        module.code.endsWith("3") || module.code.endsWith("7") -> AmberWarning
        else -> Color(0xFF38BDF8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, moduleAccentColor.copy(alpha = 0.78f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isComplete) EmeraldDark else CyanDark)
                        .border(1.dp, moduleAccentColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = module.code,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isComplete) EmeraldSuccess else CyanBright,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = module.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (bookmarkedCount > 0) {
                            Text(
                                text = "★ $bookmarkedCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bar progres dinamis yang menyesuaikan dengan progres modul
                    LinearProgressIndicator(
                        progress = { moduleProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isComplete) EmeraldSuccess else moduleAccentColor,
                        trackColor = CardSlate
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Lingkaran progres yang diperbesar tanpa lambang panah (>)
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { moduleProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isComplete) EmeraldSuccess else moduleAccentColor,
                    trackColor = CardSlate,
                    strokeWidth = 4.5.dp
                )
                Text(
                    text = "$completedCount/$totalNodes",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isComplete) EmeraldSuccess else TextPrimary
                )
            }
        }
    }
}
