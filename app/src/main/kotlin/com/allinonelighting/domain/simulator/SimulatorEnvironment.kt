package com.allinonelighting.domain.simulator

/**
 * The two 3D environments (FR-3D-09, docs/THREE_D_LIGHTING_SPEC.md §5).
 *
 * [key] is the persisted value; it must stay equal to the strings the preferences
 * store accepts, which a unit test asserts.
 */
enum class SimulatorEnvironment(val key: String) {
    Dark("dark"),
    Studio("studio"),
    ;

    companion object {
        val Default = Dark

        /** An unknown or missing key falls back to the default environment. */
        fun fromKey(key: String?): SimulatorEnvironment =
            entries.firstOrNull { it.key == key } ?: Default
    }
}

/**
 * The three quality modes (FR-3D-11, docs/THREE_D_LIGHTING_SPEC.md §7).
 *
 * There is no default constant: until the user chooses one, the simulator picks a
 * mode from the device class the first time it opens, so "not chosen" is a real
 * state and is represented as null.
 */
enum class QualityMode(val key: String) {
    Performance("performance"),
    Balanced("balanced"),
    High("high"),
    ;

    companion object {
        fun fromKey(key: String?): QualityMode? = entries.firstOrNull { it.key == key }
    }
}
