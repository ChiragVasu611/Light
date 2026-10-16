package com.allinonelighting.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * T-DS-01 — the Material 3 colour scheme matches docs/UI_DESIGN_SYSTEM.md §2.2
 * field by field, so renaming a token cannot silently change the UI.
 */
class ColorSchemeTest {

    @Test
    fun `color scheme maps every documented slot to its token`() {
        val s = AppColorScheme
        assertEquals("primary", Palette.Blue500, s.primary)
        assertEquals("onPrimary", Palette.White, s.onPrimary)
        assertEquals("primaryContainer", Palette.BlueGlow, s.primaryContainer)
        assertEquals("onPrimaryContainer", Palette.White, s.onPrimaryContainer)
        assertEquals("secondary", Palette.Cyan400, s.secondary)
        assertEquals("onSecondary", Palette.Ink900, s.onSecondary)
        assertEquals("background", Palette.Ink900, s.background)
        assertEquals("onBackground", Palette.White, s.onBackground)
        assertEquals("surface", Palette.Ink700, s.surface)
        assertEquals("onSurface", Palette.White, s.onSurface)
        assertEquals("surfaceVariant", Palette.Ink600, s.surfaceVariant)
        assertEquals("onSurfaceVariant", Palette.Slate300, s.onSurfaceVariant)
        assertEquals("outline", Palette.Ink500, s.outline)
        assertEquals("outlineVariant", Palette.Ink400, s.outlineVariant)
        assertEquals("error", Palette.Red500, s.error)
        assertEquals("onError", Palette.White, s.onError)
    }

    @Test
    fun `reduce motion collapses every duration to zero`() {
        val m = Motion.None
        assertEquals(0, m.fast)
        assertEquals(0, m.standard)
        assertEquals(0, m.emphasis)
        assertEquals(0, m.slow)
    }
}
