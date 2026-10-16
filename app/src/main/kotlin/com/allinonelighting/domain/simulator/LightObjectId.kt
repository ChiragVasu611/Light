package com.allinonelighting.domain.simulator

/**
 * The five light sources shipping in v1.0 (FR-3D-02, ADR-012).
 *
 * Screen Light and Emergency Beacon are deferred; their specs are retained in
 * docs/THREE_D_LIGHTING_SPEC.md §4.
 */
enum class LightObjectId(val slug: String, val category: LightObjectCategory) {
    Torch("obj_torch", LightObjectCategory.Portable),
    Bulb("obj_bulb", LightObjectCategory.Household),
    TubeLight("obj_tube", LightObjectCategory.Household),
    Candle("obj_candle", LightObjectCategory.Decor),
    LedLamp("obj_led_lamp", LightObjectCategory.Household),
    ;

    companion object {
        val Default = Torch

        /** Null for an unknown id: the viewer must show an error, not a wrong model. */
        fun fromSlug(slug: String?): LightObjectId? = entries.firstOrNull { it.slug == slug }
    }
}

enum class LightObjectCategory(val slug: String) {
    Portable("portable"),
    Household("household"),
    Decor("decor"),
}
