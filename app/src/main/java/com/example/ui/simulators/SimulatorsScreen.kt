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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MriRepository
import com.example.data.TissueProperties
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
import com.example.viewmodel.SimType
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

private val ID_LOCALE = Locale("id", "ID")

@Composable
fun SimulatorsScreen(
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
        Spacer(modifier = Modifier.height(12.dp))

        // 1. PINNED TOP HEADER: Tetap di atas, tidak bergerak saat di-swipe!
        Text(
            text = "Laboratorium Virtual MRI",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2. PINNED SELECTOR SIMULATOR: Tetap di atas agar mudah berganti simulator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimChip(
                label = "M01: Fisika Dasar",
                selected = state.selectedSimType == SimType.FISIKA_DASAR_M01,
                onClick = { viewModel.selectSimType(SimType.FISIKA_DASAR_M01) }
            )
            SimChip(
                label = "Spin & Larmor",
                selected = state.selectedSimType == SimType.SPIN_LARMOR,
                onClick = { viewModel.selectSimType(SimType.SPIN_LARMOR) }
            )
            SimChip(
                label = "Kontras TR & TE",
                selected = state.selectedSimType == SimType.KONTRAS_TR_TE,
                onClick = { viewModel.selectSimType(SimType.KONTRAS_TR_TE) }
            )
            SimChip(
                label = "Ruang-k (k-Space)",
                selected = state.selectedSimType == SimType.RUANG_K,
                onClick = { viewModel.selectSimType(SimType.RUANG_K) }
            )
            SimChip(
                label = "M07: SAR & Keselamatan MR",
                selected = state.selectedSimType == SimType.SAR_SAFETY,
                onClick = { viewModel.selectSimType(SimType.SAR_SAFETY) }
            )
            SimChip(
                label = "M05: Pesawat & Magnet",
                selected = state.selectedSimType == SimType.PESAWAT_MAGNET_M05,
                onClick = { viewModel.selectSimType(SimType.PESAWAT_MAGNET_M05) }
            )
            SimChip(
                label = "M06: Gradien & RF",
                selected = state.selectedSimType == SimType.GRADIEN_RF_M06,
                onClick = { viewModel.selectSimType(SimType.GRADIEN_RF_M06) }
            )
            SimChip(
                label = "M03: Sekuens & Kontras",
                selected = state.selectedSimType == SimType.SEKUENS_KONTRAS_M03,
                onClick = { viewModel.selectSimType(SimType.SEKUENS_KONTRAS_M03) }
            )
            SimChip(
                label = "M08: Artefak, Aliran & QA",
                selected = state.selectedSimType == SimType.ARTEFAK_QA_M08,
                onClick = { viewModel.selectSimType(SimType.ARTEFAK_QA_M08) }
            )
            SimChip(
                label = "S1: Kalkulator TA & SNR",
                selected = state.selectedSimType == SimType.PROTOKOL_TA_SNR,
                onClick = { viewModel.selectSimType(SimType.PROTOKOL_TA_SNR) }
            )
            SimChip(
                label = "S2: Tab Konsol & Vendor",
                selected = state.selectedSimType == SimType.KONSOL_VENDOR,
                onClick = { viewModel.selectSimType(SimType.KONSOL_VENDOR) }
            )
            SimChip(
                label = "Waktu & SNR",
                selected = state.selectedSimType == SimType.SCAN_TIME_ESTIMATOR,
                onClick = { viewModel.selectSimType(SimType.SCAN_TIME_ESTIMATOR) }
            )
        }

        // 3. SCROLLABLE SIMULATOR CONTENT: Hanya simulasi yang bergerak saat di-swipe!
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            when (state.selectedSimType) {
                SimType.FISIKA_DASAR_M01 -> Module1LabHub(
                    selectedActivityId = state.selectedM01ActivityId,
                    onSelectActivity = { viewModel.selectM01Activity(it) }
                )
                SimType.SPIN_LARMOR -> SpinLarmorSimulator(state, viewModel)
                SimType.KONTRAS_TR_TE -> {
                    Module2LabHub(
                        selectedActivityId = state.selectedM02ActivityId,
                        onSelectActivity = { viewModel.selectM02Activity(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ContrastSimulator(state, viewModel)
                }
                SimType.RUANG_K -> {
                    Module4LabHub(
                        selectedActivityId = state.selectedM04ActivityId,
                        onSelectActivity = { viewModel.selectM04Activity(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    KSpaceSimulator()
                }
                SimType.SAR_SAFETY -> SarSafetySimulator(state, viewModel)
                SimType.SCAN_TIME_ESTIMATOR -> ScanTimeEstimatorSimulator(state, viewModel)
                SimType.PROTOKOL_TA_SNR -> Module9LabSuite(
                    selectedActivityId = state.selectedM09ActivityId,
                    onSelectActivity = { viewModel.launchM09Lab(it) }
                )
                SimType.KONSOL_VENDOR -> Module9LabSuite(
                    selectedActivityId = state.selectedM09ActivityId,
                    onSelectActivity = { viewModel.launchM09Lab(it) }
                )
                SimType.PESAWAT_MAGNET_M05 -> Module5HardwareLabSuite(
                    selectedActivityId = state.selectedM05ActivityId,
                    onSelectActivity = { viewModel.selectM05Activity(it) }
                )
                SimType.GRADIEN_RF_M06 -> Module6GradientRfLabSuite(
                    selectedActivityId = state.selectedM06ActivityId,
                    onSelectActivity = { viewModel.selectM06Activity(it) }
                )
                SimType.SEKUENS_KONTRAS_M03 -> Module3SequenceContrastSuite(
                    selectedActivityId = state.selectedM03ActivityId,
                    onSelectActivity = { viewModel.selectM03Activity(it) },
                    onNodeComplete = { code ->
                        viewModel.markNodeCompleted(code)
                    }
                )
                SimType.ARTEFAK_QA_M08 -> Module8ArtifactsFlowQaSuite(
                    selectedActivityId = state.selectedM08ActivityId,
                    onSelectActivity = { viewModel.selectM08Activity(it) },
                    onNodeComplete = { code ->
                        viewModel.markNodeCompleted(code)
                    }
                )
            }

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
private fun SimChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanPrimary,
            selectedLabelColor = DeepObsidian,
            containerColor = DarkNavySlate,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) CyanBright else CardSlateBorder
        )
    )
}

// -------------------------------------------------------------
// 1. SPIN & LARMOR SIMULATOR
// -------------------------------------------------------------
@Composable
fun SpinLarmorSimulator(state: MriUiState, viewModel: MriViewModel) {
    var isNutationActive by rememberSaveable { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "spin_rotation")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (1600 / (state.selectedB0 * 0.8f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        text = "Presesi Spin Kuantum & Frekuensi Larmor (f₀)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Persamaan: f₀ = γ · B₀",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = String.format(ID_LOCALE, "%.2f MHz", state.larmorFreqMhz),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = CyanBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas animasi 3D Presesi Spin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidian),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f + 15f
                    val b0Height = 85f
                    val coneRadius = if (isNutationActive) 95f else 50f
                    val coneY = if (isNutationActive) cy + 5f else cy - 50f

                    // Sumbu Z (B0)
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(cx, cy + 60f),
                        end = Offset(cx, cy - 90f),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Ellipse lintasan presesi
                    drawOval(
                        color = CyanDark.copy(alpha = 0.5f),
                        topLeft = Offset(cx - coneRadius, coneY - 15f),
                        size = Size(coneRadius * 2f, 30f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Hitung koordinat ujung vektor spin
                    val rad = Math.toRadians(spinAngle.toDouble())
                    val px = cx + coneRadius * cos(rad).toFloat()
                    val py = coneY + 15f * sin(rad).toFloat()

                    // Vektor Spin Netto
                    val vectorColor = if (isNutationActive) EmeraldSuccess else CyanPrimary
                    drawLine(
                        color = vectorColor,
                        start = Offset(cx, cy + 30f),
                        end = Offset(px, py),
                        strokeWidth = 3.5.dp.toPx()
                    )

                    // Proton sphere di ujung
                    drawCircle(
                        color = CyanBright,
                        radius = 7.dp.toPx(),
                        center = Offset(px, py)
                    )

                    // B0 Label
                    drawCircle(
                        color = AmberWarning,
                        radius = 4.dp.toPx(),
                        center = Offset(cx, cy - 90f)
                    )
                }

                // Overlay status text
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Medan B₀: ${state.selectedB0} Tesla",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Inti: ${state.selectedNucleusName}",
                        fontSize = 11.sp,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (isNutationActive) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        color = EmeraldDark.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Pulsa RF Aktif! Nutasi Vektor NMV ke Bidang Transversal (xy)",
                            color = EmeraldSuccess,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pemilih Kuat Medan B0
            Text(
                text = "KUAT MEDAN MAGNET STATIS (B₀ - TESLA)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.5f, 1.5f, 3.0f, 7.0f).forEach { b0 ->
                    val isSel = state.selectedB0 == b0
                    OutlinedButton(
                        onClick = { viewModel.setB0(b0) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSel) CyanPrimary else CardSlate,
                            contentColor = if (isSel) DeepObsidian else TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyanBright else CardSlateBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                    ) {
                        Text("${b0}T", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pemilih Inti Atom Aktif MR
            Text(
                text = "INTI ATOM AKTIF MR (RASIO GIROMAGNETIK γ)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NucleusButton("¹H (42.58)", state.selectedNucleusGamma == MriRepository.GAMMA_1H, Modifier.weight(1f)) {
                    viewModel.setNucleus("Hidrogen (¹H)", MriRepository.GAMMA_1H)
                }
                NucleusButton("¹⁹F (40.05)", state.selectedNucleusGamma == MriRepository.GAMMA_19F, Modifier.weight(1f)) {
                    viewModel.setNucleus("Fluorin (¹⁹F)", MriRepository.GAMMA_19F)
                }
                NucleusButton("²³Na (11.26)", state.selectedNucleusGamma == MriRepository.GAMMA_23NA, Modifier.weight(1f)) {
                    viewModel.setNucleus("Natrium (²³Na)", MriRepository.GAMMA_23NA)
                }
                NucleusButton("¹³C (10.71)", state.selectedNucleusGamma == MriRepository.GAMMA_13C, Modifier.weight(1f)) {
                    viewModel.setNucleus("Karbon (¹³C)", MriRepository.GAMMA_13C)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Kirim Pulsa RF 90
            Button(
                onClick = { isNutationActive = !isNutationActive },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNutationActive) RoseDanger else CyanPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (isNutationActive) TextPrimary else DeepObsidian,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNutationActive) "Hentikan Pulsa RF (Relaksasi FID)" else "Kirim Pulsa RF Eksitasi 90°",
                    color = if (isNutationActive) TextPrimary else DeepObsidian,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun NucleusButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) CyanDark else CardSlate,
            contentColor = if (isSelected) CyanBright else TextSecondary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanPrimary else CardSlateBorder),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

// -------------------------------------------------------------
// 2. KONTRAS TR & TE + KURVA RELAKSASI T1/T2 SIMULATOR (MODUL 2)
// -------------------------------------------------------------
@Composable
fun ContrastSimulator(state: MriUiState, viewModel: MriViewModel) {
    LaunchedEffect(state.isRelaxationPlaying) {
        while (state.isRelaxationPlaying) {
            delay(40L)
            val nextT = state.relaxationTimeMs + 25f
            viewModel.setRelaxationTime(if (nextT > 3000f) 0f else nextT)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contrast_simulator_card"),
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
                        text = "Relaksasi Longitudinal T1, Transversal T2 & Kontras TR/TE",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mz(t) = M0·(1-e^(-t/T1)) | Mxy(t) = Mxy0·e^(-t/T2)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = state.weightingType,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = CyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Visualisasi Kurva Relaksasi T1/T2, Vektor Mz & Mxy, dan Koherensi Fase Spin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidian)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(14.dp))
                    .testTag("relaxation_curve_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val gx = 28.dp.toPx()
                    val gy = 22.dp.toPx()
                    val gw = w * 0.62f - gx
                    val gh = h - 42.dp.toPx()

                    // Grid horizontal
                    for (i in 0..4) {
                        val y = gy + (gh / 4f) * i
                        drawLine(
                            color = CardSlateBorder.copy(alpha = 0.45f),
                            start = Offset(gx, y),
                            end = Offset(gx + gw, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Garis ambang 63% (T1) dan 37% (T2)
                    val y63 = gy + gh * (1f - 0.63f)
                    val y37 = gy + gh * (1f - 0.37f)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    drawLine(
                        color = EmeraldSuccess.copy(alpha = 0.4f),
                        start = Offset(gx, y63),
                        end = Offset(gx + gw, y63),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = AmberWarning.copy(alpha = 0.4f),
                        start = Offset(gx, y37),
                        end = Offset(gx + gw, y37),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    val t1 = state.selectedTissue.t1Ms.coerceAtLeast(1f)
                    val t2 = state.selectedTissue.t2Ms.coerceAtLeast(1f)

                    // Kurva T1: Mz(t) = 1 - exp(-t/T1)
                    val t1Path = Path()
                    val steps = 80
                    for (step in 0..steps) {
                        val ratioX = step.toFloat() / steps.toFloat()
                        val waktu = ratioX * 3000f
                        val valMz = 1f - exp(-waktu / t1)
                        val px = gx + ratioX * gw
                        val py = gy + gh * (1f - valMz)
                        if (step == 0) t1Path.moveTo(px, py) else t1Path.lineTo(px, py)
                    }
                    drawPath(
                        path = t1Path,
                        color = EmeraldSuccess,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Kurva T2: Mxy(t) = exp(-t/T2)
                    val t2Path = Path()
                    for (step in 0..steps) {
                        val ratioX = step.toFloat() / steps.toFloat()
                        val waktu = ratioX * 3000f
                        val valMxy = exp(-waktu / t2)
                        val px = gx + ratioX * gw
                        val py = gy + gh * (1f - valMxy)
                        if (step == 0) t2Path.moveTo(px, py) else t2Path.lineTo(px, py)
                    }
                    drawPath(
                        path = t2Path,
                        color = AmberWarning,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Garis vertikal TR dan TE
                    val xTr = gx + (state.trMs.coerceIn(0f, 3000f) / 3000f) * gw
                    drawLine(
                        color = CyanBright.copy(alpha = 0.55f),
                        start = Offset(xTr, gy),
                        end = Offset(xTr, gy + gh),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Garis vertikal Waktu Aktif (t) & titik potong pada kurva T1 dan T2
                    val xt = gx + (state.relaxationTimeMs.coerceIn(0f, 3000f) / 3000f) * gw
                    drawLine(
                        color = TextPrimary.copy(alpha = 0.85f),
                        start = Offset(xt, gy),
                        end = Offset(xt, gy + gh),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val mzNow = state.mzRecoveryRatio
                    val mxyNow = state.mxyDecayRatio
                    drawCircle(
                        color = EmeraldSuccess,
                        radius = 5.dp.toPx(),
                        center = Offset(xt, gy + gh * (1f - mzNow))
                    )
                    drawCircle(
                        color = AmberWarning,
                        radius = 5.dp.toPx(),
                        center = Offset(xt, gy + gh * (1f - mxyNow))
                    )

                    // Diagram Vektor NMV (Mz & Mxy) di Kanan Atas
                    val cx = w * 0.81f
                    val cy1 = h * 0.33f
                    val r1 = (h * 0.21f).coerceAtMost(46.dp.toPx())

                    drawCircle(
                        color = CardSlateBorder,
                        radius = r1,
                        center = Offset(cx, cy1),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(cx, cy1 - r1 - 6.dp.toPx()),
                        end = Offset(cx, cy1 + r1 + 6.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(cx - r1 - 6.dp.toPx(), cy1),
                        end = Offset(cx + r1 + 6.dp.toPx(), cy1),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Vektor Mz (Hijau vertikal) & Mxy (Kuning horizontal)
                    drawLine(
                        color = EmeraldSuccess,
                        start = Offset(cx, cy1),
                        end = Offset(cx, cy1 - mzNow * r1),
                        strokeWidth = 3.5.dp.toPx()
                    )
                    drawLine(
                        color = AmberWarning,
                        start = Offset(cx, cy1),
                        end = Offset(cx + mxyNow * r1, cy1),
                        strokeWidth = 3.5.dp.toPx()
                    )

                    // Lingkaran Koherensi Fase Spin (XY) di Kanan Bawah
                    val cy2 = h * 0.77f
                    val r2 = (h * 0.16f).coerceAtMost(34.dp.toPx())
                    drawCircle(
                        color = CardSlateBorder,
                        radius = r2,
                        center = Offset(cx, cy2),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    val spinCount = 8
                    val phaseSpread = (1f - mxyNow) * Math.PI.toFloat() * 1.8f
                    for (i in 0 until spinCount) {
                        val baseAngle = (state.relaxationTimeMs * 0.015f) + ((i - spinCount / 2f) / spinCount.toFloat()) * phaseSpread
                        val sx = cx + cos(baseAngle.toDouble()).toFloat() * (r2 - 3.dp.toPx())
                        val sy = cy2 + sin(baseAngle.toDouble()).toFloat() * (r2 - 3.dp.toPx())
                        drawLine(
                            color = CyanBright,
                            start = Offset(cx, cy2),
                            end = Offset(sx, sy),
                            strokeWidth = 1.8.dp.toPx()
                        )
                    }
                }

                // Label Legenda Kurva di Atas Kiri
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "● T1 Mz (63%)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "● T2 Mxy (37%)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "t = ${state.relaxationTimeMs.toInt()} ms",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Koherensi XY",
                    fontSize = 9.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Indikator Sinyal Real-Time (Mz, Mxy, Pembobotan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pemulihan Mz (T1)", fontSize = 9.sp, color = TextSecondary)
                        Text(
                            text = String.format(ID_LOCALE, "%.1f%%", state.mzRecoveryRatio * 100f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldSuccess,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Peluruhan Mxy (T2)", fontSize = 9.sp, color = TextSecondary)
                        Text(
                            text = String.format(ID_LOCALE, "%.1f%%", state.mxyDecayRatio * 100f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberWarning,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pembobotan Citra", fontSize = 9.sp, color = TextSecondary)
                        Text(
                            text = state.weightingType.substringBefore(" "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pemilih Jaringan Biologis (1.5T)
            Text(
                text = "JARINGAN BIOLOGIS (PARAMETER RELAKSASI PADA 1,5 TESLA)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MriRepository.tissues.forEach { tissue ->
                    val isSel = state.selectedTissue.id == tissue.id
                    Card(
                        modifier = Modifier
                            .width(142.dp)
                            .clickable { viewModel.selectTissue(tissue) }
                            .testTag("tissue_chip_${tissue.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) CyanDark else CardSlate
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) CyanPrimary else CardSlateBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = tissue.name.substringBefore(" ("),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CyanBright else TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "T1:${tissue.t1Ms.toInt()} | T2:${tissue.t2Ms.toInt()}ms",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parameter Detail Jaringan Terpilih
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = state.selectedTissue.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = state.selectedTissue.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Klinis: ${state.selectedTissue.clinicalNote}",
                        fontSize = 11.sp,
                        color = CyanBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider Waktu Pasca-Pulsa RF (t) + Tombol Animasi & Pulsa 90°
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Waktu Pasca-Pulsa RF (t)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${state.relaxationTimeMs.toInt()} ms", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = state.relaxationTimeMs,
                onValueChange = { viewModel.setRelaxationTime(it) },
                valueRange = 0f..3000f,
                colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleRelaxationPlay() },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("toggle_relaxation_anim_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRelaxationPlaying) AmberWarning else CyanPrimary
                    )
                ) {
                    Text(
                        text = if (state.isRelaxationPlaying) "⏸ Jeda Animasi" else "▶ Putar Animasi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.resetPulse90() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_pulse_90_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "⚡ Pulsa 90° (t=0)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Cepat Parameter TR/TE (B-15: Label jaringan eksplisit sesuai preset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.applyPresetT1Brain() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text("T1w Otak (WM)", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.applyPresetT2Brain() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text("T2w Otak (CSF)", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.applyPresetPdwJoint() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text("PDw Korteks (GM)", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider TR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Parameter TR (Time Repetition)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${state.trMs.toInt()} ms", fontSize = 12.sp, color = CyanBright, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = state.trMs,
                onValueChange = { viewModel.setTr(it) },
                valueRange = 100f..3500f,
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )

            // Slider TE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Parameter TE (Time Echo)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${state.teMs.toInt()} ms", fontSize = 12.sp, color = CyanBright, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = state.teMs,
                onValueChange = { viewModel.setTe(it) },
                valueRange = 10f..250f,
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visualisasi Intensitas Grayscale Hasil
            val signal = state.calculatedSignal
            val grayVal = (signal * 255f).coerceIn(10f, 255f).toInt()
            val grayColor = Color(grayVal, grayVal, grayVal)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(grayColor)
                            .border(2.dp, CardSlateBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(signal * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (grayVal > 140) DeepObsidian else TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column {
                        Text(
                            text = "Estimasi Intensitas Sinyal Voksel:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = when {
                                grayVal > 180 -> "Hiperintens (Sangat Terang)"
                                grayVal < 80 -> "Hipointens (Gelap)"
                                else -> "Isointens (Intermediet)"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (grayVal > 180) CyanBright else TextPrimary
                        )
                        Text(
                            text = String.format(ID_LOCALE, "Skor Sinyal: %.3f", signal),
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. RUANG-K (K-SPACE) SIMULATOR
// -------------------------------------------------------------
@Composable
fun KSpaceSimulator() {
    var maskMode by rememberSaveable { mutableStateOf("full") } // "full", "center", "edge"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Domain Frekuensi Spasial (k-Space) & Rekonstruksi 2D-FFT",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Zona Pusat (Kontras & SNR) vs Zona Perifer (Resolusi Spasial & Tepi)",
                style = MaterialTheme.typography.bodySmall,
                color = CyanBright
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visualisasi Matriks Ruang-k
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidian),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val matrixSize = 140f

                    // Grid garis-garis ruang-k
                    for (i in -4..4) {
                        val y = cy + i * (matrixSize / 9f)
                        drawLine(
                            color = CardSlateBorder.copy(alpha = 0.4f),
                            start = Offset(cx - matrixSize / 2f, y),
                            end = Offset(cx + matrixSize / 2f, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Pusat kontras k-space (High amplitude)
                    if (maskMode != "edge") {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CyanBright, CyanPrimary.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = 45f
                            ),
                            radius = 45f
                        )
                    }

                    // Garis sumbu kx dan ky
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(cx - matrixSize / 2f, cy),
                        end = Offset(cx + matrixSize / 2f, cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(cx, cy - matrixSize / 2f),
                        end = Offset(cx, cy + matrixSize / 2f),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Indikator Mask
                    if (maskMode == "center") {
                        // Tepi ditutup
                        drawRect(
                            color = RoseDanger.copy(alpha = 0.25f),
                            topLeft = Offset(cx - matrixSize / 2f, cy - matrixSize / 2f),
                            size = Size(matrixSize, matrixSize),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                }

                Text(
                    text = when (maskMode) {
                        "center" -> "Zona Pusat k-Space (Low-Pass Filter Spasial)\nKontras dan SNR dominan, resolusi tepi anatomi kabur"
                        "edge" -> "Zona Perifer k-Space (High-Pass Filter Spasial)\nDetail tepi dan resolusi tinggi tampak, bobot kontras hilang"
                        else -> "Matriks Penuh Ruang-k (Rekonstruksi 2D-FFT Lengkap)\nKontras, SNR, dan resolusi spasial diagnostik optimal"
                    },
                    fontSize = 11.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pilihan Masking Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { maskMode = "full" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (maskMode == "full") CyanPrimary else CardSlate,
                        contentColor = if (maskMode == "full") DeepObsidian else TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text("Penuh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { maskMode = "center" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (maskMode == "center") CyanPrimary else CardSlate,
                        contentColor = if (maskMode == "center") DeepObsidian else TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text("Pusat Saja", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { maskMode = "edge" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (maskMode == "edge") CyanPrimary else CardSlate,
                        contentColor = if (maskMode == "edge") DeepObsidian else TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text("Tepi Saja", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Text(
                    text = "Prinsip FFT: Ruang-k bukan gambar visual langsung titik-ke-titik, melainkan kumpulan frekuensi spasial. Baris demi baris diisi oleh pengodean fase per interval TR.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. SAR & SAFETY SIMULATOR
// -------------------------------------------------------------
@Composable
fun SarSafetySimulator(state: MriUiState, viewModel: MriViewModel) {
    val sar = state.calculatedSar
    val isNormalMode = sar < 2.0f
    val isFirstLevel = sar in 2.0f..4.0f
    val isSecondLevel = sar > 4.0f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Module7SafetyLabSuite(
            selectedActivityId = state.selectedM07ActivityId,
            onSelectActivity = { viewModel.selectM07Activity(it) },
            onNodeComplete = { nodeCode ->
                viewModel.markNodeCompleted(nodeCode)
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        text = "Kalkulator SAR & Termal RF",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Standar IEC 60601-2-33",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isNormalMode -> EmeraldDark
                        isFirstLevel -> AmberDark
                        else -> RoseDark
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isNormalMode -> EmeraldSuccess
                            isFirstLevel -> AmberWarning
                            else -> RoseDanger
                        }
                    )
                ) {
                    Text(
                        text = String.format(ID_LOCALE, "%.2f W/kg", sar),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = when {
                            isNormalMode -> EmeraldSuccess
                            isFirstLevel -> AmberWarning
                            else -> RoseDanger
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (state.isSarClamped) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = RoseDark.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseDanger)
                ) {
                    Text(
                        text = "⚠️ Peringatan Batas: Estimasi SAR mentah (${String.format(ID_LOCALE, "%.2f W/kg", state.rawCalculatedSar)}) melampaui batas tampilan 5,50 W/kg dan batas IEC!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseDanger,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Banner Status Tingkat Operasi
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isNormalMode -> Icons.Default.CheckCircle
                            isFirstLevel -> Icons.Default.Warning
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when {
                            isNormalMode -> EmeraldSuccess
                            isFirstLevel -> AmberWarning
                            else -> RoseDanger
                        }
                    )
                    Column {
                        Text(
                            text = when {
                                isNormalMode -> "Normal Operating Mode (< 2,0 W/kg - Aman Rutin)"
                                isFirstLevel -> "First Level Controlled Operating Mode (2,0–4,0 W/kg - Supervisi Medis)"
                                else -> "Second Level Controlled Mode (> 4,0 W/kg - Protokol Riset/IRB Khusus)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = when {
                                isNormalMode -> "Batas aman: < 2.0 W/kg untuk whole body. Kenaikan suhu tubuh < 0.5°C."
                                isFirstLevel -> "Batas: 2.0 - 4.0 W/kg. Diperlukan justifikasi klinis radiolog dan monitoring pasien."
                                else -> "Batas: > 4.0 W/kg. Risiko luka bakar RF dan hipertermia berat."
                            },
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kontrol Berat Badan Pasien
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Berat Badan Pasien", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${state.patientWeightKg.toInt()} kg", fontSize = 12.sp, color = CyanBright, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = state.patientWeightKg,
                onValueChange = { viewModel.setPatientWeight(it) },
                valueRange = 30f..130f,
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )

            // Switch Sekuens Turbo Spin Echo (TSE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sekuens Turbo Spin Echo (TSE / FSE)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Menggunakan banyak pulsa refokusing 180° beruntun", fontSize = 10.sp, color = TextSecondary)
                }
                Switch(
                    checked = state.isTseSequence,
                    onCheckedChange = { viewModel.setTseSequence(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanBright,
                        checkedTrackColor = CyanDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tips Menurunkan SAR
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Strategi Menurunkan SAR pada Konsol:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright)
                    Text("• Perpanjang nilai TR (memberi waktu pendinginan termal jaringan)", fontSize = 10.sp, color = TextSecondary)
                    Text("• Kurangi sudut flip pulsa refokusing (mis. dari 180° ke 140°)", fontSize = 10.sp, color = TextSecondary)
                    Text("• Turunkan jumlah irisan (slices per TR) atau perpendek ETL", fontSize = 10.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Catatan Edukasi Non-Klinis: Model SAR di atas adalah penyederhanaan edukatif dan tidak menggantikan dosimetri RF spesifik pasien pada konsol MRI klinis.",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }
        }
    }
}

@Composable
fun ScanTimeEstimatorSimulator(
    state: MriUiState,
    viewModel: MriViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Kalkulator Waktu & Estimator SNR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Hitung total durasi waktu pemindaian (Scan Time) dan rasio sinyal-ke-derau (SNR).",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Result Display Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DeepObsidian,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ESTIMASI WAKTU PEMINDAIAN (SCAN TIME)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val totalSec = state.estimatedScanTimeSec
                    val mins = (totalSec / 60).toInt()
                    val secs = (totalSec % 60).toInt()
                    Text(
                        text = if (mins > 0) "$mins menit $secs detik" else "$secs detik",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rumus: (TR × Ny × NEX) / ETL",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TR Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Repetition Time (TR)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${state.trMs.toInt()} ms", fontSize = 12.sp, color = CyanBright, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = state.trMs,
                onValueChange = { viewModel.setTr(it) },
                valueRange = 100f..5000f,
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Matrix Ny Control
            Text(text = "Matriks Fase (Ny): ${state.matrixNy}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(128, 192, 256, 512).forEach { ny ->
                    FilterChip(
                        selected = state.matrixNy == ny,
                        onClick = { viewModel.setMatrixNy(ny) },
                        label = { Text("$ny") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // NEX Control
            Text(text = "Number of Excitations (NEX): ${state.nexCount}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 4).forEach { nex ->
                    FilterChip(
                        selected = state.nexCount == nex,
                        onClick = { viewModel.setNexCount(nex) },
                        label = { Text("${nex}x") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ETL Control
            Text(text = "Echo Train Length (ETL): ${state.etlCount}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 4, 8, 16).forEach { etl ->
                    FilterChip(
                        selected = state.etlCount == etl,
                        onClick = { viewModel.setEtlCount(etl) },
                        label = { Text("ETL $etl") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright
                        )
                    )
                }
            }
        }
    }
}
