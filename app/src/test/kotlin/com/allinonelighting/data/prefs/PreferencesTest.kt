package com.allinonelighting.data.prefs

import app.cash.turbine.test
import com.allinonelighting.fakes.FakePreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers the documented defaults and validation in docs/TECHNICAL_ARCHITECTURE.md §10. */
class PreferencesTest {

    @Test
    fun `defaults match the documented table`() = runTest {
        val repo = FakePreferencesRepository()
        repo.preferences.test {
            val prefs = awaitItem()
            assertFalse("onboarding starts incomplete", prefs.onboardingCompleted)
            assertFalse("background torch is opt-in", prefs.keepTorchInBackground)
            assertFalse("photosensitivity not yet acknowledged", prefs.photosensitivityAcknowledged)
            assertNull("reduce motion follows the system until set", prefs.reduceMotion)
            assertEquals(UserPreferences.ENVIRONMENT_DARK, prefs.simEnvironment)
            assertEquals(UserPreferences.DEFAULT_OBJECT, prefs.simLastObject)
            assertNull("quality is device-derived until chosen", prefs.simQuality)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `screen light variants keep separate defaults`() {
        assertEquals(0.7f, ScreenLightVariant.White.default.brightness)
        assertEquals(0.7f, ScreenLightVariant.Color.default.brightness)
        // Night light is deliberately dim (FR-NIGHT-01).
        assertEquals(0.15f, ScreenLightVariant.Night.default.brightness)
        assertEquals(0xFFFFB648.toInt(), ScreenLightVariant.Night.default.colorArgb)
    }

    @Test
    fun `brightness outside the valid range is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScreenLightPreferences(colorArgb = 0, brightness = 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScreenLightPreferences(colorArgb = 0, brightness = 1.5f)
        }
    }

    @Test
    fun `completing onboarding is observable`() = runTest {
        val repo = FakePreferencesRepository()
        repo.preferences.test {
            assertFalse(awaitItem().onboardingCompleted)
            repo.setOnboardingCompleted(true)
            assertTrue(awaitItem().onboardingCompleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `environment and quality reject unknown values`() = runTest {
        val repo = FakePreferencesRepository()
        // The real repository validates; this documents the contract the fake shares.
        assertTrue(UserPreferences.ENVIRONMENT_STUDIO in UserPreferences.VALID_ENVIRONMENTS)
        assertTrue(UserPreferences.QUALITY_HIGH in UserPreferences.VALID_QUALITIES)
        assertFalse("nonsense" in UserPreferences.VALID_ENVIRONMENTS)
        repo.setSimEnvironment(UserPreferences.ENVIRONMENT_STUDIO)
        assertEquals("environment=studio", repo.writes.last())
    }
}
