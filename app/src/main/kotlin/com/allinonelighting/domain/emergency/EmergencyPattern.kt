package com.allinonelighting.domain.emergency

/**
 * Emergency signalling patterns (FR-EMER-01).
 *
 * [usesSpeedPreset] drives FR-EMER-03: the Slow/Medium/Fast control is composed only
 * for patterns whose timing is adjustable. SOS timing is fixed by ITU-R M.1677 and
 * Steady has no timing at all, so neither shows a speed control.
 */
enum class EmergencyPattern(val slug: String, val usesSpeedPreset: Boolean) {
    Sos("sos", usesSpeedPreset = false),
    Strobe("strobe", usesSpeedPreset = true),
    Blink("blink", usesSpeedPreset = true),
    Steady("steady", usesSpeedPreset = false),
    ;

    /** Only the strobe is fast enough to need the photosensitivity gate (FR-EMER-05). */
    val requiresPhotosensitivityWarning: Boolean get() = this == Strobe

    companion object {
        val Default = Sos

        fun fromSlug(slug: String?): EmergencyPattern =
            entries.firstOrNull { it.slug == slug } ?: Default
    }
}
