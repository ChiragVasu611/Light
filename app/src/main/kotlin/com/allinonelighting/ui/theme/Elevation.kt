package com.allinonelighting.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Surface/border pairs — docs/UI_DESIGN_SYSTEM.md §4.
 *
 * Material 3 tonal elevation is deliberately unused: on a near-black ground it
 * produces muddy grey. Depth comes from an explicit surface colour plus a hairline.
 * Shadows are not used anywhere.
 */
@Immutable
data class SurfaceStyle(
    val background: Color,
    val border: Color,
    val borderWidth: Dp = 1.dp,
)

@Immutable
data class Surfaces(
    val card: SurfaceStyle = SurfaceStyle(Palette.Ink700, Palette.Ink500.copy(alpha = 0.6f)),
    val cardPressed: SurfaceStyle = SurfaceStyle(Palette.Ink600, Palette.Ink500),
    val cardSelected: SurfaceStyle = SurfaceStyle(Palette.BlueGlow, Palette.Blue500),
    val cardDisabled: SurfaceStyle = SurfaceStyle(Palette.Ink700, Palette.Ink500.copy(alpha = 0.3f)),
    val dialog: SurfaceStyle = SurfaceStyle(Palette.Ink600, Palette.Ink500),
    val bottomNav: SurfaceStyle = SurfaceStyle(Palette.Ink800, Palette.Ink500.copy(alpha = 0.5f)),
)

val LocalSurfaces = androidx.compose.runtime.staticCompositionLocalOf { Surfaces() }
