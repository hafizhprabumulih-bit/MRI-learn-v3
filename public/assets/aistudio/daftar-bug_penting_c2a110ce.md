# Daftar Bug yang Masih Mungkin Ada — Belajar MRI

## Dasar dan batasan daftar ini

- Dasar analisis adalah **zip asli yang Anda unggah**, yaitu kode sebelum perbaikan. Saya belum melihat kode setelah Anda menjalankan prompt perbaikan.
- Empat bug berikut **sudah dikeluarkan** karena sudah Anda tangani lewat prompt:
  1. `fallbackToDestructiveMigration()` menghapus data saat skema berubah
  2. Seed kurikulum berjalan terus-menerus
  3. Duplikasi `modul4-native.html` dan `m04a/b/c-native.html`
  4. "Lanjutkan belajar" tidak tersimpan
- Nomor baris diambil dari kode asli dan **bisa bergeser** setelah perbaikan Anda. Cari berdasarkan nama fungsi.
- **Kepastian**:
  - **Pasti**: terbukti dari membaca kode.
  - **Mungkin**: logikanya mengarah ke bug, tetapi perlu diuji di perangkat atau ada bagian kode yang belum saya baca.
- Saya tidak mengompilasi atau menjalankan aplikasi.

## Ringkasan

| ID | Bug | Tingkat | Kepastian |
|----|-----|---------|-----------|
| B-01 | Tombol Back menutup aplikasi saat loading, dan loading 5,1 detik di setiap navigasi | Tinggi | Mungkin |
| B-02 | Kuis bisa dijawab ulang, skor bisa melebihi jumlah soal | Tinggi | Pasti |
| B-03 | `study_days` tertimpa saat menandai flashcard | Tinggi | Pasti |
| B-04 | Tombol "Jeda" animasi tidak menghentikan animasi | Tinggi | Pasti |
| B-05 | Toggle "selesai" memakai snapshot state, bukan transaksi | Sedang | Pasti |
| B-06 | State simulator dan dialog hilang saat layar diputar | Sedang | Pasti |
| B-07 | Semua state aplikasi hilang saat proses dimatikan sistem | Sedang | Pasti |
| B-08 | Catatan kosong bisa disimpan, dan `createdAt` tertimpa saat edit | Sedang | Pasti |
| B-09 | Kuis dari HTML dicocokkan lewat indeks, dan kode node sintetis | Sedang | Pasti |
| B-10 | Progres total tidak konsisten dengan progres per modul | Sedang | Mungkin |
| B-11 | Label pembobotan (`weightingType`) salah untuk rentang tengah | Sedang | Pasti |
| B-12 | WebView: lifecycle, scroll bersarang, `allowFileAccess`, navigasi | Sedang | Pasti / Mungkin |
| B-13 | State flashcard dipakai bersama oleh mode Modul dan Glosarium | Sedang | Pasti |
| B-14 | `calculatedSignal` bisa menghasilkan NaN | Rendah | Pasti |
| B-15 | Preset "T1 Brain" memilih jaringan lemak, "T2 Brain" memilih CSF | Rendah | Mungkin |
| B-16 | Nilai SAR dipotong di 5,5 W/kg | Rendah | Pasti |
| B-17 | Streak tidak refresh melewati tengah malam | Rendah | Pasti |
| B-18 | Format angka bergantung locale perangkat | Rendah | Pasti |
| B-19 | Filter kategori catatan terlalu longgar | Rendah | Mungkin |
| B-20 | Loading screen tidak memulai ulang saat transisi baru dipicu | Rendah | Mungkin |
| B-21 | Nama pengguna tanpa batas panjang | Rendah | Pasti |
| B-22 | Performa: `MriUiState` monolitik, animasi tak berujung | Rendah | Mungkin |
| B-23 | Dependensi tidak terpakai, termasuk `appcheck-debug` di release | Rendah | Pasti |
| B-24 | Cakupan tes sangat tipis | Rendah | Pasti |
| R-01 s/d R-04 | **Risiko baru yang mungkin muncul dari perbaikan Anda** | Tinggi | Mungkin |

## Bug aktif (dari kode asli)

