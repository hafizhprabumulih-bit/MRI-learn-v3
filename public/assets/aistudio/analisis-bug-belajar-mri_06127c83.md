# Analisis Bug — Belajar MRI (remix-belajar-mri.zip)

Tanggal analisis: 12 November 2026

## Dasar dan batasan

- Analisis ini berasal dari **membaca kode secara statis**. Aplikasi tidak dikompilasi dan tidak dijalankan. Temuan bertanda "mungkin" perlu diuji di perangkat.
- **Dibaca penuh:** `MriDatabase`, `Daos`, `Entities`, `UserDataRepository`, `MriViewModel`, `MainActivity`, `QuizScreen`, sebagian `ModuleDetailScreen` dan `HomeScreen`, `LoadingScreen`, `StreakCalculator`, `ResumeNodeResolver`, `LegacyNodeCodes`, `Migrations`, aturan backup, manifest.
- **Hanya dipindai dengan pola (grep):** enam file simulator (`Module3` sampai `Module9Simulators`, `SimulatorsScreen`, total sekitar 500 KB). Isi fisika di dalam simulator belum diaudit baris per baris.
- **Dicek dengan skrip:** `MriCurriculumData.kt` (186 simpul, 228 soal, 67 istilah glosarium). Tidak ditemukan ID ganda, `correctIndex` di luar rentang, `nodeCode` yatim, atau istilah glosarium ganda.
- Zip memuat `bug-tersisa-belajar-mri.md`. Daftar itu dicocokkan dengan kode sekarang: **N-02** (aturan backup DataStore) dan **N-05** (efek samping di `update {}` pada kuis) sudah teratasi; **B-01** masih ada.

## Ringkasan

| ID | Bug | Tingkat | Kepastian |
|----|-----|---------|-----------|
| K-01 | Tombol Back menutup aplikasi saat loading | Tinggi | Hampir pasti |
| K-02 | Peluncur lab di simpul membuka simulator yang salah | Tinggi | Pasti |
| K-03 | Animasi relaksasi menampilkan fisika terbalik | Sedang | Pasti |
| K-04 | Opsi kuis tidak diacak; 46% jawaban benar adalah opsi A | Sedang | Pasti |
| K-05 | Lab Modul 7 memakai toggle dengan state basi | Sedang | Pasti |
| K-06 | Catatan gagal tersimpan tanpa pesan | Sedang | Pasti |
| K-07 | Streak bisa hilang karena aktivitas flashcard | Rendah–Sedang | Pasti |
| K-08 | Konten bawah bisa tertutup tombol navigasi sistem | Sedang | Mungkin |
| K-09 | Lencana "Capstone" berubah setelah seed | Rendah | Pasti |
| K-10 | Statistik "soal pernah salah" menyesatkan | Rendah | Pasti |
| K-11 | Streak tidak segar melewati tengah malam | Rendah | Pasti |
| K-12 | Peta kode lama melompati nomor simpul | Perlu konfirmasi | Mungkin |
| K-13 | State simulator hilang saat layar diputar | Sedang | Pasti |

## Detail bug

### K-01 — Back menutup aplikasi saat loading (Tinggi)
- **Lokasi:** `MainActivity.kt` (`BackHandler`), `MriViewModel.triggerSectionTransition`, `LoadingScreen.kt`.
- **Masalah:** Hanya ada dua `BackHandler`, di `MainActivity` dan `ModuleDetailScreen`. Saat `isLoading`, yang pertama dinonaktifkan (`enabled = !state.isLoading && ...`) dan yang kedua tidak sedang tampil. Back sistem langsung keluar dari aplikasi. Loading 5,1 detik juga muncul di setiap pindah tab, buka modul, dan pilih simulator.
- **Perbaikan:** tambahkan `BackHandler(enabled = state.isLoading) { viewModel.finishLoading() }` atau telan Back sampai loading selesai. Pertimbangkan memperpendek loading untuk navigasi internal.

