package com.example.ui.simulators

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal data class Vec3(val x: Float, val y: Float, val z: Float)

internal data class ProjectedPoint(
    val screen: Offset,
    val depth: Float,
    val scale: Float
)

/**
 * Proyeksi 3D perspektif dengan rotasi kamera Azimuth (sekitar sumbu Z)
 * dan Elevasi (kemiringan vertikal).
 */
internal fun project3DPoint(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    azimuthRad: Float,
    elevationRad: Float,
    point: Vec3
): ProjectedPoint {
    val cosAz = cos(azimuthRad)
    val sinAz = sin(azimuthRad)
    val cosEl = cos(elevationRad)
    val sinEl = sin(elevationRad)

    // Rotasi sekitar sumbu Z (Azimuth)
    val x1 = point.x * cosAz - point.y * sinAz
    val y1 = point.x * sinAz + point.y * cosAz
    val z1 = point.z

    // Rotasi sekitar sumbu X kamera (Elevasi)
    val y2 = y1 * sinEl + z1 * cosEl
    val depth = y1 * cosEl - z1 * sinEl

    // Faktor perspektif lembut agar bagian depan tampak lebih dekat
    val perspective = (3.4f / (3.4f - depth * 0.45f)).coerceIn(0.75f, 1.35f)
    return ProjectedPoint(
        screen = Offset(cx + x1 * baseRadius * perspective, cy - y2 * baseRadius * perspective),
        depth = depth,
        scale = perspective
    )
}

