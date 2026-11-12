package com.example.data

data class CharacterItem(
    val id: String,
    val name: String,
    val category: CharacterCategory,
    val description: String,
    val drawableRes: Int? = null,
    val colorHex: String? = null
)

enum class CharacterCategory(val title: String) {
    RAMBUT("Rambut / Gaya"),
    SERAGAM("Seragam Medis"),
    HEADSET("Headset MRI"),
    LATAR("Latar Ruang")
}

data class CharacterCustomizationState(
    val avatarId: String = "radiografer_mri",
    val equippedHair: String = "hair_default",
    val equippedUniform: String = "uniform_standard",
    val equippedHeadset: String = "headset_none",
    val backgroundId: String = "bg_control_room",
    val accentColor: String = "cyan"
)

object CharacterCatalog {
    val hairItems = listOf(
        CharacterItem("hair_default", "Natural", CharacterCategory.RAMBUT, "Gaya rambut natural standar."),
        CharacterItem("hair_visor", "Visor Radiologi", CharacterCategory.RAMBUT, "Visor pelindung klinis modern."),
        CharacterItem("hair_cap", "Topi Medis Steril", CharacterCategory.RAMBUT, "Topi bedah higienis ruang steril."),
        CharacterItem("hair_cyber", "Cyber Sleek", CharacterCategory.RAMBUT, "Gaya rambut futuristik neural.")
    )

    val uniformItems = listOf(
        CharacterItem("uniform_standard", "Jas Putih Klinis", CharacterCategory.SERAGAM, "Jas lab standar radiologi medis."),
        CharacterItem("uniform_scrub_blue", "Scrub Bedah Biru", CharacterCategory.SERAGAM, "Pakaian steril ruang operasi."),
        CharacterItem("uniform_scrub_teal", "Scrub ICU Teal", CharacterCategory.SERAGAM, "Pakaian tindakan klinis intensif."),
        CharacterItem("uniform_armor", "Cyber Armor Lab", CharacterCategory.SERAGAM, "Seragam protektif medan magnet tinggi.")
    )

    val headsetItems = listOf(
        CharacterItem("headset_none", "Tanpa Headset", CharacterCategory.HEADSET, "Telinga terbuka tanpa peredam."),
        CharacterItem("headset_radio", "Acoustic Guard", CharacterCategory.HEADSET, "Peredam bising pemindai MRI 3T."),
        CharacterItem("headset_neural", "Neural Comms", CharacterCategory.HEADSET, "Headset komunikasi interaktif ruang kontrol.")
    )

    val backgroundItems = listOf(
        CharacterItem("bg_control_room", "Ruang Kontrol MRI", CharacterCategory.LATAR, "Konsol pemindai, jam digital merah, & monitor citra."),
        CharacterItem("bg_scanner_suite", "Suite Magnet 3T", CharacterCategory.LATAR, "Gantry magnet superkonduktor & lampu amber."),
        CharacterItem("bg_led_stage", "Panggung Neon Cyan", CharacterCategory.LATAR, "Panggung holografik futuristik MRI Edu.")
    )

    val allItems = hairItems + uniformItems + headsetItems + backgroundItems

    fun getItem(id: String): CharacterItem? {
        return allItems.find { it.id == id }
    }
}
