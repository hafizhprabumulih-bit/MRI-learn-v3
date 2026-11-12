package com.example.ui.simulators

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.*

private fun fmtId(v: Double, decimals: Int = 1): String =
    String.format(Locale("id", "ID"), "%.${decimals}f", v)

// ============================================================================
// SUITE UTAMA MODUL 7 (M07A, M07B, M07C) - 100% NATIVE JETPACK COMPOSE
// ============================================================================
private data class M07SubCard(
    val id: String,
    val subModule: String,
    val code: String,
    val title: String,
    val nodeTag: String,
    val mainTab: Int,
    val subTab: Int
)

private val M07_SUB_CARDS = listOf(
    M07SubCard("m7a_zona_akses", "M07A", "A1", "Denah Zona I–IV ACR", "M07A.T1", 0, 0),
    M07SubCard("m7a_skrining", "M07A", "A2", "Simulasi Skrining", "M07A.T1", 0, 1),
    M07SubCard("m7a_label_alur", "M07A", "A3", "Label ASTM & Alur Implan", "M07A.T2", 0, 2),
    M07SubCard("m7a_lima_bahaya", "M07A", "A4", "5 Bahaya Utama MR", "M07A.T2", 0, 3),
    M07SubCard("m7a_kecemasan", "M07A", "A5", "Kecemasan Pasien", "M07A.T2", 0, 4),
    M07SubCard("m7a_tips", "M07A", "A6", "Kuis Tips Keselamatan", "M07A.T2", 0, 5),
    M07SubCard("m7a_kontras", "M07A", "A7", "Skrining Kontras & eGFR", "M07A.T3", 0, 6),
    M07SubCard("m7b_medan_tepi", "M07B", "B1", "Medan Tepi 5G & Tarikan", "M07B.T1", 1, 0),
    M07SubCard("m7b_torsi", "M07B", "B2", "Torsi Implan Memanjang", "M07B.T1", 1, 1),
    M07SubCard("m7b_skala_efek", "M07B", "B3", "Skala Efek Medan B₀", "M07B.T2", 1, 2),
    M07SubCard("m7b_oksigen_helium", "M07B", "B4", "Oksigen & Ekspansi He", "M07B.T2", 1, 3),
    M07SubCard("m7b_lenz", "M07B", "B5", "Efek Lenz (Arus Pusar)", "M07B.T3", 1, 4),
    M07SubCard("m7b_darurat", "M07B", "B6", "Tindakan Darurat Ruang MR", "M07B.T3", 1, 5),
    M07SubCard("m7c_sar", "M07C", "C1", "Kalkulator SAR IEC", "M07C.T1", 2, 0),
    M07SubCard("m7c_antena_loop", "M07C", "C2", "Efek Antena & Loop", "M07C.T2", 2, 1),
    M07SubCard("m7c_stimulasi", "M07C", "C3", "Ambang PNS Gradien", "M07C.T2", 2, 2),
    M07SubCard("m7c_bising", "M07C", "C4", "Kalkulator Bising dB", "M07C.T3", 2, 3),
    M07SubCard("m7c_b1_jaringan", "M07C", "C5", "Gelombang RF & B₁", "M07C.T3", 2, 4)
)