@Composable
fun LarmorPrecession3DSimulatorCard(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    // Orientasi Kamera 3D (Azimuth & Elevasi) + Zoom
    var cameraAzimuth by rememberSaveable { mutableFloatStateOf(0.62f) }
    var cameraElevation by rememberSaveable { mutableFloatStateOf(0.42f) }
    var cameraZoom by rememberSaveable { mutableFloatStateOf(1.0f) }

    // Mode Sentuh: false = Putar Kamera 3D, true = Putar/Miringkan Vektor Magnetisasi M langsung
    var directVectorDragMode by rememberSaveable { mutableStateOf(false) }

    // Parameter Fisika Presesi & Resonansi
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var isRotatingFrame by rememberSaveable { mutableStateOf(false) }
    var flipAngleDeg by rememberSaveable { mutableFloatStateOf(35f) }
    var targetFlipAngleDeg by rememberSaveable { mutableFloatStateOf(35f) }
    var autoRelaxation by rememberSaveable { mutableStateOf(false) }
    var rfPulseActive by rememberSaveable { mutableStateOf(false) }
    var rfDetuneMhz by rememberSaveable { mutableFloatStateOf(0f) } // Selisih f_RF - f_0

    // Visibilitas Komponen Vektor 3D
    var showDecomposedComponents by rememberSaveable { mutableStateOf(true) }
    var showPrecessionCone by rememberSaveable { mutableStateOf(true) }
    var showMicroSpins by rememberSaveable { mutableStateOf(false) }
    var showTrajectoryTrail by rememberSaveable { mutableStateOf(true) }

    // Fase Presesi (rad) & Jejak 3D Ujung Vektor
    var phaseRad by rememberSaveable { mutableFloatStateOf(0f) }
    val trailPoints = remember { mutableStateListOf<Vec3>() }
    val fidWaveHistory = remember { mutableStateListOf<Float>() }

    val larmorMhz = state.larmorFreqMhz
    val rfFreqMhz = (larmorMhz + rfDetuneMhz).coerceAtLeast(1.0f)
    val isResonant = abs(rfDetuneMhz) <= 0.45f

    // Loop fisika real-time berbasis persamaan Bloch 3D
    LaunchedEffect(
        isPlaying,
        state.selectedB0,
        state.selectedNucleusGamma,
        isRotatingFrame,
        rfPulseActive,
        rfDetuneMhz,
        autoRelaxation
    ) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.04f, (nanos - lastNanos) / 1_000_000_000f)

                    // Kecepatan sudut presesi visual proporsional terhadap f0 = gamma * B0
                    val omega0Visual = (larmorMhz / 63.87f).coerceIn(0.25f, 3.2f) * 3.8f
                    val detuneVisual = rfDetuneMhz * 0.45f
                    val effectiveOmega = if (isRotatingFrame) detuneVisual else omega0Visual

                    phaseRad = (phaseRad + effectiveOmega * dt) % (2f * PI.toFloat())

                    // Dinamika nutasi saat pulsa RF aktif vs relaksasi T1/T2
                    if (rfPulseActive) {
                        // Efisiensi nutasi bergantung pada kedekatan resonansi (Lorentzian factor)
                        val resonanceEfficiency = 1f / (1f + (rfDetuneMhz / 0.8f) * (rfDetuneMhz / 0.8f))
                        val nutationSpeed = 65f * resonanceEfficiency
                        flipAngleDeg = (flipAngleDeg + nutationSpeed * dt).coerceIn(0f, 180f)
                        targetFlipAngleDeg = flipAngleDeg
                        if (flipAngleDeg >= 180f) {
                            rfPulseActive = false
                        }
                    } else if (autoRelaxation) {
                        // Relaksasi kembali menuju kesetimbangan termal +B0 (theta -> 0°)
                        val diff = 0f - flipAngleDeg
                        flipAngleDeg = (flipAngleDeg + diff * dt * 0.85f).coerceIn(0f, 180f)
                        targetFlipAngleDeg = flipAngleDeg
                        if (flipAngleDeg < 1.0f) {
                            flipAngleDeg = 0f
                            autoRelaxation = false
                        }
                    } else if (!directVectorDragMode && abs(targetFlipAngleDeg - flipAngleDeg) > 0.2f) {
                        flipAngleDeg += (targetFlipAngleDeg - flipAngleDeg) * min(1f, dt * 6f)
                    }

                    val thetaRad = flipAngleDeg * (PI.toFloat() / 180f)
                    val mx = sin(thetaRad) * cos(phaseRad)
                    val my = sin(thetaRad) * sin(phaseRad)
                    val mz = cos(thetaRad)

                    trailPoints.add(Vec3(mx, my, mz))
                    if (trailPoints.size > 75) {
                        trailPoints.removeAt(0)
                    }

                    // Tegangan GGL induksi koil penerima di sumbu X: V(t) ~ -dMx/dt ~ Mxy * sin(phase)
                    val inducedSignal = sin(thetaRad) * cos(phaseRad)
                    fidWaveHistory.add(inducedSignal)
                    if (fidWaveHistory.size > 120) {
                        fidWaveHistory.removeAt(0)
                    }
                }
                lastNanos = nanos
            }
        }
    }

    val thetaRad = flipAngleDeg * (PI.toFloat() / 180f)
    val mzRatio = cos(thetaRad)
    val mxyRatio = abs(sin(thetaRad))

    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Judul & Frekuensi Larmor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visualisasi 3D Presesi Spin Larmor",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ω₀ = γ · B₀  |  τ⃗ = M⃗ × γB⃗",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyanDark,
                    border = BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = String.format(ID_LOCALE, "f₀ = %.2f MHz", larmorMhz),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = CyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bar Mode Interaksi Sentuh: Putar Kamera 3D vs Putar Vektor M⃗ Langsung
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { directVectorDragMode = false }
                        .testTag("mode_rotate_camera_3d"),
                    color = if (!directVectorDragMode) CyanPrimary else CardSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (!directVectorDragMode) CyanBright else CardSlateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Putar Kamera 3D",
                            tint = if (!directVectorDragMode) DeepObsidian else CyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Putar Kamera 3D",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!directVectorDragMode) DeepObsidian else TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { directVectorDragMode = true }
                        .testTag("mode_drag_vector_3d"),
                    color = if (directVectorDragMode) AmberWarning else CardSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (directVectorDragMode) AmberWarning else CardSlateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Putar Vektor M",
                            tint = if (directVectorDragMode) DeepObsidian else AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Putar Vektor M⃗",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (directVectorDragMode) DeepObsidian else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // KANVAS INTERAKTIF 3D BLOCH SPHERE & PRESESI LARMOR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepObsidian)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(16.dp))
                    .clipToBounds()
                    .testTag("larmor_3d_canvas")
                    .pointerInput(directVectorDragMode) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (directVectorDragMode) {
                                // Pengguna memutar/memiringkan model vektor magnetisasi secara langsung
                                rfPulseActive = false
                                autoRelaxation = false
                                val newFlip = (flipAngleDeg + dragAmount.y * 0.45f).coerceIn(0f, 180f)
                                flipAngleDeg = newFlip
                                targetFlipAngleDeg = newFlip
                                phaseRad = (phaseRad + dragAmount.x * 0.015f) % (2f * PI.toFloat())
                                val th = flipAngleDeg * (PI.toFloat() / 180f)
                                trailPoints.add(Vec3(sin(th) * cos(phaseRad), sin(th) * sin(phaseRad), cos(th)))
                                if (trailPoints.size > 75) trailPoints.removeAt(0)
                            } else {
                                // Pengguna mengorbit kamera 3D bebas 360 derajat
                                cameraAzimuth -= dragAmount.x * 0.011f
                                cameraElevation = (cameraElevation + dragAmount.y * 0.011f).coerceIn(-1.45f, 1.45f)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.50f
                    val cy = size.height * 0.52f
                    val baseR = min(size.width, size.height) * 0.35f * cameraZoom

                    draw3DLarmorScene(
                        cx = cx,
                        cy = cy,
                        radius = baseR,
                        azimuth = cameraAzimuth,
                        elevation = cameraElevation,
                        thetaRad = thetaRad,
                        phaseRad = phaseRad,
                        isRotatingFrame = isRotatingFrame,
                        rfPulseActive = rfPulseActive,
                        isResonant = isResonant,
                        showDecomposed = showDecomposedComponents,
                        showCone = showPrecessionCone,
                        showMicroSpins = showMicroSpins,
                        showTrail = showTrajectoryTrail,
                        trailPoints = trailPoints,
                        labelPaint = labelPaint
                    )
                }

                // HUD Overlay Kiri Atas: Info Medan & Kerangka Acuan
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkNavySlate.copy(alpha = 0.82f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "B₀ = ${state.selectedB0} T (${state.selectedNucleusName})",
                        fontSize = 10.sp,
                        color = CyanBright,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isRotatingFrame) "Kerangka: Berputar (ω_RF)" else "Kerangka: Lab Statis",
                        fontSize = 10.sp,
                        color = if (isRotatingFrame) AmberWarning else TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (directVectorDragMode) "Sentuh: Geser Vektor M⃗" else "Sentuh: Geser Orbit 3D",
                        fontSize = 9.5.sp,
                        color = EmeraldSuccess
                    )
                }

                // HUD Overlay Kanan Atas: Preset Sudut Pandang Kamera 3D & Zoom
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        CameraPresetPill("3D Iso") {
                            cameraAzimuth = 0.62f
                            cameraElevation = 0.42f
                            cameraZoom = 1.0f
                        }
                        CameraPresetPill("Atas XY") {
                            cameraAzimuth = 0f
                            cameraElevation = 1.42f
                        }
                        CameraPresetPill("Samping XZ") {
                            cameraAzimuth = 0f
                            cameraElevation = 0.04f
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        CameraPresetPill("Zoom −") {
                            cameraZoom = (cameraZoom - 0.12f).coerceIn(0.75f, 1.35f)
                        }
                        CameraPresetPill("Zoom +") {
                            cameraZoom = (cameraZoom + 0.12f).coerceIn(0.75f, 1.35f)
                        }
                    }
                }

                // HUD Overlay Bawah: Telemetri Vektor Real-Time (θ, Mz, |Mxy|)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    color = DarkNavySlate.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TelemetryBadge(
                            label = "Sudut Flip θ",
                            value = "${flipAngleDeg.roundToInt()}°",
                            color = AmberWarning
                        )
                        TelemetryBadge(
                            label = "Longitudinal Mz",
                            value = "${(mzRatio * 100).roundToInt()}% M₀",
                            color = CyanBright
                        )
                        TelemetryBadge(
                            label = "Transversal |Mxy|",
                            value = "${(mxyRatio * 100).roundToInt()}% M₀",
                            color = EmeraldSuccess
                        )
                        TelemetryBadge(
                            label = "Status RF",
                            value = if (rfPulseActive) {
                                if (isResonant) "RESONAN" else "OFF-RES"
                            } else {
                                "PRESESI"
                            },
                            color = if (rfPulseActive && !isResonant) RoseDanger else EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Osiloskop Real-Time Sinyal FID pada Koil Penerima (Hukum Induksi Faraday)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DeepObsidian,
                border = BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sinyal GGL Induksi Koil Penerima (V_FID ∝ −dMx/dt)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "Amplitudo: ${(mxyRatio * 100).roundToInt()}%",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clipToBounds()
                    ) {
                        val midY = size.height / 2f
                        drawLine(
                            color = CardSlateBorder,
                            start = Offset(0f, midY),
                            end = Offset(size.width, midY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                        if (fidWaveHistory.size > 1) {
                            val wavePath = Path()
                            fidWaveHistory.forEachIndexed { idx, sample ->
                                val x = (idx.toFloat() / 119f) * size.width
                                val y = midY - sample * (size.height * 0.42f)
                                if (idx == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                            }
                            drawPath(
                                path = wavePath,
                                color = EmeraldSuccess,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Opsi Layer Visual 3D (Komponen Mz/Mxy, Kerucut Presesi, Spin Mikroskopik, Kerangka Berputar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LayerToggleChip(
                    label = "Komponen Mz & Mxy",
                    selected = showDecomposedComponents,
                    onClick = { showDecomposedComponents = !showDecomposedComponents }
                )
                LayerToggleChip(
                    label = "Kerucut Presesi",
                    selected = showPrecessionCone,
                    onClick = { showPrecessionCone = !showPrecessionCone }
                )
                LayerToggleChip(
                    label = "Jejak Spiral 3D",
                    selected = showTrajectoryTrail,
                    onClick = {
                        showTrajectoryTrail = !showTrajectoryTrail
                        if (!showTrajectoryTrail) trailPoints.clear()
                    }
                )
                LayerToggleChip(
                    label = "12 Spin Kuantum",
                    selected = showMicroSpins,
                    onClick = { showMicroSpins = !showMicroSpins }
                )
                LayerToggleChip(
                    label = "Kerangka Berputar",
                    selected = isRotatingFrame,
                    onClick = { isRotatingFrame = !isRotatingFrame }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider Sudut Kemiringan Vektor Magnetisasi (Flip Angle θ: 0° - 180°)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUDUT NUTASI / FLIP VEKTOR (θ)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${flipAngleDeg.roundToInt()}° (Mz=${String.format(ID_LOCALE, "%.2f", mzRatio)}, Mxy=${String.format(ID_LOCALE, "%.2f", mxyRatio)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = flipAngleDeg,
                onValueChange = {
                    rfPulseActive = false
                    autoRelaxation = false
                    flipAngleDeg = it
                    targetFlipAngleDeg = it
                },
                valueRange = 0f..180f,
                colors = SliderDefaults.colors(
                    thumbColor = AmberWarning,
                    activeTrackColor = AmberWarning,
                    inactiveTrackColor = CardSlate
                )
            )

            // Tombol Eksitasi Pulsa RF Cepat (0° Kesetimbangan, Pulsa 90°, Pulsa 180°, Relaksasi T1/T2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickAngleButton(
                    text = "0° (+B₀)",
                    isSelected = flipAngleDeg.roundToInt() == 0,
                    modifier = Modifier.weight(1f)
                ) {
                    rfPulseActive = false
                    autoRelaxation = false
                    targetFlipAngleDeg = 0f
                }
                QuickAngleButton(
                    text = "Pulsa 90°",
                    isSelected = flipAngleDeg.roundToInt() == 90,
                    modifier = Modifier.weight(1f)
                ) {
                    rfPulseActive = false
                    autoRelaxation = false
                    targetFlipAngleDeg = 90f
                }
                QuickAngleButton(
                    text = "Pulsa 180°",
                    isSelected = flipAngleDeg.roundToInt() == 180,
                    modifier = Modifier.weight(1f)
                ) {
                    rfPulseActive = false
                    autoRelaxation = false
                    targetFlipAngleDeg = 180f
                }
                QuickAngleButton(
                    text = "Relaksasi",
                    isSelected = autoRelaxation,
                    modifier = Modifier.weight(1f)
                ) {
                    rfPulseActive = false
                    autoRelaxation = true
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Eksperimen Resonansi Magnetik: Pencocokan Frekuensi RF (f_RF) dengan Frekuensi Larmor (f_0)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CardSlate,
                border = BorderStroke(1.dp, if (isResonant) EmeraldSuccess.copy(alpha = 0.6f) else RoseDanger.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYARAT RESONANSI RF (f_RF vs f₀)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isResonant) {
                                "RESONAN (f_RF ≈ f₀) ✓"
                            } else {
                                String.format(ID_LOCALE, "Off-Resonance (Δf = %+.1f MHz)", rfDetuneMhz)
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isResonant) EmeraldSuccess else RoseDanger,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(
                            ID_LOCALE,
                            "Frekuensi Pulsa B₁: %.2f MHz  |  Target Larmor f₀: %.2f MHz",
                            rfFreqMhz,
                            larmorMhz
                        ),
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = rfDetuneMhz,
                        onValueChange = { rfDetuneMhz = (it * 10).roundToInt() / 10f },
                        valueRange = -5.0f..5.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isResonant) EmeraldSuccess else RoseDanger,
                            activeTrackColor = if (isResonant) EmeraldSuccess else RoseDanger
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { rfDetuneMhz = 0f },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyanPrimary),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Samakan f_RF = f₀", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                rfPulseActive = !rfPulseActive
                                if (rfPulseActive) {
                                    autoRelaxation = false
                                    if (flipAngleDeg >= 175f) flipAngleDeg = 0f
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rfPulseActive) RoseDanger else CyanPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (rfPulseActive) TextPrimary else DeepObsidian,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (rfPulseActive) "Matikan Pulsa B₁" else "Pancarkan Pulsa B₁",
                                color = if (rfPulseActive) TextPrimary else DeepObsidian,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
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
                        border = BorderStroke(1.dp, if (isSel) CyanBright else CardSlateBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("${b0}T", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pemilih Inti Atom Aktif MR
            Text(
                text = "INTI ATOM AKTIF MR (RASIO GIROMAGNETIK γ - MHz/T)",
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
                NucleusSelectBtn("¹H (42.58)", state.selectedNucleusGamma == MriRepository.GAMMA_1H, Modifier.weight(1f)) {
                    viewModel.setNucleus("Hidrogen (¹H)", MriRepository.GAMMA_1H)
                }
                NucleusSelectBtn("¹⁹F (40.05)", state.selectedNucleusGamma == MriRepository.GAMMA_19F, Modifier.weight(1f)) {
                    viewModel.setNucleus("Fluorin (¹⁹F)", MriRepository.GAMMA_19F)
                }
                NucleusSelectBtn("²³Na (11.26)", state.selectedNucleusGamma == MriRepository.GAMMA_23NA, Modifier.weight(1f)) {
                    viewModel.setNucleus("Natrium (²³Na)", MriRepository.GAMMA_23NA)
                }
                NucleusSelectBtn("¹³C (10.71)", state.selectedNucleusGamma == MriRepository.GAMMA_13C, Modifier.weight(1f)) {
                    viewModel.setNucleus("Karbon (¹³C)", MriRepository.GAMMA_13C)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bar Kontrol Animasi (Putar/Jeda & Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyanPrimary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "Jeda Presesi" else "Lanjutkan Presesi",
                        fontSize = 12.sp,
                        color = CyanBright,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        cameraAzimuth = 0.62f
                        cameraElevation = 0.42f
                        cameraZoom = 1.0f
                        flipAngleDeg = 35f
                        targetFlipAngleDeg = 35f
                        rfDetuneMhz = 0f
                        rfPulseActive = false
                        autoRelaxation = false
                        isRotatingFrame = false
                        trailPoints.clear()
                        fidWaveHistory.clear()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Model 3D", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

private fun DrawScope.draw3DLarmorScene(
    cx: Float,
    cy: Float,
    radius: Float,
    azimuth: Float,
    elevation: Float,
    thetaRad: Float,
    phaseRad: Float,
    isRotatingFrame: Boolean,
    rfPulseActive: Boolean,
    isResonant: Boolean,
    showDecomposed: Boolean,
    showCone: Boolean,
    showMicroSpins: Boolean,
    showTrail: Boolean,
    trailPoints: List<Vec3>,
    labelPaint: Paint
) {
    fun proj(x: Float, y: Float, z: Float) =
        project3DPoint(cx, cy, radius, azimuth, elevation, Vec3(x, y, z))

    // 1. Bola Bloch 3D Berlapis Cahaya Radial
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF173153).copy(alpha = 0.55f),
                Color(0xFF0A1424).copy(alpha = 0.88f)
            ),
            center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
            radius = radius * 1.3f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = CardSlateBorder.copy(alpha = 0.85f),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 2. Bidang Ekuator Transversal XY (z = 0) & Meridian Bola 3D
    val equatorPath = Path()
    val meridianXPath = Path()
    val meridianYPath = Path()
    val steps = 56
    for (i in 0..steps) {
        val a = (i.toFloat() / steps) * 2f * PI.toFloat()
        val pEq = proj(cos(a), sin(a), 0f).screen
        val pMx = proj(cos(a), 0f, sin(a)).screen
        val pMy = proj(0f, cos(a), sin(a)).screen
        if (i == 0) {
            equatorPath.moveTo(pEq.x, pEq.y)
            meridianXPath.moveTo(pMx.x, pMx.y)
            meridianYPath.moveTo(pMy.x, pMy.y)
        } else {
            equatorPath.lineTo(pEq.x, pEq.y)
            meridianXPath.lineTo(pMx.x, pMx.y)
            meridianYPath.lineTo(pMy.x, pMy.y)
        }
    }
    drawPath(equatorPath, color = CyanPrimary.copy(alpha = 0.10f))
    drawPath(equatorPath, color = CyanPrimary.copy(alpha = 0.45f), style = Stroke(width = 1.4.dp.toPx()))
    drawPath(
        meridianXPath,
        color = CardSlateBorder.copy(alpha = 0.35f),
        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
    )
    drawPath(
        meridianYPath,
        color = CardSlateBorder.copy(alpha = 0.35f),
        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
    )

    // 3. Sumbu Koordinat 3D (X, Y, Z / B0)
    val origin = proj(0f, 0f, 0f).screen
    val xPos = proj(1.18f, 0f, 0f).screen
    val xNeg = proj(-1.05f, 0f, 0f).screen
    val yPos = proj(0f, 1.18f, 0f).screen
    val yNeg = proj(0f, -1.05f, 0f).screen
    val zPos = proj(0f, 0f, 1.26f).screen
    val zNeg = proj(0f, 0f, -1.12f).screen

    drawLine(CardSlateBorder.copy(alpha = 0.6f), xNeg, xPos, strokeWidth = 1.2.dp.toPx())
    drawLine(CardSlateBorder.copy(alpha = 0.6f), yNeg, yPos, strokeWidth = 1.2.dp.toPx())
    drawLine(
        CyanBright.copy(alpha = 0.45f),
        zNeg,
        zPos,
        strokeWidth = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
    )
    // Panah Medan Magnet Statis Eksternal B0 sepanjang +Z
    draw3DArrow(
        start = proj(0f, 0f, 0.92f).screen,
        end = zPos,
        color = CyanBright,
        strokeWidth = 3.dp.toPx(),
        headSize = 11.dp.toPx()
    )

    // 4. Koil Penerima RF 3D di Sumbu +X (Mendeteksi perubahan fluks Mx)
    val coilCenter = proj(1.08f, 0f, 0f).screen
    val mxNow = sin(thetaRad) * cos(phaseRad)
    val coilGlow = abs(mxNow).coerceIn(0f, 1f)
    drawCircle(
        color = EmeraldSuccess.copy(alpha = 0.15f + 0.45f * coilGlow),
        radius = 12.dp.toPx(),
        center = coilCenter
    )
    drawCircle(
        color = EmeraldSuccess,
        radius = 6.dp.toPx(),
        center = coilCenter,
        style = Stroke(width = 2.dp.toPx())
    )

    // 5. Kerucut Presesi 3D (Precession Cone)
    val sinT = sin(thetaRad)
    val cosT = cos(thetaRad)
    if (showCone && abs(sinT) > 0.03f) {
        val coneRimPath = Path()
        for (i in 0..steps) {
            val a = (i.toFloat() / steps) * 2f * PI.toFloat()
            val p = proj(sinT * cos(a), sinT * sin(a), cosT).screen
            if (i == 0) coneRimPath.moveTo(p.x, p.y) else coneRimPath.lineTo(p.x, p.y)
        }
        // Garis selimut kerucut
        for (k in 0 until 8) {
            val a = k * (PI.toFloat() / 4f)
            val rimPt = proj(sinT * cos(a), sinT * sin(a), cosT).screen
            drawLine(
                color = AmberWarning.copy(alpha = 0.18f),
                start = origin,
                end = rimPt,
                strokeWidth = 1.dp.toPx()
            )
        }
        drawPath(
            path = coneRimPath,
            color = AmberWarning.copy(alpha = 0.72f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
            )
        )
    }

    // 6. Ensemble 12 Spin Kuantum Mikroskopik (Paralel Spin-Up vs Anti-Paralel Spin-Down)
    if (showMicroSpins) {
        val spinCount = 12
        for (idx in 0 until spinCount) {
            val isSpinUp = idx < 8 // Kelebihan populasi spin-up searah B0
            val individualPhase = phaseRad + idx * (2f * PI.toFloat() / spinCount) * (1f - 0.55f * abs(sinT))
            val coneAngle = if (isSpinUp) 0.52f else (PI.toFloat() - 0.52f)
            val sx = sin(coneAngle) * cos(individualPhase) * 0.78f
            val sy = sin(coneAngle) * sin(individualPhase) * 0.78f
            val sz = cos(coneAngle) * 0.78f
            val pSpin = proj(sx, sy, sz)
            drawLine(
                color = if (isSpinUp) CyanBright.copy(alpha = 0.45f) else RoseDanger.copy(alpha = 0.40f),
                start = origin,
                end = pSpin.screen,
                strokeWidth = 1.6.dp.toPx()
            )
            drawCircle(
                color = if (isSpinUp) CyanBright else RoseDanger,
                radius = 3.2.dp.toPx() * pSpin.scale,
                center = pSpin.screen
            )
        }
    }

    // 7. Jejak Trajektori 3D Spiral Nutasi / Presesi
    if (showTrail && trailPoints.size > 2) {
        val trailPath = Path()
        trailPoints.forEachIndexed { idx, pt ->
            val p = proj(pt.x, pt.y, pt.z).screen
            if (idx == 0) trailPath.moveTo(p.x, p.y) else trailPath.lineTo(p.x, p.y)
        }
        drawPath(
            path = trailPath,
            color = CyanBright.copy(alpha = 0.55f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 8. Koordinat Vektor Magnetisasi Netto M = (Mx, My, Mz)
    val mx = sinT * cos(phaseRad)
    val my = sinT * sin(phaseRad)
    val mz = cosT
    val tipProj = proj(mx, my, mz)
    val xyProj = proj(mx, my, 0f)
    val zProj = proj(0f, 0f, mz)

    // 9. Komponen Dekomposisi 3D: Longitudinal Mz (Biru Cyan) & Transversal Mxy (Hijau Emerald)
    if (showDecomposed) {
        // Garis proyeksi putus-putus dari ujung M ke bidang XY dan sumbu Z
        drawLine(
            color = EmeraldSuccess.copy(alpha = 0.55f),
            start = tipProj.screen,
            end = xyProj.screen,
            strokeWidth = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
        )
        drawLine(
            color = CyanBright.copy(alpha = 0.55f),
            start = tipProj.screen,
            end = zProj.screen,
            strokeWidth = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
        )
        // Vektor Mz sepanjang sumbu Z
        if (abs(mz) > 0.05f) {
            draw3DArrow(
                start = origin,
                end = zProj.screen,
                color = CyanBright,
                strokeWidth = 3.2.dp.toPx(),
                headSize = 9.dp.toPx()
            )
        }
        // Vektor Mxy di bidang ekuator XY
        if (abs(sinT) > 0.05f) {
            draw3DArrow(
                start = origin,
                end = xyProj.screen,
                color = EmeraldSuccess,
                strokeWidth = 3.2.dp.toPx(),
                headSize = 9.dp.toPx()
            )
        }
    }

    // 10. Vektor Medan Pulsa RF Berputar B1 (tegak lurus terhadap Mxy di bidang XY)
    if (rfPulseActive) {
        val b1Phase = phaseRad - (PI.toFloat() / 2f)
        val b1Tip = proj(0.68f * cos(b1Phase), 0.68f * sin(b1Phase), 0f).screen
        draw3DArrow(
            start = origin,
            end = b1Tip,
            color = if (isResonant) RoseDanger else TextMuted,
            strokeWidth = 3.2.dp.toPx(),
            headSize = 9.5.dp.toPx()
        )
    }

    // 11. Vektor Magnetisasi Utama M⃗ (Kuning-Emas dengan Bola Spin Berkilau di Ujung)
    draw3DArrow(
        start = origin,
        end = tipProj.screen,
        color = AmberWarning,
        strokeWidth = 4.8.dp.toPx(),
        headSize = 13.dp.toPx()
    )
    drawCircle(
        color = AmberWarning.copy(alpha = 0.35f),
        radius = 12.dp.toPx() * tipProj.scale,
        center = tipProj.screen
    )
    drawCircle(
        color = Color.White,
        radius = 4.5.dp.toPx() * tipProj.scale,
        center = tipProj.screen
    )

    // 12. Label Sumbu 3D (+B0/Z, X, Y, M⃗)
    drawContext.canvas.nativeCanvas.apply {
        labelPaint.color = android.graphics.Color.parseColor("#38BDF8")
        labelPaint.textSize = 26f
        drawText("B₀ (+Z)", zPos.x + 10f, zPos.y + 6f, labelPaint)

        labelPaint.color = android.graphics.Color.parseColor("#94A3B8")
        labelPaint.textSize = 23f
        drawText(if (isRotatingFrame) "X'" else "X (Koil)", xPos.x + 8f, xPos.y + 6f, labelPaint)
        drawText(if (isRotatingFrame) "Y'" else "Y", yPos.x + 8f, yPos.y + 6f, labelPaint)

        labelPaint.color = android.graphics.Color.parseColor("#FBBF24")
        labelPaint.textSize = 27f
        drawText("M⃗", tipProj.screen.x + 14f, tipProj.screen.y - 10f, labelPaint)
    }
}

private fun DrawScope.draw3DArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    headSize: Float
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = hypot(dx, dy)
    if (len < 2f) return

    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    val angle = atan2(dy, dx)
    val p1 = end
    val p2 = Offset(
        end.x - headSize * cos(angle - 0.45f),
        end.y - headSize * sin(angle - 0.45f)
    )
    val p3 = Offset(
        end.x - headSize * cos(angle + 0.45f),
        end.y - headSize * sin(angle + 0.45f)
    )
    val headPath = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path = headPath, color = color)
}

@Composable
private fun CameraPresetPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = DarkNavySlate.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, CardSlateBorder)
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = CyanBright,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TelemetryBadge(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun LayerToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanDark,
            selectedLabelColor = CyanBright,
            containerColor = CardSlate,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) CyanPrimary else CardSlateBorder
        )
    )
}

@Composable
private fun QuickAngleButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) AmberWarning.copy(alpha = 0.22f) else CardSlate,
            contentColor = if (isSelected) AmberWarning else TextPrimary
        ),
        border = BorderStroke(1.dp, if (isSelected) AmberWarning else CardSlateBorder),
        shape = RoundedCornerShape(9.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun NucleusSelectBtn(
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
        border = BorderStroke(1.dp, if (isSelected) CyanPrimary else CardSlateBorder),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
