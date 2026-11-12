package com.example.ui.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharacterCatalog
import com.example.data.CharacterCategory
import com.example.data.CharacterCustomizationState
import com.example.data.CharacterItem
import com.example.data.MriRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSlate
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
import com.example.viewmodel.MainTab
import com.example.viewmodel.MriUiState
import com.example.viewmodel.MriViewModel

@Composable
fun CharacterRoomScreen(
    state: MriUiState,
    viewModel: MriViewModel
) {
    var workingState by remember(state.characterCustomization) {
        mutableStateOf(state.characterCustomization)
    }
    var selectedCategory by rememberSaveable { mutableStateOf(CharacterCategory.RAMBUT) }
    var showAvatarSelector by rememberSaveable { mutableStateOf(false) }

    val avatars = MriRepository.avatars

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Judul
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RUANG KARAKTER MRI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanBright,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Kustomisasi Avatar & Panggung",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            // Tombol Reset
            OutlinedButton(
                onClick = {
                    viewModel.resetCharacterCustomization()
                    workingState = CharacterCustomizationState()
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Kustomisasi", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Panggung Bercahaya / Preview Ruang Kontrol MRI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A192F),
                            Color(0xFF0F2A45),
                            Color(0xFF071A2B)
                        )
                    )
                )
                .border(1.dp, CyanBright.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Latar belakang scene
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
                    Box(
                        modifier = Modifier
                            .background(CyanDark.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Latar: ${CharacterCatalog.getItem(workingState.backgroundId)?.name ?: "Ruang Kontrol"}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBright
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(AmberWarning.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, AmberWarning.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "03:00 PM • 1.5T",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                    }
                }

                // Avatar Tengah dengan Panggung Bercahaya
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CyanBright.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .border(3.dp, CyanPrimary, CircleShape)
                        .clickable { showAvatarSelector = !showAvatarSelector },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = MriRepository.getAvatarDrawableRes(workingState.avatarId)),
                        contentDescription = "Avatar Pengguna",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = state.userName.ifBlank { "Dr. Radiografer MRI" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pemilih Cepat Avatar (jika dibuka)
        if (showAvatarSelector) {
            Text(
                text = "PILIH KARAKTER DASAR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                avatars.forEach { av ->
                    val isSelected = workingState.avatarId == av.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyanDark else CardSlate)
                            .border(1.dp, if (isSelected) CyanBright else CardSlateBorder, RoundedCornerShape(12.dp))
                            .clickable { workingState = workingState.copy(avatarId = av.id) }
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = av.drawableRes),
                            contentDescription = av.label,
                            modifier = Modifier
                               .size(40.dp)
                               .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = av.label,
                            fontSize = 9.sp,
                            color = if (isSelected) CyanBright else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Kategori Tabs (Rambut, Seragam, Headset, Latar)
        ScrollableTabRow(
            selectedTabIndex = CharacterCategory.entries.indexOf(selectedCategory),
            containerColor = CardSlate,
            contentColor = CyanBright,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            CharacterCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyanDark else Color.Transparent),
                    text = {
                        Text(
                            text = category.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyanBright else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid Item Kustomisasi
        val currentItems = when (selectedCategory) {
            CharacterCategory.RAMBUT -> CharacterCatalog.hairItems
            CharacterCategory.SERAGAM -> CharacterCatalog.uniformItems
            CharacterCategory.HEADSET -> CharacterCatalog.headsetItems
            CharacterCategory.LATAR -> CharacterCatalog.backgroundItems
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentItems) { item ->
                    val isSelected = when (selectedCategory) {
                        CharacterCategory.RAMBUT -> workingState.equippedHair == item.id
                        CharacterCategory.SERAGAM -> workingState.equippedUniform == item.id
                        CharacterCategory.HEADSET -> workingState.equippedHeadset == item.id
                        CharacterCategory.LATAR -> workingState.backgroundId == item.id
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                workingState = when (selectedCategory) {
                                    CharacterCategory.RAMBUT -> workingState.copy(equippedHair = item.id)
                                    CharacterCategory.SERAGAM -> workingState.copy(equippedUniform = item.id)
                                    CharacterCategory.HEADSET -> workingState.copy(equippedHeadset = item.id)
                                    CharacterCategory.LATAR -> workingState.copy(backgroundId = item.id)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) CyanDark.copy(alpha = 0.5f) else CardSlate),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CyanBright else CardSlateBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CyanBright else DarkNavySlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Dipilih",
                                            tint = DeepObsidian,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = item.name.take(1),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(EmeraldSuccess.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Tersedia", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 2,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tombol Simpan dan Kembali
        Button(
            onClick = {
                viewModel.updateCharacterCustomization(workingState)
                viewModel.selectTab(MainTab.BERANDA)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan", tint = DeepObsidian, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simpan dan Kembali ke Beranda",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DeepObsidian
            )
        }
    }
}
