package com.example.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.CharacterAccentPalette
import com.example.data.CharacterCatalog
import com.example.ui.theme.CharAccentMint
import com.example.ui.theme.CharRoomBg
import kotlin.math.PI
import kotlin.math.sin
import com.example.ui.theme.CharStageBase
import com.example.ui.theme.CharStageDeep
import com.example.ui.theme.CharStagePlatformFill

@Composable
fun StageBackgroundCanvas(
    backgroundId: String,
    accentPalette: CharacterAccentPalette,
    glowPulse: Float,
    rfWavePhase: Float,
    isScanningRf: Boolean,
    isHoloPose: Boolean,
    modifier: Modifier = Modifier
) {
    val bgItem = CharacterCatalog.getItem(backgroundId)
    val drawableRes = bgItem?.drawableRes ?: R.drawable.bg_control_room

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Gambar Latar Ruangan (Proporsional dengan ContentScale.Crop & Alignment.BottomCenter agar tidak terdistorsi)
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = bgItem?.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter
        )

        // 2. Penyeimbang Warna Tema Gelap MRI & Panggung Bercahaya (Agar Avatar Putih Tetap Kontras dan Jelas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Gradien harmonisasi atas-tengah-bawah sesuai palet gelap aplikasi (#071A2B / #0F2A45)
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to CharStageDeep.copy(alpha = 0.62f),
                        0.38f to CharStageBase.copy(alpha = 0.36f),
                        0.74f to CharStageBase.copy(alpha = 0.55f),
                        1.0f to CharRoomBg.copy(alpha = 0.90f)
                    )
                )
            )

            // Cahaya Radial lembut di belakang karakter
            val avatarCenter = Offset(w * 0.54f, h * 0.58f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentPalette.primaryColor.copy(alpha = 0.24f * glowPulse),
                        CharStageDeep.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = avatarCenter,
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = avatarCenter
            )

            // Panggung Elips Bercahaya Tempat Karakter Berdiri
            val platformY = h * 0.78f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentPalette.primaryColor.copy(alpha = 0.52f * glowPulse),
                        accentPalette.glowColor.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.54f, platformY),
                    radius = w * 0.36f
                ),
                topLeft = Offset(w * 0.20f, platformY - 22f),
                size = Size(w * 0.68f, 44f)
            )
            drawOval(
                color = CharStagePlatformFill.copy(alpha = 0.88f),
                topLeft = Offset(w * 0.32f, platformY - 12f),
                size = Size(w * 0.44f, 24f)
            )
            drawOval(
                color = accentPalette.primaryColor,
                topLeft = Offset(w * 0.32f, platformY - 12f),
                size = Size(w * 0.44f, 24f),
                style = Stroke(width = 2.5f)
            )

            // Gelombang Pemindaian Pulsa RF 90° / Animasi Gelombang Interaktif
            if (isScanningRf || isHoloPose) {
                val scanRadius1 = (w * 0.12f) + rfWavePhase * (w * 0.36f)
                val scanRadius2 = (w * 0.12f) + ((rfWavePhase + 0.5f) % 1f) * (w * 0.36f)
                drawCircle(
                    color = accentPalette.glowColor.copy(alpha = (1f - rfWavePhase) * 0.75f),
                    radius = scanRadius1,
                    center = avatarCenter,
                    style = Stroke(width = 3.2f)
                )
                drawCircle(
                    color = CharAccentMint.copy(alpha = (1f - ((rfWavePhase + 0.5f) % 1f)) * 0.55f),
                    radius = scanRadius2,
                    center = avatarCenter,
                    style = Stroke(width = 2.4f)
                )

                // Gelombang Sinus RF mengalir di belakang karakter
                val phaseRad = rfWavePhase * 2f * PI.toFloat()
                for (waveIdx in 0..1) {
                    val baseY = h * (if (waveIdx == 0) 0.54f else 0.62f)
                    val amp = h * (if (waveIdx == 0) 0.055f else 0.040f)
                    val waveColor = if (waveIdx == 0) {
                        accentPalette.glowColor.copy(alpha = 0.72f)
                    } else {
                        CharAccentMint.copy(alpha = 0.52f)
                    }
                    val sinePath = Path()
                    val steps = 36
                    for (i in 0..steps) {
                        val t = i.toFloat() / steps
                        val x = w * t
                        val y = baseY + sin(t * 4f * PI.toFloat() + phaseRad + waveIdx * 1.3f) * amp
                        if (i == 0) sinePath.moveTo(x, y) else sinePath.lineTo(x, y)
                    }
                    drawPath(
                        path = sinePath,
                        color = waveColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
