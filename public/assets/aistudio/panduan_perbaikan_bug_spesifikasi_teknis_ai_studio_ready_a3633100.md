# Spesifikasi Perbaikan Bug Aplikasi MRI Learning (Instruksi Eksekusi Google AI Studio)

Dokumen ini berisi daftar **Bug Baru (NB-01 s/d NB-05)** dan **Bug Lama yang Perlu Perbaikan Ulang / Penyelesaian Akhir**. Seluruh bug yang sudah selesai diperbaiki pada iterasi sebelumnya (serta B-01 yang sengaja dipertahankan) telah dikeluarkan dari dokumen ini agar proses perbaikan fokus, presisi, dan bebas regresi.

> **Instruksi untuk AI Coding Assistant (Google AI Studio):**
> Terapkan setiap perbaikan di bawah ini secara berurutan pada file yang disebutkan. Jangan mengubah logika bisnis atau fitur lain yang sudah berjalan dengan baik, khususnya durasi dan perilaku *back-press* pada `LoadingScreen` (B-01) yang sengaja dipertahankan.

---

## BAGIAN 1: Temuan Bug Baru (Prioritas Tinggi & Menengah)

### NB-01: Auto-Complete pada `Module3Simulators.kt` Memicu Toggle Ganda dan Membatalkan Status "Selesai"
* **Tingkat Keparahan:** Kritis (Merusak data progres pengguna secara langsung)
* **File Terkait:**
  1. `app/src/main/java/com/example/mrilearning/ui/screens/Module3Simulators.kt`
  2. `app/src/main/java/com/example/mrilearning/ui/screens/ModuleDetailScreen.kt`
  3. `app/src/main/java/com/example/mrilearning/data/UserDataRepository.kt`
  4. `app/src/main/java/com/example/mrilearning/viewmodel/MriViewModel.kt`

#### Akar Masalah
1. Di `ModuleDetailScreen.kt`, komponen `Module3SimulatorsSection` dipanggil di dalam `NodeCard` dengan callback:
   ```kotlin
   onNodeComplete = {
       if (!isCompleted) onToggleCompleted()
   }
   ```
   Perhatikan bahwa `onNodeComplete` di `Module3SimulatorsSection` bertipe `(String) -> Unit` (menerima parameter `nodeCode`), tetapi `ModuleDetailScreen.kt` mengabaikan parameter `String` tersebut dan langsung memanggil `onToggleCompleted()` milik simpul kartu saat ini.
2. Di `Module3Simulators.kt`, banyak tab memanggil `onNodeComplete(...)` **2 hingga 3 kali berturut-turut** di dalam satu blok `LaunchedEffect(Unit)` saat tab pertama kali dirender:
   * `InversionRecoveryTabContent`: memanggil `onNodeComplete("M03A.T1.2")` lalu `onNodeComplete("M03A.T2.1")`.
   * `TurboSpinEchoTabContent`: memanggil `onNodeComplete("M03A.T1.3")` lalu `onNodeComplete("M03A.T2.2")`.
   * `GreContrastStoveTabContent`: memanggil `onNodeComplete("M03B.T1.1")` lalu `onNodeComplete("M03B.T2.1")`.
   * `ThreeSteadyStateTabContent`: memanggil `onNodeComplete("M03B.T1.2")`, `onNodeComplete("M03B.T1.3")`, lalu `onNodeComplete("M03B.T2.2")`.
   * `DwiStejskalTabContent`: memanggil `onNodeComplete("M03C.T1.1")` lalu `onNodeComplete("M03C.T1.2")`.
   * `BoldAndMtcTabContent`: memanggil `onNodeComplete("M03C.T1.3")` lalu `onNodeComplete("M03C.T1.5")`.
   * `ContrastAgentTabContent`: memanggil `onNodeComplete("M03C.T1.4")`, `onNodeComplete("M03C.T2.1")`, lalu `onNodeComplete("M03C.T2.2")`.
