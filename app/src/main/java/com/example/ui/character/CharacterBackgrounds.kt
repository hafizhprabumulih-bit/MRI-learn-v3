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
import kotlin.math.cos
import kotlin.math.min
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
    isWaveActive: Boolean,
    isCoherenceGameActive: Boolean = false,
    gamePulseProgress: Float = 0f,
    gameHitFlash: Float = 0f,
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

            // Gradien harmonisasi atas-tengah-bawah yang disesuaikan untuk panggung lebih besar
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to CharStageDeep.copy(alpha = 0.50f),
                        0.42f to CharStageBase.copy(alpha = 0.28f),
                        0.78f to CharStageBase.copy(alpha = 0.48f),
                        1.0f to CharRoomBg.copy(alpha = 0.86f)
                    )
                )
            )

            // Cahaya Radial lembut di belakang karakter (tepat di tengah sumbu AvatarView)
            val avatarCenter = Offset(w * 0.50f, h * 0.585f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentPalette.primaryColor.copy(alpha = (0.26f + gameHitFlash * 0.25f) * glowPulse),
                        CharStageDeep.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = avatarCenter,
                    radius = w * 0.50f
                ),
                radius = w * 0.50f,
                center = avatarCenter
            )

            // Panggung Elips Bercahaya Tempat Karakter Berdiri (mengikuti skala tinggi panggung baru)
            val platformY = h * 0.815f
            val outerPlatformH = 20.dp.toPx()
            val innerPlatformH = 11.dp.toPx()
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentPalette.primaryColor.copy(alpha = 0.54f * glowPulse),
                        accentPalette.glowColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.50f, platformY),
                    radius = w * 0.39f
                ),
                topLeft = Offset(w * 0.14f, platformY - outerPlatformH / 2f),
                size = Size(w * 0.72f, outerPlatformH)
            )
            drawOval(
                color = CharStagePlatformFill.copy(alpha = 0.88f),
                topLeft = Offset(w * 0.26f, platformY - innerPlatformH / 2f),
                size = Size(w * 0.48f, innerPlatformH)
            )
            drawOval(
                color = accentPalette.primaryColor,
                topLeft = Offset(w * 0.26f, platformY - innerPlatformH / 2f),
                size = Size(w * 0.48f, innerPlatformH),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Mini-Game: Tangkap Sinyal & Jaga Koherensi! (Cincin Target Resonansi Larmor + Pulsa FID)
            if (isCoherenceGameActive) {
                val minDim = min(w, h)
                val targetRadius = minDim * 0.34f
                val targetBandInner = targetRadius * 0.88f
                val inSweetSpot = gamePulseProgress in 0.70f..0.95f
                val pulseRadius = minDim * (0.10f + 0.31f * gamePulseProgress)

                // Cincin target luar
                drawCircle(
                    color = if (inSweetSpot) CharAccentMint.copy(alpha = 0.90f)
                    else accentPalette.primaryColor.copy(alpha = 0.45f),
                    radius = targetRadius,
                    center = avatarCenter,
                    style = Stroke(width = if (inSweetSpot) 3.5.dp.toPx() else 2.dp.toPx())
                )
                drawCircle(
                    color = accentPalette.glowColor.copy(alpha = 0.22f),
                    radius = targetBandInner,
                    center = avatarCenter,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Pulsa RF mengembang dari inti Nano ke cincin target
                drawCircle(
                    color = if (inSweetSpot) CharAccentMint else accentPalette.glowColor.copy(alpha = 0.85f),
                    radius = pulseRadius,
                    center = avatarCenter,
                    style = Stroke(width = 2.8.dp.toPx())
                )

                // 6 Titik Spin Proton mengelilingi cincin pulsa
                val spinAngleBase = rfWavePhase * 2f * PI.toFloat()
                for (i in 0 until 6) {
                    val ang = spinAngleBase + (i * (2f * PI.toFloat() / 6f))
                    val px = avatarCenter.x + cos(ang) * pulseRadius
                    val py = avatarCenter.y + sin(ang) * pulseRadius
                    drawCircle(
                        color = if (inSweetSpot) CharAccentMint else accentPalette.glowColor,
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // Gelombang Pemindaian Pulsa RF 90° / Animasi Gelombang Interaktif
            if (isWaveActive) {
                val scanRadius1 = (w * 0.12f) + rfWavePhase * (w * 0.36f)
                val scanRadius2 = (w * 0.12f) + ((rfWavePhase + 0.5f) % 1f) * (w * 0.36f)
                drawCircle(
                    color = accentPalette.glowColor.copy(alpha = (1f - rfWavePhase) * 0.75f),
                    radius = scanRadius1,
                    center = avatarCenter,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                    color = CharAccentMint.copy(alpha = (1f - ((rfWavePhase + 0.5f) % 1f)) * 0.55f),
                    radius = scanRadius2,
                    center = avatarCenter,
                    style = Stroke(width = 1.dp.toPx())
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
