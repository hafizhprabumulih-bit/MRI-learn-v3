package com.example.data

import androidx.compose.ui.graphics.Color

/**
 * Aturan buka-kunci berbasis kurikulum.
 * Saat ini [CharacterCatalog.ENFORCE_CURRICULUM_LOCKS] disetel ke `false`
 * sehingga seluruh item terbuka untuk pengujian dan eksplorasi penuh.
 * Nanti cukup ubah `ENFORCE_CURRICULUM_LOCKS = true` atau edit syarat di bawah
 * untuk mengaktifkan pembukaan bertahap per modul.
 */
data class UnlockRequirement(
    val requiredModuleId: String? = null,
    val requiredModuleTitle: String? = null,
    val requiredNodesCount: Int = 0,
    val futureNote: String = "Tersedia langsung"
)

data class CharacterItem(
    val id: String,
    val name: String,
    val category: CharacterCategory,
    val description: String,
    val clinicalBenefit: String = "",
    val badgeLabel: String = "MR-Safe",
    val previewColorHex: Long = 0xFF1FB6E8,
    val secondaryColorHex: Long = 0xFF7FDCD6,
    val drawableRes: Int? = null,
    val colorHex: String? = null,
    val unlockRequirement: UnlockRequirement = UnlockRequirement()
)

enum class CharacterCategory(val title: String, val subtitle: String) {
    KARAKTER("Karakter", "Pilih spesialis medis & radiologi"),
    RAMBUT("Kepala & Visor", "Pelindung kepala, antena & layar HUD"),
    SERAGAM("Seragam", "Jas lab, scrub klinis & rompi Zona IV"),
    HEADSET("Headset", "Peredam bising gradien & interkom konsol"),
    AKSESORIS("Alat Klinis", "Instrumen keselamatan & QA MRI"),
    LATAR("Latar Ruang", "Suasana ruang instalasi radiologi MRI")
}

data class CharacterAccentPalette(
    val id: String,
    val label: String,
    val primaryHex: Long,
    val glowHex: Long
) {
    val primaryColor: Color get() = Color(primaryHex)
    val glowColor: Color get() = Color(glowHex)
}

data class SpecialistProfile(
    val avatarId: String,
    val roleTitle: String,
    val zoneSpecialty: String,
    val favoriteSequence: String,
    val greetings: List<String>
)

data class CharacterCustomizationState(
    val avatarId: String = "radiografer_mri",
    val equippedHair: String = "hair_default",
    val equippedUniform: String = "uniform_scrub_mint",
    val equippedHeadset: String = "headset_radio",
    val equippedAccessory: String = "acc_magnet_detector",
    val backgroundId: String = "bg_control_room",
    val accentColor: String = "cyan"
)

object CharacterCatalog {
    /**
     * Ubah ke `true` di tahap pengembangan berikutnya bila ingin mengaktifkan
     * penguncian item berdasarkan penyelesaian modul kurikulum.
     */
    const val ENFORCE_CURRICULUM_LOCKS: Boolean = false

    val accentPalettes = listOf(
        CharacterAccentPalette("cyan", "Cyan Scanner", 0xFF1FB6E8, 0xFF7FDCD6),
        CharacterAccentPalette("mint", "Mint Klinis", 0xFF3CC4A3, 0xFF7FDCD6),
        CharacterAccentPalette("blue", "Biru Magnet", 0xFF1F4FB5, 0xFF55BBDC),
        CharacterAccentPalette("teal", "Teal Sinyal", 0xFF7FDCD6, 0xFF1FB6E8),
        CharacterAccentPalette("white", "Putih Steril", 0xFFE6F2FB, 0xFFBFD9EC),
        CharacterAccentPalette("amber", "Oranye RF", 0xFFFF8A1F, 0xFFFFC93C)
    )

    fun getAccentPalette(id: String): CharacterAccentPalette {
        return accentPalettes.find { it.id == id } ?: accentPalettes.first()
    }