3. Karena `isCompleted` pada closure Compose belum berubah pada milidetik yang sama, `onToggleCompleted()` dipanggil 2 kali berturut-turut. Panggilan pertama menjalankan transaksi *insert* (menandai selesai), sedangkan panggilan kedua melihat data sudah ada di Room Database lalu menjalankan *delete* (membatalkan status selesai).

#### Solusi Rinci
**Langkah 1 — Tambahkan fungsi idempoten `markNodeCompletedIfNotYet` di `UserDataRepository.kt`:**
Jangan gunakan fungsi *toggle* untuk penyelesaian otomatis dari simulator. Tambahkan fungsi yang hanya melakukan *insert* jika simpul belum selesai:

```kotlin
// Di dalam class UserDataRepository (UserDataRepository.kt)
suspend fun markNodeCompletedIfNotYet(rawNodeCode: String): Boolean {
    val nodeCode = normalizeNodeCode(rawNodeCode)
    if (nodeCode.isBlank()) return false

    val now = System.currentTimeMillis()
    val today = LocalDate.now().format(dateFormatter)
    val (startOfDay, endOfDay) = todayBoundsMillis()

    var inserted = false
    database.withTransaction {
        val progressDao = database.userNodeProgressDao()
        val existing = progressDao.getByNodeCode(nodeCode)
        if (existing == null || !existing.completed) {
            progressDao.upsert(
                UserNodeProgressEntity(
                    nodeCode = nodeCode,
                    completed = true,
                    completedAt = now
                )
            )
            val completedTodayCount = progressDao.getCountCompletedBetween(startOfDay, endOfDay)
            upsertStudyDayInternal(
                dateIso = today,
                completedCount = completedTodayCount,
                lastNodeCode = nodeCode,
                updatedAt = now
            )
            inserted = true
        }
    }

    if (inserted) {
        updateCompletedSimulationsDataStore(nodeCode, completed = true)
    }
    return inserted
}
```

**Langkah 2 — Ekspos fungsi `markNodeCompleted(nodeCode: String)` di `MriViewModel.kt`:**
```kotlin
// Di dalam class MriViewModel (MriViewModel.kt)
fun markNodeCompleted(nodeCode: String) {
    viewModelScope.launch {
        userDataRepository.markNodeCompletedIfNotYet(nodeCode)
    }
}
```

**Langkah 3 — Perbaiki pemanggilan callback di `ModuleDetailScreen.kt`:**
Teruskan parameter `(String) -> Unit` dari `ModuleDetailScreen` ke `NodeCard` atau gunakan `onMarkNodeCompleted: (String) -> Unit`:
```kotlin
// Pada Module3SimulatorsSection di dalam NodeCard (ModuleDetailScreen.kt):
Module3SimulatorsSection(
    subModuleIndex = index,
    onNodeComplete = { targetNodeCode ->
        onMarkNodeCompleted(targetNodeCode)
    }
)
```
*(Pastikan di `MainActivity.kt`, parameter `onMarkNodeCompleted = { code -> viewModel.markNodeCompleted(code) }` diteruskan ke `ModuleDetailScreen`).*

---

### NB-02: Kalender Streak 7 Hari di `StreakDialog.kt` Hanya Menyalakan 1 Hari Saat Streak Kelipatan 7
* **Tingkat Keparahan:** Menengah (Bug visual & logika gamifikasi)
* **File Terkait:** `app/src/main/java/com/example/mrilearning/ui/components/StreakDialog.kt`

#### Akar Masalah
Pada `StreakDialog.kt`, perhitungan jumlah kotak hari yang menyala menggunakan rumus modulo:
```kotlin
val isCompleted = index < (streakDays % 7).coerceAtLeast(if (streakDays > 0) 1 else 0)
```
Ketika pengguna mencapai streak kelipatan $7$ (misalnya $7, 14, 21$ hari), nilai $\text{streakDays} \bmod 7 = 0$. Kemudian `.coerceAtLeast(1)` memaksanya menjadi $1$. Akibatnya, di hari ke-7 pengguna justru melihat hanya 1 hari (Senin) yang menyala, bukan 7 hari penuh.

