package com.example.util

import com.example.data.QuizQuestion
import kotlin.random.Random

/**
 * Mengacak urutan opsi jawaban secara deterministik (seed = id soal) dan
 * menghitung ulang correctIndex (P2).
 */
object QuizShuffle {
    fun shuffled(q: QuizQuestion): QuizQuestion {
        if (q.options.size < 2 || q.correctIndex !in q.options.indices) return q
        val order = q.options.indices.shuffled(Random(q.id.hashCode()))
        return q.copy(
            options = order.map { q.options[it] },
            correctIndex = order.indexOf(q.correctIndex)
        )
    }
}
