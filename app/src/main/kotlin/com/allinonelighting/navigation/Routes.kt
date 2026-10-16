package com.allinonelighting.navigation

/**
 * Navigation graph — docs/SCREEN_SPECIFICATION.md §0.
 *
 * No external deep links are declared in v1.0 (LIM-NO-DEEPLINKS); the query-style
 * arguments below are in-app route arguments only.
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

    fun screenLight(mode: String) = "$SCREEN_LIGHT?$ARG_MODE=$mode"
    fun emergency(pattern: String) = "$EMERGENCY?$ARG_PATTERN=$pattern"
    fun simViewer(objectId: String) = "$SIM_VIEWER/$objectId"
}

/** Destinations that hide the bottom navigation bar. */
val FULL_SCREEN_ROUTES = setOf(
    Routes.SPLASH,
    Routes.ONBOARDING,
    Routes.SCREEN_LIGHT,
    Routes.CANDLE,
    Routes.EMERGENCY,
    Routes.SIM_VIEWER,
)
