package com.allinonelighting.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.data.licences.Licence
import com.allinonelighting.domain.simulator.SimulatorEnvironment
import com.allinonelighting.feature.settings.SettingsScreen
import com.allinonelighting.feature.settings.display.DisplaySettingsContent
import com.allinonelighting.feature.settings.display.DisplaySettingsUiState
import com.allinonelighting.feature.settings.display.ReduceMotionSetting
import com.allinonelighting.feature.settings.licences.LicenceTextState
import com.allinonelighting.feature.settings.licences.LicencesContent
import com.allinonelighting.feature.settings.licences.LicencesUiState
import com.allinonelighting.feature.settings.licences.OpenSourceComponents
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T-A11Y-01 / T-A11Y-03 — touch-target size and labelling (NFR-A11Y-01, NFR-A11Y-03).
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val compose = createComposeRule()

    private val minimumTouchTargetDp = 48

    private val displayState = DisplaySettingsUiState.Content(
        environment = SimulatorEnvironment.Dark,
        quality = null,
        reduceMotion = ReduceMotionSetting.UserChoice(enabled = false),
    )

    private val licencesState = LicencesUiState(
        components = OpenSourceComponents,
        licenceTexts = mapOf(Licence.Apache2 to LicenceTextState.Loading),
    )

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "Settings" to {
            SettingsScreen(
                onOpenDisplay = {},
                onOpenPermissions = {},
                onOpenAbout = {},
                onReplayIntro = {},
            )
        },
        "Display settings" to { DisplaySettingsContent(state = displayState, onEvent = {}) },
        "Licences" to { LicencesContent(state = licencesState) },
    )

    @Test
    fun settings_meets_the_minimum_touch_target() = assertTouchTargets("Settings")

    @Test
    fun display_settings_meets_the_minimum_touch_target() =
        assertTouchTargets("Display settings")

    @Test
    fun licences_meets_the_minimum_touch_target() = assertTouchTargets("Licences")

    @Test
    fun settings_clickables_are_labelled() = assertLabelled("Settings")

    @Test
    fun display_settings_clickables_are_labelled() = assertLabelled("Display settings")

    @Test
    fun licences_clickables_are_labelled() = assertLabelled("Licences")

    private fun setScreen(name: String) {
        val screen = screens.getValue(name)
        compose.setContent {
            AppTheme(reduceMotion = true) {
                Column { screen() }
            }
        }
    }

    private fun assertTouchTargets(name: String) {
        setScreen(name)

        val density = compose.density
        val clickables = collectClickable(compose.onRoot().fetchSemanticsNode())

        assertTrue("$name: expected clickable nodes to assert on", clickables.isNotEmpty())

        val tooSmall = clickables.mapNotNull { node ->
            val w = with(density) { node.size.width.toDp().value }
            val h = with(density) { node.size.height.toDp().value }
            val label = node.config.getOrNull(SemanticsProperties.ContentDescription)
                ?.firstOrNull()
                ?: node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.toString()
                ?: "<unlabelled>"
            if (w + 0.5f < minimumTouchTargetDp || h + 0.5f < minimumTouchTargetDp) {
                "$label is ${w.toInt()}x${h.toInt()}dp"
            } else {
                null
            }
        }

        assertTrue(
            "$name: clickable targets below ${minimumTouchTargetDp}dp (NFR-A11Y-01):\n" +
                tooSmall.joinToString("\n"),
            tooSmall.isEmpty(),
        )
    }

    private fun assertLabelled(name: String) {
        setScreen(name)

        val unlabelled = collectClickable(compose.onRoot().fetchSemanticsNode())
            .filter { node ->
                node.config.getOrNull(SemanticsProperties.ContentDescription).isNullOrEmpty() &&
                    node.config.getOrNull(SemanticsProperties.Text).isNullOrEmpty()
            }

        assertTrue(
            "$name: clickable nodes with no label (NFR-A11Y-03): ${unlabelled.size}",
            unlabelled.isEmpty(),
        )
    }

    private fun collectClickable(
        node: androidx.compose.ui.semantics.SemanticsNode,
    ): List<androidx.compose.ui.semantics.SemanticsNode> {
        val self = if (node.config.contains(SemanticsActions.OnClick)) listOf(node) else emptyList()
        return self + node.children.flatMap { collectClickable(it) }
    }
}
