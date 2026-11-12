package com.example.ui.simulators

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.*

// ============================================================================
// SUITE GABUNGAN MODUL 8: M08A (PENGODEAN & JARINGAN), M08B (ALIRAN), M08C (QA & CAPSTONE)
// ============================================================================

@Composable
fun Module8ArtifactsFlowQaSuite(
    initialModuleIndex: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var selectedSubModule by rememberSaveable(initialModuleIndex) { mutableIntStateOf(initialModuleIndex) }
    val subModules = listOf(
        "M08A: Pengodean & Jaringan (A1–A8)",
        "M08B: Aliran & Kompensasi (A1–A6)",
        "M08C: Hardware, QA & Tabel 8.8 (A1–A6)"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module8_suite_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subModules.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedSubModule == idx,
                    onClick = { selectedSubModule = idx },
                    label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("m08_suite_tab_$idx")
                )
            }
        }

        when (selectedSubModule) {
            0 -> EncodingAndTissueArtifactsSimulator(initialTab = 0, onNodeComplete = onNodeComplete)
            1 -> FlowAndCompensationSimulator(initialTab = 0, onNodeComplete = onNodeComplete)
            2 -> HardwareQaAndCapstoneSimulator(initialTab = 0, onNodeComplete = onNodeComplete)
        }
    }
}

// ============================================================================
// 1. M08A: ARTEFAK PENGODEAN & JARINGAN (A1 s.d. A8)
// ============================================================================

@Composable
fun EncodingAndTissueArtifactsSimulator(
    initialTab: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var activeTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "A1: Luar-Fase & Dixon",
        "A2: Sudut Ajaib (54,7°)",
        "A3–A4: Ghosting & Gating",
        "A5–A6: Aliasing & Nyquist",
        "A7–A8: Chem Shift & Gibbs"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m08a_simulator_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Lab M08A · Artefak Pengodean & Jaringan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        label = { Text(title, fontSize = 12.sp) },
                        modifier = Modifier.testTag("m08a_tab_$index")
                    )
                }
            }

            when (activeTab) {
                0 -> M08AOutOfPhaseAndDixonTab(onNodeComplete)
                1 -> M08AMagicAngleTab(onNodeComplete)
                2 -> M08AGhostingAndGatingTab(onNodeComplete)
                3 -> M08AAliasingAndNyquistTab(onNodeComplete)
                4 -> M08AChemShiftAndGibbsTab(onNodeComplete)
            }
        }
    }
}

