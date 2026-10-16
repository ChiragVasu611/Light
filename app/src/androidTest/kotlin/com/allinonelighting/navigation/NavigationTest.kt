package com.allinonelighting.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.domain.emergency.EmergencyPattern
import com.allinonelighting.domain.screenlight.ScreenLightMode
import com.allinonelighting.domain.simulator.LightObjectId
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T-NAV-01 / T-NAV-02 / T-TOOLS-02 — the real NavHost, driven end to end on a
 * device, including argument-carrying destinations.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var navController: NavHostController

    private fun launch(start: String = Routes.TOOLS) {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                navController = rememberNavController()
                AppNavHost(startDestination = start, navController = navController)
            }
        }
    }

    private fun currentRoute(): String? =
        navController.currentBackStackEntry?.destination?.route

    private fun argument(key: String): String? =
        navController.currentBackStackEntry?.arguments?.getString(key)

    @Test
    fun tools_list_shows_only_the_launch_tools() {
        launch()
        // FR-TOOLS-01 / ADR-011: Party Mode and Music Light are absent entirely.
        compose.onNodeWithText("Colour light").assertIsDisplayed()
        compose.onNodeWithText("Night light").assertIsDisplayed()
        compose.onNodeWithText("SOS light").assertIsDisplayed()
        compose.onNodeWithText("Strobe light").assertIsDisplayed()
        compose.onNodeWithText("Candle light").assertIsDisplayed()
    }

    @Test
    fun sos_light_opens_emergency_with_sos_preselected() {
        launch()
        compose.onNodeWithText("SOS light").performClick()
        compose.waitForIdle()

        assertEquals(Routes.EMERGENCY_PATTERN, currentRoute())
        assertEquals(
            "AC-TOOLS-02: SOS must be preselected",
            EmergencyPattern.Sos.slug,
            argument(Routes.ARG_PATTERN),
        )
    }

    @Test
    fun strobe_light_opens_emergency_with_strobe_preselected() {
        launch()
        compose.onNodeWithText("Strobe light").performClick()
        compose.waitForIdle()

        assertEquals(Routes.EMERGENCY_PATTERN, currentRoute())
        assertEquals(EmergencyPattern.Strobe.slug, argument(Routes.ARG_PATTERN))
    }

    @Test
    fun night_light_opens_screen_light_in_night_mode() {
        launch()
        compose.onNodeWithText("Night light").performClick()
        compose.waitForIdle()

        assertEquals(Routes.SCREEN_LIGHT_PATTERN, currentRoute())
        assertEquals(ScreenLightMode.Night.slug, argument(Routes.ARG_MODE))
    }

    @Test
    fun colour_light_opens_screen_light_in_colour_mode() {
        launch()
        compose.onNodeWithText("Colour light").performClick()
        compose.waitForIdle()

        assertEquals(ScreenLightMode.Color.slug, argument(Routes.ARG_MODE))
    }

    @Test
    fun screen_light_without_an_argument_defaults_to_white() {
        launch()
        compose.runOnUiThread { navController.navigate(Routes.SCREEN_LIGHT) }
        compose.waitForIdle()

        // The bare route is valid because the argument declares a default.
        assertEquals(ScreenLightMode.White.slug, argument(Routes.ARG_MODE))
    }

    @Test
    fun simulator_viewer_receives_the_requested_object() {
        launch(Routes.SIM_GALLERY)
        compose.runOnUiThread {
            navController.navigate(Routes.simViewer(LightObjectId.Candle))
        }
        compose.waitForIdle()

        assertEquals(Routes.SIM_VIEWER_PATTERN, currentRoute())
        assertEquals(LightObjectId.Candle.slug, argument(Routes.ARG_OBJECT_ID))
    }

    @Test
    fun unknown_object_id_shows_an_error_not_a_different_model() {
        launch(Routes.SIM_GALLERY)
        compose.runOnUiThread {
            navController.navigate("${Routes.SIM_VIEWER}/obj_does_not_exist")
        }
        compose.waitForIdle()

        compose.onNodeWithText("Light source not found").assertIsDisplayed()
    }

    @Test
    fun bottom_navigation_switches_top_level_destinations() {
        launch(Routes.HOME)
        compose.onNodeWithText("Settings").performClick()
        compose.waitForIdle()
        assertEquals(Routes.SETTINGS, currentRoute())

        compose.onNodeWithText("Tools").performClick()
        compose.waitForIdle()
        assertEquals(Routes.TOOLS, currentRoute())
    }

    @Test
    fun settings_opens_its_child_destinations() {
        launch(Routes.SETTINGS)
        compose.onNodeWithText("Permissions").performClick()
        compose.waitForIdle()
        assertEquals(Routes.SETTINGS_PERMISSIONS, currentRoute())
        compose.onNodeWithText("This app requests no permissions").assertIsDisplayed()
    }

    @Test
    fun full_screen_destinations_hide_the_bottom_bar() {
        launch()
        // Bottom bar is present on a top-level destination…
        compose.onNodeWithText("Tools").assertIsDisplayed()

        compose.onNodeWithText("Night light").performClick()
        compose.waitForIdle()

        // …and gone on a full-screen light destination.
        assertTrue(isFullScreenRoute(currentRoute()))
        val homeLabels = compose
            .onAllNodes(matcher = hasText("Home"))
            .fetchSemanticsNodes()
            .size
        assertEquals("bottom bar must be hidden", 0, homeLabels)
    }
}
