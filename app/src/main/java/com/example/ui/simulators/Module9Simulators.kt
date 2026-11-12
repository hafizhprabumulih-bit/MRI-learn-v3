package com.example.ui.simulators

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MriRepository
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
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private data class ProtocolParams(
    val trMs: Int = 500,
    val fovMm: Int = 240,
    val nf: Int = 256,
    val np: Int = 192,
    val tkMm: Float = 5.0f,
    val nex: Int = 2,
    val bwHzPx: Int = 200,
    val etl: Int = 1,
    val ns: Int = 1,
    val rFactor: Int = 1
)

private data class ProtocolMetrics(
    val pxMm: Float,
    val pyMm: Float,
    val voxelVolMm3: Float,
    val taSec: Float,
    val rawSnr: Float
)

private fun computeProtocolMetrics(p: ProtocolParams): ProtocolMetrics {
    val px = p.fovMm.toFloat() / p.nf.coerceAtLeast(1)
    val py = p.fovMm.toFloat() / p.np.coerceAtLeast(1)
    val vol = px * py * p.tkMm
    val ta = (p.trMs / 1000f) * p.np * p.nex * p.ns / (p.etl.coerceAtLeast(1) * p.rFactor.coerceAtLeast(1))
    val snr = vol * sqrt(
        (p.nex.toFloat() * p.np.toFloat() * p.ns.toFloat()) /
            (p.bwHzPx.coerceAtLeast(1).toFloat() * p.rFactor.coerceAtLeast(1).toFloat())
    )
    return ProtocolMetrics(px, py, vol, ta, snr)
}

private val BASE_PROTOCOL = ProtocolParams()
private val BASE_METRICS = computeProtocolMetrics(BASE_PROTOCOL)
private const val MISSION_PRESET_NAME = "Kasus: pasien gelisah"

private val S1_PRESETS: List<Pair<String, ProtocolParams>> = listOf(
    "Awal" to BASE_PROTOCOL,
    "Maks SNR" to BASE_PROTOCOL.copy(nex = 4, tkMm = 8.0f, bwHzPx = 100, nf = 192, np = 160),
    "Maks resolusi" to BASE_PROTOCOL.copy(nf = 512, np = 384, tkMm = 3.0f),
    "Tercepat" to BASE_PROTOCOL.copy(trMs = 300, np = 128, nex = 1),
    "Turbo (TSE)" to BASE_PROTOCOL.copy(trMs = 4000, np = 256, nex = 1, etl = 16),
    MISSION_PRESET_NAME to BASE_PROTOCOL.copy(trMs = 700, np = 256, nex = 3, tkMm = 4.0f)
)

private val PHANTOM_LIST = listOf(
    Triple("kepala_sagital", "Kepala Sagital", 240f),
    Triple("kepala_axial", "Kepala Axial", 220f),
    Triple("knee_sagital", "Knee Sagital", 200f),
    Triple("knee_coronal", "Knee Coronal", 200f)
)

// -------------------------------------------------------------
// KARTU INDUK M09: PROTOKOL, TA, SNR & KONSOL VENDOR
// -------------------------------------------------------------
private data class M09SubCardSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val nodeRef: String
)

private val M09_SUB_CARDS = listOf(
    M09SubCardSpec("ta_snr", "Kalkulator TA, Voksel & SNR", "S1 • Trade-off & Irisan", "M09.1–M09.4"),
    M09SubCardSpec("console_tabs", "Peta Tab Konsol & Vendor", "S2 • Siemens/GE/Philips", "M09.5–M09.7")
)

@Composable
fun Module9LabSuite(
    selectedActivityId: String = "ta_snr",
    onSelectActivity: (String) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "M09 • Protokol, TA, SNR & Konsol Vendor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                        Text(
                            text = "Pilih simulasi parameter protokol atau peta tab konsol vendor.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = CyanDark.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "2 Simulasi",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanBright,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M09_SUB_CARDS.forEach { card ->
                        val isSelected = selectedActivityId == card.id
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectActivity(card.id) },
                            color = if (isSelected) CyanDark.copy(alpha = 0.35f) else DeepObsidian,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) CyanBright else CardSlateBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = card.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyanBright else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = card.subtitle,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = card.nodeRef,
                                    fontSize = 9.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedActivityId == "console_tabs") {
            ConsoleTabsSimulator()
        } else {
            ProtocolTaSnrSimulator()
        }
    }
}

