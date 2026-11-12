# Spesifikasi Teknis Perbaikan 6 Bug Tersisa — Aplikasi Pembelajaran Fisika MRI (Iterasi Final)

> **Cara Penggunaan di Google AI Studio / Asisten Coding AI:**
> Salin seluruh isi dokumen ini dan berikan perintah:
> *"Terapkan seluruh perbaikan kode pada dokumen spesifikasi ini secara presisi ke dalam file proyek yang sesuai tanpa mengubah fitur yang sudah berjalan normal."*

---

## Ringkasan Status Audit Proyek Terbaru

Seluruh temuan dari iterasi sebelumnya (`NB-01` s/d `NB-05`, `B-06`, `B-17`, dan `B-23`) telah terverifikasi diperbaiki pada basis kode terbaru. Audit baris demi baris pada kode terbaru menemukan **6 bug tersisa** yang perlu diselesaikan agar aplikasi siap produksi $100\%$:

| Kode Bug | Prioritas | File Terdampak | Ringkasan Masalah |
| :--- | :--- | :--- | :--- |
| **FINAL-01** | **Tinggi** | `ui/simulators/Module3Simulators.kt` | 10 dari 11 tab simulator memanggil `onNodeComplete(...)` di dalam `LaunchedEffect(Unit)` sehingga seluruh materi Modul 3 otomatis selesai saat halaman baru dibuka. |
| **FINAL-02** | **Sedang** | `data/UserDataRepository.kt` | Membatalkan tanda selesai simpul materi (`un-complete`) dapat menghapus streak harian yang diperoleh dari aktivitas *Flashcards* dan meninggalkan `lastNodeCode` usang. |
| **FINAL-03** | **Sedang** | `ui/simulators/Module3Simulators.kt` | 4 komponen simulator memiliki kontrol UI yang terputus (`selectedTissue` tanpa chip di `ContrastAgentTabContent`, `teMs` & `statusText` mati di `SpinEchoTabContent`, `trMs` diabaikan di `GreContrastStoveTabContent`, dan `effectiveTe` terkunci di `TurboSpinEchoTabContent`). |
| **FINAL-04** | **Rendah** | `viewmodel/MriUiState.kt`, `viewmodel/MriViewModel.kt`, `ui/dialog/LoadingScreen.kt`, `MriApp.kt` | `sessionKey` di `LoadingScreen.kt` hanya bergantung pada perubahan teks `customMessage`, sehingga transisi beruntun dengan pesan yang sama tidak me-reset timer. |
| **FINAL-05** | **Rendah** | `ui/module/MriWebBridge.kt` | Fungsi `recordQuiz` berbasis indeks mengirimkan `explicitQuizId` sintetis yang menimpa ID soal asli dari database Room. |
| **FINAL-06** | **Rendah** | `ui/animation/LearningAnimationScreen.kt` | Variabel sudut rotasi `rotAngle` masih menggunakan `remember` biasa sehingga posisi jeda animasi hilang saat layar diputar. |

---

## Detail Spesifikasi Perbaikan per File

---

### 1. [FINAL-01 & FINAL-03] Perbaikan Auto-Complete & Kontrol Interaktif Simulator Modul 3

* **Target File**: `app/src/main/java/com/example/ui/simulators/Module3Simulators.kt`
* **Akar Masalah**:
  1. **Auto-Complete Prematur (`FINAL-01`)**: `SpinEchoTabContent`, `InversionRecoveryTabContent`, `TurboSpinEchoTabContent`, `DriveEquilibriumTabContent`, `ErnstAngleTabContent`, `GreContrastStoveTabContent`, `BipolarRewinderTabContent`, `ThreeSteadyStateTabContent`, `DwiStejskalTabContent`, `ContrastAgentTabContent`, dan `BoldAndMtcTabContent` mengeksekusi `LaunchedEffect(Unit) { onNodeComplete(...) }`. Karena `ModuleDetailScreen.kt` merender seluruh kartu di dalam `Column` dengan `verticalScroll`, membuka halaman detail `M03A`, `M03B`, atau `M03C` langsung menandai hampir semua simpul sebagai selesai tanpa interaksi pengguna.
  2. **Kontrol UI Terputus (`FINAL-03`)**:
     * Pada `ContrastAgentTabContent`, variabel `selectedTissue` tidak memiliki `FilterChip` di UI sehingga pengguna tidak bisa mengganti jaringan (`WM`, `GM`, `CSF`).
     * Pada `SpinEchoTabContent`, variabel `statusText` tidak pernah dirender ke layar dan slider `teMs` ($20\text{--}140\text{ ms}$) tidak memengaruhi posisi pulsa $\tau$ maupun $TE$ pada kanvas.
     * Pada `GreContrastStoveTabContent`, slider `trMs` ($10\text{--}200\text{ ms}$) diabaikan penuh oleh logika penentu kontras dan tidak menampilkan estimasi sinyal steady-state.
     * Pada `TurboSpinEchoTabContent`, nilai `effectiveTe` otomatis terpangkas oleh `if (effectiveTe > etl * echoSpacing)` tanpa adanya slider atau mekanisme proporsional untuk menaikkannya kembali saat `etl` dinaikkan.
* **Tindakan Perbaikan**:
  Ganti seluruh isi file `app/src/main/java/com/example/ui/simulators/Module3Simulators.kt` dengan kode di bawah ini. Seluruh `LaunchedEffect(Unit)` telah dihapus dan `onNodeComplete(node.code)` kini hanya dipanggil saat pengguna benar-benar berinteraksi dengan kontrol simulator.

