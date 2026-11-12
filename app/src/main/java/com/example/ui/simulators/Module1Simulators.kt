package com.example.ui.simulators

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Daftar 14 simulasi & aktivitas interaktif Modul 1 (Fisika Dasar MRI)
 * hasil konversi dari `modul1/js/aktivitas/`.
 */
enum class M01SimulationType(
    val id: String,
    val shortCode: String,
    val title: String,
    val nodeTag: String,
    val subtitle: String
) {
    PENGANTAR_ATOM(
        id = "pengantar_atom",
        shortCode = "Pengantar",
        title = "Air ke Proton",
        nodeTag = "T1.3–T1.4",
        subtitle = "H₂O, atom H, spin & B₀"
    ),
    ATOM_HIDROGEN(
        id = "atom_h",
        shortCode = "T1.3",
        title = "Struktur Atom ¹H",
        nodeTag = "M01.T1.3",
        subtitle = "Isotop, ionisasi & awan 1s"
    ),
    CEK_INTI_AKTIF(
        id = "cek_inti",
        shortCode = "T1.4",
        title = "Inti Aktif MR",
        nodeTag = "M01.T1.4B",
        subtitle = "Cek Z, N & spin neto"
    ),
    POPULASI_SPIN(
        id = "populasi_spin",
        shortCode = "A1",
        title = "Populasi Spin & B₀",
        nodeTag = "M01.T1.5",
        subtitle = "48 proton & Boltzmann"
    ),
    KALKULATOR_LARMOR(
        id = "larmor",
        shortCode = "A2",
        title = "Kalkulator Larmor",
        nodeTag = "M01.T1.6",
        subtitle = "f = γ·B₀ multi-inti"
    ),
    TEBAK_LARMOR(
        id = "latihan_tebak_larmor",
        shortCode = "Latihan",
        title = "Tebak Larmor",
        nodeTag = "T1.6",
        subtitle = "Kuis hitung f = γ·B₀"
    ),
    PRESESI_BOLA(
        id = "presesi_bola",
        shortCode = "Presesi",
        title = "Presesi 3D & B₀",
        nodeTag = "T1.6",
        subtitle = "Kerucut 3D & grafik x(t)"
    ),
    SPEKTRUM_EM(
        id = "spektrum_em",
        shortCode = "T1.7",
        title = "Spektrum EM",
        nodeTag = "M01.T1.7",
        subtitle = "Radio–Gamma & MRI"
    ),
    NMV_3D(
        id = "nmv_3d",
        shortCode = "A4",
        title = "NMV 3D Lab/Rot",
        nodeTag = "M01.T2.2A",
        subtitle = "Resonansi RF & FID"
    ),
    SUDUT_FLIP(
        id = "sudut_flip",
        shortCode = "A3 Flip",
        title = "Sudut Flip θ",
        nodeTag = "M01.T2.2B",
        subtitle = "θ = γ·B₁·τ (90°/180°)"
    ),
    FASE_FID(
        id = "fase_fid",
        shortCode = "A3 FID",
        title = "Fase & Sinyal FID",
        nodeTag = "M01.T2.3A",
        subtitle = "8 spin & dephasing koil"
    ),
    BOLA_BLOCH(
        id = "bola_bloch",
        shortCode = "A5",
        title = "Bola Bloch (RK4)",
        nodeTag = "M01.T2.3B",
        subtitle = "Mz, |Mxy| & Mx(t)"
    ),
    TR_BERULANG(
        id = "tr_berulang",
        shortCode = "A6",
        title = "TR Pulsa Berulang",
        nodeTag = "M01.T2.4",
        subtitle = "Bobot T1, T2 & PD"
    ),
    TAMBAHAN_TR_TE(
        id = "tambahan_tr_te",
        shortCode = "TR/TE",
        title = "Urutan Pulsa TR/TE",
        nodeTag = "T2.4",
        subtitle = "Timeline pulsa & sinyal"
    );

    companion object {
        fun fromLabType(labType: String?): M01SimulationType = when (labType) {
            "atom_h" -> ATOM_HIDROGEN
            "cek_inti" -> CEK_INTI_AKTIF
            "populasi_spin" -> POPULASI_SPIN
            "larmor", "presesi" -> KALKULATOR_LARMOR
            "spektrum_em" -> SPEKTRUM_EM
            "nmv_3d" -> NMV_3D
            "sudut_flip" -> SUDUT_FLIP
            "fase_fid" -> FASE_FID
            "bola_bloch" -> BOLA_BLOCH
            "tr_berulang" -> TR_BERULANG
            "pengantar_atom" -> PENGANTAR_ATOM
            "presesi_bola" -> PRESESI_BOLA
            "latihan_tebak_larmor" -> TEBAK_LARMOR
            "tambahan_tr_te" -> TAMBAHAN_TR_TE
            else -> PENGANTAR_ATOM
        }
    }
}

private data class M01Nucleus(val symbol: String, val name: String, val spin: String, val gamma: Double)

private val M01_NUCLEI = listOf(
    M01Nucleus("1H", "Hidrogen", "1/2", 42.58),
    M01Nucleus("13C", "Karbon-13", "1/2", 10.7084),
    M01Nucleus("19F", "Fluor-19", "1/2", 40.0776),
    M01Nucleus("23Na", "Natrium-23", "3/2", 11.2686)
)

private data class M01Tissue(val id: String, val name: String, val t1: Double, val t2: Double, val pd: Double?)

private val M01_TISSUES = listOf(
    M01Tissue("csf", "CSF (Cairan Otak)", 4000.0, 2000.0, 1.0),
    M01Tissue("gm", "Substansia Grisea", 950.0, 100.0, 0.85),
    M01Tissue("wm", "Substansia Alba", 600.0, 80.0, 0.72),
    M01Tissue("fat", "Lemak", 250.0, 70.0, 0.90),
    M01Tissue("muscle", "Otot", 900.0, 50.0, 0.80)
)

private fun scaleRelaxation(tissue: M01Tissue, b0: Float): Pair<Double, Double> {
    val factor = if (b0 >= 2.5f) 1.32 else 1.0
    return Pair((tissue.t1 * factor).roundToInt().toDouble(), tissue.t2)
}

private fun formatId(value: Double, decimals: Int = 2): String =
    "%.${decimals}f".format(value).replace('.', ',')

private fun projectSphere(
    cx: Float,
    cy: Float,
    r: Float,
    az: Float,
    el: Float,
    x: Float,
    y: Float,
    z: Float
): Offset {
    val ca = cos(az)
    val sa = sin(az)
    val ce = cos(el)
    val se = sin(el)
    val x1 = x * ca - y * sa
    val y1 = x * sa + y * ca
    val y2 = y1 * se + z * ce
    return Offset(cx + x1 * r, cy - y2 * r)
}

private fun DrawScope.drawSphereWireframe(cx: Float, cy: Float, r: Float, az: Float, el: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E3A5F).copy(alpha = 0.55f), Color(0xFF0A1424).copy(alpha = 0.85f)),
            center = Offset(cx - r * 0.28f, cy - r * 0.28f),
            radius = r * 1.25f
        ),
        radius = r,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = CardSlateBorder,
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    // Equator ellipse
    val eqPath = Path()
    for (k in 0..48) {
        val q = (k / 48.0 * 2.0 * PI).toFloat()
        val p = projectSphere(cx, cy, r, az, el, cos(q), sin(q), 0f)
        if (k == 0) eqPath.moveTo(p.x, p.y) else eqPath.lineTo(p.x, p.y)
    }
    drawPath(eqPath, color = CyanPrimary.copy(alpha = 0.35f), style = Stroke(width = 1.3f))
}

private fun DrawScope.drawArrowLine(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float = 3f,
    headSize: Float = 10f,
    dashed: Boolean = false
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f) else null
    )
    val angle = atan2(end.y - start.y, end.x - start.x)
    val p1 = end + Offset(cos(angle) * headSize * 0.4f, sin(angle) * headSize * 0.4f)
    val p2 = end + Offset(cos(angle + 2.5f) * headSize, sin(angle + 2.5f) * headSize)
    val p3 = end + Offset(cos(angle - 2.5f) * headSize, sin(angle - 2.5f) * headSize)
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path, color = color)
}

/**
 * Hub Laboratorium M01 untuk Laboratorium Virtual MRI.
 * Menampilkan Kartu Induk M01 + grid sub-kartu kecil di bawahnya + simulasi aktif.
 */
