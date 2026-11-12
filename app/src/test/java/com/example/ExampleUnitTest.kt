package com.example

import com.example.data.MriRepository
import com.example.data.TissueProperties
import com.example.viewmodel.MriUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLarmorFrequencyCalculation() {
        val b0 = 1.5f
        val gamma1H = MriRepository.GAMMA_1H // 42.58
        val freq1H = gamma1H * b0
        assertEquals(63.87f, freq1H, 0.05f)

        val b0_3T = 3.0f
        val freq3T = gamma1H * b0_3T
        assertEquals(127.74f, freq3T, 0.05f)
    }

    @Test
    fun testContrastWeightingClassification() {
        // T1w parameters (TR short, TE short)
        val stateT1 = MriUiState(trMs = 500f, teMs = 15f)
        assertEquals("Pembobotan T1-Weighted (T1w)", stateT1.weightingType)

        // T2w parameters (TR long, TE long)
        val stateT2 = MriUiState(trMs = 3500f, teMs = 90f)
        assertEquals("Pembobotan T2-Weighted (T2w)", stateT2.weightingType)

        // B-11: PDw parameters (TR >= 2000, TE <= 30) vs Intermediate/Mixed parameters
        val statePd = MriUiState(trMs = 2500f, teMs = 20f)
        assertEquals("Pembobotan Proton Density (PDw)", statePd.weightingType)

        val stateMixed = MriUiState(trMs = 1200f, teMs = 25f)
        assertEquals("Pembobotan Campuran (Mixed / Intermediate)", stateMixed.weightingType)
    }

    @Test
    fun testCalculatedSignalZeroRelaxationTimeGuard_B14() {
        val tissueZero = TissueProperties(
            id = "zero_test",
            name = "Zero Tissue",
            t1Ms = 0f,
            t2Ms = 0f,
            protonDensity = 1.0f,
            description = "Test",
            clinicalNote = "Test"
        )
        val state = MriUiState(trMs = 500f, teMs = 20f, selectedTissue = tissueZero)
        val signal = state.calculatedSignal
        assertTrue("Signal should be finite and not NaN", !signal.isNaN() && signal.isFinite())
        assertTrue("Signal should be clamped within 0..1", signal in 0f..1f)
    }

    @Test
    fun testCompletedCountExcludesOrphanNodeCodes_B10() {
        val state = MriUiState(
            modulesList = MriRepository.modules,
            completedNodeCodes = setOf("M01.T1.1", "M02.T1.1", "ORPHAN.LEGACY.999", "M03A.KUIS")
        )
        assertEquals(2, state.completedCount)
    }

    @Test
    fun testSarClampedIndicator_B16() {
        val highSarState = MriUiState(
            selectedB0 = 7.0f,
            flipAngleDeg = 180f,
            patientWeightKg = 45f,
            isTseSequence = true
        )
        assertTrue("Raw SAR should exceed 5.5 W/kg under extreme 7T TSE settings", highSarState.rawCalculatedSar > 5.5f)
        assertEquals(5.5f, highSarState.calculatedSar, 0.001f)
        assertTrue("isSarClamped flag must be true when clamped at 5.5 W/kg", highSarState.isSarClamped)
    }

    @Test
    fun testCurriculumRepositoryIntegrity() {
        assertTrue("Modules should not be empty", MriRepository.modules.isNotEmpty())
        assertTrue("Glossary should contain key terms", MriRepository.glossary.size >= 8)
        assertTrue("Quizzes should contain questions", MriRepository.quizzes.size >= 5)

        MriRepository.modules.forEach { module ->
            assertNotNull(module.id)
            assertNotNull(module.title)
            assertTrue("Module ${module.id} should have nodes", module.nodes.isNotEmpty())
            module.nodes.forEach { node ->
                assertTrue(node.code.isNotEmpty())
                assertTrue(node.title.isNotEmpty())
                assertTrue(node.summaryShort.isNotEmpty())
                assertTrue(node.analogi.isNotEmpty())
            }
        }
    }

    @Test
    fun testStreakCalendarSevenDaysModuloLogic_NB02() {
        fun activeDaysInWeek(streakDays: Int): Int =
            if (streakDays <= 0) 0 else ((streakDays - 1) % 7) + 1

        assertEquals(0, activeDaysInWeek(0))
        assertEquals(1, activeDaysInWeek(1))
        assertEquals(6, activeDaysInWeek(6))
        assertEquals("Kelipatan 7 hari harus menyalakan seluruh 7 hari", 7, activeDaysInWeek(7))
        assertEquals(1, activeDaysInWeek(8))
        assertEquals("Kelipatan 14 hari harus menyalakan seluruh 7 hari", 7, activeDaysInWeek(14))
        assertEquals("Kelipatan 21 hari harus menyalakan seluruh 7 hari", 7, activeDaysInWeek(21))
    }
}