```kotlin
package com.example.ui.simulators

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurriculumNode
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun Module3Router(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    val code = node.code.uppercase()
    when {
        code.startsWith("M03A.T1.1") || code.startsWith("M03A.T1.2") -> SpinEchoTabContent(node, onNodeComplete)
        code.startsWith("M03A.T1.3") -> InversionRecoveryTabContent(node, onNodeComplete)
        code.startsWith("M03A.T2.1") || code.startsWith("M03A.T2.2") -> TurboSpinEchoTabContent(node, onNodeComplete)
        code.startsWith("M03A.T2.3") -> DriveEquilibriumTabContent(node, onNodeComplete)
        code.startsWith("M03B.T1.1") -> ErnstAngleTabContent(node, onNodeComplete)
        code.startsWith("M03B.T1.2") -> GreContrastStoveTabContent(node, onNodeComplete)
        code.startsWith("M03B.T1.3") -> BipolarRewinderTabContent(node, onNodeComplete)
        code.startsWith("M03B.T2.1") -> SpoilingTabContent(node, onNodeComplete)
        code.startsWith("M03B.T2.2") || code.startsWith("M03B.T2.3") -> ThreeSteadyStateTabContent(node, onNodeComplete)
        code.startsWith("M03C.T1.1") || code.startsWith("M03C.T1.2") -> DwiStejskalTabContent(node, onNodeComplete)
        code.startsWith("M03C.T1.3") || code.startsWith("M03C.T1.4") || code.startsWith("M03C.T2.1") || code.startsWith("M03C.T2.2") -> ContrastAgentTabContent(node, onNodeComplete)
        code.startsWith("M03C.T2.3") || code.startsWith("M03C.T2.4") -> BoldAndMtcTabContent(node, onNodeComplete)
        else -> SpinEchoTabContent(node, onNodeComplete)
    }
}

// ==========================================
// M03A.T1.1 & T1.2: Spin Echo Simulator
// ==========================================
@Composable
fun SpinEchoTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var teMs by rememberSaveable { mutableFloatStateOf(60f) }
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var viewMode by rememberSaveable { mutableIntStateOf(0) } // 0: Spin Fan, 1: T2 vs T2* Envelope

    val infiniteTransition = rememberInfiniteTransition(label = "se_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "se_phase"
    )
    var manualProgress by rememberSaveable { mutableFloatStateOf(0.5f) }
    val t = if (isPlaying) animProgress else manualProgress

    // Posisi relatif TE pada timeline (dinormalisasi terhadap rentang maksimum 160 ms)
    val teNorm = (teMs / 160f).coerceIn(0.25f, 0.90f)
    val tauNorm = teNorm / 2f

    val statusText = when {
        t < tauNorm - 0.03f -> "Fase 1 (0 .. \u03C4): Dephasing akibat T2* (Inhomogenitas B0)"
        t < tauNorm + 0.03f -> "Pulsa RF 180\u00B0 pada t = \u03C4 (${(teMs / 2f).toInt()} ms): Membalik fase spin"
        t < teNorm - 0.03f -> "Fase 2 (\u03C4 .. TE): Rephasing menuju pembentukan gema"
        t < teNorm + 0.04f -> "t = TE (${teMs.toInt()} ms): Spin Echo Terbentuk! (Efek B0 tereliminasi)"
        else -> "Setelah TE: Spin kembali mengalami dephasing"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator Spin Echo (Hahn Echo): Pulsa 90\u00B0 - \u03C4 - 180\u00B0 - \u03C4 - Echo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewMode == 0,
                    onClick = {
                        viewMode = 0
                        onNodeComplete(node.code)
                    },
                    label = { Text("Kipas Spin (Fanning)") }
                )
                FilterChip(
                    selected = viewMode == 1,
                    onClick = {
                        viewMode = 1
                        onNodeComplete(node.code)
                    },
                    label = { Text("Kurva T2 vs T2*") }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusText,
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617))
            ) {
                val w = size.width
                val h = size.height

                if (viewMode == 0) {
                    val cx = w * 0.35f
                    val cy = h * 0.5f
                    val radius = min(w, h) * 0.35f

                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f)
                    )

                    // Hitung dephasing relatif terhadap tauNorm dan teNorm
                    val dephaseFactor = when {
                        t <= tauNorm -> t / tauNorm
                        t <= teNorm -> 1f - ((t - tauNorm) / (teNorm - tauNorm)).coerceIn(0f, 1f)
                        else -> ((t - teNorm) / (1f - teNorm).coerceAtLeast(0.1f)).coerceIn(0f, 1f)
                    }
                    // Skala lebar kipas berdasarkan TE (semakin lama TE, semakin lebar dephasing sebelum 180)
                    val teScale = (teMs / 80f).coerceIn(0.4f, 1.5f)

                    val speeds = listOf(-1.2f, -0.6f, 0f, 0.6f, 1.2f)
                    val colors = listOf(
                        Color(0xFFEF4444),
                        Color(0xFFF97316),
                        Color(0xFF22C55E),
                        Color(0xFF38BDF8),
                        Color(0xFF60A5FA)
                    )

                    speeds.forEachIndexed { idx, spd ->
                        val flipOffset = if (t > tauNorm) PI.toFloat() else 0f
                        val angle = -PI.toFloat() / 2f + spd * dephaseFactor * teScale * 1.1f + flipOffset
                        val ex = cx + radius * cos(angle)
                        val ey = cy + radius * sin(angle)
                        drawLine(
                            color = colors[idx],
                            start = Offset(cx, cy),
                            end = Offset(ex, ey),
                            strokeWidth = 5f
                        )
                        drawCircle(color = colors[idx], radius = 6f, center = Offset(ex, ey))
                    }

                    // Timeline bar di sisi kanan
                    val tlLeft = w * 0.68f
                    val tlRight = w * 0.95f
                    val tlY = h * 0.55f
                    drawLine(Color.Gray, Offset(tlLeft, tlY), Offset(tlRight, tlY), strokeWidth = 3f)

                    val tauX = tlLeft + (tlRight - tlLeft) * tauNorm
                    val teX = tlLeft + (tlRight - tlLeft) * teNorm
                    val curX = tlLeft + (tlRight - tlLeft) * t

                    drawLine(Color(0xFFF59E0B), Offset(tlLeft, tlY - 35f), Offset(tlLeft, tlY), strokeWidth = 4f)
                    drawLine(Color(0xFFEC4899), Offset(tauX, tlY - 50f), Offset(tauX, tlY), strokeWidth = 5f)
                    drawCircle(Color(0xFF22C55E), radius = 9f, center = Offset(teX, tlY))
                    drawCircle(Color.White, radius = 6f, center = Offset(curX, tlY))
                } else {
                    val padL = 36f
                    val padB = 28f
                    val gw = w - padL - 20f
                    val gh = h - padB - 20f

                    drawLine(Color.Gray, Offset(padL, 16f), Offset(padL, 16f + gh), 2f)
                    drawLine(Color.Gray, Offset(padL, 16f + gh), Offset(padL + gw, 16f + gh), 2f)

                    val t2Path = Path()
                    val t2StarPath = Path()
                    val steps = 120
                    for (i in 0..steps) {
                        val frac = i / steps.toFloat()
                        val timeMs = frac * 160f
                        val x = padL + frac * gw

                        val envT2 = exp(-timeMs / 90f)
                        val yT2 = 16f + gh * (1f - envT2)
                        if (i == 0) t2Path.moveTo(x, yT2) else t2Path.lineTo(x, yT2)

                        val distFromEcho = abs(timeMs - teMs)
                        val envT2Star = if (timeMs < teMs / 2f) {
                            exp(-timeMs / 22f)
                        } else {
                            exp(-timeMs / 90f) * exp(-distFromEcho / 18f)
                        }
                        val yStar = 16f + gh * (1f - envT2Star.coerceIn(0f, 1f))
                        if (i == 0) t2StarPath.moveTo(x, yStar) else t2StarPath.lineTo(x, yStar)
                    }

                    drawPath(t2Path, Color(0xFF38BDF8), style = Stroke(width = 3f))
                    drawPath(t2StarPath, Color(0xFF22C55E), style = Stroke(width = 4f))

                    val echoX = padL + (teMs / 160f) * gw
                    val echoY = 16f + gh * (1f - exp(-teMs / 90f))
                    drawCircle(Color(0xFFF59E0B), radius = 7f, center = Offset(echoX, echoY))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        onNodeComplete(node.code)
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                Text(
                    "TE = ${teMs.toInt()} ms (\u03C4 = ${(teMs / 2).toInt()} ms)",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Slider(
                value = teMs,
                onValueChange = {
                    teMs = it
                    onNodeComplete(node.code)
                },
                valueRange = 20f..140f
            )

            if (!isPlaying) {
                Text("Scrub Waktu Siklus: ${(manualProgress * 100).toInt()}%", color = Color.LightGray, fontSize = 12.sp)
                Slider(
                    value = manualProgress,
                    onValueChange = {
                        manualProgress = it
                        onNodeComplete(node.code)
                    },
                    valueRange = 0f..1f
                )
            }
        }
    }
}

// ==========================================
// M03A.T1.3: Inversion Recovery (STIR & FLAIR)
// ==========================================
@Composable
fun InversionRecoveryTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var tiMs by rememberSaveable { mutableFloatStateOf(200f) }
    var isMagnitude by rememberSaveable { mutableStateOf(true) }

    val t1Fat = 260f
    val t1Wm = 800f
    val t1Csf = 3600f

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator Inversion Recovery (180\u00B0 - TI - 90\u00B0): Nulling Jaringan",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {
                        tiMs = (ln(2f) * t1Fat)
                        onNodeComplete(node.code)
                    },
                    label = { Text("Preset STIR (TI\u2248180ms)") }
                )
                AssistChip(
                    onClick = {
                        tiMs = (ln(2f) * t1Csf)
                        onNodeComplete(node.code)
                    },
                    label = { Text("Preset FLAIR (TI\u22482500ms)") }
                )
                FilterChip(
                    selected = isMagnitude,
                    onClick = {
                        isMagnitude = !isMagnitude
                        onNodeComplete(node.code)
                    },
                    label = { Text(if (isMagnitude) "Magnitude |Mz|" else "Real Mz") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617))
            ) {
                val w = size.width
                val h = size.height
                val padL = 36f
                val gw = w - padL - 16f
                val zeroY = if (isMagnitude) h - 24f else h * 0.5f

                drawLine(Color.DarkGray, Offset(padL, zeroY), Offset(padL + gw, zeroY), 2f)

                fun drawCurve(t1: Float, color: Color) {
                    val path = Path()
                    val steps = 120
                    for (i in 0..steps) {
                        val frac = i / steps.toFloat()
                        val t = frac * 3500f
                        val mz = 1f - 2f * exp(-t / t1)
                        val valPlot = if (isMagnitude) abs(mz) else mz
                        val x = padL + frac * gw
                        val y = if (isMagnitude) {
                            zeroY - valPlot * (h - 44f)
                        } else {
                            zeroY - valPlot * (h * 0.42f)
                        }
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color, style = Stroke(width = 3.5f))
                }

                drawCurve(t1Fat, Color(0xFFFACC15)) // Lemak
                drawCurve(t1Wm, Color(0xFF22C55E))  // WM
                drawCurve(t1Csf, Color(0xFF38BDF8)) // CSF

                val tiX = padL + (tiMs / 3500f).coerceIn(0f, 1f) * gw
                drawLine(Color(0xFFEF4444), Offset(tiX, 12f), Offset(tiX, h - 12f), strokeWidth = 3f)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "TI = ${tiMs.toInt()} ms | Kuning: Lemak (T1=260ms), Hijau: WM (T1=800ms), Biru: CSF (T1=3600ms)",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Slider(
                value = tiMs,
                onValueChange = {
                    tiMs = it
                    onNodeComplete(node.code)
                },
                valueRange = 50f..3200f
            )
        }
    }
}

// ==========================================
// M03A.T2.1 & T2.2: Turbo Spin Echo (FSE / TSE)
// ==========================================
@Composable
fun TurboSpinEchoTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var etl by rememberSaveable { mutableIntStateOf(8) }
    var echoSpacing by rememberSaveable { mutableFloatStateOf(12f) }
    var effectiveEchoIndex by rememberSaveable { mutableIntStateOf(4) }

    val clampedEchoIdx = effectiveEchoIndex.coerceIn(1, etl)
    val effectiveTe = clampedEchoIdx * echoSpacing
    val scanTimeReduction = etl

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator Turbo Spin Echo (FSE/TSE) & Pengisian K-Space",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Faktor Percepatan = ${scanTimeReduction}x | TE_eff = ${effectiveTe.toInt()} ms (Echo ke-$clampedEchoIdx) | Total Train = ${(etl * echoSpacing).toInt()} ms",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617))
            ) {
                val w = size.width
                val h = size.height
                val spacingPx = (w - 48f) / etl.coerceAtLeast(1)

                for (i in 1..etl) {
                    val x = 24f + (i - 0.5f) * spacingPx
                    val tEcho = i * echoSpacing
                    val amp = exp(-tEcho / 100f).coerceIn(0.08f, 1f)
                    val barH = amp * (h - 48f)
                    val isCenterKSpace = i == clampedEchoIdx
                    drawRect(
                        color = if (isCenterKSpace) Color(0xFF22C55E) else Color(0xFF38BDF8),
                        topLeft = Offset(x - spacingPx * 0.28f, h - 24f - barH),
                        size = Size(spacingPx * 0.56f, barH)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Echo Train Length (ETL / Turbo Factor): $etl", color = Color.White, fontSize = 12.sp)
            Slider(
                value = etl.toFloat(),
                onValueChange = {
                    etl = it.roundToInt().coerceIn(2, 24)
                    if (effectiveEchoIndex > etl) effectiveEchoIndex = etl
                    onNodeComplete(node.code)
                },
                valueRange = 2f..24f
            )

            Text("Echo Spacing (ESP): ${echoSpacing.toInt()} ms", color = Color.White, fontSize = 12.sp)
            Slider(
                value = echoSpacing,
                onValueChange = {
                    echoSpacing = it
                    onNodeComplete(node.code)
                },
                valueRange = 6f..25f
            )

            Text(
                "Echo Pengisi Pusat K-Space (Penentu TE_eff): Echo #$clampedEchoIdx (${effectiveTe.toInt()} ms)",
                color = Color(0xFF22C55E),
                fontSize = 12.sp
            )
            Slider(
                value = clampedEchoIdx.toFloat(),
                onValueChange = {
                    effectiveEchoIndex = it.roundToInt().coerceIn(1, etl)
                    onNodeComplete(node.code)
                },
                valueRange = 1f..etl.toFloat().coerceAtLeast(2f)
            )
        }
    }
}

// ==========================================
// M03A.T2.3: Driven Equilibrium (DRIVE / RESTORE)
// ==========================================
@Composable
fun DriveEquilibriumTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var driveEnabled by rememberSaveable { mutableStateOf(true) }
    var trMs by rememberSaveable { mutableFloatStateOf(1200f) }

    val csfRecovery = if (driveEnabled) {
        1f - exp(-trMs / 900f)
    } else {
        1f - exp(-trMs / 3800f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Driven Equilibrium (Pulsa Reset -90\u00B0 di Akhir Echo Train)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = driveEnabled,
                    onCheckedChange = {
                        driveEnabled = it
                        onNodeComplete(node.code)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (driveEnabled) "DRIVE / RESTORE Aktif (Mz CSF didorong naik)" else "FSE Konvensional (CSF jenuh pada TR pendek)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { csfRecovery.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                color = if (driveEnabled) Color(0xFF22C55E) else Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Pemulihan Mz Cairan (CSF) pada TR=${trMs.toInt()} ms: ${(csfRecovery * 100).toInt()}%",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Slider(
                value = trMs,
                onValueChange = {
                    trMs = it
                    onNodeComplete(node.code)
                },
                valueRange = 400f..3000f
            )
        }
    }
}

// ==========================================
// M03B.T1.1: Ernst Angle & Low Flip Angle GRE
// ==========================================
@Composable
fun ErnstAngleTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var trMs by rememberSaveable { mutableFloatStateOf(100f) }
    var t1Ms by rememberSaveable { mutableFloatStateOf(800f) }
    var userAngle by rememberSaveable { mutableFloatStateOf(28f) }

    val e1 = exp(-trMs / t1Ms)
    val ernstRad = acos(e1.coerceIn(-1f, 1f))
    val ernstDeg = Math.toDegrees(ernstRad.toDouble()).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Kalkulator & Kurva Sudut Ernst: \u03B1_E = arccos(e^(-TR/T1))",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Sudut Ernst Optimal = ${"%.1f".format(ernstDeg)}\u00B0 | Pilihan Anda = ${userAngle.toInt()}\u00B0",
                color = Color(0xFF22C55E),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617))
            ) {
                val w = size.width
                val h = size.height
                val path = Path()
                for (deg in 1..90) {
                    val rad = Math.toRadians(deg.toDouble()).toFloat()
                    val sig = (sin(rad) * (1f - e1)) / (1f - e1 * cos(rad))
                    val x = (deg / 90f) * (w - 32f) + 16f
                    val y = h - 20f - (sig * (h - 40f)).coerceIn(0f, h - 40f)
                    if (deg == 1) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color(0xFF38BDF8), style = Stroke(width = 3.5f))

                val ernstX = (ernstDeg / 90f) * (w - 32f) + 16f
                drawLine(Color(0xFF22C55E), Offset(ernstX, 12f), Offset(ernstX, h - 12f), 2.5f)

                val userX = (userAngle / 90f) * (w - 32f) + 16f
                drawCircle(Color(0xFFF59E0B), radius = 7f, center = Offset(userX, h * 0.5f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("TR: ${trMs.toInt()} ms", color = Color.White, fontSize = 12.sp)
            Slider(
                value = trMs,
                onValueChange = {
                    trMs = it
                    onNodeComplete(node.code)
                },
                valueRange = 10f..1000f
            )
            Text("T1 Jaringan: ${t1Ms.toInt()} ms", color = Color.White, fontSize = 12.sp)
            Slider(
                value = t1Ms,
                onValueChange = {
                    t1Ms = it
                    onNodeComplete(node.code)
                },
                valueRange = 200f..2500f
            )
            Text("Flip Angle (\u03B1): ${userAngle.toInt()}\u00B0", color = Color.White, fontSize = 12.sp)
            Slider(
                value = userAngle,
                onValueChange = {
                    userAngle = it
                    onNodeComplete(node.code)
                },
                valueRange = 5f..90f
            )
        }
    }
}

// ==========================================
// M03B.T1.2: Kompor Kontras GRE (Contrast Stove)
// ==========================================
@Composable
fun GreContrastStoveTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var flipAngleDeg by rememberSaveable { mutableFloatStateOf(30f) }
    var teMs by rememberSaveable { mutableFloatStateOf(10f) }
    var trMs by rememberSaveable { mutableFloatStateOf(50f) }

    // Perhitungan sinyal steady-state Spoiled GRE untuk WM (T1=800, T2*=45) vs CSF (T1=3600, T2*=250)
    val rad = Math.toRadians(flipAngleDeg.toDouble()).toFloat()
    val e1Wm = exp(-trMs / 800f)
    val sigWm = ((sin(rad) * (1f - e1Wm)) / (1f - e1Wm * cos(rad))) * exp(-teMs / 45f)

    val e1Csf = exp(-trMs / 3600f)
    val sigCsf = ((sin(rad) * (1f - e1Csf)) / (1f - e1Csf * cos(rad))) * exp(-teMs / 250f)

    val weighting = when {
        flipAngleDeg >= 35f && teMs <= 12f && trMs <= 120f ->
            "T1-Weighted (Sudut flip besar + TR pendek memaksimalkan perbedaan saturasi T1; WM lebih terang dari CSF)"
        flipAngleDeg <= 20f && teMs >= 20f ->
            "T2*-Weighted (Sudut flip kecil meminimalkan saturasi T1 + TE panjang menonjolkan dephasing T2*)"
        flipAngleDeg <= 20f && teMs <= 12f && trMs >= 80f ->
            "Proton Density (PD) Weighted (Sudut flip kecil + TR cukup panjang + TE pendek meminimalkan efek T1 & T2*)"
        else ->
            "Kontras Campuran / Transisi (Sesuaikan kombinasi TR, TE, dan Flip Angle untuk mengisolasi T1, PD, atau T2*)"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Kompor Kontras GRE: Interaksi Flip Angle (\u03B1), TR & TE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(weighting, color = Color(0xFFFACC15), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Estimasi Sinyal Relatif -> WM: ${"%.2f".format(sigWm)} | CSF: ${"%.2f".format(sigCsf)}",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Flip Angle (\u03B1): ${flipAngleDeg.toInt()}\u00B0", color = Color.White, fontSize = 12.sp)
            Slider(
                value = flipAngleDeg,
                onValueChange = {
                    flipAngleDeg = it
                    onNodeComplete(node.code)
                },
                valueRange = 5f..80f
            )
            Text("TE: ${teMs.toInt()} ms", color = Color.White, fontSize = 12.sp)
            Slider(
                value = teMs,
                onValueChange = {
                    teMs = it
                    onNodeComplete(node.code)
                },
                valueRange = 2f..45f
            )
            Text("TR: ${trMs.toInt()} ms", color = Color.White, fontSize = 12.sp)
            Slider(
                value = trMs,
                onValueChange = {
                    trMs = it
                    onNodeComplete(node.code)
                },
                valueRange = 10f..200f
            )
        }
    }
}

// ==========================================
// M03B.T1.3: Gradien Bipolar (Defase - Refase)
// ==========================================
@Composable
fun BipolarRewinderTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var rephaseRatio by rememberSaveable { mutableFloatStateOf(1.0f) }

    val netArea = -1.0f + rephaseRatio
    val echoAmplitude = exp(-abs(netArea) * 4f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Pembentukan Gradient Echo: Lobe Defase (-A) & Lobe Readout (+A)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Net Gradient Area di TE = ${"%.2f".format(netArea)} | Amplitudo Gema = ${(echoAmplitude * 100).toInt()}%",
                color = if (abs(netArea) < 0.08f) Color(0xFF22C55E) else Color(0xFFF97316),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = rephaseRatio,
                onValueChange = {
                    rephaseRatio = it
                    onNodeComplete(node.code)
                },
                valueRange = 0f..2f
            )
            Text(
                "Geser rasio luas lobe positif hingga tepat 1.00 untuk memfokuskan kembali spin pada t = TE.",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

// ==========================================
// M03B.T2.1: Spoiling (RF Spoiling & Gradient Crusher)
// ==========================================
@Composable
fun SpoilingTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var rfSpoilingOn by rememberSaveable { mutableStateOf(true) }
    var phaseIncrement by rememberSaveable { mutableFloatStateOf(117f) }

    val residualMxy = if (!rfSpoilingOn) 0.72f else {
        val diffFromGolden = abs(phaseIncrement - 117f)
        (0.05f + (diffFromGolden / 120f) * 0.55f).coerceIn(0.04f, 0.65f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator RF Spoiling & Penghancuran Koherensi Transversal (SPGR / FLASH)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rfSpoilingOn,
                    onCheckedChange = {
                        rfSpoilingOn = it
                        onNodeComplete(node.code)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (rfSpoilingOn) "RF Phase Cycling Aktif (\u0394\u03C6 = ${phaseIncrement.toInt()}\u00B0)" else "Spoiling Mati (Kontaminasi T2* muncul)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Residu Koherensi Mxy: ${(residualMxy * 100).toInt()}% (Optimal pada sudut 117\u00B0 atau 50\u00B0)",
                color = if (residualMxy < 0.15f) Color(0xFF22C55E) else Color(0xFFF59E0B),
                fontSize = 12.sp
            )
            if (rfSpoilingOn) {
                Slider(
                    value = phaseIncrement,
                    onValueChange = {
                        phaseIncrement = it
                        onNodeComplete(node.code)
                    },
                    valueRange = 0f..180f
                )
            }
        }
    }
}

// ==========================================
// M03B.T2.2 & T2.3: 3 Rezim Steady-State GRE
// ==========================================
@Composable
fun ThreeSteadyStateTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var selectedRegime by rememberSaveable { mutableIntStateOf(2) } // 0: FID/FISP, 1: PSIF, 2: bSSFP

    val description = when (selectedRegime) {
        0 -> "Rezim 1 — Gradient-Spoiled Steady-State (FISP / GRASS): Mengakuisisi komponen FID (S+). Dipengaruhi T1 dan rasio T2*/T1."
        1 -> "Rezim 2 — Time-Reversed Steady-State (PSIF / CE-FAST): Mengakuisisi Hahn Echo yang terbentuk tepat sebelum pulsa RF berikutnya (S-). Sangat T2-weighted."
        else -> "Rezim 3 — Balanced SSFP (TrueFISP / FIESTA / bFFE): Seluruh gradien memiliki net area NOL per TR. S+ dan S- menyatu menghasilkan SNR tertinggi dengan kontras rasio T2/T1."
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Perbandingan 3 Rezim Coherent Steady-State GRE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedRegime == 0,
                    onClick = {
                        selectedRegime = 0
                        onNodeComplete(node.code)
                    },
                    label = { Text("FISP (S+)") }
                )
                FilterChip(
                    selected = selectedRegime == 1,
                    onClick = {
                        selectedRegime = 1
                        onNodeComplete(node.code)
                    },
                    label = { Text("PSIF (S-)") }
                )
                FilterChip(
                    selected = selectedRegime == 2,
                    onClick = {
                        selectedRegime = 2
                        onNodeComplete(node.code)
                    },
                    label = { Text("bSSFP (Balanced)") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, color = Color.LightGray, fontSize = 13.sp)
        }
    }
}

// ==========================================
// M03C.T1.1 & T1.2: Difusi Stejskal-Tanner (DWI & ADC)
// ==========================================
@Composable
fun DwiStejskalTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var bValue by rememberSaveable { mutableFloatStateOf(1000f) }

    val adcCsf = 0.0030f
    val adcNormalBrain = 0.0008f
    val adcIschemicStroke = 0.00035f

    val sigCsf = exp(-bValue * adcCsf)
    val sigNormal = exp(-bValue * adcNormalBrain)
    val sigStroke = exp(-bValue * adcIschemicStroke)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator DWI Stejskal-Tanner: S = S0 \u00D7 e^(-b \u00D7 ADC)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "b-value = ${bValue.toInt()} s/mm\u00B2",
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Lesi Stroke Iskemik Akut (Restriksi Difusi) -> Sinyal: ${(sigStroke * 100).toInt()}%", color = Color(0xFF22C55E), fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { sigStroke.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Color(0xFF22C55E)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text("Parenkim Otak Normal -> Sinyal: ${(sigNormal * 100).toInt()}%", color = Color(0xFF38BDF8), fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { sigNormal.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Color(0xFF38BDF8)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text("Cairan Serebrospinal / CSF (Difusi Bebas) -> Sinyal: ${(sigCsf * 100).toInt()}%", color = Color(0xFFF59E0B), fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { sigCsf.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Color(0xFFF59E0B)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Slider(
                value = bValue,
                onValueChange = {
                    bValue = it
                    onNodeComplete(node.code)
                },
                valueRange = 0f..2000f
            )
        }
    }
}

// ==========================================
// M03C.T1.3 - T2.2: Agen Kontras Gadolinium (Dipole-Dipole & T1/T2 Shortening)
// ==========================================
@Composable
fun ContrastAgentTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var gdConcentrationMmol by rememberSaveable { mutableFloatStateOf(0.5f) }
    var selectedTissue by rememberSaveable { mutableIntStateOf(0) } // 0: WM, 1: GM, 2: CSF

    val baseT1List = listOf(800f, 1100f, 3600f)
    val baseT2List = listOf(80f, 100f, 1800f)
    val tissueNames = listOf("Substansia Alba (WM)", "Substansia Grisea (GM)", "CSF")

    val r1 = 4.5f // s^-1 mM^-1
    val r2 = 5.5f // s^-1 mM^-1

    val t10Sec = baseT1List[selectedTissue] / 1000f
    val t20Sec = baseT2List[selectedTissue] / 1000f

    val newR1 = (1f / t10Sec) + r1 * gdConcentrationMmol
    val newR2 = (1f / t20Sec) + r2 * gdConcentrationMmol

    val newT1Ms = (1000f / newR1)
    val newT2Ms = (1000f / newR2)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator Relaksivitas Gadolinium (Gd3+): Pemendekan T1 vs T2",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tissueNames.forEachIndexed { index, name ->
                    FilterChip(
                        selected = selectedTissue == index,
                        onClick = {
                            selectedTissue = index
                            onNodeComplete(node.code)
                        },
                        label = { Text(name, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Jaringan: ${tissueNames[selectedTissue]} | Konsentrasi [Gd] = ${"%.2f".format(gdConcentrationMmol)} mM",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "T1: ${baseT1List[selectedTissue].toInt()} ms \u2192 ${newT1Ms.toInt()} ms | T2: ${baseT2List[selectedTissue].toInt()} ms \u2192 ${newT2Ms.toInt()} ms",
                color = Color(0xFF22C55E),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                if (gdConcentrationMmol <= 2.0f) {
                    "Dosis Klinis Normal: Efek pemendekan T1 mendominasi \u2192 Hiperintens (Terang) pada T1w."
                } else {
                    "Konsentrasi Sangat Tinggi: Dephasing T2/T2* mendominasi \u2192 Paradoxical Signal Drop (Gelap)."
                },
                color = if (gdConcentrationMmol <= 2.0f) Color.LightGray else Color(0xFFF97316),
                fontSize = 12.sp
            )
            Slider(
                value = gdConcentrationMmol,
                onValueChange = {
                    gdConcentrationMmol = it
                    onNodeComplete(node.code)
                },
                valueRange = 0f..5f
            )
        }
    }
}

// ==========================================
// M03C.T2.3 & T2.4: fMRI BOLD & Magnetization Transfer (MTC)
// ==========================================
@Composable
fun BoldAndMtcTabContent(
    node: CurriculumNode,
    onNodeComplete: (String) -> Unit
) {
    var neuralActivation by rememberSaveable { mutableStateOf(false) }
    var mtPulseOn by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Simulator fMRI BOLD (Oxy- vs Deoxy-Hb) & Magnetization Transfer (MTC)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = neuralActivation,
                    onCheckedChange = {
                        neuralActivation = it
                        onNodeComplete(node.code)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (neuralActivation) "Aktivasi Neuron: Rasio Oxy-Hb (Diamagnetik) naik \u2192 T2* memanjang (+3% Sinyal BOLD)"
                    else "Saat Istirahat (Baseline): Deoxy-Hb (Paramagnetik) mempercepat dephasing T2*",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = mtPulseOn,
                    onCheckedChange = {
                        mtPulseOn = it
                        onNodeComplete(node.code)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (mtPulseOn) "Pulsa Off-Resonance MTC Aktif: Bound Pool jenuh \u2192 Sinyal latar parenkim ditekan"
                    else "MTC Nonaktif: Tidak ada transfer saturasi dari makromolekul ke air bebas",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
```

