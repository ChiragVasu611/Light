package com.allinonelighting.navigation

import com.allinonelighting.domain.emergency.EmergencyPattern
import com.allinonelighting.domain.screenlight.ScreenLightMode
import com.allinonelighting.domain.simulator.LightObjectId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Route construction and argument parsing (docs/SCREEN_SPECIFICATION.md §0).
 *
 * Covers the deliberate asymmetry in fallback behaviour: a bad mode or pattern is
 * harmless and falls back to a safe default, but a bad object id must NOT silently
 * show a different 3D model.
 */
class RouteArgumentTest {

    @Test
    fun `screen light routes round-trip every mode`() {
        ScreenLightMode.entries.forEach { mode ->
            val route = Routes.screenLight(mode)
            assertEquals("screenlight?mode=${mode.slug}", route)
            assertEquals(mode, ScreenLightMode.fromSlug(mode.slug))
        }
    }

    @Test
    fun `emergency routes round-trip every pattern`() {
        EmergencyPattern.entries.forEach { pattern ->
            val route = Routes.emergency(pattern)
            assertEquals("emergency?pattern=${pattern.slug}", route)
            assertEquals(pattern, EmergencyPattern.fromSlug(pattern.slug))
        }
    }

    @Test
    fun `simulator viewer routes round-trip every object`() {
        LightObjectId.entries.forEach { id ->
            assertEquals("simulator/viewer/${id.slug}", Routes.simViewer(id))
            assertEquals(id, LightObjectId.fromSlug(id.slug))
        }
    }

    @Test
    fun `unknown mode falls back to white rather than failing`() {
        // A wrong colour is harmless; the screen must always open.
        assertEquals(ScreenLightMode.White, ScreenLightMode.fromSlug("nonsense"))
        assertEquals(ScreenLightMode.White, ScreenLightMode.fromSlug(null))
        assertEquals(ScreenLightMode.White, ScreenLightMode.Default)
    }

    @Test
    fun `unknown pattern falls back to SOS`() {
        assertEquals(EmergencyPattern.Sos, EmergencyPattern.fromSlug("nonsense"))
        assertEquals(EmergencyPattern.Sos, EmergencyPattern.fromSlug(null))
    }

    @Test
    fun `unknown object id does NOT fall back to a different model`() {
        // Showing the wrong light source would be wrong content, not a cosmetic
        // default — the viewer must render a recoverable error instead (FR-3D-14).
        assertNull(LightObjectId.fromSlug("obj_nonexistent"))
        assertNull(LightObjectId.fromSlug(null))
    }

    @Test
    fun `route patterns declare their arguments`() {
        assertEquals("screenlight?mode={mode}", Routes.SCREEN_LIGHT_PATTERN)
        assertEquals("emergency?pattern={pattern}", Routes.EMERGENCY_PATTERN)
        assertEquals("simulator/viewer/{objectId}", Routes.SIM_VIEWER_PATTERN)
    }

    @Test
    fun `argument-carrying routes still hide the bottom navigation`() {
        // Prefix matching, because real routes carry arguments.
        assertTrue(isFullScreenRoute(Routes.screenLight(ScreenLightMode.Night)))
        assertTrue(isFullScreenRoute(Routes.emergency(EmergencyPattern.Strobe)))
        assertTrue(isFullScreenRoute(Routes.simViewer(LightObjectId.Candle)))
        assertTrue(isFullScreenRoute(Routes.CANDLE))
    }

    @Test
    fun `top level destinations keep the bottom navigation`() {
        listOf(Routes.HOME, Routes.TOOLS, Routes.SETTINGS, Routes.SIM_GALLERY)
            .forEach { assertFalse("$it should show the bottom bar", isFullScreenRoute(it)) }
    }

    @Test
    fun `simulator viewer is full screen even though the gallery is not`() {
        // The viewer route starts with "simulator", so a naive prefix check would
        // keep the bottom bar on it. This asserts the distinction holds.
        assertFalse(isFullScreenRoute(Routes.SIM_GALLERY))
        assertTrue(isFullScreenRoute(Routes.simViewer(LightObjectId.Torch)))
    }

    @Test
    fun `only strobe requires the photosensitivity warning`() {
        // FR-EMER-05: SOS and Blink are slow enough not to need the gate.
        assertTrue(EmergencyPattern.Strobe.requiresPhotosensitivityWarning)
        listOf(EmergencyPattern.Sos, EmergencyPattern.Blink, EmergencyPattern.Steady)
            .forEach { assertFalse(it.requiresPhotosensitivityWarning) }
    }

    @Test
    fun `speed preset applies only to patterns with adjustable timing`() {
        // FR-EMER-03: SOS timing is fixed by ITU-R M.1677; Steady has no timing.
        assertTrue(EmergencyPattern.Strobe.usesSpeedPreset)
        assertTrue(EmergencyPattern.Blink.usesSpeedPreset)
        assertFalse(EmergencyPattern.Sos.usesSpeedPreset)
        assertFalse(EmergencyPattern.Steady.usesSpeedPreset)
    }

    @Test
    fun `v1 ships exactly the five documented light sources`() {
        // FR-3D-02 / ADR-012: Screen Light and Emergency Beacon are deferred.
        assertEquals(5, LightObjectId.entries.size)
        assertNull("Screen Light is deferred", LightObjectId.fromSlug("obj_screen"))
        assertNull("Emergency Beacon is deferred", LightObjectId.fromSlug("obj_beacon"))
    }
}
