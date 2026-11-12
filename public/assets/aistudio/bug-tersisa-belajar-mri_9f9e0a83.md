# Bug yang Masih Tersisa — Belajar MRI

Dokumen ini hanya memuat bug yang **belum selesai atau baru sebagian**. Bug yang sudah diperbaiki dengan benar sudah dihapus.

- **Dasar**: perbandingan zip lama dengan `remix-belajar-mri.zip` (terbaru). Tidak dikompilasi dan tidak dijalankan, jadi verifikasi di perangkat tetap perlu.
- **ID** mengikuti daftar sebelumnya agar mudah dilacak.
- **Lokasi** memakai nama fungsi karena nomor baris berubah.

**Sudah selesai dan dihapus dari daftar**: fallback destruktif database, seed kurikulum berulang, "Lanjutkan belajar", B-02, B-04, B-05, B-07, B-08, B-10, B-11, B-12, B-14, B-16, B-18, B-19, B-20, B-21.

## Ringkasan

| ID | Bug | Tingkat | Status |
|----|-----|---------|--------|
| N-01 | Fitur HTML Modul 4 hilang dari APK | Tinggi (jika tidak disengaja) | Perlu keputusan |
| B-01 | Back menutup aplikasi saat loading | Tinggi | ❌ Belum |
| N-02 | Backup DataStore memakai domain salah | Sedang | ❌ Baru ditemukan |
| B-06 | State simulator hilang saat layar diputar | Sedang | 🟡 Sebagian |
| N-03 | `validNodeCodes` dibuat ulang di setiap `copy()` | Rendah | ❌ Baru ditemukan |
| N-05 | Efek samping di dalam lambda `update { }` | Rendah | ❌ Baru ditemukan |
| B-03 | Baris `study_days` lama tidak dihitung ulang | Rendah | 🟡 Sisa |
| B-17 | Streak: hari tetap terhitung setelah semua node dibatalkan | Rendah | 🟡 Sisa |
| B-15 | Preset T2 Brain memilih CSF tanpa penjelasan | Rendah | 🟡 Sisa |
| N-04 | State flashcard cermin masih ada | Rendah | ❌ Baru ditemukan |
| N-06 | `isResumeRestored` tidak dipakai | Rendah | ❌ Baru ditemukan |
| N-07 | Dua cabang identik di `InlineMrActiveNucleusChecker` | Rendah | ❌ Baru ditemukan |
| — | Sisa kecil seed, dependensi, dan tes | Rendah | 🟡 Sisa |
| R-01, R-03 | Risiko pasca-perbaikan | Rendah | 🟡 Perlu diuji |

## Prioritas tinggi

### N-01 — Fitur HTML interaktif Modul 4 hilang dari APK
- **Bukti**: folder `app/src/main/assets/` tidak ada, `Modul4InteractiveWebViewSection` dihapus dari `ModuleDetailScreen.kt`, dan tidak ada lagi referensi `android_asset`.
- **Sisa kode mati**:
  - `MriWebBridge.kt` (kelasnya ada, tidak ada yang memakai).
  - `MriViewModel.recordQuizFromHtml(...)` (satu-satunya pemanggil adalah `MriWebBridge`). Bridge itu juga tidak mengirim `explicitQuizId`, sehingga masalah pencocokan kuis lewat indeks (B-09) muncul lagi jika WebView dikembalikan.
  - 71 baris `htmlSimulationAsset = "modul4/modul4-native.html?mod=..."` di `MriCurriculumData.kt` yang menunjuk file yang tidak ada. UI tidak membaca field ini, jadi tidak crash.
- **Pilih salah satu**:
  - **Disengaja**: hapus tiga sisa di atas.
  - **Tidak disengaja**: kembalikan `assets/modul4/modul4-native.html`, kembalikan WebView, dan perbarui `MriWebBridge` agar mengirim `explicitQuizId`.

### B-01 — Back menutup aplikasi saat loading
- **Lokasi**: `MainActivity` (`BackHandler`), `MriViewModel.triggerSectionTransition`.
- **Masalah**: `BackHandler(enabled = !state.isLoading && ...)` tidak berubah. Saat loading tampil, `ModuleDetailScreen` (yang punya `BackHandler` sendiri) tidak ada di komposisi, sehingga Back sistem kemungkinan besar menutup aplikasi. Loading 5,1 detik di setiap navigasi sengaja dipertahankan (ada komentar di kode).
- **Saran**: Tangani Back saat `isLoading`, misalnya membatalkan `transitionJob` dan kembali ke state sebelumnya, atau menelan Back sampai loading selesai.