---

### 2. [FINAL-02] Perlindungan Streak Flashcards Saat Simpul Materi Dibatalkan (`un-complete`)

* **Target File**:
  1. `app/src/main/java/com/example/data/db/Daos.kt`
  2. `app/src/main/java/com/example/data/UserDataRepository.kt`
* **Akar Masalah**:
  Pada `UserDataRepository.toggleNodeCompletionTransactional`, ketika pengguna membatalkan tanda selesai (`!newCompletedState`), kode menghapus seluruh baris `study_days` pada tanggal tersebut jika `countOnDay == 0 && quizCountOnDay == 0`. Padahal jika pengguna hari ini telah mempelajari *Flashcards* (yang dicatat dengan `lastNodeCode = "FLASHCARD_REVIEW"`), penghapusan tersebut ikut menghilangkan streak belajar hari ini. Selain itu, jika masih ada simpul lain yang selesai pada hari tersebut tetapi simpul yang baru saja dibatalkan adalah `existingDay.lastNodeCode`, maka `lastNodeCode` tetap menunjuk pada kode simpul yang sudah dibatalkan.
* **Tindakan Perbaikan**:
  1. Pada `app/src/main/java/com/example/data/db/Daos.kt`, tambahkan query `getLatestCompletedNodeCodeBetween` di dalam `NodeProgressDao`:

