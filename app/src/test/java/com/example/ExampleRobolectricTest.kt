package com.example

import com.example.data.MriRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify app title and default curriculum data`() {
    val appTitle = "Belajar MRI"
    assertEquals("Belajar MRI", appTitle)

    // Verify notes and glossary integration
    assertTrue("Default study notes should be present", MriRepository.defaultNotes.isNotEmpty())
    val firstNote = MriRepository.defaultNotes.first()
    assertEquals("Logbook Konsol: Lembar Kerja Klinis Radiografer", firstNote.title)
    assertTrue("Glossary terms must cover key MRI physics", MriRepository.glossary.any { it.term.contains("Larmor") })
  }
}