## Prioritas sedang

### N-02 — Backup DataStore memakai domain yang salah
- **Lokasi**: `res/xml/data_extraction_rules.xml` dan `res/xml/backup_rules.xml`.
- **Masalah**: Keduanya berisi `<include domain="sharedpref" path="user_preferences.preferences_pb"/>`. DataStore Preferences menyimpan filenya di `files/datastore/`, jadi aturan itu tidak cocok dengan file mana pun dan DataStore tidak ikut dicadangkan.
- **Dampak**: Setelah restore atau pindah perangkat, database (progres, catatan, riwayat kuis) kembali, tetapi bookmark, flashcard yang dihafal, avatar, nama, dan posisi "Lanjutkan" hilang. Seed kurikulum dan normalisasi kode lama akan berjalan ulang dengan aman.
- **Saran**: Ganti dengan `<include domain="file" path="datastore/user_preferences.preferences_pb"/>` di kedua file. Verifikasi path dengan `adb shell run-as <paket> ls files/datastore`.

### B-06 — State simulator hilang saat layar diputar
- **Sudah**: `AvatarDialog`, `NotesScreen`, `InlineMrActiveNucleusChecker`, `InlineEmSpectrumExplorer`, dan tab di `Module8Simulators`.
- **Belum**: sekitar 214 `remember { mutable... }` di `Module3`, `Module5`, `Module6`, `Module7`, `Module8`, dan `Module9Simulators` (nilai slider dan parameter). `AndroidManifest.xml` tidak mengatur `configChanges` maupun `screenOrientation`.
- **Saran** (dari yang termurah): kunci orientasi portrait, atau `configChanges="orientation|screenSize|uiMode"`, atau ubah state penting menjadi `rememberSaveable`.

## Prioritas rendah

### N-03 — `validNodeCodes` dibuat ulang di setiap `copy()` (juga mencakup sisa B-22)
- **Lokasi**: badan `MriUiState`, `val validNodeCodes = modulesList.flatMapTo(HashSet()) { ... }`.
- **Masalah**: Dihitung ulang di setiap `copy()`, termasuk setiap geseran slider simulator. Sebelumnya hanya `sumOf` yang ringan. `MriUiState` juga masih satu data class besar.
- **Saran**: Jadikan `by lazy { ... }` atau hitung di ViewModel saat `modulesList` berubah.

### N-05 — Efek samping di dalam lambda `update { }`
- **Lokasi**: `submitQuizAnswer` mengisi `submittedQuiz`, `chosenIndex`, dan `wasCorrect` dari dalam `_uiState.update { }`.
- **Masalah**: Lambda `update` bisa dijalankan ulang saat terjadi benturan pembaruan. Pada dua tap hampir bersamaan, jawaban yang sama bisa tertulis dua kali di `quiz_attempts`. Sangat jarang.
- **Saran**: Baca state sekali di awal, putuskan, lalu panggil `update` tanpa efek samping.
- **Catatan terkait (B-02)**: menekan "Berikutnya" di soal terakhir menandai kuis selesai walau ada soal yang dilewati. Soal itu terhitung salah di total.

### B-03 (sisa) — Baris `study_days` lama tidak dihitung ulang
- **Masalah**: Saat node dibatalkan, hanya baris hari ini yang dihitung ulang. Baris hari-hari sebelumnya tetap berisi angka lama. Batas hari memakai `ZoneId.systemDefault()`, sehingga hitungan harian bisa bergeser jika zona waktu perangkat berubah.
- **Saran**: Hitung ulang baris pada tanggal `completedAt` node yang dibatalkan.

### B-17 (sisa) — Streak setelah pembatalan
- **Masalah**: Membatalkan semua node hari ini tidak menghapus baris `study_days`, sehingga hari itu tetap terhitung sebagai hari belajar. Jika aplikasi terbuka terus melewati tengah malam tanpa `onResume`, streak belum ter-refresh.
- **Saran**: Hapus atau abaikan baris hari itu jika tidak ada aktivitas lain (kuis, flashcard), dan pertimbangkan pemicu penyegaran saat pergantian hari.

