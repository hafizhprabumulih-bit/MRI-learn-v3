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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
            val previewLoadout = remember(av.id) {
                AvatarLayerRegistry.defaultLoadoutForRole(av.id)
            }

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
                        AvatarView(
                            role = av.id,
                            equipped = previewLoadout,
                            expression = if (isSelected) Expression.SENANG else Expression.NETRAL,
                            modifier = Modifier
                                .size(70.dp)
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
    modifier: Modifier = Modifier
) {
    val itemPrimary = Color(item.previewColorHex)
    val itemSecondary = Color(item.secondaryColorHex)

    Card(
        modifier = modifier
            .width(196.dp)
            .alpha(if (isUnlocked) 1f else 0.55f)
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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CharAccentCyan else CharTextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // 2. Thumbnail Gambar PNG Layer Nano (atau Ikon Vektor untuk "Tanpa Headset")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharRoomBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) accentPalette.primaryColor else itemPrimary.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.drawableRes != null) {
                            val (scale, tx, ty) = when (item.category) {
                                CharacterCategory.RAMBUT -> Triple(2.0f, 0f, 18f)
                                CharacterCategory.SERAGAM -> Triple(1.9f, 0f, -18f)
                                CharacterCategory.HEADSET -> Triple(1.55f, 0f, 6f)
                                CharacterCategory.AKSESORIS -> Triple(2.1f, -24f, -20f)
                                else -> Triple(1.0f, 0f, 0f)
                            }
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
                    }

                    if (!isUnlocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CharActionOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(R.string.status_locked),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharActionOrange
                            )
                        }
                    } else {
                        Text(
                            text = if (isSelected) stringResource(R.string.char_item_equipped) else item.badgeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentPalette.primaryColor else CharTextDim
                        )
                    }
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

            // 5. Indikator Syarat Modul Kurikulum (dengan dukungan UI Terkunci & Terbuka)
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
                            .fillMaxWidth(
                                when {
                                    !isUnlocked -> 0.25f
                                    isSelected -> 1f
                                    else -> 0.65f
                                }
                            )
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) CharAccentMint else CharActionOrange)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CharActionOrange,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.char_item_unlock_fmt,
                            req.requiredNodesCount,
                            req.requiredModuleId.uppercase()
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            !isUnlocked -> CharActionOrange
                            isSelected -> CharAccentMint
                            else -> CharActionYellow
                        }
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
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (item.id) {
            "headset_none" -> {
                // Ikon Tanpa Headset (Siluet Headset dengan garis miring nonaktif)
                drawArc(
                    color = primary.copy(alpha = 0.65f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.18f, h * 0.16f),
                    size = Size(w * 0.64f, h * 0.56f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                drawLine(
                    color = secondary,
                    start = Offset(w * 0.20f, h * 0.80f),
                    end = Offset(w * 0.80f, h * 0.20f),
                    strokeWidth = 3.2f,
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