### K-02 — Peluncur lab membuka simulator yang salah (Tinggi)
- **Lokasi:** lambda `onLaunchLab` di `ModuleDetailScreen.kt`.
- **Masalah:** pemetaan awalan `labType` terlalu kasar:
  - `m5_*` dan `m6b_*` masuk ke Spin Larmor.
  - `m3a_*`, `m3b_*`, `m3c_*` masuk ke Kontras TR/TE.
  - `m6a_*`, `m8a/b/c_*`, `ta_snr`, dan `console_tabs` jatuh ke `else`, yaitu Ruang-k.
  - Padahal `SimType` untuk M05, M06, M03, M08, TA/SNR, dan Konsol Vendor sudah ada dan sudah dirender di `SimulatorsScreen`.
- **Dampak:** dari hitungan `labType` di data, sekitar 95 simpul terdampak. State juga tidak menyimpan lab spesifik, jadi suite yang benar pun terbuka di tab awalnya.
- **Perbaikan:** petakan per awalan ke `SimType` yang tepat (`m3*` → `SEKUENS_KONTRAS_M03`, `m5_*` → `PESAWAT_MAGNET_M05`, `m6*` → `GRADIEN_RF_M06`, `m8*` → `ARTEFAK_QA_M08`, `ta_snr` → `PROTOKOL_TA_SNR`, `console_tabs` → `KONSOL_VENDOR`). Pertimbangkan menyimpan `labType` terpilih agar suite membuka tab yang tepat.

### K-03 — Animasi relaksasi menampilkan fisika terbalik (Sedang)
- **Lokasi:** `RelaxationCurvesCanvas` di `LearningAnimationScreen.kt`.
- **Masalah:** pemulihan T1 memakai `exp(-3.5t)` dan peluruhan T2 memakai `exp(-3.0t)`. T2 jadi tampak meluruh lebih lambat daripada T1 pulih. Pada jaringan nyata T2 lebih pendek dari T1, sehingga Mxy meluruh lebih cepat daripada Mz pulih. Kurva juga tidak mengikuti jaringan yang dipilih.
- **Perbaikan:** gunakan konstanta dengan rasio T2 < T1 (misalnya T1 `exp(-2.0t)` dan T2 `exp(-4.0t)`), atau turunkan dari nilai T1/T2 jaringan terpilih.

### K-04 — Opsi kuis tidak diacak (Sedang)
- **Lokasi:** `QuizScreen.kt`, data di `MriCurriculumData.kt`.
- **Bukti:** dari 228 soal, `correctIndex` = 0 untuk 106 soal (46%), 1 untuk 81 (36%), 2 untuk 31 (14%), 3 untuk 10 (4%). Tidak ada pengacakan di layar kuis.
- **Dampak:** peserta bisa menebak A atau B dan sering benar, sehingga skor kuis kurang valid.
- **Perbaikan:** acak urutan opsi saat render dengan seed dari `quiz.id` (stabil per soal), atau seimbangkan data. Catatan: pengacakan harus memetakan indeks yang dipilih kembali ke indeks asli sebelum disimpan ke `quiz_attempts`.
- **Catatan terkait:** sebaran level tidak merata. M04A hanya level 1, M04B hanya level 2, M04C hanya level 3. M07B, M07C, dan M08C tidak punya soal level 1. Tombol T1/T2/T3 di detail modul sudah menyembunyikan level kosong, jadi ini bukan crash.

### K-05 — Lab Modul 7 memakai toggle dengan state basi (Sedang)
- **Lokasi:** `SarSafetySimulator` di `SimulatorsScreen.kt` (sekitar baris 1314).
- **Masalah:** callback `onNodeComplete` memanggil `viewModel.toggleNodeCompletion(nodeCode)` setelah memeriksa `state.completedNodeCodes` yang ditangkap lambda (bisa basi). Dua penyelesaian beruntun sebelum state diperbarui bisa membatalkan simpul.
- **Perbaikan:** pakai `viewModel.markNodeCompleted(nodeCode)` yang idempoten, seperti Modul 3 dan 8.