    val specialistProfiles = mapOf(
        "radiografer_mri" to SpecialistProfile(
            avatarId = "radiografer_mri",
            roleTitle = "Radiografer MRI Senior",
            zoneSpecialty = "Operator Konsol Zona III & Positioning Zona IV",
            favoriteSequence = "T2 TSE & DWI EPI",
            greetings = listOf(
                "Pastikan skrining logam selesai sebelum pasien masuk ke Zona IV!",
                "Tip Konsol: Gandakan NEX untuk menaikkan SNR sebesar √2 (sekitar +41%).",
                "Ingat, medan magnet statis B₀ selalu aktif 24 jam meskipun tidak sedang memindai!",
                "Periksa koil kepala sudah terkunci rapat agar tidak muncul artefak shading."
            )
        ),
        "radiolog" to SpecialistProfile(
            avatarId = "radiolog",
            roleTitle = "Dokter Spesialis Radiologi",
            zoneSpecialty = "Ekspertise Diagnostik Multi-Sekuens & Kontras",
            favoriteSequence = "T1 Pre/Post-Gadolinium & FLAIR",
            greetings = listOf(
                "Bandingkan citra T1W, T2W, dan FLAIR secara berdampingan untuk karakterisasi lesi!",
                "Cairan bebas tampak gelap pada FLAIR karena sinyal CSF ditekan pada waktu inversi TI.",
                "Selalu periksa fungsi ginjal (eGFR) sebelum pemberian kontras berbasis Gadolinium.",
                "Hiperintens pada DWI disertai nilai gelap pada peta ADC menandakan restriksi difusi akut!"
            )
        ),
        "ahli_neuro_mri" to SpecialistProfile(
            avatarId = "ahli_neuro_mri",
            roleTitle = "Spesialis Neuro-MRI & fMRI",
            zoneSpecialty = "Pencitraan Sistem Saraf Pusat, Traktografi & BOLD",
            favoriteSequence = "3D TOF MRA, SWI & DTI",
            greetings = listOf(
                "Sekuens SWI sangat sensitif mendeteksi mikro-perdarahan dan deposit hemosiderin otak.",
                "Pada 3D Time-of-Flight (TOF) MRA, aliran darah arterial tampak terang tanpa injeksi kontras!",
                "Efek BOLD pada fMRI memanfaatkan perbedaan sifat magnetik oksihemoglobin vs deoksihemoglobin.",
                "Sejajarkan potongan aksial otak sejajar garis AC-PC (Anterior-Posterior Commissure)."
            )
        ),
        "fisikawan_medis" to SpecialistProfile(
            avatarId = "fisikawan_medis",
            roleTitle = "Fisikawan Medis MRI",
            zoneSpecialty = "Optimasi Pulsa RF, K-Space, Shimming & Kendali Mutu",
            favoriteSequence = "Spektroskopi MRS & B₀/B₁ Mapping",
            greetings = listOf(
                "Frekuensi presesi Larmor berbanding lurus dengan B₀: f₀ = γ · B₀ (63,87 MHz pada 1,5 Tesla).",
                "Pusat K-Space menentukan kontras citra, sedangkan tepi K-Space menentukan resolusi spasial!",
                "Hati-hati: SAR meningkat sebanding dengan kuadrat medan magnet (B₀²) dan kuadrat sudut balik (α²).",
                "Shimming magnet yang presisi adalah kunci keberhasilan fat-suppression spektral (CHESS)."
            )
        ),
        "petugas_proteksi_radiasi" to SpecialistProfile(
            avatarId = "petugas_proteksi_radiasi",
            roleTitle = "Petugas Keselamatan MRI (MRSO)",
            zoneSpecialty = "Manajemen Risiko Medan Magnet, SAR & Quench",
            favoriteSequence = "Mode Operasi Normal IEC (SAR ≤ 2 W/kg)",
            greetings = listOf(
                "Efek proyektil terjadi akibat gradien spasial medan magnet (dB/dx) di dekat mulut bore!",
                "Jangan biarkan kabel koil membentuk lengkungan (loop) yang menyentuh kulit pasien—risiko luka bakar RF!",
                "Jika terjadi Quench darurat, segera evakuasi pasien dan pantau ventilasi oksigen ruangan.",
                "Pasien dengan implan MR-Conditional wajib mengikuti batas B₀, dB/dx, dan SAR spesifik pabrik."
            )
        ),
        "ahli_jantung" to SpecialistProfile(
            avatarId = "ahli_jantung",
            roleTitle = "Spesialis Kardio-MRI (CMR)",
            zoneSpecialty = "Sinkronisasi EKG, Cine SSFP & Late Gadolinium (LGE)",
            favoriteSequence = "bSSFP Cine & Phase-Contrast Flow",
            greetings = listOf(
                "Gunakan sadapan EKG berbahan karbon non-feromagnetik untuk memicu akuisisi tepat pada fase R-R.",
                "Sekuens bSSFP (TrueFISP/FIESTA) memberikan kontras darah-miokardium tertinggi dengan TR sangat pendek!",
                "Teknik Late Gadolinium Enhancement (LGE) memilih TI untuk me-null-kan sinyal miokardium normal.",
                "Latih aba-aba tahan napas ekspirasi yang konsisten untuk mencegah artefak gerak respirasi."
            )
        ),
        "perawat" to SpecialistProfile(
            avatarId = "perawat",
            roleTitle = "Perawat Klinis Radiologi MRI",
            zoneSpecialty = "Asuhan Pasien, Penanganan Klaustrofobia & Akses IV",
            favoriteSequence = "Protokol Cepat Quiet-MRI & Motion-Free",
            greetings = listOf(
                "Komunikasi yang tenang dan tombol squeeze-ball di tangan pasien sangat membantu meredakan klaustrofobia!",
                "Pastikan seluruh pompa infus standar ditinggal di luar Zona IV atau gunakan pompa khusus MR-Safe.",
                "Pasang sumbat telinga (earplug) ditambah headset untuk meredam kebisingan akustik hingga 30 dB.",
                "Pantau saturasi oksigen pasien sedasi menggunakan pulse oximeter serat optik."
            )
        ),
        "dokter_bedah" to SpecialistProfile(
            avatarId = "dokter_bedah",
            roleTitle = "Dokter Bedah Intervensi iMRI",
            zoneSpecialty = "Navigasi Bedah Saraf & Biopsi Terpandu MRI",
            favoriteSequence = "3D T1 MPRAGE Isotropik & Real-Time Fluoroskopi",
            greetings = listOf(
                "Seluruh instrumen bedah di ruang iMRI terbuat dari titanium atau keramik non-feromagnetik!",
                "Akuisisi 3D voksels isotropik memungkinkan rekonstruksi multiplanar (MPR) tanpa distorsi tangga.",
                "Pencitraan intraoperatif memastikan batas reseksi tumor otak sudah bersih sebelum operasi selesai.",
                "Koordinasi erat antara tim bedah dan radiografer menjaga sterilitas sekaligus keselamatan magnet."
            )
        ),
        "dokter_robot" to SpecialistProfile(
            avatarId = "dokter_robot",
            roleTitle = "Asisten AI Rekonstruksi MRI",
            zoneSpecialty = "Deep Learning Denoising, Compressed Sensing & Otomasi",
            favoriteSequence = "CS-SENSE & AI Super-Resolution",
            greetings = listOf(
                "Halo! Algoritma AI Deep Learning mampu merekonstruksi citra tajam dari sampling K-Space yang dipercepat!",
                "Parallel Imaging (SENSE/GRAPPA) mengurangi langkah pengkodean fase dengan bantuan multi-koil.",
                "Compressed Sensing memanfaatkan sifat sparsity citra untuk memangkas waktu pemindaian hingga 50%.",
                "Ketuk item kustomisasi di bawah untuk menguji perlengkapan ruang kontrol MRI milikmu!"
            )
        )
    )

