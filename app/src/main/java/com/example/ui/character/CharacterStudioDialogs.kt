package com.example.ui.character

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.CharacterAccentPalette
import com.example.data.CharacterCatalog
import com.example.data.CharacterCustomizationState
import com.example.data.MedicalAvatar
import com.example.ui.theme.CharAccentCyan
import com.example.ui.theme.CharAccentMint
import com.example.ui.theme.CharActionDarkText
import com.example.ui.theme.CharActionOrange
import com.example.ui.theme.CharBorderSlate
import com.example.ui.theme.CharCardSurface
import com.example.ui.theme.CharStageBase
import com.example.ui.theme.CharStageDeep
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary
import java.io.File
import java.io.FileOutputStream

private fun expressionLabel(exp: Expression): String = when (exp) {
    Expression.NETRAL -> "Netral"
    Expression.SENANG -> "Senang"
    Expression.BERPIKIR -> "Berpikir"
    Expression.WASPADA -> "Waspada"
    Expression.BANGGA -> "Bangga"
    Expression.SEDIH -> "Sedih"
}

/**
 * Dialog Kartu Status Shift Nano (dibuka saat menekan & menahan avatar Nano / Long Press).
 */
@Composable
fun NanoShiftStatusDialog(
    avatarLabel: String,
    specialistRole: String,
    userName: String,
    completedCount: Int,
    totalNodesCount: Int,
    studyStreakDays: Int,
    shiftMood: Int,
    favoriteSequence: String,
    activeExpression: Expression,
    onSelectExpression: (Expression) -> Unit,
    onDismiss: () -> Unit
) {
    val masteryPct = if (totalNodesCount > 0) (completedCount * 100) / totalNodesCount else 0
    val displayName = userName.ifBlank { avatarLabel }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CharStageBase),
            border = BorderStroke(1.5.dp, CharAccentCyan.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = CharAccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Kartu Status Shift Nano",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = CharTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CharCardSurface,
                    border = BorderStroke(1.dp, CharBorderSlate)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$displayName · $specialistRole",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharAccentCyan
                        )
                        Text(
                            text = "Sekuens Favorit: $favoriteSequence",
                            fontSize = 11.sp,
                            color = CharTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Penguasaan Modul: $completedCount/$totalNodesCount ($masteryPct%)",
                                fontSize = 11.sp,
                                color = CharTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (masteryPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = CharAccentMint,
                            trackColor = CharStageDeep
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = CharStageDeep
                            ) {
                                Text(
                                    text = "Streak: $studyStreakDays hari",
                                    fontSize = 11.sp,
                                    color = if (studyStreakDays > 0) CharAccentMint else CharActionOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = CharStageDeep
                            ) {
                                Text(
                                    text = "Mood Shift: $shiftMood ♥",
                                    fontSize = 11.sp,
                                    color = CharAccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Ubah Ekspresi Visor Cepat:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharTextPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Expression.entries.forEach { exp ->
                        val isSel = exp == activeExpression
                        FilterChip(
                            selected = isSel,
                            onClick = { onSelectExpression(exp) },
                            label = {
                                Text(
                                    text = expressionLabel(exp),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CharAccentCyan,
                                selectedLabelColor = CharActionDarkText,
                                containerColor = CharCardSurface,
                                labelColor = CharTextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mode Foto / Studio Kartu Profil Radiografer (memilih ekspresi, latar, & pangkat lalu ekspor/bagikan PNG).
 */
@Composable
fun NanoSnapshotStudioDialog(
    workingState: CharacterCustomizationState,
    currentAvatar: MedicalAvatar,
    specialistRole: String,
    favoriteSequence: String,
    userName: String,
    accentPalette: CharacterAccentPalette,
    completedCount: Int,
    totalNodesCount: Int,
    onDismiss: () -> Unit,
    onExported: (String) -> Unit
) {
    val context = LocalContext.current
    var studioExpression by remember { mutableStateOf(Expression.BANGGA) }
    var studioBgId by remember { mutableStateOf(workingState.backgroundId) }
    val masteryPct = if (totalNodesCount > 0) (completedCount * 100) / totalNodesCount else 0
    val defaultRank = when {
        currentAvatar.id == "dosen" -> "Dosen Pembimbing"
        masteryPct >= 75 -> "Ahli Fisika MR"
        masteryPct >= 35 -> "Radiografer Klinis"
        else -> "Mahasiswa MRI"
    }
    val rankOptions = listOf("Mahasiswa MRI", "Radiografer Klinis", "Ahli Fisika MR", "Dosen Pembimbing")
    var selectedRank by remember { mutableStateOf(defaultRank) }

    val studioLoadout = remember(workingState, studioBgId) {
        workingState.copy(backgroundId = studioBgId)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CharStageBase),
            border = BorderStroke(1.5.dp, accentPalette.primaryColor.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Studio Foto Kartu Profil",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup Studio",
                            tint = CharTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Pratinjau Kartu Foto Bersih
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.05f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, accentPalette.primaryColor, RoundedCornerShape(18.dp))
                ) {
                    StageBackgroundCanvas(
                        backgroundId = studioBgId,
                        accentPalette = accentPalette,
                        glowPulse = 0.95f,
                        rfWavePhase = 0.35f,
                        isWaveActive = false
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CharStageDeep.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, accentPalette.primaryColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = selectedRank,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentPalette.glowColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "MRI EDU · $masteryPct%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharTextPrimary
                            )
                        }

                        AvatarView(
                            role = currentAvatar.id,
                            equipped = studioLoadout,
                            expression = studioExpression,
                            modifier = Modifier.size(172.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = userName.ifBlank { currentAvatar.label },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$specialistRole · $favoriteSequence",
                                fontSize = 10.sp,
                                color = CharTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Pilihan Ekspresi
                Text("Pilih Ekspresi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharTextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Expression.entries.forEach { exp ->
                        val isSel = exp == studioExpression
                        FilterChip(
                            selected = isSel,
                            onClick = { studioExpression = exp },
                            label = { Text(expressionLabel(exp), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentPalette.primaryColor,
                                selectedLabelColor = CharActionDarkText,
                                containerColor = CharCardSurface,
                                labelColor = CharTextSecondary
                            )
                        )
                    }
                }

                // Pilihan Latar
                Text("Pilih Latar Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharTextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CharacterCatalog.backgroundItems.forEach { bg ->
                        val isSel = bg.id == studioBgId
                        FilterChip(
                            selected = isSel,
                            onClick = { studioBgId = bg.id },
                            label = { Text(bg.name, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentPalette.primaryColor,
                                selectedLabelColor = CharActionDarkText,
                                containerColor = CharCardSurface,
                                labelColor = CharTextSecondary
                            )
                        )
                    }
                }

                // Pilihan Badge Pangkat
                Text("Badge Pangkat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharTextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rankOptions.forEach { rank ->
                        val isSel = rank == selectedRank
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedRank = rank },
                            label = { Text(rank, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CharAccentMint,
                                selectedLabelColor = CharActionDarkText,
                                containerColor = CharCardSurface,
                                labelColor = CharTextSecondary
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        shareAndSaveProfileCard(
                            context = context,
                            equipped = studioLoadout,
                            expression = studioExpression,
                            displayName = userName.ifBlank { currentAvatar.label },
                            specialistRole = specialistRole,
                            favoriteSequence = favoriteSequence,
                            rankBadge = selectedRank,
                            masteryPct = masteryPct,
                            accentColorArgb = accentPalette.primaryColor.toArgb()
                        )
                        onExported("Kartu profil ${currentAvatar.label} ($selectedRank) siap dibagikan!")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentPalette.primaryColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = CharActionDarkText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan & Bagikan Kartu PNG",
                        color = CharActionDarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun shareAndSaveProfileCard(
    context: Context,
    equipped: CharacterCustomizationState,
    expression: Expression,
    displayName: String,
    specialistRole: String,
    favoriteSequence: String,
    rankBadge: String,
    masteryPct: Int,
    accentColorArgb: Int
) {
    val width = 800
    val height = 960
    val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(outBitmap)

    canvas.drawColor(0xFF071A2B.toInt())

    // Gambar Latar
    val bgRes = CharacterCatalog.getItem(equipped.backgroundId)?.drawableRes ?: R.drawable.bg_control_room
    BitmapFactory.decodeResource(context.resources, bgRes)?.let { bgBmp ->
        val scale = maxOf(width.toFloat() / bgBmp.width, height.toFloat() / bgBmp.height)
        val drawW = bgBmp.width * scale
        val drawH = bgBmp.height * scale
        val left = (width - drawW) / 2f
        val top = height - drawH
        canvas.drawBitmap(bgBmp, null, RectF(left, top, left + drawW, top + drawH), Paint(Paint.FILTER_BITMAP_FLAG))
        bgBmp.recycle()
    }

    // Vignette gelap halus
    val overlayPaint = Paint().apply { color = 0x7A071A2B }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

    // Bingkai Kartu
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColorArgb
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawRoundRect(RectF(16f, 16f, width - 16f, height - 16f), 36f, 36f, borderPaint)

    // Panggung Elips
    val ovalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCC0C2640.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawOval(RectF(170f, 700f, 630f, 748f), ovalPaint)
    canvas.drawOval(RectF(170f, 700f, 630f, 748f), Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColorArgb
        style = Paint.Style.STROKE
        strokeWidth = 3f
    })

    // Tumpuk seluruh layer PNG Nano sesuai zOrder
    val layers = AvatarLayerRegistry.buildActiveLayers(equipped, expression)
    val avatarSide = 520f
    val avatarLeft = (width - avatarSide) / 2f
    val avatarTop = 210f
    val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    layers.forEach { layer ->
        val resId = layer.drawableRes ?: return@forEach
        BitmapFactory.decodeResource(context.resources, resId)?.let { bmp ->
            val layerSize = avatarSide * layer.anchor.sizeFraction
            val lx = avatarLeft + (avatarSide * layer.anchor.x) - (layerSize / 2f)
            val ly = avatarTop + (avatarSide * layer.anchor.y) - (layerSize / 2f)
            canvas.drawBitmap(bmp, null, RectF(lx, ly, lx + layerSize, ly + layerSize), bmpPaint)
            bmp.recycle()
        }
    }

    // Header Badge Pangkat & Progres
    val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE60B2239.toInt() }
    canvas.drawRoundRect(RectF(42f, 44f, 360f, 100f), 18f, 18f, badgeBg)
    val textBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF22D3EE.toInt()
        textSize = 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(rankBadge, 64f, 82f, textBadge)

    val pctPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 24f
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("MRI EDU · $masteryPct%", width - 48f, 82f, pctPaint)

    // Footer Identitas & Sekuens
    canvas.drawRoundRect(RectF(42f, 780f, width - 42f, height - 42f), 26f, 26f, badgeBg)
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(displayName.take(28), width / 2f, 840f, titlePaint)
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFA5C4DF.toInt()
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(specialistRole.take(44), width / 2f, 878f, subPaint)
    canvas.drawText("Sekuens: $favoriteSequence".take(48), width / 2f, 906f, subPaint)

    try {
        // 1. Simpan ke Galeri jika API 29+ (tanpa izin tambahan)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "nano_mri_card_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MRI-Learn")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    outBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }

        // 2. Bagikan lewat FileProvider cache
        val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
        val file = File(dir, "nano_profile_card.png")
        FileOutputStream(file).use { out ->
            outBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val shareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_TEXT, "Kartu Radiografer MRI EDU — $displayName ($rankBadge)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Kartu Profil Nano"))
    } catch (_: Exception) {
        Toast.makeText(context, "Kartu profil berhasil disiapkan.", Toast.LENGTH_SHORT).show()
    } finally {
        outBitmap.recycle()
    }
}
