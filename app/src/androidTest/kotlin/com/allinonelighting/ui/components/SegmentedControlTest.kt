package com.allinonelighting.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CMP-SEGMENTED — docs/UI_DESIGN_SYSTEM.md §6. */
@RunWith(AndroidJUnit4::class)
class SegmentedControlTest {

    @get:Rule
    val compose = createComposeRule()

    private enum class Beam { Narrow, Medium, Wide }

    private val options = listOf(
        SegmentOption(Beam.Narrow, "Narrow"),
        SegmentOption(Beam.Medium, "Medium"),
        SegmentOption(Beam.Wide, "Wide"),
    )

    private val selected = mutableStateOf(Beam.Medium)
    private var selections = 0

    private fun setControl(enabled: Boolean = true) {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                SegmentedControl(
                    options = options,
                    selected = selected.value,
                    onSelect = { selections++; selected.value = it },
                    enabled = enabled,
                )
            }
        }
    }

    @Test
    fun every_option_is_rendered() {
        setControl()
        options.forEach { compose.onNodeWithText(it.label).assertIsDisplayed() }
    }

    @Test
    fun the_selected_option_reports_itself_as_selected() {
        setControl()
        compose.onNodeWithText("Medium").assertIsSelected()
        compose.onNodeWithText("Narrow").assertIsNotSelected()
        compose.onNodeWithText("Wide").assertIsNotSelected()
    }

    @Test
    fun tapping_an_option_selects_it() {
        setControl()
        compose.onNodeWithText("Wide").performClick()
        compose.waitForIdle()

        assertEquals(1, selections)
        assertEquals(Beam.Wide, selected.value)
        compose.onNodeWithText("Wide").assertIsSelected()
        compose.onNodeWithText("Medium").assertIsNotSelected()
    }

    @Test
    fun each_segment_is_a_radio_button_so_screen_readers_report_n_of_m() {
        setControl()
        val role = compose.onNodeWithText("Narrow")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Role)
        assertEquals(androidx.compose.ui.semantics.Role.RadioButton, role)
    }

    @Test
    fun a_disabled_control_emits_no_selection() {
        setControl(enabled = false)
        compose.onNodeWithText("Wide").performClick()
        compose.waitForIdle()
        assertEquals(0, selections)
    }
}