@Composable
fun Module1LabHub(
    selectedActivityId: String,
    onSelectActivity: (String) -> Unit
) {
    val activeType = M01SimulationType.fromLabType(selectedActivityId)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Kartu Induk M01 (sesuai gaya kartu Laboratorium MRI)
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
                            imageVector = Icons.Default.Science,
                            contentDescription = "M01",
                            tint = CyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "M01: Laboratorium Fisika Dasar MRI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "14 simulasi interaktif proton, Larmor, RF, FID, Bloch & TR/TE terhubung ke simpul M01.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-kartu kecil di bawah kartu M01 (2 kolom)
                val rows = M01SimulationType.entries.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { item ->
                                val isSelected = item == activeType
                                Surface(
                                    onClick = { onSelectActivity(item.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CyanDark else DeepObsidian,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) CyanBright else CardSlateBorder
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.shortCode,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) CyanBright else AmberWarning,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = item.nodeTag,
                                                fontSize = 9.sp,
                                                color = TextMuted
                                            )
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
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Simulasi Aktif
        when (activeType) {
            M01SimulationType.PENGANTAR_ATOM -> M01PengantarAtomSim()
            M01SimulationType.ATOM_HIDROGEN -> M01AtomHidrogenSim()
            M01SimulationType.CEK_INTI_AKTIF -> M01CekIntiAktifSim()
            M01SimulationType.POPULASI_SPIN -> M01PopulasiSpinSim()
            M01SimulationType.KALKULATOR_LARMOR -> M01KalkulatorLarmorSim()
            M01SimulationType.TEBAK_LARMOR -> M01TebakLarmorSim()
            M01SimulationType.PRESESI_BOLA -> M01PresesiBolaSim()
            M01SimulationType.SPEKTRUM_EM -> M01SpektrumEmSim()
            M01SimulationType.NMV_3D -> M01Nmv3dSim()
            M01SimulationType.SUDUT_FLIP -> M01SudutFlipSim()
            M01SimulationType.FASE_FID -> M01FaseFidSim()
            M01SimulationType.BOLA_BLOCH -> M01BolaBlochSim()
            M01SimulationType.TR_BERULANG -> M01TrBerulangSim()
            M01SimulationType.TAMBAHAN_TR_TE -> M01TambahanTrTeSim()
        }
    }
}

@Composable
private fun M01SimCardShell(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
        border = BorderStroke(1.dp, CardSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanBright)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun M01PlayResetRow(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onReset: () -> Unit,
    extraContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onTogglePlay,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = CyanBright,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isPlaying) "Jeda" else "Putar", fontSize = 11.sp, color = CyanBright)
        }
        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardSlateBorder)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", fontSize = 11.sp, color = TextSecondary)
        }
        extraContent()
    }
}

