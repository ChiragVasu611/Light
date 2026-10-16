package com.allinonelighting.ui.components

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CMP-BANNER — the component errors use instead of a snackbar (ADR-020).
 *
 * A banner persists until its cause is resolved; a snackbar auto-dismisses and can
 * be missed, which is unacceptable for a standing condition like "camera in use".
 */
@RunWith(AndroidJUnit4::class)
class BannerTest {

    @get:Rule
    val compose = createComposeRule()

    private var actions = 0

    @Test
    fun banner_shows_title_and_body() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                AppBanner(
                    title = "Camera is in use",
                    severity = BannerSeverity.Error,
                    body = "Another app is using the camera. Close it and try again.",
                )
            }
        }
        compose.onNodeWithText("Camera is in use").assertIsDisplayed()
        compose.onNodeWithText(
            "Another app is using the camera. Close it and try again.",
        ).assertIsDisplayed()
    }

    @Test
    fun banner_action_is_invoked() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                AppBanner(
                    title = "Camera is in use",
                    severity = BannerSeverity.Error,
                    actionLabel = "Retry",
                    onAction = { actions++ },
                )
            }
        }
        compose.onNodeWithText("Retry").performClick()
        compose.waitForIdle()
        assertEquals(1, actions)
    }

    @Test
    fun banner_is_a_polite_live_region_so_it_is_announced_without_stealing_focus() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                AppBanner(title = "Battery is low", severity = BannerSeverity.Warning)
            }
        }
        // A hardware condition changing must reach a screen-reader user without
        // yanking focus away from whatever they were doing.
        val node = compose.onNodeWithText("Battery is low").fetchSemanticsNode()
        val liveRegion = generateSequence(node) { it.parent }
            .mapNotNull { it.config.getOrNull(SemanticsProperties.LiveRegion) }
            .firstOrNull()
        assertNotNull("banner must be a live region", liveRegion)
    }

    @Test
    fun banner_renders_without_a_body_or_action() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                AppBanner(title = "Quality reduced", severity = BannerSeverity.Info)
            }
        }
        compose.onNodeWithText("Quality reduced").assertIsDisplayed()
    }
}
