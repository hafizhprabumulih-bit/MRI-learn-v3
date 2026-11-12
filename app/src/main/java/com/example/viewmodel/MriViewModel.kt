package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.data.GlossaryItem
import com.example.data.CharacterCustomizationState
import com.example.data.LegacyNodeCodes
import com.example.data.MriFlashcard
import com.example.data.MriModule
import com.example.data.MriNode
import com.example.data.MriRepository
import com.example.data.QuizQuestion
import com.example.data.StudyNote
import com.example.data.TissueProperties
import com.example.data.UserDataRepository
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyDayEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.toDomainModel
import com.example.util.QuizShuffle
import com.example.util.ResumeNodeResolver
import com.example.util.StreakCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import kotlin.math.exp

enum class MainTab {
    BERANDA,
    ANIMASI,
    SIMULASI,
    TERSIMPAN,
    CATATAN,
    GLOSARIUM,
    KUIS,
    KARAKTER
}

enum class SimType {
    SPIN_LARMOR,
    KONTRAS_TR_TE,
    RUANG_K,
    SAR_SAFETY,
    SCAN_TIME_ESTIMATOR,
    PROTOKOL_TA_SNR,
    KONSOL_VENDOR,
    PESAWAT_MAGNET_M05,
    GRADIEN_RF_M06,
    SEKUENS_KONTRAS_M03,
    ARTEFAK_QA_M08
}