// 1. Pengantar: dari air ke proton
@Composable
private fun M01PengantarAtomSim() {
    var step by remember { mutableIntStateOf(0) }
    var depthMode by remember { mutableIntStateOf(0) } // 0 singkat, 1 lengkap, 2 analogi
    var b0On by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var simTime by remember { mutableFloatStateOf(0f) }
    var kAlign by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, b0On) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    simTime += dt
                    val target = if (b0On && step == 3) 1f else 0f
                    kAlign += (target - kAlign) * 0.08f
                }
                lastNanos = nanos
            }
        }
    }

    val titles = listOf("Molekul air", "Atom hidrogen", "Proton berspin", "Proton di dalam B₀")
    val singkat = listOf(
        "Tubuh banyak mengandung air dan lemak. Satu molekul air (H₂O) punya dua atom hidrogen (H) sebagai sumber sinyal MRI.",
        "Atom hidrogen paling sederhana: satu proton di inti dan satu elektron yang membentuk awan peluang di sekelilingnya.",
        "Proton punya sifat spin dan bermuatan positif sehingga berperilaku seperti magnet kecil dengan kutub U dan S.",
        "Tanpa B₀ arah spin acak. Saat B₀ dinyalakan, proton cenderung menyearah dan berpresesi mengelilingi arah B₀."
    )
    val lengkap = listOf(
        "Selain di air, hidrogen melimpah di lemak bersama karbon dan oksigen.",
        "Inti memuat hampir seluruh massa atom, jauh lebih kecil daripada awan elektronnya.",
        "Inti bernomor massa ganjil seperti ¹H punya spin neto dan menghasilkan momen magnetik.",
        "Kelebihan spin searah B₀ sebenarnya sangat kecil (~orde per sejuta), di sini disederhanakan agar mudah diamati."
    )
    val analogi = listOf(
        "Tubuh seperti kota besar penuh penduduk H; MRI mendengarkan suara penduduk H tersebut.",
        "Inti ibarat kelereng di tengah stadion, sedangkan elektron seperti baling-baling kipas yang tampak kabur.",
        "Proton ibarat gasing bermuatan: muatan yang berputar menghasilkan medan magnet mini.",
        "Magnet kecil seperti jarum kompas yang bergoyang (berpresesi) mengelilingi medan besar B₀."
    )

    M01SimCardShell(
        title = "Pengantar: Dari Air ke Proton (T1.3, T1.4)",
        subtitle = "Langkah ${step + 1} dari 4: ${titles[step]}"
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Singkat", "Lengkap", "Analogi").forEachIndexed { idx, label ->
                FilterChip(
                    selected = depthMode == idx,
                    onClick = { depthMode = idx },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanDark,
                        selectedLabelColor = CyanBright
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val t = simTime
            when (step) {
                0 -> {
                    val g = sin(t * 5f) * 3f
                    drawLine(TextMuted, Offset(cx, cy - 22f), Offset(cx - 68f - g, cy + 32f), strokeWidth = 6f)
                    drawLine(TextMuted, Offset(cx, cy - 22f), Offset(cx + 68f + g, cy + 32f), strokeWidth = 6f)
                    drawCircle(CyanPrimary, radius = 30f, center = Offset(cx, cy - 22f))
                    val pulse = 0.5f + 0.4f * sin(t * 3f)
                    listOf(-1f, 1f).forEach { s ->
                        val p = Offset(cx + s * (68f + g), cy + 32f)
                        drawCircle(AmberWarning.copy(alpha = pulse), radius = 24f, center = p, style = Stroke(2f))
                        drawCircle(AmberWarning, radius = 16f, center = p)
                    }
                }
                1 -> {
                    drawCircle(CyanPrimary.copy(alpha = 0.14f), radius = 84f, center = Offset(cx, cy))
                    drawCircle(CyanPrimary.copy(alpha = 0.18f), radius = 52f, center = Offset(cx, cy))
                    for (k in 6 downTo 0) {
                        val a = t * 3.8f - k * 0.22f
                        val r = 56f + 22f * sin(a * 0.35f)
                        val ex = cx + r * cos(a)
                        val ey = cy + r * 0.78f * sin(a)
                        drawCircle(
                            color = EmeraldSuccess.copy(alpha = if (k == 0) 1f else 0.45f - k * 0.05f),
                            radius = if (k == 0) 7f else 4f,
                            center = Offset(ex, ey)
                        )
                    }
                    drawCircle(AmberWarning, radius = 13f, center = Offset(cx, cy))
                }
                2 -> {
                    val px = cx - 70f
                    drawCircle(AmberWarning, radius = 48f, center = Offset(px, cy))
                    drawOval(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = Offset(px - 48f, cy - 13f),
                        size = Size(96f, 26f),
                        style = Stroke(2f)
                    )
                    val a = t * 3f
                    drawCircle(Color.White, radius = 6f, center = Offset(px + 48f * cos(a), cy + 13f * sin(a)))
                    drawArrowLine(Offset(px, cy + 75f), Offset(px, cy - 75f), TextPrimary, 3f)
                    // Bar magnet
                    drawRect(AmberWarning, topLeft = Offset(cx + 60f, cy - 42f), size = Size(34f, 42f))
                    drawRect(CyanPrimary, topLeft = Offset(cx + 60f, cy), size = Size(34f, 42f))
                }
                else -> {
                    if (kAlign > 0.05f) {
                        drawArrowLine(
                            Offset(32f, size.height - 26f),
                            Offset(32f, 24f),
                            CyanBright.copy(alpha = 0.35f + 0.65f * kAlign),
                            strokeWidth = 4f
                        )
                    }
                    val alphas = listOf(-2.1f, 0.8f, 2.5f, -0.7f, 1.6f)
                    for (i in 0 until 5) {
                        val x = size.width * (i + 1) / 6f + 12f
                        val a = alphas[i] + 0.3f * sin(t * 1.5f + i)
                        val ox = sin(a)
                        val oy = -cos(a)
                        val tilt = 0.48f
                        val ph = t * 4f + i * 1.3f
                        val nx = sin(tilt) * cos(ph)
                        val ny = -cos(tilt) + 0.15f * sin(tilt) * sin(ph)
                        val dx = (1f - kAlign) * ox + kAlign * nx
                        val dy = (1f - kAlign) * oy + kAlign * ny
                        drawCircle(AmberWarning, radius = 14f, center = Offset(x, cy))
                        drawArrowLine(Offset(x - dx * 26f, cy - dy * 26f), Offset(x + dx * 26f, cy + dy * 26f), Color.White, 2.5f, 7f)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (depthMode) {
                1 -> "${singkat[step]} ${lengkap[step]}"
                2 -> "${singkat[step]}\nAnalogi: ${analogi[step]}"
                else -> singkat[step]
            },
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { if (step > 0) step-- },
                enabled = step > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("← Sebelumnya", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { if (step < 3) step++ },
                enabled = step < 3,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Berikutnya →", fontSize = 11.sp)
            }
        }
        if (step == 3) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { b0On = !b0On },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (b0On) EmeraldSuccess else CyanPrimary)
            ) {
                Text(if (b0On) "Matikan B₀" else "Nyalakan B₀", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { step = 0; b0On = false; kAlign = 0f; isPlaying = true }
        )
    }
}

// 2. Struktur Atom Hidrogen (T1.3)
@Composable
private fun M01AtomHidrogenSim() {
    var neutrons by remember { mutableIntStateOf(0) }
    var ionized by remember { mutableStateOf(false) }
    var cloudMode by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var angle by remember { mutableFloatStateOf(0f) }
    var ionProgress by remember { mutableFloatStateOf(0f) }
    var selectedInfo by remember { mutableStateOf("Hidrogen-1 (protium): 1 proton, 0 neutron, 1 elektron. Inilah inti utama MRI klinis.") }
    val cloudDots = remember {
        val rng = Random(42)
        List(110) {
            val r = min(130f, 23f * (-ln(max(1e-4f, rng.nextFloat() * rng.nextFloat() * rng.nextFloat()))))
            val phi = rng.nextFloat() * 2f * PI.toFloat()
            Offset(r * cos(phi), r * sin(phi))
        }
    }

    LaunchedEffect(isPlaying, ionized) {
        var lastNanos = 0L
        while (isPlaying || abs((if (ionized) 1f else 0f) - ionProgress) > 0.01f) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    if (isPlaying) angle = (angle + dt * 2.4f) % (2f * PI.toFloat())
                    val target = if (ionized) 1f else 0f
                    ionProgress += (target - ionProgress) * min(1f, dt * 5f)
                }
                lastNanos = nanos
            }
        }
    }

    M01SimCardShell(
        title = "Struktur Atom Hidrogen (M01.T1.3)",
        subtitle = "Pilih isotop (¹H, ²H, ³H), ionisasi elektron, atau ganti model orbit ke awan elektron."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Hidrogen-1", "Deuterium (²H)", "Tritium (³H)").forEachIndexed { idx, label ->
                FilterChip(
                    selected = neutrons == idx,
                    onClick = {
                        neutrons = idx
                        selectedInfo = when (idx) {
                            0 -> "Hidrogen-1 (protium): 1 proton, 0 neutron, nomor massa A = 1."
                            1 -> "Hidrogen-2 (deuterium): 1 proton + 1 neutron, A = 2."
                            else -> "Hidrogen-3 (tritium): 1 proton + 2 neutron, A = 3."
                        }
                    },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanDark, selectedLabelColor = CyanBright)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val cx = size.width * 0.48f
            val cy = size.height / 2f
            val r1 = 62f
            val r2 = 96f
            if (!cloudMode) {
                drawCircle(TextMuted.copy(alpha = 0.7f), radius = r1, center = Offset(cx, cy), style = Stroke(1.5f))
                drawCircle(
                    TextMuted.copy(alpha = 0.35f),
                    radius = r2,
                    center = Offset(cx, cy),
                    style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                )
            } else {
                val alpha = 1f - ionProgress
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(EmeraldSuccess.copy(alpha = 0.32f * alpha), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = 105f
                    ),
                    radius = 105f,
                    center = Offset(cx, cy)
                )
                cloudDots.forEach { d ->
                    drawCircle(EmeraldSuccess.copy(alpha = 0.8f * alpha), radius = 2.2f, center = Offset(cx + d.x, cy + d.y))
                }
            }
            // Nucleus
            val offsets = when (neutrons) {
                0 -> listOf(Offset.Zero)
                1 -> listOf(Offset(-10f, 3f), Offset(10f, -3f))
                else -> listOf(Offset(-11f, 8f), Offset(11f, 8f), Offset(0f, -11f))
            }
            for (i in offsets.indices.reversed()) {
                val color = if (i == 0) CyanBright else RoseDanger
                drawCircle(color, radius = 13f, center = Offset(cx + offsets[i].x, cy + offsets[i].y))
            }
            // Electron
            if (!cloudMode && ionProgress < 0.98f) {
                val rr = r1 + ionProgress * ionProgress * 160f
                val alpha = 1f - ionProgress
                val ex = cx + rr * cos(angle)
                val ey = cy + rr * sin(angle)
                drawCircle(EmeraldSuccess.copy(alpha = alpha), radius = 9f, center = Offset(ex, ey))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val massA = 1 + neutrons
        val charge = if (ionized) "+1 (Ion H⁺)" else "0 (Atom netral)"
        Text(
            text = "Z = 1 | Neutron = $neutrons | A = $massA | Elektron = ${if (ionized) 0 else 1} | Muatan = $charge",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyanBright
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = selectedInfo, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { ionized = !ionized }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                Text(if (ionized) "Pasang Elektron" else "Ionisasi (H⁺)", fontSize = 11.sp)
            }
            OutlinedButton(onClick = { cloudMode = !cloudMode }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                Text(if (cloudMode) "Mode: Awan 1s" else "Mode: Orbit", fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { neutrons = 0; ionized = false; cloudMode = false; ionProgress = 0f; isPlaying = true }
        )
    }
}

// 3. Cek Inti Aktif MR (T1.4)
@Composable
private fun M01CekIntiAktifSim() {
    val presets = remember {
        listOf(
            Triple("¹H", 1, 0),
            Triple("¹²C", 6, 6),
            Triple("¹³C", 6, 7),
            Triple("¹⁴N", 7, 7),
            Triple("¹⁹F", 9, 10),
            Triple("²³Na", 11, 12)
        )
    }
    var protons by remember { mutableIntStateOf(1) }
    var neutrons by remember { mutableIntStateOf(0) }

    val massA = protons + neutrons
    val isActive = massA % 2 == 1
    val statusText = when {
        protons % 2 == 0 && neutrons % 2 == 0 ->
            "Nomor massa A = $massA. Proton & neutron sama-sama genap → spin saling meniadakan. TIDAK aktif MR."
        massA % 2 == 1 ->
            "Nomor massa A = $massA (ganjil) → ada spin neto setengah-bulat & momen magnetik. AKTIF MR."
        else ->
            "Nomor massa A = $massA (ganjil-ganjil) → spin neto bulat, sinyal sangat lemah sehingga tidak dipakai klinis."
    }

    M01SimCardShell(
        title = "Cek Inti Aktif MR (M01.T1.4B)",
        subtitle = "Pilih inti atau ubah jumlah proton (Z) dan neutron (N) untuk menguji syarat spin neto MRI."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { (label, z, n) ->
                FilterChip(
                    selected = protons == z && neutrons == n,
                    onClick = { protons = z; neutrons = n },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanDark, selectedLabelColor = CyanBright)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Proton (Z): $protons", fontSize = 11.sp, color = TextPrimary)
        Slider(
            value = protons.toFloat(),
            onValueChange = { protons = it.roundToInt() },
            valueRange = 1f..20f,
            colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
        )
        Text("Neutron (N): $neutrons", fontSize = 11.sp, color = TextPrimary)
        Slider(
            value = neutrons.toFloat(),
            onValueChange = { neutrons = it.roundToInt() },
            valueRange = 0f..20f,
            colors = SliderDefaults.colors(thumbColor = AmberWarning, activeTrackColor = AmberWarning)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isActive) EmeraldSuccess.copy(alpha = 0.14f) else RoseDanger.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, if (isActive) EmeraldSuccess else RoseDanger)
        ) {
            Text(
                text = statusText,
                fontSize = 11.sp,
                color = if (isActive) EmeraldSuccess else RoseDanger,
                modifier = Modifier.padding(10.dp),
                lineHeight = 16.sp
            )
        }
    }
}