```kotlin
    @Query("SELECT nodeCode FROM node_progress WHERE completed = 1 AND completedAt BETWEEN :startMillis AND :endMillis ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLatestCompletedNodeCodeBetween(startMillis: Long, endMillis: Long): String?
```

  2. Pada `app/src/main/java/com/example/data/UserDataRepository.kt`, perbarui cabang `else` (saat `newCompletedState == false`) di dalam fungsi `toggleNodeCompletionTransactional` menjadi seperti berikut:

```kotlin
            } else {
                val targetMillis = existing?.completedAt ?: nowMillis
                val targetDateIso = Instant.ofEpochMilli(targetMillis)
                    .atZone(zone)
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)

                val dayStart = LocalDate.parse(targetDateIso).atStartOfDay(zone).toInstant().toEpochMilli()
                val dayEnd = LocalDate.parse(targetDateIso).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L

                val countOnDay = database.nodeProgressDao().getCountCompletedBetween(dayStart, dayEnd)
                val quizCountOnDay = database.quizAttemptDao().getCountAnsweredBetween(dayStart, dayEnd)
                val existingDay = database.studyDayDao().getByDate(targetDateIso)
                val hasFlashcardActivity = existingDay?.lastNodeCode == "FLASHCARD_REVIEW"

                if (countOnDay == 0 && quizCountOnDay == 0 && !hasFlashcardActivity) {
                    database.studyDayDao().deleteByDate(targetDateIso)
                } else if (existingDay != null) {
                    val latestRemainingCode = database.nodeProgressDao()
                        .getLatestCompletedNodeCodeBetween(dayStart, dayEnd)
                        ?: if (hasFlashcardActivity) "FLASHCARD_REVIEW" else null
                    database.studyDayDao().upsert(
                        existingDay.copy(
                            nodesCompletedCount = countOnDay,
                            lastNodeCode = if (existingDay.lastNodeCode == nodeCode) latestRemainingCode else existingDay.lastNodeCode
                        )
                    )
                }
            }
```

