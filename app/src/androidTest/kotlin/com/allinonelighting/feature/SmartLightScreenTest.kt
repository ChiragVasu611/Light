package com.allinonelighting.feature

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.feature.smartlight.SmartLightScreen
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T-SMART-01 / T-SMART-02 — the Smart Light screen is informational only.
 *
 * FR-SMART-02 forbids any device-control affordance, even a disabled one. This test
 * asserts their absence from the composition rather than their disabled state.
 */
@RunWith(AndroidJUnit4::class)
class SmartLightScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setScreen() {
        compose.setContent { AppTheme(reduceMotion = true) { SmartLightScreen() } }
    }

    @Test
    fun screen_contains_no_switch_control() {
        setScreen()
        val switches = compose.onAllNodes(matcher = 
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
        ).fetchSemanticsNodes()
        assertEquals("Smart Light must expose no Switch (FR-SMART-02)", 0, switches.size)
    }

    @Test
    fun screen_contains_no_slider_control() {
        setScreen()
        val sliders = compose.onAllNodes(matcher = 
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo),
        ).fetchSemanticsNodes()
        assertEquals("Smart Light must expose no Slider (FR-SMART-02)", 0, sliders.size)
    }

    @Test
    fun screen_is_labelled_coming_soon() {
        setScreen()
        compose.onNodeWithText("COMING SOON", ignoreCase = true).assertIsDisplayed()
    }
}
