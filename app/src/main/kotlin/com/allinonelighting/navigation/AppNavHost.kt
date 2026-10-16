package com.allinonelighting.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.allinonelighting.R
import com.allinonelighting.domain.emergency.EmergencyPattern
import com.allinonelighting.domain.screenlight.ScreenLightMode
import com.allinonelighting.domain.simulator.LightObjectId
import com.allinonelighting.feature.onboarding.OnboardingScreen
import com.allinonelighting.feature.placeholder.NotYetImplementedScreen
import com.allinonelighting.feature.settings.AboutScreen
import com.allinonelighting.feature.settings.PermissionsScreen
import com.allinonelighting.feature.settings.SettingsScreen
import com.allinonelighting.feature.settings.display.DisplaySettingsScreen
import com.allinonelighting.feature.settings.licences.LicencesScreen
import com.allinonelighting.feature.smartlight.SmartLightScreen
import com.allinonelighting.feature.tools.ToolDestination
import com.allinonelighting.feature.tools.ToolsScreen

/**
 * Root navigation — docs/SCREEN_SPECIFICATION.md §0.
 *
 * Bottom-nav destinations each keep their own back stack via saveState/restoreState.
 */
@Composable
fun AppNavHost(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = currentRoute != null && !isFullScreenRoute(currentRoute)

    Column(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.HOME) {
                // Real dashboard (hero card, tool grid) lands with torch control in Phase 2.
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(Routes.SIM_GALLERY) {
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_simulator_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(Routes.TOOLS) {
                ToolsScreen(
                    onOpen = { destination ->
                        when (destination) {
                            is ToolDestination.ScreenLight ->
                                navController.navigate(Routes.screenLight(destination.mode))

                            is ToolDestination.Emergency ->
                                navController.navigate(Routes.emergency(destination.pattern))

                            ToolDestination.Candle ->
                                navController.navigate(Routes.CANDLE)
                        }
                    },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenDisplay = { navController.navigate(Routes.SETTINGS_DISPLAY) },
                    onOpenPermissions = { navController.navigate(Routes.SETTINGS_PERMISSIONS) },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onReplayIntro = { navController.navigate(Routes.ONBOARDING) },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(Routes.SETTINGS_DISPLAY) {
                DisplaySettingsScreen(modifier = Modifier.safeDrawingPadding())
            }
            composable(Routes.SETTINGS_PERMISSIONS) {
                PermissionsScreen(modifier = Modifier.safeDrawingPadding())
            }
            composable(Routes.ABOUT) {
                AboutScreen(
                    onOpenLicences = { navController.navigate(Routes.LICENCES) },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            composable(Routes.LICENCES) {
                LicencesScreen(modifier = Modifier.safeDrawingPadding())
            }
            composable(Routes.SMART_LIGHT) {
                SmartLightScreen(modifier = Modifier.safeDrawingPadding())
            }

            composable(Routes.FLASHLIGHT) {
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_flashlight_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            // --- Argument-carrying destinations ---------------------------------

            composable(
                route = Routes.SCREEN_LIGHT_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_MODE) {
                        type = NavType.StringType
                        defaultValue = ScreenLightMode.Default.slug
                    },
                ),
            ) { entry ->
                // An unrecognised mode falls back to white rather than failing: a
                // wrong colour is harmless, and the screen must always open.
                val mode = ScreenLightMode.fromSlug(
                    entry.arguments?.getString(Routes.ARG_MODE),
                )
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_screenlight_title),
                    detail = stringResource(R.string.nav_arg_mode, mode.slug),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(Routes.CANDLE) {
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_candle_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(
                route = Routes.EMERGENCY_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_PATTERN) {
                        type = NavType.StringType
                        defaultValue = EmergencyPattern.Default.slug
                    },
                ),
            ) { entry ->
                // Unknown pattern falls back to SOS — the safe, standard default.
                val pattern = EmergencyPattern.fromSlug(
                    entry.arguments?.getString(Routes.ARG_PATTERN),
                )
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_emergency_title),
                    detail = stringResource(R.string.nav_arg_pattern, pattern.slug),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            composable(
                route = Routes.SIM_VIEWER_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_OBJECT_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                // Unlike the two above, an unknown object id does NOT fall back:
                // showing a different model than the one requested would be wrong
                // content, so it becomes a recoverable error (FR-3D-14).
                val objectId = LightObjectId.fromSlug(
                    entry.arguments?.getString(Routes.ARG_OBJECT_ID),
                )
                if (objectId == null) {
                    NotYetImplementedScreen(
                        title = stringResource(R.string.sim_object_unknown_title),
                        detail = stringResource(R.string.sim_object_unknown_body),
                        modifier = Modifier.safeDrawingPadding(),
                    )
                } else {
                    NotYetImplementedScreen(
                        title = stringResource(R.string.home_simulator_title),
                        detail = stringResource(R.string.nav_arg_object, objectId.slug),
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
            }
        }

        if (showBottomNav) {
            AppBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}