#### Solusi Rinci
Ubah logika perhitungan `isCompleted` di `StreakDialog.kt` menjadi seperti berikut:

```kotlin
// Di dalam StreakDialog.kt, ganti baris perhitungan isCompleted:
val activeDaysInWeek = if (streakDays <= 0) 0 else ((streakDays - 1) % 7) + 1

days.forEachIndexed { index, day ->
    val isCompleted = index < activeDaysInWeek
    // ... kode rendering indikator hari tetap sama
```
Dengan rumus $((\text{streakDays} - 1) \bmod 7) + 1$:
* Jika `streakDays == 0` $\rightarrow$ `0` hari menyala.
* Jika `streakDays == 1` $\rightarrow$ `1` hari menyala.
* Jika `streakDays == 6` $\rightarrow$ `6` hari menyala.
* Jika `streakDays == 7` $\rightarrow$ `7` hari menyala penuh.
* Jika `streakDays == 8` $\rightarrow$ kembali `1` hari menyala (siklus minggu baru).

---

### NB-03: Pembacaan Tabel Database Kosong (`mri_nodes` & `quiz_questions`) di `UserDataRepository.kt`
* **Tingkat Keparahan:** Tinggi (Fungsi *observer* kurikulum dan kuis di repository mengembalikan list kosong)
* **File Terkait:**
  1. `app/src/main/java/com/example/mrilearning/data/UserDataRepository.kt`
  2. `app/src/main/java/com/example/mrilearning/data/local/MriDatabase.kt`

#### Akar Masalah
Di dalam `MriDatabase.replaceCurriculum(snapshot)`, data kurikulum disimpan ke tabel milik `curriculumDao()` (`curriculum_nodes` dan `curriculum_quizzes`), sedangkan tabel lama milik `mriCurriculumDao()` (`mri_nodes` dan `quiz_questions`) hanya dikosongkan (`clearNodes()`, `clearQuizQuestions()`) tanpa pernah diisi ulang.
Namun, di `UserDataRepository.kt`, empat fungsi publik masih memanggil `mriCurriculumDao()`:
* `observeAllCurriculumNodes()` $\rightarrow$ memanggil `mriCurriculumDao().observeAllNodes()` (kosong!)
* `observeNodesByModule(moduleId)` $\rightarrow$ memanggil `mriCurriculumDao().observeNodesByModule(moduleId)` (kosong!)
* `observeAllQuizQuestions()` $\rightarrow$ memanggil `mriCurriculumDao().observeAllQuizQuestions()` (kosong!)
* `observeQuizByModule(moduleId)` $\rightarrow$ memanggil `mriCurriculumDao().observeQuizByModule(moduleId)` (kosong!)

#### Solusi Rinci
Di `UserDataRepository.kt`, arahkan seluruh fungsi *observer* kurikulum dan kuis ke `database.curriculumDao()` dan gunakan satu tipe entitas kanonik (`CurriculumNodeEntity` dan `QuizEntity`):

```kotlin
// Di dalam UserDataRepository.kt, ganti fungsi-fungsi yang memanggil mriCurriculumDao() menjadi:
fun observeAllModules(): Flow<List<MriModuleEntity>> =
    database.mriCurriculumDao().observeAllModules()

fun observeAllCurriculumNodes(): Flow<List<CurriculumNodeEntity>> =
    database.curriculumDao().observeAllNodes()

fun observeNodesByModule(moduleId: String): Flow<List<CurriculumNodeEntity>> =
    database.curriculumDao().observeNodesForModule(moduleId)

fun observeAllQuizzes(): Flow<List<QuizEntity>> =
    database.curriculumDao().observeAllQuizzes()

fun observeQuizzesForModule(moduleId: String): Flow<List<QuizEntity>> =
    database.curriculumDao().observeQuizzesForModule(moduleId)
```