    fun getSpecialistProfile(avatarId: String): SpecialistProfile {
        return specialistProfiles[avatarId] ?: specialistProfiles.getValue("radiografer_mri")
    }

    val hairItems = listOf(
        CharacterItem(
            id = "hair_default",
            name = "Antena Larmor",
            category = CharacterCategory.RAMBUT,
            description = "Antena sensor presesi standar 63,87 MHz.",
            clinicalBenefit = "Kalibrasi frekuensi pusat f₀ otomatis",
            badgeLabel = "Standar",
            previewColorHex = 0xFF1FB6E8,
            secondaryColorHex = 0xFF7FDCD6,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "hair_visor",
            name = "Visor HUD K-Space",
            category = CharacterCategory.RAMBUT,
            description = "Layar optik transparan pemantau pengisian matriks K-Space.",
            clinicalBenefit = "Visualisasi lintasan fase & frekuensi langsung",
            badgeLabel = "HUD Optik",
            previewColorHex = 0xFF7FDCD6,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m01", "MO1: Fisika Dasar MRI", 5, "Syarat rencana: 5 simpul MO1")
        ),
        CharacterItem(
            id = "hair_cap",
            name = "Topi Bedah Steril",
            category = CharacterCategory.RAMBUT,
            description = "Penutup kepala medis higienis untuk tindakan intervensi MRI.",
            clinicalBenefit = "Standar higienitas ruang tindakan iMRI",
            badgeLabel = "Steril",
            previewColorHex = 0xFF3CC4A3,
            secondaryColorHex = 0xFFE6F2FB,
            unlockRequirement = UnlockRequirement("m02", "MO2: Relaksasi & Kontras", 5, "Syarat rencana: Selesaikan MO2")
        ),
        CharacterItem(
            id = "hair_goggles",
            name = "Kacamata Prisma fMRI",
            category = CharacterCategory.RAMBUT,
            description = "Kacamata periskop non-feromagnetik anti-klaustrofobia.",
            clinicalBenefit = "Melihat keluar bore magnet tanpa menggerakkan kepala",
            badgeLabel = "Non-Mag",
            previewColorHex = 0xFFFF8A1F,
            secondaryColorHex = 0xFFFFC93C,
            unlockRequirement = UnlockRequirement("m03a", "MO3A: Sekuens Spin Echo", 6, "Syarat rencana: 6 simpul MO3A")
        ),
        CharacterItem(
            id = "hair_cyber",
            name = "Halo Neural 7T",
            category = CharacterCategory.RAMBUT,
            description = "Cincin mahkota telemetri medan ultra-tinggi 7 Tesla.",
            clinicalBenefit = "Pemetaan homogenitas medan B₁+ presisi tinggi",
            badgeLabel = "Ultra 7T",
            previewColorHex = 0xFF55BBDC,
            secondaryColorHex = 0xFF1F4FB5,
            unlockRequirement = UnlockRequirement("m05", "MO5: Komponen Pesawat MRI", 8, "Syarat rencana: 8 simpul MO5")
        ),
        CharacterItem(
            id = "hair_crown_rf",
            name = "Helm Koil 64-Ch",
            category = CharacterCategory.RAMBUT,
            description = "Array koil kepala 64-kanal berdensitas tinggi untuk neuro-imaging.",
            clinicalBenefit = "SNR korteks serebri maksimal & akselerasi tinggi",
            badgeLabel = "64-Channel",
            previewColorHex = 0xFFE6F2FB,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m07", "MO7: Koil RF & Parallel Imaging", 8, "Syarat rencana: 8 simpul MO7")
        )
    )

