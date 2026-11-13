package com.example.ui.character

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CharacterAccentPalette
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
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary
import kotlinx.coroutines.delay

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
            val previewLoadout = remember(av.id) {
                AvatarLayerRegistry.defaultLoadoutForRole(av.id)
            }
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            var popTrigger by remember { mutableStateOf(false) }

            LaunchedEffect(popTrigger) {
                if (popTrigger) {
                    delay(180)
                    popTrigger = false
                }
            }

            val cardScale by animateFloatAsState(
                targetValue = when {
                    isPressed -> 0.94f
                    popTrigger -> 1.05f
                    else -> 1f
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "specialist_card_scale"
            )

            Card(
                modifier = Modifier
                    .width(120.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        popTrigger = true
                        onSelectAvatar(av)
                    },
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
                            .height(86.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharRoomBg.copy(alpha = 0.68f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarView(
                            role = av.id,
                            equipped = previewLoadout,
                            expression = if (isSelected) Expression.SENANG else Expression.NETRAL,
                            modifier = Modifier
                                .size(78.dp)
                                .padding(2.dp)
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
    modifier: Modifier = Modifier,
    unlockProgress: Float = 0f
) {
    val itemPrimary = Color(item.previewColorHex)
    val itemSecondary = Color(item.secondaryColorHex)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var popTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(popTrigger) {
        if (popTrigger) {
            delay(180)
            popTrigger = false
        }
    }

    val cardScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            popTrigger -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "item_card_scale"
    )

    Card(
        modifier = modifier
            .width(168.dp)
            .alpha(if (isUnlocked) 1f else 0.55f)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clickable(
                enabled = isUnlocked,
                interactionSource = interactionSource,
                indication = null
            ) {
                popTrigger = true
                onClick()
            },
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
                .padding(8.dp)
        ) {
            // 1. Kotak Pratinjau Besar (Latar Ruangan maupun Item Tampilan Nano)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharRoomBg)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) accentPalette.primaryColor else CharBorderSlate,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.category == CharacterCategory.LATAR && item.drawableRes != null) {
                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                } else if (item.drawableRes != null) {
                    val density = LocalDensity.current
                    val wPx = with(density) { maxWidth.toPx() }
                    val hPx = with(density) { maxHeight.toPx() }
                    val imgSide = minOf(wPx, hPx)

                    // Titik fokus (fx, fy) dalam koordinat normalisasi 0f..1f agar item tampil pas & jelas di tengah kotak
                    val (scale, fx, fy) = when (item.category) {
                        CharacterCategory.RAMBUT -> Triple(2.05f, 0.50f, 0.22f)
                        CharacterCategory.SERAGAM -> Triple(2.15f, 0.50f, 0.71f)
                        CharacterCategory.HEADSET -> Triple(1.45f, 0.50f, 0.36f)
                        CharacterCategory.AKSESORIS -> Triple(2.35f, 0.78f, 0.74f)
                        else -> Triple(1.0f, 0.50f, 0.50f)
                    }
                    val tx = (0.5f - fx) * imgSide * scale
                    val ty = (0.5f - fy) * imgSide * scale

                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = tx,
                                translationY = ty
                            ),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CharacterItemVisualIcon(
                        item = item,
                        primary = itemPrimary,
                        secondary = itemSecondary
                    )
                }

                // Badge status ("Dipakai" / Label / "Terkunci") di pojok kanan atas pratinjau
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharStageDeep.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (!isUnlocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CharActionOrange,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = stringResource(R.string.status_locked),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharActionOrange
                            )
                        }
                    } else {
                        Text(
                            text = if (isSelected) stringResource(R.string.char_item_equipped) else item.badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CharAccentCyan else CharTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Judul Item (Tanpa teks penjelasan panjang agar kartu ringkas & fokus)
            Text(
                text = item.name,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 3. Indikator Syarat Modul Kurikulum (hanya bila item dikunci oleh modul)
            val req = item.unlockRequirement
            if (!isUnlocked && req.requiredModuleId != null && req.requiredNodesCount > 0) {
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
                            .fillMaxWidth(unlockProgress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CharActionOrange)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CharActionOrange,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.char_item_unlock_fmt,
                            req.requiredNodesCount,
                            req.requiredModuleId.uppercase()
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) CharAccentMint else CharActionYellow
                    )
                }
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
    Canvas(modifier = Modifier.size(44.dp)) {
        val w = size.width
        val h = size.height

        when (item.id) {
            "headset_none" -> {
                drawArc(
                    color = primary.copy(alpha = 0.72f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.18f, h * 0.16f),
                    size = Size(w * 0.64f, h * 0.56f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
                drawLine(
                    color = secondary,
                    start = Offset(w * 0.20f, h * 0.80f),
                    end = Offset(w * 0.80f, h * 0.20f),
                    strokeWidth = 3.8f,
                    cap = StrokeCap.Round
                )
            }
            else -> {
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