---

### NB-04: Ketidakcocokan Nama Kategori Catatan dari `MriWebBridge.kt` Saat Diedit di `NoteEditorDialog`
* **Tingkat Keparahan:** Rendah - Menengah (Inkonsistensi UI saat mengedit catatan)
* **File Terkait:**
  1. `app/src/main/java/com/example/mrilearning/ui/components/MriWebBridge.kt`
  2. `app/src/main/java/com/example/mrilearning/ui/screens/NotesScreen.kt`

#### Akar Masalah
Fungsi `saveNoteFromHtml` di `MriWebBridge.kt` menyimpan catatan dengan string `category = "Fisika"`. Sedangkan daftar kategori resmi di `NoteEditorDialog` (`NotesScreen.kt`) adalah:
`listOf("Catatan Bebas", "Protokol Klinis", "Fisika Sekuens", "Mitigasi Artefak", "Keselamatan MR")`.
Saat pengguna membuka editor untuk catatan yang berasal dari WebBridge (atau catatan lama berkategori `"Fisika"`), tidak ada satu pun `FilterChip` kategori yang menyala karena `"Fisika" != "Fisika Sekuens"`.

#### Solusi Rinci
1. Di `MriWebBridge.kt`, ubah nilai default kategori pada `saveNoteFromHtml`:
   ```kotlin
   @JavascriptInterface
   fun saveNoteFromHtml(moduleId: String, title: String, content: String) {
       val cleanTitle = title.trim().ifEmpty { "Catatan $moduleId" }
       val cleanContent = content.trim()
       if (cleanContent.isEmpty()) return

       viewModel.saveNote(
           title = cleanTitle,
           content = cleanContent,
           category = "Fisika Sekuens", // Sebelumnya "Fisika"
           moduleId = moduleId
       )
   }
   ```
2. Di `NotesScreen.kt` (di dalam `NoteEditorDialog`), normalisasikan inisialisasi state `category` agar catatan lama yang sudah telanjur tersimpan dengan nama alias tetap memilih chip yang benar:
   ```kotlin
   val initialNormalizedCategory = when (initialNote?.category?.trim()?.lowercase()) {
       "fisika", "fisika sekuens" -> "Fisika Sekuens"
       "klinis", "protokol", "protokol klinis" -> "Protokol Klinis"
       "artefak", "mitigasi artefak" -> "Mitigasi Artefak"
       "keselamatan", "safety", "keselamatan mr" -> "Keselamatan MR"
       "catatan bebas" -> "Catatan Bebas"
       else -> initialNote?.category?.takeIf { it.isNotBlank() } ?: "Catatan Bebas"
   }
   var category by rememberSaveable(initialNote?.id) {
       mutableStateOf(initialNormalizedCategory)
   }
   ```

---

### NB-05: Navigasi "Buka Modul" di `GlossaryScreen.kt` Masih Memakai Data Statis `MriRepository.modules`
* **Tingkat Keparahan:** Rendah - Menengah (Potensi ketidaksinkronan state modul)
* **File Terkait:** `app/src/main/java/com/example/mrilearning/ui/screens/GlossaryScreen.kt`

#### Akar Masalah
Pada `GlossaryScreen.kt`, saat pengguna menekan tombol **"Buka Modul X"** di dalam `GlossaryDetailDialog`, kode mencari objek modul dari `MriRepository.modules` (data statis), bukan dari `state.modulesList` yang sudah disinkronkan dengan Room Database.

#### Solusi Rinci
Di `GlossaryScreen.kt`, ubah blok `onNavigate` pada pemanggilan `GlossaryDetailDialog`:

```kotlin
// Di dalam GlossaryScreen.kt:
if (selectedItemForDetail != null) {
    GlossaryDetailDialog(
        item = selectedItemForDetail!!,
        onDismiss = { selectedItemForDetail = null },
        onNavigate = { modNum ->
            selectedItemForDetail = null
            val targetModule = state.modulesList.find {
                it.id == "M0$modNum" || it.id == "M$modNum" || it.title.startsWith("MODUL $modNum", ignoreCase = true)
            } ?: MriRepository.modules.find {
                it.id == "M0$modNum" || it.title.startsWith("MODUL $modNum", ignoreCase = true)
            }
            if (targetModule != null) {
                onSelectModule(targetModule)
            }
        }
    )
}
```

---

## BAGIAN 2: Bug Lama yang Belum Tuntas & Perlu Perbaikan Ulang

### B-06 (Bagian Simulator & Glosarium): State Hilang Saat Layar Diputar (Rotasi Konfigurasi)
* **Tingkat Keparahan:** Menengah
* **File Terkait:**
  1. `app/src/main/java/com/example/mrilearning/ui/screens/GlossaryScreen.kt`
  2. `app/src/main/java/com/example/mrilearning/ui/screens/Module3Simulators.kt`

#### Akar Masalah
`AvatarDialog.kt` dan `NotesScreen.kt` sudah berhasil diperbaiki menggunakan `rememberSaveable`. Namun:
1. Di `GlossaryScreen.kt`, `selectedItemForDetail` masih menggunakan `remember { mutableStateOf<GlossaryItem?>(null) }` dan `GlossaryItem` bukan `Parcelable`, sehingga dialog detail istilah langsung hilang saat layar diputar.
2. Di `Module3Simulators.kt`, seluruh state tab dan parameter fisika (`selectedSubModule`, `activeTab`, `teMs`, `viewMode`, `tiMs`, `isMagnitudeDisplay`, `etl`, `echoSpacing`, `effectiveTe`, `isDriveActive`, `trMs`, `flipAngleDeg`, `gradAmpG`, `agentConc`, dll.) masih menggunakan `remember` biasa, sehingga kembali ke nilai awal saat perangkat diputar.

#### Solusi Rinci
**1. Di `GlossaryScreen.kt` — Simpan term string di `rememberSaveable`:**
```kotlin
// Ganti: var selectedItemForDetail by remember { mutableStateOf<GlossaryItem?>(null) }
// Menjadi:
var selectedTermForDetail by rememberSaveable { mutableStateOf<String?>(null) }
val selectedItemForDetail = remember(selectedTermForDetail, state.glossaryList) {
    selectedTermForDetail?.let { term ->
        state.glossaryList.find { it.term == term }
    }
}
```
Lalu saat kartu diklik, set `selectedTermForDetail = item.term`, dan saat ditutup set `selectedTermForDetail = null`.

**2. Di `Module3Simulators.kt` — Ganti `remember` menjadi `rememberSaveable` pada seluruh primitif dan enum:**
Tambahkan `import androidx.compose.runtime.saveable.rememberSaveable` di bagian atas `Module3Simulators.kt`, lalu ubah:
* `var selectedSubModule by rememberSaveable(subModuleIndex) { mutableIntStateOf(subModuleIndex.coerceIn(0, 2)) }`
* `var activeTab by rememberSaveable { mutableIntStateOf(0) }` (pada `SubModule3ASimulator`, `SubModule3BSimulator`, dan `SubModule3CSimulator`)
* Pada `SpinEchoT2TabContent`:
  ```kotlin
  var teMs by rememberSaveable { mutableFloatStateOf(80f) }
  var viewMode by rememberSaveable { mutableStateOf(SeViewMode.SPIN_ECHO) }
  ```
* Pada `InversionRecoveryTabContent`:
  ```kotlin
  var tiMs by rememberSaveable { mutableFloatStateOf(2500f) }
  var isMagnitudeDisplay by rememberSaveable { mutableStateOf(true) }
  ```
* Pada `TurboSpinEchoTabContent`:
  ```kotlin
  var etl by rememberSaveable { mutableIntStateOf(8) }
  var echoSpacing by rememberSaveable { mutableFloatStateOf(12f) }
  var effectiveTe by rememberSaveable { mutableFloatStateOf(96f) }
  var isDriveActive by rememberSaveable { mutableStateOf(false) }
  ```
