package com.allinonelighting.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * T-A11Y-02 — every documented colour pairing meets its WCAG 2.1 contrast
 * obligation (NFR-A11Y-02). Ratios are computed here rather than trusted from the
 * table in docs/UI_DESIGN_SYSTEM.md §2.4.
 */
class ContrastTest {

    private fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun ratio(fg: Color, bg: Color): Double {
        val a = luminance(fg)
        val b = luminance(bg)
        val lighter = maxOf(a, b)
        val darker = minOf(a, b)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertContrast(name: String, fg: Color, bg: Color, minimum: Double) {
        val actual = ratio(fg, bg)
        println("CONTRAST  %-34s %.2f:1  (min %.1f)".format(name, actual, minimum))
        assertTrue(
            "$name must be at least $minimum:1 but was %.2f:1".format(actual),
            actual >= minimum,
        )
    }

    private val bodyMinimum = 4.5
    private val largeMinimum = 3.0

    @Test
    fun `body text pairings meet 4_5 to 1`() {
        assertContrast("White on Ink900", Palette.White, Palette.Ink900, bodyMinimum)
        assertContrast("White on Ink700", Palette.White, Palette.Ink700, bodyMinimum)
        assertContrast("White on Ink600", Palette.White, Palette.Ink600, bodyMinimum)
        assertContrast("Slate300 on Ink900", Palette.Slate300, Palette.Ink900, bodyMinimum)
        assertContrast("Slate300 on Ink700", Palette.Slate300, Palette.Ink700, bodyMinimum)
        assertContrast("Slate200 on Ink700", Palette.Slate200, Palette.Ink700, bodyMinimum)
        assertContrast("White on Blue500", Palette.White, Palette.Blue500, bodyMinimum)
        assertContrast("Cyan400 on Ink900", Palette.Cyan400, Palette.Ink900, bodyMinimum)
        assertContrast("White on BlueGlow", Palette.White, Palette.BlueGlow, bodyMinimum)
    }

    @Test
    fun `status and accent colours meet large-text contrast on their surfaces`() {
        assertContrast("Red500 on Ink700", Palette.Red500, Palette.Ink700, largeMinimum)
        assertContrast("Red500 on Ink900", Palette.Red500, Palette.Ink900, largeMinimum)
        assertContrast("Amber400 on Ink700", Palette.Amber400, Palette.Ink700, largeMinimum)
        assertContrast("Green500 on Ink700", Palette.Green500, Palette.Ink700, largeMinimum)
        assertContrast("Blue400 on Ink700", Palette.Blue400, Palette.Ink700, largeMinimum)
    }

    @Test
    fun `primary fill is distinguishable from the app ground`() {
        // WCAG 1.4.11: non-text UI components need 3:1 against adjacent colour.
        // Blue500 is a fill, never body text (docs/UI_DESIGN_SYSTEM.md §2.4).
        assertContrast("Blue500 on Ink900", Palette.Blue500, Palette.Ink900, 3.0)
        assertContrast("Blue500 on Ink700", Palette.Blue500, Palette.Ink700, 3.0)
    }

    @Test
    fun `white on Ink900 is comfortably above the enhanced threshold`() {
        // The app's dominant pairing; AAA (7:1) rather than merely AA.
        assertContrast("White on Ink900 (AAA)", Palette.White, Palette.Ink900, 7.0)
    }
}