// 4. A1 Populasi Spin & B0 (T1.5, T3.1)
@Composable
private fun M01PopulasiSpinSim() {
    var b0 by remember { mutableFloatStateOf(1.5f) }
    var boltzmannMode by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var simTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    simTime += min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                }
                lastNanos = nanos
            }
        }
    }

    val totalSpins = 48
    val upCount = (totalSpins * (0.5f + 0.4f * b0 / 3f)).roundToInt()
    val netDiff = upCount - (totalSpins - upCount)
    val netFrac = (2 * upCount - totalSpins).toFloat() / totalSpins.toFloat()

    M01SimCardShell(
        title = "A1. Populasi Spin & B₀ (M01.T1.5, T3.1)",
        subtitle = "Proton biru searah B₀ (energi rendah) vs abu-abu berlawanan B₀. Selisih bersih menghasilkan NMV."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val cols = 12
            val rows = 4
            val sx = size.width / cols
            val sy = size.height / rows
            val r = sy * 0.19f
            val len = sy * 0.36f
            drawArrowLine(
                Offset(size.width / 2f, size.height - 16f),
                Offset(size.width / 2f, 18f),
                CyanBright.copy(alpha = 0.25f),
                strokeWidth = 7f,
                headSize = 18f,
                dashed = true
            )
            for (n in 0 until totalSpins) {
                val col = n % cols
                val row = n / cols
                val cx = sx * (col + 0.5f)
                val cy = sy * (row + 0.5f)
                val isUp = n < upCount
                val dir = if (isUp) 1f else -1f
                val wob = 0.35f * sin(simTime * 3.5f + n * 1.4f)
                val dx = len * sin(wob)
                val dy = dir * len * cos(wob)
                drawCircle(if (isUp) CyanPrimary else TextMuted, radius = r, center = Offset(cx, cy))
                drawArrowLine(Offset(cx - dx, cy + dy), Offset(cx + dx, cy - dy), Color.White, 1.8f, 5f)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("B₀: ${formatId(b0.toDouble(), 1)} T", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
        Slider(
            value = b0,
            onValueChange = { b0 = (it * 10).roundToInt() / 10f },
            valueRange = 0.5f..3.0f,
            colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
        )
        Text("Searah B₀: $upCount dari $totalSpins | Selisih bersih: $netDiff", fontSize = 11.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { netFrac.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = CyanBright,
            trackColor = DeepObsidian
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = boltzmannMode, onCheckedChange = { boltzmannMode = it })
            Text("Mode Boltzmann (T = 310 K)", fontSize = 11.sp, color = TextSecondary)
        }
        if (boltzmannMode) {
            val dE = 6.62607015e-34 * 42.58e6 * b0
            val kT = 1.380649e-23 * 310.0
            val ratio = exp(-dE / kT)
            val excessPpm = (1.0 - ratio) / (1.0 + ratio) * 1e6
            Text(
                text = "ΔE = hγB₀ | N⁺/N⁻ = ${"%.8f".format(ratio).replace('.', ',')} | Kelebihan spin rendah ≈ ${formatId(excessPpm, 1)} per sejuta",
                fontSize = 10.sp,
                color = AmberWarning,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { b0 = 1.5f; boltzmannMode = false; isPlaying = true }
        )
    }
}

// 5. A2 Kalkulator Larmor (T1.6, T3.2)
@Composable
private fun M01KalkulatorLarmorSim() {
    var selectedIdx by remember { mutableIntStateOf(0) }
    var b0 by remember { mutableFloatStateOf(1.5f) }
    val nuc = M01_NUCLEI[selectedIdx]
    val freq = nuc.gamma * b0

    M01SimCardShell(
        title = "A2. Kalkulator Larmor (M01.T1.6, T3.2)",
        subtitle = "Hitung frekuensi presesi f₀ = γ·B₀ untuk inti aktif MR."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            M01_NUCLEI.forEachIndexed { idx, item ->
                FilterChip(
                    selected = selectedIdx == idx,
                    onClick = { selectedIdx = idx },
                    label = { Text("${item.symbol} (${item.name})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanDark, selectedLabelColor = CyanBright)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("B₀: ${formatId(b0.toDouble(), 1)} T", fontSize = 11.sp, color = CyanBright, fontWeight = FontWeight.Bold)
        Slider(
            value = b0,
            onValueChange = { b0 = (it * 10).roundToInt() / 10f },
            valueRange = 0.5f..3.0f,
            colors = SliderDefaults.colors(thumbColor = CyanBright, activeTrackColor = CyanPrimary)
        )
        Text(
            text = "f₀ = ${formatId(freq, 2)} MHz | Spin = ${nuc.spin} | γ = ${formatId(nuc.gamma, 2)} MHz/T",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldSuccess
        )
        Spacer(modifier = Modifier.height(8.dp))
        val fMax = 42.58 * 3.0
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            M01_NUCLEI.forEachIndexed { idx, item ->
                val f = item.gamma * b0
                val frac = (f / fMax).toFloat().coerceIn(0.03f, 1f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedIdx = idx }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.symbol, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.width(38.dp), fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepObsidian)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(frac)
                                .height(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (idx == selectedIdx) CyanPrimary else CardSlateBorder)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${formatId(f, 1)} MHz", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(68.dp))
                }
            }
        }
    }
}

// 6. Latihan Tebak Frekuensi Larmor (T1.6)
@Composable
private fun M01TebakLarmorSim() {
    var targetB0 by remember { mutableFloatStateOf(1.5f) }
    var answerText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var scoreCorrect by remember { mutableIntStateOf(0) }
    var scoreTotal by remember { mutableIntStateOf(0) }

    fun newQuestion() {
        targetB0 = ((5 + Random.nextInt(26)) / 10f)
        answerText = ""
        feedback = null
    }

    M01SimCardShell(
        title = "Latihan: Tebak Frekuensi Larmor ¹H (T1.6)",
        subtitle = "Hitung f₀ = 42,58 × B₀ (toleransi ±0,5 MHz)."
    ) {
        Text(
            text = "Diketahui B₀ = ${formatId(targetB0.toDouble(), 1)} T (γ ¹H = 42,58 MHz/T)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanBright
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = answerText,
            onValueChange = { answerText = it },
            label = { Text("Jawaban (MHz)", fontSize = 11.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val v = answerText.replace(',', '.').toDoubleOrNull()
                    if (v == null) {
                        feedback = Pair(false, "Isi angka jawaban terlebih dahulu.")
                    } else {
                        val expected = 42.58 * targetB0
                        val ok = abs(v - expected) <= 0.5
                        scoreTotal++
                        if (ok) scoreCorrect++
                        feedback = Pair(ok, "${if (ok) "Benar!" else "Belum tepat."} f₀ = 42,58 × ${formatId(targetB0.toDouble(), 1)} = ${formatId(expected, 2)} MHz.")
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Periksa", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            OutlinedButton(onClick = { newQuestion() }, modifier = Modifier.weight(1f)) {
                Text("Soal Baru", fontSize = 11.sp)
            }
        }
        feedback?.let { (ok, msg) ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(msg, fontSize = 11.sp, color = if (ok) EmeraldSuccess else RoseDanger)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Skor: $scoreCorrect / $scoreTotal", fontSize = 11.sp, color = TextMuted)
    }
}

// 7. Presesi Mengelilingi B0 (T1.6)
@Composable
private fun M01PresesiBolaSim() {
    var nucIdx by remember { mutableIntStateOf(0) }
    var b0 by remember { mutableFloatStateOf(1.5f) }
    var tiltDeg by remember { mutableFloatStateOf(35f) }
    var spinUp by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var az by remember { mutableFloatStateOf(0.5f) }
    var el by remember { mutableFloatStateOf(0.35f) }
    var phi by remember { mutableFloatStateOf(0f) }
    val hist = remember { mutableStateListOf<Float>() }

    val f0 = M01_NUCLEI[nucIdx].gamma * b0

    LaunchedEffect(isPlaying, nucIdx, b0) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    phi = (phi + 2f * PI.toFloat() * (f0.toFloat() * 0.012f) * dt) % (2f * PI.toFloat())
                    val xComp = sin(tiltDeg * PI.toFloat() / 180f) * cos(phi)
                    hist.add(xComp)
                    if (hist.size > 140) hist.removeAt(0)
                }
                lastNanos = nanos
            }
        }
    }

    M01SimCardShell(
        title = "Presesi Mengelilingi B₀ (T1.6)",
        subtitle = "Seret bola 3D untuk memutar pandangan. Sudut miring mengubah lebar kerucut, sedangkan B₀ & γ mengubah frekuensi."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        az -= drag.x * 0.01f
                        el = (el + drag.y * 0.01f).coerceIn(-0.4f, 1.3f)
                    }
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.36f
            drawSphereWireframe(cx, cy, r, az, el)
            val b0Start = projectSphere(cx, cy, r, az, el, 0f, 0f, -1.05f)
            val b0End = projectSphere(cx, cy, r, az, el, 0f, 0f, 1.25f)
            drawArrowLine(b0Start, b0End, CyanBright, 3f)

            val th = tiltDeg * PI.toFloat() / 180f
            val zDir = if (spinUp) 1f else -1f
            val conePath = Path()
            for (k in 0..48) {
                val q = (k / 48.0 * 2.0 * PI).toFloat()
                val p = projectSphere(cx, cy, r, az, el, sin(th) * cos(q), sin(th) * sin(q), zDir * cos(th))
                if (k == 0) conePath.moveTo(p.x, p.y) else conePath.lineTo(p.x, p.y)
            }
            drawPath(conePath, color = AmberWarning.copy(alpha = 0.65f), style = Stroke(2f))
            val tip = projectSphere(cx, cy, r, az, el, sin(th) * cos(phi), sin(th) * sin(phi), zDir * cos(th))
            drawArrowLine(Offset(cx, cy), tip, AmberWarning, 3.5f)
            val projXy = projectSphere(cx, cy, r, az, el, sin(th) * cos(phi), sin(th) * sin(phi), 0f)
            drawLine(EmeraldSuccess, Offset(cx, cy), projXy, strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Grafik komponen x(t)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val mid = size.height / 2f
            drawLine(CardSlateBorder, Offset(0f, mid), Offset(size.width, mid), 1f)
            if (hist.size > 1) {
                val path = Path()
                hist.forEachIndexed { idx, v ->
                    val x = idx.toFloat() / 139f * size.width
                    val y = mid - v * (size.height * 0.4f)
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = EmeraldSuccess, style = Stroke(2f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = spinUp, onClick = { spinUp = true }, label = { Text("Spin Atas", fontSize = 10.sp) })
            FilterChip(selected = !spinUp, onClick = { spinUp = false }, label = { Text("Spin Bawah", fontSize = 10.sp) })
        }
        Text("B₀: ${formatId(b0.toDouble(), 1)} T | Sudut: ${tiltDeg.roundToInt()}° | f₀ = ${formatId(f0, 2)} MHz", fontSize = 11.sp, color = CyanBright)
        Slider(value = b0, onValueChange = { b0 = (it * 10).roundToInt() / 10f }, valueRange = 0.5f..3.0f)
        Slider(value = tiltDeg, onValueChange = { tiltDeg = it }, valueRange = 10f..90f)
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { b0 = 1.5f; tiltDeg = 35f; spinUp = true; az = 0.5f; el = 0.35f; hist.clear() }
        )
    }
}

// 8. Peta Spektrum EM (T1.7)
@Composable
private fun M01SpektrumEmSim() {
    data class EmBand(val name: String, val logMin: Float, val logMax: Float, val color: Color, val use: String, val hazard: String)
    val bands = remember {
        listOf(
            EmBand("Radio", 4f, 8.5f, Color(0xFF2F6BFF), "Telekomunikasi, radar, MRI", "Gangguan implan aktif"),
            EmBand("Gelombang Mikro", 8.5f, 12f, Color(0xFF0B7285), "Telekomunikasi & memasak", "Luka bakar termal"),
            EmBand("Inframerah", 12f, 14.6f, Color(0xFFE8590C), "Pemanasan & sensor", "Luka bakar"),
            EmBand("Cahaya Tampak", 14.6f, 14.9f, Color(0xFF40C057), "Fotografi & optik", "Kerusakan retina"),
            EmBand("Ultraviolet", 14.9f, 16f, Color(0xFF7048E8), "Sterilisasi / penyamak", "Luka bakar & kulit"),
            EmBand("Sinar-X", 16f, 18f, Color(0xFFC92A2A), "Radiografi & CT-Scan", "Efek deterministik & stokastik"),
            EmBand("Sinar Gamma", 18f, 20f, Color(0xFF862E9C), "Radioterapi & PET", "Efek deterministik & stokastik")
        )
    }
    var logFreq by remember { mutableFloatStateOf(7.8f) }
    var b0 by remember { mutableFloatStateOf(1.5f) }

    val activeBand = bands.firstOrNull { logFreq >= it.logMin && logFreq < it.logMax } ?: bands.last()
    val mriLogFreq = log10(42.58 * b0 * 1e6).toFloat()

    M01SimCardShell(
        title = "Peta Spektrum Elektromagnetik (M01.T1.7)",
        subtitle = "MRI bekerja di pita Gelombang Radio (non-pengion), jauh di bawah batas pengion 10¹⁶ Hz."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        logFreq = (4f + (offset.x / size.width) * 16f).coerceIn(4f, 20f)
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            fun xOf(lf: Float) = ((lf - 4f) / 16f) * w
            // Non-ionizing vs ionizing bar
            drawRect(EmeraldSuccess.copy(alpha = 0.25f), Offset.Zero, Size(xOf(16f), 22f))
            drawRect(RoseDanger.copy(alpha = 0.25f), Offset(xOf(16f), 0f), Size(w - xOf(16f), 22f))
            // Spectrum strip
            drawRect(
                brush = Brush.horizontalGradient(bands.map { it.color }),
                topLeft = Offset(0f, 32f),
                size = Size(w, 26f)
            )
            // Ionizing boundary
            drawLine(TextPrimary, Offset(xOf(16f), 0f), Offset(xOf(16f), h), 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f))
            // MRI marker
            val xm = xOf(mriLogFreq)
            drawCircle(CyanBright, radius = 6f, center = Offset(xm, 74f))
            // Cursor marker
            val xs = xOf(logFreq)
            drawLine(AmberWarning, Offset(xs, 26f), Offset(xs, h - 8f), 2.5f)
            drawCircle(AmberWarning, radius = 7f, center = Offset(xs, 74f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Frekuensi: 10^${formatId(logFreq.toDouble(), 1)} Hz", fontSize = 11.sp, color = AmberWarning)
        Slider(value = logFreq, onValueChange = { logFreq = it }, valueRange = 4f..20f)
        Text("B₀ MRI: ${formatId(b0.toDouble(), 1)} T (f₀ ≈ 10^${formatId(mriLogFreq.toDouble(), 2)} Hz)", fontSize = 11.sp, color = CyanBright)
        Slider(value = b0, onValueChange = { b0 = (it * 10).roundToInt() / 10f }, valueRange = 0.5f..3.0f)
        Text(
            text = "Pita: ${activeBand.name} (${if (logFreq >= 16f) "Pengion" else "Non-pengion"})\nPemakaian: ${activeBand.use} | Bahaya: ${activeBand.hazard}",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(onClick = { logFreq = mriLogFreq }, shape = RoundedCornerShape(10.dp)) {
            Text("Lompat ke Frekuensi MRI (${formatId(42.58 * b0, 1)} MHz)", fontSize = 11.sp)
        }
    }
}

// 9. A4 NMV 3D: Kerangka Lab vs Berputar (T1.6, T2.2, T3.5)
@Composable
private fun M01Nmv3dSim() {
    var b0 by remember { mutableFloatStateOf(1.5f) }
    var rfFreq by remember { mutableFloatStateOf(63.9f) }
    var rotatingFrame by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var az by remember { mutableFloatStateOf(0.5f) }
    var el by remember { mutableFloatStateOf(0.35f) }
    var phi by remember { mutableFloatStateOf(0f) }
    var thetaDeg by remember { mutableFloatStateOf(0f) }
    var targetThetaDeg by remember { mutableFloatStateOf(0f) }
    var fidTime by remember { mutableFloatStateOf(0f) }
    var hasFid by remember { mutableStateOf(false) }

    val f0 = 42.58f * b0
    val isResonant = abs(rfFreq - f0) <= 0.5f

    LaunchedEffect(isPlaying, b0, rfFreq, rotatingFrame) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    val omega = if (rotatingFrame) (f0 - rfFreq) else f0
                    phi = (phi + 2f * PI.toFloat() * omega * 0.02f * dt) % (2f * PI.toFloat())
                    thetaDeg += (targetThetaDeg - thetaDeg) * min(1f, dt * 5f)
                    if (hasFid) {
                        fidTime += dt
                        if (fidTime > 2.2f) {
                            hasFid = false
                            targetThetaDeg = 0f
                        }
                    }
                }
                lastNanos = nanos
            }
        }
    }

    M01SimCardShell(
        title = "A4. NMV 3D: Kerangka Lab vs Berputar (M01.T2.2A)",
        subtitle = "Samakan frekuensi RF dengan Larmor agar terjadi resonansi, lalu kirim pulsa 90° atau 180°."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        az -= drag.x * 0.01f
                        el = (el + drag.y * 0.01f).coerceIn(-0.3f, 1.3f)
                    }
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.36f
            drawSphereWireframe(cx, cy, r, az, el)
            drawArrowLine(projectSphere(cx, cy, r, az, el, 0f, 0f, -1f), projectSphere(cx, cy, r, az, el, 0f, 0f, 1.25f), CyanBright, 2.5f)
            val th = thetaDeg * PI.toFloat() / 180f
            val env = if (hasFid) exp(-fidTime / 0.7f) else 1f
            val mx = sin(th) * env * cos(phi)
            val my = sin(th) * env * sin(phi)
            val mz = cos(th)
            val tip = projectSphere(cx, cy, r, az, el, mx, my, mz)
            drawArrowLine(Offset(cx, cy), tip, AmberWarning, 4f)
            val xyProj = projectSphere(cx, cy, r, az, el, mx, my, 0f)
            drawLine(EmeraldSuccess, Offset(cx, cy), xyProj, 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "f₀ = ${formatId(f0.toDouble(), 2)} MHz | RF = ${formatId(rfFreq.toDouble(), 1)} MHz | ${if (isResonant) "RESONAN ✓" else "Tidak resonan"}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isResonant) EmeraldSuccess else RoseDanger
        )
        Text("B₀: ${formatId(b0.toDouble(), 1)} T", fontSize = 11.sp, color = TextPrimary)
        Slider(value = b0, onValueChange = { b0 = (it * 10).roundToInt() / 10f }, valueRange = 0.5f..3.0f)
        Text("Frekuensi RF: ${formatId(rfFreq.toDouble(), 1)} MHz", fontSize = 11.sp, color = TextPrimary)
        Slider(value = rfFreq, onValueChange = { rfFreq = (it * 10).roundToInt() / 10f }, valueRange = 20f..135f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { rfFreq = f0 }, modifier = Modifier.weight(1f)) {
                Text("Samakan RF", fontSize = 11.sp)
            }
            OutlinedButton(onClick = { rotatingFrame = !rotatingFrame }, modifier = Modifier.weight(1f)) {
                Text(if (rotatingFrame) "Rotating: ON" else "Rotating: OFF", fontSize = 11.sp)
            }
        }
        if (isResonant) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { targetThetaDeg = 90f; fidTime = 0f; hasFid = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Pulsa 90°", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Button(
                    onClick = { targetThetaDeg = 180f; hasFid = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                ) {
                    Text("Pulsa 180°", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { b0 = 1.5f; rfFreq = 63.9f; targetThetaDeg = 0f; hasFid = false; rotatingFrame = false }
        )
    }
}

