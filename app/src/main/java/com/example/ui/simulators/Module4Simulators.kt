package com.example.ui.simulators

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class M04SimulationType(
    val id: String,
    val subModule: String,
    val shortCode: String,
    val title: String,
    val nodeTag: String,
    val subtitle: String
) {
    // M04A (4 aktivitas)
    M04A_GRADIEN_LARMOR("m4a_gradien_larmor", "M04A", "A1", "Gradien & Larmor", "M04A.T1.1", "Kemiringan B₀ + G·x & Δf"),
    M04A_PEMILIHAN_IRISAN("m4a_pemilihan_irisan", "M04A", "A2", "Pemilihan Irisan", "M04A.T1.2/T1.5", "Gz & Bandwidth RF → Tebal Irisan"),
    M04A_PENGODEAN_FREKUENSI("m4a_pengodean_frekuensi", "M04A", "A3", "Pengodean Frekuensi", "M04A.T1.6", "Gx & Spektrum Readout"),
    M04A_SATU_TR_TIGA("m4a_satu_tr_tiga", "M04A", "A4", "1 TR: Gz, Gy, Gx", "M04A.T1.7", "Urutan Gradien dalam 1 TR"),

    // M04B (10 aktivitas)
    M04B_LEMARI_LACI("m4b_lemari_laci", "M04B", "B1", "Lemari Laci Ruang-k", "M04B.T2.1", "Analogi laci ky & kolom kx"),
    M04B_SAMPLING_NYQUIST("m4b_sampling_nyquist", "M04B", "B2", "Sampling & Nyquist", "M04B.T2.3/T2.4", "ADC dwell time & aliasing"),
    M04B_RECEIVE_BW("m4b_receive_bw", "M04B", "B3", "Receive Bandwidth", "M04B.T2.5", "rBW vs SNR & Chemical Shift"),
    M04B_FFT_GELOMBANG("m4b_fft_gelombang", "M04B", "B4", "FFT dari Gelombang", "M04B.T2.7", "Dekomposisi sinyal waktu → f"),
    M04B_SINYAL_RESOLUSI("m4b_sinyal_resolusi", "M04B", "B5", "Sinyal vs Resolusi", "M04B.T2.10.1", "Pusat ruang-k vs tepi ruang-k"),
    M04B_FOV_VOKSEL("m4b_fov_voksel", "M04B", "B6", "FOV & Ukuran Voksel", "M04B.T2.10.5", "FOV / Matriks & SNR relatif"),
    M04B_LAB_TERPADU("m4b_lab_terpadu", "M04B", "B7", "Lab Ruang-k 2D", "M04B.T2.10.2", "Filter Low-Pass / High-Pass"),
    M04B_FOV_MATRIKS("m4b_fov_matriks", "M04B", "B7.2", "FOV & Matriks", "M04B.T2.10.4", "Δk = 1/FOV & k_max"),
    M04B_SIMULATOR_AKHIR("m4b_simulator_akhir", "M04B", "B7.3", "Simulator Akuisisi", "M04B.T2.10.3", "Waktu pindai, matriks & NEX"),

    // M04C (6 aktivitas)
    M04C_PSEUDO_FREKUENSI("m4c_pseudo_frekuensi", "M04C", "C1", "Pseudo-Frekuensi Gy", "M04C.T3.1", "Pergeseran fase antar-TR"),
    M04C_SIMETRI_KONJUGAT("m4c_simetri_konjugat", "M04C", "C2", "Simetri Konjugat", "M04C.T3.3", "Partial Fourier / Half-NEX"),
    M04C_TARIAN_KSPACE("m4c_tarian_kspace", "M04C", "C3", "Tarian Ruang-k", "M04C.T3.5", "Gerak titik oleh RF, Gy, Gx & 180°"),
    M04C_PILIHAN_PENGISIAN("m4c_pilihan_pengisian", "M04C", "C4", "Pilihan Pengisian", "M04C.T3.4", "Linear, Centric, Partial, Keyhole"),
    M04C_URUTAN_AKUISISI("m4c_urutan_akuisisi", "M04C", "C5", "Urutan Akuisisi", "M04C.T3.6", "Sequential vs Interleaved"),
    M04C_JALUR_KSPACE("m4c_jalur_kspace", "M04C", "C6", "Jalur Ruang-k", "M04C.T3.7", "Cartesian, Radial, Spiral & EPI");

    companion object {
        fun fromLabType(labType: String?): M04SimulationType =
            entries.firstOrNull { it.id == labType } ?: M04A_GRADIEN_LARMOR
    }
}

