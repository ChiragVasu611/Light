package com.allinonelighting.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Motion tokens — docs/UI_DESIGN_SYSTEM.md §7.
 *
 * Durations collapse to zero when reduce-motion is active, so composables never need
 * to branch on it themselves (NFR-A11Y-04).
 */
@Immutable
data class Motion(
    val fast: Int = 120,
    val standard: Int = 180,
    val emphasis: Int = 280,
    val slow: Int = 450,
) {
    companion object {
        val LinearOutSlowIn: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
        val FastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

        /** All durations zero — used when reduce-motion is on. */
        val None = Motion(fast = 0, standard = 0, emphasis = 0, slow = 0)
    }
}

val LocalMotion = staticCompositionLocalOf { Motion() }

/**
 * True when the user has asked for reduced motion, either via the system animator
 * duration scale or the in-app setting. Read this instead of touching Settings.Global
 * from a composable.
 */
val LocalReduceMotion = staticCompositionLocalOf { false }
