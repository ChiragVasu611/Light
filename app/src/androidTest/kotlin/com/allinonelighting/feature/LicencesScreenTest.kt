package com.allinonelighting.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.R
import com.allinonelighting.data.licences.Licence
import com.allinonelighting.feature.settings.licences.LicenceTextState
import com.allinonelighting.feature.settings.licences.LicencesContent
import com.allinonelighting.feature.settings.licences.LicencesUiState
import com.allinonelighting.feature.settings.licences.OpenSourceComponents
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T-SET-04 — the licences screen renders a non-empty list (AC-SET-04).
 *
 * The "includes Filament" half of AC-SET-04 becomes checkable when Filament ships in
 * Phase 3; until then T-SET-07 (LicenceCoverageTest) forbids listing it (ADR-023).
 */
@RunWith(AndroidJUnit4::class)
class LicencesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setScreen(textState: LicenceTextState) {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                LicencesContent(
                    state = LicencesUiState(
                        components = OpenSourceComponents,
                        licenceTexts = mapOf(Licence.Apache2 to textState),
                    ),
                )
            }
        }
    }

    @Test
    fun lists_shipped_components_with_their_licence() {
        setScreen(LicenceTextState.Loading)

        compose.onNodeWithText("AndroidX and Jetpack Compose").assertIsDisplayed()
        compose.onNodeWithText("Kotlin standard library").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Okio").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun full_licence_text_is_shown_on_request() {
        setScreen(LicenceTextState.Loaded("TERMS AND CONDITIONS FOR USE"))

        compose.onNodeWithText("Show full text").performScrollTo().performClick()

        compose.onNodeWithText("TERMS AND CONDITIONS FOR USE").performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithText("Hide full text").assertExists()
    }

    @Test
    fun unreadable_text_shows_an_explained_error() {
        setScreen(LicenceTextState.Unavailable(R.string.licences_text_unavailable))

        compose.onNodeWithText("Show full text").performScrollTo().performClick()

        compose.onNodeWithText("The licence text couldn't be loaded", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
