package com.example.ui.animation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LearningAnimationScreen(
    state: MriUiState,
    viewModel: MriViewModel,
    modifier: Modifier = Modifier
) {
    val animList = listOf(
        "Spin Echo 90°-180°",
        "Relaksasi T1 & T2",
        "Trajektori Ruang-k"
    )

    // B-04, B-22 & FINAL-06: Sudut rotasi hanya berjalan ketika state.isAnimationPlaying == true
    // dan menggunakan rememberSaveable agar tahan rotasi layar.
    var rotAngle by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(state.isAnimationPlaying) {
        if (!state.isAnimationPlaying) return@LaunchedEffect
        var lastFrameNanos = withFrameNanos { it }
        while (state.isAnimationPlaying) {
            val frameNanos = withFrameNanos { it }
            val deltaSec = ((frameNanos - lastFrameNanos).coerceAtLeast(0L)) / 1_000_000_000f
            lastFrameNanos = frameNanos
            rotAngle = (rotAngle + deltaSec * 120f) % 360f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. PINNED TOP HEADER: Tetap di atas, tidak bergerak saat di-swipe!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = "Animasi Belajar MRI",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyanDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
            ) {
                Text(
                    text = "Interaktif",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBright,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. PINNED PILIHAN TOPIK ANIMASI: Tetap di atas agar mudah berganti topik
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            animList.forEachIndexed { index, title ->
                val isSelected = state.selectedAnimationIndex == index
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectAnimation(index) },
                    color = if (isSelected) CyanPrimary else DarkNavySlate,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanBright else CardSlateBorder)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) DeepObsidian else TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. SCROLLABLE CONTENT: Hanya bagian kartu dan penjelasan di bawah ini yang bergerak saat di-swipe!
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Card Kanvas Animasi Interaktif
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Kanvas Render Animasi berdasarkan topik terpilih
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepObsidian),
                    contentAlignment = Alignment.Center
                ) {
                    when (state.selectedAnimationIndex) {
                        0 -> SpinEchoCanvas(step = state.animationStep, isPlaying = state.isAnimationPlaying, rotAngle = rotAngle)
                        1 -> RelaxationCurvesCanvas(step = state.animationStep, isPlaying = state.isAnimationPlaying, rotAngle = rotAngle)
                        else -> KSpaceFillCanvas(step = state.animationStep, isPlaying = state.isAnimationPlaying, rotAngle = rotAngle)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pengatur Langkah Bertahap (Step 1 sampai 4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.setAnimationStep(state.animationStep - 1) },
                            enabled = state.animationStep > 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Sebelumnya", tint = if (state.animationStep > 1) CyanBright else TextMuted)
                        }

                        Text(
                            text = "Langkah ${state.animationStep} dari 4",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = { viewModel.setAnimationStep(state.animationStep + 1) },
                            enabled = state.animationStep < 4,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = "Berikutnya", tint = if (state.animationStep < 4) CyanBright else TextMuted)
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleAnimationPlay() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (state.isAnimationPlaying) CardSlate else CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isAnimationPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (state.isAnimationPlaying) CyanBright else DeepObsidian,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (state.isAnimationPlaying) "Jeda" else "Putar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isAnimationPlaying) CyanBright else DeepObsidian
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Penjelasan Fisika Medis Langkah Terpilih
                val (stepTitle, stepExplanation, analogyText) = getStepDescription(
                    animIndex = state.selectedAnimationIndex,
                    step = state.animationStep
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stepTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stepExplanation,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Analogi: $analogyText",
                                fontSize = 11.sp,
                                color = AmberWarning,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
}

// -------------------------------------------------------------
// Kanvas Animasi 1: Spin Echo 90° - 180° Refokusing
// -------------------------------------------------------------
@Composable
fun SpinEchoCanvas(step: Int, isPlaying: Boolean, rotAngle: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = 70f

        // Sumbu Z (Longitudinal) dan Bidang XY (Transversal)
        drawLine(color = CardSlateBorder, start = Offset(cx, cy + 85f), end = Offset(cx, cy - 85f), strokeWidth = 1.5.dp.toPx())
        drawOval(color = CardSlateBorder.copy(alpha = 0.5f), topLeft = Offset(cx - radius, cy - 20f), size = Size(radius * 2f, 40f), style = Stroke(1.dp.toPx()))

        when (step) {
            1 -> {
                // Step 1: Eksitasi 90° -> Vektor berpresesi di bidang XY
                val rad = Math.toRadians(rotAngle.toDouble())
                val vx = cx + radius * cos(rad).toFloat()
                val vy = cy + 20f * sin(rad).toFloat()
                drawLine(color = CyanBright, start = Offset(cx, cy), end = Offset(vx, vy), strokeWidth = 4.dp.toPx())
                drawCircle(color = CyanBright, radius = 6.dp.toPx(), center = Offset(vx, vy))
                // Pulsa 90 badge
                drawCircle(color = EmeraldSuccess, radius = 4.dp.toPx(), center = Offset(cx, cy - 70f))
            }
            2 -> {
                // Step 2: Dephasing T2* (Spin cepat mendahului, lambat tertinggal)
                val fastAngle = Math.toRadians((rotAngle * 1.5).toDouble())
                val slowAngle = Math.toRadians((rotAngle * 0.7).toDouble())
                val midAngle = Math.toRadians(rotAngle.toDouble())

                // Spin cepat (hijau)
                val fx = cx + radius * cos(fastAngle).toFloat()
                val fy = cy + 20f * sin(fastAngle).toFloat()
                drawLine(color = EmeraldSuccess, start = Offset(cx, cy), end = Offset(fx, fy), strokeWidth = 3.dp.toPx())

                // Spin sedang (cyan)
                val mx = cx + radius * cos(midAngle).toFloat()
                val my = cy + 20f * sin(midAngle).toFloat()
                drawLine(color = CyanBright, start = Offset(cx, cy), end = Offset(mx, my), strokeWidth = 3.dp.toPx())

                // Spin lambat (rose)
                val sx = cx + radius * cos(slowAngle).toFloat()
                val sy = cy + 20f * sin(slowAngle).toFloat()
                drawLine(color = RoseDanger, start = Offset(cx, cy), end = Offset(sx, sy), strokeWidth = 3.dp.toPx())
            }
            3 -> {
                // Step 3: Pulsa Refokusing 180° membalikkan arah!
                val phase = (rotAngle / 360f) * 0.6f
                val spread = (1f - phase) * 14f
                drawLine(color = EmeraldSuccess, start = Offset(cx, cy), end = Offset(cx - radius * 0.85f, cy - spread), strokeWidth = 3.dp.toPx())
                drawLine(color = CyanBright, start = Offset(cx, cy), end = Offset(cx - radius, cy), strokeWidth = 3.dp.toPx())
                drawLine(color = RoseDanger, start = Offset(cx, cy), end = Offset(cx - radius * 1.05f, cy + spread), strokeWidth = 3.dp.toPx())

                // Flash 180 RF Pulse
                drawCircle(color = AmberWarning, radius = 8.dp.toPx(), center = Offset(cx, cy))
            }
            4 -> {
                // Step 4: Rephasing & Echo Peak terbentuk di TE!
                val ripple = 14f + (rotAngle / 360f) * 16f
                drawLine(color = CyanBright, start = Offset(cx, cy), end = Offset(cx - radius * 1.2f, cy), strokeWidth = 5.dp.toPx())
                drawCircle(color = CyanBright, radius = 9.dp.toPx(), center = Offset(cx - radius * 1.2f, cy))
                // Ripple Echo Sinyal
                drawCircle(
                    color = CyanBright.copy(alpha = (1f - (rotAngle / 360f) * 0.6f).coerceIn(0.2f, 0.8f)),
                    radius = ripple.dp.toPx(),
                    center = Offset(cx - radius * 1.2f, cy),
                    style = Stroke(2.dp.toPx())
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Kanvas Animasi 2: Kurva Relaksasi T1 & T2
// -------------------------------------------------------------
@Composable
fun RelaxationCurvesCanvas(step: Int, isPlaying: Boolean, rotAngle: Float = 0f) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = 40f
        val cy = size.height - 40f
        val w = size.width - 80f
        val h = size.height - 70f

        // Sumbu X dan Y
        drawLine(color = CardSlateBorder, start = Offset(cx, cy), end = Offset(cx + w, cy), strokeWidth = 2.dp.toPx())
        drawLine(color = CardSlateBorder, start = Offset(cx, cy), end = Offset(cx, cy - h), strokeWidth = 2.dp.toPx())

        // Kurva T1 Recovery (Mz tumbuh eksponensial - lebih lambat karena T1 > T2)
        val pathT1 = Path()
        pathT1.moveTo(cx, cy)
        for (i in 0..100) {
            val t = i / 100f
            val x = cx + t * w
            val y = cy - (1f - Math.exp(-t * 2.0).toFloat()) * (h * 0.9f)
            pathT1.lineTo(x, y)
        }
        drawPath(path = pathT1, color = CyanBright, style = Stroke(width = 3.dp.toPx()))

        // Kurva T2 Decay (Mxy meluruh eksponensial - jauh lebih cepat karena T2 < T1)
        val pathT2 = Path()
        pathT2.moveTo(cx, cy - h * 0.9f)
        for (i in 0..100) {
            val t = i / 100f
            val x = cx + t * w
            val y = (cy - h * 0.9f) + (1f - Math.exp(-t * 4.5).toFloat()) * (h * 0.9f)
            pathT2.lineTo(x, y)
        }
        drawPath(path = pathT2, color = RoseDanger, style = Stroke(width = 3.dp.toPx()))

        // Titik penanda waktu aktif yang bergerak mengikuti rotAngle (dan berhenti saat Jeda)
        val baseProgress = ((step - 1) * 0.22f + (rotAngle / 360f) * 0.25f).coerceIn(0.05f, 0.98f)
        val markerX = cx + baseProgress * w
        val markerT1Y = cy - (1f - Math.exp(-baseProgress * 2.0).toFloat()) * (h * 0.9f)
        val markerT2Y = (cy - h * 0.9f) + (1f - Math.exp(-baseProgress * 4.5).toFloat()) * (h * 0.9f)

        drawLine(
            color = AmberWarning.copy(alpha = 0.6f),
            start = Offset(markerX, cy),
            end = Offset(markerX, cy - h * 0.92f),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
        drawCircle(color = CyanBright, radius = 5.dp.toPx(), center = Offset(markerX, markerT1Y))
        drawCircle(color = RoseDanger, radius = 5.dp.toPx(), center = Offset(markerX, markerT2Y))
    }
}

// -------------------------------------------------------------
// Kanvas Animasi 3: Pengisian Ruang-k
// -------------------------------------------------------------
@Composable
fun KSpaceFillCanvas(step: Int, isPlaying: Boolean, rotAngle: Float = 0f) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val matrixSize = 130f

        // Grid Matrix
        for (i in -4..4) {
            val y = cy + i * (matrixSize / 9f)
            val isFilled = (i + 4) <= (step * 2)
            drawLine(
                color = if (isFilled) CyanBright else CardSlateBorder.copy(alpha = 0.4f),
                start = Offset(cx - matrixSize / 2f, y),
                end = Offset(cx + matrixSize / 2f, y),
                strokeWidth = if (isFilled) 2.5.dp.toPx() else 1.dp.toPx()
            )
        }

        // Current trajectory pen yang melintasi baris kx mengikuti rotAngle (dan berhenti saat Jeda)
        val currY = cy + (-4 + (step * 2)) * (matrixSize / 9f)
        val scanProgress = (rotAngle / 360f).coerceIn(0f, 1f)
        val penX = (cx - matrixSize / 2f) + scanProgress * matrixSize
        drawCircle(color = AmberWarning, radius = 5.dp.toPx(), center = Offset(penX, currY))
    }
}

// Helper teks penjelasan fisika medis langkah animasi
fun getStepDescription(animIndex: Int, step: Int): Triple<String, String, String> {
    return when (animIndex) {
        0 -> when (step) {
            1 -> Triple(
                "Langkah 1: Eksitasi Pulsa RF 90° Resonansi",
                "Pulsa eksitasi radiofrekuensi 90° berfrekuensi Larmor (B1) mendepresikan vektor magnetisasi netto makroskopik (NMV / M0) dari sumbu longitudinal z rebah seutuhnya ke bidang transversal xy, menghasilkan koherensi fase presesi transversal maksimum.",
                "Menyelaraskan orientasi jarum kompas makroskopik secara serentak ke bidang horizontal di bawah induksi osilasi medan frekuensi radio resonansi."
            )
            2 -> Triple(
                "Langkah 2: Dephasing Transversal Spontan (Peluruhan T2*)",
                "Fluktuasi medan magnet lokal intravoksel (ΔB0) dan interaksi dipol-dipol spin-spin acak memicu dispersi fase: isokromat spin cepat mengakumulasi fase positif mendahului rerata, sedangkan isokromat spin lambat tertinggal mengakibatkan peluruhan sinyal FID cepat.",
                "Formasi atlet berkecepatan presesi berbeda yang awalnya berada di garis start yang sama perlahan mengalami dispersi sudut fase."
            )
            3 -> Triple(
                "Langkah 3: Pulsa Refokusing RF 180° (Inversi Fase)",
                "Tepat pada interval waktu tau = TE / 2, koil RF memancarkan pulsa refokusing 180° yang membalikkan koordinat fase seluruh isokromat spin transversal terhadap sumbu refokusing (inversi konjugat fase 180°).",
                "Instruksi pembalikan orientasi di mana isokromat spin yang tertinggal diposisikan berada di depan dan spin yang mendahului diposisikan di belakang."
            )
            4 -> Triple(
                "Langkah 4: Rephasing Koheren & Pembentukan Puncak Spin Echo",
                "Isokromat spin cepat yang berada di belakang menyusul isokromat spin lambat. Tepat pada waktu echo TE = 2 × tau, seluruh isokromat mengalami rephasing koheren sempurna membentuk puncak sinyal Spin Echo yang bebas dari dephasing inhomogenitas B0.",
                "Seluruh isokromat spin melintasi garis finish pada saat yang bersamaan, menghasilkan interferensi konstruktif sinyal listrik maksimum pada koil penerima."
            )
            else -> Triple("", "", "")
        }
        1 -> when (step) {
            1 -> Triple(
                "Langkah 1: Relaksasi Longitudinal Spin-Lattice (Pemulihan Mz T1)",
                "Komponen magnetisasi longitudinal Mz pulih kembali secara eksponensial menuju kesetimbangan M0: Mz(t) = M0 · (1 − e^(−t/T1)) melalui disipasi energi termal termodinamika spin ke kisi molekuler (lattice).",
                "Pengisian kembali cadangan energi magnetisasi longitudinal yang dipengaruhi oleh kesesuaian tumbling rate molekuler terhadap frekuensi Larmor."
            )
            2 -> Triple(
                "Langkah 2: Relaksasi Transversal Spin-Spin (Peluruhan Mxy T2)",
                "Komponen magnetisasi transversal Mxy meluruh secara ireversibel: Mxy(t) = Mxy0 · e^(−t/T2) akibat dephasing intermolekuler murni antar-proton tanpa pertukaran energi termal ke lingkungan kisi.",
                "Dispersi acak koherensi fase transversal akibat interaksi medan mikro intramolekuler yang tidak dapat direfokuskan kembali."
            )
            3 -> Triple(
                "Langkah 3: Diferensiasi Kontras Jaringan Intrinsik (Adiposa vs CSF)",
                "Adiposa (lipid) memiliki relaksasi T1 sangat singkat (~260 ms) dan T2 sedang, sedangkan Cairan Serebrospinal (CSF) memiliki relaksasi T1 sangat panjang (~2400 ms) dan T2 panjang (~1600 ms).",
                "Rasio kontras intrinsik antara fraksi molekul lipid yang bertukar energi cepat dengan molekul air bebas bervolume besar."
            )
            4 -> Triple(
                "Langkah 4: Manipulasi Ekstrinsik Waktu Repetisi (TR) & Waktu Echo (TE)",
                "Kombinasi parameter operator konsol: TR pendek & TE pendek mengisolasi kontras T1-weighted (T1w); TR panjang & TE panjang memaksimalkan kontras T2-weighted (T2w); TR panjang & TE pendek menghasilkan Proton Density-weighted (PDw).",
                "Pengaturan jendela waktu akuisisi ekstrinsik di konsol untuk mengeksploitasi perbedaan relaksasi intrinsik jaringan patologis."
            )
            else -> Triple("", "", "")
        }
        else -> when (step) {
            1 -> Triple(
                "Langkah 1: Pengodean Fase Ruang-k Baris demi Baris",
                "Pada setiap repetisi TR, gradien pengodean fase (G_pe) diaplikasikan dengan amplitudo bertingkat teratur untuk mendeposisikan profil fase spasial dan mengisi satu baris matriks ruang-k (k_y).",
                "Pencatatan data frekuensi spasial baris per baris secara terkalibrasi ke dalam domain matriks Fourier."
            )
            2 -> Triple(
                "Langkah 2: Distribusi Frekuensi Spasial Domain Ruang-k",
                "Zona pusat ruang-k (frekuensi spasial rendah / k_y mendekati 0) dikodekan dengan amplitudo gradien landai untuk menentukan bobot kontras citra dan rasio SNR utama; zona perifer (frekuensi spasial tinggi) menentukan resolusi spasial dan ketajaman tepi anatomi.",
                "Pemisahan fungsional di mana titik pusat mengendalikan persepsi kontras jaringan dan batas tepi mengendalikan detail spasial beresolusi tinggi."
            )
            3 -> Triple(
                "Langkah 3: Simetri Konjugat Hermitian Ruang-k (Partial Fourier)",
                "Matriks ruang-k memiliki simetri konjugat kompleks Hermitian di mana data kuadran berlawanan merupakan proyeksi simetris, memungkinkan rekonstruksi Fractional Phase/Frequency Encoding untuk mereduksi waktu akuisisi (TA).",
                "Pemanfaatan redundansi matematis simetri Fourier untuk mempercepat akuisisi tanpa mengorbankan integritas informasi diagnostik."
            )
            4 -> Triple(
                "Langkah 4: Rekonstruksi 2D Fast Fourier Transform (FFT)",
                "Setelah seluruh data matriks ruang-k terakuisisi lengkap, prosesor rekonstruksi menjalankan algoritma 2D Fast Fourier Transform (2D-FFT) untuk mendekonvolusi data domain frekuensi spasial menjadi citra anatomi visual berformat matriks piksel.",
                "Transformasi matematis komputasi yang mendekonstruksi spektrum frekuensi dan fase gelombang RF menjadi visualisasi penampang anatomi diagnostik."
            )
            else -> Triple("", "", "")
        }
    }
}