@Composable
private fun M08AOutOfPhaseAndDixonTab(onNodeComplete: (String) -> Unit) {
    var is3Tesla by remember { mutableStateOf(false) }
    var isSpinEcho by remember { mutableStateOf(false) }
    var teMs by remember { mutableFloatStateOf(2.24f) }
    var fatFractionPct by remember { mutableFloatStateOf(50f) }

    val deltaFreqHz = if (is3Tesla) 447.0 else 224.0
    val periodMs = 1000.0 / deltaFreqHz // 4.464 ms @1.5T, 2.237 ms @3T
    val phaseDeg = if (isSpinEcho) 0.0 else ((teMs / periodMs) * 360.0) % 360.0
    val phaseRad = Math.toRadians(phaseDeg)

    val f = (fatFractionPct / 100.0).coerceIn(0.0, 1.0)
    val w = 1.0 - f
    val netSignal = if (isSpinEcho) {
        w + f
    } else {
        sqrt(w * w + f * f + 2.0 * w * f * cos(phaseRad))
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A1 · Interaksi Fase Lemak & Air (Pembatalan Luar-Fase & Metode Dixon)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !is3Tesla,
                onClick = {
                    is3Tesla = false
                    teMs = 2.24f
                },
                label = { Text("1,5 T (Δf ≈ 224 Hz, T ≈ 4,47 ms)", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
            FilterChip(
                selected = is3Tesla,
                onClick = {
                    is3Tesla = true
                    teMs = 1.12f
                },
                label = { Text("3,0 T (Δf ≈ 447 Hz, T ≈ 2,24 ms)", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isSpinEcho,
                onClick = { isSpinEcho = false },
                label = { Text("Gradient Echo (Tanpa 180°)", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
            FilterChip(
                selected = isSpinEcho,
                onClick = {
                    isSpinEcho = true
                    onNodeComplete("M08A.T2.3")
                },
                label = { Text("Spin Echo (Pulsa 180° Refokus)", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
        }

        Text("Waktu Gema TE: ${String.format(Locale("id", "ID"), "%.2f", teMs)} ms", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = teMs,
            onValueChange = { teMs = it },
            valueRange = 0f..12f
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            val halfT = (periodMs / 2.0).toFloat()
            val fullT = periodMs.toFloat()
            OutlinedButton(onClick = { teMs = halfT; onNodeComplete("M08A.T2.3") }) {
                Text("Luar-Fase 1 (${String.format(Locale("id", "ID"), "%.2f", halfT)} ms)", fontSize = 11.sp)
            }
            OutlinedButton(onClick = { teMs = fullT; onNodeComplete("M08A.T3.1") }) {
                Text("In-Phase 1 (${String.format(Locale("id", "ID"), "%.2f", fullT)} ms)", fontSize = 11.sp)
            }
            OutlinedButton(onClick = { teMs = halfT * 3f }) {
                Text("Luar-Fase 2 (${String.format(Locale("id", "ID"), "%.2f", halfT * 3f)} ms)", fontSize = 11.sp)
            }
        }

        Text("Fraksi Lemak dalam Voksel Campuran: ${fatFractionPct.toInt()}% Lemak / ${(100 - fatFractionPct.toInt())}% Air", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = fatFractionPct,
            onValueChange = { fatFractionPct = it },
            valueRange = 0f..100f
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            val clockCenter = Offset(size.width * 0.23f, size.height * 0.5f)
            val radius = min(size.width * 0.17f, size.height * 0.36f)

            drawCircle(
                color = Color(0xFF334155),
                radius = radius,
                center = clockCenter,
                style = Stroke(width = 3f)
            )
            // Vektor Air (tetap di atas / 0 deg)
            drawLine(
                color = Color(0xFF38BDF8),
                start = clockCenter,
                end = Offset(clockCenter.x, clockCenter.y - radius * 0.9f),
                strokeWidth = 6f
            )
            // Vektor Lemak (berputar sesuai fase)
            val fatAngle = Math.toRadians(phaseDeg - 90.0)
            drawLine(
                color = Color(0xFFFACC15),
                start = clockCenter,
                end = Offset(
                    clockCenter.x + (radius * 0.85f * cos(fatAngle)).toFloat(),
                    clockCenter.y + (radius * 0.85f * sin(fatAngle)).toFloat()
                ),
                strokeWidth = 6f
            )

            // Kurva sinyal voksel campuran terhadap TE (0..12 ms)
            val plotLeft = size.width * 0.46f
            val plotRight = size.width * 0.95f
            val plotTop = size.height * 0.16f
            val plotBottom = size.height * 0.84f
            val plotW = plotRight - plotLeft
            val plotH = plotBottom - plotTop

            drawRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(plotLeft, plotTop),
                size = Size(plotW, plotH)
            )

            val path = Path()
            val steps = 120
            for (i in 0..steps) {
                val t = (i / steps.toDouble()) * 12.0
                val ph = if (isSpinEcho) 0.0 else ((t / periodMs) * 2.0 * Math.PI)
                val sig = if (isSpinEcho) 1.0 else sqrt(w * w + f * f + 2.0 * w * f * cos(ph))
                val x = plotLeft + (i / steps.toFloat()) * plotW
                val y = plotBottom - (sig.toFloat() * plotH)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = Color(0xFF22C55E), style = Stroke(width = 4f))

            // Penanda TE saat ini
            val curX = plotLeft + (teMs / 12f).coerceIn(0f, 1f) * plotW
            val curY = plotBottom - (netSignal.toFloat().coerceIn(0f, 1f) * plotH)
            drawLine(
                color = Color(0xFFF97316),
                start = Offset(curX, plotTop),
                end = Offset(curX, plotBottom),
                strokeWidth = 2f
            )
            drawCircle(color = Color(0xFFF97316), radius = 8f, center = Offset(curX, curY))
        }

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Beda Fase Air–Lemak: ${String.format(Locale("id", "ID"), "%.0f", phaseDeg)}° · Sinyal Voksel Campuran: ${String.format(Locale("id", "ID"), "%.1f", netSignal * 100)}%",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Rekonstruksi Dixon: Citra Air W = ½(In-Phase + Out-of-Phase) ≈ ${String.format(Locale("id", "ID"), "%.0f", w * 100)}% | Citra Lemak F = ½(In-Phase − Out-of-Phase) ≈ ${String.format(Locale("id", "ID"), "%.0f", f * 100)}%.",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!isSpinEcho && netSignal < 0.25) {
                    Text(
                        "Garis Tinta Hitam (India Ink) aktif! Sinyal voksel batas air-lemak saling meniadakan karena fase berlawanan (~180°).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun M08AMagicAngleTab(onNodeComplete: (String) -> Unit) {
    var thetaDeg by remember { mutableFloatStateOf(54.7f) }
    var teRel by remember { mutableFloatStateOf(20f) }

    val thetaRad = Math.toRadians(thetaDeg.toDouble())
    val dipolarTerm = 3.0 * cos(thetaRad) * cos(thetaRad) - 1.0
    val tendonSignal = exp(-(teRel / 100.0) * (6.0 + 6.0 * dipolarTerm * dipolarTerm))
    val fluidSignal = exp(-(teRel / 100.0) * 1.2)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A2 · Artefak Sudut Ajaib (Magic Angle 54,7°) pada Tendon & Ligamen",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    thetaDeg = 54.7f
                    teRel = 18f
                    onNodeComplete("M08A.T2.4")
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Ke Sudut Ajaib (54,7°)", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
                onClick = { thetaDeg = 0f },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Sejajar B₀ (0°)", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
                onClick = { teRel = 85f },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Cek T2 TE Panjang (85 ms)", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
        }

        Text("Sudut Serat Kolagen terhadap B₀ (θ): ${String.format(Locale("id", "ID"), "%.1f", thetaDeg)}°", style = MaterialTheme.typography.labelMedium)
        Slider(value = thetaDeg, onValueChange = { thetaDeg = it }, valueRange = 0f..90f)

        Text("Waktu Gema (TE): ${teRel.toInt()} ms", style = MaterialTheme.typography.labelMedium)
        Slider(value = teRel, onValueChange = { teRel = it }, valueRange = 5f..100f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Kiri: Orientasi serat tendon terhadap B0 vertikal
            val cx = size.width * 0.24f
            val cy = size.height * 0.78f
            val len = size.height * 0.62f

            // Garis B0 vertikal
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(cx, cy),
                end = Offset(cx, cy - len),
                strokeWidth = 5f
            )
            // Garis serat pada sudut theta dari B0 vertikal
            val fx = cx + (len * sin(thetaRad)).toFloat()
            val fy = cy - (len * cos(thetaRad)).toFloat()
            drawLine(
                color = if (abs(thetaDeg - 54.7f) < 4f && teRel < 38f) Color(0xFFFACC15) else Color(0xFF94A3B8),
                start = Offset(cx, cy),
                end = Offset(fx, fy),
                strokeWidth = 10f
            )

            // Kanan: Kurva Sinyal Tendon terhadap sudut 0..90 deg
            val pLeft = size.width * 0.48f
            val pRight = size.width * 0.95f
            val pTop = size.height * 0.15f
            val pBottom = size.height * 0.85f
            val pW = pRight - pLeft
            val pH = pBottom - pTop

            val path = Path()
            for (deg in 0..90) {
                val r = Math.toRadians(deg.toDouble())
                val d = 3.0 * cos(r) * cos(r) - 1.0
                val s = exp(-(teRel / 100.0) * (6.0 + 6.0 * d * d))
                val x = pLeft + (deg / 90f) * pW
                val y = pBottom - s.toFloat() * pH
                if (deg == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = Color(0xFFFACC15), style = Stroke(width = 4f))

            val curX = pLeft + (thetaDeg / 90f) * pW
            val curY = pBottom - tendonSignal.toFloat() * pH
            drawCircle(color = Color(0xFFEF4444), radius = 8f, center = Offset(curX, curY))
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Faktor Dipolar (3·cos²θ − 1) = ${String.format(Locale("id", "ID"), "%.3f", dipolarTerm)}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sinyal Tendon: ${String.format(Locale("id", "ID"), "%.0f", tendonSignal * 100)}% vs Cairan Robekan Sejati: ${String.format(Locale("id", "ID"), "%.0f", fluidSignal * 100)}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    if (abs(thetaDeg - 54.7f) < 5f && teRel < 35f)
                        "WASPADA: Pada θ ≈ 54,7° dan TE pendek (<35 ms), tendon normal tampak hiperintens (mirip robekan). Konfirmasi dengan sekuens T2 TE panjang!"
                    else
                        "Pada TE panjang atau sudut jauh dari 54,7°, tendon sehat tampak gelap (sinyal rendah) sedangkan cairan robekan tetap terang.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (abs(thetaDeg - 54.7f) < 5f && teRel < 35f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun M08AGhostingAndGatingTab(onNodeComplete: (String) -> Unit) {
    var trMs by remember { mutableFloatStateOf(1000f) }
    var nPhase by remember { mutableFloatStateOf(128f) }
    var nsa by remember { mutableFloatStateOf(1f) }
    var motionPeriodSec by remember { mutableFloatStateOf(4.0f) }
    var swapPhaseFreq by remember { mutableStateOf(false) }
    var gatingMode by remember { mutableStateOf("Triggering") }
    var respRateBpm by remember { mutableFloatStateOf(15f) }
    var gateWidthPct by remember { mutableFloatStateOf(30f) }

    // Jarak hantu proporsional terhadap TR * N_phase * NSA / T_motion
    val ghostSpacingRatio = ((trMs / 1000f) * nsa / motionPeriodSec).coerceIn(0.08f, 0.9f)
    val respPeriodSec = 60f / respRateBpm
    val effectiveTaScale = 100f / gateWidthPct.coerceAtLeast(10f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A3 · Jarak Hantu Gerak Periodik (Phase Mismapping) & A4 · Triggering vs Gating Napas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { motionPeriodSec = 1.0f; onNodeComplete("M08A.T2.2") },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Denyut Jantung (1,0 dtk)", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
                onClick = { motionPeriodSec = 4.0f },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Pernapasan (4,0 dtk)", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
            FilterChip(
                selected = swapPhaseFreq,
                onClick = {
                    swapPhaseFreq = !swapPhaseFreq
                    onNodeComplete("M08A.T3.2")
                },
                label = { Text(if (swapPhaseFreq) "Fase: Kiri–Kanan" else "Fase: Depan–Belakang", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
        }

        Text("TR: ${trMs.toInt()} ms · Matriks Fase: ${nPhase.toInt()} · NSA: ${nsa.toInt()} · Periode Gerak: ${String.format(Locale("id", "ID"), "%.1f", motionPeriodSec)} dtk", style = MaterialTheme.typography.labelMedium)
        Slider(value = trMs, onValueChange = { trMs = it }, valueRange = 200f..3000f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1f, 2f, 4f).forEach { v ->
                FilterChip(
                    selected = nsa == v,
                    onClick = { nsa = v },
                    label = { Text("NSA ${v.toInt()}") }
                )
            }
        }

        // A4 Gating / Triggering controls
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = gatingMode == "Triggering",
                onClick = {
                    gatingMode = "Triggering"
                    onNodeComplete("M08A.T3.3")
                },
                label = { Text("Respiratory Triggering") }
            )
            FilterChip(
                selected = gatingMode == "Gating",
                onClick = {
                    gatingMode = "Gating"
                    onNodeComplete("M08A.T3.3")
                },
                label = { Text("Respiratory Gating (${gateWidthPct.toInt()}%)") }
            )
        }
        Text("Laju Napas: ${respRateBpm.toInt()} napas/menit (TR efektif ≈ ${String.format(Locale("id", "ID"), "%.1f", respPeriodSec)} dtk) · Lebar Jendela Akuisisi: ${gateWidthPct.toInt()}%", style = MaterialTheme.typography.labelMedium)
        Slider(value = gateWidthPct, onValueChange = { gateWidthPct = it }, valueRange = 15f..80f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Kiri: Citra Fantom + Ghosting sepanjang sumbu fase
            val leftCx = size.width * 0.25f
            val leftCy = size.height * 0.5f
            val r = min(size.width * 0.14f, size.height * 0.26f)

            // Hantu-hantu periodik
            val stepPx = r * 2.2f * ghostSpacingRatio
            for (k in -2..2) {
                if (k == 0) continue
                val alpha = (0.45f / abs(k)).coerceIn(0.1f, 0.45f)
                val gx = if (swapPhaseFreq) leftCx + k * stepPx else leftCx
                val gy = if (swapPhaseFreq) leftCy else leftCy + k * stepPx
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = alpha),
                    radius = r * 0.85f,
                    center = Offset(gx, gy),
                    style = Stroke(width = 3f)
                )
            }
            // Objek utama
            drawCircle(color = Color(0xFFE2E8F0), radius = r, center = Offset(leftCx, leftCy))

            // Kanan: Gelombang pernapasan & jendela Triggering/Gating
            val wLeft = size.width * 0.50f
            val wRight = size.width * 0.96f
            val wMidY = size.height * 0.52f
            val wAmp = size.height * 0.28f
            val wW = wRight - wLeft

            val wavePath = Path()
            val samples = 100
            for (i in 0..samples) {
                val frac = i / samples.toFloat()
                val angle = frac * 2.0 * Math.PI * 2.5
                val y = wMidY - (sin(angle) * wAmp).toFloat()
                val x = wLeft + frac * wW
                if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
            }
            drawPath(wavePath, color = Color(0xFF22C55E), style = Stroke(width = 3.5f))

            // Jendela akuisisi pada akhir ekspirasi (lembah gelombang)
            val winFrac = (gateWidthPct / 100f) * 0.35f
            drawRect(
                color = Color(0xFFFACC15).copy(alpha = 0.28f),
                topLeft = Offset(wLeft + wW * 0.25f, wMidY - wAmp),
                size = Size(wW * winFrac, wAmp * 2f)
            )
            drawRect(
                color = Color(0xFFFACC15).copy(alpha = 0.28f),
                topLeft = Offset(wLeft + wW * 0.65f, wMidY - wAmp),
                size = Size(wW * winFrac, wAmp * 2f)
            )
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Jarak Antar-Hantu Relatif: ${String.format(Locale("id", "ID"), "%.0f", ghostSpacingRatio * 100)}% FOV (Meningkat bersama TR, N_fase, dan NSA).",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Mode $gatingMode: Waktu akuisisi efektif memanjang ~${String.format(Locale("id", "ID"), "%.1f", effectiveTaScale)}× karena data hanya direkam saat dinding dada tenang (akhir ekspirasi).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun M08AAliasingAndNyquistTab(onNodeComplete: (String) -> Unit) {
    var fovPhasePct by remember { mutableFloatStateOf(70f) }
    var oversamplingMode by remember { mutableIntStateOf(0) } // 0 = Tanpa, 1 = Opsi 1, 2 = Opsi 2 (No Phase Wrap)
    var bwTotalKhz by remember { mutableFloatStateOf(32f) }
    var actualFreqKhz by remember { mutableFloatStateOf(22f) }
    var lowPassActive by remember { mutableStateOf(false) }

    val fNyquist = bwTotalKhz / 2f
    val isFreqAliased = abs(actualFreqKhz) > fNyquist && !lowPassActive
    val apparentFreqKhz = if (abs(actualFreqKhz) <= fNyquist) {
        actualFreqKhz
    } else if (lowPassActive) {
        0f // teredam oleh filter low-pass analog
    } else {
        val sign = if (actualFreqKhz >= 0f) 1f else -1f
        actualFreqKhz - sign * 2f * fNyquist
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A5 · Aliasing Fase (Phase Oversampling) & A6 · Kalkulator Frekuensi Nyquist",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Text("FOV Fase dibanding Ukuran Objek: ${fovPhasePct.toInt()}%", style = MaterialTheme.typography.labelMedium)
        Slider(value = fovPhasePct, onValueChange = { fovPhasePct = it }, valueRange = 50f..120f)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            FilterChip(
                selected = oversamplingMode == 0,
                onClick = { oversamplingMode = 0 },
                label = { Text("Tanpa Oversampling", fontSize = 11.sp) }
            )
            FilterChip(
                selected = oversamplingMode == 1,
                onClick = {
                    oversamplingMode = 1
                    onNodeComplete("M08A.T3.4")
                },
                label = { Text("Opsi 1 (FOV ×2, TA ×2)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = oversamplingMode == 2,
                onClick = {
                    oversamplingMode = 2
                    onNodeComplete("M08A.T3.4")
                },
                label = { Text("Opsi 2 No Phase Wrap (NSA ½, TA Tetap)", fontSize = 11.sp) }
            )
        }

        HorizontalDivider()

        Text(
            "A6 · Kalkulator Nyquist Frekuensi: BW Total = ${bwTotalKhz.toInt()} kHz (f_Nyquist = ±${String.format(Locale("id", "ID"), "%.1f", fNyquist)} kHz)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(value = bwTotalKhz, onValueChange = { bwTotalKhz = it }, valueRange = 16f..100f)

        Text("Frekuensi Spin Sebenarnya di Luar FOV: ${String.format(Locale("id", "ID"), "%.1f", actualFreqKhz)} kHz", style = MaterialTheme.typography.labelMedium)
        Slider(value = actualFreqKhz, onValueChange = { actualFreqKhz = it }, valueRange = -50f..50f)

        FilterChip(
            selected = lowPassActive,
            onClick = {
                lowPassActive = !lowPassActive
                onNodeComplete("M08A.T2.1")
            },
            label = { Text(if (lowPassActive) "Filter Low-Pass Anti-Aliasing: AKTIF" else "Filter Low-Pass Anti-Aliasing: MATI") }
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Kiri: Visualisasi FOV & Wrap-Around
            val cx = size.width * 0.25f
            val cy = size.height * 0.5f
            val objR = min(size.width * 0.16f, size.height * 0.36f)
            val fovW = objR * 2f * (fovPhasePct / 100f)

            drawCircle(color = Color(0xFF64748B), radius = objR, center = Offset(cx, cy))
            drawRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(cx - fovW / 2f, cy - objR),
                size = Size(fovW, objR * 2f),
                style = Stroke(width = 3f)
            )
            // Jika FOV < 100% dan tanpa oversampling, gambar lipatan di sisi berlawanan
            if (fovPhasePct < 100f && oversamplingMode == 0) {
                val overlap = (objR - fovW / 2f).coerceAtLeast(0f)
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.65f),
                    radius = overlap,
                    center = Offset(cx - fovW / 2f + overlap, cy)
                )
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.65f),
                    radius = overlap,
                    center = Offset(cx + fovW / 2f - overlap, cy)
                )
            }

            // Kanan: Pita Nyquist [-fN, +fN]
            val barLeft = size.width * 0.52f
            val barRight = size.width * 0.94f
            val barMid = (barLeft + barRight) / 2f
            val barY = size.height * 0.55f
            val halfSpan = (barRight - barLeft) * 0.32f

            drawLine(color = Color(0xFF475569), start = Offset(barLeft, barY), end = Offset(barRight, barY), strokeWidth = 4f)
            drawRect(
                color = Color(0xFF22C55E).copy(alpha = 0.25f),
                topLeft = Offset(barMid - halfSpan, barY - 28f),
                size = Size(halfSpan * 2f, 56f)
            )

            // Titik frekuensi sebenarnya vs terbaca
            val actualX = (barMid + (actualFreqKhz / 50f) * ((barRight - barLeft) / 2f)).coerceIn(barLeft, barRight)
            drawCircle(color = Color(0xFFFACC15), radius = 8f, center = Offset(actualX, barY - 14f))

            val apparentX = (barMid + (apparentFreqKhz / 50f) * ((barRight - barLeft) / 2f)).coerceIn(barLeft, barRight)
            drawCircle(
                color = if (isFreqAliased) Color(0xFFEF4444) else Color(0xFF22C55E),
                radius = 9f,
                center = Offset(apparentX, barY + 14f)
            )
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (isFreqAliased)
                        "ALIASING FREKUENSI: f_sebenarnya (${String.format(Locale("id", "ID"), "%.1f", actualFreqKhz)} kHz) melampaui f_Nyquist (±${String.format(Locale("id", "ID"), "%.1f", fNyquist)} kHz) → Terlipat ke f_terbaca = ${String.format(Locale("id", "ID"), "%.1f", apparentFreqKhz)} kHz!"
                    else if (lowPassActive && abs(actualFreqKhz) > fNyquist)
                        "Filter Low-Pass Analog menyaring sinyal di luar ±${String.format(Locale("id", "ID"), "%.1f", fNyquist)} kHz sebelum pencuplikan ADC."
                    else
                        "Aman (Memenuhi Teorema Nyquist): f_terbaca = ${String.format(Locale("id", "ID"), "%.1f", apparentFreqKhz)} kHz.",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun M08AChemShiftAndGibbsTab(onNodeComplete: (String) -> Unit) {
    var is3Tesla by remember { mutableStateOf(false) }
    var bwPlusMinusKhz by remember { mutableFloatStateOf(16f) } // ±16 kHz => total 32 kHz
    var freqMatrix by remember { mutableFloatStateOf(256f) }
    var fovMm by remember { mutableFloatStateOf(240f) }

    var gibbsPhaseMatrix by remember { mutableFloatStateOf(32f) }
    var apodizationFilter by remember { mutableStateOf(false) }

    val deltaF = if (is3Tesla) 447f else 224f
    val totalBwHz = bwPlusMinusKhz * 2f * 1000f
    val hzPerPixel = totalBwHz / freqMatrix
    val shiftPixels = deltaF / hzPerPixel
    val voxelSizeMm = fovMm / freqMatrix
    val shiftMm = shiftPixels * voxelSizeMm

    val gibbsOvershootPct = if (apodizationFilter) 2.2f else 9.0f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A7 · Kalkulator Pergeseran Kimia (Piksel & mm) & A8 · Truncation (Gibbs Ringing)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !is3Tesla,
                onClick = { is3Tesla = false },
                label = { Text("1,5 T (224 Hz)", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
            FilterChip(
                selected = is3Tesla,
                onClick = {
                    is3Tesla = true
                    onNodeComplete("M08A.T1.4")
                },
                label = { Text("3,0 T (447 Hz)", fontSize = 11.sp, maxLines = 1, softWrap = false) }
            )
            OutlinedButton(
                onClick = {
                    is3Tesla = false
                    bwPlusMinusKhz = 16f
                    freqMatrix = 256f
                    onNodeComplete("M08A.T3.5")
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Contoh Standar (±16 kHz, 256)", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
        }

        Text("Bandwidth Penerima: ±${bwPlusMinusKhz.toInt()} kHz (Total ${(bwPlusMinusKhz * 2).toInt()} kHz) · Matriks: ${freqMatrix.toInt()}", style = MaterialTheme.typography.labelMedium)
        Slider(value = bwPlusMinusKhz, onValueChange = { bwPlusMinusKhz = it }, valueRange = 4f..64f)

        HorizontalDivider()

        Text("A8 · Matriks Fase (Gibbs Ringing): ${gibbsPhaseMatrix.toInt()} baris", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = gibbsPhaseMatrix,
            onValueChange = {
                gibbsPhaseMatrix = it
                onNodeComplete("M08A.T2.5")
            },
            valueRange = 16f..128f
        )
        FilterChip(
            selected = apodizationFilter,
            onClick = { apodizationFilter = !apodizationFilter },
            label = { Text(if (apodizationFilter) "Filter Apodisasi (Smoothing): AKTIF" else "Filter Apodisasi: MATI (Overshoot ~9%)") }
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Kiri: Pergeseran posisi lemak vs air pada arah frekuensi
            val cx = size.width * 0.24f
            val cy = size.height * 0.5f
            val r = min(size.width * 0.13f, size.height * 0.30f)
            val shiftPxVisual = (shiftPixels * 4.5f).coerceAtMost(28f)

            drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.75f), radius = r, center = Offset(cx, cy))
            drawCircle(
                color = Color(0xFFFACC15).copy(alpha = 0.70f),
                radius = r,
                center = Offset(cx + shiftPxVisual, cy)
            )

            // Kanan: Profil tepi tajam dengan Gibbs Ringing
            val gLeft = size.width * 0.50f
            val gRight = size.width * 0.95f
            val gW = gRight - gLeft
            val gBase = size.height * 0.78f
            val gTop = size.height * 0.25f

            val path = Path()
            val nFreq = (gibbsPhaseMatrix / 4f).toInt().coerceIn(4, 32)
            val pts = 100
            for (i in 0..pts) {
                val xNorm = (i / pts.toFloat()) * 2f - 1f // -1..1
                var sum = 0.0
                for (k in 1..nFreq step 2) {
                    val weight = if (apodizationFilter) cos((k * Math.PI) / (2.0 * (nFreq + 2))) else 1.0
                    sum += weight * sin(k * Math.PI * xNorm * 0.8) / k
                }
                val stepVal = 0.5 + (2.0 / Math.PI) * sum
                val px = gLeft + (i / pts.toFloat()) * gW
                val py = gBase - (stepVal.toFloat() * (gBase - gTop))
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path, color = Color(0xFF22C55E), style = Stroke(width = 3.5f))
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "BW/piksel = ${String.format(Locale("id", "ID"), "%.1f", hzPerPixel)} Hz/piksel → Pergeseran Kimia = ${String.format(Locale("id", "ID"), "%.2f", shiftPixels)} piksel (${String.format(Locale("id", "ID"), "%.2f", shiftMm)} mm).",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Gibbs Overshoot: ~${String.format(Locale("id", "ID"), "%.1f", gibbsOvershootPct)}% di dekat antarmuka kontras tinggi (mis. CSF–korda spinalis menyerupai syrinx).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ============================================================================
// 2. M08B: ALIRAN & ARTEFAK ALIRAN (A1 s.d. A6)
// ============================================================================

@Composable
fun FlowAndCompensationSimulator(
    initialTab: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var activeTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "A1: Entry-Slice (ESP)",
        "A2–A3: TOF & Reynolds",
        "A4: GMN (+1, −2, +1)",
        "A5–A6: Presaturasi & Kuis"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m08b_simulator_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Lab M08B · Fenomena Aliran & Kompensasi Gradien",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        label = { Text(title, fontSize = 12.sp) },
                        modifier = Modifier.testTag("m08b_tab_$index")
                    )
                }
            }

            when (activeTab) {
                0 -> M08BEntrySliceTab(onNodeComplete)
                1 -> M08BTofAndReynoldsTab(onNodeComplete)
                2 -> M08BGmnTab(onNodeComplete)
                3 -> M08BPresatAndScenarioTab(onNodeComplete)
            }
        }
    }
}

@Composable
private fun M08BEntrySliceTab(onNodeComplete: (String) -> Unit) {
    var isCounterCurrent by remember { mutableStateOf(true) }
    var trMs by remember { mutableFloatStateOf(400f) }
    var sliceThicknessMm by remember { mutableFloatStateOf(8f) }
    var flowVelocityCmS by remember { mutableFloatStateOf(20f) }

    // v_maks penggantian penuh = d / TR
    val vReplacementCmS = (sliceThicknessMm / 10f) / (trMs / 1000f)
    val replacementFraction = (flowVelocityCmS / vReplacementCmS).coerceIn(0f, 1f)

    // Sinyal 6 irisan dari pintu masuk ke hilir
    val sliceSignals = List(6) { idx ->
        val decay = if (isCounterCurrent) 0.82f.pow(idx) else 0.65f.pow(idx)
        (0.25f + 0.75f * replacementFraction * decay).coerceIn(0.15f, 1.0f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A1 · Entry-Slice Phenomenon (ESP): Penggantian Spin Segar (v = d / TR)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isCounterCurrent,
                onClick = {
                    isCounterCurrent = true
                    onNodeComplete("M08B.T2.1")
                },
                label = { Text("Berlawanan Eksitasi (Counter-current)") }
            )
            FilterChip(
                selected = !isCounterCurrent,
                onClick = { isCounterCurrent = false },
                label = { Text("Searah Eksitasi (Co-current)") }
            )
        }

        Text("TR: ${trMs.toInt()} ms · Tebal Irisan (d): ${sliceThicknessMm.toInt()} mm · Kecepatan Aliran (v): ${flowVelocityCmS.toInt()} cm/dtk", style = MaterialTheme.typography.labelMedium)
        Slider(value = flowVelocityCmS, onValueChange = { flowVelocityCmS = it }, valueRange = 2f..50f)
        Slider(value = trMs, onValueChange = { trMs = it }, valueRange = 100f..1500f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            val barW = size.width / 8.5f
            val maxH = size.height * 0.68f
            val baseY = size.height * 0.86f

            sliceSignals.forEachIndexed { idx, sig ->
                val x = barW * (idx + 1f)
                val h = maxH * sig
                drawRoundRect(
                    color = if (idx == 0) Color(0xFFFACC15) else Color(0xFF38BDF8),
                    topLeft = Offset(x, baseY - h),
                    size = Size(barW * 0.68f, h),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Kecepatan Penggantian Penuh (v = d / TR): ${String.format(Locale("id", "ID"), "%.1f", vReplacementCmS)} cm/dtk · Fraksi Spin Segar Irisan #1: ${String.format(Locale("id", "ID"), "%.0f", replacementFraction * 100)}%",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Irisan Pertama = ${String.format(Locale("id", "ID"), "%.0f", sliceSignals.first() * 100)}% vs Irisan #6 = ${String.format(Locale("id", "ID"), "%.0f", sliceSignals.last() * 100)}% (Aorta vs Vena Kava Inferior tampak terang di pintu masuk masing-masing).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun M08BTofAndReynoldsTab(onNodeComplete: (String) -> Unit) {
    var velocityCmS by remember { mutableFloatStateOf(20f) }
    var teMs by remember { mutableFloatStateOf(40f) }
    var thicknessMm by remember { mutableFloatStateOf(5f) }

    var diameterMm by remember { mutableFloatStateOf(8f) }
    var viscosityMpas by remember { mutableFloatStateOf(4.0f) }

    // TOF Spin Echo: d = v * (TE / 2)
    val dMovedMm = (velocityCmS * 10f) * (teMs / 2000f)
    val tofSignalRemaining = (1f - (dMovedMm / thicknessMm)).coerceIn(0f, 1f)

    // Reynolds: Re = rho * v * D / mu (rho = 1.06 g/cm3)
    val reynolds = (1.06f * velocityCmS * (diameterMm / 10f)) / (viscositasToCgs(viscosityMpas))
    val isTurbulent = reynolds >= 2100f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A2 · Time-of-Flight (TOF) Spin-Echo & A3 · Bilangan Reynolds (Laminar vs Turbulen)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text("Kecepatan Darah (v): ${velocityCmS.toInt()} cm/dtk · TE: ${teMs.toInt()} ms · Tebal Irisan: ${thicknessMm.toInt()} mm", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = velocityCmS,
            onValueChange = {
                velocityCmS = it
                onNodeComplete("M08B.T2.2")
            },
            valueRange = 0f..80f
        )
        Slider(value = teMs, onValueChange = { teMs = it }, valueRange = 10f..120f)

        Text("Diameter Pembuluh (D): ${diameterMm.toInt()} mm · Viskositas (μ): ${String.format(Locale("id", "ID"), "%.1f", viscosityMpas)} mPa·s", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = diameterMm,
            onValueChange = {
                diameterMm = it
                onNodeComplete("M08B.T3.1")
            },
            valueRange = 2f..25f
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Pembuluh darah horizontal dengan profil kecepatan parabola (laminar) atau acak (turbulen)
            val topY = size.height * 0.22f
            val botY = size.height * 0.78f
            val midY = (topY + botY) / 2f
            val halfH = (botY - topY) / 2f

            drawLine(color = Color(0xFFEF4444), start = Offset(20f, topY), end = Offset(size.width - 20f, topY), strokeWidth = 4f)
            drawLine(color = Color(0xFFEF4444), start = Offset(20f, botY), end = Offset(size.width - 20f, botY), strokeWidth = 4f)

            for (i in -4..4) {
                val rNorm = i / 4f
                val y = midY + rNorm * halfH * 0.85f
                val vLocal = if (isTurbulent) {
                    (0.55f + 0.35f * cos(i * 1.9f))
                } else {
                    (1f - rNorm * rNorm)
                }
                val arrowLen = 30f + vLocal * (size.width * 0.38f)
                drawLine(
                    color = if (isTurbulent) Color(0xFFF97316) else Color(0xFF38BDF8),
                    start = Offset(40f, y),
                    end = Offset(40f + arrowLen, y),
                    strokeWidth = 5f
                )
            }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "TOF Spin-Echo: Jarak tempuh saat TE/2 = ${String.format(Locale("id", "ID"), "%.2f", dMovedMm)} mm → Sisa Sinyal Lumen = ${String.format(Locale("id", "ID"), "%.0f", tofSignalRemaining * 100)}% (${if (tofSignalRemaining == 0f) "FLOW VOID LENGKAP" else "Sinyal Parsial"}).",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Bilangan Reynolds Re = ${String.format(Locale("id", "ID"), "%.0f", reynolds)} (Ambang = 2100) → Pola Aliran: ${if (isTurbulent) "TURBULEN (Dephasing Acak, Tidak Bisa Dikompensasi GMN)" else "LAMINAR PARABOLA (v(r) = v_maks(1 − r²/R²))"}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTurbulent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun viscositasToCgs(mpas: Float): Float = (mpas * 0.01f).coerceAtLeast(0.01f)

@Composable
private fun M08BGmnTab(onNodeComplete: (String) -> Unit) {
    var gmnEnabled by remember { mutableStateOf(true) }
    var velocitySpread by remember { mutableFloatStateOf(60f) }
    var pulsatileAccel by remember { mutableFloatStateOf(15f) }

    val netCoherence = if (gmnEnabled) {
        (1.0f - (pulsatileAccel / 150f)).coerceIn(0.2f, 0.98f)
    } else {
        (1.0f - (velocitySpread / 110f) - (pulsatileAccel / 150f)).coerceIn(0.08f, 0.95f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A4 · Dephasing Intravoksel & Gradient Moment Nulling (GMN Lobus +1, −2, +1)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = gmnEnabled,
                onClick = {
                    gmnEnabled = true
                    onNodeComplete("M08B.T2.4")
                    onNodeComplete("M08B.T3.2")
                },
                label = { Text("GMN HIDUP (+1, −2, +1 · M₀=0, M₁=0)") }
            )
            FilterChip(
                selected = !gmnEnabled,
                onClick = { gmnEnabled = false },
                label = { Text("GMN MATI (Bipolar +1, −1)") }
            )
        }

        Text("Sebaran Kecepatan Laminar Intravoksel: ${velocitySpread.toInt()}%", style = MaterialTheme.typography.labelMedium)
        Slider(value = velocitySpread, onValueChange = { velocitySpread = it }, valueRange = 0f..100f)

        Text("Komponen Percepatan Pulsatil (Orde ke-2, M₂): ${pulsatileAccel.toInt()}%", style = MaterialTheme.typography.labelMedium)
        Slider(value = pulsatileAccel, onValueChange = { pulsatileAccel = it }, valueRange = 0f..100f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Kiri: Kipas 12 vektor spin dalam 1 voksel
            val cx = size.width * 0.25f
            val cy = size.height * 0.5f
            val r = min(size.width * 0.16f, size.height * 0.36f)

            drawCircle(color = Color(0xFF334155), radius = r, center = Offset(cx, cy), style = Stroke(width = 2f))
            val spreadRad = if (gmnEnabled) {
                (pulsatileAccel / 100f) * 1.1f
            } else {
                ((velocitySpread + pulsatileAccel) / 100f) * 2.6f
            }
            for (i in 0 until 12) {
                val frac = (i / 11f) - 0.5f
                val ang = -Math.PI / 2.0 + frac * spreadRad
                drawLine(
                    color = if (gmnEnabled) Color(0xFF22C55E) else Color(0xFFF97316),
                    start = Offset(cx, cy),
                    end = Offset(cx + (r * 0.88f * cos(ang)).toFloat(), cy + (r * 0.88f * sin(ang)).toFloat()),
                    strokeWidth = 3.5f
                )
            }

            // Kanan: Bentuk gelombang gradien (+1, -2, +1) vs (+1, -1)
            val gx0 = size.width * 0.52f
            val gx1 = size.width * 0.94f
            val gyMid = size.height * 0.52f
            val gw = (gx1 - gx0) / 3f
            val gh = size.height * 0.30f

            drawLine(color = Color(0xFF64748B), start = Offset(gx0, gyMid), end = Offset(gx1, gyMid), strokeWidth = 2f)
            if (gmnEnabled) {
                // +1
                drawRect(color = Color(0xFF38BDF8), topLeft = Offset(gx0, gyMid - gh * 0.5f), size = Size(gw, gh * 0.5f))
                // -2
                drawRect(color = Color(0xFFFACC15), topLeft = Offset(gx0 + gw, gyMid), size = Size(gw, gh))
                // +1
                drawRect(color = Color(0xFF38BDF8), topLeft = Offset(gx0 + 2f * gw, gyMid - gh * 0.5f), size = Size(gw, gh * 0.5f))
            } else {
                drawRect(color = Color(0xFF38BDF8), topLeft = Offset(gx0, gyMid - gh * 0.7f), size = Size(gw * 1.4f, gh * 0.7f))
                drawRect(color = Color(0xFFF97316), topLeft = Offset(gx0 + gw * 1.4f, gyMid), size = Size(gw * 1.4f, gh * 0.7f))
            }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Koherensi Fase Vektor Netto: ${String.format(Locale("id", "ID"), "%.0f", netCoherence * 100)}% · M₀ = 0, M₁ = ${if (gmnEnabled) "0 (Terkompensasi)" else "≠ 0 (Dephasing)"}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "GMN orde pertama (+1, −2, +1) meniadakan pergeseran fase untuk kecepatan konstan (v), menghasilkan sinyal lumen terang (bright-blood) pada GRE/T2.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun M08BPresatAndScenarioTab(onNodeComplete: (String) -> Unit) {
    var bandPlacement by remember { mutableIntStateOf(1) } // 0=Tanpa, 1=Hulu (Atas), 2=Hilir (Bawah), 3=Kedua Sisi
    var scenarioIdx by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }

    data class FlowScenario(
        val title: String,
        val options: List<String>,
        val correctIdx: Int,
        val feedback: String
    )

    val scenarios = remember {
        listOf(
            FlowScenario(
                "Skenario 1: Arteri mengalir dari Superior (Atas) ke Inferior (Bawah) dan menimbulkan ghosting sepanjang arah fase. Di mana pita presaturasi ditempatkan?",
                listOf("Sisi Superior (Hulu dari irisan)", "Sisi Inferior (Hilir dari irisan)", "Di dalam FOV sejajar frekuensi"),
                0,
                "Tepat! Pita presaturasi 90° harus ditempatkan di sisi HULU (Superior) agar darah arteri telah jenuh sebelum masuk ke irisan."
            ),
            FlowScenario(
                "Skenario 2: Anda ingin menekan sinyal arteri (dari atas) sekaligus vena (dari bawah) pada paket irisan leher.",
                listOf("Hanya di sisi atas", "Di kedua sisi (Superior & Inferior paket irisan)", "Mematikan gradien peremuk"),
                1,
                "Benar! Pita presaturasi ganda di kedua sisi (atas dan bawah) menjenuhkan aliran masuk arteri maupun vena."
            ),
            FlowScenario(
                "Skenario 3: Mengapa aliran jet stenosis katup jantung tetap tampak gelap (signal void) meskipun GMN telah diaktifkan?",
                listOf("Karena GMN hanya mengompensasi aliran laminar kecepatan konstan, bukan turbulensi acak (Re > 2100)", "Karena TR terlalu panjang", "Karena pita presaturasi bocor"),
                0,
                "Benar! Aliran turbulen memiliki fluktuasi kecepatan acak ruang-waktu sehingga tidak dapat direfokuskan oleh GMN."
            )
        )
    }

    val currentScenario = scenarios[scenarioIdx]
    val arterialSuppressed = bandPlacement == 1 || bandPlacement == 3
    val venousSuppressed = bandPlacement == 2 || bandPlacement == 3

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A5 · Pita Presaturasi Spasial & A6 · Latihan Skenario Aliran Klinis",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Tanpa Pita", "Sisi Hulu (Atas)", "Sisi Hilir (Bawah)", "Kedua Sisi").forEachIndexed { idx, label ->
                FilterChip(
                    selected = bandPlacement == idx,
                    onClick = {
                        bandPlacement = idx
                        onNodeComplete("M08B.T2.5")
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Status Pembuluh di Dalam Irisan:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Arteri (Aliran dari Atas): ${if (arterialSuppressed) "TERTEKAN / GELAP (Ghosting Hilang)" else "TERANG (Berpotensi Ghosting)"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Vena (Aliran dari Bawah): ${if (venousSuppressed) "TERTEKAN / GELAP (Ghosting Hilang)" else "TERANG (Berpotensi Ghosting)"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        HorizontalDivider()

        Text(currentScenario.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        currentScenario.options.forEachIndexed { idx, opt ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedAnswer = idx
                        if (idx == currentScenario.correctIdx) {
                            onNodeComplete("M08B.T1.1")
                            onNodeComplete("M08B.T2.6")
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = when {
                        selectedAnswer == idx && idx == currentScenario.correctIdx -> Color(0xFF22C55E).copy(alpha = 0.18f)
                        selectedAnswer == idx -> Color(0xFFEF4444).copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Text(
                    text = opt,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (selectedAnswer != -1) {
            Text(
                text = currentScenario.feedback,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedAnswer == currentScenario.correctIdx) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
            )
        }
        OutlinedButton(
            onClick = {
                scenarioIdx = (scenarioIdx + 1) % scenarios.size
                selectedAnswer = -1
            }
        ) {
            Text("Skenario Berikutnya (${scenarioIdx + 1}/${scenarios.size})", fontSize = 12.sp)
        }
    }
}

// ============================================================================
// 3. M08C: ARTEFAK HARDWARE, QA FANTOM & TABEL 8.8 CAPSTONE (A1 s.d. A6)
// ============================================================================

@Composable
fun HardwareQaAndCapstoneSimulator(
    initialTab: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var activeTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "A1: QA Fantom",
        "A2: Celah Irisan (Cross-Talk)",
        "A3 & A6: Spike Ruang-k & Logam",
        "A4–A5: Tabel 8.8 & Tebak Artefak"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m08c_simulator_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Lab M08C · Hardware, Cross-Talk, QA Fantom & Capstone Tabel 8.8",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        label = { Text(title, fontSize = 12.sp) },
                        modifier = Modifier.testTag("m08c_tab_$index")
                    )
                }
            }

            when (activeTab) {
                0 -> M08CQaPhantomTab(onNodeComplete)
                1 -> M08CSliceGapCrossTalkTab(onNodeComplete)
                2 -> M08CSpikeAndMetalSusceptibilityTab(onNodeComplete)
                3 -> M08CCapstoneTable88AndQuizTab(onNodeComplete)
            }
        }
    }
}

@Composable
private fun M08CQaPhantomTab(onNodeComplete: (String) -> Unit) {
    var noiseLevel by remember { mutableFloatStateOf(5f) }
    var inhomogeneity by remember { mutableFloatStateOf(4f) }
    var ghostingLevel by remember { mutableFloatStateOf(2f) }
    var geometricDistortion by remember { mutableFloatStateOf(1f) }
    var toleranceThreshold by remember { mutableFloatStateOf(10f) }

    val snrValue = (180f / noiseLevel.coerceAtLeast(1f))
    val uniformityPct = (100f - inhomogeneity * 1.4f).coerceIn(40f, 100f)
    val ghostRatioPct = (ghostingLevel * 0.45f).coerceIn(0f, 25f)
    val geomErrorPct = abs(geometricDistortion)

    val allPass = inhomogeneity <= toleranceThreshold &&
        ghostRatioPct <= (toleranceThreshold * 0.35f) &&
        geomErrorPct <= (toleranceThreshold * 0.5f) &&
        snrValue >= 20f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A1 · Simulasi Jaminan Kualitas (QA) Fantom Harian/Mingguan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            OutlinedButton(onClick = { noiseLevel = 5f; inhomogeneity = 3f; ghostingLevel = 2f; geometricDistortion = 1f; onNodeComplete("M08C.T3.2") }) {
                Text("Simpan/Reset Baseline", fontSize = 11.sp)
            }
            OutlinedButton(onClick = { noiseLevel = 25f }) { Text("Derau Naik", fontSize = 11.sp) }
            OutlinedButton(onClick = { ghostingLevel = 22f }) { Text("Ghosting Muncul", fontSize = 11.sp) }
            OutlinedButton(onClick = { geometricDistortion = 8f }) { Text("Ukuran Melenceng", fontSize = 11.sp) }
            OutlinedButton(onClick = { inhomogeneity = 22f }) { Text("Sinyal Tidak Rata", fontSize = 11.sp) }
        }

        Text("Derau (Noise): ${noiseLevel.toInt()} · Inhomogenitas (Shading): ${inhomogeneity.toInt()}% · Ghosting: ${ghostingLevel.toInt()}% · Distorsi Geometri: ${geometricDistortion.toInt()}%", style = MaterialTheme.typography.labelMedium)
        Slider(value = noiseLevel, onValueChange = { noiseLevel = it }, valueRange = 1f..40f)
        Slider(value = inhomogeneity, onValueChange = { inhomogeneity = it }, valueRange = 0f..30f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            val cx = size.width * 0.25f
            val cy = size.height * 0.5f
            val rx = min(size.width * 0.16f, size.height * 0.34f) * (1f + geometricDistortion / 60f)
            val ry = min(size.width * 0.16f, size.height * 0.34f) * (1f - geometricDistortion / 60f)

            // Fantom elips + ROI
            drawOval(
                color = Color(0xFF94A3B8).copy(alpha = (1f - inhomogeneity / 60f).coerceIn(0.4f, 0.95f)),
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2f, ry * 2f)
            )
            // ROI tengah & latar
            drawCircle(color = Color(0xFF22C55E), radius = 12f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
            drawCircle(color = Color(0xFFFACC15), radius = 10f, center = Offset(cx - rx - 18f, cy), style = Stroke(width = 2f))

            // Profil sinyal melintasi fantom di sisi kanan
            val pLeft = size.width * 0.52f
            val pRight = size.width * 0.94f
            val pBase = size.height * 0.80f
            val pTop = size.height * 0.22f
            val pW = pRight - pLeft

            val path = Path()
            for (i in 0..60) {
                val frac = i / 60f
                val shadeSlope = 1f - (inhomogeneity / 45f) * frac
                val ripple = sin(i * 1.7f) * (noiseLevel / 180f)
                val yVal = (shadeSlope + ripple).coerceIn(0.1f, 1.1f)
                val x = pLeft + frac * pW
                val y = pBase - yVal * (pBase - pTop)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = Color(0xFF38BDF8), style = Stroke(width = 3f))
        }

        Surface(
            color = if (allPass) Color(0xFF22C55E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Status QA Fantom: ${if (allPass) "DALAM AMBANG TOLERANSI (LOLOS)" else "DI LUAR AMBANG — PERLU KALIBRASI TEKNISI"}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• SNR = ${String.format(Locale("id", "ID"), "%.1f", snrValue)} | Uniformitas = ${String.format(Locale("id", "ID"), "%.1f", uniformityPct)}% | Rasio Ghosting = ${String.format(Locale("id", "ID"), "%.1f", ghostRatioPct)}% | Distorsi Geometri = ${String.format(Locale("id", "ID"), "%.1f", geomErrorPct)}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun M08CSliceGapCrossTalkTab(onNodeComplete: (String) -> Unit) {
    var sliceGapPct by remember { mutableFloatStateOf(10f) }
    var acquisitionOrderMode by remember { mutableIntStateOf(1) } // 0=Berurutan, 1=Selang-seling, 2=2 Akuisisi

    val overlapFactor = ((50f - sliceGapPct).coerceAtLeast(0f) / 50f) * when (acquisitionOrderMode) {
        0 -> 1.0f
        1 -> 0.35f
        else -> 0.08f
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A2 · Celah Irisan & Eksitasi Silang (Cross-Excitation / Cross-Talk)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            FilterChip(
                selected = acquisitionOrderMode == 0,
                onClick = { acquisitionOrderMode = 0 },
                label = { Text("Berurutan (1,2,3..)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = acquisitionOrderMode == 1,
                onClick = {
                    acquisitionOrderMode = 1
                    onNodeComplete("M08C.T2.1")
                },
                label = { Text("Selang-seling (1,3,5.. lalu 2,4,6..)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = acquisitionOrderMode == 2,
                onClick = {
                    acquisitionOrderMode = 2
                    onNodeComplete("M08C.T3.1")
                },
                label = { Text("2 Akuisisi Terpisah (Celah 100%)", fontSize = 11.sp) }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0f, 20f, 30f, 50f).forEach { g ->
                OutlinedButton(onClick = { sliceGapPct = g; onNodeComplete("M08C.T2.1") }) {
                    Text("Celah ${g.toInt()}%", fontSize = 11.sp)
                }
            }
        }

        Text("Celah Antar-Irisan: ${sliceGapPct.toInt()}% dari Tebal Irisan", style = MaterialTheme.typography.labelMedium)
        Slider(value = sliceGapPct, onValueChange = { sliceGapPct = it }, valueRange = 0f..100f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            val baseY = size.height * 0.84f
            val peakY = size.height * 0.20f
            val nSlices = 6
            val stepX = size.width / (nSlices + 1.5f)
            val halfWidth = stepX * (0.85f - (sliceGapPct / 220f))

            for (i in 0 until nSlices) {
                val cx = stepX * (i + 1f)
                val tri = Path().apply {
                    moveTo(cx - halfWidth, baseY)
                    lineTo(cx, peakY)
                    lineTo(cx + halfWidth, baseY)
                    close()
                }
                val isOdd = i % 2 == 0
                drawPath(
                    path = tri,
                    color = if (isOdd) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color(0xFFFACC15).copy(alpha = 0.45f)
                )
                drawPath(
                    path = tri,
                    color = if (isOdd) Color(0xFF38BDF8) else Color(0xFFFACC15),
                    style = Stroke(width = 2.5f)
                )
            }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Tingkat Tumpang-Tindih Eksitasi Silang: ${String.format(Locale("id", "ID"), "%.0f", overlapFactor * 100)}% · Penurunan SNR Relatif: ~${String.format(Locale("id", "ID"), "%.0f", overlapFactor * 32)}%",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Profil RF nyata memiliki sayap samping (side lobes). Urutan eksitasi selang-seling memberi waktu ½ TR bagi irisan tetangga untuk pulih sebelum dieksitasi.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun M08CSpikeAndMetalSusceptibilityTab(onNodeComplete: (String) -> Unit) {
    var kxSpike by remember { mutableFloatStateOf(4f) }
    var kySpike by remember { mutableFloatStateOf(3f) }
    var herringboneSecondSpike by remember { mutableStateOf(false) }

    var seqType by remember { mutableStateOf("SE") } // SE, GRE, EPI
    var is3T by remember { mutableStateOf(false) }
    var teMs by remember { mutableFloatStateOf(25f) }
    var bwKhz by remember { mutableFloatStateOf(16f) }

    val b0Mult = if (is3T) 2.0f else 1.0f
    val seqMult = when (seqType) {
        "SE" -> 0.45f
        "GRE" -> 1.35f
        else -> 2.40f // EPI
    }
    val maxPixelShift = (seqMult * b0Mult * (teMs / 25f) * (16f / bwKhz)).coerceAtLeast(0.2f)
    val signalLossAreaMm2 = (maxPixelShift * 14f * (if (seqType == "SE") 0.35f else 1.15f)).coerceAtLeast(1f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A3 · Lonjakan Derau Ruang-k (Moiré / Herringbone) & A6 · Suseptibilitas Logam (SE vs GRE vs EPI)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = herringboneSecondSpike,
                onClick = {
                    herringboneSecondSpike = !herringboneSecondSpike
                    onNodeComplete("M08C.T2.4")
                },
                label = { Text(if (herringboneSecondSpike) "2 Lonjakan Ruang-k (Pola Herringbone)" else "1 Lonjakan Ruang-k (Garis Moiré)") }
            )
        }
        Text("Posisi Lonjakan (kx = ${kxSpike.toInt()}, ky = ${kySpike.toInt()}) — Makin jauh dari pusat ruang-k, makin rapat pita garis di citra:", style = MaterialTheme.typography.labelMedium)
        Slider(value = kxSpike, onValueChange = { kxSpike = it }, valueRange = 1f..12f)

        HorizontalDivider()

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("SE", "GRE", "EPI").forEach { s ->
                FilterChip(
                    selected = seqType == s,
                    onClick = {
                        seqType = s
                        onNodeComplete("M08C.T2.6")
                    },
                    label = { Text("Sekuens $s") }
                )
            }
            FilterChip(
                selected = is3T,
                onClick = { is3T = !is3T },
                label = { Text(if (is3T) "3,0 T" else "1,5 T") }
            )
        }

        Text("TE: ${teMs.toInt()} ms · Bandwidth Penerima: ±${bwKhz.toInt()} kHz", style = MaterialTheme.typography.labelMedium)
        Slider(value = bwKhz, onValueChange = { bwKhz = it }, valueRange = 8f..64f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
        ) {
            // Kiri: Pola garis Moire / Herringbone
            val leftW = size.width * 0.45f
            val stripeCount = (kxSpike * 2.2f).toInt().coerceIn(3, 28)
            for (i in 0..stripeCount) {
                val x = (i / stripeCount.toFloat()) * leftW
                drawLine(
                    color = Color(0xFFFACC15).copy(alpha = 0.45f),
                    start = Offset(x, 0f),
                    end = Offset((x + kySpike * 8f).coerceAtMost(leftW), size.height),
                    strokeWidth = 3f
                )
                if (herringboneSecondSpike) {
                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.40f),
                        start = Offset(x, size.height),
                        end = Offset((x + kySpike * 8f).coerceAtMost(leftW), 0f),
                        strokeWidth = 2.5f
                    )
                }
            }

            // Kanan: Distorsi implan logam & signal void
            val cx = size.width * 0.74f
            val cy = size.height * 0.5f
            val voidR = (14f + maxPixelShift * 6.5f).coerceAtMost(size.height * 0.42f)
            drawCircle(color = Color.Black, radius = voidR, center = Offset(cx, cy))
            drawCircle(
                color = Color(0xFFFACC15),
                radius = voidR + 4f,
                center = Offset(cx + maxPixelShift * 2f, cy),
                style = Stroke(width = 4f)
            )
        }

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Sekuens $seqType (${if (is3T) "3,0 T" else "1,5 T"}): Pergeseran Piksel Maks ≈ ${String.format(Locale("id", "ID"), "%.1f", maxPixelShift)} piksel · Area Kehilangan Sinyal (>50%) ≈ ${String.format(Locale("id", "ID"), "%.0f", signalLossAreaMm2)} mm².",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Rekomendasi Implan Logam: Gunakan Spin Echo / Fast Spin Echo (FSE), TE pendek, Bandwidth penerima tinggi, dan hindari GRE/EPI.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

data class Table88Entry(
    val artifactName: String,
    val causeSummary: String,
    val remediation: String,
    val penalty: String,
    val zeroPenalty: Boolean
)

@Composable
private fun M08CCapstoneTable88AndQuizTab(onNodeComplete: (String) -> Unit) {
    val table88 = remember {
        listOf(
            Table88Entry("1. Gerak (Phase Mismapping)", "Gerak pasien/organ periodik sepanjang sumbu pengodean fase", "Imobilisasi, gating/triggering, pita presaturasi, tukar arah fase-frekuensi", "Tukar fase-frekuensi: Tidak ada penalti", true),
            Table88Entry("2. Aliasing (Wrap-Around)", "FOV lebih kecil daripada anatomi; pencuplikan di bawah Nyquist", "Perbesar FOV, Phase Oversampling (No Phase Wrap), filter low-pass analog", "No Phase Wrap (NSA ½) / Filter frekuensi: Tidak ada penalti waktu", true),
            Table88Entry("3. Kerentanan Magnetik", "Perbedaan χ di batas udara-jaringan atau implan logam", "Gunakan Spin Echo (FSE), kurangi TE, tingkatkan Bandwidth penerima, irisan tipis", "Peningkatan BW & irisan tipis menurunkan SNR", false),
            Table88Entry("4. Pergeseran Kimia", "Beda frekuensi Larmor air–lemak (~3,5 ppm) pada arah frekuensi", "Tingkatkan Bandwidth penerima, gunakan supresi lemak / STIR, atau 1,5 T", "Bandwidth lebar menurunkan SNR", false),
            Table88Entry("5. Pembatalan Luar-Fase", "TE pada kelipatan ganjil T/2 di sekuens Gradient Echo", "Gunakan Spin Echo (pulsa 180°) atau pilih TE In-Phase (4,47 ms @1,5T)", "Tidak ada penalti bermakna", true),
            Table88Entry("6. Truncation (Gibbs Ringing)", "Pemotongan pencuplikan frekuensi tinggi di tepi ruang-k", "Tingkatkan matriks pengodean fase (mis. 128 → 256) atau filter apodisasi", "Matriks fase lebih tinggi memperpanjang waktu pindai (TA)", false),
            Table88Entry("7. Sudut Ajaib (Magic Angle)", "Serat kolagen membentuk sudut θ ≈ 54,7° terhadap B₀ pada TE pendek", "Ubah posisi pasien atau gunakan sekuens T2 dengan TE panjang (>37 ms)", "Tidak ada penalti", true),
            Table88Entry("8. Aliran (Flow Dephasing/TOF)", "Pergerakan spin darah selama gradien dan di antara pulsa RF", "Aktifkan GMN (+1, −2, +1), pasang pita presaturasi spasial di sisi hulu", "GMN memperpanjang TE minimum; Presaturasi menambah SAR", false),
            Table88Entry("9. Eksitasi Silang (Cross-Talk)", "Tumpang-tindih sayap profil pulsa RF antar-irisan berdekatan", "Gunakan urutan eksitasi irisan selang-seling (interleaved) atau celah 10–30%", "Eksitasi selang-seling: Tidak ada penalti waktu!", true),
            Table88Entry("10. Zipper (Kebocoran RF)", "Interferensi RF eksternal masuk karena pintu sangkar Faraday terbuka", "Tutup rapat pintu ruang pemindai, periksa gasket Faraday & peralatan elektronik", "Tidak ada penalti", true),
            Table88Entry("11. Moiré / Herringbone", "Lonjakan derau (spike) di ruang-k atau interferensi koil/gradien", "Gunakan Spin Echo atau panggil teknisi untuk perbaikan gradien/koil", "Memerlukan servis perangkat keras", false),
            Table88Entry("12. Shading (Ketidakseragaman)", "Pemuatan koil tidak merata, kopling pasien, atau B₁ tidak homogen", "Gunakan shimming, atur posisi bantalan busa antara pasien & koil", "Tidak ada penalti", true)
        )
    }

    var selectedIdx by remember { mutableIntStateOf(0) }
    var filterZeroPenaltyOnly by remember { mutableStateOf(false) }
    var guessQuizIdx by remember { mutableIntStateOf(0) }
    var guessSelected by remember { mutableIntStateOf(-1) }

    val displayedEntries = if (filterZeroPenaltyOnly) table88.filter { it.zeroPenalty } else table88
    val activeEntry = displayedEntries.getOrElse(selectedIdx.coerceIn(0, displayedEntries.lastIndex)) { table88.first() }

    val guessQuestions = remember {
        listOf(
            Triple(
                "Tebak Artefak #1: Garis gelap tajam ('tinta hitam') mengelilingi batas organ ginjal dan lemak retroperitoneal pada sekuens GRE TE = 2,24 ms (1,5 T).",
                listOf("Pembatalan Luar-Fase (Chemical Misregistration)", "Zipper RF", "Cross-Excitation"),
                0
            ),
            Triple(
                "Tebak Artefak #2: Garis tipis berpola terang-gelap lurus memotong citra sepanjang sumbu frekuensi akibat pintu ruang MRI kurang rapat.",
                listOf("Gibbs Ringing", "Artefak Zipper (Kebocoran Sangkar Faraday)", "Sudut Ajaib 54,7°"),
                1
            ),
            Triple(
                "Tebak Artefak #3: Manakah solusi remediasi Tabel 8.8 yang TIDAK memiliki penalti waktu pindai maupun penalti SNR?",
                listOf("Meningkatkan matriks fase dari 128 ke 512", "Urutan eksitasi irisan selang-seling (interleaved) & menukar arah fase-frekuensi", "Melebarkan bandwidth penerima 4× lipat"),
                1
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "A4 · Pemilih 12 Artefak & Tabel 8.8 (Remediasi vs Penalti) + A5 · Tebak Artefak",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        FilterChip(
            selected = filterZeroPenaltyOnly,
            onClick = {
                filterZeroPenaltyOnly = !filterZeroPenaltyOnly
                selectedIdx = 0
                onNodeComplete("M08C.T3.3")
            },
            label = { Text(if (filterZeroPenaltyOnly) "Menampilkan Hanya Solusi Tanpa Penalti" else "Filter: Tampilkan Semua 12 Artefak Tabel 8.8") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            displayedEntries.forEachIndexed { idx, item ->
                FilterChip(
                    selected = selectedIdx == idx,
                    onClick = {
                        selectedIdx = idx
                        onNodeComplete("M08A.T1.1")
                        onNodeComplete("M08C.T3.3")
                    },
                    label = { Text(item.artifactName, fontSize = 11.sp) }
                )
            }
        }

        Surface(
            color = if (activeEntry.zeroPenalty) Color(0xFF22C55E).copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(activeEntry.artifactName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("• Mekanisme Fisika: ${activeEntry.causeSummary}", style = MaterialTheme.typography.bodySmall)
                Text("• Cara Remediasi: ${activeEntry.remediation}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "• Penalti Kompromi (Tabel 8.8): ${activeEntry.penalty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activeEntry.zeroPenalty) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider()

        val q = guessQuestions[guessQuizIdx]
        Text(q.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        q.second.forEachIndexed { idx, opt ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        guessSelected = idx
                        if (idx == q.third) {
                            onNodeComplete("M08C.T3.3")
                            onNodeComplete("M08A.T1.1")
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = when {
                        guessSelected == idx && idx == q.third -> Color(0xFF22C55E).copy(alpha = 0.18f)
                        guessSelected == idx -> Color(0xFFEF4444).copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Text(opt, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(
            onClick = {
                guessQuizIdx = (guessQuizIdx + 1) % guessQuestions.size
                guessSelected = -1
            }
        ) {
            Text("Kasus Tebak Artefak Berikutnya (${guessQuizIdx + 1}/${guessQuestions.size})", fontSize = 12.sp)
        }
    }
}