### B-01 — Tombol Back menutup aplikasi saat loading; loading 5,1 detik di setiap navigasi
- **Lokasi**: `MainActivity.kt` (`BackHandler` dengan `enabled = !state.isLoading && ...`), `MriViewModel.triggerSectionTransition` (`delay(5100L)`), `LoadingScreen.kt` (`duration = 5100f`).
- **Gejala**: Saat loading tampil, `BackHandler` MainActivity mati dan `ModuleDetailScreen` tidak ada di komposisi. Back sistem kemungkinan besar langsung keluar dari aplikasi. Selain itu, setiap pindah tab, buka modul, dan pilih simulator menunggu 5,1 detik.
- **Dampak**: Pengguna kehilangan posisi, dan alur belajar terasa lambat.
- **Saran**: Tangani Back saat `isLoading` (batalkan transisi atau abaikan), dan pertimbangkan memperpendek atau menghapus loading untuk navigasi internal.

### B-02 — Kuis bisa dijawab ulang, skor melebihi jumlah soal
- **Lokasi**: `MriViewModel.prevQuizQuestion` (mereset `isAnswerSubmitted = false`) dan `submitQuizAnswer`.
- **Gejala**: Setelah menekan "sebelumnya", soal yang sudah dijawab bisa dijawab lagi. `quizScore` bertambah lagi dan `quiz_attempts` terduplikasi.
- **Dampak**: Skor akhir dan "Akurasi" menyesatkan.
- **Saran**: Simpan jawaban per soal (map indeks ke pilihan). Saat kembali ke soal yang sudah dijawab, tampilkan hasilnya dan kunci tombol.

### B-03 — `study_days` tertimpa saat menandai flashcard
- **Lokasi**: `MriViewModel.toggleFlashcardMastered` memanggil `insertStudyDay(StudyDayEntity(todayIso))` dengan strategi REPLACE.
- **Gejala**: `nodesCompletedCount` dan `lastNodeCode` hari itu menjadi 0 dan null. Selain itu, `nodesCompletedCount` diisi total semua node selesai (bukan jumlah hari ini) di `toggleNodeCompletion`, `submitQuizAnswer`, dan `recordQuizFromHtml`.
- **Dampak**: Riwayat hari belajar tidak akurat. Streak sendiri tidak terganggu karena hanya memakai tanggal.
- **Saran**: Pakai upsert yang memperbarui kolom tertentu saja, dan hitung `nodesCompletedCount` per hari dari `node_progress`.

### B-04 — Tombol "Jeda" animasi tidak menghentikan animasi
- **Lokasi**: `LearningAnimationScreen.kt`. `rememberInfiniteTransition` (`rotAngle`) berjalan terus. Parameter `isPlaying` ada di `SpinEchoCanvas`, `RelaxationCurvesCanvas`, dan `KSpaceFillCanvas`, tetapi **tidak dipakai di dalamnya**.
- **Gejala**: Label tombol berubah "Jeda" dan "Putar", tetapi gambar tetap bergerak.
- **Dampak**: Fitur tampak rusak bagi pengguna.
- **Saran**: Hentikan atau lanjutkan transisi berdasarkan `isPlaying`, misalnya dengan `Animatable` yang dikendalikan `LaunchedEffect(isPlaying)`.

### B-05 — Toggle "selesai" memakai snapshot state, bukan transaksi
- **Lokasi**: `MriViewModel.toggleNodeCompletion`. Keputusan hapus atau sisip dibuat dari `_uiState.value.completedNodeCodes`, yang diperbarui asinkron lewat Flow.
- **Gejala**: Dua klik cepat, atau klik hampir bersamaan dari WebView dan kartu native, bisa menyisipkan dua kali. Padahal seharusnya toggle.
- **Catatan**: `NodeProgressDao.getByNodeCode` sudah ada tetapi tidak dipakai.
- **Saran**: Bungkus dalam `withTransaction`, baca dari DB, lalu sisip atau hapus.

### B-06 — State simulator dan dialog hilang saat layar diputar
- **Lokasi**: Sekitar 216 pemakaian `remember { mutableStateOf }` di file simulator, dan **tidak ada satu pun `rememberSaveable`** di proyek. `AndroidManifest.xml` tidak mengatur `configChanges`.
- **Gejala**: Memutar layar, mengganti mode gelap, atau mengganti bahasa membuat Activity dibuat ulang. Parameter simulator, isi dialog catatan, pilihan tab simulator, dan WebView ter-reset.
- **Saran**: Gunakan `rememberSaveable` untuk state yang penting, atau pindahkan ke ViewModel.

### B-07 — Semua state aplikasi hilang saat proses dimatikan sistem
- **Lokasi**: `MriViewModel` tidak memakai `SavedStateHandle`.
- **Gejala**: Jika aplikasi dimatikan di latar belakang, tab, modul yang terbuka, dan filter kuis kembali ke awal. Data di Room dan DataStore tetap aman.
- **Saran**: Simpan state navigasi inti (tab, id modul terpilih, filter kuis) di `SavedStateHandle`.

