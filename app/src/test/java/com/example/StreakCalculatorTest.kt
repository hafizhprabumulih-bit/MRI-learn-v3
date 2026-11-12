package com.example

import com.example.util.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 10)

    @Test
    fun `test zero days`() {
        val streak = StreakCalculator.compute(emptySet(), today)
        assertEquals(0, streak)
    }

    @Test
    fun `test today only`() {
        val days = setOf(today)
        val streak = StreakCalculator.compute(days, today)
        assertEquals(1, streak)
    }

    @Test
    fun `test yesterday only streak alive`() {
        val days = setOf(today.minusDays(1))
        val streak = StreakCalculator.compute(days, today)
        assertEquals(1, streak)
    }

    @Test
    fun `test missed two days streak zero`() {
        val days = setOf(today.minusDays(3))
        val streak = StreakCalculator.compute(days, today)
        assertEquals(0, streak)
    }

    @Test
    fun `test five day streak`() {
        val days = setOf(
            today,
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(3),
            today.minusDays(4)
        )
        val streak = StreakCalculator.compute(days, today)
        assertEquals(5, streak)
    }

    @Test
    fun `test gap in middle stops streak`() {
        // Studied today, yesterday (2 days), then missed 2 days ago, but studied 3, 4 days ago
        val days = setOf(
            today,
            today.minusDays(1),
            today.minusDays(3),
            today.minusDays(4)
        )
        val streak = StreakCalculator.compute(days, today)
        // Stops at gap (day minus 2 is missing), so streak = 2
        assertEquals(2, streak)
    }
}
