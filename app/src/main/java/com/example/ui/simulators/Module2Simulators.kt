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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.PathEffect
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

enum class M02SimulationType(
    val id: String,
    val shortCode: String,
    val title: String,
    val nodeTag: String,
    val subtitle: String
) {
    FANTOM_OTAK_SEKUENS(
        id = "m2_fantom_otak",
        shortCode = "M02.1",
        title = "Fantom Otak & Sekuens",
        nodeTag = "M02.T1.2/T1.4",
        subtitle = "SE, IR, GRE, T1w/T2w/PDw/FLAIR"
    ),
    PELURUHAN_T2_DEPHASING(
        id = "m2_peluruhan_t2",
        shortCode = "M02.2",
        title = "Peluruhan T2 & Dephasing",
        nodeTag = "M02.T2.1/T2.2",
        subtitle = "Kipas fase spin & sisa Mxy"
    ),
    KOMPOR_KONTRAS(
        id = "m2_kompor_kontras",
        shortCode = "M02.3",
        title = "Kompor Kontras SE",
        nodeTag = "M02.T1.1/T1.4",
        subtitle = "Api T1 vs Api T2 & Lemak/CSF"
    ),
    PARAMETER_SEKUENS(
        id = "m2_parameter_sekuens",
        shortCode = "M02.4",
        title = "Parameter Tiap Sekuens",
        nodeTag = "M02.T1.4",
        subtitle = "Peta TR, TE, TI, FA, TF & b"
    );

    companion object {
        fun fromLabType(labType: String?): M02SimulationType = when (labType) {
            "m2_fantom_otak", "contrast" -> FANTOM_OTAK_SEKUENS
            "m2_peluruhan_t2" -> PELURUHAN_T2_DEPHASING
            "m2_kompor_kontras" -> KOMPOR_KONTRAS
            "m2_parameter_sekuens" -> PARAMETER_SEKUENS
            else -> FANTOM_OTAK_SEKUENS
        }
    }
}

private data class M02Tissue(val id: String, val name: String, val t1: Double, val t2: Double, val pd: Double)

private val M02_TISSUES = listOf(
    M02Tissue("fat", "Lemak", 250.0, 70.0, 0.90),
    M02Tissue("gm", "Substansia Grisea", 950.0, 100.0, 0.85),
    M02Tissue("wm", "Substansia Alba", 600.0, 80.0, 0.72),
    M02Tissue("csf", "CSF (Cairan)", 4000.0, 2000.0, 1.0)
)

@Composable
fun Module2LabHub(
    selectedActivityId: String,
    onSelectActivity: (String) -> Unit
) {
    val activeType = M02SimulationType.fromLabType(selectedActivityId)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Kartu Induk M02
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
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "M02",
                            tint = CyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "M02: Laboratorium Relaksasi & Kontras",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Fantom otak interaktif, peluruhan T2 & kipas dephasing, kompor kontras, dan peta parameter sekuens.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M02SimulationType.entries.forEach { item ->
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
                                        text = item.shortCode,
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

        when (activeType) {
            M02SimulationType.FANTOM_OTAK_SEKUENS -> M02FantomOtakSim()
            M02SimulationType.PELURUHAN_T2_DEPHASING -> M02PeluruhanT2Sim()
            M02SimulationType.KOMPOR_KONTRAS -> M02KomporKontrasSim()
            M02SimulationType.PARAMETER_SEKUENS -> M02ParameterSekuensSim()
        }
    }
}

