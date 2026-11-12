# Kurikulum Aplikasi Pembelajaran MRI — Versi Terintegrasi dan Berbahasa Medis

> Berkas ini dibuat dari `kurikulum_mri_pohon_lengkap.md`. Isinya tiga hal:
> 1. **Analisis** singkat berkas sumber (Bagian 1).
> 2. **Kamus awam → medis**: istilah sehari-hari/analogi pada pohon diganti istilah radiologi/fisika medis baku (Bagian 2).
> 3. **Integrasi aktivitas**: bagian AKTIVITAS tidak lagi berdiri terpisah. Tiap aktivitas ditanam tepat di bawah simpul materi yang dilatihnya (penanda 🧪), sehingga belajar konsep dan praktik terjadi di satu tempat (Bagian 4).
>
> Alamat stabil dipertahankan. Aktivitas lama `M04B.A3` kini berada di bawah simpul `M04B.T2.6` dan tetap dapat dirujuk sebagai `M04B.T2.6·A3`.

## Daftar isi
1. Analisis berkas sumber
2. Kamus istilah awam → medis
3. Aturan integrasi aktivitas ke materi
4. Pohon terintegrasi per modul (M01–M09)
5. Tabel pemetaan aktivitas lama → simpul baru
6. Catatan dan keputusan yang masih terbuka

---

## 1. Analisis berkas sumber

**Gambaran.** 18 sub-modul (M01–M09), 410 baris pohon, 4 lapis: modul → tingkat (T1 dasar, T2 menengah, T3 lanjut) → simpul materi → aktivitas dan kuis. Prasyarat antarmodul ditandai ⚑, tautan →, simpul usulan [BARU] (39 baris).

**Kekuatan**
- Satu konsep satu rumah; duplikasi antar-modul sudah dihapus dan diganti tautan.
- Struktur seragam (header, materi, aktivitas, kuis, tautan silang) dan alamat simpul stabil.
- Alur logis: fisika → relaksasi/kontras → sekuens → pengodean spasial/ruang-k → perangkat keras → keselamatan → artefak → optimasi protokol.

**Kelemahan yang relevan dengan permintaan Anda**
1. **Aktivitas terpisah dari materi.** Semua aktivitas (±90 komponen simulasi) berada di blok sendiri, sehingga peserta membaca konsep dulu lalu "pindah ruangan" untuk berlatih. Akibatnya jarak antara konsep dan praktik jauh, dan aktivitas yang melatih dua simpul sekaligus tidak jelas menempel ke mana.
2. **Bahasa campuran awam dan teknis.** Banyak simpul memakai analogi atau kata harian sebagai judul: *jam tangan, laci, papan tuts, kenop kompor gas, tarian ruang-k, hantu, selimut sempit, sutradara/asisten, lemari, wrap, bas-relief, tebak artefak*. Analogi berguna untuk pengantar, tetapi tidak boleh menggantikan istilah baku di judul simpul, di kuis, atau di antarmuka konsol.
3. **Istilah Indonesia tidak konsisten** untuk hal yang sama: "pindai/pemindai", "waktu pindai", "irisan", "derau/bising", "gema/echo". Perlu satu istilah baku per konsep.
4. **Ketergantungan lintas modul belum konsisten** (lihat Bagian 6): penanda ⚑ M03B T3.2 menunjuk M04B di header tetapi M04C di baris simpul; M03A T2.2 dan A3 butuh M04B tanpa tercatat di prasyarat; M07A tidak masuk urutan belajar yang disarankan.
5. **M05 masih usulan** dan belum memiliki aktivitas maupun kuis.
6. **Aktivitas kembar** sebagian sudah digabung (SAR, kalkulator TA/SNR), tetapi komponen bersama (M09 Simulasi 1, M07C A1) dipakai dari modul lain; setelah integrasi perlu satu "rumah" yang jelas, sisanya hanya mode ringkas.

**Kesimpulan.** Isi kuat, struktur sudah baik. Dua perbaikan yang diminta (bahasa medis baku + aktivitas menempel pada materi) paling berdampak pada pemahaman dan konsistensi, dan tidak mengubah ID modul.

---

## 2. Kamus istilah awam → medis

Aturan pemakaian: **judul simpul, kuis, dan label antarmuka memakai istilah medis**; analogi awam boleh tetap hadir di *isi* penjelasan sebagai jembatan (ditandai "analogi"), tidak di judul.

### 2.1 Analogi/kiasan yang dipakai pada judul atau simpul

| Awam / kiasan (sumber) | Istilah medis/teknis baku | Lokasi |
|---|---|---|
| "Jam tangan" berbeda tiap baris | **Fase presesi** (*precession phase*) spin yang berbeda menurut posisi akibat gradien | M04A T1.2, T1.7, A1 |
| "Laci", "lemari laci" ruang-k | **Baris dan titik data ruang-k** (*k-space line / data point*), pengalamatan matriks | M04B T2.1, A1 |
| "Papan tuts" | **Pemetaan frekuensi–posisi** sepanjang gradien baca (*frequency encoding gradient mapping*) | M04A A3 |
| "Kenop kompor gas" | **Pemilihan parameter TR dan TE untuk menentukan pembobotan** (*parameter selection for image weighting*) | M02 T1.5, A3 |
| "Tarian ruang-k" | **Lintasan pengisian ruang-k oleh sekuens pulsa** (*k-space trajectory*) | M04C T3.5, A3 |
| "Selimut sempit" | **Kompromi (*trade-off*) antarparameter**: SNR, resolusi spasial, waktu akuisisi | M09 T2.3 |
| "Hantu" / ghosting gerak | **Artefak ghosting (replika semu) akibat *phase mismapping*** pada arah pengodean fase | M08A T1.3, T3.2, A3 |
| "Wrap" | **Aliasing (*wrap-around / fold-over*)** | M08A T1.2 |
| "Bas-relief" | **Artefak pergeseran kimia (*chemical shift artifact*)** | M08A T1.4 |
| "Luar-fase" | **Kontras *out-of-phase* (*opposed-phase*) lemak–air** | M08A T2.3, A1 |
| "Sudut ajaib" | **Efek *magic angle* (± 55°) pada jaringan berserat kolagen (tendon)** | M08A T2.4 |
| "Tebak artefak" | **Identifikasi artefak (*artifact recognition*)** | M08C A5 |
| "Sutradara" / "asisten" | **Komputer host (GUI, identitas pasien, PACS)** / **modul pembangkit pulsa (PGM)** | M09 T2.5 |
| "Pulsa persiapan" | **Pulsa pra-keadaan tunak (*dummy pulses / dummy scans*)** | M03B T2.1, A3 |
| "Gerak goyang" | **Presesi** spin di dalam B0 | M01 T1.6 |
| "Hilangnya fase" | **Dephasing** (peluruhan koherensi fase transversal) | M02 T2.2, A4 |
| "Pola sidik jari" | **Pola kumparan *fingerprint* (konduktor melingkar dengan perisai aktif)** pada koil gradien | M06A T2.3 |
| "Kandang Faraday" | **Perisai RF (*RF shielding / Faraday cage*)** | M06B T2.1 |
| "Dua lobus" | **Dua lobus gradien (*bipolar / balanced gradient lobes*)** | M06A T2.4 |
| "Celah" | **Celah antar-irisan (*inter-slice gap*)** | M04A T1.5 |
| "Perjalanan kecemasan pasien" | **Ansietas dan klaustrofobia pada pasien MRI** | M07A T2.4, A5 |
| "Benar atau keliru? Tips" | **Evaluasi miskonsepsi keselamatan (*safety myths*)** | M07A T2.5, A6 |
| "Luka bakar" | **Cedera termal (*RF burn*) akibat efek antena / loop konduktif** | M07C T2.4, A2 |
| "Kuat tarik" / "tarik" | **Gaya translasi (*translational force*) dan efek proyektil (*projectile / missile effect*)** | M07B T2.1 |
| "Ledakan" (kriogen) | **Pelepasan gas helium mendadak / peningkatan tekanan ruang (*quench*)** | M07B T3.1 |
| "Bising" (gradien) | **Kebisingan akustik (*acoustic noise*)**; "derau" dipakai khusus untuk *noise* sinyal pada SNR | M06A T2.4, M07C T2.7 |