### B-08 — Catatan kosong bisa disimpan; `createdAt` tertimpa saat edit
- **Lokasi**: `NotesScreen.kt` (tombol "Simpan" tanpa validasi), `MriViewModel.saveNote` (`createdAt = now` dan `updatedAt = now` selalu).
- **Gejala**: Judul dan isi kosong tetap tersimpan sebagai catatan. Mengedit catatan lama mengganti tanggal pembuatannya.
- **Saran**: Nonaktifkan "Simpan" jika judul dan isi kosong. Saat edit, pertahankan `createdAt` dari catatan asli.

### B-09 — Kuis dari HTML dicocokkan lewat indeks, dan kode node sintetis
- **Lokasi**: `MriViewModel.recordQuizFromHtml`. Soal dicari lewat `quizzesList.filter { moduleId }.getOrNull(questionIndex)`. HTML mengirim indeks soalnya sendiri (`modul4-native.html`, `recordQuiz(modId, i, k, benar)`).
- **Gejala**: Jika urutan soal di HTML berbeda dari Kotlin, `quizId` yang tercatat salah. Jika tidak ketemu, dipakai `"<mod>_Q<n>"` dan `nodeCode = "<mod>.KUIS"`, yang bukan node nyata sehingga tidak muncul di query per node atau prefix. Setiap jawaban salah juga tercatat sebagai attempt terpisah.
- **Saran**: Kirim `quizId` dari HTML (bukan indeks), atau buat peta eksplisit.

### B-10 — Progres total tidak konsisten dengan progres per modul
- **Lokasi**: `HomeScreen` (`state.completedCount`, yaitu jumlah semua baris `node_progress`) dibandingkan hitungan per modul (irisan dengan `module.nodes`).
- **Gejala**: Kode lama atau yatim, misalnya `M03.T1.1` yang tersimpan sebelum normalisasi, ikut terhitung di total. Persentase bisa melebihi 100% atau berbeda dari jumlah per modul. Periksa juga apakah node "Pendahuluan" (level 0) bisa ditandai selesai. Jika tidak, penyebutnya ikut menghitungnya.
- **Saran**: Hitung total dari irisan dengan node yang valid, dan migrasikan baris lama ke kode baru.

### B-11 — Label pembobotan salah untuk rentang tengah
- **Lokasi**: `MriUiState.weightingType`.
- **Gejala**: TR 800–1799 ms, atau TR di bawah 800 dengan TE di atas 30, jatuh ke "Proton Density". Dua cabang terakhir juga identik (kode mati). Contoh: TR 1000/TE 20 diberi label PDw.
- **Dampak**: Menyesatkan untuk aplikasi edukasi.
- **Saran**: Tentukan ambang yang jelas (T1w, T2w, PDw, dan "campuran") lalu perbarui tes di `ExampleUnitTest`.

### B-12 — WebView Modul 4: lifecycle, scroll, keamanan, navigasi
- **Lokasi**: `Modul4InteractiveWebViewSection` di `ModuleDetailScreen.kt`.
- **Temuan**:
  - Tidak ada `destroy()`, `removeJavascriptInterface`, atau pause. WebView bisa bocor dan JS tetap berjalan (pasti).
  - WebView setinggi 680dp berada di dalam `Column` dengan `verticalScroll`, sehingga scroll bersarang sering macet (mungkin, tergantung perangkat).
  - `allowFileAccess = true` tidak diperlukan untuk `file:///android_asset` dan memperluas permukaan serangan bersama `addJavascriptInterface` (pasti). Saat ini risikonya rendah karena HTML tidak memuat URL eksternal.
  - Tidak ada `shouldOverrideUrlLoading` dan tidak ada penanganan riwayat WebView untuk Back (pasti).
  - Kondisi `completedHash != 0 || completedNodeCodes.isEmpty()` selalu bernilai true, sehingga `evaluateJavascript` dipanggil di setiap recomposition (pasti).
- **Saran**: Tambahkan `DisposableEffect` untuk pembersihan, matikan `allowFileAccess`, dan tangani navigasi.

### B-13 — State flashcard dipakai bersama oleh mode Modul dan Glosarium
- **Lokasi**: `MriUiState.currentFlashcardIndex`, `isFlashcardFlipped`, `flashcardShuffleSeed`, dipakai oleh `isModuleFlashcardMode` dan `isGlossaryFlashcardMode`.
- **Gejala**: Acak kartu atau pindah kartu di satu mode memengaruhi mode lain. Indeks tidak dipulihkan saat berpindah.
- **Saran**: Pisahkan state per deck.

