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
import com.example.ui.components.rememberRestorableScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import com.example.viewmodel.SimType
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

internal val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")

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
                icon = Icons.Default.Science,
                selected = state.selectedSimType == SimType.FISIKA_DASAR_M01,
                onClick = { viewModel.selectSimType(SimType.FISIKA_DASAR_M01) }
            )
            SimChip(
                label = "Spin & Larmor",
                icon = Icons.Default.Autorenew,
                selected = state.selectedSimType == SimType.SPIN_LARMOR,
                onClick = { viewModel.selectSimType(SimType.SPIN_LARMOR) }
            )
            SimChip(
                label = "Kontras TR & TE",
                icon = Icons.Default.ShowChart,
                selected = state.selectedSimType == SimType.KONTRAS_TR_TE,
                onClick = { viewModel.selectSimType(SimType.KONTRAS_TR_TE) }
            )
            SimChip(
                label = "Ruang-k (k-Space)",
                icon = Icons.Default.GridOn,
                selected = state.selectedSimType == SimType.RUANG_K,
                onClick = { viewModel.selectSimType(SimType.RUANG_K) }
            )
            SimChip(
                label = "M07: SAR & Keselamatan MR",
                icon = Icons.Default.Security,
                selected = state.selectedSimType == SimType.SAR_SAFETY,
                onClick = { viewModel.selectSimType(SimType.SAR_SAFETY) }
            )
            SimChip(
                label = "M05: Pesawat & Magnet",
                icon = Icons.Default.PrecisionManufacturing,
                selected = state.selectedSimType == SimType.PESAWAT_MAGNET_M05,
                onClick = { viewModel.selectSimType(SimType.PESAWAT_MAGNET_M05) }
            )
            SimChip(
                label = "M06: Gradien & RF",
                icon = Icons.Default.Tune,
                selected = state.selectedSimType == SimType.GRADIEN_RF_M06,
                onClick = { viewModel.selectSimType(SimType.GRADIEN_RF_M06) }
            )
            SimChip(
                label = "M03: Sekuens & Kontras",
                icon = Icons.Default.GraphicEq,
                selected = state.selectedSimType == SimType.SEKUENS_KONTRAS_M03,
                onClick = { viewModel.selectSimType(SimType.SEKUENS_KONTRAS_M03) }
            )
            SimChip(
                label = "M08: Artefak, Aliran & QA",
                icon = Icons.Default.AutoFixHigh,
                selected = state.selectedSimType == SimType.ARTEFAK_QA_M08,
                onClick = { viewModel.selectSimType(SimType.ARTEFAK_QA_M08) }
            )
            SimChip(
                label = "S1: Kalkulator TA & SNR",
                icon = Icons.Default.Speed,
                selected = state.selectedSimType == SimType.PROTOKOL_TA_SNR,
                onClick = { viewModel.selectSimType(SimType.PROTOKOL_TA_SNR) }
            )
            SimChip(
                label = "S2: Tab Konsol & Vendor",
                icon = Icons.Default.Dashboard,
                selected = state.selectedSimType == SimType.KONSOL_VENDOR,
                onClick = { viewModel.selectSimType(SimType.KONSOL_VENDOR) }
            )
        }

        // 3. SCROLLABLE SIMULATOR CONTENT: Hanya simulasi yang bergerak saat di-swipe!
        val simScrollState = rememberRestorableScrollState("sim_${state.selectedSimType.name}", viewModel)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(simScrollState)
        ) {
            when (state.selectedSimType) {
                SimType.FISIKA_DASAR_M01 -> Module1LabHub(
                    selectedActivityId = state.selectedM01ActivityId,
                    onSelectActivity = { viewModel.selectM01Activity(it) }
                )
                SimType.SPIN_LARMOR -> SpinLarmorSimulator(state, viewModel)
                SimType.KONTRAS_TR_TE -> Module2LabHub(
                    selectedActivityId = state.selectedM02ActivityId,
                    onSelectActivity = { viewModel.selectM02Activity(it) }
                )
                SimType.RUANG_K -> Module4LabHub(
                    selectedActivityId = state.selectedM04ActivityId,
                    onSelectActivity = { viewModel.selectM04Activity(it) }
                )
                SimType.SAR_SAFETY -> Module7SafetyLabSuite(
                    selectedActivityId = state.selectedM07ActivityId,
                    onSelectActivity = { viewModel.selectM07Activity(it) },
                    onNodeComplete = { nodeCode ->
                        viewModel.markNodeCompleted(nodeCode)
                    }
                )
                SimType.PROTOKOL_TA_SNR,
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SimChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (selected) DeepObsidian else CyanBright
            )
        },
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
                    val cy = size.height / 2f + 6.dp.toPx()
                    val coneRadius = (if (isNutationActive) 44.dp else 24.dp).toPx()
                    val coneHalfH = 7.dp.toPx()
                    val coneY = if (isNutationActive) cy + 2.dp.toPx() else cy - 24.dp.toPx()
                    val b0Bottom = cy + 28.dp.toPx()
                    val b0Top = cy - 42.dp.toPx()
                    val pivotY = cy + 14.dp.toPx()
                    val dashLen = 4.dp.toPx()

                    // Sumbu Z (B0)
                    drawLine(
                        color = CardSlateBorder,
                        start = Offset(cx, b0Bottom),
                        end = Offset(cx, b0Top),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLen, dashLen), 0f)
                    )

                    // Ellipse lintasan presesi
                    drawOval(
                        color = CyanDark.copy(alpha = 0.5f),
                        topLeft = Offset(cx - coneRadius, coneY - coneHalfH),
                        size = Size(coneRadius * 2f, coneHalfH * 2f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Hitung koordinat ujung vektor spin
                    val rad = Math.toRadians(spinAngle.toDouble())
                    val px = cx + coneRadius * cos(rad).toFloat()
                    val py = coneY + coneHalfH * sin(rad).toFloat()

                    // Vektor Spin Netto
                    val vectorColor = if (isNutationActive) EmeraldSuccess else CyanPrimary
                    drawLine(
                        color = vectorColor,
                        start = Offset(cx, pivotY),
                        end = Offset(px, py),
                        strokeWidth = 3.5.dp.toPx()
                    )

                    // Proton sphere di ujung
                    drawCircle(
                        color = CyanBright,
                        radius = 7.dp.toPx(),
                        center = Offset(px, py)
                    )

                    // B0 marker
                    drawCircle(
                        color = AmberWarning,
                        radius = 4.dp.toPx(),
                        center = Offset(cx, b0Top)
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