@Composable
fun Module7SafetyLabSuite(
    selectedActivityId: String = "m7a_zona_akses",
    onSelectActivity: (String) -> Unit = {},
    onNodeComplete: (String) -> Unit = {}
) {
    val activeCard = M07_SUB_CARDS.firstOrNull { it.id == selectedActivityId } ?: M07_SUB_CARDS.first()
    var subFilter by remember { mutableStateOf("SEMUA") }
    val filteredCards = remember(subFilter) {
        if (subFilter == "SEMUA") M07_SUB_CARDS else M07_SUB_CARDS.filter { it.subModule == subFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module7_safety_lab_suite"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Laboratorium Keselamatan MR",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "M07: Laboratorium Keselamatan MR (M07A–C)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "18 simulasi Zona I–IV ACR, skrining implan, medan tepi 5G, quench, SAR IEC, PNS & bising.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("SEMUA" to "Semua (18)", "M07A" to "M07A (7)", "M07B" to "M07B (6)", "M07C" to "M07C (5)").forEach { (id, lbl) ->
                        FilterChip(
                            selected = subFilter == id,
                            onClick = { subFilter = id },
                            label = { Text(lbl, fontSize = 10.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredCards.chunked(2).forEach { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            pair.forEach { item ->
                                val isSel = item.id == activeCard.id
                                Surface(
                                    onClick = { onSelectActivity(item.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${item.subModule}·${item.code}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(item.nodeTag, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        when (activeCard.mainTab) {
            0 -> when (activeCard.subTab) {
                0 -> M07AZoneAndAccessSimulator()
                1 -> M07AScreeningScenarioSimulator(onComplete = { onNodeComplete("M07A.T1.2") })
                2 -> M07ADeviceLabelAndFlowSimulator()
                3 -> M07AFiveHazardsScannerSimulator()
                4 -> M07APatientAnxietyJourneySimulator()
                5 -> M07ASafetyTipsTrueFalseSimulator(onComplete = { onNodeComplete("M07A.T2.5") })
                else -> M07AContrastScreeningSimulator()
            }
            1 -> when (activeCard.subTab) {
                0 -> M07BFringeFieldAndForceSimulator()
                1 -> M07BImplantTorqueSimulator()
                2 -> M07BFieldScaleEffectsSimulator()
                3 -> M07BOxygenAndHeliumExpansionSimulator()
                4 -> M07BLenzEffectDropSimulator()
                else -> M07BEmergencyActionSimulator(onComplete = { onNodeComplete("M07B.T3.2") })
            }
            else -> when (activeCard.subTab) {
                0 -> M07CSarCalculatorSimulator()
                1 -> M07CAntennaAndLoopBurnSimulator()
                2 -> M07CPnsThresholdSimulator()
                3 -> M07CAcousticNoiseCalculatorSimulator()
                else -> M07CRfWavelengthInTissueSimulator()
            }
        }
    }
}

// ============================================================================
// M07A · A1: DENAH ZONA DAN AKSES PERSONEL
// ============================================================================
@Composable
fun M07AZoneAndAccessSimulator() {
    var panelMode by remember { mutableIntStateOf(0) } // 0 = Denah zona, 1 = Akses personel
    var selectedZone by remember { mutableIntStateOf(0) }
    var gaussOption by remember { mutableIntStateOf(0) } // 0 = 0,5 mT (standar), 1 = 0,9 mT (alternatif)

    var selectedPerson by remember { mutableIntStateOf(0) }
    var powerOutage by remember { mutableStateOf(false) }
    var resistiveSystem by remember { mutableStateOf(false) }

    val zoneData = listOf(
        "Zona I: umum" to "Area yang bebas dimasuki siapa saja, misalnya pintu masuk dan ruang tunggu.",
        "Zona II: antara" to "Pasien diterima, ditanyai, dan diberi penjelasan. Pengawasan dimulai di sini.",
        "Zona III: terkontrol" to "Hanya orang yang sudah diskrining. Akses dikendalikan fisik (kunci/passkey) karena dekat ruang magnet.",
        "Zona IV: ruang magnet" to "Ruang pemindai. B0 selalu menyala. Hanya orang yang lolos skrining dan benda aman-MR yang boleh masuk."
    )
    val gaussValues = listOf(0.5, 0.9)
    val gaussNames = listOf("standar", "alternatif")

    data class PersonAccess(
        val name: String,
        val zoneStatuses: List<String>, // "ok", "awas", "no", "sop"
        val description: String
    )

    val personnelList = listOf(
        PersonAccess(
            "Pengunjung / staf non-MRI",
            listOf("ok", "ok", "no", "no"),
            "Belum menjalani pelatihan keselamatan formal dalam 12 bulan terakhir."
        ),
        PersonAccess(
            "Pasien belum diskrining",
            listOf("ok", "awas", "no", "no"),
            "Disambut di Zona II dan tidak bebas bergerak. Jawaban skrining dan riwayat diambil di sini."
        ),
        PersonAccess(
            "Pasien sudah diskrining",
            listOf("ok", "awas", "awas", "awas"),
            "Berada di bawah pengawasan langsung personel MRI saat memasuki Zona III dan IV."
        ),
        PersonAccess(
            "Personel Level 1",
            listOf("ok", "ok", "ok", "sop"),
            "Staf kantor dan pembantu pasien dengan pendidikan keselamatan minimal untuk bekerja di Zona III."
        ),
        PersonAccess(
            "Personel Level 2",
            listOf("ok", "ok", "ok", "ok"),
            "Teknolog MRI, radiolog, dan perawat yang dilatih mendalam tentang pemanasan, luka bakar, dan rangsangan saraf-otot akibat gradien."
        )
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A1. Denah Zona dan Akses Personel (T1.1, T2.1, T2.2)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = panelMode == 0,
                    onClick = { panelMode = 0 },
                    label = { Text("Denah zona") }
                )
                FilterChip(
                    selected = panelMode == 1,
                    onClick = { panelMode = 1 },
                    label = { Text("Akses personel") }
                )
            }

            if (panelMode == 0) {
                Text(
                    "Pilih zona untuk membaca aturannya. Ubah garis 5 gauss untuk melihat batas pengaman.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Zona I", "Zona II", "Zona III", "Zona IV").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedZone == idx,
                            onClick = { selectedZone = idx },
                            label = { Text(label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    gaussValues.forEachIndexed { idx, g ->
                        FilterChip(
                            selected = gaussOption == idx,
                            onClick = { gaussOption = idx },
                            label = { Text("Garis ${fmtId(g, 1)} mT (${gaussNames[idx]})") }
                        )
                    }
                }

                val accentColor = MaterialTheme.colorScheme.primary
                val strokeActive = MaterialTheme.colorScheme.onSurface
                val strokeIdle = MaterialTheme.colorScheme.outlineVariant

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    val w = size.width
                    val h = size.height
                    for (i in 0..3) {
                        val insetX = w * (0.03f + i * 0.08f)
                        val insetY = h * (0.05f + i * 0.1f)
                        val rectW = w - insetX * 2f
                        val rectH = h - insetY * 2f
                        drawRoundRect(
                            color = accentColor.copy(alpha = 0.08f + i * 0.08f),
                            topLeft = Offset(insetX, insetY),
                            size = Size(rectW, rectH),
                            cornerRadius = CornerRadius(18f, 18f)
                        )
                        drawRoundRect(
                            color = if (i == selectedZone) strokeActive else strokeIdle,
                            topLeft = Offset(insetX, insetY),
                            size = Size(rectW, rectH),
                            cornerRadius = CornerRadius(18f, 18f),
                            style = Stroke(width = if (i == selectedZone) 6f else 2f)
                        )
                    }
                    val center = Offset(w / 2f, h / 2f)
                    val radiusFactor = (gaussValues[0] / gaussValues[gaussOption]).pow(1.0 / 3.0).toFloat()
                    val rGauss = min(w, h) * 0.25f * radiusFactor
                    drawCircle(
                        color = Color(0xFFD9480F),
                        radius = rGauss,
                        center = center,
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                        )
                    )
                    drawCircle(
                        color = Color(0xFF2F6BFF).copy(alpha = 0.8f),
                        radius = min(w, h) * 0.1f,
                        center = center
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${zoneData[selectedZone].first}. ${zoneData[selectedZone].second} Garis 5 gauss: ${fmtId(gaussValues[gaussOption], 1)} mT. Medan menurun dengan jarak, jadi garis yang lebih besar nilainya letaknya lebih dekat ke magnet.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    "Pilih siapa yang ingin masuk. Hijau: bebas. Oranye: dengan pengawasan. Merah: tidak boleh. Abu-abu: ikuti SOP.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    personnelList.forEachIndexed { idx, p ->
                        FilterChip(
                            selected = selectedPerson == idx,
                            onClick = { selectedPerson = idx },
                            label = { Text(p.name, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = powerOutage,
                        onClick = { powerOutage = !powerOutage },
                        label = { Text("Listrik padam: ${if (powerOutage) "hidup" else "mati"}") }
                    )
                    FilterChip(
                        selected = resistiveSystem,
                        onClick = { resistiveSystem = !resistiveSystem },
                        label = { Text("Sistem resistif: ${if (resistiveSystem) "hidup" else "mati"}") }
                    )
                }

                val currentPerson = personnelList[selectedPerson]
                val statusColors = mapOf(
                    "ok" to Color(0xFF2B8A3E),
                    "awas" to Color(0xFFD9480F),
                    "no" to Color(0xFFC92A2A),
                    "sop" to Color(0xFF5A6572)
                )
                val statusLabels = mapOf(
                    "ok" to "✓ bebas",
                    "awas" to "! diawasi",
                    "no" to "✕ dilarang",
                    "sop" to "? ikuti SOP"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Zona I", "Zona II", "Zona III", "Zona IV").forEachIndexed { zIdx, zTitle ->
                        val st = currentPerson.zoneStatuses[zIdx]
                        val col = statusColors[st] ?: Color.Gray
                        Surface(
                            color = col.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, col, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(zTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    statusLabels[st] ?: "",
                                    color = col,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = if (resistiveSystem) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFC92A2A).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (resistiveSystem) Color.Gray else Color(0xFFC92A2A))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Lampu Zona IV: 'The Magnet is On'",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            when {
                                resistiveSystem -> "Padam saat sistem resistif mati"
                                powerOutage -> "MENYALA (Cadangan Baterai ≥ 24 Jam)"
                                else -> "MENYALA TERUS 24/7"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (resistiveSystem) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFC92A2A)
                        )
                    }
                }

                Text(
                    text = "${currentPerson.name}. ${currentPerson.description} " +
                        if (resistiveSystem) "Sistem resistif dikecualikan dari aturan lampu yang menyala terus."
                        else "Lampu merah dan tanda di Zona IV menyala terus${if (powerOutage) ", juga saat listrik padam, lewat sumber cadangan sedikitnya 24 jam." else "."}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// M07A · A2: SIMULASI SKRINING (LANJUT / TAHAN / TOLAK)
// ============================================================================
@Composable
fun M07AScreeningScenarioSimulator(
    onComplete: () -> Unit = {}
) {
    data class ScreeningScenario(
        val text: String,
        val correctDecision: String, // "lanjut", "tahan", "tolak"
        val explanation: String
    )

    val scenarios = remember {
        listOf(
            ScreeningScenario(
                "Pasien membawa kunci, ponsel, dan ikat pinggang logam. Tidak ada implan.",
                "tahan",
                "Barang logam dilepas dan disimpan di luar Zona IV dahulu. Setelah itu skrining dilanjutkan."
            ),
            ScreeningScenario(
                "Pasien membawa implan berlabel MR Conditional. Syaratnya belum diperiksa.",
                "tahan",
                "MR Conditional berarti aman hanya bila syarat pada label dipenuhi. Syarat diperiksa petugas yang berwenang dahulu."
            ),
            ScreeningScenario(
                "Staf kebersihan ingin masuk Zona IV mendorong troli baja.",
                "tolak",
                "Troli ferromagnetik dapat menjadi proyektil. Gunakan peralatan aman-MR dan petugas yang terlatih."
            ),
            ScreeningScenario(
                "Pasien membawa implan berlabel MR Unsafe.",
                "tolak",
                "MR Unsafe berarti berbahaya di lingkungan MR, jadi tidak masuk ruang magnet."
            ),
            ScreeningScenario(
                "Pasien tidak ingat jenis implan dan tidak ada dokumennya.",
                "tahan",
                "Implan yang tidak diketahui tidak boleh dianggap aman. Cari dokumen dan tanyakan penanggung jawab keselamatan MR."
            ),
            ScreeningScenario(
                "Semua logam sudah dilepas, formulir lengkap, dan tidak ada implan.",
                "lanjut",
                "Skrining terpenuhi. Pemeriksaan boleh dilanjutkan sesuai SOP fasilitas."
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedChoice by remember { mutableStateOf<String?>(null) }
    var firstTryAnswered by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A2. Simulasi Skrining Pasien & Peralatan (T1.2, T2.3)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Baca skenario, lalu putuskan: Lanjut, Tahan, atau Tolak. Ini latihan konsep, bukan pengganti SOP.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (finished) {
                Surface(
                    color = Color(0xFF2B8A3E).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Selesai! Skor Anda: $score dari ${scenarios.size}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2B8A3E)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                onComplete()
                            }) {
                                Text("Simpan Progres ke Room DB")
                            }
                            OutlinedButton(onClick = {
                                currentIndex = 0
                                score = 0
                                selectedChoice = null
                                firstTryAnswered = false
                                finished = false
                            }) {
                                Text("Ulangi Skenario")
                            }
                        }
                    }
                }
            } else {
                val sc = scenarios[currentIndex]
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Skenario ${currentIndex + 1} dari ${scenarios.size} • Skor Benar: $score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sc.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("lanjut" to "Lanjut", "tahan" to "Tahan", "tolak" to "Tolak").forEach { (key, label) ->
                        val isSelected = selectedChoice == key
                        val isCorrect = key == sc.correctDecision
                        val btnColor = when {
                            !isSelected -> MaterialTheme.colorScheme.surfaceVariant
                            isCorrect -> Color(0xFF2B8A3E)
                            else -> Color(0xFFC92A2A)
                        }
                        Button(
                            onClick = {
                                if (!firstTryAnswered) {
                                    firstTryAnswered = true
                                    if (isCorrect) score++
                                }
                                selectedChoice = key
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = btnColor,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                selectedChoice?.let { choice ->
                    val ok = choice == sc.correctDecision
                    Surface(
                        color = (if (ok) Color(0xFF2B8A3E) else Color(0xFFC92A2A)).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (if (ok) "Tepat. " else "Belum tepat (yang sesuai: ${sc.correctDecision.uppercase()}). ") + sc.explanation,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (ok) Color(0xFF2B8A3E) else Color(0xFFC92A2A),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (currentIndex < scenarios.size - 1) {
                                currentIndex++
                                selectedChoice = null
                                firstTryAnswered = false
                            } else {
                                finished = true
                                onComplete()
                            }
                        }) {
                            Text(if (currentIndex == scenarios.size - 1) "Selesai" else "Berikutnya →")
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// M07A · A3: LABEL PERANGKAT ASTM & ALUR KEPUTUSAN PASIEN
// ============================================================================
@Composable
fun M07ADeviceLabelAndFlowSimulator() {
    var tab by remember { mutableIntStateOf(0) } // 0 = Label perangkat, 1 = Alur keputusan
    var selectedDevice by remember { mutableIntStateOf(0) }
    var selectedB0 by remember { mutableStateOf("1.5") }
    var cableRouted by remember { mutableStateOf(false) }

    var docKnown by remember { mutableStateOf(true) }
    var condMet by remember { mutableStateOf(true) }
    var newFinding by remember { mutableStateOf(false) }

    val devices = listOf(
        Triple("Cawan petri plastik", "safe", "Tidak menghantar listrik, tidak berlogam, dan tidak magnetik. Tidak ada bahaya yang diketahui di semua lingkungan MR."),
        Triple("Gunting ferromagnetik", "unsafe", "Benda magnetik yang diketahui berbahaya di semua lingkungan MR."),
        Triple("Neurostimulator (contoh fiktif)", "cond", "Contoh syarat: B0 tidak lebih dari 1,5 T dan kabel dirutekan sesuai petunjuk."),
        Triple("Implan tanpa label & dokumen", "none", "Tanpa label dan dokumen: jangan dianggap aman.")
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A3. Standar Label ASTM & Alur Keputusan Implan (T2.3)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Label perangkat ASTM") })
                FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Alur keputusan") })
            }

            if (tab == 0) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    devices.forEachIndexed { idx, d ->
                        FilterChip(
                            selected = selectedDevice == idx,
                            onClick = { selectedDevice = idx },
                            label = { Text(d.first, fontSize = 12.sp) }
                        )
                    }
                }

                val currentDev = devices[selectedDevice]
                if (currentDev.second == "cond") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedB0 == "1.5",
                            onClick = { selectedB0 = "1.5" },
                            label = { Text("B0 1,5 T") }
                        )
                        FilterChip(
                            selected = selectedB0 == "3",
                            onClick = { selectedB0 = "3" },
                            label = { Text("B0 3 T") }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { cableRouted = !cableRouted }
                    ) {
                        Checkbox(checked = cableRouted, onCheckedChange = { cableRouted = it })
                        Text("Kabel dirutekan sesuai petunjuk label", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Canvas(modifier = Modifier.size(84.dp)) {
                        val s = size.minDimension
                        when (currentDev.second) {
                            "safe" -> {
                                drawRect(
                                    color = Color(0xFF2B8A3E),
                                    topLeft = Offset(s * 0.1f, s * 0.1f),
                                    size = Size(s * 0.8f, s * 0.8f),
                                    style = Stroke(width = 10f)
                                )
                            }
                            "cond" -> {
                                val path = Path().apply {
                                    moveTo(s * 0.5f, s * 0.08f)
                                    lineTo(s * 0.92f, s * 0.88f)
                                    lineTo(s * 0.08f, s * 0.88f)
                                    close()
                                }
                                drawPath(path = path, color = Color(0xFFF2C94C))
                                drawPath(path = path, color = Color(0xFF1B1F24), style = Stroke(width = 5f))
                            }
                            "unsafe" -> {
                                drawCircle(
                                    color = Color(0xFFC92A2A),
                                    radius = s * 0.4f,
                                    center = Offset(s / 2f, s / 2f),
                                    style = Stroke(width = 10f)
                                )
                                drawLine(
                                    color = Color(0xFFC92A2A),
                                    start = Offset(s * 0.22f, s * 0.22f),
                                    end = Offset(s * 0.78f, s * 0.78f),
                                    strokeWidth = 10f
                                )
                            }
                            else -> {
                                drawCircle(
                                    color = Color.Gray,
                                    radius = s * 0.4f,
                                    center = Offset(s / 2f, s / 2f),
                                    style = Stroke(
                                        width = 6f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                    )
                                )
                            }
                        }
                    }

                    Column {
                        val badgeTitle = when (currentDev.second) {
                            "safe" -> "MR Safe (Kotak Hijau)"
                            "cond" -> "MR Conditional (Segitiga Kuning)"
                            "unsafe" -> "MR Unsafe (Lingkaran Merah Dicoret)"
                            else -> "Tanpa Label / Dokumen (?)"
                        }
                        Text(currentDev.first, fontWeight = FontWeight.ExtraBold)
                        Text(badgeTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        val condResult = if (currentDev.second == "cond") {
                            if (selectedB0 == "1.5" && cableRouted) {
                                " Syarat contoh terpenuhi: boleh dilanjutkan sesuai syarat label dan SOP."
                            } else {
                                " Syarat belum terpenuhi! Aman bersyarat pada 1,5 T belum tentu aman pada 3 T atau tanpa perutean kabel."
                            }
                        } else ""
                        Text(currentDev.third + condResult, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { docKnown = !docKnown }) {
                        Checkbox(checked = docKnown, onCheckedChange = { docKnown = it })
                        Text("1. Jenis perangkat diketahui dan ada bukti dokumen", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { condMet = !condMet }) {
                        Checkbox(checked = condMet, onCheckedChange = { condMet = it })
                        Text("2. Syarat MR Conditional terpenuhi (atau perangkat MR Safe)", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { newFinding = !newFinding }) {
                        Checkbox(checked = newFinding, onCheckedChange = { newFinding = it })
                        Text("3. Ada temuan baru saat kedatangan (misalnya tindik logam)", style = MaterialTheme.typography.bodySmall)
                    }
                }

                val outcomeIndex = when {
                    !docKnown -> 0
                    !condMet -> 1
                    newFinding -> 2
                    else -> 3
                }
                val outcomes = listOf(
                    "Tunda: cari bukti dokumen" to "Tanpa bukti dokumen, pemeriksaan ditunda sampai bukti cukup. Keterangan lisan saja tidak cukup.",
                    "Syarat belum terpenuhi: tunda/batalkan" to "Syarat label belum terpenuhi. Aman bersyarat pada satu kondisi belum tentu aman pada kondisi lain.",
                    "Tinjau ulang risiko-manfaat & minta saran" to "Temuan baru muncul saat pasien datang. Tinjau ulang risiko dan manfaat, minta saran fisikawan medis/radiolog.",
                    "Dapat lanjut sesuai SOP fasilitas" to "Bukti cukup dan syarat terpenuhi. Tanggung jawab utama ada pada dokter perujuk & radiology lead."
                )

                Surface(
                    color = (if (outcomeIndex == 3) Color(0xFF2B8A3E) else Color(0xFFD9480F)).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Keputusan Alur: ${outcomes[outcomeIndex].first}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (outcomeIndex == 3) Color(0xFF2B8A3E) else Color(0xFFD9480F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(outcomes[outcomeIndex].second, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ============================================================================
// M07A · A4: LIMA SUMBER BAHAYA PADA PEMINDAI
// ============================================================================
@Composable
fun M07AFiveHazardsScannerSimulator() {
    var selectedHazard by remember { mutableIntStateOf(0) }
    val hazards = listOf(
        "Psikologis" to "Bore sempit, bising, lama pemindaian, rasa terisolasi, dan takut hasil buruk dapat memicu cemas, klaustrofobia, sampai serangan panik.",
        "Medan statis (B0)" to "B0 selalu menyala dan berubah menurut jarak: efek sementara (vertigo, rasa logam), proyektil ferromagnetik, dan torsi pada implan.",
        "Medan RF" to "Pulsa RF menghangatkan jaringan (SAR) dan dapat menyebabkan luka bakar, terutama lewat loop konduktif dan efek antena.",
        "Gradien (dB/dt)" to "Medan yang berubah cepat menurut waktu dapat merangsang saraf tepi (PNS) dan otot, serta menimbulkan bising akustik keras.",
        "Kriogen" to "Helium cair (4 K) mendinginkan magnet. Saat quench, helium menguap menjadi gas (1 L → 754 L) dan berisiko asfiksia/tekanan tinggi."
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A4. Lima Sumber Bahaya pada Pemindai MRI (T1.3)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hazards.forEachIndexed { idx, h ->
                    FilterChip(
                        selected = selectedHazard == idx,
                        onClick = { selectedHazard = idx },
                        label = { Text(h.first) }
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                val w = size.width
                val h = size.height
                val x0 = w * 0.14f
                val mw = w * 0.72f

                // Fringe field dashed ellipse when B0 selected
                if (selectedHazard == 1) {
                    drawOval(
                        color = Color(0xFFC92A2A),
                        topLeft = Offset(w * 0.04f, h * 0.04f),
                        size = Size(w * 0.92f, h * 0.92f),
                        style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
                    )
                }

                // Magnet + Cryostat top & bottom
                listOf(h * 0.08f, h * 0.72f).forEach { yPos ->
                    drawRoundRect(
                        color = Color(0xFF2F6BFF).copy(alpha = if (selectedHazard == 1) 0.85f else 0.25f),
                        topLeft = Offset(x0, yPos),
                        size = Size(mw, h * 0.2f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFF0B7285).copy(alpha = if (selectedHazard == 4) 0.9f else 0.25f),
                        topLeft = Offset(x0 + 12f, yPos + 6f),
                        size = Size(mw - 24f, h * 0.13f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                // Gradient coils
                listOf(h * 0.29f, h * 0.61f).forEach { yPos ->
                    drawRoundRect(
                        color = Color(0xFFD9480F).copy(alpha = if (selectedHazard == 3) 0.9f else 0.25f),
                        topLeft = Offset(x0, yPos),
                        size = Size(mw, h * 0.09f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                // RF coils
                listOf(h * 0.39f, h * 0.54f).forEach { yPos ->
                    drawRoundRect(
                        color = Color(0xFF1F6FEB).copy(alpha = if (selectedHazard == 2) 0.9f else 0.25f),
                        topLeft = Offset(x0, yPos),
                        size = Size(mw, h * 0.06f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                // Bore & patient
                drawRoundRect(
                    color = Color(0xFFA5D8FF),
                    topLeft = Offset(w * 0.28f, h * 0.45f),
                    size = Size(w * 0.34f, h * 0.08f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawCircle(
                    color = Color(0xFFF1C27D),
                    radius = h * 0.045f,
                    center = Offset(w * 0.66f, h * 0.49f)
                )
                if (selectedHazard == 0) {
                    drawCircle(
                        color = Color(0xFFC92A2A),
                        radius = h * 0.08f,
                        center = Offset(w * 0.66f, h * 0.49f),
                        style = Stroke(width = 5f)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${hazards[selectedHazard].first}: ${hazards[selectedHazard].second}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================================
// M07A · A5: PERJALANAN KECEMASAN PASIEN & KLAUSTROFOBIA
// ============================================================================
@Composable
fun M07APatientAnxietyJourneySimulator() {
    val stages = listOf("Persiapan", "Masuk bore", "Awal pindai", "Pertengahan", "Akhir")
    val baseAnxiety = listOf(0.50f, 0.90f, 0.70f, 0.55f, 0.40f)
    val interventions = listOf(
        "Kipas di bore" to listOf(0.10f, 0.10f, 0.10f, 0.10f, 0.10f),
        "Lampu & panel pemandangan" to listOf(0.05f, 0.12f, 0.10f, 0.08f, 0.05f),
        "Video & hitung mundur" to listOf(0.03f, 0.10f, 0.12f, 0.12f, 0.10f),
        "Bore lebih pendek & lebar (3x turun)" to listOf(0.05f, 0.15f, 0.12f, 0.10f, 0.08f)
    )
    var activeFlags by remember { mutableStateOf(listOf(false, false, false, false)) }

    val currentValues = baseAnxiety.mapIndexed { stageIdx, base ->
        var red = 0f
        interventions.forEachIndexed { intIdx, pair ->
            if (activeFlags[intIdx]) red += pair.second[stageIdx]
        }
        max(0.05f, base - red)
    }
    val peakValue = currentValues.maxOrNull() ?: 0.5f
    val peakStageIdx = currentValues.indexOfFirst { it == peakValue }.coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A5. Perjalanan Kecemasan Pasien & Strategi Penenang (T2.4)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Nyalakan langkah penenang untuk melihat penurunan kurva kecemasan pasien selama pemeriksaan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                interventions.forEachIndexed { idx, item ->
                    FilterChip(
                        selected = activeFlags[idx],
                        onClick = {
                            activeFlags = activeFlags.toMutableList().also { it[idx] = !it[idx] }
                        },
                        label = { Text(item.first, fontSize = 12.sp) }
                    )
                }
            }

            val lineColor = MaterialTheme.colorScheme.primary
            val peakColor = when {
                peakValue >= 0.7f -> Color(0xFFC92A2A)
                peakValue >= 0.45f -> Color(0xFFD9480F)
                else -> Color(0xFF2B8A3E)
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                val w = size.width
                val h = size.height
                val path = Path()
                currentValues.forEachIndexed { i, v ->
                    val x = w * (0.1f + i * 0.2f)
                    val y = h * (0.85f - v * 0.72f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = lineColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
                currentValues.forEachIndexed { i, v ->
                    val x = w * (0.1f + i * 0.2f)
                    val y = h * (0.85f - v * 0.72f)
                    drawCircle(
                        color = if (i == peakStageIdx) peakColor else lineColor,
                        radius = if (i == peakStageIdx) 14f else 9f,
                        center = Offset(x, y)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stages.forEachIndexed { i, s ->
                    Text(
                        "$s\n(${(currentValues[i] * 100).toInt()}%)",
                        fontSize = 10.sp,
                        fontWeight = if (i == peakStageIdx) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (i == peakStageIdx) peakColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Puncak kecemasan: ${stages[peakStageIdx].lowercase()} (${(peakValue * 100).toInt()}%). " +
                    when {
                        peakValue >= 0.7f -> "Tingkat tinggi: risiko artefak gerak/pergeseran fase, serangan panik, atau rujukan ke CT."
                        peakValue >= 0.45f -> "Tingkat sedang: pasien tetap perlu dipantau dan ditenangkan."
                        else -> "Tingkat rendah: pasien cenderung mampu menyelesaikan pemeriksaan dengan tenang."
                    },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================================
// M07A · A6: BENAR ATAU KELIRU? TIPS KESELAMATAN PRAKTIS
// ============================================================================
@Composable
fun M07ASafetyTipsTrueFalseSimulator(
    onComplete: () -> Unit = {}
) {
    val statements = remember {
        listOf(
            Triple("1. Staf yang pernah masuk ruang magnet tidak perlu diskrining lagi.", false, "Jangan berasumsi staf paham bahayanya. Skrining seketat pada pasien."),
            Triple("2. Kunci kode digital adalah pilihan yang baik untuk pintu Zona III.", false, "Kode mudah dibagikan ke staf departemen lain yang tidak paham keselamatan MR."),
            Triple("3. Pasien yang kemarin dipindai di tempat lain pasti aman dipindai hari ini.", false, "Kekuatan medan dan gradien bisa berbeda, dan kondisi pasien bisa berubah."),
            Triple("4. Selain formulir tertulis, kontraindikasi perlu ditanyakan secara lisan.", true, "Pasien kadang menyebut bahaya yang tidak tertulis di formulir."),
            Triple("5. Citra rontgen terbaru dapat membantu menyingkirkan pacu jantung atau klip aneurisma.", true, "Citra yang sudah ada dapat memperlihatkan benda logam tersebut."),
            Triple("6. Tangga pijakan dan alat pemadam di departemen MRI sebaiknya non-ferrous bila ada pilihan.", true, "Benda ferromagnetik besar bisa terbawa masuk ke ruang magnet tanpa sengaja."),
            Triple("7. Bila ragu dan ditekan untuk tetap memindai, lanjutkan agar jadwal tidak tertunda.", false, "Jangan membiarkan diri ditekan. Bila ragu, tunda pemeriksaan.")
        )
    }
    var answers by remember { mutableStateOf(List<Boolean?>(statements.size) { null }) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A6. Benar atau Keliru? Tips Keselamatan (T2.5)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            statements.forEachIndexed { idx, item ->
                val userAns = answers[idx]
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(item.first, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(true to "Benar", false to "Keliru").forEach { (boolVal, label) ->
                                val selected = userAns == boolVal
                                val correct = boolVal == item.second
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val next = answers.toMutableList()
                                        next[idx] = if (selected) null else boolVal
                                        answers = next
                                        if (next.all { it != null }) onComplete()
                                    },
                                    label = {
                                        Text(
                                            if (selected) (if (correct) "✓ $label" else "✗ $label") else label,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        }
                        if (userAns != null) {
                            val ok = userAns == item.second
                            Text(
                                text = (if (ok) "Tepat. " else "Belum tepat. ") + item.third,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ok) Color(0xFF2B8A3E) else Color(0xFFC92A2A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// M07A · A7: ALUR SKRINING SEBELUM AGEN KONTRAS GADOLINIUM
// ============================================================================
@Composable
fun M07AContrastScreeningSimulator() {
    var renalRisk by remember { mutableStateOf(false) }
    var egfrChecked by remember { mutableStateOf(false) }
    var egfrVeryLow by remember { mutableStateOf(false) }
    var pregnant by remember { mutableStateOf(false) }
    var priorAllergy by remember { mutableStateOf(false) }

    val ginjalStatus = when {
        egfrVeryLow -> Triple("no", "Konsul radiolog", "eGFR sangat rendah (< 30) atau cedera ginjal akut: ada risiko NSF. Radiolog menimbang MRI tanpa kontras atau agen dengan risiko terendah.")
        renalRisk && !egfrChecked -> Triple("awas", "Periksa eGFR", "Ada faktor risiko ginjal tetapi eGFR belum diperiksa: periksa dahulu sebelum kontras.")
        renalRisk -> Triple("ok", "eGFR cukup", "Faktor risiko ada, eGFR sudah diperiksa dan tidak sangat rendah: lanjut sesuai SOP.")
        else -> Triple("ok", "Skrining singkat", "Tanpa faktor risiko ginjal: skrining singkat sesuai SOP.")
    }
    val hamilStatus = if (pregnant) {
        Triple("no", "Tinjau manfaat-risiko", "Gadolinium melewati plasenta: kontras dihindari kecuali manfaatnya jelas lebih besar. Keputusan ada pada radiolog dan dokter perujuk.")
    } else {
        Triple("ok", "Tanyakan status", "Tanyakan status kehamilan secara lisan dan tertulis pada pasien usia subur.")
    }
    val alergiStatus = if (priorAllergy) {
        Triple("awas", "Tinjau riwayat", "Riwayat reaksi alergi menaikkan peluang reaksi ulang: beri tahu radiolog dan siapkan penanganan reaksi sesuai SOP.")
    } else {
        Triple("ok", "Tanyakan riwayat", "Tanyakan riwayat alergi atau reaksi pada kontras sebelumnya.")
    }
    val allParts = listOf(ginjalStatus, hamilStatus, alergiStatus)
    val conclusion = when {
        allParts.any { it.first == "no" } -> "no" to "Tinjau dengan radiolog"
        allParts.any { it.first == "awas" } -> "awas" to "Tahan dahulu"
        else -> "ok" to "Dapat lanjut sesuai SOP"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "A7. Alur Skrining Sebelum Agen Kontras Gadolinium (T2.6)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            listOf(
                Triple("Ada faktor risiko ginjal (usia lanjut, penyakit ginjal, diabetes, hipertensi, dialisis)", renalRisk) { v: Boolean -> renalRisk = v },
                Triple("eGFR terbaru sudah diperiksa", egfrChecked) { v: Boolean -> egfrChecked = v },
                Triple("eGFR sangat rendah (contoh: di bawah 30) atau cedera ginjal akut", egfrVeryLow) { v: Boolean -> egfrVeryLow = v },
                Triple("Hamil atau mungkin hamil", pregnant) { v: Boolean -> pregnant = v },
                Triple("Pernah mengalami reaksi alergi pada kontras", priorAllergy) { v: Boolean -> priorAllergy = v }
            ).forEach { (label, checked, setter) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { setter(!checked) }
                ) {
                    Checkbox(checked = checked, onCheckedChange = setter)
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }

            val boxItems = listOf(
                Triple("Ginjal", ginjalStatus.first, ginjalStatus.second),
                Triple("Kehamilan", hamilStatus.first, hamilStatus.second),
                Triple("Alergi", alergiStatus.first, alergiStatus.second),
                Triple("Kesimpulan", conclusion.first, conclusion.second)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                boxItems.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { (title, code, subtitle) ->
                            val col = when (code) {
                                "ok" -> Color(0xFF2B8A3E)
                                "awas" -> Color(0xFFD9480F)
                                else -> Color(0xFFC92A2A)
                            }
                            Surface(
                                color = col.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, col, RoundedCornerShape(10.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        title,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        subtitle,
                                        color = col,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "${ginjalStatus.third} ${hamilStatus.third} ${alergiStatus.third}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ============================================================================
// M07B · A1: MEDAN TEPI (GARIS 5 GAUSS) DAN GAYA TARIK PROYEKTIL
// ============================================================================
@Composable
fun M07BFringeFieldAndForceSimulator() {
    var distanceM by remember { mutableFloatStateOf(4.0f) }

    val contours = remember {
        listOf(
            1.5 to 3.0,
            2.3 to 1.0,
            3.0 to 0.5,
            3.6 to 0.3,
            5.0 to 0.1
        )
    }

    fun fieldAt(d: Double): Double {
        var idx = 0
        while (idx < contours.size - 2 && d > contours[idx + 1].first) idx++
        val a = contours[idx]
        val b = contours[idx + 1]
        val slope = (log10(b.second) - log10(a.second)) / (b.first - a.first)
        return 10.0.pow(log10(a.second) + slope * (d - a.first))
    }

    fun relForceRaw(d: Double): Double =
        fieldAt(d) * abs(fieldAt(d + 0.05) - fieldAt(d - 0.05)) / 0.1

    val g0 = remember { relForceRaw(3.0) }
    val bVal = fieldAt(distanceM.toDouble())
    val fRel = relForceRaw(distanceM.toDouble()) / g0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A1. Medan Tepi Berperisai Aktif & Gaya Tarik Proyektil (T2.1–T2.3)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Jarak dari isosenter: ${fmtId(distanceM.toDouble(), 1)} m • Medan tepi: ~${fmtId(bVal, if (bVal < 1.0) 2 else 1)} mT • Gaya tarik relatif: ${fmtId(fRel, 1)}× garis 0,5 mT",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (bVal >= 0.5) Color(0xFFC92A2A) else Color(0xFF2B8A3E)
            )
            Slider(
                value = distanceM,
                onValueChange = { distanceM = it },
                valueRange = 1.0f..6.0f
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                val w = size.width
                val h = size.height
                fun xOf(v: Double): Float = ((v - 1.0) / 5.0 * (w * 0.84f) + w * 0.08f).toFloat()
                fun yOf(b: Double): Float =
                    ((log10(6.0) - log10(b)) / (log10(6.0) - log10(0.04)) * (h * 0.78f) + h * 0.08f).toFloat()

                // 0.5 mT (5 Gauss) line
                val y05 = yOf(0.5)
                drawLine(
                    color = Color(0xFFD9480F),
                    start = Offset(w * 0.08f, y05),
                    end = Offset(w * 0.92f, y05),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                val curve = Path()
                var d = 1.0
                var first = true
                while (d <= 6.001) {
                    val px = xOf(d)
                    val py = yOf(fieldAt(d))
                    if (first) {
                        curve.moveTo(px, py)
                        first = false
                    } else {
                        curve.lineTo(px, py)
                    }
                    d += 0.1
                }
                drawPath(path = curve, color = Color(0xFF1F6FEB), style = Stroke(width = 5f))

                drawCircle(
                    color = if (bVal >= 0.5) Color(0xFFC92A2A) else Color(0xFF2B8A3E),
                    radius = 12f,
                    center = Offset(xOf(distanceM.toDouble()), yOf(bVal))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Pacu jantung (≥ 0,5 mT)" to (bVal >= 0.5),
                    "Kartu kredit (≥ 1 mT)" to (bVal >= 1.0),
                    "Jam tangan (≥ 3 mT)" to (bVal >= 3.0)
                ).forEach { (label, atRisk) ->
                    Surface(
                        color = if (atRisk) Color(0xFFC92A2A) else Color(0xFF2B8A3E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// M07B · A2: TORSI PADA IMPLAN
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun M07BImplantTorqueSimulator() {
    var angleDeg by remember { mutableFloatStateOf(45f) }
    var anchoredInBone by remember { mutableStateOf(false) }
    var aligning by remember { mutableStateOf(false) }

    LaunchedEffect(aligning, anchoredInBone) {
        if (aligning && !anchoredInBone) {
            while (angleDeg > 0f && aligning) {
                delay(20L)
                angleDeg = max(0f, angleDeg - 1.5f)
            }
            aligning = false
        } else {
            aligning = false
        }
    }

    val relTorque = sin(Math.toRadians(angleDeg.toDouble())).toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A2. Torsi pada Implan Ferromagnetik (T2.5)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Sudut klip terhadap garis medan B0: ${angleDeg.toInt()}° • Torsi relatif: ${(relTorque * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = angleDeg,
                onValueChange = {
                    aligning = false
                    angleDeg = it
                },
                valueRange = 0f..90f
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = anchoredInBone,
                    onClick = {
                        aligning = false
                        anchoredInBone = !anchoredInBone
                    },
                    label = {
                        Text(
                            "Implan tertambat pada tulang/semen",
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
                Button(
                    onClick = { if (!anchoredInBone) aligning = true },
                    enabled = !anchoredInBone && angleDeg > 0f,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Biarkan menyelaras",
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                val w = size.width
                val h = size.height
                // Vertical B0 field lines
                for (i in 1..11) {
                    val x = w * i / 12f
                    drawLine(
                        color = Color(0xFF1F6FEB).copy(alpha = 0.35f),
                        start = Offset(x, 12f),
                        end = Offset(x, h - 12f),
                        strokeWidth = 3f
                    )
                }
                val pivot = Offset(w / 2f, h * 0.82f)
                rotate(degrees = angleDeg, pivot = pivot) {
                    drawRoundRect(
                        color = Color(0xFF495057),
                        topLeft = Offset(pivot.x - 10f, pivot.y - h * 0.62f),
                        size = Size(20f, h * 0.62f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
                if (anchoredInBone) {
                    drawRect(
                        color = Color(0xFFD9480F),
                        topLeft = Offset(pivot.x - 36f, pivot.y),
                        size = Size(72f, 22f)
                    )
                } else {
                    drawCircle(
                        color = Color(0xFF0B7285),
                        radius = 12f,
                        center = pivot
                    )
                }
            }

            LinearProgressIndicator(
                progress = { relTorque },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
            )

            Text(
                text = if (angleDeg.toInt() == 0) {
                    "Klip sejajar dengan garis medan: tidak ada torsi."
                } else if (anchoredInBone) {
                    "Implan tertambat pada tulang atau semen (mis. paku ortopedi): kecil kemungkinan berputar."
                } else {
                    "Implan memanjang di jaringan lunak (mis. klip aneurisma otak): bekerja seperti tuas, cenderung berputar sampai sejajar medan dan merusak jaringan sekitarnya."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ============================================================================
// M07B · A3: SKALA MEDAN DAN EFEK BIOLOGIS SEMENTARA
// ============================================================================
@Composable
fun M07BFieldScaleEffectsSimulator() {
    var selectedIdx by remember { mutableIntStateOf(2) }
    var slowTable by remember { mutableStateOf(false) }

    val scaleItems = listOf(
        Triple("Bumi (0,05 mT)", "Medan bumi sangat kecil (0,05 mT). Puluhan spesies hewan dan burung memakainya untuk menentukan arah.", false),
        Triple("1,5 T", "Efek sementara terutama dibahas pada sistem medan sangat tinggi, bukan pada kekuatan klinis 1,5 T ini.", false),
        Triple("3 T", "Pada rentang 3 sampai 7 T sebagian orang melaporkan rasa logam di mulut dan pusing saat bergerak melewati medan tepi.", false),
        Triple("7 T", "Vertigo (pusing, mual, nistagmus) akibat gaya Lorentz pada ion cairan telinga dalam terutama muncul pada ≥ 7 T. Biasanya pulih ~15 menit setelah keluar.", true),
        Triple("8 T (ICNIRP)", "ICNIRP menyatakan tidak ada bukti efek kesehatan serius pada paparan seluruh tubuh sampai 8 T.", true)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A3. Skala Kuat Medan B0 & Efek Biologis Sementara (T2.4)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scaleItems.forEachIndexed { idx, item ->
                    FilterChip(
                        selected = selectedIdx == idx,
                        onClick = { selectedIdx = idx },
                        label = { Text(item.first) }
                    )
                }
            }
            FilterChip(
                selected = slowTable,
                onClick = { slowTable = !slowTable },
                label = { Text("Meja pasien digerakkan perlahan: ${if (slowTable) "YA" else "TIDAK"}") }
            )

            val item = scaleItems[selectedIdx]
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${item.first}: ${item.second}" +
                        if (item.third) {
                            if (slowTable) " Meja lambat: laju perubahan fluks menurun sehingga keparahan vertigo jauh lebih ringan."
                            else " Makin cepat meja bergerak melewati medan tepi, makin berat vertigo yang dirasakan pasien."
                        } else "",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================================
// M07B · A4: OKSIGEN RUANGAN DAN EKSPANSI HELIUM CAIR SAAT QUENCH
// ============================================================================
@Composable
fun M07BOxygenAndHeliumExpansionSimulator() {
    var oxygenPct by remember { mutableFloatStateOf(21.0f) }
    var liquidHeliumL by remember { mutableFloatStateOf(1600f) }

    val gasLiters = (liquidHeliumL * 754f).toLong()
    val gasMillionLiters = gasLiters / 1_000_000.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A4. Kadar Oksigen Ruang & Ekspansi Gas Helium Quench (T2.7, T3.1)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                "Kadar oksigen ruang magnet: ${fmtId(oxygenPct.toDouble(), 1)}% (Normal udara: 21%; Rentang OSHA: 19,5–23,5%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = oxygenPct,
                onValueChange = { oxygenPct = (it * 2f).roundToInt() / 2f },
                valueRange = 0f..24f
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val w = size.width
                val h = size.height
                fun xOf(v: Float) = (v / 24f) * w
                val bands = listOf(
                    Triple(0f, 6f, Color(0xFFC92A2A)),
                    Triple(6f, 8f, Color(0xFFD9480F)),
                    Triple(8f, 19.5f, Color(0xFFF2C94C)),
                    Triple(19.5f, 23.5f, Color(0xFF2B8A3E)),
                    Triple(23.5f, 24f, Color.LightGray)
                )
                bands.forEach { (a, b, col) ->
                    drawRect(
                        color = col,
                        topLeft = Offset(xOf(a), 0f),
                        size = Size(xOf(b) - xOf(a), h)
                    )
                }
                val markerX = xOf(oxygenPct)
                drawLine(
                    color = Color.Black,
                    start = Offset(markerX, 0f),
                    end = Offset(markerX, h),
                    strokeWidth = 6f
                )
            }

            val o2Status = when {
                oxygenPct in 19.5f..23.5f -> "Rentang optimal untuk aktivitas normal (menurut OSHA)."
                oxygenPct > 23.5f -> "Di atas rentang optimal."
                oxygenPct >= 8.0f -> "Di bawah 19,5%: alarm sensor oksigen berbunyi, persepsi dan penilaian terganggu sehingga sulit menyelamatkan diri."
                oxygenPct >= 6.0f -> "Di bawah 8%: sangat mungkin pingsan."
                else -> "Di bawah 6%: pingsan dalam ~10 detik, gagal jantung dan kematian dalam 2 sampai 4 menit!"
            }
            Text(
                o2Status,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (oxygenPct < 19.5f) Color(0xFFC92A2A) else Color(0xFF2B8A3E)
            )

            HorizontalDivider()

            Text(
                "Volume Helium Cair di Kriostat: ${liquidHeliumL.toInt()} L → Mengembang menjadi ${String.format(Locale("id", "ID"), "%,d", gasLiters)} L gas (${fmtId(gasMillionLiters, 2)} juta liter, rasio 1:754)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = liquidHeliumL,
                onValueChange = { liquidHeliumL = (it / 50f).roundToInt() * 50f },
                valueRange = 0f..1600f
            )
            Text(
                "Tiap 1 L helium cair (4 K) menguap menjadi 754 L gas. Bila pipa quench tersumbat/pecah, kenaikan tekanan ruangan menekan pintu yang membuka ke dalam dan titik buang luar wajib ≥ 8 m dari akses umum.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// M07B · A5: EFEK LENZ — KONDUKTOR JATUH MELALUI MEDAN
// ============================================================================
@Composable
fun M07BLenzEffectDropSimulator() {
    val fields = listOf(0.0, 1.5, 3.0, 7.0)
    val materials = listOf(
        "Plastik (non-konduktor)" to 0.0,
        "Aluminium" to 1.0,
        "Tembaga" to 1.6
    )

    var fieldIdx by remember { mutableIntStateOf(2) } // 3 T
    var matIdx by remember { mutableIntStateOf(2) }   // Tembaga
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    var dropping by remember { mutableStateOf(false) }

    val g = 9.8
    val heightM = 0.8
    val dampA = 3.0 * materials[matIdx].second * fields[fieldIdx].pow(2.0)

    fun yPosAt(a: Double, t: Double): Double =
        if (a <= 0.0) 0.5 * g * t * t
        else (g / a) * t - (g / (a * a)) * (1.0 - exp(-a * t))

    fun fallDuration(a: Double): Double {
        var t = 0.0
        while (yPosAt(a, t) < heightM && t < 20.0) t += 0.005
        return t
    }

    val tFree = remember { fallDuration(0.0) }
    val tCond = fallDuration(dampA)

    LaunchedEffect(dropping, fieldIdx, matIdx) {
        if (dropping) {
            elapsedSec = 0f
            val maxT = max(tFree, tCond).toFloat()
            while (elapsedSec < maxT && dropping) {
                delay(30L)
                elapsedSec += 0.03f
            }
            dropping = false
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A5. Efek Lenz: Konduktor Non-Ferrous Jatuh di Medan Statis (T2.6)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fields.forEachIndexed { idx, b ->
                    FilterChip(
                        selected = fieldIdx == idx,
                        onClick = {
                            dropping = false
                            fieldIdx = idx
                            elapsedSec = 0f
                        },
                        label = { Text(if (b == 0.0) "Tanpa medan" else "${fmtId(b, if (b % 1.0 == 0.0) 0 else 1)} T") }
                    )
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                materials.forEachIndexed { idx, m ->
                    FilterChip(
                        selected = matIdx == idx,
                        onClick = {
                            dropping = false
                            matIdx = idx
                            elapsedSec = 0f
                        },
                        label = { Text(m.first, fontSize = 12.sp) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { dropping = true }) {
                    Text("Jatuhkan Blok")
                }
                OutlinedButton(onClick = {
                    dropping = false
                    elapsedSec = 0f
                }) {
                    Text("Reset")
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                val w = size.width
                val h = size.height
                val freeRatio = (yPosAt(0.0, elapsedSec.toDouble()) / heightM).coerceIn(0.0, 1.0).toFloat()
                val condRatio = (yPosAt(dampA, elapsedSec.toDouble()) / heightM).coerceIn(0.0, 1.0).toFloat()

                val topY = h * 0.1f
                val travelH = h * 0.68f

                // Ground line
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(w * 0.08f, topY + travelH + 26f),
                    end = Offset(w * 0.92f, topY + travelH + 26f),
                    strokeWidth = 4f
                )

                // Left block: free fall
                drawRoundRect(
                    color = Color(0xFF868E96),
                    topLeft = Offset(w * 0.2f, topY + freeRatio * travelH),
                    size = Size(w * 0.22f, 26f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Right block: selected material
                val matColor = when (matIdx) {
                    0 -> Color(0xFF868E96)
                    1 -> Color(0xFFADB5BD)
                    else -> Color(0xFFC2763C)
                }
                drawRoundRect(
                    color = matColor,
                    topLeft = Offset(w * 0.58f, topY + condRatio * travelH),
                    size = Size(w * 0.22f, 26f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            Text(
                text = if (dampA <= 0.0) {
                    "Tanpa arus induksi (bahan non-konduktor atau tanpa medan): kedua blok jatuh bebas bersamaan (~${fmtId(tFree, 2)} dtk untuk 0,8 m)."
                } else {
                    "Hukum Lenz: arus eddy induksi pada ${materials[matIdx].first} melawan gerak di medan ${fields[fieldIdx]} T. Waktu jatuh melambat menjadi ~${fmtId(tCond, 2)} dtk (dibanding jatuh bebas ${fmtId(tFree, 2)} dtk)."
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================================
// M07B · A6: SKENARIO DARURAT (QUENCH VS KELUARKAN DARI ZONA IV VS EVAKUASI)
// ============================================================================
@Composable
fun M07BEmergencyActionSimulator(
    onComplete: () -> Unit = {}
) {
    val scenarios = remember {
        listOf(
            Triple(
                "Seorang pasien mendadak tidak sadar dan tidak bernapas di Zona IV. Tidak ada benda yang menjepitnya.",
                "keluarkan",
                "Pasien yang sakit mendadak dikeluarkan dan ditangani di luar Zona IV sambil memanggil tim resusitasi. Quench tidak dipakai untuk itu."
            ),
            Triple(
                "Seorang porter terjepit antara tabung oksigen ferromagnetik dan magnet, dan tidak bisa dilepaskan.",
                "quench",
                "Orang terjepit benda ferromagnetik adalah ancaman nyawa akibat medan, alasan yang dibenarkan untuk quench. Setelah itu ikuti prosedur gas helium."
            ),
            Triple(
                "Pasien panik karena klaustrofobia dan memohon keluar dari bore.",
                "keluarkan",
                "Hentikan pemindaian dan keluarkan pasien. Tidak ada ancaman akibat medan, jadi quench tidak dipakai."
            ),
            Triple(
                "Alarm oksigen berbunyi setelah pipa quench dilaporkan rusak, dan seseorang tampak lemas di dalam ruang magnet.",
                "evakuasi",
                "Helium menggantikan oksigen dan penolong pun bisa pingsan. Evakuasi, jangan masuk sendirian, dan minta bantuan terlatih. Pastikan keselamatan diri sendiri dahulu."
            ),
            Triple(
                "Seorang staf pusing berputar (vertigo) setelah melewati medan tepi magnet berkekuatan sangat tinggi.",
                "keluarkan",
                "Keluarkan dari medan dan biarkan pulih (~15 menit). Ini efek sementara, bukan ancaman nyawa, jadi quench tidak dipakai."
            )
        )
    }

    var idx by remember { mutableIntStateOf(0) }
    var choice by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A6. Skenario Darurat: Quench vs Keluarkan vs Evakuasi (T3.2)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            val current = scenarios[idx]
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Skenario Darurat ${idx + 1} dari ${scenarios.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(current.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "quench" to "Quench magnet",
                    "keluarkan" to "Keluarkan dan tangani di luar Zona IV",
                    "evakuasi" to "Evakuasi dan panggil bantuan"
                ).forEach { (k, label) ->
                    val isSel = choice == k
                    val isOk = k == current.second
                    OutlinedButton(
                        onClick = {
                            choice = k
                            if (idx == scenarios.size - 1 && isOk) onComplete()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = when {
                                !isSel -> Color.Transparent
                                isOk -> Color(0xFF2B8A3E).copy(alpha = 0.16f)
                                else -> Color(0xFFC92A2A).copy(alpha = 0.16f)
                            }
                        )
                    ) {
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }

            choice?.let { c ->
                val ok = c == current.second
                Text(
                    text = (if (ok) "Tepat! " else "Belum tepat. ") + current.third,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ok) Color(0xFF2B8A3E) else Color(0xFFC92A2A),
                    fontWeight = FontWeight.SemiBold
                )
                Button(onClick = {
                    idx = (idx + 1) % scenarios.size
                    choice = null
                }) {
                    Text("Skenario Berikutnya →")
                }
            }
        }
    }
}

// ============================================================================
// M07C · A1: KALKULATOR SAR (3 SUB-PANEL: MODE SAR, TABEL 10.1, MENURUNKAN SAR)
// ============================================================================
@Composable
fun M07CSarCalculatorSimulator() {
    var tab by remember { mutableIntStateOf(0) }
    var bodyRegion by remember { mutableStateOf("tubuh") } // "tubuh" or "kepala"
    var opMode by remember { mutableStateOf("normal") }   // "normal" or "tingkat1"
    var b0Init by remember { mutableDoubleStateOf(1.5) }
    var b0New by remember { mutableDoubleStateOf(3.0) }
    var sarInit by remember { mutableFloatStateOf(1.0f) }
    var flipScale by remember { mutableFloatStateOf(1.0f) }

    // Sub-panel 2: Tabel 10.1
    var tableMode by remember { mutableStateOf("normal") }
    var tableRegionIdx by remember { mutableIntStateOf(0) }
    var avgSar6Min by remember { mutableFloatStateOf(1.5f) }
    var peakSar10Sec by remember { mutableFloatStateOf(3.0f) }

    // Sub-panel 3: Menurunkan SAR
    var redB0 by remember { mutableDoubleStateOf(3.0) }
    var trScale by remember { mutableFloatStateOf(1.0f) }
    var sliceScale by remember { mutableFloatStateOf(1.0f) }
    var refFlipScale by remember { mutableFloatStateOf(1.0f) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A1. Kalkulator SAR & Pemeriksa Tabel 10.1 IEC (T2.2, T2.3, T3.1, T3.3, T3.4)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Kalkulator mode SAR") })
                FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Pemeriksa Tabel 10.1") })
                FilterChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text("Menurunkan SAR") })
            }

            when (tab) {
                0 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = bodyRegion == "tubuh",
                            onClick = { bodyRegion = "tubuh" },
                            label = { Text("Seluruh tubuh") }
                        )
                        FilterChip(
                            selected = bodyRegion == "kepala",
                            onClick = { bodyRegion = "kepala" },
                            label = { Text("Kepala") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = opMode == "normal",
                            onClick = { opMode = "normal" },
                            label = { Text("Mode Normal") }
                        )
                        FilterChip(
                            selected = opMode == "tingkat1",
                            onClick = { opMode = "tingkat1" },
                            label = { Text("Mode Tingkat Satu") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = b0Init == 1.5 && b0New == 1.5,
                            onClick = { b0Init = 1.5; b0New = 1.5 },
                            label = { Text("1,5 T → 1,5 T") }
                        )
                        FilterChip(
                            selected = b0Init == 1.5 && b0New == 3.0,
                            onClick = { b0Init = 1.5; b0New = 3.0 },
                            label = { Text("1,5 T → 3 T (4×)") }
                        )
                        FilterChip(
                            selected = b0Init == 3.0 && b0New == 1.5,
                            onClick = { b0Init = 3.0; b0New = 1.5 },
                            label = { Text("3 T → 1,5 T") }
                        )
                    }

                    Text("SAR Awal: ${fmtId(sarInit.toDouble(), 1)} W/kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = sarInit, onValueChange = { sarInit = it }, valueRange = 0.2f..3.5f)

                    Text("Skala Sudut Flip: ×${fmtId(flipScale.toDouble(), 1)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = flipScale, onValueChange = { flipScale = it }, valueRange = 0.5f..1.5f)

                    val limit = if (bodyRegion == "tubuh") {
                        if (opMode == "normal") 2.0 else 4.0
                    } else 3.2
                    val newSar = sarInit * (b0New / b0Init).pow(2.0) * flipScale.toDouble().pow(2.0)
                    val pct = (newSar / limit * 100.0)
                    val isSafe = pct <= 100.0

                    LinearProgressIndicator(
                        progress = { (pct / 100.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = if (isSafe) Color(0xFF2B8A3E) else Color(0xFFC92A2A)
                    )
                    Text(
                        "SAR Baru = ${fmtId(newSar, 2)} W/kg • Batas ${if (opMode == "normal") "Mode Normal" else "Tingkat Satu"}: ${fmtId(limit, 1)} W/kg (${fmtId(pct, 0)}%) — ${if (isSafe) "DI BAWAH BATAS" else "MELEWATI BATAS!"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSafe) Color(0xFF2B8A3E) else Color(0xFFC92A2A)
                    )
                }

                1 -> {
                    val regions = listOf(
                        Triple("Seluruh tubuh (koil volume)", 2.0, 4.0),
                        Triple("Kepala (koil volume)", 3.2, 3.2),
                        Triple("Koil transmit lokal: kepala/badan", 10.0, 20.0),
                        Triple("Koil transmit lokal: ekstremitas", 20.0, 40.0)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = tableMode == "normal", onClick = { tableMode = "normal" }, label = { Text("Mode Normal") })
                        FilterChip(selected = tableMode == "tingkat1", onClick = { tableMode = "tingkat1" }, label = { Text("Tingkat Satu") })
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        regions.forEachIndexed { i, r ->
                            FilterChip(
                                selected = tableRegionIdx == i,
                                onClick = { tableRegionIdx = i },
                                label = { Text(r.first, fontSize = 11.sp) }
                            )
                        }
                    }

                    val reg = regions[tableRegionIdx]
                    val lim6Min = if (tableMode == "normal") reg.second else reg.third
                    val lim10Sec = lim6Min * 2.0

                    Text("SAR Rata-rata 6 menit: ${fmtId(avgSar6Min.toDouble(), 1)} W/kg (Batas: ${fmtId(lim6Min, 1)} W/kg)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = avgSar6Min, onValueChange = { avgSar6Min = it }, valueRange = 0.5f..45f)

                    Text("SAR Puncak 10 detik: ${fmtId(peakSar10Sec.toDouble(), 1)} W/kg (Batas 2×: ${fmtId(lim10Sec, 1)} W/kg)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = peakSar10Sec, onValueChange = { peakSar10Sec = it }, valueRange = 0.5f..85f)

                    val ok = avgSar6Min <= lim6Min && peakSar10Sec <= lim10Sec
                    val maxTemp = if (tableMode == "normal") "naik ≤ 0,5 °C (maks 39 °C)" else "naik ≤ 1,0 °C (maks 40 °C)"
                    Text(
                        "Status Tabel 10.1: ${if (ok) "MEMENUHI BATAS" else "MELEWATI BATAS"} • Batas suhu inti tubuh: $maxTemp.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (ok) Color(0xFF2B8A3E) else Color(0xFFC92A2A)
                    )
                }

                2 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = redB0 == 3.0, onClick = { redB0 = 3.0 }, label = { Text("B0 3 T") })
                        FilterChip(selected = redB0 == 1.5, onClick = { redB0 = 1.5 }, label = { Text("Turunkan ke 1,5 T") })
                    }
                    Text("Skala TR: ×${fmtId(trScale.toDouble(), 1)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = trScale, onValueChange = { trScale = it }, valueRange = 1.0f..2.0f)

                    Text("Skala Jumlah Irisan: ×${fmtId(sliceScale.toDouble(), 1)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = sliceScale, onValueChange = { sliceScale = it }, valueRange = 0.5f..1.0f)

                    Text("Skala Sudut Flip Refokus: ×${fmtId(refFlipScale.toDouble(), 1)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(value = refFlipScale, onValueChange = { refFlipScale = it }, valueRange = 0.5f..1.0f)

                    val relSar = (redB0 / 3.0).pow(2.0) * refFlipScale.toDouble().pow(2.0) * sliceScale / trScale
                    val tradeoffs = buildList {
                        if (redB0 < 3.0) add("SNR turun & NSA lebih besar membuat waktu minimal 2× lipat")
                        if (trScale > 1.05f) add("pembobotan citra berubah (terutama T1)")
                        if (sliceScale < 0.95f) add("cakupan anatomi berkurang atau butuh akuisisi tambahan")
                        if (refFlipScale < 0.95f) add("sinyal sedikit menurun namun umumnya tetap diagnostik")
                    }
                    Text(
                        "SAR Relatif: ${(relSar * 100).roundToInt()}% dari kondisi awal. Kompromi: ${if (tradeoffs.isEmpty()) "belum ada perubahan parameter." else tradeoffs.joinToString("; ") + "."}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ============================================================================
// M07C · A2: LUKA BAKAR — EFEK ANTENA PADA KAWAT & LOOP KONDUKTIF TUBUH
// ============================================================================
@Composable
fun M07CAntennaAndLoopBurnSimulator() {
    var subMode by remember { mutableIntStateOf(0) } // 0 = Efek antena, 1 = Loop konduktif
    var b0Val by remember { mutableDoubleStateOf(1.5) }
    var mediumKey by remember { mutableStateOf("d") } // "u" = Udara (1), "a" = Air (81), "d" = Darah (67)
    var wireLenCm by remember { mutableFloatStateOf(29f) }

    var handsOverhead by remember { mutableStateOf(false) }
    var handsTouchThigh by remember { mutableStateOf(true) }
    var heelsTouch by remember { mutableStateOf(true) }
    var insulationPads by remember { mutableStateOf(false) }

    val epsMap = mapOf("u" to 1.0, "a" to 81.0, "d" to 67.0)
    val freqMhz = if (b0Val == 1.5) 63.87 else 127.74
    val wavelengthCm = (299.79 / (freqMhz * sqrt(epsMap[mediumKey] ?: 67.0))) * 100.0
    val halfWaveCm = wavelengthCm / 2.0
    val ratio = wireLenCm / halfWaveCm
    val heatFactor = max(
        exp(-((ratio - 1.0) / 0.18).pow(2.0)),
        exp(-((ratio - 3.0) / 0.18).pow(2.0))
    ).toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A2. Luka Bakar: Efek Antena Kawat & Loop Konduktif Tubuh (T2.4, T2.5)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = subMode == 0,
                    onClick = { subMode = 0 },
                    label = { Text("Efek antena pada kawat", fontSize = 11.sp, maxLines = 1, softWrap = false) }
                )
                FilterChip(
                    selected = subMode == 1,
                    onClick = { subMode = 1 },
                    label = { Text("Loop konduktif pada tubuh", fontSize = 11.sp, maxLines = 1, softWrap = false) }
                )
            }

            if (subMode == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = b0Val == 1.5, onClick = { b0Val = 1.5 }, label = { Text("B0 1,5 T", fontSize = 11.sp) })
                    FilterChip(selected = b0Val == 3.0, onClick = { b0Val = 3.0 }, label = { Text("B0 3 T", fontSize = 11.sp) })
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("u" to "Udara/Vakum (εr=1)", "a" to "Air (εr=81)", "d" to "Darah (εr=67)").forEach { (k, label) ->
                        FilterChip(
                            selected = mediumKey == k,
                            onClick = { mediumKey = k },
                            label = { Text(label, fontSize = 11.sp, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Text(
                    "Panjang kawat: ${wireLenCm.toInt()} cm • λ medium ≈ ${wavelengthCm.roundToInt()} cm • Panjang kritis λ/2 ≈ ${halfWaveCm.roundToInt()} cm",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Slider(value = wireLenCm, onValueChange = { wireLenCm = it }, valueRange = 5f..300f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    val w = size.width
                    val h = size.height
                    val xStart = w * 0.08f
                    val xEnd = xStart + (wireLenCm / 300f) * (w * 0.84f)
                    val midY = h * 0.55f

                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(xStart, midY),
                        end = Offset(xEnd, midY),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    val wave = Path()
                    val steps = 80
                    for (i in 0..steps) {
                        val frac = i / steps.toFloat()
                        val cm = frac * wireLenCm
                        val px = xStart + frac * (xEnd - xStart)
                        val py = midY - (0.25f + 0.75f * heatFactor) * (h * 0.35f) *
                            cos(2.0 * PI * cm / wavelengthCm).toFloat()
                        if (i == 0) wave.moveTo(px, py) else wave.lineTo(px, py)
                    }
                    drawPath(path = wave, color = Color(0xFF1F6FEB), style = Stroke(width = 4f))

                    // Antinode heating at wire tips
                    val tipRadius = 10f + 24f * heatFactor
                    drawCircle(
                        color = Color(0xFFC92A2A).copy(alpha = 0.25f + 0.65f * heatFactor),
                        radius = tipRadius,
                        center = Offset(xStart, midY)
                    )
                    drawCircle(
                        color = Color(0xFFC92A2A).copy(alpha = 0.25f + 0.65f * heatFactor),
                        radius = tipRadius,
                        center = Offset(xEnd, midY)
                    )
                }

                Text(
                    "Pemanasan relatif ujung kawat (antinode): ${(heatFactor * 100).roundToInt()}%. " +
                        if (heatFactor > 0.7f) "BAHAYA RESONANSI! Panjang kawat mendekati λ/2 sehingga ujung kawat dapat memanas hingga ~80 °C."
                        else "Jauh dari panjang resonansi dipol λ/2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (heatFactor > 0.7f) Color(0xFFC92A2A) else MaterialTheme.colorScheme.onSurface
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = handsOverhead,
                        onClick = { handsOverhead = !handsOverhead },
                        label = { Text("Tangan di atas kepala bertemu", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = handsTouchThigh,
                        onClick = { handsTouchThigh = !handsTouchThigh },
                        label = { Text("Tangan menyentuh paha", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = heelsTouch,
                        onClick = { heelsTouch = !heelsTouch },
                        label = { Text("Tumit bersentuhan", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = insulationPads,
                        onClick = { insulationPads = !insulationPads },
                        label = { Text("Pasang Bantalan Isolasi", fontSize = 11.sp) }
                    )
                }

                val activeContacts = (if (handsOverhead) 1 else if (handsTouchThigh) 2 else 0) + (if (heelsTouch) 1 else 0)
                Surface(
                    color = (if (activeContacts == 0 || insulationPads) Color(0xFF2B8A3E) else Color(0xFFC92A2A)).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when {
                            activeContacts == 0 -> "Tidak ada loop konduktif alami yang terbentuk pada postur ini."
                            insulationPads -> "Bantalan isolasi memutus loop konduktif ($activeContacts titik terlindungi), sehingga arus induksi tidak terkonsentrasi di kulit."
                            else -> "PERINGATAN: $activeContacts titik kontak kulit-ke-kulit membentuk loop tertutup! Arus induksi terkonsentrasi di area sempit dan memicu luka bakar tajam."
                        },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (activeContacts == 0 || insulationPads) Color(0xFF2B8A3E) else Color(0xFFC92A2A)
                    )
                }
            }
        }
    }
}

// ============================================================================
// M07C · A3: AMBANG STIMULASI SARAF TEPI (PNS) OLEH GRADIEN
// ============================================================================
@Composable
fun M07CPnsThresholdSimulator() {
    var gradientPct by remember { mutableFloatStateOf(80f) }

    val thresholds = listOf(
        "Saraf tepi (PNS)" to 100f,
        "Napas (studi anjing)" to 300f,
        "Jantung (studi anjing)" to 900f
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A3. Ambang Stimulasi Saraf Tepi (PNS) oleh Gradien dB/dt (T2.6)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mode normal (80%)" to 80f, "Batas maks (100%)" to 100f, "Mode riset (120%)" to 120f).forEach { (label, v) ->
                    FilterChip(
                        selected = gradientPct.roundToInt() == v.roundToInt(),
                        onClick = { gradientPct = v },
                        label = { Text(label, fontSize = 11.sp, maxLines = 1, softWrap = false) }
                    )
                }
            }

            Text(
                "Tingkat dB/dt gradien: ${gradientPct.roundToInt()}% dari ambang rata-rata saraf tepi",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = gradientPct,
                onValueChange = { gradientPct = (it / 10f).roundToInt() * 10f },
                valueRange = 0f..1000f
            )

            thresholds.forEach { (name, th) ->
                val exceeded = gradientPct >= th
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Ambang: ${th.toInt()}% ${if (exceeded) "(TERLAMPAUI)" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (exceeded) Color(0xFFC92A2A) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (gradientPct / th).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = if (exceeded) Color(0xFFC92A2A) else Color(0xFF1F6FEB)
                    )
                }
            }

            Text(
                text = when {
                    gradientPct <= 80f -> "Di dalam batas mode normal (≤ 80%): tidak ada stimulasi saraf tepi."
                    gradientPct < 100f -> "Di atas mode normal, tetapi belum melewati ambang rata-rata saraf tepi (100%)."
                    else -> "Melewati ambang saraf tepi: pasien merasakan ketukan, kesemutan, atau kedutan otot. Produsen membatasi slew rate (~200 T/m/s) agar jauh dari ambang napas (300%) dan jantung (900%)."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ============================================================================
// M07C · A4: KALKULATOR BISING AKUSTIK DAN LAMA PAPARAN HARIAN
// ============================================================================
@Composable
fun M07CAcousticNoiseCalculatorSimulator() {
    val examples = listOf(
        "Napas (10 dB)" to 10f,
        "Bisikan (20 dB)" to 20f,
        "Percakapan normal (60 dB)" to 60f,
        "Batas ICNIRP pelindung telinga (80 dB)" to 80f,
        "Patokan 8 jam OSHA (85 dB)" to 85f,
        "Bore MRI 1,5 T (100 dB)" to 100f,
        "Bore MRI 3 T EPI (115 dB)" to 115f,
        "Ambang nyeri gendang telinga (140 dB)" to 140f
    )
    var noiseDb by remember { mutableFloatStateOf(100f) }

    val relIntensity = 2.0.pow((noiseDb - 85.0) / 3.0)
    val allowedMinutes = 480.0 / relIntensity
    val durationText = when {
        allowedMinutes >= 60.0 -> "${fmtId(allowedMinutes / 60.0, 1)} jam"
        allowedMinutes >= 1.0 -> "${fmtId(allowedMinutes, 0)} menit"
        else -> "${fmtId(allowedMinutes * 60.0, 0)} detik"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A4. Kalkulator Bising Akustik & Batas Paparan (+3 dB = 2× Intensitas) (T2.7)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                examples.forEach { (label, db) ->
                    FilterChip(
                        selected = noiseDb.roundToInt() == db.roundToInt(),
                        onClick = { noiseDb = db },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Text(
                "Tingkat bunyi: ${noiseDb.roundToInt()} dB • Intensitas relatif terhadap 85 dB: ${fmtId(relIntensity, if (relIntensity < 10) 2 else 0)}×",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = noiseDb,
                onValueChange = { noiseDb = it.roundToInt().toFloat() },
                valueRange = 10f..170f
            )

            LinearProgressIndicator(
                progress = { (noiseDb / 170f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = when {
                    noiseDb >= 140f -> Color(0xFFC92A2A)
                    noiseDb >= 85f -> Color(0xFFD9480F)
                    noiseDb >= 80f -> Color(0xFFF2C94C)
                    else -> Color(0xFF2B8A3E)
                }
            )

            Text(
                text = "${noiseDb.roundToInt()} dB: " +
                    (if (noiseDb >= 85f) "Batas paparan harian tanpa pelindung sekitar $durationText. " else "Di bawah patokan 85 dB (8 jam). ") +
                    when {
                        noiseDb >= 140f -> "Nyeri dan berisiko memecahkan gendang telinga!"
                        noiseDb >= 80f -> "ICNIRP & ACR mewajibkan pelindung telinga (earplug/headphone). Sekuens senyap memakai gradien langkah kecil."
                        noiseDb >= 75f -> "Mulai mengganggu kenyamanan pasien."
                        else -> "Relatif aman."
                    },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================================
// M07C · A5: PANJANG GELOMBANG RF DI JARINGAN (1,5 T VS 3 T) & POLA B1
// ============================================================================
@Composable
fun M07CRfWavelengthInTissueSimulator() {
    var b0 by remember { mutableDoubleStateOf(1.5) }
    var bodyWidthCm by remember { mutableFloatStateOf(35f) }

    val freqMhz = if (b0 == 1.5) 63.87 else 127.74
    val lambdaTissueCm = (299.79 / (freqMhz * sqrt(67.0))) * 100.0
    val ratio = bodyWidthCm / lambdaTissueCm
    val modAmp = min(0.6, 0.5 * ratio.pow(2.0)).toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "A5. Panjang Gelombang RF di Jaringan: 1,5 T vs 3 T & Pola B1 (T3.2)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = b0 == 1.5, onClick = { b0 = 1.5 }, label = { Text("B0 1,5 T (λ ≈ 58 cm)") })
                FilterChip(selected = b0 == 3.0, onClick = { b0 = 3.0 }, label = { Text("B0 3 T (λ ≈ 29 cm)") })
            }

            Text(
                "Lebar tubuh: ${bodyWidthCm.toInt()} cm (${fmtId(ratio, 1)}× panjang gelombang jaringan)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Slider(value = bodyWidthCm, onValueChange = { bodyWidthCm = it.roundToInt().toFloat() }, valueRange = 20f..50f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                val w = size.width
                val h = size.height
                val rx = (bodyWidthCm / 50f) * (w * 0.42f)
                val x0 = w / 2f - rx
                val strips = 40
                val stripW = (rx * 2f) / strips

                for (i in 0 until strips) {
                    val xCm = ((i + 0.5f) / strips - 0.5f) * bodyWidthCm
                    val v = 1f + modAmp * cos(2.0 * PI * xCm / lambdaTissueCm).toFloat()
                    val alpha = (0.15f + 0.7f * ((v - 0.4f) / 1.2f)).coerceIn(0.1f, 0.9f)
                    drawRect(
                        color = Color(0xFF2F6BFF).copy(alpha = alpha),
                        topLeft = Offset(x0 + i * stripW, h * 0.1f),
                        size = Size(stripW + 1f, h * 0.52f)
                    )
                }

                // Profile curve
                val path = Path()
                for (i in 0..strips) {
                    val xCm = (i.toFloat() / strips - 0.5f) * bodyWidthCm
                    val v = 1f + modAmp * cos(2.0 * PI * xCm / lambdaTissueCm).toFloat()
                    val px = x0 + i * stripW
                    val py = h * 0.82f - (v - 1f) * (h * 0.25f)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(path = path, color = Color(0xFF1F6FEB), style = Stroke(width = 5f))
            }

            Text(
                text = "Pada ${if (b0 == 1.5) "1,5 T" else "3 T"} (${fmtId(freqMhz, 1)} MHz), permitivitas relatif darah (εr ≈ 67) memendekkan panjang gelombang RF menjadi ~${lambdaTissueCm.roundToInt()} cm. " +
                    if (ratio > 0.9) "Lebar tubuh sebanding dengan λ: muncul gelombang berdiri (area terang-gelap B1) dan distribusi SAR tidak merata."
                    else "Lebar tubuh lebih kecil dari λ: distribusi B1 relatif lebih merata.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