    val uniformItems = listOf(
        CharacterItem(
            id = "uniform_scrub_mint",
            name = "Scrub Mint Klinis",
            category = CharacterCategory.SERAGAM,
            description = "Seragam harian operator konsol MRI tanpa kancing logam.",
            clinicalBenefit = "Bebas bahan feromagnetik & nyaman di Zona III–IV",
            badgeLabel = "Zona IV Safe",
            previewColorHex = 0xFF3CC4A3,
            secondaryColorHex = 0xFF7FDCD6,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "uniform_standard",
            name = "Jas Lab Radiologi",
            category = CharacterCategory.SERAGAM,
            description = "Jas putih resmi dokter radiolog dan fisikawan medis.",
            clinicalBenefit = "Kantong tanpa logam khusus kartu akses & dosimeter",
            badgeLabel = "Klinis",
            previewColorHex = 0xFFE6F2FB,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m01", "MO1: Fisika Dasar MRI", 10, "Syarat rencana: 10 simpul MO1")
        ),
        CharacterItem(
            id = "uniform_scrub_blue",
            name = "Scrub Bedah Biru",
            category = CharacterCategory.SERAGAM,
            description = "Pakaian tindakan steril ruang operasi & intervensi MRI.",
            clinicalBenefit = "Serat anti-statis untuk lingkungan magnet kuat",
            badgeLabel = "Bedah iMRI",
            previewColorHex = 0xFF1F4FB5,
            secondaryColorHex = 0xFF55BBDC,
            unlockRequirement = UnlockRequirement("m03b", "MO3B: Gradient Echo", 6, "Syarat rencana: 6 simpul MO3B")
        ),
        CharacterItem(
            id = "uniform_scrub_teal",
            name = "Scrub ICU Teal",
            category = CharacterCategory.SERAGAM,
            description = "Seragam tim anestesi dan perawat pemantau pasien kritis.",
            clinicalBenefit = "Visibilitas tinggi di ruang kontrol redup",
            badgeLabel = "Intensif",
            previewColorHex = 0xFF7FDCD6,
            secondaryColorHex = 0xFF3CC4A3,
            unlockRequirement = UnlockRequirement("m04", "MO4: Pengkodean Spasial & K-Space", 8, "Syarat rencana: 8 simpul MO4")
        ),
        CharacterItem(
            id = "uniform_apron_shield",
            name = "Rompi MRSO Oranye",
            category = CharacterCategory.SERAGAM,
            description = "Rompi pengawas keselamatan MRI (MR Safety Officer) berlini reflektif.",
            clinicalBenefit = "Identifikasi cepat komandan keselamatan Zona IV",
            badgeLabel = "MRSO Level 2",
            previewColorHex = 0xFFFF8A1F,
            secondaryColorHex = 0xFFFFC93C,
            unlockRequirement = UnlockRequirement("m09", "MO9: Keselamatan MRI", 6, "Syarat rencana: 6 simpul MO9")
        ),
        CharacterItem(
            id = "uniform_armor",
            name = "Suit Kryogenik 4K",
            category = CharacterCategory.SERAGAM,
            description = "Seragam pelindung teknisi helium cair (-269 °C / 4,2 Kelvin).",
            clinicalBenefit = "Proteksi termal saat pemeliharaan sistem superkonduktor",
            badgeLabel = "Helium 4,2K",
            previewColorHex = 0xFF1FB6E8,
            secondaryColorHex = 0xFF1F4FB5,
            unlockRequirement = UnlockRequirement("m05", "MO5: Komponen Pesawat MRI", 10, "Syarat rencana: Selesaikan MO5")
        )
    )

