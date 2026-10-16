package com.allinonelighting.feature.settings

import com.allinonelighting.core.log.AppLogger
import com.allinonelighting.data.prefs.UserPreferences
import com.allinonelighting.domain.simulator.QualityMode
import com.allinonelighting.domain.simulator.SimulatorEnvironment
import com.allinonelighting.fakes.FakePreferencesRepository
import com.allinonelighting.fakes.FakeSystemAnimationSettings
import com.allinonelighting.fakes.MainDispatcherRule
import com.allinonelighting.feature.settings.display.DisplaySettingsEvent
import com.allinonelighting.feature.settings.display.DisplaySettingsUiState
import com.allinonelighting.feature.settings.display.DisplaySettingsViewModel
import com.allinonelighting.feature.settings.display.ReduceMotionSetting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * T-SET-06 — Display settings reflect the store and only the store (FR-SET-03,
 * FR-GLOBAL-02 spirit: nothing is shown as saved until it is).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DisplaySettingsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class RecordingLogger : AppLogger {
        val warnings = mutableListOf<String>()
        override fun debug(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String, throwable: Throwable?) {
            warnings += message
        }
        override fun error(tag: String, message: String, throwable: Throwable?) = Unit
    }

    private val logger = RecordingLogger()
    private val prefs = FakePreferencesRepository()
    private val system = FakeSystemAnimationSettings()

    private fun TestScope.subscribedViewModel(): DisplaySettingsViewModel {
        val vm = DisplaySettingsViewModel(prefs, system, logger)
        // WhileSubscribed only produces values while someone collects.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        return vm
    }

    private fun DisplaySettingsViewModel.content(): DisplaySettingsUiState.Content {
        val current = state.value
        assertTrue("expected content, was $current", current is DisplaySettingsUiState.Content)
        return current as DisplaySettingsUiState.Content
    }

    @Test
    fun `defaults are Dark, no quality chosen, reduce motion off`() = runTest {
        val vm = subscribedViewModel()

        val content = vm.content()
        assertEquals(SimulatorEnvironment.Dark, content.environment)
        assertNull("quality must stay unchosen until the user picks one", content.quality)
        assertEquals(ReduceMotionSetting.UserChoice(enabled = false), content.reduceMotion)
    }

    @Test
    fun `selecting an environment persists it and the state follows the store`() = runTest {
        val vm = subscribedViewModel()

        vm.onEvent(DisplaySettingsEvent.SelectEnvironment(SimulatorEnvironment.Studio))

        assertEquals(listOf("environment=studio"), prefs.writes)
        assertEquals(SimulatorEnvironment.Studio, vm.content().environment)
    }

    @Test
    fun `selecting a quality persists its key`() = runTest {
        val vm = subscribedViewModel()

        vm.onEvent(DisplaySettingsEvent.SelectQuality(QualityMode.High))

        assertEquals(listOf("quality=high"), prefs.writes)
        assertEquals(QualityMode.High, vm.content().quality)
    }

    @Test
    fun `reduce motion toggle persists the user's choice`() = runTest {
        val vm = subscribedViewModel()

        vm.onEvent(DisplaySettingsEvent.SetReduceMotion(true))

        assertEquals(listOf("reduceMotion=true"), prefs.writes)
        assertEquals(ReduceMotionSetting.UserChoice(enabled = true), vm.content().reduceMotion)
    }

    @Test
    fun `system animations off forces reduce motion and ignores the toggle`() = runTest {
        system.disabled = true
        val vm = subscribedViewModel()

        assertEquals(ReduceMotionSetting.ForcedBySystem, vm.content().reduceMotion)

        vm.onEvent(DisplaySettingsEvent.SetReduceMotion(false))
        assertTrue("no write may happen while the system forces it", prefs.writes.isEmpty())
        assertEquals(ReduceMotionSetting.ForcedBySystem, vm.content().reduceMotion)
    }

    @Test
    fun `a failed write leaves the stored value showing, not the attempted one`() = runTest {
        prefs.failWrites = true
        val vm = subscribedViewModel()

        vm.onEvent(DisplaySettingsEvent.SelectEnvironment(SimulatorEnvironment.Studio))

        assertEquals("the write was attempted", listOf("environment=studio"), prefs.writes)
        assertEquals(SimulatorEnvironment.Dark, vm.content().environment)
        assertTrue(logger.warnings.any { it.contains("environment") })
    }

    @Test
    fun `unknown stored values fall back instead of crashing`() = runTest {
        val corrupted = FakePreferencesRepository(
            UserPreferences(simEnvironment = "underwater", simQuality = "ultra"),
        )
        val vm = DisplaySettingsViewModel(corrupted, system, logger)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        val content = vm.content()
        assertEquals(SimulatorEnvironment.Dark, content.environment)
        assertNull(content.quality)
    }
}
