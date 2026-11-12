package com.example.data

import com.example.data.local.CurriculumNodeEntity
import com.example.data.local.CurriculumQuizEntity
import com.example.data.local.MriModuleEntity
import java.security.MessageDigest
import java.util.Locale

class CurriculumSnapshot(
    val modules: List<MriModuleEntity>,
    val curriculumNodes: List<CurriculumNodeEntity>,
    val curriculumQuizzes: List<CurriculumQuizEntity>
) {
    val isComplete: Boolean
        get() = modules.isNotEmpty() && curriculumNodes.isNotEmpty() && curriculumQuizzes.isNotEmpty()

    fun fingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        fun feed(s: String) {
            digest.update(s.toByteArray(Charsets.UTF_8))
            digest.update(0.toByte())
        }
        feed("tag:$SEED_TAG")
        modules.forEach { feed(it.toString()) }
        curriculumNodes.forEach { feed(it.toString()) }
        curriculumQuizzes.forEach { feed(it.toString()) }
        return digest.digest().joinToString("") { String.format(Locale.ROOT, "%02x", it) }
    }

    companion object {
        /** Ubah hanya jika SKEMA/CARA seed berubah (bukan isi materi; isi sudah tercakup fingerprint). */
        const val SEED_TAG = "3"

        fun build() = CurriculumSnapshot(
            modules = MriRepository.toMriModuleEntities(),
            curriculumNodes = MriRepository.toCurriculumNodeEntities(),
            curriculumQuizzes = MriRepository.toCurriculumQuizEntities()
        )
    }
}
