package com.example

import com.example.data.local.ALL_MIGRATIONS
import com.example.data.local.DB_VERSION
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationChainTest {
    private val baseline = 11

    @Test
    fun everyVersionStepHasAMigration() {
        for (from in baseline until DB_VERSION) {
            val ada = ALL_MIGRATIONS.any { it.startVersion == from && it.endVersion == from + 1 }
            assertTrue("Migrasi $from -> ${from + 1} belum didaftarkan", ada)
        }
    }

    @Test
    fun noMigrationBeyondCurrentVersion() {
        val sisa = ALL_MIGRATIONS.filter { it.endVersion > DB_VERSION }
        assertTrue("Ada migrasi untuk versi di atas DB_VERSION ($DB_VERSION): $sisa", sisa.isEmpty())
    }

    @Test
    fun exportedSchemaExistsForCurrentVersion() {
        val rel = "schemas/com.example.data.local.MriDatabase/$DB_VERSION.json"
        val ada = File(rel).exists() || File("app/$rel").exists()
        assertTrue("File skema $rel tidak ditemukan; build sekali lalu commit", ada)
    }
}
