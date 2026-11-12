package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MriRepository
import com.example.data.local.CurriculumNodeEntity
import com.example.data.local.CurriculumQuizEntity
import com.example.data.local.MriDatabase
import com.example.data.local.NodeProgressEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyDayEntity
import com.example.data.local.StudyNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CurriculumSeedReconcileTest {
    private lateinit var db: MriDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MriDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun seedCreatesExactlyTheSourceData() = runBlocking {
        db.seedCurriculumIfNeeded()

        val expectedModulesCount = MriRepository.toMriModuleEntities().size
        val expectedCurrNodesCount = MriRepository.toCurriculumNodeEntities().size
        val expectedCurrQuizzesCount = MriRepository.toCurriculumQuizEntities().size

        assertEquals(expectedModulesCount, db.mriCurriculumDao().getModulesCount())
        assertEquals(expectedCurrNodesCount, db.curriculumDao().getNodesCount())
        assertEquals(expectedCurrQuizzesCount, db.curriculumDao().getQuizzesCount())
    }

    @Test
    fun seedRemovesGhostRows() = runBlocking {
        db.seedCurriculumIfNeeded()

        // Sisipkan ghost rows
        db.curriculumDao().insertNode(
            CurriculumNodeEntity("ZZ99.GHOST", "M01", 1, "Ghost Node", "", "", "")
        )
        db.curriculumDao().insertQuiz(
            CurriculumQuizEntity("ZZ99_Q1", "M01", "ZZ99.GHOST", 1, "Ghost?", "[]", 0, "")
        )

        // Seed ulang
        db.seedCurriculumIfNeeded()

        // Pastikan tidak ada baris ghost
        val currNodes = db.curriculumDao().observeAllNodes().first()
        assertTrue(currNodes.none { it.code == "ZZ99.GHOST" })

        val currQuizzes = db.curriculumDao().observeAllQuizzes().first()
        assertTrue(currQuizzes.none { it.id == "ZZ99_Q1" })

        // Pastikan jumlah kembali sama seperti sumber
        assertEquals(MriRepository.toCurriculumNodeEntities().size, db.curriculumDao().getNodesCount())
        assertEquals(MriRepository.toCurriculumQuizEntities().size, db.curriculumDao().getQuizzesCount())
    }

    @Test
    fun seedIsIdempotent() = runBlocking {
        db.seedCurriculumIfNeeded()
        db.seedCurriculumIfNeeded()
        db.seedCurriculumIfNeeded()

        assertEquals(MriRepository.toMriModuleEntities().size, db.mriCurriculumDao().getModulesCount())
        assertEquals(MriRepository.toCurriculumNodeEntities().size, db.curriculumDao().getNodesCount())
        assertEquals(MriRepository.toCurriculumQuizEntities().size, db.curriculumDao().getQuizzesCount())
    }

    @Test
    fun seedDoesNotTouchUserData() = runBlocking {
        // Sisipkan data pengguna
        db.nodeProgressDao().insert(NodeProgressEntity("M01.T1.1", 1000L, "M01"))
        db.studyNoteDao().insert(StudyNoteEntity("note_1", "Judul", "Isi", "Fisika", 1000L, 1000L))
        db.quizAttemptDao().insert(QuizAttemptEntity(1L, "M01_Q1", "M01.T1.1", "M01", 0, true, 1000L))
        db.studyDayDao().insert(StudyDayEntity("2026-11-11", 1, "M01.T1.1", "M01"))

        // Seed 2x
        db.seedCurriculumIfNeeded()
        db.seedCurriculumIfNeeded()

        // Verifikasi keempat baris data pengguna masih ada dan isinya sama
        val progress = db.nodeProgressDao().getByNodeCode("M01.T1.1")
        assertNotNull(progress)
        assertEquals("M01.T1.1", progress?.nodeCode)

        val notes = db.studyNoteDao().getAll()
        assertEquals(1, notes.size)
        assertEquals("note_1", notes[0].id)
        assertEquals("Judul", notes[0].title)

        val attempts = db.quizAttemptDao().getAll()
        assertEquals(1, attempts.size)
        assertEquals("M01_Q1", attempts[0].quizId)

        val days = db.studyDayDao().getAll()
        assertEquals(1, days.size)
        assertEquals("2026-11-11", days[0].dateIso)
    }
}