### K-06 — Catatan gagal tersimpan tanpa pesan (Sedang)
- **Lokasi:** `MriViewModel.saveNote`, `deleteNote`, `updateAvatar`, `toggleNodeCompletion`, `toggleFlashcardMastered`, `recordQuizFromHtml`.
- **Masalah:** kerja IO dibungkus `runCatching` tanpa log, dan `closeNoteDialog()` tetap dipanggil walau penyimpanan gagal. Isi catatan pengguna hilang tanpa jejak. `runCatching` juga menelan `CancellationException`.
- **Perbaikan:** gunakan `try/catch` yang melempar ulang `CancellationException`, catat error, dan tutup dialog hanya jika berhasil (atau tampilkan pesan gagal).

### K-07 — Streak bisa hilang karena aktivitas flashcard (Rendah–Sedang)
- **Lokasi:** `MriViewModel.toggleFlashcardMastered`, `UserDataRepository.toggleNodeCompletionTransactional`.
- **Masalah:**
  - Menandai flashcard (termasuk membatalkan hafalan) mencatat hari belajar.
  - Saat simpul dibatalkan, baris `study_days` hari itu dihapus jika tidak ada simpul dan tidak ada kuis. Aktivitas flashcard tidak ikut dihitung, sehingga hari tersebut bisa terhapus dan streak putus.
- **Perbaikan:** putuskan apakah flashcard dihitung sebagai aktivitas belajar. Jika ya, ikutkan dalam syarat penghapusan. Jika tidak, jangan catat `study_days` dari flashcard.

### K-08 — Konten bawah tertutup tombol navigasi sistem (mungkin)
- **Lokasi:** `MainActivity.kt` (`contentWindowInsets = WindowInsets.statusBars`), `ModuleDetailScreen`, `LoadingScreen`.
- **Masalah:** inset navigation bar hanya diterapkan pada `NavigationBar`. Saat detail modul tampil, bottom bar disembunyikan dan konten hanya diberi padding 16 dp, sehingga bagian bawah bisa berada di bawah bar navigasi (terutama pada navigasi 3-tombol).
- **Perbaikan:** tambahkan `Modifier.navigationBarsPadding()` pada konten saat bottom bar tidak tampil. Uji di perangkat.

### K-09 — Lencana "Capstone" berubah setelah seed (Rendah)
- **Lokasi:** `CurriculumQuizEntity` (tidak punya kolom badge), `CurriculumQuizEntity.toDomainModel()`.
- **Masalah:** badge diturunkan dari `level`. Lima soal M08C_Q16–Q20 yang di data statis berlabel "Capstone • Lintas Artefak" berubah menjadi "Tingkat 3 • Lanjut" setelah data dibaca dari database.
- **Perbaikan:** tambahkan kolom `levelBadge` lewat Migration nyata (naikkan `DB_VERSION`, tulis `MIGRATION_11_12`), atau hapus badge khusus itu dari data.

### K-10 — Statistik "soal pernah salah" menyesatkan (Rendah)
- **Lokasi:** `QuizScreen.kt` (kartu "Statistik & Riwayat Kuis").
- **Masalah:** angka di judul menghitung percobaan salah, sedangkan daftar memakai `distinctBy` dan hanya 3 baris. Soal yang kemudian dijawab benar tetap terhitung. Statistik juga bersifat global dan tidak mengikuti filter modul.
- **Perbaikan:** hitung per soal unik dan hanya yang jawaban terakhirnya salah; ikuti filter modul.

### K-11 — Streak tidak segar melewati tengah malam (Rendah)
- **Lokasi:** `MainActivity.onResume`, `MriViewModel.refreshStreak`.
- **Masalah:** streak hanya dihitung ulang saat `onResume` atau saat `study_days` berubah. Jika aplikasi terbuka terus melewati tengah malam, nilainya baru berubah setelah aplikasi ke latar belakang dan kembali.
- **Perbaikan:** picu refresh saat pergantian hari (misalnya `ACTION_DATE_CHANGED` atau timer ke tengah malam).