### 2.2 Istilah harian → baku (konsistensi bahasa)

| Awam / sumber | Baku (dipakai di seluruh aplikasi) |
|---|---|
| pindai, memindai, pemindai | **akuisisi, akuisisi citra, pemindai MRI** ; "waktu pindai" → **waktu akuisisi (TA)** |
| irisan | **irisan (*slice*)**; "tebal irisan" → **ketebalan irisan (*slice thickness*)** |
| gema | **echo (gema)**; "gema simetris" → **echo simetris** |
| cairan | **cairan serebrospinal (CSF), edema, cairan bebas** menurut konteks; "air" pada kontras → **cairan bebas (air bebas)** |
| lemak | **jaringan adiposa / lemak** |
| terang / gelap pada citra | **hiperintens / hipointens (isointens bila sama)** |
| patologi menonjol | **lesi hiperintens pada T2w (edema, inflamasi, tumor)** |
| hilang sinyal | **signal void / penekanan sinyal (*signal suppression*)** menurut konteks |
| gerakan pasien | **artefak gerak (*motion artifact*)**; "pasien gelisah" → **pasien non-kooperatif / gerak pasien** |
| tahan napas | **penahanan napas (*breath-hold*)** |
| pemilih koil | **pemilihan koil menurut regio anatomi** |
| alat tanam | **perangkat implan (*implantable device*)** |
| penanda/benda logam | **benda feromagnetik / benda asing logam** |
| menenangkan pasien | **manajemen ansietas pasien (komunikasi terapeutik)** |
| lambat / cepat | **laju (*rate*), mis. slew rate; sekuens cepat (*fast sequence*)** |
| semakin besar medan | **peningkatan kekuatan medan magnet utama (B0)** |
| titik/tanda pada citra | **voksel / piksel** menurut konteks (3D / 2D) |

### 2.3 Istilah yang dipertahankan (sudah baku)
TR, TE, TI, NEX, FoV, FFT, SNR, SAR, RBW, ETL, B0, B1, T1, T2, T2*, FID, NMV, ADC, TOF, ESP, GMN, DWI, BOLD, MTC, FLAIR, STIR, DRIVE, EPI, TSE/FSE/RARE, PACS, ACR, ASTM, IEC 60601-2-33.

---

## 3. Aturan integrasi aktivitas ke materi

1. **Satu aktivitas, satu simpul induk.** Aktivitas ditanam di simpul yang menjadi *tujuan belajarnya*. Bila melatih dua simpul, ia ditanam di simpul yang paling akhir/paling sintesis dan simpul lain memuat tautan `🔗 lihat ·A#`.
2. **Urutan dalam simpul:** *konsep* → 🧪 *praktik langsung* → *pertanyaan penguat (cek-sekilas)*. Praktik tidak boleh membuka materi yang belum diajarkan; bila perlu, naikkan ke simpul berikutnya.
3. **Format baris praktik:** `🧪 ID · Nama (istilah medis) — Anda lakukan: … ; amati: … ; simpulkan: …`
4. **Komponen bersama** (kalkulator TA/SNR M09 Simulasi 1, kalkulator SAR M07C A1) tetap satu komponen dengan beberapa mode; di modul lain hanya tampil sebagai mode ringkas di simpul terkait.
5. **Aktivitas sintesis** (capstone, "satu TR tiga gradien", "laboratorium ruang-k") ditempatkan di simpul penutup tingkat dan diberi label **🏁**.
6. **Kuis** tetap per tingkat, diletakkan setelah simpul terakhir tingkat itu (📝), bukan di akhir modul.
7. **Sub-simpul** kecil dari file sumber digabung ke dalam deskripsi bila tidak punya aktivitas sendiri; daftar lengkap tetap ada di berkas sumber.

Legenda: ⚑ prasyarat modul lain | → tautan | [BARU] usulan | 🧪 praktik tertanam | 🏁 sintesis | 📝 kuis

---

## 4. Pohon terintegrasi per modul

### M01 — Fisika Dasar MRI
*Prasyarat: — | Dilanjutkan: M02*

**Tingkat 1 (Dasar)**
- **T1.1 Peran MRI dalam praktik klinis** — keunggulan kontras jaringan lunak; kontraindikasi relatif (→ M07A)
- **T1.2 Dua pendekatan penjelasan MRI (model klasik vs mekanika kuantum)**
- **T1.3 Struktur atom, nomor atom, dan nomor massa** — isotop, awan elektron
- **T1.4 Inti aktif MR dan syarat spin neto** — contoh 1H, 13C, 19F, 23Na
- **T1.5 Populasi spin dan *net magnetization vector* (NMV)** — keadaan paralel (energi rendah) vs antiparalel (energi tinggi); B0 naik → NMV naik
  - 🧪 **M01.T1.5·A1 Populasi spin dan B0 [BARU]** — Anda lakukan: geser kekuatan B0; amati: selisih populasi paralel–antiparalel dan panjang NMV; simpulkan: mengapa 3 T memberi sinyal lebih besar daripada 1,5 T
- **T1.6 Presesi dan frekuensi Larmor** — f = γ · B0
  - 🧪 **M01.T1.6·A2 Kalkulator Larmor [BARU]** — Anda lakukan: pilih inti (1H, 13C, 19F, 23Na) dan B0; amati: frekuensi presesi; simpulkan: hanya frekuensi yang cocok yang menimbulkan resonansi → 🔗 T3.2