---

### 3. [FINAL-04] Penambahan `loadingSessionId` Eksplisit untuk Mereset Timer `LoadingScreen`

* **Target File**:
  1. `app/src/main/java/com/example/viewmodel/MriUiState.kt`
  2. `app/src/main/java/com/example/viewmodel/MriViewModel.kt`
  3. `app/src/main/java/com/example/ui/dialog/LoadingScreen.kt`
  4. `app/src/main/java/com/example/MriApp.kt`
* **Akar Masalah**:
  Di `LoadingScreen.kt`, baris `val sessionKey = remember(customMessage) { System.nanoTime() }` hanya memperbarui `sessionKey` jika nilai string `customMessage` berubah. Apabila transisi baru dipicu saat `LoadingScreen` masih tampil dengan `customMessage` yang sama (misalnya keduanya `null`), `sessionKey` tidak berubah sehingga `LaunchedEffect` tidak ter-reset.
* **Tindakan Perbaikan**:
  1. Pada `MriUiState.kt`, tambahkan properti `val loadingSessionId: Long = 0L` di dalam `data class MriUiState`:
```kotlin
    val isLoadingTransition: Boolean = false,
    val loadingMessage: String? = null,
    val loadingSessionId: Long = 0L,
```
  2. Pada `MriViewModel.kt`, perbarui fungsi `triggerTransition` agar selalu menghasilkan `loadingSessionId` unik:
