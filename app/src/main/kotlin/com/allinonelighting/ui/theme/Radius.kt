package com.allinonelighting.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Corner radii — docs/UI_DESIGN_SYSTEM.md §4. */
@Immutable
data class Radius(
    val sm: Dp = 8.dp,
    val md: Dp = 14.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 28.dp,
    val pill: Dp = 999.dp,
)

val LocalRadius = staticCompositionLocalOf { Radius() }