* Pada `GreContrastStoveTabContent`:
  ```kotlin
  var trMs by rememberSaveable { mutableFloatStateOf(120f) }
  var teMs by rememberSaveable { mutableFloatStateOf(10f) }
  var flipAngleDeg by rememberSaveable { mutableFloatStateOf(35f) }
  ```
* Pada `ThreeSteadyStateTabContent`:
  ```kotlin
  var selectedType by rememberSaveable { mutableStateOf(SteadyStateType.SPOILED) }
  ```
* Pada `DwiStejskalTabContent`:
  ```kotlin
  var gradAmpG by rememberSaveable { mutableFloatStateOf(3.0f) }
  var gradDurMs by rememberSaveable { mutableFloatStateOf(20f) }
  var gradSepMs by rememberSaveable { mutableFloatStateOf(35f) }
  var selectedPathology by rememberSaveable { mutableStateOf(DwiPathology.ACUTE_STROKE) }
  ```
* Pada `BoldAndMtcTabContent`:
  ```kotlin
  var subMode by rememberSaveable { mutableIntStateOf(0) }
  var isMotorActive by rememberSaveable { mutableStateOf(false) }
  var isMtcActive by rememberSaveable { mutableStateOf(false) }
  ```
* Pada `ContrastAgentTabContent`:
  ```kotlin
  var selectedAgent by rememberSaveable { mutableStateOf(ContrastAgentType.GD_DTPA) }
  var agentConc by rememberSaveable { mutableFloatStateOf(0.5f) }
  ```

---

### B-09: `MriWebBridge.kt` Masih Mencocokkan Kuis Berdasarkan Indeks (`questionIndex: Int`)
* **Tingkat Keparahan:** Menengah
* **File Terkait:**
  1. `app/src/main/java/com/example/mrilearning/ui/components/MriWebBridge.kt`
  2. `app/src/main/java/com/example/mrilearning/viewmodel/MriViewModel.kt`

#### Akar Masalah
`MriWebBridge.recordQuiz` hanya menerima `questionIndex: Int`. Jika urutan soal di HTML atau database berubah, indeks soal tidak lagi cocok dengan soal yang dimaksud.

#### Solusi Rinci
Tambahkan dukungan parameter opsional/overload `quizId: String` pada `MriWebBridge.kt` sambil tetap mempertahankan kompatibilitas mundur (*backward compatibility*) untuk pemanggil JavaScript lama:

```kotlin
// Di dalam MriWebBridge.kt:
@JavascriptInterface
fun recordQuiz(
    moduleId: String,
    questionIndex: Int,
    selectedOption: Int,
    isCorrect: Boolean
) {
    viewModel.recordQuizFromHtml(
        moduleId = moduleId,
        quizId = "${moduleId.trim().uppercase()}_Q${questionIndex + 1}",
        questionIndex = questionIndex,
        selectedOption = selectedOption,
        isCorrect = isCorrect
    )
}

@JavascriptInterface
fun recordQuizById(
    moduleId: String,
    quizId: String,
    selectedOption: Int,
    isCorrect: Boolean
) {
    viewModel.recordQuizFromHtml(
        moduleId = moduleId,
        quizId = quizId.trim(),
        questionIndex = -1,
        selectedOption = selectedOption,
        isCorrect = isCorrect
    )
}
```

---

### B-20 (Edge-Case): `LoadingScreen` Tidak Me-Reset Progress Jika Dua Transisi Memiliki `customMessage` yang Sama
* **Tingkat Keparahan:** Rendah
* **File Terkait:** `app/src/main/java/com/example/mrilearning/ui/components/LoadingScreen.kt`