### B-14 — `calculatedSignal` bisa menghasilkan NaN
- **Lokasi**: `MriUiState.calculatedSignal` memakai `selectedTissue.t1Ms` dan `t2Ms` tanpa `coerceAtLeast(1f)`, padahal properti lain memakainya. `coerceIn` tidak memperbaiki NaN.
- **Saran**: Samakan perlakuannya.

### B-15 — Preset "Brain" memilih jaringan yang tidak cocok
- **Lokasi**: `applyPresetT1Brain` memilih `"fat"`, `applyPresetT2Brain` memilih `"csf"`, dengan fallback diam-diam ke `tissues[0]`.
- **Gejala**: Nama preset dan jaringan tidak cocok. Mungkin disengaja untuk kontras, tetapi tidak dijelaskan di UI.
- **Saran**: Konfirmasi maksudnya, lalu ubah nama preset atau jaringannya.

### B-16 — Nilai SAR dipotong di 5,5 W/kg
- **Lokasi**: `MriUiState.calculatedSar` (`coerceIn(0.2f, 5.5f)`).
- **Gejala**: Nilai di atas batas tidak terlihat, dan rumusnya sangat disederhanakan.
- **Saran**: Tampilkan peringatan jika hasil asli melewati batas, dan pertegas disclaimer non-klinis di layar SAR.

### B-17 — Streak tidak refresh melewati tengah malam
- **Lokasi**: `MriViewModel` (kolektor `observeStudyDays`) memakai `LocalDate.now()` sekali per emisi.
- **Gejala lain**: Menandai lalu membatalkan node tetap menghitung hari itu. `LocalDate.parse` tanpa penjagaan jika data korup.
- **Saran**: Hitung ulang saat `ON_RESUME` atau pada pergantian hari, dan bungkus `parse` dengan penanganan error.

### B-18 — Format angka bergantung locale perangkat
- **Lokasi**: `"%.2f".format(...)` tanpa `Locale` di `SimulatorsScreen.kt` (sekitar baris 299, 821, 841, 1123, 1364) dan `String.format("%.2f", ...)` di `Module8Simulators.kt`. Di bagian lain dipakai helper `fmtId` dan `fmtKoma`.
- **Gejala**: Pada perangkat berbahasa Indonesia tampil "1,50", pada locale lain "1.50". Format tidak konsisten antar layar.
- **Saran**: Pakai satu helper format di semua tempat.

### B-19 — Filter kategori catatan terlalu longgar
- **Lokasi**: `NotesScreen.kt`, memakai `startsWith(prefix)` dengan `prefix` dari kata pertama nama kategori.
- **Gejala**: Catatan dari kategori berbeda bisa ikut tampil jika awalan katanya sama.
- **Saran**: Cocokkan kategori secara persis.

### B-20 — Loading screen tidak memulai ulang saat transisi baru dipicu
- **Lokasi**: `LoadingScreen.kt` memakai `LaunchedEffect(Unit)`.
- **Gejala**: Jika transisi baru dipicu saat loading masih tampil, pesan berubah tetapi timer progres tidak. Loading bisa berakhir lebih awal dari yang dijadwalkan ViewModel.
- **Saran**: Gunakan kunci pada `LaunchedEffect` (mis. pesan atau id transisi).

### B-21 — Nama pengguna tanpa batas panjang
- **Lokasi**: `AvatarDialog.kt`, `OutlinedTextField` tanpa batas panjang atau normalisasi.
- **Gejala**: Nama panjang bisa merusak tata letak kartu di Beranda.
- **Saran**: Batasi panjang (mis. 40 karakter) dan `trim()`.

### B-22 — Performa
- `MriUiState` adalah satu data class besar. Setiap geser slider memancarkan ulang seluruh state, dan `totalNodesCount` dihitung ulang di setiap `copy()`.
- `rememberInfiniteTransition` di layar animasi terus berjalan, termasuk saat tidak ada yang perlu digerakkan.
- Pemetaan `toDomainModel`, `groupBy`, dan `sortedBy` pada Flow kurikulum berjalan di main thread (diperkirakan sudah dibahas di tahap 2 perbaikan seed, jika belum dikerjakan).

