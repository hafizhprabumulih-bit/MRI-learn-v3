package com.example

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.ALL_MIGRATIONS
import com.example.data.local.DB_VERSION
import com.example.data.local.MriDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MriMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MriDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun schema11_validAndKeepsUserData() {
        var db = helper.createDatabase(TEST_DB, 11).apply {
            execSQL(
                "INSERT INTO node_progress (nodeCode, completedAt, moduleId) VALUES ('M01.T1.1', 1000, 'M01')"
            )
            execSQL(
                "INSERT INTO study_notes (id, title, content, category, createdAt, updatedAt, nodeCode, moduleId) " +
                "VALUES ('note_1', 'Judul Catatan', 'Isi catatan tes', 'Fisika', 1000, 1000, 'M01.T1.1', 'M01')"
            )
            execSQL(
                "INSERT INTO quiz_attempts (quizId, nodeCode, moduleId, selectedIndex, isCorrect, answeredAt) " +
                "VALUES ('M01_Q1', 'M01.T1.1', 'M01', 0, 1, 1000)"
            )
            execSQL(
                "INSERT INTO study_days (dateIso, nodesCompletedCount, lastNodeCode, lastModuleId) " +
                "VALUES ('2026-11-11', 1, 'M01.T1.1', 'M01')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, DB_VERSION, true, *ALL_MIGRATIONS)

        val progressCursor = db.query("SELECT * FROM node_progress WHERE nodeCode = 'M01.T1.1'")
        assertTrue(progressCursor.moveToFirst())
        assertEquals("M01.T1.1", progressCursor.getString(progressCursor.getColumnIndexOrThrow("nodeCode")))
        progressCursor.close()

        val notesCursor = db.query("SELECT * FROM study_notes WHERE id = 'note_1'")
        assertTrue(notesCursor.moveToFirst())
        assertEquals("Judul Catatan", notesCursor.getString(notesCursor.getColumnIndexOrThrow("title")))
        notesCursor.close()

        val quizCursor = db.query("SELECT * FROM quiz_attempts WHERE quizId = 'M01_Q1'")
        assertTrue(quizCursor.moveToFirst())
        assertEquals(1, quizCursor.getInt(quizCursor.getColumnIndexOrThrow("isCorrect")))
        quizCursor.close()

        val daysCursor = db.query("SELECT * FROM study_days WHERE dateIso = '2026-11-11'")
        assertTrue(daysCursor.moveToFirst())
        assertEquals("M01.T1.1", daysCursor.getString(daysCursor.getColumnIndexOrThrow("lastNodeCode")))
        assertEquals(0, daysCursor.getInt(daysCursor.getColumnIndexOrThrow("hasFlashcardActivity")))
        daysCursor.close()
    }

    @Test
    fun currentEntitiesOpenVersion11Database() {
        helper.createDatabase(TEST_DB, 11).apply {
            execSQL(
                "INSERT INTO node_progress (nodeCode, completedAt, moduleId) VALUES ('M01.T1.1', 1000, 'M01')"
            )
            close()
        }

        val appDb = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MriDatabase::class.java,
            TEST_DB
        ).addMigrations(*ALL_MIGRATIONS).build()

        appDb.openHelper.writableDatabase
        val nodeProgress = runBlocking { appDb.nodeProgressDao().getByNodeCode("M01.T1.1") }
        assertNotNull(nodeProgress)
        appDb.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
