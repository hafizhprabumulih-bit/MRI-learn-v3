package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CurriculumSnapshot
import com.example.data.MriRepository
import com.example.data.local.MriDatabase
import com.example.data.local.NodeProgressEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyDayEntity
import com.example.data.local.StudyNoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CurriculumSeedGuardTest {
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
    fun fingerprintDeterministic() {
        val f1 = CurriculumSnapshot.build().fingerprint()
        val f2 = CurriculumSnapshot.build().fingerprint()
        assertEquals(f1, f2)
        assertTrue(f1.isNotBlank())
    }

    @Test
    fun fingerprintDetectsContentChange() {
        val snapshot1 = CurriculumSnapshot.build()
        val modifiedNodes = snapshot1.curriculumNodes.mapIndexed { idx, node ->
            if (idx == 0) node.copy(title = node.title + " Modified") else node
        }
        val snapshot2 = CurriculumSnapshot(
            modules = snapshot1.modules,
            curriculumNodes = modifiedNodes,
            curriculumQuizzes = snapshot1.curriculumQuizzes
        )
        assertFalse(snapshot1.fingerprint() == snapshot2.fingerprint())
    }

    @Test
    fun replaceIsIdempotent() = runBlocking {
        val snapshot = CurriculumSnapshot.build()
        db.replaceCurriculum(snapshot)
        db.replaceCurriculum(snapshot)

        assertEquals(MriRepository.toMriModuleEntities().size, db.mriCurriculumDao().getModulesCount())
        assertEquals(MriRepository.toCurriculumNodeEntities().size, db.curriculumDao().getNodesCount())
        assertEquals(MriRepository.toCurriculumQuizEntities().size, db.curriculumDao().getQuizzesCount())
        assertEquals(0, db.mriCurriculumDao().getNodesCount())
        assertEquals(0, db.mriCurriculumDao().getQuizQuestionsCount())
    }

    @Test
    fun presentCheck() = runBlocking {
        val snapshot = CurriculumSnapshot.build()
        db.replaceCurriculum(snapshot)
        assertTrue(db.isCurriculumPresent(snapshot))

        db.curriculumDao().clearNodes()
        assertFalse(db.isCurriculumPresent(snapshot))
    }

    @Test
    fun userDataUntouched() = runBlocking {
        db.nodeProgressDao().insert(NodeProgressEntity("M01.T1.1", 1000L, "M01"))
        db.studyNoteDao().insert(StudyNoteEntity("note_1", "Judul", "Isi", "Fisika", 1000L, 1000L))
        db.quizAttemptDao().insert(QuizAttemptEntity(1L, "M01_Q1", "M01.T1.1", "M01", 0, true, 1000L))
        db.studyDayDao().insert(StudyDayEntity("2026-11-11", 1, "M01.T1.1", "M01"))

        val snapshot = CurriculumSnapshot.build()
        db.replaceCurriculum(snapshot)
        db.replaceCurriculum(snapshot)

        val progress = db.nodeProgressDao().getByNodeCode("M01.T1.1")
        assertNotNull(progress)
        assertEquals("M01.T1.1", progress?.nodeCode)

        val notes = db.studyNoteDao().getAll()
        assertEquals(1, notes.size)
        assertEquals("note_1", notes[0].id)

        val attempts = db.quizAttemptDao().getAll()
        assertEquals(1, attempts.size)
        assertEquals("M01_Q1", attempts[0].quizId)

        val days = db.studyDayDao().getAll()
        assertEquals(1, days.size)
        assertEquals("2026-11-11", days[0].dateIso)
    }
}
