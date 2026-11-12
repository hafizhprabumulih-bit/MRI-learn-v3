package com.example.ui.character

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CharacterAccentPalette
import com.example.data.CharacterCatalog
import com.example.data.CharacterCategory
import com.example.data.CharacterItem
import com.example.data.MedicalAvatar
import com.example.ui.theme.CharAccentCyan
import com.example.ui.theme.CharAccentMint
import com.example.ui.theme.CharActionOrange
import com.example.ui.theme.CharActionYellow
import com.example.ui.theme.CharBorderSlate
import com.example.ui.theme.CharCardSelected
import com.example.ui.theme.CharCardSurface
import com.example.ui.theme.CharRoomBg
import com.example.ui.theme.CharStageDeep
import com.example.ui.theme.CharTabActiveText
import com.example.ui.theme.CharTextDim
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary

@Composable
fun SpecialistAvatarGrid(
    avatars: List<MedicalAvatar>,
    selectedAvatarId: String,
    accentPalette: CharacterAccentPalette,
    onSelectAvatar: (MedicalAvatar) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(avatars, key = { it.id }) { av ->
            val isSelected = selectedAvatarId == av.id

            Card(
                modifier = Modifier
                    .width(114.dp)
                    .clickable { onSelectAvatar(av) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CharCardSelected else CharCardSurface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accentPalette.primaryColor else CharBorderSlate
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharRoomBg.copy(alpha = 0.68f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = av.drawableRes),
                            contentDescription = av.label,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(accentPalette.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.char_item_equipped),
                                    tint = CharTabActiveText,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = av.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) accentPalette.glowColor else CharTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterItemCard(
    item: CharacterItem,
    isSelected: Boolean,
    isUnlocked: Boolean,
    accentPalette: CharacterAccentPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemPrimary = Color(item.previewColorHex)
    val itemSecondary = Color(item.secondaryColorHex)

    Card(
        modifier = modifier
            .width(196.dp)
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CharCardSelected else CharCardSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentPalette.primaryColor else CharBorderSlate
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 1. Thumbnail Latar Ruangan bila kategori adalah LATAR
            if (item.category == CharacterCategory.LATAR && item.drawableRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharRoomBg)
                        .border(1.dp, CharBorderSlate, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharStageDeep.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSelected) stringResource(R.string.char_item_equipped) else item.badgeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CharAccentCyan else CharTextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // 2. Ilustrasi Kustom Spesifik untuk Setiap Item Kepala, Seragam, Headset & Alat Klinis
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharRoomBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) accentPalette.primaryColor else itemPrimary.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CharacterItemVisualIcon(
                            item = item,
                            primary = itemPrimary,
                            secondary = itemSecondary
                        )
                    }

                    Text(
                        text = if (isSelected) stringResource(R.string.char_item_equipped) else item.badgeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentPalette.primaryColor else CharTextDim
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. Judul Item
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Deskripsi / Manfaat Klinis
            Text(
                text = item.clinicalBenefit.ifBlank { item.description },
                fontSize = 11.sp,
                color = CharTextSecondary,
                maxLines = 2,
                lineHeight = 14.sp,
                overflow = TextOverflow.Ellipsis
            )

            // 5. Indikator Rencana Syarat Modul Kurikulum (Tetap Terbuka untuk Pengujian)
            val req = item.unlockRequirement
            if (req.requiredModuleId != null && req.requiredNodesCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CharStageDeep)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isSelected) 1f else 0.65f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) CharAccentMint else CharActionOrange)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(
                        R.string.char_item_unlock_fmt,
                        req.requiredNodesCount,
                        req.requiredModuleId.uppercase()
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) CharAccentMint else CharActionYellow
                )
            }
        }
    }
}

