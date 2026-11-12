package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MriDatabase
import com.example.data.local.NodeProgressEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyDayEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.toDomainModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDaoTest {
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
    fun testStudyNoteDaoCrud() = runBlocking {
        val dao = db.studyNoteDao()
        val note = StudyNoteEntity(
            id = "note_test_1",
            title = "Test Note",
            content = "Testing Room note content",
            category = "Fisika",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insert(note)
        val notes = dao.observeNotes().first()
        assertEquals(1, notes.size)
        assertEquals("Test Note", notes[0].title)

        dao.delete("note_test_1")
        val notesAfterDelete = dao.observeNotes().first()
        assertTrue(notesAfterDelete.isEmpty())
    }

    @Test
    fun testNodeProgressDao() = runBlocking {
        val dao = db.nodeProgressDao()
        val progress = NodeProgressEntity(nodeCode = "M02.T1.1", completedAt = System.currentTimeMillis())
        dao.insert(progress)

        val completed = dao.observeCompletedNodes().first()
        assertEquals(1, completed.size)
        assertEquals("M02.T1.1", completed[0].nodeCode)

        dao.delete("M02.T1.1")
        val completedAfter = dao.observeCompletedNodes().first()
        assertTrue(completedAfter.isEmpty())
    }

    @Test
    fun testQuizAttemptDao() = runBlocking {
        val dao = db.quizAttemptDao()
        val attempt = QuizAttemptEntity(
            quizId = "M02_Q1",
            nodeCode = "M02.T1.2",
            moduleId = "M02",
            selectedIndex = 1,
            isCorrect = true,
            answeredAt = System.currentTimeMillis()
        )
        dao.insert(attempt)
        val attempts = dao.observeQuizAttempts().first()
        assertEquals(1, attempts.size)
        assertEquals("M02_Q1", attempts[0].quizId)
        assertTrue(attempts[0].isCorrect)
    }

    @Test
    fun testMriCurriculumDaoSeedingAndModule2Integration() = runBlocking {
        db.seedCurriculumIfNeeded()
        val curriculumDao = db.curriculumDao()

        val m02Nodes = curriculumDao.observeNodesByModule("M02").first()
        val nodeCodes = m02Nodes.map { it.code }.toSet()
        assertTrue(nodeCodes.contains("M02.T1.1"))
        assertTrue(nodeCodes.contains("M02.T1.2"))
        assertTrue(nodeCodes.contains("M02.T2.1"))
        assertTrue(nodeCodes.contains("M02.T2.2"))

        val t1Node = m02Nodes.first { it.code == "M02.T1.2" }
        assertTrue(t1Node.detailFull.contains("Mz(t) = M0 · (1 - e^(-t / T1))"))
        assertTrue(t1Node.summaryShort.contains("63%"))

        val t2Node = m02Nodes.first { it.code == "M02.T2.1" }
        assertTrue(t2Node.detailFull.contains("Mxy(t) = Mxy0 · e^(-t / T2)"))
        assertTrue(t2Node.summaryShort.contains("37%"))

        val level1Nodes = curriculumDao.observeNodesByModuleAndLevel("M02", 1).first()
        assertTrue(level1Nodes.all { it.level == 1 && it.moduleId == "M02" })

        val m02Quizzes = curriculumDao.observeQuizzesByModule("M02").first()
        assertEquals(5, m02Quizzes.size)
        val firstDomainQuiz = m02Quizzes.first { it.id == "M02_Q1" }.toDomainModel()
        assertEquals(4, firstDomainQuiz.options.size)
        assertEquals(1, firstDomainQuiz.correctIndex)
    }

    @Test
    fun testCurriculumDaoModule1NodesAndThreeLevelQuizzes() = runBlocking {
        db.seedCurriculumIfNeeded()
        val dao = db.curriculumDao()

        val m01Nodes = dao.observeNodesByModule("M01").first()
        assertEquals(23, m01Nodes.size)

        val expectedCodes = listOf(
            "M01.T0",
            "M01.T1.1", "M01.T1.R", "M01.T1.2", "M01.T1.3", "M01.T1.4A", "M01.T1.4B", "M01.T1.5", "M01.T1.6", "M01.T1.7",
            "M01.T2.R", "M01.T2.1A", "M01.T2.1B", "M01.T2.2A", "M01.T2.2B", "M01.T2.3A", "M01.T2.3B", "M01.T2.4",
            "M01.T3.1", "M01.T3.2", "M01.T3.3", "M01.T3.4", "M01.T3.5"
        )
        val actualCodes = m01Nodes.map { it.code }.toSet()
        expectedCodes.forEach { code ->
            assertTrue("Expected node code $code in curriculum_nodes", actualCodes.contains(code))
        }

        val level0Nodes = dao.observeNodesByModuleAndLevel("M01", 0).first()
        assertEquals(1, level0Nodes.size)
        val level1Nodes = dao.observeNodesByModuleAndLevel("M01", 1).first()
        assertEquals(9, level1Nodes.size)
        val level2Nodes = dao.observeNodesByModuleAndLevel("M01", 2).first()
        assertEquals(8, level2Nodes.size)
        val level3Nodes = dao.observeNodesByModuleAndLevel("M01", 3).first()
        assertEquals(5, level3Nodes.size)

        val m01Quizzes = dao.observeQuizzesByModule("M01").first()
        assertEquals(12, m01Quizzes.size)
        assertEquals(5, dao.observeQuizzesByModuleAndLevel("M01", 1).first().size)
        assertEquals(4, dao.observeQuizzesByModuleAndLevel("M01", 2).first().size)
        assertEquals(3, dao.observeQuizzesByModuleAndLevel("M01", 3).first().size)
    }

    @Test
    fun testCurriculumDaoModule9NodesAndQuizzes() = runBlocking {
        db.seedCurriculumIfNeeded()
        val dao = db.curriculumDao()

        val m09Nodes = dao.observeNodesByModule("M09").first()
        assertEquals(12, m09Nodes.size)

        val expectedM09Codes = listOf(
            "M09.T1.1", "M09.T1.2", "M09.T1.3", "M09.T1.4",
            "M09.T2.1", "M09.T2.2", "M09.T2.3", "M09.T2.4", "M09.T2.5",
            "M09.T3.1", "M09.T3.2", "M09.T3.3"
        )
        val actualCodes = m09Nodes.map { it.code }.toSet()
        expectedM09Codes.forEach { code ->
            assertTrue("Expected node code $code in M09", actualCodes.contains(code))
        }

        assertEquals(4, dao.observeNodesByModuleAndLevel("M09", 1).first().size)
        assertEquals(5, dao.observeNodesByModuleAndLevel("M09", 2).first().size)
        assertEquals(3, dao.observeNodesByModuleAndLevel("M09", 3).first().size)

        assertEquals("ta_snr", m09Nodes.first { it.code == "M09.T1.1" }.labType)
        assertEquals("console_tabs", m09Nodes.first { it.code == "M09.T2.4" }.labType)
        assertEquals("console_tabs", m09Nodes.first { it.code == "M09.T3.3" }.labType)

        val m09Quizzes = dao.observeQuizzesByModule("M09").first()
        assertEquals(14, m09Quizzes.size)
        assertEquals(5, dao.observeQuizzesByModuleAndLevel("M09", 1).first().size)
        assertEquals(5, dao.observeQuizzesByModuleAndLevel("M09", 2).first().size)
        assertEquals(4, dao.observeQuizzesByModuleAndLevel("M09", 3).first().size)
    }

    @Test
    fun testCurriculumDaoModule4NodesAndQuizzes() = runBlocking {
        db.seedCurriculumIfNeeded()
        val dao = db.curriculumDao()

        val m04aNodes = dao.observeNodesByModule("M04A").first()
        val m04bNodes = dao.observeNodesByModule("M04B").first()
        val m04cNodes = dao.observeNodesByModule("M04C").first()

        assertEquals(7, m04aNodes.size)
        assertEquals(14, m04bNodes.size)
        assertEquals(8, m04cNodes.size)

        val m04aQuizzes = dao.observeQuizzesByModule("M04A").first()
        val m04bQuizzes = dao.observeQuizzesByModule("M04B").first()
        val m04cQuizzes = dao.observeQuizzesByModule("M04C").first()

        assertEquals(6, m04aQuizzes.size)
        assertEquals(7, m04bQuizzes.size)
        assertEquals(6, m04cQuizzes.size)
    }

    @Test
    fun testModule5RoomDatabaseIntegration() = runBlocking {
        db.seedCurriculumIfNeeded()
        val mriDao = db.mriCurriculumDao()
        val curriculumDao = db.curriculumDao()

        val allModules = mriDao.observeAllModules().first()
        assertTrue(allModules.any { it.id == "M05" && it.title.contains("Anatomi Pesawat") })

        val m05Nodes = curriculumDao.observeNodesByModule("M05").first()
        assertEquals(12, m05Nodes.size)

        val expectedM05Codes = listOf(
            "M05.T1.1", "M05.T1.2", "M05.T1.3", "M05.T1.4",
            "M05.T2.1", "M05.T2.2", "M05.T2.3", "M05.T2.4", "M05.T2.5", "M05.T2.6",
            "M05.T3.1", "M05.T3.2"
        )
        val actualM05Codes = m05Nodes.map { it.code }.toSet()
        expectedM05Codes.forEach { code ->
            assertTrue("Expected M05 node code $code in curriculum_nodes", actualM05Codes.contains(code))
        }

        assertEquals(4, curriculumDao.observeNodesByModuleAndLevel("M05", 1).first().size)
        assertEquals(6, curriculumDao.observeNodesByModuleAndLevel("M05", 2).first().size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M05", 3).first().size)

        val t11 = m05Nodes.first { it.code == "M05.T1.1" }
        assertEquals("m5_a1_penampang", t11.labType)

        val t21 = m05Nodes.first { it.code == "M05.T2.1" }
        assertEquals("m5_a3_superkonduktif", t21.labType)

        val t26 = m05Nodes.first { it.code == "M05.T2.6" }
        assertEquals("m5_a7_quench", t26.labType)

        val m05Quizzes = curriculumDao.observeQuizzesByModule("M05").first()
        assertEquals(19, m05Quizzes.size)
        assertEquals(6, curriculumDao.observeQuizzesByModuleAndLevel("M05", 1).first().size)
        assertEquals(8, curriculumDao.observeQuizzesByModuleAndLevel("M05", 2).first().size)
        assertEquals(5, curriculumDao.observeQuizzesByModuleAndLevel("M05", 3).first().size)

        val q1 = m05Quizzes.first { it.id == "M05_Q1" }
        assertEquals(0, q1.correctIndex)

        val currM05Quizzes = curriculumDao.observeQuizzesByModule("M05").first()
        assertEquals(19, currM05Quizzes.size)
    }

    @Test
    fun testModule6AAnd6BRoomDatabaseIntegration() = runBlocking {
        db.seedCurriculumIfNeeded()
        val mriDao = db.mriCurriculumDao()
        val curriculumDao = db.curriculumDao()

        val allModules = mriDao.observeAllModules().first()
        assertTrue(allModules.any { it.id == "M06A" && it.title.contains("Sistem Gradien") })
        assertTrue(allModules.any { it.id == "M06B" && it.title.contains("Sistem RF") })

        // Verify M06A nodes (11 nodes: 3 T1, 6 T2, 2 T3)
        val m06aNodes = curriculumDao.observeNodesByModule("M06A").first()
        assertEquals(11, m06aNodes.size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M06A", 1).first().size)
        assertEquals(6, curriculumDao.observeNodesByModuleAndLevel("M06A", 2).first().size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M06A", 3).first().size)

        val m06aT21 = m06aNodes.first { it.code == "M06A.T2.1" }
        assertEquals("m6a_trapesium_gradien", m06aT21.labType)

        // Verify M06B nodes (9 nodes: 2 T1, 5 T2, 2 T3)
        val m06bNodes = curriculumDao.observeNodesByModule("M06B").first()
        assertEquals(9, m06bNodes.size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M06B", 1).first().size)
        assertEquals(5, curriculumDao.observeNodesByModuleAndLevel("M06B", 2).first().size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M06B", 3).first().size)

        val m06bT21 = m06bNodes.first { it.code == "M06B.T2.1" }
        assertEquals("m6b_a5_faraday", m06bT21.labType)

        val m06bT23 = m06bNodes.first { it.code == "M06B.T2.3" }
        assertEquals("m6b_a8_birdcage", m06bT23.labType)

        val m06bT32 = m06bNodes.first { it.code == "M06B.T3.2" }
        assertEquals("m6b_a7_b1_ptx", m06bT32.labType)

        // Verify M06A quizzes (7 questions: 2 T1, 3 T2, 2 T3)
        val m06aQuizzes = curriculumDao.observeQuizzesByModule("M06A").first()
        assertEquals(7, m06aQuizzes.size)
        assertEquals(2, curriculumDao.observeQuizzesByModuleAndLevel("M06A", 1).first().size)
        assertEquals(3, curriculumDao.observeQuizzesByModuleAndLevel("M06A", 2).first().size)
        assertEquals(2, curriculumDao.observeQuizzesByModuleAndLevel("M06A", 3).first().size)

        // Verify M06B quizzes (15 questions: 4 T1, 6 T2, 5 T3)
        val m06bQuizzes = curriculumDao.observeQuizzesByModule("M06B").first()
        assertEquals(15, m06bQuizzes.size)
        assertEquals(4, curriculumDao.observeQuizzesByModuleAndLevel("M06B", 1).first().size)
        assertEquals(6, curriculumDao.observeQuizzesByModuleAndLevel("M06B", 2).first().size)
        assertEquals(5, curriculumDao.observeQuizzesByModuleAndLevel("M06B", 3).first().size)

        val m06bQ1 = m06bQuizzes.first { it.id == "M06B_Q1" }
        assertEquals(0, m06bQ1.correctIndex)
    }

    @Test
    fun testModule7ABCIntegration() = runBlocking {
        db.seedCurriculumIfNeeded()
        val mriDao = db.mriCurriculumDao()
        val curriculumDao = db.curriculumDao()

        val allModules = mriDao.observeAllModules().first()
        assertTrue(allModules.any { it.id == "M07A" && it.title.contains("Zona, Skrining") })
        assertTrue(allModules.any { it.id == "M07B" && it.title.contains("Medan Statis") })
        assertTrue(allModules.any { it.id == "M07C" && it.title.contains("RF (SAR)") })

        // Verify M07A nodes (9 nodes: 3 T1, 6 T2)
        val m07aNodes = curriculumDao.observeNodesByModule("M07A").first()
        assertEquals(9, m07aNodes.size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M07A", 1).first().size)
        assertEquals(6, curriculumDao.observeNodesByModuleAndLevel("M07A", 2).first().size)
        assertEquals("m7a_zona_akses", m07aNodes.first { it.code == "M07A.T1.1" }.labType)
        assertEquals("m7a_skrining", m07aNodes.first { it.code == "M07A.T1.2" }.labType)
        assertEquals("m7a_kontras", m07aNodes.first { it.code == "M07A.T2.6" }.labType)

        // Verify M07B nodes (9 nodes: 7 T2, 2 T3)
        val m07bNodes = curriculumDao.observeNodesByModule("M07B").first()
        assertEquals(9, m07bNodes.size)
        assertEquals(7, curriculumDao.observeNodesByModuleAndLevel("M07B", 2).first().size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M07B", 3).first().size)
        assertEquals("m7b_medan_tepi", m07bNodes.first { it.code == "M07B.T2.1" }.labType)
        assertEquals("m7b_lenz", m07bNodes.first { it.code == "M07B.T2.6" }.labType)
        assertEquals("m7b_darurat", m07bNodes.first { it.code == "M07B.T3.2" }.labType)

        // Verify M07C nodes (11 nodes: 7 T2, 4 T3)
        val m07cNodes = curriculumDao.observeNodesByModule("M07C").first()
        assertEquals(11, m07cNodes.size)
        assertEquals(7, curriculumDao.observeNodesByModuleAndLevel("M07C", 2).first().size)
        assertEquals(4, curriculumDao.observeNodesByModuleAndLevel("M07C", 3).first().size)
        assertEquals("m7c_sar", m07cNodes.first { it.code == "M07C.T2.2" }.labType)
        assertEquals("m7c_bising", m07cNodes.first { it.code == "M07C.T2.7" }.labType)
        assertEquals("m7c_b1_jaringan", m07cNodes.first { it.code == "M07C.T3.2" }.labType)

        // Verify 35 quizzes across M07A (13), M07B (10), M07C (12)
        val m07aQuizzes = curriculumDao.observeQuizzesByModule("M07A").first()
        val m07bQuizzes = curriculumDao.observeQuizzesByModule("M07B").first()
        val m07cQuizzes = curriculumDao.observeQuizzesByModule("M07C").first()
        assertEquals(13, m07aQuizzes.size)
        assertEquals(10, m07bQuizzes.size)
        assertEquals(12, m07cQuizzes.size)
        assertEquals(35, m07aQuizzes.size + m07bQuizzes.size + m07cQuizzes.size)

        assertEquals(5, curriculumDao.observeQuizzesByModuleAndLevel("M07A", 1).first().size)
        assertEquals(8, curriculumDao.observeQuizzesByModuleAndLevel("M07A", 2).first().size)
        assertEquals(7, curriculumDao.observeQuizzesByModuleAndLevel("M07B", 2).first().size)
        assertEquals(3, curriculumDao.observeQuizzesByModuleAndLevel("M07B", 3).first().size)
        assertEquals(9, curriculumDao.observeQuizzesByModuleAndLevel("M07C", 2).first().size)
        assertEquals(3, curriculumDao.observeQuizzesByModuleAndLevel("M07C", 3).first().size)

        assertEquals(13, curriculumDao.observeQuizzesByModule("M07A").first().size)
        assertEquals(10, curriculumDao.observeQuizzesByModule("M07B").first().size)
        assertEquals(12, curriculumDao.observeQuizzesByModule("M07C").first().size)
    }

    @Test
    fun testModule8ABCIntegration() = runBlocking {
        db.seedCurriculumIfNeeded()
        val mriDao = db.mriCurriculumDao()
        val curriculumDao = db.curriculumDao()

        val allModules = mriDao.observeAllModules().first()
        assertTrue(allModules.any { it.id == "M08A" && it.title.contains("Artefak Pengodean") })
        assertTrue(allModules.any { it.id == "M08B" && it.title.contains("Aliran") })
        assertTrue(allModules.any { it.id == "M08C" && it.title.contains("Perangkat Keras") })

        // Verify M08A nodes (14 nodes: 4 T1, 5 T2, 5 T3)
        val m08aNodes = curriculumDao.observeNodesByModule("M08A").first()
        assertEquals(14, m08aNodes.size)
        assertEquals(4, curriculumDao.observeNodesByModuleAndLevel("M08A", 1).first().size)
        assertEquals(5, curriculumDao.observeNodesByModuleAndLevel("M08A", 2).first().size)
        assertEquals(5, curriculumDao.observeNodesByModuleAndLevel("M08A", 3).first().size)
        assertEquals("m8c_capstone_tabel88", m08aNodes.first { it.code == "M08A.T1.1" }.labType)
        assertEquals("m8a_luar_fase", m08aNodes.first { it.code == "M08A.T2.3" }.labType)
        assertEquals("m8a_magic_angle", m08aNodes.first { it.code == "M08A.T2.4" }.labType)

        // Verify M08B nodes (10 nodes: 2 T1, 6 T2, 2 T3)
        val m08bNodes = curriculumDao.observeNodesByModule("M08B").first()
        assertEquals(10, m08bNodes.size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M08B", 1).first().size)
        assertEquals(6, curriculumDao.observeNodesByModuleAndLevel("M08B", 2).first().size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M08B", 3).first().size)
        assertEquals("m8b_aliran", m08bNodes.first { it.code == "M08B.T1.1" }.labType)
        assertEquals("m8b_gmn", m08bNodes.first { it.code == "M08B.T2.4" }.labType)
        assertEquals("m8b_presat", m08bNodes.first { it.code == "M08B.T2.5" }.labType)

        // Verify M08C nodes (9 nodes: 6 T2, 3 T3)
        val m08cNodes = curriculumDao.observeNodesByModule("M08C").first()
        assertEquals(9, m08cNodes.size)
        assertEquals(6, curriculumDao.observeNodesByModuleAndLevel("M08C", 2).first().size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M08C", 3).first().size)
        assertEquals("m8c_celah_irisan", m08cNodes.first { it.code == "M08C.T2.1" }.labType)
        assertEquals("m8c_qa_fantom", m08cNodes.first { it.code == "M08C.T3.2" }.labType)
        assertEquals("m8c_capstone_tabel88", m08cNodes.first { it.code == "M08C.T3.3" }.labType)

        // Total 33 nodes across M08A, M08B, M08C
        assertEquals(33, m08aNodes.size + m08bNodes.size + m08cNodes.size)

        // Verify 59 quizzes across M08A (24), M08B (15), M08C (20)
        val m08aQuizzes = curriculumDao.observeQuizzesByModule("M08A").first()
        val m08bQuizzes = curriculumDao.observeQuizzesByModule("M08B").first()
        val m08cQuizzes = curriculumDao.observeQuizzesByModule("M08C").first()
        assertEquals(24, m08aQuizzes.size)
        assertEquals(15, m08bQuizzes.size)
        assertEquals(20, m08cQuizzes.size)
        assertEquals(59, m08aQuizzes.size + m08bQuizzes.size + m08cQuizzes.size)

        assertEquals(24, curriculumDao.observeQuizzesByModule("M08A").first().size)
        assertEquals(15, curriculumDao.observeQuizzesByModule("M08B").first().size)
        assertEquals(20, curriculumDao.observeQuizzesByModule("M08C").first().size)
    }

    @Test
    fun testModule3ABCIntegration() = runBlocking {
        db.seedCurriculumIfNeeded()
        val mriDao = db.mriCurriculumDao()
        val curriculumDao = db.curriculumDao()

        // 1. Verify Modules M03A, M03B, M03C exist
        val modules = mriDao.observeAllModules().first()
        val m03a = modules.find { it.id == "M03A" }
        val m03b = modules.find { it.id == "M03B" }
        val m03c = modules.find { it.id == "M03C" }
        assertTrue(m03a != null)
        assertTrue(m03b != null)
        assertTrue(m03c != null)
        assertEquals("M03A", m03a?.code)
        assertEquals("M03B", m03b?.code)
        assertEquals("M03C", m03c?.code)

        // 2. Verify M03A nodes (8 nodes: 4 T1, 3 T2, 1 T3)
        val m03aNodes = curriculumDao.observeNodesByModule("M03A").first()
        assertEquals(8, m03aNodes.size)
        assertEquals(4, curriculumDao.observeNodesByModuleAndLevel("M03A", 1).first().size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M03A", 2).first().size)
        assertEquals(1, curriculumDao.observeNodesByModuleAndLevel("M03A", 3).first().size)
        assertEquals("m3a_spin_echo", m03aNodes.first { it.code == "M03A.T1.1" }.labType)
        assertEquals("m3a_ir_null", m03aNodes.first { it.code == "M03A.T1.3" }.labType)
        assertEquals("m3a_tse", m03aNodes.first { it.code == "M03A.T1.4" }.labType)
        assertEquals("m3a_drive", m03aNodes.first { it.code == "M03A.T3.1" }.labType)

        // 3. Verify M03B nodes (8 nodes: 3 T1, 3 T2, 2 T3)
        val m03bNodes = curriculumDao.observeNodesByModule("M03B").first()
        assertEquals(8, m03bNodes.size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M03B", 1).first().size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M03B", 2).first().size)
        assertEquals(2, curriculumDao.observeNodesByModuleAndLevel("M03B", 3).first().size)
        assertEquals("m3b_gre_kompor", m03bNodes.first { it.code == "M03B.T1.1" }.labType)
        assertEquals("m3b_bipolar_rewinder", m03bNodes.first { it.code == "M03B.T1.2" }.labType)
        assertEquals("m3b_ernst", m03bNodes.first { it.code == "M03B.T1.3" }.labType)
        assertEquals("m3b_steady_state", m03bNodes.first { it.code == "M03B.T2.1" }.labType)
        assertEquals("m3b_tiga_tunak", m03bNodes.first { it.code == "M03B.T2.2" }.labType)
        assertEquals("m3b_t2star_servikal", m03bNodes.first { it.code == "M03B.T2.3" }.labType)
        assertEquals("m3b_tiga_tunak", m03bNodes.first { it.code == "M03B.T3.1" }.labType)
        assertEquals("m3b_epi", m03bNodes.first { it.code == "M03B.T3.2" }.labType)

        // 4. Verify M03C nodes (7 nodes: 4 T1, 3 T2)
        val m03cNodes = curriculumDao.observeNodesByModule("M03C").first()
        assertEquals(7, m03cNodes.size)
        assertEquals(4, curriculumDao.observeNodesByModuleAndLevel("M03C", 1).first().size)
        assertEquals(3, curriculumDao.observeNodesByModuleAndLevel("M03C", 2).first().size)
        assertEquals("m3c_dwi", m03cNodes.first { it.code == "M03C.T1.1" }.labType)
        assertEquals("m3c_bold_mtc", m03cNodes.first { it.code == "M03C.T1.2" }.labType)
        assertEquals("m3c_bold_mtc", m03cNodes.first { it.code == "M03C.T1.3" }.labType)
        assertEquals("m3c_kontras", m03cNodes.first { it.code == "M03C.T1.4" }.labType)
        assertEquals("m3c_kontras", m03cNodes.first { it.code == "M03C.T2.1" }.labType)
        assertEquals("m3c_kontras", m03cNodes.first { it.code == "M03C.T2.2" }.labType)
        assertEquals("m3c_dwi", m03cNodes.first { it.code == "M03C.T2.3" }.labType)

        // Total 23 nodes across M03A (8), M03B (8), M03C (7)
        assertEquals(23, m03aNodes.size + m03bNodes.size + m03cNodes.size)

        // 5. Verify 43 quizzes across M03A (15), M03B (18), M03C (10)
        val m03aQuizzes = curriculumDao.observeQuizzesByModule("M03A").first()
        val m03bQuizzes = curriculumDao.observeQuizzesByModule("M03B").first()
        val m03cQuizzes = curriculumDao.observeQuizzesByModule("M03C").first()
        assertEquals(15, m03aQuizzes.size)
        assertEquals(18, m03bQuizzes.size)
        assertEquals(10, m03cQuizzes.size)
        assertEquals(43, m03aQuizzes.size + m03bQuizzes.size + m03cQuizzes.size)

        assertEquals(15, curriculumDao.observeQuizzesByModule("M03A").first().size)
        assertEquals(18, curriculumDao.observeQuizzesByModule("M03B").first().size)
        assertEquals(10, curriculumDao.observeQuizzesByModule("M03C").first().size)
    }

    @Test
    fun testModule3ABCUserProgressNotesAndQuizzes() = runBlocking {
        // 1. Test NodeProgressDao with M03A, M03B, M03C
        val progressDao = db.nodeProgressDao()
        progressDao.insert(NodeProgressEntity(nodeCode = "M03A.T1.1", completedAt = 1000L))
        progressDao.insert(NodeProgressEntity(nodeCode = "M03A.T1.2", completedAt = 1001L))
        progressDao.insert(NodeProgressEntity(nodeCode = "M03B.T1.1", completedAt = 1002L))
        progressDao.insert(NodeProgressEntity(nodeCode = "M03C.T2.1", completedAt = 1003L))

        assertEquals(4, progressDao.getAll().size)
        assertEquals(2, progressDao.getCountByModule("M03A"))
        assertEquals(1, progressDao.getCountByModule("M03B"))
        assertEquals(1, progressDao.getCountByModule("M03C"))
        assertEquals(4, progressDao.getCountByPrefix("M03"))

        val m03aNodes = progressDao.observeCompletedNodesByModule("M03A").first()
        assertEquals(2, m03aNodes.size)
        assertEquals("M03A", m03aNodes[0].moduleId)

        val m03Nodes = progressDao.observeCompletedNodesByPrefix("M03").first()
        assertEquals(4, m03Nodes.size)

        // 2. Test QuizAttemptDao with M03A, M03B, M03C
        val quizDao = db.quizAttemptDao()
        quizDao.insert(
            QuizAttemptEntity(
                quizId = "M03A_Q1",
                nodeCode = "M03A.T1.1",
                moduleId = "M03A",
                selectedIndex = 0,
                isCorrect = true,
                answeredAt = 2000L
            )
        )
        quizDao.insert(
            QuizAttemptEntity(
                quizId = "M03A_Q2",
                nodeCode = "M03A.T1.2",
                moduleId = "M03A",
                selectedIndex = 1,
                isCorrect = false,
                answeredAt = 2001L
            )
        )
        quizDao.insert(
            QuizAttemptEntity(
                quizId = "M03B_Q1",
                nodeCode = "M03B.T1.1",
                moduleId = "M03B",
                selectedIndex = 2,
                isCorrect = true,
                answeredAt = 2002L
            )
        )

        assertEquals(2, quizDao.getTotalCountByModule("M03A"))
        assertEquals(1, quizDao.getCorrectCountByModule("M03A"))
        assertEquals(1, quizDao.getTotalCountByModule("M03B"))

        val m03aAttempts = quizDao.observeQuizAttemptsByModule("M03A").first()
        assertEquals(2, m03aAttempts.size)

        val m03Attempts = quizDao.observeQuizAttemptsByPrefix("M03").first()
        assertEquals(3, m03Attempts.size)

        // 3. Test StudyNoteDao with M03A, M03B, M03C
        val noteDao = db.studyNoteDao()
        noteDao.insert(
            StudyNoteEntity(
                id = "note_m3a",
                title = "Spin Echo T1w vs T2w",
                content = "TR pendek dan TE pendek menghasilkan T1w kontras",
                category = "Fisika Sekuens",
                createdAt = 3000L,
                updatedAt = 3000L,
                nodeCode = "M03A.T1.1",
                moduleId = "M03A"
            )
        )
        noteDao.insert(
            StudyNoteEntity(
                id = "note_m3b",
                title = "Kondisi Tunak Ernst Angle",
                content = "Sudut Ernst mengoptimalkan SNR untuk TR sangat pendek",
                category = "Fisika Sekuens",
                createdAt = 3001L,
                updatedAt = 3001L,
                nodeCode = "M03B.T1.3",
                moduleId = "M03B"
            )
        )

        val m03aNotes = noteDao.observeNotesByModule("M03A").first()
        assertEquals(1, m03aNotes.size)
        assertEquals("M03A.T1.1", m03aNotes[0].nodeCode)

        val m03Notes = noteDao.observeNotesByPrefix("M03").first()
        assertEquals(2, m03Notes.size)

        // 4. Test StudyDayDao
        val dayDao = db.studyDayDao()
        dayDao.insert(
            StudyDayEntity(
                dateIso = "2026-11-11",
                nodesCompletedCount = 4,
                lastNodeCode = "M03C.T2.1",
                lastModuleId = "M03C"
            )
        )
        val day = dayDao.getByDate("2026-11-11")
        assertTrue(day != null)
        assertEquals(4, day?.nodesCompletedCount)
        assertEquals("M03C.T2.1", day?.lastNodeCode)
        assertEquals("M03C", day?.lastModuleId)
    }

    @Test
    fun testToggleNodeCompletionMappingModule3ABC() = runBlocking {
        val progressDao = db.nodeProgressDao()
        val dayDao = db.studyDayDao()

        // Test insertion of M03A, M03B, M03C and legacy M03 mapped codes
        val rawCodes = listOf("M03A.T1.1", "M03B.T1.3", "M03C.T2.1", "M03.T1.1")
        rawCodes.forEachIndexed { idx, code ->
            val normalizedCode = when (code) {
                "M03.T1.1" -> "M03A.T1.1"
                "M03.T1.2" -> "M03A.T1.3"
                "M03.T2.1" -> "M03B.T1.1"
                "M03.T2.2" -> "M03B.T2.1"
                "M03.T3.1" -> "M03C.T1.1"
                "M03.T3.2" -> "M03C.T1.4"
                else -> code
            }
            val modId = when {
                normalizedCode.startsWith("M03A.") -> "M03A"
                normalizedCode.startsWith("M03B.") -> "M03B"
                normalizedCode.startsWith("M03C.") -> "M03C"
                normalizedCode.contains(".") -> normalizedCode.substringBefore(".")
                else -> ""
            }
            progressDao.insert(
                NodeProgressEntity(
                    nodeCode = normalizedCode,
                    completedAt = System.currentTimeMillis() + idx,
                    moduleId = modId
                )
            )
            dayDao.insert(
                StudyDayEntity(
                    dateIso = "2026-11-10",
                    nodesCompletedCount = idx + 1,
                    lastNodeCode = normalizedCode,
                    lastModuleId = modId
                )
            )
        }

        // Because "M03.T1.1" normalized to "M03A.T1.1", which replaces the primary key, total unique completed nodes is 3
        assertEquals(3, progressDao.getAll().size)
        assertEquals(1, progressDao.getCountByModule("M03A"))
        assertEquals(1, progressDao.getCountByModule("M03B"))
        assertEquals(1, progressDao.getCountByModule("M03C"))
        assertEquals(3, progressDao.getCountByPrefix("M03"))

        val today = dayDao.getByDate("2026-11-10")
        assertTrue(today != null)
        assertEquals(4, today?.nodesCompletedCount)
        assertEquals("M03A.T1.1", today?.lastNodeCode)
        assertEquals("M03A", today?.lastModuleId)
    }

    @Test
    fun testBugB03AndB05TransactionalNodeToggleAndNonDestructiveStudyDay() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.UserDataRepository(context, db)
        val dateIso = java.time.LocalDate.now().toString()

        // Tandai 2 node selesai hari ini secara transaksional (B-05 & B-03)
        repo.toggleNodeCompletionTransactional("M01.T1.1", "M01", dateIso)
        repo.toggleNodeCompletionTransactional("M01.T1.2", "M01", dateIso)

        val dayAfterNodes = db.studyDayDao().getByDate(dateIso)
        assertTrue(dayAfterNodes != null)
        assertEquals(2, dayAfterNodes?.nodesCompletedCount)
        assertEquals("M01.T1.2", dayAfterNodes?.lastNodeCode)
        assertEquals("M01", dayAfterNodes?.lastModuleId)

        // Tandai flashcard sebagai dikuasai -> memanggil recordStudyActivity tanpa parameter node (B-03)
        repo.recordStudyActivity(dateIso)

        val dayAfterFlashcard = db.studyDayDao().getByDate(dateIso)
        assertTrue(dayAfterFlashcard != null)
        // B-03: nodesCompletedCount, lastNodeCode, dan lastModuleId TIDAK BOLEH ter-reset ke 0 / ""
        assertEquals(2, dayAfterFlashcard?.nodesCompletedCount)
        assertEquals("M01.T1.2", dayAfterFlashcard?.lastNodeCode)
        assertEquals("M01", dayAfterFlashcard?.lastModuleId)

        // Toggle ulang M01.T1.2 untuk membatalkan penyelesaian
        repo.toggleNodeCompletionTransactional("M01.T1.2", "M01", dateIso)
        val dayAfterUncheck = db.studyDayDao().getByDate(dateIso)
        assertEquals(1, dayAfterUncheck?.nodesCompletedCount)
    }

    @Test
    fun testBugB08PreserveNoteCreatedAtOnEdit() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.UserDataRepository(context, db)
        val originalCreatedAt = 1700000000000L
        val initialNote = StudyNoteEntity(
            id = "note_b08",
            title = "Judul Awal",
            content = "Isi Awal",
            category = "Fisika Dasar",
            createdAt = originalCreatedAt,
            updatedAt = originalCreatedAt,
            nodeCode = "M01.T1.1",
            moduleId = "M01"
        )
        repo.insertNote(initialNote)

        val existing = repo.getNoteById("note_b08")
        assertTrue(existing != null)
        assertEquals(originalCreatedAt, existing?.createdAt)

        val editedNote = StudyNoteEntity(
            id = "note_b08",
            title = "Judul Diedit",
            content = "Isi Diedit",
            category = "Fisika Dasar",
            createdAt = existing?.createdAt ?: 9999999999999L,
            updatedAt = originalCreatedAt + 50000L,
            nodeCode = "M01.T1.1",
            moduleId = "M01"
        )
        repo.insertNote(editedNote)

        val afterEdit = repo.getNoteById("note_b08")
        assertEquals("Judul Diedit", afterEdit?.title)
        assertEquals(originalCreatedAt, afterEdit?.createdAt)
    }

    @Test
    fun testBugB02AndB13ViewModelQuizAndSeparatedFlashcards() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.MriViewModel(app)

        // B-13: Pastikan state flashcard Glosarium dan Modul terpisah
        vm.setGlossaryFlashcardMode(true)
        vm.nextFlashcard(10)
        vm.flipFlashcard()
        assertEquals(1, vm.uiState.value.glossaryFlashcardIndex)
        assertTrue(vm.uiState.value.isGlossaryFlashcardFlipped)
        assertEquals(0, vm.uiState.value.moduleFlashcardIndex)
        assertTrue(!vm.uiState.value.isModuleFlashcardFlipped)

        // B-02: Pastikan soal kuis yang sudah dijawab tetap terkunci saat navigasi prev/next dan skor tidak melebihi jumlah soal
        val firstQuiz = vm.uiState.value.filteredQuizzes.first()
        val correctOpt = firstQuiz.correctIndex
        vm.selectQuizOption(correctOpt)
        vm.submitQuizAnswer()
        // Panggil submitQuizAnswer kedua kali berturut-turut -> skor tidak boleh bertambah dua kali
        vm.submitQuizAnswer()
        assertEquals(1, vm.uiState.value.quizScore)
        assertTrue(vm.uiState.value.isAnswerSubmitted)
        assertEquals(correctOpt, vm.uiState.value.quizAnswers[firstQuiz.id])

        // Pindah ke soal ke-2 lalu kembali ke soal ke-1
        vm.nextQuizQuestion()
        assertTrue(!vm.uiState.value.isAnswerSubmitted)
        vm.prevQuizQuestion()
        assertTrue("Soal ke-1 harus tetap berstatus sudah dijawab saat kembali", vm.uiState.value.isAnswerSubmitted)
        assertEquals(correctOpt, vm.uiState.value.selectedOptionIndex)
    }

    @Test
    fun testNB01IdempotentMarkNodeCompletedAndNB03ActiveCurriculumObservers() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.UserDataRepository(context, db)
        val dateIso = java.time.LocalDate.now().toString()

        // NB-01: Pemanggilan berulang markNodeCompletedIfNotYet (seperti di LaunchedEffect simulator M03)
        // tidak boleh membatalkan simpul yang sudah ditandai selesai!
        val firstCall = repo.markNodeCompletedIfNotYet("M03A.T1.2", dateIso = dateIso)
        val secondCall = repo.markNodeCompletedIfNotYet("M03A.T1.2", dateIso = dateIso)
        val thirdCall = repo.markNodeCompletedIfNotYet("M03A.T1.3", dateIso = dateIso)

        assertTrue("Panggilan pertama harus menandai selesai", firstCall)
        assertTrue("Panggilan kedua harus idempoten (mengembalikan false tanpa menghapus)", !secondCall)
        assertTrue("Panggilan simpul kedua harus menandai selesai", thirdCall)

        val completedCodes = db.nodeProgressDao().getAll().map { it.nodeCode }.toSet()
        assertTrue(completedCodes.contains("M03A.T1.2"))
        assertTrue(completedCodes.contains("M03A.T1.3"))
        assertEquals(2, db.studyDayDao().getByDate(dateIso)?.nodesCompletedCount)

        // B-03 & B-17 sisa: Jika seluruh simpul pada hari tersebut di-uncheck dan tidak ada kuis, baris study_days harus dihapus
        repo.toggleNodeCompletionTransactional("M03A.T1.2", "M03A", dateIso)
        repo.toggleNodeCompletionTransactional("M03A.T1.3", "M03A", dateIso)
        assertEquals(null, db.studyDayDao().getByDate(dateIso))

        // NB-03: Pastikan fungsi observer di UserDataRepository membaca tabel aktif curriculum_nodes & curriculum_quizzes
        db.seedCurriculumIfNeeded()
        assertTrue(repo.observeCurriculumNodes().first().isNotEmpty())
        assertTrue(repo.observeCurriculumNodesByModule("M01").first().isNotEmpty())
        assertTrue(repo.observeCurriculumQuizzes().first().isNotEmpty())
    }

    @Test
    fun testFinal02AndK07AndK09FlashcardStreakProtectionAndLastNodeCleanup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.UserDataRepository(context, db)
        val dateIso = java.time.LocalDate.now().toString()

        // K-07: Selesaikan 2 simpul lalu batalkan simpul terakhir -> lastNodeCode harus mundur ke simpul sebelumnya
        repo.toggleNodeCompletionTransactional("M01.T1.1", "M01", dateIso)
        kotlinx.coroutines.delay(5)
        repo.toggleNodeCompletionTransactional("M01.T1.2", "M01", dateIso)
        assertEquals("M01.T1.2", db.studyDayDao().getByDate(dateIso)?.lastNodeCode)

        repo.toggleNodeCompletionTransactional("M01.T1.2", "M01", dateIso)
        val dayAfterUncheckSecond = db.studyDayDao().getByDate(dateIso)
        assertEquals(1, dayAfterUncheckSecond?.nodesCompletedCount)
        assertEquals("M01.T1.1", dayAfterUncheckSecond?.lastNodeCode)

        // FINAL-02: Catat aktivitas flashcard hari ini, lalu batalkan sisa simpul M01.T1.1 -> study_days TIDAK boleh terhapus
        repo.recordStudyActivity(dateIso = dateIso, lastNodeCode = "FLASHCARD_REVIEW")
        assertEquals("FLASHCARD_REVIEW", db.studyDayDao().getByDate(dateIso)?.lastNodeCode)

        repo.toggleNodeCompletionTransactional("M01.T1.1", "M01", dateIso)
        val dayAfterAllNodesUnchecked = db.studyDayDao().getByDate(dateIso)
        assertTrue("Streak hari belajar dari flashcard harus tetap dipertahankan", dayAfterAllNodesUnchecked != null)
        assertEquals(0, dayAfterAllNodesUnchecked?.nodesCompletedCount)
        assertEquals("FLASHCARD_REVIEW", dayAfterAllNodesUnchecked?.lastNodeCode)

        // P3: Jika flashcard dipelajari lebih dulu, lalu simpul M01.T1.3 ditandai selesai (menimpa lastNodeCode),
        // dan kemudian M01.T1.3 dibatalkan -> study_days TIDAK boleh terhapus karena hasFlashcardActivity = true
        repo.toggleNodeCompletionTransactional("M01.T1.3", "M01", dateIso)
        assertEquals("M01.T1.3", db.studyDayDao().getByDate(dateIso)?.lastNodeCode)
        repo.toggleNodeCompletionTransactional("M01.T1.3", "M01", dateIso)
        val dayAfterP3Uncheck = db.studyDayDao().getByDate(dateIso)
        assertTrue("P3: Hari belajar dari flashcard tetap tersimpan meski lastNodeCode sempat tertimpa simpul", dayAfterP3Uncheck != null)
        assertTrue(dayAfterP3Uncheck?.hasFlashcardActivity == true)

        // K-09: Pastikan CurriculumQuizEntity.toDomainModel() mempertahankan lencana Capstone khusus
        db.seedCurriculumIfNeeded()
        val m08cQuizzes = repo.observeCurriculumQuizzesByModule("M08C").first().map { it.toDomainModel() }
        val capstoneQuiz = m08cQuizzes.find { it.id == "M08C_Q16" }
        assertTrue(capstoneQuiz != null)
        assertTrue("Lencana Capstone harus dipertahankan", capstoneQuiz?.levelBadge?.contains("Capstone") == true)

        // P2: Pastikan QuizShuffle menghasilkan distribusi indeks jawaban benar yang bervariasi dan tetap menunjuk teks jawaban benar yang sama
        val allQuizzes = repo.observeCurriculumQuizzes().first().map { it.toDomainModel() }
        val shuffledQuizzes = allQuizzes.map { com.example.util.QuizShuffle.shuffled(it) }
        allQuizzes.zip(shuffledQuizzes).forEach { (orig, shuf) ->
            assertEquals(orig.options[orig.correctIndex], shuf.options[shuf.correctIndex])
        }
        val distinctCorrectIndices = shuffledQuizzes.map { it.correctIndex }.toSet()
        assertTrue("P2: Opsi kuis harus teracak ke berbagai indeks", distinctCorrectIndices.size >= 3)

        // Bookmark Tersimpan: Tambah & hapus bookmark materi serta glosarium
        repo.clearAllBookmarks()
        assertTrue(repo.bookmarkedNodesFlow.first().isEmpty())
        assertTrue(repo.bookmarkedGlossaryFlow.first().isEmpty())

        repo.toggleNodeBookmark("M01.T1.1")
        repo.toggleNodeBookmark("M02.T1.1")
        repo.toggleGlossaryBookmark("Frekuensi Larmor")
        assertTrue(repo.bookmarkedNodesFlow.first().containsAll(listOf("M01.T1.1", "M02.T1.1")))
        assertTrue(repo.bookmarkedGlossaryFlow.first().contains("Frekuensi Larmor"))

        repo.removeNodeBookmark("M01.T1.1")
        assertEquals(setOf("M02.T1.1"), repo.bookmarkedNodesFlow.first())

        repo.removeGlossaryBookmark("Frekuensi Larmor")
        assertTrue(repo.bookmarkedGlossaryFlow.first().isEmpty())

        repo.clearAllBookmarks()
        assertTrue(repo.bookmarkedNodesFlow.first().isEmpty())
    }
}

