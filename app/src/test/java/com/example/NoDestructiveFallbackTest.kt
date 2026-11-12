package com.example

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NoDestructiveFallbackTest {

    @Test
    fun noDestructiveFallbackInCode() {
        val rootDir = listOf(File("src/main/java"), File("app/src/main/java")).firstOrNull { it.exists() }
            ?: throw IllegalStateException("Direktori src/main/java tidak ditemukan")

        val ktFiles = rootDir.walkTopDown().filter { it.isFile && it.extension == "kt" }

        for (file in ktFiles) {
            val lines = file.readLines()
            for ((index, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("*") || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                    continue
                }
                if (trimmed.contains("fallbackToDestructiveMigration")) {
                    assertTrue(
                        "Fallback destruktif dilarang: tabel pengguna ada di DB yang sama. Tulis Migration nyata.",
                        false
                    )
                }
            }
        }
    }
}
