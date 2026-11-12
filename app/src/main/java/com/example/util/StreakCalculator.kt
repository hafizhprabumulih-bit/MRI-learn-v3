package com.example.util

import java.time.LocalDate

object StreakCalculator {
    fun compute(days: Set<LocalDate>, today: LocalDate): Int {
        if (days.isEmpty()) return 0

        var streak = 0
        var checkDate = today

        // If today is not in days, check if yesterday was. If neither today nor yesterday is present, streak is 0.
        if (!days.contains(today)) {
            val yesterday = today.minusDays(1)
            if (!days.contains(yesterday)) {
                return 0
            } else {
                checkDate = yesterday
            }
        }

        while (days.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
