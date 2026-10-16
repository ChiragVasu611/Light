package com.allinonelighting.domain.screenlight

/**
 * Which variant of the full-screen light is being shown
 * (docs/SCREEN_SPECIFICATION.md SCR-SCREENLIGHT).
 *
 * Each variant persists its own colour and brightness, so Night Light's dim amber
 * does not overwrite the user's Colour Light choice (FR-SCREEN-09, FR-NIGHT-01).
 */
enum class ScreenLightMode(val slug: String) {
    White("white"),
    Color("color"),
    Night("night"),
    ;

    companion object {
        val Default = White

        /** Unknown input falls back to the default rather than throwing. */
        fun fromSlug(slug: String?): ScreenLightMode =
            entries.firstOrNull { it.slug == slug } ?: Default
    }
}