```kotlin
    fun triggerTransition(message: String? = null, onMidTransition: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingTransition = true,
                    loadingMessage = message,
                    loadingSessionId = System.nanoTime()
                )
            }
            onMidTransition()
        }
    }
```
  3. Pada `LoadingScreen.kt`, tambahkan parameter `sessionId: Long = 0L` dan gunakan sebagai kunci `LaunchedEffect`:
```kotlin
@Composable
fun LoadingScreen(
    customMessage: String? = null,
    sessionId: Long = 0L,
    onLoadingFinished: () -> Unit = {}
) {
    val tips = remember { MriRepository.loadingTips }
    var currentTip by remember(customMessage, sessionId) {
        mutableStateOf(customMessage ?: tips.random())
    }
    var progress by remember(customMessage, sessionId) { mutableFloatStateOf(0f) }

    LaunchedEffect(customMessage, sessionId) {
        progress = 0f
        currentTip = customMessage ?: tips.random()
        val steps = 30
        for (i in 1..steps) {
            delay(25)
            progress = i / steps.toFloat()
        }
        delay(100)
        onLoadingFinished()
    }
```
  4. Pada `MriApp.kt`, teruskan `sessionId = state.loadingSessionId` saat memanggil `LoadingScreen`:
```kotlin
            LoadingScreen(
                customMessage = state.loadingMessage,
                sessionId = state.loadingSessionId,
                onLoadingFinished = { viewModel.finishTransition() }
            )
```

