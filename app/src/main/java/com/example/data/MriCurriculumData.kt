package com.example.data

import com.example.R
import com.example.data.local.CurriculumNodeEntity
import com.example.data.local.CurriculumQuizEntity
import com.example.data.local.MriModuleEntity
import com.example.data.local.MriNodeEntity
import com.example.data.local.QuizQuestionEntity
import org.json.JSONArray

data class MedicalAvatar(
    val id: String,
    val label: String,
    val drawableRes: Int
)

data class MriFlashcard(
    val id: String,
    val moduleId: String,
    val nodeCode: String,
    val category: String, // "SIMPUL" atau "GLOSARIUM"
    val frontTitle: String,
    val frontSubtitle: String,
    val backContent: String,
    val clinicalAnalogyOrTip: String
) {
    val isGlossaryCard: Boolean
        get() = id.startsWith("GLOSS_") ||
            id.startsWith("fc_gloss_") ||
            category.contains("GLOSARIUM", ignoreCase = true)

    val glossaryTermKey: String
        get() = when {
            id.startsWith("GLOSS_") -> id.removePrefix("GLOSS_")
            else -> frontTitle.substringBefore(" (").trim()
        }
}

data class TissueProperties(
    val id: String,
    val name: String,
    val t1Ms: Float,
    val t2Ms: Float,
    val protonDensity: Float,
    val description: String,
    val clinicalNote: String
)

data class MriNode(
    val code: String,
    val level: Int, // 0 = Tujuan, 1 = T1 (Dasar), 2 = T2 (Menengah), 3 = T3 (Lanjut)
    val title: String,
    val summaryShort: String,
    val detailFull: String,
    val analogi: String,
    val prerequisite: String? = null,
    val learningGoal: String? = null,
    val crossLinks: String? = null,
    val labType: String? = null,
    val sortOrder: Int = 0,
    val htmlSimulationAsset: String? = null
)

data class MriModule(
    val id: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val levelBadge: String,
    val iconName: String,
    val nodes: List<MriNode>,
    val sortOrder: Int = 0
)

data class GlossaryItem(
    val term: String,
    val symbolOrAbbr: String,
    val moduleId: String,
    val nodeCode: String,
    val definition: String
)

data class StudyNote(
    val id: String,
    val title: String,
    val content: String,
    val category: String, // "Protokol", "Fisika", "Artefak", "Keselamatan", "Catatan Bebas"
    val dateString: String
)

data class QuizQuestion(
    val id: String,
    val moduleId: String,
    val nodeCode: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val levelBadge: String = "Dasar",
    val level: Int = 1 // 1 = Tingkat 1 (Dasar), 2 = Tingkat 2 (Menengah), 3 = Tingkat 3 (Lanjut)
)

object MriRepository {

    // Katalog Terpusat 9 Avatar Medis (Single Source of Truth)
    val avatars = listOf(
        MedicalAvatar("radiografer_mri", "Radiografer", R.drawable.avatar_radiografer_mri),
        MedicalAvatar("radiolog", "Radiolog", R.drawable.avatar_radiolog),
        MedicalAvatar("ahli_neuro_mri", "Ahli Neuro", R.drawable.avatar_ahli_neuro_mri),
        MedicalAvatar("fisikawan_medis", "Fisikawan", R.drawable.avatar_fisikawan_medis),
        MedicalAvatar("petugas_proteksi_radiasi", "MRSO", R.drawable.avatar_petugas_proteksi_radiasi),
        MedicalAvatar("ahli_jantung", "Kardiolog", R.drawable.avatar_ahli_jantung),
        MedicalAvatar("perawat", "Perawat", R.drawable.avatar_perawat),
        MedicalAvatar("dokter_bedah", "Bedah", R.drawable.avatar_dokter_bedah),
        MedicalAvatar("dokter_robot", "AI Doctor", R.drawable.avatar_dokter_robot)
    )

    fun getAvatarDrawableRes(avatarId: String): Int {
        return avatars.find { it.id == avatarId }?.drawableRes ?: R.drawable.avatar_radiografer_mri
    }

    // Konstanta Giromagnetik (MHz / Tesla)
    const val GAMMA_1H = 42.58f
    const val GAMMA_13C = 10.71f
    const val GAMMA_19F = 40.08f
    const val GAMMA_23NA = 11.26f

    val tissues = listOf(
        TissueProperties(
            id = "fat",
            name = "Adiposa / Lemak Subkutan & Retroperitoneal",
            t1Ms = 260f,
            t2Ms = 85f,
            protonDensity = 1.00f,
            description = "Relaksasi T1 sangat singkat (~250–260 ms pada 1,5 T) karena tumbling rate molekul lipid hidrofobik mendekati frekuensi Larmor f₀ sehingga pelepasan energi spin-lattice sangat efisien. Relaksasi T2 menengah (~60–85 ms).",
            clinicalNote = "Karakteristik intensitas: Sangat hiperintens pada T1-weighted (T1w) dan hiperintens menengah pada FSE T2w konvensional akibat interaksi J-coupling rantai lipid."
        ),
        TissueProperties(
            id = "wm",
            name = "Substansia Alba (White Matter)",
            t1Ms = 790f,
            t2Ms = 92f,
            protonDensity = 0.77f,
            description = "Traktus aksonal kaya selubung mielin lipoprotein hidrofobik (~780–790 ms pada 1,5 T). Pertukaran energi termal spin-lattice lebih cepat dibanding Substansia Grisea.",
            clinicalNote = "Karakteristik intensitas: Relatif hiperintens (lebih terang) terhadap korteks pada T1w, dan relatif hipointens (lebih gelap) dibanding korteks pada T2w."
        ),
        TissueProperties(
            id = "gm",
            name = "Substansia Grisea (Korteks Serebri / Gray Matter)",
            t1Ms = 925f,
            t2Ms = 100f,
            protonDensity = 0.86f,
            description = "Soma neuron kaya fraksi air bebas sitosolik (T1 ~925–950 ms, T2 ~100 ms pada 1,5 T). Relaksasi T1 lebih lambat dibanding substansia alba.",
            clinicalNote = "Karakteristik intensitas: Relatif isointens-hipointens terhadap substansia alba pada T1w dan relatif hiperintens pada T2w."
        ),
        TissueProperties(
            id = "csf",
            name = "Cairan Serebrospinal (CSF)",
            t1Ms = 2400f,
            t2Ms = 1600f,
            protonDensity = 1.00f,
            description = "Cairan bebas tanpa makromolekul dengan laju gerak Brown molekuler sangat tinggi (tumbling rate ≫ f₀). Pertukaran energi kisi sangat tidak efisien (T1 sangat panjang ~2400–4000 ms), namun dephasing spin-spin minimal (T2 sangat panjang ~1600–2000 ms).",
            clinicalNote = "Karakteristik intensitas: Sangat hipointens (gelap pekat) pada T1w, sangat hiperintens (terang menyala) pada T2w, dan mengalami supresi total (signal void / null) pada sekuens FLAIR."
        ),
        TissueProperties(
            id = "muscle",
            name = "Otot Rangka (Skeletal Muscle)",
            t1Ms = 875f,
            t2Ms = 47f,
            protonDensity = 0.75f,
            description = "Serat protein aktomiosin rapat dengan ikatan hidrasi terikat kuat. Relaksasi T1 intermediet (~875 ms) dan T2 sangat singkat (~47 ms) akibat dephasing dipoler intermolekuler yang cepat.",
            clinicalNote = "Karakteristik intensitas: Isointens pada T1w dan hipointens homogen pada T2w baku emas."
        ),
        TissueProperties(
            id = "kidney",
            name = "Ginjal (Parenkim Kortikomeduler)",
            t1Ms = 650f,
            t2Ms = 58f,
            protonDensity = 0.85f,
            description = "Parenkim renal bervaskularisasi tinggi dengan gradien hidrasi korteks vs medula (T1 ~650 ms, T2 ~58 ms pada 1,5 T).",
            clinicalNote = "Karakteristik intensitas: Memperlihatkan diferensiasi kortikomeduler yang tajam pada T1w (korteks lebih hiperintens dibanding medula renalis)."
        ),
        TissueProperties(
            id = "liver",
            name = "Hati (Parenkim Hepar)",
            t1Ms = 500f,
            t2Ms = 43f,
            protonDensity = 0.80f,
            description = "Kaya makromolekul glikogen dan cadangan hemosiderin intraseluler hepar sehingga waktu relaksasi T1 (~500 ms) dan T2 (~43 ms) relatif singkat.",
            clinicalNote = "Karakteristik intensitas: Cukup hiperintens pada T1w dan relatif hipointens pada T2w."
        ),
        TissueProperties(
            id = "edema",
            name = "Edema Vasogenik / Sitotoksik Patologis",
            t1Ms = 1400f,
            t2Ms = 280f,
            protonDensity = 0.95f,
            description = "Akumulasi cairan abnormal interstitial atau intraseluler akibat rusaknya sawar darah otak (edema vasogenik) atau kegagalan pompa Na+/K+ ATPase iskemik (edema sitotoksik).",
            clinicalNote = "Karakteristik intensitas: Hipointens pada T1w, sangat hiperintens pada T2w/FLAIR, dan menunjukkan restriksi difusi molekuler nyata (hiperintens b=1000, hipointens pada peta ADC) pada edema sitotoksik akut."
        )
    )

    val modules = listOf(
        MriModule(
            id = "M01",
            code = "M01",
            title = "Fisika Dasar MRI & Presesi Spin",
            subtitle = "Atom Hidrogen, Vektor Magnetisasi Netto, Larmor, Nutasi & FID",
            description = "Menjelaskan mengapa inti hidrogen menjadi sumber sinyal MRI, menurunkan frekuensi Larmor dari kekuatan B0, serta menjelaskan bagaimana pulsa RF menghasilkan sinyal (eksitasi, nutasi, koherensi fase, lalu FID) pada Tingkat 1 (Dasar), Tingkat 2 (Menengah), dan Tingkat 3 (Lanjut).",
            levelBadge = "T1 - T3 Lengkap",
            iconName = "atom",
            nodes = listOf(
                // ================= PENDAHULUAN (TINGKAT 0) =================
                MriNode(
                    code = "M01.T0",
                    level = 0,
                    title = "Tujuan Belajar Modul 1: Fisika Dasar MRI",
                    summaryShort = "Menjelaskan mengapa inti hidrogen menjadi sumber sinyal MRI, menurunkan frekuensi Larmor dari B0, dan menjelaskan bagaimana pulsa RF menghasilkan sinyal (eksitasi, lalu FID).",
                    detailFull = "Tujuan belajar:\n• Menjelaskan mengapa inti hidrogen menjadi sumber sinyal MRI.\n• Menurunkan frekuensi Larmor dari kekuatan B0.\n• Menjelaskan bagaimana pulsa RF menghasilkan sinyal (eksitasi, lalu FID).\n\nIsi dibagi tiga tingkat: T1 (dasar), T2 (menengah), T3 (lanjut). Media belajar, bukan untuk keputusan klinis. Prasyarat: tidak ada. Dilanjutkan: Modul 2.",
                    analogi = "Peta perjalanan belajar MRI: Tingkat 1 mengenalkan aktor utamanya (proton hidrogen & B0), Tingkat 2 menjelaskan aksi panggungnya (pulsa RF, nutasi & sinyal FID), dan Tingkat 3 membedah rumus kuantum di balik layar.",
                    labType = null
                ),
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M01.T1.1",
                    level = 1,
                    title = "T1.1 Peran MRI dalam praktik klinis",
                    summaryShort = "MRI memakai sinyal hidrogen yang melimpah di air dan lemak, sehingga sangat baik untuk jaringan lunak. MRI kurang cocok bagi pasien tertentu (kontraindikasi: M07A).",
                    detailFull = "MRI dan jaringan lunak. Jaringan lunak seperti otak, otot, sendi, dan organ perut kaya air dan lemak, jadi kaya hidrogen. Perbedaan sinyal antarjaringan menghasilkan kontras yang rinci. MRI memakai RF non-pengion (lihat T1.7).\n\nKapan MRI kurang cocok. MRI memakai medan magnet kuat dan pulsa RF, sehingga tidak cocok bagi sebagian pasien, misalnya yang memiliki implan atau benda logam tertentu, atau yang tidak dapat berbaring diam. Daftar kontraindikasi lengkap ada di M07A.",
                    analogi = "Analogi: hidrogen seperti pemancar kecil yang tersebar merata di jaringan lunak. MRI mendengarkan \"siaran\" mereka, jadi jaringan yang kaya hidrogen terlihat jelas.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.R",
                    level = 1,
                    title = "Ringkasan Tingkat 1",
                    summaryShort = "Inti atom hidrogen (proton) di air dan lemak berperilaku seperti magnet kecil yang berputar (spin). Di dalam B0, sebagian proton menyearah dengan B0 dan berpresesi mengelilingi arah B0. Pulsa RF yang frekuensinya tepat membuat proton beresonansi dan memancarkan sinyal ke RF coil.",
                    detailFull = "Inti atom hidrogen (proton) ada di air dan lemak tubuh. Proton berperilaku seperti magnet kecil yang berputar (spin).\n\nDi dalam medan magnet utama (B0), sebagian proton menyearah dengan B0. Proton tidak diam. Ia berpresesi, yaitu berputar mengelilingi arah B0, seperti gasing yang miring.\n\nPulsa RF yang frekuensinya tepat akan membuat proton beresonansi. Setelah pulsa, proton memancarkan sinyal yang diterima RF coil.\n\nHanya inti dengan nomor massa ganjil yang punya spin neto dan bisa dipakai di MRI (inti aktif MR), misalnya ¹H, ¹³C, ¹⁹F, dan ²³Na. Hidrogen dipilih karena paling melimpah di tubuh.",
                    analogi = "Analogi: bayangkan jutaan momen magnetik spin mikroskopis di dalam tubuh pasien. Biasanya jarum itu menunjuk sembarang arah. Di dalam B0 jarumnya cenderung menunjuk satu arah, tetapi tidak diam: ia bergoyang seperti gasing miring. Pulsa RF seperti mendorong ayunan tepat pada iramanya, sehingga goyangan membesar.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.2",
                    level = 1,
                    title = "T1.2 Dua cara menjelaskan MRI (Klasik vs Kuantum)",
                    summaryShort = "Teori klasik menjelaskan MRI lewat massa, spin, dan momentum sudut pada skala besar. Teori kuantum memakai tingkat energi partikel subatom. Modul ini terutama memakai sudut pandang klasik.",
                    detailFull = "MRI bisa dijelaskan dengan teori klasik atau teori kuantum. Teori klasik memakai massa, spin, dan momentum sudut pada skala besar. Teori kuantum bekerja pada skala subatom, yaitu tingkat energi proton, neutron, dan elektron. Modul ini lebih banyak memakai sudut pandang klasik karena lebih mudah dibayangkan. Teori kuantum dipakai hanya bila perlu.",
                    analogi = "Analogi: teori klasik seperti melihat ombak dari pantai: teramati tinggi dan arah rambat gelombang makroskopis. Teori kuantum seperti memeriksa tiap tetes air. Lebih rinci, tetapi untuk gambaran ombak secara umum, cara pertama sudah cukup.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.3",
                    level = 1,
                    title = "T1.3 Struktur atom dan nomor massa",
                    summaryShort = "Atom membentuk molekul. Hidrogen adalah atom terbanyak di tubuh, ada di air dan lemak. Nomor atom adalah jumlah proton, nomor massa adalah jumlah proton ditambah neutron.",
                    detailFull = "Semua benda tersusun dari atom, dan atom yang bergabung membentuk molekul. Di tubuh manusia, atom terbanyak adalah hidrogen. Hidrogen ada di air (dua H dan satu O) dan di lemak (H bersama C dan O, jumlahnya bergantung jenis lemak).\n\nInti atom sangat kecil, tetapi memuat hampir seluruh massa atom. Massa itu berasal dari nukleon, yaitu proton dan neutron. Dua ukuran mencirikan atom:\n• Nomor atom: jumlah proton di inti. Inilah yang menentukan jenis unsur.\n• Nomor massa: jumlah proton ditambah neutron.\n\nAtom dengan jumlah proton sama tetapi neutron berbeda disebut isotop. Proton bermuatan positif, neutron tidak bermuatan, elektron bermuatan negatif. Atom stabil bila jumlah elektron sama dengan jumlah proton. Jika elektron terlepas karena diberi energi (ionisasi), atom menjadi ion.\n\nElektron sering digambarkan mengorbit seperti planet. Menurut teori kuantum, posisinya tidak bisa dipastikan (prinsip ketidakpastian Heisenberg), sehingga lebih tepat dibayangkan sebagai awan.",
                    analogi = "Analogi: inti ibarat kelereng di tengah stadion, sedangkan elektron ibarat baling-baling kipas yang berputar sangat cepat: yang terlihat bukan satu titik pasti, melainkan lingkaran kabur (awan). Nomor atom seperti nomor KTP unsur: jumlah proton menentukan \"identitasnya\".",
                    labType = "atom_h",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.4A",
                    level = 1,
                    title = "T1.4 Gerak dalam atom dan syarat spin neto",
                    summaryShort = "MRI memanfaatkan putaran inti. Inti dengan proton dan neutron genap tidak punya spin neto. Inti bernomor massa ganjil punya spin neto dan dipakai di MRI.",
                    detailFull = "Ada tiga gerak di dalam atom: elektron berputar pada sumbunya, elektron mengorbit inti, dan inti berputar pada sumbunya. MRI memanfaatkan putaran inti.\n\nJika jumlah proton dan neutron sama-sama genap (misalnya karbon-12 dengan enam proton dan enam neutron), separuh nukleon berputar ke satu arah dan separuh ke arah lain. Putarannya saling meniadakan, sehingga inti tidak punya spin neto.\n\nJika ada proton ganjil, neutron ganjil, atau keduanya ganjil, arah putarnya tidak seimbang. Inti punya spin neto atau momentum sudut. Nomor massa ganjil memberi spin setengah-bulat (misalnya ½, 5/2). Karena itu syarat praktisnya: inti bernomor massa ganjil punya spin neto dan menjadi inti aktif MR. Pengecualian dibahas di T3.4.\n\nSecara subatomik, proton tersusun dari tiga kuark: dua berspin ke atas dan satu ke bawah, sehingga spin proton ½. Muatan kuark masing-masing +2/3, +2/3, dan −1/3, jumlahnya +1.",
                    analogi = "Analogi: tarik tambang. Jika dua tim sama kuat, tali tidak bergeser (spin saling meniadakan). Jika ada satu orang yang tidak punya pasangan, tali bergeser ke arahnya. Orang tak berpasangan itulah spin neto yang dimanfaatkan MRI.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.4B",
                    level = 1,
                    title = "T1.4 Contoh inti aktif MR (¹H, ¹³C, ¹⁹F, ²³Na)",
                    summaryShort = "Inti aktif MR berspin dan bermuatan positif, sehingga punya momen magnetik dan cenderung menyelaraskan diri dengan medan magnet luar. Contoh: ¹H, ¹³C, ¹⁹F, ²³Na.",
                    detailFull = "Inti aktif MR cenderung menyelaraskan sumbu putarnya dengan medan magnet luar. Inti ini berspin dan bermuatan positif, sehingga otomatis punya medan magnet sendiri. Dalam teori klasik medan itu disebut momen magnetik, sebuah vektor dengan besar dan arah. Dasarnya adalah induksi elektromagnetik (Faraday, 1833): muatan yang bergerak menimbulkan medan magnet.\n\nContoh inti aktif MR: ¹H (hidrogen), ¹³C (karbon), ¹⁹F (fluor), ²³Na (natrium).",
                    analogi = "Analogi: kawat yang dililit lalu dialiri arus menjadi elektromagnet. Inti yang berspin dan bermuatan positif mirip itu: otomatis jadi magnet mini, sehingga mau menanggapi magnet luar. Inti yang spinnya saling batal seperti dua magnet yang ditempel berlawanan: dari luar tampak tidak magnetik.",
                    labType = "cek_inti",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.5",
                    level = 1,
                    title = "T1.5 Populasi spin dan vektor magnetik neto (NMV)",
                    summaryShort = "Di dalam B0, proton punya dua pilihan: searah B0 (paralel, berenergi rendah) atau berlawanan (antiparalel, berenergi tinggi). Yang searah sedikit lebih banyak. Kelebihan itu membentuk vektor magnetik neto (NMV) yang searah B0.",
                    detailFull = "Proton di dalam B0 menempati dua keadaan. Spin ke atas (paralel dengan B0) berenergi rendah. Spin ke bawah (antiparalel) berenergi tinggi. Pada kesetimbangan termal, spin berenergi rendah sedikit lebih banyak. Resultan seluruh momen magnetik, yaitu NMV, pun searah B0.\n\nKelebihan spin searah sangat kecil. Hitungan kuantitatif (rasio Boltzmann) ada di T3.1.\n\nBesar kelebihan itu bergantung pada banyaknya molekul dalam jaringan dan pada kekuatan B0. Makin kuat B0, makin besar beda energi kedua keadaan. Makin sedikit spin yang cukup berenergi untuk melawan B0. Jadi populasi berenergi rendah bertambah dan kelebihannya membesar.\n\n• Di dalam B0, momen magnetik hidrogen menyearah (spin atas, energi rendah) atau berlawanan (spin bawah, energi tinggi).\n• Pada kesetimbangan termal, spin atas lebih banyak, sehingga NMV searah B0.\n• Selisih energi kedua populasi terutama ditentukan oleh kekuatan B0.\n• B0 yang lebih besar memperbesar selisih energi dan kelebihan spin atas.\n• SNR meningkat pada B0 yang lebih tinggi.",
                    analogi = "Analogi 1, tribun stadion: hampir separuh penonton menghadap lapangan dan hampir separuh menghadap belakang. Sorakan keduanya saling meniadakan. Yang terdengar hanya sorakan dari selisih kecil penonton yang lebih banyak menghadap lapangan. Selisih itulah NMV.\n\nAnalogi 2, bola di lereng: keadaan berenergi rendah seperti dasar lembah, berenergi tinggi seperti puncak. Makin curam lerengnya (B0 besar), makin banyak bola bertahan di bawah. Suhu seperti guncangan yang terus mengocok bola, sehingga selisihnya tetap kecil.",
                    labType = "populasi_spin",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.6",
                    level = 1,
                    title = "T1.6 Presesi dan frekuensi Larmor",
                    summaryShort = "Selain berspin pada sumbunya, momen magnet proton bergoyang mengelilingi arah B0. Gerak ini disebut presesi. Kecepatannya, frekuensi Larmor, mengikuti persamaan f = γ·B0.",
                    detailFull = "Setiap inti hidrogen berspin pada sumbunya. Pengaruh B0 menambahkan gerak goyang kedua pada momen magnetiknya, mengelilingi arah B0. Gerak sekunder ini disebut presesi, jalur putarnya disebut lintasan presesi, dan kecepatannya disebut frekuensi presesi atau frekuensi Larmor. Semua momen magnetik, baik spin atas maupun spin bawah, berpresesi pada frekuensi Larmor yang sama.\n\nRumus: f = γ · B0  (atau ω0 = γ · B0)\n\nSatuan frekuensi adalah hertz (Hz), yaitu satu putaran per detik. Satu megahertz (MHz) sama dengan sejuta putaran per detik.\n\nRasio giromagnetik (γ) menyatakan hubungan antara momentum sudut dan momen magnetik suatu inti. Nilainya tetap untuk tiap jenis inti. Ia dinyatakan sebagai frekuensi presesi inti itu pada 1 T, sehingga satuannya MHz/T. Untuk hidrogen nilainya 42,58 MHz/T. Dalam bentuk sudut, ω0 = γB0. Faktor 2π hanya dipakai untuk mengubah kecepatan sudut menjadi frekuensi siklus (Hz).\n\nContoh untuk hidrogen: pada 0,5 T frekuensinya 21,29 MHz, pada 1,0 T 42,58 MHz, pada 1,5 T 63,87 MHz, dan pada 3,0 T 127,74 MHz.",
                    analogi = "Analogi 1, senar gitar: makin kencang senarnya, makin tinggi nadanya. Makin kuat B0, makin tinggi frekuensi presesinya. Tiap inti punya \"senar\" sendiri (nilai γ), sehingga nadanya berbeda pada kekencangan yang sama.\n\nAnalogi 2, gasing miring: gasing berputar pada porosnya, dan porosnya sendiri berkeliling pelan karena ditarik gravitasi. Proton berspin, dan porosnya berkeliling karena ditarik B0.",
                    labType = "larmor",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T1.7",
                    level = 1,
                    title = "T1.7 Spektrum elektromagnetik dan sifat non-pengion RF",
                    summaryShort = "Frekuensi untuk MRI hidrogen berada di pita radiofrekuensi (RF). RF bersifat non-pengion.",
                    detailFull = "Frekuensi Larmor hidrogen pada B0 klinis berada di pita radiofrekuensi (RF) pada spektrum elektromagnetik (~63,87 MHz pada 1,5 T dan ~127,74 MHz pada 3,0 T).\n\nRadiasi RF bersifat non-pengion: energinya tidak cukup untuk melepas elektron dari atom. Sinar-X dan sinar gamma berfrekuensi jauh lebih tinggi (≥ 10¹⁶ Hz) dan bersifat pengion. Batas keselamatan RF (SAR) dibahas di M07C.\n\nSumber isi tambahan Tingkat 1: Westbrook & Talbot, MRI in Practice, edisi ke-5 (2019), Bab 1, hlm. 1–4.",
                    analogi = "Analogi, stasiun radio: tiap stasiun punya frekuensi sendiri. Radio hanya menangkap siaran bila disetel tepat pada frekuensinya. Pulsa RF juga hanya \"menyapa\" inti yang frekuensi Larmornya cocok. Inti lain tidak menanggapi.",
                    labType = "spektrum_em",
                    htmlSimulationAsset = "modul1/index.html"
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M01.T2.R",
                    level = 2,
                    title = "Ringkasan Tingkat 2",
                    summaryShort = "Frekuensi Larmor mengikuti f = γ·B0 (γ/2π = 42,58 MHz/T: pada 1,5 T = 63,87 MHz; pada 3 T = 127,74 MHz). Sudut flip α = γ·B1·τ memutar magnetisasi ke bidang transversal dan setelah pulsa 90° menghasilkan sinyal FID.",
                    detailFull = "Frekuensi Larmor mengikuti f = γ·B0, dengan γ/2π = 42,58 MHz/T. Pada 1,5 T hasilnya 63,87 MHz. Pada 3 T hasilnya 127,74 MHz.\n\nKelebihan proton yang searah B0 sangat kecil, hanya orde per sejuta. Kelebihan ini membesar bersama B0. Itulah alasan 3 T memberi sinyal lebih besar daripada 1,5 T.\n\nRumus: α = γ · B1 · τ\n\nSudut flip dihitung dengan α = γ·B1·τ. B1 adalah kekuatan medan pulsa RF dan τ adalah lama pulsa. Pulsa 90° memutar magnetisasi ke bidang transversal. Pulsa 180° membaliknya ke arah berlawanan.\n\nSetelah pulsa 90°, RF coil menerima sinyal yang meluruh. Sinyal ini disebut FID (free induction decay).",
                    analogi = "Analogi: pulsa RF seperti mendorong ayunan. Frekuensi Larmor adalah irama ayunan, sudut flip ditentukan oleh durasi dan amplitudo pulsa RF (medan B1). Sinyal FID analog dengan dentang lonceng sesaat setelah dipukul yang perlahan meluruh memudar.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.1A",
                    level = 2,
                    title = "T2.1 Fase dan koherensi (Inkoheren vs Koheren)",
                    summaryShort = "Fase adalah posisi sebuah momen magnetik pada lintasan presesinya di saat tertentu. Frekuensi adalah seberapa cepat fase itu berubah. Jika hanya ada B0, momen magnetik tidak sefase, sehingga NMV tidak berpresesi.",
                    detailFull = "Fase menyatakan di mana letak momen magnetik pada lintasan presesinya saat ini. Satuannya radian atau derajat, dan satu putaran penuh sama dengan 2π rad atau 360°. Frekuensi adalah laju perubahan fase itu. Di MRI, posisi fase relatif semua momen magnetik hidrogen sangat penting:\n• Tidak sefase (inkoheren): momen magnetik berada di tempat berbeda pada lintasan presesi.\n• Sefase (koheren): momen magnetik berada di tempat yang sama pada lintasan presesi.\n\nBila satu-satunya pengaruh adalah B0, momen magnetik tidak sefase. Komponen transversalnya saling meniadakan, sehingga NMV tidak berpresesi.\n\n• Semua spin berpresesi mengelilingi B0 pada frekuensi Larmor yang sebanding dengan B0 untuk inti tertentu. Satuan frekuensinya MHz.\n• Pada kuat medan klinis, frekuensi Larmor hidrogen berada di pita RF.\n• Fase adalah posisi momen magnetik pada lintasan presesinya.\n• Pada kesetimbangan, momen magnetik spin tidak sefase.",
                    analogi = "Analogi 1, pelari di lintasan: frekuensi adalah seberapa cepat tiap pelari mengitari lintasan, fase adalah posisinya saat ini. Sefase berarti semua pelari berada di titik yang sama, seperti saat aba-aba start. Tidak sefase berarti pelari tersebar di seluruh lintasan.\n\nAnalogi 2, jam tangan: jarum jam punya frekuensi (kecepatan putar) dan fase (jam berapa yang ditunjuk). Jika semua jam disamakan lalu sebagian berjalan sedikit cepat dan sebagian lambat, beberapa jam kemudian waktunya tidak seragam. Mereka tidak sefase.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.1B",
                    level = 2,
                    title = "T2.1 Resonansi dan pulsa RF eksitasi (B1)",
                    summaryShort = "Resonansi terjadi ketika gangguan berosilasi punya frekuensi dekat dengan frekuensi alami benda. Pulsa RF eksitasi pada frekuensi Larmor hidrogen membuat hidrogen menyerap energi. Inti lain tidak ikut beresonansi.",
                    detailFull = "Resonansi adalah gejala yang muncul ketika suatu benda dikenai gangguan berosilasi yang frekuensinya dekat dengan frekuensi alaminya. Pada inti, frekuensi alami itu adalah frekuensi Larmor. Jika energi diberikan pada frekuensi yang sama, inti menyerap energi. Jika frekuensinya berbeda, tidak terjadi resonansi dan inti tidak menyerap energi.\n\nKarena hidrogen berpresesi di pita RF, resonansi hidrogen memerlukan pulsa RF pada frekuensi Larmor hidrogen. Inti aktif MR lain tidak beresonansi karena rasio giromagnetiknya berbeda. Inilah yang memungkinkan pencitraan khusus hidrogen. Selain itu, karena γ konstan, frekuensi Larmor sebanding dengan B0: jika B0 naik, frekuensinya naik sebanding, dan sebaliknya.\n\nResonansi dicapai dengan memancarkan pulsa RF eksitasi dari koil pemancar. Gelombang elektromagnetik terdiri dari medan listrik dan medan magnet yang saling tegak lurus. Hanya komponen magnetnya yang dipakai (medan listrik menimbulkan panas). B0 diam, sedangkan pulsa RF menghasilkan medan magnet berosilasi B1 yang tegak lurus B0. B1 berada pada rentang frekuensi sempit di sekitar frekuensi pusat (lebar pita pemancar) dan sangat lemah dibanding B0.",
                    analogi = "Analogi, garpu tala: bunyikan nada A di dekat sekumpulan garpu tala. Hanya garpu tala A yang ikut bergetar. Pulsa RF pada frekuensi Larmor hidrogen \"membangunkan\" hidrogen saja. B1 yang lemah pun efektif, seperti dorongan kecil yang berirama tepat pada ayunan.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.2A",
                    level = 2,
                    title = "T2.2 Efek eksitasi: nutasi",
                    summaryShort = "(Penjelasan kuantum rinci: T3.3.) Klasik: B1 membuat NMV berpresesi dalam spiral turun dari sumbu z ke bidang transversal (nutasi), dan momen magnetik menjadi sefase.",
                    detailFull = "Teori klasik. B1 bekerja pada bidang transversal (sumbu x–y), sehingga momen magnetik berpresesi mengelilingi sumbu itu, bukan sumbu z. Karena B1 jauh lebih lemah daripada B0, presesi mengelilingi B1 jauh lebih lambat. NMV bergerak spiral dari bidang longitudinal ke bidang transversal. Gerak spiral ini disebut nutasi, hasil dua presesi serentak: yang cepat mengelilingi B0 dan yang lambat mengelilingi B1. Selain itu, momen magnetik spin atas dan bawah bergerak menjadi sefase (koheren).\n\nJika energi yang diserap tepat, NMV berada di bidang transversal, tegak lurus B0. NMV telah berpindah sejauh sudut flip (tip angle) 90°. Energi dan frekuensi radiasi saling berkaitan. Frekuensi resonansi terkait dengan beda energi kedua populasi, jadi dengan kekuatan B0. Makin besar B0, makin besar frekuensi yang diperlukan.\n\nPulsa RF ditandai oleh amplitudo (B1) dan frekuensinya. Agar resonansi terjadi, frekuensinya harus sama dengan frekuensi Larmor hidrogen. Saat B1 diberikan, momen magnetik menyelaraskan diri dan memperoleh koherensi fase. Gabungan koherensi fase dan nutasi menghasilkan magnetisasi koheren yang berpresesi di bidang transversal.",
                    analogi = "Analogi 1, sekrup: sekrup berputar cepat mengelilingi porosnya sambil maju pelan sepanjang poros. Nutasi mirip: putaran cepat mengelilingi B0 digabung putaran lambat mengelilingi B1, sehingga lintasan NMV berupa spiral.\n\nAnalogi 2, rak buku: spin rendah ada di rak bawah, spin tinggi di rak atas. Pulsa RF memberi \"tenaga\" untuk naik rak. Karena rak bawah lebih penuh, lebih banyak yang naik daripada yang turun.\n\nAnalogi 3, aba-aba baris-berbaris: sebelum aba-aba, pasukan melangkah sembarang. Setelah aba-aba (pulsa RF), semuanya serempak. Itulah koherensi fase.",
                    labType = "nmv_3d",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.2B",
                    level = 2,
                    title = "T2.2 Sudut flip (θ = γ·B1·τ)",
                    summaryShort = "Sudut flip adalah sudut perpindahan NMV dari arah B0. Besarnya bergantung pada amplitudo B1 dan lama pulsa τ: θ = γ·B1·τ. Pulsa 90° memindahkan NMV ke bidang transversal. Pulsa 180° membaliknya.",
                    detailFull = "Rumus: θ = ω1 · τ = γ · B1 · τ\n\nBesar sudut flip bergantung pada amplitudo dan lama pulsa RF eksitasi. Biasanya NMV dipindahkan sejauh 90° dari B0. Karena NMV adalah vektor, sudut selain 90° tetap menyisakan komponen magnetisasi yang tegak lurus B0. Pada 90°, seluruh NMV longitudinal dipindahkan ke transversal. Pada sudut kurang atau lebih dari 90°, hanya sebagian yang berpindah. Sudut flip ditentukan oleh kekuatan B1 dan lamanya B1 diberikan.\n\nDari persamaan θ = ω1·τ = γ·B1·τ terlihat bahwa sudut 90° sama dengan π/2 dan sudut 180° sama dengan π. Pulsa 180° memerlukan B1 dua kali lebih besar (atau durasi dua kali lebih lama) daripada pulsa 90°. Dalam bahasa kuantum, pulsa 180° membalik populasi (inversi): jumlah spin berenergi rendah dan tinggi bertukar. Pulsa 90° menyamakan jumlah kedua populasi.",
                    analogi = "Analogi, mendorong pintu putar: sudut rotasi pintu berbanding lurus dengan besar gaya dorong (B1) dan durasi dorongan pulsa (τ). Dorong dua kali lebih lama atau dua kali lebih keras, sudutnya dua kali lebih besar. Itu sebabnya 180° butuh dua kali \"usaha\" 90°.",
                    labType = "sudut_flip",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.3A",
                    level = 2,
                    title = "T2.3 Sinyal MR, Hukum Faraday, dan FID",
                    summaryShort = "Magnetisasi sefase yang berpresesi di bidang transversal mengubah medan magnet di sekitar koil, sehingga menginduksi tegangan pada koil penerima. Tegangan itu adalah sinyal. Setelah pulsa dimatikan, sinyalnya meluruh: itulah FID.",
                    detailFull = "Sinyal MR. Berkat resonansi, magnetisasi koheren berpresesi di bidang transversal. Medan magnet yang berubah ini menimbulkan arus listrik, sesuai hukum induksi Faraday: perubahan fluks magnetik melalui rangkaian tertutup menginduksi gaya gerak listrik (ggl).\n• Ggl induksi sebanding dengan laju perubahan medan magnet dan luas rangkaian.\n• Ggl sebanding dengan jumlah lilitan kawat.\n• Arah ggl melawan perubahan medan yang menyebabkannya (hukum Lenz).\n\nRumus: ε = −N · (dΦ / dt)\n\nDengan ε ggl (volt), N jumlah lilitan, dΦ/dt laju perubahan fluks magnetik, dan t waktu (detik). Bila koil penerima berada di dekat magnetisasi yang berpresesi di bidang transversal, tegangan yang timbul disebut sinyal.\n\nFID. Ketika pulsa RF dimatikan, NMV hanya dipengaruhi B0 dan berusaha menyelaraskan diri kembali dengannya (relaksasi). Pada saat yang sama, momen magnetik kehilangan koherensi (dephasing) akibat ketidakhomogenan B0 dan interaksi antarspin. Magnetisasi transversal koheren menurun, begitu pula tegangan di koil. Tegangan yang meluruh ini disebut sinyal free induction decay (FID): spin berpresesi bebas (free) hanya karena B0, sinyal meluruh (decay), dan momen magnetik menginduksi (induction) arus di koil.",
                    analogi = "Analogi 1, dinamo sepeda: magnet yang berputar dekat kumparan menimbulkan listrik. Magnetisasi yang berpresesi di dekat koil penerima bekerja serupa, dan listriknya adalah sinyal MR.\n\nAnalogi 2, paduan suara: di awal semua penyanyi serempak (koheren), suaranya keras. Lalu tiap penyanyi sedikit berbeda tempo, sehingga suara makin kacau dan melemah. Dephasing membuat sinyal FID meluruh. Relaksasi seperti ayunan yang perlahan kembali ke bawah setelah didorong.",
                    labType = "fase_fid",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.3B",
                    level = 2,
                    title = "T2.3 Komponen longitudinal dan transversal NMV",
                    summaryShort = "NMV adalah vektor dengan dua komponen yang saling tegak lurus: magnetisasi longitudinal (sejajar B0) dan magnetisasi transversal (di bidang tegak lurus B0). Sebelum resonansi seluruhnya longitudinal. Setelah pulsa 90°, seluruhnya transversal.",
                    detailFull = "Sebelum resonansi, NMV sepenuhnya longitudinal dan sejajar B0. Setelah pulsa RF dengan sudut flip 90°, NMV terbalik penuh ke bidang transversal: magnetisasi transversal penuh dan magnetisasi longitudinal nol.\n\nBegitu pulsa RF dihentikan, NMV pulih. Komponen longitudinal tumbuh lagi, sedangkan komponen transversal mengecil. Amplitudo sinyal yang diterima terkait dengan besar komponen transversal yang koheren. Karena itu sinyal di koil meluruh seiring relaksasi (dibahas rinci di Modul 2).\n\nRingkasan eksitasi dan pembentukan sinyal:\n• Energi RF pada frekuensi Larmor menyebabkan penyerapan energi neto (eksitasi) dan mengubah keseimbangan jumlah spin di populasi rendah dan tinggi.\n• Arah NMV terhadap B0 bergantung pada keseimbangan itu. Jika kedua populasi berjumlah hampir sama, NMV berada di bidang yang tegak lurus B0 (bidang transversal).\n• Resonansi juga membuat momen magnetik semua spin berpresesi sefase. Hasilnya magnetisasi transversal koheren yang berpresesi di bidang transversal.\n• Koil penerima (penghantar) yang diletakkan di bidang transversal menerima tegangan akibat gerak magnetisasi transversal koheren yang berputar.\n• Setelah pulsa RF dihentikan, momen magnetik semua spin kehilangan fase (dephasing) dan menghasilkan FID.",
                    analogi = "Analogi, tiang dan bayangannya: tiang yang miring punya dua ukuran: tingginya (komponen longitudinal) dan panjang bayangannya di tanah (komponen transversal). Tiang tegak tidak punya bayangan. Pulsa 90° seperti merebahkan tiang itu ke tanah: tingginya nol, bayangannya sepanjang tiang. Setelah pulsa dihentikan, tiang perlahan berdiri lagi.",
                    labType = "bola_bloch",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T2.4",
                    level = 2,
                    title = "T2.4 Parameter waktu pulsa: TR dan TE",
                    summaryShort = "Urutan pulsa sederhana terdiri dari pulsa RF, sinyal, dan jeda relaksasi. TR adalah jarak waktu dari satu pulsa eksitasi ke pulsa eksitasi berikutnya. TE adalah waktu dari pulsa eksitasi sampai puncak sinyal. Keduanya dalam milidetik.",
                    detailFull = "Urutan pulsa yang sangat sederhana adalah gabungan pulsa RF, sinyal, dan periode relaksasi di antaranya. Diagram dasar hanya menunjukkan parameter waktu secara terpisah. Parameter yang sama dipakai pada urutan yang lebih rumit:\n• TR (repetition time): waktu dari satu pulsa eksitasi ke pulsa eksitasi berikutnya untuk tiap irisan, dalam milidetik. TR menentukan seberapa banyak relaksasi longitudinal terjadi di antara kedua pulsa. Jadi TR mengatur seberapa besar relaksasi T1 yang telah terjadi saat sinyal dibaca.\n• TE (echo time): waktu dari pulsa eksitasi sampai puncak sinyal yang diinduksi di koil penerima, juga dalam milidetik. TE menentukan seberapa banyak magnetisasi transversal meluruh. Jadi TE mengatur seberapa besar relaksasi T2 yang telah terjadi saat sinyal dibaca.\n\nPemberian pulsa RF dan penerimaan sinyal pada waktu yang telah ditentukan inilah yang menghasilkan kontras pada citra MRI. Konsep ini dibahas lebih rinci di Modul 2.\n\nSumber isi tambahan Tingkat 2: Westbrook & Talbot, MRI in Practice, edisi ke-5 (2019), Bab 1, hlm. 9–22.",
                    analogi = "Analogi 1, mendorong ayunan: TR adalah jeda antara dua dorongan. Jika terlalu cepat diulang, ayunan belum sempat kembali ke bawah (belum pulih), sehingga dorongan berikutnya kurang efektif.\n\nAnalogi 2, dentang lonceng: TE adalah interval saat operator mulai merekam sinyal setelah lonceng dipukul; makin lama interval perekaman, makin pelan bunyinya akibat dephasing relaksasi T2.",
                    labType = "tr_berulang",
                    htmlSimulationAsset = "modul1/index.html"
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M01.T3.1",
                    level = 3,
                    title = "T3.1 Rasio Boltzmann: pengaruh B0 pada selisih populasi",
                    summaryShort = "Rasio spin berenergi tinggi dan rendah mengikuti N⁺/N⁻ = e^(−ΔE/kT). B0 lebih besar memperbesar ΔE, sehingga kelebihan spin rendah membesar (~4,5 per sejuta pada 1,5 T vs ~10 per sejuta pada 3 T).",
                    detailFull = "Rumus: N⁺ / N⁻ = e^(−ΔE / kT)\n\nPerbandingan kedua populasi diberikan oleh persamaan Boltzmann: N⁺/N⁻ = e^(−ΔE/kT). N⁺ adalah jumlah spin berenergi tinggi, N⁻ jumlah spin berenergi rendah, ΔE selisih energi keduanya (joule), k tetapan Boltzmann, dan T suhu jaringan (kelvin). MRI menganggap tubuh berada pada kesetimbangan termal, yaitu suhunya tidak berubah berarti selama pemindaian.\n\nDengan ΔE = h·γ·B0 dan T = 310 K, kelebihan spin berenergi rendah kira-kira 4,5 per sejuta pada 1,5 T dan 10 per sejuta pada 3 T.",
                    analogi = "Analogi: bola di lereng. Lereng makin curam (B0 besar), makin banyak bola bertahan di bawah, tetapi guncangan suhu menjaga selisihnya tetap kecil.",
                    labType = "populasi_spin",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T3.2",
                    level = 3,
                    title = "T3.2 Frekuensi Larmor berbagai inti aktif MR",
                    summaryShort = "Tiap inti punya γ berbeda, sehingga frekuensi Larmornya berbeda pada B0 yang sama.",
                    detailFull = "Perbandingan frekuensi Larmor berbagai inti aktif MR pada B0 = 1,5 T:\n• ¹H (Hidrogen): Spin = 1/2 | γ/2π = 42,58 MHz/T | f pada 1,5 T = 63,87 MHz\n• ¹³C (Karbon-13): Spin = 1/2 | γ/2π = 10,71 MHz/T | f pada 1,5 T = 16,06 MHz\n• ¹⁹F (Fluor-19): Spin = 1/2 | γ/2π = 40,08 MHz/T | f pada 1,5 T = 60,12 MHz\n• ²³Na (Natrium-23): Spin = 3/2 | γ/2π = 11,26 MHz/T | f pada 1,5 T = 16,89 MHz",
                    analogi = "Analogi: tiap inti seperti stasiun radio dengan frekuensi sendiri. Pulsa RF hanya \"menyapa\" inti yang cocok.",
                    labType = "larmor",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T3.3",
                    level = 3,
                    title = "T3.3 Penjelasan eksitasi: teori klasik vs kuantum",
                    summaryShort = "Klasik: vektor NMV berpresesi dan bernutasi. Kuantum: tingkat energi spin terbelah oleh B0, dan pulsa RF memindahkan populasi. Keduanya memberi frekuensi resonansi yang sama.",
                    detailFull = "Teori kuantum. Pulsa RF yang menyebabkan resonansi disebut eksitasi, artinya \"memberi energi\". Pulsa ini menambah jumlah bersih spin berenergi tinggi: spin rendah menyerap energi dan pindah ke populasi tinggi, sedangkan spin tinggi terstimulasi melepas energi dan kembali ke populasi rendah. Karena spin rendah lebih banyak, efek bersihnya adalah penyerapan energi.\n\nRumus: ΔE = h · ω0 = h · γ · B0\n\nEnergi foton E = h·ω0, dengan h tetapan Planck. Beda energi spin atas dan bawah ΔE = h·γ·B0 sebanding dengan B0. Bila energi foton RF sama dengan ΔE, energi diserap.",
                    analogi = "Analogi: melihat ombak dari pantai (klasik) dan memeriksa tiap tetes air (kuantum). Gambaran besarnya sama.",
                    labType = null,
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T3.4",
                    level = 3,
                    title = "T3.4 Pengecualian syarat nomor massa ganjil (²H dan ¹⁴N)",
                    summaryShort = "Aturan \"nomor massa ganjil\" adalah aturan praktis. Inti dengan proton dan neutron sama-sama ganjil, misalnya ²H dan ¹⁴N, punya spin neto bulat.",
                    detailFull = "Pada ²H (1 proton, 1 neutron) dan ¹⁴N (7 proton, 7 neutron), proton dan neutron masing-masing ganjil, nomor massanya genap, tetapi arah putarnya tidak seimbang, sehingga spin netonya bulat (mis. I = 1). Aturan di T1.4 tetap berguna sebagai pegangan awal, tetapi bukan hukum mutlak.",
                    analogi = "Analogi: tarik tambang dengan dua orang tak berpasangan di tiap tim. Jumlah orangnya genap, tetapi tali tetap bisa bergeser.",
                    labType = "cek_inti",
                    htmlSimulationAsset = "modul1/index.html"
                ),
                MriNode(
                    code = "M01.T3.5",
                    level = 3,
                    title = "T3.5 Kerangka acuan berputar (Rotating Frame)",
                    summaryShort = "Dalam kerangka acuan yang berotasi pada frekuensi Larmor, presesi tampak stasioner dan vektor medan B1 tampak diam.",
                    detailFull = "Dalam kerangka laboratorium, NMV tampak bernutasi: presesi cepat mengelilingi B0 dan spiral lambat mengelilingi B1. Dalam kerangka berputar pada frekuensi B0, hanya tampak gerak lambat NMV dari sumbu z ke bidang x–y akibat B1. Jika frekuensi RF berbeda dari Larmor (off-resonance), masih ada medan sisa dan sudut flip efektif lebih kecil.",
                    analogi = "Analogi: duduk di komidi putar. Wahana yang ikut berputar tampak stasioner bagi pengamat di dalamnya.",
                    labType = "nmv_3d",
                    htmlSimulationAsset = "modul1/index.html"
                )
            )
        ),
        MriModule(
            id = "M02",
            code = "M02",
            title = "Relaksasi T1/T2 & Pembobotan Kontras",
            subtitle = "Pemulihan Longitudinal, Peluruhan Transversal, dan Kontras",
            description = "Menguasai mekanisme fisik relaksasi pasca-pulsa RF, kurva pemulihan T1 (spin-lattice) & peluruhan T2/T2* (spin-spin), pengaruh lingkungan molekuler jaringan, serta parameter TR dan TE.",
            levelBadge = "T1 - T2 Menengah",
            iconName = "contrast",
            nodes = listOf(
                MriNode(
                    code = "M02.T1.1",
                    level = 1,
                    title = "Mekanisme Dasar Relaksasi Pasca-Pulsa RF",
                    summaryShort = "Relaksasi adalah proses kembalinya Net Magnetization Vector (NMV) ke keadaan kesetimbangan termal (sejajar B₀) setelah pulsa Radiofrekuensi (RF) dimatikan. Proses ini terdiri dari dua mekanisme independen yang berjalan simultan: pemulihan longitudinal (T1) dan peluruhan transversal (T2).",
                    detailFull = "Ketika pulsa RF 90° diberikan pada frekuensi Larmor, proton menyerap energi sehingga populasi spin berpindah ke tingkat energi tinggi (anti-parallel) dan seluruh spin berpresesi secara sefase (in-phase). Akibatnya, vektor magnetisasi longitudinal (Mz) turun menjadi nol dan muncul magnetisasi transversal maksimum (Mxy).\n\nSegera setelah pulsa RF dimatikan, sistem spin melepaskan energi dan kembali menuju kesetimbangan awal melalui dua proses yang terjadi secara bersamaan tetapi memiliki laju yang berbeda:\n• Pemulihan Longitudinal (Mz): Tumbuhnya kembali komponen magnetisasi sepanjang sumbu Z menuju M₀ (Relaksasi T1).\n• Peluruhan Transversal (Mxy): Hilangnya komponen magnetisasi pada bidang XY akibat hilangnya koherensi fase antar-spin (Relaksasi T2).",
                    analogi = "Analogi Pegas & Penari Sinkron: Bayangkan sekelompok penari yang awalnya berdiri tegak (sejajar B₀). Pulsa RF adalah aba-aba musik yang membuat mereka kompak membungkuk 90° dan berputar serempak (in-phase). Saat musik berhenti (RF mati), para penari perlahan kembali berdiri tegak karena melepaskan lelah ke lantai (Relaksasi T1), sembari putaran mereka mulai tidak kompak satu sama lain karena saling bersenggolan (Relaksasi T2).",
                    labType = "m2_kompor_kontras"
                ),
                MriNode(
                    code = "M02.T1.2",
                    level = 1,
                    title = "Relaksasi T1 (Spin-Lattice / Longitudinal Relaxation)",
                    summaryShort = "Relaksasi T1 adalah waktu yang dibutuhkan magnetisasi longitudinal (Mz) untuk pulih kembali sebesar 63% dari nilai kesetimbangan awalnya (M₀) melalui pelepasan energi termal dari spin proton ke lingkungan molekuler sekitar (lattice).",
                    detailFull = "Disebut spin-lattice relaxation karena energi yang diserap oleh spin selama eksitasi RF dilepaskan kembali ke kisi molekuler di sekitarnya (lattice). Saat energi dilepaskan, proton kembali ke tingkat energi rendah (parallel) dan vektor Mz tumbuh secara eksponensial mendekati M₀.\n\nRumus: Mz(t) = M0 · (1 - e^(-t / T1))\n\n• Definisi Konstanta T1: Waktu yang diperlukan agar Mz pulih mencapai 63% dari nilai magnetisasi maksimum M₀ setelah pulsa RF 90°.\n• Pada waktu 3 × T1, Mz telah pulih sekitar 95%; pada 5 × T1, Mz dianggap pulih sempurna (~99,3%).\n• Efisiensi T1 bergantung pada kesesuaian frekuensi gerak molekul (tumbling rate) dengan frekuensi Larmor.",
                    analogi = "Analogi Kopi Panas di Ruangan: Spin yang tereksitasi seperti secangkir kopi panas, sedangkan lattice (lingkungan molekul) adalah udara kamar. Kopi melepaskan panas ke udara sekitar hingga suhunya kembali seimbang. Jika udara sekitar menyerap panas dengan efisien (seperti molekul lemak), kopi cepat dingin (T1 pendek). Jika wadahnya terisolasi (seperti air murni/CSF), kopi lama dinginnya (T1 panjang).",
                    labType = "m2_fantom_otak",
                    htmlSimulationAsset = "modul2/index.html"
                ),
                MriNode(
                    code = "M02.T1.4",
                    level = 1,
                    title = "Pembobotan Citra: T1w, T2w, dan PDw",
                    summaryShort = "T1w (TR pendek, TE pendek), T2w (TR panjang, TE panjang), PDw (TR panjang, TE pendek). Nilai TR mengontrol pemulihan T1; nilai TE mengontrol peluruhan T2.",
                    detailFull = "Rumus: S = PD · (1 - e^(-TR / T1)) · e^(-TE / T2)\n\n• T1-Weighted (TR pendek 400–700 ms, TE pendek 10–25 ms): Memaksimalkan perbedaan T1 antar-jaringan (Lemak tampak terang, CSF tampak gelap).\n• T2-Weighted (TR panjang ≥ 2000 ms, TE panjang ≥ 80 ms): Meminimalkan efek T1 dan menonjolkan perbedaan peluruhan T2 (CSF tampak sangat terang).\n• PD-Weighted (TR panjang ≥ 2000 ms, TE pendek ≤ 25 ms): Meminimalkan kontras T1 maupun T2 sehingga sinyal mencerminkan kerapatan proton (Proton Density).",
                    analogi = "TR dan TE seperti dua kenop pengatur kontras foto: TR mengatur seberapa penuh daya pulih sebelum bidikan berikutnya, sedangkan TE mengatur seberapa lama gema dibiarkan meluruh sebelum direkam.",
                    labType = "m2_parameter_sekuens",
                    htmlSimulationAsset = "modul2/index.html"
                ),
                MriNode(
                    code = "M02.T2.1",
                    level = 2,
                    title = "Relaksasi T2 & T2* (Spin-Spin / Transverse Relaxation)",
                    summaryShort = "Relaksasi T2 adalah waktu yang dibutuhkan magnetisasi transversal (Mxy) untuk meluruh hingga tersisa 37% dari nilai awalnya akibat interaksi medan magnet mikro antar-spin (dephasing).",
                    detailFull = "Setelah pulsa RF dimatikan, spin-spin yang tadinya berpresesi sefase (in-phase) mulai mengalami perbedaan kecepatan presesi sesaat karena adanya fluktuasi medan magnet lokal dari spin tetangganya (interaksi spin-spin). Akibatnya terjadi kehilangan koherensi fase (dephasing) dan Mxy meluruh secara eksponensial:\n\nRumus: Mxy(t) = Mxy0 · e^(-t / T2)\n\n• Definisi Konstanta T2: Waktu yang diperlukan hingga Mxy berkurang sebanyak 63%, atau menyisakan 37% dari nilai awalnya (Mxy0).\n• Perbedaan T2 vs T2*: Peluruhan T2 murni hanya disebabkan oleh interaksi acak molekuler antar-spin (tidak dapat dibalikkan). Sedangkan T2* (T2-star) adalah peluruhan yang jauh lebih cepat karena gabungan efek T2 murni ditambah inhomogenitas medan magnet statis B₀: 1/T2* = 1/T2 + γ·ΔB₀. Efek inhomogenitas B₀ ini dapat dikompensasi dengan pulsa 180° pada sekuens Spin Echo.\n• Pada setiap jaringan biologis, nilai T2 selalu lebih pendek atau sama dengan T1 (T2 ≤ T1).",
                    analogi = "Analogi Pelari di Lintasan: Saat aba-aba mulai (pulsa RF), 10 pelari mulai berlari sejajar (in-phase). Seiring waktu, karena perbedaan kecepatan kecil saat saling berdekatan (interaksi spin-spin), jarak antar-pelari mulai menyebar (dephasing). Waktu hingga barisan pelari menyebar dan kehilangan kekompakan hingga tinggal 37% itulah T2. Jika lintasan larinya juga naik-turun tidak rata (inhomogenitas B₀), mereka menyebar jauh lebih cepat lagi—itulah T2*.",
                    labType = "m2_peluruhan_t2",
                    htmlSimulationAsset = "modul2/index.html"
                ),
                MriNode(
                    code = "M02.T2.2",
                    level = 2,
                    title = "Pengaruh Lingkungan Molekuler (Lemak vs Air/CSF)",
                    summaryShort = "Kecepatan gerak molekul (tumbling rate) menentukan efisiensi relaksasi: Lemak memiliki T1 pendek dan T2 pendek, sedangkan Air bebas (CSF) memiliki T1 panjang dan T2 panjang.",
                    detailFull = "Molekul di dalam tubuh bergerak, berotasi, dan bertumbukan dengan frekuensi tertentu yang disebut frekuensi tumbling (ωc):\n• Lemak (Molekul Sedang-Besar): Memiliki laju tumbling lambat-sedang yang dekat dengan frekuensi Larmor proton. Transfer energi ke lattice sangat efisien (T1 pendek) dan interaksi medan lokal antar-spin kuat (T2 pendek).\n• Air Bebas / CSF (Molekul Kecil): Bergerak sangat cepat (high tumbling rate ≫ frekuensi Larmor). Transfer energi ke lattice tidak efisien (T1 panjang) dan fluktuasi medan magnet antar-spin saling meniadakan (motional averaging) sehingga fase bertahan lama (T2 panjang).\n\nTabel Perbandingan Jaringan (pada 1.5 Tesla):\n• Lemak (Adiposa): T1 ~250–260 ms (Pendek) | T2 ~60–80 ms (Pendek-Sedang) | T1W: Terang (Hiperintens) | T2W: Sedang-Terang\n• Substansia Alba (WM): T1 ~600–780 ms (Sedang) | T2 ~70–90 ms (Sedang) | T1W: Agak Terang | T2W: Agak Gelap\n• Substansia Grisea (GM): T1 ~900–1100 ms (Agak Panjang) | T2 ~90–100 ms (Sedang) | T1W: Agak Gelap | T2W: Agak Terang\n• Cairan Serebrospinal (CSF / Air): T1 ~2400–4000 ms (Panjang) | T2 ~1600–2000 ms (Panjang) | T1W: Gelap (Hipointens) | T2W: Sangat Terang (Hiperintens)",
                    analogi = "Analogi Oper Bola di Kereta: Menyerahkan barang (energi T1) ke orang di kereta sebelah paling mudah jika kecepatan transfer energi ke kisi berlangsung optimal bila frekuensi gerak molekul cocok dengan frekuensi Larmor (seperti pada molekul lemak, menghasilkan T1 pendek). Jika frekuensi molekul terlalu lambat atau terlalu cepat (seperti air/CSF), transfer energi menjadi tidak efisien sehingga T1 sangat panjang.",
                    labType = "m2_peluruhan_t2",
                    htmlSimulationAsset = "modul2/index.html"
                )
            )
        ),
        MriModule(
            id = "M03A",
            code = "M03A",
            title = "Sekuens Spin Echo, Inversion Recovery & TSE",
            subtitle = "Konvensional SE, IR (STIR & FLAIR), Turbo Spin Echo (TSE/FSE) & DRIVE",
            description = "Membedah pembentukan echo 90°–180°, waktu inversi TI untuk null point jaringan (STIR/FLAIR), pengisian ruang-k berantai TSE dengan phase reordering, serta pemulihan magnetisasi transversal dengan DRIVE.",
            levelBadge = "T1 - T3 Sekuens",
            iconName = "pulse",
            nodes = listOf(
                 MriNode(
                    code = "M03A.T1.1",
                    level = 1,
                    title = "T1.1 Spin echo konvensional (SE)",
                    summaryShort = "Sekuens pulsa mengatur kapan sistem memberi pulsa RF dan gradien. Gema dibuat dengan pulsa refokus 180° (spin echo) atau dengan gradien (gradient echo).",
                    detailFull = "FID meluruh dalam sekitar 10 ms karena ketidakhomogenan medan B0, terlalu cepat untuk mengukur relaksasi T1 atau T2. Sekuens pulsa memfase ulang momen magnetik hidrogen di waktu yang lebih lambat, dan sinyal yang muncul disebut echo. Pada echo itu, kontras citra bergantung pada beda waktu pemulihan T1, peluruhan T2, atau kerapatan proton.\n\nSpin echo: pulsa RF refokus 180°. Gradient echo: gradien (lihat Modul 3B).\nDiagram sekuens: sumbu mendatar adalah waktu. Biasanya ada lima garis: RF, tiga gradien (pemilih irisan, penyandi fase, penyandi frekuensi), dan sinyal. Bentuk di atas garis berarti gradien berpolaritas positif, di bawah garis negatif, dan tingginya menunjukkan amplitudo.",
                    analogi = "Analogi dansa: semua tarian memakai gerak tangan dan kaki, seperti semua sekuens memakai pulsa RF dan gradien. Waktu dan koordinasinya yang menentukan jenis tarian, yaitu bobot citra.",
                    labType = "m3a_spin_echo"
                ),
                MriNode(
                    code = "M03A.T1.2",
                    level = 1,
                    title = "T1.2 Inversion recovery (IR)",
                    summaryShort = "IR adalah spin echo yang diawali pulsa pembalik 180°. Jarak dari pulsa itu ke pulsa 90° disebut TI. TI adalah pengendali kontras terkuat pada IR.",
                    detailFull = "Pulsa 180° di awal TR membalik NMV dari +z ke −z. Setelah pulsa dilepas, NMV pulih ke B0 lewat relaksasi T1. Pada waktu tertentu dipasang pulsa 90°, lalu FID difase ulang oleh pulsa 180° sehingga muncul spin echo pada TE.\n\nTI (time from inversion): bila pulsa 90° datang saat NMV sudah melewati bidang transversal tetapi belum pulih penuh, kontras berbobot T1. Bila menunggu pemulihan penuh, citra berbobot PD karena lemak dan air sama-sama pulih.\nTE mengatur kontras T2 seperti pada spin echo. TR selalu panjang agar semua vektor pulih penuh sebelum pulsa pembalik berikutnya, sehingga TR tidak mengatur kontras T1 pada IR.\nKegunaan: citra berbobot T1 yang sangat kuat untuk anatomi. Jaringan mulai pulih dari inversi penuh, bukan dari bidang transversal, sehingga beda T1 punya waktu lebih lama untuk tampak. Gadolinium memperpendek T1 jaringan yang menyerap, sehingga sinyalnya naik.\nTR: agar NMV pulih penuh pada 1 T, TR harus lebih dari 3000 ms. Kebanyakan sistem memakai IR bersama TSE.\nPathology weighting: TE dipanjangkan agar jaringan ber-T2 panjang bersinyal tinggi. Citra tetap dominan T1, tetapi patologi terang.\n\nParameter Sekuens Inversion Recovery (TI | TE | TR):\n• Pembobotan T1: TI 400–800 ms (bergantung kuat medan B0) | TE 10–20 ms | TR ≥ 3000 ms\n• Pembobotan PD: TI ~1800 ms | TE 10–20 ms | TR ≥ 3000 ms\n• Pathology Weighting: TI 400–800 ms | TE ≥ 70 ms | TR ≥ 3000 ms",
                    analogi = "Analogi: bandul yang dibalik ke bawah lalu berayun naik. TI menentukan di titik mana radiografer melakukan akuisisi eksitasi.",
                    labType = "m3a_ir_null"
                ),
                MriNode(
                    code = "M03A.T1.3",
                    level = 1,
                    title = "T1.3 Penekanan sinyal dengan IR",
                    summaryShort = "STIR memilih TI pendek sehingga lemak tepat di titik null saat pulsa 90° tiba. Tanpa magnetisasi longitudinal, lemak tidak menghasilkan sinyal. Sinyal lemak ditekan.",
                    detailFull = "TI STIR sama dengan waktu yang dibutuhkan vektor lemak untuk pulih dari inversi penuh sampai tepat di bidang transversal. Saat itu tidak ada komponen longitudinal lemak. Pulsa 90° tidak punya apa-apa untuk dimiringkan, sehingga tidak ada komponen transversal dan sinyal lemak nol. TI sekitar 100 sampai 175 ms biasanya menekan lemak, meski sedikit berbeda antar kuat medan.\nPersamaan 3.3: SI = 1 − 2e^(−t/T1). SI = 0 saat t = 0,693 × T1, jadi TI yang meniadakan suatu jaringan selalu 0,69 kali T1 jaringan itu.\n\nKegunaan: penting pada pencitraan muskuloskeletal, karena sumsum tulang berlemak ditekan sehingga memar tulang dan tumor tampak lebih jelas. Juga berguna untuk menekan lemak secara umum.\nJangan dipakai bersama kontras: gadolinium memperpendek T1 jaringan yang menyerap sehingga mendekati T1 lemak, dan jaringan itu ikut ditiadakan.\nParameter: TI 150 sampai 175 ms, TE 50 ms atau lebih, TR 4000 ms atau lebih, turbo factor 16 sampai 20.",
                    analogi = "Analogi: radiografer memotret bandul tepat saat ia melintas di titik nol, sehingga bandul tersebut \"hilang\" dari citra diagnostik.",
                    labType = "m3a_ir_null"
                ),
                MriNode(
                    code = "M03A.T1.4",
                    level = 1,
                    title = "T1.4 Turbo spin echo (TSE / FSE / RARE): gagasan dasar",
                    summaryShort = "Beberapa gema dalam satu TR mempercepat pindai, tetapi menambah T2 blur dan SAR.",
                    detailFull = "Pada turbo spin echo, satu TR memuat beberapa gema (echo train). Tiap gema mengisi garis ruang-k yang berbeda, sehingga waktu pindai turun kira-kira sebanding dengan panjang echo train. Harganya: gema belakangan lebih lemah sehingga citra bisa kabur (T2 blur), dan pulsa 180° yang lebih banyak menaikkan SAR.\nAda pula kompromi SNR terhadap waktu: pindai lebih singkat berarti lebih sedikit rata-rata sinyal, jadi SNR turun. Gema yang mengisi tengah ruang-k paling menentukan kontras, sehingga urutan pengisian ruang-k ikut mengatur kontras. Hubungan ini dibahas di Modul 4.",
                    analogi = "Analogi: memotret banyak foto dalam sekali kedipan lampu kilat. Lebih cepat, tetapi foto-foto terakhir lebih redup.",
                    labType = "m3a_tse"
                ),
                MriNode(
                    code = "M03A.T2.1",
                    level = 2,
                    title = "T2.1 Spin echo: gema simetris & persamaan sinyal",
                    summaryShort = "Gradient echo memakai sudut flip α tanpa pulsa 180°. Inversion recovery menambah pulsa 180° di awal dan menunggu selama TI.",
                    detailFull = "Gradient echo: S ∝ PD·sin α·(1 − E1)/(1 − E1·cos α)·e^(−TE/T2*), dengan E1 = e^(−TR/T1). Sudut yang memberi sinyal terbesar disebut sudut Ernst: α = arccos(E1).\nInversion recovery diawali pulsa 180°, lalu menunggu TI. Jaringan yang magnetisasinya melewati nol tepat saat pulsa 90° tiba tampak gelap. STIR memilih TI untuk menekan lemak, FLAIR untuk menekan cairan.\nNilai Relaksasi Jaringan pada 1,5 T (T1 | T2 | PD):\n• Lemak (Adiposa): T1 = 260 ms | T2 = 85 ms | PD = 1,00\n• Substansia Grisea (GM): T1 = 925 ms | T2 = 100 ms | PD = 0,86\n• Substansia Alba (WM): T1 = 790 ms | T2 = 92 ms | PD = 0,77\n• Cairan Serebrospinal (CSF): T1 = 2400 ms | T2 = 1600 ms (in-vivo; 180 ms pada model fantom diperpendek) | PD = 1,00",
                    analogi = "Analogi: bandul yang dibalik lalu berayun kembali. Saat bandul tepat melewati titik nol, radiografer melakukan akuisisi eksitasi, dan jaringan tersebut teredam (null) dari citra diagnostik.",
                    labType = "m3a_spin_echo"
                ),
                MriNode(
                    code = "M03A.T2.2",
                    level = 2,
                    title = "T2.2 TSE: phase reordering & TE efektif",
                    summaryShort = "Gema dalam echo train punya TE berbeda-beda, jadi TE yang dipilih hanyalah TE efektif. Sistem menata ulang langkah fase (phase reordering) agar gema yang TE-nya dekat TE efektif mengisi baris tengah ruang-k, yang paling menentukan sinyal dan kontras.",
                    detailFull = "Kemiringan penyandian fase yang sangat curam menghasilkan gema beramplitudo rendah, dan datanya ditempatkan pada baris tepi ruang-k. Kemiringan landai menghasilkan gema beramplitudo tinggi, dan datanya ditempatkan pada baris tengah. Karena itu pada TSE baris tidak diisi berurutan dari atas ke bawah. Urutannya dicocokkan dengan seberapa dekat TE tiap gema dengan TE efektif.\nContoh dari buku untuk bobot T2: TE efektif 100 ms, TR 4000 ms, turbo factor 16. Penyandian fase paling landai dilakukan pada gema di sekitar 100 ms. Gema paling awal dan paling akhir punya penyandian curam, amplitudonya kecil, dan datanya mengisi tepi ruang-k. Gema awal berisi data PD atau T2 yang sangat kuat. Keduanya tetap ikut membentuk citra, tetapi pengaruhnya jauh lebih kecil, sehingga citra akhir terutama berbobot T2.\nPhase reordering juga dipakai pada beberapa teknik untuk mengurangi artefak gerak (lihat Modul 8A).",
                    analogi = "Analogi: panitia yang menaruh peserta terbaik di barisan depan. Gema yang TE-nya paling cocok diberi tempat di tengah ruang-k, dan gema lain di barisan belakang.",
                    labType = "m3a_tse"
                ),
                MriNode(
                    code = "M03A.T2.3",
                    level = 2,
                    title = "T2.3 Titik nol IR: TI untuk menekan jaringan",
                    summaryShort = "Setelah pulsa pembalik 180°, magnetisasi pulih dari −M0 menuju +M0. Jaringan tidak bersinyal bila pulsa 90° tiba tepat saat magnetisasinya melewati nol, yaitu pada TI ≈ T1 × ln 2 ≈ 0,69 × T1.",
                    detailFull = "Dari persamaan. Setelah pulsa 180°, magnetisasi longitudinal pulih menurut Mz(TI) = M0 (1 − 2e−TI/T1). Mz bernilai nol bila e−TI/T1 = ½, sehingga TInol = T1 × ln 2 ≈ 0,69 × T1. Rumus ini berlaku bila TR cukup panjang sehingga magnetisasi pulih penuh di tiap pengulangan. Bila TR terbatas, TI nol sedikit lebih pendek: TInol = T1 × ln[2 / (1 + e−TR/T1)].\nContoh dengan T1 pada data modul (1,5 T, TR panjang).\n\nEstimasi Titik Null IR pada 1,5 T dengan TR Panjang (T1 | TI_null ≈ 0,69 × T1 | Aplikasi Klinis):\n• Lemak (Adiposa): T1 = 260 ms | TI_null ≈ 180 ms | STIR (supresi sinyal lemak)\n• Substansia Alba (WM): T1 = 790 ms | TI_null ≈ 548 ms | —\n• Substansia Grisea (GM): T1 = 925 ms | TI_null ≈ 641 ms | —\n• Cairan Serebrospinal (CSF): T1 = 2400 ms | TI_null ≈ 1664 ms | FLAIR (atenuasi sinyal cairan)",
                    analogi = "Analogi: bak air yang mula-mula dibalik menjadi penuh ke arah \"minus\", lalu perlahan terisi kembali. Foto (pulsa 90°) hanya \"kosong\" bila diambil tepat saat permukaan air melewati garis nol. Tiap jaringan terisi dengan kecepatan berbeda, jadi saat garis nolnya pun berbeda.",
                    labType = "m3a_ir_null"
                ),
                MriNode(
                    code = "M03A.T3.1",
                    level = 3,
                    title = "T3.1 Driven equilibrium (DRIVE)",
                    summaryShort = "Single-shot TSE mengambil semua baris ruang-k sekaligus. Driven equilibrium menambahkan pulsa flip balik di akhir echo train agar sisa magnetisasi transversal segera kembali ke arah longitudinal, sehingga TR bisa lebih pendek dan cairan tetap terang.",
                    detailFull = "SS-TSE (single-shot fast atau turbo spin echo) menggabungkan teknik partial Fourier dengan TSE (lihat Modul 4C). Separuh baris ruang-k diakuisisi dalam satu TR, dan separuh lainnya ditranspos. Karena seluruh data citra diambil sekaligus, waktu pencitraan turun lebih jauh.\nDriven equilibrium (DRIVE) adalah modifikasi TSE: pulsa eksitasi dengan sudut flip terbalik (pulsa reset, sekitar −90° atau 270°) dipasang di akhir echo train. Pulsa ini mendorong magnetisasi transversal ke bidang longitudinal, sehingga siap dieksitasi pada awal TR berikutnya dan tidak perlu menunggu lama untuk relaksasi T1. Sebagian produsen memberi pulsa refokus 180° dahulu sebelum pulsa 90° pemulihan. Karena air punya T1 dan T2 terpanjang, sebagian besar magnetisasi itu adalah air, sehingga sinyal struktur berbasis cairan seperti CSF naik walau TR lebih pendek daripada TSE biasa.",
                    analogi = "Analogi: mengosongkan gelas sebelum menuang lagi. Pulsa reset mengembalikan sisa magnetisasi ke posisi siap pakai, jadi gelas berikutnya bisa langsung diisi tanpa menunggu lama.",
                    labType = "m3a_drive"
                )
            )
        ),
        MriModule(
            id = "M03B",
            code = "M03B",
            title = "Sekuens Gradient Echo & Keadaan Tunak",
            subtitle = "Sudut Ernst, Gradien Bipolar, Steady State (Koheren, Inkoheren, Reverse-Echo), TrueFISP & EPI",
            description = "Mengupas refokusing gema memakai gradien bipolar berpasangan, optimasi sinyal sudut Ernst, manipulasi sisa magnetisasi transversal pada keadaan tunak (RF spoiling vs rewinder), balanced GRE (TrueFISP), serta osilasi gradien ultra-cepat EPI.",
            levelBadge = "T1 - T3 Cepat",
            iconName = "bolt",
            nodes = listOf(
                MriNode(
                    code = "M03B.T1.1",
                    level = 1,
                    title = "T1.1 Dasar gradient echo",
                    summaryShort = "Gradient echo memakai sudut flip kecil dan gradien, bukan pulsa 180°, untuk membentuk gema. TR dan TE bisa lebih pendek, tetapi ketidakhomogenan medan tidak dikoreksi.",
                    detailFull = "Ada dua beda utama dengan spin echo. Pertama, sudut flip biasanya di bawah 90°. Magnetisasi hanya dimiringkan sedikit, sehingga pemulihan longitudinal cepat tuntas dan TR boleh pendek. Karena TR ikut menentukan waktu pindai, pindai jadi lebih singkat.\nKedua, gema dibuat oleh gradien. Pulsa 180° tidak cocok di sini: pada sudut kecil sebagian besar magnetisasi masih longitudinal, sehingga pulsa itu justru membaliknya ke arah −z, bukan merefokus komponen transversal.\n\nGradien yang mengacak fase disebut spoiler. Gradien yang merapikannya kembali disebut rewinder.\nGema dibuat oleh gradien bipolar: lobus negatif lebih dulu (sinyal awal, FID, hilang), lalu lobus positif yang luasnya dua kali lipat. Gema muncul saat luas positif menetralkan luas negatif, yaitu pada TE.\nGradien hanya merapikan fase yang ia buat sendiri. Pengaruh ketidakhomogenan medan tetap tersisa, sehingga kontrasnya berbobot T2* dan artefak kerentanan magnetik bertambah.\n\nNama sekuens berbeda antar produsen. Acuan: Westbrook dan Talbot, MRI in Practice, Bab 4 (ditulis ulang).\nTerminologi Sekuens Gradient Echo Antar-Vendor (GE | Philips | Siemens):\n• Koheren (Rewound GRE): GRASS | FFE | FISP\n• Inkoheren (Spoiled GRE): SPGR | T1-FFE | FLASH\n• Balanced Steady-State (bSSFP): FIESTA | b-FFE | TrueFISP\n• Fast Gradient Echo (Ultra-cepat): Fast GRASS / Fast SPGR | TFE | TurboFLASH",
                    analogi = "Analogi: pelari di lintasan. Gradien membuat pelari berpencar karena kecepatannya berbeda. Saat gradien dibalik, yang tertinggal berlari lebih cepat dan yang di depan lebih lambat, sehingga semua tiba bersama di garis finis (gema). Rintangan acak di lintasan (medan tidak homogen) tidak ikut dibalik.",
                    labType = "m3b_gre_kompor"
                ),
                MriNode(
                    code = "M03B.T1.2",
                    level = 1,
                    title = "T1.2 Gradien bipolar (spoiler & rewinder)",
                    summaryShort = "Gradien mengubah medan secara linear sehingga momen magnetik berubah frekuensi: sebagian cepat, sebagian lambat. Gradien yang mendephase disebut spoiler, yang merephase disebut rewinder. Gema muncul saat fase semuanya sejajar lagi.",
                    detailFull = "Mengapa bukan pulsa 180°? Pada spin echo, sudut flip besar membuat magnetisasi masih transversal saat pulsa 180° tiba, sehingga bisa direfase. Pada gradient echo, sudut flip kecil meninggalkan banyak magnetisasi longitudinal. Pulsa 180° justru membaliknya ke −z, bukan merefase komponen transversal. Maka dipakai gradien.\nMendephase. Tanpa gradien semua momen magnetik berpresesi pada frekuensi sama. Gradien pada magnetisasi koheren mengubah kuat medan sepanjang sumbunya: ujung berkuat medan tinggi berpresesi lebih cepat (tepi terdepan kipas, merah), ujung berkuat medan rendah lebih lambat (tepi tertinggal, ungu). Momen tidak lagi di tempat yang sama pada waktu yang sama. Gradien yang mendephase begini disebut spoiler dan prosesnya gradient spoiling.\nMerephase. Gradien dengan arah berlawanan dipasang pada magnetisasi yang sudah menyebar. Inti lambat kini mengalami medan lebih tinggi dan mempercepat, sedangkan inti cepat mengalami medan lebih rendah dan melambat. Sesaat kemudian yang lambat menyusul yang cepat, semuanya sefase, dan sinyal maksimum diinduksi di koil penerima: gradient echo. Gradien yang merephase begini disebut rewinder.\n\nPolaritas gradien (arah arus di koil gradien) menentukan apakah medan ditambah atau dikurangi.\nGema dibuat oleh gradien bipolar dengan dua lobus. Lobus negatif lebih dulu, menambah dephasing dan menghilangkan FID. Polaritas lalu dibalik, merephase hanya momen yang tadi didephase oleh lobus negatif, dan gema muncul pada TE.\nLuas lobus negatif separuh luas lobus positif. Pada sekuens dasar, gradien penyandi frekuensi dipakai secara bipolar untuk ini (lihat Modul 4A).",
                    analogi = "Analogi: pelari berpencar karena beda kecepatan, lalu semua berbalik dan yang tertinggal berlari lebih cepat sehingga tiba bersama di garis finis.",
                    labType = "m3b_bipolar_rewinder"
                ),
                MriNode(
                    code = "M03B.T1.3",
                    level = 1,
                    title = "T1.3 Sudut Ernst",
                    summaryShort = "Sudut Ernst adalah sudut flip yang memberi sinyal terbesar untuk satu jaringan pada TR tertentu: α = arccos(e^(−TR/T1)). Di atas sudut Ernst, saturasi dan kontras T1 bertambah. Di bawahnya, kontras lebih bergantung pada PD.",
                    detailFull = "Setiap jaringan punya sudut Ernst sendiri karena T1-nya berbeda. Jaringan ber-T1 pendek punya sudut Ernst lebih besar, jaringan ber-T1 panjang lebih kecil. Pada TR pendek (kira-kira di bawah 50 ms, kisaran yang lazim untuk keadaan tunak), sudut Ernst jaringan otak hanya beberapa belas derajat.\nSinyal terbesar belum tentu kontras terbaik. Kurva sinyal jaringan berdekatan di sekitar sudut Ernst, sehingga untuk membedakan jaringan, praktiknya dipakai sudut sedang (kira-kira 30° sampai 45°) yang lebih besar dari sudut Ernst. ",
                    analogi = "Analogi: mendorong ayunan. Ada kekuatan dorongan yang paling efektif untuk jeda tertentu. Dorong lebih keras daripada itu, dan ayunan tidak sempat kembali sebelum dorongan berikutnya.",
                    labType = "m3b_ernst"
                ),
                MriNode(
                    code = "M03B.T2.1",
                    level = 2,
                    title = "T2.1 Keadaan tunak (steady state)",
                    summaryShort = "Kontras T1 muncul bila vektor belum pulih penuh di antara dua pulsa RF, jadi TR pendek memperkuatnya. Lemak (T1 pendek) pulih lebih cepat daripada air (T1 panjang), sehingga lemak terang dan air relatif gelap. Bila TR lebih panjang daripada T1 jaringan, semua pulih penuh dan kontras T1 hilang.",
                    detailFull = "Saat pulsa RF berikutnya tiba, pulsa itu membalik magnetisasi longitudinal menjadi transversal. Karena lemak sudah lebih pulih, magnetisasi longitudinalnya lebih besar, sehingga magnetisasi transversalnya juga lebih besar. Lemak bersinyal tinggi (hiperintens). Air belum banyak pulih, jadi sinyalnya rendah (relatif hipointens).\nSaturasi. Bila vektor terdorong melewati 90°, ia disebut jenuh sebagian. Bila terdorong sampai 180°, ia jenuh penuh. Jenuh sebagian pada lemak dan air membuat kontras T1 maksimal. Pada TR pendek, vektor yang belum pulih penuh terdorong melewati 90° oleh pulsa berikutnya, dan besar jenuhnya berbeda karena titik pemulihannya berbeda. Pada TR panjang, keduanya pulih penuh, langsung terbalik ke bidang transversal, dan tidak jenuh. Besar sinyalnya kemudian hanya ditentukan oleh kerapatan proton. Sudut flip juga memengaruhi saturasi, dan ini dibahas lebih lanjut pada bagian gradient echo.\nKeadaan tunak. Bila TR lebih pendek daripada T1, beberapa pulsa RF pertama menghasilkan magnetisasi transversal yang berbeda-beda, karena jaringan mulai dari titik pemulihan yang berbeda. Perlu beberapa TR sampai semuanya mantap. Pada keadaan tunak, vektor pulih ke titik yang sama di tiap TR dan selalu terbalik ke titik yang sama. Pulsa-pulsa sebelumnya disebut pulsa persiapan atau dummy karena sinyalnya diabaikan. Lamanya tergantung pada B0, kerapatan proton, sudut flip, T1, dan durasi pulsa RF.\n\nPemulihan T1 disebabkan oleh pertukaran energi spin-kisi. Efisiensinya bergantung pada energi bawaan jaringan dan seberapa cocok laju gerak molekul dengan frekuensi Larmor.\nPada medan yang lebih kuat, jaringan memerlukan waktu pemulihan lebih lama.\nKontras T1 dikendalikan oleh TR. Untuk kontras T1 yang baik, TR harus pendek.",
                    analogi = "Analogi: dua ember diisi dari keran. Ember kecil (lemak) cepat penuh, ember besar (air) lambat. Bila isinya dituang tiap kali dalam jeda singkat (TR pendek), ember kecil selalu menuang lebih banyak.",
                    labType = "m3b_steady_state"
                ),
                MriNode(
                    code = "M03B.T2.2",
                    level = 2,
                    title = "T2.2 Tiga sekuens keadaan tunak utama",
                    summaryShort = "Pada gradient echo koheren, FID dan gema terstimulasi sama-sama dibaca, sehingga bobot T1, PD, atau T2* bisa didapat. Pada yang inkoheren (spoiled), sisa magnetisasi dirusak sehingga hanya FID yang dibaca dan bobot T1 serta PD yang dominan.",
                    detailFull = "Koheren (rewound). Sekuens ini sudah dipakai sejak 1980-an, sebelum sekuens cepat seperti turbo muncul. Gradien penyandi fase dibalik untuk merapikan kembali semua magnetisasi transversal, sehingga koherensinya terjaga. Karena FID dan gema terstimulasi dibaca, TR boleh sepanjang TR spin echo T1w (kira-kira 400 ms), dan beberapa irisan bisa direkam dalam satu TR. Kalau sekuens gradient echo lain menghasilkan citra buruk, sekuens koheren layak dicoba. Parameter acuan klinis:\n• Pembobotan T1: TR ~400 ms | TE ~5 ms | Flip Angle 90°\n• Pembobotan PD: TR ~400 ms | TE ~5 ms | Flip Angle 20°\n• Pembobotan T2*: TR ~400 ms | TE ~15 ms | Flip Angle 20°",
                    analogi = "Analogi: dua orang berteriak bergantian di lembah. Pada sekuens koheren sistem menerima sinyal FID baru dan echo terstimulasi siklus sebelumnya. Pada sekuens inkoheren, sisa magnetisasi transversal diredam (RF spoiled) sehingga hanya sinyal FID murni yang dibaca.",
                    labType = "m3b_tiga_tunak"
                ),
                MriNode(
                    code = "M03B.T2.3",
                    level = 2,
                    title = "T2.3 T2* pada gradient echo",
                    summaryShort = "Gradient echo biasa memakai TE pendek, jadi bobotnya T2*. Itu cukup untuk kelainan berkontras besar, tetapi lesi halus bisa terlewat. Untuk melihatnya, dibutuhkan TE panjang yang memperlihatkan beda peluruhan T2 sejati.",
                    detailFull = "Contohnya pada tulang belakang servikal. Hernia diskus yang menekan sumsum tulang belakang tampak jelas pada gradient echo T2*: diskus bersinyal rendah menonjol ke kantung tekal berisi CSF yang bersinyal tinggi. Namun plak multipel sklerosis kecil di dalam korda bisa terlewat. TE-nya belum cukup panjang untuk mengukur beda waktu peluruhan T2 jaringan di sekitarnya, sehingga kelainan halus kurang terlihat.\nUntuk melihat lesi halus, pakai sekuens ber-TE panjang agar beda T2 sempat muncul sebelum gema terbentuk. Spin echo konvensional dan TSE adalah pilihan yang baik, tetapi punya beberapa kekurangan (dibahas di bab sebelumnya pada buku). TE panjang sulit dipakai pada gradient echo karena sekuens ini dirancang untuk TR pendek demi pindai cepat. Reverse-echo menggabungkan TR pendek dengan TE panjang, sehingga bobot T2 sejati bisa didapat dalam waktu pindai singkat.\nAcuan: Westbrook dan Talbot, MRI in Practice, Bab 4 (ditulis ulang). ",
                    analogi = "Analogi: dua lilin yang hampir sama. Bila observasi dilakukan sangat singkat (TE pendek), keduanya tampak isointens. Perbedaan peluruhan T2 intrinsik jaringan baru terungkap bila waktu tunggu dephasing (TE) diperpanjang secukupnya.",
                    labType = "m3b_t2star_servikal"
                ),
                MriNode(
                    code = "M03B.T3.1",
                    level = 3,
                    title = "T3.1 Balanced gradient echo (True FISP / FIESTA)",
                    summaryShort = "Balanced gradient echo adalah gradient echo koheren yang diubah agar tetap tunak tanpa saturasi: gradiennya seimbang dan fase pulsa RF berganti tiap TR. Hasilnya cepat, bersinyal tinggi, dan sedikit artefak aliran.",
                    detailFull = "Strategi pertama adalah gradien seimbang pada sumbu irisan dan frekuensi. Luas gradien di atas garis sama dengan luas di bawahnya, sehingga spin yang mengalir tidak mengumpulkan pergeseran fase bersih. Darah dan CSF yang bergerak tetap koheren dan bersinyal tinggi. Prinsipnya sama dengan kompensasi aliran.\nStrategi kedua: fase pulsa RF berganti tiap TR. Misalkan sudut penuhnya 90°. Pada TR pertama hanya setengahnya (45°) yang dipakai, lalu TR berikutnya memakai sudut penuh dengan fase bergantian, sehingga magnetisasi transversal selalu dibentuk pada arah fase yang berbeda. Saturasi dihindari, dan jaringan yang T1 dan T2-nya hampir sama, yaitu lemak dan air, bersinyal tinggi.\nDibanding gradient echo koheren, dipakai sudut lebih besar dan TR lebih pendek (menurut buku, di bawah sekitar 10 ms). Sekuens ini awalnya dibuat untuk jantung dan pembuluh besar, tetapi juga dipakai pada tulang belakang (terutama servikal) dan meatus akustikus internus karena aliran CSF berkurang, serta kadang pada sendi dan abdomen.\n\nEvaluasi Klinis Balanced Gradient Echo (TrueFISP / FIESTA / b-FFE):\n• Kelebihan: Waktu akuisisi sangat singkat; artefak aliran minimal; SNR dan detail anatomi 3D tinggi; kontras cairan-jaringan tegas (rasio T2/T1).\n• Keterbatasan: SNR lebih rendah pada akuisisi 2D; bising akustik gradien tinggi; rentan artefak banding B0; membutuhkan sistem gradien berkinerja tinggi.",
                    analogi = "Analogi: memutar tali lompat dengan arah berganti. Tiap putaran memakai tenaga penuh, tetapi tali tidak pernah macet di satu sisi.",
                    labType = "m3b_tiga_tunak"
                ),
                MriNode(
                    code = "M03B.T3.2",
                    level = 3,
                    title = "T3.2 Fast gradient echo & echo planar imaging (EPI)",
                    summaryShort = "Fast gradient echo memangkas TE dan TR agar satu volume terekam dalam satu tahan napas. EPI membaca banyak gema berurutan dari gradien baca yang berosilasi.",
                    detailFull = "Fast gradient echo. TE dipersingkat dengan beberapa cara: hanya sebagian gema yang dibaca (partial echo), bandwidth penerima dilebarkan, dan dipakai ramped sampling, yaitu pengambilan sampel dimulai sebelum gradien frekuensi mencapai amplitudo maksimum. Banyak sekuens cepat juga memakai pulsa persiapan 180° sebelum sekuens dimulai. Magnetisasi dibalik, lalu setelah jeda tertentu sekuens berjalan. Ini menguatkan bobot T1 dan dapat menekan jaringan tertentu seperti pada inversion recovery. Karena TE sangat pendek, beberapa irisan dapat diperoleh dalam satu tahan napas, dan pencitraan dinamis setelah kontras menjadi mungkin.\nEPI (echo planar imaging). Diawali satu atau lebih pulsa RF, lalu rangkaian gradient echo yang dihasilkan osilasi gradien baca. GE-EPI diawali pulsa eksitasi bersudut berapa saja. SE-EPI diawali pulsa 90° lalu 180°: waktunya lebih lama dan RF yang masuk ke pasien lebih banyak, tetapi kualitas citra umumnya lebih baik karena pulsa 180° membersihkan sebagian artefak akibat ketidakhomogenan medan dan pergeseran kimia. EPI bisa diawali pulsa apa pun, misalnya EPI-FLAIR (180°/90°/180° lalu bacaan EPI) yang menekan CSF jauh lebih cepat daripada FLAIR biasa.\n\nPada pencitraan single-shot, seluruh ruang-k terisi sekaligus, jadi laju pemulihan jaringan tidak kritis dan TR dianggap tak hingga.\nBobot PD atau T2* ditentukan oleh TE efektif, yaitu jarak dari pulsa eksitasi sampai pusat ruang-k terisi. Bobot T1 didapat dengan pulsa inversi sebelum eksitasi.\nGRASE adalah sekuens hibrid: kecepatan perefokusan gradien digabung dengan kemampuan pulsa RF mengimbangi efek T2*.",
                    analogi = "Analogi: fast gradient echo seperti lari cepat tanpa pemanasan. EPI seperti menyapu satu halaman penuh dengan satu tarikan pena zig-zag.",
                    labType = "m3b_epi"
                )
            )
        ),
        MriModule(
            id = "M03C",
            code = "M03C",
            title = "Mekanisme Kontras Khusus & Agen Kontras",
            subtitle = "DWI & Peta ADC, Skema Stejskal-Tanner, fMRI (BOLD), MTC & Kontras Gadolinium/SPIO",
            description = "Membahas pencitraan gerak acak Brown molekul air (DWI) dengan faktor pelemahan b Stejskal-Tanner, saturasi off-resonance MTC, hemodinamik otak fMRI BOLD, serta farmakodinamik relaksivitas kontras paramagnetik Gd dan besi superparamagnetik.",
            levelBadge = "T1 - T2 Lanjut",
            iconName = "science",
            nodes = listOf(
                MriNode(
                    code = "M03C.T1.1",
                    level = 1,
                    title = "T1.1 Diffusion-weighted imaging (DWI) & ADC",
                    summaryShort = "DWI membuat kontras dari gerak molekul air (difusi). Jaringan berdifusi terbatas (ADC rendah) bersinyal tinggi, sedangkan jaringan berdifusi bebas (ADC tinggi) kehilangan sinyal. Faktor b mengatur seberapa kuat bobot difusi.",
                    detailFull = "Difusi adalah gerak acak molekul di ruang ekstraselular akibat gerak termal, dibatasi oleh ligamen, membran, dan makromolekul. Perpindahan bersih per detik disebut apparent diffusion coefficient (ADC), parameter kontras intrinsik. ADC rendah pada ruang ekstraselular sempit (ligamen, banyak patologi) dan tinggi pada difusi bebas (substansia grisea dan alba normal, jaringan hati normal).\nDua gradien dipasang. Gradien pertama mendephase momen magnetik dan gradien kedua mencoba merefasenya. Bila molekul diam (ADC rendah), fase tidak berubah di antara dua gradien, sehingga refase berhasil dan sinyal tinggi. Bila molekul berdifusi bebas, ia berpindah di antara kedua gradien, fasenya berubah, refase gagal, dan sinyal turun. Jadi jaringan normal ber-ADC tinggi lebih gelap daripada jaringan patologis ber-ADC rendah.\nFaktor b (s/mm²) adalah parameter ekstrinsik dari amplitudo, durasi, dan jarak antar gradien (skema Stejskal–Tanner): b = γ² × G² × δ² × (Δ − δ/3), dengan G amplitudo gradien, δ durasi gradien, Δ jarak antar dua gradien. Makin besar b, makin kuat bobot difusi.\n\nNilai Tipikal ADC Jaringan Otak & Sinyal Relatif pada b = 1000 s/mm²:\n• Cairan Serebrospinal (CSF): ADC = 2,94 × 10⁻³ mm²/s | Sinyal relatif (S/S0) = 0,05 (hipointens)\n• Substansia Grisea (GM): ADC = 0,76 × 10⁻³ mm²/s | Sinyal relatif (S/S0) = 0,47\n• Substansia Alba (WM): ADC = 0,45 × 10⁻³ mm²/s | Sinyal relatif (S/S0) = 0,63",
                    analogi = "Analogi: dua orang diminta berdiri di tempat yang sama setelah jeda. Yang diam bisa kembali tepat ke posisinya (sinyal kuat), yang berkeliling tidak (sinyal hilang).",
                    labType = "m3c_dwi"
                ),
                MriNode(
                    code = "M03C.T1.2",
                    level = 1,
                    title = "T1.2 fMRI (BOLD)",
                    summaryShort = "fMRI memakai efek BOLD untuk melihat fungsi otak: sinyal naik sedikit saat area otak aktif.",
                    detailFull = "fMRI mengambil citra otak saat aktivitas atau rangsang, lalu saat istirahat. Kontrasnya bergantung pada blood oxygenation level dependent (BOLD): oksihemoglobin dan deoksihemoglobin punya kerentanan magnetik berbeda. Deoksihemoglobin bersifat paramagnetik dan membuat inhomogenitas medan lokal yang menyebabkan dephasing dan hilangnya sinyal. Saat aktif, aliran darah ke korteks naik sedangkan konsumsi oksigen hampir tidak, sehingga deoksihemoglobin berkurang, dephasing berkurang, dan sinyal naik. Efeknya singkat, jadi perlu sekuens sangat cepat dengan TE panjang. Citra \"off\" dikurangkan dari citra \"on\", lalu dianalisis statistik. Area yang melewati ambang dilapiskan pada citra anatomi.",
                    analogi = "Analogi: BOLD seperti sungai yang mengalir deras ke daerah yang sedang bekerja, membilas \"pengotor\" (deoksihemoglobin) sehingga sinyal lebih jernih.",
                    labType = "m3c_bold_mtc"
                ),
                MriNode(
                    code = "M03C.T1.3",
                    level = 1,
                    title = "T1.3 Magnetization transfer contrast (MTC)",
                    summaryShort = "MTC memakai pulsa RF off-resonance untuk menjenuhkan inti terikat, sehingga sinyal inti bebas turun.",
                    detailFull = "Magnetization transfer contrast (MTC): inti terikat (T2 sangat pendek, tidak bisa dicitrakan) punya rentang frekuensi presesi jauh lebih lebar, sehingga dieksitasi oleh pulsa RF beberapa kilohertz dari frekuensi inti bebas. Energi yang diserap menjenuhkan kolam inti terikat dan magnetisasi berpindah ke inti bebas, sehingga sinyal inti bebas turun.",
                    analogi = "Analogi: inti terikat seperti kerumunan yang tidak terlihat kamera tetapi bisa \"menarik\" sebagian sinyal orang di depan panggung. Bila kerumunan itu dibuat diam (dijenuhkan), orang di depan panggung ikut meredup.",
                    labType = "m3c_bold_mtc"
                ),
                MriNode(
                    code = "M03C.T1.4",
                    level = 1,
                    title = "T1.4 Zat kontras",
                    summaryShort = "Zat kontras tidak dicitrakan langsung, tetapi mengubah waktu relaksasi air di sekitarnya. Agen T1 (gadolinium) memendekkan T1 sehingga hiperintens pada T1w. Agen T2 (besi superparamagnetik) memendekkan T2 sehingga hipointens pada T2w.",
                    detailFull = "Patologi kaya air tampak jelas pada T2w, tetapi zat kontras dapat dipakai untuk menaikkan konspikuitas lewat perubahan selektif waktu relaksasi (relaxivity). Besarnya pemendekan bergantung pada konsentrasi zat.\n\nAgen T1: yang paling umum adalah gadolinium (Gd), logam tanah jarang. Gd alami sangat toksik dan tidak dapat diekskresi, sehingga dikelat dengan ligan seperti DTPA agar aman dikeluarkan. Gd paramagnetik dengan tujuh elektron tak berpasangan dan momen magnetik besar. Molekul air dapat mendekati Gd dan berfluktuasi mendekati frekuensi Larmor sehingga T1 inti hidrogen di dekatnya memendek. Molekul lemak terlalu besar untuk mendekat. Jaringan yang menyerap tampak hiperintens pada T1w.\nAgen T2: biasanya makromolekul besi superparamagnetik. Mereka mengganggu medan lokal sehingga momen magnetik di sekitarnya dephase, T2 memendek, dan jaringan hipointens pada T2w.\nSistem modern dapat menghitung banyak bobot dari satu akuisisi khusus dengan beragam TR dan TE, dan juga menghitung parameter untuk meniadakan jaringan tertentu.\n\nRingkasan Teknik Pencitraan Khusus (Tabel 2.14):\n• DWI: Membuat sekuens tipe Spin Echo peka terhadap gerak difusi molekuler menggunakan gradien bipolar kuat.\n• ADC: Parameter intrinsik jaringan yang menyatakan perpindahan bersih molekul air di ruang ekstraselular per detik (mm²/s).\n• Nilai b (b-value): Parameter ekstrinsik yang mengatur seberapa besar ADC memengaruhi kontras difusi (s/mm²).\n• Pencitraan Fungsional: Menilai fungsi fisiologis dan hemodinamik jaringan, bukan sekadar morfologi anatomis.\n• fMRI (BOLD): Memanfaatkan kontras suseptibilitas oksihemoglobin vs deoksihemoglobin untuk memetakan area korteks otak yang aktif.",
                    analogi = "Analogi: gadolinium seperti penari yang membuat air di sekitarnya bergerak sesuai irama Larmor, sehingga cepat \"pulih\".",
                    labType = "m3c_kontras"
                ),
                MriNode(
                    code = "M03C.T2.1",
                    level = 2,
                    title = "T2.1 Agen kontras paramagnetik: mekanisme",
                    summaryShort = "Agen kontras tidak dicitrakan langsung. Ion paramagnetik (misalnya gadolinium) mempercepat relaksasi air di sekitarnya, sehingga T1 dan T2 jaringan yang menyerapnya memendek.",
                    detailFull = "Paramagnetik. Gadolinium (Gd³⁺) punya banyak elektron tak berpasangan, sehingga momen magnetiknya sangat besar dibanding inti hidrogen. Medan kecil yang berfluktuasi di sekitar ion ini membantu proton air melepas energi ke lingkungan (kisi) lebih cepat. Karena itu Gd dipakai dalam bentuk kelat (terikat pada molekul pembawa), bukan sebagai ion bebas.\nDihitung dengan relaksivitas. 1/T1 = 1/T10 + r1·[C] dan 1/T2 = 1/T20 + r2·[C]. [C] adalah konsentrasi agen, r1 dan r2 adalah relaksivitas (satuan L/(mmol·s)). Makin tinggi konsentrasi, makin pendek T1 dan T2.\nMengapa agen T1 tampak mempengaruhi T1. Pada gadolinium, r1 dan r2 hampir sama besar. Namun T1 jaringan jauh lebih panjang daripada T2-nya, sehingga perubahan relatif T1 jauh lebih besar. Hasilnya, efek T1 dominan pada dosis klinis.\nBesi superparamagnetik. Partikel oksida besi menimbulkan medan lokal yang sangat tidak homogen. Proton yang lewat di dekatnya cepat kehilangan fase, sehingga T2 dan T2* memendek jauh lebih kuat daripada T1. Jaringan yang menyerapnya tampak gelap (hipointens), terutama pada citra T2w dan T2*w.\n\nAgen T1 (gadolinium): T1 memendek, jaringan hiperintens pada T1w.\nAgen T2 (besi superparamagnetik): T2 dan T2* memendek, jaringan hipointens pada T2w.",
                    analogi = "Analogi: gadolinium seperti petugas yang mempercepat pengisian ulang ember (T1). Besi superparamagnetik seperti gundukan di jalan lari yang membuat barisan pelari (spin) cepat berantakan (dephasing).",
                    labType = "m3c_kontras"
                ),
                MriNode(
                    code = "M03C.T2.2",
                    level = 2,
                    title = "T2.2 Kontras gadolinium secara konsep",
                    summaryShort = "Gadolinium dipakai terutama bersama citra T1w. Jaringan yang menyerap atau terisi agen punya T1 lebih pendek, sehingga lebih terang. Pada konsentrasi sangat tinggi, pemendekan T2 mulai mengimbangi dan sinyal bisa turun lagi.",
                    detailFull = "Yang terlihat adalah efeknya. Agen tidak tampak langsung. Yang berubah adalah T1 air di sekitarnya. Pada T1w (TR pendek), jaringan dengan T1 lebih pendek pulih lebih banyak di antara dua pulsa, sehingga sinyalnya naik. Pembahasan citra pascakontras T1w ada di M02 T2.3.\nDi mana tampak. Agen beredar di pembuluh. Jaringan yang kaya pembuluh, atau tempat agen keluar dari pembuluh ke jaringan, tampak lebih terang daripada sebelum kontras.\nTidak linear. Pada konsentrasi rendah sampai sedang, sinyal T1w naik. Pada konsentrasi sangat tinggi, T2 ikut memendek cukup kuat sehingga sinyal bisa melandai atau turun. \nDengan sekuens lain. STIR tidak cocok dipakai setelah gadolinium: T1 jaringan yang menyerap agen memendek mendekati T1 lemak, sehingga ikut ditekan (lihat M03A T1.3).\nKeselamatan agen kontras (misalnya retensi dan NSF) tidak dibahas di modul ini.",
                    analogi = "Analogi: seperti menyorotkan senter ke objek di foto. Sedikit cahaya membuat objek lebih jelas. Terlalu terang, justru silau dan detail hilang.",
                    labType = "m3c_kontras"
                ),
                MriNode(
                    code = "M03C.T2.3",
                    level = 2,
                    title = "T2.3 Skema Stejskal-Tanner (gradien bipolar & pulsa 180°)",
                    summaryShort = "Skema Stejskal-Tanner adalah spin echo dengan dua gradien difusi yang mengapit pulsa 180°. Spin yang diam kembali sefase dan sinyalnya utuh. Spin yang berdifusi tidak sepenuhnya sefase, sehingga sinyalnya turun: S = S0 · e^(−b · ADC).",
                    detailFull = "Urutannya. Pulsa 90°, gradien difusi pertama (lama δ, kuat G), pulsa 180°, gradien difusi kedua yang sama, lalu gema. Pulsa 180° membalik fase, jadi dua gradien identik di kedua sisinya berefek berlawanan dan saling meniadakan untuk spin yang tetap di tempat. Spin yang bergerak acak antara dua gradien itu tidak terbatalkan sempurna, sehingga sinyalnya berkurang. Gradien bipolar (polaritas berlawanan) dipakai di sisi lain pulsa 180° dengan prinsip pembatalan yang sama. Konsep gradien dan rephasing ada di M04A.\nNilai b. b = γ² G² δ² (Δ − δ/3). γ adalah rasio giromagnetik (rad/s/T), G kekuatan gradien (T/m), δ lama tiap gradien (s), Δ jarak antara kedua gradien (s). Satuannya s/m², biasanya ditulis s/mm². Makin besar G, δ, atau Δ, makin besar b dan makin kuat bobot difusinya.\nADC dari dua citra. Dengan citra b = 0 dan citra b tertentu: ADC = ln(S0 / Sb) / b. Dihitung per piksel, hasilnya adalah peta ADC (peta parameter, lihat M02 T1.1). Pada otak, nilai b yang umum dipakai ada di sekitar 0 dan 1000 s/mm².\nDua jenis parameter. Nilai b adalah pilihan operator (ekstrinsik), ADC adalah sifat jaringan (intrinsik). Gradien yang kuat dan cepat diperlukan agar b besar tercapai dalam waktu singkat (lihat M06A T2.1).",
                    analogi = "Analogi: dua lampu kilat: kilat pertama menandai posisi, kilat kedua memeriksa apakah orang itu masih di tempat yang sama. Yang diam tampak jelas (sinyal utuh), yang berpindah tampak kabur (sinyal turun).",
                    labType = "m3c_dwi"
                )
            )
        ),
        MriModule(
            id = "M04A",
            code = "M04A",
            title = "Pengodean Spasial: Dari Sinyal Menjadi Gambar",
            subtitle = "Pecahan 1 dari 3 M04 Pembentukan Citra (Tingkat 1 Dasar)",
            description = "Menjelaskan fungsi tiga gradien (pemilihan irisan, pengodean fase, pengodean frekuensi), menghubungkan tebal irisan dengan transmit bandwidth dan kekuatan gradien, serta menghubungkan gradien baca dengan FOV.",
            levelBadge = "T1 Dasar",
            iconName = "grid",
            nodes = listOf(
                MriNode(
                    code = "M04A.T1.1",
                    level = 1,
                    title = "T1.1 Dari sinyal menjadi gambar",
                    summaryShort = "Gradient coil membuat frekuensi dan fase berbeda di tiap posisi. Tiga langkah (pilih irisan, kode fase, kode frekuensi) mengodekan posisi, dan sinyalnya disusun di ruang-k.",
                    detailFull = "Magnet utama (B0) membuat seluruh tubuh beresonansi pada frekuensi Larmor yang sama. Tanpa gradien spasial, sistem tidak dapat melokalisasi koordinat anatomi asal sinyal. Karena itu gradient coil menambahkan gradien medan linear yang berubah secara linear terhadap posisi. Di tempat berbeda, frekuensi dan fase berbeda. Dari sinilah posisi bisa dikodekan.\n\nAda tiga langkah, selalu berurutan:\n1. Pilih irisan: Gradien menyala saat pulsa RF dikirim, sehingga hanya satu irisan yang beresonansi.\n2. Kode fase: Gradien singkat memberi tiap baris voksel fase yang berbeda. Langkah ini diulang pada setiap TR.\n3. Kode frekuensi: Gradien menyala saat sinyal dibaca, sehingga tiap kolom voksel berfrekuensi berbeda.\n\nSemua sinyal yang terbaca disusun di sebuah tabel yang disebut ruang-k. Ruang-k bukan gambar. Gambar baru muncul setelah ruang-k diolah.",
                    analogi = "Analogi: Bayangkan gedung bertingkat: pilih lantainya (irisan), lalu beri tiap ruangan nomor baris (fase) dan nomor kolom (frekuensi).",
                    labType = "m4a_gradien_larmor"
                ),
                MriNode(
                    code = "M04A.T1.2",
                    level = 1,
                    title = "T1.2 Mekanisme gradien: gradient coil, polaritas, dan isosenter",
                    summaryShort = "Gradient coil membuat medan yang berubah linear di sepanjang sumbunya dan menumpang pada B0. Arah arus menentukan polaritas, besar arus menentukan kemiringan (amplitudo), dan di isosenter kuat medan tetap B0.",
                    detailFull = "Mengapa perlu pengodean spasial? Pulsa RF eksitasi membuat magnetisasi transversal yang koheren dan berpresesi pada frekuensi Larmor, sehingga menginduksi sinyal di RF coil. Untuk membentuk citra, sistem harus menghitung seberapa banyak sinyal datang dari tiap lokasi tiga dimensi (voksel). Cara paling sederhana: tentukan irisan terlebih dahulu, lalu tentukan sinyal pada tiap lokasi dua dimensi di dalamnya (piksel).\n\nGradient coil: Agar sinyal dapat dilokalisasi, B0 dibuat miring secara linear sehingga dapat diprediksi. Medan miring itu dibangkitkan oleh elektromagnet silindris di dalam bore, yaitu gradient coil.\n\nPolaritas: Arah arus lewat terminal-terminal menentukan apakah medan induksi menambah atau mengurangi B0 di tiap sisi. Titik tengah sumbu gradien tetap bernilai B0 walaupun gradien menyala, disebut isosenter magnetik.\n\nRumus: Bp = B0 + G · p\ndengan Bp kuat medan di posisi p (T), G amplitudo gradien (mT/m), dan p jarak dari isosenter.\n\nFase ikut berubah: Spin dengan frekuensi lebih tinggi memperoleh fase relatif terhadap spin lain, dan spin dengan frekuensi lebih rendah kehilangan fase. Makin jauh dari isosenter, makin besar selisih fasenya.",
                    analogi = "Analogi: Spin adalah jam tangan. Tanpa gradien, semua jam berdetak sama. Saat gradien menyala, jam di sisi medan tinggi berdetak lebih cepat dan menunjukkan waktu yang mendahului jam di isosenter (maju fase). Jam di sisi medan rendah berdetak lebih lambat dan tertinggal (mundur fase).",
                    labType = "m4a_gradien_larmor",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04A"
                ),
                MriNode(
                    code = "M04A.T1.3",
                    level = 1,
                    title = "T1.3 Sumbu gradien dan lobus pada diagram urutan pulsa",
                    summaryShort = "Ada tiga gradient coil yang dinamai menurut sumbunya (z, y, x). Pada diagram urutan pulsa, gradien digambar sebagai lobus: luas menunjukkan amplitudo dan durasi, posisi di atas atau bawah garis menunjukkan polaritas.",
                    detailFull = "Tiga sumbu: Tiga gradient coil berada di dalam bore dan dinamai menurut sumbu kerjanya:\n• Gradien z mengubah kuat medan sepanjang sumbu z (panjang), dari kepala ke kaki pasien.\n• Gradien y mengubah kuat medan sepanjang sumbu y (vertikal), dari punggung ke depan pasien.\n• Gradien x mengubah kuat medan sepanjang sumbu x (horizontal), dari kanan ke kiri pasien.\n\nLobus: Pada diagram urutan pulsa, gradien digambar sebagai bentuk di atas dan di bawah garis tengah yang disebut lobus. Luas di dalam lobus menunjukkan amplitudo dan/atau durasi: luas besar berarti gradien curam atau lama, luas kecil berarti gradien landai atau singkat. Lobus di atas garis berarti polaritas positif, lobus di bawah garis berarti polaritas negatif.",
                    analogi = "Analogi: Seperti pedal gas pada grafik: lobus tinggi berarti injakan dalam, lobus lebar berarti injakan lama, dan arah atas atau bawah menunjukkan maju atau mundur.",
                    labType = "m4a_satu_tr_tiga",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04A"
                ),
                MriNode(
                    code = "M04A.T1.4",
                    level = 1,
                    title = "T1.4 Pemilihan irisan: cara kerja, bidang, dan waktu",
                    summaryShort = "Irisan dipilih dengan menyalakan gradien bersamaan dengan pulsa RF. Hanya spin di irisan yang frekuensi presesinya sama dengan frekuensi RF yang beresonansi. Bidang pindai menentukan gradien mana (x, y, atau z) yang bertugas memilih irisan.",
                    detailFull = "Tiga tugas pengodean spasial: (1) pemilihan irisan, yaitu menemukan irisan dalam bidang pindai yang dipilih, (2) pengodean frekuensi sepanjang sumbu panjang irisan, dan (3) pengodean fase sepanjang sumbu pendek irisan.\n\nCara kerja: Saat gradien menyala, tiap posisi memiliki frekuensi tertentu (Bp = B0 + G·p). Irisan dieksitasi dengan pulsa RF yang berosilasi pada frekuensi sama dengan frekuensi presesi spin di irisan itu sehingga terjadi resonansi. Dalam praktik, posisi irisan diubah dengan menggeser frekuensi tengah pulsa RF.\n\nBidang menentukan gradien: Pada konvensi umum, gradien z memilih irisan aksial (kepala ke kaki), gradien x memilih irisan sagital (kiri ke kanan), dan gradien y memilih irisan koronal (belakang ke depan). Irisan oblik dipilih dengan menggabungkan dua atau tiga gradien.\n\nKapan pemilihan irisan terjadi? Pada urutan spin-echo, gradien pemilih irisan menyala selama pulsa eksitasi 90° dan pulsa rephasing 180°. Pada gradient-echo, gradien ini menyala hanya saat pulsa eksitasi.",
                    analogi = "Analogi: Garpu tala. Bayangkan deretan garpu tala yang masing-masing disetel ke nada berbeda diletakkan sepanjang gradien. Pulsa RF seperti sumber bunyi satu nada: hanya garpu tala bernada sama yang bergetar (resonansi).",
                    labType = "m4a_pemilihan_irisan",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04A"
                ),
                MriNode(
                    code = "M04A.T1.5",
                    level = 1,
                    title = "T1.5 Tebal irisan, transmit bandwidth, dan celah",
                    summaryShort = "Tebal irisan sama dengan transmit bandwidth dibagi (γ × amplitudo gradien). Irisan tipis: gradien curam dan/atau TBW sempit. Irisan tebal: gradien landai dan/atau TBW lebar.",
                    detailFull = "Pita frekuensi: Agar irisan punya ketebalan, pulsa RF mengeksitasi satu 'pita' inti yang disebut transmit bandwidth (TBW).\n\nRumus: Tebal irisan = TBW / (γ · G)\ndengan γ rasio giromagnetik (MHz/T) dan G amplitudo gradien (mT/m). Jadi tebal irisan diatur lewat amplitudo gradien, TBW, atau keduanya.\n\nPilihan TBW: TBW rendah memberi profil irisan yang baik dan berguna untuk menurunkan SAR, tetapi citra lebih rentan terhadap distorsi dan TE minimum naik. TBW tinggi berefek sebaliknya dan kadang dipakai pada urutan gradient-echo cepat untuk memendekkan TE.\n\nHal yang perlu diingat:\n• Irisan tipis membutuhkan gradien curam dan TBW sempit.\n• Irisan tebal membutuhkan gradien landai dan TBW lebar.",
                    analogi = "Analogi: Tangga dengan anak tangga bernada berbeda. Gradien curam membuat nada antar anak tangga berjauhan. Pita RF yang sempit hanya membunyikan satu atau dua anak tangga (irisan tipis), sedangkan pita yang lebar membunyikan banyak anak tangga (irisan tebal).",
                    labType = "m4a_pemilihan_irisan",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04A"
                ),
                MriNode(
                    code = "M04A.T1.6",
                    level = 1,
                    title = "T1.6 Pengodean frekuensi: gradien baca dan FOV",
                    summaryShort = "Setelah irisan dipilih, gradien frekuensi menyala saat gema dibaca. Frekuensi spin kini berbeda menurut posisinya sepanjang sumbu gradien, sehingga sinyal dapat ditempatkan. Gradien yang lebih curam memberi FOV lebih kecil pada arah frekuensi.",
                    detailFull = "Cara kerja: Sinyal biasanya dilokalisasi di sepanjang sumbu panjang anatomi lewat pengodean frekuensi. Saat gradien frekuensi menyala, inti di ujung medan tinggi berpresesi lebih cepat daripada inti di ujung medan rendah, sedangkan di isosenter frekuensinya tetap.\n\nArah pengodean frekuensi:\n• Koronal dan sagital: sumbu panjang searah z (kepala ke kaki), sehingga gradien z melakukan pengodean frekuensi.\n• Aksial tubuh: sumbu panjang horizontal (kiri ke kanan), sehingga gradien x yang dipakai.\n• Aksial kepala: sumbu panjang depan-belakang, sehingga gradien y yang dipakai.\n\nKapan terjadi? Gradien frekuensi menyala saat sinyal diterima (saat gema muncul), sehingga disebut gradien baca (readout). Lama menyalanya disebut waktu sampling atau jendela sampling.\n\nRumus: FOV = RBW / (γ · G)\nFOV kecil membutuhkan gradien frekuensi curam, sedangkan FOV besar membutuhkan gradien landai.",
                    analogi = "Analogi: Papan tuts piano. Tiap tuts berbunyi pada nada (frekuensi) berbeda. Gradien frekuensi mengubah tubuh menjadi 'papan tuts': tiap posisi punya 'nada' sendiri, sehingga komputer mengenali posisi sinyal dari nadanya.",
                    labType = "m4a_pengodean_frekuensi",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04A"
                ),
                MriNode(
                    code = "M04A.T1.7",
                    level = 1,
                    title = "T1.7 Pengodean fase: gradien fase memberi \"jam tangan\" berbeda tiap baris",
                    summaryShort = "Gradien fase menyala singkat setelah eksitasi dan mati sebelum sinyal dibaca. Ia meninggalkan fase berbeda di tiap baris voksel, dan polaritas serta kemiringannya diubah di setiap TR sehingga tiap TR memberi satu \"kode\" baris.",
                    detailFull = "Langkah ketiga dari tiga gradien: Pengodean fase mengisi celah antara pemilihan irisan dan pengodean frekuensi: gradien pada sumbu pendek menyala singkat setelah eksitasi dan sudah mati ketika gema dibaca.\n\nCara kerja: Selama gradien fase menyala, spin di posisi berbeda berpresesi dengan frekuensi berbeda. Setelah gradien mati, semua spin kembali berpresesi dengan frekuensi yang sama, tetapi selisih fase yang sudah terbentuk tetap tersimpan. Hasilnya, tiap baris voksel membawa fase yang berbeda.\n\nSatu TR hanya memberi satu pola fase: Untuk membedakan banyak baris voksel, gradien fase harus diulang dengan polaritas dan kemiringan berbeda di setiap TR. Pada NEX 1, jumlah TR sama dengan matriks fase.",
                    analogi = "Analogi: Pelari yang dilepas dari garis start berbeda. Setelah semua berlari dengan kecepatan sama, jarak antar pelari tetap sama dengan jarak start awal. Selisih fase yang tertinggal itulah yang memberi tahu posisi baris.",
                    labType = "m4a_satu_tr_tiga",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04A"
                )
            )
        ),
        MriModule(
            id = "M04B",
            code = "M04B",
            title = "Gradien, Ruang-k, dan Fourier",
            subtitle = "Pecahan 2 dari 3 M04 Pembentukan Citra (Tingkat 2 Menengah)",
            description = "Menjelaskan apa itu ruang-k dan bagaimana gradien fase mengisinya, menghubungkan sampling, receive bandwidth, dan teorema Nyquist, serta menerapkan enam fakta penting ruang-k (FOV, piksel, waktu pindai).",
            levelBadge = "T2 Menengah",
            iconName = "chart",
            nodes = listOf(
                MriNode(
                    code = "M04B.T2.1",
                    level = 2,
                    title = "T2.1 Bagian 1: Apa itu ruang-k? Sumbu, baris pengodean fase (ky), dan penomoran",
                    summaryShort = "Ruang-k adalah tempat penyimpanan data satu irisan. Bentuknya persegi panjang dengan sumbu frekuensi (horizontal) dan sumbu fase (vertikal), dan diisi baris demi baris.",
                    detailFull = "Mengapa dinamai 'k'? Huruf k melambangkan frekuensi spasial, yaitu seberapa cepat fase berubah menurut jarak sepanjang gradien (satuan radian per meter atau per cm).\n\nDua sumbu: Sumbu frekuensi mendatar dan berpusat di tengah (data kode frekuensi). Sumbu fase tegak dan berpusat di tengah (data kode fase).\n\nEmpat hal yang harus diketahui sistem:\n1. Jumlah matriks ruang-k per irisan (slice), yaitu jumlah irisan yang dipilih.\n2. Jumlah baris pengodean fase (garis ky ruang-k), yaitu matriks fase.\n3. Nomor baris pengodean fase (garis ky), supaya tiap baris teridentifikasi dengan tepat.\n4. Cara pengisian trajektori ruang-k yang efisien.\n\nPenomoran garis: Nomor dimulai dari garis tengah (garis 0) yang selalu terisi, lalu bertambah ke arah tepi (±1, ±2, dst.). Separuh atas disebut garis positif (gradien fase positif) dan separuh bawah garis negatif (gradien fase negatif).",
                    analogi = "Analogi: Lemari laci. Laci adalah baris data, lemari adalah irisan, dan tumpukan pakaian adalah semua frekuensi spasial yang harus dipilah ke laci yang tepat.",
                    labType = "m4b_lemari_laci"
                ),
                MriNode(
                    code = "M04B.T2.2",
                    level = 2,
                    title = "T2.2 Bagian 1 (lanjutan): Gradien fase mengisi ruang-k",
                    summaryShort = "Gradien fase memilih baris pengodean fase (garis ky ruang-k) mana yang terisi pada tiap TR. Polaritas memilih separuh atas atau bawah, dan kemiringan memilih jarak dari pusat: curam ke tepi, landai ke pusat.",
                    detailFull = "Polaritas dan kemiringan: Gradien fase positif memilih garis di separuh atas ruang-k, dan gradien negatif memilih garis di separuh bawah. Gradien curam memilih garis terluar, sedangkan gradien landai memilih garis dekat pusat.\n\nJika gradien fase tidak pernah diubah, garis yang sama terisi pada setiap TR sehingga matriks fase bernilai satu. Karena itu polaritas dan kemiringan diubah di setiap TR.\n\nHal yang perlu diingat:\n• Ruang-k menyimpan informasi tentang di mana frekuensi di dalam irisan berada.\n• Titik data yang terkumpul selama pindai diubah oleh FFT menjadi informasi amplitudo.\n• Jumlah baris pengodean fase (garis ky ruang-k) sama dengan matriks fase. Jumlah titik sampel per baris sama dengan matriks frekuensi.",
                    analogi = "Analogi: Seperti tombol lift. Arah (atas atau bawah) adalah polaritas, dan seberapa jauh lift bergerak dari lantai tengah adalah kemiringan. Tiap TR, satu laci yang sama dibuka di semua lemari.",
                    labType = "m4b_lemari_laci",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.3",
                    level = 2,
                    title = "T2.3 Gradien fase: mengapa harus berubah tiap TR?",
                    summaryShort = "Hanya gradien fase yang boleh berubah dari TR ke TR tanpa efek samping. Perubahan itu membuat data tampak berbeda, sehingga masuk ke garis ruang-k yang berbeda.",
                    detailFull = "Mengapa gradien fase harus berubah? Agar data tampak berbeda dari TR sebelumnya, sehingga sistem tahu harus meletakkannya di garis ruang-k yang baru.\n\nMengapa gradien lain tidak boleh berubah? Jika kemiringan gradien frekuensi berubah tiap TR, ukuran piksel dan FOV pada arah frekuensi ikut berubah. Jika kemiringan gradien pemilih irisan berubah tiap TR, tebal irisan berubah. Hanya kemiringan gradien fase yang dapat diubah tanpa akibat buruk.\n\nHal yang perlu diingat tentang data fase:\n• Tiap voksel mempunyai pseudo-frekuensi sendiri yang diperoleh dengan mengamati perubahan fase selama seluruh pemindaian.\n• Tiap titik data memuat informasi pengodean spasial seluruh irisan, dan FFT diperlukan untuk membukanya.",
                    analogi = "Analogi: Seperti memotret benda yang sama dari sudut berbeda pada setiap pengambilan. Jika sudutnya selalu sama, semua foto identik dan tidak ada informasi baru.",
                    labType = "m4c_pseudo_frekuensi",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.4",
                    level = 2,
                    title = "T2.4 Bagian 2: Dari gema ke titik data",
                    summaryShort = "Gema memuat banyak frekuensi dengan amplitudo berbeda. Gema disederhanakan menjadi satu modulasi, didigitalkan (ADC) selama jendela sampling, dan tiap sampel menjadi satu titik data di ruang-k.",
                    detailFull = "Gema adalah banyak gelombang: Gema yang diterima RF coil pada waktu TE memuat ratusan frekuensi dengan amplitudo berbeda yang digabung menjadi satu gelombang modulasi. Puncak gema biasanya muncul di tengah masa gradien frekuensi menyala.\n\nADC & Jendela sampling: Konversi analog ke digital (ADC) mengubah gelombang kontinu menjadi deretan angka selama gradien frekuensi menyala (jendela sampling). Tiap sampel disimpan sebagai satu titik data berurutan dari kiri ke kanan dalam satu garis ruang-k.\n\nDi balik layar protokol: Jumlah irisan menentukan jumlah matriks ruang-k per irisan (slice). Matriks fase menentukan jumlah baris pengodean fase (garis ky ruang-k). Matriks frekuensi menentukan titik sampel pengodean frekuensi (kolom kx).",
                    analogi = "Analogi: ADC mengubah 'jam analog' menjadi 'jam digital'. Titik data juga mirip foto: tiap foto mencatat keadaan pada satu saat, seperti bingkai pada video.",
                    labType = "m4b_sampling_nyquist",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.5",
                    level = 2,
                    title = "T2.5 Sampling dan teorema Nyquist",
                    summaryShort = "Sampling yang terlalu jarang membuat frekuensi salah terbaca (aliasing). Frekuensi tertinggi harus disampel minimal dua kali per siklus.",
                    detailFull = "Rumus: ω_sampling = 1 / ΔTs\nFrekuensi sampling digital adalah jumlah titik data yang diambil per detik selama jendela sampling (Hz), dan selang waktu antartitik disebut interval sampling (ΔTs).\n\nTeorema Nyquist: Frekuensi tertinggi dalam modulasi harus disampel setidaknya dua kali per siklus. Sekali per siklus membuat gelombang tampak sebagai garis lurus sehingga frekuensinya hilang. Kurang dari dua kali per siklus menghasilkan frekuensi yang salah (aliasing). Frekuensi sampling biasanya dijaga sekitar dua kali frekuensi tertinggi.",
                    analogi = "Analogi: Kamera video memotret kipas yang berputar cepat. Jika bingkai terlalu jarang, kipas tampak berputar lambat atau bahkan mundur. Itulah aliasing.",
                    labType = "m4b_sampling_nyquist",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.6",
                    level = 2,
                    title = "T2.6 Receive bandwidth (RBW) dan frekuensi Nyquist",
                    summaryShort = "Frekuensi sampling dijaga dua kali frekuensi Nyquist, yaitu frekuensi tertinggi pada gema. Parameter yang dipilih di protokol adalah receive bandwidth (RBW), yang bernilai sama dengan frekuensi sampling digital.",
                    detailFull = "Rumus: ω_sampling = 2 × ω_Nyquist = RBW\nFrekuensi Nyquist adalah frekuensi tertinggi pada gema yang masih dapat disampel dengan benar.\n\nTransmit vs receive bandwidth: Transmit bandwidth adalah rentang frekuensi pulsa RF eksitasi (menentukan tebal irisan). Receive bandwidth (RBW) adalah rentang frekuensi yang didigitalkan dengan akurat selama jendela sampling (dari -RBW/2 sampai +RBW/2 di sekitar frekuensi pembawa Larmor).\n\nSaat RBW dipilih, RBW lebar berarti gema sering disampel, titik data terkumpul cepat, dan jendela sampling pendek.",
                    analogi = "Analogi: Sprinter dan kamera. Tiap foto adalah satu titik data. Jumlah foto sama dengan matriks frekuensi, panjang lomba sama dengan jendela sampling, dan jumlah foto per detik sama dengan frekuensi sampling (RBW).",
                    labType = "m4b_receive_bw",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.7",
                    level = 2,
                    title = "T2.7 Jendela sampling, TE, dan matriks frekuensi",
                    summaryShort = "Jendela sampling sama dengan jumlah titik data dikali interval sampling. Puncak gema berada di tengahnya, jadi jendela yang lebih panjang menaikkan TE minimum.",
                    detailFull = "Rumus: RBW = 1 / ΔTs  |  Jendela sampling = N × ΔTs\ndengan N adalah matriks frekuensi. Gradien frekuensi menyala selama jendela itu.\n\nMengapa TE ikut terpengaruh: Puncak gema berada di tengah jendela, sehingga jendela yang lebih lama membuat puncak gema muncul lebih lambat dan TE minimum naik.\n\nDua cara memperpanjang jendela: menurunkan RBW (RBW setengahnya membuat jendela dua kali lebih panjang), atau menaikkan matriks frekuensi (N dua kali membuat jendela dua kali lebih panjang). Keduanya menaikkan TE minimum.",
                    analogi = "Analogi: Kamera sprinter hanya sanggup separuh foto per detik. Untuk tetap mendapat jumlah foto yang sama, lomba harus dua kali lebih panjang, sehingga puncak gema lebih lambat dan TE lebih besar.",
                    labType = "m4b_receive_bw",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.8",
                    level = 2,
                    title = "T2.8 TE minimum, tampilan RBW, dan jumlah irisan",
                    summaryShort = "Mengubah RBW atau matriks frekuensi mengubah jendela sampling, dan itu menggeser TE minimum. Pabrikan juga menampilkan RBW dengan cara berbeda, dan perubahan jendela dapat memengaruhi jumlah irisan serta waktu pindai.",
                    detailFull = "Rumus: Ws = ΔTs × M(f)\ndengan M(f) matriks frekuensi. Karena puncak gema berada di tengah jendela, TE minimum kira-kira sama dengan waktu tetap sebelum jendela ditambah separuh jendela.\n\nCara pabrikan menampilkan RBW:\n1. RBW untuk seluruh FOV, biasanya dalam kHz.\n2. RBW per piksel (RBW seluruh FOV dibagi matriks frekuensi), dalam Hz per piksel.\n3. Pergeseran lemak-air dalam piksel (chemical shift).\n\nJumlah irisan dan TE: Jika jendela bertambah panjang dan TE naik, sistem harus menunggu lebih lama di tiap irisan sehingga jumlah irisan maksimum dalam TR yang sama berkurang.",
                    analogi = "Analogi: Seperti dokter yang menunggu satu pasien selesai menjalani pemeriksaan sebelum memanggil pasien berikutnya. Pemeriksaan yang lebih lama berarti lebih sedikit pasien yang tertangani dalam satu sesi.",
                    labType = "m4b_receive_bw",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.9",
                    level = 2,
                    title = "T2.9 FFT: dari titik data ke citra",
                    summaryShort = "FFT mengubah titik data (amplitudo terhadap waktu) menjadi amplitudo terhadap frekuensi. Tiap piksel mendapat tingkat abu-abu sesuai amplitudo frekuensi yang berasal dari lokasinya.",
                    detailFull = "Dari ruang-k ke piksel: Jumlah garis terisi (matriks fase) dan jumlah titik data per garis (matriks frekuensi) menentukan ukuran matriks citra. Setelah FFT, tiap piksel diberi warna pada skala abu-abu sesuai amplitudo frekuensi yang berasal dari lokasi piksel itu.\n\nMengapa perlu FFT: Setiap titik data memuat informasi fase dan frekuensi dari seluruh irisan di domain waktu. Karena gradien menentukan lokasi sinyal dari frekuensinya, FFT memindahkan data ke domain frekuensi. Gelombang dengan satu frekuensi menjadi satu puncak, dan dua frekuensi menjadi dua puncak.",
                    analogi = "Analogi: FFT seperti penyetel radio. Gelombang campuran dipisah menjadi stasiun-stasiun, dan tinggi tiap puncak menunjukkan kerasnya stasiun itu.",
                    labType = "m4b_fft_gelombang",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.10.1",
                    level = 2,
                    title = "T2.10.1 Fakta 1-3: tiga fakta penting tentang ruang-k",
                    summaryShort = "Ruang-k bukan citra. Datanya simetris. Garis tengah membawa sinyal dan kontras, sedangkan garis tepi membawa resolusi.",
                    detailFull = "Fakta 1 — Ruang-k bukan citra: Satu titik data bukan satu piksel. Setiap titik data memuat informasi seluruh irisan, karena itu FFT diperlukan untuk membuka tiap titik data.\n\nFakta 2 — Data ruang-k simetris: Data di separuh atas berpasangan dengan separuh bawah dan sisi kiri dengan sisi kanan (simetri konjugat). Tiap titik data berpasangan dengan titik yang berseberangan secara diagonal.\n\nFakta 3 — Garis tengah membawa sinyal dan kontras, garis tepi membawa resolusi:\n• Bagian tengah ruang-k (gradien fase landai): sinyal tinggi, resolusi rendah.\n• Bagian tepi ruang-k (gradien fase curam): sinyal rendah, resolusi tinggi.",
                    analogi = "Analogi: Satu nada bukan satu bagian lagu (Fakta 1). Separuh wajah di cermin sama dengan separuh lainnya (Fakta 2). Suara utama membuat lagu terdengar, sedangkan nada tinggi membuatnya jernih (Fakta 3).",
                    labType = "m4b_sinyal_resolusi",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.10.2",
                    level = 2,
                    title = "T2.10.2 Ruang-k, resolusi, dan sinyal: tiga citra pelajaran",
                    summaryShort = "Citra dari seluruh ruang-k memuat sinyal dan resolusi. Dari tepi ruang-k saja, detail jelas tetapi sinyal sangat sedikit. Dari pusat saja, sinyal baik tetapi resolusi buruk, dan seluruh objek tetap tampak.",
                    detailFull = "Bandingkan tiga citra dari satu objek:\n1. Seluruh ruang-k dipakai: sinyal dan resolusi sama-sama tampak.\n2. Hanya data di tepi ruang-k (data resolusi): detail tepi tampak jelas, tetapi sinyalnya sangat sedikit.\n3. Hanya data di pusat ruang-k (data sinyal): sinyalnya sangat baik, tetapi resolusinya rendah akibat kehilangan frekuensi spasial tinggi (blurring spasial).\n\nKedua citra tetap menampilkan seluruh objek walaupun hanya sebagian titik data yang dipakai, karena tiap titik data memuat informasi dari seluruh irisan.",
                    analogi = "Analogi: Citra kontras murni (resolusi rendah) versus kontur tepi spasial tinggi. Foto buram (pusat) tetap memperlihatkan seluruh wajah dengan terang. Sketsa garis (tepi) tajam, tetapi gelap dan miskin isi.",
                    labType = "m4b_lab_terpadu",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.10.3",
                    level = 2,
                    title = "T2.10.3 Fakta 4: Waktu pindai adalah waktu mengisi ruang-k",
                    summaryShort = "Waktu pindai dihitung dari TR × matriks fase × NSA. Pada TSE dibagi ETL, dan pada 3D dikali jumlah lokasi irisan. Intinya, waktu pindai adalah waktu yang dibutuhkan untuk mengisi seluruh ruang-k.",
                    detailFull = "Rumus: ST (2D) = TR × M(p) × NSA  |  ST (TSE) = TR × M(p) × NSA / ETL  |  ST (3D) = TR × M(p) × NSA × Ns\n\nPada setiap TR, satu garis data terisi untuk setiap irisan. Matriks fase menentukan jumlah garis ruang-k yang terisi, sedangkan NSA (NEX) adalah berapa kali tiap garis diisi dengan data. Jumlah titik data pada tiap garis (matriks frekuensi) tidak menambah waktu pindai.",
                    analogi = "Analogi: TR adalah jeda antara mengisi laci teratas lemari pertama dan laci berikutnya pada lemari yang sama. Matriks fase adalah jumlah laci tiap lemari, dan NSA adalah berapa kali tiap laci diisi.",
                    labType = "m4b_kspace_detil",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.10.4",
                    level = 2,
                    title = "T2.10.4 Fakta 5: Jarak antar titik menentukan FOV",
                    summaryShort = "Jarak antar garis ruang-k menentukan FOV fase, dan jarak antar titik data pada satu baris (interval sampling) menentukan FOV frekuensi. Hubungannya berbanding terbalik.",
                    detailFull = "Rumus: FOV = 1 / Δk  |  FOV(f) = RBW / (γ · G)\n\nArah fase: Selisih pergeseran fase (langkah inkremental Δk) antar titik pada tiap kolom berbanding terbalik dengan FOV fase. Jika langkah antar garis menjadi dua kali, FOV fase menjadi setengah.\n\nArah frekuensi: Interval sampling antar titik pada tiap baris berbanding terbalik dengan FOV frekuensi. FOV frekuensi kecil diperoleh dengan menaikkan amplitudo gradien frekuensi atau menurunkan RBW.",
                    analogi = "Analogi: Laci dan sweter. Sweter tebal membutuhkan laci yang dalam (langkah antar garis besar → FOV kecil), sedangkan benda tipis cukup dengan laci dangkal (FOV besar).",
                    labType = "m4b_fov_matriks",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                ),
                MriNode(
                    code = "M04B.T2.10.5",
                    level = 2,
                    title = "T2.10.5 Fakta 6: Dimensi ruang-k menentukan ukuran piksel",
                    summaryShort = "Ukuran piksel sama dengan FOV dibagi matriks pada tiap arah. Resolusi naik jika FOV tetap dan matriks naik, atau matriks tetap dan FOV turun.",
                    detailFull = "Rumus: Ukuran piksel = FOV / Matriks\n\nResolusi spasial adalah ukuran tiap piksel, yaitu seberapa baik dua titik yang berdekatan pada pasien dapat dibedakan.\n• Ukuran piksel arah fase ditentukan amplitudo gradien fase paling curam (rentang ky maksimum).\n• Ukuran piksel arah frekuensi ditentukan jendela sampling dan laju sampling (rentang kx maksimum).\n• Di citra, FOV = ukuran piksel × jumlah piksel. Di ruang-k, FOV = dimensi ruang-k / jarak antar titik data.",
                    analogi = "Analogi: Bingkai kayu berisi laci. Tinggi bingkai dan jumlah laci menentukan ketebalan tiap laci (piksel arah fase). Lebar laci dan jumlah kaus kaki menentukan ukuran tiap kaus kaki (piksel arah frekuensi).",
                    labType = "m4b_fov_voksel",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04B"
                )
            )
        ),
        MriModule(
            id = "M04C",
            code = "M04C",
            title = "Ruang-k Lanjut & Strategi Akuisisi",
            subtitle = "Pecahan 3 dari 3 M04 Pembentukan Citra (Tingkat 3 Lanjut)",
            description = "Menjelaskan pseudo-frekuensi, simetri konjugat ruang-k dan dampaknya pada partial Fourier, membandingkan jalur ruang-k (kartesian, EPI, spiral, propeller), serta menilai kompromi SNR dan waktu.",
            levelBadge = "T3 Lanjut",
            iconName = "chart",
            nodes = listOf(
                MriNode(
                    code = "M04C.T3.1",
                    level = 3,
                    title = "T3.1 Pseudo-frekuensi",
                    summaryShort = "Pada satu voksel, titik tengah tiap garis ruang-k berubah dari TR ke TR. Rangkaian perubahan itu membentuk gelombang baru yang disebut pseudo-frekuensi, dan FFT memakainya untuk menemukan posisi pada sumbu fase.",
                    detailFull = "Contoh spin di tepi FOV: Spin di tepi FOV mengalami variasi besar pada kemiringan gradien fase sepanjang pemindaian. Pada sebagian TR gradiennya sangat curam, pada TR lain sangat landai.\n\nTitik merah: Jika titik tengah tiap garis dihubungkan dari TR pertama sampai terakhir, muncul gelombang lain yang disebut pseudo-frekuensi. Gelombang ini mewakili perubahan fase spin di lokasi itu sepanjang seluruh pemindaian. Lokasi lain di irisan mengalami pola perubahan kemiringan yang berbeda sehingga pseudo-frekuensinya berbeda, dan FFT memakainya untuk membedakan posisi sepanjang sumbu fase.",
                    analogi = "Analogi: Seperti lampu yang berkedip dengan irama berbeda di tiap rumah. Dari variasi periodisitas kedipnya, operator dapat mengidentifikasi koordinat spasial tiap titik.",
                    labType = "m4c_pseudo_frekuensi"
                ),
                MriNode(
                    code = "M04C.T3.2",
                    level = 3,
                    title = "T3.2 Memahami ruang-k lebih dalam",
                    summaryShort = "Ruang-k adalah tabel data mentah. Satu titik berarti satu pola garis. Pusat memberi kontras, tepi memberi detail.",
                    detailFull = "Apa itu ruang-k? Ruang-k adalah tabel data mentah yang direkam pemindai. Setiap sel berisi bilangan kompleks (besar sinyal dan fasenya).\n\nSatu titik berarti satu pola garis: Titik (kx, ky) menyatakan seberapa banyak pola garis bergelombang tertentu ada di dalam citra (kx siklus horizontal, ky siklus vertikal).\n• Pusat berisi pola garis lebar dan landai (menentukan kecerahan dan kontras).\n• Tepi berisi pola garis yang sangat rapat (menentukan detail halus dan ketajaman tepi).\n\nSalah kaprah yang umum: Titik kiri atas ruang-k bukan bagian kiri atas tubuh. Setiap titik membawa informasi dari seluruh citra.",
                    analogi = "Analogi: Citra seperti lagu dan ruang-k seperti daftar nadanya. Nada rendah memberi dasar, nada tinggi memberi kerincian.",
                    labType = "m4b_simulator_akhir",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                ),
                MriNode(
                    code = "M04C.T3.3",
                    level = 3,
                    title = "T3.3 Simetri titik",
                    summaryShort = "Titik di seberang pusat ruang-k saling berpasangan. Ini dasar partial Fourier.",
                    detailFull = "Untuk objek nyata, titik di sisi berseberangan dari pusat berpasangan secara diagonal (simetri konjugat: besar sinyal sama, bagian imajiner berlawanan tanda). Karena itu sebagian ruang-k secara teori cukup diisi, dan sisanya diperkirakan. Inilah gagasan di balik partial Fourier.",
                    analogi = "Analogi: Seperti cermin: separuh wajah cukup untuk memperkirakan separuh lainnya.",
                    labType = "m4c_simetri_konjugat",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                ),
                MriNode(
                    code = "M04C.T3.4",
                    level = 3,
                    title = "T3.4 Mengatur garis ruang-k",
                    summaryShort = "Urutan dan proporsi garis tengah dibanding garis tepi memengaruhi pembobotan dan kualitas citra. Mengurangi matriks fase biasanya membuang garis tepi dan mempertahankan garis tengah.",
                    detailFull = "Mengatur ulang garis (misalnya pada TSE): Garis tengah yang memberi sinyal dan kontras diisi pada gema ber-TE mirip TE efektif yang dipilih di protokol, sehingga pengaruhnya pada pembobotan citra maksimal. Garis tepi (resolusi) diisi pada gema yang TE-nya berbeda dari TE efektif.\n\nMengurangi matriks fase: Bila matriks fase dikurangi, garis tepi dibuang dan hanya garis di sekitar tengah yang diisi, mempertahankan sinyal dan kontras.",
                    analogi = "Analogi: Seperti pembicara yang mengutamakan isi pidato (garis tengah) dan baru kemudian memoles ucapannya (garis tepi).",
                    labType = "m4c_pilihan_pengisian",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                ),
                MriNode(
                    code = "M04C.T3.5",
                    level = 3,
                    title = "T3.5 Tarian ruang-k, cara urutan pulsa menelusuri ruang-k",
                    summaryShort = "Kombinasi pulsa RF serta polaritas dan amplitudo gradien frekuensi dan fase menentukan jalur penelusuran ruang-k. Eksitasi RF memusatkan titik awal, gradien fase memilih garis atas atau bawah, dan gradien frekuensi menentukan arah kiri-kanan.",
                    detailFull = "Aturan tarian ruang-k:\n• Pulsa RF eksitasi memusatkan sistem ke tengah ruang-k.\n• Amplitudo gradien fase menentukan seberapa jauh ke atas atau ke bawah suatu garis terisi (menentukan ukuran piksel arah fase).\n• Polaritas gradien fase menentukan separuh atas (positif) atau separuh bawah (negatif).\n• Polaritas gradien frekuensi menentukan arah penelusuran: positif dari kiri ke kanan, negatif dari kanan ke kiri.\n• Pada spin-echo, pulsa rephasing 180° membalik titik ke titik cermin di sisi berlawanan ruang-k (dari +N ke -N).",
                    analogi = "Analogi: Lantai dansa. Trajektori lintasan ruang-k analog dengan gerakan penari di lantai dansa: pulsa RF memposisikan titik awal di pusat koordinat, gradien pengode fase mengarahkan langkah ke sumbu ky (atas/bawah), dan gradien pengode frekuensi mengarahkan sapuan sepanjang sumbu kx (kiri/kanan).",
                    labType = "m4c_tarian_kspace",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                ),
                MriNode(
                    code = "M04C.T3.6",
                    level = 3,
                    title = "T3.6 Pilihan pengisian (1): partial Fourier, partial echo, parallel imaging",
                    summaryShort = "Cara ruang-k diisi dapat diatur untuk mempersingkat waktu pindai, misalnya dengan memanfaatkan simetri ruang-k atau beberapa coil sekaligus.",
                    detailFull = "Partial (fractional) averaging atau half Fourier: Hanya sebagian garis ruang-k (lebih dari separuh) diukur sehingga langkah pengodean fase dan waktu pindai berkurang, tetapi SNR turun.\n\nPartial echo: Hanya puncak dan bagian dephasing gema yang disampel untuk mencapai TE sangat pendek (memaksimalkan pembobotan T1 dan PD).\n\nParallel imaging (sensitivity encoding): Mengisi beberapa garis per TR secara serentak menggunakan array coil multi-saluran dengan faktor reduksi R. Karena tiap saluran melompati tiap R-ke garis, jarak antar garis R kali lebih besar sehingga FOV fase mengecil menjadi 1/R dan terjadi lipatan (aliasing) yang kemudian dibuka menggunakan profil sensitivitas coil.",
                    analogi = "Analogi: Beberapa petugas mengisi laci berbeda sekaligus sehingga pekerjaan selesai lebih cepat, tetapi mereka harus teliti agar tidak ada yang tertukar atau terlipat.",
                    labType = "m4c_urutan_akuisisi",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                ),
                MriNode(
                    code = "M04C.T3.7",
                    level = 3,
                    title = "T3.7 Pilihan pengisian (2): single-shot, spiral, propeller, dan jenis akuisisi",
                    summaryShort = "Mengisi lebih dari satu garis per TR memangkas waktu pindai. Pada single-shot, seluruh ruang-k terisi dalam satu eksitasi. Data juga dapat diperoleh secara sequential, 2D volumetrik, atau 3D volumetrik.",
                    detailFull = "Single-shot (SS-FSE / EPI): Seluruh garis ruang-k diisi dari satu echo train dalam satu eksitasi RF. Gradien frekuensi berosilasi positif-negatif (kiri-kanan lalu kanan-kiri), sedangkan gradien fase melakukan blipping (berkedip turun satu garis tiap kali).\n\nPengisian spiral: Gradien frekuensi dan fase sama-sama berosilasi, menelusuri ruang-k dari pusat menyebar ke luar dalam satu tembakan.\n\nPropeller (radial): Data diperoleh dalam strip (bilah) yang berputar mengelilingi pusat ruang-k sehingga pusat diambil pada setiap TR, setara NSA ganda dan tahan artefak gerak.\n\nJenis akuisisi data:\n• Sequential: seluruh data irisan 1 diambil penuh, lalu irisan 2 (cocok untuk tahan napas).\n• 2D volumetrik: satu garis yang sama diisi untuk semua irisan dalam satu TR, lalu garis berikutnya (semua irisan selesai hampir bersamaan di akhir).\n• 3D volumetrik: seluruh volume (slab) tereksitasi lalu dibagi menjadi partisi tanpa celah lewat slice encoding.",
                    analogi = "Analogi: Menggambar dengan satu goresan pena tanpa pernah mengangkatnya: itulah single-shot. Pada akuisisi sequential, lemari 1 diisi penuh dulu baru lemari 2. Pada 2D volumetrik, laci teratas di semua lemari diisi dulu, lalu laci berikutnya.",
                    labType = "m4c_jalur_kspace",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                ),
                MriNode(
                    code = "M04C.T3.8",
                    level = 3,
                    title = "T3.8 SNR dan kompromi (rumah konsep: M09 T2.1 dan T2.3)",
                    summaryShort = "Kompromi antara SNR, resolusi, dan waktu pindai dibahas lengkap di M09. Di modul ini cukup diingat bahwa setiap strategi pengisian pada T3.6 dan T3.7 menukar waktu dengan SNR.",
                    detailFull = "Tautan ke M09: Rumus SNR, pengaruh NEX, ukuran voksel, dan bandwidth penerima ada di M09 T2.1 dan T2.3. Rumus waktu pindai dengan parallel imaging dan partial Fourier ada di M09 T3.1 dan T3.2.\n\nYang perlu dibawa dari M04C: Partial Fourier dan parallel imaging mempersingkat waktu dengan mengurangi data yang diukur, sehingga SNR turun. Propeller mengambil pusat ruang-k berulang kali sehingga lebih tahan gerak, tetapi waktu pindai lebih panjang.",
                    analogi = "Analogi: Seperti memotret di tempat gelap: ada harga untuk setiap cara menambah terang atau mempercepat bidikan. Perhitungan lengkapnya ada di M09.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul4/modul4-native.html?mod=M04C"
                )
            )
        ),
        MriModule(
            id = "M05",
            code = "M05",
            title = "Anatomi Pesawat & Sistem Magnet",
            subtitle = "Lima Lapisan Pesawat, Kriostat 4,2 K, Shimming, Perisai 5G & Quench",
            description = "Membedah anatomi lima lapisan pesawat MRI (potongan membujur vs aksial), tiga jenis magnet, kompromi kuat medan klinis, kriostat helium cair 4,2 K, konstruksi solenoid & bucking coil, ramping & saklar persisten, shimming homogenitas (ppm ke Hz), perisai medan tepi 5 Gauss, prosedur darurat Quench, efek kenaikan B0 terhadap SNR/SAR, serta peran Host Computer & PGM.",
            levelBadge = "T1 - T3 Lengkap",
            iconName = "magnet",
            nodes = listOf(
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M05.T1.1",
                    level = 1,
                    title = "T1.1 Lima lapisan pesawat MRI, dari luar ke dalam",
                    summaryShort = "Dari luar ke dalam: ruang berpelindung RF, magnet utama, gradient coil, RF coil, lalu bore dan meja pasien. Tiap lapisan punya satu tugas utama.",
                    detailFull = "Dari luar ke dalam, lima lapisan itu masing-masing punya satu pekerjaan, dan semuanya harus bekerja bersama agar sinyal lemah dari proton dapat dibaca.\n\n" +
                        "• Ruang berpelindung RF (sangkar Faraday, Zona IV): menahan gelombang radio dari luar.\n" +
                        "• Magnet utama (kriostat dan helium cair): menghasilkan B0 yang kuat, stabil, dan homogen.\n" +
                        "• Gradient coil (sumbu X, Y, Z): menambah medan yang berubah linear untuk mengode posisi (voksel).\n" +
                        "• RF coil (pemancar dan penerima): memancarkan pulsa RF dan mendeteksi sinyal.\n" +
                        "• Bore dan meja pasien: ruang periksa 60 sampai 70 cm (60 cm standar, 70 cm wide bore); meja menggeser pasien ke isosenter.\n\n" +
                        "Catatan istilah: ruang berpelindung RF sebenarnya ruangan, bukan bagian pesawat, dan bore serta meja bukan lapisan fisik tersendiri. Kata \"lapisan\" di sini hanya cara membaca urutan dari luar ke dalam.",
                    analogi = "Analogi: Bayangkan pesawat MRI sebagai bawang berlapis. Setiap lapisan punya satu pekerjaan, dan semuanya bekerja bersama agar sinyal lemah dari proton dapat dibaca.",
                    prerequisite = "M01",
                    learningGoal = "Menyebut lima lapisan pesawat MRI dari luar ke dalam beserta satu tugas utama tiap lapisan.",
                    crossLinks = "→ Gradient coil dan RF coil secara rinci: M06A dan M06B. Zona dan skrining pasien: M07A.",
                    labType = "m5_a1_penampang",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M05.T1.2",
                    level = 1,
                    title = "T1.2 Potongan membujur vs aksial",
                    summaryShort = "Potongan membujur sejajar bore; potongan aksial tegak lurus bore, sehingga lapisan tampak sebagai cincin konsentris.",
                    detailFull = "Pesawat dapat dipotong membujur (sejajar bore) atau aksial (tegak lurus bore, dilihat dari kaki ke kepala pasien).\n\n" +
                        "Perbandingan Potongan Membujur vs Potongan Aksial:\n" +
                        "• Arah irisan: Sejajar sumbu panjang bore (membujur) vs Tegak lurus sumbu panjang bore (aksial).\n" +
                        "• Lapisan tampak sebagai: Pita sejajar di atas dan di bawah bore (membujur) vs Cincin konsentris di sekeliling bore (aksial).\n" +
                        "• Paling jelas memperlihatkan: Panjang tiap lapisan, kriostat, meja dan pasien terlentang dari samping (membujur) vs Urutan lapisan dari luar ke dalam, bentuk melingkar RF coil dan gradient coil (aksial).\n" +
                        "• Arah B0: Sejajar bidang gambar, di sepanjang bore (membujur) vs Tegak lurus bidang gambar (aksial).",
                    analogi = "Analogi: Pesawat MRI seperti pipa berlapis. Jika dibelah memanjang, lapisannya tampak sebagai pita sejajar. Jika diiris melintang, lapisannya tampak sebagai cincin.",
                    prerequisite = "M05.T1.1",
                    learningGoal = "Membedakan tampilan lapisan pesawat pada potongan membujur dan potongan aksial.",
                    crossLinks = "Aktivitas terkait: A1 (Penjelajahan diagram penampang)",
                    labType = "m5_a1_penampang",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M05.T1.3",
                    level = 1,
                    title = "T1.3 Tiga jenis magnet MRI",
                    summaryShort = "Permanen (medan rendah, tanpa listrik dan kriogen), resistif (medan rendah sampai menengah, daya besar), dan superkonduktif (medan klinis 1,5 sampai 3 T, arus persisten).",
                    detailFull = "Pesawat klinis 1,5 T dan 3 T memakai magnet superkonduktif, karena hanya jenis ini yang memberi medan kuat dan sangat stabil tanpa daya besar yang terus mengalir.\n\n" +
                        "• Permanen: Medan rendah (maksimal sekitar 0,5 T) | Kriogen: Tidak | Tanpa listrik. Sangat berat. Selalu menyala, tidak bisa dimatikan saat darurat. Medan ikut berubah dengan suhu ruang.\n" +
                        "• Resistif: Medan rendah sampai menengah | Kriogen: Tidak (pendingin air atau udara) | Solenoid tembaga. Butuh daya besar (puluhan kW). Bisa dimatikan dengan aman.\n" +
                        "• Superkonduktif: Medan tinggi (1,5 sampai 3 T klinis) | Kriogen: Ya, helium cair | Kawat niobium-titanium. Arus persisten, sangat stabil, mahal.\n\n" +
                        "Magnet permanen memakai paduan feromagnetik (neodimium-boron-besi atau alnico) pada yoke, arah medannya vertikal. Magnet resistif mengikuti kaidah genggaman tangan kanan: arus melingkar menghasilkan medan sepanjang sumbu.",
                    analogi = "Analogi: Permanen seperti magnet kulkas raksasa: selalu menempel, tanpa listrik. Resistif seperti kompor listrik: medan ada selama arus dialirkan. Superkonduktif seperti roda gila yang terus berputar tanpa gesekan setelah didorong sekali.",
                    prerequisite = "M01",
                    learningGoal = "Membedakan magnet permanen, resistif, dan superkonduktif.",
                    crossLinks = "Aktivitas terkait: A6 (Pilih magnet untuk situasi)",
                    labType = "m5_a6_pilih_magnet",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M05.T1.4",
                    level = 1,
                    title = "T1.4 Kuat medan klinis dan kompromi pemakaiannya",
                    summaryShort = "Medan lebih tinggi memberi sinyal lebih besar, tetapi biaya, SAR, dan artefak ikut naik. Tidak ada medan yang paling baik untuk semua situasi.",
                    detailFull = "Perbandingan Rentang Kuat Medan Klinis:\n" +
                        "• Rendah (sampai sekitar 0,5 T) | Magnet: Permanen atau resistif | Untung: Biaya dan perawatan lebih rendah; desain bisa terbuka | Rugi: SNR lebih rendah, pencitraan lebih lama.\n" +
                        "• 1,5 T | Magnet: Superkonduktif | Untung: Paling umum; seimbang antara SNR, SAR, dan artefak | Rugi: Butuh helium dan ruang khusus.\n" +
                        "• 3 T | Magnet: Superkonduktif | Untung: SNR lebih tinggi; resolusi lebih baik, atau pemindaian lebih cepat | Rugi: SAR, artefak suseptibilitas, dan chemical shift lebih besar.\n" +
                        "• Di atas 3 T (sampai 7 T atau lebih) | Magnet: Superkonduktif | Untung: SNR dan resolusi sangat tinggi | Rugi: Terutama riset; kendala teknis dan keselamatan makin banyak.\n\n" +
                        "Pemilihan bergantung pada kebutuhan klinis, anggaran, ruang, dan kenyamanan pasien. ",
                    analogi = "Analogi: Seperti memilih mobil: makin kencang makin bertenaga, tetapi makin mahal, boros, dan tidak cocok untuk semua jalan.",
                    prerequisite = "M05.T1.3",
                    learningGoal = "Menyebut rentang kuat medan klinis dan satu untung-rugi tiap rentang.",
                    crossLinks = "→ Konsekuensi teknis kenaikan B0 (SNR, SAR, artefak): T3.1. | Aktivitas terkait: A6",
                    labType = "m5_a6_pilih_magnet",
                    sortOrder = 4
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M05.T2.1",
                    level = 2,
                    title = "T2.1 Kriostat dan helium cair: suhu 4,2 K agar kawat bebas hambatan",
                    summaryShort = "Kawat niobium-titanium direndam helium cair pada 4,2 K (sekitar -269 °C) di dalam kriostat, sehingga hambatannya nol.",
                    detailFull = "Kawat niobium-titanium bersifat superkonduktor (hambatan nol) di bawah suhu transisinya, sekitar 9 K. Helium cair mendidih pada 4,2 K (-268,95 °C), jadi kawat berada di bawah suhu transisi.\n\n" +
                        "Kriostat (dari bahasa Yunani: dingin dan stabil) mencegah panas dari komponen sekitar, terutama gradient coil, merambat ke helium. Dari luar ke dalam:\n" +
                        "• tangki baja yang divakumkan (mengurangi konveksi),\n" +
                        "• pelindung panas polyester beraluminium seperti selimut luar angkasa (mengurangi radiasi),\n" +
                        "• ruang kriogen aluminium berisi helium cair,\n" +
                        "• solenoid. Ruang di dalam silinder disebut warm bore.\n\n" +
                        "Unit pendingin (kepala dingin) menahan konduksi panas, dan sistem rekondensasi helium membuat pengisian ulang nyaris tidak diperlukan. Karena helium makin langka, riset mencari bahan superkonduktor lain yang bekerja pada suhu lebih tinggi (sekitar 20 K).",
                    analogi = "Analogi: Kriostat seperti termos besar. Vakum dan lapisan reflektif menahan panas, sehingga helium tetap dingin lama.",
                    prerequisite = "T1.3",
                    learningGoal = "Menjelaskan mengapa kawat magnet harus didinginkan dan bagaimana kriostat menjaganya.",
                    crossLinks = "Aktivitas terkait: A3 (Magnet superkonduktif)",
                    labType = "m5_a3_superkonduktif",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M05.T2.2",
                    level = 2,
                    title = "T2.2 Konstruksi solenoid: segmen utama dan bucking coil",
                    summaryShort = "Medan utama dibuat beberapa segmen solenoid pada satu bobbin; bucking coil adalah kumparan tambahan berarus berlawanan untuk menyempitkan medan tepi.",
                    detailFull = "Satu solenoid panjang yang seragam tidak cukup homogen untuk bola pencitraan 40 cm. Karena itu lilitan dibagi menjadi segmen pada satu bobbin:\n" +
                        "• dua solenoid besar di kedua ujung (menghasilkan sebagian besar medan),\n" +
                        "• empat solenoid kecil di antaranya (memperluas volume yang homogen).\n\n" +
                        "Kawatnya berupa untaian niobium-titanium dalam matriks tembaga. Pada 4 K arus memilih jalur paduan; tembaga menjadi pelindung bila terjadi quench karena membantu menyebarkan beban panas. Arus bertahan tanpa sumber luar, tetapi ini bukan energi gratis: helium harus tetap dingin.\n\n" +
                        "Bucking coil adalah kumparan tambahan berdiameter lebih besar di kedua ujung, dengan arus berlawanan arah terhadap kumparan utama. Fungsinya mengurangi medan tepi (lihat T2.5).",
                    analogi = "Analogi: Beberapa segmen seperti beberapa lampu yang disusun untuk menerangi ruangan lebih rata daripada satu lampu panjang. Bucking coil seperti tirai yang mencegah cahaya bocor keluar jendela.",
                    prerequisite = "T2.1",
                    learningGoal = "Menjelaskan mengapa solenoid dibagi menjadi beberapa segmen dan apa fungsi bucking coil.",
                    crossLinks = "→ Bucking coil sebagai perisai aktif: T2.5. | Aktivitas terkait: A3",
                    labType = "m5_a3_superkonduktif",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M05.T2.3",
                    level = 2,
                    title = "T2.3 Ramping (menaikkan arus awal) dan saklar persisten",
                    summaryShort = "Arus dinaikkan bertahap lewat catu daya (ramping) saat saklar persisten resistif. Setelah saklar menjadi superkonduktor, catu daya dilepas dan arus bertahan.",
                    detailFull = "Terminal solenoid dihubungkan paralel dengan saklar persisten, yaitu kawat superkonduktor dengan kumparan pemanas.\n\n" +
                        "1. Ramping up: pemanas ON, saklar menjadi resistif, sehingga arus dari catu daya mengalir ke solenoid dan dinaikkan bertahap.\n" +
                        "2. Persisten: setelah medan tercapai, pemanas OFF, saklar menjadi superkonduktor dan membentuk lingkaran tertutup dengan solenoid. Catu daya dilepas; arus bertahan.\n" +
                        "3. Ramping down: catu daya disambungkan lagi dan disamakan arusnya, pemanas ON, lalu arus diturunkan bertahap. Energinya dibuang sebagai panas pada resistor dump.\n\n" +
                        "Ramping down terencana juga dapat dipakai untuk mengeluarkan benda feromagnetik besar tanpa quench, hanya bila tidak ada bahaya bagi nyawa atau anggota tubuh. Bahaya ancaman nyawa ditangani dengan quench (T2.6).",
                    analogi = "Analogi: Saklar persisten seperti kran yang ditutup setelah bak penuh: air (arus) terus berputar di dalam lingkaran tertutup tanpa pompa.",
                    prerequisite = "T2.2",
                    learningGoal = "Menjelaskan urutan ramping up, mode persisten, dan ramping down.",
                    crossLinks = "Aktivitas terkait: A3 (Magnet superkonduktif)",
                    labType = "m5_a3_superkonduktif",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M05.T2.4",
                    level = 2,
                    title = "T2.4 Homogenitas B0 dan shimming (ppm; pasif dan aktif; koil shim)",
                    summaryShort = "B0 harus homogen dalam satuan ppm pada bola 40 cm. Shim pasif (pelat besi) dan shim aktif (koil shim) meratakan sisa ketidakmerataan.",
                    detailFull = "Homogenitas dinyatakan dalam ppm pada bola pencitraan 40 cm. Medan homogen penting karena (1) mencegah distorsi geometrik, dan (2) frekuensi presesi bergantung pada medan: ketidakhomogenan menggeser frekuensi dan mengganggu SNR serta supresi lemak.\n\n" +
                        "Medan mentah bisa bervariasi sekitar 100 ppm, padahal pencitraan pada 1,5 sampai 3 T butuh sekitar 10 ppm atau lebih baik.\n" +
                        "• Shim pasif: nampan plastik non-feromagnetik sepanjang bore dengan sekitar 14 sampai 16 kompartemen berisi pelat feromagnetik kecil. Insinyur mengukur medan pada bola 40 cm lalu menempatkan pelat untuk mengimbangi ketidakhomogenan.\n" +
                        "• Shim aktif: koil shim berarus yang berada di warm bore, antara magnet dan gradient coil. Arusnya dapat diatur, termasuk untuk tiap pasien.\n\n" +
                        "Rumus: f (Hz) = 42,58 MHz/T × B0 × ppm\n\n" +
                        "Konversi ppm ke Hz: f = 42,58 MHz/T × B0 × ppm. Pada 1,5 T, 1 ppm sekitar 64 Hz; pada 3 T sekitar 128 Hz .",
                    analogi = "Analogi: Shim seperti mengganjal kaki meja agar permukaannya benar-benar rata.",
                    prerequisite = "T2.2",
                    learningGoal = "Menjelaskan mengapa B0 harus homogen dan bagaimana shim meluruskan sisa ketidakmerataan.",
                    crossLinks = "→ Gradient coil dan shim aktif lebih rinci: M06A T3.1. | Aktivitas terkait: A2",
                    labType = "m5_a2_ppm_hz",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M05.T2.5",
                    level = 2,
                    title = "T2.5 Medan tepi dan perisai 5 gauss",
                    summaryShort = "Perisai membatasi medan tepi 5 G (0,5 mT). Pasif memakai pelat baja; aktif memakai bucking coil berarus berlawanan.",
                    detailFull = "Medan tepi dapat mengganggu peralatan sekitar dan berbahaya bagi publik pada 5 G atau lebih (5 G = 0,5 mT). Perisai menjaga garis 5 G tetap di dalam area terkendali.\n\n" +
                        "• Perisai pasif: pelat baja tebal di sekitar pesawat atau di dinding ruang. Garis fluks lebih suka melewati bahan feromagnetik daripada udara, jadi medan tepi dibelokkan kembali. Pada 1,5 T tanpa perisai, batas 5 G bisa sejauh sekitar 10 m. Kekurangan: bobot lebih dari 20 ton, mahal, dan logam dekat magnet dapat mengganggu homogenitas.\n" +
                        "• Perisai aktif: bucking coil, solenoid berdiameter lebih besar di kedua ujung dengan arus berlawanan sehingga fluks dibalik dan jejak 5 G menyempit. Kini dipakai pada sebagian besar pesawat klinis.\n\n" +
                        "Waspada: perisai aktif membuat gradien medan statis sangat curam, sehingga benda feromagnetik baru terasa ketika sudah dekat dan sulit dicegah menjadi proyektil.\n\n" +
                        "Gaya Lorentz (F = qE + qv × B): konduktor berarus yang tegak lurus medan mengalami gaya. Ini menjelaskan tantangan perisai aktif pada medan sangat tinggi dan bunyi bising gradient coil saat aktif.",
                    analogi = "Analogi: Perisai pasif seperti tembok yang membelokkan air. Perisai aktif seperti arus tandingan yang menahan air agar tidak menyebar jauh, tetapi tepinya jadi lebih tajam.",
                    prerequisite = "T2.2",
                    learningGoal = "Membedakan perisai pasif dan aktif serta menyebut satu kelebihan dan kekurangan tiap jenis.",
                    crossLinks = "→ Bahaya dan zona 5 G: M07B T2.3. | Aktivitas terkait: A4",
                    labType = "m5_a4_perisai_5g",
                    sortOrder = 9
                ),
                MriNode(
                    code = "M05.T2.6",
                    level = 2,
                    title = "T2.6 Quench: hilangnya superkonduktivitas tiba-tiba",
                    summaryShort = "Quench adalah hilangnya superkonduktivitas secara tiba-tiba: helium menguap cepat dan keluar lewat pipa quench. Hanya dilakukan untuk ancaman nyawa akibat medan magnet.",
                    detailFull = "Urutan kejadian quench:\n" +
                        "1. Sebagian kawat memanas dan keluar dari keadaan superkonduktif.\n" +
                        "2. Hambatan muncul, sehingga arus besar berubah menjadi panas.\n" +
                        "3. Panas menyebar ke seluruh kumparan dan helium cair menguap cepat (volumenya mengembang sekitar 700 kali sebagai gas).\n" +
                        "4. Gas helium keluar lewat pipa quench ke luar gedung, dan medan magnet menghilang.\n\n" +
                        "Quench dapat terjadi sendiri (gangguan pada magnet) atau sengaja dipicu lewat tombol darurat. Tombol itu hanya untuk ancaman nyawa akibat medan magnet, misalnya orang terjepit benda feromagnetik. Quench merusak ketersediaan pesawat dan mahal, sehingga tidak dipakai untuk kerusakan alat.\n\n" +
                        "Bila pipa quench bocor ke ruangan, helium dapat menggantikan oksigen dan menimbulkan bahaya sesak napas.",
                    analogi = "Analogi: Seperti tutup panci presto yang terbuka: tekanan uap yang menumpuk keluar cepat lewat saluran buang agar panci tidak meledak.",
                    prerequisite = "T2.1",
                    learningGoal = "Menjelaskan apa itu quench, urutan kejadiannya, dan kapan dipakai.",
                    crossLinks = "→ Keselamatan quench dan prosedur: M07B T2.7, T3.1, T3.2. | Aktivitas terkait: A7",
                    labType = "m5_a7_quench",
                    sortOrder = 10
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M05.T3.1",
                    level = 3,
                    title = "T3.1 Efek kenaikan B0",
                    summaryShort = "B0 naik: SNR naik kira-kira linear, SAR naik dengan B0 kuadrat, T1 memanjang, pergeseran kimia dalam Hz makin lebar, dan artefak suseptibilitas makin kuat.",
                    detailFull = "• SNR naik kira-kira linear dengan B0 (sinyal naik sebanding kuadrat medan, derau naik linear). Pada fantom melipatkan medan kira-kira melipatkan SNR; pada pasien belum tentu.\n" +
                        "• SAR naik dengan B0 kuadrat (batas seluruh tubuh pada mode normal 2 W/kg). SAR membatasi TR spin-echo dan flip angle gradient-echo.\n" +
                        "• T1 memanjang, sehingga kontras T1 berubah.\n" +
                        "• Pergeseran kimia dalam Hz makin lebar (dalam ppm tetap). Bandwidth terima perlu dinaikkan, yang mengurangi SNR. Di sisi lain, supresi lemak spektral lebih efektif karena puncak lemak terpisah dari air.\n" +
                        "• Artefak suseptibilitas makin kuat: sinyal cepat hilang fase di dekat logam atau batas udara-jaringan.\n\n" +
                        "Keuntungan lain: pencitraan lebih cepat, voksel lebih kecil, fMRI (BOLD) lebih jelas, dan spektroskopi lebih mudah membedakan metabolit.\n\n" +
                        "Kuat medan sebenarnya dinyatakan dalam A/m (simbol H), sedangkan kerapatan fluks B dalam tesla; 1 T = 10.000 G. Medan bumi sekitar 0,5 G.",
                    analogi = "Analogi: Seperti menaikkan volume radio: suara makin jelas, tetapi bila terlalu keras timbul distorsi dan perangkat jadi panas.",
                    prerequisite = "T1.4",
                    learningGoal = "Menjelaskan konsekuensi kenaikan B0 terhadap SNR, SAR, T1, pergeseran kimia, dan artefak suseptibilitas.",
                    crossLinks = "→ SAR dan batas paparan: M07C | → T1 memanjang: M02 T3.1 | → Pergeseran kimia dalam Hz: M06B T3.1 | → Artefak suseptibilitas: M08C T2.6 | Aktivitas terkait: A5",
                    labType = "m5_a5_b0_snr_sar",
                    sortOrder = 11
                ),
                MriNode(
                    code = "M05.T3.2",
                    level = 3,
                    title = "T3.2 Host computer dan pulse generator module (PGM)",
                    summaryShort = "Host computer menerima perintah operator; PGM mengatur waktu pulsa RF dan gradien sesuai sekuens.",
                    detailFull = "Host computer adalah antarmuka operator: protokol dan parameter pemindaian dipilih di sini, lalu dikirim ke sistem pengendali.\n\n" +
                        "Pulse generator module (PGM) mengatur waktu (timing) pulsa RF dan gradien sesuai sekuens yang dipilih. Rincian pembagian tugas tiap perangkat berbeda antarpabrikan dan dibahas di M09 T2.5.",
                    analogi = "Analogi: Host computer seperti dirigen yang menerima partitur dari operator; PGM seperti metronom yang menjaga ketepatan waktu tiap pemain.",
                    prerequisite = "Opsional",
                    learningGoal = "Menyebut peran host computer dan PGM di sisi pengendali pesawat.",
                    crossLinks = "→ Rincian lebih lanjut: M09 T2.5.",
                    labType = null,
                    sortOrder = 12
                )
            )
        ),
        MriModule(
            id = "M06A",
            code = "M06A",
            title = "Sistem Gradien & Shimming",
            subtitle = "Kumparan X/Y/Z, Slew Rate, Arus Eddy, Gradien Seimbang & Stejskal-Tanner",
            description = "Mempelajari fungsi gradien untuk pengodean spasial irisan dan piksel/voksel, perbedaan gradient coil vs RF coil, tiga sumbu gradien X/Y/Z (Maxwell & Golay), kinerja amplitudo (mT/m) dan slew rate (T/m/s), konstruksi perisai aktif penahan arus eddy, amplifier linear vs PWM, gradien seimbang & bising akustik Lorentz, shimming dinamis, hingga skema Stejskal–Tanner.",
            levelBadge = "T1 - T3 Gradien",
            iconName = "tune",
            nodes = listOf(
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M06A.T1.1",
                    level = 1,
                    title = "Mengapa perlu gradien",
                    summaryShort = "Tanpa gradien, seluruh proton berpresesi pada frekuensi Larmor yang sama. Gradien mengubah kuat medan secara linear terhadap posisi untuk memilih irisan dan mengode piksel/voksel.",
                    detailFull = "Di dalam medan magnet utama B0 yang homogen, semua proton hidrogen berpresesi pada frekuensi Larmor yang sama (f = γ · B0). Akibatnya, sinyal RF yang diterima tidak memberi tahu dari lokasi mana sinyal tersebut berasal.\n\n" +
                        "Kumparan gradien menghasilkan medan magnet tambahan yang berubah secara linear sepanjang sumbu tertentu:\n" +
                        "• Di isosenter (titik tengah magnet), medan tambahan bernilai nol sehingga frekuensi tetap f0.\n" +
                        "• Di satu sisi isosenter, medan bertambah sehingga proton berpresesi lebih cepat.\n" +
                        "• Di sisi berlawanan, medan berkurang sehingga proton berpresesi lebih lambat.\n\n" +
                        "Rumus: B(x) = B0 + Gx · x  →  f(x) = γ · (B0 + Gx · x)\n\n" +
                        "Dengan demikian posisi spasial diterjemahkan menjadi frekuensi dan fase untuk memilih irisan (slice selection) serta mengode piksel/voksel dalam tiga dimensi.",
                    analogi = "Analogi: Bayangkan paduan suara di mana semua penyanyi menyanyikan nada yang sama; sistem penerima tidak dapat membedakan posisi spasial masing-masing sumber. Gradien mengatur agar penyanyi di kiri bernada rendah dan makin ke kanan nadanya makin tinggi.",
                    prerequisite = "M01, M04A",
                    learningGoal = "Menjelaskan mengapa frekuensi Larmor yang seragam membutuhkan gradien untuk pengodean spasial irisan dan voksel.",
                    crossLinks = "→ Pengodean spasial 3 langkah: M04A T1.1 | → Sistem RF: M06B T1.2",
                    labType = "m6a_peta_frekuensi",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M06A.T1.2",
                    level = 1,
                    title = "Gradient coil dan RF coil",
                    summaryShort = "Gradient coil (3 pasang kumparan X, Y, Z) mengubah medan B0 secara linear untuk mengode posisi; RF coil memancarkan pulsa B1 dan menerima sinyal gema dari jaringan.",
                    detailFull = "Walaupun sama-sama disebut kumparan (coil), kumparan gradien dan kumparan RF memiliki tugas dan frekuensi kerja yang sangat berbeda:\n\n" +
                        "• Gradient coil: Terdiri atas tiga set kumparan (sumbu X, Y, dan Z) di dalam warm bore magnet. Diberi pulsa arus searah berdaya besar yang dinyalakan-matikan dengan cepat pada frekuensi audio (kHz) untuk membuat kemiringan medan linear di atas B0.\n" +
                        "• RF coil: Bekerja pada pita frekuensi radio VHF (frekuensi Larmor: 63,87 MHz pada 1,5 T; 127,74 MHz pada 3 T) untuk memancarkan medan B1 yang memiringkan magnetisasi dan menangkap sinyal FID/echo dari pasien.",
                    analogi = "Analogi: Gradient coil adalah penata nomor kursi di gedung konser agar tiap baris punya alamat unik, sedangkan RF coil adalah mikrofon dan pengeras suara panggung.",
                    prerequisite = "M05.T1.1",
                    learningGoal = "Membedakan fungsi kumparan gradien X/Y/Z pengode posisi dengan RF coil pemancar/penerima.",
                    crossLinks = "→ Pengingat fungsi di Modul RF: M06B T1.2",
                    labType = "m6a_peta_frekuensi",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M06A.T1.3",
                    level = 1,
                    title = "Tiga sumbu gradien",
                    summaryShort = "Pada pesawat bore tertutup: sumbu Z searah B0 (kepala–kaki), sumbu X kiri–kanan, dan sumbu Y depan–belakang. Pada pesawat magnet permanen terbuka, B0 dan sumbu Z berarah vertikal.",
                    detailFull = "Tiga pasang kumparan gradien disusun saling tegak lurus (ortogonal) dan berpusat pada isosenter:\n\n" +
                        "• Sumbu Z (Gz): Sejajar sumbu panjang bore (kepala ke kaki / superior–inferior pada pasien telentang). Secara konvensional mengode pemilihan irisan aksial.\n" +
                        "• Sumbu X (Gx): Melintang horizontal dari kiri ke kanan pasien.\n" +
                        "• Sumbu Y (Gy): Vertikal dari depan ke belakang (anterior–posterior) pasien.\n\n" +
                        "Catatan orientasi pesawat terbuka: Pada magnet permanen terbuka (open bore), medan B0 berarah vertikal (bawah ke atas). Sumbu Z selalu didefinisikan mengikuti arah medan utama B0, tetapi perangkat lunak konsol memetakan koordinat anatomi pasien secara otomatis (irisan aksial, sagital, koronal, maupun oblik).",
                    analogi = "Analogi: Seperti koordinat GPS 3 dimensi di dalam terowongan: Z untuk maju-mundur sepanjang terowongan, X untuk geser kiri-kanan, dan Y untuk naik-turun.",
                    prerequisite = "M06A.T1.1",
                    learningGoal = "Mengidentifikasi arah fisik sumbu gradien X, Y, dan Z pada magnet bore tertutup maupun pesawat terbuka.",
                    crossLinks = "→ Pemilihan irisan aksial, sagital, koronal: M04A T1.4",
                    labType = "m6a_sumbu_gradien",
                    sortOrder = 3
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M06A.T2.1",
                    level = 2,
                    title = "Kinerja gradien, bentuk kumparan, dan arus eddy",
                    summaryShort = "Kinerja diukur dari Amplitudo (mT/m) dan Slew Rate (T/m/s), dengan waktu naik = G/slew. Kumparan Z memakai pasangan Maxwell, sedangkan X dan Y memakai kumparan pelana Golay.",
                    detailFull = "Parameter kinerja dan geometri kumparan gradien:\n\n" +
                        "1. Amplitudo maksimum (Gmax, satuan mT/m): Kemiringan medan terbesar yang dapat dicapai (klinis 20–80 mT/m). Makin tinggi amplitudo, makin tipis irisan dan makin kecil FOV yang bisa dicapai.\n" +
                        "2. Waktu naik (Rise Time, satuan ms atau μs): Waktu untuk menaikkan gradien dari nol ke amplitudo puncak.\n" +
                        "3. Slew rate (satuan T/m/s): Laju perubahan gradien terhadap waktu.\n\n" +
                        "Rumus: Waktu naik (ms) = Amplitudo (mT/m) / Slew rate (T/m/s)\n\n" +
                        "4. Geometri kumparan:\n" +
                        "• Kumparan Z (Pasangan Maxwell): Dua cincin melingkar di kedua sisi isosenter dengan arus berlawanan arah.\n" +
                        "• Kumparan X dan Y (Kumparan Pelana / Golay): Dua pasang kumparan berbentuk pelana (saddle) yang melengkung mengikuti dinding silinder bore; pasangan Y diputar 90° terhadap pasangan X.\n\n" +
                        "5. Arus eddy (Eddy currents): Perubahan fluks gradien yang cepat menginduksi arus melingkar pada struktur konduktor di dekatnya (mis. dinding kriostat), yang melawan dan mendistorsi profil trapesium gradien.",
                    analogi = "Analogi: Amplitudo adalah kecepatan puncak mobil balap, sedangkan slew rate adalah akselerasinya (seberapa cepat mobil melesat dari 0 ke kecepatan puncak).",
                    prerequisite = "M06A.T1.3",
                    learningGoal = "Menghitung waktu naik gradien dari amplitudo dan slew rate serta membedakan kumparan Maxwell (Z) dan Golay (X/Y).",
                    crossLinks = "→ Konstruksi perisai aktif: M06A T2.2 | → EPI & ruang-k cepat: M04C T3.7",
                    labType = "m6a_trapesium_gradien",
                    sortOrder = 4
                ),
                MriNode(
                    code = "M06A.T2.2",
                    level = 2,
                    title = "Konstruksi gradient coil dan perisai aktif",
                    summaryShort = "Kumparan dibuat dari lembaran tembaga berpola sidik jari di dalam silinder epoksi berpenguat serat kaca, didinginkan air, dan dilengkapi perisai gradien aktif untuk menahan arus eddy.",
                    detailFull = "Konstruksi fisik rakitan gradien modern:\n\n" +
                        "• Pola sidik jari (Distributed fingerprint coils): Konduktor tembaga dipotong dengan komputer membentuk pola lengkung menyerupai sidik jari agar menghasilkan linearitas medan yang presisi dengan induktansi rendah.\n" +
                        "• Cangkang epoksi & penahan gaya Lorentz: Ketiga sumbu (X, Y, Z) dicor di dalam silinder resin epoksi dan fiberglass yang sangat kaku untuk menahan hentakan mekanik gaya Lorentz saat arus ratusan ampere mengalir di dalam medan B0.\n" +
                        "• Pendingin air (Water cooling): Arus besar pada tembaga resistif menghasilkan panas tinggi (puluhan kilowatt), sehingga pipa air dingin dialirkan terus-menerus di dalam rakitan gradien.\n" +
                        "• Perisai gradien aktif (Actively shielded gradients): Lapisan kumparan sekunder dipasang di bagian luar kumparan gradien utama untuk meniadakan fluks gradien yang bocor ke arah kriostat, sehingga menekan timbulnya arus eddy.",
                    analogi = "Analogi: Seperti mesin mobil bertenaga tinggi yang dibungkus sasis baja kaku agar tidak bergetar lepas, didinginkan radiator air, dan diberi peredam agar getarannya tidak merusak bodi luar.",
                    prerequisite = "M06A.T2.1",
                    learningGoal = "Menjelaskan fungsi pola sidik jari, pendingin air, coran epoksi penahan gaya Lorentz, dan perisai aktif penahan arus eddy.",
                    crossLinks = "→ Gaya Lorentz & perisai magnet: M05 T2.5",
                    labType = "m6a_koil_maxwell_golay",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M06A.T2.3",
                    level = 2,
                    title = "Amplifier gradien (Linear vs PWM)",
                    summaryShort = "Tiga amplifier gradien (X, Y, Z) menyuplai arus ratusan ampere. Amplifier PWM (Pulse Width Modulated) jauh lebih hemat energi dan sedikit membuang panas dibandingkan amplifier linear analog.",
                    detailFull = "Setiap sumbu gradien (X, Y, dan Z) dikendalikan oleh amplifier gradien berdaya besar di ruang peralatan yang mengubah sinyal kendali dari PGM menjadi arus hingga ratusan ampere dan tegangan ratusan volt.\n\n" +
                        "Perbandingan dua teknologi amplifier gradien:\n" +
                        "• Amplifier Linear (Analog): Mengatur arus secara kontinu menggunakan transistor daya yang bekerja di daerah aktif. Sangat presisi dan berderau rendah, tetapi membuang banyak energi sebagai panas walaupun gradien sedang berada di nilai menengah.\n" +
                        "• Amplifier PWM (Pulse Width Modulated / Saklar Digital): Menggunakan saklar semikonduktor berkecepatan tinggi (IGBT/MOSFET) yang hanya berada di keadaan ON penuh atau OFF penuh, dengan lebar pulsa yang diatur sesuai bentuk gelombang target. Efisiensi energinya jauh lebih tinggi dan disipasi panasnya jauh lebih rendah.",
                    analogi = "Analogi: Amplifier linear seperti mengatur terangnya lampu dengan tahanan geser yang ikut menjadi panas; amplifier PWM seperti dimmer modern yang menyalakan-matikan saklar ribuan kali per detik tanpa membuang panas.",
                    prerequisite = "M05.T3.2, M06A.T2.1",
                    learningGoal = "Membandingkan prinsip kerja dan efisiensi energi amplifier gradien linear analog dengan amplifier PWM.",
                    crossLinks = "→ Kendali waktu oleh PGM: M05 T3.2 & M09 T2.5",
                    labType = null,
                    sortOrder = 6
                ),
                MriNode(
                    code = "M06A.T2.4",
                    level = 2,
                    title = "Fungsi dan ukuran kinerja gradien",
                    summaryShort = "Empat ukuran kinerja utama: Amplitudo maksimum (mT/m), Rise Time (μs), Slew Rate = Amplitudo/Rise Time (T/m/s), dan Duty Cycle (%). Slew rate tinggi dibatasi oleh stimulasi saraf tepi (PNS).",
                    detailFull = "Ringkasan fungsi dan tolok ukur spesifikasi gradien:\n\n" +
                        "1. Fungsi utama gradien: Memilih irisan (Gz), mengode frekuensi/baca (Gx), mengode fase (Gy), serta me-rephase atau men-dephase magnetisasi transversal (rewinder, pre-dephaser, spoiler/crusher, dan difusi).\n" +
                        "2. Empat ukuran kinerja:\n" +
                        "• Amplitudo maksimum (mT/m): Menentukan ketebalan irisan minimum dan FOV minimum.\n" +
                        "• Rise time (μs atau ms): Waktu transisi dari 0 ke amplitudo puncak.\n" +
                        "• Slew rate (T/m/s): Amplitudo dibagi rise time. Menentukan seberapa pendek TE dan echo spacing pada EPI/TSE.\n" +
                        "• Duty cycle (%): Persentase waktu maksimum gradien boleh menyala pada daya puncak tanpa mengalami panas berlebih.\n\n" +
                        "Batas fisiologis: Slew rate yang terlalu tinggi menghasilkan perubahan medan magnet terhadap waktu (dB/dt) yang menginduksi arus listrik pada serabut saraf pasien dan memicu stimulasi saraf perifer (Peripheral Nerve Stimulation / PNS).",
                    analogi = "Analogi: Duty cycle adalah ketahanan stamina pelari sprint: seberapa sering ia boleh berlari kecepatan penuh sebelum harus beristirahat agar tidak cedera panas.",
                    prerequisite = "M06A.T2.1",
                    learningGoal = "Merinci keempat parameter ukuran kinerja gradien beserta batas fisiologis stimulasi saraf perifer (PNS).",
                    crossLinks = "→ Keselamatan dB/dt & PNS: M07",
                    labType = "m6a_trapesium_gradien",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M06A.T2.5",
                    level = 2,
                    title = "Gradien seimbang dan kebisingan akustik",
                    summaryShort = "Gradien seimbang memakai dua lobus berpolaritas berlawanan dengan luas (G × t) yang sama agar spin kembali sefase di tengah gema. Gaya Lorentz pada kumparan memicu kebisingan akustik.",
                    detailFull = "1. Prinsip Gradien Seimbang (Balanced Gradient Lobes):\n" +
                        "Setiap kali gradien dinyalakan (misalnya saat memilih irisan atau membaca frekuensi), spin di posisi berbeda berpresesi dengan kecepatan berbeda sehingga mengalami dephasing (kehilangan koherensi fase). Untuk memulihkan fase spin agar terbentuk gema (gradient echo), diberikan lobus gradien kedua dengan polaritas berlawanan yang memiliki luas area (Amplitudo × Durasi) sama dengan separuh lobus utama.\n\n" +
                        "2. Kebisingan Akustik (Acoustic Noise):\n" +
                        "Arus bolak-balik pada kumparan gradien berada di dalam medan statis B0 yang kuat, sehingga mengalami gaya Lorentz (F = I · L × B0). Karena arus gradien berubah cepat, kumparan bergetar seperti membran pengeras suara raksasa dan menghasilkan suara ketukan keras (dapat melampaui 100 dB(A), terutama pada sekuens cepat seperti EPI), sehingga pasien wajib memakai pelindung telinga.",
                    analogi = "Analogi: Lobus gradien seimbang seperti pelari yang berlari maju selama 10 detik, lalu berbalik arah dengan kecepatan sama selama 10 detik hingga semuanya berkumpul kembali di garis start pada saat yang bersamaan.",
                    prerequisite = "M04A.T1.5, M06A.T2.1",
                    learningGoal = "Menjelaskan bagaimana dua lobus gradien berlawanan polaritas memulihkan fase spin dan mengapa gaya Lorentz menimbulkan bising akustik.",
                    crossLinks = "→ Pembentukan gema gradien: M04A T1.6",
                    labType = "m6a_gradien_seimbang",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M06A.T2.6",
                    level = 2,
                    title = "Hal yang perlu diingat tentang gradient coil",
                    summaryShort = "Lima kaidah inti: (1) Besar arus menentukan amplitudo, (2) Arah arus menentukan polaritas, (3) Amplitudo menentukan resolusi/ketebalan irisan, (4) Slew rate menentukan kecepatan akuisisi, (5) Luas lobus menentukan fase.",
                    detailFull = "Lima kaidah utama kumparan gradien yang wajib dikuasai radiografer:\n\n" +
                        "1. Besar arus listrik yang dialirkan ke kumparan menentukan amplitudo (kecuraman) gradien.\n" +
                        "2. Arah aliran arus menentukan polaritas gradien (sisi mana yang medannya lebih tinggi atau lebih rendah dari B0).\n" +
                        "3. Amplitudo gradien maksimum menentukan ketebalan irisan terkecil dan resolusi spasial tertinggi (FOV terkecil).\n" +
                        "4. Slew rate menentukan kecepatan pembalikan gradien dan mempersingkat TE serta waktu pindai sekuens cepat.\n" +
                        "5. Luas di bawah kurva gradien (Amplitudo × Durasi) menentukan besar pergeseran fase total yang dialami spin.",
                    analogi = "Analogi: Lima tombol kendali utama kemudi: injakan gas mengatur tanjakan (amplitudo), gigi maju/mundur mengatur arah (polaritas), dan luas injakan × waktu menentukan jarak tempuh fase.",
                    prerequisite = "M06A.T2.4, M06A.T2.5",
                    learningGoal = "Merangkum kelima hubungan antara arus listrik, polaritas, amplitudo, slew rate, dan luas lobus gradien.",
                    crossLinks = "→ Ruang-k dan luas gradien: M04B T2.3",
                    labType = "m6a_trapesium_gradien",
                    sortOrder = 9
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M06A.T3.1",
                    level = 3,
                    title = "Shimming dinamis dan uji homogenitas",
                    summaryShort = "Selain shim superkonduktor di kriostat, shim aktif resistif dan offset arus pada kumparan gradien X/Y/Z melakukan shimming dinamis per pasien/irisan berdasarkan peta beda fase dua gema GRE.",
                    detailFull = "Tubuh pasien yang masuk ke dalam bore mengubah keseragaman medan B0 akibat perbedaan suseptibilitas magnetik jaringan dan rongga udara. Karena itu diperlukan shimming per pasien:\n\n" +
                        "• Shim aktif superkonduktor vs resistif: Kumparan shim superkonduktor di dalam kriostat diatur saat instalasi, sedangkan kumparan shim resistif suhu ruang di warm bore dapat diubah arusnya untuk tiap pemeriksaan.\n" +
                        "• Gradient offset shimming (Shimming dinamis): Untuk mengoreksi komponen ketidakhomogenan linear (orde pertama), pesawat menambahkan arus offset DC kecil langsung pada kumparan gradien X, Y, dan Z, bahkan dapat diubah secara dinamis dari satu irisan ke irisan berikutnya.\n" +
                        "• Uji homogenitas dengan dua gema GRE: Pesawat mengambil sekuens gradient-echo cepat dengan dua waktu gema berbeda (TE1 dan TE2). Selisih fase (Δφ) antar piksel dibagi selisih waktu (ΔTE) menghasilkan peta frekuensi ΔB0 untuk menghitung arus koreksi shim secara otomatis.",
                    analogi = "Analogi: Seperti suspensi aktif mobil modern yang memindai kemiringan jalan tiap detik (dua gema GRE) lalu menyesuaikan tinggi tiap roda secara otomatis untuk tiap penumpang.",
                    prerequisite = "M05.T2.4, M06A.T2.5",
                    learningGoal = "Menjelaskan mekanisme gradient offset shimming dinamis dan pemetaan homogenitas B0 menggunakan beda fase dua gema GRE.",
                    crossLinks = "→ Homogenitas ppm dan shim dasar: M05 T2.4",
                    labType = "m6a_gradien_seimbang",
                    sortOrder = 10
                ),
                MriNode(
                    code = "M06A.T3.2",
                    level = 3,
                    title = "Skema Stejskal–Tanner",
                    summaryShort = "Pada sekuens spin-echo difusi (DWI), dua lobus gradien pengode difusi dipasang mengapit pulsa RF 180° dengan polaritas searah: spin diam kembali sefase, sedangkan spin yang berdifusi mengalami atenuasi sinyal.",
                    detailFull = "Skema Stejskal–Tanner adalah fondasi pencitraan difusi (DWI) menggunakan sekuens spin-echo:\n\n" +
                        "1. Lobus gradien pertama (di antara pulsa RF 90° dan 180°) memberikan pergeseran fase yang bergantung pada posisi awal molekul air.\n" +
                        "2. Pulsa RF refokusing 180° membalikkan tanda fase seluruh spin (−φ).\n" +
                        "3. Lobus gradien kedua (di antara pulsa RF 180° dan pembacaan gema) diberikan dengan polaritas yang SAMA (searah) dan luas yang sama dengan lobus pertama.\n\n" +
                        "Mengapa polaritasnya searah? Karena pulsa 180° sudah membalik fase spin, lobus kedua yang searah justru membatalkan pergeseran fase lobus pertama bagi molekul air yang DIAM di tempatnya (kembali sefase sempurna). Sebaliknya, molekul air yang berpindah posisi (berdifusi gerak Brown) selama jeda waktu Δ merasakan kuat medan berbeda pada lobus kedua, sehingga fasenya tidak pulih sempurna dan amplitudo sinyalnya meluruh.",
                    analogi = "Analogi: Memotret orang dengan dua kilatan lampu di tengah cermin pembalik (180°): orang yang berdiri diam bayangannya berimpit tajam, sedangkan orang yang berjalan bayangannya kabur dan redup.",
                    prerequisite = "M06A.T2.5",
                    learningGoal = "Menjelaskan mengapa dua lobus gradien Stejskal–Tanner di kedua sisi pulsa 180° memiliki polaritas searah untuk mendeteksi difusi molekul air.",
                    crossLinks = "→ Pencitraan difusi DWI & ADC: M03C",
                    labType = "m6a_gradien_seimbang",
                    sortOrder = 11
                )
            )
        ),
        MriModule(
            id = "M06B",
            code = "M06B",
            title = "Sistem RF dan Koil",
            subtitle = "Kandang Faraday, Pulsa Sinc, Birdcage Kuadratur, Array Fasa & Transmit Paralel",
            description = "Menelusuri frekuensi Larmor pada pita VHF, fungsi kandang Faraday dan artefak kebocoran RF, rantai pemancar dan pulsa sinc, desain birdcage kuadratur 90° & faktor pengisi, rantai penerima (Coil → Preamplifier → Demodulasi → ADC), karakteristik coil permukaan/volume/array fasa, asal fisika pergeseran kimia lemak-air, serta ketidakhomogenan B1 & transmit paralel.",
            levelBadge = "T1 - T3 RF & Koil",
            iconName = "radio",
            nodes = listOf(
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M06B.T1.1",
                    level = 1,
                    title = "T1.1 Sistem RF (pita radio VHF, frekuensi Larmor)",
                    summaryShort = "RF (B1) dipancarkan pada frekuensi Larmor, yang berada di pita radio VHF (63,87 MHz pada 1,5 T dan 127,74 MHz pada 3 T).",
                    detailFull = "Sistem RF memancarkan dan menerima radiasi elektromagnetik selama akuisisi. RF dalam MRI sering disebut B1, yaitu medan magnet sekunder di samping B0. Pulsa RF dipancarkan pada frekuensi Larmor untuk memberi energi pada sebagian proton di irisan yang dipilih.\n\n" +
                        "Rasio giromagnetik hidrogen γ/2π = 42,58 MHz/T. Pada 1,5 T frekuensinya 63,87 MHz, dan pada 3 T 127,74 MHz. Frekuensi ini berada di pita VHF, yang juga dipakai radio FM, televisi, dan komunikasi lain. Karena itu ruang magnet dilindungi dari sumber radio luar (lihat T2.1).\n\n" +
                        "Perbandingan Pita Frekuensi:\n" +
                        "• MRI 1,5 T (hidrogen): 63,87 MHz\n" +
                        "• MRI 3 T (hidrogen): 127,74 MHz\n" +
                        "• Pita VHF: sekitar 30 sampai 300 MHz\n" +
                        "• Radio FM: sekitar 88 sampai 108 MHz",
                    analogi = "Analogi: Seperti menyetel stasiun radio: proton hanya “mendengar” pulsa jika disetel pada frekuensi yang tepat.",
                    prerequisite = "M01 · Dasar MRI",
                    learningGoal = "Menyebutkan frekuensi Larmor hidrogen pada 1,5 T dan 3 T serta posisinya di dalam pita radio VHF.",
                    crossLinks = "→ Perisai RF (kandang Faraday): M06B T2.1",
                    labType = null,
                    sortOrder = 1
                ),
                MriNode(
                    code = "M06B.T1.2",
                    level = 1,
                    title = "T1.2 Gradient coil vs RF coil: pengingat fungsi",
                    summaryShort = "Gradient coil memberi kode posisi. RF coil memancarkan pulsa dan menerima sinyal. Penjelasan lengkap ada di M06A.",
                    detailFull = "Pengingat singkat sebelum masuk ke sistem RF. Keduanya sama-sama kumparan, tetapi tugas dan frekuensinya berbeda.\n\n" +
                        "Perbandingan Gradient coil vs RF coil:\n" +
                        "• Tugas: Membuat medan yang berubah bertahap sehingga posisi terkode (Gradient coil) vs Memancarkan pulsa RF dan/atau menerima sinyal (RF coil).\n" +
                        "• Jumlah: Tiga sumbu X, Y, Z (Gradient coil) vs Body coil, kepala, permukaan, array fasa (RF coil).\n" +
                        "• Medan yang dihasilkan: Perubahan lambat pada B0 sepanjang satu arah (Gradient coil) vs B1 berosilasi pada frekuensi Larmor (RF coil).\n" +
                        "• Dibahas di: M06A (Gradient coil) vs M06B modul ini (RF coil).",
                    analogi = "Analogi: Gradient coil seperti pemberi nomor kursi di gedung konser. RF coil seperti pengeras suara sekaligus mikrofon.",
                    prerequisite = "M06A.T1.2",
                    learningGoal = "Membedakan tugas, jumlah kumparan, dan jenis medan yang dihasilkan antara gradient coil dan RF coil.",
                    crossLinks = "→ M06A T1.2 (konsep lengkap)",
                    labType = null,
                    sortOrder = 2
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M06B.T2.1",
                    level = 2,
                    title = "T2.1 Perisai RF (kandang Faraday)",
                    summaryShort = "Dinding, lantai, langit-langit, dan pintu dilapisi logam agar radio luar tidak masuk dan RF pesawat tidak bocor.",
                    detailFull = "Sinyal dari jaringan sangat lemah dibandingkan siaran radio di sekitarnya. Jika sinyal luar terdeteksi, citra memuat artefak. Sebaliknya, RF dari pesawat dapat mengganggu peralatan listrik di dekatnya. Karena itu seluruh ruang pindai dilapisi logam penghantar sebagai kandang Faraday (bukan timbal seperti ruang sinar-X).\n\n" +
                        "Tembaga menghantar sangat baik, tetapi mahal dan berat. Aluminium kini banyak dipilih karena lebih mudah ditangani dan cocok untuk pemasangan bergerak. Celah seperti jendela observasi dan ventilasi memakai kawat jala atau filter sarang lebah. Akses sesekali untuk gas, pipa, atau cairan lewat panel penetrasi yang berisi pemandu gelombang (waveguide): benda kecil bisa lewat, tetapi gelombang radio berfrekuensi MRI tidak. Panel itu juga membawa konektor multipin untuk listrik.",
                    analogi = "Analogi: Seperti ruang kedap suara: bising luar tidak masuk, dan suara dari dalam tidak keluar.",
                    prerequisite = "M06B.T1.1",
                    learningGoal = "Menjelaskan fungsi kandang Faraday, kawat jala jendela, ventilasi sarang lebah, dan pemandu gelombang pada panel penetrasi.",
                    crossLinks = "▶ Simulasi A5: Perisai RF dan artefak (simulasi kebocoran radio)",
                    labType = "m6b_a5_faraday",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M06B.T2.2",
                    level = 2,
                    title = "T2.2 Sistem pemancar RF dan pulsa sinc",
                    summaryShort = "Pulsa sinc memuat rentang frekuensi sempit yang rata, sehingga irisan berbatas tegas.",
                    detailFull = "Pulsa RF memuat rentang sempit frekuensi di sekitar frekuensi Larmor. Rentang itu disebut bandwidth pemancar dan menentukan durasi pulsa. Agar irisan berprofil persegi, pulsa harus persegi pada domain frekuensi: hanya memuat rentang sempit yang ditetapkan gradien pemilih irisan pada ketebalan irisan, dengan amplitudo sama. Bentuk pulsa yang memenuhi syarat ini disebut sinc.\n\n" +
                        "Rantai pemancar: pembangkit gelombang digital → DAC → sintesiser frekuensi (memusatkan frekuensi di sekitar Larmor dan mengatur fase) → amplifier RF berdaya tinggi → coil pemancar. Pulsa dikirim berulang selama akuisisi dan dinamai menurut fungsinya: eksitasi, rephasing, atau inversi.\n\n" +
                        "Rumus: Ketebalan irisan = Bandwidth pemancar / (γ × Gradien pemilih irisan)",
                    analogi = "Analogi: Bandwidth seperti lebar kuas dan gradien seperti kerapatan garis di dinding: kuas lebih lebar menutup irisan lebih tebal.",
                    prerequisite = "M04A.T1.5",
                    learningGoal = "Menjelaskan mengapa pulsa sinc menghasilkan profil irisan persegi serta mengurutkan komponen rantai pemancar RF.",
                    crossLinks = "→ M04A A2 (simulasi pulsa sinc)",
                    labType = null,
                    sortOrder = 4
                ),
                MriNode(
                    code = "M06B.T2.3",
                    level = 2,
                    title = "T2.3 Coil pemancar (birdcage, faktor pengisi)",
                    summaryShort = "Body coil birdcage memancarkan RF yang berputar agar energi masuk ke spin seefisien mungkin.",
                    detailFull = "Coil pemancar adalah coil resonator. Saat beresonansi pada frekuensi yang diperlukan, muncul osilasi besar pada tegangan dan arus, lalu terpancar gelombang radio ke daerah yang diperiksa. Cara paling efisien adalah medan yang berosilasi pada frekuensi Larmor sekaligus berputar searah dengan spin (polarisasi sirkular, atau desain kuadratur). Coil dihubungkan ke dua suplai dari amplifier RF, dan suplai kedua tertunda 90°, sehingga ada dua medan RF yang saling tegak lurus.\n\n" +
                        "Transceiver berarti coil yang memancar dan, bila perlu, juga menerima. Pada pesawat bore tertutup, coil utamanya disebut body coil dan berdesain birdcage: dua cincin ujung dihubungkan batang tembaga lurus berjumlah genap, di dalam struktur plexiglass.\n\n" +
                        "Kualitas sinyal sangat ditentukan faktor pengisi (filling factor), yaitu seberapa cocok bentuk dan ukuran coil dengan area anatomi. Homogenitas RF membaik pada coil yang lebih besar, tetapi ada tawar-menawar dengan faktor pengisi. ",
                    analogi = "Analogi: Seperti mendorong komidi putar: dorongan paling efektif bila mengikuti arah putarannya.",
                    prerequisite = "M06B.T2.2",
                    learningGoal = "Menjelaskan prinsip eksitasi kuadratur (2 suplai beda fase 90°) pada coil birdcage dan konsep faktor pengisi.",
                    crossLinks = "▶ Simulasi A8 · Birdcage: medan B1 berputar & faktor pengisi",
                    labType = "m6b_a8_birdcage",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M06B.T2.4",
                    level = 2,
                    title = "T2.4 Sistem penerima RF (coil, preamplifier, demodulasi, ADC)",
                    summaryShort = "Sinyal lemah dikuatkan, disaring dari derau, lalu didigitalkan: Coil → Preamplifier → Demodulasi → ADC.",
                    detailFull = "Sinyal dari pasien pada saat TE menginduksi tegangan berosilasi pada coil yang diletakkan dekat. Sinyal ini berfrekuensi VHF tetapi beramplitudo rendah. Pesawat lama menurunkan frekuensi sinyal (demodulasi) sebelum diproses, sedangkan teknologi baru dapat mengambil sampel langsung.\n\n" +
                        "Coil juga menerima derau, terutama dari gerak termal elektron di dalam coil dan jaringan tubuh. Derau ada di semua frekuensi, sehingga tampak di seluruh citra kecuali disaring. Rantai penerima: coil → preamplifier berderau rendah → penyaring → amplifier → ADC → komputer. Pada pesawat modern, penguatan, penyaringan, dan digitalisasi dilakukan di dalam coil atau tubuh pesawat, lalu sinyal digital dikirim lewat serat optik yang kebal terhadap kehilangan sinyal, interferensi, dan lonjakan derau.",
                    analogi = "Analogi: Seperti mendengar bisikan di keramaian: dekatkan telinga (coil dekat tubuh), perkeras suara (preamplifier), dan abaikan bising (penyaring).",
                    prerequisite = "M06B.T2.3",
                    learningGoal = "Mengurutkan tahapan rantai sinyal penerima RF dari coil, preamplifier, demodulasi, hingga ADC.",
                    crossLinks = "▶ Simulasi A1 · Rantai sinyal penerima",
                    labType = "m6b_a1_rantai_sinyal",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M06B.T2.5",
                    level = 2,
                    title = "T2.5 Jenis coil penerima (permukaan, volume, array fasa)",
                    summaryShort = "Coil permukaan: sinyal tinggi tetapi dangkal. Coil volume: merata. Array fasa: banyak elemen kecil untuk FOV luas.",
                    detailFull = "Ada dua jenis dasar coil penerima: coil permukaan dan coil volume. Coil permukaan dipakai untuk anatomi dekat permukaan, dan coil volume untuk jaringan yang besar. Coil permukaan tidak dipakai sebagai pemancar karena homogenitas RF-nya buruk, jadi ia hanya penerima.\n\n" +
                        "• Coil permukaan: Dipasang rata dekat anatomi, sehingga faktor pengisinya optimum dan sinyalnya tinggi. Ia juga hanya menerima derau dari area kecil. Kelemahannya: sensitivitas turun menurut jarak dari coil. Sinyal berguna hanya berasal dari kedalaman yang sebanding dengan diameter loop.\n\n" +
                        "• Coil volume: Mengelilingi bagian tubuh, medannya lebih merata, tetapi SNR umumnya lebih rendah daripada coil permukaan di dekat anatomi.\n\n" +
                        "• Array fasa (Phased-array): Terdiri atas banyak elemen kecil, tiap elemen punya kanalnya sendiri. Susunan ini memberi keunggulan SNR loop tunggal dengan FOV jauh lebih luas. Contohnya array linear untuk tulang belakang: enam bagian dengan elemen yang saling tumpang tindih. Bagian pertama dapat dipakai sendiri untuk tulang belakang servikal; untuk FOV lebih besar, lebih banyak elemen dipilih.",
                    analogi = "Analogi: Coil permukaan seperti senter kecil: terang tetapi jangkauannya dekat. Array fasa seperti banyak senter kecil yang berjajar, sehingga area luas tetap terang.",
                    prerequisite = "M06B.T2.4",
                    learningGoal = "Memilih jenis coil penerima (permukaan, volume, array fasa) berdasarkan anatomi dan kedalaman target.",
                    crossLinks = "▶ Simulasi A2 (Pemilih coil), A3 (Sensitivitas kedalaman), A4 (Array fasa tulang belakang) | → M08C (Shading)",
                    labType = "m6b_a2_a3_a4_koil",
                    sortOrder = 7
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M06B.T3.1",
                    level = 3,
                    title = "T3.1 Pergeseran kimia lemak-air: asal fisika",
                    summaryShort = "Selisih lemak-air tetap dalam ppm (3,5 ppm), tetapi naik dalam Hz saat B0 naik (~224 Hz pada 1,5 T dan ~447 Hz pada 3 T).",
                    detailFull = "Pergeseran kimia lemak-air: Proton lemak berputar sedikit lebih lambat dari proton air. Selisihnya sekitar 3,5 ppm dari frekuensi Larmor, sehingga selisih dalam Hz sama dengan ppm dikali frekuensi dalam MHz. Pada 1,5 T selisihnya sekitar 224 Hz, dan pada 3 T sekitar 447 Hz.\n\n" +
                        "Rumus: Δf (Hz) = 3,5 ppm × f_Larmor (MHz)\n\n" +
                        "Asal fisika: Awan elektron di sekitar inti ikut meredam medan yang dirasakan proton (efek perisai / electron shielding). Proton air dan proton lemak terikat pada molekul berbeda, sehingga derajat perisainya berbeda, dan medan yang dirasakan sedikit berbeda. Perbedaan itu sebanding dengan B0, maka selisih frekuensinya juga sebanding dengan B0, dan dalam ppm nilainya tetap. Proton lemak lebih terperisai, jadi frekuensinya lebih rendah.",
                    analogi = "Analogi: Seperti dua jam yang selisihnya selalu 1% dari kecepatannya: makin cepat jamnya, makin besar selisih detiknya.",
                    prerequisite = "M05.T2.4, M06B.T1.1",
                    learningGoal = "Menjelaskan asal fisika perisai elektron pada pergeseran kimia lemak-air serta menghitung selisih frekuensi dalam Hz pada 1,5 T dan 3 T.",
                    crossLinks = "▶ Simulasi A6 · Spektrum pergeseran kimia | → M05 A2 (Kalkulator ppm) | → M08A (Artefak pergeseran kimia)",
                    labType = "m6b_a6_chemical_shift",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M06B.T3.2",
                    level = 3,
                    title = "T3.2 Ketidakhomogenan B1 dan transmit paralel",
                    summaryShort = "Pada B0 tinggi, panjang gelombang RF di jaringan memendek (~50 cm di 1,5 T vs ~26 cm di 3 T) sehingga B1 tidak merata. Transmit paralel membantu meratakannya.",
                    detailFull = "Ketidakhomogenan B1 dan efek dielektrik: Pulsa RF tidak selalu merata di seluruh tubuh. Pada B0 yang lebih tinggi, frekuensi RF naik dan panjang gelombangnya di jaringan makin pendek. Bila panjang gelombang sebanding dengan lebar tubuh, gelombang saling menguatkan di sebagian tempat dan melemahkan di tempat lain, sehingga muncul daerah yang lebih terang atau lebih gelap.\n\n" +
                        "Sebagai perkiraan kasar, jaringan diperlakukan seperti air: panjang gelombang di jaringan kira-kira 50 cm pada 1,5 T dan 26 cm pada 3 T, dibanding lebar badan sekitar 35 cm.\n\n" +
                        "Transmit paralel (pTx): Secara konsep, beberapa elemen pemancar diatur terpisah (amplitudo dan fase tiap elemen) agar medan B1 lebih merata di seluruh penampang tubuh.",
                    analogi = "Analogi: Seperti satu lampu di ruangan besar yang menyisakan sudut gelap. Beberapa lampu kecil yang diatur sendiri-sendiri meratakan cahaya.",
                    prerequisite = "M05.T3.1, M06B.T2.3",
                    learningGoal = "Menjelaskan hubungan antara pemendekan panjang gelombang RF pada 3 T dengan ketidakhomogenan B1 serta prinsip transmit paralel.",
                    crossLinks = "▶ Simulasi A7 · Peta B1 dan transmit paralel | → M07C (SAR & efek antena)",
                    labType = "m6b_a7_b1_ptx",
                    sortOrder = 9
                )
            )
        ),
        MriModule(
            id = "M07A",
            code = "M07A",
            title = "Keselamatan MR: Zona, Skrining & Perangkat",
            subtitle = "4 Zona ACR, 3 Level Personel, Label ASTM, Psikologis & Agen Kontras",
            description = "Tujuan belajar M07A:\n• Menjelaskan empat zona keselamatan dan tiga tingkat personel\n• Menjalankan alur skrining pasien dengan atau tanpa implan\n• Membaca label keselamatan perangkat (standar ASTM)\n• Mengenali skrining keselamatan sebelum agen kontras",
            levelBadge = "T1 - T2 Wajib",
            iconName = "shield",
            nodes = listOf(
                MriNode(
                    code = "M07A.T1.1",
                    level = 1,
                    title = "Empat zona keselamatan",
                    summaryShort = "Fasilitas MRI dibagi menjadi empat zona. Makin besar nomornya, makin ketat aksesnya. Medan magnet utama (B0) selalu menyala.",
                    detailFull = "Zona I untuk umum. Zona II adalah area antara, tempat pasien diterima dan ditanyai. Zona III terkontrol: hanya orang yang sudah diskrining. Zona IV adalah ruang magnet. Setiap orang diskrining sebelum masuk.\n\n• Benda ferromagnetik bisa tertarik dan menjadi proyektil.\n• Magnet tetap menyala walau tidak ada pemindaian.",
                    analogi = "Zona seperti lapisan keamanan di bandara. Makin dalam, makin ketat pemeriksaannya. Magnetnya seperti arus sungai yang tidak pernah berhenti: verifikasi ketat terhadap objek ferromagnetik mutlak dilakukan sebelum melintasi batas zona.",
                    prerequisite = "M01",
                    learningGoal = "Menjelaskan pembagian Zona I hingga Zona IV pada fasilitas MRI serta fakta bahwa B0 selalu menyala.",
                    crossLinks = "▶ Simulasi: A1 Denah zona dan akses personel",
                    labType = "m7a_zona_akses",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M07A.T1.2",
                    level = 1,
                    title = "Prinsip keselamatan & analisis risiko-manfaat",
                    summaryShort = "Setiap kasus berbeda dan dinilai sendiri-sendiri. Pasien tidak dikeluarkan tanpa alasan dari pemeriksaan yang ia butuhkan, tetapi risikonya tetap ditimbang.",
                    detailFull = "Untuk pasien dengan implan, perlu analisis risiko dan manfaat. Tanggung jawab utama ada pada dokter perujuk dan radiology lead. Praktisi MRI juga boleh memutuskan lanjut atau menunda sampai bukti keselamatan cukup, sebab hal baru seperti tindik bisa muncul saat pasien datang. Saran fisikawan medis dan ahli lain boleh diminta. Karena perangkat baru terus bermunculan, pengetahuan perlu diperbarui.",
                    analogi = "Seperti menimbang obat: manfaatnya nyata, tetapi efek sampingnya tetap dihitung kasus per kasus.",
                    prerequisite = "M07A.T1.1",
                    learningGoal = "Menjelaskan pembagian tanggung jawab analisis risiko-manfaat dan wewenang praktisi MRI menunda pemeriksaan bila bukti dokumen kurang.",
                    crossLinks = "▶ Simulasi: A2 Simulasi skrining",
                    labType = "m7a_skrining",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M07A.T1.3",
                    level = 1,
                    title = "Lima sumber bahaya pada pemindai",
                    summaryShort = "Bahaya MRI berasal dari lima hal: faktor psikologis, medan statis yang berubah menurut jarak, medan RF, gradien yang berubah menurut waktu, dan kriogen.",
                    detailFull = "Sebagian efeknya sementara dan tidak berdampak jangka panjang, tetapi sebagian dapat menyebabkan cedera berat atau kematian. Bagian ini hanya pengantar umum, bukan panduan keselamatan lengkap. Pemindai, kekuatan medan, koil, lebar bore, tubuh pasien, dan implan sangat beragam. Implan bahkan bisa aman hanya di bawah kekuatan medan, gradien ruang, atau SAR tertentu. Keamanan zat kontras berbasis gadolinium bukan bahaya fisik MR dan dibahas terpisah di T2.6. Rincian: medan statis dan kriogen di M07B, RF dan gradien di M07C.",
                    analogi = "Seperti peta bahaya sebuah pabrik: ada lima titik rawan, dan tiap titik punya cara pencegahannya sendiri.",
                    prerequisite = "M07A.T1.1",
                    learningGoal = "Mengidentifikasi kelima sumber bahaya fisik dan psikologis pada pemindai MRI.",
                    crossLinks = "▶ Simulasi: A4 Lima sumber bahaya pada pemindai",
                    labType = "m7a_lima_bahaya",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M07A.T2.1",
                    level = 2,
                    title = "Zona menurut ACR & lampu peringatan",
                    summaryShort = "ACR menyarankan fasilitas MRI dizonakan menurut risiko, agar orang yang tidak berwenang tidak masuk ke area bermedan magnet kuat.",
                    detailFull = "Zona I adalah area umum. Zona II adalah batas antara area umum dan zona terkontrol: pasien disambut dan tidak bebas bergerak, dan jawaban skrining serta riwayat diambil di sini. Zona III adalah tempat orang tanpa skrining atau benda ferromagnetik dapat menyebabkan cedera berat, jadi aksesnya dibatasi fisik, misalnya dengan kunci atau sistem passkey yang bisa membedakan personel MRI dari non-MRI. Zona IV adalah ruang magnet. Ia ditandai lampu merah dan tanda The Magnet is On yang menyala terus (kecuali sistem resistif), dengan cadangan energi sedikitnya 24 jam bila listrik padam.",
                    analogi = "Seperti zona di rumah sakit isolasi: tiap pintu punya aturan dan penjaganya sendiri.",
                    prerequisite = "M07A.T1.1",
                    learningGoal = "Menjelaskan pembatasan akses fisik Zona III dan ketentuan lampu peringatan 'The Magnet is On' dengan cadangan daya minimal 24 jam.",
                    crossLinks = "▶ Simulasi: A1 Denah zona | → M01 T1.1 (Kontraindikasi sebagai pendahuluan)",
                    labType = "m7a_zona_akses",
                    sortOrder = 4
                ),
                MriNode(
                    code = "M07A.T2.2",
                    level = 2,
                    title = "Tiga tingkat personel (Non-MRI, Level 1, Level 2)",
                    summaryShort = "Ada personel non-MRI, personel Level 1, dan personel Level 2.",
                    detailFull = "Non-MRI: pasien, pengunjung, atau staf fasilitas yang belum menjalani pelatihan keselamatan formal dalam 12 bulan terakhir. Level 1: staf kantor dan pembantu pasien yang lulus pendidikan keselamatan minimal agar aman bekerja di Zona III. Level 2: teknolog MRI, radiolog, dan perawat yang dilatih mendalam, termasuk soal pemanasan, luka bakar, dan rangsangan saraf-otot akibat gradien.",
                    analogi = "Seperti tingkat izin keamanan: tamu, staf umum, dan ahli yang bertanggung jawab.",
                    prerequisite = "M07A.T2.1",
                    learningGoal = "Membedakan kualifikasi dan hak akses personel Non-MRI, Level 1, dan Level 2.",
                    crossLinks = "▶ Simulasi: A1 Akses personel",
                    labType = "m7a_zona_akses",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M07A.T2.3",
                    level = 2,
                    title = "Keselamatan perangkat: standar ASTM & label implan",
                    summaryShort = "Perangkat dibagi menjadi MR Safe, MR Conditional, dan MR Unsafe. Ikonnya: kotak hijau, segitiga kuning, dan lingkaran merah yang dicoret.",
                    detailFull = "MR Safe: tidak ada bahaya yang diketahui di semua lingkungan MR, misalnya cawan petri plastik. MR Conditional: terbukti aman pada lingkungan MR dan syarat tertentu, seperti kekuatan medan statis, gradien ruang, laju perubahan medan (dB/dt), medan RF, SAR, dan pengaturan kabel. Pengujiannya mencakup gaya tarik, torsi, dan pemanasan RF, dan hasilnya tercantum pada label. Setiap parameter yang memengaruhi keselamatan harus dicantumkan, begitu pula kondisi yang diketahui menghasilkan keadaan tidak aman. Isu lain yang diperhatikan: cedera termal, arus atau tegangan induksi, kompatibilitas elektromagnetik, perangsangan saraf, bising, interaksi antarperangkat, fungsi aman perangkat, dan pengoperasian aman sistem MR. Status MR Safe dapat ditetapkan lewat alasan ilmiah, tidak harus dengan data uji. MR Unsafe: diketahui berbahaya di semua lingkungan MR, misalnya gunting ferromagnetik. Perangkat yang aman bersyarat pada 1 T belum tentu aman pada medan yang lebih tinggi.\n\nRingkasan label:\n• MR Safe: Tidak menimbulkan bahaya di lingkungan MR.\n• MR Conditional: Aman hanya bila syarat pada label dipenuhi.\n• MR Unsafe: Berbahaya di lingkungan MR.\nSyarat dibaca dari label atau dokumen implan, bukan ditebak.",
                    analogi = "Seperti lampu lalu lintas: hijau jalan, kuning hati-hati dengan syarat, merah berhenti.",
                    prerequisite = "M07A.T1.2",
                    learningGoal = "Membaca ketiga label keselamatan ASTM (MR Safe, MR Conditional, MR Unsafe) dan mengevaluasi syarat keamanannya.",
                    crossLinks = "▶ Simulasi: A2 Simulasi skrining, A3 Label perangkat dan alur keputusan",
                    labType = "m7a_label_alur",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M07A.T2.4",
                    level = 2,
                    title = "Efek psikologis & klaustrofobia",
                    summaryShort = "Desain bore tertutup dapat memicu cemas dan klaustrofobia. Kecemasan biasanya paling tinggi di awal, saat pasien dimasukkan ke bore, lalu menurun.",
                    detailFull = "Pada studi tahun 1998, 14,3 persen pasien membutuhkan sedasi atau anestesi, sebagian besar untuk pemeriksaan otak. Pemindai tertutup awal memakai bore yang sangat panjang agar volume citra homogen, sehingga pasien nyaris seluruhnya berada di dalam bore. Pada pemeriksaan otak, kepala berada di isosenter dengan atap bore dekat wajah. Faktor lainnya: koil kepala, rasa terisolasi, lama pemindaian, bising, dan takut hasil yang buruk. Pasien juga dapat takut sesak, takut pingsan, atau mengalami serangan panik. Ketidakpatuhan berdampak nyata: sulit diam atau menahan napas merusak citra (artefak pergeseran fase), dan serangan panik dapat menghentikan pemeriksaan sebelum ada citra. Pasien lalu dirujuk ke CT. Pada pencitraan jantung, 42 persen pasien lebih memilih CT dan hanya 12 persen memilih MRI. Waktu utilisasi pesawat MRI terbuang, biaya naik, dan jadwal tertunda. Cara menenangkan: bore lebih pendek dan lebar (kejadian klaustrofobia turun sekitar 3 kali), kipas, lampu dan panel pemandangan, video, hitung mundur, dan pada pemindai terbuka kerabat dapat duduk dekat pasien. Manfaat aromaterapi belum pasti. Anak kecil atau pasien yang sangat klaustrofobia mungkin butuh sedasi dan pemantauan tambahan. Kabel pemantau tidak boleh menempel pada kulit pasien yang dibius.",
                    analogi = "Seperti masuk air dingin: bagian paling berat adalah saat pertama kali masuk.",
                    prerequisite = "M07A.T1.3",
                    learningGoal = "Menjelaskan perjalanan kecemasan pasien selama pemindaian dan strategi menurunkan klaustrofobia.",
                    crossLinks = "▶ Simulasi: A5 Perjalanan kecemasan pasien",
                    labType = "m7a_kecemasan",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M07A.T2.5",
                    level = 2,
                    title = "Tips keselamatan praktis",
                    summaryShort = "Jangan berasumsi. Periksa staf seketat pasien, dan jangan pernah merasa wajib memindai bila ragu.",
                    detailFull = "• Jangan anggap staf tahu bahaya ruang magnet, dan hindari kunci kode digital untuk Zona III.\n• Jangan anggap pasien yang dipindai di tempat lain kemarin aman hari ini: medan dan gradien bisa berbeda, dan kondisi pasien bisa berubah.\n• Tanyakan kontraindikasi sebelum jadwal, beri informasi yang cukup, dan tanyakan juga secara lisan selain formulir.\n• Untuk pasien yang sulit memberi riwayat, tanyakan keluarga dan ganti pakaian dengan baju pasien. Citra rontgen terbaru dapat membantu menyingkirkan pacu jantung atau klip aneurisma.\n• Beri stiker peringatan pada benda ferromagnetik besar dan pilih alternatif non-ferrous, misalnya tangga pijakan dan alat pemadam.\n• Bila ragu, tunda pemeriksaan.\n\nKorban jiwa terkait MRI yang dilaporkan sangat sedikit, kurang dari satu per tahun, berkat kewaspadaan staf dan dokter perujuk.",
                    analogi = "Seperti daftar periksa pilot: bukan karena tidak percaya, tetapi karena taruhannya besar.",
                    prerequisite = "M07A.T1.2, M07A.T2.1",
                    learningGoal = "Menerapkan prinsip kehati-hatian praktis dalam skrining staf, pasien, dan peralatan ruang MRI.",
                    crossLinks = "▶ Simulasi: A6 Benar atau keliru? Tips keselamatan",
                    labType = "m7a_tips",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M07A.T2.6",
                    level = 2,
                    title = "Keselamatan agen kontras gadolinium",
                    summaryShort = "Kontras berbasis gadolinium umumnya aman, tetapi pasien perlu diskrining sebelum disuntik: fungsi ginjal, kehamilan, dan riwayat alergi. Ini keselamatan obat, bukan bahaya fisik MR.",
                    detailFull = "Pada pasien dengan gangguan ginjal berat atau cedera ginjal akut, gadolinium dapat berkaitan dengan nephrogenic systemic fibrosis (NSF), pengerasan kulit dan jaringan yang dapat berat. Karena itu fungsi ginjal (eGFR) diperiksa pada pasien berisiko, misalnya usia lanjut, penyakit ginjal, diabetes, atau hipertensi. Risiko berbeda antar jenis agen, dan fasilitas biasanya memilih agen dengan risiko terendah bila ginjal terganggu.\n\nGadolinium juga dapat tertahan (retensi) di jaringan tubuh, termasuk otak, bahkan pada orang dengan ginjal normal. Makna klinisnya belum pasti. Prinsipnya: kontras diberikan bila manfaat diagnostiknya jelas, dengan dosis seperlunya.\n\nKehamilan: gadolinium melewati plasenta, jadi kontras dihindari kecuali manfaatnya jelas lebih besar daripada risikonya, dan keputusannya ada pada radiolog dan dokter perujuk. Untuk ibu menyusui, ikuti SOP fasilitas.\n\nReaksi alergi terhadap kontras jarang dan umumnya ringan, tetapi riwayat reaksi sebelumnya menaikkan peluangnya. Tim perlu siap menangani reaksi.",
                    analogi = "Seperti memeriksa saluran pembuangan sebelum menuang air: bila ginjal (saluran) bermasalah, zat tidak terbuang dengan baik dan bisa menumpuk.",
                    prerequisite = "M07A.T1.2",
                    learningGoal = "Menjelaskan alur skrining fungsi ginjal (eGFR/NSF), kehamilan, dan riwayat alergi sebelum pemberian kontras gadolinium.",
                    crossLinks = "▶ Simulasi: A7 Alur skrining sebelum agen kontras",
                    labType = "m7a_kontras",
                    sortOrder = 9
                )
            )
        ),
        MriModule(
            id = "M07B",
            code = "M07B",
            title = "Keselamatan MR: Medan Statis (B0), Proyektil & Kriogen",
            subtitle = "Garis 5 Gauss, Torsi Implan, Efek Lenz, Asfiksia Helium & Prosedur Quench",
            description = "Tujuan belajar M07B:\n• Menjelaskan bahaya proyektil, torsi, dan efek sementara medan statis\n• Menjelaskan empat kekhawatiran kriogen dan mengapa quench bukan prosedur biasa\n• Mengenali bahaya benda asing dan efek Lenz",
            levelBadge = "T2 - T3 Fisika",
            iconName = "magnet",
            nodes = listOf(
                MriNode(
                    code = "M07B.T2.1",
                    level = 2,
                    title = "Medan statis dan proyektil",
                    summaryShort = "Gaya tarik makin kuat di dekat magnet. Garis 5 gauss (0,5 mT) menandai batas pengaman. Sebagian fasilitas memakai 0,9 mT sesuai kebijakan.",
                    detailFull = "Benda ferromagnetik dapat melesat ke arah bore. Implan logam dapat mengalami gaya dan putaran (torsi). Orang dengan perangkat tertentu perlu tetap di luar garis ini.",
                    analogi = "Garis 5 gauss seperti garis kuning di tepi peron: tidak ada pagar, tetapi melewatinya punya konsekuensi.",
                    prerequisite = "M07A.T1.3",
                    learningGoal = "Menjelaskan arti batas garis pengaman 5 gauss (0,5 mT) dan bahaya efek proyektil ferromagnetik.",
                    crossLinks = "▶ Simulasi: A1 Medan tepi dan gaya tarik",
                    labType = "m7b_medan_tepi",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M07B.T2.2",
                    level = 2,
                    title = "Siapa yang sering terlibat insiden proyektil",
                    summaryShort = "Insiden sering melibatkan staf non-MRI yang masuk ke ruang magnet tanpa pengawasan, misalnya petugas kebersihan, porter, atau teknisi anestesi.",
                    detailFull = "Contohnya tabung oksigen dan mesin pemoles lantai. Pelajarannya: zonasi, pintu fisik yang terkunci, dan pelatihan semua staf rumah sakit. Kantong imobilisasi berisi butiran baja juga bisa menjadi proyektil, dan benda kecil seperti koin atau jepit rambut yang tertinggal di bore dapat mengganggu kualitas citra.",
                    analogi = "Seperti ruang server: bukan hanya tamu, orang dalam yang belum terlatih pun perlu dibatasi aksesnya.",
                    prerequisite = "M07B.T2.1",
                    learningGoal = "Mengidentifikasi kelompok personel dan jenis benda yang paling sering terlibat dalam insiden proyektil.",
                    crossLinks = "▶ Simulasi: A1 Medan tepi dan gaya tarik",
                    labType = "m7b_medan_tepi",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M07B.T2.3",
                    level = 2,
                    title = "Medan tepi berperisai aktif",
                    summaryShort = "Magnet modern berperisai aktif menjaga medan tepi tetap kecil. Akibatnya medan turun sangat curam: tarikan hampir tidak terasa sampai benda sudah dekat.",
                    detailFull = "Gaya pada benda ferromagnetik sebanding dengan kuat medan dikali kemiringan (gradien) medan statis. Pada sistem berperisai, garis 0,5 mT hanya beberapa meter dari magnet. Kursi roda ferromagnetik pernah baru tertarik ketika pasien berdiri dan mendekati ujung bore. Pada sistem tanpa perisai, tarikan sudah terasa dari jauh sehingga masih ada waktu bertindak. Pencegahannya: kewaspadaan, penghalang fisik, tanda peringatan, dan pelatihan staf. Peralatan pemantau harus yang bersertifikat MR, sebab bodi plastik bisa menutupi rangka ferromagnetik.",
                    analogi = "Seperti tebing di ujung dataran: jalannya rata dan tenang, lalu tiba-tiba curam.",
                    prerequisite = "M07B.T2.1, M05.T2.5",
                    learningGoal = "Menjelaskan mengapa gradien ruang medan tepi pada magnet berperisai aktif memicu tarikan mendadak di dekat bore.",
                    crossLinks = "▶ Simulasi: A1 Medan tepi dan gaya tarik",
                    labType = "m7b_medan_tepi",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M07B.T2.4",
                    level = 2,
                    title = "Efek biologis sementara dari B0",
                    summaryShort = "B0 yang kuat dapat menimbulkan efek sementara: rasa logam di mulut, pusing berputar (vertigo), dan kilatan cahaya (fosfen). Efek ini umumnya hilang setelah keluar dari magnet.",
                    detailFull = "Efek muncul saat tubuh bergerak melewati medan tepi. Vertigo dikaitkan dengan gaya Lorentz pada ion di cairan telinga dalam, dan terutama muncul pada medan sangat tinggi (7 T ke atas). Kilatan cahaya berkaitan dengan induksi Faraday pada saraf optik. Makin cepat meja bergerak, makin berat vertigo, jadi meja digerakkan perlahan. Komite ICNIRP menyatakan tidak ada bukti efek kesehatan serius pada paparan seluruh tubuh sampai 8 T.",
                    analogi = "Seperti naik perahu: telinga dalam perlu waktu menyesuaikan, dan gerakan cepat membuatnya makin terasa.",
                    prerequisite = "M07B.T2.1",
                    learningGoal = "Menjelaskan mekanisme efek biologis sementara medan statis (rasa logam, vertigo, fosfen) dan cara menguranginya.",
                    crossLinks = "▶ Simulasi: A3 Skala medan dan efek sementara",
                    labType = "m7b_skala_efek",
                    sortOrder = 4
                ),
                MriNode(
                    code = "M07B.T2.5",
                    level = 2,
                    title = "Torsi pada implan",
                    summaryShort = "Implan ferromagnetik dapat berputar sampai sejajar garis medan. Gaya putar ini disebut torsi.",
                    detailFull = "Klip aneurisma yang panjang bekerja seperti tuas dan dapat merusak jaringan di sekitarnya. Implan yang tertambat pada tulang atau semen, misalnya paku ortopedi dan sendi pinggul, kecil kemungkinan bergerak. Fibrosis (jaringan parut) dapat menahan implan, tetapi tidak terjadi di semua jaringan, misalnya otak. Bila jenis implan tidak bisa dipastikan dengan bukti dokumen, pemeriksaan ditunda atau dibatalkan. Keterangan lisan saja tidak cukup. Alat jantung modern tertentu bersifat MR Conditional dan perlu diprogram ke mode aman-MR di bawah tim kardiologi.",
                    analogi = "Seperti jarum kompas: ia berputar sampai sejajar dengan medan.",
                    prerequisite = "M07B.T2.1",
                    learningGoal = "Menjelaskan mekanisme gaya putar (torsi) pada implan memanjang di jaringan lunak vs implan yang tertambat pada tulang.",
                    crossLinks = "▶ Simulasi: A2 Torsi pada implan | → M07C T2.5 (Arus induksi pada implan)",
                    labType = "m7b_torsi",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M07B.T2.6",
                    level = 2,
                    title = "Benda asing intraokular dan efek Lenz",
                    summaryShort = "Benda asing ferromagnetik dapat bergerak oleh gaya tarik dan gaya putar, seperti implan. Risiko terbesar bila gerakannya menyebabkan perdarahan atau merusak struktur halus, misalnya di orbita mata.",
                    detailFull = "Pecahan logam berkecepatan tinggi pernah menembus bola mata dan, pada sedikitnya dua kasus, berinteraksi dengan medan statis hingga menyebabkan kebutaan satu mata atau katarak. Pada satu kasus pasien tidak tahu ada serpihan logam. Pada kasus lain pasien tahu, tetapi foto rontgen dilaporkan normal dan pemindaian dilanjutkan. Cara skrining masih diperdebatkan karena rontgen memberi dosis pada lensa dan pemeriksaan yang negatif menambah biaya. Namun pasien tetap diskrining untuk benda asing intraokular, dan riwayat lisannya dihargai sama seperti isian kotak centang. Pasien dengan riwayat cedera mata tembus perlu dipertimbangkan untuk rontgen polos bila tidak ada citra terbaru. Logam non-ferrous pun dapat mengalami torsi lewat efek Lenz: arus induksi selalu melawan perubahan medan yang menimbulkannya. Pada uji in vitro, fungsi katup jantung logam jenis lama dapat terhambat saat bergerak sepanjang gradien medan statis.",
                    analogi = "Seperti serpihan di dalam sepatu: kecil, tetapi bila di tempat yang salah akibatnya besar.",
                    prerequisite = "M07B.T2.5",
                    learningGoal = "Menjelaskan risiko serpihan logam intraokular dan hambatan gerak konduktor non-ferrous akibat efek Lenz.",
                    crossLinks = "▶ Simulasi: A5 Efek Lenz: konduktor jatuh melalui medan",
                    labType = "m7b_lenz",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M07B.T2.7",
                    level = 2,
                    title = "Kriogen: empat kekhawatiran dan quench",
                    summaryShort = "Magnet superkonduktor didinginkan dengan helium cair. Ada empat kekhawatiran: suhu sangat rendah, asfiksia, quench, dan ledakan. Quench adalah hilangnya superkonduktivitas secara tiba-tiba.",
                    detailFull = "Helium cair mendidih pada 4 K di atas nol mutlak, jadi percikannya merusak jaringan, terutama mata dan kulit. Helium hanya ditangani teknisi terlatih dengan APD dan pelindung wajah, dalam dewar khusus yang disimpan tegak. Bila terkena, segera cari bantuan medis. Helium tidak berbau dan tidak beracun, tetapi menggantikan oksigen. Udara biasa berisi sekitar 21 persen oksigen. OSHA menetapkan rentang optimal 19,5 sampai 23,5 persen, dan ruang magnet punya sensor oksigen dengan alarm. Di bawah 8 persen orang cenderung pingsan. Di bawah 6 persen pingsan dalam sekitar 10 detik, dan gagal jantung hingga kematian terjadi dalam 2 sampai 4 menit. Nitrogen berbahaya dengan cara yang sama. Aturan emas pertolongan pertama: pastikan keselamatan diri sendiri dahulu.\n\nQuench adalah hilangnya superkonduktivitas secara tiba-tiba. Saat itu helium cair mendidih menjadi gas dalam jumlah besar dan dibuang lewat saluran khusus. Bila gas bocor ke ruangan, ia menggantikan oksigen .",
                    analogi = "Seperti air yang naik tanpa terlihat: tidak berbau dan tidak terasa, tetapi mendesak volume oksigen pernapasan di dalam ruangan. Quench seperti tutup panci tekanan yang terbuka mendadak: uap keluar sekaligus.",
                    prerequisite = "M05.T2.6",
                    learningGoal = "Menjelaskan empat bahaya kriogen (radang dingin 4 K, asfiksia O2 < 19,5%, quench, ledakan) serta ambang kritis oksigen.",
                    crossLinks = "▶ Simulasi: A4 Oksigen dan ekspansi helium | → M05 T2.1–T2.6",
                    labType = "m7b_oksigen_helium",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M07B.T3.1",
                    level = 3,
                    title = "Pipa quench, tekanan pintu, dan ledakan",
                    summaryShort = "Pipa quench membuang gas helium ke luar gedung. Bila gagal, tekanan ruang magnet dapat naik, atau kriostat dapat meledak.",
                    detailFull = "Kriostat yang penuh dapat berisi lebih dari 1600 L helium cair, dan tiap liter mengembang menjadi 754 L gas. Pipa dapat pecah, misalnya karena sumbatan es dari air hujan. Helium lebih ringan dari udara, jadi gas naik ke ruang tertinggi gedung dan gedung perlu dievakuasi. Kenaikan tekanan mendadak menekan pintu yang membuka ke dalam. Menyamakan tekanan, misalnya dengan memecahkan kaca ruang kontrol, mempercepat akses. Titik buang luar sedikitnya 8 m dari area akses umum. Ledakan kriostat pernah terjadi ketika katup piringan pecah (burst disk) gagal bekerja, jadi inspeksi servis dan prosedur dekomisioning penting. Penolong pun bisa pingsan, jadi waspadalah, terutama bila bekerja sendirian.",
                    analogi = "Seperti kaleng soda yang dikocok: bila tutupnya tidak melepas tekanan, kaleng bisa meledak.",
                    prerequisite = "M07B.T2.7",
                    learningGoal = "Menghitung ekspansi gas helium (1 L cair = 754 L gas) dan menjelaskan bahaya kenaikan tekanan ruangan terhadap pintu yang membuka ke dalam.",
                    crossLinks = "▶ Simulasi: A4 Oksigen dan ekspansi helium",
                    labType = "m7b_oksigen_helium",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M07B.T3.2",
                    level = 3,
                    title = "Quench bukan prosedur darurat medis biasa",
                    summaryShort = "Pasien yang sakit mendadak dikeluarkan dan ditangani di luar Zona IV. Quench tidak dipakai untuk itu.",
                    detailFull = "Quench hanya untuk ancaman nyawa akibat medan, misalnya orang terjepit benda ferromagnetik. Semua staf baru perlu tahu letak tombol quench, dan rangkaiannya diperiksa berkala oleh teknisi servis.",
                    analogi = "Seperti rem darurat kereta: dipakai hanya bila ada bahaya langsung.",
                    prerequisite = "M07B.T2.7, M07B.T3.1",
                    learningGoal = "Membedakan prosedur henti jantung/medis (keluarkan dari Zona IV tanpa quench) dari darurat terjepit ferromagnetik (quench).",
                    crossLinks = "▶ Simulasi: A6 Skenario darurat: pilih tindakan | → M05 A7",
                    labType = "m7b_darurat",
                    sortOrder = 9
                )
            )
        ),
        MriModule(
            id = "M07C",
            code = "M07C",
            title = "Keselamatan MR: RF (SAR), Gradien & Bising",
            subtitle = "Batas SAR IEC, Luka Bakar Antena/Loop, Stimulasi Saraf Tepi & Desibel",
            description = "Tujuan belajar M07C:\n• Menjelaskan SAR, bagian tubuh yang rentan panas, dan cara menurunkannya\n• Menjelaskan mekanisme luka bakar (antena, loop konduktif) dan arus induksi\n• Menjelaskan stimulasi saraf oleh gradien dan batas paparan bising",
            levelBadge = "T2 - T3 Klinis",
            iconName = "warning",
            nodes = listOf(
                MriNode(
                    code = "M07C.T2.1",
                    level = 2,
                    title = "MRI memakai radiasi non-pengion",
                    summaryShort = "Pernyataan \"MRI tidak memakai radiasi\" kurang tepat. MRI memakai radiasi non-pengion: energinya terlalu rendah untuk memutus ikatan kimia atau menyebabkan mutasi DNA.",
                    detailFull = "Pada radiografi konvensional, bahaya berpusat pada radiasi pengion dan efek stokastik (acak, menaikkan peluang kanker di masa depan) serta efek deterministik (sebab-akibat jelas seperti eritema atau rambut rontok). Energi foton sebanding dengan frekuensinya dan berbanding terbalik dengan panjang gelombangnya. Sinar-X berpanjang gelombang kurang dari 10 nm. MRI memakai RF pada frekuensi Larmor. Pada 1 T frekuensinya 42,6 MHz (panjang gelombang sekitar 7 m), dan pada 1,5 T 63,9 MHz (4,7 m). Walau non-pengion, RF tidak sepenuhnya aman. Ada tiga bahaya utama: pemanasan atau luka bakar, efek antena, dan gangguan fungsi implan akibat arus induksi.",
                    analogi = "Seperti cahaya lampu dan sinar-X: sama-sama gelombang elektromagnetik, tetapi energinya jauh berbeda.",
                    prerequisite = "M01.T1.7",
                    learningGoal = "Menjelaskan mengapa gelombang RF MRI termasuk radiasi non-pengion beserta tiga risiko utamanya.",
                    crossLinks = "→ M01 T1.7 (rumah konsep radiasi non-pengion)",
                    labType = null,
                    sortOrder = 1
                ),
                MriNode(
                    code = "M07C.T2.2",
                    level = 2,
                    title = "RF dan pemanasan (SAR): organ rentan panas",
                    summaryShort = "Energi RF yang diserap jaringan menjadi panas. SAR (W/kg) mengukur laju penyerapannya per kilogram jaringan, sehingga berat pasien dimasukkan ke komputer sebelum pemindaian. Seluruh tubuh: mode normal 2 W/kg, mode tingkat satu 4 W/kg. Kepala mode normal: 3,2 W/kg.",
                    detailFull = "Kalibrasi pra-pindai memakai semburan RF pendek untuk membuat sudut flip 90 dan 180 derajat. Energi semua pulsa dalam satu sekuens dijumlahkan, dibagi TR, lalu dibagi massa pasien untuk mendapat SAR seluruh tubuh. Lensa mata sangat peka karena tidak punya aliran darah untuk membuang panas. Sel kelamin pria juga dapat terganggu: pada hewan, ketidaksuburan sementara muncul pada SAR sekitar 6 W/kg, tingkat yang menaikkan suhu testis sekitar 3,5 °C. Karena itu batas SAR berbeda untuk badan, kepala, dan ekstremitas. Standar tiap negara bisa berbeda, misalnya pedoman FDA sedikit berbeda dari standar Eropa.\n\nLuka bakar paling sering akibat loop konduktif, misalnya kabel melingkar atau kulit yang saling bersentuhan sehingga membentuk lingkaran (lihat T2.4).",
                    analogi = "Seperti kompor kecil: panasnya bergantung pada daya dan pada massa yang dipanaskan, sebab itu berat badan dimasukkan.",
                    prerequisite = "M07C.T2.1",
                    learningGoal = "Menjelaskan definisi SAR (W/kg), batas mode normal/tingkat satu, serta kerentanan lensa mata dan testis terhadap panas.",
                    crossLinks = "▶ Simulasi: A1 Kalkulator SAR",
                    labType = "m7c_sar",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M07C.T2.3",
                    level = 2,
                    title = "Cara menurunkan SAR & komprominya",
                    summaryShort = "Bila SAR melebihi batas, protokol dapat diubah agar RF yang diberikan berkurang. Tiap langkah punya konsekuensi.",
                    detailFull = "• Medan lebih rendah: energi turun, tetapi SNR turun dan NSA lebih besar membuat waktu minimal dua kali lipat. Biasanya tidak praktis.\n• Hapus atau kurangi pulsa presaturasi: dapat menambah artefak pergeseran fase.\n• Perpanjang TR: pulsa per satuan waktu berkurang, tetapi pembobotan berubah, terutama pada T1.\n• Kurangi jumlah irisan: cakupan berkurang atau perlu akuisisi tambahan sehingga waktu naik.\n• Pakai koil penerima yang berbeda: koil kuadratur lebih efisien daripada koil linear, tanpa konsekuensi bila dipilih sejak awal.\n• Hindari TSE atau FSE, atau perpendek rangkaian gema: energi naik dengan kuadrat sudut flip, jadi pulsa 180 derajat memberi empat kali energi pulsa 90 derajat.\n• Kecilkan sudut flip pulsa refokus: SAR turun, kualitas citra sedikit menurun tetapi jarang membuatnya tidak diagnostik.",
                    analogi = "Seperti mengatur kompor: kecilkan api, perpanjang jeda, atau kurangi jumlah panci.",
                    prerequisite = "M07C.T2.2",
                    learningGoal = "Menganalisis strategi menurunkan SAR beserta kompromi kualitas citra dan waktu akuisisinya.",
                    crossLinks = "▶ Simulasi: A1 Kalkulator SAR | → M09 T2.3",
                    labType = "m7c_sar",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M07C.T2.4",
                    level = 2,
                    title = "Luka bakar, loop konduktif, dan efek antena",
                    summaryShort = "Luka bakar dapat muncul pada loop alami di tubuh dan pada kabel logam yang beresonansi seperti antena. Pasien yang dibius tidak dapat melaporkan rasa panas.",
                    detailFull = "Tangan yang menempel pada sisi tungkai atau tumit yang bersentuhan membentuk sirkuit biologis. Arus induksi terkonsentrasi di area kulit yang bersentuhan, yang hanya beberapa sentimeter persegi, sehingga panasnya tajam. Hal serupa dapat terjadi antara lutut atau betis, jadi produsen menganjurkan penempatan pasien dan bantalan isolasi untuk memutus loop. Pada efek antena, konduktor dengan panjang kritis bertindak sebagai antena. Dipol paling efektif berpanjang setengah panjang gelombang, dan gelombang berdiri membuat ujung kawat (antinode) menerima energi terbesar. Uji in vitro pada kawat pacu jantung menunjukkan suhu ujungnya naik hingga 80 °C. Kawat penghantar harus dijauhkan dari kulit, terutama pada pasien yang dibius. Satu kasus tragis: gelang oksimeter nadi yang tidak aman-MR memanas pada bayi berusia lima minggu di bawah anestesi, menyebabkan luka bakar derajat empat dan amputasi tangan hingga lengan bawah.",
                    analogi = "Seperti tali gitar yang bergetar paling keras di ujungnya ketika panjangnya pas dengan nadanya.",
                    prerequisite = "M07C.T2.2",
                    learningGoal = "Menjelaskan mekanisme luka bakar akibat loop konduktif kulit dan resonansi gelombang berdiri pada efek antena kawat.",
                    crossLinks = "▶ Simulasi: A2 Luka bakar: efek antena pada kawat dan loop konduktif",
                    labType = "m7c_antena_loop",
                    sortOrder = 4
                ),
                MriNode(
                    code = "M07C.T2.5",
                    level = 2,
                    title = "Arus induksi pada implan pacu jantung",
                    summaryShort = "Selain memanaskan kabel, RF dan gradien dapat mengubah fungsi pacu jantung, sementara atau permanen. Komplikasinya jarang, tetapi dapat mengancam nyawa.",
                    detailFull = "Sedikitnya 13 kematian akibat malfungsi pacu jantung telah dilaporkan, penyebab kematian terkait MRI yang paling umum. Mekanisme fisiologisnya biasanya asistol atau fibrilasi ventrikel, karena RF atau gradien mengganggu rangkaian pengindera sehingga memicu rangsangan jantung yang tidak tepat. Pacu jantung yang terlalu cepat menurunkan tekanan darah arteri secara bermakna. Gangguan lain: catu daya internal terganggu dan saklar buluh (reed switch) aktif sehingga pacu tidak sinkron. Produsen kini memodifikasi kabel agar ujungnya tidak memanas, memperbaiki rangkaian pengindera, mengurangi komponen ferromagnetik, memasang filter pada catu daya, dan mengganti saklar buluh dengan transduser analog. Karena itu ada perangkat yang aman bersyarat dipindai.",
                    analogi = "Seperti radio yang kena gangguan sinyal: bunyinya bisa kacau atau mati.",
                    prerequisite = "M07C.T2.4, M07B.T2.5",
                    learningGoal = "Menjelaskan bahaya arus induksi RF dan gradien terhadap fungsi pacu jantung serta modifikasi desain MR Conditional.",
                    crossLinks = "▶ Simulasi: A2 Luka bakar & implan",
                    labType = "m7c_antena_loop",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M07C.T2.6",
                    level = 2,
                    title = "Stimulasi saraf tepi (PNS) oleh gradien",
                    summaryShort = "Gradien yang berganti cepat menginduksi arus di jaringan tubuh. Bila arus melewati ambang, saraf atau otot terangsang. Pasien merasa ketukan, kesemutan, atau denyutan di tungkai.",
                    detailFull = "Arus minimum yang mendepolarisasi sel saraf atau otot disebut reobase. Kepekaan jaringan dinyatakan dengan nilai kronaksi, yaitu lama denyut minimum saat arus bernilai dua kali reobase. Risiko serius bila otot jantung atau diafragma terpengaruh, tetapi pada pemindai diagnostik ambang jantung jauh lebih tinggi daripada ambang saraf tepi. Pedoman: pada mode normal gradien tidak melebihi 80 persen ambang rata-rata saraf tepi dan tidak pernah melebihi 100 persen. Mode riset boleh sampai 120 persen. Pada studi anjing, rangsangan napas butuh 300 persen dan rangsangan jantung 900 persen. Produsen juga membatasi slew rate, biasanya sampai 200 T/m/s. Gradien dapat pula mengganggu elektronik implan, menginduksi arus pada logam, dan menimbulkan getaran berfrekuensi tinggi yang tidak nyaman.",
                    analogi = "Seperti sengatan listrik statis yang kecil: di bawah ambang tidak terasa, di atas ambang mulai terasa.",
                    prerequisite = "M06A.T2.1",
                    learningGoal = "Menjelaskan mekanisme stimulasi saraf tepi (PNS) akibat dB/dt gradien serta batas persentase ambangnya (80%, 100%, 120%).",
                    crossLinks = "▶ Simulasi: A3 Ambang stimulasi saraf oleh gradien",
                    labType = "m7c_stimulasi",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M07C.T2.7",
                    level = 2,
                    title = "Bising akustik & batas paparan",
                    summaryShort = "Gradient coil menimbulkan bising keras karena gaya Lorentz, dan pelindung telinga dianjurkan untuk pasien. Makin cepat arus gradien berganti (dB/dt), makin keras bunyinya.",
                    detailFull = "Arus di gradient coil berinteraksi dengan medan statis, lalu getaran merambat lewat struktur pemindai ke udara dan ditangkap gendang telinga. Skala desibel bersifat logaritmik: kenaikan 3 dB berarti intensitas dua kali lipat. OSHA menyarankan paparan di bawah 85 dB setara 8 jam. Pada 100 dB, batasnya tinggal 15 menit per hari. Bore 1,5 T dapat mencapai 100 dB, dan bore 3 T dengan sekuens echo-planar 115 dB. Bunyinya terputus-putus dan pasien tidak sering dipindai, jadi kerusakan permanen akibat satu pemeriksaan kecil kemungkinannya. Tetap saja bunyi di atas 75 dB mulai mengganggu, dan ICNIRP menyarankan pelindung telinga di atas 80 dB. Bila koil kepala tidak muat untuk pelindung, pakai sumbat telinga yang mengisi seluruh liang telinga dan ajarkan cara memasangnya. Sekuens senyap memakai lintasan ruang-k radial spiral dengan langkah gradien kecil, cocok untuk anak, lansia, atau pasien gelisah.\n\nRangsangan saraf oleh gradien (ketukan, kesemutan) punya mekanisme dan ambang berbeda; lihat T2.6.",
                    analogi = "Seperti knop volume: tiap naik 3 dB, daya bunyinya digandakan.",
                    prerequisite = "M06A.T2.4",
                    learningGoal = "Menghitung pelipatan intensitas bising (+3 dB = 2× intensitas) dan menjelaskan batas waktu paparan harian serta kewajiban pelindung telinga.",
                    crossLinks = "▶ Simulasi: A4 Kalkulator bising dan lama paparan | → M06A T2.4",
                    labType = "m7c_bising",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M07C.T3.1",
                    level = 3,
                    title = "Batas SAR & suhu tubuh inti (Tabel 10.1)",
                    summaryShort = "Batas SAR dirata-ratakan selama 6 menit, dan dalam 10 detik mana pun tidak boleh melebihi 2 kali nilainya. Ada pula batas kenaikan suhu inti tubuh untuk tiap mode.",
                    detailFull = "Mode normal: seluruh tubuh 2, kepala 3,2 W/kg, koil lokal kepala dan badan 10, ekstremitas 20 W/kg. Mode tingkat satu: seluruh tubuh 4, kepala 3,2, koil lokal kepala dan badan 20, ekstremitas 40 W/kg. Mode tingkat dua: di atas nilai itu. Suhu inti: mode normal naik paling banyak 0,5 °C hingga paling tinggi 39 °C. Mode tingkat satu naik paling banyak 1 °C hingga paling tinggi 40 °C. Mode tingkat dua boleh lebih dari itu. Dengan koil transmit lokal, suhu orbita dibatasi 1 °C. Bila sistem menolak memulai akuisisi karena SAR terlampaui, operator dapat meminta nasihat medis untuk beralih ke mode tingkat satu. Mode tingkat dua hanya untuk riset, tidak untuk pemeriksaan klinis, dan dikunci perangkat lunak. Hal yang dipertimbangkan sebelum beralih: usia (lansia, neonatus), bentuk tubuh (obesitas), demam, dan obat yang mengubah pengaturan suhu. Pasien yang dibius atau tak dapat berkomunikasi mungkin dikecualikan karena pemantauan efektif diperlukan.",
                    analogi = "Seperti batas kecepatan: ada batas biasa, batas dengan pengawasan, dan lintasan uji tertutup.",
                    prerequisite = "M07C.T2.2",
                    learningGoal = "Memeriksa kepatuhan SAR rata-rata 6 menit, SAR puncak 10 detik, dan kenaikan suhu tubuh inti sesuai Tabel 10.1 IEC.",
                    crossLinks = "▶ Simulasi: A1 Pemeriksa batas (Tabel 10.1)",
                    labType = "m7c_sar",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M07C.T3.2",
                    level = 3,
                    title = "Panjang gelombang RF di dalam jaringan",
                    summaryShort = "Panjang gelombang RF di tubuh lebih pendek daripada di vakum, jadi panjang kritis kawat pun lebih pendek.",
                    detailFull = "Panjang gelombang beberapa meter yang disebut tadi berlaku di vakum. Jaringan mengandung muatan dan dipol molekul yang melawan medan luar. Besarnya pengurangan dinyatakan dengan permitivitas relatif, yaitu tepat 1 untuk vakum dan lebih dari 1 untuk jaringan. Pada sekitar 63,9 MHz air bernilai 81 dan darah 67. Karena kawat pacu jantung berada di dalam darah, nilai 67 memendekkan panjang gelombang menjadi sekitar 58 cm, sehingga panjang dipol kritisnya sekitar 29 cm.",
                    analogi = "Seperti suara di dalam air: gelombangnya lebih pendek daripada di udara.",
                    prerequisite = "M07C.T2.4, M06B.T3.2",
                    learningGoal = "Menghitung pemendekan panjang gelombang RF di dalam darah (εr ≈ 67) menjadi ~58 cm pada 1,5 T dan panjang dipol kritis ~29 cm.",
                    crossLinks = "▶ Simulasi: A5 Panjang gelombang RF di jaringan | → M06B A7",
                    labType = "m7c_b1_jaringan",
                    sortOrder = 9
                ),
                MriNode(
                    code = "M07C.T3.3",
                    level = 3,
                    title = "Mode operasi IEC 60601-2-33",
                    summaryShort = "Ada tiga mode: normal, tingkat satu, dan tingkat dua. Mode yang lebih tinggi mengizinkan batas lebih besar.",
                    detailFull = "Menaikkan mode hanya boleh dilakukan oleh pihak yang berwenang menurut kebijakan fasilitas. Mode Normal ditujukan untuk pemindaian rutin tanpa stres fisiologis; Mode Terkontrol Tingkat Satu memerlukan penilaian medis serta pemantauan pasien; sedangkan Mode Tingkat Dua khusus protokol riset berizin etik dan dikunci kata sandi perangkat lunak.",
                    analogi = "Seperti gigi kendaraan: gigi lebih tinggi butuh pengemudi yang berwenang.",
                    prerequisite = "M07C.T3.1",
                    learningGoal = "Membedakan persyaratan klinis dan wewenang pada Mode Normal, Mode Terkontrol Tingkat Satu, dan Mode Tingkat Dua.",
                    crossLinks = "▶ Simulasi: A1 Kalkulator SAR",
                    labType = "m7c_sar",
                    sortOrder = 10
                ),
                MriNode(
                    code = "M07C.T3.4",
                    level = 3,
                    title = "Hubungan kuadratik SAR terhadap B0 dan sudut flip",
                    summaryShort = "SAR naik kira-kira dengan kuadrat B0 dan kuadrat sudut flip.",
                    detailFull = "Untuk menurunkan SAR, sudut flip dapat dikecilkan atau jumlah pulsa RF per waktu dikurangi. Karena SAR ∝ B0² × α², berpindah dari 1,5 T ke 3 T meningkatkan SAR empat kali lipat, dan menggandakan sudut flip dari 90° menjadi 180° juga melipatgandakan energi pulsa empat kali lipat.",
                    analogi = "Menggandakan sudut flip seperti menggandakan tenaga dorongan: panasnya naik empat kali lipat.",
                    prerequisite = "M07C.T2.2, M07C.T2.3",
                    learningGoal = "Menghitung perubahan SAR ketika kuat medan B0 atau sudut flip (FA) diubah menggunakan hubungan kuadratik.",
                    crossLinks = "▶ Simulasi: A1 Kalkulator mode SAR",
                    labType = "m7c_sar",
                    sortOrder = 11
                )
            )
        ),
        // =========================================================================
        // MODUL 8A: ARTEFAK PENGODEAN & JARINGAN (14 Simpul: 4 T1, 5 T2, 5 T3)
        // =========================================================================
        MriModule(
            id = "M08A",
            code = "M08A",
            title = "Artefak Pengodean & Jaringan",
            subtitle = "Aliasing, Ghosting Gerak, Pergeseran Kimia, Luar-Fase, Magic Angle & Gibbs",
            description = "Mengenali aliasing, ghosting gerak (phase mismapping), pergeseran kimia (bas-relief), pembatalan sinyal luar-fase, sudut ajaib (54,7°), dan truncation (Gibbs ringing), menghitung aliasing frekuensi serta pergeseran piksel, dan memilih strategi kompensasi beserta komprominya.",
            levelBadge = "T1 - T3 Lengkap",
            iconName = "bug",
            nodes = listOf(
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M08A.T1.1",
                    level = 1,
                    title = "Peta artefak MRI",
                    summaryShort = "Kenali tampilannya, cari penyebab fisiknya, lalu pilih cara mengurangi.",
                    detailFull = "Artefak adalah gambaran pada citra yang tidak sesuai dengan jaringan sebenarnya. Tiap artefak punya penyebab fisik yang bisa ditelusuri ke modul sebelumnya.\n\nRingkasan Peta Artefak Utama:\n• Gerak: Jaringan bergerak antar pengambilan data sehingga ruang-k tidak konsisten (Modul 4) → Sangga pasien, sekuens cepat, sinkronisasi gerak (T1.3).\n• Aliasing (wrap): Undersampling frekuensi; anatomi di luar FOV melewati Nyquist dan terlipat ke dalam citra (Modul 4) → Perbesar FOV, pita presaturasi, antialiasing/oversampling (T1.2).\n• Kerentanan magnetik: Logam mengubah B0 setempat, frekuensi Larmor menyimpang (Modul 1, 3) → Spin echo, TE pendek, ubah orientasi (M08C T2.6).\n• Pergeseran kimia: Lemak dan air punya frekuensi Larmor berbeda (~3,5 ppm karena self-shielding) sehingga sinyal lemak bergeser pada arah frekuensi (Modul 6) → B0 rendah, FOV kecil, bandwidth lebar, saturasi lemak (T1.4).\n• Pembatalan sinyal luar-fase: Lemak dan air dalam satu voksel berlawanan fase 180° pada TE tertentu dan saling meniadakan tanpa pergeseran posisi (Modul 3, 6) → Pilih TE in-phase atau spin echo (T2.3).\n• Sudut ajaib (magic angle): Serat kolagen pada sudut ~54,7° (55°) terhadap B0 memperpanjang T2 sehingga tendon tampak terang pada TE pendek (Modul 2) → Ubah sudut anatomi atau perpanjang TE (T2.4).\n• Aliran (ESP & TOF): Spin segar masuk irisan belum jenuh (ESP) atau spin cepat keluar irisan sebelum direfase (TOF) → Atur TR, sudut flip, tebal irisan, arah eksitasi, slab saturasi, GMN (M08B T1.1).\n• Kegagalan peralatan: Kerusakan gradien, arus eddy, atau galat array processor (Modul 6) → Ulangi pemindaian; jika menetap panggil teknisi (M08C T2.5).\n• Gibbs (truncation): Ruang-k dipotong sebelum frekuensi tinggi tepi tajam terekam utuh (Modul 4) → Tambah langkah fase atau filter (T2.5).\n• Cross-excitation: Profil irisan nyata tidak persegi sehingga tetangga ikut terpapar RF (Modul 3, 4) → Celah irisan ≥30%, eksitasi selang-seling, interleaved (M08C T2.1).\n• Zipper (garis RF): RF luar masuk pada frekuensi echo akibat pintu terbuka atau sangkar Faraday bocor (Modul 5, 6) → Tutup pintu ruang magnet, perbaiki kebocoran sangkar RF (M08C T2.2).\n• Shading: Eksitasi tidak merata, beban koil tidak normal (pasien menyentuh koil), atau B0 tidak homogen → Koil sesuai ukuran, bantalan busa, prescan tepat, shimming (M08C T2.3).\n• Moiré (lonjakan derau & pita zebra): Satu lonjakan derau di ruang-k menjadi garis sejajar; pada GRE, aliasing + inhomogenitas B0 di tepi FOV menimbulkan pita zebra → Jaga kelembapan ruang, kencangkan konektor koil, gunakan SE dan anti-aliasing (M08C T2.4).",
                    analogi = "Artefak seperti gejala pada mobil. Lampu indikator yang menyala menunjuk ke bagian tertentu yang bermasalah.",
                    prerequisite = "M04B",
                    learningGoal = "Mengenali tampilan visual, penyebab fisik, asal konsep modul, dan cara mengurangi berbagai artefak citra MRI.",
                    crossLinks = "▶ Simulasi: M08C A4 Pemilih artefak & Tabel 8.8 | → M08B, M08C",
                    labType = "m8c_capstone_tabel88",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M08A.T1.2",
                    level = 1,
                    title = "Aliasing (wrap-around)",
                    summaryShort = "Sinyal dari luar FOV tetap diterima koil, tetapi frekuensinya melewati batas Nyquist, sehingga salah dibaca dan muncul di sisi seberang.",
                    detailFull = "Aliasing atau wrap terjadi bila anatomi di luar FOV terlipat ke atas anatomi di dalam FOV. Contoh: pada citra sagital otak, FOV arah fase lebih kecil dari dimensi depan-belakang kepala.\n\n• Penyebab: Anatomi di luar FOV tetap terkena gradien dan menghasilkan sinyal bila masih berada dalam volume penerimaan koil. Sinyalnya berfrekuensi lebih tinggi atau lebih rendah daripada yang ada di dalam FOV. Jika frekuensinya melebihi frekuensi Nyquist, ia tidak terdigitalisasi dengan akurat dan dianggap sebagai frekuensi yang lebih rendah. Dua sampel per siklus membuat gelombang terbaca benar; kurang dari itu membuatnya terbaca sebagai frekuensi yang salah.\n• Frequency wrap: Aliasing pada arah pengodean frekuensi (high-frequency aliasing) bila FOV lebih kecil dari anatomi pada arah frekuensi.\n• Phase wrap: Aliasing pada arah fase akibat undersampling data sepanjang sumbu fase. Sinyal dari luar FOV mendapat nilai fase dan pseudo-frekuensi yang sudah dimiliki sinyal dari dalam FOV, sehingga terlipat ke dalam (contoh: FOV kanan-kiri lebih kecil dari lebar perut).\n• Arah irisan: Pada pencitraan volume 3D, gradien pemilih irisan bekerja seperti gradien fase kedua; sinyal dari hidung misalnya dapat terlipat ke area oksipital otak.",
                    analogi = "Roda mobil di film tampak berputar mundur karena kamera memotret terlalu jarang.",
                    prerequisite = "M04B.T2.5, M04B.T2.6",
                    learningGoal = "Menjelaskan mekanisme frequency wrap, phase wrap, dan aliasing arah irisan akibat pelanggaran batas pencuplikan Nyquist.",
                    crossLinks = "▶ Simulasi: A5 Aliasing fase dan oversampling, A6 Aliasing frekuensi dan Nyquist",
                    labType = "m8a_aliasing",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M08A.T1.3",
                    level = 1,
                    title = "Phase mismapping (ghosting gerak)",
                    summaryShort = "Anatomi bergerak saat gradien fase berganti tiap TR, sehingga sinyalnya salah tempat. Hantu muncul hanya pada arah fase.",
                    detailFull = "Artefak ini membuat salinan anatomi yang bergerak, berulang sepanjang arah pengodean fase. Sumbernya biasanya gerak periodik: dinding perut saat bernapas, denyut pembuluh dan CSF, menelan, atau gerak mata.\n\n• Tampilan & Rumus Jarak Hantu:\n  Jarak antar hantu (piksel) = TR × matriks fase × NSA ÷ periode gerak\n  Pembilangnya sama dengan waktu pemindaian. Jika periode gerak dihitung dari denyut jantung 60 kali per menit, periodenya 1 detik. Hasilnya dalam piksel dan dikali ukuran piksel memberi jarak sebenarnya.\n• Penyebab (Inter-view & Intra-view mismapping):\n  Gradien fase diterapkan dengan amplitudo berbeda tiap TR, tidak seperti gradien frekuensi dan gradien pemilih irisan yang sama tiap kali. Anatomi yang berpindah posisi antar-TR mendapat nilai fase yang salah (inter-view / view-to-view mismapping). Gerak berulang menimbulkan gangguan periodik pada data ruang-k yang setelah transformasi Fourier menjadi hantu anatomi. Jeda antara pengodean fase dan pembacaan (intra-view mismapping) hanya beberapa milidetik sehingga efeknya kecil.",
                    analogi = "Memotret orang yang berjalan dengan foto panjang yang disusun baris demi baris. Tiap baris dipotret pada saat berbeda, sehingga orangnya muncul di beberapa tempat.",
                    prerequisite = "M04A, M04B",
                    learningGoal = "Menghitung jarak antar hantu gerak pada arah fase dan menjelaskan penyebab inter-view phase mismapping.",
                    crossLinks = "▶ Simulasi: A3 Jarak hantu gerak (phase mismapping)",
                    labType = "m8a_ghosting",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M08A.T1.4",
                    level = 1,
                    title = "Pergeseran kimia (bas-relief artifact)",
                    summaryShort = "Lemak berpresesi sedikit lebih lambat dari air. Selisih frekuensi itu terbaca sebagai selisih posisi pada arah frekuensi.",
                    detailFull = "Artefak ini menggeser letak sinyal lemak pada citra. Di antarmuka lemak-air ia menimbulkan area kosong sinyal (gelap) di satu sisi dan area sinyal tumpang tindih (sangat terang) di sisi seberang.\n\n• Tampilan: Contoh klasik adalah ginjal yang dikelilingi lemak perirenal. Citra tampak seperti timbul seakan disinari dari satu arah dan membuat bayangan di sisi lain, sehingga disebut juga bas-relief artifact.\n• Penyebab: Pada air, hidrogen terikat pada oksigen; pada lemak, hidrogen berada pada rantai karbon trigliserida yang awan elektronnya melindungi inti dari B0 (self-shielding), sehingga frekuensi Larmor lemak lebih rendah sekitar 3,5 ppm. Selisih dalam Hz = frekuensi Larmor × 3,5 ppm (sekitar 224 Hz pada 1,5 T dan 447 Hz pada 3 T; pada ≤0,5 T umumnya tidak berarti).\n• Cara mengurangi:\n  1. Pindai pada kekuatan medan B0 lebih rendah.\n  2. Perkecil FOV agar ukuran piksel lebih kecil.\n  3. Naikkan bandwidth penerimaan agar tiap piksel mencakup rentang frekuensi lebih lebar (kompromi: SNR turun).\n  4. Jenuhkan sinyal lemak atau air (fat/water suppression).",
                    analogi = "Dua penyiar radio dengan frekuensi yang sedikit berbeda. Pada pemindai yang resolusinya kasar, keduanya terdengar bergeser satu kanal.",
                    prerequisite = "M06B.T3.1",
                    learningGoal = "Menjelaskan penyebab self-shielding 3,5 ppm pada pergeseran kimia lemak-air dan kompromi bandwidth terhadap SNR.",
                    crossLinks = "▶ Simulasi: A7 Pergeseran kimia dalam piksel | → Asal fisika: M06B T3.1",
                    labType = "m8a_chemical_shift",
                    sortOrder = 4
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M08A.T2.1",
                    level = 2,
                    title = "Mengurangi aliasing",
                    summaryShort = "Perbesar FOV, jenuhkan sinyal di luar FOV, atau pakai antialiasing (filter pada arah frekuensi, oversampling pada arah fase).",
                    detailFull = "Aliasing pada arah frekuensi maupun fase mengganggu citra dan perlu dikompensasi:\n• Perbesar FOV agar semua anatomi penghasil sinyal masuk (pada arah fase dapat menambah waktu pemindaian bila matriks disesuaikan).\n• Pita presaturasi di area luar FOV yang berpotensi terlipat untuk meniadakan sinyalnya.\n• Antialiasing arah frekuensi: laju sampling digital dinaikkan (selang sampling dipendekkan dengan jendela sampling tetap) dan diterapkan filter low-pass yang membuang frekuensi di luar bandwidth.\n• Antialiasing arah fase (no phase wrap / phase oversampling / fold-over suppression): jumlah langkah fase ditambah sehingga garis ruang-k bertambah dan FOV fase membesar tanpa mengubah ukuran piksel.\n\nDi balik layar (Contoh FOV frekuensi 256 mm, FOV fase 200%, matriks 256×256 → 512 garis ruang-k):\n• Opsi 1: FOV fase membesar menjadi 512 mm untuk memuat anatomi, waktu pemindaian naik sebanding (2×).\n• Opsi 2: FOV fase otomatis 2× lalu bagian tambahan dibuang sehingga FOV asli tersisa; sistem mengimbangi waktu dengan membagi dua NSA. Akibatnya hantu gerak bisa lebih terlihat (NSA tampil 4 padahal 2 yang dipakai), tetapi SNR hampir tidak berubah karena oversampling mengimbangi penurunan NSA.",
                    analogi = "Lemari laci: tinggi bingkai (ukuran piksel) tetap, tetapi laci dibuat lebih tipis sehingga muat lebih banyak laci (garis ruang-k).",
                    prerequisite = "M08A.T1.2",
                    learningGoal = "Membandingkan cara kerja filter low-pass arah frekuensi serta Opsi 1 dan Opsi 2 phase oversampling beserta dampaknya pada waktu pindai dan NSA.",
                    crossLinks = "▶ Simulasi: A5 Aliasing fase dan oversampling",
                    labType = "m8a_aliasing",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M08A.T2.2",
                    level = 2,
                    title = "Mengurangi ghosting gerak",
                    summaryShort = "Perbesar jarak antar hantu, pindahkan arah fase, jenuhkan sumber gerak, rata-ratakan sinyal, atau sinkronkan dengan gerak.",
                    detailFull = "Strategi mengatasi phase mismapping:\n• Atur parameter: Jika TR, matriks fase, dan NSA naik, jarak antar hantu bertambah sehingga hantu di dalam citra berkurang. Hal yang sama terjadi bila periode gerak memendek (struktur berdenyut cepat).\n• Tukar fase dan frekuensi: Hantu hanya muncul pada arah fase. Pada potongan aksial dada, arah fase depan-belakang membuat denyut jantung menutupi aorta; menukar fase menjadi kanan-kiri menggeser hantu menjauh dari aorta (tidak menghilangkan artefak, hanya memindahkannya).\n• Pulsa presaturasi: Ditaruh di atas sumber gerak (misalnya tenggorokan pada MRI servikal untuk meredam gerak menelan) atau di luar FOV untuk menjenuhkan darah berdenyut yang masuk.\n• NSA lebih banyak: Sinyal sejati diperkuat sedangkan sinyal salah posisi yang acak dirata-ratakan (waktu pemindaian naik; pengisian ruang-k radial/BLADE membantu menekan penalti ini).\n• Kenyamanan pasien: Posisi nyaman, bantalan/tali imobilisasi, penjelasan prosedur, dan komunikasi interkom.",
                    analogi = "Kalau bayangan hantu tidak bisa dihilangkan, geser arah panggungnya agar tidak menutupi bintang utamanya.",
                    prerequisite = "M08A.T1.3",
                    learningGoal = "Memilih strategi penukaran sumbu fase-frekuensi, presaturasi, peningkatan NSA, atau pengaturan parameter untuk meredam ghosting gerak.",
                    crossLinks = "▶ Simulasi: A3 Jarak hantu gerak, A4 Triggering dan gating napas",
                    labType = "m8a_ghosting",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M08A.T2.3",
                    level = 2,
                    title = "Pembatalan sinyal luar-fase (out-of-phase)",
                    summaryShort = "Lemak dan air di voksel yang sama berlawanan fase pada TE tertentu dan saling meniadakan. Pilih TE yang cocok atau pakai spin echo.",
                    detailFull = "Artefak ini tampak sebagai cincin gelap (india ink artifact) di sekitar organ tempat antarmuka lemak dan air berada dalam satu voksel, terutama pada sekuens gradient echo.\n\n• Beda dengan pergeseran kimia: Pada pergeseran kimia, sinyal lemak bergeser posisi sepanjang arah frekuensi. Pada pembatalan luar-fase TIDAK ada sinyal yang bergeser posisi; artefak muncul di dalam piksel mana pun yang memuat lemak dan air sekaligus (misalnya penurunan sinyal seluruh parenkim pada perlemakan hati).\n• Penyebab: Karena lemak dan air berpresesi pada frekuensi berbeda, selisih fasenya berubah secara berkala terhadap TE: kadang searah (in-phase) dan kadang berlawanan 180° (out-of-phase) sehingga saling meniadakan.\n• Cara mengurangi:\n  1. Pilih TE yang sesuai dengan periodisitas in-phase lemak-air (makin tinggi B0, makin pendek periodenya).\n  2. Gunakan sekuens spin echo karena pulsa rephasing 180° mengompensasi selisih fase kimia (rephasing gradien pada GRE tidak mengompensasinya).\n• Manfaat klinis: Mendeteksi kandungan lemak mikroskopik (adenoma adrenal, infiltrasi lemak hati), menekan latar pada angiografi inflow, dan menghasilkan citra khusus air/lemak pada teknik Dixon.",
                    analogi = "Dua jarum jam yang berputar dengan kecepatan berbeda: kadang bertumpuk searah (sekitar pukul 12.00, 1.05) dan kadang berlawanan arah 180° (sekitar pukul 12.33, 1.38).",
                    prerequisite = "M08A.T1.4, M03A",
                    learningGoal = "Membedakan pembatalan sinyal luar-fase dari pergeseran kimia serta menentukan TE in-phase/out-of-phase pada GRE dan SE.",
                    crossLinks = "▶ Simulasi: A1 Lemak, air, dan TE (luar-fase)",
                    labType = "m8a_luar_fase",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M08A.T2.4",
                    level = 2,
                    title = "Sudut ajaib (magic angle)",
                    summaryShort = "Serat kolagen pada sudut sekitar 55° terhadap B0 memperpanjang T2, sehingga tendon terang pada TE pendek. Ubah sudut atau perpanjang TE.",
                    detailFull = "Artefak ini membuat jaringan berkolagen (seperti tendon patela, rotator cuff, atau meniskus) tampak terang secara tidak wajar dan dapat menyerupai robekan atau tendinopati.\n\n• Penyebab: Air yang terikat pada serat kolagen yang anisotropik normalnya mengalami interaksi dipolar spin-spin yang kuat sehingga T2 sangat pendek dan tampak gelap. Ketika serat kolagen membentuk sudut θ yang memenuhi persamaan 3·cos²θ − 1 = 0 (yaitu θ ≈ 54,7° atau dibulatkan 55°) terhadap medan B0, interaksi dipolar spin-spin turun mendekati nol. Akibatnya waktu peluruhan T2 memanjang drastis dan tendon tampak hiperintens pada sekuens ber-TE pendek (T1W, PD, GRE).\n• Cara mengurangi:\n  1. Ubah sudut posisi anatomi pasien terhadap B0.\n  2. Perpanjang TE (misalnya gunakan T2W dengan TE panjang): sinyal artefak sudut ajaib akan meredup pada TE panjang, sedangkan cairan patologis sejati tetap terang.",
                    analogi = "Kerumunan yang saling bertabrakan sehingga cepat berhenti. Pada susunan sudut tertentu mereka tidak saling mengganggu, sehingga gerak mereka bertahan lebih lama.",
                    prerequisite = "M02.T2.1",
                    learningGoal = "Menjelaskan syarat sudut ajaib 54,7° (3·cos²θ − 1 = 0) pada serat kolagen dan membedakannya dari cairan patologis menggunakan TE panjang.",
                    crossLinks = "▶ Simulasi: A2 Sudut ajaib pada tendon",
                    labType = "m8a_magic_angle",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M08A.T2.5",
                    level = 2,
                    title = "Truncation (Gibbs ringing)",
                    summaryShort = "Data ruang-k kurang, jadi tepi tajam tidak direkonstruksi mulus. Tambah langkah pengodean fase.",
                    detailFull = "Artefak ini muncul sebagai pita terang dan gelap bergantian yang berjalan sejajar dengan antarmuka bertepi tajam dan berkontras tinggi (misalnya batas CSF–korda spinalis yang dapat menyerupai siringomielia).\n\n• Penyebab: Pengambilan sampel ruang-k dihentikan (terpotong/truncated) sebelum frekuensi spasial tinggi di tepi ruang-k terekam semuanya. Deret Fourier berhingga kemudian mengalami osilasi (overshoot dan undershoot ~9%) di dekat diskontinuitas tepi tajam.\n• Cara mengurangi:\n  1. Tambah jumlah langkah pengodean fase (gunakan matriks yang lebih halus/besar pada arah fase).\n  2. Hindari teknik pengisian ruang-k sebagian seperti partial Fourier.\n  3. Pada citra T1W, supresi lemak dapat membantu menurunkan lonjakan sinyal lemak di tepi.\n  4. Terapkan filter apodisasi (windowing) yang meredam amplitudo sinyal menuju nol di tepi jendela sampling (kompromi: tepi citra sedikit lebih kabur / resolusi efektif turun).",
                    analogi = "Menggambar kotak bertepi tajam dengan pena yang terlalu tebal: garis tepinya bergetar karena detail halus tidak muat.",
                    prerequisite = "M04B.T2.1",
                    learningGoal = "Menganalisis hubungan antara pemotongan frekuensi tinggi ruang-k, ukuran matriks fase, filter apodisasi, dan osilasi Gibbs ringing.",
                    crossLinks = "▶ Simulasi: A8 Truncation (Gibbs): ubah matriks, lihat ringing",
                    labType = "m8a_gibbs",
                    sortOrder = 9
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M08A.T3.1",
                    level = 3,
                    title = "Periodisitas lemak-air dan teknik Dixon",
                    summaryShort = "Periode = 1 dibagi selisih frekuensi. B0 lebih tinggi berarti selisih Hz lebih besar dan periode lebih pendek.",
                    detailFull = "Lemak dan air kembali searah (in-phase) setiap satu periode T = 1 / Δf:\n• Pada 1,5 T (f₀ = 63,87 MHz, Δppm = 3,5 ppm): selisih frekuensi Δf = 3,5 × 63,87 ≈ 224 Hz → periode T = 1000 / 223,55 ≈ 4,47 ms (in-phase pada TE ≈ 4,47 ms, 8,95 ms; out-of-phase pada TE ≈ 2,24 ms, 6,71 ms).\n• Pada 3,0 T (f₀ = 127,74 MHz, Δppm = 3,5 ppm): selisih frekuensi Δf ≈ 447 Hz → periode T ≈ 2,24 ms (in-phase pada TE ≈ 2,24 ms, 4,47 ms; out-of-phase pada TE ≈ 1,12 ms, 3,35 ms).\n\nTeknik Dixon mengakuisisi citra pada TE searah (In-Phase = W + F) dan berlawanan fase (Out-of-Phase = |W − F|), kemudian mengolahnya secara matematis (penjumlahan dan pengurangan) untuk menghasilkan citra khusus air (water-only) dan khusus lemak (fat-only).",
                    analogi = "Dua pelari di lintasan melingkar dengan kecepatan berbeda: mereka bertemu tiap beberapa putaran (in-phase), dan tepat di tengah-tengahnya berada di sisi berlawanan (out-of-phase).",
                    prerequisite = "M08A.T2.3",
                    learningGoal = "Menghitung periode siklus fase lemak-air pada 1,5 T (4,47 ms) dan 3 T (2,24 ms) serta prinsip rekonstruksi air-lemak Dixon.",
                    crossLinks = "▶ Simulasi: A1 Lemak, air, dan TE (luar-fase)",
                    labType = "m8a_luar_fase",
                    sortOrder = 10
                ),
                MriNode(
                    code = "M08A.T3.2",
                    level = 3,
                    title = "Mengapa hantu hanya pada arah fase",
                    summaryShort = "Titik data sepanjang satu garis diambil dalam milidetik, sedangkan antar garis diambil tiap TR. Peluang anatomi bergerak jauh lebih besar antar garis.",
                    detailFull = "Alasan fisik mengapa ghosting gerak hanya terbentang pada arah pengodean fase terletak pada perbedaan skala waktu pencuplikan di ruang-k:\n• Arah frekuensi (kiri-kanan di ruang-k): seluruh titik data pada satu garis diambil dalam satu jendela pembacaan yang sangat singkat. Selang waktu antar titik sampel adalah Δt = 1 / BW_terima. Misalnya untuk bandwidth 32 kHz, selang antar titik hanya 1 / 32.000 s ≈ 0,031 ms (31 μs).\n• Arah fase (atas-bawah di ruang-k): satu baris baru diisi setiap satu TR (ratusan hingga ribuan milidetik), dan seluruh matriks membutuhkan waktu berdetik-detik hingga bermenit-menit.\n\nKarena peluang jaringan bergerak dalam 0,031 ms nyaris nol sedangkan dalam ratusan milidetik (antar-TR) sangat besar, ketidakkonsistenan fase akibat gerak fisiologis terakumulasi sepanjang sumbu fase.",
                    analogi = "Menulis satu kalimat dalam sekejap (arah frekuensi) berbeda dengan menulis satu bab di mana tiap kalimat ditulis pada hari berbeda (arah fase): perubahan suasana antar hari jauh lebih besar.",
                    prerequisite = "M08A.T1.3",
                    learningGoal = "Membandingkan selang pencuplikan arah frekuensi (~0,031 ms pada BW 32 kHz) dengan selang antar baris fase (TR) sebagai penyebab ghosting arah fase.",
                    crossLinks = "▶ Simulasi: A3 Jarak hantu gerak (phase mismapping)",
                    labType = "m8a_ghosting",
                    sortOrder = 11
                ),
                MriNode(
                    code = "M08A.T3.3",
                    level = 3,
                    title = "Kompensasi napas, triggering, dan gating",
                    summaryShort = "Kompensasi napas mengatur ulang urutan garis ruang-k, triggering memicu RF pada fase napas yang sama, dan gating membuang data di ujung siklus napas.",
                    detailFull = "Empat metode sinkronisasi fisiologis untuk menekan artefak gerak:\n1. Kompensasi napas (respiratory compensation / phase reordering): Menggunakan sabuk bellows di dada atau navigator echo di atas hemidiafragma kanan (~5 sampel/detik). Sistem mengurutkan ulang pengodean fase: garis tengah ruang-k (penentu kontras & sinyal) diisi saat gerak dada minimal, sedangkan garis tepi (penentu resolusi) diisi saat gerak maksimal sehingga kesalahan menjadi tidak periodik.\n2. Triggering napas: Pulsa RF dipicu pada titik fase napas yang sama. Kelemahan: TR ditentukan oleh laju napas pasien (pada 15 napas/menit, TR ≈ 4000 ms atau kelipatannya) sehingga membatasi pembobotan T1, dan variasi napas mencampur kontras.\n3. Gating napas: Menetapkan ambang gerbang sehingga data hanya diterima saat diafragma tenang (ekspirasi penuh). Keunggulan: TR dan parameter kontras bebas diatur. Kelemahan: hingga ~60% data dapat ditolak sehingga waktu pindai memanjang signifikan.\n4. Triggering jantung: Mengunci pulsa RF pada gelombang R EKG (akurat untuk jantung/aorta) atau sensor denyut perifer fotopletismografi di jari (untuk menekan pulsatilitas CSF/pembuluh perifer).",
                    analogi = "Memotret ombak pantai: triggering memotret setiap kali ombak tiba di titik yang sama; gating membuang semua foto yang terambil saat ombak sedang bergulung tinggi.",
                    prerequisite = "M08A.T2.2",
                    learningGoal = "Membedakan mekanisme dan konsekuensi waktu/kontras antara respiratory phase reordering, triggering napas, gating napas, dan cardiac triggering.",
                    crossLinks = "▶ Simulasi: A4 Triggering dan gating napas",
                    labType = "m8a_resp_gating",
                    sortOrder = 12
                ),
                MriNode(
                    code = "M08A.T3.4",
                    level = 3,
                    title = "Menghitung aliasing frekuensi",
                    summaryShort = "Frekuensi dipersepsikan = frekuensi sebenarnya − 2 × frekuensi Nyquist (untuk frekuensi di atas Nyquist).",
                    detailFull = "Frekuensi yang dipersepsikan sistem digital ditentukan dengan membandingkan frekuensi sinyal sebenarnya terhadap frekuensi Nyquist (f_Nyquist = BW_total / 2):\n• Rumus pelipatan (untuk f_sebenarnya > f_Nyquist):\n  f_terbaca = f_sebenarnya − 2 × f_Nyquist\n• Contoh perhitungan: Bandwidth penerimaan total 64 kHz memiliki batas Nyquist +32 kHz dan −32 kHz terhadap frekuensi tengah. Jika jaringan di luar FOV memiliki frekuensi sebenarnya +35 kHz, maka frekuensi yang dipersepsikan sistem adalah:\n  f_terbaca = 35 − 2 × 32 = −29 kHz\n  Akibatnya sinyal tersebut dipetakan ke sisi frekuensi rendah di seberang FOV.",
                    analogi = "Jarum jam yang melompat melewati batas angka dan muncul kembali dari ujung seberang.",
                    prerequisite = "M08A.T1.2, M08A.T2.1",
                    learningGoal = "Menghitung frekuensi terlipat (aliased frequency) menggunakan rumus f_terbaca = f_sebenarnya − 2·f_Nyquist.",
                    crossLinks = "▶ Simulasi: A6 Aliasing frekuensi dan Nyquist",
                    labType = "m8a_aliasing",
                    sortOrder = 13
                ),
                MriNode(
                    code = "M08A.T3.5",
                    level = 3,
                    title = "Menghitung pergeseran piksel",
                    summaryShort = "Pergeseran piksel = (selisih ppm × frekuensi Larmor) ÷ (bandwidth penerimaan ÷ matriks frekuensi).",
                    detailFull = "Langkah menghitung besar pergeseran kimia lemak-air dalam satuan piksel dan milimeter:\n1. Selisih frekuensi lemak-air: Δf = 3,5 ppm × f₀ (pada 1,5 T ≈ 224 Hz; pada 3,0 T ≈ 447 Hz).\n2. Bandwidth per piksel: BW_piksel = BW_total ÷ Matriks_frekuensi.\n3. Pergeseran dalam piksel: Geser (piksel) = Δf ÷ BW_piksel.\n4. Pergeseran dalam milimeter: Geser (mm) = Geser (piksel) × (FOV ÷ Matriks_frekuensi).\n\nContoh pada 1,5 T dengan bandwidth ±16 kHz (total 32.000 Hz), matriks frekuensi 256, dan FOV 240 mm:\n• BW_piksel = 32.000 ÷ 256 = 125 Hz/piksel\n• Geser piksel = 223,55 ÷ 125 ≈ 1,79 piksel\n• Ukuran piksel = 240 ÷ 256 ≈ 0,94 mm → Geser nyata ≈ 1,79 × 0,94 ≈ 1,68 mm.\nJika bandwidth dinaikkan menjadi ±32 kHz (total 64 kHz), BW_piksel menjadi 250 Hz dan pergeseran menyusut menjadi < 1 piksel (~0,89 piksel).",
                    analogi = "Penggaris dengan garis tiap 1 mm: selisih 1,8 garis terlihat jelas; bila skala tiap garis dibuat dua kali lebih lebar, selisihnya menyusut menjadi kurang dari satu petak.",
                    prerequisite = "M08A.T1.4",
                    learningGoal = "Menghitung bandwidth per piksel serta pergeseran kimia dalam satuan piksel dan milimeter pada berbagai kombinasi B0, BW, matriks, dan FOV.",
                    crossLinks = "▶ Simulasi: A7 Pergeseran kimia dalam piksel",
                    labType = "m8a_chemical_shift",
                    sortOrder = 14
                )
            )
        ),

        // =========================================================================
        // MODUL 8B: ALIRAN DAN ARTEFAK ALIRAN (10 Simpul: 2 T1, 6 T2, 2 T3)
        // =========================================================================
        MriModule(
            id = "M08B",
            code = "M08B",
            title = "Aliran & Artefak Aliran",
            subtitle = "Entry-Slice (ESP), Time-of-Flight (TOF), Bilangan Reynolds, Dephasing, GMN & Presaturasi",
            description = "Menjelaskan fenomena entry-slice (ESP) dan time-of-flight (TOF), membedakan aliran laminar, spiral, vorteks, dan turbulen (bilangan Reynolds), menganalisis dephasing intravoksel dan momen gradien (M0, M1, M2), serta memilih teknik kompensasi aliran (GMN vs presaturasi spasial).",
            levelBadge = "T1 - T3 Lengkap",
            iconName = "waves",
            nodes = listOf(
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M08B.T1.1",
                    level = 1,
                    title = "Aliran darah dan artefak aliran: gambaran umum",
                    summaryShort = "ESP membuat lumen tampak terang (peningkatan terkait aliran), TOF membuat lumen tampak gelap pada kecepatan tinggi, dan fase yang tersebar membuat sinyal melemah (dephasing intravoksel) atau salah petak (pemetaan fase salah).",
                    detailFull = "Darah dan cairan serebrospinal (CSF) yang mengalir membuat sinyal di lumen sangat berbeda dari jaringan di sekitarnya. Dua fenomena utamanya adalah entry-slice phenomenon (ESP) dan time-of-flight (TOF). Selain itu, spin yang mengalir sepanjang gradien mendapat fase yang berbeda-beda.\n\n• Sisi buruk dan sisi baik: Aliran arteri yang berdenyut dapat menghasilkan pemetaan fase yang keliru, dan saturasi pada aliran lambat dapat memberi kesan ukuran pembuluh yang salah. Di sisi lain, bila parameter sekuens diatur agar sinyal latar tersaturasi sementara pembuluh tetap hiperintens, dihasilkan citra Magnetic Resonance Angiography (MRA) dengan kontras lumen yang tegas.\n• Dua penyebab sinyal lumen berubah:\n  1. Efek inflow (ESP): spin yang baru masuk irisan belum jenuh sehingga lebih terang daripada jaringan diam (T1.2, T2.1).\n  2. Efek keluar irisan (TOF): pada kecepatan tinggi, spin yang tereksitasi sudah meninggalkan irisan sebelum direfase (T2.2).\n• Dephasing intravoksel: Dalam satu voksel ada spin dengan kecepatan berbeda (lambat di tepi, cepat di tengah) sehingga fasenya menyebar dan saling meniadakan (T2.3, T3.1).\n• Pemetaan fase salah: Bila sinyal tinggi berpindah tempat di antara langkah penyandian fase, posisinya pada arah fase menjadi keliru. Perbaikan: pulsa presaturasi di luar volume citra (T2.5), atau bila sinyal tinggi pembuluh dibutuhkan (angiografi inflow) gunakan GMN (T2.4).",
                    analogi = "Seperti memotret air sungai: air yang baru datang tampak jernih dan terang, air yang sudah lama berputar tampak keruh, dan air yang melaju terlalu cepat tidak sempat terekam.",
                    prerequisite = "M03A, M04A",
                    learningGoal = "Mengidentifikasi empat dampak aliran fluida (ESP, TOF, dephasing intravoksel, dan salah petak fase) beserta pemanfaatannya pada MRA.",
                    crossLinks = "▶ Simulasi: A6 Latihan: sebab dan teknik perbaikan artefak aliran",
                    labType = "m8b_aliran",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M08B.T1.2",
                    level = 1,
                    title = "Entry-slice phenomenon (ESP)",
                    summaryShort = "Darah yang baru masuk irisan masih \"segar\" (tidak jenuh), sehingga sinyalnya lebih tinggi daripada jaringan diam di sekitarnya.",
                    detailFull = "Inti yang diam di dalam irisan menerima pulsa eksitasi RF berulang. Karena TR tidak cukup panjang untuk pemulihan longitudinal penuh, inti jaringan diam tersaturasi sebagian dan sinyalnya rendah.\n\nSebaliknya, inti darah yang baru mengalir masuk belum pernah terkena pulsa selektif-irisan tersebut, sehingga magnetisasi longitudinalnya masih penuh searah B0 (spin-up). Saat pertama kali terkena pulsa eksitasi di irisan masuk, magnetisasinya menghasilkan sinyal transversal yang sangat besar (flow-related enhancement). Efek ini paling kuat bila aliran tegak lurus irisan dan paling jelas pada irisan pertama dalam tumpukan. Makin dalam ke tumpukan irisan, spin telah menerima banyak pulsa sehingga efek ESP menurun.",
                    analogi = "Papan tulis jaringan diam sudah penuh coretan (jenuh); darah yang baru masuk seperti papan tulis bersih yang siap ditulisi.",
                    prerequisite = "M08B.T1.1",
                    learningGoal = "Menjelaskan mengapa spin darah segar menghasilkan sinyal hiperintens pada irisan masuk pertama dibanding jaringan diam yang jenuh.",
                    crossLinks = "▶ Simulasi: A1 Aliran dan entry-slice (ESP)",
                    labType = "m8b_aliran",
                    sortOrder = 2
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M08B.T2.1",
                    level = 2,
                    title = "Faktor yang memengaruhi ESP (Aorta vs IVC)",
                    summaryShort = "Arah aliran faktor terpenting. Searah eksitasi (co-current) cepat jenuh; berlawanan arah (counter-current) tetap segar.",
                    detailFull = "Intensitas sinyal darah pada tumpukan irisan bergantung pada lima faktor:\n• TR lebih pendek → Spin menerima banyak pulsa, saturasi naik, ESP berkurang.\n• Sudut balik (flip angle) lebih besar → Saturasi naik, ESP berkurang.\n• Tebal irisan lebih tebal → Spin tinggal lebih lama di irisan, menerima lebih banyak pulsa, ESP berkurang (ESP lebih jelas pada irisan tipis).\n• Kecepatan aliran lebih cepat → Spin hanya terkena satu atau dua pulsa, ESP lebih jelas.\n• Arah aliran berlawanan arah eksitasi irisan (counter-current) → Spin yang masuk ke tiap irisan tetap tak jenuh sehingga sinyal tinggi menembus lebih dalam.\n\nESP dalam praktik (Aorta dan IVC): Pada 4 irisan aksial abdomen yang dieksitasi dari inferior ke superior (1 → 4), irisan 1 menampilkan IVC terang dan aorta lebih gelap; pada irisan 4 kebalikannya (aorta terang karena mengalir berlawanan arah eksitasi, sedangkan IVC yang searah eksitasi sudah jenuh).",
                    analogi = "Antrean loket: orang yang berjalan searah antrean sudah diperiksa berkali-kali; yang datang dari arah berlawanan baru pertama kali diperiksa di tiap loket.",
                    prerequisite = "M08B.T1.2",
                    learningGoal = "Menganalisis pengaruh TR, sudut flip, tebal irisan, kecepatan, dan arah aliran (co-current vs counter-current pada Aorta dan IVC) terhadap ESP.",
                    crossLinks = "▶ Simulasi: A1 Aliran dan entry-slice (ESP) Tahap 2",
                    labType = "m8b_aliran",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M08B.T2.2",
                    level = 2,
                    title = "Time-of-flight (TOF) signal loss",
                    summaryShort = "Darah yang keluar irisan sebelum pulsa 180°, atau baru masuk setelah pulsa 90°, tidak menghasilkan sinyal, sehingga pembuluh tampak gelap.",
                    detailFull = "Agar menghasilkan sinyal spin-echo, inti harus menerima pulsa eksitasi 90° sekaligus pulsa rephasing 180°:\n• Pada sekuens spin-echo, inti yang terkena pulsa 90° tetapi sudah mengalir keluar dari irisan saat pulsa 180° tiba tidak akan direfase. Demikian pula inti yang baru masuk irisan saat pulsa 180° belum pernah tereksitasi. Keduanya tidak menghasilkan sinyal sehingga lumen tampak gelap (high-velocity signal loss / flow void).\n• Faktor yang memperkuat signal void TOF pada spin-echo:\n  1. Kecepatan aliran naik → lebih banyak spin keluar irisan dalam selang TE/2.\n  2. TE naik → jeda waktu antara 90° dan 180° (TE/2) makin lama.\n  3. Tebal irisan turun (irisan tipis) → spin lebih cepat melintasi keluar irisan.\n• Bandingkan dengan gradient-echo: TE biasanya pendek dan rephasing gradien bekerja pada seluruh volume (bukan selektif satu irisan), sehingga spin yang tereksitasi tetap direfase dan tampak terang (flow-sensitive).",
                    analogi = "Fotografer yang menekan tombol \"ambil\" (90°) lalu \"simpan\" (180°): bila objek berlari keluar bingkai di antara dua langkah itu, fotonya kosong.",
                    prerequisite = "M08B.T1.1, M03A",
                    learningGoal = "Menghitung perpindahan darah selama TE/2 terhadap tebal irisan untuk memprediksi terjadinya high-velocity signal loss (flow void) pada spin-echo.",
                    crossLinks = "▶ Simulasi: A2 Simulasi TOF pada spin-echo",
                    labType = "m8b_tof",
                    sortOrder = 4
                ),
                MriNode(
                    code = "M08B.T2.3",
                    level = 2,
                    title = "Jenis-jenis aliran (Laminar, Spiral, Vorteks, Turbulen)",
                    summaryShort = "Laminar (berlapis), spiral (sekrup), vorteks (pusaran), turbulen (acak).",
                    detailFull = "Empat profil aliran hemodinamik dan dampaknya pada pencitraan MRI:\n1. Laminar (parabolik): Kecepatan berlapis konsisten; tengah lumen bergerak paling cepat sedangkan dekat dinding lambat. Masalah: dephasing intravoksel, serta aliran lambat di tepi dinding cepat jenuh sehingga lumen pembuluh tampak lebih sempit dari aslinya.\n2. Spiral (heliks): Aliran melilit seperti sekrup, khas pada pembuluh besar melengkung seperti arkus aorta torakalis. Masalah: komponen kecepatan multi-arah; sering memerlukan angiografi kontras fase (phase-contrast MRA).\n3. Vorteks: Pusaran berputar balik, misalnya tepat di hilir penyempitan (stenosis/striktur) atau di dalam kantong aneurisma. Masalah: spin tinggal lama sehingga jenuh dan mengalami dephasing intravoksel.\n4. Turbulen: Kecepatan dan arah berubah secara acak ketika bilangan Reynolds melampaui ambang (Re > 2100). Masalah: dispersi fase acak di dalam voksel yang tidak dapat dikompensasi dengan GMN.",
                    analogi = "Laminar seperti lajur jalan tol yang tertib, vorteks seperti putaran di bundaran, dan turbulen seperti kerumunan pasar yang bergerak acak.",
                    prerequisite = "M08B.T1.1",
                    learningGoal = "Membedakan empat jenis aliran darah (laminar, spiral, vorteks, turbulen) dan masalah artefak spesifik yang ditimbulkannya.",
                    crossLinks = "▶ Simulasi: A3 Aliran laminar atau turbulen (Reynolds)",
                    labType = "m8b_reynolds",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M08B.T2.4",
                    level = 2,
                    title = "Gradient moment rephasing / nulling (GMN)",
                    summaryShort = "Lobe positif, negatif ganda, lalu positif membuat fase tambahan akibat aliran kembali ke nol.",
                    detailFull = "GMN (gradient moment nulling / flow compensation) menambahkan lobus gradien ekstra pada sumbu pemilih irisan dan/atau pengode frekuensi agar pergeseran fase akibat aliran berkecepatan konstan kembali ke nol pada saat echo:\n• Pola lobus bipolar/tripolar: Polaritas gradien diatur dengan perbandingan luas +1, −2, +1 (positif, negatif ganda, lalu positif). Inti yang bergerak dengan kecepatan konstan memperoleh tambahan fase pada lobus pertama, dikurangi dua kali pada lobus kedua, dan dikembalikan tepat ke fase nol pada lobus ketiga.\n• Karakteristik klinis:\n  - Sangat efektif mengurangi dephasing intravoksel pada aliran lambat dan laminar (kompensasi gerak orde pertama / first-order motion).\n  - Lebih efektif pada vena daripada arteri (karena aliran arteri berdenyut memiliki percepatan).\n  - Kurang efektif pada aliran turbulen cepat.\n• Konsekuensi (Penalti): Lobus gradien tambahan memakan waktu sehingga menaikkan TE minimum dan dapat mengurangi jumlah irisan yang tersedia per TR.",
                    analogi = "Seperti menyeimbangkan timbangan: dorong ke kanan sekali, dorong ke kiri dua kali, lalu ke kanan sekali lagi sehingga kembali tepat ke titik nol.",
                    prerequisite = "M08B.T2.3, M06A.T2.5",
                    learningGoal = "Menjelaskan cara kerja lobus gradien +1, −2, +1 pada GMN, efektivitasnya pada aliran laminar/vena, dan penaltinya terhadap TE minimum.",
                    crossLinks = "▶ Simulasi: A4 Dephasing intravoksel (tiga langkah)",
                    labType = "m8b_gmn",
                    sortOrder = 6
                ),
                MriNode(
                    code = "M08B.T2.5",
                    level = 2,
                    title = "Presaturasi spasial",
                    summaryShort = "Spin yang melewati volume itu sudah jenuh, sehingga saat masuk irisan ia tidak menghasilkan sinyal.",
                    detailFull = "Presaturasi spasial mengirimkan pulsa RF selektif 90° ke pita/slab jaringan di luar (atau di atas sumber gerak dalam) FOV sesaat sebelum urutan eksitasi irisan:\n• Mekanisme: Darah yang mengalir melewati pita presaturasi menerima pulsa 90° terlebih dahulu. Ketika darah tersebut masuk ke tumpukan irisan pencitraan dan terkena pulsa eksitasi 90° sekuens, magnetisasi longitudinalnya sudah nol (jenuh) sehingga tidak menghasilkan sinyal transversal. Baik artefak ESP maupun ghosting aliran dapat ditekan.\n• Penempatan pita:\n  - Ditempatkan di sisi hulu (antara asal aliran dan volume citra).\n  - Pada irisan aksial/sagital: pita di superior (atas) memadamkan darah arteri yang turun; pita di inferior (bawah) memadamkan darah vena yang naik.\n  - Pada irisan koronal dada: pita di kanan dan kiri memadamkan aliran pembuluh subklavia.\n• Konsekuensi (Penalti): Pulsa RF 90° tambahan meningkatkan deposisi energi RF (SAR naik) dan memakan waktu dalam TR sehingga jumlah irisan maksimum berkurang. Umumnya dipakai pada citra T1W dan PD.",
                    analogi = "Seperti menghapus papan tulis tepat sebelum masuk kelas: begitu tiba di dalam ruangan, tidak ada catatan sinyal yang tersisa untuk dibaca.",
                    prerequisite = "M08B.T1.2, M07C.T2.2",
                    learningGoal = "Menentukan posisi penempatan pita presaturasi spasial untuk aliran arteri/vena dan menjelaskan konsekuensinya pada SAR serta jumlah irisan.",
                    crossLinks = "▶ Simulasi: A5 Presaturasi: animasi spin yang melewati pita",
                    labType = "m8b_presat",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M08B.T2.6",
                    level = 2,
                    title = "Ringkasan teknik kompensasi aliran",
                    summaryShort = "Even-echo rephasing dan GMN mengurangi dephasing, presaturasi meniadakan sinyal inti yang masuk.",
                    detailFull = "Perbandingan tiga teknik utama pengendalian artefak aliran:\n1. Even-echo rephasing: Pada sekuens multi-echo simetris, echo genap (TE2 = 2·TE1) mengalami rephasing otomatis untuk aliran berkecepatan konstan dibanding echo ganjil. Mengurangi dephasing intravoksel terutama pada sekuens berbobot T2.\n2. Gradient Moment Nulling (GMN): Menambahkan lobus gradien (+1, −2, +1) untuk mengoreksi pergeseran fase akibat kecepatan. Mempertahankan sinyal terang pembuluh dan menekan dephasing intravoksel pada aliran lambat laminar, dengan penalti TE minimum naik.\n3. Presaturasi spasial: Memberikan pulsa RF 90° di sisi hulu untuk memadamkan sinyal darah yang masuk (mengatasi ESP dan TOF sekaligus pada aliran cepat maupun lambat), dengan penalti peningkatan SAR dan berkurangnya jumlah irisan.",
                    analogi = "Seperti tiga alat di kotak perkakas: GMN merapikan barisan spin agar tetap terang, sedangkan presaturasi mematikan lampu spin sejak di pintu masuk.",
                    prerequisite = "M08B.T2.4, M08B.T2.5",
                    learningGoal = "Memilih teknik kompensasi aliran yang tepat (even-echo rephasing, GMN, atau presaturasi spasial) sesuai tujuan klinis pemeriksaan.",
                    crossLinks = "▶ Simulasi: A6 Latihan: sebab dan teknik perbaikan artefak aliran",
                    labType = "m8b_aliran",
                    sortOrder = 8
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M08B.T3.1",
                    level = 3,
                    title = "Dephasing intravoksel dan bilangan Reynolds",
                    summaryShort = "Fase yang saling berlawanan saling meniadakan, sehingga amplitudo sinyal voksel turun (dephasing intravoksel).",
                    detailFull = "Analisis kuantitatif aliran dan dephasing intravoksel:\n• Di dalam satu voksel yang memuat gradien kecepatan (mis. aliran laminar parabolik), spin berkecepatan berbeda memperoleh sudut fase berbeda (Δφ ∝ γ · G · v · TE²), sehingga penjumlahan vektornya melemah. Dephasing intravoksel berkurang bila menggunakan TE pendek dan ukuran voksel kecil.\n• Kecepatan penggantian penuh (replacement velocity): Kecepatan minimum agar seluruh spin jenuh di dalam irisan setebal d terganti oleh spin segar dalam satu TR adalah:\n  v = tebal irisan (d) ÷ TR\n  Contoh: tebal irisan 5 mm dan TR 500 ms menghasilkan v = 5 mm / 500 ms = 0,01 mm/ms = 1,0 cm/s.\n• Bilangan Reynolds (Re): Menentukan transisi aliran laminar ke turbulen:\n  Re = (ρ · v · D) ÷ μ\n  dengan ρ = densitas darah (~1,06 g/cm³), v = kecepatan rata-rata (cm/s), D = diameter pembuluh (cm), dan μ = viskositas (~0,035 g/(cm·s)). Jika Re < 2100 aliran bersifat laminar; jika Re > 2100 aliran berubah menjadi turbulen dengan dispersi fase acak yang tak dapat dibalik oleh GMN.",
                    analogi = "Sekelompok pelari dengan kecepatan berbeda: makin lama waktu berlari (TE) dan makin lebar lintasan yang diukur (voksel), makin sulit menentukan posisi rata-rata kelompok tersebut.",
                    prerequisite = "M08B.T2.3",
                    learningGoal = "Menghitung kecepatan penggantian penuh (v = d/TR) serta bilangan Reynolds (Re = ρ·v·D/μ, batas 2100) untuk mengevaluasi dephasing intravoksel.",
                    crossLinks = "▶ Simulasi: A3 Reynolds & A4 Dephasing intravoksel",
                    labType = "m8b_reynolds",
                    sortOrder = 9
                ),
                MriNode(
                    code = "M08B.T3.2",
                    level = 3,
                    title = "Momen gradien (M0, M1, M2) dan orde kompensasi",
                    summaryShort = "M0 (luas lobe) mengatur fase spin diam, M1 mengatur fase akibat kecepatan, M2 akibat percepatan. GMN menolkan M0 dan M1.",
                    detailFull = "Untuk partikel spin yang bergerak menurut deret gerak x(t) = x₀ + v·t + ½·a·t², akumulasi fase di sepanjang gradien G(t) dapat diuraikan menurut momen gradien ke-n (Mₙ = ∫ G(t)·tⁿ dt):\n  φ(TE) = γ · (x₀·M₀ + v·M₁ + ½·a·M₂ + ...)\n\n• M₀ (momen orde nol = luas lobus): mengatur rephasing posisi spin diam (x₀). Pada gradien baca seimbang (+1, −1), M₀ = 0 tetapi M₁ ≠ 0.\n• M₁ (momen orde pertama): mengatur fase akibat kecepatan konstan (v). Pada skema GMN tripolar dengan lobus berdurasi sama (+1, −2, +1), hasil integral memberikan:\n  M₀ = 0, M₁ = 0, sedangkan M₂ = 1 (tidak nol).\n• Karena M₁ = 0 tetapi M₂ ≠ 0, GMN orde pertama mengompensasi sempurna aliran berkecepatan konstan (first-order motion), namun aliran pulsatil arteri yang memiliki percepatan (a ≠ 0) masih menyisakan dispersi fase. Kompensasi orde lebih tinggi (menolkan M₂) membutuhkan lobus tambahan dan memperpanjang TE minimum lebih jauh.",
                    analogi = "Menyamakan posisi dua mobil: selisih kecepatan konstan mudah dihitung dan disamakan, tetapi mobil yang terus menginjak pedal gas (percepatan) tetap menyimpang.",
                    prerequisite = "M08B.T2.4, M08B.T3.1",
                    learningGoal = "Membuktikan bahwa konfigurasi lobus gradien +1, −2, +1 menghasilkan M0 = 0 dan M1 = 0 namun M2 ≠ 0.",
                    crossLinks = "▶ Simulasi: A4 Dephasing intravoksel Langkah 2 & 3",
                    labType = "m8b_gmn",
                    sortOrder = 10
                )
            )
        ),

        // =========================================================================
        // MODUL 8C: ARTEFAK PERANGKAT KERAS, CROSS-TALK & QA (9 Simpul: 6 T2, 3 T3)
        // =========================================================================
        MriModule(
            id = "M08C",
            code = "M08C",
            title = "Artefak Perangkat Keras, Cross-talk & QA",
            subtitle = "Cross-Excitation, Zipper, Shading, Moiré, Suseptibilitas Logam, QA Fantom & Tabel 8.8",
            description = "Mengenali artefak cross-excitation, zipper, shading, moiré (lonjakan derau ruang-k dan pita zebra), kegagalan peralatan, dan suseptibilitas logam/udara (SE vs GRE vs EPI), menjalankan Quality Assurance (QA) fantom terhadap baseline, serta menguasai matriks remediasi dan penalti Tabel 8.8.",
            levelBadge = "T2 - T3 & Capstone",
            iconName = "fact_check",
            nodes = listOf(
                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M08C.T2.1",
                    level = 2,
                    title = "Cross-excitation dan cross-talk",
                    summaryShort = "Pulsa RF nyata tidak persegi, sehingga tetangga ikut tereksitasi dan jenuh. Beri celah atau atur urutan eksitasi.",
                    detailFull = "Pada tumpukan multi-irisan 2D, SNR irisan bertetangga dapat turun dan kontras berubah dari irisan ke irisan:\n• Penyebab: Profil frekuensi pulsa RF nyata tidak berbentuk persegi panjang sempurna, melainkan memiliki sisi miring (side lobes). Akibatnya irisan di sebelahnya ikut menerima sebagian energi RF pada daerah tumpang tindih, mendorong proton tetangga ke bidang transversal sebelum gilirannya dieksitasi sehingga menjadi jenuh dan SNR-nya turun. Pada irisan multi-sudut yang saling berpotongan secara anatomis, daerah perpotongan tampak sebagai pita gelap bebas sinyal (cross-talk).\n• Cara mengurangi:\n  1. Beri celah antar irisan (slice gap) sedikitnya sekitar 30% dari tebal irisan.\n  2. Eksitasi irisan secara selang-seling (ganjil lalu genap dalam satu TR) agar tetangga sempat meluruh.\n  3. Gunakan akuisisi interleaved (concatenation: membagi irisan ganjil dan genap ke dua akuisisi terpisah) sehingga celah tidak diperlukan.\n  4. Perangkat lunak squaring-off RF (kompromi: sebagian sinyal hilang).\n  5. Akuisisi volume 3D (partisi fase) tidak memerlukan celah antar irisan.",
                    analogi = "Dua lampu sorot yang berkas cahayanya melebar di tepi: jika dipasang terlalu rapat, bagian tengah terkena sorotan dua kali. Memberi jarak atau menyalakannya bergantian mengatasi masalah tersebut.",
                    prerequisite = "M06B.T2.2, M04A",
                    learningGoal = "Menjelaskan penyebab cross-excitation akibat ketidaksempurnaan profil pulsa RF serta cara mengatasinya dengan slice gap ≥30% atau urutan eksitasi.",
                    crossLinks = "▶ Simulasi: A2 Celah irisan dan cross-excitation",
                    labType = "m8c_celah_irisan",
                    sortOrder = 1
                ),
                MriNode(
                    code = "M08C.T2.2",
                    level = 2,
                    title = "Artefak Zipper (kebocoran RF)",
                    summaryShort = "RF luar masuk ke penerima. Tutup pintu ruang magnet dan periksa sangkar RF.",
                    detailFull = "Zipper tampak sebagai garis padat terang-gelap putus-putus yang melintang pada citra, berorientasi tegak lurus terhadap sumbu pengodean frekuensi (berada pada satu kolom frekuensi spesifik).\n\n• Penyebab: Gelombang radiofrekuensi (RF) eksternal pada frekuensi sempit yang bertepatan dengan pita frekuensi penerimaan echo bocor masuk ke koil penerima. Setelah transformasi Fourier, interferensi frekuensi tunggal tersebut dipetakan menjadi garis sepanjang arah pengodean fase pada posisi frekuensi tersebut. Sumber pemicunya antara lain pintu ruang magnet (Zona IV) yang kurang rapat/terbuka saat akuisisi, kebocoran segel kandang Faraday, lampu penerangan/dimmer yang rusak, motor listrik aus, atau peralatan pemantau elektronik di dalam ruang pindai.\n• Cara mengurangi:\n  1. Pastikan pintu ruang magnet tertutup rapat selama akuisisi.\n  2. Matikan atau keluarkan peralatan elektronik non-esensial yang memancarkan interferensi RF.\n  3. Panggil teknisi untuk memeriksa integritas perisai kandang Faraday dan waveguide.",
                    analogi = "Radio mobil yang mendadak menangkap bunyi dengung satu stasiun liar karena pelindung antenanya terbuka.",
                    prerequisite = "M06B.T2.1",
                    learningGoal = "Mengidentifikasi tampilan artefak zipper (tegak lurus sumbu frekuensi) dan langkah penanganannya pada pintu/kandang Faraday.",
                    crossLinks = "▶ Simulasi: A4 Pemilih artefak & Tabel 8.8",
                    labType = "m8c_capstone_tabel88",
                    sortOrder = 2
                ),
                MriNode(
                    code = "M08C.T2.3",
                    level = 2,
                    title = "Artefak Shading",
                    summaryShort = "Bagian yang sama jaringannya tampak berbeda terang karena sensitivitas tidak merata.",
                    detailFull = "Shading adalah ketidakseragaman (gradasi gelap-terang) intensitas sinyal di seluruh volume citra meskipun jenis jaringannya sama (satu sisi citra tampak normal sementara sisi lainnya meredup).\n\n• Penyebab:\n  1. Eksitasi spin tidak merata di dalam tubuh karena inhomogenitas B1 (sudut flip menyimpang dari 90° dan 180°).\n  2. Pembebanan koil (coil loading) yang tidak normal, misalnya pasien bertubuh besar menyentuh langsung salah satu sisi koil sehingga terjadi kopling di titik tersebut.\n  3. Ketidakhomogenan medan magnet statis B0.\n• Cara mengurangi:\n  1. Pilih koil yang ukurannya sesuai dengan anatomi dan pasang dengan benar.\n  2. Cegah pasien menyentuh dinding koil menggunakan bantalan busa.\n  3. Pastikan kalibrasi prescan (transmit gain & center frequency) tepat.\n  4. Lakukan shimming aktif/pasif untuk meratakan B0.",
                    analogi = "Foto dengan lampu kilat yang hanya menyinari satu sudut ruangan: objeknya seragam, tetapi terangnya berat sebelah.",
                    prerequisite = "M05.T2.4, M06B.T2.3",
                    learningGoal = "Menjelaskan penyebab artefak shading (inhomogenitas B1/B0 dan pembebanan koil) serta cara pencegahannya.",
                    crossLinks = "▶ Simulasi: A4 Pemilih artefak | → M06B A3 Sensitivitas koil",
                    labType = "m8c_capstone_tabel88",
                    sortOrder = 3
                ),
                MriNode(
                    code = "M08C.T2.4",
                    level = 2,
                    title = "Artefak Moiré (lonjakan derau & pita zebra)",
                    summaryShort = "Lonjakan derau di ruang-k memberi garis sejajar, sedangkan aliasing dengan medan tidak homogen memberi pita zebra di tepi FOV.",
                    detailFull = "Moiré adalah pola interferensi bergaris yang pada MRI muncul dari dua mekanisme berbeda:\n\n1. Lonjakan derau di ruang-k (k-space spike noise / herringbone):\n   • Satu semburan derau sesaat (misalnya pelepasan listrik statis di udara kering atau percikan pada konektor kabel koil yang longgar) terekam sebagai satu titik beramplitudo sangat tinggi (spike) di ruang-k. Setelah transformasi Fourier 2D, satu titik di ruang-k berubah menjadi gelombang kosinus berupa garis-garis sejajar di seluruh citra.\n   • Jarak dan sudut garis ditentukan oleh posisi spike di ruang-k: dekat pusat ruang-k menghasilkan garis renggang; jauh dari pusat menghasilkan garis rapat. Dua spike menghasilkan pola silang seperti anyaman kain (herringbone/criss-cross).\n   • Pencegahan: Jaga kelembapan ruangan dalam batas normal dan pastikan konektor koil terpasang kencang.\n2. Pita zebra di tepi FOV (banding artifact):\n   • Muncul pada sekuens gradient echo (terutama bSSFP) di tepi FOV akibat gabungan aliasing dan ketidakhomogenan medan B0 di tepi bore, yang membuat sinyal terlipat berinterferensi konstruktif dan destruktif (belang hitam-putih).\n   • Cara mengurangi: Gunakan sekuens spin echo, perbaiki shimming, dan aktifkan anti-aliasing.",
                    analogi = "Dua tirai jaring yang ditumpuk memperlihatkan pola garis bergelombang; satu titik noda di ruang-k menenun garis di seluruh kanvas citra.",
                    prerequisite = "M04B.T2.1",
                    learningGoal = "Membedakan garis moiré akibat spike noise di ruang-k dari pita zebra di tepi FOV pada GRE beserta pencegahannya.",
                    crossLinks = "▶ Simulasi: A3 Lonjakan derau di ruang-k (moiré)",
                    labType = "m8c_spike_moire",
                    sortOrder = 4
                ),
                MriNode(
                    code = "M08C.T2.5",
                    level = 2,
                    title = "Kegagalan peralatan (gradien, arus eddy & array processor)",
                    summaryShort = "Kehilangan gradien menimbulkan distorsi, arus eddy menimbulkan pergeseran fase, dan galat akuisisi data menimbulkan pita sinyal hilang. Ulangi pemindaian dulu.",
                    detailFull = "Sebagian artefak berasal dari malfungsi perangkat keras, bukan dari pengaturan protokol:\n• Hilangnya salah satu sumbu gradien menyebabkan distorsi geometris berat atau kolapsnya citra.\n• Arus eddy berlebih yang terinduksi pada struktur konduktor di sekitar kumparan gradien menimbulkan pergeseran fase tambahan dan distorsi tepi.\n• Galat akuisisi atau pengolah data (array processor) menimbulkan artefak geometris berbatas tegas, seperti pita gelap kehilangan sinyal yang memotong citra.\n\nLangkah penanganan: Ulangi pemindaian terlebih dahulu untuk memastikan apakah galat bersifat transien. Jika artefak tetap muncul di tempat yang sama, hentikan pemeriksaan klinis pada unit tersebut dan panggil teknisi servis.",
                    analogi = "Printer yang salah satu jarum cetaknya macet: hasilnya bergaris kosong di tempat yang sama dan bukan salah dokumennya.",
                    prerequisite = "M06A.T2.1, M06B.T2.4",
                    learningGoal = "Mengenali ciri artefak kegagalan perangkat keras (gradien, arus eddy, array processor) dan prosedur eskalasi teknisnya.",
                    crossLinks = "▶ Simulasi: A4 Pemilih artefak & A1 QA Fantom",
                    labType = "m8c_capstone_tabel88",
                    sortOrder = 5
                ),
                MriNode(
                    code = "M08C.T2.6",
                    level = 2,
                    title = "Artefak suseptibilitas magnetik dan logam",
                    summaryShort = "Logam dan antarmuka jaringan-udara membuat B0 tidak seragam, sehingga terjadi kehilangan sinyal (area gelap) dan distorsi geometri. Spin echo lebih tahan daripada gradient echo dan EPI.",
                    detailFull = "Suseptibilitas magnetik adalah derajat termagnetisasinya suatu bahan di dalam medan B0. Di perbatasan bahan dengan suseptibilitas sangat berbeda (jaringan–udara di sinus/basis cranii, atau jaringan–implan logam), medan B0 lokal terdistorsi tajam sehingga frekuensi Larmor menyimpang:\n\n• Dua dampak utama:\n  1. Kehilangan sinyal (signal void / blooming): Gradien medan mikro di dalam voksel membuat fase spin cepat menyebar (dephasing intravoksel T2*). Pada Spin Echo (SE/TSE), pulsa RF 180° me-rephase dephasing statis ini. Pada Gradient Echo (GRE) dan EPI tidak ada pulsa 180°, sehingga area gelap membesar seiring peningkatan TE.\n  2. Distorsi geometri dan penumpukan sinyal (pile-up): Penyimpangan frekuensi Δf dibaca sistem sebagai pergeseran posisi (Geser = Δf ÷ BW_piksel). Pada EPI, bandwidth per piksel pada arah fase sangat kecil sehingga distorsi geometris pada arah fase sangat parah.\n• Cara mengurangi:\n  - Gunakan SE atau TSE/FSE alih-alih GRE atau EPI.\n  - Perpendek TE dan naikkan bandwidth penerimaan (kompromi: SNR turun).\n  - Gunakan irisan tipis (voksel kecil) dan pindai pada B0 lebih rendah (1,5 T vs 3 T).\n  - Tukar sumbu fase dan frekuensi agar arah distorsi menjauhi struktur vital.\n• Manfaat klinis: Pada GRE/SWI, efek suseptibilitas justru dimanfaatkan untuk mendeteksi mikro-perdarahan (hemosiderin) atau kalsifikasi.",
                    analogi = "Seperti kompas di dekat bongkahan besi: jarumnya melenceng dari utara. Spin echo mengoreksi arah jarum di tengah jalan dengan pulsa 180°, sedangkan gradient echo membiarkannya terus menyimpang.",
                    prerequisite = "M03A, M05.T1.3",
                    learningGoal = "Membandingkan besarnya signal loss dan distorsi suseptibilitas logam/udara antara sekuens SE, GRE, dan EPI terhadap perubahan TE, BW, dan B0.",
                    crossLinks = "▶ Simulasi: A6 Logam dan udara: bandingkan SE, GRE, EPI",
                    labType = "m8c_logam_udara",
                    sortOrder = 6
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M08C.T3.1",
                    level = 3,
                    title = "Urutan eksitasi irisan (Selang-seling vs Interleaved)",
                    summaryShort = "Selang-seling memberi jeda sekitar setengah TR, interleaved memberi jeda satu akuisisi penuh.",
                    detailFull = "Urutan eksitasi irisan menentukan berapa lama waktu pemulihan longitudinal yang dimiliki tepi irisan tetangga yang terkena cross-excitation:\n• Eksitasi Berurutan (Sequential: 1, 2, 3, 4, 5, 6, 7, 8): Irisan tetangga langsung dieksitasi sesaat setelah irisan sebelumnya, sehingga jeda peluruhannya nyaris nol dan saturasi silang paling berat.\n• Eksitasi Selang-seling dalam satu TR (Alternating / Odd-Even in single TR: 1, 3, 5, 7 lalu 2, 4, 6, 8): Irisan ganjil dieksitasi pada paruh pertama TR dan irisan genap pada paruh kedua TR. Tepi irisan genap memiliki waktu sekitar ½ TR untuk pulih sebelum dieksitasi (tetap membutuhkan celah irisan kecil).\n• Akuisisi Interleaved (Concatenation = 2 akuisisi): Irisan ganjil diakuisisi seluruhnya pada akuisisi pertama, kemudian irisan genap pada akuisisi kedua. Jeda pemulihan antar irisan bertetangga adalah satu waktu akuisisi penuh (beberapa menit), sehingga celah antar irisan (0% gap / contiguous slices) dapat digunakan tanpa cross-excitation, namun waktu pemindaian menjadi dua kali lipat.",
                    analogi = "Dua kelompok pekerja yang bergantian memakai jalur sempit: jika dijadwalkan pada hari yang berbeda (interleaved), mereka tidak pernah berpapasan meskipun tanpa pembatas jalan.",
                    prerequisite = "M08C.T2.1",
                    learningGoal = "Membandingkan jeda peluruhan dan konsekuensi waktu pemindaian antara eksitasi berurutan, selang-seling (½ TR), dan interleaved (2 akuisisi tanpa celah).",
                    crossLinks = "▶ Simulasi: A2 Celah irisan dan cross-excitation",
                    labType = "m8c_celah_irisan",
                    sortOrder = 7
                ),
                MriNode(
                    code = "M08C.T3.2",
                    level = 3,
                    title = "Quality Assurance (QA) dengan fantom",
                    summaryShort = "Ukur metrik yang sama berulang kali, bandingkan dengan baseline, dan laporkan jika menyimpang.",
                    detailFull = "Fantom QA adalah objek uji standar berisi larutan dengan sifat relaksasi dan dimensi geometris yang telah diketahui secara presisi. Pemindaian fantom secara rutin memantau performa pesawat sebelum penyimpangan merugikan citra klinis:\n\n• Lima metrik utama QA Fantom:\n  1. SNR (Signal-to-Noise Ratio): Sinyal rata-rata ROI di pusat fantom dibagi simpangan baku (SD) derau pada ROI latar belakang sudut.\n  2. Uniformitas (Integral Uniformity): Kerataan intensitas sinyal di seluruh penampang fantom homogen, dihitung dari 100% × (1 − (S_max − S_min)/(S_max + S_min)).\n  3. Akurasi Geometris: Kesesuaian diameter terukur pada citra (batas ambang 50% sinyal pusat) terhadap diameter fisik fantom sebenarnya.\n  4. Ketebalan Irisan: Diukur dari lebar profil sinyal pada struktur baji/ramp miring di dalam fantom.\n  5. Rasio Ghosting (Percent Signal Ghosting): Perbandingan intensitas bayangan di latar arah fase terhadap sinyal utama fantom.\n• Baseline & Tren: Hasil pengukuran saat penerimaan alat (acceptance testing) disimpan sebagai baseline. Pemantauan tren penyimpangan terhadap ambang toleransi menentukan kapan teknisi servis harus dipanggil.",
                    analogi = "Menimbang satu anak timbangan standar setiap pagi: jika angkanya melenceng dari catatan awal (baseline), yang perlu dikalibrasi adalah timbangannya.",
                    prerequisite = "M08C.T2.5",
                    learningGoal = "Menghitung dan mengevaluasi metrik QA fantom (SNR, uniformitas, akurasi geometris, ghosting) terhadap nilai baseline dan ambang toleransi.",
                    crossLinks = "▶ Simulasi: A1 Simulasi QA fantom",
                    labType = "m8c_qa_fantom",
                    sortOrder = 8
                ),
                MriNode(
                    code = "M08C.T3.3",
                    level = 3,
                    title = "Kompromi & harga setiap perbaikan artefak (Tabel 8.8)",
                    summaryShort = "Untuk artefak aliran, penaltinya antara lain antialiasing (tukar sumbu), TR dan kontras bervariasi (gating), kehilangan irisan (presaturasi), dan TE minimum naik (GMN).",
                    detailFull = "Setiap tindakan perbaikan artefak di konsol MRI memiliki konsekuensi atau \"harga\" (penalti parameter) sesuai Tabel 8.8:\n• Gerak aliran (Sumbu Fase):\n  - Tukar sumbu fase & frekuensi → Penalti: mungkin memerlukan antialiasing pada sumbu fase baru.\n  - Gating / triggering → Penalti: TR bervariasi, kontras citra bervariasi, waktu pindai bertambah.\n  - Presaturasi spasial → Penalti: kehilangan irisan maksimum per TR dan peningkatan SAR.\n  - Gradient moment rephasing (GMN) → Penalti: TE minimum naik.\n• Pergeseran kimia (Sumbu Frekuensi):\n  - Naikkan bandwidth penerima → Penalti: SNR menurun (meski TE minimum berkurang).\n  - Perkecil FOV → Penalti: SNR menurun (resolusi meningkat).\n  - Saturasi kimia (fat-sat) → Penalti: SNR berkurang, mungkin kehilangan irisan.\n• Pembatalan luar-fase (Frekuensi & Fase):\n  - Pilih TE in-phase → Penalti: mungkin kehilangan jumlah irisan bila TE dinaikkan banyak.\n• Aliasing (Frekuensi & Fase):\n  - Tanpa frequency wrap (filter low-pass) → Penalti: Tidak ada.\n  - Antialiasing / phase oversampling → Penalti: waktu pemindaian dapat naik atau artefak gerak bertambah bila NSA dibagi dua.\n  - Pita presaturasi → Penalti: SAR naik.\n• Eksitasi silang (Pilih Irisan):\n  - Interleaving → Penalti: waktu pemindaian dua kali lipat.\n  - Squaring-off pulsa RF → Penalti: SNR menurun.\n• Kerentanan magnetik: Gunakan SE/TSE/FSE → Penalti: produk darah (hemosiderin) dapat terlewat; Singkirkan logam → Penalti: Tidak ada.\n• Shading, Moiré (pakai SE), dan Magic Angle (ubah posisi/TE sedikit) memiliki opsi perbaikan tanpa penalti langsung.",
                    analogi = "Seperti meresepkan obat: hampir setiap tindakan punya efek samping, sehingga radiografer harus memilih kompromi yang paling menguntungkan diagnosis.",
                    prerequisite = "M08A, M08B, M08C.T2.1",
                    learningGoal = "Mencocokkan setiap jenis artefak dengan sumbu terdampak, teknik remediasi, dan penalti spesifiknya berdasarkan Tabel 8.8.",
                    crossLinks = "▶ Simulasi: A4 Pemilih artefak dan perbaikannya (Tabel 8.8), A5 Tebak artefak",
                    labType = "m8c_capstone_tabel88",
                    sortOrder = 9
                )
            )
        ),
        MriModule(
            id = "M09",
            code = "M09",
            title = "Optimasi Protokol & Membaca Layar Konsol",
            subtitle = "Parameter Konsol, Kalkulasi TA (2D/TSE/3D), SNR, Voksel, Parallel Imaging & Tab Vendor",
            description = "Menjelaskan arti parameter di layar konsol, menghitung waktu pindai (TA) 2D dan mengoreksinya untuk TSE dan 3D, memperkirakan perubahan SNR, menilai kompromi (trade-off) parameter, serta menunjukkan fungsi tab konsol dan pemetaan istilah antar-vendor (Siemens, GE, Philips).",
            levelBadge = "T1 - T3 Lengkap",
            iconName = "settings",
            nodes = listOf(
                // ================= TINGKAT 1 (DASAR) =================
                MriNode(
                    code = "M09.T1.1",
                    level = 1,
                    title = "T1.1 Parameter yang tampil di konsol",
                    summaryShort = "Hampir semua protokol dapat dibaca dari enam angka: TR, TE, flip angle, FoV, matriks, dan ketebalan irisan.\nTR/TE mengatur kontras dan waktu; flip angle mengatur besar tip magnetisasi; FoV dan matriks menentukan ukuran piksel; ketebalan melengkapi volume voksel.",
                    detailFull = "TR (waktu ulang) menentukan seberapa pulih magnetisasi longitudinal antar pulsa dan ikut menentukan jumlah irisan serta TA. TE menentukan bobot T2: TE panjang menambah kontras T2 tetapi menurunkan SNR. Flip angle mengatur besar tip magnetisasi. FoV dan matriks menentukan ukuran piksel (FoV/matriks), sedangkan ketebalan irisan melengkapi volume voksel. Perlu diingat: matriks fase mengubah TA, matriks frekuensi tidak.\n\nRujukan parameter: M01 & M02",
                    analogi = "Analogi: Layar konsol seperti dasbor mobil: TR/TE adalah gigi yang mengubah karakter gambar, FoV/matriks adalah kerapatan jendela mozaik, dan TA adalah argo yang terus berjalan.",
                    labType = "ta_snr"
                ),
                MriNode(
                    code = "M09.T1.2",
                    level = 1,
                    title = "T1.2 Waktu pindai (TA)",
                    summaryShort = "TA 2D = TR × N_fase × NEX.\nTiga faktor ini menentukan TA. Matriks frekuensi tidak ikut menaikkan TA.",
                    detailFull = "Rumus: TA 2D = TR × N_fase × NEX\n\nSetiap langkah pengodean fase membutuhkan satu TR, dan seluruh baris diulang sebanyak NEX. Karena itu menaikkan matriks fase menaikkan TA, sedangkan menaikkan matriks frekuensi tidak (data frekuensi dibaca sekaligus dalam satu pembacaan). Rumus ini berlaku untuk 2D konvensional; koreksi untuk TSE dan 3D ada di T3.1.\n\nRujukan ruang-k: M04B",
                    analogi = "Analogi: Setiap baris fase seperti satu ketukan pintu: TR adalah jeda antar ketukan, NEX berapa kali diulang. Menambah kolom frekuensi tidak menambah ketukan, hanya menambah lubang pada kertas.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T1.3",
                    level = 1,
                    title = "T1.3 Contoh protokol sederhana",
                    summaryShort = "Protokol adalah daftar urutan pemindaian dalam satu pemeriksaan. Protokol menggabungkan beberapa pembobotan agar saling melengkapi.",
                    detailFull = "Gambaran untuk otak: pemindaian pengenal lokasi, lalu citra berbobot T1, berbobot T2, dan satu citra inversion recovery yang menekan sinyal cairan. Tiap citra menjawab pertanyaan kontras yang berbeda.\n\nIni ilustrasi belajar. Protokol nyata ditentukan fasilitas dan tim klinisnya.\n\nContoh kalkulasi parameter protokol dasar: TR 500 ms, FoV 240 mm, matriks 256 × 192, irisan 5 mm, NEX 2 memberi TA = 0,5 s × 192 × 2 = 192 s dan voksel sekitar 5,9 mm³. Bila NEX menjadi 1, TA menjadi 96 s dan SNR sekitar 0,71×. Angka ini ilustrasi, bukan protokol klinis.",
                    analogi = "Analogi: Seperti memotret satu objek dengan beberapa filter. Tiap filter memperlihatkan hal yang berbeda.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T1.4",
                    level = 1,
                    title = "T1.4 Manajemen pasien (pilih koil, minimalkan TA)",
                    summaryShort = "Pilih koil yang benar, buat pasien nyaman, jaga TA sependek mungkin.\nSemakin lama pasien di meja, semakin besar peluang bergerak (bukan hanya pasien gelisah).",
                    detailFull = "Pasien yang tidak nyaman bergerak. Protokol pabrik tidak otomatis optimal; tinjau secara berkala dan konfirmasi protokol dengan radiolog sebelum pindai. Koil yang pas ukurannya menaikkan SNR sehingga TA bisa dipendekkan. Optimasi klinis: pada pasien tidak kooperatif, prioritaskan pemendekan TA dengan mempertahankan SNR diagnostik memadai.",
                    analogi = "Analogi: Seperti memotret anak kecil: makin lama menunggu, makin besar peluang ia bergerak, jadi siapkan posisi nyaman dulu lalu ambil gambar secepat yang masih memadai.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),

                // ================= TINGKAT 2 (MENENGAH) =================
                MriNode(
                    code = "M09.T2.1",
                    level = 2,
                    title = "T2.1 SNR dan voksel",
                    summaryShort = "SNR ∝ volume voksel × √(NEX · N_fase / BW).\nVoksel besar, NEX banyak, bandwidth sempit = SNR tinggi. FoV adalah pengendali SNR terkuat.",
                    detailFull = "Rumus: SNR ∝ voksel × √(NEX · N_fase / BW)\n\nPada MRI, SNR adalah faktor kualitas paling penting; resolusi bagus tidak berguna bila SNR buruk. Saat FoV mengecil (matriks tetap), piksel mengecil pada kedua sumbu, maka FoV menjadi pengendali SNR paling kuat (contoh: FoV 160 mm vs 80 mm). Area dengan sinyal bawaan baik (otak) memungkinkan matriks halus dan NEX kecil; area sinyal rendah (paru) butuh NEX lebih banyak dan matriks lebih kasar. Voksel = FoV/matriks; interpolasi (zero-fill) memperhalus tampilan tetapi tidak menambah resolusi sejati.\n\nRujukan SNR: M04C",
                    analogi = "Analogi: Voksel seperti ember hujan: ember lebar menampung banyak air (sinyal) tetapi kurang rinci menunjukkan di mana hujan jatuh. Noise adalah percikan acak; mengulang pengukuran meratakannya.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T2.2",
                    level = 2,
                    title = "T2.2 Resolusi naik tanpa tambah TA (FoV asimetris)",
                    summaryShort = "Dua cara menaikkan resolusi tanpa menambah TA.\n(1) Naikkan matriks frekuensi saja. (2) Pakai FoV asimetris: FoV fase diperkecil, TA turun sebanding, resolusi tetap.",
                    detailFull = "Resolusi biasanya naik dengan menaikkan matriks fase, dan itu menambah TA. Cara lain: menaikkan matriks frekuensi saja, atau memakai FoV asimetris (rectangular FoV) yang memperkecil FoV arah fase sehingga langkah fase berkurang sebanding, selagi ukuran piksel tetap. Cocok untuk anatomi yang muat dalam persegi panjang.",
                    analogi = "Analogi: Seperti memotret dengan lebar bingkai yang lebih sempit: detail sama, tetapi frame yang perlu diisi lebih sedikit sehingga lebih cepat.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T2.3",
                    level = 2,
                    title = "T2.3 Kompromi (trade-off)",
                    summaryShort = "Ideal: SNR tinggi, resolusi baik, waktu singkat. Mengubah satu parameter selalu memengaruhi yang lain.\nTidak ada pengaturan yang menang di semua sisi; pilih sesuai tujuan pemeriksaan (prinsip selimut sempit).",
                    detailFull = "Ideal: SNR tinggi, resolusi baik, waktu singkat. Mengubah satu parameter selalu memengaruhi yang lain:\n• NEX naik: SNR naik, TA naik.\n• Ketebalan irisan naik: SNR naik, resolusi irisan turun, partial voluming naik.\n• Bandwidth turun: SNR naik, tetapi pergeseran kimia dan TE minimum naik.\n• TR naik: SNR dan jumlah irisan naik (sampai pemulihan penuh), kontras T1 turun, TA naik.\n• TE naik: kontras T2 naik, SNR turun.\n• Koil besar: area sinyal luas tetapi SNR lebih rendah; koil kecil: SNR tinggi, area terbatas.\n\nSumber: Westbrook, Tabel 7.6. Dari M04: menaikkan SNR lewat NEX memperpanjang TA dan hasilnya hanya naik sebesar akar. Parallel imaging dan partial Fourier memangkas waktu dengan harga SNR (lihat T3.2).\n\nMitigasi SAR & Artefak: M07C & M08C",
                    analogi = "Analogi: Seperti selimut sempit: ditarik menutupi kaki (SNR), terbuka bahu (resolusi). Tugas radiografer memilih bagian mana yang harus tertutup.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T2.4",
                    level = 2,
                    title = "T2.4 Tab konsol (contoh antarmuka)",
                    summaryShort = "Parameter dikelompokkan dalam tab menurut fungsinya.\nRoutine, Contrast, Resolution, Geometry, System, Physio, Inline, Sequence. Susunan ini contoh antarmuka, bukan aturan universal.",
                    detailFull = "Antarmuka konsol Siemens (Routine, Contrast, Resolution, Geometry, System, Physio, Inline, Sequence). Pada unit lain nama dan letak tab berbeda; pemetaan nama antar-vendor ada di T3.3.",
                    analogi = "Analogi: Seperti laci perkakas berlabel: semua alat sama, hanya dikelompokkan agar mudah dicari.",
                    labType = "console_tabs",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T2.5",
                    level = 2,
                    title = "T2.5 Sistem komputer",
                    summaryShort = "Opsional di modul ini; materi lengkapnya dipindah ke M05 T3.2.\nHost computer = sutradara (GUI, identitas, PACS). Pulse generator module (PGM) = asisten yang mengendalikan RF dan gradien.",
                    detailFull = "Host computer (GUI) menghubungkan identitas pasien lewat jaringan ke RIS, menyimpan umur, berat, jenis kelamin, dan orientasi untuk menghitung amplitudo RF dan label gambar. PGM mengirim instruksi waktu, amplitudo, bentuk RF, jendela sampling, serta mengendalikan gradien dan sensor fisiologis. Modul pemroses gambar mempercepat rekonstruksi. Gambar disimpan dalam format DICOM pada server berarray redundan dan diakses lewat PACS.\n\n→ Ringkasan singkat ada di M05 T3.2. Rincian lengkap tetap di sini.",
                    analogi = "Analogi: Host computer adalah sutradara; PGM adalah asisten panggung yang menghitung detik demi detik kapan lampu (RF) dan efek (gradien) menyala.",
                    labType = null,
                    htmlSimulationAsset = "modul9-native.html"
                ),

                // ================= TINGKAT 3 (LANJUT) =================
                MriNode(
                    code = "M09.T3.1",
                    level = 3,
                    title = "T3.1 Koreksi rumus TA untuk TSE dan 3D",
                    summaryShort = "TA umum = TR × N_fase × NEX × N_partisi ÷ ETL.\nTSE: TA dibagi turbo factor (ETL). 3D: TA dikali jumlah partisi. 2D konvensional: ETL = 1 dan partisi = 1.",
                    detailFull = "Rumus: TA = TR × N_fase × NEX × N_partisi ÷ ETL\n\nContoh TSE: TR 4000 ms, 256 langkah fase, NEX 1, ETL 16 memberi TA = 4 × 256 ÷ 16 = 64 s. Contoh 3D: TR 20 ms, 128 fase, 64 partisi, NEX 1 memberi TA = 0,02 × 128 × 64 ≈ 164 s. Pada 3D, SNR juga ikut naik dengan akar jumlah partisi. ",
                    analogi = "Analogi: Seperti memindahkan banyak barang dengan satu perjalanan membawa beberapa kardus (ETL), sementara 3D seperti menambah jumlah lantai yang harus dikerjakan.",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T3.2",
                    level = 3,
                    title = "T3.2 Parallel imaging: dampak pada TA dan SNR",
                    summaryShort = "iPAT, SENSE, dan ASSET memakai banyak elemen koil untuk melewati sebagian langkah fase.\nFaktor akselerasi R membagi TA dengan R, tetapi SNR turun minimal √R.",
                    detailFull = "Rumus: TA = TR × N_fase × NEX × N_partisi ÷ (ETL × R) | SNR ∝ voksel × √(NEX · N_fase · N_partisi / BW / R)\n\nIdeal: TA ÷ R dan SNR ÷ √R. Di praktik ada penalti tambahan (g-factor, g ≥ 1) sehingga SNR turun lebih banyak, dan R terlalu besar menimbulkan artefak aliasing sisa. Pengurangan SNR akibat noise amplifikasi (g-factor) menjadi limitasi rasio akselerasi tinggi.\n\nRujukan akselerasi: M04C",
                    analogi = "Analogi: Seperti beberapa orang membaca bagian buku berbeda lalu digabung: lebih cepat, tetapi sambungannya tidak selalu mulus (g-factor).",
                    labType = "ta_snr",
                    htmlSimulationAsset = "modul9-native.html"
                ),
                MriNode(
                    code = "M09.T3.3",
                    level = 3,
                    title = "T3.3 Nama tab dan parameter antar-vendor",
                    summaryShort = "Fungsi sama, nama berbeda.\nPerbandingan terminologi parameter utama lintas platform vendor Siemens, GE, dan Philips.",
                    detailFull = "Satu fungsi (misalnya akselerasi paralel) dapat bernama iPAT, ASSET, atau SENSE. Nama juga dapat berubah antar versi perangkat lunak, jadi selalu cocokkan dengan manual unit yang dipakai di tempat praktik.\n\nPemetaan istilah antar-vendor (Siemens | GE | Philips):\n• Rata-rata sinyal: Averages | NEX | NSA\n• Akselerasi paralel: iPAT (mis. GRAPPA) | ASSET / ARC | SENSE\n• Jumlah echo per TR (turbo): Turbo factor | Echo train length (ETL) | TSE factor\n• Bandwidth: Bandwidth (Hz/piksel) | Receive bandwidth (kHz) | Water-fat shift (piksel)\n• FoV arah fase: FoV phase (%) | Phase FOV | Rectangular FOV (%)\n• Pengurangan langkah fase: Phase partial Fourier | Fractional NEX | Half scan",
                    analogi = "Analogi: Seperti satu perintah dengan nama berbeda di tiap bahasa: fungsinya sama, kata kuncinya lain.",
                    labType = "console_tabs",
                    htmlSimulationAsset = "modul9-native.html"
                )
            )
        )
    )

    val glossary = listOf(
        GlossaryItem("Frekuensi Presesi Larmor", "f₀", "M01", "M01.T1.6", "Frekuensi presesi kuantum momen magnetik inti hidrogen (¹H) di bawah induksi fluks medan magnet statis eksternal B₀: f₀ = γ · B₀ (di mana γ = 42,58 MHz/T untuk ¹H)."),
        GlossaryItem("Vektor Magnetisasi Netto (NMV)", "M₀ / NMV", "M01", "M01.T1.5", "Vektor makroskopik total hasil penjumlahan seluruh momen magnetik spin kuantum proton yang memiliki kelebihan populasi paralel (spin-up / energi rendah) di dalam medan B₀."),
        GlossaryItem("Nutasi Eksitasi RF", "θ / α", "M01", "M01.T2.2A", "Gerak rotasi spiral vektor NMV dari sumbu longitudinal z rebah menuju bidang transversal xy akibat interaksi medan magnet osilasi B1 frekuensi radio resonansi."),
        GlossaryItem("Sudut Flip (Tip Angle)", "α / θ", "M01", "M01.T2.2B", "Sudut perpindahan NMV dari arah B0 setelah eksitasi RF: θ = γ · B1 · τ."),
        GlossaryItem("FID (Free Induction Decay)", "FID", "M01", "M01.T2.3A", "Sinyal radiofrekuensi transien yang diinduksikan ke koil penerima segera setelah pulsa RF 90° dinonaktifkan dan meluruh akibat dephasing."),
        GlossaryItem("Distribusi Boltzmann", "N⁺/N⁻", "M01", "M01.T3.1", "Persamaan kesetimbangan termal N⁺/N⁻ = e^(−ΔE/kT) yang menentukan kelebihan populasi spin energi rendah (~4,5 per sejuta pada 1,5 T; ~10 per sejuta pada 3 T)."),
        GlossaryItem("Kerangka Berputar (Rotating Frame)", "ω₀", "M01", "M01.T3.5", "Kerangka acuan koordinat yang ikut berputar pada frekuensi Larmor sehingga B1 tampak diam dan nutasi NMV terlihat sebagai putaran sederhana."),
        GlossaryItem("Relaksasi Longitudinal T1 (Spin-Lattice)", "T1", "M02", "M02.T1.2", "Waktu pemulihan eksponensial magnetisasi longitudinal Mz hingga mencapai 63% dari nilai kesetimbangan awal M₀ melalui pertukaran energi termal ke kisi molekuler (lattice)."),
        GlossaryItem("Relaksasi Transversal T2 (Spin-Spin)", "T2", "M02", "M02.T2.1", "Waktu peluruhan eksponensial magnetisasi transversal Mxy hingga tersisa 37% dari nilai awalnya akibat dephasing intermolekuler murni interaksi dipol-dipol antar-spin."),
        GlossaryItem("Peluruhan Transversal T2*", "T2*", "M02", "M02.T2.1", "Laju peluruhan magnetisasi transversal teramati yang jauh lebih cepat daripada T2 murni akibat kombinasi interaksi spin-spin acak dan inhomogenitas medan magnet statis lokal (1/T2* = 1/T2 + γ·ΔB₀)."),
        GlossaryItem("Waktu Repetisi / Repetition Time (TR)", "TR", "M02", "M02.T1.4", "Interval waktu ekstrinsik antar pulsa RF eksitasi berturut-turut pada irisan anatomi yang sama (satuan milidetik ms). Mengontrol derajat pemulihan T1 dan pembobotan kontras T1-weighted."),
        GlossaryItem("Waktu Echo / Echo Time (TE)", "TE", "M02", "M02.T1.4", "Interval waktu dari puncak pulsa RF eksitasi 90° hingga puncak pencatatan sinyal echo pada koil penerima (ms). Mengontrol derajat peluruhan dephasing T2 dan pembobotan T2-weighted."),
        GlossaryItem("Time of Inversion (TI)", "TI", "M03A", "M03A.T1.3", "Interval waktu jeda antara pulsa RF inversi awal 180° dengan pulsa RF eksitasi 90° pada sekuens Inversion Recovery untuk mengeksploitasi titik null jaringan spesifik (STIR / FLAIR)."),
        GlossaryItem("SAR (Specific Absorption Rate)", "SAR", "M07C", "M07C.T2.2", "Laju daya radiofrekuensi yang diserap per satuan massa jaringan tubuh (W/kg). Batas mode normal seluruh tubuh adalah 2 W/kg dan tingkat satu 4 W/kg."),
        GlossaryItem("Zona Keselamatan ACR (Zona I–IV)", "Zona I-IV", "M07A", "M07A.T1.1", "Pembagian 4 area keselamatan fasilitas MRI menurut ACR: Zona I (umum), Zona II (skrining), Zona III (terkontrol ketat), dan Zona IV (ruang pemindai magnet)."),
        GlossaryItem("Standar Label ASTM (MR Safe / Conditional / Unsafe)", "ASTM", "M07A", "M07A.T2.3", "Klasifikasi perangkat/implan: MR Safe (kotak hijau, aman di semua kondisi), MR Conditional (segitiga kuning, aman dengan syarat spesifik), dan MR Unsafe (lingkaran merah dicoret)."),
        GlossaryItem("Nephrogenic Systemic Fibrosis (NSF)", "NSF", "M07A", "M07A.T2.6", "Fibrosis sistemik kulit dan organ dalam yang berkaitan dengan pemberian agen kontras gadolinium pada pasien gagal ginjal berat (eGFR < 30) atau cedera ginjal akut."),
        GlossaryItem("Garis 5 Gauss (0,5 mT)", "5 G / 0,5 mT", "M07B", "M07B.T2.1", "Garis batas medan tepi magnet (0,5 mT; alternatif 0,9 mT) yang menandai batas aman bagi pacu jantung dan risiko proyektil ferromagnetik."),
        GlossaryItem("Efek Lenz", "Hukum Lenz", "M07B", "M07B.T2.6", "Arus eddy terinduksi pada konduktor non-ferrous (seperti aluminium/tembaga) yang bergerak memotong medan magnet statis sehingga menimbulkan gaya hambat berlawanan arah gerak."),
        GlossaryItem("Efek Antena (Resonansi Kawat λ/2)", "λ/2", "M07C", "M07C.T2.4", "Resonansi gelombang berdiri RF pada kabel/kawat konduktor sepanjang setengah panjang gelombang (~29 cm di dalam darah pada 1,5 T) yang memanaskan ujung kawat (antinode) hingga 80 °C."),
        GlossaryItem("Stimulasi Saraf Tepi (PNS)", "dB/dt", "M07C", "M07C.T2.6", "Depolarisasi saraf tepi akibat perubahan gradien medan magnet terhadap waktu (dB/dt) yang cepat; dibatasi ≤ 80% ambang rata-rata pada mode normal."),
        GlossaryItem("Ruang-k (k-Space)", "k", "M04B", "M04B.T2.1", "Matriks digital penyimpanan data mentah spasial-frekuensi sebelum dikonversi menjadi citra visual oleh algoritma FFT."),
        GlossaryItem("FFT (Fast Fourier Transform)", "FFT", "M04B", "M04B.T2.1", "Operasi matematika cepat yang mentransformasikan data gelombang frekuensi dan fase di ruang-k menjadi citra visual piksel 2D."),
        GlossaryItem("Aliasing (Wrap-around)", "Artefak", "M08A", "M08A.T1.2", "Artefak lipatan anatomi di luar FoV yang masuk ke sisi berlawanan citra karena pelanggaran Teorema Nyquist."),
        GlossaryItem("Chemical Shift (Bas-relief)", "3,5 ppm", "M08A", "M08A.T1.4", "Pergeseran spasial relatif semu antara jaringan lemak dan air sebesar ~3,5 ppm (~224 Hz pada 1,5 T; ~447 Hz pada 3 T) sepanjang sumbu frekuensi akibat self-shielding elektron."),
        GlossaryItem("Phase Mismapping (Ghosting)", "Artefak", "M08A", "M08A.T1.3", "Bayangan berulang anatomi yang bergerak sepanjang sumbu pengodean fase akibat perbedaan posisi antar-TR (Jarak = TR × N_fase × NSA / T_gerak)."),
        GlossaryItem("Out-of-Phase (India Ink)", "TE", "M08A", "M08A.T2.3", "Pembatalan sinyal di dalam voksel yang memuat lemak dan air sekaligus saat fase keduanya berlawanan 180° pada sekuens GRE tanpa pergeseran posisi."),
        GlossaryItem("Gibbs Ringing (Truncation)", "Ruang-k", "M08A", "M08A.T2.5", "Garis terang-gelap sejajar tepi berkontras tajam akibat pemotongan pengambilan data frekuensi tinggi di tepi ruang-k."),
        GlossaryItem("ETL (Echo Train Length)", "ETL", "M09", "M09.T1.2", "Jumlah echo yang direkam dalam satu TR pada sekuens Turbo Spin Echo. Mempercepat akuisisi sebanding nilainya."),
        GlossaryItem("DWI (Diffusion Weighted Imaging)", "DWI", "M03C", "M03C.T1.1", "Pencitraan difusi molekuler air bebas berbasis gerak Brown mikro. Sensitif terhadap iskemia serebral hiperakut."),
        GlossaryItem("ADC (Apparent Diffusion Coefficient)", "ADC", "M03C", "M03C.T1.1", "Peta kuantitatif laju difusi air jaringan tanpa pengaruh kontras T2 shinethrough."),
        GlossaryItem("FLAIR (Fluid Attenuated IR)", "FLAIR", "M03A", "M03A.T1.3", "Sekuens inversi recovery dengan TI panjang untuk memadamkan sinyal air bebas (CSF), menonjolkan lesi periventrikular."),
        GlossaryItem("STIR (Short TI IR)", "STIR", "M03A", "M03A.T1.3", "Sekuens inversi recovery dengan TI pendek (~150 ms) untuk memadamkan lemak, ideal untuk mendeteksi edema sumsum tulang."),
        GlossaryItem("Sudut Ernst", "α_Ernst", "M03B", "M03B.T1.3", "Sudut flip optimum yang memaksimalkan sinyal longitudinal steady state untuk TR dan T1 tertentu: cos(α) = exp(-TR/T1)."),
        GlossaryItem("Keadaan Tunak (Steady State)", "Steady State", "M03B", "M03B.T2.1", "Kondisi dinamis di mana magnetisasi longitudinal dan transversal konstan dari siklus ke siklus ketika TR lebih pendek daripada T1 dan T2."),
        GlossaryItem("RF Spoiling (Jam Fase)", "Spoiling", "M03B", "M03B.T2.2", "Variasi fase pulsa RF kuadratik pulsa demi pulsa untuk menghancurkan koherensi sisa magnetisasi transversal, menghasilkan sekuens inkoheren murni."),
        GlossaryItem("TrueFISP / FIESTA (b-FFE)", "TrueFISP", "M03B", "M03B.T3.1", "Sekuens balanced steady-state free precession (bSSFP) dengan gradien refokus penuh di ketiga sumbu, menghasilkan kontras rasio T2/T1 tinggi."),
        GlossaryItem("EPI (Echo Planar Imaging)", "EPI", "M03B", "M03B.T3.2", "Teknik akuisisi ultra-cepat di mana seluruh atau sebagian ruang-k dibaca dalam satu pulsa eksitasi menggunakan osilasi gradien baca bolak-balik."),
        GlossaryItem("DRIVE (Driven Equilibrium)", "DRIVE", "M03A", "M03A.T3.1", "Pulsa RF tambahan -90° di akhir echo train TSE yang membalikkan sisa magnetisasi transversal langsung kembali ke sumbu z."),
        GlossaryItem("BOLD (Blood Oxygen Level Dependent)", "BOLD", "M03C", "M03C.T1.2", "Kontras fMRI berbasis suseptibilitas magnetik: oksihemoglobin bersifat diamagnetik sedangkan deoksihemoglobin paramagnetik mempercepat peluruhan T2*."),
        GlossaryItem("MTC (Magnetization Transfer Contrast)", "MTC", "M03C", "M03C.T1.3", "Teknik saturasi pulsa off-resonance pada proton terikat makromolekul yang menurunkan sinyal jaringan parenkim terikat."),
        GlossaryItem("Gadolinium (Gd-DTPA)", "Gd", "M03C", "M03C.T1.4", "Agen kontras paramagnetik bervalensi 7 elektron tak berpasangan yang memperpendek T1 proton air secara drastis (sinyal hiperintens T1)."),

        GlossaryItem("Magic Angle", "54,7°", "M08A", "M08A.T2.4", "Peningkatan sinyal semu abnormal pada struktur tendon/ligamen berkolagen yang membentuk sudut ~54,7° (55°) terhadap medan B0 pada TE pendek."),
        GlossaryItem("Entry-Slice Phenomenon (ESP)", "Inflow", "M08B", "M08B.T1.2", "Peningkatan sinyal lumen pembuluh pada irisan masuk karena darah segar belum mengalami saturasi pulsa RF berulang."),
        GlossaryItem("Time-of-Flight (TOF) Signal Loss", "Flow Void", "M08B", "M08B.T2.2", "Kehilangan sinyal lumen pada spin-echo ketika darah berkecepatan tinggi keluar dari irisan sebelum menerima pulsa rephasing 180°."),
        GlossaryItem("Gradient Moment Nulling (GMN)", "+1, -2, +1", "M08B", "M08B.T2.4", "Kompensasi aliran orde pertama menggunakan lobus gradien tambahan (+1, -2, +1) untuk menolkan momen pertama (M1) dan mengatasi dephasing intravoksel."),
        GlossaryItem("Presaturasi Spasial", "Sat Band", "M08B", "M08B.T2.5", "Pulsa RF 90° yang ditempatkan di sisi hulu luar FOV untuk menjenuhkan spin darah sebelum masuk ke irisan pencitraan."),
        GlossaryItem("Cross-Excitation & Cross-Talk", "Slice Gap", "M08C", "M08C.T2.1", "Eksitasi parsial irisan tetangga akibat profil pulsa RF tidak persegi sempurna; diatasi dengan celah irisan ≥30% atau akuisisi interleaved."),
        GlossaryItem("Artefak Zipper", "RF Leak", "M08C", "M08C.T2.2", "Garis terang-gelap putus-putus tegak lurus sumbu frekuensi akibat kebocoran interferensi RF luar ke ruang magnet."),
        GlossaryItem("Artefak Moiré & Herringbone", "Spike k", "M08C", "M08C.T2.4", "Pola garis sejajar di seluruh citra akibat lonjakan derau (spike) di ruang-k, atau pita zebra di tepi FOV pada GRE."),
        GlossaryItem("QA Fantom MRI", "Baseline", "M08C", "M08C.T3.2", "Pengujian kendali mutu rutin menggunakan fantom standar untuk memantau SNR, uniformitas, akurasi geometris, dan rasio ghosting terhadap baseline."),
        GlossaryItem("Slew Rate", "SR", "M06A", "M06A.T2.1", "Laju kecepatan gradien mencapai amplitudo maksimum (SR = Amplitudo / Rise Time, satuan T/m/s). Menentukan kecepatan sekuens EPI dan TSE."),
        GlossaryItem("Rise Time (Waktu Naik)", "trise", "M06A", "M06A.T2.4", "Waktu yang dibutuhkan kumparan gradien untuk naik dari 0 menuju amplitudo puncak (trise = G / slew rate, dalam satuan ms atau μs)."),
        GlossaryItem("Kumparan Maxwell & Golay", "Gz / Gx,Gy", "M06A", "M06A.T2.1", "Pasangan kumparan melingkar Maxwell untuk gradien sumbu Z dan pasangan kumparan pelana (saddle) Golay untuk gradien sumbu X dan Y."),
        GlossaryItem("Arus Eddy (Eddy Currents)", "Ieddy", "M06A", "M06A.T2.1", "Arus induksi liar pada struktur konduktor di sekitar magnet akibat perubahan fluks gradien yang cepat; ditekan dengan perisai gradien aktif."),
        GlossaryItem("Skema Stejskal–Tanner", "DWI", "M06A", "M06A.T3.2", "Sepasang lobus gradien bipolar berpolaritas searah di kedua sisi pulsa RF 180° untuk mengode difusi molekul air pada sekuens spin-echo."),
        GlossaryItem("Kandang Faraday (Perisai RF)", "RF Shield", "M06B", "M06B.T2.1", "Lapisan logam tembaga/aluminium pembungkus ruang pindai (dilengkapi kawat jala, sarang lebah, dan waveguide) yang menahan interferensi radio VHF luar."),
        GlossaryItem("Pulsa Sinc", "sinc(t)", "M06B", "M06B.T2.2", "Bentuk gelombang pulsa RF pada domain waktu yang menghasilkan profil eksitasi irisan mendekati persegi tegas pada domain frekuensi."),
        GlossaryItem("Birdcage Kuadratur", "90° B1", "M06B", "M06B.T2.3", "Desain body coil pemancar/penerima dengan dua suplai RF yang berbeda fase 90° untuk menghasilkan medan B1 berpolarisasi sirkular searah presesi spin."),
        GlossaryItem("Faktor Pengisi (Filling Factor)", "η", "M06B", "M06B.T2.3", "Ukuran kesesuaian volume anatomi pasien terhadap volume sensitif koil RF; makin pas koil dengan anatomi, makin tinggi SNR."),
        GlossaryItem("Array Fasa (Phased-Array Coil)", "Array", "M06B", "M06B.T2.5", "Susunan banyak elemen koil permukaan kecil dengan kanal penerima independen yang mempertahankan SNR tinggi pada FOV luas serta mendukung parallel imaging."),
        GlossaryItem("Transmit Paralel (pTx)", "pTx", "M06B", "M06B.T3.2", "Teknik meratakan ketidakhomogenan medan B1 pada medan tinggi (≥ 3 T) dengan mengatur amplitudo dan fase beberapa elemen pemancar RF secara terpisah."),
        GlossaryItem("Quench", "Keselamatan", "M05", "M05.T2.6", "Pelepasan mendadak gas helium cair dari kriostat magnet superkonduktif akibat hilangnya superkonduktivitas, yang menghilangkan medan B0 seketika."),
        GlossaryItem("Kriostat (Cryostat)", "4,2 K", "M05", "M05.T2.1", "Bejana berinsulasi vakum dan pelindung radiasi termal yang menjaga kumparan superkonduktor NbTi tetap terendam helium cair pada suhu 4,2 K (-268,95 °C)."),
        GlossaryItem("Saklar Persisten", "Switch", "M05", "M05.T2.3", "Segmen kawat superkonduktor berpengatur pemanas yang dipasang paralel dengan solenoid utama untuk mengunci arus dalam lingkaran tertutup tanpa catu daya."),
        GlossaryItem("Shimming (Pasif & Aktif)", "ppm", "M05", "M05.T2.4", "Penghalusan homogenitas medan magnet B0 pada bola pencitraan 40 cm menggunakan pelat besi (shim pasif) dan kumparan berarus (shim aktif) dalam satuan ppm."),
        GlossaryItem("Bucking Coil (Perisai Aktif)", "5 G", "M05", "M05.T2.5", "Kumparan superkonduktor tambahan di kedua ujung solenoid dengan arah arus berlawanan untuk mempersempit jejak medan tepi 5 Gauss.")
    )

    val defaultNotes = listOf(
        StudyNote(
            id = "note_1",
            title = "Logbook Konsol: Lembar Kerja Klinis Radiografer",
            content = "Logbook operasional harian radiografer MRI:\n• Identifikasi Pasien & No. RM: Evaluasi kesesuaian klinis indikasi rujukan dan organ target.\n• Status Implan & Perangkat Medis: Konfirmasi lembar skrining Zona II & klasifikasi ASTM F2503 (MR Safe / MR Conditional / MR Unsafe).\n• Verifikasi Fungsi Ginjal: Nilai eGFR (mL/min/1.73m²) pra-injeksi GBCA untuk pencegahan risiko NSF (eGFR < 30 mL/min/1.73m² kontraindikasi GBCA kelompok risiko tinggi).\n• Catatan Tambahan: Pengaturan bantalan isolasi (skin-to-skin contact avoidance) untuk mencegah luka bakar induksi RF loop.",
            category = "Protokol Klinis",
            dateString = "Hari Ini"
        ),
        StudyNote(
            id = "note_2",
            title = "Formulasi Matematis & Parameter Konsol Rutin",
            content = "Rangkuman formulasi fisika pencitraan untuk optimasi di konsol:\n1. Frekuensi Larmor: f₀ = γ · B₀ (¹H: γ = 42,58 MHz/T; f₀ di 1,5 T = 63,87 MHz; f₀ di 3,0 T = 127,74 MHz).\n2. Waktu Akuisisi 2D: TA = (TR × N_phase × NEX) ÷ ETL. Menambah matriks frekuensi tidak meningkatkan TA!\n3. Dimensi Voksel 3D: Volume Voksel = (FOV_x ÷ N_freq) × (FOV_y ÷ N_phase) × Slice Thickness.\n4. Sudut Ernst (GRE): cos(α_Ernst) = exp(−TR ÷ T1). Mengoptimalkan SNR steady-state pada TR pendek.\n5. Titik Null IR: TI_null = T1 × ln(2) ≈ 0,693 × T1 (STIR ~180 ms untuk adiposa; FLAIR ~1664 ms untuk CSF pada 1,5 T).\n6. Pergeseran Kimiawi Type 1: Δf = 3,5 ppm × f₀ (~224 Hz pada 1,5 T; ~447 Hz pada 3,0 T sepanjang sumbu frekuensi).",
            category = "Fisika Sekuens",
            dateString = "Kemarin"
        ),
        StudyNote(
            id = "note_3",
            title = "Protokol Rutin MRI Otak (Brain Stroke & Tumor)",
            content = "Urutan rangkaian sekuens standar radiologi diagnostik serebral:\n1. 3-Plane Localizer (Scout / Tri-pilot)\n2. Sagital T1-Weighted Spin Echo / FSE: Evaluasi arsitektur korpus kalosum, batang otak, dan fosa posterior.\n3. Aksial T2-Weighted FSE (TR ~4000 ms, TE ~100 ms, ETL 12–16): Deteksi edema perilesional, efusi, dan patologi vaskular.\n4. Aksial FLAIR (TR ~9000 ms, TE ~120 ms, TI ~2400 ms): Atenuasi sinyal CSF bebas untuk menegaskan plak demielinasi MS / edema sitotoksik subaraknoid.\n5. Aksial DWI (b = 0 dan b = 1000 s/mm²) + Rekonstruksi Peta ADC: Konfirmasi restriksi difusi molekuler pada stroke iskemik hiperakut (<6 jam).\n6. Aksial T2* GRE / SWI: Deteksi mikrolesi perdarahan (hemosiderin blooming) dan kalsifikasi intrakranial.\n7. Pasca-Kontras T1w 3D (Gd-DTPA 0,1 mmol/kgBB): Aksial, koronal, sagital untuk evaluasi penyengatan vaskular/metastasis.",
            category = "Protokol Klinis",
            dateString = "2 Hari Lalu"
        ),
        StudyNote(
            id = "note_4",
            title = "Daftar Periksa Keselamatan MR & Mitigasi Artefak",
            content = "Prosedur kendali keselamatan (ACR Guidance Document):\n• Mitigasi SAR: Batasi < 2,0 W/kg (Normal Mode); perpanjang TR, turunkan sudut flip refocusing TSE (misal 140°), atau perkecil slice coverage bila SAR mendekati ambang batas.\n• Mitigasi Aliasing: Aktifkan Phase Oversampling (No Phase Wrap) atau Filter Low-Pass pada arah frekuensi.\n• Mitigasi Motion Ghosting: Aktifkan Respiratory Triggering / Gating, aplikasikan Spatial Presaturation Band pada dinding abdomen anterior, atau balikkan arah sumbu fase (Phase-Frequency Swap).\n• Mitigasi Susceptibility Logam: Gunakan sekuens FSE dengan rBW lebar (Receiver Bandwidth tinggi) atau teknik WARP/SEMAC.",
            category = "Keselamatan MR",
            dateString = "3 Hari Lalu"
        )
    )

    val quizzes = listOf(
        // =========================================================================
        // MODUL 1 — TINGKAT 1 (DASAR): MODUL.soal[1] (5 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M01_T1_Q1",
            moduleId = "M01",
            nodeCode = "M01.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa atom hidrogen (1H) dipilih sebagai sumber sinyal utama dalam pencitraan MRI klinis?",
            options = listOf(
                "Memiliki nomor massa genap dan bersifat radioaktif",
                "Sangat melimpah di jaringan tubuh (air dan lemak) serta memiliki spin neto",
                "Menghasilkan radiasi pengion yang menembus tulang",
                "Tidak terpengaruh oleh medan magnet utama B0"
            ),
            correctIndex = 1,
            explanation = "Tubuh manusia kaya akan air (H2O) dan lemak, serta inti 1H memiliki satu proton (nomor massa ganjil) yang menghasilkan spin neto dan momen magnetik kuat."
        ),
        QuizQuestion(
            id = "M01_T1_Q2",
            moduleId = "M01",
            nodeCode = "M01.T1.4B",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Manakah di antara inti berikut yang merupakan inti aktif MR karena memiliki nomor massa ganjil?",
            options = listOf(
                "12C (6 proton, 6 neutron)",
                "16O (8 proton, 8 neutron)",
                "13C (6 proton, 7 neutron)",
                "4He (2 proton, 2 neutron)"
            ),
            correctIndex = 2,
            explanation = "13C memiliki 6 proton dan 7 neutron sehingga nomor massanya 13 (ganjil) dan memiliki spin neto."
        ),
        QuizQuestion(
            id = "M01_T1_Q3",
            moduleId = "M01",
            nodeCode = "M01.T1.5",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Di dalam medan magnet utama B0, bagaimana keadaan dua populasi spin hidrogen pada kesetimbangan termal?",
            options = listOf(
                "Seluruh spin mengarah berlawanan B0",
                "Jumlah spin searah B0 (energi rendah) sedikit lebih banyak daripada yang berlawanan (energi tinggi)",
                "Jumlah spin searah dan berlawanan B0 selalu sama persis tanpa selisih",
                "Spin berhenti berputar sama sekali"
            ),
            correctIndex = 1,
            explanation = "Pada kesetimbangan termal, populasi spin paralel (energi rendah) sedikit lebih banyak daripada antiparalel, membentuk vektor magnetisasi neto (NMV) searah B0."
        ),
        QuizQuestion(
            id = "M01_T1_Q4",
            moduleId = "M01",
            nodeCode = "M01.T1.6",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Berapakah frekuensi Larmor hidrogen pada medan magnet B0 = 1,5 T jika rasio giromagnetik hidrogen 42,58 MHz/T?",
            options = listOf(
                "21,29 MHz",
                "42,58 MHz",
                "63,87 MHz",
                "127,74 MHz"
            ),
            correctIndex = 2,
            explanation = "Sesuai persamaan Larmor f = γ · B0 = 42,58 MHz/T × 1,5 T = 63,87 MHz."
        ),
        QuizQuestion(
            id = "M01_T1_Q5",
            moduleId = "M01",
            nodeCode = "M01.T1.7",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Berada pada pita manakah frekuensi presesi hidrogen MRI dalam spektrum elektromagnetik?",
            options = listOf(
                "Sinar-X (pengion)",
                "Ultraviolet",
                "Radiofrekuensi / RF (non-pengion)",
                "Sinar gamma"
            ),
            correctIndex = 2,
            explanation = "Frekuensi Larmor hidrogen klinis (misalnya ~63,87 MHz pada 1,5 T) berada di pita radiofrekuensi (RF) yang bersifat non-pengion."
        ),

        // =========================================================================
        // MODUL 1 — TINGKAT 2 (MENENGAH): MODUL.soal[2] (4 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M01_T2_Q1",
            moduleId = "M01",
            nodeCode = "M01.T2.2A",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apa yang terjadi pada vektor magnetisasi neto (NMV) ketika diberikan pulsa RF eksitasi tepat pada frekuensi Larmor?",
            options = listOf(
                "NMV mengalami nutasi (berpindah spiral dari sumbu longitudinal ke bidang transversal) dan spin menjadi sefase",
                "NMV langsung menghilang tanpa menginduksi sinyal",
                "Spin kehilangan koherensi fase seketika saat pulsa RF menyala",
                "B0 berubah arah mengikuti koil penerima"
            ),
            correctIndex = 0,
            explanation = "Pulsa RF pada frekuensi Larmor memicu resonansi: NMV bernutasi turun menuju bidang transversal dan momen magnetik spin memperoleh koherensi fase."
        ),
        QuizQuestion(
            id = "M01_T2_Q2",
            moduleId = "M01",
            nodeCode = "M01.T2.2B",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Faktor apakah yang menentukan besar sudut flip (θ = γ · B1 · τ) pada eksitasi RF?",
            options = listOf(
                "Kuat medan statis B0 dan suhu ruang",
                "Amplitudo medan RF (B1) dan durasi pemberian pulsa (τ)",
                "Jumlah lilitan koil penerima dan waktu TE",
                "Jenis kontras gadolinium yang disuntikkan"
            ),
            correctIndex = 1,
            explanation = "Sudut flip sebanding dengan rasio giromagnetik (γ), amplitudo medan magnet pulsa RF (B1), dan lamanya pulsa diberikan (τ)."
        ),
        QuizQuestion(
            id = "M01_T2_Q3",
            moduleId = "M01",
            nodeCode = "M01.T2.3A",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apa yang dimaksud dengan sinyal Free Induction Decay (FID)?",
            options = listOf(
                "Sinyal tegangan di koil penerima yang meningkat terus-menerus setelah pulsa RF mati",
                "Sinyal yang diinduksi oleh magnetisasi transversal yang berpresesi bebas dan meluruh akibat dephasing setelah pulsa RF dimatikan",
                "Pemanasan jaringan akibat komponen medan listrik RF",
                "Perpindahan proton dari energi rendah ke energi tinggi"
            ),
            correctIndex = 1,
            explanation = "Setelah pulsa RF dimatikan, NMV berpresesi bebas di bawah B0 dan menginduksi ggl pada koil penerima yang meluruh seiring hilangnya koherensi fase (dephasing)."
        ),
        QuizQuestion(
            id = "M01_T2_Q4",
            moduleId = "M01",
            nodeCode = "M01.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Parameter waktu manakah yang mengatur jarak antar-pulsa eksitasi RF berturut-turut dan mengendalikan pemulihan longitudinal?",
            options = listOf(
                "TE (Echo Time)",
                "TR (Repetition Time)",
                "TI (Inversion Time)",
                "Lebar pita (Bandwidth)"
            ),
            correctIndex = 1,
            explanation = "TR (Repetition Time) adalah waktu dari satu pulsa eksitasi RF ke pulsa eksitasi berikutnya dan menentukan seberapa banyak relaksasi longitudinal (T1) yang berlangsung."
        ),

        // =========================================================================
        // MODUL 1 — TINGKAT 3 (LANJUT): MODUL.soal[3] (3 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M01_T3_Q1",
            moduleId = "M01",
            nodeCode = "M01.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Menurut distribusi Boltzmann, apa dampak peningkatan medan magnet utama dari 1,5 T menjadi 3,0 T terhadap populasi spin?",
            options = listOf(
                "Selisih energi ΔE membesar sehingga kelebihan spin berenergi rendah meningkat sekitar dua kali lipat",
                "Selisih energi ΔE mengecil sehingga SNR menurun",
                "Semua spin berpindah ke tingkat energi tinggi",
                "Frekuensi Larmor tetap sama pada 63,87 MHz"
            ),
            correctIndex = 0,
            explanation = "Karena ΔE = h·γ·B0 sebanding dengan B0, menggandakan B0 dari 1,5 T ke 3 T menggandakan kelebihan populasi spin rendah (dari ~4,5 menjadi ~10 per sejuta), meningkatkan NMV dan SNR."
        ),
        QuizQuestion(
            id = "M01_T3_Q2",
            moduleId = "M01",
            nodeCode = "M01.T3.4",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Mengapa inti 2H (1 proton, 1 neutron) dan 14N (7 proton, 7 neutron) tetap bersifat aktif MR meskipun nomor massanya genap?",
            options = listOf(
                "Karena keduanya tidak memiliki neutron",
                "Karena jumlah proton dan neutronnya sama-sama ganjil sehingga arah putarnya tidak saling meniadakan (spin neto bulat)",
                "Karena keduanya memancarkan sinar gamma",
                "Karena nomor atomnya lebih besar dari 20"
            ),
            correctIndex = 1,
            explanation = "Inti dengan jumlah proton ganjil dan neutron ganjil menghasilkan nomor massa genap tetapi tetap memiliki spin neto bulat (bukan nol)."
        ),
        QuizQuestion(
            id = "M01_T3_Q3",
            moduleId = "M01",
            nodeCode = "M01.T3.5",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Bagaimana gerak NMV selama eksitasi RF on-resonance jika diamati dari kerangka acuan yang ikut berputar pada frekuensi Larmor?",
            options = listOf(
                "Presesi cepat mengelilingi B0 tampak berhenti dan NMV hanya tampak berputar murni mengelilingi B1 menuju bidang transversal",
                "NMV tampak berputar dua kali lebih cepat mengelilingi sumbu z",
                "B1 tampak berputar sangat cepat mengelilingi sumbu z",
                "NMV tidak mengalami perubahan sudut flip"
            ),
            correctIndex = 0,
            explanation = "Dalam kerangka acuan berputar pada frekuensi Larmor, komponen presesi mengelilingi B0 tampak diam sehingga hanya terlihat nutasi murni NMV mengelilingi B1."
        ),

        // =========================================================================
        // MODUL 2 — EVALUASI PEMAHAMAN RELAKSASI T1 & T2 (5 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M02_Q1",
            moduleId = "M02",
            nodeCode = "M02.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Apa definisi fisik dari konstanta waktu relaksasi T1 setelah pemberian pulsa RF 90°?",
            options = listOf(
                "Waktu yang dibutuhkan magnetisasi transversal (Mxy) untuk meluruh hingga 37% dari nilai awalnya",
                "Waktu yang dibutuhkan magnetisasi longitudinal (Mz) untuk pulih mencapai 63% dari nilai kesetimbangan (M0)",
                "Waktu antara pemberian pulsa RF 90° dengan puncak sinyal echo yang terdeteksi",
                "Waktu yang diperlukan seluruh spin proton untuk kembali ke fase presesi yang sama"
            ),
            correctIndex = 1,
            explanation = "Konstanta T1 (spin-lattice relaxation time) adalah waktu yang diperlukan komponen magnetisasi longitudinal (Mz) untuk pulih sebesar 63% dari nilai magnetisasi maksimum awalnya (M0)."
        ),
        QuizQuestion(
            id = "M02_Q2",
            moduleId = "M02",
            nodeCode = "M02.T2.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa proses relaksasi T2 disebut juga sebagai relaksasi spin-spin?",
            options = listOf(
                "Karena terjadi pelepasan energi panas dari spin proton menuju kisi molekuler di sekitarnya",
                "Karena peluruhan Mxy dipicu oleh pertukaran energi dan interaksi medan magnet mikro antar-spin yang berdekatan sehingga kehilangan koherensi fase",
                "Karena hanya terjadi pada jaringan yang memiliki kerapatan proton sangat tinggi",
                "Karena membutuhkan dua pulsa RF berturut-turut untuk membalikkan arah spin"
            ),
            correctIndex = 1,
            explanation = "Relaksasi T2 disebabkan oleh interaksi medan magnetik lokal antar-spin tetangga yang menyebabkan kecepatan presesi tiap spin sedikit berbeda, memicu dephasing (kehilangan koherensi fase) pada bidang transversal."
        ),
        QuizQuestion(
            id = "M02_Q3",
            moduleId = "M02",
            nodeCode = "M02.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Bagaimana karakteristik waktu relaksasi T1 dan T2 pada cairan serebrospinal (CSF / air bebas) dibandingkan dengan jaringan lemak?",
            options = listOf(
                "CSF memiliki T1 pendek dan T2 pendek karena molekulnya bergerak sangat cepat",
                "CSF memiliki T1 panjang dan T2 pendek karena frekuensi tumbling-nya mendekati frekuensi Larmor",
                "CSF memiliki T1 panjang dan T2 panjang karena molekul air berukuran kecil dan memiliki tumbling rate sangat cepat",
                "CSF dan lemak memiliki nilai T1 yang sama tetapi berbeda pada nilai T2"
            ),
            correctIndex = 2,
            explanation = "Molekul air bebas pada CSF berukuran kecil dan bergerak sangat cepat (high tumbling rate >> frekuensi Larmor), sehingga transfer energi ke lattice tidak efisien (T1 panjang) dan dephasing antar-spin berjalan lambat (T2 panjang)."
        ),
        QuizQuestion(
            id = "M02_Q4",
            moduleId = "M02",
            nodeCode = "M02.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Faktor apakah yang membedakan peluruhan T2* (T2-star) dengan peluruhan T2 murni?",
            options = listOf(
                "T2* memperhitungkan percepatan dephasing akibat ketidakhomogenan (inhomogenitas) medan magnet statis B0 dan efek suseptibilitas magnetik",
                "T2* hanya terjadi pada cairan bergerak seperti aliran darah arteri",
                "T2* selalu memiliki waktu relaksasi yang lebih panjang daripada T2 murni",
                "T2* berkaitan dengan pemulihan vektor magnetisasi pada sumbu longitudinal Z"
            ),
            correctIndex = 0,
            explanation = "Peluruhan T2* merupakan kombinasi dari interaksi spin-spin murni (T2) ditambah efek dephasing akibat inhomogenitas medan magnet utama B0, sehingga T2* selalu lebih pendek daripada T2."
        ),
        QuizQuestion(
            id = "M02_Q5",
            moduleId = "M02",
            nodeCode = "M02.T2.2",
            levelBadge = "Tingkat 3 • Klinis",
            level = 3,
            question = "Kombinasi parameter TR (Time Repetition) dan TE (Time Echo) manakah yang digunakan untuk menghasilkan citra T1-Weighted (T1W) di mana lemak tampak terang dan CSF tampak gelap?",
            options = listOf(
                "TR panjang (≥ 2000 ms) dan TE panjang (≥ 80 ms)",
                "TR panjang (≥ 2000 ms) dan TE pendek (≤ 25 ms)",
                "TR pendek (400–700 ms) dan TE pendek (10–25 ms)",
                "TR pendek (400–700 ms) dan TE panjang (≥ 100 ms)"
            ),
            correctIndex = 2,
            explanation = "Untuk memaksimalkan kontras T1 (T1-Weighted), digunakan TR pendek (agar jaringan dengan T1 pendek seperti lemak sudah pulih tinggi sementara CSF masih rendah) dan TE pendek (untuk meminimalkan kontribusi peluruhan T2)."
        ),

        // =========================================================================
        // MODUL LAINNYA (M03A, M07, M09)
        // =========================================================================
QuizQuestion(
            id = "M03A_Q1",
            moduleId = "M03A",
            nodeCode = "M03A.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada FLAIR, TI dipilih dekat dengan...",
            options = listOf(
                "TI nol lemak",
                "TI nol cairan (CSF)",
                "TE terpanjang",
                "Sudut Ernst"
            ),
            correctIndex = 1,
            explanation = "Pada TI nol cairan, magnetisasi CSF melewati nol saat pulsa 90°, sehingga CSF tampak gelap."
        ),
        QuizQuestion(
            id = "M03A_Q2",
            moduleId = "M03A",
            nodeCode = "M03A.T1.4",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Dibanding spin echo biasa, turbo spin echo paling mungkin menghasilkan...",
            options = listOf(
                "Waktu pindai naik",
                "Waktu pindai turun, tetapi T2 blur dan SAR bisa naik",
                "Tanpa pulsa 180°",
                "Ruang-k tidak terisi"
            ),
            correctIndex = 1,
            explanation = "Beberapa gema per TR mempercepat pindai, tetapi gema akhir lebih lemah dan pulsa 180° bertambah."
        ),
        QuizQuestion(
            id = "M03A_Q3",
            moduleId = "M03A",
            nodeCode = "M03A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada spin echo, bila TE dipilih 80 ms, kapan sistem memberi pulsa 180°?",
            options = listOf(
                "40 ms setelah pulsa 90°",
                "80 ms setelah pulsa 90°",
                "20 ms setelah pulsa 90°",
                "160 ms setelah pulsa 90°"
            ),
            correctIndex = 0,
            explanation = "TE = 2 × tau, jadi pulsa 180° diberikan pada tau, yaitu separuh TE."
        ),
        QuizQuestion(
            id = "M03A_Q4",
            moduleId = "M03A",
            nodeCode = "M03A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pulsa 180° pada spin echo mengoreksi...",
            options = listOf(
                "Dephasing T2* akibat ketidakhomogenan medan, tetapi bukan peluruhan T2",
                "Peluruhan T2 sekaligus T2*",
                "Pemulihan T1",
                "Hanya kerapatan proton"
            ),
            correctIndex = 0,
            explanation = "Ketidakhomogenan medan dapat diprediksi, sehingga bisa direfokus. Peluruhan T2 berasal dari interaksi spin-spin acak dan tidak bisa."
        ),
        QuizQuestion(
            id = "M03A_Q5",
            moduleId = "M03A",
            nodeCode = "M03A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada spin echo dua echo dengan TR panjang, echo pertama (TE pendek) berbobot...",
            options = listOf(
                "PD",
                "T1",
                "T2",
                "T2*"
            ),
            correctIndex = 0,
            explanation = "TE pendek meminimalkan beda T2 dan TR panjang meminimalkan beda T1, jadi yang tersisa PD. Echo kedua (TE panjang) berbobot T2."
        ),
        QuizQuestion(
            id = "M03A_Q6",
            moduleId = "M03A",
            nodeCode = "M03A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Suatu jaringan bersinyal nol pada inversion recovery bila TI kira-kira...",
            options = listOf(
                "0,69 × T1 jaringan itu",
                "2 × T1 jaringan itu",
                "Sama dengan TE",
                "Sama dengan TR"
            ),
            correctIndex = 0,
            explanation = "Dari SI = 1 − 2e^(−TI/T1), SI = 0 saat TI = 0,693 × T1."
        ),
        QuizQuestion(
            id = "M03A_Q7",
            moduleId = "M03A",
            nodeCode = "M03A.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa STIR sebaiknya tidak dipakai bersama gadolinium?",
            options = listOf(
                "T1 jaringan yang menyerap kontras memendek mendekati T1 lemak, sehingga ikut ditiadakan",
                "Gadolinium memanjangkan TR",
                "Gadolinium menghilangkan pulsa 180°",
                "STIR hanya untuk otak"
            ),
            correctIndex = 0,
            explanation = "Gadolinium memperpendek T1 jaringan yang menyerap, sehingga jaringan itu bisa ikut berada di titik null."
        ),
        QuizQuestion(
            id = "M03A_Q8",
            moduleId = "M03A",
            nodeCode = "M03A.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "FLAIR memakai TI kira-kira 1700 sampai 2200 ms untuk...",
            options = listOf(
                "Meniadakan sinyal CSF",
                "Meniadakan lemak",
                "Meniadakan darah",
                "Memperpendek TR"
            ),
            correctIndex = 0,
            explanation = "TI itu sesuai titik null CSF (0,69 × T1 CSF), sehingga lesi di dekat CSF tampak lebih jelas pada citra T2w."
        ),
        QuizQuestion(
            id = "M03A_Q9",
            moduleId = "M03A",
            nodeCode = "M03A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada TSE dengan matriks fase 256, NSA 1, dan turbo factor 16, berapa TR yang harus berlalu untuk menyelesaikan pindai?",
            options = listOf(
                "16 TR",
                "256 TR",
                "4096 TR",
                "1 TR"
            ),
            correctIndex = 0,
            explanation = "ST = TR × M(p) × NSA / ETL. Dengan 256 langkah fase dan 16 baris per TR, dibutuhkan 256 ÷ 16 = 16 TR, yaitu 1/16 waktu spin echo konvensional."
        ),
        QuizQuestion(
            id = "M03A_Q10",
            moduleId = "M03A",
            nodeCode = "M03A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada TSE, TE yang dipilih di protokol disebut TE efektif. Artinya...",
            options = listOf(
                "Itu TE gema yang datanya mengisi bagian tengah ruang-k, sehingga paling menentukan bobot citra",
                "Itu TE gema paling akhir",
                "Itu TE gema paling awal",
                "Itu rata-rata TE semua gema"
            ),
            correctIndex = 0,
            explanation = "Lewat phase reordering, gema yang TE-nya dekat TE efektif mengisi baris tengah ruang-k yang paling menentukan sinyal dan kontras."
        ),
        QuizQuestion(
            id = "M03A_Q11",
            moduleId = "M03A",
            nodeCode = "M03A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Cara mengurangi blur pada TSE adalah...",
            options = listOf(
                "Memperkecil jarak antar gema (echo spacing) dan/atau turbo factor",
                "Memperpanjang echo train",
                "Menambah jumlah irisan",
                "Menghapus pulsa 180°"
            ),
            correctIndex = 0,
            explanation = "Gema akhir beramplitudo rendah ikut membentuk resolusi. Echo spacing dan turbo factor yang lebih kecil mengurangi efek itu."
        ),
        QuizQuestion(
            id = "M03A_Q12",
            moduleId = "M03A",
            nodeCode = "M03A.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Apa tujuan pulsa reset (flip balik) pada driven equilibrium (DRIVE)?",
            options = listOf(
                "Mendorong sisa magnetisasi transversal ke arah longitudinal, sehingga TR bisa lebih pendek dan sinyal cairan tinggi",
                "Menambah SAR",
                "Menghilangkan sinyal air",
                "Memperpanjang TE efektif"
            ),
            correctIndex = 0,
            explanation = "Magnetisasi itu didominasi air. Dengan mendorongnya ke arah longitudinal, tidak perlu menunggu lama pemulihan T1, dan sinyal struktur berisi cairan seperti CSF naik."
        ),
        QuizQuestion(
            id = "M03A_Q13",
            moduleId = "M03A",
            nodeCode = "M03A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa pada TSE berbobot T1, TR dijaga pendek dan turbo factor kecil?",
            options = listOf(
                "TR panjang dan turbo factor besar menurunkan kontras T1 dan memasukkan terlalu banyak bobot T2",
                "Agar irisan berkurang",
                "Agar SAR turun",
                "Karena gema T1 lebih kuat"
            ),
            correctIndex = 0,
            explanation = "Memperpanjang TR menurunkan kontras T1, dan turbo factor panjang memasukkan banyak data ber-TE salah, sehingga bobot T2 terlalu besar."
        ),
        QuizQuestion(
            id = "M03A_Q14",
            moduleId = "M03A",
            nodeCode = "M03A.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada inversion recovery, TI adalah jarak waktu antara...",
            options = listOf(
                "Pulsa 90° dan gema",
                "Pulsa 180° pembalik dan pulsa 90° eksitasi",
                "Dua pulsa 90° berurutan",
                "Pulsa 180° dan akhir TR"
            ),
            correctIndex = 1,
            explanation = "TI (time from inversion) diukur dari pulsa pembalik 180° sampai pulsa 90° yang mengeksitasi magnetisasi."
        ),
        QuizQuestion(
            id = "M03A_Q15",
            moduleId = "M03A",
            nodeCode = "M03A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Lemak punya T1 260 ms. Dengan TR panjang, TI nol lemak kira-kira...",
            options = listOf(
                "90 ms",
                "260 ms",
                "520 ms",
                "180 ms"
            ),
            correctIndex = 3,
            explanation = "TI nol ≈ 0,69 × T1 = 0,69 × 260 ≈ 180 ms."
        ),
        QuizQuestion(
            id = "M03B_Q1",
            moduleId = "M03B",
            nodeCode = "M03B.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada gradient echo, sudut Ernst dihitung dari...",
            options = listOf(
                "arccos(E1), dengan E1 = e^(−TR/T1)",
                "arcsin(E1)",
                "E1 dikali 90°",
                "TE dibagi TR"
            ),
            correctIndex = 0,
            explanation = "Sudut Ernst adalah arccos(E1), yaitu sudut yang memberi sinyal terbesar untuk TR dan T1 tertentu."
        ),
        QuizQuestion(
            id = "M03B_Q2",
            moduleId = "M03B",
            nodeCode = "M03B.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa pulsa 180° tidak dipakai untuk membuat gema pada gradient echo bersudut kecil?",
            options = listOf(
                "Sebagian besar magnetisasi masih longitudinal dan akan terbalik ke arah −z",
                "TR menjadi terlalu panjang",
                "Gradien tidak dapat dimatikan",
                "Sudut kecil memperpanjang T2"
            ),
            correctIndex = 0,
            explanation = "Pada sudut kecil, magnetisasi transversal sedikit. Pulsa 180° justru membalik komponen longitudinalnya ke −z."
        ),
        QuizQuestion(
            id = "M03B_Q3",
            moduleId = "M03B",
            nodeCode = "M03B.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada gradient echo, luas lobus gradien negatif dibanding lobus positif adalah...",
            options = listOf(
                "Setengahnya",
                "Sama besar",
                "Dua kali lipat",
                "Nol"
            ),
            correctIndex = 0,
            explanation = "Gema muncul di tengah lobus positif, yaitu saat luas positif yang sudah terkumpul menetralkan luas negatif. Luas negatifnya setengah dari luas positif."
        ),
        QuizQuestion(
            id = "M03B_Q4",
            moduleId = "M03B",
            nodeCode = "M03B.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Untuk bobot T1 pada gradient echo, kombinasi yang tepat adalah...",
            options = listOf(
                "Sudut besar, TR pendek, TE pendek",
                "Sudut kecil, TR panjang, TE panjang",
                "Sudut kecil, TR panjang, TE pendek",
                "Sudut besar, TR panjang, TE panjang"
            ),
            correctIndex = 0,
            explanation = "Sudut besar dan TR pendek membuat magnetisasi jenuh sehingga beda T1 menonjol, dan TE pendek menekan pengaruh T2*."
        ),
        QuizQuestion(
            id = "M03B_Q5",
            moduleId = "M03B",
            nodeCode = "M03B.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada keadaan tunak, apa yang seimbang?",
            options = listOf(
                "Energi masuk lewat pulsa RF (sudut flip) dan energi keluar lewat relaksasi (TR)",
                "Jumlah slice dan jumlah average",
                "Gradien negatif dan gradien positif",
                "TE dan lebar pita penerima"
            ),
            correctIndex = 0,
            explanation = "Sudut flip menentukan energi yang masuk dan TR menentukan energi yang hilang lewat relaksasi. Saat keduanya seimbang, sinyal stabil dari TR ke TR."
        ),
        QuizQuestion(
            id = "M03B_Q6",
            moduleId = "M03B",
            nodeCode = "M03B.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Jika sudut flip lebih besar daripada sudut Ernst, yang terjadi adalah...",
            options = listOf(
                "Saturasi dan kontras T1 bertambah",
                "Kontras hanya ditentukan PD",
                "Sinyal selalu maksimum",
                "Efek T2* hilang"
            ),
            correctIndex = 0,
            explanation = "Di atas sudut Ernst, magnetisasi longitudinal tidak sempat pulih penuh (jenuh), sehingga beda T1 antar jaringan menonjol."
        ),
        QuizQuestion(
            id = "M03B_Q7",
            moduleId = "M03B",
            nodeCode = "M03B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa setiap pulsa RF pada gradient echo keadaan tunak dapat menghasilkan gema?",
            options = listOf(
                "Sisa magnetisasi transversal tidak dihapus, sehingga pulsa berikutnya sekaligus mengeksitasi dan merefokusnya",
                "Karena gradien selalu dimatikan",
                "Karena TR lebih panjang daripada T2",
                "Karena setiap pulsa bersudut 180°"
            ),
            correctIndex = 0,
            explanation = "TR yang sangat pendek meninggalkan magnetisasi transversal residual. Tanpa spoiling atau crusher, pulsa berikutnya merefokusnya dan menghasilkan gema."
        ),
        QuizQuestion(
            id = "M03B_Q8",
            moduleId = "M03B",
            nodeCode = "M03B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Gradien seimbang pada balanced gradient echo dipakai untuk...",
            options = listOf(
                "Membuat luas gradien total nol sehingga fase spin yang mengalir tidak bergeser dan sinyal tetap koheren",
                "Menghapus semua magnetisasi transversal",
                "Memanjangkan TE",
                "Menaikkan bising gradien"
            ),
            correctIndex = 0,
            explanation = "Luas gradien di atas garis sama dengan di bawahnya, sehingga spin yang mengalir tidak mengumpulkan pergeseran fase bersih. Darah dan CSF tetap koheren dan terang."
        ),
        QuizQuestion(
            id = "M03B_Q9",
            moduleId = "M03B",
            nodeCode = "M03B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Bayangan setengah FOV pada EPI terutama disebabkan oleh...",
            options = listOf(
                "Selisih kecil antara gema berpolaritas gradien baca positif dan negatif",
                "Aliran darah",
                "Terlalu banyak pulsa 180°",
                "TR terlalu panjang"
            ),
            correctIndex = 0,
            explanation = "Kesalahan kecil pada waktu dan bentuk gradien baca membuat gema positif dan negatif berbeda, sehingga muncul bayangan yang bergeser setengah FOV di arah fase. Koreksinya memakai pindai referensi saat rekonstruksi."
        ),
        QuizQuestion(
            id = "M03B_Q10",
            moduleId = "M03B",
            nodeCode = "M03B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada RF spoiling, mengapa sisa magnetisasi transversal tidak terbaca?",
            options = listOf(
                "Fasenya berbeda dari fase pulsa terbaru, dan penerima terkunci pada fase pulsa terbaru",
                "TR dibuat sangat panjang",
                "Gradien selalu dimatikan",
                "Sudut flip selalu 90°"
            ),
            correctIndex = 0,
            explanation = "Fase pulsa RF diubah tiap TR dan penerima terkunci pada fase pulsa terbaru, sehingga hanya magnetisasi yang baru dibentuk yang mempengaruhi kontras."
        ),
        QuizQuestion(
            id = "M03B_Q11",
            moduleId = "M03B",
            nodeCode = "M03B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada reverse-echo gradient echo dengan TR 30 ms dan TE aktual 10 ms, berapa TE efektifnya?",
            options = listOf(
                "50 ms",
                "20 ms",
                "70 ms",
                "10 ms"
            ),
            correctIndex = 0,
            explanation = "TE efektif = 2 × TR − TE aktual = 2 × 30 − 10 = 50 ms. Karena lebih panjang daripada TR, bobot T2 dimungkinkan dengan TR pendek."
        ),
        QuizQuestion(
            id = "M03B_Q12",
            moduleId = "M03B",
            nodeCode = "M03B.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pulsa-pulsa RF pertama sebelum keadaan tunak tercapai disebut...",
            options = listOf(
                "Pulsa persiapan (dummy) karena sinyalnya diabaikan",
                "Pulsa refokus",
                "Pulsa spoiler",
                "Pulsa crusher"
            ),
            correctIndex = 0,
            explanation = "Pada beberapa pulsa awal, jumlah magnetisasi transversal masih berbeda-beda tiap TR. Sinyalnya diabaikan sampai keadaan tunak tercapai."
        ),
        QuizQuestion(
            id = "M03B_Q13",
            moduleId = "M03B",
            nodeCode = "M03B.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Gradien yang merapikan kembali fase momen magnetik disebut...",
            options = listOf(
                "Rewinder",
                "Spoiler",
                "Crusher",
                "Pulsa dummy"
            ),
            correctIndex = 0,
            explanation = "Gradien yang mendephase disebut spoiler, sedangkan gradien yang merephase disebut rewinder."
        ),
        QuizQuestion(
            id = "M03B_Q14",
            moduleId = "M03B",
            nodeCode = "M03B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada reverse-echo gradient echo, TE yang menentukan kontras T2 adalah...",
            options = listOf(
                "TE efektif, yaitu jarak dari pulsa RF yang membentuk FID sampai puncak gema",
                "TE aktual, yaitu jarak dari puncak gema ke pulsa RF berikutnya",
                "TR dibagi dua",
                "Lebar pita penerima"
            ),
            correctIndex = 0,
            explanation = "TE efektif adalah waktu yang diberikan bagi gema untuk meluruh secara T2. TE aktual hanyalah TE yang dipilih di konsol dan tidak menentukan kontras T2."
        ),
        QuizQuestion(
            id = "M03B_Q15",
            moduleId = "M03B",
            nodeCode = "M03B.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa plak multipel sklerosis kecil di korda spinal bisa terlewat pada gradient echo T2*?",
            options = listOf(
                "TE tidak cukup panjang untuk memperlihatkan beda waktu peluruhan T2 jaringan",
                "Gradient echo tidak memakai gradien",
                "Plak selalu bersinyal sama dengan CSF",
                "TR terlalu panjang"
            ),
            correctIndex = 0,
            explanation = "Pada TE pendek, beda T2 antar jaringan belum sempat muncul, sehingga kelainan halus kurang terlihat. Kelainan berkontras besar seperti hernia diskus tetap tampak."
        ),
        QuizQuestion(
            id = "M03B_Q16",
            moduleId = "M03B",
            nodeCode = "M03B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Sekuens tunak mana yang hanya membaca FID, sehingga bobot T1 dan PD dominan?",
            options = listOf(
                "Inkoheren (spoiled)",
                "Koheren (rewound)",
                "Reverse-echo",
                "Spin echo"
            ),
            correctIndex = 0,
            explanation = "Pada sekuens inkoheren, gema terstimulasi di-spoil lewat RF spoiling atau gradient spoiling, sehingga hanya FID yang membentuk kontras."
        ),
        QuizQuestion(
            id = "M03B_Q17",
            moduleId = "M03B",
            nodeCode = "M03B.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Apa fungsi rewinder pada gradient echo koheren?",
            options = listOf(
                "Membalik kemiringan gradien penyandi fase setelah pembacaan, sehingga magnetisasi transversal koheren pada awal TR berikutnya",
                "Menghapus semua sisa magnetisasi transversal",
                "Memanjangkan TE efektif",
                "Mengubah fase pulsa RF tiap TR"
            ),
            correctIndex = 0,
            explanation = "Rewinding merefokus semua magnetisasi transversal kapan pun terbentuk, sehingga koherensinya terjaga. Menghapus sisa magnetisasi adalah tugas spoiling."
        ),
        QuizQuestion(
            id = "M03B_Q18",
            moduleId = "M03B",
            nodeCode = "M03B.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada keadaan tunak, jaringan bersinyal tinggi bila...",
            options = listOf(
                "T1 dan T2-nya sebanding (rasio mendekati 1)",
                "T1-nya sangat panjang dan T2-nya sangat pendek",
                "TR lebih panjang daripada T1",
                "Sudut flip selalu 90°"
            ),
            correctIndex = 0,
            explanation = "Kontras tunak ditentukan oleh perbandingan T1 dan T2. Air dan lemak serasi sehingga terang, sedangkan otot (T2 sangat pendek, T1 sangat panjang) bersinyal rendah."
        ),
        QuizQuestion(
            id = "M03C_Q1",
            moduleId = "M03C",
            nodeCode = "M03C.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada DWI, jaringan dengan ADC rendah tampak...",
            options = listOf(
                "Bersinyal tinggi, karena molekulnya hampir diam sehingga gradien kedua berhasil merefase",
                "Bersinyal rendah, karena molekulnya bebas bergerak",
                "Tanpa sinyal pada semua nilai b",
                "Sama dengan CSF"
            ),
            correctIndex = 0,
            explanation = "Molekul yang hampir diam tidak berubah fase di antara dua gradien, sehingga sinyalnya tetap koheren."
        ),
        QuizQuestion(
            id = "M03C_Q2",
            moduleId = "M03C",
            nodeCode = "M03C.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Faktor b pada DWI adalah parameter...",
            options = listOf(
                "Ekstrinsik, yang mengatur seberapa besar ADC memengaruhi kontras",
                "Intrinsik jaringan",
                "Hanya ditentukan TR",
                "Hanya ditentukan TE"
            ),
            correctIndex = 0,
            explanation = "ADC adalah parameter intrinsik. Nilai b bergantung pada amplitudo, durasi, dan jarak gradien, sehingga dapat diatur operator."
        ),
        QuizQuestion(
            id = "M03C_Q3",
            moduleId = "M03C",
            nodeCode = "M03C.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa sinyal BOLD naik saat area otak aktif?",
            options = listOf(
                "Aliran darah naik, deoksihemoglobin berkurang, sehingga dephasing berkurang",
                "Deoksihemoglobin bertambah",
                "TR memendek sendiri",
                "Gadolinium terkumpul"
            ),
            correctIndex = 0,
            explanation = "Deoksihemoglobin bersifat paramagnetik dan menyebabkan dephasing. Pengurangannya menaikkan sinyal."
        ),
        QuizQuestion(
            id = "M03C_Q4",
            moduleId = "M03C",
            nodeCode = "M03C.T1.4",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada citra T1w, jaringan yang menyerap gadolinium tampak...",
            options = listOf(
                "Hiperintens, karena T1-nya memendek",
                "Hipointens, karena T2-nya memendek",
                "Tidak berubah",
                "Selalu hitam"
            ),
            correctIndex = 0,
            explanation = "Gadolinium adalah agen T1: ia memendekkan waktu pemulihan T1 sehingga jaringan yang menyerapnya terang pada T1w."
        ),
        QuizQuestion(
            id = "M03C_Q5",
            moduleId = "M03C",
            nodeCode = "M03C.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa gadolinium memendekkan T1 air di sekitarnya?",
            options = listOf(
                "Medan magnet kecil yang berfluktuasi dari ion paramagnetik mempercepat pelepasan energi proton ke lingkungan",
                "Gadolinium menaikkan frekuensi Larmor air",
                "Gadolinium menghapus magnetisasi transversal",
                "Gadolinium memperpanjang TR"
            ),
            correctIndex = 0,
            explanation = "Ion paramagnetik punya momen magnetik besar. Fluktuasi medan lokalnya mempercepat relaksasi spin-kisi, sehingga T1 memendek."
        ),
        QuizQuestion(
            id = "M03C_Q6",
            moduleId = "M03C",
            nodeCode = "M03C.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada konsentrasi gadolinium yang sangat tinggi, sinyal T1w dapat turun karena...",
            options = listOf(
                "T1 memanjang lagi",
                "Pemendekan T2 mulai mengimbangi kenaikan akibat pemendekan T1",
                "TR otomatis memendek",
                "Kerapatan proton hilang"
            ),
            correctIndex = 1,
            explanation = "Pada konsentrasi tinggi T2 ikut memendek kuat, sehingga hubungan sinyal dan konsentrasi tidak linear dan bisa melandai atau turun."
        ),
        QuizQuestion(
            id = "M03C_Q7",
            moduleId = "M03C",
            nodeCode = "M03C.T1.4",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Agen kontras besi superparamagnetik terutama membuat jaringan tampak...",
            options = listOf(
                "Terang pada T1w",
                "Gelap (hipointens) pada T2w karena T2 memendek",
                "Tidak berubah pada semua citra",
                "Terang pada T2w"
            ),
            correctIndex = 1,
            explanation = "Partikel besi menimbulkan inhomogenitas medan lokal yang memendekkan T2 dan T2*, sehingga jaringan menjadi gelap."
        ),
        QuizQuestion(
            id = "M03C_Q8",
            moduleId = "M03C",
            nodeCode = "M03C.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada skema Stejskal-Tanner, spin yang diam akan...",
            options = listOf(
                "Kehilangan seluruh sinyal",
                "Kembali sefase karena dua gradien saling meniadakan, sehingga sinyalnya tidak berkurang",
                "Membuat ADC naik",
                "Menaikkan faktor b"
            ),
            correctIndex = 1,
            explanation = "Pulsa 180° membalik fase sehingga dua gradien identik saling membatalkan untuk spin yang tetap di tempat. Hanya spin yang bergerak yang kehilangan sinyal."
        ),
        QuizQuestion(
            id = "M03C_Q9",
            moduleId = "M03C",
            nodeCode = "M03C.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada DWI, S0 = 100 dan S pada b = 1000 s/mm² adalah 37. ADC kira-kira...",
            options = listOf(
                "0,1 × 10⁻³ mm²/s",
                "2,7 × 10⁻³ mm²/s",
                "1,0 × 10⁻³ mm²/s",
                "0,37 × 10⁻³ mm²/s"
            ),
            correctIndex = 2,
            explanation = "ADC = ln(S0/S)/b = ln(100/37)/1000 ≈ 0,99/1000 ≈ 1,0 × 10⁻³ mm²/s."
        ),
        QuizQuestion(
            id = "M03C_Q10",
            moduleId = "M03C",
            nodeCode = "M03C.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada MTC, pulsa saturasi off-resonance terutama menjenuhkan...",
            options = listOf(
                "Inti bebas, lalu sinyalnya naik",
                "Inti terikat, lalu magnetisasi berpindah ke inti bebas dan sinyalnya turun",
                "Lemak saja",
                "Aliran darah"
            ),
            correctIndex = 1,
            explanation = "Inti terikat punya rentang frekuensi lebar sehingga dapat dijenuhkan oleh pulsa off-resonance. Perpindahan magnetisasi menurunkan sinyal inti bebas."
        ),
        // =========================================================================
        // MODUL 9 — OPTIMASI PROTOKOL & MEMBACA LAYAR KONSOL (14 Soal: T1, T2, T3)
        // =========================================================================
        QuizQuestion(
            id = "M09_T1_Q1",
            moduleId = "M09",
            nodeCode = "M09.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Menaikkan TE akan...",
            options = listOf(
                "Menaikkan SNR dan kontras T2",
                "Menaikkan kontras T2 dan menurunkan SNR",
                "Menurunkan TA",
                "Menurunkan kontras T2 dan SNR"
            ),
            correctIndex = 1,
            explanation = "TE panjang menambah bobot T2 tetapi lebih banyak sinyal meluruh sehingga SNR turun."
        ),
        QuizQuestion(
            id = "M09_T1_Q2",
            moduleId = "M09",
            nodeCode = "M09.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "FoV 240 mm dengan matriks frekuensi 256, ukuran piksel arah frekuensi sekitar...",
            options = listOf(
                "0,5 mm",
                "0,94 mm",
                "1,5 mm",
                "2,4 mm"
            ),
            correctIndex = 1,
            explanation = "240/256 ≈ 0,94 mm."
        ),
        QuizQuestion(
            id = "M09_T1_Q3",
            moduleId = "M09",
            nodeCode = "M09.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Rumus waktu pindai 2D adalah...",
            options = listOf(
                "TR × N fase × NEX",
                "TR × N frekuensi × NEX",
                "TE × N fase × NEX",
                "FoV × N fase × NEX"
            ),
            correctIndex = 0,
            explanation = "TA 2D = TR × matriks fase × NEX. Matriks frekuensi tidak memengaruhi TA."
        ),
        QuizQuestion(
            id = "M09_T1_Q4",
            moduleId = "M09",
            nodeCode = "M09.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Cara menaikkan resolusi tanpa menambah waktu pindai adalah...",
            options = listOf(
                "Menaikkan matriks fase",
                "Menaikkan NEX",
                "Menaikkan matriks frekuensi saja",
                "Menaikkan TR"
            ),
            correctIndex = 2,
            explanation = "Matriks frekuensi tidak memengaruhi TA, tetapi menaikkannya menaikkan resolusi."
        ),
        QuizQuestion(
            id = "M09_T1_Q5",
            moduleId = "M09",
            nodeCode = "M09.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "TR 500 ms, 128 langkah fase, NEX 2. TA adalah...",
            options = listOf(
                "64 detik",
                "128 detik",
                "256 detik",
                "500 detik"
            ),
            correctIndex = 1,
            explanation = "0,5 s × 128 × 2 = 128 s (2 menit 8 detik)."
        ),
        QuizQuestion(
            id = "M09_T2_Q1",
            moduleId = "M09",
            nodeCode = "M09.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Menurut buku, faktor kualitas gambar MRI yang paling penting adalah...",
            options = listOf(
                "Resolusi spasial",
                "SNR",
                "Jumlah irisan",
                "Ukuran FoV"
            ),
            correctIndex = 1,
            explanation = "Tidak ada gunanya resolusi baik bila SNR buruk."
        ),
        QuizQuestion(
            id = "M09_T2_Q2",
            moduleId = "M09",
            nodeCode = "M09.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "FoV asimetris (persegi panjang) bermanfaat karena...",
            options = listOf(
                "Menaikkan SNR tanpa batas",
                "Memperkecil FoV arah fase sehingga TA turun dengan resolusi tetap",
                "Menambah matriks frekuensi",
                "Menghilangkan artefak gerak sepenuhnya"
            ),
            correctIndex = 1,
            explanation = "Langkah fase berkurang sebanding pengecilan FoV fase, sementara ukuran piksel tetap."
        ),
        QuizQuestion(
            id = "M09_T2_Q3",
            moduleId = "M09",
            nodeCode = "M09.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Bandwidth penerima diturunkan. Dampak yang benar adalah...",
            options = listOf(
                "SNR naik, pergeseran kimia dan TE minimum naik",
                "SNR turun, pergeseran kimia turun",
                "SNR naik, TE minimum turun",
                "TA turun, resolusi naik"
            ),
            correctIndex = 0,
            explanation = "BW sempit mengurangi noise (SNR naik) tetapi menambah pergeseran kimia dan TE minimum."
        ),
        QuizQuestion(
            id = "M09_T2_Q4",
            moduleId = "M09",
            nodeCode = "M09.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "NEX dinaikkan dari 1 menjadi 4. Perubahan SNR dan waktu pindai?",
            options = listOf(
                "SNR naik 2 kali, waktu naik 4 kali",
                "SNR naik 4 kali, waktu naik 4 kali",
                "SNR naik 4 kali, waktu tetap",
                "SNR naik 2 kali, waktu tetap"
            ),
            correctIndex = 0,
            explanation = "SNR sebanding dengan √NEX, sedangkan waktu pindai sebanding dengan NEX."
        ),
        QuizQuestion(
            id = "M09_T2_Q5",
            moduleId = "M09",
            nodeCode = "M09.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Modul yang mengirim instruksi waktu, amplitudo, dan bentuk pulsa RF serta mengendalikan gradien adalah...",
            options = listOf(
                "PACS",
                "Server DICOM",
                "Pulse generator module (PGM)",
                "Headset pasien"
            ),
            correctIndex = 2,
            explanation = "PGM adalah komputer independen yang menjalankan urutan pulsa; ia juga terhubung ke sensor fisiologis."
        ),
        QuizQuestion(
            id = "M09_T3_Q1",
            moduleId = "M09",
            nodeCode = "M09.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "TSE: TR 4000 ms, 256 langkah fase, NEX 1, ETL 16. TA adalah...",
            options = listOf(
                "64 detik",
                "1024 detik",
                "16 detik",
                "256 detik"
            ),
            correctIndex = 0,
            explanation = "4 s × 256 × 1 ÷ 16 = 64 s."
        ),
        QuizQuestion(
            id = "M09_T3_Q2",
            moduleId = "M09",
            nodeCode = "M09.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "3D: TR 20 ms, 128 fase, 64 partisi, NEX 1. TA mendekati...",
            options = listOf(
                "2,6 detik",
                "164 detik",
                "10 detik",
                "328 detik"
            ),
            correctIndex = 1,
            explanation = "0,02 s × 128 × 64 ≈ 164 s."
        ),
        QuizQuestion(
            id = "M09_T3_Q3",
            moduleId = "M09",
            nodeCode = "M09.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Parallel imaging R = 2 (kasus ideal). Dampaknya...",
            options = listOf(
                "TA turun setengah, SNR turun sekitar 0,71×",
                "TA tetap, SNR naik 2×",
                "TA turun setengah, SNR naik",
                "TA naik 2×, SNR tetap"
            ),
            correctIndex = 0,
            explanation = "TA ÷ R dan SNR ÷ √R (minimal; g-factor menambah penurunan)."
        ),
        QuizQuestion(
            id = "M09_T3_Q4",
            moduleId = "M09",
            nodeCode = "M09.T3.3",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Turbo factor pada antarmuka Siemens setara dengan istilah GE...",
            options = listOf(
                "NEX",
                "Echo train length (ETL)",
                "Receive bandwidth",
                "Fractional NEX"
            ),
            correctIndex = 1,
            explanation = "Keduanya menyatakan jumlah echo per TR pada sekuens turbo/fast spin echo."
        ),

        // =========================================================================
        // MODUL 4A — PENGODEAN SPASIAL (TINGKAT 1: SOAL_M04A — 6 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M04A_Q1",
            moduleId = "M04A",
            nodeCode = "M04A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Apa urutan tiga langkah pengodean posisi dalam MRI 2D?",
            options = listOf(
                "Kode frekuensi, pilih irisan, kode fase",
                "Pilih irisan, kode fase, kode frekuensi",
                "Kode fase, kode frekuensi, pilih irisan",
                "Pilih irisan, kode frekuensi, lalu seluruh ruang-k dihapus"
            ),
            correctIndex = 1,
            explanation = "Irisan dipilih saat pulsa RF, fase dikodekan sebelum pembacaan, dan frekuensi dikodekan saat sinyal dibaca."
        ),
        QuizQuestion(
            id = "M04A_Q2",
            moduleId = "M04A",
            nodeCode = "M04A.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Saat gradien menyala, apa yang terjadi pada inti di isosenter?",
            options = listOf(
                "Frekuensinya naik paling besar",
                "Frekuensinya tidak berubah",
                "Frekuensinya turun menjadi nol",
                "Fasenya selalu berbalik"
            ),
            correctIndex = 1,
            explanation = "Gradien tidak mengubah kuat medan di isosenter, sehingga frekuensinya tetap. Inti di ujung medan tinggi berpresesi lebih cepat, dan di ujung medan rendah lebih lambat."
        ),
        QuizQuestion(
            id = "M04A_Q3",
            moduleId = "M04A",
            nodeCode = "M04A.T1.5",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Gradien Gz diperkuat dan BW pulsa RF tetap. Bagaimana tebal irisan?",
            options = listOf(
                "Lebih tebal",
                "Tidak berubah",
                "Lebih tipis",
                "Menjadi nol"
            ),
            correctIndex = 2,
            explanation = "Δz = BW_RF / (γ̄·Gz). Gz lebih besar membuat Δz lebih kecil."
        ),
        QuizQuestion(
            id = "M04A_Q4",
            moduleId = "M04A",
            nodeCode = "M04A.T1.6",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Kapan gradien frekuensi (gradien baca) menyala?",
            options = listOf(
                "Saat pulsa RF dikirim",
                "Singkat sebelum pembacaan, lalu mati",
                "Saat sinyal diterima (gema dibaca)",
                "Hanya pada TR pertama"
            ),
            correctIndex = 2,
            explanation = "Gradien frekuensi menyala selama gema dibaca, sehingga disebut gradien baca."
        ),
        QuizQuestion(
            id = "M04A_Q5",
            moduleId = "M04A",
            nodeCode = "M04A.T1.6",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "RBW tetap dan gradien frekuensi dibuat lebih curam. Bagaimana FOV pada arah frekuensi?",
            options = listOf(
                "Lebih besar",
                "Tidak berubah",
                "Lebih kecil",
                "Menjadi nol"
            ),
            correctIndex = 2,
            explanation = "FOV = RBW / (γ·G). G lebih besar membuat FOV lebih kecil."
        ),
        QuizQuestion(
            id = "M04A_Q6",
            moduleId = "M04A",
            nodeCode = "M04A.T1.7",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa gradien fase harus diubah di setiap TR?",
            options = listOf(
                "Agar frekuensi Larmor naik",
                "Agar tiap TR mengisi garis ruang-k yang berbeda",
                "Agar irisan bergeser",
                "Agar gema lebih cepat hilang"
            ),
            correctIndex = 1,
            explanation = "Jika gradien fase tidak diubah, garis yang sama terisi pada setiap TR dan matriks fase bernilai satu."
        ),

        // =========================================================================
        // MODUL 4B — GRADIEN, RUANG-K, DAN FOURIER (TINGKAT 2: SOAL_M04B — 7 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M04B_Q1",
            moduleId = "M04B",
            nodeCode = "M04B.T2.10.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Jika hanya tepi ruang-k yang dipakai untuk membentuk citra, apa yang terjadi?",
            options = listOf(
                "Citra tampak normal",
                "Kontras dan sinyal besar hilang, tersisa tepi dan detail halus",
                "Citra hanya berisi bentuk kasar yang halus",
                "Aliasing muncul"
            ),
            correctIndex = 1,
            explanation = "Pusat ruang-k membawa sinyal dan kontras. Tepinya membawa detail halus."
        ),
        QuizQuestion(
            id = "M04B_Q2",
            moduleId = "M04B",
            nodeCode = "M04B.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apa penyebab aliasing (lipatan) pada citra?",
            options = listOf(
                "TR terlalu panjang",
                "FOV lebih kecil dari objek",
                "NEX terlalu besar",
                "Gradien terlalu lemah"
            ),
            correctIndex = 1,
            explanation = "Bagian objek di luar FOV terlipat ke sisi seberang citra."
        ),
        QuizQuestion(
            id = "M04B_Q3",
            moduleId = "M04B",
            nodeCode = "M04B.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Berapa titik minimal per siklus agar frekuensi terbaca benar menurut Nyquist?",
            options = listOf(
                "Satu",
                "Dua",
                "Empat",
                "Delapan"
            ),
            correctIndex = 1,
            explanation = "Nyquist menuntut minimal dua titik per siklus. Kurang dari itu terjadi aliasing."
        ),
        QuizQuestion(
            id = "M04B_Q4",
            moduleId = "M04B",
            nodeCode = "M04B.T2.7",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "RBW dikurangi setengahnya dengan matriks frekuensi tetap. Bagaimana jendela sampling?",
            options = listOf(
                "Dua kali lebih panjang",
                "Setengahnya",
                "Tidak berubah",
                "Menjadi nol"
            ),
            correctIndex = 0,
            explanation = "Interval sampling = 1/RBW, dan jendela = matriks × interval. RBW setengah membuat jendela dua kali lebih panjang."
        ),
        QuizQuestion(
            id = "M04B_Q5",
            moduleId = "M04B",
            nodeCode = "M04B.T2.10.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Jarak antar garis ruang-k (Δk) dibuat dua kali lebih besar. Apa yang terjadi pada FOV?",
            options = listOf(
                "Dua kali lebih besar",
                "Setengahnya",
                "Tidak berubah",
                "Menjadi nol"
            ),
            correctIndex = 1,
            explanation = "FOV = 1/Δk. Δk dua kali lebih besar membuat FOV setengahnya."
        ),
        QuizQuestion(
            id = "M04B_Q6",
            moduleId = "M04B",
            nodeCode = "M04B.T2.10.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "TR 500 ms, matriks fase 128, NEX 2 (2D, satu lokasi irisan, tanpa turbo). Berapa waktu pindainya?",
            options = listOf(
                "64 detik",
                "128 detik",
                "256 detik",
                "500 detik"
            ),
            correctIndex = 1,
            explanation = "Waktu = TR × N_fase × NEX = 500 ms × 128 × 2 = 128.000 ms = 128 detik."
        ),
        QuizQuestion(
            id = "M04B_Q7",
            moduleId = "M04B",
            nodeCode = "M04B.T2.10.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apakah satu titik data ruang-k sama dengan satu piksel citra?",
            options = listOf(
                "Ya, satu lawan satu",
                "Tidak, tiap titik memuat informasi seluruh irisan",
                "Ya, tetapi hanya di pusat",
                "Tidak, tetapi hanya di tepi"
            ),
            correctIndex = 1,
            explanation = "Ruang-k bukan citra. Karena tiap titik data memuat informasi seluruh irisan, FFT diperlukan untuk membentuk citra."
        ),

        // =========================================================================
        // MODUL 4C — RUANG-K LANJUT & STRATEGI AKUISIS (TINGKAT 3: SOAL_M04C — 6 Soal)
        // =========================================================================
        QuizQuestion(
            id = "M04C_Q1",
            moduleId = "M04C",
            nodeCode = "M04C.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Di pusat FOV sepanjang sumbu fase, apa yang terjadi pada pseudo-frekuensi?",
            options = listOf(
                "Nol: nilai sinyal tidak berubah dari garis ke garis",
                "Tertinggi",
                "Setengah dari maksimum",
                "Tidak dapat ditentukan"
            ),
            correctIndex = 0,
            explanation = "Di pusat tidak ada perubahan fase antar garis, sehingga tidak ada pseudo-frekuensi. Makin jauh dari pusat, makin tinggi."
        ),
        QuizQuestion(
            id = "M04C_Q2",
            moduleId = "M04C",
            nodeCode = "M04C.T3.3",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Apa dasar yang memungkinkan partial Fourier?",
            options = listOf(
                "Simetri konjugat ruang-k",
                "Gradien frekuensi yang lebih curam",
                "Pulsa RF 180°",
                "Kenaikan NEX"
            ),
            correctIndex = 0,
            explanation = "Titik yang berseberangan dari pusat berpasangan (simetri konjugat), sehingga sebagian ruang-k dapat diperkirakan."
        ),
        QuizQuestion(
            id = "M04C_Q3",
            moduleId = "M04C",
            nodeCode = "M04C.T3.7",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Pada EPI single-shot, bagaimana gradien fase bekerja?",
            options = listOf(
                "Berosilasi seperti gradien frekuensi",
                "Hanya berkedip (blip) kecil, turun satu garis tiap kali",
                "Dimatikan seluruhnya",
                "Menyala penuh sepanjang pembacaan"
            ),
            correctIndex = 1,
            explanation = "Gradien frekuensi berosilasi, sedangkan gradien fase hanya blipping untuk berpindah satu garis."
        ),
        QuizQuestion(
            id = "M04C_Q4",
            moduleId = "M04C",
            nodeCode = "M04C.T3.6",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Parallel imaging melewati setiap garis kedua (R = 2). Tanpa pembukaan lipatan, apa konsekuensinya?",
            options = listOf(
                "FOV fase menjadi setengah sehingga objek terlipat (aliasing)",
                "Resolusi menjadi dua kali",
                "Waktu pindai menjadi dua kali",
                "Tidak ada perubahan"
            ),
            correctIndex = 0,
            explanation = "Waktu turun sekitar R kali, tetapi FOV fase menjadi 1/R dan objek terlipat. Profil sensitivitas coil dipakai untuk membukanya."
        ),
        QuizQuestion(
            id = "M04C_Q5",
            moduleId = "M04C",
            nodeCode = "M04C.T3.7",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Apa ciri propeller dibanding pengisian kartesian?",
            options = listOf(
                "Seluruh ruang-k diisi dalam satu tembakan",
                "Pusat ruang-k diambil pada setiap TR sehingga lebih tahan gerak",
                "Tidak memerlukan algoritma rekonstruksi khusus",
                "Waktu pindai selalu lebih singkat"
            ),
            correctIndex = 1,
            explanation = "Tiap strip melewati pusat ruang-k, setara NSA ganda dan mengurangi artefak gerak, tetapi memerlukan algoritma rumit dan waktu pindai lebih lama."
        ),
        QuizQuestion(
            id = "M04C_Q6",
            moduleId = "M04C",
            nodeCode = "M04C.T3.7",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Pada akuisisi 2D volumetrik, kapan semua irisan lengkap?",
            options = listOf(
                "Satu per satu sejak awal",
                "Hampir bersamaan di akhir pemindaian",
                "Tidak pernah lengkap",
                "Setelah separuh pemindaian"
            ),
            correctIndex = 1,
            explanation = "Pada 2D volumetrik satu laci diisi di semua lemari lebih dulu, sehingga semua irisan baru lengkap hampir bersamaan di akhir."
        ),

        // =========================================================================
        // MODUL 5 — ANATOMI PESAWAT DAN SISTEM MAGNET (19 Soal: T1, T2, T3)
        // =========================================================================
        QuizQuestion(
            id = "M05_Q1",
            moduleId = "M05",
            nodeCode = "M05.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Lapisan terluar yang berperan menahan gelombang radio dari luar adalah...",
            options = listOf(
                "Ruang berpelindung RF",
                "Gradient coil",
                "RF coil",
                "Bore"
            ),
            correctIndex = 0,
            explanation = "Sangkar Faraday menahan gelombang radio luar dan menjaga sinyal pasien tidak keluar."
        ),
        QuizQuestion(
            id = "M05_Q2",
            moduleId = "M05",
            nodeCode = "M05.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Tugas gradient coil adalah...",
            options = listOf(
                "Menghasilkan B0",
                "Menambah medan linear untuk mengode posisi",
                "Mendinginkan helium",
                "Menerima sinyal proton"
            ),
            correctIndex = 1,
            explanation = "Medan yang berubah linear di sumbu X, Y, Z membuat frekuensi bergantung posisi."
        ),
        QuizQuestion(
            id = "M05_Q3",
            moduleId = "M05",
            nodeCode = "M05.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada potongan aksial, lapisan pesawat tampak sebagai...",
            options = listOf(
                "Pita sejajar di atas dan bawah bore",
                "Cincin konsentris di sekeliling bore",
                "Garis lurus tunggal",
                "Titik-titik terpisah"
            ),
            correctIndex = 1,
            explanation = "Potongan aksial tegak lurus bore, jadi lapisan melingkar tampak sebagai cincin."
        ),
        QuizQuestion(
            id = "M05_Q4",
            moduleId = "M05",
            nodeCode = "M05.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Jenis magnet yang tidak dapat dimatikan saat darurat adalah...",
            options = listOf(
                "Resistif",
                "Superkonduktif",
                "Permanen",
                "Semua jenis dapat dimatikan"
            ),
            correctIndex = 2,
            explanation = "Magnet permanen selalu menyala. Resistif dapat dimatikan dengan memutus arus."
        ),
        QuizQuestion(
            id = "M05_Q5",
            moduleId = "M05",
            nodeCode = "M05.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa pesawat klinis 1,5 T dan 3 T memakai magnet superkonduktif?",
            options = listOf(
                "Karena medan kuat dan stabil dapat dicapai tanpa daya besar yang terus mengalir",
                "Karena tidak butuh pendingin",
                "Karena paling murah",
                "Karena medannya vertikal"
            ),
            correctIndex = 0,
            explanation = "Arus persisten tanpa hambatan menjaga medan kuat dan stabil; harganya ada pada kriogen."
        ),
        QuizQuestion(
            id = "M05_Q6",
            moduleId = "M05",
            nodeCode = "M05.T1.4",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Dibanding 1,5 T, pesawat 3 T umumnya...",
            options = listOf(
                "SNR lebih tinggi, tetapi SAR dan artefak suseptibilitas juga naik",
                "SNR lebih rendah dan SAR turun",
                "SNR sama dan artefak turun",
                "Tidak ada bedanya"
            ),
            correctIndex = 0,
            explanation = "Medan lebih tinggi memberi sinyal lebih besar dengan konsekuensi SAR dan artefak."
        ),
        QuizQuestion(
            id = "M05_Q7",
            moduleId = "M05",
            nodeCode = "M05.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada suhu berapa helium cair menjaga kawat magnet bebas hambatan?",
            options = listOf(
                "77 K",
                "4,2 K",
                "293 K",
                "100 K"
            ),
            correctIndex = 1,
            explanation = "4,2 K (-268,95 °C) berada di bawah suhu transisi niobium-titanium."
        ),
        QuizQuestion(
            id = "M05_Q8",
            moduleId = "M05",
            nodeCode = "M05.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Fungsi utama kriostat adalah...",
            options = listOf(
                "Memanaskan kawat",
                "Mencegah panas luar merambat ke helium cair",
                "Menghasilkan gradien",
                "Memperkuat sinyal RF"
            ),
            correctIndex = 1,
            explanation = "Vakum dan pelindung panas membuat helium tetap dingin lama."
        ),
        QuizQuestion(
            id = "M05_Q9",
            moduleId = "M05",
            nodeCode = "M05.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Setelah arus mencapai nilai medan yang diinginkan, apa yang dilakukan?",
            options = listOf(
                "Pemanas saklar dimatikan sehingga saklar menjadi superkonduktor, lalu catu daya dilepas",
                "Pemanas dinyalakan terus",
                "Catu daya dibiarkan menyala selamanya",
                "Helium dikuras"
            ),
            correctIndex = 0,
            explanation = "Saklar superkonduktor menutup lingkaran; arus bertahan (persisten)."
        ),
        QuizQuestion(
            id = "M05_Q10",
            moduleId = "M05",
            nodeCode = "M05.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Shim pasif dan aktif berturut-turut memakai...",
            options = listOf(
                "Kumparan berarus dan pelat besi",
                "Pelat besi kecil dan koil shim berarus",
                "Helium dan nitrogen",
                "Dua kumparan gradien"
            ),
            correctIndex = 1,
            explanation = "Pelat feromagnetik dipasang oleh insinyur; koil shim arusnya dapat diatur."
        ),
        QuizQuestion(
            id = "M05_Q11",
            moduleId = "M05",
            nodeCode = "M05.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Bucking coil sebagai perisai aktif bekerja dengan...",
            options = listOf(
                "Arus searah dengan kumparan utama",
                "Arus berlawanan arah sehingga garis 5 G menyempit",
                "Menyerap panas",
                "Menahan helium"
            ),
            correctIndex = 1,
            explanation = "Fluks tepi dibalik sehingga jejak 5 G mengecil."
        ),
        QuizQuestion(
            id = "M05_Q12",
            moduleId = "M05",
            nodeCode = "M05.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Risiko khusus perisai aktif dibanding pasif adalah...",
            options = listOf(
                "Medan tepi lebih lebar",
                "Gradien medan statis curam, benda feromagnetik sulit dicegah menjadi proyektil",
                "Tidak butuh arus",
                "Lebih berat"
            ),
            correctIndex = 1,
            explanation = "Medan turun tajam di tepi, sehingga gaya tarik naik tiba-tiba saat mendekat."
        ),
        QuizQuestion(
            id = "M05_Q13",
            moduleId = "M05",
            nodeCode = "M05.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Quench darurat sengaja dipicu hanya bila...",
            options = listOf(
                "Alat rusak",
                "Ada ancaman nyawa akibat medan magnet",
                "Pasien gelisah",
                "Pesawat perlu dimatikan malam hari"
            ),
            correctIndex = 1,
            explanation = "Quench mahal dan mengganggu layanan; hanya untuk bahaya nyawa yang berasal dari medan magnet."
        ),
        QuizQuestion(
            id = "M05_Q14",
            moduleId = "M05",
            nodeCode = "M05.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada quench, helium yang menguap keluar lewat...",
            options = listOf(
                "Meja pasien",
                "Pipa quench ke luar gedung",
                "Gradient coil",
                "Pintu ruangan"
            ),
            correctIndex = 1,
            explanation = "Helium mengembang sangat besar; pipa quench mengarahkannya ke luar gedung."
        ),
        QuizQuestion(
            id = "M05_Q15",
            moduleId = "M05",
            nodeCode = "M05.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Bila B0 dilipatkan dua, SAR kira-kira menjadi...",
            options = listOf(
                "Sama",
                "Dua kali",
                "Empat kali",
                "Setengah"
            ),
            correctIndex = 2,
            explanation = "SAR sebanding kuadrat B0."
        ),
        QuizQuestion(
            id = "M05_Q16",
            moduleId = "M05",
            nodeCode = "M05.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "SNR terhadap B0 kira-kira...",
            options = listOf(
                "Turun linear",
                "Naik kira-kira linear",
                "Naik kuadrat",
                "Tidak berubah"
            ),
            correctIndex = 1,
            explanation = "Sinyal naik sebanding kuadrat medan, derau linear, sehingga SNR kira-kira linear."
        ),
        QuizQuestion(
            id = "M05_Q17",
            moduleId = "M05",
            nodeCode = "M05.T2.4",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Ketidakhomogenan 2 ppm pada 3 T setara berapa Hz (1H, 127,74 MHz)?",
            options = listOf(
                "64 Hz",
                "128 Hz",
                "255 Hz",
                "510 Hz"
            ),
            correctIndex = 2,
            explanation = "2 x 127,74 = sekitar 255 Hz."
        ),
        QuizQuestion(
            id = "M05_Q18",
            moduleId = "M05",
            nodeCode = "M05.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Jarak puncak lemak-air 3,5 ppm pada 3 T sekitar...",
            options = listOf(
                "224 Hz",
                "447 Hz",
                "64 Hz",
                "128 Hz"
            ),
            correctIndex = 1,
            explanation = "3,5 x 127,74 = sekitar 447 Hz; pada 1,5 T sekitar 224 Hz."
        ),
        QuizQuestion(
            id = "M05_Q19",
            moduleId = "M05",
            nodeCode = "M05.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Saat B0 naik, pergeseran kimia lemak-air...",
            options = listOf(
                "Tetap sama dalam Hz",
                "Makin lebar dalam Hz",
                "Menghilang",
                "Berubah dalam ppm"
            ),
            correctIndex = 1,
            explanation = "Dalam ppm tetap, tetapi dalam Hz melebar sebanding B0."
        ),

        // =========================================================================
        // MODUL 6A — SISTEM GRADIEN DAN SHIMMING (7 Soal: T1, T2, T3)
        // =========================================================================
        QuizQuestion(
            id = "M06A_Q1",
            moduleId = "M06A",
            nodeCode = "M06A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Mengapa gradien medan magnet diperlukan dalam pencitraan MRI?",
            options = listOf(
                "Agar seluruh proton di tubuh memiliki frekuensi presesi yang persis sama",
                "Agar frekuensi dan fase presesi proton berubah secara linear terhadap posisi sehingga irisan dan voksel dapat dikodekan",
                "Untuk menggantikan fungsi medan magnet utama B0 sepenuhnya",
                "Untuk menahan gelombang radio FM dari luar ruangan"
            ),
            correctIndex = 1,
            explanation = "Tanpa gradien, semua proton berpresesi pada frekuensi Larmor yang sama. Gradien menciptakan variasi medan linear terhadap posisi untuk memilih irisan dan mengode posisi spasial."
        ),
        QuizQuestion(
            id = "M06A_Q2",
            moduleId = "M06A",
            nodeCode = "M06A.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Pada pesawat MRI bore tertutup standar dengan pasien telentang, sumbu gradien manakah yang sejajar dengan arah medan utama B0 (kepala–kaki)?",
            options = listOf(
                "Sumbu X (kiri–kanan)",
                "Sumbu Y (depan–belakang)",
                "Sumbu Z (sepanjang sumbu bore kepala–kaki)",
                "Tidak ada sumbu gradien yang sejajar B0"
            ),
            correctIndex = 2,
            explanation = "Sumbu Z selalu didefinisikan sejajar dengan arah medan magnet utama B0 (sepanjang sumbu bore kepala–kaki pada magnet silinder tertutup)."
        ),
        QuizQuestion(
            id = "M06A_Q3",
            moduleId = "M06A",
            nodeCode = "M06A.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Sebuah gradien dioperasikan pada amplitudo G = 40 mT/m dengan slew rate 200 T/m/s. Berapakah waktu naik (rise time) yang dibutuhkan?",
            options = listOf(
                "0,20 ms (200 μs)",
                "0,50 ms (500 μs)",
                "5,0 ms",
                "8,0 ms"
            ),
            correctIndex = 0,
            explanation = "Waktu naik = Amplitudo / Slew rate = (40 mT/m) / (200 T/m/s) = 0,20 ms atau 200 μs."
        ),
        QuizQuestion(
            id = "M06A_Q4",
            moduleId = "M06A",
            nodeCode = "M06A.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pasangan konfigurasi kumparan gradien manakah yang digunakan pada magnet bore tertutup?",
            options = listOf(
                "Pasangan Maxwell untuk sumbu Z, dan kumparan pelana (Golay) untuk sumbu X serta Y",
                "Kumparan Golay untuk sumbu Z, dan pasangan Maxwell untuk sumbu X serta Y",
                "Birdcage untuk ketiga sumbu X, Y, dan Z",
                "Bucking coil untuk sumbu X dan Y"
            ),
            correctIndex = 0,
            explanation = "Gradien longitudinal (Z) dihasilkan oleh pasangan kumparan melingkar Maxwell, sedangkan gradien transversal (X dan Y) dihasilkan oleh pasangan kumparan pelana Golay."
        ),
        QuizQuestion(
            id = "M06A_Q5",
            moduleId = "M06A",
            nodeCode = "M06A.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa gradien seimbang (balanced gradient) memakai dua lobus berpolaritas berlawanan, dan apa pemicu bising akustik saat gradien bekerja?",
            options = listOf(
                "Lobus kedua memulihkan koherensi fase spin (rephasing); bising dipicu oleh gaya Lorentz pada kumparan berarus di dalam medan B0",
                "Lobus kedua mematikan B0; bising dipicu oleh mendidihnya helium cair",
                "Lobus kedua memperkecil SAR; bising dipicu oleh preamplifier RF",
                "Lobus kedua menggandakan TR; bising dipicu oleh kipas pendingin ruang operator"
            ),
            correctIndex = 0,
            explanation = "Dua lobus berlawanan polaritas dengan luas efektif sama mengembalikan spin ke fase nol (rephasing), sedangkan arus pulsa di dalam medan B0 menimbulkan gaya Lorentz yang menggetarkan kumparan gradien."
        ),
        QuizQuestion(
            id = "M06A_Q6",
            moduleId = "M06A",
            nodeCode = "M06A.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Bagaimana shimming dinamis orde pertama dilakukan untuk mengoreksi ketidakhomogenan B0 akibat tubuh pasien?",
            options = listOf(
                "Menambahkan arus offset DC kecil secara dinamis pada kumparan gradien X, Y, dan Z berdasarkan peta beda fase dari dua gema GRE",
                "Menempelkan pelat baja baru di dalam bore setiap kali ganti irisan",
                "Mengubah suhu helium cair di dalam kriostat",
                "Menurunkan frekuensi Larmor menjadi setengahnya"
            ),
            correctIndex = 0,
            explanation = "Shimming dinamis (gradient offset shimming) menyuntikkan arus offset pada kumparan gradien X/Y/Z setelah memetakan ketidakhomogenan ΔB0 lewat selisih fase dua gema GRE."
        ),
        QuizQuestion(
            id = "M06A_Q7",
            moduleId = "M06A",
            nodeCode = "M06A.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Pada skema Stejskal–Tanner untuk sekuens spin-echo difusi, mengapa dua lobus gradien di kedua sisi pulsa RF 180° diberikan dengan polaritas yang searah?",
            options = listOf(
                "Karena pulsa RF 180° membalik tanda fase spin, sehingga lobus kedua yang searah me-rephase spin diam sementara spin yang berdifusi mengalami atenuasi sinyal",
                "Agar seluruh spin diam ikut kehilangan sinyal sepenuhnya",
                "Karena amplifier gradien tidak bisa membalik arah arus",
                "Untuk menghilangkan efek pembobotan T2"
            ),
            correctIndex = 0,
            explanation = "Pulsa 180° membalik fase spin (−φ), sehingga lobus kedua yang berpolaritas sama akan mengembalikan fase spin diam ke nol, sedangkan molekul yang berpindah (difusi) tidak ter-rephase sempurna."
        ),

        // =========================================================================
        // MODUL 6B — SISTEM RF DAN KOIL (15 Soal: T1, T2, T3 dari SOAL M06B)
        // =========================================================================
        QuizQuestion(
            id = "M06B_Q1",
            moduleId = "M06B",
            nodeCode = "M06B.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Rentang frekuensi RF yang dipakai MRI berada pada pita...",
            options = listOf(
                "VHF (very high frequency), yang juga dipakai radio FM dan televisi",
                "Sinar-X",
                "Gelombang bunyi",
                "Inframerah"
            ),
            correctIndex = 0,
            explanation = "Frekuensi Larmor hidrogen berada di pita VHF, sehingga ruang magnet perlu dilindungi dari sumber radio luar."
        ),
        QuizQuestion(
            id = "M06B_Q2",
            moduleId = "M06B",
            nodeCode = "M06B.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Berapa frekuensi Larmor hidrogen pada 3 T (γ/2π = 42,58 MHz/T)?",
            options = listOf(
                "Sekitar 63,9 MHz",
                "Sekitar 127,7 MHz",
                "Sekitar 42,6 MHz",
                "Sekitar 255 MHz"
            ),
            correctIndex = 1,
            explanation = "Frekuensi = 42,58 × 3 = 127,74 MHz. Pada 1,5 T hasilnya 63,87 MHz."
        ),
        QuizQuestion(
            id = "M06B_Q3",
            moduleId = "M06B",
            nodeCode = "M06B.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Manakah pasangan tugas yang benar?",
            options = listOf(
                "Gradient coil memancarkan pulsa RF; RF coil memberi kode posisi",
                "Gradient coil memberi kode posisi; RF coil memancarkan pulsa dan menerima sinyal",
                "Keduanya hanya menerima sinyal",
                "Keduanya menghasilkan B0"
            ),
            correctIndex = 1,
            explanation = "Gradient coil membuat medan yang berubah bertahap untuk mengode posisi. RF coil memancarkan B1 dan menerima sinyal."
        ),
        QuizQuestion(
            id = "M06B_Q4",
            moduleId = "M06B",
            nodeCode = "M06B.T2.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Apa fungsi utama kandang Faraday pada ruang magnet?",
            options = listOf(
                "Menahan medan B0",
                "Mencegah gelombang radio luar masuk dan RF pesawat bocor keluar",
                "Mendinginkan gradient coil",
                "Menambah slew rate"
            ),
            correctIndex = 1,
            explanation = "Lapisan logam penghantar mengisolasi ruang dari gangguan radio, sehingga citra bebas artefak dan peralatan lain tidak terganggu."
        ),
        QuizQuestion(
            id = "M06B_Q5",
            moduleId = "M06B",
            nodeCode = "M06B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Bentuk pulsa RF yang menghasilkan irisan berprofil mendekati persegi adalah...",
            options = listOf(
                "Persegi pada domain waktu",
                "Sinc, yang persegi pada domain frekuensi",
                "Segitiga",
                "Konstan"
            ),
            correctIndex = 1,
            explanation = "Irisan persegi memerlukan rentang frekuensi yang rata dan sempit, dan itulah profil pulsa sinc."
        ),
        QuizQuestion(
            id = "M06B_Q6",
            moduleId = "M06B",
            nodeCode = "M06B.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa body coil birdcage dihubungkan ke dua suplai yang berbeda fase 90° (kuadratur)?",
            options = listOf(
                "Agar medan B1 berputar searah spin, sehingga energi RF terpakai efisien",
                "Agar gradien lebih cepat",
                "Agar derau hilang sepenuhnya",
                "Agar coil menjadi coil permukaan"
            ),
            correctIndex = 0,
            explanation = "Dua medan tegak lurus yang tertunda 90° menghasilkan medan sirkular yang berputar searah presesi spin."
        ),
        QuizQuestion(
            id = "M06B_Q7",
            moduleId = "M06B",
            nodeCode = "M06B.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Faktor pengisi (filling factor) yang baik berarti...",
            options = listOf(
                "Coil jauh dari tubuh",
                "Bentuk dan ukuran coil sesuai dengan bagian tubuh yang diperiksa",
                "Coil berisi banyak udara",
                "Coil sebesar bore"
            ),
            correctIndex = 1,
            explanation = "Makin cocok coil dengan anatomi, makin sedikit ruang kosong dan jaringan lain yang ikut menyumbang derau."
        ),
        QuizQuestion(
            id = "M06B_Q8",
            moduleId = "M06B",
            nodeCode = "M06B.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Manakah urutan rantai sinyal penerima yang benar?",
            options = listOf(
                "Coil, ADC, preamplifier, demodulasi",
                "Coil, preamplifier, demodulasi, ADC",
                "Preamplifier, coil, ADC, demodulasi",
                "Coil, demodulasi, ADC, preamplifier"
            ),
            correctIndex = 1,
            explanation = "Sinyal diperkuat dahulu, didemodulasi, lalu didigitalkan oleh ADC sebelum diproses komputer."
        ),
        QuizQuestion(
            id = "M06B_Q9",
            moduleId = "M06B",
            nodeCode = "M06B.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa coil permukaan hanya dipakai sebagai penerima?",
            options = listOf(
                "Karena tidak bisa menerima sinyal",
                "Karena homogenitas RF-nya buruk",
                "Karena terlalu besar",
                "Karena memakai gradien"
            ),
            correctIndex = 1,
            explanation = "Pemancar perlu RF yang merata. Coil permukaan tidak merata, tetapi sangat sensitif untuk menerima dari area dekat."
        ),
        QuizQuestion(
            id = "M06B_Q10",
            moduleId = "M06B",
            nodeCode = "M06B.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Keunggulan utama coil array fasa dibanding satu loop permukaan adalah...",
            options = listOf(
                "Hanya satu kanal sehingga murah",
                "SNR loop kecil dipertahankan pada FOV yang jauh lebih luas",
                "Dapat dipakai sebagai pemancar utama",
                "Tidak memerlukan preamplifier"
            ),
            correctIndex = 1,
            explanation = "Banyak elemen kecil, masing-masing dengan kanal sendiri, memberi SNR loop tunggal dengan cakupan yang luas."
        ),
        QuizQuestion(
            id = "M06B_Q11",
            moduleId = "M06B",
            nodeCode = "M06B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Pergeseran kimia lemak-air tetap dalam ppm. Apa yang terjadi pada selisihnya dalam Hz bila B0 dinaikkan?",
            options = listOf(
                "Menjadi lebih kecil",
                "Tidak berubah",
                "Hilang sama sekali",
                "Menjadi lebih besar"
            ),
            correctIndex = 3,
            explanation = "Selisih dalam Hz sama dengan ppm dikali frekuensi Larmor, jadi naik bersama B0."
        ),
        QuizQuestion(
            id = "M06B_Q12",
            moduleId = "M06B",
            nodeCode = "M06B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Dengan 3,5 ppm, kira-kira berapa selisih frekuensi lemak-air pada 1,5 T (63,87 MHz)?",
            options = listOf(
                "Sekitar 22 Hz",
                "Sekitar 224 Hz",
                "Sekitar 447 Hz",
                "Sekitar 3,5 kHz"
            ),
            correctIndex = 1,
            explanation = "3,5 ppm × 63,87 MHz ≈ 224 Hz. Pada 3 T hasilnya sekitar 447 Hz."
        ),
        QuizQuestion(
            id = "M06B_Q13",
            moduleId = "M06B",
            nodeCode = "M06B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Apa penyebab fisika pergeseran kimia lemak-air?",
            options = listOf(
                "Lemak dan air berada pada irisan yang berbeda",
                "Awan elektron meredam medan secara berbeda di molekul lemak dan air",
                "Gradien memberi frekuensi berbeda pada lemak",
                "Lemak tidak memiliki proton"
            ),
            correctIndex = 1,
            explanation = "Derajat perisai elektron berbeda, sehingga medan yang dirasakan proton sedikit berbeda dan frekuensi presesinya ikut berbeda."
        ),
        QuizQuestion(
            id = "M06B_Q14",
            moduleId = "M06B",
            nodeCode = "M06B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Mengapa B1 menjadi kurang merata pada B0 yang lebih tinggi?",
            options = listOf(
                "Panjang gelombang RF di jaringan memendek dan mendekati ukuran badan",
                "Gradien menjadi lebih lemah",
                "Frekuensi RF turun",
                "Coil menjadi lebih besar"
            ),
            correctIndex = 0,
            explanation = "Frekuensi naik, panjang gelombang memendek, sehingga muncul daerah penguatan dan pelemahan di dalam badan."
        ),
        QuizQuestion(
            id = "M06B_Q15",
            moduleId = "M06B",
            nodeCode = "M06B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "Apa tujuan transmit paralel?",
            options = listOf(
                "Meratakan B1 dengan mengatur amplitudo dan fase beberapa elemen pemancar secara terpisah",
                "Menambah kecepatan ADC",
                "Menghilangkan kebutuhan gradien",
                "Menurunkan frekuensi Larmor"
            ),
            correctIndex = 0,
            explanation = "Beberapa elemen pemancar yang diatur sendiri-sendiri dapat mengimbangi pola terang-gelap sehingga B1 lebih merata."
        ),
        // =========================================================================
        // MODUL 7A — ZONA, SKRINING & KESELAMATAN PERANGKAT (13 Soal: 5 T1, 8 T2)
        // =========================================================================
        QuizQuestion(
            id = "M07A_Q1",
            moduleId = "M07A",
            nodeCode = "M07A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Kapan medan magnet utama (B0) menyala?",
            options = listOf(
                "Hanya saat pemindaian",
                "Selalu menyala",
                "Hanya saat pintu ditutup",
                "Hanya malam hari"
            ),
            correctIndex = 1,
            explanation = "Magnet superkonduktor selalu menyala, jadi bahayanya ada walau tidak ada pemindaian."
        ),
        QuizQuestion(
            id = "M07A_Q2",
            moduleId = "M07A",
            nodeCode = "M07A.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Zona mana yang berisi pemindai MRI?",
            options = listOf(
                "Zona I",
                "Zona II",
                "Zona III",
                "Zona IV"
            ),
            correctIndex = 3,
            explanation = "Zona IV adalah ruang magnet tempat pemindai berada."
        ),
        QuizQuestion(
            id = "M07A_Q3",
            moduleId = "M07A",
            nodeCode = "M07A.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Jenis implan pasien tidak bisa dipastikan dengan bukti dokumen. Apa langkah yang sesuai konsep modul ini?",
            options = listOf(
                "Lanjut berdasarkan keterangan lisan",
                "Lanjut dengan sudut flip kecil",
                "Lanjut asal memakai proteksi telinga",
                "Tunda atau batalkan sampai terbukti aman"
            ),
            correctIndex = 3,
            explanation = "Keterangan lisan saja tidak cukup. Pemeriksaan ditunda sampai keamanan terbukti melalui bukti dokumen."
        ),
        QuizQuestion(
            id = "M07A_Q4",
            moduleId = "M07A",
            nodeCode = "M07A.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Siapa yang memikul tanggung jawab utama atas analisis risiko dan manfaat pada pasien dengan perangkat implan?",
            options = listOf(
                "Pasien sendiri",
                "Dokter perujuk dan radiology lead",
                "Petugas kebersihan",
                "Produsen perangkat saja"
            ),
            correctIndex = 1,
            explanation = "Tanggung jawab utama ada pada dokter perujuk dan radiology lead. Praktisi MRI juga boleh menunda bila bukti kurang."
        ),
        QuizQuestion(
            id = "M07A_Q5",
            moduleId = "M07A",
            nodeCode = "M07A.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "Manakah yang BUKAN salah satu dari lima sumber bahaya utama pada bagian ini?",
            options = listOf(
                "Kriogen",
                "Medan RF",
                "Efek psikologis",
                "Efek kimia zat kontras gadolinium"
            ),
            correctIndex = 3,
            explanation = "Lima sumber bahaya: faktor psikologis, medan statis, medan RF, gradien, dan kriogen. Keamanan kontras gadolinium adalah keselamatan obat dan dibahas terpisah di T2.6."
        ),
        QuizQuestion(
            id = "M07A_Q6",
            moduleId = "M07A",
            nodeCode = "M07A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apa arti label MR Conditional pada sebuah implan?",
            options = listOf(
                "Aman dalam semua kondisi",
                "Aman bila syarat pada label dipenuhi",
                "Selalu berbahaya",
                "Tidak perlu skrining"
            ),
            correctIndex = 1,
            explanation = "MR Conditional berarti aman hanya bila syarat yang tertulis dipenuhi."
        ),
        QuizQuestion(
            id = "M07A_Q7",
            moduleId = "M07A",
            nodeCode = "M07A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Sebuah perangkat MR Conditional aman pada medan rendah. Apa kesimpulan yang tepat untuk medan yang lebih tinggi?",
            options = listOf(
                "Otomatis aman",
                "Otomatis tidak aman",
                "Tidak perlu dicek lagi",
                "Belum tentu aman, syaratnya harus diperiksa"
            ),
            correctIndex = 3,
            explanation = "MR Conditional hanya berlaku pada syarat yang tertera, termasuk kekuatan medan. Aman pada medan rendah belum tentu aman pada medan tinggi."
        ),
        QuizQuestion(
            id = "M07A_Q8",
            moduleId = "M07A",
            nodeCode = "M07A.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pada tahap mana kecemasan pasien biasanya paling tinggi?",
            options = listOf(
                "Di akhir pemeriksaan",
                "Di tengah pemindaian",
                "Saat dimasukkan ke bore",
                "Saat berganti pakaian"
            ),
            correctIndex = 2,
            explanation = "Kecemasan biasanya memuncak saat pasien dimasukkan ke bore lalu menurun, sehingga strategi menenangkan difokuskan pada tahap itu."
        ),
        QuizQuestion(
            id = "M07A_Q9",
            moduleId = "M07A",
            nodeCode = "M07A.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Di zona mana jawaban skrining dan riwayat pasien diambil, dan pasien belum bebas bergerak?",
            options = listOf(
                "Zona I",
                "Zona II",
                "Zona III",
                "Zona IV"
            ),
            correctIndex = 1,
            explanation = "Zona II adalah batas antara area umum dan zona terkontrol: pasien disambut, ditanyai, dan diawasi."
        ),
        QuizQuestion(
            id = "M07A_Q10",
            moduleId = "M07A",
            nodeCode = "M07A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Siapa yang termasuk personel Level 2?",
            options = listOf(
                "Pengunjung rumah sakit",
                "Staf kantor dengan pendidikan keselamatan minimal",
                "Pasien yang sudah diskrining",
                "Teknolog MRI, radiolog, dan perawat yang dilatih mendalam"
            ),
            correctIndex = 3,
            explanation = "Level 2 dilatih mendalam, termasuk soal pemanasan, luka bakar, dan rangsangan saraf-otot akibat gradien."
        ),
        QuizQuestion(
            id = "M07A_Q11",
            moduleId = "M07A",
            nodeCode = "M07A.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Anda ditekan agar tetap memindai padahal ragu soal keselamatan. Tindakan yang sesuai?",
            options = listOf(
                "Lanjutkan agar jadwal tidak tertunda",
                "Kecilkan sudut flip lalu lanjut",
                "Tunda pemeriksaan",
                "Minta pasien menandatangani formulir lalu lanjut"
            ),
            correctIndex = 2,
            explanation = "Jangan membiarkan diri ditekan. Bila ragu, tunda pemeriksaan."
        ),
        QuizQuestion(
            id = "M07A_Q12",
            moduleId = "M07A",
            nodeCode = "M07A.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Kondisi mana yang paling perlu ditinjau ulang sebelum kontras gadolinium karena risiko NSF?",
            options = listOf(
                "Alergi serbuk sari",
                "Gangguan ginjal berat atau cedera ginjal akut",
                "Tinggi badan",
                "Warna rambut"
            ),
            correctIndex = 1,
            explanation = "NSF dikaitkan dengan gadolinium pada gangguan ginjal berat atau cedera ginjal akut, sehingga eGFR diperiksa pada pasien berisiko."
        ),
        QuizQuestion(
            id = "M07A_Q13",
            moduleId = "M07A",
            nodeCode = "M07A.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Bagaimana sikap yang sesuai terhadap kontras gadolinium pada pasien hamil?",
            options = listOf(
                "Selalu diberikan",
                "Dosis digandakan",
                "Dihindari kecuali manfaatnya jelas lebih besar daripada risikonya",
                "Tidak perlu ditanyakan"
            ),
            correctIndex = 2,
            explanation = "Gadolinium melewati plasenta. Keputusan ada pada radiolog dan dokter perujuk."
        ),
        // =========================================================================
        // MODUL 7B — MEDAN STATIS (B0), PROYEKTIL & KRIOGEN (10 Soal: 7 T2, 3 T3)
        // =========================================================================
        QuizQuestion(
            id = "M07B_Q1",
            moduleId = "M07B",
            nodeCode = "M07B.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa kursi roda ferromagnetik berbahaya di ruang magnet berperisai aktif?",
            options = listOf(
                "Karena medan tepinya sangat luas",
                "Karena kursi roda memanaskan magnet",
                "Karena medan turun curam, tarikan baru terasa saat sudah dekat",
                "Karena kursi roda mematikan B0"
            ),
            correctIndex = 2,
            explanation = "Pada magnet berperisai aktif, tarikan hampir tak terasa dari jauh lalu naik tajam di dekat bore."
        ),
        QuizQuestion(
            id = "M07B_Q2",
            moduleId = "M07B",
            nodeCode = "M07B.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apa yang cenderung terjadi pada klip aneurisma ferromagnetik di jaringan lunak saat berada di medan statis?",
            options = listOf(
                "Berputar sampai sejajar garis medan (torsi)",
                "Tidak terjadi apa pun",
                "Mencair karena panas",
                "Berubah menjadi MR Safe"
            ),
            correctIndex = 0,
            explanation = "Gaya putar (torsi) membuat sumbu panjang implan menyelaras dengan medan dan dapat merusak jaringan sekitarnya."
        ),
        QuizQuestion(
            id = "M07B_Q3",
            moduleId = "M07B",
            nodeCode = "M07B.T2.7",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Kadar oksigen ruang magnet turun di bawah 19,5 persen. Apa yang seharusnya terjadi?",
            options = listOf(
                "Tidak ada yang perlu dilakukan",
                "Alarm sensor oksigen berbunyi",
                "Pintu terkunci otomatis",
                "Magnet dimatikan sendiri"
            ),
            correctIndex = 1,
            explanation = "Ruang magnet punya sensor oksigen yang dikalibrasi untuk berbunyi saat kadar oksigen turun di bawah ambang itu."
        ),
        QuizQuestion(
            id = "M07B_Q4",
            moduleId = "M07B",
            nodeCode = "M07B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Mengapa pintu ruang magnet bisa sulit dibuka setelah pipa quench rusak?",
            options = listOf(
                "Magnet menarik pintu",
                "Tekanan udara di ruangan naik dan menekan pintu yang membuka ke dalam",
                "Helium membekukan engsel pintu",
                "Alarm mengunci pintu"
            ),
            correctIndex = 1,
            explanation = "Kenaikan tekanan yang kecil pun memberi gaya besar pada pintu yang membuka ke dalam. Menyamakan tekanan mempercepat akses."
        ),
        QuizQuestion(
            id = "M07B_Q5",
            moduleId = "M07B",
            nodeCode = "M07B.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Garis 5 gauss setara berapa mT?",
            options = listOf(
                "5 mT",
                "0,5 mT",
                "50 mT",
                "0,05 mT"
            ),
            correctIndex = 1,
            explanation = "5 gauss sama dengan 0,5 mT."
        ),
        QuizQuestion(
            id = "M07B_Q6",
            moduleId = "M07B",
            nodeCode = "M07B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Siapa yang paling sering terlibat insiden proyektil?",
            options = listOf(
                "Radiolog yang bertugas",
                "Pasien yang sudah diskrining",
                "Staf non-MRI yang masuk tanpa pengawasan",
                "Teknisi gradient coil"
            ),
            correctIndex = 2,
            explanation = "Contohnya petugas kebersihan, porter, atau teknisi anestesi. Karena itu zonasi, pintu terkunci, dan pelatihan semua staf penting."
        ),
        QuizQuestion(
            id = "M07B_Q7",
            moduleId = "M07B",
            nodeCode = "M07B.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Bagaimana cara mengurangi keparahan vertigo saat pasien masuk ke medan sangat tinggi?",
            options = listOf(
                "Menggerakkan meja lebih cepat",
                "Menggerakkan meja perlahan",
                "Mematikan magnet",
                "Mengganti koil"
            ),
            correctIndex = 1,
            explanation = "Makin cepat meja bergerak, makin berat vertigo, jadi meja digerakkan perlahan."
        ),
        QuizQuestion(
            id = "M07B_Q8",
            moduleId = "M07B",
            nodeCode = "M07B.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa logam non-ferrous seperti aluminium dapat melambat saat bergerak dalam medan statis?",
            options = listOf(
                "Karena ditarik magnet",
                "Karena memanas oleh RF",
                "Karena arus induksi melawan perubahan medan (efek Lenz)",
                "Karena berubah menjadi ferromagnetik"
            ),
            correctIndex = 2,
            explanation = "Arus induksi selalu melawan perubahan medan yang menimbulkannya."
        ),
        QuizQuestion(
            id = "M07B_Q9",
            moduleId = "M07B",
            nodeCode = "M07B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Seseorang terjepit antara tabung oksigen ferromagnetik dan magnet. Tindakan yang dibenarkan?",
            options = listOf(
                "Quench magnet untuk membebaskannya",
                "Menunggu pemindaian selesai",
                "Memindai ulang",
                "Mematikan lampu ruangan"
            ),
            correctIndex = 0,
            explanation = "Quench hanya untuk ancaman nyawa akibat medan, misalnya orang terjepit benda ferromagnetik."
        ),
        QuizQuestion(
            id = "M07B_Q10",
            moduleId = "M07B",
            nodeCode = "M07B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Pasien mendadak pingsan di Zona IV tanpa terjepit. Apa yang dilakukan?",
            options = listOf(
                "Quench magnet",
                "Biarkan di bore sampai magnet turun",
                "Keluarkan pasien dan tangani di luar Zona IV",
                "Tunggu tim datang ke ruang magnet"
            ),
            correctIndex = 2,
            explanation = "Pasien yang sakit mendadak dikeluarkan dan ditangani di luar Zona IV. Quench tidak dipakai untuk itu."
        ),
        // =========================================================================
        // MODUL 7C — RF (SAR), GRADIEN & BISING (12 Soal: 9 T2, 3 T3)
        // =========================================================================
        QuizQuestion(
            id = "M07C_Q1",
            moduleId = "M07C",
            nodeCode = "M07C.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Berapa batas SAR seluruh tubuh pada mode normal?",
            options = listOf(
                "2 W/kg",
                "4 W/kg",
                "3,2 W/kg",
                "Tidak ada batas"
            ),
            correctIndex = 0,
            explanation = "Mode normal untuk seluruh tubuh 2 W/kg. Mode tingkat satu 4 W/kg."
        ),
        QuizQuestion(
            id = "M07C_Q2",
            moduleId = "M07C",
            nodeCode = "M07C.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa lensa mata sangat peka terhadap pemanasan RF?",
            options = listOf(
                "Karena tidak punya aliran darah untuk membuang panas",
                "Karena banyak mengandung logam",
                "Karena letaknya dekat koil",
                "Karena sangat tipis"
            ),
            correctIndex = 0,
            explanation = "Lensa tidak memiliki suplai darah, sehingga panas sulit tersebar."
        ),
        QuizQuestion(
            id = "M07C_Q3",
            moduleId = "M07C",
            nodeCode = "M07C.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Dibanding pulsa 90 derajat, pulsa 180 derajat memberi energi RF sebesar...",
            options = listOf(
                "Setengahnya",
                "Sama",
                "Empat kali lipat",
                "Dua kali lipat"
            ),
            correctIndex = 2,
            explanation = "Energi naik dengan kuadrat sudut flip, jadi pulsa 180 derajat memberi empat kali energi pulsa 90 derajat."
        ),
        QuizQuestion(
            id = "M07C_Q4",
            moduleId = "M07C",
            nodeCode = "M07C.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Di bagian mana kawat yang beresonansi seperti antena memanas paling kuat?",
            options = listOf(
                "Di tengah kawat",
                "Di seluruh kawat sama rata",
                "Di ujung yang jauh dari pasien",
                "Di ujung kawat (antinode)"
            ),
            correctIndex = 3,
            explanation = "Pada gelombang berdiri, antinode berada di ujung kawat dan di sanalah energi terbesar sehingga pemanasan terjadi."
        ),
        QuizQuestion(
            id = "M07C_Q5",
            moduleId = "M07C",
            nodeCode = "M07C.T2.7",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Tingkat tekanan bunyi naik 3 dB. Apa artinya bagi intensitas bunyi?",
            options = listOf(
                "Menjadi dua kali lipat",
                "Naik sedikit sekali",
                "Menjadi setengahnya",
                "Tidak berubah"
            ),
            correctIndex = 0,
            explanation = "Skala desibel logaritmik: kenaikan 3 dB berarti intensitas dua kali lipat."
        ),
        QuizQuestion(
            id = "M07C_Q6",
            moduleId = "M07C",
            nodeCode = "M07C.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Mengapa panjang gelombang RF di dalam darah lebih pendek daripada di vakum?",
            options = listOf(
                "Karena darah dingin",
                "Karena permitivitas relatif darah lebih dari 1",
                "Karena darah mengandung banyak besi bebas",
                "Karena frekuensi RF berubah"
            ),
            correctIndex = 1,
            explanation = "Permitivitas relatif jaringan lebih dari 1, sehingga panjang gelombang RF memendek dan panjang dipol kritisnya ikut memendek."
        ),
        QuizQuestion(
            id = "M07C_Q7",
            moduleId = "M07C",
            nodeCode = "M07C.T3.4",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) SAR naik kira-kira dengan apa?",
            options = listOf(
                "Akar B0 saja",
                "Berbanding terbalik dengan B0",
                "Kuadrat B0 dan kuadrat sudut flip",
                "Tidak bergantung pada keduanya"
            ),
            correctIndex = 2,
            explanation = "SAR naik kira-kira dengan kuadrat B0 dan kuadrat sudut flip."
        ),
        QuizQuestion(
            id = "M07C_Q8",
            moduleId = "M07C",
            nodeCode = "M07C.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Mengapa pernyataan \"MRI tidak memakai radiasi\" kurang tepat?",
            options = listOf(
                "MRI memakai sinar-X dosis kecil",
                "MRI memakai radiasi non-pengion (gelombang RF)",
                "MRI memakai radiasi neutron",
                "Pernyataan itu tepat"
            ),
            correctIndex = 1,
            explanation = "MRI memakai radiasi non-pengion: energinya terlalu rendah untuk memutus ikatan kimia."
        ),
        QuizQuestion(
            id = "M07C_Q9",
            moduleId = "M07C",
            nodeCode = "M07C.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Apa fungsi bantalan isolasi di antara kulit yang bersentuhan?",
            options = listOf(
                "Menahan getaran",
                "Memutus loop konduktif agar arus tidak terkonsentrasi",
                "Menurunkan B0",
                "Menambah sinyal"
            ),
            correctIndex = 1,
            explanation = "Bantalan memutus loop sehingga arus induksi tidak terkonsentrasi di satu titik kulit."
        ),
        QuizQuestion(
            id = "M07C_Q10",
            moduleId = "M07C",
            nodeCode = "M07C.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Penyebab kematian terkait MRI yang paling umum dilaporkan adalah...",
            options = listOf(
                "Luka bakar",
                "Proyektil",
                "Malfungsi pacu jantung",
                "Asfiksia helium"
            ),
            correctIndex = 2,
            explanation = "Malfungsi pacu jantung adalah penyebab kematian terkait MRI yang paling umum dilaporkan."
        ),
        QuizQuestion(
            id = "M07C_Q11",
            moduleId = "M07C",
            nodeCode = "M07C.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "Pasien merasa ketukan atau kesemutan di tungkai saat pemindaian. Penyebab paling mungkin?",
            options = listOf(
                "Medan statis menarik tulang",
                "Helium bocor",
                "RF memanaskan sumsum",
                "Gradien yang berganti cepat menginduksi arus melewati ambang saraf tepi"
            ),
            correctIndex = 3,
            explanation = "Arus induksi dari gradien yang berganti cepat dapat melewati ambang saraf tepi."
        ),
        QuizQuestion(
            id = "M07C_Q12",
            moduleId = "M07C",
            nodeCode = "M07C.T3.3",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Mode operasi mana yang hanya untuk riset dan dikunci perangkat lunak?",
            options = listOf(
                "Mode normal",
                "Mode tingkat satu",
                "Mode tingkat dua",
                "Semua mode"
            ),
            correctIndex = 2,
            explanation = "Mode tingkat dua hanya untuk riset, tidak untuk pemeriksaan klinis."
        ),
        // =========================================================================
        // MODUL 8A — ARTEFAK PENGODEAN DAN JARINGAN (24 Soal: 4 T1, 12 T2, 8 T3)
        // =========================================================================
        QuizQuestion(
            id = "M08A_Q1",
            moduleId = "M08A",
            nodeCode = "M08A.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Modul 4) Citra menampilkan lipatan: bagian kepala di luar FOV muncul di sisi seberang. Apa penyebab fisiknya?",
            options = listOf(
                "FOV pada arah fase lebih kecil dari objek",
                "Frekuensi Larmor terlalu tinggi",
                "Ruang berpelindung bocor",
                "Gradient coil terlalu panas"
            ),
            correctIndex = 0,
            explanation = "Sinyal dari luar FOV salah diberi posisi pada arah fase, sehingga terlipat. Memperbesar FOV atau oversampling mengurangi aliasing."
        ),
        QuizQuestion(
            id = "M08A_Q2",
            moduleId = "M08A",
            nodeCode = "M08A.T1.3",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Modul 4 dan 6) Citra memperlihatkan satu bayangan objek yang bergeser jauh pada arah fase. Artefak apa yang paling sesuai?",
            options = listOf(
                "Zipper",
                "Gibbs",
                "Ghosting fase",
                "Pergeseran kimia"
            ),
            correctIndex = 2,
            explanation = "Kesalahan fase berulang antar garis ruang-k menghasilkan salinan objek yang bergeser. Zipper berupa garis tipis, Gibbs berupa pita di tepi."
        ),
        QuizQuestion(
            id = "M08A_Q3",
            moduleId = "M08A",
            nodeCode = "M08A.T1.4",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Modul 6) Bagaimana selisih frekuensi lemak dan air (pergeseran kimia) berubah saat B0 naik?",
            options = listOf(
                "Selisih dalam Hz mengecil",
                "Selisih dalam Hz membesar",
                "Selisih hilang sama sekali",
                "Selisih tidak ada hubungannya dengan B0"
            ),
            correctIndex = 1,
            explanation = "Selisih tetap dalam ppm, tetapi frekuensi Larmor naik bersama B0. Jadi selisih dalam Hz membesar."
        ),
        QuizQuestion(
            id = "M08A_Q4",
            moduleId = "M08A",
            nodeCode = "M08A.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Aliasing) Mengapa sinyal dari luar FOV bisa muncul di dalam citra?",
            options = listOf(
                "Frekuensinya melewati batas Nyquist sehingga terbaca sebagai frekuensi yang lebih rendah",
                "Gradien hilang di luar FOV",
                "Pulsa RF tidak mencapai area itu",
                "B0 terlalu tinggi"
            ),
            correctIndex = 0,
            explanation = "Anatomi di luar FOV tetap menghasilkan sinyal. Frekuensi di atas Nyquist tidak terdigitalisasi benar dan dipetakan ke frekuensi yang lebih rendah."
        ),
        QuizQuestion(
            id = "M08A_Q5",
            moduleId = "M08A",
            nodeCode = "M08A.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Phase mismapping) Mengapa hantu gerak hanya muncul pada arah fase?",
            options = listOf(
                "Titik data antar garis ruang-k diambil tiap TR (lama), sedangkan sepanjang satu garis diambil dalam milidetik",
                "Gradien frekuensi tidak pernah dipakai",
                "B0 hanya bekerja pada arah fase",
                "Arah frekuensi tidak punya ruang-k"
            ),
            correctIndex = 0,
            explanation = "Peluang anatomi bergerak jauh lebih besar dalam selang antar garis (ratusan sampai ribuan milidetik) daripada dalam selang antar titik pada satu garis."
        ),
        QuizQuestion(
            id = "M08A_Q6",
            moduleId = "M08A",
            nodeCode = "M08A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Phase mismapping) Jika TR, matriks fase, dan NSA dinaikkan, apa yang terjadi pada hantu gerak?",
            options = listOf(
                "Jarak antar hantu bertambah dan hantu di dalam citra berkurang",
                "Jarak antar hantu menyusut",
                "Hantu pindah ke arah frekuensi",
                "Tidak ada perubahan"
            ),
            correctIndex = 0,
            explanation = "Jarak antar hantu sebanding dengan TR × matriks fase × NSA dibagi periode gerak, yaitu waktu pemindaian dibagi periode gerak."
        ),
        QuizQuestion(
            id = "M08A_Q7",
            moduleId = "M08A",
            nodeCode = "M08A.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Phase mismapping) Apa yang dilakukan strategi menukar arah fase dan frekuensi?",
            options = listOf(
                "Memindahkan hantu menjauh dari area yang diperiksa, tanpa menghilangkannya",
                "Menghilangkan gerak sepenuhnya",
                "Menaikkan SNR dua kali lipat",
                "Menghapus aliasing"
            ),
            correctIndex = 0,
            explanation = "Hantu hanya muncul pada arah fase. Menukar arah memindahkan hantu, tetapi tidak mengurangi gerak itu sendiri."
        ),
        QuizQuestion(
            id = "M08A_Q8",
            moduleId = "M08A",
            nodeCode = "M08A.T3.3",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Kompensasi napas) Bagaimana kompensasi napas mengurangi hantu?",
            options = listOf(
                "Mengurutkan ulang garis ruang-k: garis tengah diisi saat gerak dada minimal, garis tepi saat gerak maksimal",
                "Menutup pintu ruang magnet",
                "Menambah TR tanpa batas",
                "Mengganti B0 selama napas"
            ),
            correctIndex = 0,
            explanation = "Garis tengah memberi sinyal dan kontras, sehingga diambil saat gerak kecil. Data ruang-k jadi tidak periodik."
        ),
        QuizQuestion(
            id = "M08A_Q9",
            moduleId = "M08A",
            nodeCode = "M08A.T3.3",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Triggering dan gating) Apa kelemahan gating napas dibandingkan triggering?",
            options = listOf(
                "Banyak data ditolak sehingga waktu pemindaian naik",
                "TR menjadi sama dengan periode napas",
                "Kontras bercampur karena laju napas berubah",
                "Tidak butuh sensor napas"
            ),
            correctIndex = 0,
            explanation = "Gating menolak data di luar gerbang (hingga sekitar 60 % menurut buku acuan), tetapi tidak membatasi TR. Kelemahan TR terikat napas ada pada triggering."
        ),
        QuizQuestion(
            id = "M08A_Q10",
            moduleId = "M08A",
            nodeCode = "M08A.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Aliasing) Apa konsekuensi memakai phase oversampling (antialiasing arah fase)?",
            options = listOf(
                "Garis ruang-k bertambah sehingga waktu pemindaian naik, kecuali diimbangi, misalnya dengan menurunkan NSA",
                "Ukuran piksel mengecil dua kali",
                "Aliasing pindah ke arah frekuensi",
                "SNR turun drastis tanpa kompensasi apa pun"
            ),
            correctIndex = 0,
            explanation = "Oversampling menambah langkah fase. Sebagian produsen mengimbangi dengan membagi dua NSA, dan SNR hampir tidak berubah."
        ),
        QuizQuestion(
            id = "M08A_Q11",
            moduleId = "M08A",
            nodeCode = "M08A.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Aliasing) Bagaimana aliasing pada arah frekuensi dicegah?",
            options = listOf(
                "Menaikkan laju sampling digital dan memakai filter low-pass untuk membuang frekuensi di luar bandwidth",
                "Menaikkan TR",
                "Menambah NSA",
                "Menutup pintu ruang magnet"
            ),
            correctIndex = 0,
            explanation = "Sampling lebih rapat dengan jendela sampling sama, lalu hanya frekuensi tengah yang ditampilkan."
        ),
        QuizQuestion(
            id = "M08A_Q12",
            moduleId = "M08A",
            nodeCode = "M08A.T3.4",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Bandwidth total 64 kHz dan frekuensi sebenarnya anatomi +35 kHz. Frekuensi yang dipersepsikan sistem adalah ...",
            options = listOf(
                "−29 kHz",
                "+35 kHz",
                "+3 kHz",
                "−35 kHz"
            ),
            correctIndex = 0,
            explanation = "f = 35 − 2 × 32 = −29 kHz. Nyquist adalah setengah bandwidth (32 kHz)."
        ),
        QuizQuestion(
            id = "M08A_Q13",
            moduleId = "M08A",
            nodeCode = "M08A.T1.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Pergeseran kimia) Cara mengurangi pergeseran kimia adalah ...",
            options = listOf(
                "Menaikkan bandwidth penerimaan, memperkecil FOV, atau memakai B0 lebih rendah",
                "Menyempitkan bandwidth penerimaan",
                "Memperbesar FOV",
                "Menambah celah irisan"
            ),
            correctIndex = 0,
            explanation = "Bandwidth lebih lebar membuat tiap piksel mencakup lebih banyak Hz, sehingga selisih lemak-air menjadi pergeseran piksel yang kecil."
        ),
        QuizQuestion(
            id = "M08A_Q14",
            moduleId = "M08A",
            nodeCode = "M08A.T3.5",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Pergeseran kimia) Mengapa menyempitkan bandwidth penerimaan tidak selalu baik?",
            options = listOf(
                "SNR naik, tetapi pergeseran kimia membesar",
                "SNR turun dan pergeseran kimia hilang",
                "Ukuran piksel membesar",
                "Aliasing menghilang"
            ),
            correctIndex = 0,
            explanation = "Bandwidth sempit menurunkan derau (SNR naik) tetapi membuat bandwidth tiap piksel kecil, sehingga pergeseran dalam piksel membesar."
        ),
        QuizQuestion(
            id = "M08A_Q15",
            moduleId = "M08A",
            nodeCode = "M08A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Luar-fase) Apa penyebab pembatalan sinyal luar-fase?",
            options = listOf(
                "Lemak dan air dalam satu voksel berlawanan fase pada TE tertentu dan saling meniadakan",
                "Sinyal lemak bergeser sepanjang arah frekuensi",
                "RF luar masuk lewat pintu yang terbuka",
                "FOV lebih kecil dari objek"
            ),
            correctIndex = 0,
            explanation = "Lemak dan air berpresesi pada frekuensi berbeda, sehingga fasenya berselisih. Pada selisih 180° sinyalnya saling meniadakan."
        ),
        QuizQuestion(
            id = "M08A_Q16",
            moduleId = "M08A",
            nodeCode = "M08A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Luar-fase) Apa beda pembatalan luar-fase dengan pergeseran kimia?",
            options = listOf(
                "Luar-fase tidak menggeser posisi sinyal dan dapat muncul di arah mana pun",
                "Luar-fase selalu menggeser sinyal sepanjang arah fase",
                "Keduanya sama persis",
                "Pergeseran kimia hanya terjadi pada spin echo"
            ),
            correctIndex = 0,
            explanation = "Pergeseran kimia memindahkan sinyal lemak sepanjang arah frekuensi. Luar-fase terjadi di piksel yang memuat lemak dan air, tanpa pergeseran posisi."
        ),
        QuizQuestion(
            id = "M08A_Q17",
            moduleId = "M08A",
            nodeCode = "M08A.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Luar-fase) Cara mengurangi pembatalan luar-fase adalah ...",
            options = listOf(
                "Memilih TE yang sesuai periodisitas lemak-air atau memakai spin echo",
                "Menutup pintu ruang magnet",
                "Menambah langkah pengodean fase",
                "Memberi celah antar irisan"
            ),
            correctIndex = 0,
            explanation = "Pulsa rephasing 180° pada spin echo mengompensasi selisih fase, sedangkan rephasing gradien tidak."
        ),
        QuizQuestion(
            id = "M08A_Q18",
            moduleId = "M08A",
            nodeCode = "M08A.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Saat B0 naik dari 1,5 T ke 3 T, periodisitas lemak-air ...",
            options = listOf(
                "Memendek, karena selisih frekuensi dalam Hz membesar",
                "Memanjang, karena selisih frekuensi mengecil",
                "Tetap sama",
                "Menjadi nol"
            ),
            correctIndex = 0,
            explanation = "Periode adalah kebalikan selisih frekuensi. Selisih dalam Hz naik bersama B0, jadi periodenya memendek."
        ),
        QuizQuestion(
            id = "M08A_Q19",
            moduleId = "M08A",
            nodeCode = "M08A.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Sudut ajaib) Mengapa tendon bisa tampak terang pada sudut tertentu terhadap B0?",
            options = listOf(
                "Pada sudut itu interaksi spin-spin turun, T2 memanjang, sehingga sinyal naik pada TE pendek",
                "Tendon berhenti mengandung kolagen",
                "B0 menjadi nol di sudut itu",
                "Pulsa RF menjadi lebih kuat"
            ),
            correctIndex = 0,
            explanation = "Kolagen biasanya bersinyal rendah karena T2 sangat pendek. Pada sudut sekitar 55° T2 memanjang, dan sinyal tampak pada TE pendek."
        ),
        QuizQuestion(
            id = "M08A_Q20",
            moduleId = "M08A",
            nodeCode = "M08A.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Sudut ajaib) Cara mengurangi artefak sudut ajaib adalah ...",
            options = listOf(
                "Mengubah sudut anatomi terhadap B0 atau memperpanjang TE",
                "Menutup pintu ruang magnet",
                "Menambah celah antar irisan",
                "Memperkecil FOV"
            ),
            correctIndex = 0,
            explanation = "Dengan sudut berbeda atau TE lebih panjang, sinyal tendon turun. Jika tetap terang, kemungkinan itu cairan."
        ),
        QuizQuestion(
            id = "M08A_Q21",
            moduleId = "M08A",
            nodeCode = "M08A.T2.4",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Dari 3cos²θ − 1 = 0, sudut ajaib kira-kira ...",
            options = listOf(
                "55°",
                "0°",
                "90°",
                "30°"
            ),
            correctIndex = 0,
            explanation = "cos²θ = 1/3, jadi θ sekitar 54,7°. Buku acuan membulatkannya menjadi 55°."
        ),
        QuizQuestion(
            id = "M08A_Q22",
            moduleId = "M08A",
            nodeCode = "M08A.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Truncation) Cara yang tepat untuk mengurangi artefak truncation adalah ...",
            options = listOf(
                "Menambah langkah pengodean fase sehingga data ruang-k lebih lengkap",
                "Mengurangi jumlah langkah fase",
                "Memakai partial Fourier sebanyak mungkin",
                "Membuka pintu ruang magnet"
            ),
            correctIndex = 0,
            explanation = "Artefak ini muncul karena data kurang. Lebih banyak langkah fase mengisi ruang-k lebih lengkap."
        ),
        QuizQuestion(
            id = "M08A_Q23",
            moduleId = "M08A",
            nodeCode = "M08A.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Truncation) Pada simulasi A8, matriks fase diturunkan dari 96 ke 24 garis. Apa yang terjadi?",
            options = listOf(
                "Ringing di dekat tepi tajam membesar dan waktu pemindaian turun",
                "Ringing hilang dan waktu pemindaian naik",
                "Citra bergeser pada arah frekuensi",
                "Aliasing muncul pada arah frekuensi"
            ),
            correctIndex = 0,
            explanation = "Data frekuensi tinggi yang terpotong membuat tepi berosilasi. Langkah fase lebih sedikit memendekkan waktu pemindaian."
        ),
        QuizQuestion(
            id = "M08A_Q24",
            moduleId = "M08A",
            nodeCode = "M08A.T2.5",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Apa harga memakai filter di ujung jendela sampling untuk menekan ringing Gibbs?",
            options = listOf(
                "Tepi menjadi lebih kabur, resolusi efektif turun",
                "Waktu pemindaian naik dua kali lipat",
                "Aliasing arah fase muncul",
                "SAR naik"
            ),
            correctIndex = 0,
            explanation = "Filter melemahkan data frekuensi tinggi di tepi ruang-k. Osilasi mengecil, tetapi tepi tajam ikut melunak."
        ),

        // =========================================================================
        // MODUL 8B — ALIRAN DAN ARTEFAK ALIRAN (15 Soal: 5 T1, 6 T2, 4 T3)
        // =========================================================================
        QuizQuestion(
            id = "M08B_Q1",
            moduleId = "M08B",
            nodeCode = "M08B.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Tingkat 1) Efek yang membuat lumen pembuluh tampak hiperintens karena spin yang baru masuk irisan adalah...",
            options = listOf(
                "Entry-slice phenomenon",
                "Time-of-flight signal loss",
                "Cross-excitation",
                "Truncation"
            ),
            correctIndex = 0,
            explanation = "ESP menghasilkan peningkatan sinyal terkait aliran."
        ),
        QuizQuestion(
            id = "M08B_Q2",
            moduleId = "M08B",
            nodeCode = "M08B.T1.2",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Tingkat 1) Mengapa inti jaringan diam mengalami saturasi pada TR pendek?",
            options = listOf(
                "Menerima pulsa RF berulang sebelum magnetisasi longitudinal pulih",
                "Tidak pernah terkena RF",
                "Selalu berada di spin-up",
                "Bergerak searah gradien"
            ),
            correctIndex = 0,
            explanation = "TR yang pendek tidak memberi waktu pemulihan longitudinal penuh."
        ),
        QuizQuestion(
            id = "M08B_Q3",
            moduleId = "M08B",
            nodeCode = "M08B.T1.1",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Tingkat 1) Mengapa spin darah yang mengalir sepanjang gradien dapat menimbulkan artefak?",
            options = listOf(
                "Karena mengalami medan yang berbeda sehingga fasenya berubah",
                "Karena darah tidak mengandung proton",
                "Karena aliran menaikkan B0",
                "Karena aliran menghilangkan frekuensi Larmor"
            ),
            correctIndex = 0,
            explanation = "Spin yang mengalir melewati kekuatan medan berbeda sehingga fasenya berubah. Akibatnya terjadi dephasing intravoksel atau pemetaan fase salah."
        ),
        QuizQuestion(
            id = "M08B_Q4",
            moduleId = "M08B",
            nodeCode = "M08B.T2.5",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Tingkat 1) Apa tujuan utama pulsa presaturasi spasial?",
            options = listOf(
                "Menaikkan resolusi",
                "Menaikkan SNR di seluruh FOV",
                "Menjenuhkan spin yang akan mengalir masuk agar tidak bersinyal",
                "Memendekkan TR"
            ),
            correctIndex = 2,
            explanation = "Spin yang sudah jenuh tidak punya magnetisasi transversal saat masuk irisan, jadi tidak menghasilkan sinyal."
        ),
        QuizQuestion(
            id = "M08B_Q5",
            moduleId = "M08B",
            nodeCode = "M08B.T2.4",
            levelBadge = "Tingkat 1 • Dasar",
            level = 1,
            question = "(Tingkat 1) Manakah nama lain gradient moment rephasing?",
            options = listOf(
                "Interleaving",
                "Gradient moment nulling atau flow compensation",
                "Squaring-off",
                "Antialiasing"
            ),
            correctIndex = 1,
            explanation = "GMN juga disebut gradient moment nulling atau flow compensation."
        ),
        QuizQuestion(
            id = "M08B_Q6",
            moduleId = "M08B",
            nodeCode = "M08B.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Tingkat 2) Pada spin-echo, TE dinaikkan. Apa yang terjadi pada sinyal darah yang mengalir?",
            options = listOf(
                "Naik, karena lebih banyak spin direfase",
                "Turun, karena lebih banyak spin keluar irisan sebelum 180°",
                "Tetap",
                "Berubah menjadi ESP"
            ),
            correctIndex = 1,
            explanation = "Dengan TE panjang, lebih banyak inti hanya menerima satu pulsa."
        ),
        QuizQuestion(
            id = "M08B_Q7",
            moduleId = "M08B",
            nodeCode = "M08B.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Tingkat 2) Pada irisan ke-4 (paling superior), manakah yang lebih terang dan mengapa?",
            options = listOf(
                "IVC, karena searah eksitasi",
                "Aorta, karena berlawanan arah eksitasi sehingga spinnya masih segar",
                "Keduanya sama",
                "Tidak bisa ditentukan"
            ),
            correctIndex = 1,
            explanation = "Aorta mengalir berlawanan dengan urutan eksitasi sehingga tidak jenuh saat memasuki irisan 4."
        ),
        QuizQuestion(
            id = "M08B_Q8",
            moduleId = "M08B",
            nodeCode = "M08B.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Tingkat 2) Di mana pita presaturasi sebaiknya diletakkan?",
            options = listOf(
                "Di tengah irisan",
                "Di luar bore",
                "Antara asal aliran dan volume citra",
                "Hanya di sisi hilir"
            ),
            correctIndex = 2,
            explanation = "Pita ditempatkan di antara asal aliran dan volume citra agar spin yang masuk sudah jenuh."
        ),
        QuizQuestion(
            id = "M08B_Q9",
            moduleId = "M08B",
            nodeCode = "M08B.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Tingkat 2) Apa konsekuensi utama gradient moment rephasing?",
            options = listOf(
                "TE minimum naik",
                "SAR turun",
                "SNR naik pasti",
                "Resolusi turun"
            ),
            correctIndex = 0,
            explanation = "Gradien ekstra memerlukan waktu, sehingga TE minimum naik dan irisan yang tersedia untuk TR tertentu dapat berkurang."
        ),
        QuizQuestion(
            id = "M08B_Q10",
            moduleId = "M08B",
            nodeCode = "M08B.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Tingkat 2) GMN paling efektif pada aliran yang...",
            options = listOf(
                "Turbulen dan cepat",
                "Berdenyut kuat",
                "Lambat dan laminar",
                "Tegak lurus irisan"
            ),
            correctIndex = 2,
            explanation = "GMN mengandaikan kecepatan dan arah konstan, jadi paling efektif pada aliran lambat laminar."
        ),
        QuizQuestion(
            id = "M08B_Q11",
            moduleId = "M08B",
            nodeCode = "M08B.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Tingkat 2) Mengapa GMN cenderung lebih efektif pada aliran vena daripada arteri?",
            options = listOf(
                "Vena lebih besar",
                "Aliran arteri berdenyut sehingga kecepatannya tidak konstan",
                "Vena tidak mengandung proton",
                "Arteri selalu turbulen"
            ),
            correctIndex = 1,
            explanation = "Aliran berdenyut tidak benar-benar konstan, sedangkan GMN mengandaikan kecepatan konstan."
        ),
        QuizQuestion(
            id = "M08B_Q12",
            moduleId = "M08B",
            nodeCode = "M08B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Irisan 5 mm dan TR 500 ms. Kecepatan agar seluruh spin jenuh terganti dalam satu TR adalah...",
            options = listOf(
                "0,1 cm/s",
                "1 cm/s",
                "10 cm/s",
                "100 cm/s"
            ),
            correctIndex = 1,
            explanation = "v = 5 mm / 500 ms = 0,01 mm/ms = 1 cm/s (1 mm/ms = 100 cm/s)."
        ),
        QuizQuestion(
            id = "M08B_Q13",
            moduleId = "M08B",
            nodeCode = "M08B.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Cara mengurangi dephasing intravoksel pada aliran laminar berkecepatan konstan adalah...",
            options = listOf(
                "Memperbesar voksel",
                "Memperpanjang TE",
                "Gradient moment rephasing dan TE pendek",
                "Menaikkan Re di atas 2100"
            ),
            correctIndex = 2,
            explanation = "Kompensasi first-order serta TE pendek dan voksel kecil mengurangi dephasing."
        ),
        QuizQuestion(
            id = "M08B_Q14",
            moduleId = "M08B",
            nodeCode = "M08B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Pada lobe +1, -2, +1 dengan durasi sama, berapa momen pertama (M1)?",
            options = listOf(
                "Positif",
                "Negatif",
                "Tidak dapat dihitung",
                "Nol"
            ),
            correctIndex = 3,
            explanation = "Hitungan M1 = 0,5 - 3 + 2,5 = 0, sehingga fase akibat kecepatan konstan terkompensasi."
        ),
        QuizQuestion(
            id = "M08B_Q15",
            moduleId = "M08B",
            nodeCode = "M08B.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Mengapa GMN orde pertama tidak sepenuhnya menghapus fase akibat percepatan?",
            options = listOf(
                "Momen kedua (M2) tidak nol",
                "Percepatan tidak menghasilkan fase",
                "GMN hanya bekerja pada spin diam",
                "Gradien terlalu lemah"
            ),
            correctIndex = 0,
            explanation = "Fase akibat percepatan sebanding dengan M2, yang pada lobe +1, -2, +1 tidak nol."
        ),

        // =========================================================================
        // MODUL 8C — ARTEFAK PERANGKAT KERAS, CROSS-TALK & QA (20 Soal: 11 T2, 4 T3, 5 Capstone)
        // =========================================================================
        QuizQuestion(
            id = "M08C_Q1",
            moduleId = "M08C",
            nodeCode = "M08C.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Modul 1 dan 3) Mengapa logam di dekat jaringan menimbulkan area gelap dan distorsi, terutama pada gradient echo?",
            options = listOf(
                "Logam menambah sinyal proton",
                "Logam mengubah B0 setempat sehingga frekuensi Larmor menyimpang",
                "Logam menurunkan suhu kriostat",
                "Logam mematikan pulsa RF seluruhnya"
            ),
            correctIndex = 1,
            explanation = "Perubahan B0 setempat membuat fase spin cepat berantakan dan lokalisasi salah. Spin echo mengoreksinya sebagian."
        ),
        QuizQuestion(
            id = "M08C_Q2",
            moduleId = "M08C",
            nodeCode = "M08C.T2.1",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Cross-excitation) Mengapa SNR irisan bertetangga bisa turun pada tumpukan irisan?",
            options = listOf(
                "Profil pulsa RF nyata tidak persegi, sehingga tetangga ikut terpapar dan jenuh",
                "Gradient coil berhenti bekerja di tepi irisan",
                "B0 menghilang di antara irisan",
                "Penerima menyerap sinyal irisan lain"
            ),
            correctIndex = 0,
            explanation = "Energi dari pulsa tetangga mendorong proton ke bidang transversal sebelum gilirannya, sehingga jenuh dan sinyalnya turun."
        ),
        QuizQuestion(
            id = "M08C_Q3",
            moduleId = "M08C",
            nodeCode = "M08C.T2.2",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Zipper) Garis terang putus-putus melintang citra. Tindakan yang sesuai secara konsep adalah ...",
            options = listOf(
                "Menambah TR sebanyak mungkin",
                "Menutup pintu ruang magnet saat akuisisi dan meminta teknisi memeriksa pelindung RF",
                "Menurunkan B0",
                "Memperbesar celah antar irisan"
            ),
            correctIndex = 1,
            explanation = "Zipper berasal dari RF luar pada frekuensi echo, biasanya karena pintu terbuka atau sangkar RF bocor."
        ),
        QuizQuestion(
            id = "M08C_Q4",
            moduleId = "M08C",
            nodeCode = "M08C.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Shading) Apa ciri shading pada citra?",
            options = listOf(
                "Garis tipis melintang citra",
                "Intensitas sinyal berbeda di seluruh volume untuk jaringan yang sama",
                "Bayangan objek yang bergeser",
                "Lipatan di tepi citra"
            ),
            correctIndex = 1,
            explanation = "Shading adalah ketidakseragaman intensitas sinyal di seluruh volume citra."
        ),
        QuizQuestion(
            id = "M08C_Q5",
            moduleId = "M08C",
            nodeCode = "M08C.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Moiré) Satu lonjakan derau di satu titik ruang-k, setelah transformasi Fourier, tampil sebagai ...",
            options = listOf(
                "Satu titik terang pada citra",
                "Garis-garis sejajar yang menutupi citra",
                "Bayangan objek yang bergeser",
                "Daerah gelap tanpa sinyal"
            ),
            correctIndex = 1,
            explanation = "Satu titik di ruang-k setara dengan gelombang sinus di seluruh citra. Letak titik menentukan jarak dan sudut garis."
        ),
        QuizQuestion(
            id = "M08C_Q6",
            moduleId = "M08C",
            nodeCode = "M08C.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Moiré) Garis moiré dari lonjakan derau yang berasal dari lompatan listrik statis dapat dicegah dengan ...",
            options = listOf(
                "Menjaga kelembapan ruangan dalam batas normal",
                "Memperbesar FOV",
                "Menaikkan B0",
                "Menambah celah irisan"
            ),
            correctIndex = 0,
            explanation = "Lonjakan biasanya berasal dari pelepasan muatan statis. Kelembapan yang normal mengurangi risikonya. Steker kabel coil juga harus terpasang kuat."
        ),
        QuizQuestion(
            id = "M08C_Q7",
            moduleId = "M08C",
            nodeCode = "M08C.T2.4",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Moiré) Pita zebra di tepi FOV pada gradient echo disebabkan oleh ...",
            options = listOf(
                "Aliasing sinyal dari luar FOV dan ketidakhomogenan medan",
                "Zipper dari pintu ruang magnet",
                "Gerak napas pasien",
                "Celah irisan terlalu kecil"
            ),
            correctIndex = 0,
            explanation = "Sinyal terlipat searah atau berlawanan fase dengan sinyal di voksel itu, sehingga saling menguatkan atau meniadakan."
        ),
        QuizQuestion(
            id = "M08C_Q8",
            moduleId = "M08C",
            nodeCode = "M08C.T2.3",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Shading) Manakah yang termasuk penyebab shading?",
            options = listOf(
                "Pasien menyentuh coil di satu titik sehingga beban coil tidak normal",
                "Pintu ruang magnet terbuka",
                "Matriks fase terlalu besar",
                "Celah irisan terlalu lebar"
            ),
            correctIndex = 0,
            explanation = "Beban coil yang tidak normal, eksitasi tidak merata, dan B0 tidak homogen menyebabkan shading. Pintu terbuka menyebabkan zipper."
        ),
        QuizQuestion(
            id = "M08C_Q9",
            moduleId = "M08C",
            nodeCode = "M08C.T2.5",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Kegagalan peralatan) Citra menunjukkan pita sinyal hilang yang berbatas tegas. Langkah yang masuk akal secara konsep adalah ...",
            options = listOf(
                "Mengulang pemindaian, dan memanggil teknisi jika menetap",
                "Menaikkan sudut flip",
                "Memperbesar FOV",
                "Menambah TE"
            ),
            correctIndex = 0,
            explanation = "Galat akuisisi data kadang hilang saat pemindaian diulang. Jika menetap, perangkat keras pengolah citra mungkin perlu diganti oleh teknisi."
        ),
        QuizQuestion(
            id = "M08C_Q10",
            moduleId = "M08C",
            nodeCode = "M08C.T3.3",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Perbaikan cross-excitation yang menggandakan waktu pemindaian adalah...",
            options = listOf(
                "Squaring-off RF",
                "Interleaving",
                "Ubah TE sedikit",
                "Periksa shim"
            ),
            correctIndex = 1,
            explanation = "Interleaving menggandakan waktu pemindaian, sedangkan squaring-off RF menurunkan SNR."
        ),
        QuizQuestion(
            id = "M08C_Q11",
            moduleId = "M08C",
            nodeCode = "M08C.T3.2",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Apa fungsi baseline pada QA fantom?",
            options = listOf(
                "Menentukan diagnosis pasien",
                "Menjadi pembanding saat pesawat dinyatakan baik, sehingga penyimpangan dan tren terlihat",
                "Menggantikan semua pemeriksaan servis",
                "Menetapkan ambang yang sama untuk semua produsen"
            ),
            correctIndex = 1,
            explanation = "Baseline adalah hasil ukur saat pesawat baik. Nilai berikutnya dibandingkan dengannya."
        ),
        QuizQuestion(
            id = "M08C_Q12",
            moduleId = "M08C",
            nodeCode = "M08C.T3.1",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Mengapa pada akuisisi interleaved celah antar irisan tidak diperlukan?",
            options = listOf(
                "Irisan bertetangga diambil pada akuisisi berbeda, sehingga sempat meluruh",
                "Irisan tidak lagi punya tebal",
                "Pulsa RF menjadi persegi sempurna",
                "TR menjadi nol"
            ),
            correctIndex = 0,
            explanation = "Irisan ganjil dan genap dibagi ke dua akuisisi. Jeda satu akuisisi penuh cukup bagi tetangga untuk meluruh."
        ),
        QuizQuestion(
            id = "M08C_Q13",
            moduleId = "M08C",
            nodeCode = "M08C.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Suseptibilitas) Mengapa gradient echo lebih rentan terhadap kehilangan sinyal di dekat logam daripada spin echo?",
            options = listOf(
                "Tidak ada pulsa 180° yang merefase dephasing statis akibat B0 tidak seragam",
                "Spin echo memakai B0 lebih tinggi",
                "Gradient echo tidak memakai gradien",
                "Logam hanya memengaruhi pulsa 180°"
            ),
            correctIndex = 0,
            explanation = "Pulsa 180° pada spin echo merefase sebagian besar dephasing statis. Gradient echo tidak punya pulsa itu, sehingga kehilangan sinyal membesar bersama TE."
        ),
        QuizQuestion(
            id = "M08C_Q14",
            moduleId = "M08C",
            nodeCode = "M08C.T2.6",
            levelBadge = "Tingkat 2 • Menengah",
            level = 2,
            question = "(Suseptibilitas) Cara mengurangi geser geometri pada arah frekuensi di dekat logam adalah ...",
            options = listOf(
                "Menaikkan bandwidth penerimaan, dengan harga SNR turun",
                "Menyempitkan bandwidth penerimaan",
                "Memperpanjang TE",
                "Menaikkan B0"
            ),
            correctIndex = 0,
            explanation = "Geser = selisih frekuensi dibagi bandwidth per piksel. Bandwidth lebih lebar memperkecil geser, tetapi SNR turun."
        ),
        QuizQuestion(
            id = "M08C_Q15",
            moduleId = "M08C",
            nodeCode = "M08C.T2.6",
            levelBadge = "Tingkat 3 • Lanjut",
            level = 3,
            question = "(Lanjut) Mengapa distorsi di dekat sinus sangat besar pada EPI?",
            options = listOf(
                "Bandwidth per piksel pada arah fase sangat kecil",
                "EPI tidak memakai gradien",
                "EPI memakai TE nol",
                "B0 turun selama EPI"
            ),
            correctIndex = 0,
            explanation = "Banyak garis dibaca dalam satu eksitasi, sehingga selisih frekuensi kecil pun menjadi geser banyak piksel pada arah fase."
        ),
        QuizQuestion(
            id = "M08C_Q16",
            moduleId = "M08C",
            nodeCode = "M08C.T3.3",
            levelBadge = "Capstone • Lintas Artefak",
            level = 3,
            question = "(Capstone) Garis terang putus-putus melintang citra tetap muncul setelah pemindaian diulang. Langkah yang masuk akal secara konsep adalah ...",
            options = listOf(
                "Memeriksa pintu dan pelindung RF, lalu memanggil teknisi bila menetap",
                "Menambah NSA",
                "Menukar arah fase dan frekuensi",
                "Memperkecil FOV"
            ),
            correctIndex = 0,
            explanation = "Itu ciri zipper (RF luar masuk pada frekuensi echo). Perbaikannya di sisi perangkat keras."
        ),
        QuizQuestion(
            id = "M08C_Q17",
            moduleId = "M08C",
            nodeCode = "M08C.T3.3",
            levelBadge = "Capstone • Lintas Artefak",
            level = 3,
            question = "(Capstone) Hantu gerak napas ingin dipindahkan dari area yang diperiksa. Perbaikan dan harganya yang tepat adalah ...",
            options = listOf(
                "Tukar arah fase dan frekuensi; mungkin perlu antialiasing",
                "Naikkan bandwidth; SNR naik",
                "Perpanjang TE; waktu turun",
                "Interleaving; SNR menurun"
            ),
            correctIndex = 0,
            explanation = "Hantu hanya muncul pada arah fase. Menukar sumbu memindahkannya, tetapi arah frekuensi yang baru bisa memerlukan antialiasing."
        ),
        QuizQuestion(
            id = "M08C_Q18",
            moduleId = "M08C",
            nodeCode = "M08C.T3.3",
            levelBadge = "Capstone • Lintas Artefak",
            level = 3,
            question = "(Capstone) Implan logam membuat area gelap besar pada gradient echo. Perubahan yang tepat secara konsep adalah ...",
            options = listOf(
                "Pakai spin echo atau TSE dan perpendek TE",
                "Perpanjang TE",
                "Perbesar FOV",
                "Naikkan B0"
            ),
            correctIndex = 0,
            explanation = "Spin echo merefase dephasing statis dan TE pendek mengurangi akumulasi fase. B0 lebih tinggi justru memperbesar efek."
        ),
        QuizQuestion(
            id = "M08C_Q19",
            moduleId = "M08C",
            nodeCode = "M08C.T3.3",
            levelBadge = "Capstone • Lintas Artefak",
            level = 3,
            question = "(Capstone) Artefak mana yang tidak menggeser posisi sinyal, tetapi menghilangkan sinyal pada TE tertentu?",
            options = listOf(
                "Pembatalan sinyal luar-fase",
                "Pergeseran kimia",
                "Aliasing",
                "Ghosting fase"
            ),
            correctIndex = 0,
            explanation = "Lemak dan air dalam satu voksel berlawanan fase pada TE tertentu dan saling meniadakan, tanpa perubahan posisi."
        ),
        QuizQuestion(
            id = "M08C_Q20",
            moduleId = "M08C",
            nodeCode = "M08C.T3.3",
            levelBadge = "Capstone • Lintas Artefak",
            level = 3,
            question = "(Capstone) Manakah pasangan perbaikan dan harga yang benar?",
            options = listOf(
                "Presaturasi: mungkin kehilangan satu irisan dan RF bertambah",
                "Interleaving: SNR menurun",
                "Squaring-off RF: waktu pemindaian dua kali lipat",
                "Gating: TR tetap dan kontras tetap"
            ),
            correctIndex = 0,
            explanation = "Interleaving menggandakan waktu, squaring-off RF menurunkan SNR, dan gating membuat TR serta kontras bervariasi."
        )
    )

    // Konversi ke Room Entities untuk Pre-populate / Seeding
    fun encodeOptionsJson(options: List<String>): String {
        val jsonArray = JSONArray()
        options.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    fun decodeOptionsJson(optionsJson: String): List<String> {
        return runCatching {
            val arr = JSONArray(optionsJson)
            List(arr.length()) { idx -> arr.getString(idx) }
        }.getOrElse {
            optionsJson.split("||")
        }
    }

    fun toMriModuleEntities(): List<MriModuleEntity> {
        return modules.mapIndexed { idx, module ->
            MriModuleEntity(
                id = module.id,
                code = module.code,
                title = module.title,
                subtitle = module.subtitle,
                description = module.description,
                levelBadge = module.levelBadge,
                iconName = module.iconName,
                sortOrder = if (module.sortOrder != 0) module.sortOrder else idx + 1
            )
        }
    }

    fun toCurriculumNodeEntities(): List<CurriculumNodeEntity> {
        return modules.flatMap { module ->
            module.nodes.mapIndexed { idx, node ->
                CurriculumNodeEntity(
                    code = node.code,
                    moduleId = module.id,
                    level = node.level,
                    title = node.title,
                    summaryShort = node.summaryShort,
                    detailFull = node.detailFull,
                    analogi = node.analogi,
                    prerequisite = node.prerequisite,
                    learningGoal = node.learningGoal,
                    crossLinks = node.crossLinks,
                    labType = node.labType,
                    sortOrder = if (node.sortOrder != 0) node.sortOrder else idx + 1
                )
            }
        }
    }

    fun toCurriculumQuizEntities(): List<CurriculumQuizEntity> {
        return quizzes.map { q ->
            CurriculumQuizEntity(
                id = q.id,
                moduleId = q.moduleId,
                nodeCode = q.nodeCode,
                level = q.level,
                question = q.question,
                optionsJson = encodeOptionsJson(q.options),
                correctIndex = q.correctIndex,
                explanation = q.explanation
            )
        }
    }

    fun toMriNodeEntities(): List<MriNodeEntity> {
        return modules.flatMap { module ->
            module.nodes.mapIndexed { idx, node ->
                MriNodeEntity(
                    code = node.code,
                    moduleId = module.id,
                    level = node.level,
                    title = node.title,
                    summaryShort = node.summaryShort,
                    detailFull = node.detailFull,
                    analogi = node.analogi,
                    prerequisite = node.prerequisite,
                    learningGoal = node.learningGoal,
                    crossLinks = node.crossLinks,
                    labType = node.labType,
                    sortOrder = if (node.sortOrder != 0) node.sortOrder else idx + 1,
                    htmlSimulationAsset = node.htmlSimulationAsset
                )
            }
        }
    }

    fun toQuizQuestionEntities(): List<QuizQuestionEntity> {
        return quizzes.map { q ->
            QuizQuestionEntity(
                id = q.id,
                moduleId = q.moduleId,
                nodeCode = q.nodeCode,
                level = q.level,
                levelBadge = q.levelBadge,
                question = q.question,
                optionA = q.options.getOrElse(0) { "" },
                optionB = q.options.getOrElse(1) { "" },
                optionC = q.options.getOrElse(2) { "" },
                optionD = q.options.getOrElse(3) { "" },
                optionsJson = encodeOptionsJson(q.options),
                correctIndex = q.correctIndex,
                explanation = q.explanation
            )
        }
    }

    // Dek Flashcards Hafalan Cepat terintegrasi untuk Modul & Glosarium
    val flashcards: List<MriFlashcard> by lazy {
        val nodeCards = modules.flatMap { mod ->
            mod.nodes.map { node ->
                MriFlashcard(
                    id = "NODE_${node.code}",
                    moduleId = mod.id,
                    nodeCode = node.code,
                    category = "SIMPUL MATERI",
                    frontTitle = node.title,
                    frontSubtitle = "${mod.code} • Simpul ${node.code} (Tingkat ${node.level})",
                    backContent = node.summaryShort,
                    clinicalAnalogyOrTip = node.analogi
                )
            }
        }
        val glossaryCards = glossary.map { item ->
            MriFlashcard(
                id = "GLOSS_${item.term}",
                moduleId = item.moduleId,
                nodeCode = item.nodeCode,
                category = "ISTILAH GLOSARIUM",
                frontTitle = "${item.term} (${item.symbolOrAbbr})",
                frontSubtitle = "Modul ${item.moduleId} • Rujukan ${item.nodeCode}",
                backContent = item.definition,
                clinicalAnalogyOrTip = "Simbol / Singkatan Baku: ${item.symbolOrAbbr} pada modul ${item.moduleId}"
            )
        }
        nodeCards + glossaryCards
    }
}

fun CurriculumNodeEntity.toDomainModel(): MriNode = MriNode(
    code = code,
    level = level,
    title = title,
    summaryShort = summaryShort,
    detailFull = detailFull,
    analogi = analogi,
    prerequisite = prerequisite,
    learningGoal = learningGoal,
    crossLinks = crossLinks,
    labType = labType,
    sortOrder = sortOrder
)

fun CurriculumQuizEntity.toDomainModel(): QuizQuestion = QuizQuestion(
    id = id,
    moduleId = moduleId,
    nodeCode = nodeCode,
    levelBadge = MriRepository.quizzes.find { it.id == id }?.levelBadge?.takeIf { it.isNotBlank() } ?: when (level) {
        1 -> "Tingkat 1 • Dasar"
        2 -> "Tingkat 2 • Menengah"
        else -> "Tingkat 3 • Lanjut"
    },
    level = level,
    question = question,
    options = MriRepository.decodeOptionsJson(optionsJson),
    correctIndex = correctIndex,
    explanation = explanation
)

fun MriNodeEntity.toDomainModel(): MriNode = MriNode(
    code = code,
    level = level,
    title = title,
    summaryShort = summaryShort,
    detailFull = detailFull,
    analogi = analogi,
    prerequisite = prerequisite,
    learningGoal = learningGoal,
    crossLinks = crossLinks,
    labType = labType,
    sortOrder = sortOrder,
    htmlSimulationAsset = htmlSimulationAsset
)

fun QuizQuestionEntity.toDomainModel(): QuizQuestion {
    val parsedOptions = if (optionsJson.isNotBlank()) {
        MriRepository.decodeOptionsJson(optionsJson)
    } else {
        listOf(optionA, optionB, optionC, optionD).filter { it.isNotBlank() }
    }
    return QuizQuestion(
        id = id,
        moduleId = moduleId,
        nodeCode = nodeCode,
        levelBadge = levelBadge.ifBlank {
            when (level) {
                1 -> "Tingkat 1 • Dasar"
                2 -> "Tingkat 2 • Menengah"
                else -> "Tingkat 3 • Lanjut"
            }
        },
        level = if (level in 1..3) level else when {
            levelBadge.contains("1") || levelBadge.contains("Dasar", ignoreCase = true) -> 1
            levelBadge.contains("2") || levelBadge.contains("Menengah", ignoreCase = true) -> 2
            else -> 3
        },
        question = question,
        options = parsedOptions,
        correctIndex = correctIndex,
        explanation = explanation
    )
}