// 10. A3 Sudut Flip θ = γ·B1·τ (T2.2)
@Composable
private fun M01SudutFlipSim() {
    var b1MicroT by remember { mutableFloatStateOf(5.9f) }
    var tauMs by remember { mutableFloatStateOf(1.0f) }
    var az by remember { mutableFloatStateOf(1.0f) }
    var el by remember { mutableFloatStateOf(0.3f) }

    val flipDeg = (360f * 42.58f * b1MicroT * tauMs / 1000f).coerceIn(0f, 360f)
    val rad = flipDeg * PI.toFloat() / 180f
    val transPct = (abs(sin(rad)) * 100f).roundToInt()
    val longPct = (cos(rad) * 100f).roundToInt()

    M01SimCardShell(
        title = "A3. Sudut Flip θ = γ·B₁·τ (M01.T2.2B)",
        subtitle = "Atur amplitudo B₁ (µT) dan durasi pulsa τ (ms) untuk memiringkan NMV ke bidang transversal."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        az -= drag.x * 0.01f
                        el = (el + drag.y * 0.01f).coerceIn(-0.2f, 1.3f)
                    }
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.37f
            drawSphereWireframe(cx, cy, r, az, el)
            drawArrowLine(projectSphere(cx, cy, r, az, el, 0f, 0f, -1f), projectSphere(cx, cy, r, az, el, 0f, 0f, 1.25f), CyanBright, 2.5f)
            val arc = Path()
            for (k in 0..32) {
                val a = rad * k / 32f
                val p = projectSphere(cx, cy, r, az, el, 0f, -sin(a), cos(a))
                if (k == 0) arc.moveTo(p.x, p.y) else arc.lineTo(p.x, p.y)
            }
            drawPath(arc, color = AmberWarning.copy(alpha = 0.6f), style = Stroke(2f))
            val tip = projectSphere(cx, cy, r, az, el, 0f, -sin(rad), cos(rad))
            drawArrowLine(Offset(cx, cy), tip, AmberWarning, 4f)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "θ = ${flipDeg.roundToInt()}° | Mxy = $transPct% | Mz = $longPct%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanBright
        )
        Text("B₁: ${formatId(b1MicroT.toDouble(), 1)} µT", fontSize = 11.sp, color = TextPrimary)
        Slider(value = b1MicroT, onValueChange = { b1MicroT = (it * 10).roundToInt() / 10f }, valueRange = 1f..15f)
        Text("Durasi Pulsa τ: ${formatId(tauMs.toDouble(), 2)} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = tauMs, onValueChange = { tauMs = (it * 20).roundToInt() / 20f }, valueRange = 0.1f..3.0f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { tauMs = (90f / (360f * 42.58f * b1MicroT / 1000f)).coerceIn(0.1f, 3.0f) },
                modifier = Modifier.weight(1f)
            ) { Text("Atur 90°", fontSize = 11.sp) }
            OutlinedButton(
                onClick = { tauMs = (180f / (360f * 42.58f * b1MicroT / 1000f)).coerceIn(0.1f, 3.0f) },
                modifier = Modifier.weight(1f)
            ) { Text("Atur 180°", fontSize = 11.sp) }
        }
    }
}

