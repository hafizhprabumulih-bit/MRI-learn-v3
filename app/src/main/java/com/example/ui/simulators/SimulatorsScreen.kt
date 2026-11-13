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
// 1. SPIN & LARMOR 3D INTERACTIVE SIMULATOR
// -------------------------------------------------------------
@Composable
fun SpinLarmorSimulator(state: MriUiState, viewModel: MriViewModel) {
    LarmorPrecession3DSimulatorCard(
        state = state,
        viewModel = viewModel
    )
}

