package com.allinonelighting.core.device

/**
 * Read access to the system-wide animation setting, so ViewModels can reason about
 * reduce-motion without touching Settings.Global (docs/UI_DESIGN_SYSTEM.md §7).
 */
interface SystemAnimationSettings {
    /** True when the system animator duration scale is 0 ("Remove animations"). */
    fun animationsDisabled(): Boolean
}

/**
 * The effective reduce-motion state (NFR-A11Y-04, FR-SET-03).
 *
 * The system setting always wins; the in-app toggle can only force reduce-motion on.
 * A user who removed animations system-wide must never get them back from inside
 * the app.
 */
fun resolveReduceMotion(systemAnimationsDisabled: Boolean, userPreference: Boolean?): Boolean =
    systemAnimationsDisabled || userPreference == true
