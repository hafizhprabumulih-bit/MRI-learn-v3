package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Satu-satunya sumber kebenaran versi skema [MriDatabase].
 * Naikkan nilai ini HANYA bersamaan dengan menulis Migration yang nyata (lihat prosedur di bawah).
 */
const val DB_VERSION = 12

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE study_days ADD COLUMN hasFlashcardActivity INTEGER NOT NULL DEFAULT 0"
        )
        // Baris lama yang sudah bertanda flashcard ikut ditandai.
        db.execSQL(
            "UPDATE study_days SET hasFlashcardActivity = 1 WHERE lastNodeCode = 'FLASHCARD_REVIEW'"
        )
    }
}

/**
 * Daftar migrasi yang benar-benar ada.
 *
 * Prosedur saat skema berubah (mis. 11 -> 12):
 * 1. Ubah entity, naikkan DB_VERSION menjadi 12.
 * 2. Build sekali agar Room mengekspor schemas/.../12.json, lalu commit file itu.
 * 3. Tulis `val MIGRATION_11_12 = object : Migration(11, 12) { ... }` dengan SQL nyata yang
 *    menyamai 12.json, dan JANGAN menyentuh tabel data pengguna (node_progress, study_notes,
 *    quiz_attempts, study_days) kecuali dengan ALTER TABLE atau tabel sementara yang menyalin data.
 * 4. Daftarkan di ALL_MIGRATIONS DI SINI, dan baru pada langkah ini.
 * 5. Jalankan MigrationChainTest (JVM) dan MriMigrationTest (perangkat) sampai hijau.
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_11_12)