// -------------------------------------------------------------
// SIMULASI S1: KALKULATOR TA, VOKSEL, DAN SNR (MODUL 9)
// -------------------------------------------------------------
@Composable
fun ProtocolTaSnrSimulator() {
    var params by remember { mutableStateOf(BASE_PROTOCOL) }
    var activePresetName by remember { mutableStateOf("Awal") }
    var isMissionActive by remember { mutableStateOf(false) }
    var activePhantomIdx by remember { mutableIntStateOf(0) }

    // Mode irisan (Δz = BW_RF ÷ (γ × Gz))
    var bwRfKhz by remember { mutableFloatStateOf(1.0f) }
    var gzMtPerM by remember { mutableFloatStateOf(20f) }

    val metrics = remember(params) { computeProtocolMetrics(params) }
    val relSnr = (metrics.rawSnr / BASE_METRICS.rawSnr).coerceAtLeast(0.01f)
    val relRes = sqrt((BASE_METRICS.pxMm * BASE_METRICS.pyMm) / (metrics.pxMm * metrics.pyMm))
    val relTa = metrics.taSec / BASE_METRICS.taSec

    val taMinutes = (metrics.taSec / 60f).toInt()
    val taSeconds = (metrics.taSec % 60f).roundToInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("protocol_ta_snr_simulator"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "S1: Kalkulator TA, Voksel, dan SNR",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Menjelaskan: T1.1, T1.2, T1.3, T1.4, T2.1, T2.2, T2.3, T3.1, T3.2",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "M09 • S1",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = CyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Geser parameter. Gambar fantom berbintik bila SNR turun; batang membandingkan dengan protokol awal (garis putus = 1×). Slider ETL, partisi 3D (T3.1), serta R (T3.2) adalah materi Tingkat 3.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Fantom Prosedural + 3 Batang Perbandingan (SNR, Resolusi, Waktu TA)
            val currentPhantom = PHANTOM_LIST[activePhantomIdx]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidian)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(14.dp))
                    .testTag("s1_phantom_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val phantomBoxSize = (h - 36.dp.toPx()).coerceAtMost(w * 0.46f)
                    val px0 = 12.dp.toPx()
                    val py0 = 20.dp.toPx()

                    // Latar Fantom Hitam
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(px0, py0),
                        size = Size(phantomBoxSize, phantomBoxSize)
                    )

                    // Skala FoV Fantom
                    val fovScale = (currentPhantom.third / params.fovMm.toFloat()).coerceIn(0.55f, 1.5f)
                    drawProceduralPhantom(
                        phantomId = currentPhantom.first,
                        topLeft = Offset(px0, py0),
                        boxSize = phantomBoxSize,
                        fovScale = fovScale
                    )

                    // Kisi Piksel / Voksel (Resolusi Matriks NF x NP)
                    val gridCols = (params.nf / 16).coerceIn(4, 28)
                    val gridRows = (params.np / 16).coerceIn(4, 28)
                    for (c in 1 until gridCols) {
                        val gx = px0 + (phantomBoxSize / gridCols) * c
                        drawLine(
                            color = CyanPrimary.copy(alpha = 0.14f),
                            start = Offset(gx, py0),
                            end = Offset(gx, py0 + phantomBoxSize),
                            strokeWidth = 1f
                        )
                    }
                    for (r in 1 until gridRows) {
                        val gy = py0 + (phantomBoxSize / gridRows) * r
                        drawLine(
                            color = CyanPrimary.copy(alpha = 0.14f),
                            start = Offset(px0, gy),
                            end = Offset(px0 + phantomBoxSize, gy),
                            strokeWidth = 1f
                        )
                    }

                    // Bintik Noise Dinamis saat SNR Turun
                    val noiseAmp = (0.30f / relSnr.coerceAtLeast(0.08f)).coerceAtMost(0.95f)
                    val rng = Random(
                        params.trMs * 31 + params.fovMm * 17 + params.nf * 13 +
                            params.np * 7 + params.nex * 5 + params.bwHzPx * 3 +
                            params.etl * 11 + params.ns * 19 + params.rFactor * 23 + activePhantomIdx
                    )
                    val dotCount = (180 * noiseAmp).toInt().coerceIn(15, 220)
                    for (i in 0 until dotCount) {
                        val nx = px0 + rng.nextFloat() * phantomBoxSize
                        val ny = py0 + rng.nextFloat() * phantomBoxSize
                        val gray = if (rng.nextBoolean()) 1f else 0f
                        drawRect(
                            color = Color(gray, gray, gray, (noiseAmp * 0.65f).coerceIn(0.08f, 0.75f)),
                            topLeft = Offset(nx, ny),
                            size = Size(3.dp.toPx(), 3.dp.toPx())
                        )
                    }

                    // Bingkai Fantom
                    drawRect(
                        color = CyanPrimary,
                        topLeft = Offset(px0, py0),
                        size = Size(phantomBoxSize, phantomBoxSize),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Indikator Ketebalan Irisan di Bawah Fantom
                    val sliceBarW = (params.tkMm * 6.dp.toPx()).coerceAtMost(phantomBoxSize * 0.6f)
                    drawRect(
                        color = CyanBright,
                        topLeft = Offset(px0 + phantomBoxSize - sliceBarW, py0 + phantomBoxSize + 6.dp.toPx()),
                        size = Size(sliceBarW, 5.dp.toPx())
                    )

                    // 3 Batang Perbandingan (SNR, Resolusi, Waktu TA) terhadap Protokol Awal (1x, skala maks 3x)
                    val barStartX = px0 + phantomBoxSize + 16.dp.toPx()
                    val barWidth = (w - barStartX - 12.dp.toPx()).coerceAtLeast(60.dp.toPx())
                    val barHeight = 22.dp.toPx()
                    val barSpacing = (phantomBoxSize - 3 * barHeight) / 2f + barHeight

                    val barItems = listOf(
                        Triple("SNR", relSnr, CyanBright),
                        Triple("Resolusi", relRes, EmeraldSuccess),
                        Triple("Waktu TA", relTa, AmberWarning)
                    )

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                    barItems.forEachIndexed { idx, (_, value, barColor) ->
                        val by = py0 + 14.dp.toPx() + idx * barSpacing
                        // Track
                        drawRect(
                            color = CardSlate,
                            topLeft = Offset(barStartX, by),
                            size = Size(barWidth, barHeight)
                        )
                        // Fill (skala maks 3x)
                        val fillFraction = (value.coerceIn(0f, 3f) / 3f)
                        drawRect(
                            color = barColor,
                            topLeft = Offset(barStartX, by),
                            size = Size(barWidth * fillFraction, barHeight)
                        )
                        // Garis putus-putus = protokol awal (1x = 1/3 dari skala maks 3x)
                        val oneX = barStartX + barWidth / 3f
                        drawLine(
                            color = TextPrimary,
                            start = Offset(oneX, by - 4.dp.toPx()),
                            end = Offset(oneX, by + barHeight + 4.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }
                }

                // Label Overlay di Atas & Kanan Canvas
                Text(
                    text = "${currentPhantom.second} (FoV ${params.fovMm} mm • Irisan ${params.tkMm} mm)",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(26.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format(Locale.US, "SNR: %.2f×", relSnr),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "Resolusi: %.2f×", relRes),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "Waktu TA: %.2f×", relTa),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "garis putus = protokol awal (1×); skala maks 3×",
                    fontSize = 9.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pemilih Potongan Citra Fantom (< Nama Fantom >)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        activePhantomIdx = (activePhantomIdx - 1 + PHANTOM_LIST.size) % PHANTOM_LIST.size
                    },
                    modifier = Modifier.testTag("s1_prev_phantom"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("< Sebelumnya", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${currentPhantom.second} (${activePhantomIdx + 1}/${PHANTOM_LIST.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                OutlinedButton(
                    onClick = {
                        activePhantomIdx = (activePhantomIdx + 1) % PHANTOM_LIST.size
                    },
                    modifier = Modifier.testTag("s1_next_phantom"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Berikutnya >", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Kartu Hasil Perhitungan Real-Time (TA, Piksel, Volume Voksel, SNR Relatif)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricResultBox(
                    title = "Waktu Pindai (TA)",
                    value = "$taMinutes mnt $taSeconds dtk",
                    accentColor = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricResultBox(
                    title = "Piksel (frek × fase)",
                    value = String.format(Locale.US, "%.2f × %.2f mm", metrics.pxMm, metrics.pyMm),
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricResultBox(
                    title = "Volume Voksel",
                    value = String.format(Locale.US, "%.2f mm³", metrics.voxelVolMm3),
                    accentColor = CyanBright,
                    modifier = Modifier.weight(1f)
                )
                MetricResultBox(
                    title = "SNR Relatif",
                    value = String.format(Locale.US, "%.2f×", relSnr),
                    accentColor = CyanBright,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6 Tombol Preset Protokol
            Text(
                text = "PRESET PROTOKOL (KLIK UNTUK MEMUAT)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                S1_PRESETS.forEachIndexed { idx, (presetName, presetVal) ->
                    val isSelected = activePresetName == presetName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            params = presetVal
                            activePresetName = presetName
                            isMissionActive = (presetName == MISSION_PRESET_NAME)
                        },
                        modifier = Modifier.testTag("s1_preset_$idx"),
                        label = {
                            Text(
                                text = presetName,
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

            // Status Misi / Peringatan TA > 5 menit
            if (metrics.taSec > 300f || isMissionActive) {
                Spacer(modifier = Modifier.height(8.dp))
                val missionPassed = metrics.taSec <= 120f && relSnr >= 0.6f
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isMissionActive && missionPassed -> EmeraldDark.copy(alpha = 0.7f)
                        metrics.taSec > 300f -> RoseDark.copy(alpha = 0.7f)
                        else -> AmberDark.copy(alpha = 0.7f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isMissionActive && missionPassed -> EmeraldSuccess
                            metrics.taSec > 300f -> RoseDanger
                            else -> AmberWarning
                        }
                    )
                ) {
                    val warningText = if (metrics.taSec > 300f) {
                        "⚠ Peringatan: TA di atas 5 menit meningkatkan risiko gerakan pasien. "
                    } else ""
                    val missionText = if (isMissionActive) {
                        if (missionPassed) {
                            "✓ Misi tercapai: TA ≤ 2 menit dan SNR ≥ 0,6×!"
                        } else {
                            "🎯 Misi aktif: capai TA ≤ 2 menit (≤ 120 dtk) dan SNR ≥ 0,6×."
                        }
                    } else ""
                    Text(
                        text = warningText + missionText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isMissionActive && missionPassed -> EmeraldSuccess
                            metrics.taSec > 300f -> RoseDanger
                            else -> AmberWarning
                        },
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 10 Slider Parameter S1
            S1SliderRow(
                label = "TR (ms)",
                valueText = "${params.trMs} ms",
                value = params.trMs.toFloat(),
                range = 10f..4000f,
                onValueChange = { params = params.copy(trMs = ((it / 10f).roundToInt() * 10).coerceIn(10, 4000)) }
            )
            S1SliderRow(
                label = "FoV (mm)",
                valueText = "${params.fovMm} mm",
                value = params.fovMm.toFloat(),
                range = 100f..450f,
                onValueChange = { params = params.copy(fovMm = ((it / 10f).roundToInt() * 10).coerceIn(100, 450)) }
            )
            S1SliderRow(
                label = "Matriks frekuensi (N_frek)",
                valueText = "${params.nf}",
                value = params.nf.toFloat(),
                range = 64f..512f,
                onValueChange = { params = params.copy(nf = ((it / 32f).roundToInt() * 32).coerceIn(64, 512)) }
            )
            S1SliderRow(
                label = "Matriks fase (N_fase)",
                valueText = "${params.np}",
                value = params.np.toFloat(),
                range = 64f..512f,
                onValueChange = { params = params.copy(np = ((it / 16f).roundToInt() * 16).coerceIn(64, 512)) }
            )
            S1SliderRow(
                label = "Tebal irisan (mm)",
                valueText = String.format(Locale.US, "%.1f mm", params.tkMm),
                value = params.tkMm,
                range = 1f..10f,
                onValueChange = { params = params.copy(tkMm = ((it * 2f).roundToInt() / 2f).coerceIn(1f, 10f)) }
            )
            S1SliderRow(
                label = "NEX (Averages / NSA)",
                valueText = "${params.nex}",
                value = params.nex.toFloat(),
                range = 1f..6f,
                onValueChange = { params = params.copy(nex = it.roundToInt().coerceIn(1, 6)) }
            )
            S1SliderRow(
                label = "Bandwidth (Hz/piksel)",
                valueText = "${params.bwHzPx} Hz/px",
                value = params.bwHzPx.toFloat(),
                range = 100f..500f,
                onValueChange = { params = params.copy(bwHzPx = ((it / 10f).roundToInt() * 10).coerceIn(100, 500)) }
            )
            S1SliderRow(
                label = "Turbo factor / ETL (T3.1)",
                valueText = "${params.etl}",
                value = params.etl.toFloat(),
                range = 1f..32f,
                onValueChange = { params = params.copy(etl = it.roundToInt().coerceIn(1, 32)) }
            )
            S1SliderRow(
                label = "Jumlah partisi 3D, 1 = 2D (T3.1)",
                valueText = "${params.ns}",
                value = params.ns.toFloat(),
                range = 1f..64f,
                onValueChange = { params = params.copy(ns = it.roundToInt().coerceIn(1, 64)) }
            )
            S1SliderRow(
                label = "Faktor iPAT/SENSE R (T3.2)",
                valueText = "${params.rFactor}×",
                value = params.rFactor.toFloat(),
                range = 1f..4f,
                onValueChange = { params = params.copy(rFactor = it.roundToInt().coerceIn(1, 4)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rumus Kotak Monospace
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "TA = TR × N_fase × NEX × N_partisi ÷ (ETL × R)\nvoksel = (FoV/N_frek) × (FoV/N_fase) × tebal\nSNR ∝ voksel × √(NEX·N_fase·N_partisi / BW / R)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyanBright,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kartu Studi Kasus S1 (terkait T1.4)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Studi Kasus S1 (terkait T1.4 — Manajemen Pasien)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pasien gelisah, protokol awal berjalan lebih dari 3 menit. Pilih preset \"Kasus: pasien gelisah\", lalu cari kombinasi dengan TA ≤ 2 menit tanpa SNR turun di bawah 0,6×.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kartu Mode Irisan (Δz = BW_RF ÷ (γ × Gz))
            val dzMm = (bwRfKhz * 1000f) / (MriRepository.GAMMA_1H * 1e6f * (gzMtPerM / 1000f)) * 1000f
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slice_thickness_calculator_card"),
                shape = RoundedCornerShape(12.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Mode Irisan: Δz = BW pulsa RF ÷ (γ × Gz)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Text(
                        text = "γ Hidrogen = 42,58 MHz/T | Rentang Gradien Gz: 20–80 mT/m",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    S1SliderRow(
                        label = "BW pulsa RF (kHz)",
                        valueText = String.format(Locale.US, "%.1f kHz", bwRfKhz),
                        value = bwRfKhz,
                        range = 0.5f..5.0f,
                        onValueChange = { bwRfKhz = ((it * 10f).roundToInt() / 10f).coerceIn(0.5f, 5.0f) }
                    )
                    S1SliderRow(
                        label = "Gradien Gz (mT/m)",
                        valueText = "${gzMtPerM.roundToInt()} mT/m",
                        value = gzMtPerM,
                        range = 20f..80f,
                        onValueChange = { gzMtPerM = it.roundToInt().toFloat().coerceIn(20f, 80f) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CardSlate,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                    ) {
                        Text(
                            text = String.format(Locale.US, "Tebal irisan Δz = %.2f mm", dzMm),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricResultBox(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(text = title, fontSize = 10.sp, color = TextSecondary)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun S1SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(
                text = valueText,
                fontSize = 11.sp,
                color = CyanBright,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.height(28.dp),
            colors = SliderDefaults.colors(
                thumbColor = CyanBright,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = CardSlate
            )
        )
    }
}

private fun DrawScope.drawProceduralPhantom(
    phantomId: String,
    topLeft: Offset,
    boxSize: Float,
    fovScale: Float
) {
    val cx = topLeft.x + boxSize / 2f
    val cy = topLeft.y + boxSize / 2f
    val unit = (boxSize / 256f) * fovScale

    fun mapX(x: Float) = cx + (x - 128f) * unit
    fun mapY(y: Float) = cy + (y - 128f) * unit
    fun gray(v: Float) = Color(v.coerceIn(0f, 1f), v.coerceIn(0f, 1f), v.coerceIn(0f, 1f), 1f)

    fun drawEl(x: Float, y: Float, rx: Float, ry: Float, v: Float) {
        drawOval(
            color = gray(v),
            topLeft = Offset(mapX(x - rx), mapY(y - ry)),
            size = Size(rx * 2f * unit, ry * 2f * unit)
        )
    }

    fun drawRc(x: Float, y: Float, w: Float, h: Float, v: Float) {
        val left = mapX(x).coerceIn(topLeft.x, topLeft.x + boxSize)
        val top = mapY(y).coerceIn(topLeft.y, topLeft.y + boxSize)
        val right = mapX(x + w).coerceIn(topLeft.x, topLeft.x + boxSize)
        val bottom = mapY(y + h).coerceIn(topLeft.y, topLeft.y + boxSize)
        if (right > left && bottom > top) {
            drawRect(
                color = gray(v),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top)
            )
        }
    }

    when (phantomId) {
        "kepala_sagital" -> {
            drawEl(130f, 100f, 84f, 92f, 0.45f)
            drawEl(130f, 100f, 78f, 86f, 0.12f)
            drawEl(130f, 100f, 72f, 80f, 0.52f)
            drawEl(130f, 100f, 64f, 72f, 0.72f)
            // Corpus callosum & batang otak
            drawEl(122f, 92f, 42f, 18f, 0.92f)
            drawEl(122f, 101f, 30f, 8f, 0.10f)
            drawRc(120f, 125f, 20f, 65f, 0.68f)
            // Cerebellum
            drawEl(168f, 142f, 30f, 22f, 0.62f)
        }
        "kepala_axial" -> {
            drawEl(128f, 28f, 9f, 10f, 0.55f)
            drawEl(128f, 132f, 90f, 106f, 0.82f)
            drawEl(128f, 132f, 84f, 100f, 0.12f)
            drawEl(128f, 132f, 77f, 93f, 0.50f)
            drawEl(128f, 132f, 68f, 83f, 0.72f)
            drawRc(127f, 44f, 2f, 176f, 0.14f)
            drawEl(113f, 116f, 9f, 26f, 0.08f)
            drawEl(143f, 116f, 9f, 26f, 0.08f)
            drawEl(111f, 140f, 10f, 13f, 0.56f)
            drawEl(145f, 140f, 10f, 13f, 0.56f)
        }
        "knee_sagital" -> {
            drawRc(44f, 10f, 168f, 236f, 0.42f)
            drawRc(98f, 10f, 56f, 96f, 0.14f)
            drawRc(104f, 10f, 44f, 96f, 0.75f)
            drawEl(130f, 112f, 46f, 40f, 0.14f)
            drawEl(130f, 112f, 41f, 35f, 0.75f)
            drawRc(100f, 178f, 58f, 68f, 0.14f)
            drawRc(106f, 178f, 46f, 68f, 0.75f)
            drawRc(92f, 150f, 76f, 12f, 0.15f)
            drawEl(74f, 108f, 12f, 24f, 0.78f)
        }
        else -> {
            // knee_coronal
            drawRc(60f, 10f, 136f, 236f, 0.42f)
            drawRc(104f, 10f, 50f, 92f, 0.75f)
            drawEl(150f, 112f, 26f, 30f, 0.75f)
            drawEl(106f, 112f, 26f, 30f, 0.75f)
            drawRc(88f, 146f, 80f, 12f, 0.15f)
            drawRc(90f, 160f, 76f, 16f, 0.75f)
            drawRc(106f, 176f, 42f, 68f, 0.75f)
        }
    }
}

// -------------------------------------------------------------
// SIMULASI S2: PETA TAB KONSOL & TABEL ANTAR-VENDOR (MODUL 9)
// -------------------------------------------------------------
private data class ConsoleParamItem(
    val name: String,
    val badges: List<String> // "ta", "snr", "res", "kon"
)

private data class ConsoleTabInfo(
    val tabName: String,
    val description: String,
    val parameters: List<ConsoleParamItem>
)

private val CONSOLE_TABS = listOf(
    ConsoleTabInfo(
        tabName = "Routine",
        description = "Parameter utama yang paling sering diubah.",
        parameters = listOf(
            ConsoleParamItem("TR", listOf("ta", "snr", "kon")),
            ConsoleParamItem("TE", listOf("snr", "kon")),
            ConsoleParamItem("Flip angle", listOf("snr", "kon")),
            ConsoleParamItem("FoV", listOf("res", "snr")),
            ConsoleParamItem("Jumlah irisan & tebal", listOf("snr", "res")),
            ConsoleParamItem("NEX / averages", listOf("ta", "snr")),
            ConsoleParamItem("Waktu pindai (TA)", listOf("ta"))
        )
    ),
    ConsoleTabInfo(
        tabName = "Contrast",
        description = "Pembobotan gambar.",
        parameters = listOf(
            ConsoleParamItem("TR", listOf("kon", "ta")),
            ConsoleParamItem("TE", listOf("kon", "snr")),
            ConsoleParamItem("Flip angle", listOf("kon")),
            ConsoleParamItem("Saturasi lemak, MTC", listOf("kon")),
            ConsoleParamItem("Inversion time (TI)", listOf("kon", "ta"))
        )
    ),
    ConsoleTabInfo(
        tabName = "Resolution",
        description = "Ukuran matriks dan akselerasi.",
        parameters = listOf(
            ConsoleParamItem("Matriks dasar (frekuensi)", listOf("res")),
            ConsoleParamItem("Resolusi fase (%)", listOf("res", "ta")),
            ConsoleParamItem("Interpolasi", listOf("res")),
            ConsoleParamItem("Partial Fourier", listOf("ta", "snr")),
            ConsoleParamItem("Paralel imaging (iPAT)", listOf("ta", "snr")),
            ConsoleParamItem("Bandwidth", listOf("snr"))
        )
    ),
    ConsoleTabInfo(
        tabName = "Geometry",
        description = "Posisi, orientasi, dan cakupan.",
        parameters = listOf(
            ConsoleParamItem("FoV & FoV fase (%)", listOf("res", "ta")),
            ConsoleParamItem("Orientasi, arah fase", listOf("res")),
            ConsoleParamItem("Gap irisan", listOf("snr")),
            ConsoleParamItem("Saturation band", listOf("res"))
        )
    ),
    ConsoleTabInfo(
        tabName = "System",
        description = "Koil dan perangkat keras.",
        parameters = listOf(
            ConsoleParamItem("Pemilihan elemen koil", listOf("snr")),
            ConsoleParamItem("Posisi meja / isocenter", listOf("res")),
            ConsoleParamItem("Shim / penyetelan", listOf("kon"))
        )
    ),
    ConsoleTabInfo(
        tabName = "Physio",
        description = "Sinkronisasi fisiologis via PGM.",
        parameters = listOf(
            ConsoleParamItem("Gating EKG / pulse", listOf("res", "ta")),
            ConsoleParamItem("Trigger napas", listOf("res", "ta")),
            ConsoleParamItem("Trigger delay", listOf("ta"))
        )
    ),
    ConsoleTabInfo(
        tabName = "Inline",
        description = "Pascapemrosesan otomatis.",
        parameters = listOf(
            ConsoleParamItem("Subtraksi", listOf("kon")),
            ConsoleParamItem("MIP", listOf("kon")),
            ConsoleParamItem("Peta (mis. ADC)", listOf("kon"))
        )
    ),
    ConsoleTabInfo(
        tabName = "Sequence",
        description = "Tipe dan detail sekuens.",
        parameters = listOf(
            ConsoleParamItem("Tipe sekuens", listOf("kon", "ta")),
            ConsoleParamItem("Turbo factor / ETL", listOf("ta", "snr")),
            ConsoleParamItem("Bandwidth & echo spacing", listOf("snr", "res")),
            ConsoleParamItem("Fase encoding order", listOf("res"))
        )
    )
)

private data class VendorMappingRow(
    val functionName: String,
    val siemens: String,
    val ge: String,
    val philips: String
)

private val VENDOR_TABLE = listOf(
    VendorMappingRow("Rata-rata sinyal", "Averages", "NEX", "NSA"),
    VendorMappingRow("Akselerasi paralel", "iPAT (mis. GRAPPA)", "ASSET / ARC", "SENSE"),
    VendorMappingRow("Jumlah echo per TR (turbo)", "Turbo factor", "Echo train length (ETL)", "TSE factor"),
    VendorMappingRow("Bandwidth", "Bandwidth (Hz/piksel)", "Receive bandwidth (kHz)", "Water-fat shift (piksel)"),
    VendorMappingRow("FoV arah fase", "FoV phase (%)", "Phase FOV", "Rectangular FOV (%)"),
    VendorMappingRow("Pengurangan langkah fase", "Phase partial Fourier", "Fractional NEX", "Half scan")
)

@Composable
fun ConsoleTabsSimulator() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val activeTab = CONSOLE_TABS[selectedTabIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("console_tabs_simulator"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "S2: Peta Tab Konsol & Tabel Vendor",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Menjelaskan: T2.4 (Tab Konsol) & T3.3 (Pemetaan Antar-Vendor)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "M09 • S2",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = CyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pilih tab untuk melihat parameter yang biasanya ada di sana. Nama dan susunan tab berbeda antar vendor; peta ini contoh antarmuka.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 8 Chip Tab Konsol Interaktif
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CONSOLE_TABS.forEachIndexed { idx, tabInfo ->
                    val isSelected = selectedTabIndex == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTabIndex = idx },
                        modifier = Modifier.testTag("console_tab_${tabInfo.tabName}"),
                        label = {
                            Text(
                                text = tabInfo.tabName,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Detail Parameter Tab Konsol Terpilih
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${activeTab.tabName} — ${activeTab.description}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeTab.parameters.forEach { param ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${param.name}",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    param.badges.forEach { badgeCode ->
                                        ImpactBadgeChip(code = badgeCode)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legenda Lencana Warna
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Warna lencana:", fontSize = 10.sp, color = TextMuted)
                ImpactBadgeChip("ta")
                ImpactBadgeChip("snr")
                ImpactBadgeChip("res")
                ImpactBadgeChip("kon")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabel T3.3: Pemetaan Nama Antar-Vendor (Siemens vs GE vs Philips)
            Text(
                text = "T3.3: Pemetaan Nama Parameter Antar-Vendor",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vendor_mapping_table"),
                shape = RoundedCornerShape(12.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Header Tabel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSlate, RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp, horizontal = 6.dp)
                    ) {
                        Text("Fungsi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanBright, modifier = Modifier.weight(1.2f))
                        Text("Siemens", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text("GE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text("Philips", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                    }

                    VENDOR_TABLE.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.functionName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1.2f))
                            Text(row.siemens, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(row.ge, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(row.philips, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Nama dapat berubah antar versi perangkat lunak; selalu cocokkan dengan manual unit MRI Anda.",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ImpactBadgeChip(code: String) {
    val (label, borderColor, bgColor) = when (code) {
        "ta" -> Triple("waktu", AmberWarning, AmberDark.copy(alpha = 0.5f))
        "snr" -> Triple("SNR", CyanBright, CyanDark.copy(alpha = 0.5f))
        "res" -> Triple("resolusi", Color(0xFF6F9BFF), Color(0xFF1F3A68).copy(alpha = 0.5f))
        else -> Triple("kontras", EmeraldSuccess, EmeraldDark.copy(alpha = 0.5f))
    }
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = borderColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}
