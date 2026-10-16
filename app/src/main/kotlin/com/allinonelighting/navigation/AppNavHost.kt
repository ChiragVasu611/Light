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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allinonelighting.R
import com.allinonelighting.feature.onboarding.OnboardingScreen
import com.allinonelighting.feature.placeholder.NotYetImplementedScreen
import com.allinonelighting.feature.settings.AboutScreen
import com.allinonelighting.feature.settings.PermissionsScreen
import com.allinonelighting.feature.settings.SettingsScreen
import com.allinonelighting.feature.smartlight.SmartLightScreen

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
    val showBottomNav = currentRoute != null &&
        FULL_SCREEN_ROUTES.none { currentRoute.startsWith(it) }

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
                // Replaced by the real dashboard in Phase 2 (torch control).
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
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_more_title),
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
                NotYetImplementedScreen(
                    title = stringResource(R.string.settings_display_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
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
                NotYetImplementedScreen(
                    title = stringResource(R.string.licences_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
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
            composable(Routes.SCREEN_LIGHT) {
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_screenlight_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            composable(Routes.CANDLE) {
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_candle_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            composable(Routes.EMERGENCY) {
                NotYetImplementedScreen(
                    title = stringResource(R.string.home_emergency_title),
                    modifier = Modifier.safeDrawingPadding(),
                )
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
