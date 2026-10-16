package com.allinonelighting.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.ui.theme.AppTheme
import com.allinonelighting.ui.theme.FeatureAccent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CMP-CARD-HERO, CMP-CARD-TOOL, CMP-ERROR, CMP-LOADING. */
@RunWith(AndroidJUnit4::class)
class CardsAndStatesTest {

    @get:Rule
    val compose = createComposeRule()

    private var clicks = 0

    // --- HeroCard ---------------------------------------------------------

    @Test
    fun hero_card_merges_title_and_status_into_one_accessible_node() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                HeroCard(
                    title = "Phone flashlight",
                    statusLine = "Ready to use",
                    icon = Icons.Filled.FlashlightOn,
                    accent = FeatureAccent.Flashlight,
                    enabled = true,
                    onClick = { clicks++ },
                )
            }
        }
        // FR-HOME-02: the status line is the node's stateDescription, so TalkBack
        // announces hardware changes without the user re-focusing.
        val node = compose.onNodeWithContentDescription("Phone flashlight").fetchSemanticsNode()
        assertEquals("Ready to use", node.config.getOrNull(SemanticsProperties.StateDescription))
    }

    @Test
    fun hero_card_reports_its_live_status_as_state_not_as_a_separate_label() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                HeroCard(
                    title = "Phone flashlight",
                    statusLine = "Not supported on this device",
                    icon = Icons.Filled.FlashlightOn,
                    accent = FeatureAccent.Flashlight,
                    enabled = false,
                    onClick = { clicks++ },
                )
            }
        }
        val node = compose.onNodeWithContentDescription("Phone flashlight").fetchSemanticsNode()
        assertEquals(
            "Not supported on this device",
            node.config.getOrNull(SemanticsProperties.StateDescription),
        )
    }

    @Test
    fun a_disabled_hero_card_is_not_clickable_and_emits_nothing() {
        clicks = 0
        compose.setContent {
            AppTheme(reduceMotion = true) {
                HeroCard(
                    title = "Phone flashlight",
                    statusLine = "Not supported on this device",
                    icon = Icons.Filled.FlashlightOn,
                    accent = FeatureAccent.Flashlight,
                    enabled = false,
                    onClick = { clicks++ },
                )
            }
        }
        // AC-HOME-02: a device with no flash must not accept taps.
        compose.onNodeWithContentDescription("Phone flashlight")
            .assertHasNoClickAction()
            .assertIsNotEnabled()
        compose.onNodeWithContentDescription("Phone flashlight").performClick()
        compose.waitForIdle()
        assertEquals(0, clicks)
    }

    @Test
    fun an_enabled_hero_card_forwards_its_click() {
        clicks = 0
        compose.setContent {
            AppTheme(reduceMotion = true) {
                HeroCard(
                    title = "Phone flashlight",
                    statusLine = "Ready to use",
                    icon = Icons.Filled.FlashlightOn,
                    accent = FeatureAccent.Flashlight,
                    enabled = true,
                    onClick = { clicks++ },
                )
            }
        }
        compose.onNodeWithContentDescription("Phone flashlight")
            .assertHasClickAction()
            .performClick()
        compose.waitForIdle()
        assertEquals(1, clicks)
    }

    // --- ToolCard ---------------------------------------------------------

    @Test
    fun tool_card_exposes_one_merged_label_of_title_and_subtitle() {
        clicks = 0
        compose.setContent {
            AppTheme(reduceMotion = true) {
                ToolCard(
                    title = "Screen light",
                    subtitle = "Colour & brightness",
                    icon = Icons.Filled.FlashlightOn,
                    accent = FeatureAccent.ScreenLight,
                    onClick = { clicks++ },
                )
            }
        }
        compose.onNodeWithContentDescription("Screen light. Colour & brightness")
            .assertIsDisplayed()
            .performClick()
        compose.waitForIdle()
        assertEquals(1, clicks)
    }

    // --- FullScreenState / LoadingState -----------------------------------

    @Test
    fun error_state_names_the_cause_and_offers_one_primary_action() {
        clicks = 0
        compose.setContent {
            AppTheme(reduceMotion = true) {
                FullScreenState(
                    icon = Icons.Filled.WarningAmber,
                    title = "Couldn't load the light source",
                    body = "The model file is missing from this build.",
                    iconTint = MaterialTheme.colorScheme.error,
                    primaryActionLabel = "Retry",
                    onPrimaryAction = { clicks++ },
                )
            }
        }
        compose.onNodeWithText("Couldn't load the light source").assertIsDisplayed()
        compose.onNodeWithText("The model file is missing from this build.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.waitForIdle()
        assertEquals("AC-3D-05: the error must be recoverable", 1, clicks)
    }

    @Test
    fun error_state_renders_without_an_action_when_there_is_no_recovery() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                FullScreenState(
                    icon = Icons.Filled.WarningAmber,
                    title = "Flashlight not available",
                    body = "This device doesn't have a camera flash.",
                    iconTint = MaterialTheme.colorScheme.error,
                )
            }
        }
        // No retry: a missing flash unit cannot be retried into existence.
        compose.onNodeWithText("Flashlight not available").assertIsDisplayed()
        compose.onNodeWithText("This device doesn't have a camera flash.").assertIsDisplayed()
    }

    @Test
    fun loading_state_shows_what_is_loading() {
        compose.setContent {
            AppTheme(reduceMotion = true) { LoadingState(caption = "Loading model…") }
        }
        compose.onNodeWithText("Loading model…").assertIsDisplayed()
    }
}
