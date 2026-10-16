package com.allinonelighting.feature.settings

import com.allinonelighting.data.licences.Licence
import com.allinonelighting.data.licences.reflowLicenceText
import com.allinonelighting.fakes.FakeLicenceTextSource
import com.allinonelighting.fakes.MainDispatcherRule
import com.allinonelighting.feature.settings.licences.LicenceTextState
import com.allinonelighting.feature.settings.licences.LicencesViewModel
import com.allinonelighting.feature.settings.licences.OpenSourceComponents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

/** T-SET-04 (state side) — the licences screen's data (FR-SET-06, AC-SET-04). */
class LicencesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `component list is non-empty and every licence text is loaded`() {
        val source = FakeLicenceTextSource()
        val vm = LicencesViewModel(source)

        val state = vm.state.value
        assertTrue(state.components.isNotEmpty())
        assertEquals(OpenSourceComponents, state.components)
        state.components.map { it.licence }.distinct().forEach { licence ->
            assertEquals(
                LicenceTextState.Loaded(FakeLicenceTextSource.textFor(licence)),
                state.licenceTexts[licence],
            )
        }
    }

    @Test
    fun `each licence is read once however many components share it`() {
        val source = FakeLicenceTextSource()
        LicencesViewModel(source)

        assertEquals(source.reads.distinct(), source.reads)
    }

    @Test
    fun `an unreadable licence text becomes an explained error, components remain`() {
        val source = FakeLicenceTextSource(failing = setOf(Licence.Apache2))
        val vm = LicencesViewModel(source)

        val state = vm.state.value
        assertTrue("attribution must still render", state.components.isNotEmpty())
        assertEquals(
            LicenceTextState.Unavailable(FakeLicenceTextSource.FAILURE_MESSAGE_RES),
            state.licenceTexts[Licence.Apache2],
        )
    }

    @Test
    fun `reflow joins hard-wrapped lines and keeps paragraphs`() {
        val raw = "  First line\n  continues here.\n\n  Second\n  paragraph.\n"
        assertEquals("First line continues here.\n\nSecond paragraph.", reflowLicenceText(raw))
    }

    @Test
    fun `reflow keeps bulleted clauses on their own lines`() {
        val raw = "met:\n\n    * First clause\nwraps here.\n    * Second clause.\n"
        assertEquals(
            "met:\n\n* First clause wraps here.\n* Second clause.",
            reflowLicenceText(raw),
        )
    }

    @Test
    fun `bundled BSD text keeps Google's copyright line and its wording`() {
        val raw = File("src/main/res/raw/licence_bsd_3_clause_protobuf.txt").readText()
        val reflowed = reflowLicenceText(raw)

        assertTrue(reflowed.startsWith("Copyright 2008 Google Inc."))
        val words = { s: String -> s.split(Regex("""\s+""")).filter { it.isNotEmpty() } }
        assertEquals(words(raw), words(reflowed))
    }

    @Test
    fun `bundled Apache text survives reflow with its wording intact`() {
        val raw = File("src/main/res/raw/licence_apache_2_0.txt").readText()
        val reflowed = reflowLicenceText(raw)

        assertTrue(reflowed.startsWith("Apache License Version 2.0, January 2004"))
        assertTrue(reflowed.contains("END OF TERMS AND CONDITIONS"))
        // Same words in the same order — only whitespace may change.
        val words = { s: String -> s.split(Regex("""\s+""")).filter { it.isNotEmpty() } }
        assertEquals(words(raw), words(reflowed))
        assertFalse("no hard wraps left inside paragraphs", reflowed.contains(" \n"))
    }
}