---

### 4. [FINAL-05] Perbaikan Pencocokan ID Soal Kuis pada `MriWebBridge.kt`

* **Target File**: `app/src/main/java/com/example/ui/module/MriWebBridge.kt`
* **Akar Masalah**:
  Fungsi `recordQuiz(moduleId: String, questionIndex: Int, selectedIndex: Int, isCorrect: Boolean)` membuat string `val explicitQuizId = "${resolvedMod.uppercase()}_Q${questionIndex + 1}"` dan meneruskannya ke `viewModel.recordQuizFromHtml(...)`. Di dalam `MriViewModel.recordQuizFromHtml`, karena `!explicitQuizId.isNullOrBlank()` bernilai `true`, ID soal asli di database (`moduleQuizzes.getOrNull(questionIndex)?.id`) dilewati dan digantikan oleh ID sintetis tersebut. Akibatnya, halaman evaluasi `QuizScreen.kt` gagal menemukan detail soal untuk ditampilkan pada daftar *"Soal yang pernah salah"*.
* **Tindakan Perbaikan**:
  Ubah fungsi `recordQuiz` di `MriWebBridge.kt` agar meneruskan `explicitQuizId = null` (biarkan `MriViewModel` mencocokkan ID soal asli dari database berdasarkan `questionIndex` terlebih dahulu, dan hanya gunakan fallback sintetis di ViewModel jika indeks di luar jangkauan):

