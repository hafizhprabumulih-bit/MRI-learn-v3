package com.example.ui.simulators

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun fmtId(value: Float, decimals: Int = 2): String =
    String.format(Locale.US, "%.${decimals}f", value).replace('.', ',')

// ============================================================================
// SUITE PENUH MODUL 6 (M06A GRADIEN + M06B SISTEM RF & KOIL) UNTUK LAB VIRTUAL
// ============================================================================
private data class M06SubCard(val id: String, val code: String, val title: String, val nodeTag: String, val tabIdx: Int)

private val M06_SUB_CARDS = listOf(
    M06SubCard("m6a_trapesium_gradien", "M06A", "Trapesium Slew Rate", "M06A.T1", 0),
    M06SubCard("m6a_gradien_seimbang", "M06A", "Gradien Seimbang & DWI", "M06A.T2", 0),
    M06SubCard("m6a_peta_frekuensi", "M06A", "Peta Frekuensi & Sumbu", "M06A.T1", 0),
    M06SubCard("m6a_koil_maxwell_golay", "M06A", "Koil Maxwell & Golay", "M06A.T2", 0),
    M06SubCard("m6b_a1_rantai_sinyal", "M06B·A1", "Rantai Sinyal Penerima", "M06B.T1", 1),
    M06SubCard("m6b_a2_a3_a4_koil", "M06B·A2-4", "Pemilih & Array Koil", "M06B.T2", 2),
    M06SubCard("m6b_a5_faraday", "M06B·A5", "Kandang Faraday & RF", "M06B.T2", 3),
    M06SubCard("m6b_a6_chemical_shift", "M06B·A6", "Spektrum Pergeseran Kimia", "M06B.T3", 4),
    M06SubCard("m6b_a7_b1_ptx", "M06B·A7", "Peta B₁ & pTx", "M06B.T3", 4),
    M06SubCard("m6b_a8_birdcage", "M06B·A8", "Birdcage Kuadratur", "M06B.T3", 4)
)

@Composable
fun Module6GradientRfLabSuite(
    selectedActivityId: String = "m6a_trapesium_gradien",
    onSelectActivity: (String) -> Unit = {}
) {
    val activeCard = M06_SUB_CARDS.firstOrNull { it.id == selectedActivityId } ?: M06_SUB_CARDS.first()
    val selectedTab = activeCard.tabIdx

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module6_lab_suite"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "M06: Laboratorium Gradien (M06A) & Sistem RF/Koil (M06B)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Trapesium Slew Rate, Maxwell/Golay, Rantai Sinyal, Koil Array, Faraday & Birdcage.",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    M06_SUB_CARDS.chunked(2).forEach { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            pair.forEach { item ->
                                val isSel = item.id == activeCard.id
                                androidx.compose.material3.Surface(
                                    onClick = { onSelectActivity(item.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) CyanDark else DeepObsidian,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) CyanBright else CardSlateBorder
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(item.code, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) CyanBright else AmberAccent)
                                            Text(item.nodeTag, fontSize = 9.sp, color = TextMuted)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSel) CyanBright else TextPrimary,
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

        when (selectedTab) {
            0 -> GradientTrapezoidAndLobesSimulator()
            1 -> RfSignalChainSimulator()
            2 -> CoilSelectorAndDepthSimulator()
            3 -> FaradayShieldArtifactSimulator()
            4 -> BirdcageAndB1ParallelTransmitSimulator()
        }
    }
}