    val headsetItems = listOf(
        CharacterItem(
            id = "headset_none",
            name = "Tanpa Headset",
            category = CharacterCategory.HEADSET,
            description = "Mode komunikasi langsung untuk wawancara skrining di Zona II.",
            clinicalBenefit = "Optimal untuk edukasi & anamnesis pasien",
            badgeLabel = "Zona II",
            previewColorHex = 0xFF8FB3CC,
            secondaryColorHex = 0xFFBFD9EC,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "headset_radio",
            name = "Acoustic Guard 3T",
            category = CharacterCategory.HEADSET,
            description = "Headset peredam bising koil gradien hingga 30 dB (NRR).",
            clinicalBenefit = "Melindungi pendengaran dari hentakan gaya Lorentz",
            badgeLabel = "-30 dB NRR",
            previewColorHex = 0xFF1FB6E8,
            secondaryColorHex = 0xFF7FDCD6,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "headset_neural",
            name = "Interkom Konsol",
            category = CharacterCategory.HEADSET,
            description = "Headset operator dengan mikrofon terarah untuk instruksi tahan napas.",
            clinicalBenefit = "Aba-aba inspirasi/ekspirasi jernih ke dalam gantry",
            badgeLabel = "Dual-Mic",
            previewColorHex = 0xFF3CC4A3,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m06", "MO6: Gradien & Sistem RF", 5, "Syarat rencana: 5 simpul MO6")
        ),
        CharacterItem(
            id = "headset_pneumatic",
            name = "Audio Pneumatik",
            category = CharacterCategory.HEADSET,
            description = "Headset tabung udara akustik 100% bebas logam untuk di dalam bore.",
            clinicalBenefit = "Nol artefak suseptibilitas pada pemindaian otak",
            badgeLabel = "Air-Tube",
            previewColorHex = 0xFF7FDCD6,
            secondaryColorHex = 0xFFE6F2FB,
            unlockRequirement = UnlockRequirement("m08", "MO8: Artefak & Kendali Mutu", 6, "Syarat rencana: 6 simpul MO8")
        ),
        CharacterItem(
            id = "headset_holo",
            name = "Headset Telemetri SAR",
            category = CharacterCategory.HEADSET,
            description = "Headset pintar dengan indikator peringatan deposisi energi RF (W/kg).",
            clinicalBenefit = "Notifikasi real-time saat mendekati batas First-Level Mode",
            badgeLabel = "SAR Alert",
            previewColorHex = 0xFFFF8A1F,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m09", "MO9: Keselamatan MRI", 8, "Syarat rencana: 8 simpul MO9")
        )
    )

