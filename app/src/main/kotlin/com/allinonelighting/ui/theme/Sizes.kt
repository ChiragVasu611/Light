package com.allinonelighting.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Component dimensions — docs/UI_DESIGN_SYSTEM.md §5–6.
 *
 * Named so components never carry bare dp literals, and so a size can be changed in
 * one place rather than hunted through the tree.
 */
@Immutable
data class Sizes(
    // Icons
    val iconInline: Dp = 16.dp,
    val iconRow: Dp = 20.dp,
    val iconDefault: Dp = 24.dp,
    val iconHero: Dp = 28.dp,

    // Icon tiles
    val tile: Dp = 40.dp,
    val tileHero: Dp = 48.dp,

    // Interactive
    val minTouchTarget: Dp = 48.dp,
    val powerButton: Dp = 48.dp,
    val powerButtonLarge: Dp = 80.dp,

    // Containers
    val toolCardMinHeight: Dp = 92.dp,
    val rowMinHeight: Dp = 64.dp,
    val segmentMinHeight: Dp = 40.dp,
    val chipHeight: Dp = 34.dp,
    val badgeHeight: Dp = 22.dp,
    val bottomNavHeight: Dp = 64.dp,
    val swatch: Dp = 36.dp,

    // Indicators
    val sliderTrack: Dp = 4.dp,
    val sliderThumb: Dp = 20.dp,
    val navIndicatorHeight: Dp = 3.dp,
    val navIndicatorWidth: Dp = 24.dp,
    val progressIndicator: Dp = 32.dp,
    val emptyStateIcon: Dp = 56.dp,
    val emptyStateCircle: Dp = 96.dp,
    val hairline: Dp = 1.dp,

    /** Text never measures wider than this, however large the window. */
    val maxTextMeasure: Dp = 640.dp,

    /**
     * Single-column content is capped here and centred on Medium and Expanded
     * windows (docs/UI_DESIGN_SYSTEM.md §8). Compact windows are narrower than this,
     * so the cap has no effect there.
     */
    val maxContentWidth: Dp = 560.dp,
)

val LocalSizes = staticCompositionLocalOf { Sizes() }