### K-12 — Peta kode lama melompati nomor simpul (perlu konfirmasi)
- **Lokasi:** `LegacyNodeCodes.MAP`.
- **Masalah:** `M03.T1.2 → M03A.T1.3` dan `M03.T3.2 → M03C.T1.4` melompati nomor, padahal `M03A.T1.2` dan `M03C.T1.2/T1.3` ada di kurikulum. Jika bukan disengaja, progres lama pindah ke simpul yang salah.
- **Tindakan:** konfirmasi bahwa pemetaan ini memang sesuai penomoran ulang yang dilakukan.

### K-13 — State simulator hilang saat layar diputar (Sedang)
- **Lokasi:** `AndroidManifest.xml`, file simulator.
- **Masalah:** manifest tidak mengatur `configChanges` maupun `screenOrientation`. Sebagian state sudah `rememberSaveable`, tetapi sebagian besar nilai slider simulator masih `remember` biasa (dokumen bug internal menyebut sekitar 214; belum dihitung ulang di analisis ini).
- **Perbaikan (termurah dulu):** kunci orientasi portrait, atau `configChanges="orientation|screenSize|uiMode"`, atau ubah state penting menjadi `rememberSaveable`.

## Risiko dan kode mati

### Risiko upgrade database
`DB_VERSION = 11` dengan `ALL_MIGRATIONS` kosong, dan `fallbackToDestructiveMigration` sengaja dilarang. Perangkat uji yang memegang database v11 dari build lama dengan skema yang pernah berubah tanpa menaikkan versi akan crash saat start (identity hash tidak cocok). Memasang APK lama di atas yang baru (downgrade) juga gagal keras. Solusi sementara: hapus data aplikasi di perangkat itu. Untuk perubahan skema berikutnya, ikuti prosedur di komentar `Migrations.kt`.

### Tabel dan fungsi yang tidak terpakai
- `replaceCurriculum` menghapus `mri_nodes` dan `quiz_questions` tetapi tidak pernah mengisinya. DAO dan fungsi `observe...` terkait tidak dipakai UI.
- `MriDatabase.seedCurriculumIfNeeded()` namanya menyesatkan karena selalu menimpa (gerbang pengecekan ada di repository). Ganti nama, misalnya `replaceCurriculumFromSource()`.

### Fitur HTML Modul 4 (keputusan diperlukan)
- `MriWebBridge` dan `MriViewModel.recordQuizFromHtml` tidak dipakai siapa pun karena tidak ada `WebView` di aplikasi.
- Ada 72 nilai `htmlSimulationAsset` di `MriCurriculumData.kt` yang menunjuk file yang tidak ada di APK (folder `app/src/main/assets/` tidak ada). `CurriculumNodeEntity` juga tidak membawa kolom itu.
- Jika dihapus dengan sengaja: buang ketiganya. Jika tidak: kembalikan `assets/`, WebView, dan perbarui bridge agar mengirim `quizId` eksplisit.

### Lain-lain
- `MriUiState.lastVisitedNode` punya fallback `MriRepository.modules[0].nodes[1]` yang bisa melempar `IndexOutOfBounds` jika data modul kosong.
- Fingerprint kurikulum dihitung ulang dari seluruh data di setiap cold start (biaya belum diukur).
- State flashcard "cermin" (`currentFlashcardIndex`, `isFlashcardFlipped`, `flashcardShuffleSeed`) masih diperbarui di banyak tempat padahal kemungkinan tidak dibaca lagi.

## Urutan perbaikan yang disarankan

1. K-01 (Back saat loading) dan K-02 (peta lab).
2. K-05 dan K-06 (integritas data pengguna).
3. K-04 (pengacakan opsi) dan K-03 (fisika animasi).
4. K-07, K-13, K-08.
5. Sisanya, termasuk keputusan soal Modul 4 HTML dan pembersihan kode mati.
6. Jalankan `./gradlew :app:testDebugUnitTest` dan `connectedAndroidTest`, lalu uji manual: putar layar, matikan proses, tekan Back saat loading, dan coba tombol lab di tiap modul.
