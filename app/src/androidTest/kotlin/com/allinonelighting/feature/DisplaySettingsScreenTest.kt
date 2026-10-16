package com.allinonelighting.feature

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.domain.simulator.QualityMode
import com.allinonelighting.domain.simulator.SimulatorEnvironment
import com.allinonelighting.feature.settings.display.DisplaySettingsContent
import com.allinonelighting.feature.settings.display.DisplaySettingsEvent
import com.allinonelighting.feature.settings.display.DisplaySettingsUiState
import com.allinonelighting.feature.settings.display.ReduceMotionSetting
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** SCR-SETTINGS-DISPLAY (FR-SET-03) — T-SET-06 UI side. */
@RunWith(AndroidJUnit4::class)
class DisplaySettingsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<DisplaySettingsEvent>()

    private val defaults = DisplaySettingsUiState.Content(
        environment = SimulatorEnvironment.Dark,
        quality = null,
        reduceMotion = ReduceMotionSetting.UserChoice(enabled = false),
    )

    private val reduceMotionSwitch =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

    private fun setScreen(state: DisplaySettingsUiState) {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                DisplaySettingsContent(state = state, onEvent = { events += it })
            }
        }
    }

    @Test
    fun selections_reflect_the_state() {
        setScreen(defaults.copy(environment = SimulatorEnvironment.Studio))

        compose.onNodeWithText("Studio").assertIsSelected()
        compose.onNodeWithText("Dark").assertIsNotSelected()
    }

    @Test
    fun unchosen_quality_selects_nothing_and_says_so() {
        setScreen(defaults)

        listOf("Performance", "Balanced", "High quality").forEach {
            compose.onNodeWithText(it).assertIsNotSelected()
        }
        compose.onNodeWithText("Not chosen yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun chosen_quality_is_selected_and_the_unchosen_note_is_gone() {
        setScreen(defaults.copy(quality = QualityMode.Balanced))

        compose.onNodeWithText("Balanced").assertIsSelected()
        assertEquals(
            0,
            compose.onAllNodesWithText("Not chosen yet", substring = true)
                .fetchSemanticsNodes().size,
        )
    }

    @Test
    fun tapping_segments_sends_events() {
        setScreen(defaults)

        compose.onNodeWithText("Studio").performClick()
        compose.onNodeWithText("High quality").performClick()

        assertEquals(
            listOf(
                DisplaySettingsEvent.SelectEnvironment(SimulatorEnvironment.Studio),
                DisplaySettingsEvent.SelectQuality(QualityMode.High),
            ),
            events,
        )
    }

    @Test
    fun reduce_motion_row_is_a_switch_that_toggles() {
        setScreen(defaults)

        compose.onNode(reduceMotionSwitch).assertIsOff().performClick()

        assertEquals(listOf(DisplaySettingsEvent.SetReduceMotion(true)), events)
    }

    @Test
    fun system_forced_reduce_motion_is_on_disabled_and_explained() {
        setScreen(defaults.copy(reduceMotion = ReduceMotionSetting.ForcedBySystem))

        compose.onNode(reduceMotionSwitch).assertIsOn().assertIsNotEnabled()
        // FR-GLOBAL-01: a disabled control carries its reason.
        compose.onNodeWithText("Animations are turned off in your device settings")
            .assertExists()

        compose.onNode(reduceMotionSwitch).performClick()
        assertTrue("a disabled switch must not send events", events.isEmpty())
    }

    @Test
    fun loading_state_shows_no_values() {
        setScreen(DisplaySettingsUiState.Loading)

        compose.onNodeWithText("Display").assertIsDisplayed()
        assertEquals(0, compose.onAllNodesWithText("Dark").fetchSemanticsNodes().size)
    }
}