// 11. A3 Fase & Sinyal FID (T2.1, T2.3)
@Composable
private fun M01FaseFidSim() {
    val dw = remember { listOf(-1f, 0.6f, -0.3f, 0.9f, -0.7f, 0.2f, 1f, -0.5f) }
    var inh by remember { mutableFloatStateOf(0.5f) }
    var isPlaying by remember { mutableStateOf(true) }
    val phases = remember { mutableStateListOf(*Array(8) { it * 2f * PI.toFloat() / 8f }) }
    val lengths = remember { mutableStateListOf(*Array(8) { 0.85f }) }
    val fidHist = remember { mutableStateListOf<Float>() }
    var pulseTimer by remember { mutableFloatStateOf(0.7f) }
    var refPhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, inh) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    if (pulseTimer > 0f) {
                        pulseTimer -= dt
                        refPhase += 3f * dt
                        for (i in 0 until 8) {
                            val diff = atan2(sin(refPhase - phases[i]), cos(refPhase - phases[i]))
                            phases[i] += diff * min(1f, dt * 6f)
                            lengths[i] += (1f - lengths[i]) * min(1f, dt * 4f)
                        }
                    } else {
                        for (i in 0 until 8) {
                            phases[i] += (3f + inh * 2.5f * dw[i]) * dt
                        }
                    }
                    var sy = 0f
                    for (i in 0 until 8) sy += lengths[i] * sin(phases[i])
                    fidHist.add(sy / 8f)
                    if (fidHist.size > 140) fidHist.removeAt(0)
                }
                lastNanos = nanos
            }
        }
    }

    var sx = 0f
    var sy = 0f
    for (i in 0 until 8) {
        sx += lengths[i] * cos(phases[i])
        sy += lengths[i] * sin(phases[i])
    }
    sx /= 8f
    sy /= 8f
    val coherencePct = (hypot(sx, sy) * 100f).roundToInt()

    M01SimCardShell(
        title = "A3. Eksitasi, Fase & Sinyal FID (M01.T2.3A)",
        subtitle = "8 spin berpresesi di bidang transversal. Pulsa RF 90° menyamakan fase; ketidakhomogenan B₀ memicu dephasing."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val cx = size.width * 0.27f
            val cy = size.height / 2f
            val r = min(cx * 0.78f, size.height * 0.42f)
            drawCircle(CardSlateBorder, radius = r, center = Offset(cx, cy), style = Stroke(1.5f))
            for (i in 0 until 8) {
                val p = Offset(cx + r * lengths[i] * cos(phases[i]), cy - r * lengths[i] * sin(phases[i]))
                drawLine(CyanPrimary.copy(alpha = 0.7f), Offset(cx, cy), p, 2f)
                drawCircle(CyanBright, radius = 4.5f, center = p)
            }
            val resTip = Offset(cx + r * sx, cy - r * sy)
            drawArrowLine(Offset(cx, cy), resTip, EmeraldSuccess, 4f)

            // FID box
            val x0 = size.width * 0.54f
            val bw = size.width * 0.43f
            drawRect(CardSlateBorder, topLeft = Offset(x0, 14f), size = Size(bw, size.height - 28f), style = Stroke(1f))
            drawLine(CardSlateBorder, Offset(x0, cy), Offset(x0 + bw, cy), 1f)
            if (fidHist.size > 1) {
                val path = Path()
                fidHist.forEachIndexed { idx, v ->
                    val x = x0 + idx.toFloat() / 139f * bw
                    val y = cy - v * (size.height * 0.38f)
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = EmeraldSuccess, style = Stroke(2.2f))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Koherensi Transversal: $coherencePct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
        Text("Ketidakhomogenan B₀: ${formatId(inh.toDouble(), 2)}", fontSize = 11.sp, color = TextPrimary)
        Slider(value = inh, onValueChange = { inh = it }, valueRange = 0f..1f)
        Button(
            onClick = { refPhase = phases[0]; pulseTimer = 0.7f },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Beri Pulsa RF 90°", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = {
                for (i in 0 until 8) {
                    phases[i] = i * 2f * PI.toFloat() / 8f
                    lengths[i] = 0.35f
                }
                fidHist.clear()
                pulseTimer = 0f
            }
        )
    }
}