```kotlin
    @JavascriptInterface
    fun recordQuiz(moduleId: String, questionIndex: Int, selectedIndex: Int, isCorrect: Boolean) {
        val resolvedMod = moduleId.ifBlank { currentModuleId }
        viewModel.recordQuizFromHtml(
            moduleId = resolvedMod,
            questionIndex = questionIndex,
            selectedIndex = selectedIndex,
            isCorrect = isCorrect,
            explicitQuizId = null
        )
    }
```

---

### 5. [FINAL-06] Penyimpanan State Sudut Rotasi (`rotAngle`) pada `LearningAnimationScreen.kt`

* **Target File**: `app/src/main/java/com/example/ui/animation/LearningAnimationScreen.kt`
* **Akar Masalah**:
  Variabel `var rotAngle by remember { mutableFloatStateOf(0f) }` pada baris 46 masih memakai `remember` biasa, sehingga ketika pengguna menjeda animasi (`isPlaying = false`) untuk mengamati vektor magnetisasi lalu memutar layar perangkat, sudut rotasi kembali ke `0f`.
* **Tindakan Perbaikan**:
  Ubah deklarasi `rotAngle` pada `LearningAnimationScreen.kt` menjadi `rememberSaveable`:

```kotlin
    var rotAngle by rememberSaveable { mutableFloatStateOf(0f) }
```

---

## Daftar Periksa Verifikasi Akhir (Post-Fix Verification)

1. **Verifikasi `FINAL-01`**: Buka halaman detail Modul 3 (`M03A`, `M03B`, `M03C`) dengan akun baru. Pastikan persentase progres modul tetap $0\%$ saat layar hanya dibuka/di-scroll, dan baru bertambah ketika slider/chip/tombol pada kartu simulator digerakkan.
2. **Verifikasi `FINAL-02`**: Tandai 1 kartu *Flashcard* sebagai "Sudah Hafal" hari ini (streak bertambah menjadi 1 hari), lalu tandai selesai 1 simpul materi dan batalkan kembali simpul tersebut. Pastikan streak hari ini tidak terhapus.
3. **Verifikasi `FINAL-03`**:
   * Pada simulator Gadolinium (`M03C`), pastikan terdapat 3 chip jaringan (`Substansia Alba (WM)`, `Substansia Grisea (GM)`, `CSF`) yang mengubah nilai $T_1$ dan $T_2$.
   * Pada simulator Spin Echo (`M03A`), pastikan menggeser slider $TE$ mengubah posisi pulsa $\tau$ dan titik gema $TE$ pada timeline, serta teks status fase tampil di atas kanvas.
   * Pada Kompor Kontras GRE (`M03B`), pastikan menggeser slider $TR$, $TE$, dan $\alpha$ memperbarui estimasi sinyal relatif `WM` vs `CSF` serta kategori pembobotan.
   * Pada simulator Turbo Spin Echo (`M03A`), pastikan pengguna dapat memilih indeks gema pengisi pusat *k-space* (`Echo #1` s/d `Echo #ETL`) secara bebas.
4. **Verifikasi `FINAL-04` s/d `FINAL-06`**: Pastikan rotasi layar pada `LearningAnimationScreen` mempertahankan sudut animasi yang dijeda, dan soal yang dijawab salah dari modul HTML tampil dengan teks soal lengkap di layar `QuizScreen`.