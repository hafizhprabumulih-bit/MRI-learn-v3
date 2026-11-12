package com.example.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CharacterAccentPalette
import com.example.data.CharacterCategory
import com.example.ui.theme.CharBorderSlate
import com.example.ui.theme.CharCardSurface
import com.example.ui.theme.CharTabActiveText
import com.example.ui.theme.CharTextPrimary
import com.example.ui.theme.CharTextSecondary

enum class CharacterTabGroup(val titleRes: Int) {
    KARAKTER(R.string.char_tab_group_karakter),
    TAMPILAN(R.string.char_tab_group_tampilan),
    LATAR_WARNA(R.string.char_tab_group_latar)
}

@Composable
fun CharacterCategoryTabs(
    selectedCategory: CharacterCategory,
    accentPalette: CharacterAccentPalette,
    onSelectCategory: (CharacterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeGroup = when (selectedCategory) {
        CharacterCategory.KARAKTER -> CharacterTabGroup.KARAKTER
        CharacterCategory.LATAR -> CharacterTabGroup.LATAR_WARNA
        else -> CharacterTabGroup.TAMPILAN
    }

    val appearanceSubCategories = listOf(
        CharacterCategory.RAMBUT to R.string.char_subtab_kepala,
        CharacterCategory.SERAGAM to R.string.char_subtab_seragam,
        CharacterCategory.HEADSET to R.string.char_subtab_headset,
        CharacterCategory.AKSESORIS to R.string.char_subtab_alat
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Segmented Group Bar: Karakter | Tampilan | Latar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CharCardSurface)
                .border(1.dp, CharBorderSlate, RoundedCornerShape(18.dp))
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CharacterTabGroup.entries.forEach { group ->
                val isGroupActive = activeGroup == group
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isGroupActive) accentPalette.primaryColor else Color.Transparent)
                        .clickable {
                            when (group) {
                                CharacterTabGroup.KARAKTER -> onSelectCategory(CharacterCategory.KARAKTER)
                                CharacterTabGroup.TAMPILAN -> {
                                    if (selectedCategory == CharacterCategory.KARAKTER || selectedCategory == CharacterCategory.LATAR) {
                                        onSelectCategory(CharacterCategory.RAMBUT)
                                    }
                                }
                                CharacterTabGroup.LATAR_WARNA -> onSelectCategory(CharacterCategory.LATAR)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(group.titleRes),
                        fontSize = 13.sp,
                        fontWeight = if (isGroupActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isGroupActive) CharTabActiveText else CharTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. Sub-Tab Kategori Tampilan (Kepala | Seragam | Headset | Alat Klinis)
        if (activeGroup == CharacterTabGroup.TAMPILAN) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                appearanceSubCategories.forEach { (cat, labelRes) ->
                    val isSelected = selectedCategory == cat
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectCategory(cat) }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) accentPalette.primaryColor else CharTextSecondary
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) accentPalette.primaryColor else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