// ============================================================================
// 1. M06A: GRADIENT TRAPEZOID, MAXWELL/GOLAY, & BALANCED LOBES SIMULATOR
// ============================================================================
@Composable
fun GradientTrapezoidAndLobesSimulator(initialTab: Int = 0) {
    var activeSubTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var amplitudeMtPerM by remember { mutableFloatStateOf(40f) } // 20 - 80 mT/m
    var slewRateTPerMs by remember { mutableFloatStateOf(160f) } // 100 - 200 T/m/s
    var isMaxwellCoil by remember { mutableStateOf(true) } // true = Maxwell (Z), false = Golay Pelana (X/Y)
    var selectedAxis by remember { mutableIntStateOf(0) } // 0 = Z (Kepala-Kaki), 1 = X (Kiri-Kanan), 2 = Y (Depan-Belakang)
    var isOpenMagnet by remember { mutableStateOf(false) }

    // Sub-tab 2: Gradien Seimbang & Stejskal-Tanner
    var lobeProgress by remember { mutableFloatStateOf(0.5f) } // 0.0 = lobus 1 akhir (dephased), 1.0 = lobus 2 akhir (rephased)
    var isStejskalTanner by remember { mutableStateOf(false) }
    var hasWaterDiffusion by remember { mutableStateOf(false) }

    val riseTimeMs = amplitudeMtPerM / slewRateTPerMs // mT/m divided by T/m/s = ms
    val riseTimeUs = (riseTimeMs * 1000f).roundToInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m6a_gradient_simulator"),
        shape = RoundedCornerShape(16.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = CyanPrimary) {
                    Text(
                        text = "M06A",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                OutlinedButton(
                    onClick = {
                        amplitudeMtPerM = 40f
                        slewRateTPerMs = 160f
                        isMaxwellCoil = true
                        lobeProgress = 1.0f
                        isStejskalTanner = false
                        hasWaterDiffusion = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = CyanBright, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 10.sp, color = CyanBright)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Simulator Gradien, Kumparan & Fase Spin",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val subTabs = listOf(
                    0 to "1. Trapesium & Maxwell/Golay",
                    1 to "2. Lobus Seimbang & 7 Jam Fase",
                    2 to "3. Peta Frekuensi & Sumbu X/Y/Z"
                )
                subTabs.forEach { (idx, label) ->
                    val sel = activeSubTab == idx
                    FilterChip(
                        selected = sel,
                        onClick = { activeSubTab = idx },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = if (sel) CyanPrimary else CardSlateBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeSubTab) {
                0 -> {
                    // SUB-TAB 0: Trapesium Gradien (Amplitudo & Slew Rate) + Maxwell vs Golay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Amplitudo Gradien (G): ${amplitudeMtPerM.toInt()} mT/m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Waktu naik: ${fmtId(riseTimeMs, 2)} ms ($riseTimeUs μs)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = amplitudeMtPerM,
                        onValueChange = { amplitudeMtPerM = (it / 5f).roundToInt() * 5f },
                        valueRange = 20f..80f,
                        colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Slew Rate (SR): ${slewRateTPerMs.toInt()} T/m/s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Rumus: t_naik = G / SR",
                            fontSize = 10.sp,
                            color = AmberWarning,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = slewRateTPerMs,
                        onValueChange = { slewRateTPerMs = (it / 10f).roundToInt() * 10f },
                        valueRange = 100f..200f,
                        colors = SliderDefaults.colors(thumbColor = AmberWarning, activeTrackColor = AmberWarning)
                    )

                    // Toggle Kumparan Maxwell (Z) vs Golay Pelana (X/Y)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isMaxwellCoil,
                            onClick = { isMaxwellCoil = true },
                            modifier = Modifier.weight(1f),
                            label = { Text("Kumparan Z: Pasangan Maxwell", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = !isMaxwellCoil,
                            onClick = { isMaxwellCoil = false },
                            modifier = Modifier.weight(1f),
                            label = { Text("Kumparan X/Y: Pelana Golay", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Canvas Trapesium Gradien + Geometri Kumparan Maxwell/Golay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkNavySlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val splitX = w * 0.56f

                            // Sisi Kiri: Kurva Trapesium Gradien (Amplitudo & Rise Time)
                            val gx0 = 16.dp.toPx()
                            val gx1 = splitX - 10.dp.toPx()
                            val gyBase = h * 0.78f
                            val gyTopMax = h * 0.16f

                            drawLine(
                                color = CardSlateBorder,
                                start = Offset(gx0, gyBase),
                                end = Offset(gx1, gyBase),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = CardSlateBorder,
                                start = Offset(gx0, gyTopMax - 6.dp.toPx()),
                                end = Offset(gx0, gyBase + 10.dp.toPx()),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            val normAmp = (amplitudeMtPerM / 80f).coerceIn(0.2f, 1.0f)
                            val trapHeight = (gyBase - gyTopMax) * normAmp
                            val yPeak = gyBase - trapHeight

                            // Rise time fraction (0.1 ms to 0.8 ms mapped to x width)
                            val gw = gx1 - gx0
                            val riseFrac = (riseTimeMs / 1.0f).coerceIn(0.08f, 0.34f)
                            val xStart = gx0 + gw * 0.06f
                            val xPlateauStart = xStart + gw * riseFrac
                            val xPlateauEnd = gx1 - gw * 0.06f - gw * riseFrac
                            val xEnd = gx1 - gw * 0.06f

                            val trapPath = Path().apply {
                                moveTo(xStart, gyBase)
                                lineTo(xPlateauStart, yPeak)
                                lineTo(xPlateauEnd, yPeak)
                                lineTo(xEnd, gyBase)
                                close()
                            }
                            drawPath(trapPath, color = CyanPrimary.copy(alpha = 0.22f))
                            drawPath(trapPath, color = CyanBright, style = Stroke(width = 2.8.dp.toPx()))

                            // Penanda Rise Time (Slew Rate slope)
                            drawLine(
                                color = AmberWarning,
                                start = Offset(xStart, gyBase + 8.dp.toPx()),
                                end = Offset(xPlateauStart, gyBase + 8.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx()
                            )

                            // Sisi Kanan: Geometri Kumparan (Maxwell vs Golay Pelana) + Perisai Aktif
                            val cx = (splitX + w) / 2f
                            val cy = h * 0.50f
                            val boreW = (w - splitX) * 0.78f
                            val boreH = h * 0.56f

                            // Cangkang Perisai Aktif Luar (putus-putus)
                            drawRoundRect(
                                color = EmeraldSuccess.copy(alpha = 0.55f),
                                topLeft = Offset(cx - boreW / 2f - 6.dp.toPx(), cy - boreH / 2f - 6.dp.toPx()),
                                size = Size(boreW + 12.dp.toPx(), boreH + 12.dp.toPx()),
                                cornerRadius = CornerRadius(10.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            )

                            // Silinder Bore Gradien
                            drawRoundRect(
                                color = CardSlateBorder,
                                topLeft = Offset(cx - boreW / 2f, cy - boreH / 2f),
                                size = Size(boreW, boreH),
                                cornerRadius = CornerRadius(8.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            if (isMaxwellCoil) {
                                // Dua cincin melingkar di ujung kiri dan kanan dengan arus berlawanan
                                val rX = boreW * 0.30f
                                drawOval(
                                    color = CyanBright,
                                    topLeft = Offset(cx - rX - 10.dp.toPx(), cy - boreH * 0.42f),
                                    size = Size(20.dp.toPx(), boreH * 0.84f),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawOval(
                                    color = AmberWarning,
                                    topLeft = Offset(cx + rX - 10.dp.toPx(), cy - boreH * 0.42f),
                                    size = Size(20.dp.toPx(), boreH * 0.84f),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            } else {
                                // Empat pelana Golay di atas dan bawah dinding silinder
                                val saddleW = boreW * 0.34f
                                val saddleH = boreH * 0.24f
                                listOf(-1f, 1f).forEach { dx ->
                                    listOf(-1f, 1f).forEach { dy ->
                                        val col = if (dx * dy > 0) AmberWarning else CyanBright
                                        drawRoundRect(
                                            color = col,
                                            topLeft = Offset(
                                                cx + dx * boreW * 0.22f - saddleW / 2f,
                                                cy + dy * boreH * 0.28f - saddleH / 2f
                                            ),
                                            size = Size(saddleW, saddleH),
                                            cornerRadius = CornerRadius(6.dp.toPx()),
                                            style = Stroke(width = 2.5.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "G = ${amplitudeMtPerM.toInt()} mT/m | t_naik = $riseTimeUs μs",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        Text(
                            text = if (isMaxwellCoil) "Maxwell (Gz) + Perisai Aktif" else "Pelana Golay (Gx/Gy)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMaxwellCoil) EmeraldSuccess else AmberWarning,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isMaxwellCoil) {
                            "Pasangan Maxwell (Sumbu Z): Dua kumparan melingkar berarus berlawanan menciptakan kemiringan medan linear sepanjang bore. Perisai gradien aktif di luar kumparan menahan kebocoran fluks agar tidak memicu arus eddy di kriostat."
                        } else {
                            "Kumparan Pelana Golay (Sumbu X & Y): Dua pasang kumparan berbentuk pelana pada dinding silinder menciptakan gradien kiri–kanan (X) atau depan–belakang (Y). Waktu naik = ${amplitudeMtPerM.toInt()} / ${slewRateTPerMs.toInt()} = ${fmtId(riseTimeMs, 2)} ms."
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                1 -> {
                    // SUB-TAB 1: Lobus Gradien Seimbang vs Stejskal-Tanner + 7 Jam Fase Spin
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = !isStejskalTanner,
                            onClick = {
                                isStejskalTanner = false
                                hasWaterDiffusion = false
                            },
                            label = { Text("Gradien Seimbang (Bipolar + / −)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = isStejskalTanner,
                            onClick = { isStejskalTanner = true },
                            label = { Text("Stejskal–Tanner (+ / 180° / +)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    if (isStejskalTanner) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !hasWaterDiffusion,
                                onClick = { hasWaterDiffusion = false },
                                label = { Text("Molekul Air Diam (Kembali Sefase)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldSuccess,
                                    selectedLabelColor = DeepObsidian,
                                    containerColor = DarkNavySlate,
                                    labelColor = TextSecondary
                                )
                            )
                            FilterChip(
                                selected = hasWaterDiffusion,
                                onClick = { hasWaterDiffusion = true },
                                label = { Text("Difusi Gerak Brown (Sinyal Meluruh)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoseDanger,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = DarkNavySlate,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tahap Penelusuran Waktu Lobus: ${(lobeProgress * 100).roundToInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = when {
                                lobeProgress < 0.45f -> "Lobus 1 (Dephasing)"
                                lobeProgress < 0.55f -> if (isStejskalTanner) "Pulsa RF 180° (−φ)" else "Titik Balik Polaritas"
                                else -> "Lobus 2 (Rephasing)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                    }
                    Slider(
                        value = lobeProgress,
                        onValueChange = { lobeProgress = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
                    )

                    // Canvas 2 Lobus Gradien + 7 Jam Fase Spin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(195.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkNavySlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val midY = h * 0.34f
                            val lobeH = h * 0.24f

                            // Garis baseline G = 0
                            drawLine(
                                color = CardSlateBorder,
                                start = Offset(16.dp.toPx(), midY),
                                end = Offset(w - 16.dp.toPx(), midY),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            // Lobus 1 (Negatif di gradien seimbang, Positif di Stejskal-Tanner)
                            val l1Start = w * 0.08f
                            val l1End = w * 0.42f
                            val l1Sign = if (isStejskalTanner) -1f else 1f // -1f means upwards on screen
                            val l1Path = Path().apply {
                                moveTo(l1Start, midY)
                                lineTo(l1Start + 14.dp.toPx(), midY + l1Sign * lobeH)
                                lineTo(l1End - 14.dp.toPx(), midY + l1Sign * lobeH)
                                lineTo(l1End, midY)
                                close()
                            }
                            drawPath(l1Path, color = AmberWarning.copy(alpha = 0.28f))
                            drawPath(l1Path, color = AmberWarning, style = Stroke(width = 2.dp.toPx()))

                            // Pulsa 180° di tengah bila Stejskal-Tanner
                            if (isStejskalTanner) {
                                val midX = w * 0.50f
                                drawLine(
                                    color = RoseDanger,
                                    start = Offset(midX, midY - lobeH * 1.15f),
                                    end = Offset(midX, midY),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            // Lobus 2 (Positif di keduanya)
                            val l2Start = w * 0.58f
                            val l2End = w * 0.92f
                            val l2Path = Path().apply {
                                moveTo(l2Start, midY)
                                lineTo(l2Start + 14.dp.toPx(), midY - lobeH)
                                lineTo(l2End - 14.dp.toPx(), midY - lobeH)
                                lineTo(l2End, midY)
                                close()
                            }
                            drawPath(l2Path, color = CyanPrimary.copy(alpha = 0.28f))
                            drawPath(l2Path, color = CyanBright, style = Stroke(width = 2.dp.toPx()))

                            // Penanda waktu saat ini
                            val cursorX = l1Start + (l2End - l1Start) * lobeProgress
                            drawLine(
                                color = EmeraldSuccess,
                                start = Offset(cursorX, 8.dp.toPx()),
                                end = Offset(cursorX, midY + lobeH + 6.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )

                            // 7 Jam Fase Spin di bagian bawah
                            val clockY = h * 0.79f
                            val clockR = (w / 18f).coerceAtMost(18.dp.toPx())
                            // Faktor dephasing bersih:
                            // Di t=0 -> 0; di t=0.5 -> 1.0 (maks dephase); di t=1.0 -> 0 (sefase kembali, kecuali ada difusi acak)
                            val netDephase = if (lobeProgress <= 0.5f) {
                                lobeProgress * 2f
                            } else {
                                (1f - lobeProgress) * 2f
                            }

                            for (idx in -3..3) {
                                val cx = w / 2f + idx * (w * 0.125f)
                                drawCircle(
                                    color = DeepObsidian,
                                    radius = clockR,
                                    center = Offset(cx, clockY)
                                )
                                drawCircle(
                                    color = if (idx == 0) EmeraldSuccess else CardSlateBorder,
                                    radius = clockR,
                                    center = Offset(cx, clockY),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )

                                val diffusionJitter = if (isStejskalTanner && hasWaterDiffusion && lobeProgress > 0.5f) {
                                    (lobeProgress - 0.5f) * 2f * (idx * 0.95f + sin(idx * 2.1f) * 1.4f)
                                } else {
                                    0f
                                }
                                val angleRad = -PI.toFloat() / 2f + idx * netDephase * 0.85f + diffusionJitter
                                val px = cx + cos(angleRad) * (clockR * 0.82f)
                                val py = clockY + sin(angleRad) * (clockR * 0.82f)

                                drawLine(
                                    color = if (netDephase < 0.08f && diffusionJitter == 0f) EmeraldSuccess else CyanBright,
                                    start = Offset(cx, clockY),
                                    end = Offset(px, py),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                            }
                        }

                        Text(
                            text = if (isStejskalTanner) "Lobus +G → RF 180° → Lobus +G" else "Lobus −G (Dephase) → Lobus +G (Rephase)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (!isStejskalTanner) {
                            "Gradien Seimbang: Lobus pertama membuat 7 jam fase spin di kiri–kanan isosenter menyebar (dephasing). Lobus kedua yang berlawanan polaritas dan berluas sama mengembalikan ketujuh jarum fase searah di t = 100% sehingga terbentuk gema maksimum."
                        } else if (!hasWaterDiffusion) {
                            "Skema Stejskal–Tanner (Air Diam): Kedua lobus gradien memiliki polaritas SEARAH, tetapi pulsa RF 180° di tengah membalik tanda fase (−φ), sehingga spin yang diam kembali sefase penuh."
                        } else {
                            "Skema Stejskal–Tanner (Difusi Air): Molekul air yang berpindah posisi secara acak di antara kedua lobus merasakan kuat medan berbeda pada lobus kedua, sehingga jarum fase tidak kembali sejajar dan sinyal mengalami atenuasi."
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                2 -> {
                    // SUB-TAB 2: Peta Frekuensi Larmor Spasial & Tiga Sumbu X/Y/Z
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val axes = listOf(
                            0 to "Sumbu Z (Kepala–Kaki)",
                            1 to "Sumbu X (Kiri–Kanan)",
                            2 to "Sumbu Y (Depan–Belakang)"
                        )
                        axes.forEach { (idx, label) ->
                            FilterChip(
                                selected = selectedAxis == idx,
                                onClick = { selectedAxis = idx },
                                label = { Text(label, fontSize = 10.sp, maxLines = 1, softWrap = false) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = DeepObsidian,
                                    containerColor = DarkNavySlate,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isOpenMagnet,
                            onClick = { isOpenMagnet = false },
                            label = { Text("Bore Tertutup (B0 Horizontal)", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSuccess,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = isOpenMagnet,
                            onClick = { isOpenMagnet = true },
                            label = { Text("Pesawat Terbuka (B0 Vertikal)", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    val deltaFKhz = (42.58f * amplitudeMtPerM * 0.20f) // at x = +20 cm (0.2 m): 42.58 kHz/mT * mT/m * m
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DarkNavySlate,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Peta Frekuensi pada B0 = 1,5 T (f₀ = 63,870 MHz) dengan G = ${amplitudeMtPerM.toInt()} mT/m:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Ujung negatif (−20 cm): ${fmtId(63.870f - deltaFKhz / 1000f, 3)} MHz (−${fmtId(deltaFKhz, 1)} kHz)\n" +
                                    "• Isosenter (0 cm): 63,870 MHz (tetap f₀, medan gradien = 0)\n" +
                                    "• Ujung positif (+20 cm): ${fmtId(63.870f + deltaFKhz / 1000f, 3)} MHz (+${fmtId(deltaFKhz, 1)} kHz)",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (!isOpenMagnet) {
                                    "Orientasi Bore Tertutup: Sumbu Z sejajar B0 sepanjang terowongan (kepala–kaki), X melintang kiri–kanan, dan Y vertikal depan–belakang."
                                } else {
                                    "Orientasi Pesawat Terbuka: Medan utama B0 dan sumbu Z berarah vertikal (antara kutub atas dan bawah), sedangkan sumbu X dan Y berada pada bidang horizontal meja pasien."
                                },
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. M06B A1: RANTAI SINYAL PENERIMA RF (COIL -> PREAMP -> DEMODULASI -> ADC)
// ============================================================================
private data class RfChainStage(
    val number: Int,
    val name: String,
    val description: String,
    val signalBadge: String
)

private val RF_CHAIN_STAGES = listOf(
    RfChainStage(
        1,
        "Coil",
        "RF coil menangkap sinyal lemah dari tubuh dan mengubahnya menjadi tegangan listrik berosilasi pada frekuensi Larmor. Derau ikut tertangkap.",
        "Lemah, frekuensi VHF, berderau"
    ),
    RfChainStage(
        2,
        "Preamplifier",
        "Memperkuat sinyal sedini mungkin agar tidak tenggelam oleh derau elektronik di tahap berikutnya. Biasanya disusul penyaring dan penguat lanjutan.",
        "Lebih kuat, masih frekuensi VHF"
    ),
    RfChainStage(
        3,
        "Demodulasi",
        "Memisahkan informasi sinyal dari frekuensi Larmor yang membawanya, sehingga tersisa frekuensi yang lebih rendah.",
        "Frekuensi rendah, analog"
    ),
    RfChainStage(
        4,
        "ADC",
        "Mengubah sinyal analog menjadi angka digital yang bisa diproses komputer. Setelah ini data diteruskan untuk disusun menjadi citra.",
        "Digital: deretan angka"
    )
)

@Composable
fun RfSignalChainSimulator() {
    var selectedStageIndex by remember { mutableIntStateOf(0) } // 0..3, -1 for none
    var isAutoPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isAutoPlaying) {
        if (isAutoPlaying) {
            for (idx in 0 until RF_CHAIN_STAGES.size) {
                selectedStageIndex = idx
                delay(1600L)
            }
            isAutoPlaying = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m6b_a1_signal_chain"),
        shape = RoundedCornerShape(16.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = CyanPrimary) {
                    Text(
                        text = "A1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Rantai Sinyal Penerima RF (Coil → ADC)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { isAutoPlaying = !isAutoPlaying },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoPlaying) "Berhenti" else "Putar otomatis",
                            fontSize = 10.sp,
                            color = CyanBright,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            isAutoPlaying = false
                            selectedStageIndex = 0
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reset", fontSize = 10.sp, color = TextSecondary, maxLines = 1, softWrap = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Tombol Tahapan Rantai Sinyal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RF_CHAIN_STAGES.forEachIndexed { idx, stage ->
                    val isSel = selectedStageIndex == idx
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            isAutoPlaying = false
                            selectedStageIndex = idx
                        },
                        modifier = Modifier.testTag("a1_stage_btn_${idx + 1}"),
                        label = {
                            Text(
                                text = "${stage.number}. ${stage.name}",
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
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
                            selected = isSel,
                            borderColor = if (isSel) CyanBright else CardSlateBorder
                        )
                    )
                    if (idx < RF_CHAIN_STAGES.lastIndex) {
                        Text("→", fontSize = 12.sp, color = TextMuted)
                    } else {
                        Text("→ Komputer", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val activeStage = RF_CHAIN_STAGES.getOrElse(selectedStageIndex) { RF_CHAIN_STAGES[0] }

            // Canvas Bentuk Gelombang Sinyal pada Tahap Terpilih
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkNavySlate)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h * 0.48f

                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(8.dp.toPx(), midY),
                        end = Offset(w - 8.dp.toPx(), midY),
                        strokeWidth = 1.dp.toPx()
                    )

                    fun pseudoNoise(x: Int): Float =
                        (sin(x * 12.9898f) * 0.5f + sin(x * 78.233f) * 0.5f)

                    val startX = 10f
                    val endX = w - 10f
                    val steps = 160

                    if (selectedStageIndex <= 1) {
                        val amp = if (selectedStageIndex == 0) h * 0.14f else h * 0.36f
                        val nz = if (selectedStageIndex == 0) h * 0.06f else h * 0.12f
                        val path = Path()
                        for (i in 0..steps) {
                            val ratio = i.toFloat() / steps
                            val x = startX + ratio * (endX - startX)
                            val y = midY - amp * sin(i * 1.15f) - nz * pseudoNoise(i)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, color = CyanBright, style = Stroke(width = 2.dp.toPx()))
                    } else if (selectedStageIndex == 2) {
                        val amp = h * 0.36f
                        val path = Path()
                        for (i in 0..steps) {
                            val ratio = i.toFloat() / steps
                            val x = startX + ratio * (endX - startX)
                            val y = midY - amp * sin(ratio * 4f * PI.toFloat())
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, color = CyanBright, style = Stroke(width = 2.8.dp.toPx()))
                    } else {
                        val amp = h * 0.36f
                        val path = Path()
                        for (i in 0..steps) {
                            val ratio = i.toFloat() / steps
                            val x = startX + ratio * (endX - startX)
                            val y = midY - amp * sin(ratio * 4f * PI.toFloat())
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, color = CardSlateBorder, style = Stroke(width = 1.5.dp.toPx()))

                        val sampleCount = 32
                        for (s in 0..sampleCount) {
                            val ratio = s.toFloat() / sampleCount
                            val x = startX + ratio * (endX - startX)
                            val rawY = midY - amp * sin(ratio * 4f * PI.toFloat())
                            val quantizedY = (rawY / 6f).roundToInt() * 6f
                            drawLine(
                                color = AmberWarning.copy(alpha = 0.45f),
                                start = Offset(x, midY),
                                end = Offset(x, quantizedY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawRect(
                                color = AmberWarning,
                                topLeft = Offset(x - 3.dp.toPx(), quantizedY - 3.dp.toPx()),
                                size = Size(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(6.dp),
                    color = DeepObsidian.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = activeStage.signalBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${activeStage.number}. ${activeStage.name}: ${activeStage.description}",
                fontSize = 11.sp,
                color = TextPrimary,
                lineHeight = 17.sp
            )
        }
    }
}

// ============================================================================
// 3. M06B A2, A3, A4: PEMILIH KOIL, SENSITIVITAS KEDALAMAN & ARRAY TULANG BELAKANG
// ============================================================================
private data class AnatomyCoilChoice(
    val anatomy: String,
    val exampleCoil: String,
    val categoryBadge: String,
    val explanation: String
)

private val ANATOMY_COIL_CHOICES = listOf(
    AnatomyCoilChoice(
        "Kepala",
        "Coil kepala",
        "Coil volume (mengelilingi)",
        "Mengelilingi kepala dengan jarak dekat, jadi SNR relatif tinggi dan merata untuk daerah itu."
    ),
    AnatomyCoilChoice(
        "Perut atau dada",
        "Coil body atau array fasa",
        "Array fasa",
        "Array fasa menutup area luas. SNR relatif baik, dan parallel imaging dimungkinkan."
    ),
    AnatomyCoilChoice(
        "Sendi dangkal (bahu, lutut)",
        "Coil permukaan",
        "Permukaan",
        "Dekat dengan jaringan yang dicitrakan, jadi SNR relatif tinggi. Jangkauan kedalamannya terbatas."
    ),
    AnatomyCoilChoice(
        "Tulang belakang",
        "Array fasa linear",
        "Array fasa",
        "Beberapa elemen tersusun memanjang. Cakupan luas dengan SNR relatif baik (lihat A4 di bawah)."
    ),
    AnatomyCoilChoice(
        "Tangan atau pergelangan",
        "Coil permukaan kecil / khusus pergelangan",
        "Permukaan",
        "Melingkupi atau menempel pada tangan, jadi SNR relatif tinggi. Cakupannya kecil."
    )
)

@Composable
fun CoilSelectorAndDepthSimulator() {
    var selectedAnatomyIdx by remember { mutableIntStateOf(0) }
    var loopDiameter by remember { mutableFloatStateOf(2.0f) } // 1.0 .. 4.0
    var targetDepth by remember { mutableFloatStateOf(1.0f) } // 0.0 .. 4.0
    var activeSpineElements by remember { mutableIntStateOf(2) } // 1 .. 6

    val volumeSensitivity = 0.35f
    fun surfaceSensitivity(d: Float, z: Float): Float =
        ((d * d) / (d * d + z * z)).toDouble().pow(1.5).toFloat()

    val currentSt = surfaceSensitivity(loopDiameter, targetDepth)
    val crossoverDepth = (loopDiameter * sqrt(volumeSensitivity.toDouble().pow(-2.0 / 3.0) - 1.0)).toFloat()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m6b_a2_a3_a4_coil_simulator"),
        shape = RoundedCornerShape(16.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = CyanPrimary) {
                    Text(
                        text = "A2 • A3 • A4",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                OutlinedButton(
                    onClick = {
                        selectedAnatomyIdx = 0
                        loopDiameter = 2.0f
                        targetDepth = 1.0f
                        activeSpineElements = 1
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Reset", fontSize = 10.sp, color = CyanBright)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pemilih Coil, Sensitivitas Kedalaman & Array",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ================= BAGIAN A2: PEMILIH COIL MENURUT ANATOMI =================
            Text(
                text = "A2 · Pilih Bagian Tubuh (Ketuk Chip atau Siluet)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright
            )
            Spacer(modifier = Modifier.height(6.dp))

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ANATOMY_COIL_CHOICES.forEachIndexed { idx, item ->
                    val sel = selectedAnatomyIdx == idx
                    FilterChip(
                        selected = sel,
                        onClick = { selectedAnatomyIdx = idx },
                        label = { Text(item.anatomy, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = DeepObsidian,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val choice = ANATOMY_COIL_CHOICES[selectedAnatomyIdx]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Body Silhouette Canvas
                Box(
                    modifier = Modifier
                        .width(105.dp)
                        .height(155.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkNavySlate)
                        .border(1.dp, CardSlateBorder, RoundedCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val nx = offset.x / size.width
                                val ny = offset.y / size.height
                                selectedAnatomyIdx = when {
                                    ny < 0.24f -> 0 // Kepala
                                    nx in 0.42f..0.58f && ny in 0.24f..0.64f -> 3 // Tulang belakang
                                    nx in 0.26f..0.74f && ny in 0.24f..0.64f -> 1 // Perut/dada
                                    ny > 0.56f && (nx < 0.28f || nx > 0.72f) -> 4 // Tangan
                                    else -> 2 // Sendi
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f

                        // Siluet tubuh dasar
                        drawCircle(color = CardSlateBorder, radius = w * 0.14f, center = Offset(cx, h * 0.13f), style = Stroke(2.dp.toPx()))
                        drawRoundRect(
                            color = CardSlateBorder,
                            topLeft = Offset(cx - w * 0.21f, h * 0.24f),
                            size = Size(w * 0.42f, h * 0.38f),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(2.dp.toPx())
                        )
                        // Lengan kiri & kanan
                        drawRoundRect(
                            color = CardSlateBorder,
                            topLeft = Offset(cx - w * 0.36f, h * 0.26f),
                            size = Size(w * 0.11f, h * 0.34f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(1.8.dp.toPx())
                        )
                        drawRoundRect(
                            color = CardSlateBorder,
                            topLeft = Offset(cx + w * 0.25f, h * 0.26f),
                            size = Size(w * 0.11f, h * 0.34f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(1.8.dp.toPx())
                        )
                        // Tungkai kiri & kanan
                        drawRoundRect(
                            color = CardSlateBorder,
                            topLeft = Offset(cx - w * 0.18f, h * 0.62f),
                            size = Size(w * 0.14f, h * 0.32f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(1.8.dp.toPx())
                        )
                        drawRoundRect(
                            color = CardSlateBorder,
                            topLeft = Offset(cx + w * 0.04f, h * 0.62f),
                            size = Size(w * 0.14f, h * 0.32f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(1.8.dp.toPx())
                        )

                        // Highlight zona terpilih
                        when (selectedAnatomyIdx) {
                            0 -> drawCircle(color = CyanBright.copy(alpha = 0.35f), radius = w * 0.18f, center = Offset(cx, h * 0.13f))
                            1 -> drawRoundRect(
                                color = CyanBright.copy(alpha = 0.35f),
                                topLeft = Offset(cx - w * 0.24f, h * 0.26f),
                                size = Size(w * 0.48f, h * 0.34f),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                            2 -> {
                                drawCircle(color = AmberWarning.copy(alpha = 0.45f), radius = w * 0.10f, center = Offset(cx - w * 0.25f, h * 0.29f))
                                drawCircle(color = AmberWarning.copy(alpha = 0.45f), radius = w * 0.10f, center = Offset(cx - w * 0.11f, h * 0.76f))
                                drawCircle(color = AmberWarning.copy(alpha = 0.45f), radius = w * 0.10f, center = Offset(cx + w * 0.11f, h * 0.76f))
                            }
                            3 -> drawRoundRect(
                                color = EmeraldSuccess.copy(alpha = 0.45f),
                                topLeft = Offset(cx - w * 0.06f, h * 0.24f),
                                size = Size(w * 0.12f, h * 0.38f),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                            4 -> {
                                drawCircle(color = CyanBright.copy(alpha = 0.45f), radius = w * 0.10f, center = Offset(cx - w * 0.31f, h * 0.62f))
                                drawCircle(color = CyanBright.copy(alpha = 0.45f), radius = w * 0.10f, center = Offset(cx + w * 0.31f, h * 0.62f))
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                    ) {
                        Text(
                            text = choice.categoryBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rekomendasi: ${choice.exampleCoil}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = choice.explanation,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= BAGIAN A3: SENSITIVITAS COIL TERHADAP KEDALAMAN =================
            Text(
                text = "A3 · Sensitivitas Loop Permukaan vs Kedalaman: s = (D² / (D² + z²))^1,5",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Diameter loop (D): ${fmtId(loopDiameter, 2)}", fontSize = 11.sp, color = TextPrimary)
                Text("Kedalaman target (z): ${fmtId(targetDepth, 2)}", fontSize = 11.sp, color = AmberWarning)
            }
            Slider(
                value = loopDiameter,
                onValueChange = { loopDiameter = (it * 4f).roundToInt() / 4f },
                valueRange = 1f..4f,
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )
            Slider(
                value = targetDepth,
                onValueChange = { targetDepth = (it * 4f).roundToInt() / 4f },
                valueRange = 0f..4f,
                colors = SliderDefaults.colors(thumbColor = AmberWarning, activeTrackColor = AmberWarning)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkNavySlate)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val x0 = 18.dp.toPx()
                    val x1 = w - 12.dp.toPx()
                    val yT = 10.dp.toPx()
                    val yB = h - 18.dp.toPx()

                    fun toX(z: Float) = x0 + (x1 - x0) * (z / 4f)
                    fun toY(s: Float) = yB - (yB - yT) * s

                    drawLine(CardSlateBorder, Offset(x0, yT), Offset(x0, yB), strokeWidth = 1.2.dp.toPx())
                    drawLine(CardSlateBorder, Offset(x0, yB), Offset(x1, yB), strokeWidth = 1.2.dp.toPx())

                    // Garis coil volume (0.35 merata)
                    drawLine(
                        color = EmeraldSuccess,
                        start = Offset(x0, toY(volumeSensitivity)),
                        end = Offset(x1, toY(volumeSensitivity)),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )

                    // Kurva loop permukaan
                    val path = Path()
                    val steps = 100
                    for (i in 0..steps) {
                        val z = 4f * i / steps
                        val s = surfaceSensitivity(loopDiameter, z)
                        val px = toX(z)
                        val py = toY(s)
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(path, color = CyanBright, style = Stroke(width = 2.8.dp.toPx()))

                    // Titik target z
                    val tx = toX(targetDepth)
                    val ty = toY(currentSt)
                    drawLine(
                        color = AmberWarning,
                        start = Offset(tx, yB),
                        end = Offset(tx, ty),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawCircle(color = AmberWarning, radius = 5.dp.toPx(), center = Offset(tx, ty))
                }

                Text(
                    text = "● Biru: Loop permukaan | -- Hijau: Coil volume (35%)",
                    fontSize = 9.sp,
                    color = CyanBright,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            val winner = if (currentSt >= volumeSensitivity) "loop permukaan" else "coil volume"
            Text(
                text = "Pada kedalaman z = ${fmtId(targetDepth, 2)}, sensitivitas loop permukaan adalah ${(currentSt * 100).roundToInt()}% (unggul sampai z ≈ ${fmtId(crossoverDepth, 2)}), sehingga di titik ini $winner lebih unggul.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ================= BAGIAN A4: ARRAY FASA TULANG BELAKANG (6 KANAL) =================
            Text(
                text = "A4 · Array Fasa Tulang Belakang: $activeSpineElements dari 6 Kanal Aktif",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright
            )
            Slider(
                value = activeSpineElements.toFloat(),
                onValueChange = { activeSpineElements = it.roundToInt().coerceIn(1, 6) },
                valueRange = 1f..6f,
                steps = 4,
                colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkNavySlate)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val boxW = (w - 16.dp.toPx()) / 6f

                    for (i in 0 until 6) {
                        val isOn = i < activeSpineElements
                        val x = 8.dp.toPx() + i * boxW
                        drawRoundRect(
                            color = if (isOn) EmeraldSuccess.copy(alpha = 0.32f) else CardSlate.copy(alpha = 0.25f),
                            topLeft = Offset(x + 2.dp.toPx(), 6.dp.toPx()),
                            size = Size(boxW - 4.dp.toPx(), h * 0.58f),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (isOn) EmeraldSuccess else CardSlateBorder,
                            topLeft = Offset(x + 2.dp.toPx(), 6.dp.toPx()),
                            size = Size(boxW - 4.dp.toPx(), h * 0.58f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(width = if (isOn) 2.2.dp.toPx() else 1.dp.toPx())
                        )
                    }

                    // Garis rentang FOV aktif
                    val fovEndX = 8.dp.toPx() + activeSpineElements * boxW - 2.dp.toPx()
                    drawLine(
                        color = AmberWarning,
                        start = Offset(10.dp.toPx(), h * 0.84f),
                        end = Offset(fovEndX, h * 0.84f),
                        strokeWidth = 3.5.dp.toPx()
                    )
                }
            }
            val acquisitions = ceil(activeSpineElements / 2.0).toInt()
            Text(
                text = "$activeSpineElements elemen aktif (FOV = $activeSpineElements segmen, sekitar $acquisitions akuisisi dengan pergeseran meja ke isosenter). SNR tinggi loop kecil tetap terjaga di sepanjang tulang belakang.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ============================================================================
// 4. M06B A5: PERISAI RF (KANDANG FARADAY) & ARTEFAK KEBOCORAN RADIO
// ============================================================================
@Composable
fun FaradayShieldArtifactSimulator() {
    var isExternalRadioOn by remember { mutableStateOf(false) }
    var isFaradayInstalled by remember { mutableStateOf(true) }
    var artifactDamage by remember { mutableFloatStateOf(0f) } // 0f .. 1f
    var wavePhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isExternalRadioOn, isFaradayInstalled) {
        while (isExternalRadioOn || artifactDamage > 0f) {
            delay(50L)
            if (isExternalRadioOn) {
                wavePhase = (wavePhase + 0.06f) % 1f
            }
            val isLeaking = isExternalRadioOn && !isFaradayInstalled
            artifactDamage = if (isLeaking) {
                (artifactDamage + 0.025f).coerceAtMost(1f)
            } else {
                (artifactDamage - 0.06f).coerceAtLeast(0f)
            }
        }
    }

    val damagePercent = (artifactDamage * 100f).roundToInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m6b_a5_faraday_simulator"),
        shape = RoundedCornerShape(16.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = CyanPrimary) {
                    Text(
                        text = "A5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                OutlinedButton(
                    onClick = {
                        isExternalRadioOn = false
                        isFaradayInstalled = true
                        artifactDamage = 0f
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Reset", fontSize = 10.sp, color = CyanBright)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Perisai RF (Kandang Faraday) & Artefak",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2 Toggle Buttons sesuai m06b-native.html
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isExternalRadioOn,
                    onClick = { isExternalRadioOn = !isExternalRadioOn },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a5_toggle_radio_source"),
                    label = {
                        Text(
                            text = "Sumber radio luar: ${if (isExternalRadioOn) "nyala" else "mati"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = isFaradayInstalled,
                    onClick = { isFaradayInstalled = !isFaradayInstalled },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a5_toggle_faraday_cage"),
                    label = {
                        Text(
                            text = "Kandang Faraday: ${if (isFaradayInstalled) "terpasang" else "dilepas"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSuccess,
                        selectedLabelColor = DeepObsidian,
                        containerColor = RoseDark,
                        labelColor = TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Denah Ruang Pindai MRI + Citra Aksial di Sampingnya
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kiri: Denah Ruang Perisai RF
                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .height(165.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkNavySlate)
                        .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Dinding Kandang Faraday
                        val rx = w * 0.24f
                        val ry = h * 0.16f
                        val rw = w * 0.68f
                        val rh = h * 0.68f

                        drawRoundRect(
                            color = if (isFaradayInstalled) CyanBright else RoseDanger.copy(alpha = 0.5f),
                            topLeft = Offset(rx, ry),
                            size = Size(rw, rh),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(
                                width = if (isFaradayInstalled) 3.dp.toPx() else 1.8.dp.toPx(),
                                pathEffect = if (isFaradayInstalled) null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )

                        // Jendela kawat jala (atas), pintu logam (bawah kiri), waveguide (bawah kanan), ventilasi sarang lebah (kanan)
                        drawLine(EmeraldSuccess, Offset(rx + rw * 0.55f, ry), Offset(rx + rw * 0.80f, ry), strokeWidth = 4.dp.toPx())
                        drawLine(AmberWarning, Offset(rx + rw * 0.12f, ry + rh), Offset(rx + rw * 0.34f, ry + rh), strokeWidth = 4.dp.toPx())
                        drawLine(CyanPrimary, Offset(rx + rw * 0.58f, ry + rh), Offset(rx + rw * 0.82f, ry + rh), strokeWidth = 4.dp.toPx())

                        // Pesawat MRI di dalam ruang
                        val mriX = rx + rw * 0.28f
                        val mriY = ry + rh * 0.25f
                        val mriW = rw * 0.42f
                        val mriH = rh * 0.50f
                        drawRoundRect(
                            color = DeepObsidian,
                            topLeft = Offset(mriX, mriY),
                            size = Size(mriW, mriH),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                        drawRoundRect(
                            color = CardSlateBorder,
                            topLeft = Offset(mriX, mriY),
                            size = Size(mriW, mriH),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(1.8.dp.toPx())
                        )

                        // Menara Radio Luar di Kiri
                        val tx = w * 0.08f
                        val ty = h * 0.45f
                        drawCircle(
                            color = if (isExternalRadioOn) AmberWarning else TextMuted,
                            radius = 5.dp.toPx(),
                            center = Offset(tx, ty)
                        )
                        drawLine(TextMuted, Offset(tx, ty), Offset(tx - 6.dp.toPx(), h * 0.78f), strokeWidth = 2.dp.toPx())
                        drawLine(TextMuted, Offset(tx, ty), Offset(tx + 6.dp.toPx(), h * 0.78f), strokeWidth = 2.dp.toPx())

                        // Gelombang Radio Merambat
                        if (isExternalRadioOn) {
                            val maxReach = if (isFaradayInstalled) rx - tx else (mriX + mriW / 2f) - tx
                            for (i in 0..3) {
                                val f = (wavePhase + i * 0.25f) % 1f
                                val r = 8.dp.toPx() + f * maxReach
                                drawArc(
                                    color = AmberWarning.copy(alpha = (1f - f * 0.6f).coerceIn(0.15f, 1f)),
                                    startAngle = -35f,
                                    sweepAngle = 70f,
                                    useCenter = false,
                                    topLeft = Offset(tx - r, ty - r),
                                    size = Size(r * 2f, r * 2f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isFaradayInstalled) "Sangkar Faraday Aktif" else "TANPA PERISAI (Bocor!)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFaradayInstalled) CyanBright else RoseDanger,
                        modifier = Modifier.align(Alignment.TopStart),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Pintu • Panel • Sarang Lebah",
                        fontSize = 8.sp,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                // Kanan: Kanvas Citra Irisan Aksial + Artefak Garis/Bintik
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(165.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141820))
                        .border(
                            1.dp,
                            if (artifactDamage > 0.1f) RoseDanger else EmeraldSuccess.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(6.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        // Anatomi aksial dasar (kepala/badan)
                        drawOval(
                            color = Color(0xFF8C96A4),
                            topLeft = Offset(cx - w * 0.42f, cy - h * 0.38f),
                            size = Size(w * 0.84f, h * 0.76f)
                        )
                        drawOval(
                            color = Color(0xFFDCE2EA),
                            topLeft = Offset(cx - w * 0.40f, cy - h * 0.36f),
                            size = Size(w * 0.80f, h * 0.72f),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        drawOval(
                            color = Color(0xFF56606E),
                            topLeft = Offset(cx - w * 0.30f, cy - h * 0.27f),
                            size = Size(w * 0.60f, h * 0.54f)
                        )
                        // Ventrikel gelap di tengah
                        drawOval(
                            color = Color(0xFF12161F),
                            topLeft = Offset(cx - w * 0.12f, cy - h * 0.10f),
                            size = Size(w * 0.08f, h * 0.20f)
                        )
                        drawOval(
                            color = Color(0xFF12161F),
                            topLeft = Offset(cx + w * 0.04f, cy - h * 0.10f),
                            size = Size(w * 0.08f, h * 0.20f)
                        )

                        // Overlay Artefak RF (pita & garis zipper/noise)
                        if (artifactDamage > 0.01f) {
                            val totalLines = (artifactDamage * 36).roundToInt()
                            for (i in 0 until totalLines) {
                                val lx = ((i * 37 + 13) % 100) / 100f * w
                                val isBright = i % 2 == 0
                                drawLine(
                                    color = (if (isBright) Color.White else Color.Black).copy(
                                        alpha = (artifactDamage * 0.85f).coerceIn(0.15f, 0.9f)
                                    ),
                                    start = Offset(lx, 0f),
                                    end = Offset(lx, h),
                                    strokeWidth = if (artifactDamage > 0.5f && i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                                )
                            }
                        }
                    }

                    Text(
                        text = "Gangguan: $damagePercent%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (damagePercent == 0) EmeraldSuccess else RoseDanger,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { artifactDamage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (artifactDamage > 0.3f) RoseDanger else CyanBright,
                trackColor = DarkNavySlate
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    !isExternalRadioOn && isFaradayInstalled -> "Tidak ada gangguan radio. Citra bersih."
                    !isExternalRadioOn && !isFaradayInstalled -> "Perisai dilepas, tetapi sumber radio luar mati. Nyalakan sumber radio luar untuk mengamati kebocoran."
                    isExternalRadioOn && isFaradayInstalled -> "Sumber radio luar menyala, tetapi kandang Faraday menahannya di dinding. Citra tetap bersih (0%)."
                    else -> "Perisai dilepas saat radio luar menyala ($damagePercent%): gelombang VHF luar masuk ke koil penerima dan menimbulkan pita artefak pada citra!"
                },
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

// ============================================================================
// 5. M06B A6, A7, A8: PERGESERAN KIMIA, KETIDAKHOMOGENAN B1 (pTx) & BIRDCAGE
// ============================================================================
@Composable
fun BirdcageAndB1ParallelTransmitSimulator(initialSection: Int = 0) {
    var activeSection by remember(initialSection) { mutableIntStateOf(initialSection) }

    // A6 State: Spektrum Pergeseran Kimia Air & Lemak
    var b0Chem by remember { mutableFloatStateOf(1.5f) } // 0.5 .. 3.0 T
    var isPpmAxis by remember { mutableStateOf(false) } // false = Hz, true = ppm

    // A7 State: Ketidakhomogenan B1 & Transmit Paralel
    var b0Ptx by remember { mutableFloatStateOf(3.0f) } // 1.5f or 3.0f
    var isParallelTransmitOn by remember { mutableStateOf(false) }

    // A8 State: Birdcage Kuadratur & Faktor Pengisi
    var isQuadrature by remember { mutableStateOf(true) }
    var rfPhaseDeg by remember { mutableFloatStateOf(45f) }
    var isBirdcagePlaying by remember { mutableStateOf(false) }
    var isHeadAnatomy by remember { mutableStateOf(true) } // true = kepala (18 cm), false = badan (35 cm)
    var coilDiameterCm by remember { mutableFloatStateOf(28f) } // 15 .. 65 cm

    LaunchedEffect(isBirdcagePlaying) {
        while (isBirdcagePlaying) {
            delay(40L)
            rfPhaseDeg = (rfPhaseDeg + 6f) % 360f
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m6b_a6_a7_a8_simulator"),
        shape = RoundedCornerShape(16.dp),
        color = DeepObsidian,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = CyanPrimary) {
                    Text(
                        text = "A6 • A7 • A8",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                OutlinedButton(
                    onClick = {
                        b0Chem = 1.5f
                        isPpmAxis = false
                        b0Ptx = 3.0f
                        isParallelTransmitOn = false
                        isQuadrature = true
                        isBirdcagePlaying = false
                        rfPhaseDeg = 45f
                        isHeadAnatomy = true
                        coilDiameterCm = 28f
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Reset", fontSize = 10.sp, color = CyanBright)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Birdcage Kuadratur, Pergeseran Kimia & pTx",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val sections = listOf(
                    0 to "A8: Birdcage Kuadratur & Faktor Pengisi",
                    1 to "A6: Spektrum Pergeseran Kimia (Hz vs ppm)",
                    2 to "A7: Peta B1 & Transmit Paralel (pTx)"
                )
                sections.forEach { (idx, title) ->
                    val sel = activeSection == idx
                    FilterChip(
                        selected = sel,
                        onClick = { activeSection = idx },
                        label = { Text(title, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = if (sel) CyanPrimary else CardSlateBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeSection) {
                0 -> {
                    // ================= A8: BIRDCAGE KUADRATUR & FAKTOR PENGISI =================
                    Text(
                        text = "Bagian 1 · Medan B1 Berputar pada Birdcage (8 Batang)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !isQuadrature,
                            onClick = { isQuadrature = false },
                            label = { Text("Linear (1 suplai)", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = isQuadrature,
                            onClick = { isQuadrature = true },
                            label = { Text("Kuadratur (2 suplai, tunda 90°)", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        OutlinedButton(
                            onClick = { isBirdcagePlaying = !isBirdcagePlaying },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isBirdcagePlaying) "Berhenti" else "Putar otomatis",
                                fontSize = 10.sp,
                                color = CyanBright,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fase siklus RF: ${rfPhaseDeg.roundToInt()}°", fontSize = 11.sp, color = TextPrimary)
                        Text(
                            text = if (isQuadrature) "Efisiensi B1: 100% searah spin" else "Efisiensi B1: 50% (separuh terbuang)",
                            fontSize = 10.sp,
                            color = if (isQuadrature) EmeraldSuccess else AmberWarning
                        )
                    }
                    Slider(
                        value = rfPhaseDeg,
                        onValueChange = {
                            isBirdcagePlaying = false
                            rfPhaseDeg = it
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkNavySlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w * 0.26f
                            val cy = h * 0.50f
                            val rCage = (h * 0.38f).coerceAtMost(w * 0.20f)
                            val rad = rfPhaseDeg * PI.toFloat() / 180f

                            // Lingkaran Birdcage & 8 batang konduktor
                            drawCircle(color = CardSlateBorder, radius = rCage, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
                            for (i in 0 until 8) {
                                val a = i * PI.toFloat() / 4f
                                drawCircle(
                                    color = CyanBright,
                                    radius = 4.dp.toPx(),
                                    center = Offset(cx + rCage * cos(a), cy + rCage * sin(a))
                                )
                            }

                            val vecLen = rCage * 0.76f
                            if (isQuadrature) {
                                drawVectorArrow(
                                    start = Offset(cx, cy),
                                    end = Offset(cx + vecLen * cos(rad), cy - vecLen * sin(rad)),
                                    color = CyanBright,
                                    strokeW = 3.5.dp.toPx()
                                )
                            } else {
                                // Vektor linear horizontal + 2 komponen berputar berlawanan
                                drawVectorArrow(
                                    start = Offset(cx, cy),
                                    end = Offset(cx + vecLen * cos(rad), cy),
                                    color = TextPrimary,
                                    strokeW = 3.2.dp.toPx()
                                )
                                drawVectorArrow(
                                    start = Offset(cx, cy),
                                    end = Offset(cx + 0.5f * vecLen * cos(rad), cy - 0.5f * vecLen * sin(rad)),
                                    color = CyanBright,
                                    strokeW = 2.2.dp.toPx()
                                )
                                drawVectorArrow(
                                    start = Offset(cx, cy),
                                    end = Offset(cx + 0.5f * vecLen * cos(rad), cy + 0.5f * vecLen * sin(rad)),
                                    color = AmberWarning.copy(alpha = 0.7f),
                                    strokeW = 2.dp.toPx()
                                )
                            }

                            // Grafik Gelombang Suplai 1 (0°) & Suplai 2 (90°) di Sisi Kanan
                            val gx = w * 0.52f
                            val gw = w * 0.44f
                            val gy = h * 0.50f
                            val ga = h * 0.34f
                            drawLine(CardSlateBorder, Offset(gx, gy), Offset(gx + gw, gy), strokeWidth = 1.dp.toPx())

                            val path1 = Path()
                            val path2 = Path()
                            for (i in 0..80) {
                                val p = 2f * PI.toFloat() * i / 80f
                                val x = gx + gw * i / 80f
                                val y1 = gy - ga * cos(p)
                                val y2 = gy - ga * sin(p)
                                if (i == 0) {
                                    path1.moveTo(x, y1)
                                    path2.moveTo(x, y2)
                                } else {
                                    path1.lineTo(x, y1)
                                    path2.lineTo(x, y2)
                                }
                            }
                            drawPath(path1, color = AmberWarning, style = Stroke(width = 2.2.dp.toPx()))
                            if (isQuadrature) {
                                drawPath(
                                    path2,
                                    color = CyanBright,
                                    style = Stroke(
                                        width = 2.2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                    )
                                )
                            }

                            val markerX = gx + gw * (rfPhaseDeg / 360f)
                            drawLine(TextMuted, Offset(markerX, gy - ga), Offset(markerX, gy + ga), strokeWidth = 1.dp.toPx())
                            drawCircle(TextPrimary, radius = 4.dp.toPx(), center = Offset(markerX, gy - ga * cos(rad)))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bagian 2: Faktor Pengisi (Filling Factor)
                    val anatomyDiamCm = if (isHeadAnatomy) 18f else 35f
                    val fitsInside = anatomyDiamCm <= coilDiameterCm * 0.95f
                    val fillingFactor = if (fitsInside) ((anatomyDiamCm / coilDiameterCm).pow(2)).coerceAtMost(1f) else 0f

                    Text(
                        text = "Bagian 2 · Faktor Pengisi (Filling Factor ≈ ${(fillingFactor * 100).roundToInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isHeadAnatomy,
                            onClick = { isHeadAnatomy = true },
                            label = { Text("Anatomi: Kepala (18 cm)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = !isHeadAnatomy,
                            onClick = {
                                isHeadAnatomy = false
                                if (coilDiameterCm < 37f) coilDiameterCm = 38f
                            },
                            label = { Text("Anatomi: Badan (35 cm)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    Text(
                        text = "Diameter dalam coil: ${coilDiameterCm.roundToInt()} cm (Anatomi: ${anatomyDiamCm.roundToInt()} cm)",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Slider(
                        value = coilDiameterCm,
                        onValueChange = { coilDiameterCm = it.roundToInt().toFloat() },
                        valueRange = 15f..65f,
                        colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
                    )
                    Text(
                        text = if (!fitsInside) {
                            "Anatomi (${anatomyDiamCm.toInt()} cm) tidak muat di dalam coil (${coilDiameterCm.toInt()} cm). Perbesar diameter coil!"
                        } else {
                            "Faktor pengisi sekitar ${(fillingFactor * 100).roundToInt()}%. Coil yang pas dengan anatomi memberi faktor pengisi tinggi dan SNR optimal."
                        },
                        fontSize = 11.sp,
                        color = if (!fitsInside) RoseDanger else TextSecondary
                    )
                }

                1 -> {
                    // ================= A6: SPEKTRUM PERGESERAN KIMIA AIR & LEMAK =================
                    val larmorMhz = 42.58f * b0Chem
                    val shiftHz = 3.5f * larmorMhz

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1.5f, 3.0f).forEach { presetB ->
                                FilterChip(
                                    selected = kotlin.math.abs(b0Chem - presetB) < 0.05f,
                                    onClick = { b0Chem = presetB },
                                    label = { Text("${fmtId(presetB, 1)} T", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary,
                                        selectedLabelColor = DeepObsidian,
                                        containerColor = DarkNavySlate,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                        FilterChip(
                            selected = isPpmAxis,
                            onClick = { isPpmAxis = !isPpmAxis },
                            label = {
                                Text(
                                    text = "Sumbu: ${if (isPpmAxis) "ppm (Tetap)" else "Hz (Naik bersama B0)"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = CyanBright
                            )
                        )
                    }

                    Text(
                        text = "Kuat Medan B₀: ${fmtId(b0Chem, 1)} T (Larmor: ${fmtId(larmorMhz, 2)} MHz) → Δf = ${shiftHz.roundToInt()} Hz (3,5 ppm)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Slider(
                        value = b0Chem,
                        onValueChange = { b0Chem = (it * 2f).roundToInt() / 2f },
                        valueRange = 0.5f..3.0f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkNavySlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val m = 24.dp.toPx()
                            val base = h - 22.dp.toPx()

                            drawLine(CardSlateBorder, Offset(m, base), Offset(w - m, base), strokeWidth = 1.5.dp.toPx())

                            val maxAxis = if (isPpmAxis) 3.5f * 1.25f else (3.5f * 42.58f * 3f) * 1.25f
                            val currentShift = if (isPpmAxis) 3.5f else shiftHz
                            fun toPx(v: Float) = m + (v / maxAxis) * (w - 2f * m)

                            fun drawSpectralPeak(centerPx: Float, peakH: Float, col: Color) {
                                val path = Path()
                                val startX = m.toInt()
                                val endX = (w - m).toInt()
                                for (x in startX..endX step 2) {
                                    val xf = x.toFloat()
                                    val y = base - peakH * exp(-((xf - centerPx) / 14f).pow(2))
                                    if (x == startX) path.moveTo(xf, y) else path.lineTo(xf, y)
                                }
                                drawPath(path, color = col, style = Stroke(width = 2.8.dp.toPx()))
                            }

                            drawSpectralPeak(toPx(0f), h * 0.68f, CyanBright)
                            drawSpectralPeak(toPx(currentShift), h * 0.44f, AmberWarning)
                        }
                        Text(
                            text = "● Puncak Air (0)   ● Puncak Lemak (${if (isPpmAxis) "3,5 ppm" else "${shiftHz.roundToInt()} Hz"})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPpmAxis) {
                            "Pada sumbu ppm, jarak puncak lemak dan air selalu tetap 3,5 ppm karena perisai elektron sebanding dengan B0."
                        } else {
                            "Pada sumbu Hz, jarak puncak lemak–air melebar saat B0 naik: ~224 Hz pada 1,5 T dan ~447 Hz pada 3,0 T."
                        },
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                2 -> {
                    // ================= A7: KETIDAKHOMOGENAN B1 & TRANSMIT PARALEL (pTx) =================
                    val larmor = 42.58f * b0Ptx
                    val wavelengthCm = (299.792458f * 100f / (larmor * sqrt(80f))).roundToInt() // ~52 cm at 1.5T, ~26 cm at 3T

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = b0Ptx == 1.5f,
                            onClick = { b0Ptx = 1.5f },
                            label = { Text("1,5 T (λ ≈ 52 cm)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = b0Ptx == 3.0f,
                            onClick = { b0Ptx = 3.0f },
                            label = { Text("3,0 T (λ ≈ 26 cm)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = isParallelTransmitOn,
                            onClick = { isParallelTransmitOn = !isParallelTransmitOn },
                            label = {
                                Text(
                                    text = "Transmit paralel: ${if (isParallelTransmitOn) "hidup" else "mati"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSuccess,
                                selectedLabelColor = DeepObsidian,
                                containerColor = DarkNavySlate,
                                labelColor = AmberWarning
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkNavySlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f
                            val rx = w * 0.34f
                            val ry = h * 0.36f

                            // Cincin elips gradien gelombang berdiri B1 di dalam potongan badan (35 cm)
                            val amp = if (isParallelTransmitOn) 0.06f else if (b0Ptx >= 3.0f) 0.42f else 0.16f
                            for (ring in 10 downTo 1) {
                                val frac = ring / 10f
                                val rCm = frac * 17.5f
                                val b1Val = 1f + amp * cos(2f * PI.toFloat() * rCm / wavelengthCm.toFloat())
                                val brightness = (0.35f + 0.45f * b1Val).coerceIn(0.18f, 0.92f)
                                drawOval(
                                    color = Color(brightness * 0.7f, brightness * 0.85f, brightness),
                                    topLeft = Offset(cx - rx * frac, cy - ry * frac),
                                    size = Size(rx * 2f * frac, ry * 2f * frac)
                                )
                            }

                            drawOval(
                                color = CardSlateBorder,
                                topLeft = Offset(cx - rx, cy - ry),
                                size = Size(rx * 2f, ry * 2f),
                                style = Stroke(2.dp.toPx())
                            )

                            if (isParallelTransmitOn) {
                                // 4 Elemen pemancar independen di sekeliling badan
                                listOf(
                                    Offset(cx, cy - ry - 10.dp.toPx()),
                                    Offset(cx, cy + ry + 10.dp.toPx()),
                                    Offset(cx - rx - 10.dp.toPx(), cy),
                                    Offset(cx + rx + 10.dp.toPx(), cy)
                                ).forEach { pt ->
                                    drawCircle(color = EmeraldSuccess, radius = 6.dp.toPx(), center = pt)
                                }
                            } else {
                                drawOval(
                                    color = AmberWarning,
                                    topLeft = Offset(cx - rx - 8.dp.toPx(), cy - ry - 8.dp.toPx()),
                                    size = Size((rx + 8.dp.toPx()) * 2f, (ry + 8.dp.toPx()) * 2f),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                    )
                                )
                            }
                        }
                        Text(
                            text = if (isParallelTransmitOn) "Beberapa pemancar diatur terpisah (pTx)" else "Satu pemancar untuk seluruh badan",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isParallelTransmitOn) EmeraldSuccess else AmberWarning,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "B₀ = ${fmtId(b0Ptx, 1)} T: panjang gelombang RF di jaringan sekitar $wavelengthCm cm dibanding lebar badan 35 cm. " +
                            if (isParallelTransmitOn) {
                                "Transmit paralel (pTx) mengatur amplitudo dan fase tiap elemen pemancar secara terpisah sehingga distribusi B1 kembali merata."
                            } else if (b0Ptx >= 3.0f) {
                                "Karena λ (26 cm) sebanding dengan lebar badan (35 cm), timbul interferensi gelombang berdiri (daerah tengah terang dan cincin gelap)."
                            } else {
                                "Pada 1,5 T, λ (~52 cm) lebih besar dari lebar badan sehingga ketidakmerataan B1 masih relatif lemah."
                            },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawVectorArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeW: Float
) {
    drawLine(color = color, start = start, end = end, strokeWidth = strokeW)
    val len = hypot(end.x - start.x, end.y - start.y)
    if (len < 6f) return
    val angle = atan2(end.y - start.y, end.x - start.x)
    val headLen = 9.dp.toPx()
    val p = Path().apply {
        moveTo(end.x, end.y)
        lineTo(end.x - headLen * cos(angle - 0.45f), end.y - headLen * sin(angle - 0.45f))
        lineTo(end.x - headLen * cos(angle + 0.45f), end.y - headLen * sin(angle + 0.45f))
        close()
    }
    drawPath(p, color = color)
}
