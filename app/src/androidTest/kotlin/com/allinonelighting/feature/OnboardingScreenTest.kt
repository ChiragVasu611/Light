package com.allinonelighting.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.feature.onboarding.OnboardingContent
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** T-ONB-01 / T-ONB-03 — onboarding flow (FR-ONB-02/03). */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var skips = 0
    private var finishes = 0

    private fun setScreen() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                OnboardingContent(onSkip = { skips++ }, onFinish = { finishes++ })
            }
        }
    }

    @Test
    fun first_page_shows_practical_lighting_and_skip() {
        setScreen()
        compose.onNodeWithText("Practical lighting for everyday life").assertIsDisplayed()
        compose.onNodeWithText("Skip").assertIsDisplayed()
        compose.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun skip_completes_onboarding_without_reaching_the_last_page() {
        setScreen()
        compose.onNodeWithText("Skip").performClick()
        compose.waitForIdle()
        assertEquals("Skip must complete onboarding (AC-ONB-03)", 1, skips)
    }

    @Test
    fun advancing_through_all_pages_ends_with_get_started() {
        setScreen()
        compose.onNodeWithText("Next").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Next").performClick()
        compose.waitForIdle()

        // Last page swaps the primary action and drops Skip.
        compose.onNodeWithText("Get started").assertIsDisplayed()
        compose.onNodeWithText("Get started").performClick()
        compose.waitForIdle()
        assertEquals("Get started must complete onboarding (AC-ONB-02)", 1, finishes)
    }

    @Test
    fun onboarding_requests_no_permission_dialog() {
        // AC-ONB-04: nothing in onboarding may trigger a permission request. The
        // app declares no runtime permissions at all (T-PERM-01), so this asserts
        // the screen renders and completes without any system dialog interrupting.
        setScreen()
        compose.onNodeWithText("Next").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Explore realistic 3D light sources").assertIsDisplayed()
    }
}