### B-15 (sisa) — Preset T2 Brain
- **Masalah**: `applyPresetT2Brain` memilih `csf` tanpa penjelasan. Ini wajar untuk menunjukkan kontras T2 yang terang, tetapi belum dijelaskan di UI maupun komentar.
- **Saran**: Putuskan apakah disengaja, lalu tambahkan komentar atau label di UI.

### N-04 — State flashcard cermin masih ada
- **Masalah**: Field `currentFlashcardIndex`, `isFlashcardFlipped`, dan `flashcardShuffleSeed` masih diperbarui di belasan tempat, padahal di `ModuleDetailScreen` dan `GlossaryScreen` yang saya periksa tidak ada lagi yang membacanya. `moduleFlashcardShuffleSeed` juga tidak direset saat berpindah modul.
- **Saran**: Hapus field cermin setelah memastikan tidak ada pembacanya, dan reset seed saat membuka modul lain.

### N-06 — `isResumeRestored` tidak dipakai
- **Masalah**: `HomeScreen.kt` tidak berubah, jadi judul default pada kartu "Lanjutkan" bisa sempat berkedip sebelum diganti nilai dari DataStore.
- **Saran**: Tampilkan placeholder sampai `isResumeRestored == true`, setelah memastikan `HeroProgressCard` merender judul kosong dengan wajar.
- **Batasan yang diketahui**: tombol "Lanjutkan" tetap hanya membuka modul, tanpa scroll ke node tujuan.

### N-07 — Dua cabang identik
- **Lokasi**: `InlineMrActiveNucleusChecker`, `mutableIntStateOf(if (isExceptionLesson) 1 else 1)`.
- **Masalah**: Kedua cabang bernilai sama, kemungkinan salah salin.
- **Saran**: Periksa nilai awal `z` yang diinginkan untuk pelajaran biasa dan pelajaran pengecualian.

### Sisa kecil lainnya
- **Seed**: `MriDatabase.seedCurriculumIfNeeded()` namanya menyesatkan karena selalu menimpa tanpa pengecekan (gerbangnya ada di repository). Ganti nama, misalnya `replaceCurriculumFromSource()`.
- **Tabel kosong**: `mri_nodes` dan `quiz_questions` kini selalu kosong (tes mengunci ini). DAO dan fungsi `observe...` terkait di repository tidak dipakai UI. Rencanakan penghapusannya lewat `Migration`.
- **Dependensi**: `firebase-ai`, `appcheck-*`, `retrofit`, `okhttp`, `moshi`, dan `logging-interceptor` hanya dikomentari. Setelah build sukses, hapus barisnya dan entri tak terpakai di `libs.versions.toml`.
- **`runCatching`**: `toggleNodeCompletion` dan `submitQuizAnswer` masih membungkus kerja IO dengan `runCatching`, yang menelan error tanpa jejak, termasuk `CancellationException`.
- **Visual**: penanda di kurva relaksasi bergerak maju lalu melompat kembali setiap 3 detik (`rotAngle / 360`).
- **Celah tes**: tidak ada tes khusus untuk alur kuis per soal, jeda animasi, dan pemulihan `SavedStateHandle`.

## Risiko pasca-perbaikan yang masih terbuka

- **R-01**: Perangkat uji yang memegang database v11 dari build lama dengan skema yang pernah berubah tanpa menaikkan versi bisa crash saat start (identity hash tidak cocok). Solusinya hapus data aplikasi di perangkat itu. Downgrade (memasang APK lama di atas yang baru) kini gagal keras, dan itu disengaja.
- **R-03**: Fingerprint kurikulum dihitung ulang dari seluruh data di setiap cold start. Biayanya belum diukur.

## Urutan pengerjaan yang disarankan

1. Putuskan **N-01** (Modul 4 sengaja dihapus atau tidak).
2. Perbaiki **B-01** dan **N-02**.
3. Putuskan **B-06** (kunci orientasi atau `rememberSaveable`).
4. Perbaiki **N-03** dan **N-05**.
5. Bersihkan sisa kecil (**N-04**, **N-06**, **N-07**, dependensi, nama fungsi seed).
6. Jalankan semua tes di mesin Anda (`./gradlew :app:testDebugUnitTest` dan `connectedAndroidTest`), lalu uji manual di perangkat: putar layar, mulai ulang proses, dan restore backup.