#### Akar Masalah
Di `LoadingScreen.kt`, efek animasi menggunakan `LaunchedEffect(customMessage)`. Jika transisi pertama dan transisi berikutnya sama-sama memiliki `customMessage == null` (atau string yang sama) dan komponen tetap berada di pohon komposisi, key `LaunchedEffect` tidak berubah.

#### Solusi Rinci
Pastikan `progress` dan `factIndex` selalu di-reset di awal `LoadingScreen` menggunakan kombinasi `customMessage` dan waktu mulai komponen, tanpa mengubah durasi `5100f` (sesuai ketentuan B-01):

```kotlin
// Di dalam LoadingScreen.kt:
// Ganti: LaunchedEffect(customMessage)
// Menjadi:
val sessionKey = remember(customMessage) { System.nanoTime() }

LaunchedEffect(customMessage, sessionKey) {
    progress = 0f
    val duration = 5100f // Dipertahankan 5,1 detik sesuai B-01
    val step = 50f
    var elapsed = 0f
    while (elapsed < duration) {
        delay(step.toLong())
        elapsed += step
        progress = (elapsed / duration).coerceIn(0f, 1f)
    }
    onLoadingFinished()
}
```

---

## BAGIAN 3: Checklist & Patch Verifikasi di `MriViewModel.kt` (B-02, B-11, B-14, B-15, B-16)

Pastikan di dalam file `app/src/main/java/com/example/mrilearning/viewmodel/MriViewModel.kt` kelima logika berikut sudah diterapkan dengan benar:

### 1. B-02: Mencegah Soal Kuis Dijawab Ulang Saat Menekan Tombol "Sebelumnya"
Pastikan `MriUiState` menyimpan riwayat jawaban per indeks soal di dalam sesi kuis aktif:
```kotlin
// Di dalam MriUiState:
val answeredQuizMap: Map<Int, Int> = emptyMap() // Key: currentQuizIndex, Value: selectedQuizOption
```
Dan pada `MriViewModel`:
```kotlin
fun submitQuizAnswer() {
    val s = _uiState.value
    val currentQuestion = s.activeQuizList.getOrNull(s.currentQuizIndex) ?: return
    val selected = s.selectedQuizOption ?: return

    // Cegah penambahan skor ganda jika soal pada indeks ini sudah pernah disubmit
    if (s.answeredQuizMap.containsKey(s.currentQuizIndex)) {
        _uiState.update { it.copy(isAnswerSubmitted = true) }
        return
    }

    val isCorrect = selected == currentQuestion.correctAnswerIndex
    val updatedMap = s.answeredQuizMap + (s.currentQuizIndex to selected)
    val newScore = if (isCorrect) (s.quizScore + 1).coerceAtMost(s.activeQuizList.size) else s.quizScore

    _uiState.update {
        it.copy(
            isAnswerSubmitted = true,
            quizScore = newScore,
            answeredQuizMap = updatedMap
        )
    }

    // Simpan ke Room Database
    viewModelScope.launch {
        userDataRepository.recordQuizAnswer(
            quizId = currentQuestion.id.ifBlank { "${s.selectedModule?.id ?: "GEN"}_Q${s.currentQuizIndex + 1}" },
            selectedIndex = selected,
            isCorrect = isCorrect
        )
    }
}

fun prevQuizQuestion() {
    _uiState.update { s ->
        if (s.currentQuizIndex > 0) {
            val prevIdx = s.currentQuizIndex - 1
            val prevSelected = s.answeredQuizMap[prevIdx]
            s.copy(
                currentQuizIndex = prevIdx,
                selectedQuizOption = prevSelected,
                isAnswerSubmitted = prevSelected != null
            )
        } else s
    }
}

fun nextQuizQuestion() {
    _uiState.update { s ->
        if (s.currentQuizIndex < s.activeQuizList.size - 1) {
            val nextIdx = s.currentQuizIndex + 1
            val nextSelected = s.answeredQuizMap[nextIdx]
            s.copy(
                currentQuizIndex = nextIdx,
                selectedQuizOption = nextSelected,
                isAnswerSubmitted = nextSelected != null
            )
        } else {
            s.copy(isQuizFinished = true)
        }
    }
}
```
*(Jangan lupa mengosongkan `answeredQuizMap = emptyMap()` saat memulai ulang kuis di `startQuiz()` atau `restartQuiz()`).*

