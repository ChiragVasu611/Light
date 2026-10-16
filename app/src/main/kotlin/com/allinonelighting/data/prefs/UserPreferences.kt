package com.allinonelighting.data.prefs

/**
 * Everything the app persists. Nothing here is personal data
 * (docs/DATA_PRIVACY_AND_SECURITY.md §2).
 */
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val keepTorchInBackground: Boolean = false,
    val photosensitivityAcknowledged: Boolean = false,
    val reduceMotion: Boolean? = null,
    val simEnvironment: String = ENVIRONMENT_DARK,
    val simQuality: String? = null,
    val simLastObject: String = DEFAULT_OBJECT,
) {
    companion object {
        const val ENVIRONMENT_DARK = "dark"
        const val ENVIRONMENT_STUDIO = "studio"
        const val DEFAULT_OBJECT = "obj_torch"

        const val QUALITY_PERFORMANCE = "performance"
        const val QUALITY_BALANCED = "balanced"
        const val QUALITY_HIGH = "high"

        val VALID_ENVIRONMENTS = setOf(ENVIRONMENT_DARK, ENVIRONMENT_STUDIO)
        val VALID_QUALITIES = setOf(QUALITY_PERFORMANCE, QUALITY_BALANCED, QUALITY_HIGH)
    }
}

/** Per-variant screen-light settings; white/colour/night keep separate values. */
data class ScreenLightPreferences(
    val colorArgb: Int,
    val brightness: Float,
) {
    init {
        require(brightness in MIN_BRIGHTNESS..MAX_BRIGHTNESS) {
            "brightness $brightness out of range"
        }
    }

    companion object {
        const val MIN_BRIGHTNESS = 0.01f
        const val MAX_BRIGHTNESS = 1.0f

        val White = ScreenLightPreferences(0xFFFFFFFF.toInt(), 0.7f)
        val Color = ScreenLightPreferences(0xFF4BD8FF.toInt(), 0.7f)
        val Night = ScreenLightPreferences(0xFFFFB648.toInt(), 0.15f)
    }
}
