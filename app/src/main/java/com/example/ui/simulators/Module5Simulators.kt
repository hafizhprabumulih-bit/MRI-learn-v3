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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableStateListOf
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

private fun fmtKoma(value: Double, decimals: Int = 0): String {
    return String.format(Locale.US, "%.${decimals}f", value).replace('.', ',')
}

// ============================================================================
// SUITE GABUNGAN MODUL 5 UNTUK TAB SIMULASI
// ============================================================================
@Composable
fun Module5HardwareLabSuite() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "A1: Penampang 5 Lapisan",
        "A2: Kalkulator ppm → Hz",
        "A3: Superkonduktif & Ramping",
        "A4: Perisai 5 Gauss",
        "A5: B0 vs SNR & SAR",
        "A6: Pilih Magnet",
        "A7: Urutan Quench"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module5_hardware_lab_suite"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("m5_suite_tab_$index"),
                    label = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                        selected = isSelected,
                        borderColor = if (isSelected) CyanBright else CardSlateBorder
                    )
                )
            }
        }

        when (selectedTab) {
            0 -> A1PenampangPesawatSimulator()
            1 -> A2PpmKeHzCalculator()
            2 -> A3SuperkonduktifSimulator()
            3 -> A4Perisai5GSimulator()
            4 -> A5B0SnrSarSimulator()
            5 -> A6PilihMagnetActivity()
            6 -> A7UrutanQuenchActivity()
        }
    }
}

// ============================================================================
// AKTIVITAS A1: PENJELAJAHAN DIAGRAM PENAMPANG PESAWAT MRI (5 LAPISAN)
// ============================================================================
private data class ScannerLayerInfo(
    val index: Int,
    val name: String,
    val shortLabel: String,
    val task: String,
    val physics: String,
    val color: Color,
    val borderColor: Color
)