### 2. B-11 & B-14: Perhitungan `weightingType` dan `calculatedSignal` di `MriUiState`
Pastikan properti terkomputasi di `MriUiState` menangani zona transisi dan mencegah pembagian dengan nol ($\text{NaN}$):
```kotlin
val weightingType: String
    get() = when {
        trValue < 700f && teValue < 30f -> "T1-Weighted"
        trValue > 2000f && teValue > 80f -> "T2-Weighted"
        trValue > 2000f && teValue < 30f -> "Proton Density (PD)"
        else -> "Campuran / Transisional"
    }

val calculatedSignal: Float
    get() {
        val safeT1 = selectedTissue.t1Ms.coerceAtLeast(1f)
        val safeT2 = selectedTissue.t2Ms.coerceAtLeast(1f)
        val recovery = 1f - kotlin.math.exp(-trValue / safeT1)
        val decay = kotlin.math.exp(-teValue / safeT2)
        val rawSignal = selectedTissue.protonDensity * recovery * decay
        return if (rawSignal.isNaN() || rawSignal.isInfinite()) 0f else rawSignal.coerceIn(0f, 1f)
    }
```

### 3. B-15 & B-16: Sinkronisasi Preset Protokol Klinis & Indikator Peringatan SAR
Pada fungsi `applyClinicalPreset` dan `recalculateClinicalSimulator` di `MriViewModel.kt`:
* Pastikan saat preset `"Brain Routine"` diterapkan, `clinicalSelectedSlice = "Brain"` juga ikut diatur di `_uiState`.
* Jangan memotong nilai $\text{SAR}$ secara diam-diam di angka $5.5\text{ W/kg}$ tanpa status peringatan. Hitung nilai $\text{SAR}$ aktual:
  $$\text{SAR} \propto B_0^2 \cdot \alpha^2 \cdot \frac{\text{ETL}}{\text{TR}}$$
  Jika $\text{SAR} > 4.0\text{ W/kg}$ (batas First Level Controlled Mode IEC), tampilkan peringatan keselamatan di UI simulator klinis.

---

## Ringkasan Urutan Eksekusi untuk AI Studio
1. **Perbaiki `UserDataRepository.kt`**: Tambahkan `markNodeCompletedIfNotYet` (NB-01) dan arahkan fungsi *observer* ke `curriculumDao()` (NB-03).
2. **Perbaiki `MriViewModel.kt`**: Tambahkan `markNodeCompleted(nodeCode)` (NB-01) dan pastikan patch kuis `answeredQuizMap` (B-02) serta `MriUiState` (B-11, B-14, B-15, B-16) sudah sesuai.
3. **Perbaiki `ModuleDetailScreen.kt` & `MainActivity.kt`**: Hubungkan `onNodeComplete` pada `Module3SimulatorsSection` ke `viewModel.markNodeCompleted(nodeCode)` (NB-01).
4. **Perbaiki `StreakDialog.kt`**: Gunakan rumus `((streakDays - 1) % 7) + 1` (NB-02).
5. **Perbaiki `MriWebBridge.kt` & `NotesScreen.kt`**: Ubah kategori menjadi `"Fisika Sekuens"` dan normalisasikan kategori awal di `NoteEditorDialog` (NB-04, B-09).
6. **Perbaiki `GlossaryScreen.kt` & `Module3Simulators.kt`**: Gunakan `state.modulesList` untuk navigasi modul (NB-05) dan ganti `remember` menjadi `rememberSaveable` (B-06).
7. **Perbaiki `LoadingScreen.kt`**: Tambahkan `sessionKey` pada `LaunchedEffect` dengan tetap mempertahankan durasi `5100f` (B-20 & B-01).