- **T1.7 Spektrum elektromagnetik dan sifat non-pengion RF**
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Fase, koherensi, dan resonansi** — koheren vs inkoheren; syarat resonansi (frekuensi RF = Larmor)
- **T2.2 Efek eksitasi: nutasi dan sudut flip** — θ = γ · B1 · τ (pulsa 90°, 180°)
- **T2.3 Sinyal MR dan *free induction decay* (FID)** — hukum induksi Faraday; peluruhan sinyal
  - 🧪 **M01.T2.3·A3 Eksitasi dan FID [BARU]** — Anda lakukan: atur B1 · τ (flip 90°/180°); amati: nutasi NMV ke bidang transversal dan bentuk FID; simpulkan: sinyal maksimum pada flip 90°, nol pada 180° (mencakup T2.2)
- **T2.4 Definisi TR dan TE** — TR mengontrol relaksasi longitudinal; TE mengontrol peluruhan transversal (efek kontras → M02)
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Rasio Boltzmann (kuantitatif)** — pengaruh B0 pada selisih populasi spin; 🔗 T1.5·A1
- **T3.2 Frekuensi Larmor berbagai inti aktif MR** — 🔗 T1.6·A2
- **T3.3 Penjelasan eksitasi: klasik vs kuantum**
- **T3.4 Pengecualian syarat "nomor massa ganjil" (mis. 2H, 14N)** [BARU] — inti bermassa genap dengan proton dan neutron sama-sama ganjil tetap bersifat aktif MR
- 📝 Kuis Tingkat 3

*Tautan:* efek TR/TE → M02; batas SAR → M07C; perangkat keras RF → M06B.

---

### M02 — Relaksasi T1/T2 dan Dasar Kontras
*Prasyarat: M01 | Dilanjutkan: M03A, M03B, M03C, M04A*

**Tingkat 1 (Dasar)**
- **T1.1 Parameter kontras intrinsik dan ekstrinsik** — intrinsik: T1, T2, PD, aliran, ADC; ekstrinsik: TR, TE, sudut flip, TI, *turbo factor*, nilai b (→ M03A, M03C)
- **T1.2 Dua proses relaksasi simultan** — T1 (spin–kisi, pemulihan longitudinal) dan T2 (spin–spin, peluruhan transversal)
- **T1.3 Kontras jaringan: adiposa vs cairan bebas** — hiperintens/hipointens; PD; struktur molekul (lipid rapat vs H2O longgar); T1 dan T2 lemak pendek, T1 dan T2 cairan panjang
- **T1.4 Pembobotan citra: T1w, T2w, PDw** — T1w (TR pendek, TE pendek); T2w (TR panjang, TE panjang); PDw (TR panjang, TE pendek)
  - 🧪 **M02.T1.4·A2 Lemak vs cairan pada tiga pembobotan [BARU]** — Anda lakukan: geser TR dan TE; amati: intensitas sinyal lemak dan cairan; simpulkan: pembobotan apa yang dihasilkan tiap kombinasi (mencakup T1.3)
- **T1.5 Panduan praktis membaca pembobotan** — cairan hiperintens = T2w; cairan hipointens = T1w (analogi pengaturan nyala kompor boleh dipakai di isi, bukan di judul)
  - 🧪 **M02.T1.5·A3 Menentukan pembobotan dari TR, TE, dan sinyal cairan [BARU]** — Anda lakukan: diberi TR/TE dan sinyal cairan; amati: umpan balik; simpulkan: klasifikasi T1w/T2w/PDw tanpa menghafal
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Pemulihan T1 dan peluruhan T2 secara matematis** — Mz(t) = M0 · (1 − e^(−t/T1)); Mxy(t) = M0 · e^(−t/T2)
  - 🧪 **M02.T2.1·A1 Kurva relaksasi T1 dan T2 [BARU]** — Anda lakukan: ubah T1/T2 jaringan; amati: Mz(t) dan Mxy(t); simpulkan: jaringan dengan T1 pendek pulih lebih cepat
- **T2.2 Dephasing, T2*, dan spin echo** — penyebab: interaksi spin–spin dan ketidakhomogenan B0; T2* vs T2 sejati; refokusing oleh pulsa 180°
  - 🧪 **M02.T2.2·A4 Dephasing dan refokusing spin echo [BARU]** — Anda lakukan: jalankan animasi dephasing lalu beri pulsa 180°; amati: fase spin kembali sefase membentuk echo; simpulkan: spin echo menghapus efek B0 tetapi tidak efek T2 sejati
- **T2.3 Pembobotan T1 rinci (TR pendek, TE pendek)** — saturasi sebagian dan penuh; pascakontras gadolinium (mekanisme → M03C)
- **T2.4 Pembobotan T2 rinci (TR panjang, TE panjang)** — penekanan pengaruh T1; penonjolan jaringan berair (lesi)
- **T2.5 Pembobotan PD rinci (TR panjang, TE pendek)** — persamaan: SI = PD · e^(−TE/T2) · (1 − e^(−TR/T1))
  - 🏁🧪 **M02.T2.5·A5 Kalkulator persamaan sinyal SI [BARU]** — Anda lakukan: isi PD, T1, T2, TR, TE; amati: SI dan peringkat kontras; simpulkan: parameter mana yang mendominasi T1w/T2w/PDw (mencakup T2.3 dan T2.4)
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Pengaruh kekuatan B0** — perubahan 1,5 T → 3 T pada T1 (memanjang) dan T2
- **T3.2 Pengaruh gerak molekul pada relaksasi**
- 📝 Kuis Tingkat 3

*Tautan:* TR/TE → M01 T2.4; spin echo sebagai sekuens → M03A; keadaan tunak → M03B T2.1; gadolinium → M03C; TA/SNR → M09.

---

### M03A — Sekuens Spin Echo, Inversion Recovery, dan Turbo Spin Echo
*Prasyarat: M02 (+ ⚑ M04B untuk T2.2) | Dilanjutkan: M03B, M08B*

**Tingkat 1 (Dasar)**
- **T1.1 Spin echo (SE) konvensional** — eksitasi 90° dan refokusing 180°
  - 🧪 **M03A.T1.1·A1 Spin echo langkah demi langkah [BARU]** — Anda lakukan: jalankan 90° → 180° → echo; amati: fase spin pada tiap tahap; simpulkan: echo terbentuk pada TE = 2 × jarak antara 90° dan 180° (juga melatih T2.1)
- **T1.2 *Inversion recovery* (IR)** — pulsa inversi 180° dan *time from inversion* (TI)
- **T1.3 Penekanan sinyal dengan IR** — STIR (supresi lemak), FLAIR (supresi CSF)
  - 🧪 **M03A.T1.3·A2 Inversion recovery: STIR vs FLAIR [BARU]** — Anda lakukan: geser TI; amati: jaringan mana yang mencapai titik nol (*null point*); simpulkan: TI ≈ 0,69 × T1 jaringan yang ingin ditekan