private val SCANNER_LAYERS = listOf(
    ScannerLayerInfo(
        index = 0,
        name = "Ruang berpelindung RF",
        shortLabel = "1. Ruang berpelindung RF",
        task = "Menahan gelombang radio dari luar dan menjaga sinyal pasien tidak keluar.",
        physics = "Sangkar Faraday yang menyatu dengan dinding ruang (Zona IV).",
        color = Color(0xFF8B5CF6),
        borderColor = Color(0xFFC2ADFF)
    ),
    ScannerLayerInfo(
        index = 1,
        name = "Magnet utama",
        shortLabel = "2. Magnet utama",
        task = "Menghasilkan B0 yang kuat, stabil, dan homogen.",
        physics = "Kriostat berisi kumparan superkonduktor dalam helium cair 4,2 K.",
        color = Color(0xFF2F6BFF),
        borderColor = Color(0xFF8FB0FF)
    ),
    ScannerLayerInfo(
        index = 2,
        name = "Gradient coil",
        shortLabel = "3. Gradient coil",
        task = "Menambah medan yang berubah linear di sumbu X, Y, Z untuk mengode posisi.",
        physics = "Tiga set kumparan; penyebab bunyi bising saat dipindai.",
        color = Color(0xFF12B8A6),
        borderColor = Color(0xFF6FF0DD)
    ),
    ScannerLayerInfo(
        index = 3,
        name = "RF coil",
        shortLabel = "4. RF coil",
        task = "Memancarkan pulsa RF dan menerima sinyal dari proton.",
        physics = "Koil volume (body, kepala) atau koil permukaan; frekuensi sama dengan frekuensi Larmor.",
        color = Color(0xFFF5B82E),
        borderColor = Color(0xFFFFD978)
    ),
    ScannerLayerInfo(
        index = 4,
        name = "Bore dan meja pasien",
        shortLabel = "5. Bore dan meja pasien",
        task = "Ruang periksa 60 sampai 70 cm; meja menggeser pasien ke isosenter.",
        physics = "Isosenter: titik medan paling homogen, tempat area yang diperiksa diletakkan.",
        color = Color(0xFF9AA6B6),
        borderColor = Color(0xFFDDE4EE)
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun A1PenampangPesawatSimulator(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf("jelajah") } // "jelajah" or "uji"
    var viewType by remember { mutableStateOf("long") } // "long" (membujur) or "axial" (aksial)
    var activeLayerIndex by remember { mutableIntStateOf(0) }

    // Mode Uji states
    var shuffleSeed by remember { mutableIntStateOf(1) }
    val shuffledOrder = remember(shuffleSeed) {
        val list = listOf(0, 1, 2, 3, 4).shuffled(kotlin.random.Random(shuffleSeed))
        list
    }
    var selectedQuizLabel by remember { mutableIntStateOf(-1) }
    val matchedLayers = remember { mutableStateListOf<Int>() }
    var statusMessage by remember {
        mutableStateOf("Tur berurutan dari luar ke dalam. Ketuk salah satu lapisan pada diagram atau tombol di bawah.")
    }

    fun handleLayerClick(layerIdx: Int) {
        if (mode == "jelajah") {
            activeLayerIndex = layerIdx
        } else {
            if (selectedQuizLabel < 0) {
                statusMessage = "Pilih satu label dulu di bawah, lalu ketuk lapisan pada diagram."
                return
            }
            if (matchedLayers.contains(layerIdx)) {
                statusMessage = "Lapisan itu sudah terisi."
                return
            }
            if (selectedQuizLabel == layerIdx) {
                matchedLayers.add(layerIdx)
                activeLayerIndex = layerIdx
                selectedQuizLabel = -1
                statusMessage = if (matchedLayers.size == 5) {
                    "Selesai! Semua 5 lapisan benar."
                } else {
                    "Benar! Lanjutkan dengan label berikutnya."
                }
            } else {
                statusMessage = "Belum tepat. Pikirkan lagi tugas label itu, lalu coba lapisan lain."
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "a1_b0_flow")
    val b0DashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "b0_phase"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a1_penampang_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Penjelajahan Diagram Penampang",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T1.1, T1.2",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mode Jelajah: ketuk satu lapisan pada diagram atau tombol di bawahnya. Mode Uji: ketuk satu label, lalu ketuk lapisan yang cocok.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Toggle: Jelajah vs Uji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == "jelajah",
                    onClick = {
                        mode = "jelajah"
                        activeLayerIndex = 0
                        statusMessage = "Tur berurutan dari luar ke dalam."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a1_mode_jelajah"),
                    label = { Text("Mode Jelajah", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = mode == "uji",
                    onClick = {
                        mode = "uji"
                        matchedLayers.clear()
                        selectedQuizLabel = -1
                        activeLayerIndex = -1
                        shuffleSeed++
                        statusMessage = "Mode Uji: ketuk satu label, lalu ketuk lapisan yang cocok."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a1_mode_uji"),
                    label = { Text("Mode Uji (Skor ${matchedLayers.size}/5)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberAccent,
                        selectedLabelColor = DeepObsidian,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
            }

            // View Toggle: Potongan Membujur vs Potongan Aksial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewType == "long",
                    onClick = { viewType = "long" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a1_view_long"),
                    label = { Text("Potongan membujur", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanDark,
                        selectedLabelColor = CyanBright,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = viewType == "axial",
                    onClick = { viewType = "axial" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a1_view_axial"),
                    label = { Text("Potongan aksial", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanDark,
                        selectedLabelColor = CyanBright,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Canvas Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111E33))
                    .border(1.dp, Color(0xFF243A5C), RoundedCornerShape(12.dp))
                    .testTag("a1_diagram_canvas")
                    .pointerInput(viewType, mode, selectedQuizLabel) {
                        detectTapGestures { offset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            if (viewType == "axial") {
                                val cx = w * 0.5f
                                val cy = h * 0.5f
                                val maxR = minOf(w, h) * 0.45f
                                val dist = hypot(offset.x - cx, offset.y - cy)
                                val tappedLayer = when {
                                    dist <= maxR * 0.44f -> 4 // Bore & meja
                                    dist <= maxR * 0.58f -> 3 // RF coil
                                    dist <= maxR * 0.74f -> 2 // Gradient coil
                                    dist <= maxR * 0.92f -> 1 // Magnet utama
                                    else -> 0 // Ruang berpelindung RF
                                }
                                handleLayerClick(tappedLayer)
                            } else {
                                val cy = h * 0.5f
                                val dy = abs(offset.y - cy) / (h * 0.5f)
                                val inHorizontalBore = offset.x in (w * 0.14f)..(w * 0.86f)
                                val tappedLayer = when {
                                    !inHorizontalBore || dy > 0.85f -> 0 // Ruang berpelindung RF
                                    dy > 0.62f -> 1 // Magnet utama
                                    dy > 0.45f -> 2 // Gradient coil
                                    dy > 0.32f -> 3 // RF coil
                                    else -> 4 // Bore & meja pasien
                                }
                                handleLayerClick(tappedLayer)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Subtle grid background
                    val gridStep = 24.dp.toPx()
                    var gx = 0f
                    while (gx < w) {
                        drawLine(Color(0x1478A0DC), Offset(gx, 0f), Offset(gx, h), 1f)
                        gx += gridStep
                    }
                    var gy = 0f
                    while (gy < h) {
                        drawLine(Color(0x1478A0DC), Offset(0f, gy), Offset(w, gy), 1f)
                        gy += gridStep
                    }

                    fun isHighlighted(idx: Int): Boolean {
                        return if (mode == "jelajah") activeLayerIndex == idx else matchedLayers.contains(idx)
                    }

                    if (viewType == "long") {
                        // POTONGAN MEMBUJUR (Sejajar sumbu panjang bore)
                        val padX = w * 0.04f
                        val padY = h * 0.06f
                        val shieldW = w - padX * 2f
                        val shieldH = h - padY * 2f

                        // 1. Ruang berpelindung RF (bingkai luar)
                        val s0 = isHighlighted(0)
                        drawRoundRect(
                            color = SCANNER_LAYERS[0].color.copy(alpha = if (s0) 0.35f else 0.14f),
                            topLeft = Offset(padX, padY),
                            size = Size(shieldW, shieldH),
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (s0) SCANNER_LAYERS[0].borderColor else SCANNER_LAYERS[0].color,
                            topLeft = Offset(padX, padY),
                            size = Size(shieldW, shieldH),
                            cornerRadius = CornerRadius(10.dp.toPx()),
                            style = Stroke(width = if (s0) 3.5.dp.toPx() else 2.dp.toPx())
                        )

                        val magLeft = w * 0.16f
                        val magWidth = w * 0.68f
                        // 2. Magnet utama (pita atas & bawah dengan kriostat 4,2 K)
                        val s1 = isHighlighted(1)
                        val topMagY = h * 0.14f
                        val magH = h * 0.17f
                        val botMagY = h - topMagY - magH
                        listOf(topMagY, botMagY).forEach { my ->
                            drawRoundRect(
                                color = SCANNER_LAYERS[1].color.copy(alpha = if (s1) 0.9f else 0.65f),
                                topLeft = Offset(magLeft, my),
                                size = Size(magWidth, magH),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                            drawRoundRect(
                                color = if (s1) Color.White else SCANNER_LAYERS[1].borderColor,
                                topLeft = Offset(magLeft, my),
                                size = Size(magWidth, magH),
                                cornerRadius = CornerRadius(6.dp.toPx()),
                                style = Stroke(width = if (s1) 3.dp.toPx() else 1.2.dp.toPx())
                            )
                            // Segmen kumparan solenoid di dalam kriostat
                            val segCount = 6
                            for (s in 0 until segCount) {
                                val sx = magLeft + 14.dp.toPx() + s * ((magWidth - 28.dp.toPx()) / (segCount - 1))
                                drawCircle(
                                    color = Color(0xFFD9E6FF),
                                    radius = 4.dp.toPx(),
                                    center = Offset(sx, my + magH * 0.5f)
                                )
                            }
                        }

                        // 3. Gradient coil (sumbu X, Y, Z)
                        val s2 = isHighlighted(2)
                        val gradLeft = w * 0.18f
                        val gradWidth = w * 0.64f
                        val gradH = h * 0.08f
                        val topGradY = topMagY + magH + 2.dp.toPx()
                        val botGradY = botMagY - gradH - 2.dp.toPx()
                        listOf(topGradY, botGradY).forEach { gyPos ->
                            drawRoundRect(
                                color = SCANNER_LAYERS[2].color.copy(alpha = if (s2) 0.95f else 0.7f),
                                topLeft = Offset(gradLeft, gyPos),
                                size = Size(gradWidth, gradH),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                            drawRoundRect(
                                color = if (s2) Color.White else SCANNER_LAYERS[2].borderColor,
                                topLeft = Offset(gradLeft, gyPos),
                                size = Size(gradWidth, gradH),
                                cornerRadius = CornerRadius(4.dp.toPx()),
                                style = Stroke(width = if (s2) 2.8.dp.toPx() else 1.dp.toPx())
                            )
                        }

                        // 4. RF coil (pita emas tipis di permukaan dalam + koil kepala)
                        val s3 = isHighlighted(3)
                        val rfLeft = w * 0.20f
                        val rfWidth = w * 0.60f
                        val rfH = h * 0.045f
                        val topRfY = topGradY + gradH + 2.dp.toPx()
                        val botRfY = botGradY - rfH - 2.dp.toPx()
                        listOf(topRfY, botRfY).forEach { ry ->
                            drawRoundRect(
                                color = SCANNER_LAYERS[3].color.copy(alpha = if (s3) 0.98f else 0.78f),
                                topLeft = Offset(rfLeft, ry),
                                size = Size(rfWidth, rfH),
                                cornerRadius = CornerRadius(3.dp.toPx())
                            )
                            drawRoundRect(
                                color = if (s3) Color.White else SCANNER_LAYERS[3].borderColor,
                                topLeft = Offset(rfLeft, ry),
                                size = Size(rfWidth, rfH),
                                cornerRadius = CornerRadius(3.dp.toPx()),
                                style = Stroke(width = if (s3) 2.5.dp.toPx() else 1.dp.toPx())
                            )
                        }

                        // 5. Bore & meja pasien + isosenter + garis B0
                        val s4 = isHighlighted(4)
                        val boreTop = topRfY + rfH + 2.dp.toPx()
                        val boreBot = botRfY - 2.dp.toPx()
                        val boreH = (boreBot - boreTop).coerceAtLeast(10f)
                        drawRoundRect(
                            color = if (s4) Color(0xFF1E3A5F) else Color(0xFF0A1426),
                            topLeft = Offset(magLeft, boreTop),
                            size = Size(magWidth, boreH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        if (s4) {
                            drawRoundRect(
                                color = SCANNER_LAYERS[4].borderColor,
                                topLeft = Offset(magLeft, boreTop),
                                size = Size(magWidth, boreH),
                                cornerRadius = CornerRadius(4.dp.toPx()),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        // Garis fluks B0 sejajar bore
                        val b0Alpha = if (s1) 0.95f else 0.35f
                        for (row in 1..3) {
                            val by = boreTop + boreH * (row / 4f)
                            drawLine(
                                color = Color(0xFF6F9BFF).copy(alpha = b0Alpha),
                                start = Offset(magLeft + 8.dp.toPx(), by),
                                end = Offset(magLeft + magWidth - 8.dp.toPx(), by),
                                strokeWidth = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), -b0DashPhase)
                            )
                        }

                        // Meja pasien & siluet pasien terlentang
                        val tableY = boreTop + boreH * 0.72f
                        drawRoundRect(
                            color = SCANNER_LAYERS[4].color,
                            topLeft = Offset(w * 0.08f, tableY),
                            size = Size(w * 0.56f, 6.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx())
                        )
                        // Siluet tubuh & kepala pasien di dekat isosenter
                        drawRoundRect(
                            color = Color(0xFFCBD5E1),
                            topLeft = Offset(w * 0.16f, tableY - 14.dp.toPx()),
                            size = Size(w * 0.28f, 13.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFCBD5E1),
                            radius = 8.dp.toPx(),
                            center = Offset(w * 0.47f, tableY - 8.dp.toPx())
                        )
                        // Penanda Isosenter (+)
                        val isoX = w * 0.5f
                        val isoY = boreTop + boreH * 0.5f
                        drawCircle(
                            color = CyanBright,
                            radius = 3.5.dp.toPx(),
                            center = Offset(isoX, isoY),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    } else {
                        // POTONGAN AKSIAL (Tegak lurus bore -> 5 Cincin Konsentris)
                        val cx = w * 0.5f
                        val cy = h * 0.5f
                        val maxR = minOf(w, h) * 0.44f

                        // 1. Ruang berpelindung RF (kotak terluar)
                        val s0 = isHighlighted(0)
                        val padX = w * 0.06f
                        val padY = h * 0.06f
                        drawRoundRect(
                            color = SCANNER_LAYERS[0].color.copy(alpha = if (s0) 0.35f else 0.12f),
                            topLeft = Offset(padX, padY),
                            size = Size(w - padX * 2f, h - padY * 2f),
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (s0) SCANNER_LAYERS[0].borderColor else SCANNER_LAYERS[0].color,
                            topLeft = Offset(padX, padY),
                            size = Size(w - padX * 2f, h - padY * 2f),
                            cornerRadius = CornerRadius(10.dp.toPx()),
                            style = Stroke(width = if (s0) 3.5.dp.toPx() else 2.dp.toPx())
                        )

                        // 2. Magnet utama (cincin terluar)
                        val s1 = isHighlighted(1)
                        val rMagOuter = maxR * 0.92f
                        val rMagInner = maxR * 0.74f
                        drawCircle(
                            color = SCANNER_LAYERS[1].color.copy(alpha = if (s1) 0.9f else 0.65f),
                            radius = (rMagOuter + rMagInner) / 2f,
                            center = Offset(cx, cy),
                            style = Stroke(width = rMagOuter - rMagInner)
                        )
                        if (s1) {
                            drawCircle(
                                color = Color.White,
                                radius = rMagOuter,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        // 3. Gradient coil (cincin kedua)
                        val s2 = isHighlighted(2)
                        val rGradOuter = maxR * 0.73f
                        val rGradInner = maxR * 0.58f
                        drawCircle(
                            color = SCANNER_LAYERS[2].color.copy(alpha = if (s2) 0.92f else 0.68f),
                            radius = (rGradOuter + rGradInner) / 2f,
                            center = Offset(cx, cy),
                            style = Stroke(width = rGradOuter - rGradInner)
                        )
                        if (s2) {
                            drawCircle(
                                color = Color.White,
                                radius = rGradOuter,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        // 4. RF coil (cincin ketiga dengan 16 batang birdcage)
                        val s3 = isHighlighted(3)
                        val rRfOuter = maxR * 0.57f
                        val rRfInner = maxR * 0.45f
                        val rRfMid = (rRfOuter + rRfInner) / 2f
                        drawCircle(
                            color = SCANNER_LAYERS[3].color.copy(alpha = if (s3) 0.95f else 0.75f),
                            radius = rRfMid,
                            center = Offset(cx, cy),
                            style = Stroke(width = rRfOuter - rRfInner)
                        )
                        for (rung in 0 until 16) {
                            val angle = rung * (Math.PI * 2.0 / 16.0)
                            val rx = cx + rRfMid * cos(angle).toFloat()
                            val ry = cy + rRfMid * sin(angle).toFloat()
                            drawCircle(
                                color = Color(0xFF6B4300),
                                radius = 2.2.dp.toPx(),
                                center = Offset(rx, ry)
                            )
                        }

                        // 5. Bore & meja pasien (lingkaran pusat)
                        val s4 = isHighlighted(4)
                        val rBore = maxR * 0.44f
                        drawCircle(
                            color = if (s4) Color(0xFF1E3A5F) else Color(0xFF0A1426),
                            radius = rBore,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = if (s4) Color.White else SCANNER_LAYERS[4].borderColor,
                            radius = rBore,
                            center = Offset(cx, cy),
                            style = Stroke(width = if (s4) 2.5.dp.toPx() else 1.2.dp.toPx())
                        )
                        // Meja & penampang aksial pasien
                        drawRoundRect(
                            color = SCANNER_LAYERS[4].color,
                            topLeft = Offset(cx - rBore * 0.65f, cy + rBore * 0.35f),
                            size = Size(rBore * 1.3f, 6.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx())
                        )
                        drawOval(
                            color = Color(0xFFCBD5E1),
                            topLeft = Offset(cx - rBore * 0.48f, cy - rBore * 0.25f),
                            size = Size(rBore * 0.96f, rBore * 0.58f)
                        )
                        // Penanda Isosenter (+)
                        drawCircle(
                            color = CyanBright,
                            radius = 3.5.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }

                // Legend Overlay di pojok atas
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = DeepObsidian.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = if (viewType == "long") "Potongan Membujur (B0 →)" else "Potongan Aksial (B0 ⊗)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tombol Lapisan 1 s.d. 5 (Selalu bisa diketuk di kedua mode)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SCANNER_LAYERS.forEach { layer ->
                    val isSelected = if (mode == "jelajah") {
                        activeLayerIndex == layer.index
                    } else {
                        matchedLayers.contains(layer.index)
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { handleLayerClick(layer.index) }
                            .testTag("a1_layer_chip_${layer.index}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) layer.color.copy(alpha = 0.28f) else CardSlate,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) layer.borderColor else CardSlateBorder
                        )
                    ) {
                        Text(
                            text = if (mode == "jelajah") layer.shortLabel else "Lapisan ${layer.index + 1}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (mode == "jelajah") {
                val activeLayer = SCANNER_LAYERS.getOrNull(activeLayerIndex)
                val progress = if (activeLayer != null) (activeLayer.index + 1) / 5f else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeLayer != null) {
                            "Lapisan ${activeLayer.index + 1} dari 5: ${activeLayer.name}"
                        } else {
                            "Belum ada lapisan dipilih"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = activeLayer?.color ?: CyanPrimary,
                    trackColor = CardSlate
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = CardSlate,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        activeLayer?.borderColor ?: CardSlateBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (activeLayer != null) {
                            Text(
                                text = activeLayer.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tugas: ${activeLayer.task}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Fisika: ${activeLayer.physics}",
                                fontSize = 11.sp,
                                color = CyanBright,
                                lineHeight = 16.sp
                            )
                        } else {
                            Text(
                                text = "Pilih satu lapisan untuk melihat tugas dan fisikanya.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            activeLayerIndex = if (activeLayerIndex <= 0) 0 else activeLayerIndex - 1
                        },
                        enabled = activeLayerIndex > 0,
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "← Sebelumnya",
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            activeLayerIndex = if (activeLayerIndex < 0) 0 else (activeLayerIndex + 1).coerceAtMost(4)
                        },
                        enabled = activeLayerIndex in 0..3,
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Berikutnya →",
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    OutlinedButton(
                        onClick = { activeLayerIndex = -1 },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            } else {
                // Mode Uji UI
                Text(
                    text = "Pilih satu label di bawah, lalu ketuk nomor Lapisan (1–5) atau area pada diagram:",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shuffledOrder.forEach { idx ->
                        val layer = SCANNER_LAYERS[idx]
                        val isMatched = matchedLayers.contains(idx)
                        val isSelectedLabel = selectedQuizLabel == idx
                        FilterChip(
                            selected = isSelectedLabel,
                            enabled = !isMatched,
                            onClick = {
                                selectedQuizLabel = idx
                                statusMessage = "Label '${layer.name}' dipilih. Ketuk lapisan yang cocok pada diagram."
                            },
                            label = {
                                Text(
                                    text = if (isMatched) "✓ ${layer.name}" else layer.name,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberAccent,
                                selectedLabelColor = DeepObsidian,
                                disabledContainerColor = EmeraldDark.copy(alpha = 0.5f),
                                disabledLabelColor = EmeraldSuccess
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Benar: ${matchedLayers.size} / 5",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (matchedLayers.size == 5) EmeraldSuccess else AmberAccent
                    )
                    OutlinedButton(
                        onClick = {
                            matchedLayers.clear()
                            selectedQuizLabel = -1
                            activeLayerIndex = -1
                            shuffleSeed++
                            statusMessage = "Mode Uji diulang: ketuk satu label, lalu ketuk lapisan yang cocok."
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Ulangi uji", fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                }
                Text(
                    text = statusMessage,
                    fontSize = 11.sp,
                    color = CyanBright
                )
            }
        }
    }
}

// ============================================================================
// AKTIVITAS A2: KALKULATOR KONVERSI PPM KE HZ (SHIMMING & PERGESERAN KIMIA)
// ============================================================================
private data class NucleusConstant(
    val key: String,
    val label: String,
    val gammaMhzPerT: Double
)

private val NUCLEUS_CONSTANTS = listOf(
    NucleusConstant("1H", "Hidrogen (¹H)", 42.58),
    NucleusConstant("19F", "Fluor (¹⁹F)", 40.08),
    NucleusConstant("31P", "Fosfor (³¹P)", 17.24),
    NucleusConstant("23Na", "Natrium (²³Na)", 11.26)
)

@Composable
fun A2PpmKeHzCalculator(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf("inhom") } // "inhom" or "kimia"
    var selectedNucleus by remember { mutableStateOf(NUCLEUS_CONSTANTS[0]) }
    var selectedB0 by remember { mutableStateOf(1.5) }
    var ppmValue by remember { mutableFloatStateOf(1.0f) }

    val f0Mhz = selectedNucleus.gammaMhzPerT * selectedB0
    val deltaHz = f0Mhz * ppmValue.toDouble()
    val fatWaterHz = f0Mhz * 3.5

    val statusText = if (selectedNucleus.key == "1H") {
        if (mode == "inhom") {
            when {
                ppmValue <= 1.0f -> "Sangat homogen"
                ppmValue <= 3.5f -> "Sedang"
                else -> "Ketidakhomogenan besar"
            }
        } else {
            when {
                abs(ppmValue - 3.5f) < 0.08f -> "Setara pergeseran lemak-air"
                ppmValue < 3.5f -> "Lebih kecil dari lemak-air"
                else -> "Lebih besar dari lemak-air"
            }
        }
    } else {
        "Frekuensi Larmor inti ini lebih rendah"
    }

    val explanationText = if (selectedNucleus.key == "1H") {
        val ratioPct = (deltaHz / fatWaterHz) * 100.0
        "Jarak puncak lemak-air pada medan ini sekitar ${fmtKoma(fatWaterHz, 0)} Hz. Nilai ini sama dengan ${fmtKoma(ratioPct, 0)}% dari jarak itu."
    } else {
        "Prinsip sama: Hz = frekuensi Larmor (MHz) × ppm. Nilai gamma dibulatkan."
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a2_ppm_hz_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A2",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Kalkulator Konversi ppm ke Hz",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T2.4 • Rumus: Δf (Hz) = f₀ (MHz) × ppm",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Toggle: Ketidakhomogenan B0 vs Pergeseran kimia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == "inhom",
                    onClick = {
                        mode = "inhom"
                        ppmValue = 1.0f
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a2_mode_inhom"),
                    label = { Text("Ketidakhomogenan B0", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = mode == "kimia",
                    onClick = {
                        mode = "kimia"
                        ppmValue = 3.5f
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a2_mode_kimia"),
                    label = { Text("Pergeseran kimia", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Pilih Inti:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NUCLEUS_CONSTANTS.forEach { nuc ->
                    val isSel = selectedNucleus.key == nuc.key
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedNucleus = nuc },
                        label = { Text(nuc.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright,
                            containerColor = CardSlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Pilih Kuat Medan B0:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.5, 1.5, 3.0, 7.0).forEach { b0 ->
                    val isSel = selectedB0 == b0
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedB0 = b0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("a2_b0_${b0}"),
                        label = { Text("${fmtKoma(b0, if (b0 % 1.0 == 0.0) 0 else 1)} T", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright,
                            containerColor = CardSlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (mode == "inhom") "Ketidakhomogenan:" else "Pergeseran kimia:",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${fmtKoma(ppmValue.toDouble(), 1)} ppm",
                    fontSize = 12.sp,
                    color = CyanBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = ppmValue,
                onValueChange = { ppmValue = it },
                valueRange = 0f..10f,
                modifier = Modifier.testTag("a2_ppm_slider"),
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = CardSlate,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${fmtKoma(ppmValue.toDouble(), 1)} ppm = ${fmtKoma(deltaHz, 0)} Hz (Larmor ${fmtKoma(f0Mhz, 2)} MHz)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("a2_hz_output")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: $statusText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = explanationText,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        selectedNucleus = NUCLEUS_CONSTANTS[0]
                        selectedB0 = 1.5
                        ppmValue = if (mode == "inhom") 1.0f else 3.5f
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("Reset", fontSize = 11.sp, maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = { ppmValue = 3.5f },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a2_fat_water_button"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent)
                ) {
                    Text(
                        text = "Preset Lemak-Air (3,5 ppm)",
                        fontSize = 11.sp,
                        color = AmberAccent,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ============================================================================
// AKTIVITAS A3: MAGNET SUPERKONDUKTIF (MODE 1: SUHU & HAMBATAN | MODE 2: RAMPING)
// ============================================================================
@Composable
fun A3SuperkonduktifSimulator(modifier: Modifier = Modifier) {
    var activeTab by remember { mutableStateOf("m1") } // "m1" or "m2"
    var tempKelvin by remember { mutableFloatStateOf(4.2f) }

    // Mode 2 states
    var rampStage by remember { mutableStateOf("up") } // "up", "per", "down"
    var targetCurrentPct by remember { mutableFloatStateOf(100f) }
    var currentPct by remember { mutableFloatStateOf(60f) }
    val currentHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(activeTab, rampStage, targetCurrentPct) {
        if (activeTab == "m2") {
            while (true) {
                delay(140L)
                currentPct = when (rampStage) {
                    "up" -> {
                        if (currentPct < targetCurrentPct) {
                            (currentPct + 2.0f).coerceAtMost(targetCurrentPct)
                        } else {
                            (currentPct - 2.0f).coerceAtLeast(targetCurrentPct)
                        }
                    }
                    "down" -> (currentPct - 2.0f).coerceAtLeast(0f)
                    else -> currentPct // Persisten: arus konstan!
                }
                currentHistory.add(currentPct)
                if (currentHistory.size > 80) {
                    currentHistory.removeAt(0)
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "a3_circuit_flow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "a3_flow_phase"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a3_superkonduktif_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A3",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Magnet Superkonduktif (4,2 K & Saklar Persisten)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T2.1, T2.2, T2.3",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTab == "m1",
                    onClick = { activeTab = "m1" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a3_tab_m1"),
                    label = { Text("Mode 1: Suhu & Hambatan", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = activeTab == "m2",
                    onClick = { activeTab = "m2" },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a3_tab_m2"),
                    label = { Text("Mode 2: Ramping & Saklar", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = DeepObsidian,
                        containerColor = CardSlate,
                        labelColor = TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeTab == "m1") {
                val tcKelvin = 9.0f
                val isSuperconducting = tempKelvin <= tcKelvin

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Suhu Kawat NbTi:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${fmtKoma(tempKelvin.toDouble(), 1)} K",
                        fontSize = 12.sp,
                        color = if (isSuperconducting) EmeraldSuccess else AmberWarning,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = tempKelvin,
                    onValueChange = { tempKelvin = it },
                    valueRange = 0f..300f,
                    modifier = Modifier.testTag("a3_temp_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isSuperconducting) EmeraldSuccess else AmberWarning,
                        activeTrackColor = if (isSuperconducting) EmeraldSuccess else AmberWarning
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        293f to "Suhu ruang (293 K)",
                        77f to "Nitrogen cair (77 K)",
                        4.2f to "Helium cair (4,2 K)"
                    ).forEach { (kVal, label) ->
                        OutlinedButton(
                            onClick = { tempKelvin = kVal },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(label, fontSize = 10.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas Grafik Hambatan vs Suhu (0..300 K)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSlate)
                        .border(1.dp, CardSlateBorder, RoundedCornerShape(10.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val padL = 32.dp.toPx()
                        val padR = 16.dp.toPx()
                        val padT = 18.dp.toPx()
                        val padB = 22.dp.toPx()
                        val gw = size.width - padL - padR
                        val gh = size.height - padT - padB

                        // Axes
                        drawLine(TextMuted, Offset(padL, padT + gh), Offset(padL + gw, padT + gh), 1.5.dp.toPx())
                        drawLine(TextMuted, Offset(padL, padT), Offset(padL, padT + gh), 1.5.dp.toPx())

                        fun xOf(t: Float) = padL + (t / 300f) * gw
                        fun rNormal(t: Float) = 0.04f + 0.96f * (t / 300f)
                        fun rNbTi(t: Float) = if (t <= tcKelvin) 0f else rNormal(t)
                        fun yOf(r: Float) = padT + gh * (1f - r)

                        // Normal metal curve (gray)
                        val normalPath = Path()
                        for (step in 0..100) {
                            val t = step * 3f
                            val px = xOf(t)
                            val py = yOf(rNormal(t))
                            if (step == 0) normalPath.moveTo(px, py) else normalPath.lineTo(px, py)
                        }
                        drawPath(normalPath, TextMuted, style = Stroke(width = 2.dp.toPx()))

                        // NbTi superconductor curve (cyan)
                        val nbtiPath = Path()
                        for (step in 0..100) {
                            val t = step * 3f
                            val px = xOf(t)
                            val py = yOf(rNbTi(t))
                            if (step == 0) nbtiPath.moveTo(px, py) else nbtiPath.lineTo(px, py)
                        }
                        drawPath(nbtiPath, CyanBright, style = Stroke(width = 3.dp.toPx()))

                        // Current Temperature dashed marker
                        val curX = xOf(tempKelvin.coerceIn(0f, 300f))
                        drawLine(
                            color = if (isSuperconducting) EmeraldSuccess else RoseDanger,
                            start = Offset(curX, padT),
                            end = Offset(curX, padT + gh),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                        drawCircle(
                            color = if (isSuperconducting) EmeraldSuccess else RoseDanger,
                            radius = 5.dp.toPx(),
                            center = Offset(curX, yOf(rNbTi(tempKelvin)))
                        )
                    }

                    Text(
                        text = "Abu-abu: Logam biasa | Biru: NbTi (Tc ≈ 9 K)",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSuperconducting) EmeraldDark.copy(alpha = 0.5f) else AmberDark.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSuperconducting) EmeraldSuccess else AmberWarning
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isSuperconducting) {
                                "Hambatan kawat NbTi = 0 (superkonduktif)"
                            } else {
                                "Kawat NbTi punya hambatan (belum superkonduktif)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuperconducting) EmeraldSuccess else AmberWarning
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = when {
                                isSuperconducting -> "Di bawah suhu transisi (9 K), arus mengalir tanpa kehilangan energi. Helium cair (4,2 K) berada di bawah suhu transisi."
                                tempKelvin <= 100f -> "Dingin, tetapi masih di atas suhu transisi: hambatan masih ada. Nitrogen cair (77 K) belum cukup untuk NbTi."
                                else -> "Suhu tinggi: hambatan besar, arus besar akan menjadi panas."
                            },
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                // Mode 2: Ramping & Saklar Persisten
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "up" to "Ramping up",
                        "per" to "Persisten",
                        "down" to "Ramping down"
                    ).forEach { (key, label) ->
                        val isSel = rampStage == key
                        FilterChip(
                            selected = isSel,
                            onClick = { rampStage = key },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("a3_stage_$key"),
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DeepObsidian,
                                containerColor = CardSlate,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Arus target ramping up:", fontSize = 11.sp, color = TextSecondary)
                    Text("${targetCurrentPct.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright)
                }
                Slider(
                    value = targetCurrentPct,
                    onValueChange = { targetCurrentPct = it },
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
                )

                // Canvas Rangkaian Paralel (Catu daya, Resistor dump, Saklar persisten, Solenoid utama)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSlate)
                        .border(1.dp, CardSlateBorder, RoundedCornerShape(10.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val leftX = w * 0.14f
                        val rightX = w * 0.86f
                        val yPs = h * 0.16f
                        val yDump = h * 0.40f
                        val ySw = h * 0.64f
                        val ySol = h * 0.86f

                        // Static wires
                        drawLine(TextMuted, Offset(leftX, yPs), Offset(leftX, ySol), 2.dp.toPx())
                        drawLine(TextMuted, Offset(rightX, yPs), Offset(rightX, ySol), 2.dp.toPx())
                        listOf(yPs, yDump, ySw, ySol).forEach { y ->
                            drawLine(TextMuted, Offset(leftX, y), Offset(rightX, y), 2.dp.toPx())
                        }

                        // Active animated current loop
                        val loopTopY = when (rampStage) {
                            "up" -> yPs
                            "per" -> ySw
                            else -> yDump
                        }
                        val loopRectPath = Path().apply {
                            moveTo(leftX, ySol)
                            lineTo(leftX, loopTopY)
                            lineTo(rightX, loopTopY)
                            lineTo(rightX, ySol)
                            close()
                        }
                        drawPath(
                            path = loopRectPath,
                            color = CyanBright,
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), -flowPhase)
                            )
                        )

                        // Component boxes
                        val boxW = w * 0.38f
                        val boxH = 18.dp.toPx()
                        val cx = w * 0.5f

                        // 1. Catu daya
                        drawRoundRect(
                            color = if (rampStage != "per") CyanPrimary else CardSlateBorder,
                            topLeft = Offset(cx - boxW / 2, yPs - boxH / 2),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        // 2. Resistor dump
                        drawRoundRect(
                            color = if (rampStage == "down") AmberWarning else DeepObsidian,
                            topLeft = Offset(cx - boxW / 2, yDump - boxH / 2),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        // 3. Saklar persisten
                        drawRoundRect(
                            color = if (rampStage == "per") EmeraldSuccess else AmberWarning,
                            topLeft = Offset(cx - boxW / 2, ySw - boxH / 2),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        // 4. Solenoid utama (4 K)
                        drawRoundRect(
                            color = Color(0xFF2F6BFF),
                            topLeft = Offset(cx - boxW / 2, ySol - boxH / 2),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Catu daya: ${if (rampStage != "per") "ON" else "OFF (Dilepas)"}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepObsidian
                        )
                        Text(
                            text = "Resistor Dump",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rampStage == "down") DeepObsidian else TextSecondary
                        )
                        Text(
                            text = if (rampStage == "per") "Saklar: Superkonduktor (Pemanas OFF)" else "Saklar: Resistif (Pemanas ON)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepObsidian
                        )
                        Text(
                            text = "Solenoid Utama (4,2 K)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val b0T = (currentPct / 100.0) * 1.5
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = CardSlate,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Arus: ${currentPct.toInt()}% | Medan B0 sekitar ${fmtKoma(b0T, 2)} T (contoh pesawat 1,5 T)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (rampStage) {
                                "up" -> "Ramping up: pemanas ON membuat saklar resistif, sehingga arus dari catu daya mengalir ke solenoid dan dinaikkan bertahap sampai target."
                                "per" -> "Persisten: pemanas OFF, saklar menjadi superkonduktor dan membentuk lingkaran tertutup dengan solenoid. Catu daya dilepas; arus bertahan."
                                else -> "Ramping down: catu daya disambungkan lagi, pemanas ON, arus diturunkan bertahap. Energi dibuang sebagai panas pada resistor dump."
                            },
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        currentPct = 0f
                        currentHistory.clear()
                        rampStage = "up"
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset arus ke nol", fontSize = 11.sp)
                }
            }
        }
    }
}

// ============================================================================
// AKTIVITAS A4: PERISAI MEDAN TEPI (5 GAUSS)
// ============================================================================
@Composable
fun A4Perisai5GSimulator(modifier: Modifier = Modifier) {
    var shieldType by remember { mutableStateOf("none") } // "none", "pas", "akt"
    var fieldB0 by remember { mutableStateOf(1.5) }

    val scaleFactor = (fieldB0 / 1.5).pow(1.0 / 3.0).toFloat()
    val (baseRx, baseRy, descText) = when (shieldType) {
        "pas" -> Triple(
            150f,
            54f,
            "Perisai pasif: pelat baja membelokkan garis fluks sehingga garis 5 G menyempit. Berat (>20 ton) dan mahal."
        )
        "akt" -> Triple(
            120f,
            44f,
            "Perisai aktif: bucking coil berarus berlawanan menyempitkan garis 5 G tanpa baja. Gradien medan di tepinya curam, sehingga waspada proyektil."
        )
        else -> Triple(
            300f,
            100f,
            "Tanpa perisai, garis 5 G meluas jauh ke segala arah dan dapat mengenai area publik."
        )
    }

    val outerRx = baseRx * scaleFactor
    val outerRy = baseRy * scaleFactor
    val isPublicSafe = (620f - 350f) > outerRx

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a4_perisai_5g_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A4",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Perisai Medan Tepi (5 Gauss / 0,5 mT)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T2.5",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shield Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "none" to "Tanpa perisai",
                    "pas" to "Perisai pasif (baja)",
                    "akt" to "Perisai aktif (bucking coil)"
                ).forEach { (key, label) ->
                    val isSel = shieldType == key
                    FilterChip(
                        selected = isSel,
                        onClick = { shieldType = key },
                        modifier = Modifier.testTag("a4_shield_$key"),
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = DeepObsidian,
                            containerColor = CardSlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // B0 Selector (1,5 T vs 3 T)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.5 to "Medan 1,5 T", 3.0 to "Medan 3 T").forEach { (b0, label) ->
                    val isSel = fieldB0 == b0
                    FilterChip(
                        selected = isSel,
                        onClick = { fieldB0 = b0 },
                        modifier = Modifier.weight(1f),
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanDark,
                            selectedLabelColor = CyanBright,
                            containerColor = CardSlate,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Jejak Medan Tepi 5 G
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardSlate)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(10.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sx = size.width / 700f
                    val sy = size.height / 240f
                    val cx = 350f * sx
                    val cy = 120f * sy
                    val rx = outerRx * sx
                    val ry = outerRy * sy

                    // Outer 5G ellipse
                    drawOval(
                        color = Color(0xFF2F6BFF).copy(alpha = 0.16f),
                        topLeft = Offset(cx - rx, cy - ry),
                        size = Size(rx * 2f, ry * 2f)
                    )
                    drawOval(
                        color = Color(0xFF6F9BFF),
                        topLeft = Offset(cx - rx, cy - ry),
                        size = Size(rx * 2f, ry * 2f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )

                    // Inner stronger field ellipse
                    val irx = rx * 0.62f
                    val iry = ry * 0.62f
                    drawOval(
                        color = Color(0xFF2F6BFF).copy(alpha = 0.25f),
                        topLeft = Offset(cx - irx, cy - iry),
                        size = Size(irx * 2f, iry * 2f)
                    )

                    // Steel wall (pasif)
                    if (shieldType == "pas") {
                        drawRect(
                            color = Color(0xFF9AA6B6),
                            topLeft = Offset(540f * sx, 30f * sy),
                            size = Size(14f * sx, 180f * sy)
                        )
                    }

                    // Bucking coils (aktif)
                    if (shieldType == "akt") {
                        drawRect(
                            color = AmberWarning,
                            topLeft = Offset(290f * sx, 80f * sy),
                            size = Size(10f * sx, 80f * sy)
                        )
                        drawRect(
                            color = AmberWarning,
                            topLeft = Offset(400f * sx, 80f * sy),
                            size = Size(10f * sx, 80f * sy)
                        )
                    }

                    // MRI Unit Box
                    drawRoundRect(
                        color = Color(0xFF475569),
                        topLeft = Offset(300f * sx, 90f * sy),
                        size = Size(100f * sx, 60f * sy),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )

                    // Person in public area
                    val px = 620f * sx
                    val py = 150f * sy
                    drawCircle(
                        color = if (isPublicSafe) EmeraldSuccess else RoseDanger,
                        radius = 8.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                Text(
                    text = "Garis putus-putus = batas 5 G",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
                Text(
                    text = "Area publik",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (isPublicSafe) EmeraldDark.copy(alpha = 0.5f) else RoseDark.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPublicSafe) EmeraldSuccess else RoseDanger
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isPublicSafe) {
                            "Orang di area publik: di luar garis 5 G (Aman)"
                        } else {
                            "Orang di area publik: di dalam garis 5 G (Tidak aman)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPublicSafe) EmeraldSuccess else RoseDanger
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = descText + if (fieldB0 > 1.5) " Pada medan lebih tinggi jejaknya lebih lebar." else "",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ============================================================================
// AKTIVITAS A5: GRAFIK KUAT MEDAN B0 VS SNR DAN SAR
// ============================================================================
@Composable
fun A5B0SnrSarSimulator(modifier: Modifier = Modifier) {
    var b0Tesla by remember { mutableFloatStateOf(1.5f) }
    val relSnr = b0Tesla / 1.5f
    val relSar = relSnr * relSnr
    val maxRatio = 7.0f / 1.5f
    val larmorMhz = 42.58 * b0Tesla.toDouble()
    val earthMultiple = (b0Tesla * 10000f) / 0.5f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a5_b0_snr_sar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Grafik Kuat Medan vs SNR dan SAR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T3.1 (Nilai relatif terhadap 1,5 T)",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kuat Medan B0:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${fmtKoma(b0Tesla.toDouble(), 1)} T",
                    fontSize = 12.sp,
                    color = CyanBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = b0Tesla,
                onValueChange = { b0Tesla = it },
                valueRange = 0.5f..7.0f,
                modifier = Modifier.testTag("a5_b0_slider"),
                colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.5f, 1.5f, 3.0f, 7.0f).forEach { preset ->
                    OutlinedButton(
                        onClick = { b0Tesla = preset },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("a5_preset_$preset"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${fmtKoma(preset.toDouble(), if (preset % 1f == 0f) 0 else 1)}\u00A0T",
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Larmor ¹H ${fmtKoma(larmorMhz, 2)} MHz | ${fmtKoma(earthMultiple.toDouble(), 0)} × medan bumi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SNR relatif (kira-kira linear): ${fmtKoma(relSnr.toDouble(), 2)} ×",
                fontSize = 11.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (relSnr / maxRatio).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = CyanPrimary,
                trackColor = CardSlate
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SAR relatif (sebanding kuadrat B0): ${fmtKoma(relSar.toDouble(), 2)} ×",
                fontSize = 11.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (relSar / (maxRatio * maxRatio)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = AmberWarning,
                trackColor = CardSlate
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    b0Tesla < 1.45f -> "Medan rendah: SNR lebih kecil, SAR lebih ringan."
                    b0Tesla <= 1.55f -> "Titik acuan klinis (1,5 T)."
                    b0Tesla <= 3.05f -> "SNR naik kira-kira sebanding medan, SAR naik lebih cepat (kuadrat). Artefak suseptibilitas dan pergeseran kimia (Hz) juga membesar."
                    else -> "SAR naik tajam; pada medan ini SAR membatasi sekuens, dan T1 makin panjang."
                },
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

// ============================================================================
// AKTIVITAS A6: PILIH MAGNET UNTUK SITUASI KLINIS
// ============================================================================
private data class MagnetScenario(
    val situation: String,
    val correctIndices: Set<Int>,
    val reason: String
)

private val MAGNET_OPTIONS = listOf(
    "Permanen, medan rendah (terbuka)",
    "Resistif, medan rendah sampai menengah",
    "Superkonduktif 1,5 T",
    "Superkonduktif 3 T"
)

private val MAGNET_SCENARIOS = listOf(
    MagnetScenario(
        situation = "Klinik kecil dengan anggaran terbatas, tanpa pasokan helium.",
        correctIndices = setOf(0),
        reason = "Magnet permanen tanpa listrik dan kriogen, biaya operasi rendah. Kekurangannya SNR lebih rendah dan bobot besar."
    ),
    MagnetScenario(
        situation = "Rumah sakit besar dengan banyak pasien dan pemeriksaan beragam.",
        correctIndices = setOf(2, 3),
        reason = "Superkonduktif 1,5 T (serba guna) atau 3 T (SNR dan resolusi lebih tinggi). Butuh helium dan ruang khusus."
    ),
    MagnetScenario(
        situation = "Lokasi dengan daya listrik terbatas.",
        correctIndices = setOf(0),
        reason = "Magnet permanen tidak memakai listrik untuk menjaga medan. Resistif butuh daya puluhan kW; superkonduktif butuh pendingin kriogen."
    ),
    MagnetScenario(
        situation = "Banyak pasien klaustrofobik.",
        correctIndices = setOf(0, 2),
        reason = "Desain permanen terbuka, atau superkonduktif 1,5 T dengan bore lebar 70 cm, membuat pasien lebih nyaman."
    )
)

@Composable
fun A6PilihMagnetActivity(modifier: Modifier = Modifier) {
    var scenarioIdx by remember { mutableIntStateOf(0) }
    val selectedCorrectByScenario = remember { mutableStateListOf(-1, -1, -1, -1) }
    val wrongAttemptsByScenario = remember {
        listOf(
            mutableStateListOf<Int>(),
            mutableStateListOf<Int>(),
            mutableStateListOf<Int>(),
            mutableStateListOf<Int>()
        )
    }

    val currentScenario = MAGNET_SCENARIOS[scenarioIdx]
    val chosenCorrect = selectedCorrectByScenario[scenarioIdx]
    val wrongSet = wrongAttemptsByScenario[scenarioIdx]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a6_pilih_magnet_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A6",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Pilih Magnet untuk Situasi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T1.3, T1.4",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Situasi ${scenarioIdx + 1}/${MAGNET_SCENARIOS.size}: ${currentScenario.situation}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            MAGNET_OPTIONS.forEachIndexed { optIdx, optText ->
                val isCorrectSelected = chosenCorrect == optIdx
                val isWrongAttempt = wrongSet.contains(optIdx)
                val isLocked = chosenCorrect >= 0 && !isCorrectSelected

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable(enabled = !isWrongAttempt && !isLocked) {
                            if (currentScenario.correctIndices.contains(optIdx)) {
                                selectedCorrectByScenario[scenarioIdx] = optIdx
                            } else if (!wrongSet.contains(optIdx)) {
                                wrongSet.add(optIdx)
                            }
                        }
                        .testTag("a6_option_$optIdx"),
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isCorrectSelected -> EmeraldDark
                        isWrongAttempt -> RoseDark
                        else -> CardSlate
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isCorrectSelected -> EmeraldSuccess
                            isWrongAttempt -> RoseDanger
                            else -> CardSlateBorder
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isCorrectSelected -> "✓ $optText"
                                isWrongAttempt -> "✗ $optText"
                                else -> optText
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isCorrectSelected) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimary
                        )
                        if (isCorrectSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                        } else if (isWrongAttempt) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (chosenCorrect >= 0 || wrongSet.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (chosenCorrect >= 0) {
                        "Cocok! ${currentScenario.reason}"
                    } else {
                        "Belum cocok. Coba pilihan lain."
                    },
                    fontSize = 11.sp,
                    color = if (chosenCorrect >= 0) EmeraldSuccess else RoseDanger,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (scenarioIdx > 0) scenarioIdx-- },
                    enabled = scenarioIdx > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "← Sebelumnya",
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                OutlinedButton(
                    onClick = { if (scenarioIdx < MAGNET_SCENARIOS.size - 1) scenarioIdx++ },
                    enabled = scenarioIdx < MAGNET_SCENARIOS.size - 1,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Berikutnya →",
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ============================================================================
// AKTIVITAS A7: URUTKAN KEJADIAN QUENCH
// ============================================================================
private data class QuenchStep(
    val stepIndex: Int,
    val title: String,
    val detail: String
)

private val QUENCH_STEPS = listOf(
    QuenchStep(
        stepIndex = 0,
        title = "Sebagian kawat memanas dan keluar dari keadaan superkonduktif",
        detail = "Pemicunya bisa gangguan pada magnet atau tombol quench darurat."
    ),
    QuenchStep(
        stepIndex = 1,
        title = "Hambatan muncul, arus besar berubah menjadi panas",
        detail = "Arus yang tadinya tanpa hambatan kini menghasilkan panas dan menyebar ke kumparan lain."
    ),
    QuenchStep(
        stepIndex = 2,
        title = "Helium cair mendidih dan menguap sangat cepat",
        detail = "Gas helium mengembang sekitar 700 kali volume cairnya."
    ),
    QuenchStep(
        stepIndex = 3,
        title = "Gas helium keluar lewat pipa quench ke luar gedung",
        detail = "Medan magnet menghilang. Prosedur keselamatan lengkap ada di M07B."
    )
)

@Composable
fun A7UrutanQuenchActivity(modifier: Modifier = Modifier) {
    var shuffleSeed by remember { mutableIntStateOf(1) }
    val shuffledStepIndices = remember(shuffleSeed) {
        listOf(0, 1, 2, 3).shuffled(kotlin.random.Random(shuffleSeed))
    }
    var nextRequiredStep by remember { mutableIntStateOf(0) }
    var feedbackMessage by remember { mutableStateOf("Ketuk kartu sesuai urutan kejadian, dari yang pertama.") }
    var isLastTapError by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m5_a7_quench_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                ) {
                    Text(
                        text = "A7",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(
                        text = "Urutkan Kejadian Quench",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menjelaskan: T2.6",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kartu pilihan yang belum ditempatkan
            val remainingSteps = shuffledStepIndices.filter { it >= nextRequiredStep }
            if (remainingSteps.isNotEmpty()) {
                Text(
                    text = "Pilih langkah ke-${nextRequiredStep + 1}:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                remainingSteps.forEach { stepIdx ->
                    val step = QUENCH_STEPS[stepIdx]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                if (stepIdx == nextRequiredStep) {
                                    nextRequiredStep++
                                    isLastTapError = false
                                    feedbackMessage = if (nextRequiredStep == QUENCH_STEPS.size) {
                                        "Urutan benar! Quench selalu dimulai dari satu titik yang memanas."
                                    } else {
                                        "Benar! Lanjutkan ke langkah berikutnya."
                                    }
                                } else {
                                    isLastTapError = true
                                    feedbackMessage = "Belum tepat. Apa yang terjadi lebih dahulu?"
                                }
                            }
                            .testTag("a7_step_card_$stepIdx"),
                        shape = RoundedCornerShape(10.dp),
                        color = CardSlate,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                    ) {
                        Text(
                            text = step.title,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Urutan Anda (${nextRequiredStep}/${QUENCH_STEPS.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBright
            )
            Spacer(modifier = Modifier.height(6.dp))

            for (idx in 0 until nextRequiredStep) {
                val placed = QUENCH_STEPS[idx]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldDark.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${idx + 1}. ${placed.title}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = placed.detail,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feedbackMessage,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    nextRequiredStep == QUENCH_STEPS.size -> EmeraldSuccess
                    isLastTapError -> RoseDanger
                    else -> CyanBright
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    nextRequiredStep = 0
                    shuffleSeed++
                    isLastTapError = false
                    feedbackMessage = "Ketuk kartu sesuai urutan kejadian, dari yang pertama."
                },
                modifier = Modifier.testTag("a7_reset_button"),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Acak ulang", fontSize = 11.sp, maxLines = 1, softWrap = false)
            }
        }
    }
}
