package com.allinonelighting.core.device

import com.allinonelighting.MainViewModel
import com.allinonelighting.data.prefs.UserPreferences
import com.allinonelighting.fakes.FakePreferencesRepository
import com.allinonelighting.fakes.FakeSystemAnimationSettings
import com.allinonelighting.fakes.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * NFR-A11Y-04 / FR-SET-03 — the system setting always wins; the in-app toggle can
 * only force reduce-motion on.
 */
class ReduceMotionTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `truth table`() {
        assertFalse(resolveReduceMotion(systemAnimationsDisabled = false, userPreference = null))
        assertFalse(resolveReduceMotion(systemAnimationsDisabled = false, userPreference = false))
        assertTrue(resolveReduceMotion(systemAnimationsDisabled = false, userPreference = true))
        assertTrue(resolveReduceMotion(systemAnimationsDisabled = true, userPreference = null))
        assertTrue(
            "the app must never re-enable animations the user removed system-wide",
            resolveReduceMotion(systemAnimationsDisabled = true, userPreference = false),
        )
        assertTrue(resolveReduceMotion(systemAnimationsDisabled = true, userPreference = true))
    }

    @Test
    fun `app-wide flag follows the in-app setting as it changes`() = runTest {
        val prefs = FakePreferencesRepository()
        val vm = MainViewModel(prefs, FakeSystemAnimationSettings(disabled = false))

        assertFalse(vm.reduceMotion.value)
        prefs.setReduceMotion(true)
        assertTrue(vm.reduceMotion.value)
        prefs.setReduceMotion(false)
        assertFalse(vm.reduceMotion.value)
    }

    @Test
    fun `app-wide flag is on when the system removes animations`() {
        val prefs = FakePreferencesRepository(UserPreferences(reduceMotion = false))
        val vm = MainViewModel(prefs, FakeSystemAnimationSettings(disabled = true))

        assertEquals(true, vm.reduceMotion.value)
    }
}