    val accessoryItems = listOf(
        CharacterItem(
            id = "acc_magnet_detector",
            name = "Detektor Feromagnetik",
            category = CharacterCategory.AKSESORIS,
            description = "Sensor genggam ultra-sensitif pendeteksi benda magnetis tersembunyi.",
            clinicalBenefit = "Mencegah insiden proyektil sebelum masuk Zona IV",
            badgeLabel = "Skrining",
            previewColorHex = 0xFF1FB6E8,
            secondaryColorHex = 0xFFFF8A1F,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "acc_badge_zona4",
            name = "Lencana MR Level 2",
            category = CharacterCategory.AKSESORIS,
            description = "Kartu otorisasi personel terlatih keselamatan medan magnet kuat.",
            clinicalBenefit = "Akses penuh Zona III & Zona IV",
            badgeLabel = "Level 2",
            previewColorHex = 0xFF3CC4A3,
            secondaryColorHex = 0xFF7FDCD6,
            unlockRequirement = UnlockRequirement("m01", "MO1: Fisika Dasar MRI", 5, "Syarat rencana: 5 simpul MO1")
        ),
        CharacterItem(
            id = "acc_phantom",
            name = "Phantom Kalibrasi ACR",
            category = CharacterCategory.AKSESORIS,
            description = "Silinder uji berisi larutan NiCl₂ & NaCl untuk QA harian pesawat MRI.",
            clinicalBenefit = "Uji akurasi geometrik, SNR, dan keseragaman intensitas",
            badgeLabel = "QA Phantom",
            previewColorHex = 0xFF55BBDC,
            secondaryColorHex = 0xFF1F4FB5,
            unlockRequirement = UnlockRequirement("m08", "MO8: Artefak & Kendali Mutu", 5, "Syarat rencana: 5 simpul MO8")
        ),
        CharacterItem(
            id = "acc_clipboard",
            name = "Tablet Protokol TR/TE",
            category = CharacterCategory.AKSESORIS,
            description = "Panel digital pengaturan waktu pengulangan (TR), waktu gema (TE), & TI.",
            clinicalBenefit = "Simulasi kontras T1, T2, PD, dan FLAIR secara instan",
            badgeLabel = "Parameter",
            previewColorHex = 0xFF7FDCD6,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m02", "MO2: Relaksasi & Kontras", 6, "Syarat rencana: 6 simpul MO2")
        ),
        CharacterItem(
            id = "acc_oximeter",
            name = "Oximeter Serat Optik",
            category = CharacterCategory.AKSESORIS,
            description = "Sensor SpO₂ nir-logam berbasis cahaya optik aman dari induksi RF.",
            clinicalBenefit = "Pemantauan vital pasien tanpa risiko antena pemanas",
            badgeLabel = "Fiber-Optic",
            previewColorHex = 0xFFFF8A1F,
            secondaryColorHex = 0xFF3CC4A3,
            unlockRequirement = UnlockRequirement("m09", "MO9: Keselamatan MRI", 5, "Syarat rencana: 5 simpul MO9")
        ),
        CharacterItem(
            id = "acc_coil_surface",
            name = "Koil Loop Permukaan",
            category = CharacterCategory.AKSESORIS,
            description = "Koil penerima RF lokal untuk anatomi superfisial (bahu, lutut, TMJ).",
            clinicalBenefit = "Sensitivitas tinggi sesuai rumus s = (D²/(D²+z²))^1,5",
            badgeLabel = "High-SNR",
            previewColorHex = 0xFFE6F2FB,
            secondaryColorHex = 0xFF1FB6E8,
            unlockRequirement = UnlockRequirement("m07", "MO7: Koil RF & Parallel Imaging", 5, "Syarat rencana: 5 simpul MO7")
        )
    )

