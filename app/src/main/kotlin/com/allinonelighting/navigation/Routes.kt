package com.allinonelighting.navigation

import com.allinonelighting.domain.emergency.EmergencyPattern
import com.allinonelighting.domain.screenlight.ScreenLightMode
import com.allinonelighting.domain.simulator.LightObjectId

/**
 * Navigation graph — docs/SCREEN_SPECIFICATION.md §0.
 *
 * No external deep links are declared in v1.0 (LIM-NO-DEEPLINKS). The query-style
 * arguments below are in-app route arguments only; nothing here is reachable from
 * outside the app, and the manifest declares no intent-filter beyond LAUNCHER.
 */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // Top-level bottom-nav destinations
    const val HOME = "home"
    const val SIM_GALLERY = "simulator"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"

    // Home children
    const val FLASHLIGHT = "flashlight"
    const val SMART_LIGHT = "smartlight"

    // Full-screen light destinations
    const val SCREEN_LIGHT = "screenlight"
    const val CANDLE = "candle"
    const val EMERGENCY = "emergency"

    // Simulator child
    const val SIM_VIEWER = "simulator/viewer"

    // Settings children
    const val SETTINGS_DISPLAY = "settings/display"
    const val SETTINGS_PERMISSIONS = "settings/permissions"
    const val ABOUT = "about"
    const val LICENCES = "licences"

    const val ARG_MODE = "mode"
    const val ARG_PATTERN = "pattern"
    const val ARG_OBJECT_ID = "objectId"

    /**
     * Route patterns registered with the NavHost.
     *
     * Optional query arguments carry a default, so navigating to the bare route is
     * always valid — `screenlight` and `screenlight?mode=white` are equivalent.
     */
    const val SCREEN_LIGHT_PATTERN = "$SCREEN_LIGHT?$ARG_MODE={$ARG_MODE}"
    const val EMERGENCY_PATTERN = "$EMERGENCY?$ARG_PATTERN={$ARG_PATTERN}"

    /**
     * The object id is a required path argument, not optional: there is no sensible
     * default model to show, and guessing one would display the wrong content.
     */
    const val SIM_VIEWER_PATTERN = "$SIM_VIEWER/{$ARG_OBJECT_ID}"

    fun screenLight(mode: ScreenLightMode) = "$SCREEN_LIGHT?$ARG_MODE=${mode.slug}"

    fun emergency(pattern: EmergencyPattern) = "$EMERGENCY?$ARG_PATTERN=${pattern.slug}"

    fun simViewer(objectId: LightObjectId) = "$SIM_VIEWER/${objectId.slug}"
}

/**
 * Destinations that hide the bottom navigation bar.
 *
 * Matched by prefix, because the real routes carry arguments
 * (`screenlight?mode=night`, `simulator/viewer/obj_torch`).
 */
val FULL_SCREEN_ROUTE_PREFIXES = listOf(
    Routes.SPLASH,
    Routes.ONBOARDING,
    Routes.SCREEN_LIGHT,
    Routes.CANDLE,
    Routes.EMERGENCY,
    Routes.SIM_VIEWER,
)

/**
 * `simulator/viewer` must be tested before `simulator`, or the viewer would be
 * treated as the gallery and keep the bottom bar.
 */
fun isFullScreenRoute(route: String?): Boolean =
    route != null && FULL_SCREEN_ROUTE_PREFIXES.any { route.startsWith(it) }