- **T1.4 *Turbo spin echo* (TSE/FSE/RARE)** — *echo train* dan *turbo factor* (ETL)
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Echo simetris dan persamaan sinyal spin echo** — 🔗 T1.1·A1
- **T2.2 TSE: penataan ulang fase (*phase reordering*) dan TE efektif** ⚑ M04B (ruang-k)
  - 🧪 **M03A.T2.2·A3 Turbo factor [BARU] ⚑ M04B** — Anda lakukan: ubah ETL; amati: TE efektif dan waktu akuisisi; simpulkan: ETL naik → TA turun tetapi kekaburan (*blurring*) bertambah
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 *Driven equilibrium* (DRIVE)**
- 📝 Kuis Tingkat 3

*Tautan:* pembobotan → M02 T1.4; rephasing T2* → M02 T2.2; TA pada TSE → M09 T3.1; artefak aliran pada SE → M08B.

---

### M03B — Sekuens Gradient Echo dan Keadaan Tunak
*Prasyarat: M02, M03A ; ⚑ M04A untuk T1.2 ; ⚑ M04C (jalur ruang-k EPI) untuk T3.2*

**Tingkat 1 (Dasar)**
- **T1.1 Dasar *gradient echo* (GRE)** — sudut flip kecil tanpa pulsa 180°
- **T1.2 Gradien bipolar (*spoiler* dan *rewinder*)** ⚑ M04A
- **T1.3 Sudut Ernst**
  - 🧪 **M03B.T1.3·A1 Sudut flip dan sudut Ernst [BARU]** — Anda lakukan: ubah TR dan T1; amati: sudut flip yang menghasilkan sinyal maksimum; simpulkan: TR pendek → sudut optimal kecil (juga melatih T1.1)
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Keadaan tunak (*steady state*)** — TR pendek; sinyal tidak pulih penuh; pulsa pra-keadaan tunak (*dummy pulses*)
  - 🧪 **M03B.T2.1·A3 Dummy pulses [BARU]** — Anda lakukan: tambah jumlah dummy pulses; amati: sinyal mencapai plateau; simpulkan: data hanya direkam setelah keadaan tunak tercapai