    val backgroundItems = listOf(
        CharacterItem(
            id = "bg_control_room",
            name = "Ruang Kontrol MRI",
            category = CharacterCategory.LATAR,
            description = "Konsol operator Zona III dengan jendela scanner, jam digital merah & monitor otak.",
            clinicalBenefit = "Pusat kendali sekuens & pemantauan pasien",
            badgeLabel = "Zona III",
            previewColorHex = 0xFF1FB6E8,
            secondaryColorHex = 0xFF0E5F86,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "bg_changing_room",
            name = "Ruang Ganti & Loker",
            category = CharacterCategory.LATAR,
            description = "Ruang persiapan radiografer dengan loker penyimpanan barang magnetis & panggung.",
            clinicalBenefit = "Area wajib lepas jam tangan, kunci & kartu magnetik",
            badgeLabel = "Zona II",
            previewColorHex = 0xFF3CC4A3,
            secondaryColorHex = 0xFF0F2A45,
            unlockRequirement = UnlockRequirement(null, null, 0, "Terbuka sejak awal")
        ),
        CharacterItem(
            id = "bg_scanner_suite",
            name = "Ruang Magnet 3T",
            category = CharacterCategory.LATAR,
            description = "Suite gantry superkonduktor 3,0 Tesla di dalam perisai sangkar Faraday RF.",
            clinicalBenefit = "Medan B₀ = 3,0 T (f₀ = 127,74 MHz untuk ¹H)",
            badgeLabel = "Zona IV • 3T",
            previewColorHex = 0xFF1F4FB5,
            secondaryColorHex = 0xFF55BBDC,
            unlockRequirement = UnlockRequirement("m02", "MO2: Relaksasi & Kontras", 5, "Syarat rencana: Selesaikan MO2")
        ),
        CharacterItem(
            id = "bg_reading_room",
            name = "Ruang Baca PACS",
            category = CharacterCategory.LATAR,
            description = "Ruang diagnostik dokter radiolog dengan monitor kalibrasi DICOM Grayscale.",
            clinicalBenefit = "Analisis multi-sekuens T1, T2, FLAIR, DWI & MRA",
            badgeLabel = "Diagnostik",
            previewColorHex = 0xFFFF8A1F,
            secondaryColorHex = 0xFF1F4FB5,
            unlockRequirement = UnlockRequirement("m03a", "MO3A: Sekuens Spin Echo", 8, "Syarat rencana: Selesaikan MO3A")
        ),
        CharacterItem(
            id = "bg_physics_lab",
            name = "Lab Fisika & Spektroskopi",
            category = CharacterCategory.LATAR,
            description = "Laboratorium uji pulsa RF, kurva relaksasi Bloch T1/T2, dan osiloskop FID.",
            clinicalBenefit = "Eksperimen presesi spin & transformasi Fourier 2D",
            badgeLabel = "Lab NMR",
            previewColorHex = 0xFF7FDCD6,
            secondaryColorHex = 0xFF3CC4A3,
            unlockRequirement = UnlockRequirement("m04", "MO4: Pengkodean Spasial & K-Space", 8, "Syarat rencana: Selesaikan MO4")
        ),
        CharacterItem(
            id = "bg_led_stage",
            name = "Panggung Hologram K-Space",
            category = CharacterCategory.LATAR,
            description = "Panggung neon cyan futuristik dengan proyeksi kisi frekuensi spasial (kx, ky).",
            clinicalBenefit = "Simulasi pengisian Kartesian, Radial & Spiral",
            badgeLabel = "Holografik",
            previewColorHex = 0xFF55BBDC,
            secondaryColorHex = 0xFF7FDCD6,
            unlockRequirement = UnlockRequirement("m07", "MO7: Koil RF & Parallel Imaging", 8, "Syarat rencana: Selesaikan MO7")
        )
    )

    val allItems: List<CharacterItem> =
        hairItems + uniformItems + headsetItems + accessoryItems + backgroundItems

    fun getItem(id: String): CharacterItem? {
        return allItems.find { it.id == id }
    }

    /**
     * Menghitung apakah item sudah terbuka.
     * Selama [ENFORCE_CURRICULUM_LOCKS] bernilai `false`, fungsi ini selalu mengembalikan `true`
     * sehingga semua item dapat langsung dipilih oleh pengguna.
     */
    fun isItemUnlocked(
        item: CharacterItem,
        completedNodeCodes: Set<String> = emptySet(),
        modules: List<MriModule> = emptyList()
    ): Boolean {
        if (!ENFORCE_CURRICULUM_LOCKS) return true
        val req = item.unlockRequirement
        val moduleId = req.requiredModuleId ?: return true
        if (req.requiredNodesCount <= 0) return true
        val module = modules.find { it.id.equals(moduleId, ignoreCase = true) || it.code.equals(moduleId, ignoreCase = true) }
        val completedInModule = module?.nodes?.count { it.code in completedNodeCodes } ?: completedNodeCodes.size
        return completedInModule >= req.requiredNodesCount
    }
}