@Composable
fun Module4LabHub(
    selectedActivityId: String,
    onSelectActivity: (String) -> Unit
) {
    val activeType = M04SimulationType.fromLabType(selectedActivityId)
    var subFilter by remember { mutableStateOf("SEMUA") }
    val filteredItems = remember(subFilter) {
        if (subFilter == "SEMUA") M04SimulationType.entries
        else M04SimulationType.entries.filter { it.subModule == subFilter }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanDark)
                            .border(1.dp, CyanBright.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "M04",
                            tint = CyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "M04: Laboratorium Gradien, FFT & Ruang-k",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "20 simulasi M04A–M04C: pemilihan irisan, sampling Nyquist, FFT, FOV/voksel & pengisian ruang-k.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("SEMUA" to "Semua (20)", "M04A" to "M04A (4)", "M04B" to "M04B (10)", "M04C" to "M04C (6)").forEach { (id, label) ->
                        FilterChip(
                            selected = subFilter == id,
                            onClick = { subFilter = id },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanDark, selectedLabelColor = CyanBright)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredItems.forEach { item ->
                        val isSelected = item == activeType
                        Surface(
                            onClick = { onSelectActivity(item.id) },
                            modifier = Modifier.width(164.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyanDark else DeepObsidian,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CyanBright else CardSlateBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.subModule}·${item.shortCode}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanBright else AmberWarning,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(item.nodeTag, fontSize = 9.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) CyanBright else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Simulasi Aktif M04
        when (activeType) {
            M04SimulationType.M04A_GRADIEN_LARMOR -> M04GradientLarmorSim()
            M04SimulationType.M04A_PEMILIHAN_IRISAN -> M04SliceSelectionSim()
            M04SimulationType.M04A_PENGODEAN_FREKUENSI -> M04FrequencyEncodingSim()
            M04SimulationType.M04A_SATU_TR_TIGA -> M04OneTrSequenceSim()
            M04SimulationType.M04B_LEMARI_LACI -> M04DrawerKSpaceSim()
            M04SimulationType.M04B_SAMPLING_NYQUIST -> M04NyquistSamplingSim()
            M04SimulationType.M04B_RECEIVE_BW -> M04ReceiveBandwidthSim()
            M04SimulationType.M04B_FFT_GELOMBANG -> M04FftWaveSim()
            M04SimulationType.M04B_SINYAL_RESOLUSI -> M04KSpaceCenterEdgeSim()
            M04SimulationType.M04B_FOV_VOKSEL -> M04FovVoxelSim()
            M04SimulationType.M04B_LAB_TERPADU -> M04KSpaceFilterSim()
            M04SimulationType.M04B_FOV_MATRIKS -> M04FovMatrixDeltaKSim()
            M04SimulationType.M04B_SIMULATOR_AKHIR -> M04AcquisitionTradeoffSim()
            M04SimulationType.M04C_PSEUDO_FREKUENSI -> M04PhaseEncodingSim()
            M04SimulationType.M04C_SIMETRI_KONJUGAT -> M04PartialFourierSim()
            M04SimulationType.M04C_TARIAN_KSPACE -> M04KSpaceDanceStepSim()
            M04SimulationType.M04C_PILIHAN_PENGISIAN -> M04FillingOrderSim()
            M04SimulationType.M04C_URUTAN_AKUISISI -> M04AcquisitionOrderSim()
            M04SimulationType.M04C_JALUR_KSPACE -> M04TrajectoryDanceSim()
        }
    }
}

@Composable
private fun M04CardShell(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanBright)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun M04GradientLarmorSim() {
    var gradMtM by rememberSaveable { mutableFloatStateOf(20f) }
    var posCm by rememberSaveable { mutableFloatStateOf(10f) }
    val b0 = 1.5
    val deltaB = gradMtM * 1e-3 * (posCm / 100.0)
    val freqMhz = 42.58 * (b0 + deltaB)
    val deltaKhz = (42.58 * deltaB * 1000.0)

    M04CardShell("A1. Gradien & Frekuensi Larmor (M04A.T1.1)", "Gradien G memiringkan medan magnet sepanjang posisi x sehingga f(x) = γ(B₀ + G·x).") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val midY = size.height / 2f
            val hPad = 12.dp.toPx()
            val gPad = 16.dp.toPx()
            drawLine(CardSlateBorder, Offset(hPad, midY), Offset(size.width - hPad, midY), 1.dp.toPx())
            val slope = (gradMtM / 40f) * (size.height * 0.34f)
            drawLine(CyanBright, Offset(gPad, midY + slope), Offset(size.width - gPad, midY - slope), 2.5.dp.toPx())
            val xMarker = size.width / 2f + (posCm / 20f) * (size.width * 0.42f)
            val yMarker = midY - (posCm / 20f) * slope
            drawCircle(AmberWarning, radius = 5.dp.toPx(), center = Offset(xMarker, yMarker))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Gradien G: ${gradMtM.roundToInt()} mT/m | Posisi x: ${posCm.roundToInt()} cm", fontSize = 11.sp, color = TextPrimary)
        Slider(value = gradMtM, onValueChange = { gradMtM = it }, valueRange = 0f..40f)
        Slider(value = posCm, onValueChange = { posCm = it }, valueRange = -20f..20f)
        Text("f(x) = ${"%.4f".format(freqMhz)} MHz (Δf = ${"%.1f".format(deltaKhz)} kHz)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
    }
}

@Composable
private fun M04SliceSelectionSim() {
    var gzMtM by rememberSaveable { mutableFloatStateOf(20f) }
    var rfBwKhz by rememberSaveable { mutableFloatStateOf(2.0f) }
    val thicknessMm = (rfBwKhz * 1000.0 / (42.58e6 * gzMtM * 1e-3)) * 1000.0

    M04CardShell("A2. Pemilihan Irisan / Slice Selection (M04A.T1.2 / T1.5)", "Tebal irisan = Bandwidth RF / (γ · Gz). Gradien Gz lebih curam atau BW lebih sempit menghasilkan irisan lebih tipis.") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val wFrac = (thicknessMm / 25.0).toFloat().coerceIn(0.04f, 0.7f)
            val sliceW = size.width * wFrac
            val vPad = 12.dp.toPx()
            drawRect(CyanPrimary.copy(alpha = 0.28f), Offset(size.width / 2f - sliceW / 2f, vPad), Size(sliceW, size.height - vPad * 2f))
            drawRect(CyanBright, Offset(size.width / 2f - sliceW / 2f, vPad), Size(sliceW, size.height - vPad * 2f), style = Stroke(1.5.dp.toPx()))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Tebal Irisan ≈ ${"%.2f".format(thicknessMm).replace('.', ',')} mm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanBright)
        Text("Gradien Gz: ${gzMtM.roundToInt()} mT/m", fontSize = 11.sp, color = TextPrimary)
        Slider(value = gzMtM, onValueChange = { gzMtM = it }, valueRange = 5f..45f)
        Text("Bandwidth RF: ${"%.1f".format(rfBwKhz)} kHz", fontSize = 11.sp, color = TextPrimary)
        Slider(value = rfBwKhz, onValueChange = { rfBwKhz = it }, valueRange = 0.5f..5.0f)
    }
}