- **T2.2 Tiga sekuens keadaan tunak** — koheren/*rewound* (FID + gema terstimulasi); inkoheren/*spoiled* (FID saja); *reverse-echo* (gema terstimulasi saja, TE efektif panjang)
  - 🧪 **M03B.T2.2·A2 Perbandingan tiga sekuens keadaan tunak [BARU]** — Anda lakukan: ganti mode koheren/inkoheren/*reverse-echo*; amati: kontras dan komponen sinyal; simpulkan: sekuens mana untuk kontras T1, T2*, atau T2
- **T2.3 T2* pada gradient echo** (→ M02 T2.2)
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 *Balanced gradient echo* (True FISP/FIESTA)**
- **T3.2 GRE cepat dan *echo planar imaging* (EPI)** ⚑ M04C
- 📝 Kuis Tingkat 3

*Tautan:* saturasi → M02 T2.3; T2* → M02 T2.2; TA GRE/3D → M09 T3.1.

---

### M03C — Mekanisme Kontras Khusus dan Agen Kontras
*Prasyarat: M02 ; ⚑ M04A untuk T2.3 | Dilanjutkan: M08A*

**Tingkat 1 (Dasar)**
- **T1.1 *Diffusion-weighted imaging* (DWI) dan ADC**
  - 🧪 **M03C.T1.1·A1 DWI [BARU]** — Anda lakukan: ubah nilai b; amati: peluruhan sinyal dan peta ADC; simpulkan: difusi terbatas (mis. infark akut) tampak hiperintens pada DWI dan hipointens pada ADC (juga melatih T2.3)
- **T1.2 fMRI (BOLD)**
- **T1.3 *Magnetization transfer contrast* (MTC)**
  - 🧪 **M03C.T1.3·A3 Ilustrasi BOLD dan MTC (konsep) [BARU]** — Anda lakukan: jalankan ilustrasi; amati: perubahan sinyal akibat deoksihemoglobin dan transfer magnetisasi; simpulkan: sumber kontras tiap teknik (mencakup T1.2)
- **T1.4 Zat kontras** — agen T1: gadolinium; agen T2: besi superparamagnetik
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Mekanisme agen kontras paramagnetik**
- **T2.2 Gadolinium secara konsep** — pemendekan T1; hiperintens pada T1w pascakontras
  - 🧪 **M03C.T2.2·A2 Agen kontras [BARU]** — Anda lakukan: ubah konsentrasi gadolinium; amati: pemendekan T1 pada citra T1w; simpulkan: peningkatan sinyal tidak linear pada konsentrasi tinggi (mencakup T2.1)
- **T2.3 Skema Stejskal–Tanner** (gradien bipolar + pulsa 180°; nilai b) ⚑ M04A — 🔗 T1.1·A1
- 📝 Kuis Tingkat 2

*Catatan: keselamatan agen kontras (retensi gadolinium, NSF) belum ada di modul mana pun → usulan di M07A.*

---

### M04A — Pengodean Spasial: dari Sinyal menjadi Citra
*Prasyarat: M01, M02 | Dilanjutkan: M04B, M06A, M06B, M08B*

**Tingkat 1 (Dasar)**
- **T1.1 Dari sinyal menjadi citra**
- **T1.2 Mekanisme gradien** — koil gradien, polaritas, isosenter; fase presesi berbeda menurut posisi (analogi: jam tangan yang berjalan berbeda kecepatan)
  - 🧪 **M04A.T1.2·A1 Gradien, frekuensi, dan fase presesi** — Anda lakukan: aktifkan gradien; amati: frekuensi dan fase spin menurut posisi; simpulkan: gradien memberi tiap lokasi "alamat" frekuensi/fase (juga melatih T1.7)
- **T1.3 Sumbu gradien dan lobus pada diagram urutan pulsa**
- **T1.4 Pemilihan irisan** — cara kerja, bidang, waktu
- **T1.5 Ketebalan irisan, *transmit bandwidth*, dan celah antar-irisan**
  - 🧪 **M04A.T1.5·A2 Pemilihan irisan** — Anda lakukan: ubah bidang, frekuensi RF, dan ketebalan; amati: lokasi dan tebal irisan, profil sinc (toggle); simpulkan: ketebalan ∝ bandwidth ÷ kekuatan gradien (mencakup T1.4)
- **T1.6 Pengodean frekuensi: gradien baca dan FoV**
  - 🧪 **M04A.T1.6·A3 Pengodean frekuensi dan FoV** — Anda lakukan: ubah gradien baca dan FoV; amati: pemetaan frekuensi–posisi (analogi: papan tuts); simpulkan: FoV ditentukan oleh kekuatan gradien dan bandwidth
- **T1.7 Pengodean fase** — gradien fase memberi fase berbeda pada tiap baris [BARU]
  - 🏁🧪 **M04A.T1.7·A4 Satu TR, tiga gradien** — Anda lakukan: telusuri satu TR; amati: peran gradien pilih-irisan, fase, dan frekuensi berurutan; simpulkan: satu TR mengisi satu baris ruang-k (→ M04B)
- 📝 Kuis Tingkat 1

*Tautan:* perangkat keras → M06A; pulsa sinc → M06B T2.2; gradien bipolar → M03B T1.2.

---

### M04B — Gradien, Ruang-k, dan Fourier
*Prasyarat: M04A | Dilanjutkan: M04C, M08A, M09*

**Tingkat 2 (Menengah)**
- **T2.1 Apa itu ruang-k** — sumbu, baris/titik data, penomoran (analogi: lemari laci)
  - 🧪 **M04B.T2.1·A1 Pengalamatan ruang-k** — Anda lakukan: isi dan buka alamat baris/titik; amati: posisi tiap titik pada matriks; simpulkan: ruang-k adalah matriks data mentah (bukan citra)
- **T2.2 Gradien fase mengisi ruang-k**
- **T2.3 Mengapa gradien fase berubah tiap TR**
  - 🧪 **M04B.T2.3·A7.3 Simulator akuisisi: isi ruang-k baris demi baris** — Anda lakukan: jalankan akuisisi per TR; amati: satu baris terisi per TR; simpulkan: TA ∝ jumlah baris pengodean fase (mencakup T2.2)
- **T2.4 Dari echo ke titik data**
- **T2.5 *Sampling* dan teorema Nyquist**
  - 🧪 **M04B.T2.5·A2 Sampling dan Nyquist** — Anda lakukan: turunkan frekuensi sampling; amati: sinyal terlipat; simpulkan: sampling ≥ 2 × frekuensi tertinggi
- **T2.6 *Receive bandwidth* (RBW) dan frekuensi Nyquist**
- **T2.7 Jendela sampling, TE, dan matriks frekuensi**
  - 🧪 **M04B.T2.7·A3 RBW, jendela sampling, dan TE** — Anda lakukan: ubah RBW dan matriks frekuensi; amati: lama jendela sampling dan TE minimum; simpulkan: RBW sempit → SNR naik, TE naik (mencakup T2.6)
- **T2.8 TE minimum, tampilan RBW, dan jumlah irisan** (kalkulator → M09 Simulasi 1, mode ringkas)
- **T2.9 FFT: dari titik data ke citra**
  - 🧪 **M04B.T2.9·A4 FFT** — Anda lakukan: tambah komponen gelombang; amati: puncak spektrum muncul; simpulkan: frekuensi ↔ posisi
  - 🧪 **M04B.T2.9·A7.1 Ruang-k dengan *mask*** — Anda lakukan: tutup bagian ruang-k; amati: citra rekonstruksi berubah; simpulkan: pusat ruang-k membawa kontras dan sinyal, tepi membawa detail
- **T2.10 Enam fakta penting ruang-k** (Fakta 1–3: *isi perlu dituliskan*; lihat Bagian 6)
  - 🧪 **M04B.T2.10·A5 Sinyal dan resolusi: garis tengah vs tepi** — amati: garis tengah menentukan kontras/SNR, tepi menentukan resolusi
  - 🧪 **M04B.T2.10·A6 FoV dan ukuran piksel pada dua sumbu** — amati: jarak antar titik ↔ FoV; dimensi ruang-k ↔ ukuran piksel (Fakta 4–6)
  - 🏁🧪 **M04B.T2.10·A7.2 FoV, matriks, dan aliasing** — amati: FoV terlalu kecil menimbulkan aliasing
- 📝 Kuis Tingkat 2

*Tautan:* TA/SNR → M09 (kalkulator); aliasing sebagai artefak → M08A; partial Fourier dan parallel imaging → M04C.

---

### M04C — Ruang-k Lanjut dan Strategi Akuisisi
*Prasyarat: M04B | Dilanjutkan: M03B (EPI), M09*

**Tingkat 3 (Lanjut)**
- **T3.1 Pseudo-frekuensi**
  - 🧪 **M04C.T3.1·A1 Posisi fase pada pseudo-frekuensi** — amati: bagaimana posisi fase "terbaca" sebagai frekuensi semu
- **T3.2 Pemahaman ruang-k lebih dalam**
- **T3.3 Simetri titik (simetri konjugat)**
  - 🧪 **M04C.T3.3·A2 Simetri konjugat ruang-k** — amati: setengah ruang-k cukup untuk merekonstruksi setengahnya lagi (dasar *partial Fourier*)
- **T3.4 Pengaturan garis ruang-k**
- **T3.5 Lintasan pengisian ruang-k oleh urutan pulsa** (analogi: "tarian")
  - 🧪 **M04C.T3.5·A3 Lintasan ruang-k** — amati: gradien menelusuri titik demi titik
- **T3.6 Pilihan pengisian (1): *partial Fourier*, *partial echo*, *parallel imaging***
  - 🧪 **M04C.T3.6·A4 Partial Fourier dan parallel imaging** — amati: waktu turun, SNR turun, risiko aliasing
- **T3.7 Pilihan pengisian (2): *single-shot*, spiral, PROPELLER, jenis akuisisi**
  - 🧪 **M04C.T3.7·A5 Urutan akuisisi: sequential vs 2D volumetrik**
  - 🏁🧪 **M04C.T3.7·A6 Jalur ruang-k: EPI, spiral, PROPELLER** — simpulkan: tiap jalur menukar kecepatan dengan jenis artefak
- **T3.8 SNR dan kompromi** (→ M09 T2.1 dan T2.3)
- 📝 Kuis Tingkat 3

---

### M05 — Arsitektur Sistem dan Magnet B0 *(USULAN)*
*Prasyarat: M01 | Dilanjutkan: M06A, M06B, M07B*

**Tingkat 1** — T1.1 Arsitektur sistem MRI [BARU]; T1.2 Jenis magnet (permanen, resistif, superkonduktif) [BARU]; T1.3 Kekuatan medan 0,5–3 T dan kompromi pemakaian [BARU]
**Tingkat 2** — T2.1 Homogenitas B0 dan shimming (pasif/aktif) [BARU] (kandidat dari M06A T3.1); T2.2 Magnet superkonduktif: kumparan dan helium cair [BARU]; T2.3 Medan tepi dan perisai magnet [BARU]
**Tingkat 3** — T3.1 Ringkasan efek peningkatan B0 pada SNR, T1/T2, pergeseran kimia, SAR [BARU]

🧪 Aktivitas: **belum ada**. Saran untuk keputusan Anda: T1.3 (membandingkan kekuatan medan) dan T2.1 (shimming) adalah kandidat praktik pertama.

---

### M06A — Sistem Gradien
*Prasyarat: M04A | Dilanjutkan: M07C*

**Tingkat 1 (Dasar)**
- **T1.1 Mengapa gradien diperlukan** (→ M04A)
- **T1.2 Koil gradien vs koil RF**
- **T1.3 Tiga sumbu gradien** (X melintang, Y vertikal, Z memanjang)
  - 🧪 **M06A.T1.3·A1 Arah sumbu gradien** — amati: arah dan bidang irisan yang dihasilkan tiap sumbu
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Kinerja gradien** — amplitudo, waktu naik, *slew rate*, *duty cycle*
  - 🧪 **M06A.T2.1·A4 Trapesium gradien** — Anda lakukan: ubah amplitudo dan slew rate; amati: waktu naik; simpulkan: waktu naik = amplitudo ÷ slew rate
- **T2.2 Rantai sinyal dan amplifier gradien** (linear vs PWM)
- **T2.3 Konstruksi koil gradien** — pola *fingerprint*, perisai aktif
  - 🧪 **M06A.T2.3·A3 Koil gradien: Maxwell (Z) vs Golay (X/Y)** — amati: arah aliran arus dan pola medan
- **T2.4 Gradien seimbang dan kebisingan akustik** — dua lobus, gaya Lorentz
  - 🧪 **M06A.T2.4·A2 Gradien seimbang** — Anda lakukan: atur amplitudo dan durasi lobus; amati: luas total nol; simpulkan: kebisingan berasal dari getaran koil akibat gaya Lorentz
- **T2.5 Lima poin penting koil gradien** (arah, amplitudo, polaritas)
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Shimming** — aktif vs *dynamic offset* (kandidat ke M05 T2.1)
- 📝 Kuis Tingkat 3 (usulan 8 soal)

---

### M06B — Sistem RF dan Koil
*Prasyarat: M01, M04A | Dilanjutkan: M07C, M08A*

**Tingkat 1 (Dasar)**
- **T1.1 Sistem RF** — pita VHF, frekuensi Larmor
- **T1.2 Koil gradien vs koil RF** (→ M06A T1.2)
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Perisai RF (*Faraday cage*)**
  - 🧪 **M06B.T2.1·A5 Perisai RF dan artefak** — amati: kebocoran gelombang radio menghasilkan pita artefak
- **T2.2 Sistem pemancar RF dan pulsa sinc** (→ M04A.T1.5·A2)
- **T2.3 Koil pemancar** (*birdcage*, faktor pengisian)
- **T2.4 Sistem penerima RF** — koil → preamplifier → demodulasi → ADC
  - 🧪 **M06B.T2.4·A1 Urutan rantai sinyal** — susun komponen sesuai urutan
- **T2.5 Jenis koil penerima** — permukaan, volume, *phased array*
  - 🧪 **M06B.T2.5·A2 Pemilihan koil menurut anatomi**
  - 🧪 **M06B.T2.5·A3 Sensitivitas koil menurut kedalaman** — amati: koil permukaan unggul dangkal, volume lebih merata
  - 🧪 **M06B.T2.5·A4 *Phased array* tulang belakang** — amati: penggabungan kanal menaikkan SNR dan cakupan
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Pergeseran kimia lemak–air** (naik bersama B0; asal fisika)
  - 🧪 **M06B.T3.1·A6 Simulasi pergeseran kimia** — Anda lakukan: ubah B0; amati: jarak puncak air–lemak dalam spektrum (±3,5 ppm); simpulkan: pergeseran dalam Hz naik sebanding B0 (→ M08A T1.4)
- **T3.2 Ketidakhomogenan B1 dan *parallel transmit***
  - 🧪 **M06B.T3.2·A7 B1 tidak merata dan transmit paralel** — amati: pola *shading* dan koreksinya
- 📝 Kuis Tingkat 3 (usulan 7 soal)

---

### M07A — Zona, Skrining, dan Keselamatan Perangkat
*Prasyarat: M01 (disarankan sebagai "gerbang keselamatan" setelah M01) | Dilanjutkan: M07B, M07C*

**Tingkat 1 (Dasar)**
- **T1.1 Empat zona keselamatan**
  - 🧪 **M07A.T1.1·A1 Denah zona dan akses personel** (juga melatih T2.1 dan T2.2)
- **T1.2 Prinsip keselamatan dan skrining**
  - 🧪 **M07A.T1.2·A2 Simulasi skrining pasien**
- **T1.3 Lima sumber bahaya** (peta; rincian → M07B, M07C)
  - 🧪 **M07A.T1.3·A4 Lima sumber bahaya pada pemindai** — kenali tiap bahaya pada gambar pemindai
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Zona menurut ACR**; **T2.2 Tiga tingkat personel** — 🔗 T1.1·A1
- **T2.3 Keselamatan perangkat: standar ASTM dan label implan** (MR Safe / MR Conditional / MR Unsafe)
  - 🧪 **M07A.T2.3·A3 Label perangkat dan alur keputusan pasien berimplan**
- **T2.4 Efek psikologis: ansietas dan klaustrofobia**
  - 🧪 **M07A.T2.4·A5 Perjalanan ansietas pasien**
- **T2.5 Tips keselamatan**
  - 🧪 **M07A.T2.5·A6 Evaluasi miskonsepsi keselamatan (benar/keliru)**
- 📝 Kuis Tingkat 2

---

### M07B — Medan Statis (B0), Proyektil, dan Kriogen
*Prasyarat: M07A, M01 | Dilanjutkan: M07C*

**Tingkat 2 (Menengah)**
- **T2.1 Medan statis dan efek proyektil**
  - 🧪 **M07B.T2.1·A1 Medan tepi dan gaya translasi** — amati: gaya naik tajam mendekati bore
- **T2.2 Kelompok yang sering terlibat insiden proyektil**
- **T2.3 Medan tepi berperisai aktif**
- **T2.4 Efek sementara B0** (vertigo, rasa logam, fosfen)
  - 🧪 **M07B.T2.4·A3 Skala medan dan efek sementara**
- **T2.5 Torsi pada implan**
  - 🧪 **M07B.T2.5·A2 Torsi pada implan**
- **T2.6 Benda asing dan efek Lenz**
- **T2.7 Kriogen: empat kekhawatiran dan *quench***
  - 🧪 **M07B.T2.7·A4 Oksigen dan ekspansi helium** — amati: volume gas helium dan penurunan oksigen ruang (juga melatih T3.1)
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Pipa *quench*, tekanan pintu, dan risiko pelepasan gas**; **T3.2 *Quench* bukan prosedur darurat biasa**
- 📝 Kuis Tingkat 3

---

### M07C — RF (SAR), Gradien, dan Kebisingan
*Prasyarat: M07A (disarankan M06A, M06B) | Dilanjutkan: M09*

**Tingkat 2 (Menengah)**
- **T2.1 Radiasi non-pengion** (→ M01 T1.7)
- **T2.2 Pemanasan RF (SAR) dan bagian tubuh yang rentan panas**
  - 🧪 **M07C.T2.2·A1 Kalkulator SAR — Mode 1: mode SAR**
- **T2.3 Cara menurunkan SAR**
  - 🧪 **M07C.T2.3·A1 Kalkulator SAR — Mode 3: tiap langkah ada harganya**
- **T2.4 Cedera termal dan efek antena**
  - 🧪 **M07C.T2.4·A2 Efek antena pada kawat dan loop konduktif pada tubuh**
- **T2.5 Arus induksi pada implan**
- **T2.6 Stimulasi saraf perifer oleh gradien**
  - 🧪 **M07C.T2.6·A3 Ambang stimulasi saraf oleh gradien**
- **T2.7 Kebisingan akustik**
  - 🧪 **M07C.T2.7·A4 Kalkulator kebisingan dan lama paparan**
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Batas SAR dan suhu tubuh**
  - 🧪 **M07C.T3.1·A1 Kalkulator SAR — Mode 2: pemeriksa batas SAR**
- **T3.2 Panjang gelombang di jaringan**; **T3.3 Mode operasi IEC 60601-2-33**; **T3.4 Kenaikan dan penurunan SAR**
- 📝 Kuis Tingkat 3

---

### M08A — Artefak Pengodean dan Jaringan
*Prasyarat: M04B | Dilanjutkan: M08B, M08C*

**Tingkat 1 (Dasar)**
- **T1.1 Peta artefak**
- **T1.2 Aliasing (*wrap-around*)**
- **T1.3 *Phase mismapping* (artefak ghosting gerak)**
  - 🧪 **M08A.T1.3·A3 Jarak ghosting gerak** — amati: jarak replika ∝ periode gerak, TR, dan jumlah baris
- **T1.4 Artefak pergeseran kimia** (asal fisika → M06B T3.1)
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Mengurangi aliasing**
  - 🧪 **M08A.T2.1·A5 Aliasing fase dan *oversampling*** — amati: *phase oversampling* menghilangkan lipatan dengan harga TA
- **T2.2 Mengurangi ghosting gerak**
- **T2.3 Pembatalan sinyal *out-of-phase***
  - 🧪 **M08A.T2.3·A1 Lemak, air, dan TE (out-of-phase)** — amati: sinyal turun pada TE tertentu di voksel campuran lemak–air
- **T2.4 Efek *magic angle***
  - 🧪 **M08A.T2.4·A2 *Magic angle* pada tendon** — amati: tendon hiperintens pada ± 55° terhadap B0
- **T2.5 Artefak *truncation* (Gibbs)**
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Periodisitas lemak–air dan teknik Dixon**
- **T3.2 Mengapa ghosting hanya pada arah pengodean fase**
- **T3.3 Kompensasi napas, *triggering*, dan *gating***
  - 🧪 **M08A.T3.3·A4 *Triggering* dan *gating* napas**
- **T3.4 Menghitung aliasing frekuensi**
  - 🧪 **M08A.T3.4·A6 Aliasing frekuensi dan Nyquist**
- **T3.5 Menghitung pergeseran piksel**
  - 🧪 **M08A.T3.5·A7 Pergeseran kimia dalam piksel** (+ tampilan pada citra; juga melatih T1.4)
- 📝 Kuis Tingkat 3

---

### M08B — Aliran dan Artefak Aliran
*Prasyarat: M03A, M04A | Dilanjutkan: M08C*

**Tingkat 1 (Dasar)**
- **T1.1 Aliran darah dan artefak aliran (gambaran umum)**
- **T1.2 *Entry-slice phenomenon* (ESP)**
  - 🧪 **M08B.T1.2·A1 ESP dengan simulasi empat irisan** — ubah TR, sudut flip, kecepatan; amati: spin segar pada irisan masuk lebih hiperintens (juga melatih T2.1)
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 Faktor yang memengaruhi ESP**
- **T2.2 *Time-of-flight* (TOF)**
  - 🧪 **M08B.T2.2·A2 TOF pada spin echo** — amati: spin keluar irisan sebelum echo → signal void
- **T2.3 Jenis aliran (laminar vs turbulen)**
  - 🧪 **M08B.T2.3·A3 Bilangan Reynolds** — amati: transisi laminar → turbulen
- **T2.4 *Gradient moment rephasing* (GMN)**
- **T2.5 Presaturasi spasial**
  - 🧪 **M08B.T2.5·A5 Presaturasi: spin melewati pita saturasi**
- **T2.6 Ringkasan teknik kompensasi aliran**
  - 🏁🧪 **M08B.T2.6·A6 Latihan sebab dan teknik perbaikan artefak aliran**
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 *Dephasing* intravoksel dan rumus**
  - 🧪 **M08B.T3.1·A4 Dephasing intravoksel** — langkah 1: sebaran kecepatan; langkah 2: fase spin mengalir saat gradien bekerja; langkah 3: dephasing dengan GMN (juga melatih T2.4)
- **T3.2 Momen gradien dan orde kompensasi**
- 📝 Kuis Tingkat 3

---

### M08C — Artefak Perangkat Keras, Cross-talk, dan QA
*Prasyarat: M08A, M04B | Dilanjutkan: M09*

**Tingkat 2 (Menengah)**
- **T2.1 *Cross-excitation* dan *cross-talk***
  - 🧪 **M08C.T2.1·A2 Celah antar-irisan dan cross-excitation** — amati: celah kecil → saturasi sebagian pada irisan tetangga
- **T2.2 Artefak *zipper***
- **T2.3 Artefak *shading***
- **T2.4 Artefak moiré**
  - 🧪 **M08C.T2.4·A3 Lonjakan derau di ruang-k (moiré/*spike*)** — amati: satu titik ruang-k bermasalah → pola garis pada seluruh citra
- **T2.5 Kegagalan peralatan**
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Urutan eksitasi irisan** (*interleaved*)
- **T3.2 *Quality assurance* (QA) dengan fantom**
  - 🧪 **M08C.T3.2·A1 Simulasi QA fantom**
- **T3.3 Tiap perbaikan artefak ada harganya** (kompromi → M09 T2.3)
  - 🏁🧪 **M08C.T3.3·CAPSTONE Latihan lintas artefak M08A–M08C**
    - **A4** Pemilih artefak dan perbaikannya (Tabel 8.8)
    - **A5** Identifikasi artefak (*artifact recognition*)
- 📝 Kuis Tingkat 2, Tingkat 3 (+ usulan satu set capstone lintas M08A–M08C)

---

### M09 — Optimasi Protokol dan Membaca Layar Konsol
*Prasyarat: M04B (disarankan M03A, M08A)*

**Tingkat 1 (Dasar)**
- **T1.1 Parameter pada konsol** — TR, TE, sudut flip, FoV dan matriks, ketebalan irisan
- **T1.2 Waktu akuisisi (TA)** — TA = TR × N_fase × NEX (2D konvensional); matriks frekuensi tidak memengaruhi TA
  - 🧪 **M09.T1.2·S1(a) Kalkulator TA — mode dasar** — Anda lakukan: ubah TR, FoV, matriks, NEX; amati: TA; simpulkan: parameter mana yang menambah TA
- **T1.3 Contoh protokol sederhana**
- **T1.4 Manajemen pasien** — pemilihan koil, minimisasi TA
  - 🧪 **M09.T1.4·S1(b) Studi kasus pasien non-kooperatif (TA ≤ 2 menit)** — gunakan preset *tercepat*
- 📝 Kuis Tingkat 1

**Tingkat 2 (Menengah)**
- **T2.1 SNR dan voksel** — SNR ∝ volume voksel × √(NEX · N_fase / BW)
  - 🧪 **M09.T2.1·S1(c) Kalkulator voksel dan SNR (+ mode irisan dan SNR relatif)**
- **T2.2 Meningkatkan resolusi tanpa menambah TA (FoV asimetris)**
- **T2.3 Kompromi (*trade-off*)** — NEX, ketebalan, bandwidth (pergeseran kimia naik)
  - 🏁🧪 **M09.T2.3·S1(d) Preset maksimum SNR vs tercepat** — amati: tidak ada pengaturan yang sempurna
- **T2.4 Tab konsol** (contoh antarmuka)
  - 🧪 **M09.T2.4·S2 Peta tab konsol** — Routine, Contrast, Resolution, Geometry, System, Physio, Inline, Sequence
- **T2.5 Sistem komputer** — komputer host dan PGM (kandidat ke M05)
- 📝 Kuis Tingkat 2

**Tingkat 3 (Lanjut)**
- **T3.1 Koreksi rumus TA untuk TSE (÷ ETL) dan 3D (× jumlah irisan)** [BARU]
- **T3.2 *Parallel imaging* (iPAT/SENSE/ASSET): dampak pada TA dan SNR** [BARU] → M04C T3.6
- **T3.3 Pemetaan tab/parameter antar-vendor (Siemens, GE, Philips)** [BARU] — 🔗 T2.4·S2
- 📝 Kuis Tingkat 3

---

## 5. Tabel pemetaan aktivitas lama → simpul baru

| Modul | Aktivitas lama | Simpul induk baru | Catatan |
|---|---|---|---|
| M01 | A1 | T1.5 | juga T3.1 |
| M01 | A2 | T1.6 | juga T3.2 |
| M01 | A3 | T2.3 | mencakup T2.2 |
| M02 | A1 | T2.1 | |
| M02 | A2 | T1.4 | mencakup T1.3 |
| M02 | A3 | T1.5 | |
| M02 | A4 | T2.2 | |
| M02 | A5 | T2.5 | 🏁 mencakup T2.3, T2.4 |
| M03A | A1 / A2 / A3 | T1.1 / T1.3 / T2.2 | A1 juga T2.1 |
| M03B | A1 / A2 / A3 | T1.3 / T2.2 / T2.1 | |
| M03C | A1 / A2 / A3 | T1.1 / T2.2 / T1.3 | A1 juga T2.3; A3 mencakup T1.2 |
| M04A | A1 / A2 / A3 / A4 | T1.2 / T1.5 / T1.6 / T1.7 | A4 🏁 |
| M04B | A1 / A2 / A3 | T2.1 / T2.5 / T2.7 | |
| M04B | A4 / A5 / A6 | T2.9 / T2.10 / T2.10 | |
| M04B | A7.1 / A7.2 / A7.3 | T2.9 / T2.10 / T2.3 | A7.2 🏁 |
| M04C | A1 … A6 | T3.1 / T3.3 / T3.5 / T3.6 / T3.7 / T3.7 | A6 🏁 |
| M06A | A1 / A2 / A3 / A4 | T1.3 / T2.4 / T2.3 / T2.1 | |
| M06B | A1 … A7 | T2.4 / T2.5 / T2.5 / T2.5 / T2.1 / T3.1 / T3.2 | |
| M07A | A1 … A6 | T1.1 / T1.2 / T2.3 / T1.3 / T2.4 / T2.5 | A1 juga T2.1, T2.2 |
| M07B | A1 / A2 / A3 / A4 | T2.1 / T2.5 / T2.4 / T2.7 | A4 juga T3.1 |
| M07C | A1 mode 1 / 3 / 2 | T2.2 / T2.3 / T3.1 | satu komponen, tiga mode |
| M07C | A2 / A3 / A4 | T2.4 / T2.6 / T2.7 | |
| M08A | A1 … A7 | T2.3 / T2.4 / T1.3 / T3.3 / T2.1 / T3.4 / T3.5 | A7 juga T1.4 |
| M08B | A1 … A6 | T1.2 / T2.2 / T2.3 / T3.1 / T2.5 / T2.6 | A1 juga T2.1; A4 juga T2.4 |
| M08C | A1 / A2 / A3 / capstone | T3.2 / T2.1 / T2.4 / T3.3 | capstone 🏁 (A4, A5) |
| M09 | Simulasi 1 / 2 | T1.2, T1.4, T2.1, T2.3 / T2.4 | Simulasi 1 satu komponen, empat mode |

**Hasil:** semua aktivitas terpetakan, tidak ada yang dihapus. 3 modul (M01–M03) tetap berstatus [BARU]; M05 tidak punya aktivitas.

---

## 6. Catatan dan keputusan yang masih terbuka

**Perbaikan ketergantungan (tidak diubah di teks, perlu keputusan)**
1. ⚑ M03B T3.2 menyebut M04B di header tetapi M04C pada baris simpul. Sebaiknya seragam M04C.
2. Urutan belajar yang disarankan menaruh M03B sebelum M04C, padahal EPI (M03B T3.2) butuh M04C. Pilih: pindahkan M04C sebelum M03B, atau jadikan T3.2 tautan maju.
3. M03A T2.2 dan 🧪 T2.2·A3 butuh M04B tetapi M03A tidak mencantumkannya di prasyarat; dalam urutan yang disarankan simpul ini terkunci sampai M04B selesai.
4. M07A belum masuk urutan yang disarankan; tambahkan tepat setelah M01.

**Konten yang masih harus ditulis**
5. M04B T2.10 Fakta 1–3 belum tertulis.
6. M05 belum punya aktivitas dan kuis.
7. Keselamatan agen kontras (retensi gadolinium, NSF) belum ada; usulan di M07A.
8. M09 memakai nama tab Siemens; T3.3 menutup ini, perlu dikonfirmasi vendor yang dipakai.

**Perawatan istilah**
9. "Tabel 10.1" (M07C) dan "Tabel 8.8" (M08C) mengikuti penomoran buku sumber; pindahkan ke metadata sumber.
10. Analogi di Bagian 2.1 tetap boleh muncul sebagai *jembatan* pada isi materi, tetapi judul simpul, soal kuis, dan label antarmuka harus memakai istilah baku. Disarankan satu glosarium dalam aplikasi yang memuat tabel 2.1 dan 2.2.
11. Usulan istilah Indonesia baku untuk "scan": **akuisisi** (kata kerja), **pemindai MRI** (alat), **waktu akuisisi (TA)**.

**Langkah berikutnya yang saya sarankan**
- Menyalin struktur ini ke JSON/YAML dengan ID stabil (`M04B.T2.5`, `M04B.T2.5·A2`) sehingga aplikasi bisa memuat simpul dan praktiknya sebagai satu unit.
- Menulis satu modul pilot lengkap (usulan M04A) memakai pola "konsep → praktik → cek-sekilas".
