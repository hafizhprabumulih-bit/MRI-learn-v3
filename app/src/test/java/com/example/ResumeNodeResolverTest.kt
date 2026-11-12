package com.example

import com.example.data.MriModule
import com.example.data.MriNode
import com.example.data.MriRepository
import com.example.util.ResumeNodeResolver
import com.example.viewmodel.MriUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeNodeResolverTest {

    private val testNodeLevel0 = MriNode(
        code = "M01.T0",
        level = 0,
        title = "Tujuan Belajar",
        summaryShort = "",
        detailFull = "",
        analogi = ""
    )

    private val testNodeLevel1 = MriNode(
        code = "M01.T1.1",
        level = 1,
        title = "T1.1 Peran MRI",
        summaryShort = "",
        detailFull = "",
        analogi = ""
    )

    private val testNodeLevel2 = MriNode(
        code = "M01.T2.1",
        level = 2,
        title = "T2.1 Presesi",
        summaryShort = "",
        detailFull = "",
        analogi = ""
    )

    private val testModule1 = MriModule(
        id = "M01",
        code = "M01",
        title = "Fisika Dasar MRI",
        subtitle = "",
        description = "",
        levelBadge = "",
        iconName = "",
        nodes = listOf(testNodeLevel0, testNodeLevel1, testNodeLevel2)
    )

    private val testModule2Node1 = MriNode(
        code = "M04A.T1.1",
        level = 1,
        title = "T1.1 Gradien",
        summaryShort = "",
        detailFull = "",
        analogi = ""
    )

    private val testModule2 = MriModule(
        id = "M04A",
        code = "M04A",
        title = "Pengodean Spasial",
        subtitle = "",
        description = "",
        levelBadge = "",
        iconName = "",
        nodes = listOf(testModule2Node1)
    )

    private val modules = listOf(testModule1, testModule2)

    @Test
    fun `test findNode with existing code`() {
        val found = ResumeNodeResolver.findNode(modules, "M01.T2.1")
        assertNotNull(found)
        assertEquals("M01.T2.1", found?.code)
    }

    @Test
    fun `test findNode with null, blank, non-existing, and legacy code`() {
        assertNull(ResumeNodeResolver.findNode(modules, null))
        assertNull(ResumeNodeResolver.findNode(modules, ""))
        assertNull(ResumeNodeResolver.findNode(modules, "   "))
        assertNull(ResumeNodeResolver.findNode(modules, "NON_EXISTING"))
        assertNull(ResumeNodeResolver.findNode(modules, "M03.T1.1"))
    }

    @Test
    fun `test moduleIdOf via node and prefix`() {
        assertEquals("M01", ResumeNodeResolver.moduleIdOf(modules, "M01.T2.1"))
        assertEquals("M04A", ResumeNodeResolver.moduleIdOf(modules, "M04A.T1.1"))
        assertEquals("M04A", ResumeNodeResolver.moduleIdOf(modules, "M04A.KUIS"))
        assertNull(ResumeNodeResolver.moduleIdOf(modules, null))
        assertNull(ResumeNodeResolver.moduleIdOf(modules, ""))
        assertNull(ResumeNodeResolver.moduleIdOf(modules, "UNKNOWN.KUIS"))
    }

    @Test
    fun `test codeToRememberOnOpen`() {
        // Same module node stored -> preserved
        assertEquals("M01.T2.1", ResumeNodeResolver.codeToRememberOnOpen(testModule1, "M01.T2.1"))

        // Stored node belongs to another module -> first node with level >= 1
        assertEquals("M01.T1.1", ResumeNodeResolver.codeToRememberOnOpen(testModule1, "M04A.T1.1"))

        // Module with only level 0 -> returns level 0 node
        val moduleLevel0Only = testModule1.copy(nodes = listOf(testNodeLevel0))
        assertEquals("M01.T0", ResumeNodeResolver.codeToRememberOnOpen(moduleLevel0Only, "M04A.T1.1"))

        // Empty module -> returns null
        val emptyModule = testModule1.copy(nodes = emptyList())
        assertNull(ResumeNodeResolver.codeToRememberOnOpen(emptyModule, "M01.T1.1"))
    }

    @Test
    fun `test isKnownNode`() {
        assertTrue(ResumeNodeResolver.isKnownNode(modules, "M01.T1.1"))
        assertTrue(ResumeNodeResolver.isKnownNode(modules, "M04A.T1.1"))
        assertFalse(ResumeNodeResolver.isKnownNode(modules, "M04A.KUIS"))
        assertFalse(ResumeNodeResolver.isKnownNode(modules, ""))
        assertFalse(ResumeNodeResolver.isKnownNode(modules, "M03.T1.1"))
    }

    @Test
    fun `test MriUiState lastVisitedNode derived property`() {
        // Valid lastVisitedNodeCode
        val stateValid = MriUiState(modulesList = modules, lastVisitedNodeCode = "M04A.T1.1")
        assertEquals("M04A.T1.1", stateValid.lastVisitedNode.code)

        // Invalid lastVisitedNodeCode -> fallback to level >= 1
        val stateInvalid = MriUiState(modulesList = modules, lastVisitedNodeCode = "INVALID_CODE")
        assertEquals("M01.T1.1", stateInvalid.lastVisitedNode.code)

        // Null lastVisitedNodeCode -> fallback to level >= 1
        val stateNull = MriUiState(modulesList = modules, lastVisitedNodeCode = null)
        assertEquals("M01.T1.1", stateNull.lastVisitedNode.code)

        // Empty modules list -> fallback to default MriRepository node without exception
        val stateEmptyModules = MriUiState(modulesList = emptyList(), lastVisitedNodeCode = null)
        assertNotNull(stateEmptyModules.lastVisitedNode)
        assertEquals(MriRepository.modules[0].nodes[1].code, stateEmptyModules.lastVisitedNode.code)
    }
}
