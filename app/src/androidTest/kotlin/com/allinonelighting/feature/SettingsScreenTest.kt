package com.allinonelighting.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.feature.settings.SettingsScreen
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** T-SET-01 — Settings has no Audio group (FR-SET-01, ADR-009). */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setScreen() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                SettingsScreen(
                    onOpenDisplay = {},
                    onOpenPermissions = {},
                    onOpenAbout = {},
                    onReplayIntro = {},
                )
            }
        }
    }

    @Test
    fun settings_has_no_audio_group() {
        setScreen()
        // The concept board showed "Audio — Sounds & Vibration"; the app emits no
        // sound, so such a group would be controls over nothing.
        listOf("Audio", "Sounds", "Vibration").forEach { forbidden ->
            val found = compose.onAllNodesWithText(forbidden, substring = true, ignoreCase = true)
                .fetchSemanticsNodes()
            assertEquals("Settings must not contain '$forbidden'", 0, found.size)
        }
    }

    @Test
    fun settings_shows_the_documented_groups() {
        setScreen()
        compose.onNodeWithText("General").assertIsDisplayed()
        compose.onNodeWithText("Privacy").assertIsDisplayed()
        // "Display" is both a group heading and a row title, so assert on count.
        assertEquals(
            2,
            compose.onAllNodesWithText("Display").fetchSemanticsNodes().size,
        )
    }
}