@Composable
fun CharacterItemVisualIcon(
    item: CharacterItem,
    primary: Color,
    secondary: Color
) {
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (item.id) {
            // --- KEPALA & VISOR ---
            "hair_default" -> {
                // Antena Larmor Bulat
                drawLine(
                    color = primary,
                    start = Offset(cx, h * 0.78f),
                    end = Offset(cx, h * 0.30f),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = secondary, radius = w * 0.18f, center = Offset(cx, h * 0.24f))
                drawArc(
                    color = primary,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.18f, h * 0.62f),
                    size = Size(w * 0.64f, h * 0.32f),
                    style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                )
            }
            "hair_visor" -> {
                // Visor HUD K-Space dengan Kisi Matriks
                drawRoundRect(
                    color = primary.copy(alpha = 0.35f),
                    topLeft = Offset(w * 0.08f, h * 0.28f),
                    size = Size(w * 0.84f, h * 0.44f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = secondary,
                    topLeft = Offset(w * 0.08f, h * 0.28f),
                    size = Size(w * 0.84f, h * 0.44f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 2.8f)
                )
                drawLine(color = primary, start = Offset(w * 0.25f, cy), end = Offset(w * 0.75f, cy), strokeWidth = 2f)
            }
            "hair_cap" -> {
                // Topi Bedah Steril Medis
                drawArc(
                    color = primary,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.12f, h * 0.22f),
                    size = Size(w * 0.76f, h * 0.66f)
                )
                drawRoundRect(
                    color = secondary,
                    topLeft = Offset(w * 0.10f, h * 0.54f),
                    size = Size(w * 0.80f, h * 0.16f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            "hair_goggles" -> {
                // Kacamata Prisma fMRI
                drawCircle(
                    color = primary,
                    radius = w * 0.20f,
                    center = Offset(w * 0.30f, cy),
                    style = Stroke(width = 3.2f)
                )
                drawCircle(
                    color = primary,
                    radius = w * 0.20f,
                    center = Offset(w * 0.70f, cy),
                    style = Stroke(width = 3.2f)
                )
                drawLine(
                    color = secondary,
                    start = Offset(w * 0.48f, cy),
                    end = Offset(w * 0.52f, cy),
                    strokeWidth = 3f
                )
            }
            "hair_cyber" -> {
                // Halo Neural 7T Bercahaya
                drawOval(
                    color = primary,
                    topLeft = Offset(w * 0.08f, h * 0.26f),
                    size = Size(w * 0.84f, h * 0.34f),
                    style = Stroke(width = 3.6f)
                )
                drawCircle(color = secondary, radius = 4f, center = Offset(cx, h * 0.26f))
            }
            "hair_crown_rf" -> {
                // Helm Array Koil Kepala 64-Ch
                drawArc(
                    color = primary,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.12f, h * 0.16f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = Stroke(width = 3.5f)
                )
                drawLine(color = secondary, start = Offset(w * 0.35f, h * 0.22f), end = Offset(w * 0.35f, h * 0.65f), strokeWidth = 2.2f)
                drawLine(color = secondary, start = Offset(w * 0.65f, h * 0.22f), end = Offset(w * 0.65f, h * 0.65f), strokeWidth = 2.2f)
            }

            // --- SERAGAM & ROMPI ---
            "uniform_apron_shield" -> {
                // Rompi MRSO Oranye Reflektif
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(w * 0.18f, h * 0.14f),
                    size = Size(w * 0.64f, h * 0.72f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRect(
                    color = secondary,
                    topLeft = Offset(w * 0.18f, h * 0.58f),
                    size = Size(w * 0.64f, h * 0.12f)
                )
            }
            "uniform_armor" -> {
                // Suit Kryogenik Helium 4,2K
                drawRoundRect(
                    color = primary.copy(alpha = 0.4f),
                    topLeft = Offset(w * 0.15f, h * 0.15f),
                    size = Size(w * 0.70f, h * 0.70f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawCircle(color = secondary, radius = w * 0.18f, center = Offset(cx, cy), style = Stroke(width = 3f))
            }
            else -> {
                if (item.category == CharacterCategory.SERAGAM) {
                    // Scrub Klinis / Jas Lab dengan Kerah V
                    val path = Path().apply {
                        moveTo(w * 0.28f, h * 0.16f)
                        lineTo(w * 0.08f, h * 0.38f)
                        lineTo(w * 0.22f, h * 0.48f)
                        lineTo(w * 0.22f, h * 0.86f)
                        lineTo(w * 0.78f, h * 0.86f)
                        lineTo(w * 0.78f, h * 0.48f)
                        lineTo(w * 0.92f, h * 0.38f)
                        lineTo(w * 0.72f, h * 0.16f)
                        lineTo(cx, h * 0.36f)
                        close()
                    }
                    drawPath(path = path, color = primary.copy(alpha = 0.35f))
                    drawPath(path = path, color = primary, style = Stroke(width = 2.6f))
                    drawCircle(color = secondary, radius = 3.5f, center = Offset(w * 0.63f, h * 0.54f))
                } else if (item.category == CharacterCategory.HEADSET) {
                    // Headset Peredam Akustik / Interkom / Pneumatik
                    drawArc(
                        color = primary,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.18f, h * 0.14f),
                        size = Size(w * 0.64f, h * 0.60f),
                        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                    )
                    drawRoundRect(
                        color = secondary,
                        topLeft = Offset(w * 0.08f, h * 0.44f),
                        size = Size(w * 0.22f, h * 0.36f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    drawRoundRect(
                        color = secondary,
                        topLeft = Offset(w * 0.70f, h * 0.44f),
                        size = Size(w * 0.22f, h * 0.36f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    if (item.id == "headset_neural") {
                        // Boom Microphone
                        drawLine(
                            color = primary,
                            start = Offset(w * 0.20f, h * 0.76f),
                            end = Offset(cx, h * 0.88f),
                            strokeWidth = 2.8f,
                            cap = StrokeCap.Round
                        )
                    }
                } else if (item.category == CharacterCategory.AKSESORIS) {
                    when (item.id) {
                        "acc_magnet_detector" -> {
                            // Tongkat Detektor Feromagnetik
                            drawLine(
                                color = primary,
                                start = Offset(w * 0.22f, h * 0.78f),
                                end = Offset(w * 0.72f, h * 0.28f),
                                strokeWidth = 5f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(color = secondary, radius = 5f, center = Offset(w * 0.76f, h * 0.24f))
                        }
                        "acc_badge_zona4" -> {
                            // Kartu Lencana MR Safety Level 2
                            drawRoundRect(
                                color = primary.copy(alpha = 0.3f),
                                topLeft = Offset(w * 0.18f, h * 0.22f),
                                size = Size(w * 0.64f, h * 0.58f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = primary,
                                topLeft = Offset(w * 0.18f, h * 0.22f),
                                size = Size(w * 0.64f, h * 0.58f),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 2.6f)
                            )
                            drawCircle(color = secondary, radius = 4.5f, center = Offset(cx, cy))
                        }
                        "acc_phantom" -> {
                            // Silinder Phantom Kalibrasi ACR
                            drawCircle(
                                color = primary.copy(alpha = 0.32f),
                                radius = w * 0.34f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = primary,
                                radius = w * 0.34f,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.8f)
                            )
                            drawLine(color = secondary, start = Offset(w * 0.24f, cy), end = Offset(w * 0.76f, cy), strokeWidth = 2f)
                            drawLine(color = secondary, start = Offset(cx, h * 0.24f), end = Offset(cx, h * 0.76f), strokeWidth = 2f)
                        }
                        "acc_clipboard" -> {
                            // Tablet Protokol TR/TE
                            drawRoundRect(
                                color = primary,
                                topLeft = Offset(w * 0.20f, h * 0.14f),
                                size = Size(w * 0.60f, h * 0.72f),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 2.8f)
                            )
                            drawLine(color = secondary, start = Offset(w * 0.32f, h * 0.38f), end = Offset(w * 0.68f, h * 0.38f), strokeWidth = 2.5f)
                            drawLine(color = secondary, start = Offset(w * 0.32f, h * 0.56f), end = Offset(w * 0.68f, h * 0.56f), strokeWidth = 2.5f)
                        }
                        "acc_oximeter" -> {
                            // Pulse Oximeter Serat Optik dengan Gelombang SpO2
                            val wave = Path().apply {
                                moveTo(w * 0.12f, cy)
                                lineTo(w * 0.34f, cy)
                                lineTo(w * 0.44f, h * 0.22f)
                                lineTo(w * 0.56f, h * 0.78f)
                                lineTo(w * 0.66f, cy)
                                lineTo(w * 0.88f, cy)
                            }
                            drawPath(path = wave, color = primary, style = Stroke(width = 3f, cap = StrokeCap.Round))
                        }
                        else -> {
                            // Koil Loop Permukaan RF
                            drawCircle(
                                color = primary,
                                radius = w * 0.30f,
                                center = Offset(cx, cy - 2f),
                                style = Stroke(width = 3.4f)
                            )
                            drawLine(color = secondary, start = Offset(cx, h * 0.74f), end = Offset(cx, h * 0.92f), strokeWidth = 3.5f)
                        }
                    }
                } else {
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(primary, secondary)),
                        topLeft = Offset(w * 0.10f, h * 0.18f),
                        size = Size(w * 0.80f, h * 0.64f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }
    }
}