@Composable
private fun M04FrequencyEncodingSim() {
    var gxMtM by rememberSaveable { mutableFloatStateOf(15f) }
    var fovCm by rememberSaveable { mutableFloatStateOf(24f) }
    val spanKhz = 42.58 * gxMtM * (fovCm / 100.0)

    M04CardShell("A3. Pengodean Frekuensi / Readout (M04A.T1.6)", "Gradien Gx aktif saat echo dibaca sehingga posisi x terpetakan ke frekuensi.") {
        Text("Gx: ${gxMtM.roundToInt()} mT/m | FOV: ${fovCm.roundToInt()} cm", fontSize = 11.sp, color = TextPrimary)
        Slider(value = gxMtM, onValueChange = { gxMtM = it }, valueRange = 5f..40f)
        Slider(value = fovCm, onValueChange = { fovCm = it }, valueRange = 12f..40f)
        Text("Rentang Frekuensi Lintas FOV: ${"%.1f".format(spanKhz)} kHz", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
    }
}

@Composable
private fun M04OneTrSequenceSim() {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val labels = listOf("1. RF 90° + Gz (Pilih Irisan)", "2. Gy (Pengodean Fase)", "3. RF 180° + Gz (Refocusing)", "4. Gx + ADC (Baca Echo)")
    M04CardShell("A4. Satu TR: Gz, Gy, Gx Bekerja Berurutan (M04A.T1.7)", "Pilih tahapan untuk melihat gradien yang aktif dalam satu siklus TR Spin Echo.") {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            labels.forEachIndexed { idx, txt ->
                FilterChip(selected = step == idx, onClick = { step = idx }, label = { Text("${idx + 1}", fontSize = 11.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(labels[step], fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanBright)
        Text(
            text = when (step) {
                0 -> "Gz menyala bersamaan dengan pulsa RF 90° untuk mengeksitasi satu irisan tipis."
                1 -> "Gy menyala singkat untuk memberi pergeseran fase berbeda di tiap baris y."
                2 -> "Pulsa 180° membalik fase spin untuk menghilangkan dephasing T2*."
                else -> "Gx menyala saat sinyal gema dicuplik oleh ADC ke satu baris ruang-k."
            },
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun M04DrawerKSpaceSim() {
    var selectedKy by remember { mutableIntStateOf(4) }
    M04CardShell("B1. Analogi Lemari Laci Ruang-k (M04B.T2.1)", "Setiap baris ruang-k (ky) adalah satu laci frekuensi spasial; laci tengah menyimpan kontras.") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until 8) {
                val isCenter = i in 3..4
                val isSel = i == selectedKy
                Surface(
                    onClick = { selectedKy = i },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) CyanPrimary else if (isCenter) CyanDark else DeepObsidian,
                    border = BorderStroke(1.dp, if (isSel) CyanBright else CardSlateBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("k${i - 4}", fontSize = 10.sp, color = if (isSel) DeepObsidian else TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (selectedKy in 3..4) "Laci Tengah (ky ≈ 0): Menyimpan energi sinyal & kontras utama citra!"
            else "Laci Tepi (|ky| besar): Menyimpan tepi tajam & resolusi detail halus.",
            fontSize = 11.sp,
            color = if (selectedKy in 3..4) EmeraldSuccess else AmberWarning
        )
    }
}

@Composable
private fun M04NyquistSamplingSim() {
    var signalFreq by rememberSaveable { mutableFloatStateOf(4f) }
    var sampleRate by rememberSaveable { mutableFloatStateOf(12f) }
    val aliased = sampleRate < 2f * signalFreq

    M04CardShell("B2. Sampling & Batas Nyquist (M04B.T2.3 / T2.4)", "Frekuensi sampling harus ≥ 2 × frekuensi tertinggi (f_Nyquist), jika tidak terjadi aliasing (wrap-around).") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val mid = size.height / 2f
            val path = Path()
            for (px in 0..size.width.toInt() step 4) {
                val t = px / size.width
                val y = mid - sin(2f * PI.toFloat() * signalFreq * t) * (size.height * 0.36f)
                if (px == 0) path.moveTo(px.toFloat(), y) else path.lineTo(px.toFloat(), y)
            }
            drawPath(path, CyanPrimary, style = Stroke(1.5.dp.toPx()))
            val n = sampleRate.roundToInt().coerceAtLeast(2)
            for (k in 0..n) {
                val t = k.toFloat() / n
                val x = t * size.width
                val y = mid - sin(2f * PI.toFloat() * signalFreq * t) * (size.height * 0.36f)
                drawCircle(if (aliased) RoseDanger else EmeraldSuccess, radius = 3.5.dp.toPx(), center = Offset(x, y))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("f_sinyal: ${signalFreq.roundToInt()} Hz | f_sampling: ${sampleRate.roundToInt()} Hz", fontSize = 11.sp, color = TextPrimary)
        Slider(value = signalFreq, onValueChange = { signalFreq = it }, valueRange = 1f..10f)
        Slider(value = sampleRate, onValueChange = { sampleRate = it }, valueRange = 4f..28f)
        Text(
            text = if (aliased) "ALIASING! f_sampling < 2 × f_sinyal" else "Aman (Memenuhi syarat Nyquist ≥ 2 × f_sinyal)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (aliased) RoseDanger else EmeraldSuccess
        )
    }
}

@Composable
private fun M04ReceiveBandwidthSim() {
    var rbwKhz by rememberSaveable { mutableFloatStateOf(32f) }
    val relSnr = sqrt(32f / rbwKhz)
    val chemShiftPx = (220f / (rbwKhz * 1000f / 256f))

    M04CardShell("B3. Receive Bandwidth (rBW) vs SNR & Chemical Shift (M04B.T2.5)", "Menaikkan rBW mengurangi chemical shift & distorsi, tetapi menurunkan SNR (∝ 1/√rBW).") {
        Text("Receive Bandwidth: ±${rbwKhz.roundToInt()} kHz", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
        Slider(value = rbwKhz, onValueChange = { rbwKhz = it }, valueRange = 8f..125f)
        Text("SNR Relatif: ${"%.2f".format(relSnr)}x | Pergeseran Lemak-Air (1,5 T): ${"%.2f".format(chemShiftPx)} piksel", fontSize = 11.sp, color = EmeraldSuccess)
    }
}

@Composable
private fun M04FftWaveSim() {
    var w1 by remember { mutableFloatStateOf(1.0f) }
    var w2 by remember { mutableFloatStateOf(0.5f) }
    M04CardShell("B4. Transformasi Fourier (FFT) dari Gelombang (M04B.T2.7)", "Gabungan dua gelombang kosinus di domain waktu dipisahkan menjadi dua puncak di domain frekuensi.") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val mid = size.height / 2f
            val path = Path()
            for (px in 0..size.width.toInt() step 4) {
                val t = px / size.width
                val v = (w1 * cos(2f * PI.toFloat() * 3f * t) + w2 * cos(2f * PI.toFloat() * 8f * t)) / 1.5f
                val y = mid - v * (size.height * 0.38f)
                if (px == 0) path.moveTo(px.toFloat(), y) else path.lineTo(px.toFloat(), y)
            }
            drawPath(path, CyanBright, style = Stroke(2f))
        }
        Text("Amplitudo f₁ (rendah): ${"%.1f".format(w1)} | Amplitudo f₂ (tinggi): ${"%.1f".format(w2)}", fontSize = 11.sp, color = TextPrimary)
        Slider(value = w1, onValueChange = { w1 = it }, valueRange = 0f..1f)
        Slider(value = w2, onValueChange = { w2 = it }, valueRange = 0f..1f)
    }
}

@Composable
private fun M04KSpaceCenterEdgeSim() {
    var mode by remember { mutableIntStateOf(0) } // 0 penuh, 1 tengah saja, 2 tepi saja
    M04CardShell("B5. Pusat vs Tepi Ruang-k (M04B.T2.10.1)", "Bandingkan efek mengisi pusat ruang-k saja (kontras tinggi, buram) vs tepi saja (tepi tajam, gelap).") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Penuh", "Pusat (Low-Pass)", "Tepi (High-Pass)").forEachIndexed { idx, lbl ->
                FilterChip(selected = mode == idx, onClick = { mode = idx }, label = { Text(lbl, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (mode) {
                0 -> "Penuh: Kontras jaringan utuh dan batas tepi anatomi tajam."
                1 -> "Pusat Saja: Kontras terang-gelap terjaga, tetapi detail halus tampak buram."
                else -> "Tepi Saja: Hanya garis batas objek yang tampak, kontras isi jaringan hilang."
            },
            fontSize = 11.sp,
            color = CyanBright
        )
    }
}

@Composable
private fun M04FovVoxelSim() {
    var fovMm by remember { mutableFloatStateOf(240f) }
    var matrix by remember { mutableIntStateOf(256) }
    var sliceMm by remember { mutableFloatStateOf(4f) }
    val pixelMm = fovMm / matrix
    val voxelMm3 = pixelMm * pixelMm * sliceMm

    M04CardShell("B6. FOV & Ukuran Voksel (M04B.T2.10.5)", "Ukuran piksel = FOV / Matriks; Volume voksel = Δx · Δy · tebal irisan.") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(128, 256, 512).forEach { m ->
                FilterChip(selected = matrix == m, onClick = { matrix = m }, label = { Text("${m}×$m", fontSize = 10.sp) })
            }
        }
        Text("FOV: ${fovMm.roundToInt()} mm | Tebal Irisan: ${"%.1f".format(sliceMm)} mm", fontSize = 11.sp, color = TextPrimary)
        Slider(value = fovMm, onValueChange = { fovMm = (it / 10f).roundToInt() * 10f }, valueRange = 120f..400f)
        Slider(value = sliceMm, onValueChange = { sliceMm = (it * 2f).roundToInt() / 2f }, valueRange = 1f..10f)
        Text("Resolusi dalam bidang: ${"%.2f".format(pixelMm)} × ${"%.2f".format(pixelMm)} mm | Volume voksel: ${"%.2f".format(voxelMm3)} mm³", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
    }
}

@Composable
private fun M04KSpaceFilterSim() {
    var cutoff by remember { mutableFloatStateOf(0.5f) }
    M04CardShell("B7. Laboratorium Filter Ruang-k 2D (M04B.T2.10.2)", "Geser radius cutoff ruang-k untuk melihat proporsi energi kontras vs detail.") {
        Text("Radius Cutoff: ${(cutoff * 100).roundToInt()}%", fontSize = 11.sp, color = CyanBright)
        Slider(value = cutoff, onValueChange = { cutoff = it }, valueRange = 0.1f..1f)
        Text("Fraksi energi pusat terambil ≈ ${((1f - exp(-cutoff * 4f)) * 100).roundToInt()}%", fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun M04FovMatrixDeltaKSim() {
    var fovCm by remember { mutableFloatStateOf(24f) }
    val deltaK = 1.0 / (fovCm / 100.0)
    M04CardShell("B7.2. Hubungan FOV & Δk (M04B.T2.10.4)", "Jarak antar-baris ruang-k berbanding terbalik dengan FOV: Δk = 1 / FOV.") {
        Text("FOV: ${fovCm.roundToInt()} cm → Δk = ${"%.2f".format(deltaK)} m⁻¹", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanBright)
        Slider(value = fovCm, onValueChange = { fovCm = it }, valueRange = 10f..45f)
    }
}

@Composable
private fun M04AcquisitionTradeoffSim() {
    var nex by remember { mutableIntStateOf(2) }
    var pe by remember { mutableIntStateOf(256) }
    var trMs by remember { mutableFloatStateOf(500f) }
    val scanSec = (trMs / 1000f) * pe * nex
    val relSnr = sqrt(nex.toFloat())

    M04CardShell("B7.3. Simulator Trade-off Akuisisi (M04B.T2.10.3 / T3.2)", "Waktu pindai TA = TR × N_PE × NEX, sedangkan SNR ∝ √NEX.") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 2, 4).forEach { n -> FilterChip(selected = nex == n, onClick = { nex = n }, label = { Text("NEX=$n", fontSize = 10.sp) }) }
            listOf(128, 256, 384).forEach { p -> FilterChip(selected = pe == p, onClick = { pe = p }, label = { Text("PE=$p", fontSize = 10.sp) }) }
        }
        Text("TR: ${trMs.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = trMs, onValueChange = { trMs = (it / 50f).roundToInt() * 50f }, valueRange = 200f..4000f)
        Text("Estimasi TA: ${"%.1f".format(scanSec)} detik | SNR Relatif: ${"%.2f".format(relSnr)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
    }
}

@Composable
private fun M04PhaseEncodingSim() {
    var kyStep by remember { mutableIntStateOf(2) }
    M04CardShell("C1. Pseudo-Frekuensi Pengodean Fase (M04C.T3.1)", "Perubahan fase antar-TR membentuk gelombang spasial di sumbu y.") {
        Text("Langkah gradien Gy: ky = $kyStep", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
        Slider(value = kyStep.toFloat(), onValueChange = { kyStep = it.roundToInt() }, valueRange = -6f..6f)
        Text("Jumlah siklus fase penuh melintasi FOV = ${abs(kyStep)} siklus.", fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun M04PartialFourierSim() {
    var fraction by remember { mutableFloatStateOf(0.625f) }
    M04CardShell("C2. Simetri Konjugat / Partial Fourier (M04C.T3.3)", "Karena ruang-k bersifat simetri konjugat (Hermitian), mengisi ~60–75% baris sudah cukup untuk rekonstruksi.") {
        Text("Fraksi Pengisian: ${(fraction * 100).roundToInt()}% (Penghematan waktu: ${((1f - fraction) * 100).roundToInt()}%)", fontSize = 11.sp, color = CyanBright)
        Slider(value = fraction, onValueChange = { fraction = it }, valueRange = 0.55f..1.0f)
        Text("SNR relatif ≈ ${"%.2f".format(sqrt(fraction))}x dibanding pengisian penuh.", fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun M04KSpaceDanceStepSim() {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val labels = listOf("1. Eksitasi RF", "2. Gy (+): Baris Atas", "3. Gx (+): Kiri→Kanan", "4. Pulsa 180° (SE)")
    val notes = listOf(
        "Pulsa RF eksitasi menempatkan titik awal penelusuran tepat di pusat ruang-k (kx=0, ky=0).",
        "Amplitudo & polaritas Gy menggeser posisi ke baris ky (+ atas / − bawah) dan menyiapkan dephasing awal Gx (−).",
        "Gradien readout Gx (+) menyapu baris ruang-k secara horizontal dari kiri (−kx) ke kanan (+kx) saat ADC membaca gema.",
        "Pada sekuens Spin Echo, pulsa refokus 180° membalik koordinat fase ke titik cermin berseberangan (−kx, −ky)."
    )

    M04CardShell("C3. Aturan Tarian Ruang-k (M04C.T3.5)", "Ikuti langkah demi langkah perpindahan titik ruang-k oleh RF, gradien Gy, gradien Gx, dan pulsa 180°.") {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            labels.forEachIndexed { idx, lbl ->
                FilterChip(selected = step == idx, onClick = { step = idx }, label = { Text(lbl, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rx = size.width * 0.34f
            val ry = size.height * 0.34f
            drawLine(CardSlateBorder, Offset(cx - rx, cy), Offset(cx + rx, cy), 1.5f)
            drawLine(CardSlateBorder, Offset(cx, cy - ry), Offset(cx, cy + ry), 1.5f)

            val center = Offset(cx, cy)
            val preRead = Offset(cx - rx * 0.78f, cy - ry * 0.65f)
            val endRead = Offset(cx + rx * 0.78f, cy - ry * 0.65f)
            val inverted = Offset(cx - rx * 0.78f, cy + ry * 0.65f)

            when (step) {
                0 -> drawCircle(AmberWarning, 7.dp.toPx(), center)
                1 -> {
                    drawLine(CyanBright.copy(alpha = 0.5f), center, preRead, 2f)
                    drawCircle(CyanBright, 7.dp.toPx(), preRead)
                }
                2 -> {
                    drawLine(CardSlateBorder, center, preRead, 1.5f)
                    drawLine(EmeraldSuccess, preRead, endRead, 3.dp.toPx())
                    drawCircle(EmeraldSuccess, 7.dp.toPx(), endRead)
                }
                else -> {
                    drawLine(CyanBright.copy(alpha = 0.45f), preRead, endRead, 2f)
                    drawLine(AmberWarning, endRead, inverted, 2.5f)
                    drawCircle(AmberWarning, 7.dp.toPx(), inverted)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(notes[step], fontSize = 11.sp, color = CyanBright)
    }
}

@Composable
private fun M04TrajectoryDanceSim() {
    var traj by remember { mutableIntStateOf(0) } // 0 Cartesian, 1 Radial, 2 Spiral, 3 EPI
    M04CardShell("C6. Jalur Pengisian Ruang-k (M04C.T3.7)", "Bandingkan lintasan pengisian ruang-k: Cartesian, Radial, Spiral, dan Echo-Planar (EPI).") {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Cartesian", "Radial", "Spiral", "EPI").forEachIndexed { idx, lbl ->
                FilterChip(selected = traj == idx, onClick = { traj = idx }, label = { Text(lbl, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.4f
            when (traj) {
                0 -> {
                    for (i in -3..3) {
                        val y = cy + i * (r / 3.5f)
                        drawLine(CyanBright, Offset(cx - r, y), Offset(cx + r, y), 2f)
                    }
                }
                1 -> {
                    for (i in 0 until 8) {
                        val a = i * PI.toFloat() / 8f
                        drawLine(EmeraldSuccess, Offset(cx - r * cos(a), cy - r * sin(a)), Offset(cx + r * cos(a), cy + r * sin(a)), 2f)
                    }
                }
                2 -> {
                    val p = Path()
                    for (k in 0..120) {
                        val t = k / 120f
                        val ang = t * 5f * 2f * PI.toFloat()
                        val rad = r * t
                        val x = cx + rad * cos(ang)
                        val y = cy + rad * sin(ang)
                        if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    drawPath(p, AmberWarning, style = Stroke(2f))
                }
                else -> {
                    val p = Path()
                    val rowStep = r / 3.5f
                    for (i in -3..3) {
                        val y = cy + i * rowStep
                        val left = cx - r
                        val right = cx + r
                        if (i == -3) p.moveTo(left, y)
                        if (i % 2 == 0) {
                            p.lineTo(right, y)
                            if (i < 3) p.lineTo(right, y + rowStep)
                        } else {
                            p.lineTo(left, y)
                            if (i < 3) p.lineTo(left, y + rowStep)
                        }
                    }
                    drawPath(p, CyanBright, style = Stroke(2f))
                }
            }
        }
    }
}

@Composable
private fun M04FillingOrderSim() {
    var order by remember { mutableIntStateOf(0) }
    M04CardShell("C4. Pilihan Urutan Pengisian Ruang-k (M04C.T3.5)", "Linear mengisi baris dari atas ke bawah; Centric memulai dari pusat ky=0 untuk kontras bolus cepat.") {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Linear", "Centric", "Partial Fourier", "Keyhole").forEachIndexed { idx, lbl ->
                FilterChip(selected = order == idx, onClick = { order = idx }, label = { Text(lbl, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (order) {
                0 -> "Linear: Baris diisi berurutan dari -kmax ke +kmax; TE efektif berada di tengah akuisisi."
                1 -> "Centric: Pusat ruang-k diisi paling awal sehingga ideal untuk MRA kontras bolus."
                2 -> "Partial Fourier: Sebagian baris negatif dilewati dengan memanfaatkan simetri konjugat."
                else -> "Keyhole: Hanya pusat ruang-k yang diperbarui berulang untuk pencitraan dinamis cepat."
            },
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun M04AcquisitionOrderSim() {
    var interleaved by remember { mutableStateOf(true) }
    M04CardShell("C5. Urutan Akuisisi Irisan: Sequential vs Interleaved (M04C.T3.6)", "Interleaved mengambil irisan ganjil lalu genap untuk mengurangi cross-talk antar-irisan berdekatan.") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !interleaved, onClick = { interleaved = false }, label = { Text("Sequential (1,2,3,4)", fontSize = 10.sp) })
            FilterChip(selected = interleaved, onClick = { interleaved = true }, label = { Text("Interleaved (1,3,5 | 2,4,6)", fontSize = 10.sp) })
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (interleaved) "Interleaved: Jarak antar-irisan yang dieksitasi berurutan lebih lebar → meminimalkan cross-excitation."
            else "Sequential: Irisan diambil berurutan 1→N; berisiko cross-talk jika celah irisan terlalu sempit.",
            fontSize = 11.sp,
            color = if (interleaved) EmeraldSuccess else AmberWarning
        )
    }
}
