package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.LegacyNodeCodes
import com.example.data.MriRepository
import com.example.data.local.MriDatabase
import com.example.data.local.NodeProgressEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyDayEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.normalizeLegacyNodeCodes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LegacyNodeCodesTest {
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
    fun mapTargetsExistInCurriculum() {
        val allNodeCodesInCurriculum = MriRepository.modules
            .flatMap { it.nodes }
            .map { it.code }
            .toSet()

        LegacyNodeCodes.MAP.values.forEach { targetCode ->
            assertTrue(
                "Target code $targetCode should exist in MriRepository curriculum nodes",
                allNodeCodesInCurriculum.contains(targetCode)
            )
        }
    }

    @Test
    fun mapKeysDoNotExistInCurriculum() {
        val allNodeCodesInCurriculum = MriRepository.modules
            .flatMap { it.nodes }
            .map { it.code }
            .toSet()

        LegacyNodeCodes.MAP.keys.forEach { legacyCode ->
            assertFalse(
                "Legacy key code $legacyCode should NOT exist in MriRepository curriculum nodes",
                allNodeCodesInCurriculum.contains(legacyCode)
            )
        }
    }

    @Test
    fun migratesNodeProgress() = runBlocking {
        val dao = db.nodeProgressDao()
        dao.insert(NodeProgressEntity(nodeCode = "M03.T1.1", completedAt = 1000L, moduleId = "M03"))

        db.normalizeLegacyNodeCodes()

        val allProgress = dao.getAll()
        assertEquals(1, allProgress.size)
        val migrated = allProgress[0]
        assertEquals("M03A.T1.1", migrated.nodeCode)
        assertEquals("M03A", migrated.moduleId)
        assertEquals(1000L, migrated.completedAt)
    }

    @Test
    fun collisionKeepsNewRow() = runBlocking {
        val dao = db.nodeProgressDao()
        dao.insert(NodeProgressEntity(nodeCode = "M03.T1.1", completedAt = 1000L, moduleId = "M03"))
        dao.insert(NodeProgressEntity(nodeCode = "M03A.T1.1", completedAt = 2000L, moduleId = "M03A"))

        db.normalizeLegacyNodeCodes()

        val allProgress = dao.getAll()
        assertEquals(1, allProgress.size)
        val remaining = allProgress[0]
        assertEquals("M03A.T1.1", remaining.nodeCode)
        assertEquals("M03A", remaining.moduleId)
        assertEquals(2000L, remaining.completedAt)
    }

    @Test
    fun migratesOtherTables() = runBlocking {
        val quizDao = db.quizAttemptDao()
        val noteDao = db.studyNoteDao()
        val dayDao = db.studyDayDao()

        quizDao.insert(
            QuizAttemptEntity(
                quizId = "Q1",
                nodeCode = "M03.T1.2",
                moduleId = "M03",
                selectedIndex = 0,
                isCorrect = true,
                answeredAt = 500L
            )
        )

        noteDao.insert(
            StudyNoteEntity(
                id = "N1",
                title = "Note 1",
                content = "Content 1",
                category = "General",
                createdAt = 500L,
                updatedAt = 500L,
                nodeCode = "M03.T2.1",
                moduleId = "M03"
            )
        )

        dayDao.insert(
            StudyDayEntity(
                dateIso = "2026-11-11",
                nodesCompletedCount = 1,
                lastNodeCode = "M03.T3.1",
                lastModuleId = "M03"
            )
        )

        db.normalizeLegacyNodeCodes()

        val quizAttempts = quizDao.observeQuizAttempts().first()
        assertEquals(1, quizAttempts.size)
        assertEquals("M03A.T1.3", quizAttempts[0].nodeCode)

        val notes = noteDao.observeNotes().first()
        assertEquals(1, notes.size)
        assertEquals("M03B.T1.1", notes[0].nodeCode)

        val day = dayDao.getByDate("2026-11-11")
        assertEquals("M03C.T1.1", day?.lastNodeCode)
    }

    @Test
    fun idempotentAndLeavesOthersAlone() = runBlocking {
        val progressDao = db.nodeProgressDao()
        progressDao.insert(NodeProgressEntity(nodeCode = "M01.T1.1", completedAt = 500L, moduleId = "M01"))
        progressDao.insert(NodeProgressEntity(nodeCode = "M03.T1.1", completedAt = 1000L, moduleId = "M03"))

        db.normalizeLegacyNodeCodes()
        db.normalizeLegacyNodeCodes() // Run twice

        val allProgress = progressDao.getAll().associateBy { it.nodeCode }
        assertEquals(2, allProgress.size)
        assertTrue(allProgress.containsKey("M01.T1.1"))
        assertTrue(allProgress.containsKey("M03A.T1.1"))
        assertFalse(allProgress.containsKey("M03.T1.1"))
    }

    @Test
    fun normalizeAllSet() {
        val input = setOf("M03.T1.1", "M01.T1.1")
        val expected = setOf("M03A.T1.1", "M01.T1.1")
        assertEquals(expected, LegacyNodeCodes.normalizeAll(input))
    }
}