@Composable
private fun M02CardShell(
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
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanBright)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun M02FantomOtakSim() {
    var seqType by rememberSaveable { mutableStateOf("se") } // se, ir, gre
    var tr by rememberSaveable { mutableFloatStateOf(500f) }
    var te by rememberSaveable { mutableFloatStateOf(15f) }
    var ti by rememberSaveable { mutableFloatStateOf(2200f) }
    var fa by rememberSaveable { mutableFloatStateOf(90f) }

    fun signalFor(t: M02Tissue): Double {
        val e1 = exp(-tr / t.t1)
        val e2 = exp(-te / t.t2)
        return when (seqType) {
            "gre" -> {
                val a = fa * PI / 180.0
                t.pd * sin(a) * (1.0 - e1) / (1.0 - e1 * cos(a)) * e2
            }
            "ir" -> t.pd * abs(1.0 - 2.0 * exp(-min(ti, tr) / t.t1) + e1) * e2
            else -> t.pd * (1.0 - e1) * e2
        }
    }

    val signals = M02_TISSUES.map { signalFor(it) }
    val maxSig = max(0.01, signals.maxOrNull() ?: 1.0)
    val wm = M02_TISSUES[2]
    val gm = M02_TISSUES[1]
    val ernstWm = acos(exp(-tr / wm.t1)) * 180.0 / PI

    val weightLabel = when (seqType) {
        "ir" -> {
            val suppressed = M02_TISSUES.filterIndexed { i, _ -> signals[i] < 0.1 * maxSig }.joinToString { it.name }
            if (suppressed.isNotEmpty()) "IR: menekan $suppressed" else "IR: geser TI ke titik nol jaringan"
        }
        "gre" -> if (fa > ernstWm) "GRE: Bobot T1 (α > sudut Ernst)" else if (te < 40f) "GRE: Bobot PD" else "GRE: Bobot T2*"
        else -> {
            val t1Mean = (gm.t1 + wm.t1) / 2.0
            val t2Mean = (gm.t2 + wm.t2) / 2.0
            when {
                tr < t1Mean && te < t2Mean -> "Spin Echo: T1-Weighted (T1w)"
                tr >= t1Mean && te >= t2Mean -> "Spin Echo: T2-Weighted (T2w)"
                tr >= t1Mean && te < t2Mean -> "Spin Echo: Proton Density (PDw)"
                else -> "Spin Echo: Kontras Campuran"
            }
        }
    }

    M02CardShell(
        title = "Fantom Otak & Diagram Sekuens (M02.T1.2 / T1.4)",
        subtitle = "Amati intensitas Lemak, Substansia Grisea (GM), Substansia Alba (WM), dan CSF pada sekuens SE, IR, dan GRE."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(onClick = { seqType = "se"; tr = 500f; te = 15f; fa = 90f }) { Text("T1w", fontSize = 10.sp) }
            OutlinedButton(onClick = { seqType = "se"; tr = 4000f; te = 100f; fa = 90f }) { Text("T2w", fontSize = 10.sp) }
            OutlinedButton(onClick = { seqType = "se"; tr = 3000f; te = 15f; fa = 90f }) { Text("PDw", fontSize = 10.sp) }
            OutlinedButton(onClick = { seqType = "ir"; tr = 9000f; te = 100f; ti = 2500f }) { Text("FLAIR", fontSize = 10.sp) }
            OutlinedButton(onClick = { seqType = "ir"; tr = 4000f; te = 60f; ti = 170f }) { Text("STIR", fontSize = 10.sp) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("se" to "Spin Echo", "ir" to "Inversion Rec", "gre" to "Gradient Echo").forEach { (id, lbl) ->
                FilterChip(
                    selected = seqType == id,
                    onClick = {
                        seqType = id
                        if (id == "gre") fa = ernstWm.roundToInt().toFloat().coerceIn(5f, 90f)
                    },
                    label = { Text(lbl, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanDark, selectedLabelColor = CyanBright)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Fantom Otak Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .clipToBounds()
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rx = (size.width * 0.34f).coerceAtMost(size.height * 0.62f)
            val ry = rx * 0.72f
            fun gray(sig: Double): Color {
                val g = (sig / maxSig).toFloat().coerceIn(0f, 1f)
                return Color(g, g, g, 1f)
            }
            // Lemak (kulit kepala), GM (korteks), WM (inti dalam), CSF (ventrikel)
            drawOval(gray(signals[0]), Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))
            drawOval(gray(signals[1]), Offset(cx - rx * 0.85f, cy - ry * 0.82f), Size(rx * 1.70f, ry * 1.64f))
            drawOval(gray(signals[2]), Offset(cx - rx * 0.64f, cy - ry * 0.59f), Size(rx * 1.28f, ry * 1.18f))
            drawOval(gray(signals[3]), Offset(cx - rx * 0.22f, cy - ry * 0.28f), Size(rx * 0.16f, ry * 0.56f))
            drawOval(gray(signals[3]), Offset(cx + rx * 0.06f, cy - ry * 0.28f), Size(rx * 0.16f, ry * 0.56f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(weightLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
        Spacer(modifier = Modifier.height(4.dp))
        M02_TISSUES.forEachIndexed { idx, t ->
            Text(
                text = "• ${t.name}: sinyal relatif ${"%.2f".format(signals[idx]).replace('.', ',')}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("TR: ${tr.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = tr, onValueChange = { tr = (it / 50f).roundToInt() * 50f }, valueRange = 100f..9000f)
        Text("TE: ${te.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = te, onValueChange = { te = (it / 5f).roundToInt() * 5f }, valueRange = 5f..200f)
        if (seqType == "ir") {
            Text("TI: ${ti.roundToInt()} ms", fontSize = 11.sp, color = AmberWarning)
            Slider(value = ti, onValueChange = { ti = (it / 25f).roundToInt() * 25f }, valueRange = 50f..3000f)
        }
        if (seqType == "gre") {
            Text("Flip Angle α: ${fa.roundToInt()}° (Ernst WM ≈ ${ernstWm.roundToInt()}°)", fontSize = 11.sp, color = CyanBright)
            Slider(value = fa, onValueChange = { fa = it.roundToInt().toFloat() }, valueRange = 5f..90f)
        }
    }
}

@Composable
private fun M02PeluruhanT2Sim() {
    var te by rememberSaveable { mutableFloatStateOf(100f) }
    var tissueIdx by rememberSaveable { mutableIntStateOf(3) } // CSF default
    val fat = M02_TISSUES[0]
    val comp = M02_TISSUES[tissueIdx]
    val ampFat = exp(-te / fat.t2).toFloat()
    val ampComp = exp(-te / comp.t2).toFloat()
    val contrastRel = (abs(ampComp - ampFat) / max(0.001f, ampComp + ampFat) * 100f).roundToInt()

    M02CardShell(
        title = "Peluruhan T2 & Kipas Dephasing (M02.T2.1 / T2.2)",
        subtitle = "Lihat hubungan antara kurva peluruhan T2 dan penyebaran fase spin (dephasing) pada waktu TE."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(1, 2, 3).forEach { idx ->
                FilterChip(
                    selected = tissueIdx == idx,
                    onClick = { tissueIdx = idx },
                    label = { Text(M02_TISSUES[idx].name, fontSize = 10.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepObsidian)
                .clipToBounds()
        ) {
            val leftW = size.width * 0.56f
            val h = size.height
            val padLeft = 14.dp.toPx()
            val padTop = 12.dp.toPx()
            val padBottom = 14.dp.toPx()
            val padRight = 8.dp.toPx()
            fun xOf(t: Float) = padLeft + (t / 400f) * (leftW - padLeft - padRight)
            fun yOf(v: Float) = h - padBottom - v * (h - padTop - padBottom)

            drawLine(CardSlateBorder, Offset(padLeft, h - padBottom), Offset(leftW - padRight, h - padBottom), 1.dp.toPx())
            drawLine(CardSlateBorder, Offset(padLeft, padTop), Offset(padLeft, h - padBottom), 1.dp.toPx())

            listOf(fat to AmberWarning, comp to CyanBright).forEach { (tissue, col) ->
                val path = Path()
                for (t in 0..400 step 8) {
                    val x = xOf(t.toFloat())
                    val y = yOf(exp(-t / tissue.t2).toFloat())
                    if (t == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, col, style = Stroke(2.dp.toPx()))
            }
            val xTe = xOf(te)
            drawLine(TextMuted, Offset(xTe, padTop), Offset(xTe, h - padBottom), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()), 0f))

            // Kipas dephasing di sisi kanan
            val fanX = size.width * 0.78f
            val fanR = min(h * 0.16f, 24.dp.toPx())
            listOf(Triple(ampFat, AmberWarning, h * 0.30f), Triple(ampComp, CyanBright, h * 0.74f)).forEach { (amp, col, cy) ->
                val spread = PI.toFloat() * (1f - amp)
                for (i in 0 until 13) {
                    val a = (i / 12f - 0.5f) * 2f * spread
                    drawLine(col, Offset(fanX, cy), Offset(fanX + fanR * cos(a), cy - fanR * sin(a)), 1.2.dp.toPx())
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("TE: ${te.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = te, onValueChange = { te = (it / 5f).roundToInt() * 5f }, valueRange = 5f..350f)
        Text(
            text = "Sisa sinyal Lemak: ${(ampFat * 100).roundToInt()}% | ${comp.name}: ${(ampComp * 100).roundToInt()}% | Kontras relatif: $contrastRel%",
            fontSize = 11.sp,
            color = CyanBright
        )
    }
}

@Composable
private fun M02KomporKontrasSim() {
    var tr by rememberSaveable { mutableFloatStateOf(500f) }
    var te by rememberSaveable { mutableFloatStateOf(15f) }
    val fat = M02_TISSUES[0]
    val csf = M02_TISSUES[3]

    val d1 = abs((1.0 - exp(-tr / fat.t1)) - (1.0 - exp(-tr / csf.t1)))
    val d2 = abs(exp(-te / fat.t2) - exp(-te / csf.t2))
    val h1 = (d1 / 0.72).toFloat().coerceIn(0f, 1f)
    val h2 = (d2 / 0.85).toFloat().coerceIn(0f, 1f)
    val sigFat = (fat.pd * exp(-te / fat.t2) * (1.0 - exp(-tr / fat.t1))).toFloat()
    val sigCsf = (csf.pd * exp(-te / csf.t2) * (1.0 - exp(-tr / csf.t1))).toFloat()

    val mode = when {
        h1 >= 0.5f && h2 < 0.5f -> "T1w (Api T1 dominan, Api T2 rendah)"
        h2 >= 0.5f && h1 < 0.5f -> "T2w (Api T2 dominan, Api T1 rendah)"
        h1 < 0.5f && h2 < 0.5f -> "PDw (Kedua api rendah → kerapatan proton dominan)"
        else -> "Kontras Campuran (Kedua api menyala)"
    }

    M02CardShell(
        title = "Kompor Kontras Spin Echo (M02.T1.1 / T1.4)",
        subtitle = "TR pendek menyalakan Api T1; TE panjang menyalakan Api T2. Bandingkan sinyal Lemak vs CSF."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { tr = 500f; te = 15f }, modifier = Modifier.weight(1f)) { Text("T1w", fontSize = 10.sp) }
            OutlinedButton(onClick = { tr = 3000f; te = 100f }, modifier = Modifier.weight(1f)) { Text("T2w", fontSize = 10.sp) }
            OutlinedButton(onClick = { tr = 3000f; te = 15f }, modifier = Modifier.weight(1f)) { Text("PDw", fontSize = 10.sp) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E1420))
                .clipToBounds()
        ) {
            val baseY = size.height - 16.dp.toPx()
            val x1 = size.width * 0.28f
            val x2 = size.width * 0.72f
            fun drawFlame(cx: Float, norm: Float, color: Color) {
                val fh = 12.dp.toPx() + norm * (size.height * 0.52f).coerceAtLeast(24.dp.toPx())
                val fw = 14.dp.toPx() + norm * 10.dp.toPx()
                drawRect(TextMuted, Offset(cx - 24.dp.toPx(), baseY), Size(48.dp.toPx(), 4.dp.toPx()))
                val p = Path().apply {
                    moveTo(cx - fw, baseY)
                    quadraticTo(cx - fw * 0.7f, baseY - fh * 0.5f, cx, baseY - fh)
                    quadraticTo(cx + fw * 0.7f, baseY - fh * 0.5f, cx + fw, baseY)
                    close()
                }
                drawPath(p, color.copy(alpha = 0.85f))
            }
            drawFlame(x1, h1, AmberWarning)
            drawFlame(x2, h2, CyanBright)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
        Text("Api T1: ${(h1 * 100).roundToInt()}% | Api T2: ${(h2 * 100).roundToInt()}%", fontSize = 11.sp, color = TextSecondary)
        Text("Sinyal Lemak: ${(sigFat * 100).roundToInt()}% | Sinyal CSF: ${(sigCsf * 100).roundToInt()}%", fontSize = 11.sp, color = CyanBright)
        Text("TR: ${tr.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = tr, onValueChange = { tr = (it / 50f).roundToInt() * 50f }, valueRange = 100f..4000f)
        Text("TE: ${te.roundToInt()} ms", fontSize = 11.sp, color = TextPrimary)
        Slider(value = te, onValueChange = { te = (it / 5f).roundToInt() * 5f }, valueRange = 5f..200f)
    }
}

@Composable
private fun M02ParameterSekuensSim() {
    val sequences = remember {
        listOf(
            "Spin Echo Konvensional (CSE)" to setOf("TR", "TE"),
            "Turbo Spin Echo (TSE)" to setOf("TR", "TE", "TF"),
            "Inversion Recovery (IR)" to setOf("TR", "TE", "TI"),
            "STIR / FLAIR-TSE" to setOf("TR", "TE", "TI", "TF"),
            "Gradient Echo (GRE)" to setOf("TR", "TE", "FA"),
            "DWI (Difusi)" to setOf("TR", "TE", "b")
        )
    }
    var selectedIdx by rememberSaveable { mutableIntStateOf(0) }
    val allKeys = listOf("TR", "TE", "TI", "FA", "TF", "b")
    val activeKeys = sequences[selectedIdx].second

    M02CardShell(
        title = "Parameter Kontrol Tiap Sekuens (M02.T1.4)",
        subtitle = "Pilih jenis sekuens untuk melihat parameter kontrol yang aktif."
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sequences.forEachIndexed { idx, (name, _) ->
                FilterChip(
                    selected = selectedIdx == idx,
                    onClick = { selectedIdx = idx },
                    label = { Text(name, fontSize = 10.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            allKeys.forEach { key ->
                val on = key in activeKeys
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (on) CyanDark else DeepObsidian,
                    border = BorderStroke(1.dp, if (on) CyanBright else CardSlateBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(key, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (on) CyanBright else TextMuted)
                        Text(if (on) "✓" else "–", fontSize = 11.sp, color = if (on) EmeraldSuccess else TextMuted)
                    }
                }
            }
        }
    }
}