data class MriUiState(
    val currentTab: MainTab = MainTab.BERANDA,
    val modulesList: List<MriModule> = MriRepository.modules,
    val quizzesList: List<QuizQuestion> = MriRepository.quizzes,
    val selectedModule: MriModule? = null,
    val selectedSimType: SimType = SimType.SPIN_LARMOR,
    val completedNodeCodes: Set<String> = emptySet(),
    val lastVisitedNodeCode: String? = null,
    val isResumeRestored: Boolean = false,
    // Streak Hari Belajar 🔥
    val studyStreakDays: Int = 0,
    val showStreakDialog: Boolean = false,
    // Avatar Pengguna & Ruang Karakter 👨‍⚕️
    val userAvatarId: String = "radiografer_mri",
    val userName: String = "",
    val showAvatarDialog: Boolean = false,
    val characterCustomization: CharacterCustomizationState = CharacterCustomizationState(),
    // Catatan Belajar (Notes) 📝
    val notesList: List<StudyNote> = emptyList(),
    val selectedNoteCategory: String = "SEMUA",
    val showNoteDialog: Boolean = false,
    val editingNote: StudyNote? = null,
    // Riwayat Kuis
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    // Animasi Belajar 🎬
    val selectedAnimationIndex: Int = 0,
    val isAnimationPlaying: Boolean = true,
    val animationStep: Int = 1, // 1 to 4
    // Simulator Kontras & Kurva Relaksasi T1/T2 States
    val selectedTissue: TissueProperties = MriRepository.tissues[0],
    val relaxationTimeMs: Float = 260f,
    val isRelaxationPlaying: Boolean = false,
    val trMs: Float = 500f,
    val teMs: Float = 20f,
    // Simulator Larmor States
    val selectedB0: Float = 1.5f,
    val selectedNucleusGamma: Float = MriRepository.GAMMA_1H,
    val selectedNucleusName: String = "Hidrogen (¹H)",
    val flipAngleDeg: Float = 90f,
    // SAR Calculator States
    val patientWeightKg: Float = 70f,
    val isTseSequence: Boolean = true,
    // Kuis States
    val quizModuleFilter: String = "SEMUA",
    val quizLevelFilter: Int = 0, // 0 = Semua, 1 = Tingkat 1, 2 = Tingkat 2, 3 = Tingkat 3
    val currentQuizIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val quizAnswers: Map<String, Int> = emptyMap(),
    val answeredQuizMap: Map<Int, Int> = emptyMap(),
    val quizScore: Int = 0,
    val isQuizFinished: Boolean = false,
    // Glosarium States
    val glossarySearchQuery: String = "",
    val glossaryModuleFilter: String = "SEMUA",
    val selectedGlossaryDetail: GlossaryItem? = null,
    // Level & Mode filter for Module Detail (-1 = Semua, 0 = Tujuan, 1 = T1, 2 = T2, 3 = T3)
    val nodeLevelFilter: Int = 0,
    val globalNodeMode: String = "s",
    val nodeModeMap: Map<String, String> = emptyMap(),
    // Bookmark & Flashcards States (dipisahkan per mode Modul vs Glosarium - B-13)
    val bookmarkedNodeCodes: Set<String> = emptySet(),
    val bookmarkedGlossaryTerms: Set<String> = emptySet(),
    val masteredFlashcardIds: Set<String> = emptySet(),
    val onlyBookmarkedNodes: Boolean = false,
    val onlyBookmarkedGlossary: Boolean = false,
    val isModuleFlashcardMode: Boolean = false,
    val isGlossaryFlashcardMode: Boolean = false,
    val moduleFlashcardIndex: Int = 0,
    val isModuleFlashcardFlipped: Boolean = false,
    val moduleFlashcardShuffleSeed: Int = 0,
    val glossaryFlashcardIndex: Int = 0,
    val isGlossaryFlashcardFlipped: Boolean = false,
    val glossaryFlashcardShuffleSeed: Int = 0,
    val currentFlashcardIndex: Int = 0,
    val isFlashcardFlipped: Boolean = false,
    val flashcardShuffleSeed: Int = 0,
    // Dialog & Loading States
    val showInfoDialog: Boolean = false,
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val matrixNy: Int = 256,
    val nexCount: Int = 1,
    val etlCount: Int = 4
) {
    // B-10, B-22 & N-03: Evaluasi malas (lazy) agar penyalinan state saat geser slider tidak mengalokasikan HashSet baru di setiap frame
    val validNodeCodes: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        modulesList.flatMapTo(HashSet()) { m -> m.nodes.map { it.code } }
    }
    val totalNodesCount: Int by lazy(LazyThreadSafetyMode.NONE) { validNodeCodes.size }
    val completedCount: Int by lazy(LazyThreadSafetyMode.NONE) {
        completedNodeCodes.count { it in validNodeCodes }
    }

    val isFlashcardMode: Boolean
        get() = if (selectedModule != null) isModuleFlashcardMode else isGlossaryFlashcardMode

    fun isFlashcardBookmarked(card: MriFlashcard): Boolean =
        if (card.isGlossaryCard) {
            bookmarkedGlossaryTerms.contains(card.glossaryTermKey) ||
                bookmarkedGlossaryTerms.contains(card.frontTitle)
        } else {
            bookmarkedNodeCodes.contains(card.nodeCode)
        }

    val lastVisitedNode: MriNode
        get() = ResumeNodeResolver.findNode(modulesList, lastVisitedNodeCode)
            ?: modulesList.firstOrNull()?.nodes?.firstOrNull { it.level >= 1 }
            ?: modulesList.firstOrNull()?.nodes?.firstOrNull()
            ?: MriRepository.modules[0].nodes[1]

    val filteredQuizzes: List<QuizQuestion>
        get() = quizzesList.filter { q ->
            val filter = quizModuleFilter
            val matchesMod = filter == "SEMUA" ||
                q.moduleId == filter ||
                (filter.length <= 3 && q.moduleId.startsWith(filter)) ||
                (filter.length <= 3 && q.nodeCode.startsWith(filter)) ||
                q.nodeCode.startsWith(filter)
            val matchesLevel = quizLevelFilter == 0 || q.level == quizLevelFilter
            matchesMod && matchesLevel
        }.map { QuizShuffle.shuffled(it) }

    val estimatedScanTimeSec: Float
        get() {
            val trSec = trMs / 1000f
            return (trSec * matrixNy * nexCount) / etlCount.coerceAtLeast(1)
        }

    val larmorFreqMhz: Float
        get() = selectedNucleusGamma * selectedB0

    val mzRecoveryRatio: Float
        get() = (1f - exp(-relaxationTimeMs / selectedTissue.t1Ms.coerceAtLeast(1f))).coerceIn(0f, 1f)

    val mxyDecayRatio: Float
        get() = exp(-relaxationTimeMs / selectedTissue.t2Ms.coerceAtLeast(1f)).coerceIn(0f, 1f)

    // B-14: Cegah pembagian dengan nol / NaN jika t1Ms atau t2Ms <= 0
    val calculatedSignal: Float
        get() {
            val t1 = selectedTissue.t1Ms.coerceAtLeast(1f)
            val t2 = selectedTissue.t2Ms.coerceAtLeast(1f)
            val pd = selectedTissue.protonDensity
            val t1Factor = 1f - exp(-trMs / t1)
            val t2Factor = exp(-teMs / t2)
            return (pd * t1Factor * t2Factor).coerceIn(0.01f, 1.0f)
        }

    // B-11: Rentang tengah TR/TE menghasilkan Pembobotan Campuran (Mixed / Intermediate), bukan jatuh ke PDw
    val weightingType: String
        get() = when {
            trMs < 800f && teMs <= 30f -> "Pembobotan T1-Weighted (T1w)"
            trMs >= 1800f && teMs >= 70f -> "Pembobotan T2-Weighted (T2w)"
            trMs >= 1800f && teMs <= 30f -> "Pembobotan Proton Density (PDw)"
            else -> "Pembobotan Campuran (Mixed / Intermediate)"
        }

    // B-16: Simpan nilai SAR mentah (tanpa pemotongan 5,5 W/kg) dan flag batas terlampaui
    val rawCalculatedSar: Float
        get() {
            val b0Factor = (selectedB0 / 1.5f) * (selectedB0 / 1.5f)
            val rfPowerFactor = if (isTseSequence) 1.85f else 0.85f
            val baseSar = 0.95f * b0Factor * rfPowerFactor
            return (baseSar * (70f / patientWeightKg.coerceAtLeast(30f))).coerceAtLeast(0.2f)
        }

    val isSarClamped: Boolean
        get() = rawCalculatedSar > 5.5f

    val calculatedSar: Float
        get() = rawCalculatedSar.coerceIn(0.2f, 5.5f)
}

class MriViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Konstruktor sekunder untuk kompatibilitas tes unit yang memanggil MriViewModel(application)
    constructor(application: Application) : this(application, SavedStateHandle())

    private val repository = UserDataRepository(application)

    private val initialTab: MainTab = savedStateHandle.get<String>(KEY_CURRENT_TAB)
        ?.let { runCatching { MainTab.valueOf(it) }.getOrNull() }
        ?: MainTab.BERANDA

    private val initialSimType: SimType = savedStateHandle.get<String>(KEY_SELECTED_SIM_TYPE)
        ?.let { runCatching { SimType.valueOf(it) }.getOrNull() }
        ?: SimType.SPIN_LARMOR

    private val initialModule: MriModule? = savedStateHandle.get<String>(KEY_SELECTED_MODULE_ID)
        ?.let { modId -> MriRepository.modules.find { it.id == modId } }

    private val initialQuizModFilter: String = savedStateHandle.get<String>(KEY_QUIZ_MOD_FILTER) ?: "SEMUA"
    private val initialQuizLevelFilter: Int = savedStateHandle.get<Int>(KEY_QUIZ_LEVEL_FILTER) ?: 0
    private val initialNoteCategory: String = savedStateHandle.get<String>(KEY_NOTE_CATEGORY) ?: "SEMUA"

    private val _uiState = MutableStateFlow(
        MriUiState(
            currentTab = initialTab,
            selectedModule = initialModule,
            selectedSimType = initialSimType,
            quizModuleFilter = initialQuizModFilter,
            quizLevelFilter = initialQuizLevelFilter,
            selectedNoteCategory = initialNoteCategory
        )
    )
    val uiState: StateFlow<MriUiState> = _uiState.asStateFlow()

    private var latestStudyDays: List<StudyDayEntity> = emptyList()

    companion object {
        private const val KEY_CURRENT_TAB = "saved_current_tab"
        private const val KEY_SELECTED_MODULE_ID = "saved_selected_module_id"
        private const val KEY_SELECTED_SIM_TYPE = "saved_selected_sim_type"
        private const val KEY_QUIZ_MOD_FILTER = "saved_quiz_mod_filter"
        private const val KEY_QUIZ_LEVEL_FILTER = "saved_quiz_level_filter"
        private const val KEY_NOTE_CATEGORY = "saved_note_category"
        const val MAX_USER_NAME_LENGTH = 40
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.seedCurriculumIfNeeded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal seed kurikulum", e)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.normalizeLegacyNodeCodesIfNeeded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menormalkan kode lama", e)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.seededNotesFlow.collect { seeded ->
                if (!seeded) {
                    val count = repository.getNotesCount()
                    if (count == 0) {
                        MriRepository.defaultNotes.forEach { defaultNote ->
                            repository.insertNote(
                                StudyNoteEntity(
                                    id = defaultNote.id,
                                    title = defaultNote.title,
                                    content = defaultNote.content,
                                    category = defaultNote.category,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    repository.setSeededNotes(true)
                }
            }
        }

        viewModelScope.launch {
            repository.observeCurriculumNodes()
                .map { nodeEntities ->
                    if (nodeEntities.isEmpty()) null else {
                        val groupedByModule = nodeEntities.groupBy { it.moduleId }
                        MriRepository.modules.map { baseMod ->
                            val orderMap = baseMod.nodes.mapIndexed { idx, n -> n.code to idx }.toMap()
                            val dbNodes = groupedByModule[baseMod.id]
                                ?.map { it.toDomainModel() }
                                ?.sortedBy { orderMap[it.code] ?: Int.MAX_VALUE }
                            if (!dbNodes.isNullOrEmpty()) {
                                baseMod.copy(nodes = dbNodes)
                            } else {
                                baseMod
                            }
                        }
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect { updatedModules ->
                    if (updatedModules != null) {
                        _uiState.update { current ->
                            val updatedSelected = current.selectedModule?.let { sel ->
                                updatedModules.find { it.id == sel.id } ?: sel
                            }
                            current.copy(
                                modulesList = updatedModules,
                                selectedModule = updatedSelected
                            )
                        }
                    }
                }
        }

        viewModelScope.launch {
            repository.observeCurriculumQuizzes()
                .map { quizEntities ->
                    if (quizEntities.isEmpty()) null else {
                        val mappedQuizzes = quizEntities.map { it.toDomainModel() }
                        val orderMap = MriRepository.quizzes.mapIndexed { index, q -> q.id to index }.toMap()
                        mappedQuizzes.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect { sortedQuizzes ->
                    if (sortedQuizzes != null) {
                        _uiState.update { it.copy(quizzesList = sortedQuizzes) }
                    }
                }
        }

        viewModelScope.launch {
            repository.observeCompletedNodes().collect { nodes ->
                val codes = nodes.map { it.nodeCode }.toSet()
                _uiState.update { it.copy(completedNodeCodes = codes) }
            }
        }

        viewModelScope.launch {
            repository.observeNotes().collect { noteEntities ->
                val notes = noteEntities.map { entity ->
                    StudyNote(
                        id = entity.id,
                        title = entity.title,
                        content = entity.content,
                        category = entity.category,
                        dateString = android.text.format.DateFormat.format("dd MMM yyyy", entity.updatedAt).toString()
                    )
                }
                _uiState.update { it.copy(notesList = notes) }
            }
        }

        viewModelScope.launch {
            repository.observeQuizAttempts().collect { attempts ->
                _uiState.update { it.copy(quizAttempts = attempts) }
            }
        }

        viewModelScope.launch {
            repository.observeStudyDays().collect { days ->
                latestStudyDays = days
                recomputeStreak(days)
            }
        }

        viewModelScope.launch {
            repository.userNameFlow.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            repository.userAvatarIdFlow.collect { avatar ->
                _uiState.update { it.copy(userAvatarId = avatar) }
            }
        }

        viewModelScope.launch {
            repository.bookmarkedNodesFlow.collect { bookmarks ->
                _uiState.update { it.copy(bookmarkedNodeCodes = bookmarks) }
            }
        }

        viewModelScope.launch {
            repository.bookmarkedGlossaryFlow.collect { bookmarks ->
                _uiState.update { it.copy(bookmarkedGlossaryTerms = bookmarks) }
            }
        }

        viewModelScope.launch {
            repository.masteredFlashcardsFlow.collect { mastered ->
                _uiState.update { it.copy(masteredFlashcardIds = mastered) }
            }
        }

        viewModelScope.launch {
            repository.lastVisitedNodeCodeFlow.collect { code ->
                _uiState.update { it.copy(lastVisitedNodeCode = code, isResumeRestored = true) }
            }
        }

        viewModelScope.launch {
            repository.characterCustomizationFlow.collect { charState ->
                _uiState.update { it.copy(characterCustomization = charState, userAvatarId = charState.avatarId) }
            }
        }
    }

    // B-17: Hitung ulang streak dengan penjagaan parse tanggal korup dan dukungan refresh saat ganti hari / ON_RESUME
    private fun recomputeStreak(days: List<StudyDayEntity> = latestStudyDays, today: LocalDate = LocalDate.now()) {
        val studyDaysSet = days.mapNotNull { day ->
            runCatching { LocalDate.parse(day.dateIso) }.getOrNull()
        }.toSet()
        val streak = StreakCalculator.compute(studyDaysSet, today)
        _uiState.update { it.copy(studyStreakDays = streak) }
    }

    fun refreshStreak(today: LocalDate = LocalDate.now()) {
        recomputeStreak(latestStudyDays, today)
    }

    private fun rememberLastVisited(code: String) {
        if (code.isBlank() || !ResumeNodeResolver.isKnownNode(_uiState.value.modulesList, code)) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setLastVisitedNodeCode(code)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menyimpan posisi terakhir", e)
            }
        }
    }

    private var transitionJob: Job? = null

    fun triggerSectionTransition(destinationName: String? = null, onStart: () -> Unit = {}) {
        transitionJob?.cancel()
        onStart()
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = destinationName
            )
        }
        transitionJob = viewModelScope.launch {
            delay(5100L) // 5.1 seconds duration exactly matching user request & HTML logic (B-01 sengaja disimpan)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadingMessage = null
                )
            }
        }
    }

    fun finishLoading() {
        transitionJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = false,
                loadingMessage = null
            )
        }
    }

    fun selectTab(tab: MainTab) {
        if (_uiState.value.currentTab == tab && _uiState.value.selectedModule == null && !_uiState.value.isLoading) return
        val tabName = when (tab) {
            MainTab.BERANDA -> "Beranda"
            MainTab.ANIMASI -> "Animasi Belajar"
            MainTab.SIMULASI -> "Laboratorium Simulasi"
            MainTab.TERSIMPAN -> "Materi & Glosarium Tersimpan"
            MainTab.CATATAN -> "Catatan Klinis"
            MainTab.GLOSARIUM -> "Glosarium Radiologi"
            MainTab.KUIS -> "Uji Kompetensi"
            MainTab.KARAKTER -> "Ruang Karakter"
        }
        savedStateHandle[KEY_CURRENT_TAB] = tab.name
        savedStateHandle[KEY_SELECTED_MODULE_ID] = null
        triggerSectionTransition(tabName) {
            _uiState.update {
                it.copy(
                    currentTab = tab,
                    selectedModule = null,
                    isModuleFlashcardMode = false,
                    currentFlashcardIndex = it.glossaryFlashcardIndex,
                    isFlashcardFlipped = it.isGlossaryFlashcardFlipped,
                    flashcardShuffleSeed = it.glossaryFlashcardShuffleSeed
                )
            }
        }
    }

    fun updateCharacterCustomization(state: com.example.data.CharacterCustomizationState) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateCharacterCustomization(state)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal memperbarui kustomisasi karakter", e)
            }
        }
    }

    fun resetCharacterCustomization() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val defaultState = com.example.data.CharacterCustomizationState()
                repository.updateCharacterCustomization(defaultState)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal mereset kustomisasi karakter", e)
            }
        }
    }

    // --- Streak Belajar 🔥 ---
    fun setStreakDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showStreakDialog = visible) }
    }

    // --- Avatar Pengguna 👨‍⚕️ ---
    fun setAvatarDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAvatarDialog = visible) }
    }

    fun updateAvatar(avatarId: String, name: String) {
        val normalizedName = name.trim().take(MAX_USER_NAME_LENGTH)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateUserAvatarId(avatarId)
                repository.updateUserName(normalizedName)
                _uiState.update { it.copy(showAvatarDialog = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal memperbarui avatar", e)
            }
        }
    }

    // --- Catatan Belajar (Notes) 📝 ---
    fun setNoteCategory(category: String) {
        savedStateHandle[KEY_NOTE_CATEGORY] = category
        _uiState.update { it.copy(selectedNoteCategory = category) }
    }

    fun openNoteDialog(note: StudyNote? = null) {
        _uiState.update { it.copy(showNoteDialog = true, editingNote = note) }
    }

    fun closeNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, editingNote = null) }
    }

    // B-08: Cegah penyimpanan catatan kosong & pertahankan createdAt saat mengedit catatan lama
    fun saveNote(
        id: String?,
        title: String,
        content: String,
        category: String,
        nodeCode: String? = null,
        moduleId: String? = null
    ) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()
        if (cleanTitle.isEmpty() && cleanContent.isEmpty()) {
            closeNoteDialog()
            return
        }
        val finalTitle = cleanTitle.ifEmpty { "Catatan Tanpa Judul" }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val noteId = id ?: UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val existingNote = if (id != null) repository.getNoteById(id) else null
                val preservedCreatedAt = existingNote?.createdAt ?: now
                val resolvedNodeCode = nodeCode ?: existingNote?.nodeCode
                val resolvedMod = moduleId
                    ?: existingNote?.moduleId
                    ?: if (resolvedNodeCode?.contains(".") == true) resolvedNodeCode.substringBefore(".") else null
                val entity = StudyNoteEntity(
                    id = noteId,
                    title = finalTitle,
                    content = cleanContent,
                    category = category,
                    createdAt = preservedCreatedAt,
                    updatedAt = now,
                    nodeCode = resolvedNodeCode,
                    moduleId = resolvedMod
                )
                repository.insertNote(entity)
                closeNoteDialog()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menyimpan catatan", e)
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteNote(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menghapus catatan", e)
            }
        }
    }

    fun exportNotesToJson(): String {
        val currentState = _uiState.value
        val root = org.json.JSONObject()
        root.put("app", "Belajar MRI")
        root.put("exportedAt", LocalDate.now().toString())
        root.put("userName", currentState.userName.ifBlank { "Radiografer MRI" })
        root.put("totalNotes", currentState.notesList.size)
        val notesArray = org.json.JSONArray()
        currentState.notesList.forEach { note ->
            val item = org.json.JSONObject()
            item.put("id", note.id)
            item.put("title", note.title)
            item.put("category", note.category)
            item.put("content", note.content)
            item.put("dateString", note.dateString)
            notesArray.put(item)
        }
        root.put("notes", notesArray)
        return root.toString(2)
    }


    // --- Simpul Kurikulum & Progress ---
    fun openModuleDetail(module: MriModule) {
        val resolvedModule = _uiState.value.modulesList.find { it.id == module.id } ?: module
        savedStateHandle[KEY_SELECTED_MODULE_ID] = resolvedModule.id
        triggerSectionTransition("Modul ${resolvedModule.code}: ${resolvedModule.title}") {
            _uiState.update {
                it.copy(
                    selectedModule = resolvedModule,
                    nodeLevelFilter = 0,
                    onlyBookmarkedNodes = false,
                    isModuleFlashcardMode = false,
                    moduleFlashcardIndex = 0,
                    isModuleFlashcardFlipped = false,
                    moduleFlashcardShuffleSeed = 0,
                    currentFlashcardIndex = 0,
                    isFlashcardFlipped = false,
                    flashcardShuffleSeed = 0
                )
            }
        }
        ResumeNodeResolver.codeToRememberOnOpen(resolvedModule, _uiState.value.lastVisitedNodeCode)?.let {
            rememberLastVisited(it)
        }
    }

    fun closeModuleDetail() {
        savedStateHandle[KEY_SELECTED_MODULE_ID] = null
        triggerSectionTransition("Menu Beranda") {
            _uiState.update {
                it.copy(
                    selectedModule = null,
                    isModuleFlashcardMode = false,
                    currentFlashcardIndex = it.glossaryFlashcardIndex,
                    isFlashcardFlipped = it.isGlossaryFlashcardFlipped,
                    flashcardShuffleSeed = it.glossaryFlashcardShuffleSeed
                )
            }
        }
    }

    fun resumeStudy() {
        val targetNode = _uiState.value.lastVisitedNode
        val modId = ResumeNodeResolver.moduleIdOf(_uiState.value.modulesList, targetNode.code)
            ?: targetNode.code.substringBefore(".").ifBlank { "M01" }
        val targetModule = _uiState.value.modulesList.find { it.id == modId }
            ?: _uiState.value.modulesList.find { it.id == targetNode.code.substringBefore(".") }
            ?: _uiState.value.modulesList.first()
        openModuleDetail(targetModule)
    }

    fun setNodeLevelFilter(level: Int) {
        _uiState.update {
            it.copy(
                nodeLevelFilter = level,
                moduleFlashcardIndex = 0,
                isModuleFlashcardFlipped = false,
                currentFlashcardIndex = if (it.selectedModule != null) 0 else it.currentFlashcardIndex,
                isFlashcardFlipped = if (it.selectedModule != null) false else it.isFlashcardFlipped
            )
        }
    }

    fun setNodeMode(nodeCode: String, mode: String) {
        _uiState.update {
            it.copy(nodeModeMap = it.nodeModeMap + (nodeCode to mode))
        }
    }

    fun setGlobalNodeMode(module: MriModule, mode: String) {
        _uiState.update { current ->
            val updatedMap = current.nodeModeMap.toMutableMap()
            module.nodes.forEach { node ->
                updatedMap[node.code] = mode
            }
            current.copy(
                globalNodeMode = mode,
                nodeModeMap = updatedMap
            )
        }
    }

    fun openModuleQuiz(moduleId: String, level: Int = 0) {
        savedStateHandle[KEY_SELECTED_MODULE_ID] = null
        savedStateHandle[KEY_CURRENT_TAB] = MainTab.KUIS.name
        savedStateHandle[KEY_QUIZ_MOD_FILTER] = moduleId
        savedStateHandle[KEY_QUIZ_LEVEL_FILTER] = level
        _uiState.update {
            it.copy(
                selectedModule = null,
                currentTab = MainTab.KUIS,
                quizModuleFilter = moduleId,
                quizLevelFilter = level,
                currentQuizIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizAnswers = emptyMap(),
                answeredQuizMap = emptyMap(),
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    // B-05 & B-03: Gunakan transaksi database atomik untuk toggle selesai dan perhitungan jumlah hari ini
    fun toggleNodeCompletion(nodeCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val normalizedCode = LegacyNodeCodes.normalize(nodeCode)
                val resolvedModuleId = when {
                    normalizedCode.startsWith("M03A.") -> "M03A"
                    normalizedCode.startsWith("M03B.") -> "M03B"
                    normalizedCode.startsWith("M03C.") -> "M03C"
                    normalizedCode.contains(".") -> normalizedCode.substringBefore(".")
                    else -> ""
                }
                val todayIso = LocalDate.now().toString()
                val nowCompleted = repository.toggleNodeCompletionTransactional(
                    normalizedCode = normalizedCode,
                    resolvedModuleId = resolvedModuleId,
                    dateIso = todayIso
                )
                if (nowCompleted) {
                    rememberLastVisited(normalizedCode)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal mengubah status simpul: $nodeCode", e)
            }
        }
    }

    /**
     * Menandai simpul selesai secara satu-arah / idempoten untuk auto-complete simulator (NB-01).
     * Tidak pernah membatalkan simpul yang sudah selesai meskipun dipanggil berulang kali.
     */
    fun markNodeCompleted(nodeCode: String) {
        val normalizedCode = LegacyNodeCodes.normalize(nodeCode.trim())
        if (normalizedCode.isBlank()) return
        if (_uiState.value.completedNodeCodes.contains(normalizedCode)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newlyMarked = repository.markNodeCompletedIfNotYet(normalizedCode)
                if (newlyMarked) {
                    rememberLastVisited(normalizedCode)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menandai simpul selesai: $normalizedCode", e)
            }
        }
    }

    // --- Bookmark Materi & Glosarium 🔖 ---
    fun toggleNodeBookmark(nodeCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.toggleNodeBookmark(nodeCode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal mengubah markah simpul: $nodeCode", e)
            }
        }
    }

    fun removeNodeBookmark(nodeCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.removeNodeBookmark(nodeCode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menghapus markah simpul: $nodeCode", e)
            }
        }
    }

    fun toggleGlossaryBookmark(term: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.toggleGlossaryBookmark(term)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal mengubah markah glosarium: $term", e)
            }
        }
    }

    fun removeGlossaryBookmark(term: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.removeGlossaryBookmark(term)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menghapus markah glosarium: $term", e)
            }
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.clearAllBookmarks()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal mengosongkan semua markah", e)
            }
        }
    }

    fun toggleFlashcardBookmark(card: MriFlashcard) {
        if (card.isGlossaryCard) {
            toggleGlossaryBookmark(card.glossaryTermKey)
        } else {
            toggleNodeBookmark(card.nodeCode)
        }
    }

    fun setOnlyBookmarkedNodes(onlyBookmarked: Boolean) {
        _uiState.update {
            it.copy(
                onlyBookmarkedNodes = onlyBookmarked,
                moduleFlashcardIndex = 0,
                isModuleFlashcardFlipped = false,
                currentFlashcardIndex = if (it.selectedModule != null) 0 else it.currentFlashcardIndex,
                isFlashcardFlipped = if (it.selectedModule != null) false else it.isFlashcardFlipped
            )
        }
    }

    fun setOnlyBookmarkedGlossary(onlyBookmarked: Boolean) {
        _uiState.update {
            it.copy(
                onlyBookmarkedGlossary = onlyBookmarked,
                glossaryFlashcardIndex = 0,
                isGlossaryFlashcardFlipped = false,
                currentFlashcardIndex = if (it.selectedModule == null) 0 else it.currentFlashcardIndex,
                isFlashcardFlipped = if (it.selectedModule == null) false else it.isFlashcardFlipped
            )
        }
    }

    // --- Flashcards Hafalan Cepat 🗂️ (B-13: State terpisah untuk Modul vs Glosarium) ---
    fun setFlashcardMode(enabled: Boolean) {
        if (_uiState.value.selectedModule != null) {
            setModuleFlashcardMode(enabled)
        } else {
            setGlossaryFlashcardMode(enabled)
        }
    }

    fun setModuleFlashcardMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isModuleFlashcardMode = enabled,
                currentFlashcardIndex = it.moduleFlashcardIndex,
                isFlashcardFlipped = it.isModuleFlashcardFlipped,
                flashcardShuffleSeed = it.moduleFlashcardShuffleSeed
            )
        }
    }

    fun setGlossaryFlashcardMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isGlossaryFlashcardMode = enabled,
                currentFlashcardIndex = it.glossaryFlashcardIndex,
                isFlashcardFlipped = it.isGlossaryFlashcardFlipped,
                flashcardShuffleSeed = it.glossaryFlashcardShuffleSeed
            )
        }
    }

    private fun isModuleDeckActive(state: MriUiState): Boolean =
        state.selectedModule != null && state.isModuleFlashcardMode

    fun flipFlashcard() {
        _uiState.update { state ->
            if (isModuleDeckActive(state)) {
                val newFlipped = !state.isModuleFlashcardFlipped
                state.copy(
                    isModuleFlashcardFlipped = newFlipped,
                    isFlashcardFlipped = newFlipped
                )
            } else {
                val newFlipped = !state.isGlossaryFlashcardFlipped
                state.copy(
                    isGlossaryFlashcardFlipped = newFlipped,
                    isFlashcardFlipped = newFlipped
                )
            }
        }
    }

    fun nextFlashcard(deckSize: Int) {
        if (deckSize <= 0) return
        _uiState.update { state ->
            if (isModuleDeckActive(state)) {
                val next = (state.moduleFlashcardIndex + 1) % deckSize
                state.copy(
                    moduleFlashcardIndex = next,
                    isModuleFlashcardFlipped = false,
                    currentFlashcardIndex = next,
                    isFlashcardFlipped = false
                )
            } else {
                val next = (state.glossaryFlashcardIndex + 1) % deckSize
                state.copy(
                    glossaryFlashcardIndex = next,
                    isGlossaryFlashcardFlipped = false,
                    currentFlashcardIndex = next,
                    isFlashcardFlipped = false
                )
            }
        }
    }

    fun prevFlashcard(deckSize: Int) {
        if (deckSize <= 0) return
        _uiState.update { state ->
            if (isModuleDeckActive(state)) {
                val prev = if (state.moduleFlashcardIndex - 1 < 0) deckSize - 1 else state.moduleFlashcardIndex - 1
                state.copy(
                    moduleFlashcardIndex = prev,
                    isModuleFlashcardFlipped = false,
                    currentFlashcardIndex = prev,
                    isFlashcardFlipped = false
                )
            } else {
                val prev = if (state.glossaryFlashcardIndex - 1 < 0) deckSize - 1 else state.glossaryFlashcardIndex - 1
                state.copy(
                    glossaryFlashcardIndex = prev,
                    isGlossaryFlashcardFlipped = false,
                    currentFlashcardIndex = prev,
                    isFlashcardFlipped = false
                )
            }
        }
    }

    fun shuffleFlashcards() {
        _uiState.update { state ->
            if (isModuleDeckActive(state)) {
                val nextSeed = state.moduleFlashcardShuffleSeed + 1
                state.copy(
                    moduleFlashcardShuffleSeed = nextSeed,
                    moduleFlashcardIndex = 0,
                    isModuleFlashcardFlipped = false,
                    flashcardShuffleSeed = nextSeed,
                    currentFlashcardIndex = 0,
                    isFlashcardFlipped = false
                )
            } else {
                val nextSeed = state.glossaryFlashcardShuffleSeed + 1
                state.copy(
                    glossaryFlashcardShuffleSeed = nextSeed,
                    glossaryFlashcardIndex = 0,
                    isGlossaryFlashcardFlipped = false,
                    flashcardShuffleSeed = nextSeed,
                    currentFlashcardIndex = 0,
                    isFlashcardFlipped = false
                )
            }
        }
    }

    // B-03, FINAL-02 & P3: Catat aktivitas belajar dengan penanda flashcardActivity = true saat kartu ditandai dikuasai
    fun toggleFlashcardMastered(cardId: String) {
        val isNowMastered = !_uiState.value.masteredFlashcardIds.contains(cardId)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.toggleFlashcardMastered(cardId)
                if (isNowMastered) {
                    val todayIso = LocalDate.now().toString()
                    repository.recordStudyActivity(
                        dateIso = todayIso,
                        flashcardActivity = true
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal mengubah status flashcard: $cardId", e)
            }
        }
    }

    // --- Animasi Belajar ---
    fun selectAnimation(index: Int) {
        _uiState.update { it.copy(selectedAnimationIndex = index, animationStep = 1) }
    }

    fun toggleAnimationPlay() {
        _uiState.update { it.copy(isAnimationPlaying = !it.isAnimationPlaying) }
    }

    fun setAnimationStep(step: Int) {
        _uiState.update { it.copy(animationStep = step.coerceIn(1, 4)) }
    }

    // --- Simulator ---
    fun selectSimType(simType: SimType) {
        val simName = when (simType) {
            SimType.SPIN_LARMOR -> "Simulator Presesi Larmor"
            SimType.KONTRAS_TR_TE -> "Simulator Kontras TR/TE"
            SimType.RUANG_K -> "Simulator Domain Ruang-k"
            SimType.PESAWAT_MAGNET_M05 -> "Laboratorium Anatomi Pesawat"
            SimType.GRADIEN_RF_M06 -> "Laboratorium Gradien & RF"
            SimType.SEKUENS_KONTRAS_M03 -> "Laboratorium Sekuens & Kontras"
            SimType.SAR_SAFETY -> "Simulator Keselamatan & SAR"
            SimType.ARTEFAK_QA_M08 -> "Laboratorium Artefak & QA"
            SimType.SCAN_TIME_ESTIMATOR -> "Kalkulator Waktu Pindai"
            SimType.PROTOKOL_TA_SNR -> "Kalkulator Protokol TA & SNR"
            SimType.KONSOL_VENDOR -> "Peta Konsol Vendor MRI"
        }
        savedStateHandle[KEY_CURRENT_TAB] = MainTab.SIMULASI.name
        savedStateHandle[KEY_SELECTED_MODULE_ID] = null
        savedStateHandle[KEY_SELECTED_SIM_TYPE] = simType.name
        triggerSectionTransition(simName) {
            _uiState.update {
                it.copy(
                    currentTab = MainTab.SIMULASI,
                    selectedModule = null,
                    selectedSimType = simType
                )
            }
        }
    }

    fun selectTissue(tissue: TissueProperties) {
        _uiState.update { it.copy(selectedTissue = tissue) }
    }

    fun setRelaxationTime(timeMs: Float) {
        _uiState.update { it.copy(relaxationTimeMs = timeMs.coerceIn(0f, 3000f)) }
    }

    fun resetPulse90() {
        _uiState.update { it.copy(relaxationTimeMs = 0f) }
    }

    fun toggleRelaxationPlay() {
        _uiState.update { it.copy(isRelaxationPlaying = !it.isRelaxationPlaying) }
    }

    fun setTr(tr: Float) {
        _uiState.update { it.copy(trMs = tr) }
    }

    fun setTe(te: Float) {
        _uiState.update { it.copy(teMs = te) }
    }

    fun setB0(b0: Float) {
        _uiState.update { it.copy(selectedB0 = b0) }
    }

    fun setNucleus(name: String, gamma: Float) {
        _uiState.update { it.copy(selectedNucleusName = name, selectedNucleusGamma = gamma) }
    }

    // B-15: Preset T1w Otak memilih Substansia Alba (wm); Preset T2w Otak memilih CSF (csf) karena cairan serebrospinal
    // memiliki T2 sangat panjang sehingga tampak hiperintens terang pada T2w; Preset PDw memilih Substansia Grisea (gm).
    fun applyPresetT1Brain() {
        setTr(500f)
        setTe(20f)
        setRelaxationTime(500f)
        selectTissue(MriRepository.tissues.find { it.id == "wm" } ?: MriRepository.tissues[0])
    }

    fun applyPresetT2Brain() {
        setTr(2500f)
        setTe(100f)
        setRelaxationTime(950f)
        selectTissue(MriRepository.tissues.find { it.id == "csf" } ?: MriRepository.tissues[0])
    }

    fun applyPresetPdwJoint() {
        setTr(2500f)
        setTe(20f)
        setRelaxationTime(600f)
        selectTissue(MriRepository.tissues.find { it.id == "gm" } ?: MriRepository.tissues[0])
    }

    fun setPatientWeight(weight: Float) {
        _uiState.update { it.copy(patientWeightKg = weight) }
    }

    fun setTseSequence(isTse: Boolean) {
        _uiState.update { it.copy(isTseSequence = isTse) }
    }

    fun setMatrixNy(ny: Int) {
        _uiState.update { it.copy(matrixNy = ny) }
    }

    fun setNexCount(nex: Int) {
        _uiState.update { it.copy(nexCount = nex) }
    }

    fun setEtlCount(etl: Int) {
        _uiState.update { it.copy(etlCount = etl) }
    }

    // --- Kuis (B-02: Kunci jawaban per soal, cegah jawab ulang & skor melampaui jumlah soal) ---
    fun setQuizModuleFilter(moduleId: String) {
        savedStateHandle[KEY_QUIZ_MOD_FILTER] = moduleId
        _uiState.update {
            it.copy(
                quizModuleFilter = moduleId,
                currentQuizIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizAnswers = emptyMap(),
                answeredQuizMap = emptyMap(),
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    fun setQuizLevelFilter(level: Int) {
        savedStateHandle[KEY_QUIZ_LEVEL_FILTER] = level
        _uiState.update {
            it.copy(
                quizLevelFilter = level,
                currentQuizIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizAnswers = emptyMap(),
                answeredQuizMap = emptyMap(),
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    fun selectQuizOption(optionIndex: Int) {
        _uiState.update { state ->
            val currentQuiz = state.filteredQuizzes.getOrNull(state.currentQuizIndex)
            val alreadyAnswered = (currentQuiz != null && state.quizAnswers.containsKey(currentQuiz.id)) ||
                state.answeredQuizMap.containsKey(state.currentQuizIndex)
            if (state.isAnswerSubmitted || alreadyAnswered) {
                state
            } else {
                state.copy(selectedOptionIndex = optionIndex)
            }
        }
    }

    // B-02 & N-05: Evaluasi jawaban kuis tanpa efek samping di dalam lambda _uiState.update
    fun submitQuizAnswer() {
        val snapshot = _uiState.value
        val selected = snapshot.selectedOptionIndex ?: return
        val quizzes = snapshot.filteredQuizzes
        val qIdx = snapshot.currentQuizIndex
        val quiz = quizzes.getOrNull(qIdx) ?: return
        if (snapshot.isAnswerSubmitted || snapshot.quizAnswers.containsKey(quiz.id) || snapshot.answeredQuizMap.containsKey(qIdx)) {
            return
        }

        val wasCorrect = (selected == quiz.correctIndex)

        _uiState.update { state ->
            if (state.isAnswerSubmitted || state.quizAnswers.containsKey(quiz.id) || state.answeredQuizMap.containsKey(qIdx)) {
                return@update state
            }
            val updatedAnswers = state.quizAnswers + (quiz.id to selected)
            val updatedIndexMap = state.answeredQuizMap + (qIdx to selected)
            val newScore = quizzes.count { q -> updatedAnswers[q.id] == q.correctIndex }
            state.copy(
                isAnswerSubmitted = true,
                quizAnswers = updatedAnswers,
                answeredQuizMap = updatedIndexMap,
                quizScore = newScore
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertQuizAttempt(
                    QuizAttemptEntity(
                        quizId = quiz.id,
                        nodeCode = quiz.nodeCode,
                        moduleId = quiz.moduleId,
                        selectedIndex = selected,
                        isCorrect = wasCorrect,
                        answeredAt = System.currentTimeMillis()
                    )
                )
                val todayIso = LocalDate.now().toString()
                repository.recordStudyActivity(
                    dateIso = todayIso,
                    lastNodeCode = quiz.nodeCode,
                    lastModuleId = quiz.moduleId
                )
                rememberLastVisited(quiz.nodeCode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MriViewModel", "gagal menyimpan hasil kuis", e)
            }
        }
    }

    fun prevQuizQuestion() {
        _uiState.update { state ->
            val prevIndex = state.currentQuizIndex - 1
            if (prevIndex < 0) return@update state
            val prevQuiz = state.filteredQuizzes.getOrNull(prevIndex)
            val savedSelection = prevQuiz?.let { state.quizAnswers[it.id] } ?: state.answeredQuizMap[prevIndex]
            state.copy(
                currentQuizIndex = prevIndex,
                selectedOptionIndex = savedSelection,
                isAnswerSubmitted = savedSelection != null
            )
        }
    }

    fun nextQuizQuestion() {
        _uiState.update { state ->
            val quizzes = state.filteredQuizzes
            val nextIndex = state.currentQuizIndex + 1
            if (nextIndex < quizzes.size) {
                val nextQuiz = quizzes.getOrNull(nextIndex)
                val savedSelection = nextQuiz?.let { state.quizAnswers[it.id] } ?: state.answeredQuizMap[nextIndex]
                state.copy(
                    currentQuizIndex = nextIndex,
                    selectedOptionIndex = savedSelection,
                    isAnswerSubmitted = savedSelection != null
                )
            } else {
                state.copy(isQuizFinished = true)
            }
        }
    }

    fun restartQuiz() {
        _uiState.update {
            it.copy(
                currentQuizIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizAnswers = emptyMap(),
                answeredQuizMap = emptyMap(),
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    // --- Glosarium ---
    fun setGlossarySearch(query: String) {
        _uiState.update {
            it.copy(
                glossarySearchQuery = query,
                glossaryFlashcardIndex = 0,
                isGlossaryFlashcardFlipped = false,
                currentFlashcardIndex = if (it.selectedModule == null) 0 else it.currentFlashcardIndex,
                isFlashcardFlipped = if (it.selectedModule == null) false else it.isFlashcardFlipped
            )
        }
    }

    fun setGlossaryModuleFilter(modId: String) {
        _uiState.update {
            it.copy(
                glossaryModuleFilter = modId,
                glossaryFlashcardIndex = 0,
                isGlossaryFlashcardFlipped = false,
                currentFlashcardIndex = if (it.selectedModule == null) 0 else it.currentFlashcardIndex,
                isFlashcardFlipped = if (it.selectedModule == null) false else it.isFlashcardFlipped
            )
        }
    }

    fun setInfoDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showInfoDialog = visible) }
    }
}