### B-23 — Dependensi tidak terpakai
- **Lokasi**: `app/build.gradle.kts`.
- **Temuan**: `firebase-ai`, `appcheck-debug`, `retrofit`, `okhttp`, `moshi`, dan `logging-interceptor` tidak dipakai di kode. `appcheck-debug` ikut ke build release.
- **Saran**: Hapus yang tidak dipakai. Catatan: jangan ubah blok `signingConfigs` dan `signingConfig` (dibutuhkan AI Studio).

### B-24 — Cakupan tes sangat tipis
- Total sekitar 855 baris tes: `StreakCalculatorTest`, `ExampleUnitTest` (Larmor dan pembobotan), `RoomDaoTest`, dan contoh bawaan.
- Tidak ada tes untuk `MriViewModel`, alur kuis, bridge WebView, dan UI.
- **Dampak**: Bug seperti B-02, B-03, dan B-05 lolos tanpa terdeteksi.

## Risiko baru yang mungkin muncul dari perbaikan Anda

Bagian ini berupa dugaan karena saya belum melihat hasil perbaikannya. Verifikasi satu per satu.

### R-01 — Crash saat start pada perangkat yang membawa database lama
- **Pemicu**: `fallbackToDestructiveMigration()` dihapus.
- **Gejala**: Perangkat uji yang masih membawa database versi 11 dari build lama, dengan skema yang sempat berubah tanpa menaikkan versi, bisa gagal dengan `IllegalStateException` (identity hash tidak cocok).
- **Solusi**: Uninstall atau hapus data aplikasi di perangkat uji.

### R-02 — Perubahan skema berikutnya akan crash tanpa Migration
- **Pemicu**: Fallback destruktif sudah tidak ada.
- **Gejala**: Setiap perubahan `@Entity` tanpa `Migration` yang cocok membuat aplikasi crash saat start untuk pengguna yang sudah memasang versi sebelumnya.
- **Solusi**: Biasakan membuat `Migration` dan tes migrasi sebelum merilis perubahan skema.

### R-03 — Fingerprint seed menghitung hash data besar di setiap cold start
- **Pemicu**: Fingerprint SHA-256 dari `toString()` lima list entity (sekitar 6.700 baris data).
- **Gejala**: Ada biaya CPU kecil di setiap start. Jika `toString()` entity tidak deterministik, seed akan jalan di setiap start.
- **Solusi**: Ukur waktunya, dan pastikan seed kedua mengembalikan `false` (lihat checklist manual di prompt).

### R-04 — Pembuka modul HTML lewat query atau hash
- **Pemicu**: Satu file HTML dibuka dengan `?mod=M04A` atau `#mod=M04A`.
- **Gejala**: Jika WebView tidak meneruskan query pada `file:///android_asset`, filter pecahan tidak terpilih. Perbandingan `webView.tag != assetUrl` juga ikut berubah karena URL kini membawa parameter.
- **Solusi**: Uji M04A, M04B, M04C, dan mode "Gabungan", termasuk sinkronisasi tombol "Tandai Selesai".

## Belum diaudit

Bagian kode berikut belum saya baca secara menyeluruh, jadi tidak ada klaim "aman" untuk bagian ini:

- Logika dan akurasi fisika di `Module3Simulators`, `Module5Simulators`, `Module6Simulators`, `Module7Simulators`, `Module8Simulators`, dan `Module9Simulators` (sekitar 12.000 baris). Yang sudah saya cek hanya pembagian dengan nol pada tiga titik, dan semuanya aman.
- Bagian lain dari `GlossaryScreen.kt`, `FlashcardDeckSection.kt`, `QuizScreen.kt`, `InfoDialog.kt`, `StreakDialog.kt`, dan `Theme.kt`.
- Versi web di `public/belajar_mri_web/` (`app.js`, `index.html`, `modul1/`).
- Isi data `MriCurriculumData.kt` (akurasi konten, konsistensi kode node dan id kuis).

## Urutan pengerjaan yang disarankan

1. **B-04** (jeda animasi): kecil dan jelas, dengan dampak langsung ke pengguna.
2. **B-02, B-03, B-05**: integritas data kuis dan progres.
3. **B-01**: Back saat loading.
4. **B-08**: validasi catatan dan `createdAt`.
5. **B-06 dan B-07**: ketahanan terhadap rotasi dan proses dimatikan.
6. **B-09, B-10, B-12**: sinkronisasi WebView dan progres.
7. **B-11, B-14 s/d B-16**: akurasi konten simulator.
8. Sisanya (B-13, B-17 s/d B-24).

Tambahkan tes untuk setiap bug yang diperbaiki (B-24).
