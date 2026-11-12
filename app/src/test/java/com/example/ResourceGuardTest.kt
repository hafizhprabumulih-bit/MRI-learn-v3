package com.example

import java.io.File
import java.security.MessageDigest
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceGuardTest {
    private fun drawableDir(): File? =
        listOf("src/main/res/drawable", "app/src/main/res/drawable")
            .map { File(it) }
            .firstOrNull { it.isDirectory }

    private fun sha256(f: File): String =
        MessageDigest.getInstance("SHA-256").digest(f.readBytes())
            .joinToString("") { String.format(Locale.ROOT, "%02x", it) }

    @Test
    fun drawableDirectoryIsFound() {
        assertTrue("Folder res/drawable tidak ditemukan dari direktori kerja tes", drawableDir() != null)
    }

    @Test
    fun noDuplicateImageContent() {
        val dir = requireNotNull(drawableDir()) { "Folder res/drawable tidak ditemukan" }
        val images = dir.listFiles { f -> f.isFile && f.extension.lowercase() in setOf("png", "jpg", "jpeg", "webp") }
            .orEmpty()
        val duplicates = images.groupBy { sha256(it) }.values
            .filter { it.size > 1 }
            .map { grup -> grup.map { it.name }.sorted() }
        assertTrue(
            "Gambar duplikat (isi identik) di res/drawable: $duplicates. " +
                "Hapus salinannya, simpan satu yang dirujuk kode.",
            duplicates.isEmpty()
        )
    }

    @Test
    fun requiredDrawablesExist() {
        val dir = requireNotNull(drawableDir()) { "Folder res/drawable tidak ditemukan" }
        val wajib = listOf(
            "mri_app_icon.jpg", "mri_scanner_hero.jpg", "mri_icon_bersih.png",
            "avatar_radiografer_mri.png", "avatar_radiolog.png", "avatar_ahli_neuro_mri.png",
            "avatar_fisikawan_medis.png", "avatar_petugas_proteksi_radiasi.png",
            "avatar_ahli_jantung.png", "avatar_perawat.png", "avatar_dokter_bedah.png",
            "avatar_dokter_robot.png"
        )
        val hilang = wajib.filter { !File(dir, it).isFile }
        assertTrue("Drawable wajib hilang: $hilang", hilang.isEmpty())
    }
}
