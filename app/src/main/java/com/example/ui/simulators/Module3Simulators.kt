package com.example.ui.simulators

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.example.ui.theme.*
import kotlin.math.*

// ============================================================================
// SUITE GABUNGAN MODUL 3: M03A (SPIN ECHO & TSE), M03B (GRADIENT ECHO & TUNAK), M03C (DWI & KONTRAS)
// ============================================================================
@Composable
fun Module3SequenceContrastSuite(
    initialModuleIndex: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var selectedSubModule by rememberSaveable(initialModuleIndex) { mutableIntStateOf(initialModuleIndex) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSubModule == 0,
                onClick = { selectedSubModule = 0 },
                label = { Text("M03A: Spin Echo, IR & TSE", fontSize = 11.sp, fontWeight = if (selectedSubModule == 0) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = DeepObsidian,
                    containerColor = DarkNavySlate,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = selectedSubModule == 1,
                onClick = { selectedSubModule = 1 },
                label = { Text("M03B: Gradient Echo & Keadaan Tunak", fontSize = 11.sp, fontWeight = if (selectedSubModule == 1) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberAccent,
                    selectedLabelColor = DeepObsidian,
                    containerColor = DarkNavySlate,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = selectedSubModule == 2,
                onClick = { selectedSubModule = 2 },
                label = { Text("M03C: DWI, BOLD, MTC & Kontras", fontSize = 11.sp, fontWeight = if (selectedSubModule == 2) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldSuccess,
                    selectedLabelColor = DeepObsidian,
                    containerColor = DarkNavySlate,
                    labelColor = TextSecondary
                )
            )
        }

        when (selectedSubModule) {
            0 -> SpinEchoIrTseSimulator(initialTab = 0, onNodeComplete = onNodeComplete)
            1 -> GradientEchoSteadyStateSimulator(initialTab = 0, onNodeComplete = onNodeComplete)
            2 -> SpecialContrastDwiAgentSimulator(initialTab = 0, onNodeComplete = onNodeComplete)
        }
    }
}
// ============================================================================
// SIMULATOR M03A: SPIN ECHO, INVERSION RECOVERY, TSE & DRIVE
// ============================================================================
@Composable
fun SpinEchoIrTseSimulator(
    initialTab: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var activeTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m03a_simulator_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulasi Interaktif: Sekuens Spin Echo & Pulsa Refokusing",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Eksplorasi gema 90°–180°, titik null IR (STIR/FLAIR), phase reordering TSE & pemulihan DRIVE.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "Spin Echo (90°-180°)",
                    1 to "Inversion Recovery (Null TI)",
                    2 to "TSE & Phase Reordering",
                    3 to "DRIVE Equilibrium"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = DeepObsidian,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            when (activeTab) {
                0 -> SpinEchoTabContent(onNodeComplete)
                1 -> InversionRecoveryTabContent(onNodeComplete)
                2 -> TurboSpinEchoTabContent(onNodeComplete)
                3 -> DriveEquilibriumTabContent(onNodeComplete)
            }
        }
    }
}

@Composable
private fun SpinEchoTabContent(onNodeComplete: (String) -> Unit) {
    var teMs by rememberSaveable { mutableFloatStateOf(80f) }
    var viewMode by rememberSaveable { mutableIntStateOf(0) } // 0: Grand Prix (Fase Melingkar), 1: Diagram Pulsa & Echo
    val infiniteTransition = rememberInfiniteTransition(label = "se_rot")
    val timeNorm by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeNorm"
    )

    val teNorm = (teMs / 160f).coerceIn(0.25f, 0.90f)
    val tauNorm = teNorm / 2f

    val statusText = when {
        timeNorm < 0.08f -> "Eksitasi 90° (Mxy maksimum, semua spin in-phase)"
        timeNorm < tauNorm - 0.03f -> "Dephasing T2* (spin cepat mendahului, spin lambat tertinggal)"
        timeNorm < tauNorm + 0.05f -> "Pulsa 180° Refokusing pada τ = ${(teMs / 2f).roundToInt()} ms! (Posisi spin dibalik 180°)"
        timeNorm < teNorm - 0.04f -> "Rephasing (Spin cepat mengejar dari belakang)"
        timeNorm < teNorm + 0.06f -> "Puncak Spin Echo pada TE = ${teMs.roundToInt()} ms! (Semua spin kembali sefase)"
        else -> "Setelah TE: spin kembali terdephasing menuju siklus TR berikutnya"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mode Visualisasi:", fontSize = 11.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = viewMode == 0,
                    onClick = {
                        viewMode = 0
                        onNodeComplete("M03A.T1.1")
                    },
                    label = { Text("Larmor Grand Prix", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = viewMode == 1,
                    onClick = {
                        viewMode = 1
                        onNodeComplete("M03A.T1.1")
                    },
                    label = { Text("Diagram Pulsa & Echo", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (viewMode == 0) {
                    // Grand Prix: Lingkaran fase transversal
                    val cx = w / 2f
                    val cy = h / 2f
                    val radius = min(cx, cy) - 24f

                    // Lingkaran referensi
                    drawCircle(
                        color = CardSlateBorder,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5f)
                    )

                    val isPast180 = timeNorm >= tauNorm
                    val spinSpeeds = listOf(-1.6f, -0.8f, 0f, 0.8f, 1.6f)
                    val colors = listOf(CyanBright, EmeraldSuccess, AmberAccent, RoseDanger, Color(0xFFB197FC))

                    spinSpeeds.forEachIndexed { idx, speed ->
                        val phaseAngle = if (!isPast180) {
                            (timeNorm / tauNorm.coerceAtLeast(0.05f)) * (speed * 1.8f)
                        } else {
                            val angleAtTau = 1f * (speed * 1.8f)
                            -angleAtTau + ((timeNorm - tauNorm) / tauNorm.coerceAtLeast(0.05f)) * (speed * 1.8f)
                        }

                        val sx = cx + radius * sin(phaseAngle)
                        val sy = cy - radius * cos(phaseAngle)

                        drawLine(
                            color = colors[idx],
                            start = Offset(cx, cy),
                            end = Offset(sx, sy),
                            strokeWidth = 2.5f
                        )
                        drawCircle(
                            color = colors[idx],
                            radius = 5f,
                            center = Offset(sx, sy)
                        )
                    }
                } else {
                    // Diagram garis pulsa: RF 90°, tau (180°), TE (Echo) dinamis mengikuti teMs
                    val baseline = h * 0.7f
                    val startX = 24f
                    val spanW = (w - 48f).coerceAtLeast(10f)
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(startX, baseline),
                        end = Offset(w - 24f, baseline),
                        strokeWidth = 2f
                    )

                    // Pulsa 90
                    drawRect(
                        color = CyanBright,
                        topLeft = Offset(startX + 8f, baseline - 55f),
                        size = Size(14f, 55f)
                    )

                    // Pulsa 180 pada tau
                    val tauX = startX + spanW * tauNorm
                    drawRect(
                        color = AmberAccent,
                        topLeft = Offset(tauX - 9f, baseline - 95f),
                        size = Size(18f, 95f)
                    )

                    // Sinyal echo simetris pada TE (2*tau) dengan peluruhan T2
                    val teX = startX + spanW * teNorm
                    val t2DecayFactor = exp(-teMs / 100f).coerceIn(0.25f, 1f)
                    val echoPeakH = 55f * t2DecayFactor
                    val echoPath = Path().apply {
                        moveTo(teX - 35f, baseline)
                        cubicTo(teX - 15f, baseline - 5f, teX - 5f, baseline - echoPeakH, teX, baseline - echoPeakH)
                        cubicTo(teX + 5f, baseline - echoPeakH, teX + 15f, baseline - 5f, teX + 35f, baseline)
                    }
                    drawPath(echoPath, color = EmeraldSuccess, style = Stroke(width = 2.5f))

                    // Garis waktu berjalan
                    val curX = startX + spanW * timeNorm
                    drawLine(
                        color = RoseDanger,
                        start = Offset(curX, 15f),
                        end = Offset(curX, baseline + 15f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DeepObsidian,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Fase Saat Ini: $statusText",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanBright,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Parameter Spin Echo: TE = ${teMs.roundToInt()} ms | tau = ${(teMs / 2f).roundToInt()} ms",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBright
                )
                Slider(
                    value = teMs,
                    onValueChange = {
                        teMs = it
                        onNodeComplete("M03A.T1.1")
                    },
                    valueRange = 20f..140f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = DarkNavySlate
                    )
                )
                Text(
                    text = "Rumus Sinyal SE: S = PD · (1 − e^(−TR/T1)) · e^(−TE/T2). Pulsa 180° membalikkan dephasing inhomogenitas B0 (T2*), namun peluruhan spin-spin acak T2 murni tetap berlangsung tak terbalikkan.",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun InversionRecoveryTabContent(onNodeComplete: (String) -> Unit) {
    var tiMs by rememberSaveable { mutableFloatStateOf(600f) }
    var isMagnitudeDisplay by rememberSaveable { mutableStateOf(true) }

    // Jaringan dengan T1 masing-masing (pada 1.5 T)
    val tissues = listOf(
        Triple("Lemak (Fat)", 260f, AmberAccent),
        Triple("Substansia Alba (WM)", 780f, CyanBright),
        Triple("Substansia Grisea (GM)", 920f, EmeraldSuccess),
        Triple("Cairan Bebas (CSF)", 2400f, RoseDanger)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preset Cepat Titik Nol:",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (abs(tiMs - 180f) < 20f) AmberDark else CardSlate,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (abs(tiMs - 180f) < 20f) AmberAccent else CardSlateBorder
                    ),
                    modifier = Modifier.clickable {
                        tiMs = 180f
                        onNodeComplete("M03A.T1.2")
                        onNodeComplete("M03A.T1.3")
                    }
                ) {
                    Text(
                        text = "STIR (Lemak)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (abs(tiMs - 1664f) < 30f) RoseDark else CardSlate,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (abs(tiMs - 1664f) < 30f) RoseDanger else CardSlateBorder
                    ),
                    modifier = Modifier.clickable {
                        tiMs = 1664f
                        onNodeComplete("M03A.T1.2")
                        onNodeComplete("M03A.T1.3")
                    }
                ) {
                    Text(
                        text = "FLAIR (CSF)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoseDanger,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padLeft = 40f
                val padBottom = 24f
                val plotW = w - padLeft - 16f
                val plotH = h - padBottom - 16f

                val zeroY = if (isMagnitudeDisplay) h - padBottom else 16f + plotH / 2f

                // Sumbu nol
                drawLine(
                    color = CardSlateBorder,
                    start = Offset(padLeft, zeroY),
                    end = Offset(w - 16f, zeroY),
                    strokeWidth = 1.5f
                )

                // Gambar kurva untuk tiap jaringan: Mz(t) = 1 - 2*exp(-t / T1)
                val maxT = 3000f
                tissues.forEach { (_, t1, col) ->
                    val path = Path()
                    var first = true
                    for (step in 0..100) {
                        val t = (step / 100f) * maxT
                        val rawMz = 1f - 2f * exp(-t / t1)
                        val dispMz = if (isMagnitudeDisplay) abs(rawMz) else rawMz

                        val px = padLeft + (t / maxT) * plotW
                        val py = if (isMagnitudeDisplay) {
                            zeroY - dispMz * plotH
                        } else {
                            zeroY - (dispMz / 1.1f) * (plotH / 2f)
                        }

                        if (first) {
                            path.moveTo(px, py)
                            first = false
                        } else {
                            path.lineTo(px, py)
                        }
                    }
                    drawPath(path, color = col, style = Stroke(width = 2f))
                }

                // Garis penanda slider TI
                val tiX = padLeft + (tiMs / maxT) * plotW
                drawLine(
                    color = Color.White,
                    start = Offset(tiX, 10f),
                    end = Offset(tiX, h - padBottom),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inversion Time (TI): ${tiMs.roundToInt()} ms",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        isMagnitudeDisplay = !isMagnitudeDisplay
                        onNodeComplete("M03A.T1.2")
                        onNodeComplete("M03A.T1.3")
                    }
            ) {
                Text("Magnitudo", fontSize = 10.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = isMagnitudeDisplay,
                    onCheckedChange = {
                        isMagnitudeDisplay = it
                        onNodeComplete("M03A.T1.2")
                        onNodeComplete("M03A.T1.3")
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanPrimary,
                        checkedTrackColor = CyanDark
                    )
                )
            }
        }

        Slider(
            value = tiMs,
            onValueChange = {
                tiMs = it
                onNodeComplete("M03A.T1.2")
                onNodeComplete("M03A.T1.3")
            },
            valueRange = 50f..2800f,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkNavySlate
            )
        )

        // Tabel evaluasi sinyal jaringan saat TI terpilih (desain kartu responsif)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Titik Nol Teoretis (TI_null = ln(2) · T1 ≈ 0.693 · T1):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                tissues.forEach { (name, t1, col) ->
                    val nullTi = (0.693f * t1).roundToInt()
                    val curMz = abs(1f - 2f * exp(-tiMs / t1))
                    val isAtNull = abs(tiMs - nullTi) < 40

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAtNull) AmberDark.copy(alpha = 0.35f) else CardSlate.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAtNull) AmberAccent else CardSlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = col
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "T1: ${t1.roundToInt()} ms → Null: ${nullTi} ms",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isAtNull) AmberDark else DeepObsidian,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isAtNull) AmberAccent else CardSlateBorder
                                )
                            ) {
                                Text(
                                    text = if (isAtNull) "TITIK NOL (PADAM!)" else "Sinyal: ${(curMz * 100).roundToInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAtNull) AmberAccent else CyanBright,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TurboSpinEchoTabContent(onNodeComplete: (String) -> Unit) {
    var etl by rememberSaveable { mutableIntStateOf(8) }
    var echoSpacing by rememberSaveable { mutableFloatStateOf(12f) }
    var effectiveEchoIndex by rememberSaveable { mutableIntStateOf(4) }

    val clampedEchoIdx = effectiveEchoIndex.coerceIn(1, etl)
    val effectiveTe = clampedEchoIdx * echoSpacing

    val totalScanReduction = etl
    val t2BlurringRisk = when {
        etl <= 4 -> "Rendah (Ketajaman batas jaringan sangat baik)"
        etl <= 12 -> "Sedang (Ideal untuk T2w rutin otak/tulang)"
        else -> "Tinggi (T2 blurring signifikan, SAR melonjak tinggi)"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseline = h * 0.75f

                // Garis dasar
                drawLine(
                    color = CardSlateBorder,
                    start = Offset(20f, baseline),
                    end = Offset(w - 20f, baseline),
                    strokeWidth = 1.5f
                )

                // Pulsa eksitasi 90
                drawRect(
                    color = CyanBright,
                    topLeft = Offset(25f, baseline - 50f),
                    size = Size(10f, 50f)
                )

                // Echo train: barisan pulsa 180 dan echo yang meluruh sesuai T2
                val trainW = w - 60f
                val stepW = trainW / etl
                for (i in 0 until etl) {
                    val x180 = 45f + i * stepW + stepW * 0.3f
                    val xEcho = 45f + i * stepW + stepW * 0.7f

                    // Pulsa 180
                    drawRect(
                        color = AmberAccent,
                        topLeft = Offset(x180, baseline - 75f),
                        size = Size(6f, 75f)
                    )

                    // Tinggi echo meluruh
                    val decay = exp(-((i + 1) * echoSpacing) / 80f)
                    val echoH = 45f * decay
                    val isEffective = (i + 1) == clampedEchoIdx

                    drawCircle(
                        color = if (isEffective) EmeraldSuccess else CyanPrimary.copy(alpha = 0.7f),
                        radius = 4f + echoH * 0.15f,
                        center = Offset(xEcho, baseline - echoH)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Turbo Factor / ETL: $etl", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright)
            Text("Waktu Scan: 1/${totalScanReduction}x lebih cepat", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = etl.toFloat(),
            onValueChange = {
                etl = it.roundToInt()
                if (effectiveEchoIndex > etl) effectiveEchoIndex = etl
                onNodeComplete("M03A.T1.4")
                onNodeComplete("M03A.T2.2")
            },
            valueRange = 2f..24f,
            steps = 21,
            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Echo Spacing (ESP): ${echoSpacing.roundToInt()} ms", fontSize = 11.sp, color = TextSecondary)
            Text("TE Efektif (Echo #$clampedEchoIdx): ${effectiveTe.roundToInt()} ms", fontSize = 11.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = echoSpacing,
            onValueChange = {
                echoSpacing = it
                onNodeComplete("M03A.T1.4")
                onNodeComplete("M03A.T2.2")
            },
            valueRange = 8f..20f,
            colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent)
        )

        Text("Pilih Echo Pengisi Pusat Ruang-k (k = 0): Echo #$clampedEchoIdx dari $etl", fontSize = 10.sp, color = TextSecondary)
        Slider(
            value = clampedEchoIdx.toFloat(),
            onValueChange = {
                effectiveEchoIndex = it.roundToInt().coerceIn(1, etl)
                onNodeComplete("M03A.T1.4")
                onNodeComplete("M03A.T2.2")
            },
            valueRange = 1f..etl.toFloat().coerceAtLeast(2f),
            steps = (etl - 2).coerceAtLeast(0),
            colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Phase Reordering & Kontras Citra:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("• Gema yang jatuh di TE efektif dialokasikan ke baris tengah ruang-k (k = 0) untuk mendikte kontras utama.", fontSize = 10.sp, color = TextSecondary)
                Text("• Risiko T2 Blurring & SAR: $t2BlurringRisk", fontSize = 10.sp, color = if (etl > 12) RoseDanger else AmberAccent)
            }
        }
    }
}

@Composable
private fun DriveEquilibriumTabContent(onNodeComplete: (String) -> Unit) {
    var isDriveActive by rememberSaveable { mutableStateOf(true) }
    var selectedTissue by rememberSaveable { mutableIntStateOf(0) } // 0: CSF (T2 panjang), 1: Cairan Sendi/Edema

    val tissueName = if (selectedTissue == 0) "CSF (T1=2400 ms, T2=1600 ms)" else "Edema / Cairan Sendi (T1=1200 ms, T2=400 ms)"
    val t1 = if (selectedTissue == 0) 2400f else 1200f
    val t2 = if (selectedTissue == 0) 1600f else 400f
    val et = 160f // durasi echo train
    val tr = 1500f

    // Pemulihan magnetisasi Mz
    val m0 = 1f - exp(-et / t1)
    val mz0 = if (isDriveActive) min(1f, m0 + exp(-et / t2)) else m0
    val finalMz = 1f - (1f - mz0) * exp(-(tr - et) / t1)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pilihan Jaringan Cair:", fontSize = 11.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedTissue == 0,
                    onClick = {
                        selectedTissue = 0
                        onNodeComplete("M03A.T3.1")
                    },
                    label = { Text("CSF", fontSize = 10.sp, fontWeight = if (selectedTissue == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = selectedTissue == 1,
                    onClick = {
                        selectedTissue = 1
                        onNodeComplete("M03A.T3.1")
                    },
                    label = { Text("Cairan Sendi", fontSize = 10.sp, fontWeight = if (selectedTissue == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseline = h - 20f

                // Sumbu nol
                drawLine(CardSlateBorder, Offset(30f, baseline), Offset(w - 20f, baseline), 1.5f)
                drawLine(CardSlateBorder, Offset(30f, 20f), Offset(30f, baseline), 1.5f)

                // Kurva pemulihan Mz sepanjang TR (0..TR)
                val maxT = 2000f
                val path = Path()
                var first = true
                for (s in 0..100) {
                    val t = (s / 100f) * maxT
                    val curMz = if (t < et) {
                        1f - exp(-t / t1)
                    } else {
                        1f - (1f - mz0) * exp(-(t - et) / t1)
                    }
                    val px = 30f + (t / maxT) * (w - 50f)
                    val py = baseline - curMz * (h - 40f)

                    if (first) {
                        path.moveTo(px, py)
                        first = false
                    } else {
                        path.lineTo(px, py)
                    }
                }

                drawPath(path, color = if (isDriveActive) EmeraldSuccess else RoseDanger, style = Stroke(width = 2.5f))

                // Garis pulsa DRIVE pada t = et
                val driveX = 30f + (et / maxT) * (w - 50f)
                drawLine(
                    color = AmberAccent,
                    start = Offset(driveX, 20f),
                    end = Offset(driveX, baseline),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tissueName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanBright
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDriveActive) "DRIVE Aktif (Pulsa -90°)" else "DRIVE Nonaktif (Tanpa Pulsa -90°)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDriveActive) EmeraldSuccess else TextMuted,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Switch(
                        checked = isDriveActive,
                        onCheckedChange = {
                            isDriveActive = it
                            onNodeComplete("M03A.T3.1")
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldSuccess,
                            checkedTrackColor = EmeraldDark
                        )
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Keuntungan DRIVE (Driven Equilibrium / RESTORE):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("• Sisa magnetisasi transversal (Mxy) dari jaringan T2 panjang dipaksa kembali ke sumbu z dengan pulsa RF -90° segera setelah echo train selesai.", fontSize = 10.sp, color = TextSecondary)
                Text("• Mz awal pulih melonjak ke ${(mz0 * 100).roundToInt()}%, menghasilkan sinyal akhir ${(finalMz * 100).roundToInt()}% dengan TR yang jauh lebih pendek!", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================================
// SIMULATOR M03B: GRADIENT ECHO & KEADAAN TUNAK (STEADY STATE)
// ============================================================================
@Composable
fun GradientEchoSteadyStateSimulator(
    initialTab: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var activeTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m03b_simulator_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulasi Interaktif: Sekuens Gradient Echo & Keadaan Tunak",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Eksplorasi sudut Ernst, kompor kontras GRE, dephase/rephase bipolar, steady state 3 sekuens & EPI.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "Sudut Ernst & TR",
                    1 to "Kompor GRE & Saturasi",
                    2 to "Gradien Bipolar Rewinder",
                    3 to "Tiga Sekuens Tunak",
                    4 to "T2* Servikal & Kasus A12"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent,
                            selectedLabelColor = DeepObsidian,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            when (activeTab) {
                0 -> ErnstAngleTabContent(onNodeComplete)
                1 -> GreContrastStoveTabContent(onNodeComplete)
                2 -> BipolarRewinderTabContent(onNodeComplete)
                3 -> ThreeSteadyStateTabContent(onNodeComplete)
                4 -> CervicalSpineAndCaseMatchTabContent(onNodeComplete)
            }
        }
    }
}

@Composable
private fun ErnstAngleTabContent(onNodeComplete: (String) -> Unit) {
    var trMs by rememberSaveable { mutableFloatStateOf(40f) }
    var flipAngleDeg by rememberSaveable { mutableFloatStateOf(20f) }

    val tissues = listOf(
        Triple("Lemak (T1=260 ms)", 260f, AmberAccent),
        Triple("Hati (T1=500 ms)", 500f, EmeraldSuccess),
        Triple("WM (T1=780 ms)", 780f, CyanBright),
        Triple("CSF (T1=2400 ms)", 2400f, RoseDanger)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padLeft = 40f
                val padBottom = 24f
                val plotW = w - padLeft - 16f
                val plotH = h - padBottom - 16f

                // Sumbu
                drawLine(CardSlateBorder, Offset(padLeft, h - padBottom), Offset(w - 16f, h - padBottom), 1.5f)
                drawLine(CardSlateBorder, Offset(padLeft, 16f), Offset(padLeft, h - padBottom), 1.5f)

                // Gambar kurva sinyal S(alpha) untuk tiap jaringan: S = sin(a)*(1-E1)/(1 - cos(a)*E1)
                tissues.forEach { (_, t1, col) ->
                    val e1 = exp(-trMs / t1)
                    val path = Path()
                    var first = true
                    for (deg in 0..90) {
                        val rad = deg * PI.toFloat() / 180f
                        val s = (sin(rad) * (1f - e1)) / (1f - cos(rad) * e1)
                        val px = padLeft + (deg / 90f) * plotW
                        val py = (h - padBottom) - (s / 0.6f) * plotH

                        if (first) {
                            path.moveTo(px, py)
                            first = false
                        } else {
                            path.lineTo(px, py)
                        }
                    }
                    drawPath(path, color = col, style = Stroke(width = 2f))
                }

                // Garis penanda Flip Angle terpilih
                val faX = padLeft + (flipAngleDeg / 90f) * plotW
                drawLine(
                    color = Color.White,
                    start = Offset(faX, 16f),
                    end = Offset(faX, h - padBottom),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TR Sekuens: ${trMs.roundToInt()} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright)
            Text("Sudut Flip (α): ${flipAngleDeg.roundToInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
        }

        Slider(
            value = trMs,
            onValueChange = {
                trMs = it
                onNodeComplete("M03B.T1.3")
            },
            valueRange = 5f..120f,
            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
        )

        Slider(
            value = flipAngleDeg,
            onValueChange = {
                flipAngleDeg = it
                onNodeComplete("M03B.T1.3")
            },
            valueRange = 2f..90f,
            colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sudut Ernst Teoretis: cos(α_Ernst) = e^(−TR/T1)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                tissues.forEach { (name, t1, col) ->
                    val e1 = exp(-trMs / t1)
                    val ernstRad = acos(e1.coerceIn(0f, 1f))
                    val ernstDeg = (ernstRad * 180f / PI.toFloat()).roundToInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• $name", fontSize = 10.sp, color = col, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("α_Ernst: $ernstDeg°", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (abs(flipAngleDeg - ernstDeg) < 3) AmberAccent else TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GreContrastStoveTabContent(onNodeComplete: (String) -> Unit) {
    var trMs by rememberSaveable { mutableFloatStateOf(30f) }
    var teMs by rememberSaveable { mutableFloatStateOf(8f) }
    var flipAngleDeg by rememberSaveable { mutableFloatStateOf(45f) }

    // Perhitungan sinyal steady-state aktual yang bergantung pada TR, FA, dan TE
    val rad = flipAngleDeg * PI.toFloat() / 180f
    val e1Wm = exp(-trMs / 780f)
    val e1Csf = exp(-trMs / 2400f)
    val sigWm = ((sin(rad) * (1f - e1Wm)) / (1f - cos(rad) * e1Wm).coerceAtLeast(1e-4f)) * exp(-teMs / 50f)
    val sigCsf = ((sin(rad) * (1f - e1Csf)) / (1f - cos(rad) * e1Csf).coerceAtLeast(1e-4f)) * exp(-teMs / 300f)

    // Klasifikasi bobot kontras GRE dengan memperhitungkan TR, FA, dan TE
    val weighting = when {
        flipAngleDeg >= 35f && teMs <= 12f && trMs <= 120f -> "T1-Weighted (TR pendek, FA besar, TE pendek)"
        flipAngleDeg <= 20f && teMs >= 15f -> "T2*-Weighted (FA kecil meminimalkan T1, TE panjang)"
        flipAngleDeg <= 20f && teMs <= 12f && trMs >= 80f -> "PD-Weighted (FA kecil, TR agak panjang, TE pendek)"
        else -> "Campuran / Transisional (Sesuaikan kombinasi TR, α, dan TE)"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Column {
                    Text("Kompor 3-Api Kontras GRE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Hasil: $weighting", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sinyal Relatif WM: ${(sigWm * 100).roundToInt()}% | CSF: ${(sigCsf * 100).roundToInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldSuccess
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Text("1. Tombol Waktu Pengulangan (TR): ${trMs.roundToInt()} ms", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = trMs,
                    onValueChange = {
                        trMs = it
                        onNodeComplete("M03B.T1.1")
                        onNodeComplete("M03B.T2.1")
                    },
                    valueRange = 10f..200f
                )

                Text("2. Tombol Sudut Flip (α): ${flipAngleDeg.roundToInt()}°", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = flipAngleDeg,
                    onValueChange = {
                        flipAngleDeg = it
                        onNodeComplete("M03B.T1.1")
                        onNodeComplete("M03B.T2.1")
                    },
                    valueRange = 5f..90f
                )

                Text("3. Tombol Waktu Gema (TE): ${teMs.roundToInt()} ms", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = teMs,
                    onValueChange = {
                        teMs = it
                        onNodeComplete("M03B.T1.1")
                        onNodeComplete("M03B.T2.1")
                    },
                    valueRange = 2f..35f
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Dinamika Pulsa demi Pulsa Menuju Steady State:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("• Tanpa pulsa 180°, TR yang sangat pendek (<50 ms) tidak membiarkan Mxy meluruh habis antar-pulsa.", fontSize = 10.sp, color = TextSecondary)
                Text("• Dibutuhkan 20–30 'dummy pulses' di awal pemindaian agar magnetisasi longitudinal & transversal stabil sebelum pembacaan data ruang-k dimulai.", fontSize = 10.sp, color = AmberAccent)
            }
        }
    }
}

@Composable
private fun BipolarRewinderTabContent(onNodeComplete: (String) -> Unit) {
    var isRewinderActive by rememberSaveable { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // Garis gradien bipolar dephase/rephase
                drawLine(CardSlateBorder, Offset(20f, midY), Offset(w - 20f, midY), 1.5f)

                // Lobus negatif (Dephase)
                val dephaseW = w * 0.2f
                drawRect(
                    color = RoseDanger,
                    topLeft = Offset(40f, midY),
                    size = Size(dephaseW, 40f)
                )

                // Lobus positif (Rephase - Luas 2x lipat)
                val rephaseX = 40f + dephaseW + 10f
                val rephaseW = dephaseW * 2f
                drawRect(
                    color = EmeraldSuccess,
                    topLeft = Offset(rephaseX, midY - 40f),
                    size = Size(rephaseW, 40f)
                )

                // Puncak gema GRE tepat di tengah lobus rephase
                val echoX = rephaseX + rephaseW / 2f
                drawCircle(color = CyanBright, radius = 6f, center = Offset(echoX, midY - 40f))

                // Rewinder di akhir TR
                if (isRewinderActive) {
                    val rewinderX = rephaseX + rephaseW + 20f
                    drawRect(
                        color = AmberAccent,
                        topLeft = Offset(rewinderX, midY),
                        size = Size(dephaseW, 40f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (isRewinderActive) "Gradien Rewinder Aktif (Koheren)" else "Rewinder Nonaktif (Spoiler / RF Spoiled)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isRewinderActive) AmberAccent else TextSecondary)
            Switch(
                checked = isRewinderActive,
                onCheckedChange = {
                    isRewinderActive = it
                    onNodeComplete("M03B.T1.2")
                },
                colors = SwitchDefaults.colors(checkedThumbColor = AmberAccent, checkedTrackColor = AmberDark)
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Peran Gradien Bipolar & Rewinder:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("• Gradien bipolar: lobus negatif mendefase spin dengan sengaja, lalu lobus positif merefase kembali hingga terbentuk Gradient Echo.", fontSize = 10.sp, color = TextSecondary)
                Text("• Gradien Rewinder: membalikkan pengodean fase sebelum pulsa RF berikutnya sehingga Mxy tetap koheren dan ikut menghasilkan gema di siklus berikutnya.", fontSize = 10.sp, color = CyanBright)
            }
        }
    }
}

@Composable
private fun ThreeSteadyStateTabContent(onNodeComplete: (String) -> Unit) {
    var selectedSeq by rememberSaveable { mutableIntStateOf(0) } // 0: Koheren, 1: Inkoheren (RF Spoiled), 2: Reverse-Echo

    val seqDetails = listOf(
        Triple(
            "Koheren (FID + Echo)",
            "Rewinder gradien mempertahankan Mxy sisa. Membaca kombinasi FID dan echo terstimulasi. Sinyal T2* tinggi, sangat peka terhadap aliran (Angiografi). Vendor: FISP (Siemens), GRASS (GE), FFE (Philips).",
            CyanBright
        ),
        Triple(
            "Inkoheren (RF Spoiled)",
            "Menggunakan 'jam fase' (fase RF diubah acak secara kuadratik pulsa demi pulsa) untuk mematikan sisa Mxy. Hanya membaca FID murni. Kontras T1 sangat murni, ideal untuk 3D T1 kontras gadolinium. Vendor: FLASH (Siemens), SPGR (GE), T1-FFE (Philips).",
            AmberAccent
        ),
        Triple(
            "Reverse-Echo (Echo Terstimulasi)",
            "Gema dari pulsa sebelumnya digeser dan dibaca tepat sebelum pulsa berikutnya. TE efektif = 2*TR - TE_aktual. Menghasilkan kontras T2 sejati dengan TR sangat pendek! Vendor: PSIF (Siemens), SSFP (GE), T2-FFE (Philips).",
            EmeraldSuccess
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            seqDetails.forEachIndexed { idx, (name, _, col) ->
                FilterChip(
                    selected = selectedSeq == idx,
                    onClick = {
                        selectedSeq = idx
                        onNodeComplete("M03B.T2.2")
                        onNodeComplete("M03B.T3.1")
                    },
                    label = { Text(name.split(" ")[0], fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = col,
                        selectedLabelColor = DeepObsidian,
                        containerColor = DarkNavySlate,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, seqDetails[selectedSeq].third)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(seqDetails[selectedSeq].first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = seqDetails[selectedSeq].third)
                Text(seqDetails[selectedSeq].second, fontSize = 10.sp, color = TextSecondary, lineHeight = 15.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Balanced Steady State (TrueFISP / FIESTA / b-FFE):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                Text("• Semua gradien (slice, phase, read) seimbang sempurna (netto luas nol dalam 1 TR).", fontSize = 10.sp, color = TextSecondary)
                Text("• Kontras ditentukan oleh rasio T2 / T1. Darah dan cairan tubuh tampak sangat terang hiperintens secara alami tanpa agen kontras!", fontSize = 10.sp, color = CyanBright)
            }
        }
    }
}

@Composable
private fun CervicalSpineAndCaseMatchTabContent(onNodeComplete: (String) -> Unit) {
    // Kasus interaktif A12
    val cases = listOf(
        Triple("Anatomi pergelangan kaki potongan sagital dengan bobot T1.", 1, "Sekuens inkoheren (SPGR/FLASH) hanya membaca FID sehingga anatomi T1 sangat tajam."),
        Triple("Angiografi: dibutuhkan sinyal darah yang sangat peka terhadap aliran.", 0, "Sekuens koheren (FISP/GRASS) mempertahankan koherensi fase sehingga aliran darah bersinyal tinggi."),
        Triple("Citra T1 setelah injeksi gadolinium dalam satu kali tahan napas.", 1, "Sekuens inkoheren memakai TR pendek, sangat cocok untuk citra post-kontras gadolinium cepat."),
        Triple("Citra berbobot T2 sejati dengan TR pendek untuk sendi/otak.", 2, "Reverse-echo (PSIF) menghasilkan TE efektif panjang melebihi TR untuk kontras T2 sejati.")
    )

    var currentCaseIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAnswer by rememberSaveable { mutableStateOf<Int?>(null) }
    var answeredCorrectly by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(answeredCorrectly) {
        if (answeredCorrectly) {
            onNodeComplete("M03B.T2.2")
            onNodeComplete("M03B.T2.3")
        }
    }

    val curCase = cases[currentCaseIndex]

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Skema Tip Pindai Servikal T2* vs T2
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tip Pindai Klinis: T2* vs T2 Sejati Servikal (A10)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                Text("• Hernia diskus menekan medulla spinalis: tampak sangat jelas pada gradient echo T2* karena CSF terang dan diskus gelap berkontras tajam.", fontSize = 10.sp, color = TextSecondary)
                Text("• Plak Multipel Sklerosis (MS) korda servikal: rentan terlewat pada gradient echo karena butuh TE panjang relaksasi T2 sejati spin-spin untuk menampakkan lesi.", fontSize = 10.sp, color = RoseDanger)
            }
        }

        // Aktivitas Pencocokan Kasus A12
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aktivitas A12: Cocokkan Kasus Klinis dengan Sekuens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright)
                Text("Kasus ${currentCaseIndex + 1} dari ${cases.size}:", fontSize = 10.sp, color = TextMuted)
                Text(curCase.first, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "Koheren", 1 to "Inkoheren", 2 to "Reverse-Echo").forEach { (typeId, name) ->
                        val isChosen = selectedAnswer == typeId
                        val isCorrect = curCase.second == typeId

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                selectedAnswer == null -> if (isChosen) CyanDark.copy(alpha = 0.4f) else DarkNavySlate
                                isCorrect -> EmeraldDark.copy(alpha = 0.5f)
                                isChosen -> RoseDark.copy(alpha = 0.5f)
                                else -> DarkNavySlate
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    selectedAnswer == null -> if (isChosen) CyanPrimary else CardSlateBorder
                                    isCorrect -> EmeraldSuccess
                                    isChosen -> RoseDanger
                                    else -> CardSlateBorder
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedAnswer = typeId
                                    if (typeId == curCase.second) {
                                        answeredCorrectly = true
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isChosen || (selectedAnswer != null && isCorrect)) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        selectedAnswer != null && isCorrect -> EmeraldSuccess
                                        selectedAnswer != null && isChosen -> RoseDanger
                                        isChosen -> CyanBright
                                        else -> TextPrimary
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (selectedAnswer != null) {
                    Text(
                        text = if (selectedAnswer == curCase.second) "✓ Benar! " + curCase.third else "✗ Kurang tepat. Coba lagi atau baca penjelasan.",
                        fontSize = 10.sp,
                        color = if (selectedAnswer == curCase.second) EmeraldSuccess else RoseDanger
                    )

                    Button(
                        onClick = {
                            selectedAnswer = null
                            currentCaseIndex = (currentCaseIndex + 1) % cases.size
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Kasus Berikutnya", fontSize = 10.sp, color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================================
// SIMULATOR M03C: DWI, STEJSKAL-TANNER, BOLD, MTC & AGEN KONTRAS
// ============================================================================
@Composable
fun SpecialContrastDwiAgentSimulator(
    initialTab: Int = 0,
    onNodeComplete: (String) -> Unit = {}
) {
    var activeTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("m03c_simulator_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulasi Interaktif: DWI, BOLD, MTC & Agen Kontras",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Eksplorasi kalkulator faktor b Stejskal-Tanner, kurva difusi ADC, fMRI BOLD, MTC & relaksivitas Gd/Fe.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "DWI & Nilai b (Stejskal)",
                    1 to "Agen Kontras (Gd vs Fe)",
                    2 to "fMRI BOLD & Saturasi MTC"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess,
                            selectedLabelColor = DeepObsidian,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            when (activeTab) {
                0 -> DwiStejskalTabContent(onNodeComplete)
                1 -> ContrastAgentTabContent(onNodeComplete)
                2 -> BoldAndMtcTabContent(onNodeComplete)
            }
        }
    }
}

@Composable
private fun DwiStejskalTabContent(onNodeComplete: (String) -> Unit) {
    var gradAmpG by rememberSaveable { mutableFloatStateOf(40f) } // mT/m (10..80)
    var gradDurationDelta by rememberSaveable { mutableFloatStateOf(20f) } // ms (5..40)
    var gradSpacingBigDelta by rememberSaveable { mutableFloatStateOf(30f) } // ms (10..60)

    // Rumus b = gamma^2 * G^2 * delta^2 * (Delta - delta/3)
    // gamma = 2.675e8 rad/s/T
    val gamma = 2.675e8
    val gTesla = gradAmpG * 1e-3
    val dSec = gradDurationDelta * 1e-3
    val bigDSec = max(gradSpacingBigDelta, gradDurationDelta) * 1e-3
    val bValueSPerMm2 = (gamma * gamma * gTesla * gTesla * dSec * dSec * (bigDSec - dSec / 3.0) / 1e6).toFloat()

    // 4 Jaringan: Nama, ADC (x10^-3 mm2/s), Color
    val dwiTissues = listOf(
        Triple("CSF (Difusi Bebas)", 2.94f, CyanBright),
        Triple("Substansia Grisea (GM)", 0.76f, EmeraldSuccess),
        Triple("Substansia Alba (WM)", 0.45f, AmberAccent),
        Triple("Lesi Stroke Akut (Restriksi)", 0.30f, RoseDanger)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkNavySlate)
                .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padLeft = 40f
                val padBottom = 24f
                val plotW = w - padLeft - 16f
                val plotH = h - padBottom - 16f

                // Sumbu b (0..4000 s/mm2) dan Sinyal S/S0 (0..1)
                drawLine(CardSlateBorder, Offset(padLeft, h - padBottom), Offset(w - 16f, h - padBottom), 1.5f)
                drawLine(CardSlateBorder, Offset(padLeft, 16f), Offset(padLeft, h - padBottom), 1.5f)

                // Garis referensi 0.5 dan 1.0
                drawLine(CardSlateBorder.copy(alpha = 0.5f), Offset(padLeft, 16f), Offset(w - 16f, 16f), 1f)
                drawLine(CardSlateBorder.copy(alpha = 0.5f), Offset(padLeft, 16f + plotH / 2f), Offset(w - 16f, 16f + plotH / 2f), 1f)

                val maxB = 4000f
                dwiTissues.forEach { (_, adc, col) ->
                    val path = Path()
                    var first = true
                    for (step in 0..100) {
                        val b = (step / 100f) * maxB
                        val signal = exp(-b * adc * 1e-3f)
                        val px = padLeft + (b / maxB) * plotW
                        val py = (h - padBottom) - signal * plotH

                        if (first) {
                            path.moveTo(px, py)
                            first = false
                        } else {
                            path.lineTo(px, py)
                        }
                    }
                    drawPath(path, color = col, style = Stroke(width = 2f))
                }

                // Penanda b-value saat ini
                val curBX = padLeft + (min(bValueSPerMm2, maxB) / maxB) * plotW
                drawLine(
                    color = Color.White,
                    start = Offset(curBX, 16f),
                    end = Offset(curBX, h - padBottom),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nilai b Dihitung: ${bValueSPerMm2.roundToInt()} s/mm²",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (bValueSPerMm2 in 900f..1200f) AmberDark else DarkNavySlate,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (bValueSPerMm2 in 900f..1200f) AmberAccent else CardSlateBorder
                )
            ) {
                Text(
                    text = if (bValueSPerMm2 in 900f..1200f) "STANDAR (b~1000)" else "b ekstrinsik",
                    fontSize = 10.sp,
                    color = AmberAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Text("Kekuatan Gradien Difusi G: ${gradAmpG.roundToInt()} mT/m", fontSize = 10.sp, color = TextSecondary)
        Slider(
            value = gradAmpG,
            onValueChange = {
                gradAmpG = it
                onNodeComplete("M03C.T1.1")
                onNodeComplete("M03C.T2.3")
            },
            valueRange = 10f..80f,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkNavySlate
            )
        )

        Text("Durasi Gradien δ (delta kecil): ${gradDurationDelta.roundToInt()} ms", fontSize = 10.sp, color = TextSecondary)
        Slider(
            value = gradDurationDelta,
            onValueChange = {
                gradDurationDelta = it
                onNodeComplete("M03C.T1.1")
                onNodeComplete("M03C.T2.3")
            },
            valueRange = 5f..40f,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkNavySlate
            )
        )

        Text("Interval Antar-Gradien Δ (Delta besar): ${gradSpacingBigDelta.roundToInt()} ms", fontSize = 10.sp, color = TextSecondary)
        Slider(
            value = gradSpacingBigDelta,
            onValueChange = {
                gradSpacingBigDelta = it
                onNodeComplete("M03C.T1.1")
                onNodeComplete("M03C.T2.3")
            },
            valueRange = 10f..60f,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkNavySlate
            )
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Mengapa Lesi Stroke Akut Tampak Terang di DWI?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoseDanger)
                Text("• Pada iskemia akut, edema sitotoksik mempersempit ruang ekstraseluler sehingga difusi air terhambat (ADC rendah ~0.30).", fontSize = 10.sp, color = TextSecondary)
                Text("• Karena ADC rendah, sinyalnya tidak meluruh (S = S0 · e^(−b·ADC)) sehingga tetap terang benderang di b=1000!", fontSize = 10.sp, color = CyanBright)
            }
        }
    }
}

@Composable
private fun ContrastAgentTabContent(onNodeComplete: (String) -> Unit) {
    var agentType by rememberSaveable { mutableIntStateOf(0) } // 0: Gadolinium (T1), 1: Besi Superparamagnetik (T2)
    var agentConc by rememberSaveable { mutableFloatStateOf(0.4f) } // mmol/L
    var selectedTissue by rememberSaveable { mutableIntStateOf(0) } // 0: WM, 1: GM, 2: CSF

    val tissueData = listOf(
        Triple("Substansia Alba (WM)", 780f, 70f),
        Triple("Substansia Grisea (GM)", 920f, 80f),
        Triple("CSF", 2400f, 1600f)
    )
    val curTissue = tissueData[selectedTissue.coerceIn(0, tissueData.lastIndex)]

    // Relaksivitas (r1, r2)
    val r1 = if (agentType == 0) 4f else 3f
    val r2 = if (agentType == 0) 5f else 100f

    // 1/T1_post = 1/T1_pre + r1*C
    val t1Post = 1f / (1f / curTissue.second + r1 * agentConc / 1000f)
    val t2Post = 1f / (1f / curTissue.third + r2 * agentConc / 1000f)

    // Sinyal T1W (TR=500, TE=15)
    val sPre = (1f - exp(-500f / curTissue.second)) * exp(-15f / curTissue.third)
    val sPost = (1f - exp(-500f / t1Post)) * exp(-15f / t2Post)
    val enhancementPct = ((sPost / sPre - 1f) * 100f).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Jenis Agen Kontras:", fontSize = 11.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = agentType == 0,
                    onClick = {
                        agentType = 0
                        onNodeComplete("M03C.T1.4")
                        onNodeComplete("M03C.T2.1")
                        onNodeComplete("M03C.T2.2")
                    },
                    label = { Text("Gadolinium (T1)", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanPrimary, selectedLabelColor = DeepObsidian)
                )
                FilterChip(
                    selected = agentType == 1,
                    onClick = {
                        agentType = 1
                        onNodeComplete("M03C.T1.4")
                        onNodeComplete("M03C.T2.1")
                        onNodeComplete("M03C.T2.2")
                    },
                    label = { Text("Besi (SPIO T2)", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberAccent, selectedLabelColor = DeepObsidian)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Target Jaringan (${curTissue.first}):", fontSize = 11.sp, color = TextSecondary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tissueData.forEachIndexed { idx, (name, _, _) ->
                    FilterChip(
                        selected = selectedTissue == idx,
                        onClick = {
                            selectedTissue = idx
                            onNodeComplete("M03C.T1.4")
                            onNodeComplete("M03C.T2.1")
                            onNodeComplete("M03C.T2.2")
                        },
                        label = { Text(name, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess,
                            selectedLabelColor = DeepObsidian,
                            containerColor = DarkNavySlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (agentType == 0) CyanPrimary else AmberAccent)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (agentType == 0) "Agen Paramagnetik Gadolinium (Gd-DTPA)" else "Agen Superparamagnetik Besi (SPIO / USPIO)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (agentType == 0) CyanBright else AmberAccent
                )
                Text(
                    text = "Konsentrasi Agen: ${(agentConc * 10).roundToInt() / 10f} mmol/L | T1 Baru: ${t1Post.roundToInt()} ms | T2 Baru: ${t2Post.roundToInt()} ms",
                    fontSize = 10.sp,
                    color = TextPrimary
                )
                Slider(
                    value = agentConc,
                    onValueChange = {
                        agentConc = it
                        onNodeComplete("M03C.T1.4")
                        onNodeComplete("M03C.T2.1")
                        onNodeComplete("M03C.T2.2")
                    },
                    valueRange = 0f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (agentType == 0) CyanPrimary else AmberAccent,
                        activeTrackColor = if (agentType == 0) CyanPrimary else AmberAccent,
                        inactiveTrackColor = DarkNavySlate
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Perubahan Sinyal T1w:",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (enhancementPct >= 0) EmeraldDark.copy(alpha = 0.4f) else RoseDark.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (enhancementPct >= 0) EmeraldSuccess else RoseDanger
                        )
                    ) {
                        Text(
                            text = if (enhancementPct >= 0) "+$enhancementPct% (LEBIH TERANG)" else "$enhancementPct% (GELAP)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (enhancementPct >= 0) EmeraldSuccess else RoseDanger,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kurva Bifasik Konsentrasi & Aturan STIR:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("• Pada konsentrasi moderat, efek pemendekan T1 dominan sehingga sinyal naik terang.", fontSize = 10.sp, color = TextSecondary)
                Text("• Pada konsentrasi sangat tinggi (misal kandung kemih), efek T2 ikut memendek drastis sehingga sinyal justru anjlok hitam (T2 susceptibility black-out).", fontSize = 10.sp, color = AmberAccent)
                Text("• PERINGATAN: Jangan gunakan STIR setelah gadolinium karena T1 lesi yang menyerap kontras memendek mirip T1 lemak sehingga ikut tertekan padam!", fontSize = 10.sp, color = RoseDanger, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BoldAndMtcTabContent(onNodeComplete: (String) -> Unit) {
    var brainActivationPct by rememberSaveable { mutableFloatStateOf(60f) }
    var mtcOffsetKhz by rememberSaveable { mutableFloatStateOf(2f) }

    // Perubahan BOLD: oksihemoglobin (diamagnetik) mendesak deoksihemoglobin (paramagnetik), T2* naik
    val boldSignalIncrease = (brainActivationPct / 100f * 4.2f).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Bagian BOLD
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, RoseDanger.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "fMRI: Kontras BOLD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseDanger,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldDark.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "Sinyal: +$boldSignalIncrease%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text("Tingkat Aktivasi Korteks Otak: ${brainActivationPct.roundToInt()}%", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = brainActivationPct,
                    onValueChange = {
                        brainActivationPct = it
                        onNodeComplete("M03C.T1.2")
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = RoseDanger,
                        activeTrackColor = RoseDanger,
                        inactiveTrackColor = CardSlate
                    )
                )

                Text(
                    text = "Mekanisme: Saat neuron aktif, suplai darah beroksigen (oksiHb) melimpah melebihi konsumsi. OksiHb bersifat diamagnetik, menggantikan deoksiHb paramagnetik. Ketidakhomogenan mikrovaskuler berkurang → T2* memanjang → sinyal T2*W meningkat!",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        // Bagian MTC
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkNavySlate,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Magnetization Transfer (MTC)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberDark.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "Offset: ${mtcOffsetKhz.roundToInt()} kHz",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text("Offset Frekuensi Pulsa Saturasi: ${mtcOffsetKhz.roundToInt()} kHz", fontSize = 10.sp, color = TextSecondary)
                Slider(
                    value = mtcOffsetKhz,
                    onValueChange = {
                        mtcOffsetKhz = it
                        onNodeComplete("M03C.T1.3")
                    },
                    valueRange = 0.5f..5f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = CardSlate
                    )
                )

                Text(
                    text = "Mekanisme MTC: Pulsa saturasi off-resonance menjenuhkan proton terikat makromolekul yang berspektrum lebar. Melalui transfer magnetisasi, sinyal parenkim otak tertekan ~30-40%, sedangkan air bebas/darah (CSF & pembuluh darah) tidak terpengaruh, meningkatkan kontras MRA secara signifikan!",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