// 12. A5 Bola Bloch (Persamaan Bloch RK4) (T2.3)
@Composable
private fun M01BolaBlochSim() {
    var tissueIdx by remember { mutableIntStateOf(1) }
    var b0 by remember { mutableFloatStateOf(1.5f) }
    var offsetHz by remember { mutableFloatStateOf(20f) }
    var speedMsPerSec by remember { mutableFloatStateOf(300f) }
    var isPlaying by remember { mutableStateOf(true) }
    var az by remember { mutableFloatStateOf(-0.6f) }
    var el by remember { mutableFloatStateOf(0.35f) }

    var mx by remember { mutableFloatStateOf(0f) }
    var my by remember { mutableFloatStateOf(0f) }
    var mz by remember { mutableFloatStateOf(1f) }
    var simTimeMs by remember { mutableFloatStateOf(0f) }
    val mzHist = remember { mutableStateListOf<Float>() }
    val mxyHist = remember { mutableStateListOf<Float>() }

    val (t1, t2) = scaleRelaxation(M01_TISSUES[tissueIdx], b0)

    fun rk4Step(h: Double, w1: Double, dw: Double) {
        fun deriv(a: DoubleArray) = doubleArrayOf(
            dw * a[1] - a[0] / t2,
            -dw * a[0] + w1 * a[2] - a[1] / t2,
            -w1 * a[1] - (a[2] - 1.0) / t1
        )
        val m = doubleArrayOf(mx.toDouble(), my.toDouble(), mz.toDouble())
        val k1 = deriv(m)
        val k2 = deriv(DoubleArray(3) { m[it] + k1[it] * h / 2.0 })
        val k3 = deriv(DoubleArray(3) { m[it] + k2[it] * h / 2.0 })
        val k4 = deriv(DoubleArray(3) { m[it] + k3[it] * h })
        mx = (m[0] + h / 6.0 * (k1[0] + 2 * k2[0] + 2 * k3[0] + k4[0])).toFloat()
        my = (m[1] + h / 6.0 * (k1[1] + 2 * k2[1] + 2 * k3[1] + k4[1])).toFloat()
        mz = (m[2] + h / 6.0 * (k1[2] + 2 * k2[2] + 2 * k3[2] + k4[2])).toFloat()
    }

    LaunchedEffect(isPlaying, tissueIdx, b0, offsetHz, speedMsPerSec) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    val dMs = (dt * speedMsPerSec).toDouble()
                    val dw = 2.0 * PI * offsetHz / 1000.0
                    val steps = max(1, (dMs / 2.0).toInt())
                    val h = dMs / steps
                    for (i in 0 until steps) rk4Step(h, 0.0, dw)
                    simTimeMs += dMs.toFloat()
                    mzHist.add(mz)
                    mxyHist.add(hypot(mx, my))
                    if (mzHist.size > 120) mzHist.removeAt(0)
                    if (mxyHist.size > 120) mxyHist.removeAt(0)
                }
                lastNanos = nanos
            }
        }
    }

    M01SimCardShell(
        title = "A5. Bola Bloch: Persamaan Bloch RK4 (M01.T2.3B)",
        subtitle = "Integrasi numerik RK4 Mz (pemulihan T1) dan |Mxy| (peluruhan T2) dalam kerangka berputar."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            M01_TISSUES.forEachIndexed { idx, t ->
                FilterChip(
                    selected = tissueIdx == idx,
                    onClick = { tissueIdx = idx },
                    label = { Text(t.name, fontSize = 10.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepObsidian)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectDragGestures { _, drag ->
                            az -= drag.x * 0.01f
                            el = (el + drag.y * 0.01f).coerceIn(-1.2f, 1.2f)
                        }
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = min(size.width, size.height) * 0.4f
                drawSphereWireframe(cx, cy, r, az, el)
                val tip = projectSphere(cx, cy, r, az, el, mx, my, mz)
                drawArrowLine(Offset(cx, cy), tip, AmberWarning, 3.5f)
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepObsidian)
                    .clipToBounds()
            ) {
                val mid = size.height / 2f
                drawLine(CardSlateBorder, Offset(0f, mid), Offset(size.width, mid), 1f)
                if (mzHist.size > 1) {
                    val pZ = Path()
                    val pXy = Path()
                    mzHist.forEachIndexed { idx, v ->
                        val x = idx.toFloat() / 119f * size.width
                        val yZ = mid - v * (size.height * 0.42f)
                        val yXy = mid - mxyHist[idx] * (size.height * 0.42f)
                        if (idx == 0) { pZ.moveTo(x, yZ); pXy.moveTo(x, yXy) } else { pZ.lineTo(x, yZ); pXy.lineTo(x, yXy) }
                    }
                    drawPath(pZ, CyanBright, style = Stroke(2.2f))
                    drawPath(pXy, EmeraldSuccess, style = Stroke(2.2f))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Mz = ${formatId(mz.toDouble(), 2)} | |Mxy| = ${formatId(hypot(mx, my).toDouble(), 2)} | T1 = ${t1.toInt()} ms, T2 = ${t2.toInt()} ms",
            fontSize = 11.sp,
            color = CyanBright,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val oldMz = mz
                    mz = -my
                    my = oldMz
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) { Text("Pulsa 90°", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            Button(
                onClick = { mz = -mz; my = -my },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
            ) { Text("Pulsa 180°", color = DeepObsidian, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        }
        Text("Offset Medan: ${offsetHz.roundToInt()} Hz", fontSize = 11.sp, color = TextPrimary)
        Slider(value = offsetHz, onValueChange = { offsetHz = it }, valueRange = -60f..60f)
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { mx = 0f; my = 0f; mz = 1f; simTimeMs = 0f; mzHist.clear(); mxyHist.clear() }
        )
    }
}

// 13. A6 TR dengan Pulsa Berulang (T2.4)
@Composable
private fun M01TrBerulangSim() {
    var tissueAIdx by remember { mutableIntStateOf(3) } // Lemak
    var tissueBIdx by remember { mutableIntStateOf(0) } // CSF
    var tr by remember { mutableFloatStateOf(500f) }
    var te by remember { mutableFloatStateOf(15f) }
    var flipDeg by remember { mutableFloatStateOf(90f) }

    val tA = M01_TISSUES[tissueAIdx]
    val tB = M01_TISSUES[tissueBIdx]
    val rad = flipDeg * PI / 180.0
    fun steadySignal(t: M01Tissue): Pair<Double, Double> {
        val e1 = exp(-tr / t.t1)
        val mz = (1.0 - e1) / (1.0 - e1 * cos(rad))
        val sig = (t.pd ?: 1.0) * mz * sin(rad) * exp(-te / t.t2)
        return Pair(mz, sig)
    }

    val (mzA, sigA) = steadySignal(tA)
    val (mzB, sigB) = steadySignal(tB)
    val contrast = if (sigA + sigB > 0) abs(sigA - sigB) / (sigA + sigB) else 0.0
    val weightLabel = when {
        tr <= 800f && te <= 30f -> "Condong ke Bobot T1 (T1-weighted)"
        tr >= 2000f && te >= 80f -> "Condong ke Bobot T2 (T2-weighted)"
        tr >= 2000f && te <= 30f -> "Condong ke Bobot PD (Proton Density)"
        else -> "Pembobotan Campuran"
    }

    M01SimCardShell(
        title = "A6. TR Pulsa Berulang: Bobot T1, T2 & PD (M01.T2.4)",
        subtitle = "Bandingkan sinyal steady-state dua jaringan terhadap TR, TE, dan sudut flip."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { tr = 500f; te = 15f; flipDeg = 90f }, modifier = Modifier.weight(1f)) { Text("Bobot T1", fontSize = 10.sp) }
            OutlinedButton(onClick = { tr = 3000f; te = 100f; flipDeg = 90f }, modifier = Modifier.weight(1f)) { Text("Bobot T2", fontSize = 10.sp) }
            OutlinedButton(onClick = { tr = 3000f; te = 15f; flipDeg = 90f }, modifier = Modifier.weight(1f)) { Text("Bobot PD", fontSize = 10.sp) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Jaringan A (${tA.name}) vs Jaringan B (${tB.name})", fontSize = 11.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = tissueAIdx == 3, onClick = { tissueAIdx = 3 }, label = { Text("A: Lemak", fontSize = 10.sp) })
            FilterChip(selected = tissueAIdx == 2, onClick = { tissueAIdx = 2 }, label = { Text("A: WM", fontSize = 10.sp) })
            FilterChip(selected = tissueBIdx == 0, onClick = { tissueBIdx = 0 }, label = { Text("B: CSF", fontSize = 10.sp) })
            FilterChip(selected = tissueBIdx == 1, onClick = { tissueBIdx = 1 }, label = { Text("B: GM", fontSize = 10.sp) })
        }
        Text("TR: ${tr.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = tr, onValueChange = { tr = (it / 50f).roundToInt() * 50f }, valueRange = 100f..5000f)
        Text("TE: ${te.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = te, onValueChange = { te = (it / 5f).roundToInt() * 5f }, valueRange = 5f..200f)
        Text("Sudut Flip: ${flipDeg.roundToInt()}°", fontSize = 11.sp, color = TextPrimary)
        Slider(value = flipDeg, onValueChange = { flipDeg = (it / 5f).roundToInt() * 5f }, valueRange = 10f..90f)

        // Bar comparison
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val h = size.height - 20f
            val wA = (sigA.toFloat().coerceIn(0f, 1f)) * (size.width - 90f)
            val wB = (sigB.toFloat().coerceIn(0f, 1f)) * (size.width - 90f)
            drawRoundRect(CyanPrimary, Offset(12f, 14f), Size(max(6f, wA), h * 0.34f), CornerRadius(6f, 6f))
            drawRoundRect(EmeraldSuccess, Offset(12f, 18f + h * 0.45f), Size(max(6f, wB), h * 0.34f), CornerRadius(6f, 6f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$weightLabel\nSinyal A: ${formatId(sigA, 3)} (Mz=${formatId(mzA, 2)}) | Sinyal B: ${formatId(sigB, 3)} (Mz=${formatId(mzB, 2)}) | Kontras: ${formatId(contrast, 3)}",
            fontSize = 11.sp,
            color = CyanBright,
            lineHeight = 16.sp
        )
    }
}

// 14. Tambahan: Urutan Pulsa Dasar TR & TE (T2.4)
@Composable
private fun M01TambahanTrTeSim() {
    var tissueIdx by remember { mutableIntStateOf(0) }
    var b0 by remember { mutableFloatStateOf(1.5f) }
    var tr by remember { mutableFloatStateOf(500f) }
    var te by remember { mutableFloatStateOf(30f) }
    var isPlaying by remember { mutableStateOf(true) }
    var phase by remember { mutableFloatStateOf(0f) }

    val (t1, t2) = scaleRelaxation(M01_TISSUES[tissueIdx], b0)
    val clampedTe = min(te, tr - 5f).coerceAtLeast(5f)
    val recPct = ((1.0 - exp(-tr / t1)) * 100.0).roundToInt()
    val remainPct = (exp(-clampedTe / t2) * 100.0).roundToInt()

    LaunchedEffect(isPlaying) {
        var lastNanos = 0L
        while (isPlaying) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = min(0.05f, (nanos - lastNanos) / 1_000_000_000f)
                    phase = (phase + dt / 6f) % 1f
                }
                lastNanos = nanos
            }
        }
    }

    M01SimCardShell(
        title = "Tambahan: Urutan Pulsa Dasar TR & TE (T2.4)",
        subtitle = "Visualisasi garis waktu pulsa RF berjarak TR dan pembacaan sinyal pada waktu TE."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            M01_TISSUES.forEachIndexed { idx, t ->
                FilterChip(selected = tissueIdx == idx, onClick = { tissueIdx = idx }, label = { Text(t.name, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val x0 = 16f
            val span = size.width - 32f
            val tot = 2f * tr
            fun xOf(t: Float) = x0 + (t / tot) * span
            val baseY = size.height * 0.65f
            drawLine(CardSlateBorder, Offset(x0, baseY), Offset(x0 + span, baseY), 1.5f)
            // RF pulses at 0, TR, 2*TR
            listOf(0f, tr, tot).forEach { tp ->
                val xp = xOf(tp).coerceAtMost(x0 + span - 10f)
                drawRect(CyanPrimary, Offset(xp, 28f), Size(10f, 22f))
            }
            // Echoes at TE and TR + TE
            listOf(clampedTe, tr + clampedTe).forEachIndexed { k, tc ->
                val cx = xOf(tc)
                val amp = 46f * (remainPct / 100f) * (if (k == 0) 1f else recPct / 100f)
                val echoPath = Path()
                for (d in -24..24) {
                    val y = baseY - amp * exp(-(d / 10f) * (d / 10f)) * cos(d * 0.8f)
                    if (d == -24) echoPath.moveTo(cx + d, y) else echoPath.lineTo(cx + d, y)
                }
                drawPath(echoPath, EmeraldSuccess, style = Stroke(2f))
            }
            // Playhead
            val xp = xOf(phase * tot)
            drawLine(AmberWarning, Offset(xp, 14f), Offset(xp, size.height - 14f), 2f)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("TR: ${tr.roundToInt()} ms | TE: ${clampedTe.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = tr, onValueChange = { tr = (it / 50f).roundToInt() * 50f }, valueRange = 50f..3000f)
        Slider(value = clampedTe, onValueChange = { te = (it / 5f).roundToInt() * 5f }, valueRange = 5f..300f)
        Text("Pemulihan T1 saat TR: $recPct% | Sisa T2 saat TE: $remainPct%", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        M01PlayResetRow(
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying },
            onReset = { tr = 500f; te = 30f; phase = 0f; isPlaying = true }
        )
    }
}
