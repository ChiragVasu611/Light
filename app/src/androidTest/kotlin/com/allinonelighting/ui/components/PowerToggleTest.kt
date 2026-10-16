package com.allinonelighting.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CMP-POWER behaviour (FR-GLOBAL-02, ADR-004).
 *
 * The decisive property: the control renders the [PowerState] it is given and nothing
 * else. It has no internal toggle state, so it is structurally incapable of showing
 * "On" after a tap that the hardware rejected.
 */
@RunWith(AndroidJUnit4::class)
class PowerToggleTest {

    @get:Rule
    val compose = createComposeRule()

    private val label = "Flashlight"
    private var clicks = 0

    private fun setToggle(state: PowerState) {
        clicks = 0
        compose.setContent {
            AppTheme(reduceMotion = true) {
                PowerToggle(
                    state = state,
                    contentDescriptionText = label,
                    stateDescriptionText = state.name,
                    onClick = { clicks++ },
                )
            }
        }
    }

    private fun stateDescription(): String? =
        compose.onNodeWithContentDescription(label)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    @Test
    fun off_state_is_actionable_and_announces_off() {
        setToggle(PowerState.Off)
        compose.onNodeWithContentDescription(label).assertIsDisplayed().assertHasClickAction()
        assertEquals("Off", stateDescription())
    }

    @Test
    fun on_state_is_actionable_and_announces_on() {
        setToggle(PowerState.On)
        compose.onNodeWithContentDescription(label).assertHasClickAction()
        assertEquals("On", stateDescription())
    }

    @Test
    fun unavailable_state_is_not_actionable() {
        setToggle(PowerState.Unavailable)
        // A device with no flash must not accept taps that can never succeed.
        compose.onNodeWithContentDescription(label).assertHasNoClickAction()
        assertEquals("Unavailable", stateDescription())
    }

    @Test
    fun checking_state_is_not_actionable() {
        setToggle(PowerState.Checking)
        // Before the OS reports torch state there is nothing meaningful to toggle.
        compose.onNodeWithContentDescription(label).assertHasNoClickAction()
    }

    @Test
    fun tapping_an_unavailable_toggle_emits_no_event() {
        setToggle(PowerState.Unavailable)
        compose.onNodeWithContentDescription(label).performClick()
        compose.waitForIdle()
        assertEquals("AC-HOME-02: no torch call from a disabled control", 0, clicks)
    }

    @Test
    fun tapping_an_enabled_toggle_emits_exactly_one_event() {
        setToggle(PowerState.Off)
        compose.onNodeWithContentDescription(label).performClick()
        compose.waitForIdle()
        assertEquals(1, clicks)
    }

    @Test
    fun the_control_has_no_internal_state_so_a_tap_alone_cannot_show_on() {
        // This is the FR-GLOBAL-02 guarantee expressed as a test: the composable is
        // given Off, the user taps, and it stays Off. Only the owning state holder,
        // acting on an OS callback, can move it to On.
        setToggle(PowerState.Off)
        compose.onNodeWithContentDescription(label).performClick()
        compose.waitForIdle()
        assertEquals("a tap must not flip the control optimistically", "Off", stateDescription())
    }

    @Test
    fun state_is_announced_in_words_not_only_colour() {
        // NFR-A11Y: state must never be conveyed by colour alone.
        // One setContent, state driven externally — the same way the real screen
        // drives it from a StateFlow.
        val current = mutableStateOf(PowerState.Off)
        compose.setContent {
            AppTheme(reduceMotion = true) {
                PowerToggle(
                    state = current.value,
                    contentDescriptionText = label,
                    stateDescriptionText = current.value.name,
                    onClick = {},
                )
            }
        }

        PowerState.entries.forEach { state ->
            compose.runOnUiThread { current.value = state }
            compose.waitForIdle()
            assertEquals(state.name, stateDescription())
        }
    }
}
