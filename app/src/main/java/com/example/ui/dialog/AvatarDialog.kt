package com.example.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CharacterCustomizationState
import com.example.data.MriRepository
import com.example.ui.character.AvatarLayerRegistry
import com.example.ui.character.AvatarView
import com.example.ui.character.Expression
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AvatarDialog(
    currentAvatarId: String,
    currentName: String,
    characterCustomization: CharacterCustomizationState = CharacterCustomizationState(),
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var selectedId by rememberSaveable { mutableStateOf(currentAvatarId.ifBlank { "radiografer_mri" }) }
    var nameInput by rememberSaveable { mutableStateOf(currentName.take(40)) }

    val avatars = MriRepository.avatars
    val selectedPreviewLoadout = if (selectedId == characterCustomization.avatarId) {
        characterCustomization
    } else {
        AvatarLayerRegistry.defaultLoadoutForRole(
            avatarId = selectedId,
            currentBackgroundId = characterCustomization.backgroundId
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profil & Pilihan Avatar Medis",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pratinjau Avatar Nano Terpilih
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(CardSlate)
                        .border(3.dp, CyanBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        role = selectedId,
                        equipped = selectedPreviewLoadout,
                        expression = Expression.SENANG,
                        modifier = Modifier.size(74.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PILIH KARAKTER AVATAR MEDIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    avatars.forEach { (id, label, _) ->
                        val itemLoadout = if (id == characterCustomization.avatarId) {
                            characterCustomization
                        } else {
                            AvatarLayerRegistry.defaultLoadoutForRole(
                                avatarId = id,
                                currentBackgroundId = characterCustomization.backgroundId
                            )
                        }
                        AvatarOptionItem(
                            id = id,
                            label = label,
                            equipped = itemLoadout,
                            isSelected = selectedId == id,
                            onSelect = { selectedId = id }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 40) nameInput = it },
                    label = { Text("Nama Pengguna / Gelar (Maks. 40 karakter)", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Ahmad, S.Tr.Kes.(Rad) / dr. Sp.Rad", fontSize = 11.sp, color = TextMuted) },
                    supportingText = {
                        Text(
                            text = "${nameInput.length}/40 karakter",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeepObsidian,
                        unfocusedContainerColor = DeepObsidian,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = CardSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSave(selectedId, nameInput.trim().take(40)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Simpan Avatar", fontSize = 12.sp, color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarOptionItem(
    id: String,
    label: String,
    equipped: CharacterCustomizationState,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onSelect)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(CardSlate)
                .border(2.dp, if (isSelected) CyanBright else CardSlateBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AvatarView(
                role = id,
                equipped = equipped,
                expression = if (isSelected) Expression.BANGGA else Expression.NETRAL,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) CyanBright else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
